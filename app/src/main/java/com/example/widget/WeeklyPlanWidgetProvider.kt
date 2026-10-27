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
import com.example.data.entity.WeeklyPlanEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class WeeklyPlanWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val weekStartPref = db.settingsDao().getSettingValue("week_start_day")
                    ?: db.settingsDao().getSettingValue("app_week_start_day") ?: "MONDAY"
                val firstDay = when (weekStartPref.uppercase()) {
                    "SUNDAY" -> Calendar.SUNDAY
                    "MONDAY" -> Calendar.MONDAY
                    "TUESDAY" -> Calendar.TUESDAY
                    "WEDNESDAY" -> Calendar.WEDNESDAY
                    "THURSDAY" -> Calendar.THURSDAY
                    "FRIDAY" -> Calendar.FRIDAY
                    "SATURDAY" -> Calendar.SATURDAY
                    else -> Calendar.MONDAY
                }

                val cal = Calendar.getInstance().apply {
                    firstDayOfWeek = firstDay
                    minimalDaysInFirstWeek = 4
                }
                val currentWeek = cal.get(Calendar.WEEK_OF_YEAR)
                val currentYear = cal.get(Calendar.YEAR)
                val maxWeeks = cal.getActualMaximum(Calendar.WEEK_OF_YEAR)
                val weeksLeft = (maxWeeks - currentWeek).coerceAtLeast(0)

                val plans: List<WeeklyPlanEntity> = db.weeklyPlanDao().getPlansForWeekOnce(currentWeek, currentYear)
                val total = plans.size
                val done = plans.count { it.isCompleted }
                val rate = if (total > 0) (done.toFloat() / total * 100f).toInt() else 0
                val isAbove70 = rate >= 70

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_weekly_plan).apply {
                        setTextViewText(R.id.widget_weekly_weeknum, "Week $currentWeek")
                        setTextViewText(R.id.widget_weekly_subtitle, "$weeksLeft weeks left in year")
                        setProgressBar(R.id.widget_weekly_progress, 100, rate, false)
                        setTextViewText(
                            R.id.widget_weekly_status,
                            if (total == 0) "No goals set for this week"
                            else "$done of $total done (${rate}%) ${if (isAbove70) "🟢 Met 70%" else "🔴 Below 70%"}"
                        )
                        setOnClickPendingIntent(R.id.widget_weekly_root, pendingIntent)
                    }
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_weekly_plan).apply {
                        setOnClickPendingIntent(R.id.widget_weekly_root, pendingIntent)
                    }
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, WeeklyPlanWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                val intent = Intent(context, WeeklyPlanWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
