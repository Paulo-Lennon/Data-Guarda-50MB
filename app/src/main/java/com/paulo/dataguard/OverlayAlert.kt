package com.paulo.dataguard

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button

object OverlayAlert {

    private var currentView: android.view.View? = null
    private var currentWindowManager: WindowManager? = null

    fun show(
        context: Context,
        onClosed: (() -> Unit)? = null
    ): Boolean {

        if (!PermissionUtils.canDrawOverlays(context)) {
            return false
        }

        val appContext = context.applicationContext

        // Evita criar mais de um pop-up ao mesmo tempo.
        if (currentView != null) {
            return true
        }

        val windowManager =
            appContext.getSystemService(
                Context.WINDOW_SERVICE
            ) as WindowManager

        val view =
            LayoutInflater.from(appContext)
                .inflate(
                    R.layout.view_usage_alert,
                    null
                )

        val params =
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {

                // Centraliza o alerta horizontal e verticalmente
                gravity = Gravity.CENTER

                y = 0
            }

        try {

            windowManager.addView(
                view,
                params
            )

            currentView = view
            currentWindowManager = windowManager

        } catch (_: Exception) {

            currentView = null
            currentWindowManager = null

            return false
        }

        view.findViewById<Button>(
            R.id.btnCloseAlert
        ).setOnClickListener {

            AlertSound.stop()

            close()

            onClosed?.invoke()
        }

        return true
    }

    fun close() {

        val view = currentView
        val windowManager = currentWindowManager

        if (
            view != null &&
            windowManager != null
        ) {

            try {

                windowManager.removeView(view)

            } catch (_: Exception) {
            }
        }

        currentView = null
        currentWindowManager = null
    }
}