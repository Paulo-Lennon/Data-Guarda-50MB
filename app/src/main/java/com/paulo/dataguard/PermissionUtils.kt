package com.paulo.dataguard

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import android.provider.Settings

object PermissionUtils {
    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)
}
