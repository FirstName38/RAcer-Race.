package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.FocusMode
import com.example.data.model.HabitFrequency
import com.example.data.model.JournalMood
import com.example.data.model.TaskPriority

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val plannedDurationMinutes: Int,
    val actualDurationSeconds: Int,
    val mode: FocusMode,
    val isCompleted: Boolean,
    val pauseCount: Int = 0,
    val breakMinutes: Int = 0,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val soundUsed: String = "none",
    val wallpaperUsed: String = "dark_minimal",
    val milestoneReached: Int = 0, // for ADHD mode
    val cycleNumber: Int = 1,
    val sessionInCycle: Int = 1,
    val totalSessionsInCycle: Int = 4,
    val sessionType: String = "FOCUS", // FOCUS, SHORT_BREAK, LONG_BREAK
    val clockStartTimeStr: String = "",
    val clockEndTimeStr: String = "",
    val totalPauseDurationSeconds: Int = 0,
    val note: String = ""
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDateMillis: Long? = null,
    val dueTimeString: String? = null, // e.g. "14:30"
    val reminderEnabled: Boolean = false,
    val category: String = "General",
    val estimatedMinutes: Int = 25,
    val recurrence: String = "None", // None, Daily, Weekly
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val isArchived: Boolean = false,
    val subtasksJson: String = "[]" // JSON array of string subtasks / steps
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val label: String = "Habit", // e.g. "Health", "Study", "Mindfulness"
    val iconName: String = "check_circle",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val targetCountPerPeriod: Int = 1,
    val reminderTimeString: String? = null,
    val reminderContext: String = "", // e.g. "Walk after eating"
    val reminderEnabled: Boolean = false,
    val colorHex: String = "#8B5CF6",
    val startDateMillis: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val isChallenge: Boolean = false,
    val challengeDays: Int = 30 // Target days for Challenge (e.g. 7, 14, 21, 30, 75, 100)
)

@Entity(tableName = "habit_completions")
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val dateString: String, // "YYYY-MM-DD"
    val isCompleted: Boolean = true,
    val perfectionScore: Int = 100, // 0..100% "how perfectly I did it"
    val note: String = ""
)

@Entity(tableName = "special_dates")
data class SpecialDateEntity(
    @PrimaryKey val dateString: String, // "YYYY-MM-DD"
    val title: String,
    val colorHex: String = "#EC4899",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wellness_routines")
data class WellnessRoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // "YOGA", "MOBILITY", "STRETCH", "BREATHING", "MENTAL_HEALTH"
    val durationMinutes: Int,
    val description: String,
    val iconName: String = "self_improvement",
    val colorHex: String = "#06B6D4",
    val stepsJson: String = "[]", // JSON array of steps
    val isCustom: Boolean = false
)

@Entity(tableName = "wellness_logs")
data class WellnessLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long,
    val routineTitle: String,
    val category: String,
    val durationSeconds: Int,
    val completedAt: Long = System.currentTimeMillis(),
    val dateString: String = "", // "YYYY-MM-DD"
    val feelingRating: Int = 5, // 1..5
    val reflectionNotes: String = ""
)

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String, // "YYYY-MM-DD"
    val learnedText: String = "",
    val feltText: String = "",
    val wantToDoText: String = "",
    val freeformNotes: String = "",
    val gratitudeText: String = "",
    val mood: JournalMood = JournalMood.GOOD,
    val tagsCsv: String = "",
    val imageUrisCsv: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "Focus Alarm",
    val isEnabled: Boolean = true,
    val daysOfWeekMask: Int = 127, // bitmask for Mon-Sun (1=Mon, 2=Tue, 4=Wed, 8=Thu, 16=Fri, 32=Sat, 64=Sun; 127=all days)
    val soundName: String = "default",
    val vibrateEnabled: Boolean = true,
    val snoozeMinutes: Int = 5,
    val isBedtimeAlarm: Boolean = false
)

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val dateString: String, // "YYYY-MM-DD"
    val totalTimeInForegroundSeconds: Long,
    val launchCount: Int = 0,
    val isBlockedDuringFocus: Boolean = false
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "USER" or "LUMA"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentUri: String? = null,
    val category: String = "chat" // "chat", "daily_analysis", "weekly_analysis", "monthly_analysis", "coach"
)
