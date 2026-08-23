package com.paulo.dataguard

import android.content.Context
import android.net.TrafficStats

object MobileSessionManager {

    /**
     * Retorna o total de bytes móveis contabilizados pelo Android
     * desde a última inicialização do aparelho.
     */
    fun getTotalMobileBytes(): Long {
        val received = TrafficStats.getMobileRxBytes()
        val transmitted = TrafficStats.getMobileTxBytes()

        if (
            received == TrafficStats.UNSUPPORTED.toLong() ||
            transmitted == TrafficStats.UNSUPPORTED.toLong()
        ) {
            return -1L
        }

        return received + transmitted
    }

    /**
     * Inicia uma nova sessão móvel caso ainda não exista uma ativa.
     */
    fun ensureSessionStarted(context: Context) {
        if (Prefs.isMobileSessionActive(context)) {
            return
        }

        val totalMobileBytes = getTotalMobileBytes()

        if (totalMobileBytes >= 0L) {
            Prefs.startMobileSession(
                context,
                totalMobileBytes
            )
        }
    }

    /**
     * Retorna quantos bytes foram consumidos
     * desde o início da sessão atual.
     */
    fun getSessionUsedBytes(context: Context): Long {
        if (!Prefs.isMobileSessionActive(context)) {
            return 0L
        }

        val baseBytes =
            Prefs.getSessionBaseBytes(context)

        val currentBytes =
            getTotalMobileBytes()

        if (
            baseBytes < 0L ||
            currentBytes < 0L ||
            currentBytes < baseBytes
        ) {
            return 0L
        }

        return currentBytes - baseBytes
    }

    /**
     * Encerra a sessão móvel atual.
     * Será chamado quando entrar no Wi-Fi.
     */
    fun resetSession(context: Context) {
        Prefs.resetMobileSession(context)
    }

    /**
     * Reinicia a referência da sessão no valor atual.
     *
     * Isso será usado depois que o usuário tocar
     * em "Fechar" no alerta.
     */
    fun restartSessionFromNow(context: Context) {
        val currentBytes =
            getTotalMobileBytes()

        if (currentBytes >= 0L) {
            Prefs.startMobileSession(
                context,
                currentBytes
            )
        }
    }
}