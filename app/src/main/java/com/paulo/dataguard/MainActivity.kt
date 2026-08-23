package com.paulo.dataguard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.paulo.dataguard.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUsagePermission.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        binding.btnOverlayPermission.setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        binding.btnToggleMonitoring.setOnClickListener {
            val enabling = !Prefs.isEnabled(this)
            if (enabling) {
                if (!PermissionUtils.hasUsageAccess(this) || !PermissionUtils.canDrawOverlays(this)) {
                    updateUi()
                    return@setOnClickListener
                }
                Prefs.setEnabled(this, true)
                MonitoringScheduler.start(this)
            } else {
                Prefs.setEnabled(this, false)
                MonitoringScheduler.stop(this)
            }
            updateUi()
        }

        updateUi()
    }

    override fun onResume() {
        super.onResume()
        updateUi()
        refreshUsage()
    }

    private fun updateUi() {
        val hasUsage = PermissionUtils.hasUsageAccess(this)
        val hasOverlay = PermissionUtils.canDrawOverlays(this)
        val enabled = Prefs.isEnabled(this)

        binding.btnUsagePermission.text = if (hasUsage) {
            "✓ Acesso ao uso de dados permitido"
        } else {
            "1. Permitir acesso ao uso de dados"
        }

        binding.btnOverlayPermission.text = if (hasOverlay) {
            "✓ Exibição sobre outros apps permitida"
        } else {
            "2. Permitir exibir sobre outros apps"
        }

        binding.btnToggleMonitoring.text = if (enabled) "Desativar monitoramento" else "Ativar monitoramento"
        binding.tvStatus.text = when {
            enabled -> "Monitoramento: ativo"
            !hasUsage || !hasOverlay -> "Monitoramento: aguardando permissões"
            else -> "Monitoramento: desativado"
        }
    }

    private fun refreshUsage() {
        if (!PermissionUtils.hasUsageAccess(this)) {
            binding.tvUsage.text = "Consumo móvel hoje: permissão necessária"
            return
        }

        lifecycleScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                runCatching { MobileDataReader.bytesUsedToday(this@MainActivity) }.getOrNull()
            }
            binding.tvUsage.text = if (bytes == null) {
                "Consumo móvel hoje: indisponível"
            } else {
                val mb = bytes / (1024.0 * 1024.0)
                String.format(Locale.getDefault(), "Consumo móvel hoje: %.1f MB", mb)
            }
            // Verificação imediata do limite
            if (
                bytes != null &&
                bytes >= DataUsageWorker.DAILY_LIMIT_BYTES &&
                PermissionUtils.canDrawOverlays(this@MainActivity)
            ) {
                OverlayAlert.show(this@MainActivity)
            }
        }
    }
}
