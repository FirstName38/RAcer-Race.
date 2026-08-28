package com.example.receiver

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.database.AppDatabase
import com.example.data.entity.TaskEntity
import com.example.service.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TASK_REMINDER = "com.example.ACTION_TASK_REMINDER"
        const val ACTION_MARK_DONE = "com.example.ACTION_MARK_DONE"
        const val ACTION_SNOOZE = "com.example.ACTION_SNOOZE"

        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"
        const val EXTRA_TASK_CATEGORY = "EXTRA_TASK_CATEGORY"
        const val EXTRA_TASK_PRIORITY = "EXTRA_TASK_PRIORITY"
        const val EXTRA_TASK_ESTIMATED = "EXTRA_TASK_ESTIMATED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: ACTION_TASK_REMINDER
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Reminder"
        val category = intent.getStringExtra(EXTRA_TASK_CATEGORY) ?: "General"
        val priority = intent.getStringExtra(EXTRA_TASK_PRIORITY) ?: "Medium"
        val estimatedMins = intent.getIntExtra(EXTRA_TASK_ESTIMATED, 25)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        NotificationHelper.createNotificationChannels(context)

        when (action) {
            ACTION_MARK_DONE -> {
                // Mark task as done in Database
                if (taskId > 0) {
                    notificationManager.cancel(NotificationHelper.NOTIFICATION_ID_ALARM + 1000 + taskId.toInt())
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = AppDatabase.getDatabase(context)
                        val task = db.taskDao().getTaskById(taskId)
                        if (task != null) {
                            db.taskDao().updateTask(task.copy(isCompleted = true, completedAt = System.currentTimeMillis()))
                        }
                    }
                }
            }

            ACTION_SNOOZE -> {
                // Dismiss current notification and reschedule in 10 minutes
                notificationManager.cancel(NotificationHelper.NOTIFICATION_ID_ALARM + 1000 + taskId.toInt())
                val snoozeTime = System.currentTimeMillis() + 10 * 60 * 1000L
                TaskScheduler.scheduleExactTaskAlert(
                    context = context,
                    taskId = taskId,
                    taskTitle = taskTitle,
                    category = category,
                    priority = priority,
                    estimatedMins = estimatedMins,
                    triggerAtMillis = snoozeTime
                )
            }

            else -> {
                // Show notification
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("NAVIGATE_TO", "tasks")
                    putExtra("FOCUS_TASK_ID", taskId)
                }
                val pendingOpen = PendingIntent.getActivity(
                    context,
                    taskId.toInt(),
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Mark Done Action
                val doneIntent = Intent(context, TaskReminderReceiver::class.java).apply {
                    this.action = ACTION_MARK_DONE
                    putExtra(EXTRA_TASK_ID, taskId)
                }
                val pendingDone = PendingIntent.getBroadcast(
                    context,
                    (taskId + 10000).toInt(),
                    doneIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Snooze 10m Action
                val snoozeIntent = Intent(context, TaskReminderReceiver::class.java).apply {
                    this.action = ACTION_SNOOZE
                    putExtra(EXTRA_TASK_ID, taskId)
                    putExtra(EXTRA_TASK_TITLE, taskTitle)
                    putExtra(EXTRA_TASK_CATEGORY, category)
                    putExtra(EXTRA_TASK_PRIORITY, priority)
                    putExtra(EXTRA_TASK_ESTIMATED, estimatedMins)
                }
                val pendingSnooze = PendingIntent.getBroadcast(
                    context,
                    (taskId + 20000).toInt(),
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notifId = NotificationHelper.NOTIFICATION_ID_ALARM + 1000 + taskId.toInt().coerceAtLeast(0)

                val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_REMINDERS)
                    .setContentTitle("📌 Task Reminder: $taskTitle")
                    .setContentText("Scheduled for now • $category ($priority Priority)")
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setAutoCancel(true)
                    .setContentIntent(pendingOpen)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .addAction(android.R.drawable.checkbox_on_background, "Done", pendingDone)
                    .addAction(android.R.drawable.ic_lock_idle_alarm, "+10m Snooze", pendingSnooze)
                    .build()

                notificationManager.notify(notifId, notification)

                // Vibration feedback
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                        vibratorManager?.defaultVibrator?.vibrate(
                            VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator?.vibrate(
                                VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1)
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator?.vibrate(longArrayOf(0, 300, 150, 300), -1)
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }
}

object TaskScheduler {

    fun scheduleTaskReminder(context: Context, task: TaskEntity, reminderLeadMinutes: Int = 0) {
        val dueTime = task.dueDateMillis ?: return
        val triggerTime = dueTime - (reminderLeadMinutes * 60 * 1000L)
        if (triggerTime <= System.currentTimeMillis() - 60000L) {
            // Already passed
            return
        }

        scheduleExactTaskAlert(
            context = context,
            taskId = task.id,
            taskTitle = task.title,
            category = task.category,
            priority = task.priority.displayName,
            estimatedMins = task.estimatedMinutes,
            triggerAtMillis = triggerTime
        )
    }

    fun scheduleExactTaskAlert(
        context: Context,
        taskId: Long,
        taskTitle: String,
        category: String,
        priority: String,
        estimatedMins: Int,
        triggerAtMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = TaskReminderReceiver.ACTION_TASK_REMINDER
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, taskTitle)
            putExtra(TaskReminderReceiver.EXTRA_TASK_CATEGORY, category)
            putExtra(TaskReminderReceiver.EXTRA_TASK_PRIORITY, priority)
            putExtra(TaskReminderReceiver.EXTRA_TASK_ESTIMATED, estimatedMins)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = TaskReminderReceiver.ACTION_TASK_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun testImmediateNotification(context: Context, taskTitle: String = "Test Future Task Alert") {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        NotificationHelper.createNotificationChannels(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "tasks")
        }
        val pendingOpen = PendingIntent.getActivity(
            context,
            9999,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_REMINDERS)
            .setContentTitle("🔔 Task Reminder: $taskTitle")
            .setContentText("Immediate test: Your upcoming tasks and deadlines will alert you here!")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingOpen)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()

        notificationManager.notify(NotificationHelper.NOTIFICATION_ID_ALARM + 9999, notification)
    }
}
