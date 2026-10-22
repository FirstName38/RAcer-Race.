package com.example.data.repository

import android.content.Context
import com.example.data.database.AppDatabase
import com.example.data.entity.AlarmEntity
import com.example.data.entity.AppUsageEntity
import com.example.data.entity.ChatMessageEntity
import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.HabitCompletionEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.JournalEntryEntity
import com.example.data.entity.SettingsEntity
import com.example.data.entity.SpecialDateEntity
import com.example.data.entity.TaskEntity
import com.example.data.entity.WellnessLogEntity
import com.example.data.entity.WellnessRoutineEntity
import com.example.data.entity.WeeklyPlanEntity
import com.example.data.model.FocusMode
import com.example.data.model.FocusSound
import com.example.data.model.FocusWallpaper
import com.example.data.model.HabitFrequency
import com.example.data.model.JournalMood
import com.example.data.model.TaskPriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RacerRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val focusDao = db.focusDao()
    private val taskDao = db.taskDao()
    private val habitDao = db.habitDao()
    private val journalDao = db.journalDao()
    private val alarmDao = db.alarmDao()
    private val appUsageDao = db.appUsageDao()
    private val settingsDao = db.settingsDao()
    private val chatDao = db.chatDao()
    private val specialDateDao = db.specialDateDao()
    private val wellnessDao = db.wellnessDao()
    private val dailyRoutineDao = db.dailyRoutineDao()
    private val weeklyPlanDao = db.weeklyPlanDao()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getTodayDateString(): String = dateFormat.format(Date())

    // --- Focus Sessions ---
    val allFocusSessions: Flow<List<FocusSessionEntity>> = focusDao.getAllSessions()
    val latestFocusSession: Flow<FocusSessionEntity?> = focusDao.getLatestSession()

    fun getTodayFocusSessions(): Flow<List<FocusSessionEntity>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1
        return focusDao.getSessionsForDay(startOfDay, endOfDay)
    }

    suspend fun saveFocusSession(session: FocusSessionEntity): Long = withContext(Dispatchers.IO) {
        focusDao.insertSession(session)
    }

    suspend fun updateFocusSession(session: FocusSessionEntity) = withContext(Dispatchers.IO) {
        focusDao.updateSession(session)
    }

    suspend fun updateSessionNote(id: Long, note: String) = withContext(Dispatchers.IO) {
        focusDao.updateSessionNote(id, note)
    }

    suspend fun deleteFocusSession(id: Long) = withContext(Dispatchers.IO) {
        focusDao.deleteSessionById(id)
    }

    suspend fun deleteAllFocusSessions() = withContext(Dispatchers.IO) {
        focusDao.deleteAllSessions()
    }

    // --- Tasks ---
    val allActiveTasks: Flow<List<TaskEntity>> = taskDao.getAllActiveTasks()
    val completedTasks: Flow<List<TaskEntity>> = taskDao.getCompletedTasks()
    val archivedTasks: Flow<List<TaskEntity>> = taskDao.getArchivedTasks()

    suspend fun getTaskById(id: Long): TaskEntity? = withContext(Dispatchers.IO) {
        taskDao.getTaskById(id)
    }

    suspend fun insertTask(task: TaskEntity): Long = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        taskDao.deleteTaskById(id)
    }

    suspend fun toggleTaskCompleted(taskId: Long) = withContext(Dispatchers.IO) {
        val task = taskDao.getTaskById(taskId) ?: return@withContext
        val newStatus = !task.isCompleted
        val updated = task.copy(
            isCompleted = newStatus,
            completedAt = if (newStatus) System.currentTimeMillis() else null
        )
        taskDao.updateTask(updated)
    }

    suspend fun archiveTask(taskId: Long) = withContext(Dispatchers.IO) {
        val task = taskDao.getTaskById(taskId) ?: return@withContext
        taskDao.updateTask(task.copy(isArchived = true))
    }

    // --- Habits ---
    val activeHabits: Flow<List<HabitEntity>> = habitDao.getActiveHabits()
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    val allCompletions: Flow<List<HabitCompletionEntity>> = habitDao.getAllCompletions()

    fun getCompletionsForDate(dateString: String): Flow<List<HabitCompletionEntity>> =
        habitDao.getCompletionsForDate(dateString)

    suspend fun insertHabit(habit: HabitEntity): Long = withContext(Dispatchers.IO) {
        habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: HabitEntity) = withContext(Dispatchers.IO) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: HabitEntity) = withContext(Dispatchers.IO) {
        habitDao.deleteHabit(habit)
        habitDao.deleteCompletionsForHabit(habit.id)
    }

    suspend fun deleteHabitById(id: Long) = withContext(Dispatchers.IO) {
        habitDao.deleteHabitById(id)
        habitDao.deleteCompletionsForHabit(id)
    }

    suspend fun getCustomHabitCategories(): List<String> = withContext(Dispatchers.IO) {
        val saved = getSetting("custom_habit_categories", "")
        if (saved.isBlank()) emptyList() else saved.split("|").filter { it.isNotBlank() }
    }

    suspend fun addCustomHabitCategory(cat: String) = withContext(Dispatchers.IO) {
        val clean = cat.trim()
        if (clean.isBlank()) return@withContext
        val current = getCustomHabitCategories().toMutableList()
        if (!current.contains(clean)) {
            current.add(clean)
            setSetting("custom_habit_categories", current.joinToString("|"))
        }
    }

    suspend fun toggleHabitCompletion(habitId: Long, dateString: String) = withContext(Dispatchers.IO) {
        val existing = habitDao.getCompletionsForDate(dateString).firstOrNull()?.find { it.habitId == habitId }
        if (existing != null) {
            habitDao.deleteCompletion(habitId, dateString)
        } else {
            habitDao.insertCompletion(
                HabitCompletionEntity(
                    habitId = habitId,
                    dateString = dateString,
                    isCompleted = true,
                    perfectionScore = 100,
                    note = ""
                )
            )
        }
    }

    suspend fun recordHabitCompletion(
        habitId: Long,
        dateString: String,
        isCompleted: Boolean,
        perfectionScore: Int = 100,
        note: String = ""
    ) = withContext(Dispatchers.IO) {
        if (!isCompleted) {
            habitDao.deleteCompletion(habitId, dateString)
        } else {
            habitDao.insertCompletion(
                HabitCompletionEntity(
                    habitId = habitId,
                    dateString = dateString,
                    isCompleted = true,
                    perfectionScore = perfectionScore.coerceIn(0, 100),
                    note = note
                )
            )
        }
    }

    fun calculateHabitStreak(habitId: Long, completions: List<HabitCompletionEntity>): Int {
        val habitCompletions = completions.filter { it.habitId == habitId && it.isCompleted }.map { it.dateString }.toSet()
        if (habitCompletions.isEmpty()) return 0

        val cal = Calendar.getInstance()
        val todayStr = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(cal.time)

        // If not completed today and not completed yesterday, challenge streak resets to 0
        if (!habitCompletions.contains(todayStr) && !habitCompletions.contains(yesterdayStr)) {
            return 0
        }

        var streak = 0
        val checkCal = Calendar.getInstance()
        if (habitCompletions.contains(todayStr)) {
            streak = 1
            while (true) {
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
                val str = dateFormat.format(checkCal.time)
                if (habitCompletions.contains(str)) {
                    streak++
                } else {
                    break
                }
            }
        } else if (habitCompletions.contains(yesterdayStr)) {
            streak = 1
            checkCal.add(Calendar.DAY_OF_YEAR, -1) // at yesterday
            while (true) {
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
                val str = dateFormat.format(checkCal.time)
                if (habitCompletions.contains(str)) {
                    streak++
                } else {
                    break
                }
            }
        }
        return streak
    }

    // --- Special Dates ---
    val allSpecialDates: Flow<List<SpecialDateEntity>> = specialDateDao.getAllSpecialDates()

    fun getSpecialDate(dateString: String): Flow<SpecialDateEntity?> = specialDateDao.getSpecialDate(dateString)

    suspend fun setSpecialDate(dateString: String, title: String, colorHex: String = "#EC4899", note: String = "") = withContext(Dispatchers.IO) {
        specialDateDao.insertOrUpdate(
            SpecialDateEntity(
                dateString = dateString,
                title = title,
                colorHex = colorHex,
                note = note
            )
        )
    }

    suspend fun deleteSpecialDate(dateString: String) = withContext(Dispatchers.IO) {
        specialDateDao.deleteSpecialDate(dateString)
    }

    // --- Mental Health, Yoga, Mobility & Stretch Routines ---
    val allWellnessRoutines: Flow<List<WellnessRoutineEntity>> = wellnessDao.getAllRoutines()
    val allWellnessLogs: Flow<List<WellnessLogEntity>> = wellnessDao.getAllLogs()

    fun getWellnessLogsForDate(dateString: String): Flow<List<WellnessLogEntity>> =
        wellnessDao.getLogsForDate(dateString)

    suspend fun insertWellnessRoutine(routine: WellnessRoutineEntity): Long = withContext(Dispatchers.IO) {
        wellnessDao.insertRoutine(routine)
    }

    suspend fun deleteWellnessRoutine(routine: WellnessRoutineEntity) = withContext(Dispatchers.IO) {
        wellnessDao.deleteRoutine(routine)
    }

    suspend fun logWellnessSession(
        routineId: Long,
        routineTitle: String,
        category: String,
        durationSeconds: Int,
        feelingRating: Int,
        notes: String
    ) = withContext(Dispatchers.IO) {
        wellnessDao.insertLog(
            WellnessLogEntity(
                routineId = routineId,
                routineTitle = routineTitle,
                category = category,
                durationSeconds = durationSeconds,
                dateString = getTodayDateString(),
                feelingRating = feelingRating,
                reflectionNotes = notes
            )
        )
    }

    suspend fun seedDefaultWellnessRoutinesIfEmpty() = withContext(Dispatchers.IO) {
        val existing = wellnessDao.getAllRoutines().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val defaults = listOf(
                WellnessRoutineEntity(
                    title = "Sun Salutation & Flow",
                    category = "YOGA",
                    durationMinutes = 10,
                    description = "A gentle morning vinyasa flow to awaken the spine and harmonize breath.",
                    iconName = "self_improvement",
                    colorHex = "#F59E0B",
                    stepsJson = """[{"name":"Mountain Pose (Tadasana)","durationSeconds":30,"cue":"Ground through all four corners of your feet."},{"name":"Upward Salute (Urdhva Hastasana)","durationSeconds":30,"cue":"Reach fingertips to sky, relax shoulders."},{"name":"Standing Forward Fold (Uttanasana)","durationSeconds":45,"cue":"Hinge at hips, bend knees slightly."},{"name":"Halfway Lift (Ardha Uttanasana)","durationSeconds":30,"cue":"Flat back, elongate neck."},{"name":"Plank to Chaturanga","durationSeconds":30,"cue":"Engage core, lower slowly."},{"name":"Upward-Facing Dog","durationSeconds":45,"cue":"Open chest, press tops of feet into mat."},{"name":"Downward-Facing Dog","durationSeconds":60,"cue":"Push hips high, lengthen spine."},{"name":"Child's Pose Rest","durationSeconds":60,"cue":"Breathe deeply into lower back."}]"""
                ),
                WellnessRoutineEntity(
                    title = "Desk Neck & Spine Decompression",
                    category = "MOBILITY",
                    durationMinutes = 6,
                    description = "Relieve computer posture, tight neck extensors, and rounded upper shoulders.",
                    iconName = "accessibility_new",
                    colorHex = "#06B6D4",
                    stepsJson = """[{"name":"Chin Tucks","durationSeconds":45,"cue":"Gently pull chin straight back like making a double chin."},{"name":"Lateral Neck Release","durationSeconds":60,"cue":"Ear towards shoulder, breathe into opposite trapezius."},{"name":"Seated Cat-Cow Spine Waves","durationSeconds":60,"cue":"Arch back on inhale, round back on exhale."},{"name":"Thoracic Rotations","durationSeconds":60,"cue":"Hand behind head, rotate elbow towards ceiling."},{"name":"Chest & Shoulder Wall Openers","durationSeconds":60,"cue":"Open pectoral muscles and ribcage."}]"""
                ),
                WellnessRoutineEntity(
                    title = "Deep Full-Body Stretch & Unwind",
                    category = "STRETCH",
                    durationMinutes = 8,
                    description = "Slow, restorative stretches for tight hamstrings, hips, and lower back.",
                    iconName = "fitness_center",
                    colorHex = "#8B5CF6",
                    stepsJson = """[{"name":"Seated Forward Fold","durationSeconds":60,"cue":"Softly reach towards ankles, release lower back."},{"name":"Pigeon Pose / Figure Four (Right)","durationSeconds":75,"cue":"Deep glute and piriformis tension release."},{"name":"Pigeon Pose / Figure Four (Left)","durationSeconds":75,"cue":"Mirror the opening on left hip."},{"name":"Butterfly Pose (Baddha Konasana)","durationSeconds":60,"cue":"Soles together, gently let knees fall open."},{"name":"Supine Spinal Twist","durationSeconds":90,"cue":"Knees fall to side, arms spread wide."}]"""
                ),
                WellnessRoutineEntity(
                    title = "Box Breathing & Somatic Reset",
                    category = "BREATHING",
                    durationMinutes = 5,
                    description = "Regulate the autonomic nervous system: 4s Inhale, 4s Hold, 4s Exhale, 4s Hold.",
                    iconName = "air",
                    colorHex = "#10B981",
                    stepsJson = """[{"name":"Phase 1: Deep Slow Inhale (4s)","durationSeconds":60,"cue":"Fill belly, ribs, and chest evenly."},{"name":"Phase 2: Gentle Lung Hold (4s)","durationSeconds":60,"cue":"Soft throat, relaxed shoulders."},{"name":"Phase 3: Smooth Controlled Exhale (4s)","durationSeconds":60,"cue":"Empty lungs fully through mouth."},{"name":"Phase 4: Empty Pause (4s)","durationSeconds":60,"cue":"Feel absolute stillness and calm."},{"name":"Somatic Body Scan","durationSeconds":60,"cue":"Release any lingering jaw and eyebrow tension."}]"""
                ),
                WellnessRoutineEntity(
                    title = "ADHD Calm Down & Brain Reset",
                    category = "MENTAL_HEALTH",
                    durationMinutes = 4,
                    description = "Quick grounding exercise using 5-4-3-2-1 sensory orientation and somatic shaking.",
                    iconName = "psychology",
                    colorHex = "#EC4899",
                    stepsJson = """[{"name":"Somatic Arm & Leg Shakeout","durationSeconds":45,"cue":"Vigorously shake arms, hands, and legs to discharge adrenaline."},{"name":"5 Things You Can See","durationSeconds":45,"cue":"Notice 5 physical colors and textures around the room."},{"name":"4 Things You Can Touch","durationSeconds":45,"cue":"Feel texture of clothes, desk, and ground."},{"name":"3 Things You Can Hear","durationSeconds":45,"cue":"Listen to ambient room sounds without judgment."},{"name":"Grounding Deep Breath","durationSeconds":60,"cue":"Hand on chest, you are safe and in control."}]"""
                )
            )
            wellnessDao.insertAllRoutines(defaults)
        }
    }

    // --- Daily Routine ---
    val allDailyRoutines: Flow<List<com.example.data.entity.DailyRoutineEntity>> = dailyRoutineDao.getAllRoutines()

    suspend fun insertDailyRoutine(routine: com.example.data.entity.DailyRoutineEntity): Long = withContext(Dispatchers.IO) {
        dailyRoutineDao.insertRoutine(routine)
    }

    suspend fun updateDailyRoutine(routine: com.example.data.entity.DailyRoutineEntity) = withContext(Dispatchers.IO) {
        dailyRoutineDao.updateRoutine(routine)
    }

    suspend fun deleteDailyRoutine(id: Long) = withContext(Dispatchers.IO) {
        dailyRoutineDao.deleteRoutineById(id)
    }

    suspend fun toggleDailyRoutine(id: Long) = withContext(Dispatchers.IO) {
        val routine = dailyRoutineDao.getRoutineById(id) ?: return@withContext
        val todayStr = getTodayDateString()
        val newStatus = if (routine.lastCompletedDate == todayStr) !routine.isCompletedToday else true
        dailyRoutineDao.updateCompletion(id, newStatus, todayStr)
    }

    suspend fun resetDailyRoutines() = withContext(Dispatchers.IO) {
        dailyRoutineDao.resetAllCompletions()
    }

    suspend fun checkDailyRoutineReset() = withContext(Dispatchers.IO) {
        dailyRoutineDao.resetIfNotToday(getTodayDateString())
    }

    suspend fun seedDefaultDailyRoutinesIfEmpty() = withContext(Dispatchers.IO) {
        val existing = dailyRoutineDao.getAllRoutines().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val defaults = listOf(
                com.example.data.entity.DailyRoutineEntity(
                    title = "Wake Up & 500ml Water",
                    timeSlot = "MORNING",
                    scheduledTime = "06:30",
                    durationMinutes = 5,
                    iconName = "wb_sunny",
                    colorHex = "#F59E0B",
                    cueNote = "Kickstart hydration and metabolic alert systems.",
                    actionType = "NONE",
                    sortOrder = 1
                ),
                com.example.data.entity.DailyRoutineEntity(
                    title = "Box Breathing 4-4-4-4 Practice",
                    timeSlot = "MORNING",
                    scheduledTime = "06:45",
                    durationMinutes = 5,
                    iconName = "air",
                    colorHex = "#06B6D4",
                    cueNote = "Calm autonomic nervous system with 4s inhale/hold/exhale.",
                    actionType = "BREATHING",
                    sortOrder = 2
                ),
                com.example.data.entity.DailyRoutineEntity(
                    title = "Sun Salutation & Flow",
                    timeSlot = "MORNING",
                    scheduledTime = "07:00",
                    durationMinutes = 10,
                    iconName = "self_improvement",
                    colorHex = "#10B981",
                    cueNote = "Awaken thoracic spine and loosen hip flexors.",
                    actionType = "YOGA",
                    sortOrder = 3
                ),
                com.example.data.entity.DailyRoutineEntity(
                    title = "Daily Focus Plan & Top 3 Tasks",
                    timeSlot = "MORNING",
                    scheduledTime = "07:30",
                    durationMinutes = 15,
                    iconName = "check_circle",
                    colorHex = "#8B5CF6",
                    cueNote = "Select the single highest leverage task before opening email.",
                    actionType = "JOURNAL",
                    sortOrder = 4
                ),
                com.example.data.entity.DailyRoutineEntity(
                    title = "Deep Work Focus Block #1",
                    timeSlot = "DEEP_WORK",
                    scheduledTime = "09:00",
                    durationMinutes = 50,
                    iconName = "timer",
                    colorHex = "#EC4899",
                    cueNote = "High cognitive effort. Phones on silent, zero interruption.",
                    actionType = "FOCUS",
                    sortOrder = 5
                ),
                com.example.data.entity.DailyRoutineEntity(
                    title = "Desk Neck & Spine Decompression",
                    timeSlot = "MIDDAY",
                    scheduledTime = "12:30",
                    durationMinutes = 6,
                    iconName = "accessibility_new",
                    colorHex = "#06B6D4",
                    cueNote = "Neutralize computer hunch and eye strain.",
                    actionType = "STRETCH",
                    sortOrder = 6
                ),
                com.example.data.entity.DailyRoutineEntity(
                    title = "Mindful Nutrient Lunch & Screen Break",
                    timeSlot = "MIDDAY",
                    scheduledTime = "13:00",
                    durationMinutes = 45,
                    iconName = "restaurant",
                    colorHex = "#10B981",
                    cueNote = "Eat away from your work monitor.",
                    actionType = "NONE",
                    sortOrder = 7
                ),
                com.example.data.entity.DailyRoutineEntity(
                    title = "Afternoon Execution Sprint #2",
                    timeSlot = "AFTERNOON",
                    scheduledTime = "14:30",
                    durationMinutes = 45,
                    iconName = "timer",
                    colorHex = "#8B5CF6",
                    cueNote = "Task clearance and administrative follow-through.",
                    actionType = "FOCUS",
                    sortOrder = 8
                ),
                com.example.data.entity.DailyRoutineEntity(
                    title = "Outdoor 20-Minute Brisk Walk",
                    timeSlot = "AFTERNOON",
                    scheduledTime = "17:30",
                    durationMinutes = 20,
                    iconName = "directions_walk",
                    colorHex = "#F59E0B",
                    cueNote = "Natural sunlight and optic flow reset.",
                    actionType = "NONE",
                    sortOrder = 9
                ),
                com.example.data.entity.DailyRoutineEntity(
                    title = "Digital Detox & Evening Journal",
                    timeSlot = "EVENING",
                    scheduledTime = "21:00",
                    durationMinutes = 15,
                    iconName = "edit_note",
                    colorHex = "#8B5CF6",
                    cueNote = "Log 1 thing learned, 1 thing felt, and gratitude.",
                    actionType = "JOURNAL",
                    sortOrder = 10
                ),
                com.example.data.entity.DailyRoutineEntity(
                    title = "4-7-8 Relaxation Breathing & Bedtime",
                    timeSlot = "NIGHT",
                    scheduledTime = "22:30",
                    durationMinutes = 10,
                    iconName = "bedtime",
                    colorHex = "#6366F1",
                    cueNote = "Activate parasympathetic tone for deep restorative sleep.",
                    actionType = "BREATHING",
                    sortOrder = 11
                )
            )
            dailyRoutineDao.insertAllRoutines(defaults)
        }
    }

    // --- Journal ---
    val allJournalEntries: Flow<List<JournalEntryEntity>> = journalDao.getAllEntries()

    fun getJournalEntryForDate(dateString: String): Flow<JournalEntryEntity?> =
        journalDao.getEntryForDate(dateString)

    suspend fun saveJournalEntry(entry: JournalEntryEntity): Long = withContext(Dispatchers.IO) {
        journalDao.insertEntry(entry)
    }

    suspend fun deleteJournalEntry(id: Long) = withContext(Dispatchers.IO) {
        journalDao.deleteEntryById(id)
    }

    // --- Alarms ---
    val allAlarms: Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()

    suspend fun insertAlarm(alarm: AlarmEntity): Long = withContext(Dispatchers.IO) {
        alarmDao.insertAlarm(alarm)
    }

    suspend fun updateAlarm(alarm: AlarmEntity) = withContext(Dispatchers.IO) {
        alarmDao.updateAlarm(alarm)
    }

    suspend fun deleteAlarm(id: Long) = withContext(Dispatchers.IO) {
        alarmDao.deleteAlarmById(id)
    }

    // --- App Usage & Blocking ---
    val blockedApps: Flow<List<AppUsageEntity>> = appUsageDao.getBlockedApps()

    fun getUsageForDate(dateString: String): Flow<List<AppUsageEntity>> =
        appUsageDao.getUsageForDate(dateString)

    suspend fun updateUsageList(list: List<AppUsageEntity>) = withContext(Dispatchers.IO) {
        appUsageDao.insertOrUpdateUsage(list)
    }

    suspend fun setAppBlocked(packageName: String, blocked: Boolean) = withContext(Dispatchers.IO) {
        appUsageDao.setAppBlocked(packageName, blocked)
    }

    // --- Chat & AI ---
    val chatMessages: Flow<List<ChatMessageEntity>> = chatDao.getMessagesByCategory("chat")
    fun getMessagesByCategory(category: String): Flow<List<ChatMessageEntity>> = chatDao.getMessagesByCategory(category)

    suspend fun insertChatMessage(message: ChatMessageEntity): Long = withContext(Dispatchers.IO) {
        chatDao.insertMessage(message)
    }

    suspend fun clearChatHistory(category: String = "chat") = withContext(Dispatchers.IO) {
        chatDao.deleteMessagesByCategory(category)
    }

    // --- Settings Key-Value ---
    suspend fun getSetting(key: String, defaultValue: String): String = withContext(Dispatchers.IO) {
        settingsDao.getSettingValue(key) ?: defaultValue
    }

    fun getSettingFlow(key: String, defaultValue: String): Flow<String> {
        return settingsDao.getSettingValueFlow(key).map { it ?: defaultValue }
    }

    suspend fun setSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        settingsDao.setSetting(SettingsEntity(key, value))
    }

    // --- Analytics & Statistics Calculations ---
    suspend fun calculateStats(): FocusStatistics = withContext(Dispatchers.IO) {
        val all = focusDao.getAllSessions().firstOrNull() ?: emptyList()
        val now = System.currentTimeMillis()
        val oneDayAgo = now - 24 * 60 * 60 * 1000L
        val oneWeekAgo = now - 7 * 24 * 60 * 60 * 1000L
        val oneMonthAgo = now - 30 * 24 * 60 * 60 * 1000L
        val oneYearAgo = now - 365 * 24 * 60 * 60 * 1000L

        val todaySeconds = all.filter { it.startTime >= oneDayAgo }.sumOf { it.actualDurationSeconds.toLong() }
        val weekSeconds = all.filter { it.startTime >= oneWeekAgo }.sumOf { it.actualDurationSeconds.toLong() }
        val monthSeconds = all.filter { it.startTime >= oneMonthAgo }.sumOf { it.actualDurationSeconds.toLong() }
        val yearSeconds = all.filter { it.startTime >= oneYearAgo }.sumOf { it.actualDurationSeconds.toLong() }
        val totalSeconds = all.sumOf { it.actualDurationSeconds.toLong() }

        val totalSessions = all.size
        val completedCount = all.count { it.isCompleted }
        val completionRate = if (totalSessions > 0) (completedCount.toFloat() / totalSessions) * 100f else 0f
        val avgSessionLengthSeconds = if (totalSessions > 0) (totalSeconds / totalSessions).toInt() else 0
        val longestSessionSeconds = all.maxOfOrNull { it.actualDurationSeconds } ?: 0

        // Best focus hour of day (0-23)
        val hourDistribution = IntArray(24)
        val dayOfWeekDistribution = IntArray(7) // Sun=0..Sat=6
        for (session in all) {
            val cal = Calendar.getInstance().apply { timeInMillis = session.startTime }
            val hour = cal.get(Calendar.HOUR_OF_DAY).coerceIn(0, 23)
            val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)
            hourDistribution[hour] += session.actualDurationSeconds
            dayOfWeekDistribution[dayOfWeek] += session.actualDurationSeconds
        }
        val bestHour = hourDistribution.indices.maxByOrNull { hourDistribution[it] } ?: 10
        val bestDayOfWeek = dayOfWeekDistribution.indices.maxByOrNull { dayOfWeekDistribution[it] } ?: 1

        val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

        // Streak calculation (consecutive active days)
        val distinctDays = all.map {
            dateFormat.format(Date(it.startTime))
        }.distinct().sortedDescending()

        var currentStreak = 0
        var checkCal = Calendar.getInstance()
        var expectedDateStr = dateFormat.format(checkCal.time)
        if (distinctDays.contains(expectedDateStr)) {
            currentStreak = 1
            while (true) {
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
                expectedDateStr = dateFormat.format(checkCal.time)
                if (distinctDays.contains(expectedDateStr)) {
                    currentStreak++
                } else {
                    break
                }
            }
        } else {
            // Check if streak was active yesterday
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
            expectedDateStr = dateFormat.format(checkCal.time)
            if (distinctDays.contains(expectedDateStr)) {
                var s = 1
                while (true) {
                    checkCal.add(Calendar.DAY_OF_YEAR, -1)
                    expectedDateStr = dateFormat.format(checkCal.time)
                    if (distinctDays.contains(expectedDateStr)) {
                        s++
                    } else {
                        break
                    }
                }
                currentStreak = s
            }
        }

        FocusStatistics(
            todaySeconds = todaySeconds,
            weekSeconds = weekSeconds,
            monthSeconds = monthSeconds,
            yearSeconds = yearSeconds,
            totalSeconds = totalSeconds,
            totalSessions = totalSessions,
            completedSessions = completedCount,
            completionRate = completionRate,
            avgSessionLengthSeconds = avgSessionLengthSeconds,
            longestSessionSeconds = longestSessionSeconds,
            bestHourOfDay = bestHour,
            bestDayOfWeek = dayNames[bestDayOfWeek],
            currentStreak = currentStreak,
            hourDistribution = hourDistribution.toList()
        )
    }

    // --- JSON Export / Import ---
    suspend fun exportDataJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val allTasks = taskDao.getAllActiveTasks().firstOrNull() ?: emptyList()
        val allHabitsList = habitDao.getAllHabits().firstOrNull() ?: emptyList()
        val allCompletionsList = habitDao.getAllCompletions().firstOrNull() ?: emptyList()
        val allJournals = journalDao.getAllEntries().firstOrNull() ?: emptyList()
        val allSessions = focusDao.getAllSessions().firstOrNull() ?: emptyList()

        val tasksArr = JSONArray()
        for (t in allTasks) {
            val o = JSONObject()
            o.put("title", t.title)
            o.put("description", t.description)
            o.put("priority", t.priority.name)
            o.put("category", t.category)
            o.put("isCompleted", t.isCompleted)
            tasksArr.put(o)
        }
        root.put("tasks", tasksArr)

        val habitsArr = JSONArray()
        for (h in allHabitsList) {
            val o = JSONObject()
            o.put("name", h.name)
            o.put("frequency", h.frequency.name)
            o.put("targetCount", h.targetCountPerPeriod)
            o.put("colorHex", h.colorHex)
            habitsArr.put(o)
        }
        root.put("habits", habitsArr)

        val journalsArr = JSONArray()
        for (j in allJournals) {
            val o = JSONObject()
            o.put("dateString", j.dateString)
            o.put("learnedText", j.learnedText)
            o.put("feltText", j.feltText)
            o.put("wantToDoText", j.wantToDoText)
            o.put("gratitudeText", j.gratitudeText)
            o.put("freeformNotes", j.freeformNotes)
            o.put("mood", j.mood.name)
            journalsArr.put(o)
        }
        root.put("journal", journalsArr)

        val focusArr = JSONArray()
        for (f in allSessions) {
            val o = JSONObject()
            o.put("startTime", f.startTime)
            o.put("actualDurationSeconds", f.actualDurationSeconds)
            o.put("mode", f.mode.name)
            o.put("isCompleted", f.isCompleted)
            focusArr.put(o)
        }
        root.put("focusSessions", focusArr)

        root.toString(2)
    }

    suspend fun deleteAllData() = withContext(Dispatchers.IO) {
        focusDao.deleteAllSessions()
        taskDao.deleteAllTasks()
        habitDao.deleteAllHabits()
        habitDao.deleteAllCompletions()
        journalDao.deleteAllEntries()
        alarmDao.deleteAllAlarms()
        chatDao.deleteAllMessages()
    }

    suspend fun deleteJournalData() = withContext(Dispatchers.IO) {
        journalDao.deleteAllEntries()
    }

    suspend fun deleteFocusHistory() = withContext(Dispatchers.IO) {
        focusDao.deleteAllSessions()
    }

    suspend fun deleteAIChatHistory() = withContext(Dispatchers.IO) {
        chatDao.deleteAllMessages()
    }

    // --- Weekly Plans ---
    val allWeeklyPlans: Flow<List<WeeklyPlanEntity>> = weeklyPlanDao.getAllWeeklyPlans()

    fun getPlansForWeek(week: Int, year: Int): Flow<List<WeeklyPlanEntity>> =
        weeklyPlanDao.getPlansForWeek(week, year)

    suspend fun insertWeeklyPlan(plan: WeeklyPlanEntity): Long = withContext(Dispatchers.IO) {
        weeklyPlanDao.insertWeeklyPlan(plan)
    }

    suspend fun updateWeeklyPlan(plan: WeeklyPlanEntity) = withContext(Dispatchers.IO) {
        weeklyPlanDao.updateWeeklyPlan(plan)
    }

    suspend fun deleteWeeklyPlan(id: Long) = withContext(Dispatchers.IO) {
        weeklyPlanDao.deleteWeeklyPlanById(id)
    }

    suspend fun toggleWeeklyPlan(id: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        weeklyPlanDao.toggleWeeklyPlan(id, isCompleted)
    }

    suspend fun seedDefaultWeeklyPlansIfEmpty(weekNumber: Int, year: Int) = withContext(Dispatchers.IO) {
        val existing = weeklyPlanDao.getPlansForWeek(weekNumber, year).firstOrNull() ?: emptyList()
        if (existing.isEmpty()) {
            val defaults = listOf(
                WeeklyPlanEntity(
                    weekNumber = weekNumber,
                    year = year,
                    title = "Deep Work: 10 Hours Milestone",
                    description = "Accumulate 10 high-quality Pomodoro or ADHD gentle focus hours",
                    dayOfWeek = "ALL",
                    isCompleted = false,
                    priority = "HIGH",
                    category = "Productivity",
                    targetTimeMinutes = 600
                ),
                WeeklyPlanEntity(
                    weekNumber = weekNumber,
                    year = year,
                    title = "Review Weekly Goals & Priorities",
                    description = "Set top 3 focal milestones for the upcoming cycle",
                    dayOfWeek = "MON",
                    isCompleted = true,
                    priority = "HIGH",
                    category = "Planning",
                    targetTimeMinutes = 30
                ),
                WeeklyPlanEntity(
                    weekNumber = weekNumber,
                    year = year,
                    title = "Mid-week Energy Calibration",
                    description = "Do a 15-minute box breathing reset & desk stretches",
                    dayOfWeek = "WED",
                    isCompleted = false,
                    priority = "MEDIUM",
                    category = "Wellness",
                    targetTimeMinutes = 15
                ),
                WeeklyPlanEntity(
                    weekNumber = weekNumber,
                    year = year,
                    title = "Finish Major Project Milestone",
                    description = "Submit or complete primary work/study deliverables",
                    dayOfWeek = "THU",
                    isCompleted = false,
                    priority = "HIGH",
                    category = "Work",
                    targetTimeMinutes = 180
                ),
                WeeklyPlanEntity(
                    weekNumber = weekNumber,
                    year = year,
                    title = "Inbox Zero & Desk Declutter",
                    description = "Clear digital tabs, clean workspace for a fresh weekend",
                    dayOfWeek = "FRI",
                    isCompleted = false,
                    priority = "LOW",
                    category = "Organize",
                    targetTimeMinutes = 20
                )
            )
            defaults.forEach { weeklyPlanDao.insertWeeklyPlan(it) }
        }
    }
}

data class FocusStatistics(
    val todaySeconds: Long = 0,
    val weekSeconds: Long = 0,
    val monthSeconds: Long = 0,
    val yearSeconds: Long = 0,
    val totalSeconds: Long = 0,
    val totalSessions: Int = 0,
    val completedSessions: Int = 0,
    val completionRate: Float = 0f,
    val avgSessionLengthSeconds: Int = 0,
    val longestSessionSeconds: Int = 0,
    val bestHourOfDay: Int = 10,
    val bestDayOfWeek: String = "Monday",
    val currentStreak: Int = 0,
    val hourDistribution: List<Int> = emptyList()
)
