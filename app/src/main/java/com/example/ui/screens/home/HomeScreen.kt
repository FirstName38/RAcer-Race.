package com.example.ui.screens.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.WeeklyPlanEntity
import com.example.data.model.FocusMode
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingOrb
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: RacerViewModel,
    onNavigateToFocus: () -> Unit,
    onNavigateToFocusRoom: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToAI: () -> Unit,
    onNavigateToSessions: () -> Unit,
    onNavigateToRoutine: () -> Unit,
    onNavigateToWeeklyPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.stats.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val tasks by viewModel.activeTasks.collectAsState()
    val habits by viewModel.activeHabits.collectAsState()
    val habitCompletions by viewModel.allHabitCompletions.collectAsState()
    val dailyRoutines by viewModel.allDailyRoutines.collectAsState()
    val weeklyPlans by viewModel.allWeeklyPlans.collectAsState()
    val weekStartDay by viewModel.weekStartDay.collectAsState()
    val todayDateStr = viewModel.repository.getTodayDateString()

    val greeting = rememberGreeting()
    val topTask = tasks.firstOrNull { !it.isCompleted }

    // Routine metrics
    val routineTotal = dailyRoutines.size
    val routineCompleted = dailyRoutines.count { it.isCompletedToday }
    val routineRate = if (routineTotal > 0) (routineCompleted.toFloat() / routineTotal * 100f) else 0f
    val routineAbove70 = routineRate >= 70f
    val routineColor = if (routineAbove70) EmeraldSuccess else RoseUrgent

    val week1BeginsRule by viewModel.week1BeginsRule.collectAsState()
    val coins by viewModel.coinBalance.collectAsState()
    val googleEmail by viewModel.googleAccountEmail.collectAsState()
    val userDisplayName = remember(googleEmail) {
        val namePart = googleEmail.substringBefore("@")
        if (namePart.isNotBlank()) namePart.replaceFirstChar { it.uppercase() } else "Flow"
    }

    // Dynamic Week & Date calculation using user settings
    val weekInfo = remember(weekStartDay, week1BeginsRule) {
        com.example.util.WeekCalculationHelper.calculateWeekInfo(
            weekStartDay = weekStartDay,
            week1BeginsRule = week1BeginsRule
        )
    }

    val currentWeekNum = weekInfo.currentWeekNumber
    val weeksRemaining = weekInfo.weeksRemainingInYear
    val thisWeekPlans = weeklyPlans.filter { plan: WeeklyPlanEntity -> plan.weekNumber == currentWeekNum }
    val weeklyTotal = thisWeekPlans.size
    val weeklyDone = thisWeekPlans.count { it.isCompleted }
    val weeklyRate = if (weeklyTotal > 0) (weeklyDone.toFloat() / weeklyTotal * 100f) else 0f
    val weeklyAbove70 = weeklyRate >= 70f
    val weeklyColor = if (weeklyTotal == 0) CyanAccent else if (weeklyAbove70) EmeraldSuccess else RoseUrgent

    var liveSecondsTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        while (true) {
            liveSecondsTime = sdf.format(Date())
            delay(1000)
        }
    }

    // Ambient pulse animation for active elements
    val infiniteTransition = rememberInfiniteTransition(label = "home_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // 1. Dynamic Identity Header (Matches Screenshot 6)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // UPPERCASE DAY, DATE · WEEK NUMBER (Screenshot 6: MONDAY 26 OCTOBER · WEEK 43)
                    Text(
                        text = weekInfo.dayDateUpperStr,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA78BFA),
                        letterSpacing = 1.1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$greeting, $userDisplayName.",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    // NO OF WEEKS LEFT IN YEAR (Screenshot 6: 10 weeks left this year. One block at a time.)
                    Text(
                        text = weekInfo.subtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Coins Balance Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFBBF24).copy(alpha = 0.18f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🪙", fontSize = 13.sp)
                            Text(
                                text = "$coins",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                        }
                    }

                    // Fire Streak Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.18f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${stats.currentStreak}d",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }
        }

        // 1.5 Live Precise Clock with Seconds (Request 2: "at home time with second add also")
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF141226).copy(alpha = 0.85f),
                borderColor = Color(0xFF8B5CF6).copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CyanAccent.copy(alpha = 0.15f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Clock",
                                tint = CyanAccent,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "LOCAL PRECISION TIME",
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 1.sp,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = liveSecondsTime.ifEmpty { "12:00:00 PM" },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF8B5CF6).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "LIVE SECONDS ⏱️",
                            color = Color(0xFFA78BFA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // 2. Active Session Radar / Quick Launchpad
        item {
            if (timerState.isRunning) {
                // Pulsating active radar banner
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = EmeraldSuccess.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = pulseGlow)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToFocus() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                val mins = timerState.currentSeconds / 60
                                val secs = timerState.currentSeconds % 60
                                Text(
                                    text = "Deep Focus Active: %02d:%02d".format(mins, secs),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess
                                )
                                Text(
                                    text = if (!timerState.activeTaskTitle.isNullOrBlank()) "Working on: ${timerState.activeTaskTitle}" else "Dopamine shield engaged",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }

                        Button(
                            onClick = onNavigateToFocus,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.Black)
                        ) {
                            Text("Resume", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // High-Tech Hero Focus Nexus Launchpad
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = CharcoalCardElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Focus Velocity Nexus",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Launch a calm session with one tap",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }

                            val todayMins = stats.todaySeconds / 60
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CyanAccent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${todayMins}m / 120m goal",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Daily focus goal progress bar
                        val dailyGoalProgress = ((stats.todaySeconds / 60f) / 120f).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { dailyGoalProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = CyanAccent,
                            trackColor = CharcoalBorder
                        )

                        // 3 Quick Preset Sprints
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // ADHD 15m Gentle
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = VioletPrimary.copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.startFocus(mode = FocusMode.ADHD, durationMinutes = 15)
                                        onNavigateToFocus()
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("⚡ Gentle 15m", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VioletPrimary)
                                    Text("Zero Pressure", fontSize = 9.sp, color = TextMuted)
                                }
                            }

                            // Classic 25m Pomodoro
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = CyanAccent.copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.startFocus(mode = FocusMode.POMODORO, durationMinutes = 25)
                                        onNavigateToFocus()
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("⏱️ Sprint 25m", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CyanAccent)
                                    Text("5m Break Cycle", fontSize = 9.sp, color = TextMuted)
                                }
                            }

                            // Deep Work 50m
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.startFocus(mode = FocusMode.CUSTOM, durationMinutes = 50)
                                        onNavigateToFocus()
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🧠 Deep 50m", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                    Text("High Output", fontSize = 9.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Top Actionable Task (ADHD Single-Task Spotlight)
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CharcoalCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTasks() }
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CyanAccent)
                            )
                            Text(
                                text = "Next Priority Target",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = VioletPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "ADHD Spotlight",
                                style = MaterialTheme.typography.labelSmall,
                                color = VioletPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (topTask != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = topTask.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Estimated ${topTask.estimatedMinutes}m • ${topTask.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.startFocus(
                                        mode = FocusMode.ADHD,
                                        durationMinutes = topTask.estimatedMinutes,
                                        taskId = topTask.id,
                                        taskTitle = topTask.title
                                    )
                                    onNavigateToFocus()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Focus Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Text(
                            text = "🎉 All cleared! Tap to add your next small milestone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 4. Combined Cognitive Rhythms (Routine & Weekly Objectives)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Routine Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CharcoalCardElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, routineColor.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToRoutine() }
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = routineColor, modifier = Modifier.size(18.dp))
                            Text(
                                text = "${routineRate.toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = routineColor
                            )
                        }

                        Text("Daily Routine", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("$routineCompleted of $routineTotal done", style = MaterialTheme.typography.bodySmall, color = TextMuted, fontSize = 11.sp)

                        LinearProgressIndicator(
                            progress = { if (routineTotal > 0) (routineCompleted.toFloat() / routineTotal).coerceIn(0f, 1f) else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = routineColor,
                            trackColor = CharcoalBorder
                        )
                    }
                }

                // Weekly Plan Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CharcoalCardElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, weeklyColor.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToWeeklyPlan() }
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = weeklyColor, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (weeklyTotal == 0) "Plan" else "${weeklyRate.toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = weeklyColor
                            )
                        }

                        Text("Week $currentWeekNum Goals", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("$weeklyDone of $weeklyTotal completed", style = MaterialTheme.typography.bodySmall, color = TextMuted, fontSize = 11.sp)

                        LinearProgressIndicator(
                            progress = { if (weeklyTotal > 0) (weeklyDone.toFloat() / weeklyTotal).coerceIn(0f, 1f) else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = weeklyColor,
                            trackColor = CharcoalBorder
                        )
                    }
                }
            }
        }

        // 5. Quick Command Bento Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tile 1: Focus Room
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CharcoalCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToFocusRoom() }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(VioletPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("Focus Room", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Co-working", style = MaterialTheme.typography.bodySmall, color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }

                    // Tile 2: Calendar & Deadlines
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CharcoalCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToCalendar() }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CyanAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("Deadlines", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Exam matrix", style = MaterialTheme.typography.bodySmall, color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tile 3: Habit Tracker
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CharcoalCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToHabits() }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EmeraldSuccess.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Repeat, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("Habits", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("${habits.size} active", style = MaterialTheme.typography.bodySmall, color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }

                    // Tile 4: Luma AI Companion
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CharcoalCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToAI() }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFF2A85).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFF2A85), modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("Luma AI", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Ask anything", style = MaterialTheme.typography.bodySmall, color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // 6. Chronological Session Logs Shortcut
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CharcoalCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSessions() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Column {
                            Text("Session Telemetry History", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("${stats.completedSessions} completed sessions recorded", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                    Text("View →", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun rememberGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good Morning ☀️"
        in 12..16 -> "Good Afternoon 🌿"
        in 17..21 -> "Good Evening 🌆"
        else -> "Night Focus 🌙"
    }
}
