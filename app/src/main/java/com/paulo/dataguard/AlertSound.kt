package com.paulo.dataguard

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager

object AlertSound {

    private var ringtone: Ringtone? = null

    fun play(context: Context) {
        try {
            // Garante que um alerta anterior seja interrompido.
            stop()

            // Usa o toque padrão configurado para chamadas do celular.
            val uri = RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_RINGTONE
            )

            ringtone = RingtoneManager.getRingtone(
                context.applicationContext,
                uri
            )

            ringtone?.play()

        } catch (_: Exception) {
            ringtone = null
        }
    }

    fun stop() {
        try {
            ringtone?.stop()
        } catch (_: Exception) {
        }

        ringtone = null
    }
}