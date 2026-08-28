package com.example.ui.screens.focusroom

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.TaskEntity
import com.example.data.model.FocusMode
import com.example.data.model.FocusSound
import com.example.data.model.FocusWallpaper
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingOrb
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.RoseUrgent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.RacerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusRoomScreen(
    viewModel: RacerViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val timerState by viewModel.timerState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CharcoalDark)
    ) {
        // Tab Navigation Header
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CharcoalCard,
            contentColor = TextPrimary,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CyanAccent
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Focus Room", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.MeetingRoom, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Join Study Room", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Room History", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        when (selectedTab) {
            0 -> MyPersonalFocusRoom(viewModel = viewModel, roomName = "Private Sanctuary")
            1 -> JoinStudyRoomSimulation(viewModel = viewModel)
            2 -> FocusRoomHistoryView(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MyPersonalFocusRoom(
    viewModel: RacerViewModel,
    roomName: String = "Private Sanctuary",
    studyPartners: List<String> = emptyList()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val timerState by viewModel.timerState.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()

    // Pre-flight settings
    var isCameraEnabled by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(true) }
    var isMicMuted by remember { mutableStateOf(true) }

    // Pomodoro & Structure Settings
    var usePomodoro by remember { mutableStateOf(true) }
    var pomodoroPreset by remember { mutableStateOf("STANDARD") } // "STANDARD", "ADHD", "CUSTOM"
    var eachSessionMin by remember { mutableIntStateOf(25) }
    var shortBreakMin by remember { mutableIntStateOf(5) }
    var longBreakMin by remember { mutableIntStateOf(15) }
    var sessionsInCycle by remember { mutableIntStateOf(4) }
    var loopCycleEnabled by remember { mutableStateOf(true) }

    // Task selection
    var selectedTaskId by remember { mutableStateOf<Long?>(null) }
    var selectedTaskTitle by remember { mutableStateOf("") }
    var showPastTasksSelector by remember { mutableStateOf(false) }

    // Audio & Ambience
    var selectedSound by remember { mutableStateOf(FocusSound.RAIN) }
    var soundVolume by remember { mutableFloatStateOf(0.7f) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showNextConfirmDialog by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) isCameraEnabled = true
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave Focus Room?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Your focus time up to this moment will be automatically recorded into your productivity history.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showLeaveDialog = false
                        viewModel.stopFocus()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseUrgent)
                ) {
                    Text("Leave Room", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Stay", color = CyanAccent)
                }
            },
            containerColor = CharcoalCardElevated
        )
    }

    if (showNextConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showNextConfirmDialog = false },
            title = { Text("Advance to Next Session?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                val nextLabel = if (timerState.isBreak) "Focus Session #${timerState.sessionInCycle}" else "Break Period"
                Text("Completed your current task ahead of time? Advance directly to $nextLabel.", color = TextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNextConfirmDialog = false
                        viewModel.nextFocusPeriod()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Next Session", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNextConfirmDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CharcoalCardElevated
        )
    }

    if (!timerState.isRunning) {
        // ==========================================
        // 1. PRE-ENTRY ROOM SETUP
        // ==========================================
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Enter $roomName",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Customize your focus cycle, select tasks, and configure your room ambience",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // --- Pomodoro Structure Card ---
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Pomodoro Cycle Structure", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text(
                                        text = if (usePomodoro) "Structured Focus + Breaks" else "Open Flow (Count-up Timer)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = usePomodoro,
                                onCheckedChange = { usePomodoro = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyanAccent,
                                    checkedTrackColor = CyanAccent.copy(alpha = 0.4f)
                                )
                            )
                        }

                        if (usePomodoro) {
                            // Preset Selection: Standard, ADHD, Custom
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Triple("STANDARD", "Standard", "25m • 4 sessions"),
                                    Triple("ADHD", "ADHD Flow", "15m • 3 sessions"),
                                    Triple("CUSTOM", "Custom", "Tailored intervals")
                                ).forEach { (presetKey, title, subtitle) ->
                                    val isSel = pomodoroPreset == presetKey
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSel) CyanAccent.copy(alpha = 0.22f) else CharcoalCardElevated,
                                        border = if (isSel) BorderStroke(1.dp, CyanAccent) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                pomodoroPreset = presetKey
                                                when (presetKey) {
                                                    "STANDARD" -> {
                                                        eachSessionMin = 25
                                                        shortBreakMin = 5
                                                        longBreakMin = 15
                                                        sessionsInCycle = 4
                                                    }
                                                    "ADHD" -> {
                                                        eachSessionMin = 15
                                                        shortBreakMin = 3
                                                        longBreakMin = 10
                                                        sessionsInCycle = 3
                                                    }
                                                }
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) CyanAccent else TextPrimary
                                            )
                                            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        }
                                    }
                                }
                            }

                            // Interval Controls (Adjustable for all or Custom)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CharcoalCardElevated)
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Session Duration (Truly custom exact minutes)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Focus Session Duration:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    if (eachSessionMin > 1) {
                                                        eachSessionMin--
                                                        pomodoroPreset = "CUSTOM"
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text("-", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = VioletPrimary.copy(alpha = 0.3f),
                                                border = BorderStroke(1.dp, CyanAccent)
                                            ) {
                                                Text(
                                                    text = "${eachSessionMin}m",
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    if (eachSessionMin < 180) {
                                                        eachSessionMin++
                                                        pomodoroPreset = "CUSTOM"
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text("+", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }
                                        }
                                    }

                                    // Quick Selection Chips + Slider
                                    Slider(
                                        value = eachSessionMin.toFloat(),
                                        onValueChange = {
                                            eachSessionMin = it.toInt().coerceIn(1, 120)
                                            pomodoroPreset = "CUSTOM"
                                        },
                                        valueRange = 1f..120f,
                                        steps = 119,
                                        colors = SliderDefaults.colors(
                                            thumbColor = CyanAccent,
                                            activeTrackColor = VioletPrimary,
                                            inactiveTrackColor = CharcoalDark
                                        )
                                    )

                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(15, 25, 30, 37, 45, 50, 60, 90).forEach { mins ->
                                            val isSel = eachSessionMin == mins
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSel) VioletPrimary else CharcoalDark,
                                                modifier = Modifier.clickable {
                                                    eachSessionMin = mins
                                                    pomodoroPreset = "CUSTOM"
                                                }
                                            ) {
                                                Text(
                                                    text = "${mins}m",
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                                    fontSize = 11.sp,
                                                    color = if (isSel) Color.White else TextPrimary,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }

                                // Short Break Duration (Truly custom)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Short Break:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    if (shortBreakMin > 1) {
                                                        shortBreakMin--
                                                        pomodoroPreset = "CUSTOM"
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text("-", color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = EmeraldSuccess.copy(alpha = 0.2f),
                                                border = BorderStroke(1.dp, EmeraldSuccess)
                                            ) {
                                                Text(
                                                    text = "${shortBreakMin}m",
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldSuccess
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    if (shortBreakMin < 60) {
                                                        shortBreakMin++
                                                        pomodoroPreset = "CUSTOM"
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text("+", color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }
                                        }
                                    }

                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(2, 3, 5, 7, 10, 15).forEach { mins ->
                                            val isSel = shortBreakMin == mins
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSel) EmeraldSuccess else CharcoalDark,
                                                modifier = Modifier.clickable {
                                                    shortBreakMin = mins
                                                    pomodoroPreset = "CUSTOM"
                                                }
                                            ) {
                                                Text(
                                                    text = "${mins}m",
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                                    fontSize = 11.sp,
                                                    color = if (isSel) Color.Black else TextPrimary,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }

                                // Long Break Duration (Truly custom)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Long Break:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    if (longBreakMin > 1) {
                                                        longBreakMin--
                                                        pomodoroPreset = "CUSTOM"
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text("-", color = PinkAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = PinkAccent.copy(alpha = 0.2f),
                                                border = BorderStroke(1.dp, PinkAccent)
                                            ) {
                                                Text(
                                                    text = "${longBreakMin}m",
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PinkAccent
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    if (longBreakMin < 90) {
                                                        longBreakMin++
                                                        pomodoroPreset = "CUSTOM"
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text("+", color = PinkAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }
                                        }
                                    }

                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(10, 15, 20, 25, 30, 45).forEach { mins ->
                                            val isSel = longBreakMin == mins
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSel) PinkAccent else CharcoalDark,
                                                modifier = Modifier.clickable {
                                                    longBreakMin = mins
                                                    pomodoroPreset = "CUSTOM"
                                                }
                                            ) {
                                                Text(
                                                    text = "${mins}m",
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                                    fontSize = 11.sp,
                                                    color = if (isSel) Color.White else TextPrimary,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }

                                // Sessions per Cycle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Sessions / Cycle:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(1, 2, 3, 4, 5, 6, 8).forEach { num ->
                                            val isSel = sessionsInCycle == num
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSel) CyanAccent else CharcoalDark,
                                                modifier = Modifier.clickable {
                                                    sessionsInCycle = num
                                                    pomodoroPreset = "CUSTOM"
                                                }
                                            ) {
                                                Text(
                                                    text = "$num",
                                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                                    fontSize = 11.sp,
                                                    color = if (isSel) Color.Black else TextPrimary,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }

                                // Loop Cycle Switch
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Repeat, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Loop Cycle Continuously", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                    }
                                    Switch(
                                        checked = loopCycleEnabled,
                                        onCheckedChange = { loopCycleEnabled = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = CyanAccent,
                                            checkedTrackColor = CyanAccent.copy(alpha = 0.4f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- Task Selection Card (Today & Yesterday/Past) ---
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Focus Task / Study Goal", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            TextButton(onClick = { showPastTasksSelector = !showPastTasksSelector }) {
                                Text(if (showPastTasksSelector) "Show Active Tasks" else "Pick Yesterday / Past Task", fontSize = 12.sp, color = CyanAccent)
                            }
                        }

                        OutlinedTextField(
                            value = selectedTaskTitle,
                            onValueChange = {
                                selectedTaskTitle = it
                                selectedTaskId = null
                            },
                            placeholder = { Text("e.g. Study Physics Chapter 4, Practice Calculus...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = CharcoalBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        if (!showPastTasksSelector && activeTasks.isNotEmpty()) {
                            Text("Today's Active Tasks:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                activeTasks.take(6).forEach { t ->
                                    val isSel = selectedTaskId == t.id || selectedTaskTitle == t.title
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) VioletPrimary else CharcoalCardElevated,
                                        modifier = Modifier.clickable {
                                            selectedTaskId = t.id
                                            selectedTaskTitle = t.title
                                        }
                                    ) {
                                        Text(
                                            text = t.title,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            color = if (isSel) Color.White else TextPrimary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }

                        if (showPastTasksSelector && completedTasks.isNotEmpty()) {
                            Text("Yesterday / Past Tasks to Revisit:", style = MaterialTheme.typography.labelSmall, color = AmberWarning)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                completedTasks.take(6).forEach { t ->
                                    val isSel = selectedTaskId == t.id || selectedTaskTitle == t.title
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) AmberWarning else CharcoalCardElevated,
                                        modifier = Modifier.clickable {
                                            selectedTaskId = t.id
                                            selectedTaskTitle = t.title
                                        }
                                    ) {
                                        Text(
                                            text = "↩ ${t.title}",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            color = if (isSel) Color.Black else TextPrimary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- Camera & Presence Card ---
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isCameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                    contentDescription = null,
                                    tint = if (isCameraEnabled) CyanAccent else TextMuted
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Study Mirror Camera", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Local on-device presence accountability", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }

                            Button(
                                onClick = {
                                    if (!hasCameraPermission) {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    } else {
                                        isCameraEnabled = !isCameraEnabled
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCameraEnabled) CyanAccent.copy(alpha = 0.2f) else CharcoalCardElevated
                                )
                            ) {
                                Text(
                                    text = if (isCameraEnabled) "ON" else "OFF",
                                    color = if (isCameraEnabled) CyanAccent else TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (isCameraEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Camera Lens", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (useFrontCamera) VioletPrimary else CharcoalCardElevated,
                                        modifier = Modifier.clickable { useFrontCamera = true }
                                    ) {
                                        Text(
                                            "Front Face",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            color = if (useFrontCamera) Color.White else TextMuted,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (!useFrontCamera) VioletPrimary else CharcoalCardElevated,
                                        modifier = Modifier.clickable { useFrontCamera = false }
                                    ) {
                                        Text(
                                            "Rear Desk",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            color = if (!useFrontCamera) Color.White else TextMuted,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- Focus Soundscape ---
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Focus Soundscape", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                FocusSound.NONE,
                                FocusSound.RAIN,
                                FocusSound.COZY_FIRE,
                                FocusSound.LO_FI,
                                FocusSound.DEEP_FOCUS,
                                FocusSound.FOREST,
                                FocusSound.STREAM
                            ).forEach { snd ->
                                val isSel = selectedSound == snd
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) VioletPrimary else CharcoalCardElevated,
                                    modifier = Modifier.clickable { selectedSound = snd }
                                ) {
                                    Text(
                                        text = snd.displayName,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSel) Color.White else TextPrimary
                                    )
                                }
                            }
                        }

                        if (selectedSound != FocusSound.NONE) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Slider(
                                    value = soundVolume,
                                    onValueChange = { soundVolume = it },
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = CyanAccent,
                                        activeTrackColor = CyanAccent,
                                        inactiveTrackColor = CharcoalBorder
                                    )
                                )
                                Text("${(soundVolume * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = TextMuted, modifier = Modifier.width(36.dp))
                            }
                        }
                    }
                }
            }

            // --- Enter Room Button ---
            item {
                Button(
                    onClick = {
                        viewModel.setSound(selectedSound)
                        viewModel.setVolume(soundVolume)
                        val modeToUse = if (!usePomodoro) {
                            FocusMode.STOPWATCH
                        } else {
                            when (pomodoroPreset) {
                                "ADHD" -> FocusMode.ADHD
                                "CUSTOM" -> FocusMode.CUSTOM
                                else -> FocusMode.POMODORO
                            }
                        }
                        viewModel.startFocus(
                            mode = modeToUse,
                            durationMinutes = eachSessionMin,
                            sound = selectedSound,
                            taskId = selectedTaskId,
                            taskTitle = selectedTaskTitle.ifBlank { "$roomName Study" },
                            totalSessions = sessionsInCycle,
                            shortBreak = shortBreakMin,
                            longBreak = longBreakMin,
                            loop = loopCycleEnabled
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                ) {
                    Icon(Icons.Default.DoorFront, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enter Focus Room & Start Timer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    } else {
        // ==========================================
        // 2. INSIDE THE FOCUS ROOM
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Room Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = timerState.activeTaskTitle ?: "$roomName Active",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (timerState.isBreak) "🌿 Rest Phase • Breathe & recharge" else "🔥 Phase: Deep Flow • Minimal distraction",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (timerState.isBreak) EmeraldSuccess else CyanAccent
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldSuccess.copy(alpha = 0.2f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ROOM ACTIVE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                    }
                }
            }

            // Pomodoro Cycle & Count-up Status Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CharcoalCardElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cycle indicator
                    if (timerState.mode != FocusMode.STOPWATCH) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Repeat, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cycle #${timerState.cycleNumber} • ${if (timerState.isBreak) timerState.breakType + " Break" else "Session ${timerState.sessionInCycle}/${timerState.totalSessionsInCycle}"}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (timerState.isBreak) EmeraldSuccess else CyanAccent
                            )
                        }
                    } else {
                        Text("Continuous Flow Mode", style = MaterialTheme.typography.labelMedium, color = CyanAccent, fontWeight = FontWeight.Bold)
                    }

                    // Total Elapsed in Room since joining (Count-up)
                    val elapsedM = timerState.elapsedSeconds / 60
                    val elapsedS = timerState.elapsedSeconds % 60
                    Text(
                        text = "Time in Room: %02d:%02d".format(elapsedM, elapsedS),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            // Main Room Centerpiece: Camera feed or Ambient Study Orb
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CharcoalCard)
                    .border(2.dp, VioletPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isCameraEnabled && hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val selector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
                                } catch (e: Exception) {
                                    try {
                                        cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview)
                                    } catch (ex: Exception) {
                                        // Ignore
                                    }
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        GlowingOrb(
                            size = 130.dp,
                            isPulsing = timerState.isRunning && !timerState.isPaused
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (timerState.isBreak) "Rest & Recharge" else "Focus Flow Active",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (timerState.isBreak) EmeraldSuccess else CyanAccent
                        )
                        Text(
                            text = "Timer counting continuously while in room",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                // Floating Timer HUD on top
                val displaySecs = if (timerState.mode == FocusMode.STOPWATCH) timerState.elapsedSeconds else timerState.currentSeconds
                val mins = displaySecs / 60
                val secs = displaySecs % 60
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "%02d:%02d".format(mins, secs),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (timerState.mode != FocusMode.STOPWATCH) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (timerState.isBreak) "BREAK" else "FOCUS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (timerState.isBreak) EmeraldSuccess else CyanAccent
                            )
                        }
                    }
                }
            }

            // In-Room Bottom Control Panel
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Camera Toggle
                        IconButton(
                            onClick = {
                                if (!hasCameraPermission) {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                } else {
                                    isCameraEnabled = !isCameraEnabled
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isCameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = "Camera Toggle",
                                tint = if (isCameraEnabled) CyanAccent else TextMuted
                            )
                        }

                        // Flip Camera Lens
                        if (isCameraEnabled) {
                            IconButton(onClick = { useFrontCamera = !useFrontCamera }) {
                                Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip Lens", tint = TextPrimary)
                            }
                        }

                        // Play/Pause
                        Button(
                            onClick = {
                                if (timerState.isPaused) viewModel.resumeFocus() else viewModel.pauseFocus()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                        ) {
                            Icon(
                                imageVector = if (timerState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (timerState.isPaused) "Resume" else "Pause")
                        }

                        // Next Session / Skip early
                        if (timerState.mode != FocusMode.STOPWATCH) {
                            IconButton(onClick = { showNextConfirmDialog = true }) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Next Session", tint = CyanAccent)
                            }
                        }

                        // Leave Room
                        IconButton(onClick = { showLeaveDialog = true }) {
                            Icon(Icons.Default.DoorFront, contentDescription = "Leave Room", tint = RoseUrgent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JoinStudyRoomSimulation(viewModel: RacerViewModel) {
    val virtualRooms = listOf(
        VirtualRoom("Silent Library Hall", 4, "Deep Silent Reading & Study", listOf("Maya (Biology)", "Alex (Calculus)", "Ken (Economics)", "Sophie (History)")),
        VirtualRoom("Late Night Code & Coffee", 3, "Programming & Problem Solving", listOf("Liam (Kotlin)", "Carlos (Algorithms)", "Emma (Data Structures)")),
        VirtualRoom("Deep Focus & Writing Lounge", 2, "Writing & Deep Research", listOf("Chloe (Essay)", "David (Thesis)"))
    )

    var activeVirtualRoom by remember { mutableStateOf<VirtualRoom?>(null) }

    if (activeVirtualRoom == null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text("Join Study Room", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Study alongside virtual study partners with live count-up & Pomodoro cycles", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CyanAccent.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Accountability Mode: When you join a room, your timer immediately starts counting up and tracks your Pomodoro intervals until you leave.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }

            items(virtualRooms) { room ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { activeVirtualRoom = room }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(room.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Surface(shape = RoundedCornerShape(8.dp), color = EmeraldSuccess.copy(alpha = 0.2f)) {
                                Text("${room.activeCount} Focusing", color = EmeraldSuccess, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                        Text(room.topic, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            room.members.forEach { m ->
                                Surface(shape = RoundedCornerShape(6.dp), color = CharcoalCardElevated) {
                                    Text(m, style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        val room = activeVirtualRoom!!
        MyPersonalFocusRoom(
            viewModel = viewModel,
            roomName = room.name,
            studyPartners = room.members
        )
    }
}

@Composable
private fun FocusRoomHistoryView(viewModel: RacerViewModel) {
    val allSessions by viewModel.repository.allFocusSessions.collectAsState(initial = emptyList())
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("Focus Room Sessions", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Detailed record of every focus session you completed", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        if (allSessions.isEmpty()) {
            item {
                Text("No past room sessions found. Start a focus session to record your history!", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            }
        } else {
            items(allSessions) { session ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = session.taskTitle ?: session.mode.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (session.isCompleted) EmeraldSuccess.copy(alpha = 0.2f) else CharcoalCardElevated
                            ) {
                                Text(
                                    text = if (session.isCompleted) "Completed" else "Partial",
                                    color = if (session.isCompleted) EmeraldSuccess else TextMuted,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = sdf.format(Date(session.startTime)),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Focus Time: ${session.actualDurationSeconds / 60}m", style = MaterialTheme.typography.bodySmall, color = CyanAccent)
                            Text("Sound: ${session.soundUsed}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

private data class VirtualRoom(
    val name: String,
    val activeCount: Int,
    val topic: String,
    val members: List<String>
)
