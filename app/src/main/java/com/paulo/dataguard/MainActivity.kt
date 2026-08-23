package com.paulo.dataguard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.paulo.dataguard.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /*
     * Android 13+ exige permissão para mostrar notificações.
     * O Foreground Service usa uma notificação permanente
     * enquanto o monitoramento estiver ativo.
     */
    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            startDataGuardService()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
         * Permissão para consultar estatísticas de uso.
         */
        binding.btnUsagePermission.setOnClickListener {
            startActivity(
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            )
        }

        /*
         * Permissão para mostrar o alerta por cima
         * de outros aplicativos.
         */
        binding.btnOverlayPermission.setOnClickListener {

            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )

            startActivity(intent)
        }

        /*
         * Liga ou desliga o monitoramento.
         */
        binding.btnToggleMonitoring.setOnClickListener {

            val enabling = !Prefs.isEnabled(this)

            if (enabling) {

                if (
                    !PermissionUtils.hasUsageAccess(this) ||
                    !PermissionUtils.canDrawOverlays(this)
                ) {

                    updateUi()
                    return@setOnClickListener
                }

                enableMonitoring()

            } else {

                disableMonitoring()
            }
        }

        /*
         * Se o usuário já havia deixado o monitoramento ativo
         * e abriu novamente o aplicativo, garantimos que o
         * serviço esteja rodando.
         */
        if (Prefs.isEnabled(this)) {
            requestNotificationPermissionAndStartService()
        }

        updateUi()
        refreshUsage()
    }

    override fun onResume() {
        super.onResume()

        updateUi()
        refreshUsage()
    }

    /*
     * ATIVAR MONITORAMENTO
     */
    private fun enableMonitoring() {

        Prefs.setEnabled(this, true)

        requestNotificationPermissionAndStartService()

        updateUi()
    }

    /*
     * DESATIVAR MONITORAMENTO
     */
    private fun disableMonitoring() {

        Prefs.setEnabled(this, false)

        stopService(
            Intent(
                this,
                DataGuardService::class.java
            )
        )

        MobileSessionManager.resetSession(this)

        updateUi()
        refreshUsage()
    }

    /*
     * Android 13 ou superior:
     * solicita permissão para mostrar notificações.
     */
    private fun requestNotificationPermissionAndStartService() {

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )

        } else {

            startDataGuardService()
        }
    }

    /*
     * Inicia o Foreground Service.
     */
    private fun startDataGuardService() {

        val serviceIntent =
            Intent(
                this,
                DataGuardService::class.java
            )

        ContextCompat.startForegroundService(
            this,
            serviceIntent
        )
    }

    /*
     * Atualiza textos e botões da tela.
     */
    private fun updateUi() {

        val hasUsage =
            PermissionUtils.hasUsageAccess(this)

        val hasOverlay =
            PermissionUtils.canDrawOverlays(this)

        val enabled =
            Prefs.isEnabled(this)

        binding.btnUsagePermission.text =
            if (hasUsage) {

                "✓ Acesso ao uso de dados permitido"

            } else {

                "1. Permitir acesso ao uso de dados"
            }

        binding.btnOverlayPermission.text =
            if (hasOverlay) {

                "✓ Exibição sobre outros apps permitida"

            } else {

                "2. Permitir exibir sobre outros apps"
            }

        binding.btnToggleMonitoring.text =
            if (enabled) {

                "Desativar monitoramento"

            } else {

                "Ativar monitoramento"
            }

        binding.tvStatus.text =
            when {

                enabled ->
                    "Monitoramento: ativo"

                !hasUsage || !hasOverlay ->
                    "Monitoramento: aguardando permissões"

                else ->
                    "Monitoramento: desativado"
            }
    }

    /*
     * Agora mostramos o consumo da SESSÃO,
     * e não mais o consumo total do dia.
     */
    private fun refreshUsage() {

        when (
            NetworkUtils.getNetworkType(this)
        ) {

            NetworkUtils.NetworkType.WIFI -> {

                binding.tvUsage.text =
                    "Consumo da sessão: 0.0 MB • Wi-Fi conectado"
            }

            NetworkUtils.NetworkType.MOBILE -> {

                val bytes =
                    MobileSessionManager
                        .getSessionUsedBytes(this)

                val mb =
                    bytes / (1024.0 * 1024.0)

                binding.tvUsage.text =
                    String.format(
                        Locale.getDefault(),
                        "Consumo da sessão: %.1f MB",
                        mb
                    )
            }

            NetworkUtils.NetworkType.NONE -> {

                binding.tvUsage.text =
                    "Consumo da sessão: 0.0 MB • Sem conexão"
            }
        }
    }
}