package com.example.ui.screens.clock

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AlarmEntity
import com.example.data.model.FocusSound
import com.example.ui.components.GlassCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseUrgent
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.RacerViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockScreen(
    viewModel: RacerViewModel,
    modifier: Modifier = Modifier
) {
    // 0: Alarm, 1: Bedtime, 2: Timer, 3: Stopwatch, 4: World Clock
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Alarm", "Bedtime", "Timer", "Stopwatch", "World")

    val alarms by viewModel.allAlarms.collectAsState()
    var showAddAlarmSheet by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Google Clock Suite",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Alarms, Bedtime Schedule, Timers & World Clocks",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Segmented Tabs Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    val icon = when (index) {
                        0 -> Icons.Default.Alarm
                        1 -> Icons.Default.Bedtime
                        2 -> Icons.Default.Timer
                        3 -> Icons.Default.HourglassBottom
                        else -> Icons.Default.Language
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) VioletPrimary else CharcoalCardElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CyanAccent else CharcoalBorder
                        ),
                        modifier = Modifier.clickable { selectedTab = index }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }

            // Main Tab Content
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> AlarmsSection(
                        alarms = alarms.filter { !it.isBedtimeAlarm },
                        onToggleAlarm = { viewModel.toggleAlarm(it) },
                        onDeleteAlarm = { viewModel.deleteAlarm(it.id) },
                        onAddAlarmClick = { showAddAlarmSheet = true },
                        onQuickNap = { minutes ->
                            val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, minutes) }
                            viewModel.addAlarm(
                                hour = cal.get(Calendar.HOUR_OF_DAY),
                                minute = cal.get(Calendar.MINUTE),
                                label = "Power Nap ($minutes min)"
                            )
                        },
                        onTestRing = {
                            viewModel.testTaskReminder("Alarm Test Sound & Vibration")
                        }
                    )
                    1 -> BedtimeSection(
                        viewModel = viewModel,
                        bedtimeAlarms = alarms.filter { it.isBedtimeAlarm }
                    )
                    2 -> StandaloneTimerSection()
                    3 -> StopwatchSection()
                    4 -> WorldClockSection()
                }
            }
        }

        // Add Alarm FAB for Alarms Tab
        if (selectedTab == 0) {
            FloatingActionButton(
                onClick = { showAddAlarmSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = VioletPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Alarm")
            }
        }

        if (showAddAlarmSheet) {
            AddAlarmBottomSheet(
                onDismiss = { showAddAlarmSheet = false },
                onSaveAlarm = { hour, min, label, daysMask, vibrate ->
                    viewModel.addAlarm(
                        hour = hour,
                        minute = min,
                        label = label,
                        daysOfWeekMask = daysMask,
                        vibrate = vibrate,
                        isBedtime = false
                    )
                    showAddAlarmSheet = false
                }
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 1. ALARMS SECTION
// -----------------------------------------------------------------------------
@Composable
fun AlarmsSection(
    alarms: List<AlarmEntity>,
    onToggleAlarm: (AlarmEntity) -> Unit,
    onDeleteAlarm: (AlarmEntity) -> Unit,
    onAddAlarmClick: () -> Unit,
    onQuickNap: (Int) -> Unit,
    onTestRing: () -> Unit
) {
    val nextAlarmCountdown = remember(alarms) {
        calculateNextAlarmText(alarms)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Next Alarm Banner
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (alarms.any { it.isEnabled }) CyanAccent.copy(alpha = 0.5f) else CharcoalBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (alarms.any { it.isEnabled }) "Upcoming Alarm" else "No Alarms Scheduled",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = nextAlarmCountdown,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (alarms.any { it.isEnabled }) CyanAccent else TextMuted
                        )
                    }

                    Button(
                        onClick = onTestRing,
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalCard),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Ring", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                    }
                }
            }
        }

        // Quick Power Nap Shortcuts
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Power Nap",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10, 20, 30, 45, 60).forEach { mins ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CharcoalCardElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                            modifier = Modifier.clickable { onQuickNap(mins) }
                        ) {
                            Text(
                                text = "+$mins min",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Alarms List
        items(alarms, key = { it.id }) { alarm ->
            AlarmCard(
                alarm = alarm,
                onToggle = { onToggleAlarm(alarm) },
                onDelete = { onDeleteAlarm(alarm) }
            )
        }

        if (alarms.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No Alarms Set",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tap '+' to schedule a wake-up or focus alarm.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun AlarmCard(
    alarm: AlarmEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val hour12 = if (alarm.hour == 0) 12 else if (alarm.hour > 12) alarm.hour - 12 else alarm.hour
    val amPm = if (alarm.hour < 12) "AM" else "PM"
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", hour12, alarm.minute)

    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        borderColor = if (alarm.isEnabled) CyanAccent.copy(alpha = 0.4f) else CharcoalBorder
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (alarm.isEnabled) TextPrimary else TextDisabled
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = amPm,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (alarm.isEnabled) CyanAccent else TextDisabled,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (alarm.isEnabled) TextSecondary else TextMuted
                    )
                }

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyanAccent,
                        checkedTrackColor = VioletPrimary
                    )
                )
            }

            // Days of Week Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                dayNames.forEachIndexed { index, name ->
                    val isDayActive = (alarm.daysOfWeekMask and (1 shl index)) != 0
                    Surface(
                        shape = CircleShape,
                        color = if (isDayActive && alarm.isEnabled) VioletPrimary else CharcoalCard,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDayActive && alarm.isEnabled) Color.White else TextMuted
                            )
                        }
                    }
                }
            }

            // Expandable details (Vibration, Delete)
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (alarm.vibrateEnabled) "Vibration ON" else "Vibration OFF",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = RoseUrgent
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 2. BEDTIME & SLEEP RHYTHM SECTION (Google Clock Bedtime Mode)
// -----------------------------------------------------------------------------
@Composable
fun BedtimeSection(
    viewModel: RacerViewModel,
    bedtimeAlarms: List<AlarmEntity>
) {
    var bedtimeHour by remember { mutableIntStateOf(22) } // 10:00 PM
    var bedtimeMinute by remember { mutableIntStateOf(30) }
    var wakeHour by remember { mutableIntStateOf(6) } // 06:30 AM
    var wakeMinute by remember { mutableIntStateOf(30) }

    var bedtimeReminderEnabled by remember { mutableStateOf(true) }
    var sunriseAlarmEnabled by remember { mutableStateOf(true) }
    var isPlayingSleepSound by remember { mutableStateOf(false) }
    var selectedSleepSound by remember { mutableStateOf(FocusSound.RAIN) }
    var sleepTimerMinutes by remember { mutableIntStateOf(30) }

    // Calculate total sleep duration
    val sleepDurationHours = remember(bedtimeHour, bedtimeMinute, wakeHour, wakeMinute) {
        val bedTotalMins = bedtimeHour * 60 + bedtimeMinute
        val wakeTotalMins = wakeHour * 60 + wakeMinute
        val diffMins = if (wakeTotalMins >= bedTotalMins) wakeTotalMins - bedTotalMins else (1440 - bedTotalMins) + wakeTotalMins
        val h = diffMins / 60
        val m = diffMins % 60
        "${h}h ${m}m"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Sleep Target Summary Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = VioletPrimary.copy(alpha = 0.6f),
                backgroundColor = CharcoalCardElevated
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Restorative Sleep Rhythm",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Target: $sleepDurationHours of optimal sleep",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CyanAccent
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = VioletPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Bedtime, contentDescription = null, tint = CyanAccent)
                            }
                        }
                    }

                    // Bedtime vs Wake-Up Tiles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Bedtime Tile
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = CharcoalCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bedtime, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bedtime", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                                val formattedBed = formatTime12h(bedtimeHour, bedtimeMinute)
                                Text(
                                    text = formattedBed,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        // Wake-up Tile
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = CharcoalCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Wake Up", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                                val formattedWake = formatTime12h(wakeHour, wakeMinute)
                                Text(
                                    text = formattedWake,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bedtime Schedule & Wind Down Actions
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Bedtime Routine & Wind Down",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Wind Down Notification Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Wind Down Reminder", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Text("Alert 15 mins before bedtime to unplug", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        Switch(
                            checked = bedtimeReminderEnabled,
                            onCheckedChange = {
                                bedtimeReminderEnabled = it
                                if (it) {
                                    viewModel.addAlarm(
                                        hour = if (bedtimeMinute >= 15) bedtimeHour else (bedtimeHour - 1).coerceAtLeast(0),
                                        minute = if (bedtimeMinute >= 15) bedtimeMinute - 15 else (bedtimeMinute + 45),
                                        label = "Bedtime Wind Down (15 min)",
                                        isBedtime = true
                                    )
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = VioletPrimary)
                        )
                    }

                    // Sunrise Wake-Up Glow Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sunrise Wake-Up Effect", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Text("Gradual ambient sound & brightness crescendo", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        Switch(
                            checked = sunriseAlarmEnabled,
                            onCheckedChange = { sunriseAlarmEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = VioletPrimary)
                        )
                    }
                }
            }
        }

        // Sleep Soundscapes (Soothing Audio for falling asleep)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sleep Soundscapes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CharcoalCardElevated
                        ) {
                            Text(
                                text = "Auto-off: ${sleepTimerMinutes}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    val sleepSounds = listOf(
                        FocusSound.RAIN,
                        FocusSound.WHITE_NOISE,
                        FocusSound.COZY_FIRE,
                        FocusSound.STREAM,
                        FocusSound.FOREST,
                        FocusSound.CHIME
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sleepSounds.forEach { sound ->
                            val isSelected = selectedSleepSound == sound
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) VioletPrimary else CharcoalCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyanAccent else CharcoalBorder),
                                modifier = Modifier.clickable {
                                    selectedSleepSound = sound
                                    if (isPlayingSleepSound) {
                                        viewModel.setSound(sound)
                                    }
                                }
                            ) {
                                Text(
                                    text = sound.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            isPlayingSleepSound = !isPlayingSleepSound
                            if (isPlayingSleepSound) {
                                viewModel.setSound(selectedSleepSound)
                                viewModel.startFocus(
                                    durationMinutes = sleepTimerMinutes,
                                    sound = selectedSleepSound,
                                    taskId = null,
                                    taskTitle = "Restorative Sleep"
                                )
                            } else {
                                viewModel.stopFocus()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlayingSleepSound) RoseUrgent else VioletPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlayingSleepSound) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlayingSleepSound) "Stop Sleep Sound" else "Play ${selectedSleepSound.displayName} & Sleep",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// -----------------------------------------------------------------------------
