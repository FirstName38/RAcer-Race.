package com.example.ui.screens.insights

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.FocusSessionEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.RoseUrgent
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.RacerViewModel
import com.example.usage.AppUsageHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: RacerViewModel,
    onNavigateToAI: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.stats.collectAsState()
    val usageList by viewModel.dailyUsageList.collectAsState()
    val blockedApps by viewModel.blockedApps.collectAsState()
    val allSessions by viewModel.allFocusSessions.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedPeriodFilter by remember { mutableStateOf<String?>(null) }
    var behavioralReport by remember { mutableStateOf<String?>(null) }
    var isAnalyzingBehavior by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Column {
                Text(
                    text = "Insights & Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Understand your biological rhythms, tap cards for session breakdowns",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // 4-Period Duration Cards Grid (Interactive: Tap to inspect cycles & clock times)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricMiniCard(
                        title = "Today",
                        value = "${stats.todaySeconds / 60}m",
                        sub = "${stats.completedSessions} completed (Tap details)",
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPeriodFilter = "TODAY" },
                        accentColor = CyanAccent
                    )
                    MetricMiniCard(
                        title = "This Week",
                        value = "${stats.weekSeconds / 60}m",
                        sub = "7-Day Breakdown",
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPeriodFilter = "WEEK" },
                        accentColor = VioletPrimary
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricMiniCard(
                        title = "This Month",
                        value = "${stats.monthSeconds / 60}m",
                        sub = "30-Day History",
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPeriodFilter = "MONTH" },
                        accentColor = EmeraldSuccess
                    )
                    MetricMiniCard(
                        title = "All Time",
                        value = "${stats.totalSeconds / 60}m",
                        sub = "${stats.totalSessions} Total Sessions",
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPeriodFilter = "ALL" },
                        accentColor = AmberWarning
                    )
                }
            }
        }

        // Peak Flow Window Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Peak Energy Window", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyanAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "ADHD Pattern",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Best Focus Hour", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                text = "${stats.bestHourOfDay}:00",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Column {
                            Text("Strongest Day", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                text = stats.bestDayOfWeek,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Column {
                            Text("Completion Rate", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                text = "${stats.completionRate.toInt()}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                    }
                }
            }
        }

        // Deep Behavioral Analysis by Luma AI
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Luma Behavioral & Neuro-Rhythm Audit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }

                    Text(
                        text = "Luma analyzes your telemetry: completed vs broken sessions, break skipping tendencies, hyperfocus traps, and compulsive app reopens.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Button(
                        onClick = {
                            isAnalyzingBehavior = true
                            scope.launch {
                                val result = viewModel.lumaService.generateBehavioralPatternAnalysis()
                                behavioralReport = result
                                isAnalyzingBehavior = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isAnalyzingBehavior
                    ) {
                        if (isAnalyzingBehavior) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing Biological Patterns...", color = Color.White)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (behavioralReport != null) "Refresh Behavioral Audit" else "Run Deep Behavioral Audit", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (behavioralReport != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CharcoalCardElevated,
                            border = BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = behavioralReport!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Hourly Focus Distribution Visual Bar Chart
        if (stats.hourDistribution.isNotEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "24-Hour Focus Distribution",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            val maxVal = (stats.hourDistribution.maxOrNull() ?: 1).coerceAtLeast(1)
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val barWidth = size.width / 24f
                                for (i in 0 until 24) {
                                    val barHeight = (stats.hourDistribution[i].toFloat() / maxVal) * size.height
                                    val left = i * barWidth + 2f
                                    val top = size.height - barHeight
                                    val isPeak = i == stats.bestHourOfDay

                                    drawRoundRect(
                                        color = if (isPeak) CyanAccent else VioletPrimary.copy(alpha = 0.6f),
                                        topLeft = Offset(left, top),
                                        size = Size(barWidth - 4f, barHeight.coerceAtLeast(4f)),
                                        cornerRadius = CornerRadius(4f, 4f)
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("12 AM", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("6 AM", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("12 PM", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("6 PM", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("11 PM", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }
            }
        }

        // Digital Detox & App Distraction Blocker Section
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = RoseUrgent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("App Usage & Distraction Blocker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }

                    Text(
                        text = "Toggle apps you want RAcer to block during active focus sessions:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    if (usageList.isNotEmpty()) {
                        usageList.take(6).forEach { app ->
                            val isBlocked = blockedApps.any { it.packageName == app.packageName }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CharcoalCardElevated)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${app.totalTimeInForegroundSeconds / 60}m spent today • ${app.launchCount} reopens",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }

                                Switch(
                                    checked = isBlocked,
                                    onCheckedChange = { viewModel.setAppBlocked(app.packageName, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = RoseUrgent,
                                        checkedTrackColor = RoseUrgent.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "To track real app screen time & block social apps during focus, enable 'Usage Access' in system settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Luma AI Weekly Retrospective Trigger
        item {
            Button(
                onClick = onNavigateToAI,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chat with Luma AI & Open Coach", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Interactive Period History Breakdown Sheet
    if (selectedPeriodFilter != null) {
        val filter = selectedPeriodFilter!!
        val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000)
        val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
        val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

        val filteredSessions = remember(filter, allSessions) {
            when (filter) {
                "TODAY" -> allSessions.filter { dateFormat.format(Date(it.startTime)) == todayDateStr }
                "WEEK" -> allSessions.filter { it.startTime >= sevenDaysAgo }
                "MONTH" -> allSessions.filter { it.startTime >= thirtyDaysAgo }
                else -> allSessions
            }
        }

        PeriodDetailBreakdownSheet(
            title = when (filter) {
                "TODAY" -> "Today's Session & Cycle Breakdown"
                "WEEK" -> "This Week's Session & Cycle Breakdown"
                "MONTH" -> "This Month's Session & Cycle Breakdown"
                else -> "All-Time Session & Cycle History"
            },
            sessions = filteredSessions,
            onDismiss = { selectedPeriodFilter = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodDetailBreakdownSheet(
    title: String,
    sessions: List<FocusSessionEntity>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CharcoalDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            val completedFocus = sessions.count { it.isCompleted && it.sessionType == "FOCUS" }
            val brokenFocus = sessions.count { !it.isCompleted && it.sessionType == "FOCUS" }
            val breakCount = sessions.count { it.sessionType.contains("BREAK") }
            val totalSeconds = sessions.filter { it.sessionType == "FOCUS" }.sumOf { it.actualDurationSeconds }

            // Summary Stats Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalCardElevated)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Focus", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("${totalSeconds / 60}m", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CyanAccent)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Completed", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("$completedFocus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Broken/Early End", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("$brokenFocus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RoseUrgent)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Breaks", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("$breakCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PinkAccent)
                }
            }

            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No sessions recorded in this period yet.", color = TextMuted)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sessions) { s ->
                        val isFocus = s.sessionType == "FOCUS"
                        val typeLabel = when (s.sessionType) {
                            "FOCUS" -> if (s.isCompleted) "Completed Focus" else "Interrupted / Broken Focus"
                            "SHORT_BREAK" -> "Short Break"
                            "LONG_BREAK" -> "Long Break"
                            else -> s.sessionType
                        }
                        val typeColor = when {
                            !isFocus -> PinkAccent
                            s.isCompleted -> EmeraldSuccess
                            else -> RoseUrgent
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CharcoalCard,
                            border = BorderStroke(1.dp, typeColor.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = typeColor.copy(alpha = 0.2f),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                if (isFocus) (if (s.isCompleted) Icons.Default.Check else Icons.Default.Warning) else Icons.Default.FreeBreakfast,
                                                contentDescription = null,
                                                tint = typeColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Cycle #${s.cycleNumber} • Session ${s.sessionInCycle}/${s.totalSessionsInCycle}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "$typeLabel (${s.actualDurationSeconds / 60}m logged)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = typeColor
                                        )
                                        if (!s.taskTitle.isNullOrBlank()) {
                                            Text(
                                                text = "Task: ${s.taskTitle}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = dateFormat.format(Date(s.startTime)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                    if (s.clockStartTimeStr.isNotBlank()) {
                                        Text(
                                            text = "${s.clockStartTimeStr} - ${s.clockEndTimeStr}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MetricMiniCard(
    title: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier,
    accentColor: Color = CyanAccent
) {
    GlassCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = accentColor)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}

