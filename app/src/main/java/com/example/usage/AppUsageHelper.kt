package com.example.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import android.provider.Settings
import com.example.data.entity.AppUsageEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AppDetailUsage(
    val packageName: String,
    val appName: String,
    val totalTimeSeconds: Long,
    val reopenCount: Int,
    val lastUsedTimestamp: Long = 0L,
    val dailyLimitMinutes: Int = 0, // 0 = no limit
    val isBlockedDuringFocus: Boolean = false,
    val category: String = "App"
) {
    val isOverLimit: Boolean get() = dailyLimitMinutes > 0 && (totalTimeSeconds / 60) >= dailyLimitMinutes
    val percentOfLimit: Float get() = if (dailyLimitMinutes > 0) ((totalTimeSeconds / 60f) / dailyLimitMinutes.toFloat()).coerceIn(0f, 2f) else 0f
}

object AppUsageHelper {

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    @Suppress("DEPRECATION")
    fun getDailyUsage(context: Context): List<AppUsageEntity> {
        if (!hasUsageStatsPermission(context)) return emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return emptyList()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()

        // 1. Get Aggregate usage time
        val statsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
        val pm = context.packageManager
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // 2. Query UsageEvents to accurately count Reopen / Launch events
        val launchCounts = mutableMapOf<String, Int>()
        try {
            val events = usageStatsManager.queryEvents(startTime, endTime)
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val pkg = event.packageName ?: continue
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                    event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    launchCounts[pkg] = (launchCounts[pkg] ?: 0) + 1
                }
            }
        } catch (e: Exception) {
            // Ignore event query failure fallback
        }

        val result = mutableListOf<AppUsageEntity>()
        for ((pkg, stats) in statsMap) {
            val totalSeconds = stats.totalTimeInForeground / 1000
            val count = launchCounts[pkg] ?: (if (totalSeconds > 0) 1 else 0)
            
            // Filter noise: include apps with >15s screen time or at least 1 reopen
            if (totalSeconds > 15 || count > 0) {
                val appName = try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }
                }

                result.add(
                    AppUsageEntity(
                        packageName = pkg,
                        appName = appName,
                        dateString = dateString,
                        totalTimeInForegroundSeconds = totalSeconds,
                        launchCount = count
                    )
                )
            }
        }

        return result.sortedByDescending { it.totalTimeInForegroundSeconds }
    }
}
