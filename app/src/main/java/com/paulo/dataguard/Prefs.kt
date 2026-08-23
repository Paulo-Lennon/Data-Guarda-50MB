package com.paulo.dataguard

import android.content.Context

object Prefs {
    private const val FILE = "data_guard_prefs"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_ALERT_ARMED = "alert_armed"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }

    fun isAlertArmed(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean(KEY_ALERT_ARMED, true)

    fun setAlertArmed(context: Context, armed: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ALERT_ARMED, armed)
            .apply()
    }
}