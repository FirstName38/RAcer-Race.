package com.example.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.example.audio.FocusAudioSynthesizer
import com.example.data.entity.FocusSessionEntity
import com.example.data.model.FocusMode
import com.example.data.model.FocusSound
import com.example.data.repository.RacerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TimerUiState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isBreak: Boolean = false,
    val breakType: String = "NONE", // "NONE", "SHORT", "LONG"
    val mode: FocusMode = FocusMode.POMODORO,
    val currentSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val elapsedSeconds: Int = 0,
    val pauseCount: Int = 0,
    val pauseDurationSeconds: Int = 0,
    val currentSound: FocusSound = FocusSound.NONE,
    val volume: Float = 0.8f,
    val currentWallpaper: String = "dark_minimal",
    val activeTaskId: Long? = null,
    val activeTaskTitle: String? = null,
    val adhdReducedPressure: Boolean = false,
    val hideRemainingTime: Boolean = false,
    val milestoneReached: Int = 0, // 0..4 (25%, 50%, 75%, 100%)
    val sessionCount: Int = 1,
    // Cycle and Pomodoro pacing
    val cycleNumber: Int = 1,
    val sessionInCycle: Int = 1,
    val totalSessionsInCycle: Int = 4,
    val focusDurationMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val loopCycle: Boolean = true,
    val sessionStartTimeMillis: Long = 0L,
    val clockStartTimeStr: String = ""
)

