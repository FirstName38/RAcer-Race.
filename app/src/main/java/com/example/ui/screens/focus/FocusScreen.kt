package com.example.ui.screens.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FocusMode
import com.example.data.model.FocusSound
import com.example.data.model.FocusWallpaper
import com.example.ui.components.FocusTimerDial
import com.example.ui.components.GlassCard
import com.example.ui.components.WallpaperBackground
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseUrgent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.RacerViewModel

@Composable
fun FocusScreen(
    viewModel: RacerViewModel,
    onNavigateToSessions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val timerState by viewModel.timerState.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()

    var selectedMode by remember { mutableStateOf(FocusMode.POMODORO) }
    var selectedTaskId by remember { mutableStateOf<Long?>(null) }
    var showTaskMenu by remember { mutableStateOf(false) }
    var showCustomExtendDialog by remember { mutableStateOf(false) }
    var showCycleConfigDialog by remember { mutableStateOf(false) }
    var showTakeBreakDialog by remember { mutableStateOf(false) }

    val currentSound by viewModel.selectedSound.collectAsState()
    val soundVolume by viewModel.soundVolume.collectAsState()
    val currentWallpaper by viewModel.selectedWallpaper.collectAsState()
    val hideTime by viewModel.hideRemainingTime.collectAsState()

    val focusMinutes by viewModel.customFocusMinutes.collectAsState()
    val shortBreakMinutes by viewModel.customShortBreakMinutes.collectAsState()
    val longBreakMinutes by viewModel.customLongBreakMinutes.collectAsState()
    val sessionsPerCycle by viewModel.customSessionsPerCycle.collectAsState()
    val loopCycle by viewModel.customLoopCycle.collectAsState()

    val chosenTask = activeTasks.find { it.id == selectedTaskId }

    WallpaperBackground(
        wallpaperId = if (timerState.isRunning) timerState.currentWallpaper else currentWallpaper
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Quick Top Bar for Session History & Config
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Focus Sanctuary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                OutlinedButton(
                    onClick = onNavigateToSessions,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = CharcoalCardElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("History & Logs", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            // Mode Selector Tabs (only active if timer is not running)
            if (!timerState.isRunning) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CharcoalCardElevated)
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FocusMode.values().forEach { mode ->
                        val isSelected = selectedMode == mode
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) VioletPrimary else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyanAccent.copy(alpha = 0.5f) else Color.Transparent),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedMode = mode
                                    if (mode == FocusMode.ADHD) viewModel.customFocusMinutes.value = 15
                                    if (mode == FocusMode.POMODORO) viewModel.customFocusMinutes.value = 25
                                    if (mode == FocusMode.CUSTOM) viewModel.customFocusMinutes.value = 45
                                }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }
            } else {
                // RUNNING CYCLE STATUS BANNER
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (timerState.isBreak) EmeraldSuccess.copy(alpha = 0.5f) else CyanAccent.copy(alpha = 0.4f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (timerState.isBreak) "Break: ${timerState.currentSeconds / 60}m ${timerState.currentSeconds % 60}s remaining"
                                           else if (timerState.mode == FocusMode.STOPWATCH) "Stopwatch Focus Active"
                                           else "Focus Session ${timerState.sessionInCycle}/${timerState.totalSessionsInCycle}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (timerState.isBreak) EmeraldSuccess else CyanAccent
                                )
                                Text(
                                    text = if (timerState.mode == FocusMode.STOPWATCH) "Elapsed: ${timerState.elapsedSeconds / 3600}h ${(timerState.elapsedSeconds % 3600) / 60}m ${timerState.elapsedSeconds % 60}s • Started ${timerState.clockStartTimeStr}"
                                           else "Cycle ${timerState.cycleNumber} • Started at ${timerState.clockStartTimeStr}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            if (timerState.pauseCount > 0) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${timerState.pauseCount} pauses",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AmberWarning,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${timerState.pauseDurationSeconds}s paused",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }

                        if (timerState.isPaused) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AmberWarning.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("⏸ Paused live: ${timerState.pauseDurationSeconds}s spent on pause", style = MaterialTheme.typography.labelSmall, color = AmberWarning, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Central Interactive Focus Dial
            val dialCurrentSeconds = if (timerState.isRunning) {
                if (timerState.mode == FocusMode.STOPWATCH) timerState.elapsedSeconds else timerState.currentSeconds
            } else {
                if (selectedMode == FocusMode.STOPWATCH) 0 else (focusMinutes * 60)
            }
            val dialTotalSeconds = if (timerState.isRunning) {
                if (timerState.mode == FocusMode.STOPWATCH) 86400 else timerState.totalSeconds
            } else {
                if (selectedMode == FocusMode.STOPWATCH) 86400 else (focusMinutes * 60)
            }

            FocusTimerDial(
                currentSeconds = dialCurrentSeconds,
                totalSeconds = dialTotalSeconds,
                mode = if (timerState.isRunning) timerState.mode else selectedMode,
                isRunning = timerState.isRunning,
                hideRemainingTime = hideTime,
                adhdMilestone = timerState.milestoneReached,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            // Duration Slider for Custom Mode or Pomodoro Configuration / Stopwatch Explainer
            if (!timerState.isRunning) {
                if (selectedMode == FocusMode.STOPWATCH) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Open-Ended Stopwatch Focus", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Counts up continuously from 00:00:00 without limit (up to 24h+). Take custom breaks and track pause duration anytime.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                } else {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Focus Cycle Duration", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                    Text("$focusMinutes mins focus • $shortBreakMinutes mins break", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CyanAccent)
                                }
                                IconButton(onClick = { showCycleConfigDialog = true }) {
                                    Icon(Icons.Default.Tune, contentDescription = "Configure Cycle", tint = CyanAccent)
                                }
                            }
                            Slider(
                                value = focusMinutes.toFloat(),
                                onValueChange = { viewModel.customFocusMinutes.value = it.toInt() },
                                valueRange = 0f..480f,
                                steps = 95,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyanAccent,
                                    activeTrackColor = VioletPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Study Room Linkage Selector
            val customRooms by viewModel.customStudyRooms.collectAsState()
            val linkedRoomId by viewModel.linkedStudyRoomId.collectAsState()
            val habits by viewModel.activeHabits.collectAsState()
            val challenges = remember(habits) { habits.filter { it.isChallenge } }
            val weeklyPlans by viewModel.allWeeklyPlans.collectAsState()
            val linkedTargetType by viewModel.linkedFocusTargetType.collectAsState()
            val linkedTargetId by viewModel.linkedFocusTargetId.collectAsState()
            val linkedTargetTitle by viewModel.linkedFocusTargetTitle.collectAsState()
            var showRoomMenu by remember { mutableStateOf(false) }
            var showTargetDialog by remember { mutableStateOf(false) }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { if (!timerState.isRunning) showRoomMenu = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Linked Study Room (Optional)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = when (linkedRoomId) {
                                null, "", "none" -> "None (Solo Focus Sanctuary)"
                                "room_pomodoro" -> "Silent Deep Work (Pomodoro)"
                                "room_lofi" -> "Lo-Fi Study Cafe (Chill)"
                                "room_sprint" -> "Coding & Exam Sprint"
                                else -> customRooms.find { it.id == linkedRoomId }?.name ?: "Custom Study Room"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = CyanAccent
                        )
                    }
                    if (!timerState.isRunning) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CharcoalCardElevated
                        ) {
                            Text(
                                text = "Change",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = showRoomMenu,
                    onDismissRequest = { showRoomMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None (Solo Focus Sanctuary)") },
                        onClick = {
                            viewModel.setLinkedStudyRoom("none")
                            showRoomMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Silent Deep Work (Pomodoro)") },
                        onClick = {
                            viewModel.setLinkedStudyRoom("room_pomodoro")
                            showRoomMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Lo-Fi Study Cafe (Chill)") },
                        onClick = {
                            viewModel.setLinkedStudyRoom("room_lofi")
                            showRoomMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Coding & Exam Sprint") },
                        onClick = {
                            viewModel.setLinkedStudyRoom("room_sprint")
                            showRoomMenu = false
                        }
                    )
                    customRooms.forEach { room ->
                        DropdownMenuItem(
                            text = { Text("${room.name} (${room.purpose})") },
                            onClick = {
                                viewModel.setLinkedStudyRoom(room.id)
                                showRoomMenu = false
                            }
                        )
                    }
                }
            }

            // Linked Target Selector (Task, Habit, Challenge, Weekly Plan)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { if (!timerState.isRunning) showTargetDialog = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Linked Focus Target (Task / Habit / Challenge / Plan)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        val displayTarget = if (timerState.isRunning) {
                            timerState.activeTaskTitle ?: if (linkedTargetType != "NONE") "[$linkedTargetType] $linkedTargetTitle" else "Free Flow (No Target)"
                        } else {
                            if (linkedTargetType != "NONE" && linkedTargetTitle.isNotBlank()) {
                                "[$linkedTargetType] $linkedTargetTitle"
                            } else {
                                chosenTask?.let { "[TASK] ${it.title}" } ?: "Select task, habit, challenge or weekly plan..."
                            }
                        }
                        Text(
                            text = displayTarget,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    if (!timerState.isRunning) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CharcoalCardElevated
                        ) {
                            Text(
                                text = "Select",
                                style = MaterialTheme.typography.labelSmall,
                                color = VioletPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (showTargetDialog) {
                var selectedTargetTab by remember { mutableIntStateOf(0) }
                AlertDialog(
                    onDismissRequest = { showTargetDialog = false },
                    title = { Text("Link Focus Target", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth().height(320.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Tasks", "Habits", "Challenges", "Weekly Plan").forEachIndexed { idx, label ->
                                    val isTabSelected = selectedTargetTab == idx
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isTabSelected) VioletPrimary else CharcoalCardElevated,
                                        modifier = Modifier.clickable { selectedTargetTab = idx }
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isTabSelected) Color.White else TextSecondary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                when (selectedTargetTab) {
                                    0 -> { // Tasks
                                        val pendingTasks = activeTasks.filter { !it.isCompleted }
                                        if (pendingTasks.isEmpty()) {
                                            Text("No pending tasks. Create one in Tasks navigation.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                                        } else {
                                            pendingTasks.forEach { task ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = CharcoalCardElevated,
                                                    modifier = Modifier.fillMaxWidth().clickable {
                                                        selectedTaskId = task.id
                                                        viewModel.setLinkedFocusTarget("TASK", task.id, task.title)
                                                        showTargetDialog = false
                                                    }
                                                ) {
                                                    Text("${task.title} (${task.priority.displayName})", modifier = Modifier.padding(10.dp), color = TextPrimary)
                                                }
                                            }
                                        }
                                    }
                                    1 -> { // Gentle Habits
                                        val regularHabits = habits.filter { !it.isChallenge }
                                        if (regularHabits.isEmpty()) {
                                            Text("No gentle habits configured.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                                        } else {
                                            regularHabits.forEach { habit ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = CharcoalCardElevated,
                                                    modifier = Modifier.fillMaxWidth().clickable {
                                                        selectedTaskId = null
                                                        viewModel.setLinkedFocusTarget("HABIT", habit.id, habit.name)
                                                        showTargetDialog = false
                                                    }
                                                ) {
                                                    Text(habit.name, modifier = Modifier.padding(10.dp), color = TextPrimary)
                                                }
                                            }
                                        }
                                    }
                                    2 -> { // Strict Challenges
                                        if (challenges.isEmpty()) {
                                            Text("No active strict challenges.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                                        } else {
                                            challenges.forEach { ch ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = CharcoalCardElevated,
                                                    modifier = Modifier.fillMaxWidth().clickable {
                                                        selectedTaskId = null
                                                        viewModel.setLinkedFocusTarget("CHALLENGE", ch.id, ch.name)
                                                        showTargetDialog = false
                                                    }
                                                ) {
                                                    Text("${ch.name} (${ch.challengeDays}d streak)", modifier = Modifier.padding(10.dp), color = TextPrimary)
                                                }
                                            }
                                        }
                                    }
                                    3 -> { // Weekly Plans
                                        val pendingPlans = weeklyPlans.filter { !it.isCompleted }
                                        if (pendingPlans.isEmpty()) {
                                            Text("No weekly objectives for this week.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                                        } else {
                                            pendingPlans.forEach { plan ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = CharcoalCardElevated,
                                                    modifier = Modifier.fillMaxWidth().clickable {
                                                        selectedTaskId = null
                                                        viewModel.setLinkedFocusTarget("WEEKLY_PLAN", plan.id, plan.title)
                                                        showTargetDialog = false
                                                    }
                                                ) {
                                                    Text("W${plan.weekNumber} · ${plan.title}", modifier = Modifier.padding(10.dp), color = TextPrimary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                selectedTaskId = null
                                viewModel.setLinkedFocusTarget("NONE", null, "")
                                showTargetDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalCardElevated)
                        ) {
                            Text("Clear Target", color = TextSecondary)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showTargetDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }

            // Controls: Main Action Buttons
            if (!timerState.isRunning) {
                Button(
                    onClick = {
                        val finalTaskTitle = if (linkedTargetType != "NONE" && linkedTargetTitle.isNotBlank()) {
                            "[$linkedTargetType] $linkedTargetTitle"
                        } else chosenTask?.title

                        viewModel.startFocus(
                            mode = selectedMode,
                            durationMinutes = focusMinutes,
                            taskId = selectedTaskId,
                            taskTitle = finalTaskTitle,
                            totalSessions = sessionsPerCycle,
                            shortBreak = shortBreakMinutes,
                            longBreak = longBreakMinutes,
                            loop = loopCycle
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Focus",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedMode == FocusMode.ADHD) "Start Gentle Flow" else "Start Focus Cycle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                // If break is active, show Break notification card
                if (timerState.isBreak) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldSuccess.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("☕ Break Active", style = MaterialTheme.typography.labelMedium, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                Text("${timerState.currentSeconds / 60}m ${timerState.currentSeconds % 60}s remaining", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                            Button(
                                onClick = { viewModel.resumeFocus() },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Resume Focus", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Running Controls: Pause/Resume, Take Break, +5m, +Custom, Next, End
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pause / Resume
                    Button(
                        onClick = {
                            if (timerState.isPaused) viewModel.resumeFocus() else viewModel.pauseFocus()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (timerState.isPaused) EmeraldSuccess else CharcoalCardElevated
                        )
                    ) {
                        Icon(
                            imageVector = if (timerState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (timerState.isPaused) "Resume" else "Pause"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (timerState.isPaused) "Resume" else "Pause", fontSize = 13.sp)
                    }

                    // In Stopwatch Mode: Take Break button
                    if (timerState.mode == FocusMode.STOPWATCH) {
                        Button(
                            onClick = { showTakeBreakDialog = true },
                            modifier = Modifier.height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess.copy(alpha = 0.25f)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Coffee, contentDescription = "Take Break", tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Break", fontSize = 12.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // +5 Min
                        Button(
                            onClick = { viewModel.extendFocus(minutes = 5) },
                            modifier = Modifier.height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalCardElevated),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "+5 Min", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("+5m", fontSize = 12.sp)
                        }

                        // +Custom
                        Button(
                            onClick = { showCustomExtendDialog = true },
                            modifier = Modifier.height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalCardElevated),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.MoreTime, contentDescription = "+Custom", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("+Custom", fontSize = 12.sp)
                        }

                        // Next / Skip
                        Button(
                            onClick = { viewModel.nextFocusPeriod() },
                            modifier = Modifier.height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent.copy(alpha = 0.25f)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next", tint = CyanAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Next", fontSize = 12.sp, color = CyanAccent)
                        }
                    }

                    // Stop
                    var showEndSessionRewardDialog by remember { mutableStateOf(false) }

                    Button(
                        onClick = {
                            if (linkedTargetType != "NONE" && linkedTargetId != null) {
                                showEndSessionRewardDialog = true
                            } else {
                                viewModel.stopFocus()
                            }
                        },
                        modifier = Modifier.height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoseUrgent.copy(alpha = 0.2f)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = "End", tint = RoseUrgent, modifier = Modifier.size(18.dp))
                    }

                    if (showEndSessionRewardDialog) {
                        AlertDialog(
                            onDismissRequest = { showEndSessionRewardDialog = false },
                            title = { Text("Complete Target & Claim Coins? 🪙", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                            text = {
                                Text("Did you achieve your linked target: \"$linkedTargetTitle\"? Marking it complete awards you bonus coins!")
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.completeLinkedFocusTarget()
                                        viewModel.stopFocus()
                                        showEndSessionRewardDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                                ) {
                                    Text("Yes, Complete (+Coins)", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.stopFocus()
                                        showEndSessionRewardDialog = false
                                    }
                                ) {
                                    Text("End Session Only")
                                }
                            }
                        )
                    }
                }
            }

            // ADHD Accommodations Accordion
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ADHD Calm Settings",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hide Remaining Countdown", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Text("Removes ticking time urgency", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        Switch(
                            checked = hideTime,
                            onCheckedChange = { viewModel.hideRemainingTime.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyanAccent,
                                checkedTrackColor = VioletPrimary
                            )
                        )
                    }
                }
            }

            // Ambient Sound & Synthesizer Rack
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = CyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ambient Noise Generator",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }

                    // Sound Selector Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FocusSound.values().forEach { sound ->
                            val isSelected = currentSound == sound
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { viewModel.setSound(sound) }
                                    .background(
                                        if (isSelected) CyanAccent.copy(alpha = 0.25f)
                                        else CharcoalCardElevated
                                    )
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Transparent
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = sound.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) CyanAccent else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isSelected && sound != FocusSound.NONE) {
                                        Icon(
                                            imageVector = Icons.Default.GraphicEq,
                                            contentDescription = null,
                                            tint = CyanAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Volume Slider
                    if (currentSound != FocusSound.NONE) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Slider(
                                value = soundVolume,
                                onValueChange = { viewModel.setVolume(it) },
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = CyanAccent,
                                    activeTrackColor = CyanAccent
                                )
                            )
                        }
                    }
                }
            }

            // Wallpaper Theme Picker
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Wallpaper, contentDescription = null, tint = VioletPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Atmospheric Wallpaper",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FocusWallpaper.values().forEach { wp ->
                            val isSelected = currentWallpaper == wp.id
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { viewModel.selectedWallpaper.value = wp.id }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) VioletPrimary else CharcoalCardElevated)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = wp.displayName.take(8),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) CyanAccent else TextMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Custom Extend Dialog
        if (showCustomExtendDialog) {
            var customMinStr by remember { mutableStateOf("10") }
            var customSecStr by remember { mutableStateOf("0") }

            AlertDialog(
                onDismissRequest = { showCustomExtendDialog = false },
                containerColor = CharcoalCardElevated,
                title = { Text("Add Custom Time", fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Add custom minutes or seconds to current session:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = customMinStr,
                                onValueChange = { customMinStr = it.filter { c -> c.isDigit() } },
                                label = { Text("Minutes") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanAccent,
                                    unfocusedBorderColor = CharcoalBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            OutlinedTextField(
                                value = customSecStr,
                                onValueChange = { customSecStr = it.filter { c -> c.isDigit() } },
                                label = { Text("Seconds") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanAccent,
                                    unfocusedBorderColor = CharcoalBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val m = customMinStr.toIntOrNull() ?: 0
                            val s = customSecStr.toIntOrNull() ?: 0
                            if (m > 0 || s > 0) {
                                viewModel.extendFocus(minutes = m, seconds = s)
                            }
                            showCustomExtendDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Add Time", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showCustomExtendDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = TextPrimary)
                    }
                }
            )
        }

        // Pomodoro Cycle Settings Dialog
        if (showCycleConfigDialog) {
            var focusM by remember { mutableIntStateOf(focusMinutes) }
            var shortM by remember { mutableIntStateOf(shortBreakMinutes) }
            var longM by remember { mutableIntStateOf(longBreakMinutes) }
            var cycleSessions by remember { mutableIntStateOf(sessionsPerCycle) }
            var loop by remember { mutableStateOf(loopCycle) }

            AlertDialog(
                onDismissRequest = { showCycleConfigDialog = false },
                containerColor = CharcoalCardElevated,
                title = { Text("Pomodoro Cycle Settings", fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Focus Session: $focusM mins ${if (focusM == 0) "(Continuous)" else ""}", style = MaterialTheme.typography.labelMedium, color = CyanAccent)
                        Slider(
                            value = focusM.toFloat(),
                            onValueChange = { focusM = it.toInt() },
                            valueRange = 0f..480f,
                            steps = 95,
                            colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                        )

                        Text("Short Break: $shortM mins", style = MaterialTheme.typography.labelMedium, color = EmeraldSuccess)
                        Slider(
                            value = shortM.toFloat(),
                            onValueChange = { shortM = it.toInt() },
                            valueRange = 0f..60f,
                            steps = 59,
                            colors = SliderDefaults.colors(thumbColor = EmeraldSuccess, activeTrackColor = EmeraldSuccess)
                        )

                        Text("Long Break: $longM mins", style = MaterialTheme.typography.labelMedium, color = VioletPrimary)
                        Slider(
                            value = longM.toFloat(),
                            onValueChange = { longM = it.toInt() },
                            valueRange = 0f..90f,
                            steps = 89,
                            colors = SliderDefaults.colors(thumbColor = VioletPrimary, activeTrackColor = VioletPrimary)
                        )

                        Text("Sessions before Long Break: $cycleSessions", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                        Slider(
                            value = cycleSessions.toFloat(),
                            onValueChange = { cycleSessions = it.toInt() },
                            valueRange = 2f..8f,
                            steps = 5,
                            colors = SliderDefaults.colors(thumbColor = AmberWarning, activeTrackColor = AmberWarning)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Auto Loop Cycle", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Switch(
                                checked = loop,
                                onCheckedChange = { loop = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = CyanAccent.copy(alpha = 0.5f))
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.customFocusMinutes.value = focusM
                            viewModel.customShortBreakMinutes.value = shortM
                            viewModel.customLongBreakMinutes.value = longM
                            viewModel.customSessionsPerCycle.value = cycleSessions
                            viewModel.customLoopCycle.value = loop
                            showCycleConfigDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Settings", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Take Break Dialog for Stopwatch Mode
        if (showTakeBreakDialog) {
            var selectedBreakMins by remember { mutableIntStateOf(5) }
            var customBreakStr by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showTakeBreakDialog = false },
                containerColor = CharcoalCardElevated,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Coffee, contentDescription = null, tint = EmeraldSuccess)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take a Break", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Pause your stopwatch focus session for a refreshing break. Choose duration:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(5, 10, 15, 20, 30).forEach { mins ->
                                val isSel = selectedBreakMins == mins && customBreakStr.isBlank()
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) EmeraldSuccess.copy(alpha = 0.25f) else CharcoalCard,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) EmeraldSuccess else CharcoalBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedBreakMins = mins
                                            customBreakStr = ""
                                        }
                                ) {
                                    Text(
                                        text = "${mins}m",
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSel) EmeraldSuccess else TextMuted,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = customBreakStr,
                            onValueChange = {
                                customBreakStr = it.filter { c -> c.isDigit() }
                                val v = customBreakStr.toIntOrNull()
                                if (v != null && v > 0) selectedBreakMins = v
                            },
                            label = { Text("Custom Break Minutes") },
                            placeholder = { Text("e.g. 8") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldSuccess,
                                unfocusedBorderColor = CharcoalBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val mins = customBreakStr.toIntOrNull() ?: selectedBreakMins
                            viewModel.takeBreak(mins.coerceAtLeast(1))
                            showTakeBreakDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Start Break", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showTakeBreakDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = TextPrimary)
                    }
                }
            )
        }
    }
}
