package com.example.ui.screens.sessions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.FocusSessionEntity
import com.example.data.model.FocusMode
import com.example.ui.components.GlassCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalSubtle
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class PeriodFilter(val label: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDashboardScreen(
    viewModel: RacerViewModel,
    onNavigateToFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allSessions by viewModel.allFocusSessions.collectAsState()
    val stats by viewModel.stats.collectAsState()

    var selectedPeriod by remember { mutableStateOf(PeriodFilter.TODAY) }
    var selectedModeFilter by remember { mutableStateOf<FocusMode?>(null) }
    var onlyCompletedFilter by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog States
    var showManualLogDialog by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<FocusSessionEntity?>(null) }
    var sessionToEditNote by remember { mutableStateOf<FocusSessionEntity?>(null) }
    var noteEditText by remember { mutableStateOf("") }

    val now = System.currentTimeMillis()

    // Filter sessions by Time Period
    val periodFilteredSessions = remember(allSessions, selectedPeriod) {
        when (selectedPeriod) {
            PeriodFilter.TODAY -> {
                val startOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                allSessions.filter { it.startTime >= startOfDay }
            }
            PeriodFilter.THIS_WEEK -> {
                val startOfWeek = Calendar.getInstance().apply {
                    firstDayOfWeek = Calendar.MONDAY
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                allSessions.filter { it.startTime >= startOfWeek }
            }
            PeriodFilter.THIS_MONTH -> {
                val startOfMonth = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                allSessions.filter { it.startTime >= startOfMonth }
            }
            PeriodFilter.ALL_TIME -> allSessions
        }
    }

    // Secondary filters (Mode, Completed, Search)
    val displaySessions = remember(periodFilteredSessions, selectedModeFilter, onlyCompletedFilter, searchQuery) {
        periodFilteredSessions.filter { session ->
            val matchesMode = selectedModeFilter == null || session.mode == selectedModeFilter
            val matchesCompleted = !onlyCompletedFilter || session.isCompleted
            val matchesSearch = searchQuery.isBlank() ||
                    (session.taskTitle?.contains(searchQuery, ignoreCase = true) == true) ||
                    session.note.contains(searchQuery, ignoreCase = true) ||
                    session.mode.displayName.contains(searchQuery, ignoreCase = true)
            matchesMode && matchesCompleted && matchesSearch
        }
    }

    // Dashboard Metrics for current period
    val totalFocusSeconds = periodFilteredSessions.sumOf { it.actualDurationSeconds.toLong() }
    val completedCount = periodFilteredSessions.count { it.isCompleted }
    val totalCount = periodFilteredSessions.size
    val completionRate = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else 0
    val totalBreakMinutes = periodFilteredSessions.sumOf { it.breakMinutes }
    val avgSessionMinutes = if (totalCount > 0) (totalFocusSeconds / 60 / totalCount).toInt() else 0

    // Group sessions by Day for clean chronological timeline
    val groupedSessions = remember(displaySessions) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        displaySessions.groupBy { session ->
            dateFormat.format(Date(session.startTime))
        }
    }

    val dayHeaderFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now - 86400000L))

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Screen Header & Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Session Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Track focus logs, completion rates & rhythm patterns",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showManualLogDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CharcoalCardElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Log Manual Session",
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (allSessions.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearAllDialog = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(CharcoalCardElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear All History",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Period Selection Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CharcoalCard)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PeriodFilter.entries.forEach { period ->
                    val isSelected = selectedPeriod == period
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) VioletPrimary else Color.Transparent)
                            .clickable { selectedPeriod = period }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = period.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }
            }
        }

        // Dashboard Metric Cards (Hero Overview)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Main Big Card: Focus Time & Radial Progress
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = CharcoalCardElevated,
                    borderColor = VioletPrimary.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "TOTAL FOCUS TIME",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val totalHours = totalFocusSeconds / 3600
                            val totalMinutes = (totalFocusSeconds % 3600) / 60
                            val focusTimeString = if (totalHours > 0) {
                                "${totalHours}h ${totalMinutes}m"
                            } else {
                                "${totalMinutes} min"
                            }
                            Text(
                                text = focusTimeString,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "$totalCount total sessions logged ($completedCount completed)",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        // Circular Gauge for Completion Rate
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Canvas(modifier = Modifier.size(72.dp)) {
                                drawArc(
                                    color = CharcoalBorder,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = CyanAccent,
                                    startAngle = -90f,
                                    sweepAngle = (completionRate / 100f) * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$completionRate%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "SUCCESS",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                // 3-Column Mini Metrics Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricMiniStatCard(
                        title = "Avg Session",
                        value = "${avgSessionMinutes}m",
                        sub = "Per block",
                        icon = Icons.Default.Speed,
                        accentColor = CyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniStatCard(
                        title = "Rest Taken",
                        value = "${totalBreakMinutes}m",
                        sub = "Break buffer",
                        icon = Icons.Default.Schedule,
                        accentColor = VioletPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniStatCard(
                        title = "Streak",
                        value = "${stats.currentStreak}d",
                        sub = "Consecutive",
                        icon = Icons.Default.LocalFireDepartment,
                        accentColor = AmberWarning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Mode Distribution Bar (Visual Breakdown)
        if (periodFilteredSessions.isNotEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = CharcoalCard
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Focus Mode Breakdown",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${periodFilteredSessions.size} sessions",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }

                        // Horizontal segmented bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape)
                                .background(CharcoalBorder)
                        ) {
                            val modes = FocusMode.entries
                            modes.forEach { mode ->
                                val modeCount = periodFilteredSessions.count { it.mode == mode }
                                if (modeCount > 0) {
                                    val weight = modeCount.toFloat() / periodFilteredSessions.size
                                    val color = when (mode) {
                                        FocusMode.POMODORO -> CyanAccent
                                        FocusMode.ADHD -> AmberWarning
                                        FocusMode.CUSTOM -> VioletPrimary
                                        FocusMode.STOPWATCH -> EmeraldSuccess
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(weight)
                                            .fillMaxSize()
                                            .background(color)
                                    )
                                }
                            }
                        }

                        // Legend row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FocusMode.entries.forEach { mode ->
                                val count = periodFilteredSessions.count { it.mode == mode }
                                if (count > 0) {
                                    val color = when (mode) {
                                        FocusMode.POMODORO -> CyanAccent
                                        FocusMode.ADHD -> AmberWarning
                                        FocusMode.CUSTOM -> VioletPrimary
                                        FocusMode.STOPWATCH -> EmeraldSuccess
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Text(
                                            text = "${mode.displayName} ($count)",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Filter Bar & Search
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by task name, mode, or notes...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanAccent) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CharcoalBorder,
                        focusedContainerColor = CharcoalCard,
                        unfocusedContainerColor = CharcoalCard,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                // Filter Chips Scrollable Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedModeFilter == null && !onlyCompletedFilter,
                        onClick = {
                            selectedModeFilter = null
                            onlyCompletedFilter = false
                        },
                        label = { Text("All (${displaySessions.size})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VioletPrimary,
                            selectedLabelColor = TextPrimary,
                            containerColor = CharcoalCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = CharcoalBorder,
                            selectedBorderColor = VioletPrimary,
                            enabled = true,
                            selected = selectedModeFilter == null && !onlyCompletedFilter
                        )
                    )

                    FilterChip(
                        selected = onlyCompletedFilter,
                        onClick = { onlyCompletedFilter = !onlyCompletedFilter },
                        label = { Text("Completed Only", fontSize = 12.sp) },
                        leadingIcon = {
                            if (onlyCompletedFilter) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldSuccess.copy(alpha = 0.25f),
                            selectedLabelColor = EmeraldSuccess,
                            containerColor = CharcoalCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = CharcoalBorder,
                            selectedBorderColor = EmeraldSuccess,
                            enabled = true,
                            selected = onlyCompletedFilter
                        )
                    )

                    FocusMode.entries.forEach { mode ->
                        val isModeSelected = selectedModeFilter == mode
                        FilterChip(
                            selected = isModeSelected,
                            onClick = {
                                selectedModeFilter = if (isModeSelected) null else mode
                            },
                            label = { Text(mode.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent.copy(alpha = 0.25f),
                                selectedLabelColor = CyanAccent,
                                containerColor = CharcoalCard,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = CharcoalBorder,
                                selectedBorderColor = CyanAccent,
                                enabled = true,
                                selected = isModeSelected
                            )
                        )
                    }
                }
            }
        }

        // Section Title: History Logs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Session History Log",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${displaySessions.size} entries",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        // Empty State if no sessions match
        if (displaySessions.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = CharcoalCard
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = CyanAccent.copy(alpha = 0.6f),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No focus sessions recorded yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Start a timer or log a completed study session to build your history.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onNavigateToFocus,
                            colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Focus Session Now")
                        }
                    }
                }
            }
        } else {
            // Grouped Sessions by Date Header
            groupedSessions.forEach { (dateKey, sessionsInDay) ->
                val headerTitle = when (dateKey) {
                    todayStr -> "Today"
                    yesterdayStr -> "Yesterday"
                    else -> try {
                        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey)
                        if (parsed != null) dayHeaderFormat.format(parsed) else dateKey
                    } catch (e: Exception) {
                        dateKey
                    }
                }

                val dayTotalMinutes = sessionsInDay.sumOf { it.actualDurationSeconds } / 60

                item(key = "header_$dateKey") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = headerTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                        Text(
                            text = "$dayTotalMinutes min total · ${sessionsInDay.size} sessions",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                items(sessionsInDay, key = { it.id }) { session ->
                    SessionHistoryCard(
                        session = session,
                        onEditNote = {
                            sessionToEditNote = session
                            noteEditText = session.note
                        },
                        onDelete = { sessionToDelete = session }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // --- DIALOGS ---

    // 1. Manual Session Logger Dialog
    if (showManualLogDialog) {
        ManualSessionDialog(
            onDismiss = { showManualLogDialog = false },
            onSave = { mode, durationMin, taskTitle, note ->
                viewModel.logManualSession(
                    mode = mode,
                    durationMinutes = durationMin,
                    taskTitle = taskTitle,
                    note = note
                )
                showManualLogDialog = false
            }
        )
    }

    // 2. Edit Reflection Note Dialog
    if (sessionToEditNote != null) {
        val s = sessionToEditNote!!
        AlertDialog(
            onDismissRequest = { sessionToEditNote = null },
            title = {
                Text(
                    text = "Session Reflection & Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Add takeaways, concepts learned, or reasons for interruptions:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = noteEditText,
                        onValueChange = { noteEditText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("e.g. Revised Chapter 3 Chemistry, completed 15 problem sets...", color = TextMuted, fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
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
                        viewModel.updateSessionNote(s.id, noteEditText)
                        sessionToEditNote = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                ) {
                    Text("Save Reflection")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToEditNote = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CharcoalCardElevated
        )
    }

    // 3. Delete Session Confirmation Dialog
    if (sessionToDelete != null) {
        val s = sessionToDelete!!
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete Session Entry?", color = TextPrimary) },
            text = {
                Text(
                    "Are you sure you want to remove this ${s.actualDurationSeconds / 60}m ${s.mode.displayName} session from your history?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFocusSession(s.id)
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseUrgent)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CharcoalCardElevated
        )
    }

    // 4. Clear All History Confirmation Dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear All Session History?", color = TextPrimary) },
            text = {
                Text(
                    "This will delete all past focus logs and reset session statistics. This action cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllFocusSessions()
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseUrgent)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CharcoalCardElevated
        )
    }
}

@Composable
fun MetricMiniStatCard(
    title: String,
    value: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        backgroundColor = CharcoalCard
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
            }
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = sub,
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun SessionHistoryCard(
    session: FocusSessionEntity,
    onEditNote: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modeColor = when (session.mode) {
        FocusMode.POMODORO -> CyanAccent
        FocusMode.ADHD -> AmberWarning
        FocusMode.CUSTOM -> VioletPrimary
        FocusMode.STOPWATCH -> EmeraldSuccess
    }

    val actualMinutes = session.actualDurationSeconds / 60
    val plannedMinutes = session.plannedDurationMinutes
    val isFullComplete = session.isCompleted || (actualMinutes >= plannedMinutes && plannedMinutes > 0)

    val timeRangeStr = if (session.clockStartTimeStr.isNotBlank() && session.clockEndTimeStr.isNotBlank()) {
        "${session.clockStartTimeStr} → ${session.clockEndTimeStr}"
    } else {
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        "${format.format(Date(session.startTime))} → ${format.format(Date(session.endTime))}"
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = CharcoalCardElevated,
        borderColor = if (isFullComplete) CharcoalBorder else AmberWarning.copy(alpha = 0.3f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Row 1: Mode Tag + Clock Time + Completion Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = modeColor.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = session.mode.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = modeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (session.cycleNumber > 0 && session.totalSessionsInCycle > 1) {
                        Text(
                            text = "Cycle ${session.cycleNumber} · #${session.sessionInCycle}/${session.totalSessionsInCycle}",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isFullComplete) EmeraldSuccess.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (isFullComplete) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (isFullComplete) EmeraldSuccess else AmberWarning,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isFullComplete) "Completed" else "Shortened",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isFullComplete) EmeraldSuccess else AmberWarning
                        )
                    }
                }
            }

            // Row 2: Duration Details & Time Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$actualMinutes min",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        if (plannedMinutes > 0) {
                            Text(
                                text = "/ ${plannedMinutes}m target",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Text(
                        text = timeRangeStr,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                // Delete & Edit Note Quick Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEditNote,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit reflection note",
                            tint = if (session.note.isNotBlank()) CyanAccent else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete entry",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Attached Task (if linked)
            if (!session.taskTitle.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CharcoalCard,
                    border = BorderStroke(1.dp, CharcoalBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = session.taskTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Metadata Badges (Break taken, Soundpack, Pauses)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (session.breakMinutes > 0) {
                    MetadataPill(
                        icon = Icons.Default.Schedule,
                        text = "${session.breakMinutes}m break",
                        color = VioletPrimary
                    )
                }

                if (session.soundUsed != "none") {
                    MetadataPill(
                        icon = Icons.Default.VolumeUp,
                        text = session.soundUsed.replace("_", " ").replaceFirstChar { it.uppercase() },
                        color = CyanAccent
                    )
                }

                if (session.pauseCount > 0) {
                    val pauseSecs = session.totalPauseDurationSeconds
                    val pauseStr = if (pauseSecs > 0) "${session.pauseCount} pause (${pauseSecs}s)" else "${session.pauseCount} pause"
                    MetadataPill(
                        icon = Icons.Default.Pause,
                        text = pauseStr,
                        color = AmberWarning
                    )
                }
            }

            // Reflection Note View (if exists)
            if (session.note.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CharcoalDark.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, CharcoalSubtle.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditNote() }
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = session.note,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetadataPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = text,
                fontSize = 10.sp,
                color = color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualSessionDialog(
    onDismiss: () -> Unit,
    onSave: (mode: FocusMode, durationMin: Int, taskTitle: String?, note: String) -> Unit
) {
    var selectedMode by remember { mutableStateOf(FocusMode.POMODORO) }
    var durationText by remember { mutableStateOf("25") }
    var taskTitle by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Completed Session",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Record focus or study time done offline or on another device:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                // Mode Selector Chips
                Text("Focus Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FocusMode.entries.forEach { mode ->
                        val isSelected = selectedMode == mode
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) VioletPrimary else CharcoalCard,
                            border = BorderStroke(1.dp, if (isSelected) VioletPrimary else CharcoalBorder),
                            modifier = Modifier.clickable {
                                selectedMode = mode
                                if (durationText.isBlank() || durationText == "25" || durationText == "50" || durationText == "15" || durationText == "90") {
                                    durationText = when (mode) {
                                        FocusMode.POMODORO -> "25"
                                        FocusMode.ADHD -> "15"
                                        FocusMode.CUSTOM -> "30"
                                        FocusMode.STOPWATCH -> "45"
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = mode.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Duration Field (Minutes)
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Duration (Minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CharcoalBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                // Task Name
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Task / Subject Name (Optional)") },
                    placeholder = { Text("e.g. Physics Problem Sets", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CharcoalBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                // Reflection Note
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Notes / Reflections (Optional)") },
                    placeholder = { Text("What did you accomplish?", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CharcoalBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = durationText.toIntOrNull()?.coerceAtLeast(1) ?: 25
                    onSave(selectedMode, duration, taskTitle.takeIf { it.isNotBlank() }, noteText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Save Session", color = CharcoalDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = CharcoalCardElevated
    )
}
