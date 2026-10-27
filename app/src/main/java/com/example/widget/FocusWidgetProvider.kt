package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.database.AppDatabase
import com.example.data.entity.FocusSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class FocusWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val allSessions: List<FocusSessionEntity> = db.focusDao().getAllSessionsOnce()
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val todaySeconds = allSessions
                    .filter { it.sessionType == "FOCUS" && it.startTime >= todayStart }
                    .sumOf { it.actualDurationSeconds }
                val todayMins = (todaySeconds / 60).toInt()

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_focus).apply {
                        setTextViewText(R.id.widget_focus_subtitle, "$todayMins min focused today")
                        setProgressBar(R.id.widget_focus_progress, 100, todayMins.coerceAtMost(100), false)
                        setOnClickPendingIntent(R.id.widget_focus_root, pendingIntent)
                        setOnClickPendingIntent(R.id.widget_focus_btn_start, pendingIntent)
                    }
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_focus).apply {
                        setOnClickPendingIntent(R.id.widget_focus_root, pendingIntent)
                        setOnClickPendingIntent(R.id.widget_focus_btn_start, pendingIntent)
                    }
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, FocusWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                val intent = Intent(context, FocusWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
