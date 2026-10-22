package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.LumaAIService
import com.example.data.entity.AlarmEntity
import com.example.data.entity.AppUsageEntity
import com.example.data.entity.ChatMessageEntity
import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.HabitCompletionEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.JournalEntryEntity
import com.example.data.entity.SpecialDateEntity
import com.example.data.entity.TaskEntity
import com.example.data.entity.WellnessLogEntity
import com.example.data.entity.WellnessRoutineEntity
import com.example.data.model.FocusMode
import com.example.data.model.FocusSound
import com.example.data.model.FocusWallpaper
import com.example.data.model.HabitFrequency
import com.example.data.model.JournalMood
import com.example.data.entity.WeeklyPlanEntity
import com.example.data.model.TaskPriority
import com.example.data.repository.FocusStatistics
import com.example.data.repository.RacerRepository
import com.example.receiver.AlarmScheduler
import com.example.receiver.TaskScheduler
import java.util.Calendar
import com.example.service.FocusTimerService
import com.example.service.TimerUiState
import com.example.usage.AppUsageHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RacerViewModel(application: Application) : AndroidViewModel(application) {

    val repository = RacerRepository(application)
    val lumaService = LumaAIService(repository)

    // Live Timer state from Foreground service
    val timerState: StateFlow<TimerUiState> = FocusTimerService.timerState

    // Data streams
    val activeTasks = repository.allActiveTasks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val completedTasks = repository.completedTasks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val activeHabits = repository.activeHabits.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allHabitCompletions = repository.allCompletions.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allJournalEntries = repository.allJournalEntries.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allAlarms = repository.allAlarms.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val blockedApps = repository.blockedApps.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val chatMessages = repository.chatMessages.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allFocusSessions = repository.allFocusSessions.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allSpecialDates = repository.allSpecialDates.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allWellnessRoutines = repository.allWellnessRoutines.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allWellnessLogs = repository.allWellnessLogs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allDailyRoutines = repository.allDailyRoutines.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allWeeklyPlans = repository.allWeeklyPlans.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val alarmRingingState = com.example.audio.RealAlarmAudioPlayer.ringingState

    private val _stats = MutableStateFlow(FocusStatistics())
    val stats: StateFlow<FocusStatistics> = _stats.asStateFlow()

    private val _dailyUsageList = MutableStateFlow<List<AppUsageEntity>>(emptyList())
    val dailyUsageList: StateFlow<List<AppUsageEntity>> = _dailyUsageList.asStateFlow()

    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading.asStateFlow()

    private val _aiResponseText = MutableStateFlow<String?>(null)
    val aiResponseText: StateFlow<String?> = _aiResponseText.asStateFlow()

    // Configurable Settings & Visual Theme State
    val soundVolume = MutableStateFlow(0.8f)
    val selectedSound = MutableStateFlow(FocusSound.NONE)
    val selectedWallpaper = MutableStateFlow(FocusWallpaper.DARK_MINIMAL.id)
    val adhdReducedPressure = MutableStateFlow(false)
    val hideRemainingTime = MutableStateFlow(false)

    val currentAppTheme = MutableStateFlow(com.example.ui.theme.AppTheme.MIDNIGHT_OLED)
    val currentAppWallpaper = MutableStateFlow(com.example.ui.theme.AppWallpaper.AURORA_WAVES)
    val weekStartDay = MutableStateFlow("MONDAY") // MONDAY, SUNDAY, SATURDAY
    val customHabitCategories = MutableStateFlow<List<String>>(emptyList())

    // Custom Pomodoro Preferences
    val customFocusMinutes = MutableStateFlow(25)
    val customShortBreakMinutes = MutableStateFlow(5)
    val customLongBreakMinutes = MutableStateFlow(15)
    val customSessionsPerCycle = MutableStateFlow(4)
    val customLoopCycle = MutableStateFlow(true)

    init {
        refreshStats()
        loadUsage()
        seedWellness()
        seedDailyRoutines()
        loadVisualPreferences()
        seedWeeklyPlans()
    }

    private fun loadVisualPreferences() {
        viewModelScope.launch {
            val savedThemeId = repository.getSetting("app_theme_id", com.example.ui.theme.AppTheme.MIDNIGHT_OLED.id)
            currentAppTheme.value = com.example.ui.theme.AppTheme.entries.find { it.id == savedThemeId }
                ?: com.example.ui.theme.AppTheme.MIDNIGHT_OLED

            val savedWallpaperId = repository.getSetting("app_wallpaper_id", com.example.ui.theme.AppWallpaper.AURORA_WAVES.id)
            currentAppWallpaper.value = com.example.ui.theme.AppWallpaper.entries.find { it.id == savedWallpaperId }
                ?: com.example.ui.theme.AppWallpaper.AURORA_WAVES

            val savedWeekStart = repository.getSetting("week_start_day", "MONDAY")
            weekStartDay.value = savedWeekStart

            customHabitCategories.value = repository.getCustomHabitCategories()
        }
    }

    private fun seedWeeklyPlans() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val week = cal.get(Calendar.WEEK_OF_YEAR)
            repository.seedDefaultWeeklyPlansIfEmpty(week, year)
        }
    }

    fun setAppTheme(theme: com.example.ui.theme.AppTheme) {
        currentAppTheme.value = theme
        viewModelScope.launch {
            repository.setSetting("app_theme_id", theme.id)
        }
    }

    fun setAppWallpaper(wallpaper: com.example.ui.theme.AppWallpaper) {
        currentAppWallpaper.value = wallpaper
        viewModelScope.launch {
            repository.setSetting("app_wallpaper_id", wallpaper.id)
        }
    }

    fun setWeekStartDay(day: String) {
        weekStartDay.value = day
        viewModelScope.launch {
            repository.setSetting("week_start_day", day)
        }
    }

    fun addCustomHabitCategory(cat: String) {
        viewModelScope.launch {
            repository.addCustomHabitCategory(cat)
            customHabitCategories.value = repository.getCustomHabitCategories()
        }
    }

    private fun seedDailyRoutines() {
        viewModelScope.launch {
            repository.seedDefaultDailyRoutinesIfEmpty()
            repository.checkDailyRoutineReset()
        }
    }

    private fun seedWellness() {
        viewModelScope.launch {
            repository.seedDefaultWellnessRoutinesIfEmpty()
        }
    }

    fun refreshStats() {
        viewModelScope.launch {
            _stats.value = repository.calculateStats()
        }
    }

    fun loadUsage() {
        viewModelScope.launch {
            val usage = AppUsageHelper.getDailyUsage(getApplication())
            _dailyUsageList.value = usage
        }
    }

    // --- Focus Actions ---
    fun startFocus(
        mode: FocusMode = FocusMode.POMODORO,
        durationMinutes: Int = customFocusMinutes.value,
        sound: FocusSound = selectedSound.value,
        wallpaper: String = selectedWallpaper.value,
        taskId: Long? = null,
        taskTitle: String? = null,
        totalSessions: Int = customSessionsPerCycle.value,
        shortBreak: Int = customShortBreakMinutes.value,
        longBreak: Int = customLongBreakMinutes.value,
        loop: Boolean = customLoopCycle.value
    ) {
        val app = getApplication<Application>()
        val intent = Intent(app, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_START
            putExtra(FocusTimerService.EXTRA_MODE, mode.name)
            putExtra(FocusTimerService.EXTRA_DURATION_MINUTES, durationMinutes)
            putExtra(FocusTimerService.EXTRA_SOUND, sound.name)
            putExtra(FocusTimerService.EXTRA_VOLUME, soundVolume.value)
            putExtra(FocusTimerService.EXTRA_WALLPAPER, wallpaper)
            if (taskId != null) putExtra(FocusTimerService.EXTRA_TASK_ID, taskId)
            if (taskTitle != null) putExtra(FocusTimerService.EXTRA_TASK_TITLE, taskTitle)
            putExtra(FocusTimerService.EXTRA_ADHD_REDUCED, adhdReducedPressure.value)
            putExtra(FocusTimerService.EXTRA_HIDE_TIME, hideRemainingTime.value)
            putExtra(FocusTimerService.EXTRA_TOTAL_SESSIONS, totalSessions)
            putExtra(FocusTimerService.EXTRA_SHORT_BREAK, shortBreak)
            putExtra(FocusTimerService.EXTRA_LONG_BREAK, longBreak)
            putExtra(FocusTimerService.EXTRA_LOOP_CYCLE, loop)
        }
        app.startService(intent)
    }

    fun pauseFocus() {
        val app = getApplication<Application>()
        app.startService(Intent(app, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_PAUSE
        })
    }

    fun resumeFocus() {
        val app = getApplication<Application>()
        app.startService(Intent(app, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_RESUME
        })
    }

    fun nextFocusPeriod() {
        val app = getApplication<Application>()
        app.startService(Intent(app, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_NEXT
        })
    }

    fun extendFocus(minutes: Int = 5, seconds: Int = 0) {
        val app = getApplication<Application>()
        app.startService(Intent(app, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_EXTEND
            putExtra(FocusTimerService.EXTRA_EXTEND_MINUTES, minutes)
            putExtra(FocusTimerService.EXTRA_EXTEND_SECONDS, seconds)
        })
    }

    fun stopFocus() {
        val app = getApplication<Application>()
        app.startService(Intent(app, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_STOP
        })
        refreshStats()
    }

    fun deleteFocusSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteFocusSession(sessionId)
            refreshStats()
        }
    }

    fun updateSessionNote(sessionId: Long, note: String) {
        viewModelScope.launch {
            repository.updateSessionNote(sessionId, note)
        }
    }

    fun clearAllFocusSessions() {
        viewModelScope.launch {
            repository.deleteAllFocusSessions()
            refreshStats()
        }
    }

    fun logManualSession(
        mode: FocusMode,
        durationMinutes: Int,
        taskTitle: String? = null,
        note: String = "",
        isCompleted: Boolean = true
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val start = now - (durationMinutes * 60 * 1000L)
            val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            val session = FocusSessionEntity(
                startTime = start,
                endTime = now,
                plannedDurationMinutes = durationMinutes,
                actualDurationSeconds = durationMinutes * 60,
                mode = mode,
                isCompleted = isCompleted,
                taskTitle = taskTitle?.takeIf { it.isNotBlank() },
                soundUsed = "none",
                wallpaperUsed = "dark_minimal",
                clockStartTimeStr = timeFormat.format(java.util.Date(start)),
                clockEndTimeStr = timeFormat.format(java.util.Date(now)),
                note = note
            )
            repository.saveFocusSession(session)
            refreshStats()
        }
    }

    fun setSound(sound: FocusSound) {
        selectedSound.value = sound
        val app = getApplication<Application>()
        app.startService(Intent(app, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_SET_SOUND
            putExtra(FocusTimerService.EXTRA_SOUND, sound.name)
        })
    }

    fun setVolume(vol: Float) {
        soundVolume.value = vol
        val app = getApplication<Application>()
        app.startService(Intent(app, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_SET_VOLUME
            putExtra(FocusTimerService.EXTRA_VOLUME, vol)
        })
    }

    // --- Task Actions ---
    fun addTask(
        title: String,
        description: String = "",
        priority: TaskPriority = TaskPriority.MEDIUM,
        dueDateMillis: Long? = null,
        dueTimeString: String? = null,
        reminderEnabled: Boolean = false,
        reminderLeadMinutes: Int = 0,
        category: String = "General",
        estimatedMinutes: Int = 25,
        subtasksJson: String = "[]"
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                description = description,
                priority = priority,
                dueDateMillis = dueDateMillis,
                dueTimeString = dueTimeString,
                reminderEnabled = reminderEnabled,
                category = category,
                estimatedMinutes = estimatedMinutes,
                subtasksJson = subtasksJson
            )
            val id = repository.insertTask(task)
            if (reminderEnabled && dueDateMillis != null) {
                TaskScheduler.scheduleTaskReminder(
                    context = getApplication(),
                    task = task.copy(id = id),
                    reminderLeadMinutes = reminderLeadMinutes
                )
            }
        }
    }

    fun toggleTask(taskId: Long) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(taskId)
            refreshStats()
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            TaskScheduler.cancelTaskReminder(getApplication(), taskId)
        }
    }

    fun testTaskReminder(title: String = "Test Future Task Alert") {
        TaskScheduler.testImmediateNotification(getApplication(), title)
    }

    fun reorderTask(task: TaskEntity, moveUp: Boolean, currentList: List<TaskEntity>) {
        val index = currentList.indexOfFirst { it.id == task.id }
        if (index == -1) return
        val targetIndex = if (moveUp) index - 1 else index + 1
        if (targetIndex !in currentList.indices) return
        val targetTask = currentList[targetIndex]

        viewModelScope.launch {
            val t1CreatedAt = task.createdAt
            val t2CreatedAt = targetTask.createdAt
            repository.updateTask(task.copy(createdAt = t2CreatedAt))
            repository.updateTask(targetTask.copy(createdAt = t1CreatedAt))
        }
    }

    fun archiveTask(taskId: Long) {
        viewModelScope.launch {
            repository.archiveTask(taskId)
        }
    }

    // --- Habit & Challenge Actions ---
    fun addHabit(
        name: String,
        description: String = "",
        label: String = "Habit",
        frequency: HabitFrequency = HabitFrequency.DAILY,
        colorHex: String = "#8B5CF6",
        reminderTimeString: String? = null,
        reminderContext: String = "",
        reminderEnabled: Boolean = false,
        reminderType: String = "NOTIFICATION",
        reminderSound: String = "zen_bell"
    ) {
        viewModelScope.launch {
            val id = repository.insertHabit(
                HabitEntity(
                    name = name,
                    description = description,
                    label = label,
                    frequency = frequency,
                    colorHex = colorHex,
                    reminderTimeString = reminderTimeString,
                    reminderContext = reminderContext,
                    reminderEnabled = reminderEnabled,
                    isChallenge = false,
                    reminderType = reminderType,
                    reminderSound = reminderSound
                )
            )
            if (reminderEnabled && !reminderTimeString.isNullOrBlank()) {
                AlarmScheduler.scheduleHabitReminder(
                    context = getApplication(),
                    habitId = id,
                    timeString = reminderTimeString,
                    habitName = name,
                    contextNote = reminderContext
                )
            }
        }
    }

    fun addChallenge(
        name: String,
        description: String = "",
        label: String = "Challenge",
        challengeDays: Int = 30,
        colorHex: String = "#EC4899",
        reminderTimeString: String? = null,
        reminderContext: String = "",
        reminderEnabled: Boolean = false,
        reminderType: String = "NOTIFICATION",
        reminderSound: String = "zen_bell"
    ) {
        viewModelScope.launch {
            val id = repository.insertHabit(
                HabitEntity(
                    name = name,
                    description = description,
                    label = label,
                    colorHex = colorHex,
                    isChallenge = true,
                    challengeDays = challengeDays,
                    reminderTimeString = reminderTimeString,
                    reminderContext = reminderContext,
                    reminderEnabled = reminderEnabled,
                    reminderType = reminderType,
                    reminderSound = reminderSound
                )
            )
            if (reminderEnabled && !reminderTimeString.isNullOrBlank()) {
                AlarmScheduler.scheduleHabitReminder(
                    context = getApplication(),
                    habitId = id,
                    timeString = reminderTimeString,
                    habitName = name,
                    contextNote = reminderContext
                )
            }
        }
    }

    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit)
            if (habit.reminderEnabled && !habit.reminderTimeString.isNullOrBlank()) {
                AlarmScheduler.scheduleHabitReminder(
                    context = getApplication(),
                    habitId = habit.id,
                    timeString = habit.reminderTimeString,
                    habitName = habit.name,
                    contextNote = habit.reminderContext
                )
            } else {
                AlarmScheduler.cancelHabitReminder(getApplication(), habit.id)
            }
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            AlarmScheduler.cancelHabitReminder(getApplication(), habit.id)
            repository.deleteHabit(habit)
        }
    }

    fun deleteHabitById(id: Long) {
        viewModelScope.launch {
            AlarmScheduler.cancelHabitReminder(getApplication(), id)
            repository.deleteHabitById(id)
        }
    }

    fun testHabitAlarm(habitName: String = "Habit Streak Alarm") {
        AlarmScheduler.testImmediateAlarmNotification(getApplication(), habitName)
    }

    fun previewNotificationSound(soundName: String) {
        com.example.audio.RealNotificationSoundPlayer.playNotification(getApplication(), soundName, withVibrate = true)
    }

    fun previewAlarmSound(soundName: String) {
        com.example.audio.RealAlarmAudioPlayer.previewSound(getApplication(), soundName)
    }

    fun toggleHabitToday(habitId: Long) {
        viewModelScope.launch {
            val todayStr = repository.getTodayDateString()
            repository.toggleHabitCompletion(habitId, todayStr)
        }
    }

    fun recordHabitCompletion(
        habitId: Long,
        dateString: String,
        isCompleted: Boolean,
        perfectionScore: Int = 100,
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.recordHabitCompletion(
                habitId = habitId,
                dateString = dateString,
                isCompleted = isCompleted,
                perfectionScore = perfectionScore,
                note = note
            )
        }
    }

    // --- Weekly Plans Actions ---
    fun addWeeklyPlan(
        title: String,
        description: String = "",
        dayOfWeek: String = "ALL",
        priority: String = "HIGH",
        category: String = "General",
        targetMinutes: Int = 0,
        weekNumber: Int,
        year: Int
    ) {
        viewModelScope.launch {
            repository.insertWeeklyPlan(
                WeeklyPlanEntity(
                    weekNumber = weekNumber,
                    year = year,
                    title = title,
                    description = description,
                    dayOfWeek = dayOfWeek,
                    priority = priority,
                    category = category,
                    targetTimeMinutes = targetMinutes,
                    isCompleted = false
                )
            )
        }
    }

    fun updateWeeklyPlan(plan: WeeklyPlanEntity) {
        viewModelScope.launch {
            repository.updateWeeklyPlan(plan)
        }
    }

    fun deleteWeeklyPlan(id: Long) {
        viewModelScope.launch {
            repository.deleteWeeklyPlan(id)
        }
    }

    fun toggleWeeklyPlan(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleWeeklyPlan(id, isCompleted)
        }
    }

    // --- Special Dates Actions ---
    fun setSpecialDate(dateString: String, title: String, colorHex: String = "#EC4899", note: String = "") {
        viewModelScope.launch {
            repository.setSpecialDate(dateString, title, colorHex, note)
        }
    }

    fun deleteSpecialDate(dateString: String) {
        viewModelScope.launch {
            repository.deleteSpecialDate(dateString)
        }
    }

    // --- Wellness / Yoga / Mobility / Stretch Actions ---
    fun logWellnessSession(
        routineId: Long,
        routineTitle: String,
        category: String,
        durationSeconds: Int,
        feelingRating: Int,
        notes: String
    ) {
        viewModelScope.launch {
            repository.logWellnessSession(
                routineId = routineId,
                routineTitle = routineTitle,
                category = category,
                durationSeconds = durationSeconds,
                feelingRating = feelingRating,
                notes = notes
            )
        }
    }

    fun addCustomWellnessRoutine(
        title: String,
        category: String,
        durationMinutes: Int,
        description: String,
        colorHex: String = "#06B6D4",
        stepsJson: String = "[]"
    ) {
        viewModelScope.launch {
            repository.insertWellnessRoutine(
                WellnessRoutineEntity(
                    title = title,
                    category = category,
                    durationMinutes = durationMinutes,
                    description = description,
                    colorHex = colorHex,
                    stepsJson = stepsJson,
                    isCustom = true
                )
            )
        }
    }

    fun deleteWellnessRoutine(routine: WellnessRoutineEntity) {
        viewModelScope.launch {
            repository.deleteWellnessRoutine(routine)
        }
    }

    // --- Journal Actions ---
    fun saveJournal(
        dateString: String = repository.getTodayDateString(),
        learnedText: String,
        feltText: String,
        wantToDoText: String,
        gratitudeText: String,
        freeformNotes: String,
        mood: JournalMood,
        tagsCsv: String = "",
        imageUrisCsv: String = ""
    ) {
        viewModelScope.launch {
            repository.saveJournalEntry(
                JournalEntryEntity(
                    dateString = dateString,
                    learnedText = learnedText,
                    feltText = feltText,
                    wantToDoText = wantToDoText,
                    gratitudeText = gratitudeText,
                    freeformNotes = freeformNotes,
                    mood = mood,
                    tagsCsv = tagsCsv,
                    imageUrisCsv = imageUrisCsv,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // --- Alarm Actions ---
    fun addAlarm(
        hour: Int,
        minute: Int,
        label: String = "Focus Alarm",
        daysOfWeekMask: Int = 127,
        vibrate: Boolean = true,
        isBedtime: Boolean = false
    ) {
        viewModelScope.launch {
            val id = repository.insertAlarm(
                AlarmEntity(
                    hour = hour,
                    minute = minute,
                    label = label,
                    daysOfWeekMask = daysOfWeekMask,
                    vibrateEnabled = vibrate,
                    isBedtimeAlarm = isBedtime
                )
            )
            AlarmScheduler.scheduleAlarm(getApplication(), id, hour, minute, label, vibrate, isBedtime)
        }
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            repository.updateAlarm(updated)
            if (updated.isEnabled) {
                AlarmScheduler.scheduleAlarm(
                    getApplication(),
                    updated.id,
                    updated.hour,
                    updated.minute,
                    updated.label,
                    updated.vibrateEnabled,
                    updated.isBedtimeAlarm
                )
            } else {
                AlarmScheduler.cancelAlarm(getApplication(), updated.id)
            }
        }
    }

    fun deleteAlarm(alarmId: Long) {
        viewModelScope.launch {
            repository.deleteAlarm(alarmId)
            AlarmScheduler.cancelAlarm(getApplication(), alarmId)
        }
    }

    fun dismissRingingAlarm() {
        com.example.audio.RealAlarmAudioPlayer.stopAlarm(getApplication())
    }

    fun snoozeRingingAlarm(alarmId: Long, minutes: Int = 5) {
        val app = getApplication<android.app.Application>()
        com.example.audio.RealAlarmAudioPlayer.stopAlarm(app)
        val cal = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.MINUTE, minutes)
        }
        AlarmScheduler.scheduleAlarm(
            context = app,
            alarmId = if (alarmId != 0L) alarmId else System.currentTimeMillis(),
            hour = cal.get(java.util.Calendar.HOUR_OF_DAY),
            minute = cal.get(java.util.Calendar.MINUTE),
            label = "Snoozed (${minutes}m)",
            vibrate = true,
            isBedtime = false
        )
    }

    fun testRealAlarmAudio(soundName: String = "DIGITAL_BEEP", label: String = "Test Real Alarm") {
        val cal = java.util.Calendar.getInstance()
        com.example.audio.RealAlarmAudioPlayer.startAlarm(
            context = getApplication(),
            alarmId = 9999L,
            label = label,
            hour = cal.get(java.util.Calendar.HOUR_OF_DAY),
            minute = cal.get(java.util.Calendar.MINUTE),
            soundName = soundName
        )
    }

    fun previewAlarmSound(soundType: com.example.audio.AlarmSoundType) {
        com.example.audio.RealAlarmAudioPlayer.previewSound(getApplication(), soundType, 3500L)
    }

    // --- Daily Routine Actions ---
    fun toggleDailyRoutine(routineId: Long) {
        viewModelScope.launch {
            repository.toggleDailyRoutine(routineId)
        }
    }

    fun addDailyRoutine(
        title: String,
        timeSlot: String = "MORNING",
        scheduledTime: String = "07:00",
        durationMinutes: Int = 15,
        cueNote: String = "",
        actionType: String = "NONE",
        colorHex: String = "#8B5CF6"
    ) {
        viewModelScope.launch {
            repository.insertDailyRoutine(
                com.example.data.entity.DailyRoutineEntity(
                    title = title,
                    timeSlot = timeSlot,
                    scheduledTime = scheduledTime,
                    durationMinutes = durationMinutes,
                    cueNote = cueNote,
                    actionType = actionType,
                    colorHex = colorHex
                )
            )
        }
    }

    fun deleteDailyRoutine(routineId: Long) {
        viewModelScope.launch {
            repository.deleteDailyRoutine(routineId)
        }
    }

    fun resetDailyRoutines() {
        viewModelScope.launch {
            repository.resetDailyRoutines()
        }
    }

    // --- AI / Luma Actions ---
    fun askLuma(message: String, bitmap: Bitmap? = null) {
        viewModelScope.launch {
            _isAILoading.value = true
            try {
                val resp = lumaService.askLuma(message, bitmap)
                _aiResponseText.value = resp
            } finally {
                _isAILoading.value = false
            }
        }
    }

    fun generateDailyRetrospective() {
        viewModelScope.launch {
            _isAILoading.value = true
            try {
                val resp = lumaService.generateDailyAnalysis()
                _aiResponseText.value = resp
            } finally {
                _isAILoading.value = false
            }
        }
    }

    fun generateWeeklyRetrospective() {
        viewModelScope.launch {
            _isAILoading.value = true
            try {
                val resp = lumaService.generateWeeklyAnalysis()
                _aiResponseText.value = resp
            } finally {
                _isAILoading.value = false
            }
        }
    }

    fun generateStudyPlan(
        subject: String,
        hoursPerDay: Float = 2.0f,
        targetDurationWeeks: Int = 2,
        focusTopics: String = "",
        studyStyle: String = "Pomodoro (25/5)"
    ) {
        viewModelScope.launch {
            _isAILoading.value = true
            try {
                val resp = lumaService.generateStudyPlan(
                    subject = subject,
                    hoursPerDay = hoursPerDay,
                    targetDurationWeeks = targetDurationWeeks,
                    focusTopics = focusTopics,
                    studyStyle = studyStyle
                )
                _aiResponseText.value = resp
            } finally {
                _isAILoading.value = false
            }
        }
    }

    fun generateExamPlan(
        examName: String,
        examDateStr: String,
        daysLeft: Int,
        highYieldTopics: String = "",
        targetScoreGoal: String = "High Confidence / Top Grade"
    ) {
        viewModelScope.launch {
            _isAILoading.value = true
            try {
                val resp = lumaService.generateExamPlan(
                    examName = examName,
                    examDateStr = examDateStr,
                    daysLeft = daysLeft,
                    highYieldTopics = highYieldTopics,
                    targetScoreGoal = targetScoreGoal
                )
                _aiResponseText.value = resp
            } finally {
                _isAILoading.value = false
            }
        }
    }

    fun generateMentalHealthAnalysis(stateType: String = "general", notes: String = "") {
        viewModelScope.launch {
            _isAILoading.value = true
            try {
                val resp = lumaService.generateMentalHealthAnalysis(stateType, notes)
                _aiResponseText.value = resp
            } finally {
                _isAILoading.value = false
            }
        }
    }

    fun generateCoachPlan(goal: String) {
        viewModelScope.launch {
            _isAILoading.value = true
            try {
                val resp = lumaService.generateCoachPlan(goal)
                _aiResponseText.value = resp
            } finally {
                _isAILoading.value = false
            }
        }
    }

    fun analyzeNotesImage(bitmap: Bitmap, prompt: String = "Extract tasks and study plan") {
        viewModelScope.launch {
            _isAILoading.value = true
            try {
                val resp = lumaService.analyzeImageStudyNotes(bitmap, prompt)
                _aiResponseText.value = resp
            } finally {
                _isAILoading.value = false
            }
        }
    }

    fun setAppBlocked(pkg: String, blocked: Boolean) {
        viewModelScope.launch {
            repository.setAppBlocked(pkg, blocked)
        }
    }
}
