package com.paulo.dataguard

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataUsageWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        const val DAILY_LIMIT_BYTES = 150L * 1024L * 1024L
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        if (!Prefs.isEnabled(applicationContext)) {
            return@withContext Result.success()
        }

        if (!PermissionUtils.hasUsageAccess(applicationContext)) {
            return@withContext Result.success()
        }

        if (!PermissionUtils.canDrawOverlays(applicationContext)) {
            return@withContext Result.success()
        }

        try {

            // Se voltou para o Wi-Fi, prepara o alerta novamente.
            if (NetworkUtils.isWifiConnected(applicationContext)) {
                Prefs.setAlertArmed(applicationContext, true)
                return@withContext Result.success()
            }

            val used =
                MobileDataReader.bytesUsedToday(applicationContext)

            if (
                used >= DAILY_LIMIT_BYTES &&
                Prefs.isAlertArmed(applicationContext)
            ) {

                val shown =
                    OverlayAlert.show(applicationContext)

                if (shown) {
                    // Não mostra novamente enquanto não voltar ao Wi-Fi.
                    Prefs.setAlertArmed(
                        applicationContext,
                        false
                    )
                }
            }

            Result.success()

        } catch (_: SecurityException) {

            Result.success()

        } catch (_: Exception) {

            Result.retry()
        }
    }
}