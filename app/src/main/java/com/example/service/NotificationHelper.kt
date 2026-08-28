package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_FOCUS_TIMER = "channel_focus_timer"
    const val CHANNEL_ALARMS = "channel_alarms"
    const val CHANNEL_REMINDERS = "channel_reminders"
    const val CHANNEL_AI_INSIGHTS = "channel_ai_insights"

    const val NOTIFICATION_ID_FOCUS_TIMER = 1001
    const val NOTIFICATION_ID_ALARM = 2001

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val timerChannel = NotificationChannel(
                CHANNEL_FOCUS_TIMER,
                "Focus Timer & Audio",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live focus timer countdown and background audio status"
                setShowBadge(false)
            }

            val alarmChannel = NotificationChannel(
                CHANNEL_ALARMS,
                "Alarms & Timers",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority alarms and timer completion alerts"
                enableVibration(true)
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Habits & Task Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle prompts for scheduled habits and tasks"
            }

            val aiChannel = NotificationChannel(
                CHANNEL_AI_INSIGHTS,
                "Luma AI Insights",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Daily retrospectives and coaching suggestions"
            }

            manager.createNotificationChannels(listOf(timerChannel, alarmChannel, reminderChannel, aiChannel))
        }
    }

    fun buildTimerNotification(
        context: Context,
        title: String,
        contentText: String,
        isRunning: Boolean,
        progressPercent: Int
    ): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(context, FocusTimerService::class.java).apply {
            action = if (isRunning) FocusTimerService.ACTION_PAUSE else FocusTimerService.ACTION_RESUME
        }
        val pendingToggle = PendingIntent.getService(
            context,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            context,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_FOCUS_TIMER)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingOpenApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, progressPercent.coerceIn(0, 100), false)
            .addAction(
                if (isRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isRunning) "Pause" else "Resume",
                pendingToggle
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "End Focus",
                pendingStop
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }
}
