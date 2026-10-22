package com.example.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.audio.RealAlarmAudioPlayer
import java.util.Calendar

class AlarmActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_DISMISS = "com.example.ACTION_DISMISS_ALARM"
        const val ACTION_SNOOZE = "com.example.ACTION_SNOOZE_ALARM"
        const val EXTRA_ALARM_ID = "EXTRA_ALARM_ID"
        const val EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID"
        const val EXTRA_ALARM_LABEL = "EXTRA_ALARM_LABEL"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        val notifId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val label = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: "Focus Alarm"

        // Stop ringing sound and vibration immediately
        RealAlarmAudioPlayer.stopAlarm(context)

        // Cancel the ringing notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notifId != -1) {
            notificationManager.cancel(notifId)
        }

        when (intent.action) {
            ACTION_SNOOZE -> {
                // Snooze for 5 minutes
                val cal = Calendar.getInstance().apply {
                    add(Calendar.MINUTE, 5)
                }
                AlarmScheduler.scheduleAlarm(
                    context = context,
                    alarmId = if (alarmId != -1L) alarmId else System.currentTimeMillis(),
                    hour = cal.get(Calendar.HOUR_OF_DAY),
                    minute = cal.get(Calendar.MINUTE),
                    label = "$label (Snoozed 5m)",
                    vibrate = true,
                    isBedtime = false
                )
            }
            ACTION_DISMISS -> {
                // Already stopped
            }
        }
    }
}
