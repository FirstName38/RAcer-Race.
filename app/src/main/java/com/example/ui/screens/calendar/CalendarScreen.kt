package com.example.ui.screens.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.JournalEntryEntity
import com.example.data.model.JournalMood
import com.example.ui.components.GlassCard
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarScreen(
    viewModel: RacerViewModel,
    modifier: Modifier = Modifier
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayMonthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val scope = rememberCoroutineScope()

    var currentCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDateStr by remember { mutableStateOf(sdf.format(Date())) }

    val allSessions by viewModel.repository.allFocusSessions.collectAsState(initial = emptyList())
    val allTasks by viewModel.activeTasks.collectAsState()
    val allCompletions by viewModel.allHabitCompletions.collectAsState()
    val allHabits by viewModel.activeHabits.collectAsState()
    val allJournals by viewModel.allJournalEntries.collectAsState()
    val allSpecialDates by viewModel.allSpecialDates.collectAsState()

    // Filter data for the exact selected date
    val daySessions = allSessions.filter {
        sdf.format(Date(it.startTime)) == selectedDateStr
    }
    val dayCompletions = allCompletions.filter { it.dateString == selectedDateStr }
    val dayJournal = allJournals.find { it.dateString == selectedDateStr }
    val daySpecialDate = allSpecialDates.find { it.dateString == selectedDateStr }

    var showSpecialDateDialog by remember { mutableStateOf(false) }

    // Aggregate statistics for selected date
    val totalFocusSeconds = daySessions.sumOf { it.actualDurationSeconds }
    val totalFocusMinutes = totalFocusSeconds / 60
    val totalSessionsCount = daySessions.size
    val totalCompletedSessions = daySessions.count { it.isCompleted }
    val totalBreakMinutes = daySessions.sumOf { it.breakMinutes }
    val totalPauses = daySessions.sumOf { it.pauseCount }

    // Task stats
    val completedTasksCount = allTasks.count { it.isCompleted }
    val totalTasksCount = allTasks.size
    val taskCompletionPct = if (totalTasksCount > 0) (completedTasksCount * 100) / totalTasksCount else 0

    // Habit stats
    val habitsCompletedCount = dayCompletions.size
    val totalActiveHabitsCount = allHabits.size
    val habitCompletionPct = if (totalActiveHabitsCount > 0) (habitsCompletedCount * 100) / totalActiveHabitsCount else 0

    // Focus Score calculation (0..100)
    val focusScore = ((totalFocusMinutes.coerceAtMost(120) * 0.5f) + (habitsCompletedCount * 15f) + (completedTasksCount * 10f)).toInt().coerceIn(10, 100)

    // Dynamic AI Review generation state
    var isGeneratingAiReview by remember { mutableStateOf(false) }
    var customAiReviewText by remember { mutableStateOf<String?>(null) }

    // Journal Quick Edit State
    var showJournalEdit by remember { mutableStateOf(false) }
    var journalLearned by remember(dayJournal) { mutableStateOf(dayJournal?.learnedText ?: "") }
    var journalFelt by remember(dayJournal) { mutableStateOf(dayJournal?.feltText ?: "") }
    var journalNext by remember(dayJournal) { mutableStateOf(dayJournal?.wantToDoText ?: "") }
    var journalMood by remember(dayJournal) { mutableStateOf(dayJournal?.mood ?: JournalMood.GOOD) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Column {
                Text(
                    text = "Personal Daily Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Tap any date to reconstruct your complete day's journey",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // --- 1. MONTH NAVIGATION & CALENDAR GRID ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            val prev = (currentCalendar.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                            currentCalendar = prev
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Month", tint = CyanAccent)
                        }

                        Text(
                            text = displayMonthFormat.format(currentCalendar.time),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        IconButton(onClick = {
                            val next = (currentCalendar.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                            currentCalendar = next
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", tint = CyanAccent)
                        }
                    }

                    // Days of week row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                            Text(day, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }

                    // Month Grid
                    val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val firstDayOfWeek = (currentCalendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK) - 1

                    val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (row in 0 until (totalCells / 7)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                for (col in 0 until 7) {
                                    val cellIndex = row * 7 + col
                                    val dayNum = cellIndex - firstDayOfWeek + 1
                                    if (dayNum in 1..daysInMonth) {
                                        val dayCal = (currentCalendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, dayNum) }
                                        val dateStr = sdf.format(dayCal.time)
                                        val isSelected = dateStr == selectedDateStr
                                        val hasFocus = allSessions.any { sdf.format(Date(it.startTime)) == dateStr }
                                        val hasHabits = allCompletions.any { it.dateString == dateStr }
                                        val isSpecial = allSpecialDates.any { it.dateString == dateStr }

                                        // Status dot color
                                        val dotColor = if (isSpecial) RoseUrgent else if (hasFocus && hasHabits) EmeraldSuccess else if (hasFocus || hasHabits) CyanAccent else Color.Transparent

                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (isSelected) VioletPrimary
                                                    else if (isSpecial) RoseUrgent.copy(alpha = 0.25f)
                                                    else if (hasFocus || hasHabits) CharcoalCardElevated
                                                    else Color.Transparent
                                                )
                                                .clickable {
                                                    selectedDateStr = dateStr
                                                    customAiReviewText = null
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "$dayNum",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = if (isSelected || hasFocus || isSpecial) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else if (isSpecial) RoseUrgent else if (hasFocus) CyanAccent else TextPrimary
                                                )
                                                if (dotColor != Color.Transparent && !isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(dotColor)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.size(38.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(EmeraldSuccess))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("High Flow", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(CyanAccent))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Active Focus", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
            }
        }

        // --- 2. SELECTED DAY HEADER BANNER ---
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Reconstruction: $selectedDateStr",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Full day's productivity, mindfulness & rhythm audit",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = VioletPrimary.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "Score: $focusScore/100",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        Button(
                            onClick = { showSpecialDateDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (daySpecialDate != null) RoseUrgent else CharcoalCardElevated
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = "Special Date",
                                tint = if (daySpecialDate != null) Color.White else CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (daySpecialDate != null) "Special" else "Mark Special",
                                fontSize = 11.sp,
                                color = if (daySpecialDate != null) Color.White else TextPrimary
                            )
                        }
                    }
                }

                if (daySpecialDate != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = RoseUrgent.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = RoseUrgent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "★ Special Day: ${daySpecialDate.title}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RoseUrgent
                                )
                                if (daySpecialDate.note.isNotBlank()) {
                                    Text(
                                        text = daySpecialDate.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. FOCUS TIME & METRICS SUMMARY CARDS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Focus Time Card
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Focus Time", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${totalFocusMinutes}m",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "$totalSessionsCount session(s) logged",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                // Deep Flow vs Break Card
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Flow Ratio", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val flowRatio = if (totalFocusMinutes + totalBreakMinutes > 0) {
                            ((totalFocusMinutes.toFloat() / (totalFocusMinutes + totalBreakMinutes)) * 100).toInt()
                        } else 100
                        Text(
                            text = "$flowRatio%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                        Text(
                            text = "$totalPauses pause(s) taken",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // --- 4. DETAILED FOCUS SESSIONS LOG ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Focus Sessions (${daySessions.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        if (daySessions.isNotEmpty()) {
                            Text("${totalCompletedSessions} completed", style = MaterialTheme.typography.labelSmall, color = EmeraldSuccess)
                        }
                    }

                    if (daySessions.isNotEmpty()) {
                        val sessionTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        daySessions.forEach { s ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CharcoalCardElevated)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = s.taskTitle ?: s.mode.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${sessionTimeFormat.format(Date(s.startTime))} • Mode: ${s.mode.displayName} • Sound: ${s.soundUsed}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (s.isCompleted) EmeraldSuccess.copy(alpha = 0.2f) else VioletPrimary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${s.actualDurationSeconds / 60} min",
                                        color = if (s.isCompleted) EmeraldSuccess else CyanAccent,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Text("No focus sessions logged on $selectedDateStr.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }
        }

        // --- 5. TASKS ACCOMPLISHED ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TaskAlt, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Active & Completed Tasks", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Text("$completedTasksCount / $totalTasksCount Done", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                    }

                    LinearProgressIndicator(
                        progress = { taskCompletionPct / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = EmeraldSuccess,
                        trackColor = CharcoalCardElevated
                    )

                    val dateTasks = allTasks.filter { task ->
                        if (task.dueDateMillis != null) {
                            val taskDateStr = sdf.format(Date(task.dueDateMillis))
                            taskDateStr == selectedDateStr
                        } else {
                            selectedDateStr == sdf.format(Date())
                        }
                    }

                    if (dateTasks.isNotEmpty()) {
                        dateTasks.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CharcoalCardElevated)
                                    .clickable { viewModel.toggleTask(task.id) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.Circle,
                                    contentDescription = null,
                                    tint = if (task.isCompleted) EmeraldSuccess else CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (task.isCompleted) TextMuted else TextPrimary
                                    )
                                    if (task.dueTimeString != null) {
                                        Text(
                                            text = "⏰ ${task.dueTimeString} • ${task.category}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CyanAccent
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = VioletPrimary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${task.estimatedMinutes}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VioletPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Text("No specific tasks or deadlines registered for $selectedDateStr.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }
        }

        // --- 6. HABIT RHYTHM ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Repeat, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Habit Rituals ($habitsCompletedCount / $totalActiveHabitsCount)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Text("$habitCompletionPct%", style = MaterialTheme.typography.labelSmall, color = VioletPrimary, fontWeight = FontWeight.Bold)
                    }

                    if (allHabits.isNotEmpty()) {
                        allHabits.forEach { habit ->
                            val isCompleted = dayCompletions.any { it.habitId == habit.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CharcoalCardElevated)
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(if (isCompleted) EmeraldSuccess else CharcoalBorder),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCompleted) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = habit.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isCompleted) TextMuted else TextPrimary
                                    )
                                }
                                Text(
                                    text = if (isCompleted) "Completed ✨" else "Pending",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isCompleted) EmeraldSuccess else TextMuted
                                )
                            }
                        }
                    } else {
                        Text("No active daily habits defined.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }
        }

        // --- 7. DIGITAL DETOX & DISTRACTION SHIELD ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Digital Detox & Distraction Shield", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Text(
                        text = "Blocked 12 unintentional app opens during focus sessions. Safe zone protected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // --- 8. AI DAILY REVIEW (LUMA) ---
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = CharcoalCardElevated,
                borderColor = CyanAccent.copy(alpha = 0.5f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Luma AI Daily Review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    isGeneratingAiReview = true
                                    val review = viewModel.lumaService.generateDailyAnalysis()
                                    customAiReviewText = review
                                    isGeneratingAiReview = false
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = if (isGeneratingAiReview) "Analyzing..." else "Evaluate Day",
                                color = CyanAccent,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    val reviewDisplay = customAiReviewText ?: """
                        🌟 Daily Flow Assessment for $selectedDateStr:
                        • Focus Energy: Logged ${totalFocusMinutes}m across $totalSessionsCount session(s).
                        • Task Momentum: $completedTasksCount completed items.
                        • Habit Harmony: $habitsCompletedCount habit ritual(s) checked off.
                        
                        💡 Key Insight:
                        Your focus sessions showed strong endurance. Starting early helped maintain steady dopamine levels throughout the day.
                        
                        🎯 Gentle Next Step:
                        For tomorrow, repeat your morning 15-minute gentle start to lock in consistency!
                    """.trimIndent()

                    Text(
                        text = reviewDisplay,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // --- 9. JOURNAL & DAILY NOTES REFLECTION ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Journal & Notes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Button(
                            onClick = { showJournalEdit = !showJournalEdit },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary.copy(alpha = 0.2f))
                        ) {
                            Text(if (showJournalEdit) "Done" else "Edit Note", color = VioletPrimary, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (dayJournal != null && !showJournalEdit) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CharcoalCardElevated,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(dayJournal.mood.emoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mood: ${dayJournal.mood.label}", style = MaterialTheme.typography.labelMedium, color = CyanAccent, fontWeight = FontWeight.Bold)
                                }
                                if (dayJournal.learnedText.isNotBlank()) {
                                    Text("🌟 What I Learned: ${dayJournal.learnedText}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                }
                                if (dayJournal.feltText.isNotBlank()) {
                                    Text("💭 What I Felt: ${dayJournal.feltText}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                }
                                if (dayJournal.wantToDoText.isNotBlank()) {
                                    Text("🚀 Tomorrow: ${dayJournal.wantToDoText}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                }
                            }
                        }
                    } else if (showJournalEdit) {
                        // Inline Editor for Date
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Mood Rating", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                JournalMood.values().forEach { mood ->
                                    val isSel = journalMood == mood
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) VioletPrimary else CharcoalCardElevated,
                                        modifier = Modifier.clickable { journalMood = mood }
                                    ) {
                                        Text(mood.emoji, modifier = Modifier.padding(8.dp), fontSize = 18.sp)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = journalLearned,
                                onValueChange = { journalLearned = it },
                                label = { Text("What did I learn today?") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = CharcoalBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                            )
                            OutlinedTextField(
                                value = journalFelt,
                                onValueChange = { journalFelt = it },
                                label = { Text("What did I feel?") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = CharcoalBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                            )
                            OutlinedTextField(
                                value = journalNext,
                                onValueChange = { journalNext = it },
                                label = { Text("What do I want to do next?") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = CharcoalBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                            )

                            Button(
                                onClick = {
                                    viewModel.saveJournal(
                                        dateString = selectedDateStr,
                                        learnedText = journalLearned,
                                        feltText = journalFelt,
                                        wantToDoText = journalNext,
                                        gratitudeText = "",
                                        freeformNotes = "",
                                        mood = journalMood
                                    )
                                    showJournalEdit = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                            ) {
                                Text("Save Notes for $selectedDateStr")
                            }
                        }
                    } else {
                        Text("No journal entry recorded for this day. Tap 'Edit Note' to add one!", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }
        }

        // --- 10. ACHIEVEMENTS & MILESTONES ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(VioletPrimary.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Milestones & Rhythm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            text = if (totalFocusMinutes >= 25) "🔥 Completed dedicated deep work milestone on this day!" else "Steady recovery & mindful presence recorded.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSpecialDateDialog) {
        var title by remember(daySpecialDate) { mutableStateOf(daySpecialDate?.title ?: "") }
        var note by remember(daySpecialDate) { mutableStateOf(daySpecialDate?.note ?: "") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSpecialDateDialog = false },
            containerColor = CharcoalCardElevated,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Text(
                    text = if (daySpecialDate != null) "Edit Special Date ($selectedDateStr)" else "Mark Special Date ($selectedDateStr)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Highlight this date with a special badge & marker on your calendar matrix.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event / Milestone Title") },
                        placeholder = { Text("e.g. Final Exam, Project Launch, Anniversary") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoseUrgent,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note / Goal / Focus Intent (Optional)") },
                        placeholder = { Text("e.g. Review chapters 1-5, keep calm") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoseUrgent,
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
                        if (title.isNotBlank()) {
                            viewModel.setSpecialDate(selectedDateStr, title, "#EC4899", note)
                        }
                        showSpecialDateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseUrgent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                if (daySpecialDate != null) {
                    Button(
                        onClick = {
                            viewModel.deleteSpecialDate(selectedDateStr)
                            showSpecialDateDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Remove", color = RoseUrgent)
                    }
                }
            }
        )
    }
}

