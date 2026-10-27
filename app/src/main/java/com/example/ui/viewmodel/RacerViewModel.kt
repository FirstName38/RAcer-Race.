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
    val allManifestations = repository.allManifestations.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allAppUsageLimits = repository.allAppUsageLimits.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val alarmRingingState = com.example.audio.RealAlarmAudioPlayer.ringingState

    private val _stats = MutableStateFlow(FocusStatistics())
    val stats: StateFlow<FocusStatistics> = _stats.asStateFlow()

    private val _dailyUsageList = MutableStateFlow<List<AppUsageEntity>>(emptyList())
    val dailyUsageList: StateFlow<List<AppUsageEntity>> = _dailyUsageList.asStateFlow()

    // Coins & Gamification
    val coinBalance = MutableStateFlow(120)

    // Week Calculation Rules
    val weekStartDay = MutableStateFlow("WEDNESDAY") // Matches user screenshot default or preference
    val week1BeginsRule = MutableStateFlow(com.example.util.WeekCalculationHelper.RULE_WEEK_CONTAINING_JAN_1)

    // Theme Mode: SYSTEM, DARK, LIGHT
    val themeMode = MutableStateFlow("DARK")

    // Google Account & Protection
    val googleAccountEmail = MutableStateFlow("whoiamfirstname@gmail.com")
    val isGoogleAccountLinked = MutableStateFlow(true)
    val lastGoogleSyncTimestamp = MutableStateFlow(System.currentTimeMillis())

    // Linked Focus Elements
    val linkedStudyRoomId = MutableStateFlow<String?>("none")
    val linkedFocusTargetType = MutableStateFlow("NONE") // NONE, TASK, HABIT, CHALLENGE, WEEKLY_PLAN
    val linkedFocusTargetId = MutableStateFlow<Long?>(null)
    val linkedFocusTargetTitle = MutableStateFlow("")

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
    val currentAppFont = MutableStateFlow(com.example.ui.theme.AppFont.MODERN_SANS)
    val customWallpaperUri = MutableStateFlow<String?>(null)
    val customAccentColorHex = MutableStateFlow<String?>(null)
    val customWallpaperScrimAlpha = MutableStateFlow(0.65f)
    val customHabitCategories = MutableStateFlow<List<String>>(emptyList())
    val customTaskCategories = MutableStateFlow<List<String>>(emptyList())
    val customWeeklyCategories = MutableStateFlow<List<String>>(emptyList())

    // Custom Pomodoro Preferences
    val customFocusMinutes = MutableStateFlow(25)
    val customShortBreakMinutes = MutableStateFlow(5)
    val customLongBreakMinutes = MutableStateFlow(15)
    val customSessionsPerCycle = MutableStateFlow(4)
    val customLoopCycle = MutableStateFlow(true)
    val customStudyRooms = MutableStateFlow<List<com.example.data.repository.CustomStudyRoom>>(emptyList())

    init {
        refreshStats()
        loadUsage()
        seedWellness()
        loadVisualPreferences()
        cleanUpDefaults()
        loadStudyRooms()
    }

    private fun cleanUpDefaults() {
        viewModelScope.launch {
            repository.cleanUpDefaultRoutinesAndPlans()
        }
    }

    fun loadStudyRooms() {
        viewModelScope.launch {
            customStudyRooms.value = repository.getCustomStudyRooms()
        }
    }

    fun createStudyRoom(room: com.example.data.repository.CustomStudyRoom) {
        viewModelScope.launch {
            repository.saveCustomStudyRoom(room)
            customStudyRooms.value = repository.getCustomStudyRooms()
        }
    }

    fun createStudyRoom(
        name: String,
        purpose: String,
        creatorName: String = "You (Host)",
        focusMinutes: Int = 25,
        breakMinutes: Int = 5,
        ambientSound: String = "Gentle Rain",
        mode: String = "POMODORO",
        partnerNames: List<String> = emptyList()
    ) {
        createStudyRoom(
            com.example.data.repository.CustomStudyRoom(
                name = name,
                purpose = purpose,
                creatorName = creatorName,
                focusMinutes = focusMinutes,
                breakMinutes = breakMinutes,
                ambientSound = ambientSound,
                mode = mode,
                partnerNames = partnerNames
            )
        )
    }

    fun deleteStudyRoom(id: String) {
        viewModelScope.launch {
            repository.deleteCustomStudyRoom(id)
            customStudyRooms.value = repository.getCustomStudyRooms()
        }
    }

    private fun loadVisualPreferences() {
        viewModelScope.launch {
            val savedThemeId = repository.getSetting("app_theme_id", com.example.ui.theme.AppTheme.MIDNIGHT_OLED.id)
            currentAppTheme.value = com.example.ui.theme.AppTheme.entries.find { it.id == savedThemeId }
                ?: com.example.ui.theme.AppTheme.MIDNIGHT_OLED

            val savedWallpaperId = repository.getSetting("app_wallpaper_id", com.example.ui.theme.AppWallpaper.AURORA_WAVES.id)
            currentAppWallpaper.value = com.example.ui.theme.AppWallpaper.entries.find { it.id == savedWallpaperId }
                ?: com.example.ui.theme.AppWallpaper.AURORA_WAVES

            val savedFontId = repository.getSetting("app_font_id", com.example.ui.theme.AppFont.MODERN_SANS.id)
            currentAppFont.value = com.example.ui.theme.AppFont.entries.find { it.id == savedFontId }
                ?: com.example.ui.theme.AppFont.MODERN_SANS

            val savedCustomUri = repository.getSetting("custom_wallpaper_uri", "")
            customWallpaperUri.value = savedCustomUri.ifBlank { null }

            val savedAccentHex = repository.getSetting("custom_accent_hex", "")
            customAccentColorHex.value = savedAccentHex.ifBlank { null }

            val savedScrim = repository.getSetting("custom_scrim_alpha", "0.65")
            customWallpaperScrimAlpha.value = savedScrim.toFloatOrNull() ?: 0.65f

            val savedWeekStart = repository.getSetting("week_start_day", "WEDNESDAY")
            weekStartDay.value = savedWeekStart

            val savedWeek1Rule = repository.getSetting("week_1_begins_rule", com.example.util.WeekCalculationHelper.RULE_WEEK_CONTAINING_JAN_1)
            week1BeginsRule.value = savedWeek1Rule

            val savedThemeMode = repository.getSetting("app_theme_mode", "DARK")
            themeMode.value = savedThemeMode

            coinBalance.value = repository.getCoinBalance()
            googleAccountEmail.value = repository.getGoogleUserEmail()
            isGoogleAccountLinked.value = repository.isGoogleAccountLinked()
            lastGoogleSyncTimestamp.value = repository.getLastGoogleSyncTimestamp()

            customHabitCategories.value = repository.getCustomHabitCategories()
            customTaskCategories.value = repository.getCustomTaskCategories()
            customWeeklyCategories.value = repository.getCustomWeeklyCategories()
        }
    }

    fun setWeekStartDay(day: String) {
        weekStartDay.value = day
        viewModelScope.launch {
            repository.setSetting("week_start_day", day)
        }
    }

    fun setWeek1BeginsRule(rule: String) {
        week1BeginsRule.value = rule
        viewModelScope.launch {
            repository.setSetting("week_1_begins_rule", rule)
        }
    }

    fun setThemeMode(mode: String) {
        themeMode.value = mode
        viewModelScope.launch {
            repository.setSetting("app_theme_mode", mode)
        }
    }

    fun awardCoins(amount: Int, reason: String = "") {
        viewModelScope.launch {
            val updated = repository.addCoins(amount)
            coinBalance.value = updated
        }
    }

    fun setGoogleAccount(email: String, linked: Boolean) {
        googleAccountEmail.value = email
        isGoogleAccountLinked.value = linked
        viewModelScope.launch {
            repository.setGoogleUserEmail(email)
            repository.setGoogleAccountLinked(linked)
            repository.recordGoogleSync()
            lastGoogleSyncTimestamp.value = System.currentTimeMillis()
        }
    }

    fun triggerGoogleCloudSync() {
        viewModelScope.launch {
            repository.recordGoogleSync()
            lastGoogleSyncTimestamp.value = System.currentTimeMillis()
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

    fun setAppFont(font: com.example.ui.theme.AppFont) {
        currentAppFont.value = font
        viewModelScope.launch {
            repository.setSetting("app_font_id", font.id)
        }
    }

    fun setCustomWallpaperUri(uri: String?) {
        customWallpaperUri.value = uri
        viewModelScope.launch {
            repository.setSetting("custom_wallpaper_uri", uri ?: "")
        }
    }

    fun setCustomAccentColor(hex: String?) {
        customAccentColorHex.value = hex
        viewModelScope.launch {
            repository.setSetting("custom_accent_hex", hex ?: "")
        }
    }

    fun setCustomWallpaperScrim(alpha: Float) {
        customWallpaperScrimAlpha.value = alpha
        viewModelScope.launch {
            repository.setSetting("custom_scrim_alpha", alpha.toString())
        }
    }

    fun setWeekStartDay(day: String) {
        weekStartDay.value = day
        viewModelScope.launch {
            repository.setSetting("week_start_day", day)
            repository.setSetting("app_week_start_day", day)
        }
    }

    fun addCustomHabitCategory(cat: String) {
        viewModelScope.launch {
            repository.addCustomHabitCategory(cat)
            customHabitCategories.value = repository.getCustomHabitCategories()
        }
    }

    fun setLinkedStudyRoom(roomId: String?) {
        linkedStudyRoomId.value = roomId
    }

    fun setLinkedFocusTarget(type: String, id: Long?, title: String) {
        linkedFocusTargetType.value = type
        linkedFocusTargetId.value = id
        linkedFocusTargetTitle.value = title
    }

    fun addCustomTaskCategory(cat: String) {
        viewModelScope.launch {
            repository.addCustomTaskCategory(cat)
            customTaskCategories.value = repository.getCustomTaskCategories()
        }
    }

    fun deleteCustomTaskCategory(cat: String) {
        viewModelScope.launch {
            repository.deleteCustomTaskCategory(cat)
            customTaskCategories.value = repository.getCustomTaskCategories()
        }
    }

    fun addCustomWeeklyCategory(cat: String) {
        viewModelScope.launch {
            repository.addCustomWeeklyCategory(cat)
            customWeeklyCategories.value = repository.getCustomWeeklyCategories()
        }
    }

    fun deleteCustomWeeklyCategory(cat: String) {
        viewModelScope.launch {
            repository.deleteCustomWeeklyCategory(cat)
            customWeeklyCategories.value = repository.getCustomWeeklyCategories()
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

    fun takeBreak(minutes: Int = 5) {
        val app = getApplication<Application>()
        app.startService(Intent(app, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_TAKE_BREAK
            putExtra(FocusTimerService.EXTRA_BREAK_MINUTES, minutes)
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
        subtasksJson: String = "[]",
        reminderType: String = "NOTIFICATION",
        reminderSound: String = "default",
        weeklyPlanWeekNumber: Int? = null
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
                subtasksJson = subtasksJson,
                reminderType = reminderType,
                reminderSound = reminderSound,
                weeklyPlanWeekNumber = weeklyPlanWeekNumber
            )
            val id = repository.insertTask(task)
            awardCoins(5, "Created Task")
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

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
            if (task.reminderEnabled && task.dueDateMillis != null) {
                TaskScheduler.scheduleTaskReminder(
                    context = getApplication(),
                    task = task,
                    reminderLeadMinutes = 0
                )
            } else {
                TaskScheduler.cancelTaskReminder(getApplication(), task.id)
            }
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
        reminderSound: String = "zen_bell",
        iconName: String = "icon_leaf"
    ) {
        viewModelScope.launch {
            val id = repository.insertHabit(
                HabitEntity(
                    name = name,
                    description = description,
                    label = label,
                    iconName = iconName,
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
            awardCoins(10, "Created new Habit")
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
        reminderSound: String = "zen_bell",
        iconName: String = "icon_leaf"
    ) {
        viewModelScope.launch {
            val id = repository.insertHabit(
                HabitEntity(
                    name = name,
                    description = description,
                    label = label,
                    iconName = iconName,
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
            awardCoins(20, "Committed to Strict Challenge")
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
        val soundType = try {
            com.example.audio.AlarmSoundType.valueOf(soundName.uppercase())
        } catch (_: Exception) {
            com.example.audio.AlarmSoundType.SUNRISE_MELODY
        }
        com.example.audio.RealAlarmAudioPlayer.previewSound(getApplication(), soundType)
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
        isBedtime: Boolean = false,
        soundName: String = "Sunrise Melody",
        volume: Float = 0.8f,
        ringLengthSeconds: Int = 60,
        snoozeMinutes: Int = 5,
        silenceAfterMinutes: Int = 10,
        isWakeUpAlarm: Boolean = false
    ) {
        viewModelScope.launch {
            val id = repository.insertAlarm(
                AlarmEntity(
                    hour = hour,
                    minute = minute,
                    label = label,
                    daysOfWeekMask = daysOfWeekMask,
                    vibrateEnabled = vibrate,
                    isBedtimeAlarm = isBedtime,
                    soundName = soundName,
                    volume = volume,
                    ringLengthSeconds = ringLengthSeconds,
                    snoozeMinutes = snoozeMinutes,
                    silenceAfterMinutes = silenceAfterMinutes,
                    isWakeUpAlarm = isWakeUpAlarm
                )
            )
            AlarmScheduler.scheduleAlarm(getApplication(), id, hour, minute, label, vibrate, isBedtime)
        }
    }

    fun updateAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.updateAlarm(alarm)
            if (alarm.isEnabled) {
                AlarmScheduler.scheduleAlarm(
                    getApplication(),
                    alarm.id,
                    alarm.hour,
                    alarm.minute,
                    alarm.label,
                    alarm.vibrateEnabled,
                    alarm.isBedtimeAlarm
                )
            } else {
                AlarmScheduler.cancelAlarm(getApplication(), alarm.id)
            }
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

    fun updateDailyRoutine(routine: com.example.data.entity.DailyRoutineEntity) {
        viewModelScope.launch {
            repository.updateDailyRoutine(routine)
        }
    }

    fun reorderDailyRoutine(id: Long, moveUp: Boolean) {
        viewModelScope.launch {
            repository.reorderDailyRoutine(id, moveUp)
        }
    }

    fun reorderWeeklyPlan(id: Long, moveUp: Boolean) {
        viewModelScope.launch {
            repository.reorderWeeklyPlan(id, moveUp)
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

    suspend fun getOpenAiApiKey(): String = repository.getSetting("openai_api_key", "")
    suspend fun getGeminiApiKey(): String = repository.getSetting("gemini_api_key", "")
    suspend fun getAiProvider(): String = repository.getSetting("ai_provider", "AUTO")

    fun saveAiConfiguration(openAiKey: String, geminiKey: String, provider: String) {
        viewModelScope.launch {
            repository.setSetting("openai_api_key", openAiKey.trim())
            repository.setSetting("gemini_api_key", geminiKey.trim())
            repository.setSetting("ai_provider", provider.trim())
            if (openAiKey.isNotBlank()) {
                repository.setSetting("ai_api_key", openAiKey.trim())
            } else if (geminiKey.isNotBlank()) {
                repository.setSetting("ai_api_key", geminiKey.trim())
            }
        }
    }

    // --- Manifestations ---
    fun addManifestation(
        title: String,
        description: String = "",
        imageUri: String? = null,
        priority: String = "HIGH",
        reminderEnabled: Boolean = false,
        reminderTimeString: String? = "08:00"
    ) {
        viewModelScope.launch {
            repository.insertManifestation(
                com.example.data.entity.ManifestationEntity(
                    title = title,
                    description = description,
                    imageUri = imageUri,
                    priority = priority,
                    reminderEnabled = reminderEnabled,
                    reminderTimeString = reminderTimeString,
                    sortOrder = (allManifestations.value.size)
                )
            )
            awardCoins(15, "Created new Manifestation")
        }
    }

    fun updateManifestation(manifestation: com.example.data.entity.ManifestationEntity) {
        viewModelScope.launch {
            repository.updateManifestation(manifestation)
        }
    }

    fun deleteManifestation(id: Long) {
        viewModelScope.launch {
            repository.deleteManifestation(id)
        }
    }

    fun reorderManifestation(item: com.example.data.entity.ManifestationEntity, moveUp: Boolean) {
        viewModelScope.launch {
            val list = allManifestations.value.toMutableList()
            val index = list.indexOfFirst { it.id == item.id }
            if (index != -1) {
                val targetIndex = if (moveUp) index - 1 else index + 1
                if (targetIndex in 0 until list.size) {
                    val other = list[targetIndex]
                    repository.updateManifestation(item.copy(sortOrder = targetIndex))
                    repository.updateManifestation(other.copy(sortOrder = index))
                }
            }
        }
    }

    // --- App Usage Limits ---
    fun saveAppLimit(limit: com.example.data.entity.AppUsageLimitEntity) {
        viewModelScope.launch {
            repository.saveAppUsageLimit(limit)
        }
    }

    fun deleteAppLimit(packageName: String) {
        viewModelScope.launch {
            repository.deleteAppUsageLimit(packageName)
        }
    }

    // Linked Focus Completion
    fun completeLinkedFocusTarget() {
        viewModelScope.launch {
            val targetType = linkedFocusTargetType.value
            val targetId = linkedFocusTargetId.value
            if (targetId != null) {
                when (targetType) {
                    "TASK" -> {
                        val task = repository.getTaskById(targetId)
                        if (task != null) {
                            repository.updateTask(task.copy(isCompleted = true, completedAt = System.currentTimeMillis()))
                            awardCoins(15, "Completed linked Task in Focus Session")
                        }
                    }
                    "HABIT" -> {
                        val todayStr = repository.getTodayDateString()
                        repository.insertCompletion(
                            com.example.data.entity.HabitCompletionEntity(
                                habitId = targetId,
                                dateString = todayStr,
                                isCompleted = true,
                                perfectionScore = 100,
                                note = "Completed via linked Focus Session"
                            )
                        )
                        awardCoins(15, "Completed linked Habit in Focus Session")
                    }
                    "CHALLENGE" -> {
                        val todayStr = repository.getTodayDateString()
                        repository.insertCompletion(
                            com.example.data.entity.HabitCompletionEntity(
                                habitId = targetId,
                                dateString = todayStr,
                                isCompleted = true,
                                perfectionScore = 100,
                                note = "Conquered linked Challenge day via Focus Session"
                            )
                        )
                        awardCoins(25, "Conquered linked Challenge in Focus Session")
                    }
                    "WEEKLY_PLAN" -> {
                        repository.toggleWeeklyPlan(targetId, true)
                        awardCoins(30, "Achieved linked Weekly Objective in Focus Session")
                    }
                }
            }
        }
    }
}

