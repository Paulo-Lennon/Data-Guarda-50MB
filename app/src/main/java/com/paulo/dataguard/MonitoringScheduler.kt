package com.paulo.dataguard

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object MonitoringScheduler {
    private const val PERIODIC_NAME = "mobile-data-monitor-150mb"

    fun start(context: Context) {
        val periodic = PeriodicWorkRequestBuilder<DataUsageWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodic
        )
        checkNow(context)
    }

    fun stop(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_NAME)
    }

    fun checkNow(context: Context) {
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<DataUsageWorker>().build())
    }
}
