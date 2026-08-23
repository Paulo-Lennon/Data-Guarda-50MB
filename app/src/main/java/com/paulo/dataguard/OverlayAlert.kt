package com.paulo.dataguard

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button

object OverlayAlert {
    fun show(context: Context): Boolean {
        if (!PermissionUtils.canDrawOverlays(context)) return false

        val appContext = context.applicationContext
        val windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val view = LayoutInflater.from(appContext).inflate(R.layout.view_usage_alert, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 120
        }

        var added = false
        try {
            windowManager.addView(view, params)
            added = true
        } catch (_: Exception) {
            return false
        }

        view.findViewById<Button>(R.id.btnCloseAlert).setOnClickListener {
            if (added) {
                try {
                    windowManager.removeView(view)
                } catch (_: Exception) {
                    // View já removida pelo sistema/OEM.
                }
                added = false
            }
        }
        return true
    }
}
