package com.paulo.dataguard

import android.content.Context

object Prefs {

    private const val FILE = "data_guard_prefs"

    private const val KEY_ENABLED = "enabled"
    private const val KEY_ALERT_ARMED = "alert_armed"

    // Valor total de dados móveis no momento
    // em que a nova sessão começou.
    private const val KEY_SESSION_BASE_BYTES = "session_base_bytes"

    // Indica se estamos atualmente contando
    // uma sessão de dados móveis.
    private const val KEY_MOBILE_SESSION_ACTIVE = "mobile_session_active"


    fun isEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, false)


    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }


    fun isAlertArmed(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ALERT_ARMED, true)


    fun setAlertArmed(context: Context, armed: Boolean) {
        prefs(context)
            .edit()
            .putBoolean(KEY_ALERT_ARMED, armed)
            .apply()
    }


    fun getSessionBaseBytes(context: Context): Long =
        prefs(context).getLong(KEY_SESSION_BASE_BYTES, -1L)


    fun setSessionBaseBytes(context: Context, bytes: Long) {
        prefs(context)
            .edit()
            .putLong(KEY_SESSION_BASE_BYTES, bytes)
            .apply()
    }


    fun isMobileSessionActive(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MOBILE_SESSION_ACTIVE, false)


    fun setMobileSessionActive(context: Context, active: Boolean) {
        prefs(context)
            .edit()
            .putBoolean(KEY_MOBILE_SESSION_ACTIVE, active)
            .apply()
    }


    /**
     * Usado quando entra no Wi-Fi.
     *
     * A sessão móvel anterior é encerrada e,
     * na próxima vez que entrar nos dados móveis,
     * uma nova contagem começará do zero.
     */
    fun resetMobileSession(context: Context) {
        prefs(context)
            .edit()
            .putLong(KEY_SESSION_BASE_BYTES, -1L)
            .putBoolean(KEY_MOBILE_SESSION_ACTIVE, false)
            .putBoolean(KEY_ALERT_ARMED, true)
            .apply()
    }


    /**
     * Inicia uma nova sessão de dados móveis.
     *
     * totalMobileBytes representa o contador total
     * de dados móveis informado pelo Android naquele momento.
     */
    fun startMobileSession(
        context: Context,
        totalMobileBytes: Long
    ) {
        prefs(context)
            .edit()
            .putLong(KEY_SESSION_BASE_BYTES, totalMobileBytes)
            .putBoolean(KEY_MOBILE_SESSION_ACTIVE, true)
            .putBoolean(KEY_ALERT_ARMED, true)
            .apply()
    }


    private fun prefs(context: Context) =
        context.getSharedPreferences(
            FILE,
            Context.MODE_PRIVATE
        )
}