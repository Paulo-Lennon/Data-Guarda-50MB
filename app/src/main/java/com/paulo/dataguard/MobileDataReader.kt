package com.paulo.dataguard

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import java.time.LocalDate
import java.time.ZoneId

object MobileDataReader {
    fun bytesUsedToday(context: Context): Long {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = System.currentTimeMillis()

        val manager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val bucket = manager.querySummaryForDevice(
            ConnectivityManager.TYPE_MOBILE,
            null,
            start,
            end
        )
        return bucket.rxBytes + bucket.txBytes
    }
}
