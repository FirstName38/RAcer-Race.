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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
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
    modifier: Modifier = Modifier
) {
    val timerState by viewModel.timerState.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()

    var selectedMode by remember { mutableStateOf(FocusMode.POMODORO) }
    var selectedTaskId by remember { mutableStateOf<Long?>(null) }
    var showTaskMenu by remember { mutableStateOf(false) }
    var showCustomExtendDialog by remember { mutableStateOf(false) }
    var showCycleConfigDialog by remember { mutableStateOf(false) }

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
            Spacer(modifier = Modifier.height(10.dp))

            // Mode Selector Tabs (only active if timer is not running)
            if (!timerState.isRunning) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CharcoalCardElevated)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FocusMode.values().forEach { mode ->
                        val isSelected = selectedMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) VioletPrimary else Color.Transparent)
                                .clickable {
                                    selectedMode = mode
                                    if (mode == FocusMode.ADHD) viewModel.customFocusMinutes.value = 15
                                    if (mode == FocusMode.POMODORO) viewModel.customFocusMinutes.value = 25
                                    if (mode == FocusMode.CUSTOM) viewModel.customFocusMinutes.value = 45
                                }
                                .padding(vertical = 8.dp),
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
            } else {
                // RUNNING CYCLE STATUS BANNER
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (timerState.isBreak) CyanAccent.copy(alpha = 0.4f) else VioletPrimary.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (timerState.isBreak) "Break: ${timerState.breakType}" else "Focus Session ${timerState.sessionInCycle}/${timerState.totalSessionsInCycle}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (timerState.isBreak) CyanAccent else TextPrimary
                            )
                            Text(
                                text = "Cycle ${timerState.cycleNumber} • Started at ${timerState.clockStartTimeStr}",
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
                }
            }

            // Central Interactive Focus Dial
            FocusTimerDial(
                currentSeconds = timerState.currentSeconds,
                totalSeconds = if (timerState.isRunning) timerState.totalSeconds else (focusMinutes * 60),
                mode = if (timerState.isRunning) timerState.mode else selectedMode,
                isRunning = timerState.isRunning,
                hideRemainingTime = hideTime,
                adhdMilestone = timerState.milestoneReached,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            // Duration Slider for Custom Mode or Pomodoro Configuration
            if (!timerState.isRunning) {
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
                            valueRange = 5f..120f,
                            steps = 22,
                            colors = SliderDefaults.colors(
                                thumbColor = CyanAccent,
                                activeTrackColor = VioletPrimary
                            )
                        )
                    }
                }
            }

            // Task Attachment Selector
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { if (!timerState.isRunning) showTaskMenu = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Linked Task",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = if (timerState.isRunning) {
                                timerState.activeTaskTitle ?: "Free Flow (No Task)"
                            } else {
                                chosenTask?.title ?: "Select a task to focus on..."
                            },
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
                                text = "Choose",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = showTaskMenu,
                    onDismissRequest = { showTaskMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None (Free Flow)") },
                        onClick = {
                            selectedTaskId = null
                            showTaskMenu = false
                        }
                    )
                    activeTasks.filter { !it.isCompleted }.forEach { task ->
                        DropdownMenuItem(
                            text = { Text("${task.title} (${task.priority.displayName})") },
                            onClick = {
                                selectedTaskId = task.id
                                showTaskMenu = false
                            }
                        )
                    }
                }
            }

            // Controls: Main Action Buttons
            if (!timerState.isRunning) {
                Button(
                    onClick = {
                        viewModel.startFocus(
                            mode = selectedMode,
                            durationMinutes = focusMinutes,
                            taskId = selectedTaskId,
                            taskTitle = chosenTask?.title,
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
                // Running Controls: Pause/Resume, +5m, +Custom, Next, End
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

                    // Stop
                    Button(
                        onClick = { viewModel.stopFocus() },
                        modifier = Modifier.height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoseUrgent.copy(alpha = 0.2f)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = "End", tint = RoseUrgent, modifier = Modifier.size(18.dp))
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
                                Text(
                                    text = sound.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) CyanAccent else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
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
                        Text("Focus Session: $focusM mins", style = MaterialTheme.typography.labelMedium, color = CyanAccent)
                        Slider(
                            value = focusM.toFloat(),
                            onValueChange = { focusM = it.toInt() },
                            valueRange = 5f..90f,
                            steps = 16,
                            colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                        )

                        Text("Short Break: $shortM mins", style = MaterialTheme.typography.labelMedium, color = EmeraldSuccess)
                        Slider(
                            value = shortM.toFloat(),
                            onValueChange = { shortM = it.toInt() },
                            valueRange = 1f..30f,
                            steps = 28,
                            colors = SliderDefaults.colors(thumbColor = EmeraldSuccess, activeTrackColor = EmeraldSuccess)
                        )

                        Text("Long Break: $longM mins", style = MaterialTheme.typography.labelMedium, color = VioletPrimary)
                        Slider(
                            value = longM.toFloat(),
                            onValueChange = { longM = it.toInt() },
                            valueRange = 5f..60f,
                            steps = 10,
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
    }
}
