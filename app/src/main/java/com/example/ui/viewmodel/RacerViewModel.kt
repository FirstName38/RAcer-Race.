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
import com.example.data.model.TaskPriority
import com.example.data.repository.FocusStatistics
import com.example.data.repository.RacerRepository
import com.example.receiver.AlarmScheduler
import com.example.receiver.TaskScheduler
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

    private val _stats = MutableStateFlow(FocusStatistics())
    val stats: StateFlow<FocusStatistics> = _stats.asStateFlow()

    private val _dailyUsageList = MutableStateFlow<List<AppUsageEntity>>(emptyList())
    val dailyUsageList: StateFlow<List<AppUsageEntity>> = _dailyUsageList.asStateFlow()

    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading.asStateFlow()

    private val _aiResponseText = MutableStateFlow<String?>(null)
    val aiResponseText: StateFlow<String?> = _aiResponseText.asStateFlow()

    // Configurable Settings State
    val soundVolume = MutableStateFlow(0.8f)
    val selectedSound = MutableStateFlow(FocusSound.NONE)
    val selectedWallpaper = MutableStateFlow(FocusWallpaper.DARK_MINIMAL.id)
    val adhdReducedPressure = MutableStateFlow(false)
    val hideRemainingTime = MutableStateFlow(false)

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
        reminderEnabled: Boolean = false
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
                    isChallenge = false
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
        reminderEnabled: Boolean = false
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
                    reminderEnabled = reminderEnabled
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

    fun testHabitAlarm(habitName: String = "Habit Streak Alarm") {
        AlarmScheduler.testImmediateAlarmNotification(getApplication(), habitName)
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

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
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