// 3. STANDALONE TIMER SECTION
// -----------------------------------------------------------------------------
@Composable
fun StandaloneTimerSection() {
    var totalSeconds by remember { mutableIntStateOf(300) } // 5 min default
    var remainingSeconds by remember { mutableIntStateOf(300) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        } else if (isRunning && remainingSeconds == 0) {
            isRunning = false
        }
    }

    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Timer Dial
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = CyanAccent,
                    trackColor = CharcoalCardElevated,
                    strokeWidth = 10.dp
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isRunning) "Running" else if (remainingSeconds == 0) "Completed" else "Ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isRunning) EmeraldSuccess else TextMuted
                    )
                }
            }
        }

        // Quick Preset Grid
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Quick Timer Presets",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                val presets = listOf(1, 3, 5, 10, 15, 25, 30, 45, 60)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { mins ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CharcoalCardElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                            modifier = Modifier.clickable {
                                isRunning = false
                                totalSeconds = mins * 60
                                remainingSeconds = totalSeconds
                            }
                        ) {
                            Text(
                                text = "${mins}m",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Control Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        remainingSeconds += 60
                        totalSeconds += 60
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalCardElevated)
                ) {
                    Text("+1 Min", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier.weight(1.5f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) AmberWarning else VioletPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isRunning) "Pause" else "Start", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        isRunning = false
                        remainingSeconds = totalSeconds
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalCardElevated)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = TextMuted)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 4. STOPWATCH SECTION
// -----------------------------------------------------------------------------
@Composable
fun StopwatchSection() {
    var elapsedMillis by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    val laps = remember { mutableStateListOf<Long>() }

    LaunchedEffect(isRunning) {
        var lastTime = System.currentTimeMillis()
        while (isRunning) {
            val now = System.currentTimeMillis()
            elapsedMillis += (now - lastTime)
            lastTime = now
            delay(16L) // ~60fps refresh
        }
    }

    val totalSecs = elapsedMillis / 1000
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    val millis = (elapsedMillis % 1000) / 10

    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d.%02d", mins, secs, millis)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (isRunning) {
                            laps.add(0, elapsedMillis)
                        } else {
                            elapsedMillis = 0L
                            laps.clear()
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalCardElevated)
                ) {
                    Text(if (isRunning) "Lap" else "Reset", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier.weight(1.5f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) RoseUrgent else EmeraldSuccess
                    )
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isRunning) "Stop" else "Start", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Laps List
        itemsIndexed(laps) { index, lapTime ->
            val lapMins = (lapTime / 1000) / 60
            val lapSecs = (lapTime / 1000) % 60
            val lapMs = (lapTime % 1000) / 10
            val lapFormatted = String.format(Locale.getDefault(), "%02d:%02d.%02d", lapMins, lapSecs, lapMs)

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CharcoalCardElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lap ${laps.size - index}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = lapFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 5. WORLD CLOCK SECTION
// -----------------------------------------------------------------------------
data class WorldCity(val cityName: String, val timeZoneId: String, val country: String)

@Composable
fun WorldClockSection() {
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val cities = listOf(
        WorldCity("London", "Europe/London", "United Kingdom"),
        WorldCity("New York", "America/New_York", "United States"),
        WorldCity("Tokyo", "Asia/Tokyo", "Japan"),
        WorldCity("Paris", "Europe/Paris", "France"),
        WorldCity("Dubai", "Asia/Dubai", "United Arab Emirates"),
        WorldCity("Singapore", "Asia/Singapore", "Singapore"),
        WorldCity("Sydney", "Australia/Sydney", "Australia"),
        WorldCity("San Francisco", "America/Los_Angeles", "United States"),
        WorldCity("Berlin", "Europe/Berlin", "Germany"),
        WorldCity("Toronto", "America/Toronto", "Canada"),
        WorldCity("Mumbai", "Asia/Kolkata", "India"),
        WorldCity("Hong Kong", "Asia/Hong_Kong", "China")
    )

    var searchQuery by remember { mutableStateOf("") }
    val filteredCities = cities.filter {
        it.cityName.contains(searchQuery, ignoreCase = true) || it.country.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Home Local Time Header
        item {
            val localFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            val localDate = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date(currentTimeMillis))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = CyanAccent.copy(alpha = 0.5f),
                backgroundColor = CharcoalCardElevated
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Local Home Time", style = MaterialTheme.typography.labelMedium, color = CyanAccent)
                        Text(TimeZone.getDefault().id, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    Text(
                        text = localFormat.format(Date(currentTimeMillis)),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(localDate, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search international cities...", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = CharcoalBorder,
                    focusedContainerColor = CharcoalCard,
                    unfocusedContainerColor = CharcoalCard
                )
            )
        }

        // Cities Cards
        items(filteredCities) { city ->
            val tz = TimeZone.getTimeZone(city.timeZoneId)
            val cityFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()).apply { timeZone = tz }
            val cityDate = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).apply { timeZone = tz }
            val formattedTime = cityFormat.format(Date(currentTimeMillis))
            val formattedDate = cityDate.format(Date(currentTimeMillis))

            // Time difference relative to local
            val localTz = TimeZone.getDefault()
            val diffHours = (tz.getOffset(currentTimeMillis) - localTz.getOffset(currentTimeMillis)) / (1000 * 60 * 60)
            val diffText = if (diffHours == 0) "Same time" else if (diffHours > 0) "+$diffHours hrs ahead" else "$diffHours hrs behind"

            val cal = Calendar.getInstance(tz).apply { timeInMillis = currentTimeMillis }
            val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
            val isDaytime = hourOfDay in 6..18

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = city.cityName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (isDaytime) Icons.Default.WbSunny else Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = if (isDaytime) AmberWarning else VioletPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "${city.country} • $diffText",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// -----------------------------------------------------------------------------
// ADD ALARM BOTTOM SHEET
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmBottomSheet(
    onDismiss: () -> Unit,
    onSaveAlarm: (hour: Int, min: Int, label: String, daysMask: Int, vibrate: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var hour by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(0) }
    var isAm by remember { mutableStateOf(true) }
    var label by remember { mutableStateOf("Morning Routine") }
    var vibrate by remember { mutableStateOf(true) }
    var daysMask by remember { mutableIntStateOf(127) } // All days

    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CharcoalCardElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Set New Alarm",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Hour & Minute Stepper Selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour Selector
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CharcoalCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d", hour),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { hour = if (hour <= 1) 12 else hour - 1 }) {
                                Text("-", style = MaterialTheme.typography.headlineMedium, color = CyanAccent)
                            }
                            IconButton(onClick = { hour = if (hour >= 12) 1 else hour + 1 }) {
                                Text("+", style = MaterialTheme.typography.headlineMedium, color = CyanAccent)
                            }
                        }
                    }
                }

                Text(":", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

                // Minute Selector
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CharcoalCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d", minute),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { minute = if (minute <= 0) 55 else minute - 5 }) {
                                Text("-", style = MaterialTheme.typography.headlineMedium, color = CyanAccent)
                            }
                            IconButton(onClick = { minute = if (minute >= 55) 0 else minute + 5 }) {
                                Text("+", style = MaterialTheme.typography.headlineMedium, color = CyanAccent)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // AM/PM Toggle
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isAm) VioletPrimary else CharcoalCard,
                        modifier = Modifier.clickable { isAm = true }
                    ) {
                        Text(
                            text = "AM",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isAm) Color.White else TextMuted,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isAm) VioletPrimary else CharcoalCard,
                        modifier = Modifier.clickable { isAm = false }
                    ) {
                        Text(
                            text = "PM",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (!isAm) Color.White else TextMuted,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Alarm Label
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Alarm Label") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = CharcoalBorder
                )
            )

            // Repeat Days of Week
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Repeat on", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayNames.forEachIndexed { index, name ->
                        val isSelected = (daysMask and (1 shl index)) != 0
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) VioletPrimary else CharcoalCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyanAccent else CharcoalBorder),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    daysMask = daysMask xor (1 shl index)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Vibrate Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vibrate", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Switch(
                    checked = vibrate,
                    onCheckedChange = { vibrate = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = VioletPrimary)
                )
            }

            // Save Button
            Button(
                onClick = {
                    val final24Hour = if (isAm) {
                        if (hour == 12) 0 else hour
                    } else {
                        if (hour == 12) 12 else hour + 12
                    }
                    onSaveAlarm(final24Hour, minute, label, daysMask, vibrate)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
            ) {
                Text("Save Alarm", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// -----------------------------------------------------------------------------
// HELPER FUNCTIONS
// -----------------------------------------------------------------------------
private fun calculateNextAlarmText(alarms: List<AlarmEntity>): String {
    val enabled = alarms.filter { it.isEnabled }
    if (enabled.isEmpty()) return "Turn on an alarm to see next wake-up"

    val now = Calendar.getInstance()
    var closestDiffMins = Long.MAX_VALUE

    for (alarm in enabled) {
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val diffMins = (target.timeInMillis - now.timeInMillis) / (1000 * 60)
        if (diffMins < closestDiffMins) {
            closestDiffMins = diffMins
        }
    }

    val hours = closestDiffMins / 60
    val mins = closestDiffMins % 60
    return if (hours > 0) "Next alarm in ${hours}h ${mins}m" else "Next alarm in ${mins} minutes"
}

private fun formatTime12h(hour24: Int, min: Int): String {
    val h = if (hour24 == 0) 12 else if (hour24 > 12) hour24 - 12 else hour24
    val amPm = if (hour24 < 12) "AM" else "PM"
    return String.format(Locale.getDefault(), "%d:%02d %s", h, min, amPm)
}
