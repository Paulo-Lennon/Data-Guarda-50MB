package com.paulo.dataguard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class DataGuardService : Service() {

    companion object {

        private const val CHANNEL_ID =
            "data_guard_monitor"

        private const val NOTIFICATION_ID =
            150

        /*
         * TESTE:
         * deixe em 5 MB por enquanto.
         *
         * Quando confirmarmos que tudo funciona,
         * voltamos definitivamente para 150 MB.
         */
        const val LIMIT_BYTES =
            3L * 1024L * 1024L
    }

    private lateinit var connectivityManager:
            ConnectivityManager

    private lateinit var networkStatsManager:
            NetworkStatsManager

    private val mainHandler =
        Handler(Looper.getMainLooper())

    private var usageCallback:
            NetworkStatsManager.UsageCallback? = null

    /*
     * Impede que dois alertas sejam apresentados
     * simultaneamente.
     */
    @Volatile
    private var alertActive = false


    /*
     * Escuta alterações de conectividade.
     */
    private val networkCallback =
        object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(
                network: Network
            ) {
                checkCurrentNetwork()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                checkCurrentNetwork()
            }

            override fun onLost(
                network: Network
            ) {
                checkCurrentNetwork()
            }
        }


    override fun onCreate() {
        super.onCreate()

        connectivityManager =
            getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager

        networkStatsManager =
            getSystemService(
                Context.NETWORK_STATS_SERVICE
            ) as NetworkStatsManager

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )

        /*
         * Monitora imediatamente mudanças:
         *
         * Wi-Fi → dados móveis
         * dados móveis → Wi-Fi
         */
        connectivityManager
            .registerDefaultNetworkCallback(
                networkCallback
            )

        checkCurrentNetwork()
    }


    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        Prefs.setEnabled(
            this,
            true
        )

        checkCurrentNetwork()

        /*
         * Se o processo do serviço for encerrado
         * pelo Android, o sistema poderá recriá-lo.
         */
        return START_STICKY
    }


    /*
     * Verifica qual conexão está ativa.
     */
    private fun checkCurrentNetwork() {

        when (
            NetworkUtils.getNetworkType(this)
        ) {

            /*
             * Wi-Fi conectado:
             *
             * cancela a contagem atual
             * e deixa uma futura sessão móvel
             * começar novamente do zero.
             */
            NetworkUtils.NetworkType.WIFI -> {

                unregisterUsageCallback()

                AlertSound.stop()

                OverlayAlert.close()

                alertActive = false

                MobileSessionManager
                    .resetSession(this)
            }


            /*
             * Dados móveis:
             *
             * inicia uma nova sessão caso
             * ainda não exista uma.
             */
            NetworkUtils.NetworkType.MOBILE -> {

                MobileSessionManager
                    .ensureSessionStarted(this)

                if (!alertActive) {
                    registerUsageCallback()
                }
            }


            /*
             * Sem conexão.
             */
            NetworkUtils.NetworkType.NONE -> {

                unregisterUsageCallback()
            }
        }
    }


    /*
     * Registra no Android o limite que queremos
     * acompanhar.
     */
    private fun registerUsageCallback() {

        /*
         * Não registra outro callback se
         * já existe um ativo.
         */
        if (usageCallback != null) {
            return
        }

        /*
         * Não registra outro enquanto o
         * alerta estiver na tela.
         */
        if (alertActive) {
            return
        }

        /*
         * Certifica que existe uma sessão móvel.
         */
        MobileSessionManager
            .ensureSessionStarted(this)

        val callback =
            object :
                NetworkStatsManager.UsageCallback() {

                override fun onThresholdReached(
                    networkType: Int,
                    subscriberId: String?
                ) {

                    /*
                     * Chamamos a interface através
                     * da Main Thread.
                     */
                    mainHandler.post {
                        showLimitAlert()
                    }
                }
            }

        usageCallback = callback

        try {

            /*
             * IMPORTANTE:
             *
             * Registramos um NOVO limite completo
             * para esta sessão.
             *
             * Depois que o usuário fechar o alerta,
             * registraremos novamente outros 5 MB
             * durante os testes ou 150 MB na versão final.
             */
            networkStatsManager
                .registerUsageCallback(
                    ConnectivityManager.TYPE_MOBILE,
                    null,
                    LIMIT_BYTES,
                    callback
                )

        } catch (_: Exception) {

            usageCallback = null
        }
    }


    /*
     * Remove o callback atual.
     */
    private fun unregisterUsageCallback() {

        usageCallback?.let { callback ->

            try {

                networkStatsManager
                    .unregisterUsageCallback(
                        callback
                    )

            } catch (_: Exception) {
            }
        }

        usageCallback = null
    }


    /*
     * Limite atingido.
     */
    private fun showLimitAlert() {

        /*
         * Proteção contra callbacks duplicados.
         */
        if (alertActive) {
            return
        }

        /*
         * Se por algum motivo já voltamos ao Wi-Fi,
         * não devemos alertar.
         */
        if (
            NetworkUtils.getNetworkType(this) !=
            NetworkUtils.NetworkType.MOBILE
        ) {
            return
        }

        alertActive = true

        unregisterUsageCallback()

        /*
         * Primeiro tentamos apresentar
         * o pop-up.
         */
        val shown =
            OverlayAlert.show(this) {

                /*
                 * Usuário pressionou FECHAR.
                 */

                AlertSound.stop()

                alertActive = false

                /*
                 * O consumo atual passa a ser
                 * o novo "zero".
                 */
                MobileSessionManager
                    .restartSessionFromNow(this)

                /*
                 * Só começamos outra contagem
                 * se ainda estivermos usando
                 * dados móveis.
                 */
                if (
                    NetworkUtils.getNetworkType(this) ==
                    NetworkUtils.NetworkType.MOBILE
                ) {

                    /*
                     * Pequeno atraso apenas para permitir
                     * que o Android finalize completamente
                     * o callback anterior antes de
                     * registrar o próximo.
                     */
                    mainHandler.postDelayed(
                        {
                            registerUsageCallback()
                        },
                        1000L
                    )
                }
            }


        if (shown) {

            /*
             * Só toca se o pop-up realmente
             * apareceu.
             */
            AlertSound.play(this)

        } else {

            /*
             * Se não conseguiu abrir o overlay,
             * não deixamos o serviço preso
             * em estado de alerta.
             */
            AlertSound.stop()

            alertActive = false

            if (
                NetworkUtils.getNetworkType(this) ==
                NetworkUtils.NetworkType.MOBILE
            ) {

                mainHandler.postDelayed(
                    {
                        registerUsageCallback()
                    },
                    1000L
                )
            }
        }
    }


    /*
     * Canal da notificação permanente do serviço.
     */
    private fun createNotificationChannel() {

        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                "Monitoramento de dados",
                NotificationManager.IMPORTANCE_LOW
            )

        channel.description =
            "Mantém o monitoramento de dados móveis ativo."

        manager.createNotificationChannel(
            channel
        )
    }


    /*
     * Notificação visível enquanto o
     * monitoramento estiver ativo.
     */
    private fun createNotification():
            Notification {

        return NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle(
                "Data Guard 150"
            )
            .setContentText(
                "Monitoramento de dados ativo"
            )
            .setSmallIcon(
                R.mipmap.ic_launcher
            )
            .setOngoing(true)
            .setPriority(
                NotificationCompat.PRIORITY_LOW
            )
            .build()
    }


    override fun onDestroy() {

        unregisterUsageCallback()

        AlertSound.stop()

        OverlayAlert.close()

        alertActive = false

        try {

            connectivityManager
                .unregisterNetworkCallback(
                    networkCallback
                )

        } catch (_: Exception) {
        }

        super.onDestroy()
    }


    override fun onBind(
        intent: Intent?
    ): IBinder? = null
}