class FocusTimerService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var tickerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var repository: RacerRepository
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreate() {
        super.onCreate()
        repository = RacerRepository(applicationContext)
        NotificationHelper.createNotificationChannels(this)
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RAcer:FocusTimerWakeLock")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val modeStr = intent.getStringExtra(EXTRA_MODE) ?: FocusMode.POMODORO.name
                val durationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 25)
                val soundStr = intent.getStringExtra(EXTRA_SOUND) ?: FocusSound.NONE.name
                val volume = intent.getFloatExtra(EXTRA_VOLUME, 0.8f)
                val wallpaper = intent.getStringExtra(EXTRA_WALLPAPER) ?: "dark_minimal"
                val taskId = if (intent.hasExtra(EXTRA_TASK_ID)) intent.getLongExtra(EXTRA_TASK_ID, -1L) else null
                val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE)
                val adhdReduced = intent.getBooleanExtra(EXTRA_ADHD_REDUCED, false)
                val hideTime = intent.getBooleanExtra(EXTRA_HIDE_TIME, false)
                val totalSessions = intent.getIntExtra(EXTRA_TOTAL_SESSIONS, 4)
                val shortBreak = intent.getIntExtra(EXTRA_SHORT_BREAK, 5)
                val longBreak = intent.getIntExtra(EXTRA_LONG_BREAK, 15)
                val loop = intent.getBooleanExtra(EXTRA_LOOP_CYCLE, true)

                startFocusSession(
                    mode = try { FocusMode.valueOf(modeStr) } catch (e: Exception) { FocusMode.POMODORO },
                    durationMinutes = durationMinutes,
                    sound = try { FocusSound.valueOf(soundStr) } catch (e: Exception) { FocusSound.NONE },
                    volume = volume,
                    wallpaper = wallpaper,
                    taskId = if (taskId != null && taskId > 0) taskId else null,
                    taskTitle = taskTitle,
                    adhdReduced = adhdReduced,
                    hideTime = hideTime,
                    totalSessions = totalSessions,
                    shortBreak = shortBreak,
                    longBreak = longBreak,
                    loop = loop
                )
            }
            ACTION_PAUSE -> pauseSession()
            ACTION_RESUME -> resumeSession()
            ACTION_NEXT -> nextSessionOrBreak()
            ACTION_EXTEND -> {
                val extraMinutes = intent.getIntExtra(EXTRA_EXTEND_MINUTES, 5)
                val extraSeconds = intent.getIntExtra(EXTRA_EXTEND_SECONDS, 0)
                extendSession(extraMinutes, extraSeconds)
            }
            ACTION_STOP -> stopSession(completed = false)
            ACTION_SET_SOUND -> {
                val soundStr = intent.getStringExtra(EXTRA_SOUND) ?: FocusSound.NONE.name
                val s = try { FocusSound.valueOf(soundStr) } catch (e: Exception) { FocusSound.NONE }
                setSound(s)
            }
            ACTION_SET_VOLUME -> {
                val v = intent.getFloatExtra(EXTRA_VOLUME, 0.8f)
                setVolume(v)
            }
        }
        return START_STICKY
    }

    private fun startFocusSession(
        mode: FocusMode,
        durationMinutes: Int,
        sound: FocusSound,
        volume: Float,
        wallpaper: String,
        taskId: Long?,
        taskTitle: String?,
        adhdReduced: Boolean,
        hideTime: Boolean,
        totalSessions: Int = 4,
        shortBreak: Int = 5,
        longBreak: Int = 15,
        loop: Boolean = true
    ) {
        tickerJob?.cancel()
        if (wakeLock?.isHeld != true) {
            wakeLock?.acquire(3 * 60 * 60 * 1000L) // 3 hours max wake lock
        }

        val initialSeconds = if (mode == FocusMode.STOPWATCH) 0 else durationMinutes * 60
        val now = System.currentTimeMillis()
        val nowFormatted = timeFormat.format(Date(now))

        _timerState.value = TimerUiState(
            isRunning = true,
            isPaused = false,
            isBreak = false,
            breakType = "NONE",
            mode = mode,
            currentSeconds = initialSeconds,
            totalSeconds = durationMinutes * 60,
            elapsedSeconds = 0,
            pauseCount = 0,
            pauseDurationSeconds = 0,
            currentSound = sound,
            volume = volume,
            currentWallpaper = wallpaper,
            activeTaskId = taskId,
            activeTaskTitle = taskTitle,
            adhdReducedPressure = adhdReduced,
            hideRemainingTime = hideTime,
            milestoneReached = 0,
            sessionCount = 1,
            cycleNumber = 1,
            sessionInCycle = 1,
            totalSessionsInCycle = totalSessions,
            focusDurationMinutes = durationMinutes,
            shortBreakMinutes = shortBreak,
            longBreakMinutes = longBreak,
            loopCycle = loop,
            sessionStartTimeMillis = now,
            clockStartTimeStr = nowFormatted
        )

        FocusAudioSynthesizer.playSound(sound, volume)

        startForeground(
            NotificationHelper.NOTIFICATION_ID_FOCUS_TIMER,
            NotificationHelper.buildTimerNotification(
                this,
                "Focus Session 1/${totalSessions}",
                formatTimeDisplay(initialSeconds, mode),
                true,
                0
            )
        )

        startTicker()
    }

    private fun startTicker() {
        tickerJob = scope.launch {
            while (isActive && _timerState.value.isRunning) {
                if (!_timerState.value.isPaused) {
                    delay(1000L)
                    val state = _timerState.value
                    if (state.mode == FocusMode.STOPWATCH) {
                        val newElapsed = state.elapsedSeconds + 1
                        _timerState.value = state.copy(
                            currentSeconds = newElapsed,
                            elapsedSeconds = newElapsed
                        )
                        updateNotification(newElapsed, state.mode, 0)
                    } else {
                        val newRemaining = state.currentSeconds - 1
                        val newElapsed = state.elapsedSeconds + 1
                        val progress = if (state.totalSeconds > 0) {
                            ((newElapsed.toFloat() / state.totalSeconds) * 100).toInt()
                        } else 0

                        val milestone = when {
                            progress >= 100 -> 4
                            progress >= 75 -> 3
                            progress >= 50 -> 2
                            progress >= 25 -> 1
                            else -> 0
                        }

                        if (newRemaining <= 0) {
                            handlePeriodComplete()
                        } else {
                            _timerState.value = state.copy(
                                currentSeconds = newRemaining,
                                elapsedSeconds = newElapsed,
                                milestoneReached = milestone
                            )
                            updateNotification(newRemaining, state.mode, progress)
                        }
                    }
                } else {
                    delay(1000L)
                    val state = _timerState.value
                    if (state.isRunning && state.isPaused) {
                        _timerState.value = state.copy(
                            pauseDurationSeconds = state.pauseDurationSeconds + 1
                        )
                    }
                }
            }
        }
    }

    private fun handlePeriodComplete() {
        val state = _timerState.value
        val now = System.currentTimeMillis()
        val nowStr = timeFormat.format(Date(now))

        if (!state.isBreak) {
            // Focus session finished
            saveSessionRecord(
                completed = true,
                sessionType = "FOCUS",
                actualSeconds = state.elapsedSeconds,
                plannedMinutes = state.totalSeconds / 60
            )

            // Transition to break if Pomodoro
            if (state.mode == FocusMode.POMODORO || state.mode == FocusMode.ADHD) {
                val isLong = (state.sessionInCycle >= state.totalSessionsInCycle)
                val breakMinutes = if (isLong) state.longBreakMinutes else state.shortBreakMinutes
                val breakTypeStr = if (isLong) "LONG" else "SHORT"

                FocusAudioSynthesizer.stopSound(fadeOut = true)

                // Trigger Sound Reminder
                if (isLong) {
                    FocusAudioSynthesizer.playLongBreakChime()
                } else {
                    FocusAudioSynthesizer.playSessionEndBreakStartChime()
                }

                _timerState.value = state.copy(
                    isBreak = true,
                    breakType = breakTypeStr,
                    currentSeconds = breakMinutes * 60,
                    totalSeconds = breakMinutes * 60,
                    elapsedSeconds = 0,
                    pauseCount = 0,
                    pauseDurationSeconds = 0,
                    sessionStartTimeMillis = now,
                    clockStartTimeStr = nowStr
                )

                updateNotification(breakMinutes * 60, state.mode, 0)
            } else {
                FocusAudioSynthesizer.playSessionEndBreakStartChime()
                finishAllWork(completed = true)
            }
        } else {
            // Break finished! Save break record and transition to next focus session
            saveSessionRecord(
                completed = true,
                sessionType = if (state.breakType == "LONG") "LONG_BREAK" else "SHORT_BREAK",
                actualSeconds = state.elapsedSeconds,
                plannedMinutes = state.totalSeconds / 60
            )

            FocusAudioSynthesizer.playBreakEndSessionStartChime()
            advanceToNextFocusSession(now, nowStr)
        }
    }

    private fun nextSessionOrBreak() {
        val state = _timerState.value
        if (!state.isRunning) return
        val now = System.currentTimeMillis()
        val nowStr = timeFormat.format(Date(now))

        if (state.isBreak) {
            // Skip break -> save actual break time spent
            if (state.elapsedSeconds > 2) {
                saveSessionRecord(
                    completed = true,
                    sessionType = if (state.breakType == "LONG") "LONG_BREAK" else "SHORT_BREAK",
                    actualSeconds = state.elapsedSeconds,
                    plannedMinutes = state.totalSeconds / 60
                )
            }
            FocusAudioSynthesizer.playBreakEndSessionStartChime()
            advanceToNextFocusSession(now, nowStr)
        } else {
            // Skip/complete focus early -> save actual focus time
            saveSessionRecord(
                completed = true,
                sessionType = "FOCUS",
                actualSeconds = state.elapsedSeconds,
                plannedMinutes = state.totalSeconds / 60
            )

            val isLong = (state.sessionInCycle >= state.totalSessionsInCycle)
            val breakMinutes = if (isLong) state.longBreakMinutes else state.shortBreakMinutes
            val breakTypeStr = if (isLong) "LONG" else "SHORT"

            FocusAudioSynthesizer.stopSound(fadeOut = true)

            if (isLong) {
                FocusAudioSynthesizer.playLongBreakChime()
            } else {
                FocusAudioSynthesizer.playSessionEndBreakStartChime()
            }

            _timerState.value = state.copy(
                isBreak = true,
                breakType = breakTypeStr,
                currentSeconds = breakMinutes * 60,
                totalSeconds = breakMinutes * 60,
                elapsedSeconds = 0,
                pauseCount = 0,
                pauseDurationSeconds = 0,
                sessionStartTimeMillis = now,
                clockStartTimeStr = nowStr
            )
            updateNotification(breakMinutes * 60, state.mode, 0)
        }
    }

    private fun advanceToNextFocusSession(now: Long, nowStr: String) {
        val state = _timerState.value
        var nextSessionInCycle = state.sessionInCycle + 1
        var nextCycleNumber = state.cycleNumber

        if (nextSessionInCycle > state.totalSessionsInCycle) {
            if (!state.loopCycle) {
                finishAllWork(completed = true)
                return
            }
            nextSessionInCycle = 1
            nextCycleNumber += 1
        }

        val focusSeconds = state.focusDurationMinutes * 60
        _timerState.value = state.copy(
            isBreak = false,
            breakType = "NONE",
            currentSeconds = focusSeconds,
            totalSeconds = focusSeconds,
            elapsedSeconds = 0,
            pauseCount = 0,
            pauseDurationSeconds = 0,
            sessionInCycle = nextSessionInCycle,
            cycleNumber = nextCycleNumber,
            sessionCount = state.sessionCount + 1,
            sessionStartTimeMillis = now,
            clockStartTimeStr = nowStr
        )

        FocusAudioSynthesizer.playSound(state.currentSound, state.volume)
        updateNotification(focusSeconds, state.mode, 0)
    }

    private fun saveSessionRecord(
        completed: Boolean,
        sessionType: String,
        actualSeconds: Int,
        plannedMinutes: Int
    ) {
        val state = _timerState.value
        val now = System.currentTimeMillis()
        val nowStr = timeFormat.format(Date(now))
        val startTime = if (state.sessionStartTimeMillis > 0) state.sessionStartTimeMillis else (now - actualSeconds * 1000L)
        val startTimeStr = if (state.clockStartTimeStr.isNotBlank()) state.clockStartTimeStr else timeFormat.format(Date(startTime))

        scope.launch {
            repository.saveFocusSession(
                FocusSessionEntity(
                    startTime = startTime,
                    endTime = now,
                    plannedDurationMinutes = plannedMinutes,
                    actualDurationSeconds = actualSeconds,
                    mode = state.mode,
                    isCompleted = completed,
                    pauseCount = state.pauseCount,
                    breakMinutes = if (sessionType.contains("BREAK")) actualSeconds / 60 else 0,
                    taskId = state.activeTaskId,
                    taskTitle = state.activeTaskTitle,
                    soundUsed = state.currentSound.name,
                    wallpaperUsed = state.currentWallpaper,
                    milestoneReached = if (completed) 4 else state.milestoneReached,
                    cycleNumber = state.cycleNumber,
                    sessionInCycle = state.sessionInCycle,
                    totalSessionsInCycle = state.totalSessionsInCycle,
                    sessionType = sessionType,
                    clockStartTimeStr = startTimeStr,
                    clockEndTimeStr = nowStr,
                    totalPauseDurationSeconds = state.pauseDurationSeconds
                )
            )
        }
    }

    private fun finishAllWork(completed: Boolean) {
        val state = _timerState.value
        FocusAudioSynthesizer.stopSound(fadeOut = true)
        _timerState.value = state.copy(
            isRunning = false,
            isPaused = false,
            milestoneReached = 4
        )

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(
            NotificationHelper.NOTIFICATION_ID_FOCUS_TIMER,
            NotificationHelper.buildTimerNotification(
                this,
                "Focus Complete!",
                "Cycle completed with mindful consistency.",
                false,
                100
            )
        )

        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    private fun pauseSession() {
        val state = _timerState.value
        if (!state.isRunning || state.isPaused) return
        _timerState.value = state.copy(
            isPaused = true,
            pauseCount = state.pauseCount + 1
        )
        FocusAudioSynthesizer.stopSound(fadeOut = false)
        updateNotification(state.currentSeconds, state.mode, 0)
    }

    private fun resumeSession() {
        val state = _timerState.value
        if (!state.isRunning || !state.isPaused) return
        _timerState.value = state.copy(isPaused = false)
        if (!state.isBreak) {
            FocusAudioSynthesizer.playSound(state.currentSound, state.volume)
        }
        updateNotification(state.currentSeconds, state.mode, 0)
    }

    private fun extendSession(extraMinutes: Int, extraSeconds: Int = 0) {
        val state = _timerState.value
        val addSecs = (extraMinutes * 60) + extraSeconds
        if (addSecs <= 0) return
        _timerState.value = state.copy(
            currentSeconds = state.currentSeconds + addSecs,
            totalSeconds = state.totalSeconds + addSecs
        )
    }

    private fun stopSession(completed: Boolean) {
        val state = _timerState.value
        if (state.elapsedSeconds > 5) {
            saveSessionRecord(
                completed = completed,
                sessionType = if (state.isBreak) "BREAK_CANCELLED" else "FOCUS_INCOMPLETE",
                actualSeconds = state.elapsedSeconds,
                plannedMinutes = state.totalSeconds / 60
            )
        }

        tickerJob?.cancel()
        tickerJob = null
        FocusAudioSynthesizer.stopSound(fadeOut = true)
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }

        _timerState.value = TimerUiState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun setSound(sound: FocusSound) {
        val state = _timerState.value
        _timerState.value = state.copy(currentSound = sound)
        if (state.isRunning && !state.isPaused && !state.isBreak) {
            FocusAudioSynthesizer.playSound(sound, state.volume)
        }
    }

    private fun setVolume(volume: Float) {
        val state = _timerState.value
        _timerState.value = state.copy(volume = volume)
        FocusAudioSynthesizer.setVolume(volume)
    }

    private fun updateNotification(seconds: Int, mode: FocusMode, progress: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val title = if (_timerState.value.isBreak) {
            "Break: ${_timerState.value.breakType} (${_timerState.value.sessionInCycle}/${_timerState.value.totalSessionsInCycle})"
        } else {
            "Focus [Session ${_timerState.value.sessionInCycle}/${_timerState.value.totalSessionsInCycle} - Cycle ${_timerState.value.cycleNumber}]"
        }
        val notif = NotificationHelper.buildTimerNotification(
            this,
            title,
            formatTimeDisplay(seconds, mode),
            !_timerState.value.isPaused,
            progress
        )
        manager.notify(NotificationHelper.NOTIFICATION_ID_FOCUS_TIMER, notif)
    }

    private fun formatTimeDisplay(totalSecs: Int, mode: FocusMode): String {
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    override fun onDestroy() {
        super.onDestroy()
        tickerJob?.cancel()
        FocusAudioSynthesizer.stopSound(fadeOut = false)
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    companion object {
        const val ACTION_START = "com.example.action.START"
        const val ACTION_PAUSE = "com.example.action.PAUSE"
        const val ACTION_RESUME = "com.example.action.RESUME"
        const val ACTION_NEXT = "com.example.action.NEXT"
        const val ACTION_EXTEND = "com.example.action.EXTEND"
        const val ACTION_STOP = "com.example.action.STOP"
        const val ACTION_SET_SOUND = "com.example.action.SET_SOUND"
        const val ACTION_SET_VOLUME = "com.example.action.SET_VOLUME"

        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"
        const val EXTRA_SOUND = "extra_sound"
        const val EXTRA_VOLUME = "extra_volume"
        const val EXTRA_WALLPAPER = "extra_wallpaper"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_ADHD_REDUCED = "extra_adhd_reduced"
        const val EXTRA_HIDE_TIME = "extra_hide_time"
        const val EXTRA_EXTEND_MINUTES = "extra_extend_minutes"
        const val EXTRA_EXTEND_SECONDS = "extra_extend_seconds"
        const val EXTRA_TOTAL_SESSIONS = "extra_total_sessions"
        const val EXTRA_SHORT_BREAK = "extra_short_break"
        const val EXTRA_LONG_BREAK = "extra_long_break"
        const val EXTRA_LOOP_CYCLE = "extra_loop_cycle"

        private val _timerState = MutableStateFlow(TimerUiState())
        val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()
    }
}
