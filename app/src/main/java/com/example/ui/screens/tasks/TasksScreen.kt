package com.example.ui.screens.tasks

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.FocusSessionEntity
import com.example.data.entity.TaskEntity
import com.example.data.model.FocusMode
import com.example.data.model.TaskPriority
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: RacerViewModel,
    onNavigateToFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val activeTasks by viewModel.activeTasks.collectAsState()
    val allFocusSessions by viewModel.allFocusSessions.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: All, 1: Today, 2: Future & Scheduled, 3: Completed
    var isSpotlightMode by remember { mutableStateOf(false) }
    var showAddTaskSheet by remember { mutableStateOf(false) }
    var selectedTaskForDetail by remember { mutableStateOf<TaskEntity?>(null) }

    val categories = listOf("All", "Work", "Study", "Personal", "Creative", "Health", "Events")

    val nowMillis = System.currentTimeMillis()
    val startOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val endOfToday = startOfToday + 24 * 60 * 60 * 1000L - 1

    val filteredTasks = activeTasks.filter { task ->
        val matchesCategory = if (selectedCategory == "All") true else task.category.equals(selectedCategory, ignoreCase = true)
        val matchesTab = when (selectedFilterTab) {
            0 -> !task.isCompleted // All active
            1 -> !task.isCompleted && (task.dueDateMillis == null || task.dueDateMillis in startOfToday..endOfToday) // Today
            2 -> !task.isCompleted && (task.dueDateMillis != null && task.dueDateMillis > endOfToday) // Future / Upcoming
            3 -> task.isCompleted // Completed
            else -> true
        }
        matchesCategory && matchesTab
    }

    val topTask = filteredTasks.firstOrNull { !it.isCompleted }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Title and Spotlight Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tasks & Future Events",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Scheduled milestones, order & cycle focus breakdown",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSpotlightMode) CyanAccent.copy(alpha = 0.2f) else CharcoalCardElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSpotlightMode) CyanAccent else CharcoalBorder),
                        modifier = Modifier.clickable { isSpotlightMode = !isSpotlightMode }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = if (isSpotlightMode) CyanAccent else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSpotlightMode) "Spotlight ON" else "Spotlight",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSpotlightMode) CyanAccent else TextSecondary
                            )
                        }
                    }
                }
            }

            // Filter Tabs: All, Today, Future/Upcoming, Completed
            item {
                val filterTabs = listOf("Active", "Today", "Future Events", "Completed")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalCardElevated)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    filterTabs.forEachIndexed { index, label ->
                        val isSelected = selectedFilterTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) VioletPrimary else Color.Transparent)
                                .clickable { selectedFilterTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextMuted
                            )
                        }
                    }
                }
            }

            // Category Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CyanAccent.copy(alpha = 0.2f) else CharcoalCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyanAccent else CharcoalBorder),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) CyanAccent else TextSecondary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // SPOTLIGHT MODE: Single task display for ADHD focus
            if (isSpotlightMode) {
                item {
                    if (topTask != null) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = CyanAccent
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CURRENT SPOTLIGHT FOCUS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanAccent
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = getPriorityColor(topTask.priority).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = topTask.priority.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = getPriorityColor(topTask.priority),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = topTask.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                if (topTask.description.isNotBlank()) {
                                    Text(
                                        text = topTask.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
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
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Start Focus on This Task", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "All tasks completed! Enjoy the quiet or add a new future goal.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            } else {
                // Regular Numbered List View (1. 2. 3... with Move Up / Down controls)
                itemsIndexed(filteredTasks, key = { _, task -> task.id }) { index, task ->
                    TaskCard(
                        task = task,
                        index = index + 1,
                        canMoveUp = index > 0,
                        canMoveDown = index < filteredTasks.size - 1,
                        onMoveUp = { viewModel.reorderTask(task, moveUp = true, currentList = filteredTasks) },
                        onMoveDown = { viewModel.reorderTask(task, moveUp = false, currentList = filteredTasks) },
                        onCardClick = { selectedTaskForDetail = task },
                        onToggle = { viewModel.toggleTask(task.id) },
                        onFocus = {
                            viewModel.startFocus(
                                mode = FocusMode.ADHD,
                                durationMinutes = task.estimatedMinutes,
                                taskId = task.id,
                                taskTitle = task.title
                            )
                            onNavigateToFocus()
                        },
                        onDelete = { viewModel.deleteTask(task.id) }
                    )
                }

                if (filteredTasks.isEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "No tasks or events found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Tap '+' below to schedule a future task or event with notifications.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Add Task Floating Action Button
        FloatingActionButton(
            onClick = { showAddTaskSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = VioletPrimary,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task or Event")
        }

        if (showAddTaskSheet) {
            AddTaskBottomSheet(
                onDismiss = { showAddTaskSheet = false },
                onAddTask = { title, desc, priority, category, estMins, subtasksJson, dueDateMillis, dueTimeString, reminderEnabled, reminderLeadMins ->
                    viewModel.addTask(
                        title = title,
                        description = desc,
                        priority = priority,
                        dueDateMillis = dueDateMillis,
                        dueTimeString = dueTimeString,
                        reminderEnabled = reminderEnabled,
                        reminderLeadMinutes = reminderLeadMins,
                        category = category,
                        estimatedMinutes = estMins,
                        subtasksJson = subtasksJson
                    )
                    showAddTaskSheet = false
                },
                onGenerateMicroSteps = { goal, callback ->
                    coroutineScope.launch {
                        val plan = viewModel.lumaService.generateCoachPlan(goal)
                        callback(plan)
                    }
                },
                onTestReminder = {
                    viewModel.testTaskReminder("Upcoming Event Notification Test")
                }
            )
        }

        // Task Detail & Cycle / Session History Sheet
        if (selectedTaskForDetail != null) {
            val currentDetailTask = selectedTaskForDetail!!
            val taskSessions = allFocusSessions.filter { it.taskId == currentDetailTask.id || it.taskTitle.equals(currentDetailTask.title, ignoreCase = true) }

            TaskDetailCyclesSheet(
                task = currentDetailTask,
                sessions = taskSessions,
                onDismiss = { selectedTaskForDetail = null },
                onStartFocusForTask = {
                    selectedTaskForDetail = null
                    viewModel.startFocus(
                        mode = FocusMode.POMODORO,
                        durationMinutes = currentDetailTask.estimatedMinutes,
                        taskId = currentDetailTask.id,
                        taskTitle = currentDetailTask.title
                    )
                    onNavigateToFocus()
                }
            )
        }
    }
}

@Composable
fun TaskCard(
    task: TaskEntity,
    index: Int = 1,
    canMoveUp: Boolean = false,
    canMoveDown: Boolean = false,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    onCardClick: () -> Unit = {},
    onToggle: () -> Unit,
    onFocus: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val formattedDueDate = remember(task.dueDateMillis, task.dueTimeString) {
        if (task.dueDateMillis != null) {
            val d = dateFormat.format(Date(task.dueDateMillis))
            val t = task.dueTimeString?.let { " at $it" } ?: ""
            "$d$t"
        } else null
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        borderColor = if (task.isCompleted) CharcoalBorder else getPriorityColor(task.priority).copy(alpha = 0.35f)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Task Number Badge (1. 2. 3...)
                Surface(
                    shape = CircleShape,
                    color = CharcoalCardElevated,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$index",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle",
                        tint = if (task.isCompleted) EmeraldSuccess else CyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isCompleted) TextDisabled else TextPrimary,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                }

                // Reorder controls (Up & Down arrows)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = canMoveUp,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move Up",
                            tint = if (canMoveUp) CyanAccent else TextDisabled,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = canMoveDown,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move Down",
                            tint = if (canMoveDown) CyanAccent else TextDisabled,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                if (!task.isCompleted) {
                    IconButton(
                        onClick = onFocus,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Focus",
                            tint = CyanAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Chips Row: Date & Reminder Badge + Category + Priority + Duration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 38.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (formattedDueDate != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CyanAccent.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (task.reminderEnabled) Icons.Default.NotificationsActive else Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formattedDueDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CharcoalCardElevated
                ) {
                    Text(
                        text = task.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getPriorityColor(task.priority).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = task.priority.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = getPriorityColor(task.priority),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "${task.estimatedMinutes}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailCyclesSheet(
    task: TaskEntity,
    sessions: List<FocusSessionEntity>,
    onDismiss: () -> Unit,
    onStartFocusForTask: () -> Unit
) {
    val totalFocusMins = sessions.filter { it.sessionType == "FOCUS" }.sumOf { it.actualDurationSeconds } / 60
    val completedSessions = sessions.count { it.sessionType == "FOCUS" && it.isCompleted }
    val brokenSessions = sessions.count { it.sessionType == "FOCUS" && !it.isCompleted }
    val shortBreaks = sessions.count { it.sessionType == "SHORT_BREAK" }
    val longBreaks = sessions.count { it.sessionType == "LONG_BREAK" }
    val cyclesCompleted = if (sessions.isNotEmpty()) sessions.maxOfOrNull { it.cycleNumber } ?: 0 else 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CharcoalCardElevated,
        contentColor = TextPrimary
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Category: ${task.category} • Priority: ${task.priority.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }
            }

            // Overview stats grid
            item {
                Text("Focus Cycles & Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CyanAccent)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$totalFocusMins min", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Total Focused", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }

                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$completedSessions done", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                            Text("Sessions Finished", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }

                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FreeBreakfast, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${shortBreaks + longBreaks}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AmberWarning)
                            Text("Breaks Taken", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }
            }

            // Detailed Cycle & Session List
            item {
                Text("Session History for this Task", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            }

            if (sessions.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No focus sessions recorded yet for this task.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Start a Pomodoro cycle to track sessions and breaks!", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                        }
                    }
                }
            } else {
                items(sessions) { s ->
                    val typeColor = when (s.sessionType) {
                        "SHORT_BREAK" -> AmberWarning
                        "LONG_BREAK" -> CyanAccent
                        else -> if (s.isCompleted) EmeraldSuccess else RoseUrgent
                    }
                    val typeLabel = when (s.sessionType) {
                        "SHORT_BREAK" -> "Short Break (${s.breakMinutes}m)"
                        "LONG_BREAK" -> "Long Break (${s.breakMinutes}m)"
                        else -> "Focus Session (${s.actualDurationSeconds / 60}m / ${s.plannedDurationMinutes}m)"
                    }

                    GlassCard(modifier = Modifier.fillMaxWidth(), borderColor = typeColor.copy(alpha = 0.3f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = typeColor.copy(alpha = 0.2f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (s.sessionType == "FOCUS") (if (s.isCompleted) Icons.Default.Check else Icons.Default.HourglassBottom) else Icons.Default.FreeBreakfast,
                                            contentDescription = null,
                                            tint = typeColor,
                                            modifier = Modifier.size(16.dp)
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
                                        text = typeLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = typeColor
                                    )
                                }
                            }

                            if (s.clockStartTimeStr.isNotBlank()) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${s.clockStartTimeStr} - ${s.clockEndTimeStr}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                    if (!s.isCompleted && s.sessionType == "FOCUS") {
                                        Text(
                                            text = "Broken / Incomplete",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = RoseUrgent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onStartFocusForTask,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Pomodoro Focus Cycle", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(
    onDismiss: () -> Unit,
    onAddTask: (
        title: String,
        desc: String,
        priority: TaskPriority,
        category: String,
        estMins: Int,
        subtasksJson: String,
        dueDateMillis: Long?,
        dueTimeString: String?,
        reminderEnabled: Boolean,
        reminderLeadMins: Int
    ) -> Unit,
    onGenerateMicroSteps: (String, (String) -> Unit) -> Unit,
    onTestReminder: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var category by remember { mutableStateOf("Work") }
    var estimatedMinutes by remember { mutableIntStateOf(25) }
    var isGeneratingMicroSteps by remember { mutableStateOf(false) }

    // Future Scheduling State
    var isFutureScheduled by remember { mutableStateOf(false) }
    var isCustomExactDate by remember { mutableStateOf(false) }
    var scheduledDaysOffset by remember { mutableIntStateOf(0) } // 0: Today, 1: Tomorrow, 2: 2 days, 7: Next week
    
    // Exact Date components
    val nowCal = remember { Calendar.getInstance() }
    var exactYear by remember { mutableIntStateOf(nowCal.get(Calendar.YEAR)) }
    var exactMonth by remember { mutableIntStateOf(nowCal.get(Calendar.MONTH) + 1) } // 1..12
    var exactDay by remember { mutableIntStateOf(nowCal.get(Calendar.DAY_OF_MONTH)) }
    var scheduledHour by remember { mutableIntStateOf(14) } // 2:00 PM
    var scheduledMinute by remember { mutableIntStateOf(0) }
    var reminderEnabled by remember { mutableStateOf(true) }
    var reminderLeadMinutes by remember { mutableIntStateOf(15) } // 15 mins before

    val categories = listOf("Work", "Study", "Personal", "Creative", "Health", "Events", "Meeting")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CharcoalCardElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "New Milestone, Task or Event",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task or Event Title") },
                placeholder = { Text("e.g. Design Presentation, Doctor Appt, Gym Session") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = CharcoalBorder
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notes / Micro-steps") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = CharcoalBorder
                )
            )

            // AI Micro-step Breakdown Button
            if (title.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = VioletPrimary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable {
                        isGeneratingMicroSteps = true
                        onGenerateMicroSteps(title) { result ->
                            description = result
                            isGeneratingMicroSteps = false
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = VioletPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isGeneratingMicroSteps) "Breaking down goal..." else "✨ Break down into micro-steps with Luma AI",
                            style = MaterialTheme.typography.bodySmall,
                            color = VioletPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Scheduling & Notification Toggle Card with EXACT DATE & TIME
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = CyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Schedule Exact Date & Reminder Alert", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Set exact calendar date & reminder", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }
                        Switch(
                            checked = isFutureScheduled,
                            onCheckedChange = { isFutureScheduled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = VioletPrimary)
                        )
                    }

                    if (isFutureScheduled) {
                        // Quick Date Chips + Exact Custom Date Toggle
                        Text("Select Date Mode", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val dateOptions = listOf("Today" to 0, "Tomorrow" to 1, "In 2 Days" to 2, "In 3 Days" to 3, "Next Week" to 7)
                            dateOptions.forEach { (label, offset) ->
                                val isSelected = !isCustomExactDate && scheduledDaysOffset == offset
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) VioletPrimary else CharcoalCard,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyanAccent else CharcoalBorder),
                                    modifier = Modifier.clickable {
                                        isCustomExactDate = false
                                        scheduledDaysOffset = offset
                                    }
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            // Custom Exact Date Chip
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCustomExactDate) CyanAccent.copy(alpha = 0.25f) else CharcoalCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isCustomExactDate) CyanAccent else CharcoalBorder),
                                modifier = Modifier.clickable { isCustomExactDate = true }
                            ) {
                                Text(
                                    text = "📅 Exact Date",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCustomExactDate) CyanAccent else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Exact Date Inputs (Year, Month, Day) when Custom Date is selected
                        if (isCustomExactDate) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = exactDay.toString(),
                                    onValueChange = { exactDay = (it.toIntOrNull() ?: 1).coerceIn(1, 31) },
                                    label = { Text("Day (1-31)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = CharcoalBorder)
                                )
                                OutlinedTextField(
                                    value = exactMonth.toString(),
                                    onValueChange = { exactMonth = (it.toIntOrNull() ?: 1).coerceIn(1, 12) },
                                    label = { Text("Month (1-12)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = CharcoalBorder)
                                )
                                OutlinedTextField(
                                    value = exactYear.toString(),
                                    onValueChange = { exactYear = it.toIntOrNull() ?: 2026 },
                                    label = { Text("Year") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = CharcoalBorder)
                                )
                            }
                        }

                        // Time Selector Row with Exact Hour & Minute Inputs
                        Text("Scheduled Time (Exact Clock Time)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = scheduledHour.toString(),
                                onValueChange = { scheduledHour = (it.toIntOrNull() ?: 0).coerceIn(0, 23) },
                                label = { Text("Hour (0-23)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = CharcoalBorder)
                            )
                            Text(":", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            OutlinedTextField(
                                value = String.format(Locale.getDefault(), "%02d", scheduledMinute),
                                onValueChange = { scheduledMinute = (it.toIntOrNull() ?: 0).coerceIn(0, 59) },
                                label = { Text("Min (0-59)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = CharcoalBorder)
                            )
                        }

                        // Quick Time Presets
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val timeOptions = listOf(
                                "9:00 AM" to (9 to 0),
                                "12:00 PM" to (12 to 0),
                                "2:30 PM" to (14 to 30),
                                "5:00 PM" to (17 to 0),
                                "8:00 PM" to (20 to 0),
                                "10:30 PM" to (22 to 30)
                            )
                            timeOptions.forEach { (label, time) ->
                                val isSelected = (scheduledHour == time.first && scheduledMinute == time.second)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) CyanAccent.copy(alpha = 0.25f) else CharcoalCard,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyanAccent else CharcoalBorder),
                                    modifier = Modifier.clickable {
                                        scheduledHour = time.first
                                        scheduledMinute = time.second
                                    }
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) CyanAccent else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Reminder Lead Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Reminder Alert Lead", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("At time" to 0, "15m before" to 15, "1h before" to 60).forEach { (lbl, mins) ->
                                    val isSelected = reminderLeadMinutes == mins
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) VioletPrimary else CharcoalCard,
                                        modifier = Modifier.clickable { reminderLeadMinutes = mins }
                                    ) {
                                        Text(
                                            text = lbl,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.White else TextMuted,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Priority Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskPriority.values().forEach { prio ->
                    val isSelected = priority == prio
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) getPriorityColor(prio) else CharcoalCard,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { priority = prio }
                    ) {
                        Text(
                            text = prio.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else TextSecondary,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Category & Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CharcoalBorder
                    )
                )

                OutlinedTextField(
                    value = estimatedMinutes.toString(),
                    onValueChange = { estimatedMinutes = it.toIntOrNull() ?: 25 },
                    label = { Text("Est. Minutes") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CharcoalBorder
                    )
                )
            }

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val dueMillis = if (isFutureScheduled) {
                            if (isCustomExactDate) {
                                Calendar.getInstance().apply {
                                    set(Calendar.YEAR, exactYear)
                                    set(Calendar.MONTH, exactMonth - 1)
                                    set(Calendar.DAY_OF_MONTH, exactDay)
                                    set(Calendar.HOUR_OF_DAY, scheduledHour)
                                    set(Calendar.MINUTE, scheduledMinute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                            } else {
                                Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_YEAR, scheduledDaysOffset)
                                    set(Calendar.HOUR_OF_DAY, scheduledHour)
                                    set(Calendar.MINUTE, scheduledMinute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                            }
                        } else null

                        val dueTimeStr = if (isFutureScheduled) {
                            String.format(Locale.getDefault(), "%02d:%02d", scheduledHour, scheduledMinute)
                        } else null

                        onAddTask(
                            title,
                            description,
                            priority,
                            category,
                            estimatedMinutes,
                            "[]",
                            dueMillis,
                            dueTimeStr,
                            isFutureScheduled && reminderEnabled,
                            reminderLeadMinutes
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                enabled = title.isNotBlank()
            ) {
                Text(if (isFutureScheduled) "Schedule Future Task & Set Alert" else "Create Task", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun getPriorityColor(priority: TaskPriority): Color = when (priority) {
    TaskPriority.URGENT -> RoseUrgent
    TaskPriority.HIGH -> AmberWarning
    TaskPriority.MEDIUM -> CyanAccent
    TaskPriority.LOW -> TextSecondary
}
