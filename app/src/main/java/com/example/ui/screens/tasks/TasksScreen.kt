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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import com.example.util.WeekCalculationHelper
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
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
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var viewingWeekNumber by remember { mutableStateOf<Int?>(null) }

    val customTaskCategories by viewModel.customTaskCategories.collectAsState()
    val allWeeklyPlans by viewModel.allWeeklyPlans.collectAsState()
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val categories = remember(customTaskCategories) {
        listOf("All", "Work", "Study", "Personal", "Creative", "Health", "Events") + customTaskCategories
    }

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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        val isCustom = cat in customTaskCategories
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CyanAccent.copy(alpha = 0.2f) else CharcoalCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyanAccent else CharcoalBorder),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) CyanAccent else TextSecondary
                                )
                                if (isCustom) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete Category",
                                        tint = TextMuted,
                                        modifier = Modifier.size(13.dp).clickable {
                                            if (selectedCategory == cat) selectedCategory = "All"
                                            viewModel.deleteCustomTaskCategory(cat)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Add Custom Category Chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VioletPrimary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.clickable { showAddCategoryDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Custom Category", tint = VioletPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Custom", style = MaterialTheme.typography.labelSmall, color = VioletPrimary, fontWeight = FontWeight.Bold)
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
                        onEdit = { taskToEdit = task },
                        onDelete = { viewModel.deleteTask(task.id) },
                        onWeekPlanClicked = { weekNum -> viewingWeekNumber = weekNum }
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
                availableCategories = categories,
                onDismiss = { showAddTaskSheet = false },
                onAddTask = { title, desc, priority, category, estMins, subtasksJson, dueDateMillis, dueTimeString, reminderEnabled, reminderLeadMins, reminderType, reminderSound, weeklyPlanWeekNumber ->
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
                        subtasksJson = subtasksJson,
                        reminderType = reminderType,
                        reminderSound = reminderSound,
                        weeklyPlanWeekNumber = weeklyPlanWeekNumber
                    )
                    showAddTaskSheet = false
                },
                onGenerateMicroSteps = { goal, callback ->
                    coroutineScope.launch {
                        val plan = viewModel.lumaService.generateCoachPlan(goal)
                        callback(plan)
                    }
                }
            )
        }

        if (taskToEdit != null) {
            EditTaskBottomSheet(
                task = taskToEdit!!,
                availableCategories = categories,
                onDismiss = { taskToEdit = null },
                onSaveTask = { updated ->
                    viewModel.updateTask(updated)
                    taskToEdit = null
                }
            )
        }

        if (viewingWeekNumber != null) {
            val weekNum = viewingWeekNumber!!
            val plansForWeek = allWeeklyPlans.filter { it.weekNumber == weekNum }
            AlertDialog(
                onDismissRequest = { viewingWeekNumber = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = CyanAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Week $weekNum Objectives & Plans", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    if (plansForWeek.isEmpty()) {
                        Text("No objectives planned for Week $weekNum yet. Add one in Weekly Objectives navigation.", color = TextMuted)
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().height(260.dp).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            plansForWeek.forEach { plan ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = CharcoalCardElevated,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (plan.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (plan.isCompleted) EmeraldSuccess else CyanAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = plan.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary
                                            )
                                            if (plan.description.isNotBlank()) {
                                                Text(
                                                    text = plan.description,
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
                },
                confirmButton = {
                    Button(onClick = { viewingWeekNumber = null }) {
                        Text("Close")
                    }
                }
            )
        }

        if (showAddCategoryDialog) {
            AlertDialog(
                onDismissRequest = { showAddCategoryDialog = false },
                title = { Text("New Custom Task Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                viewModel.addCustomTaskCategory(newCategoryName.trim())
                                selectedCategory = newCategoryName.trim()
                                newCategoryName = ""
                                showAddCategoryDialog = false
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showAddCategoryDialog = false }) {
                        Text("Cancel")
                    }
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
    onEdit: () -> Unit = {},
    onDelete: () -> Unit,
    onWeekPlanClicked: (Int) -> Unit = {}
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

                // Task Editing Button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Task",
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
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

            // Chips Row: Date & Reminder Badge + Category + Priority + Duration + Week Plan Link
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

                // Reminder Type & Sound Badge
                if (task.reminderEnabled) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmberWarning.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (task.reminderType == "ALARM") Icons.Default.Alarm else Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            val rLabel = when (task.reminderType) {
                                "ALARM" -> "Alarm"
                                "BOTH" -> "Alarm & Notif"
                                else -> "Notif"
                            }
                            val sLabel = if (task.reminderSound.isNotBlank() && task.reminderSound != "default") " · ${task.reminderSound}" else ""
                            Text(
                                text = "$rLabel$sLabel",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberWarning,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Belongs to Weekly Plan Badge (W43 Plan)
                if (task.weeklyPlanWeekNumber != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VioletPrimary.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VioletPrimary),
                        modifier = Modifier.clickable { onWeekPlanClicked(task.weeklyPlanWeekNumber) }
                    ) {
                        Text(
                            text = "W${task.weeklyPlanWeekNumber} Plan 📋",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA78BFA),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
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

                if (task.estimatedMinutes > 0) {
                    Text(
                        text = "${task.estimatedMinutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
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
    availableCategories: List<String>,
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
        reminderLeadMins: Int,
        reminderType: String,
        reminderSound: String,
        weeklyPlanWeekNumber: Int?
    ) -> Unit,
    onGenerateMicroSteps: (String, (String) -> Unit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedCategory by remember { mutableStateOf(availableCategories.firstOrNull { it != "All" } ?: "Work") }
    var estimatedMinutesStr by remember { mutableStateOf("") }
    var isGeneratingMicroSteps by remember { mutableStateOf(false) }

    // Date & Time Scheduling
    var isScheduled by remember { mutableStateOf(false) }
    var isCustomExactDate by remember { mutableStateOf(false) }
    var scheduledDaysOffset by remember { mutableIntStateOf(0) } // 0: Today, 1: Tomorrow, 2: 2 days, 7: Next week
    
    val nowCal = remember { Calendar.getInstance() }
    var exactYear by remember { mutableIntStateOf(nowCal.get(Calendar.YEAR)) }
    var exactMonth by remember { mutableIntStateOf(nowCal.get(Calendar.MONTH) + 1) }
    var exactDay by remember { mutableIntStateOf(nowCal.get(Calendar.DAY_OF_MONTH)) }
    var hourStr by remember { mutableStateOf(String.format(Locale.getDefault(), "%02d", nowCal.get(Calendar.HOUR_OF_DAY))) }
    var minStr by remember { mutableStateOf(String.format(Locale.getDefault(), "%02d", ((nowCal.get(Calendar.MINUTE) / 5) * 5 + 5) % 60)) }

    // Reminder Settings
    var reminderEnabled by remember { mutableStateOf(true) }
    var reminderType by remember { mutableStateOf("BOTH") } // NOTIFICATION, ALARM, BOTH
    var reminderSound by remember { mutableStateOf("Default Bell") }
    var reminderLeadMinutes by remember { mutableIntStateOf(0) } // 0: At exact time

    // Weekly Plan Link
    var linkToWeeklyPlan by remember { mutableStateOf(false) }
    val currentWeekNumber = remember { WeekCalculationHelper.calculateWeekInfo(nowCal.timeInMillis).currentWeekNumber }
    var selectedWeekNumber by remember { mutableIntStateOf(currentWeekNumber) }

    val soundOptions = listOf("Default Bell", "Sunrise Melody", "Digital Chime", "Radar Alert", "Zen Gong")
    val reminderTypeOptions = listOf("NOTIFICATION" to "🔔 Notification", "ALARM" to "⏰ Alarm", "BOTH" to "🔔+⏰ Both")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CharcoalCardElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                placeholder = { Text("e.g. Design Presentation, Study Chapter 4") },
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

            // Scheduling & Notification Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = CyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Schedule Date & Exact Time", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Custom date & seamless clock entry", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }
                        Switch(
                            checked = isScheduled,
                            onCheckedChange = { isScheduled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = VioletPrimary)
                        )
                    }

                    if (isScheduled) {
                        // Date selector
                        Text("Date", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val dateOptions = listOf("Today" to 0, "Tomorrow" to 1, "In 2 Days" to 2, "Next Week" to 7)
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

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCustomExactDate) CyanAccent.copy(alpha = 0.25f) else CharcoalCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isCustomExactDate) CyanAccent else CharcoalBorder),
                                modifier = Modifier.clickable { isCustomExactDate = true }
                            ) {
                                Text(
                                    text = "📅 Custom Date",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCustomExactDate) CyanAccent else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        if (isCustomExactDate) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = exactDay.toString(),
                                    onValueChange = { exactDay = (it.toIntOrNull() ?: 1).coerceIn(1, 31) },
                                    label = { Text("Day") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = exactMonth.toString(),
                                    onValueChange = { exactMonth = (it.toIntOrNull() ?: 1).coerceIn(1, 12) },
                                    label = { Text("Month") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = exactYear.toString(),
                                    onValueChange = { exactYear = it.toIntOrNull() ?: 2026 },
                                    label = { Text("Year") },
                                    modifier = Modifier.weight(1.2f),
                                    singleLine = true
                                )
                            }
                        }

                        // Exact Seamless Time Input (Hour & Min without forced presets)
                        Text("Exact Clock Time", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = hourStr,
                                onValueChange = { text ->
                                    val filtered = text.filter { it.isDigit() }.take(2)
                                    hourStr = filtered
                                },
                                label = { Text("Hour (0-23)") },
                                placeholder = { Text("14") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = CharcoalBorder)
                            )
                            Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            OutlinedTextField(
                                value = minStr,
                                onValueChange = { text ->
                                    val filtered = text.filter { it.isDigit() }.take(2)
                                    minStr = filtered
                                },
                                label = { Text("Minute (0-59)") },
                                placeholder = { Text("00") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = CharcoalBorder)
                            )
                        }

                        HorizontalDivider(color = CharcoalBorder.copy(alpha = 0.5f))

                        // Reminder Options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = VioletPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enable Reminder", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { reminderEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = VioletPrimary)
                            )
                        }

                        if (reminderEnabled) {
                            // Reminder Type: Alarm, Notification, or Both
                            Text("Alert Style", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                reminderTypeOptions.forEach { (typeKey, label) ->
                                    val isSelected = reminderType == typeKey
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) VioletPrimary else CharcoalCard,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyanAccent else CharcoalBorder),
                                        modifier = Modifier.weight(1f).clickable { reminderType = typeKey }
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else TextSecondary,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // Reminder Sound
                            Text("Sound", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                soundOptions.forEach { sound ->
                                    val isSelected = reminderSound == sound
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) CyanAccent.copy(alpha = 0.25f) else CharcoalCard,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyanAccent else CharcoalBorder),
                                        modifier = Modifier.clickable { reminderSound = sound }
                                    ) {
                                        Text(
                                            text = sound,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) CyanAccent else TextSecondary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            // Lead time
                            Text("Remind Me", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("At exact time" to 0, "5m before" to 5, "15m before" to 15, "30m before" to 30, "1h before" to 60).forEach { (lbl, mins) ->
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
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Belongs to Weekly Plan Section
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = AmberWarning)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Belongs to Weekly Plan", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Link this task to a specific week", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }
                        Switch(
                            checked = linkToWeeklyPlan,
                            onCheckedChange = { linkToWeeklyPlan = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AmberWarning)
                        )
                    }

                    if (linkToWeeklyPlan) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Week Number:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            OutlinedTextField(
                                value = selectedWeekNumber.toString(),
                                onValueChange = { selectedWeekNumber = (it.toIntOrNull() ?: currentWeekNumber).coerceIn(1, 53) },
                                label = { Text("Week (1-53)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberWarning)
                            )
                        }
                    }
                }
            }

            // Priority Selector
            Text("Priority", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskPriority.values().forEach { prio ->
                    val isSelected = priority == prio
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) getPriorityColor(prio) else CharcoalCard,
                        modifier = Modifier.weight(1f).clickable { priority = prio }
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
            Text("Category", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val cats = availableCategories.filter { it != "All" }
                cats.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CyanAccent.copy(alpha = 0.25f) else CharcoalCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyanAccent else CharcoalBorder),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) CyanAccent else TextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = estimatedMinutesStr,
                onValueChange = { text -> estimatedMinutesStr = text.filter { it.isDigit() } },
                label = { Text("Est. Minutes (Optional)") },
                placeholder = { Text("25") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = CharcoalBorder)
            )

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val finalHour = (hourStr.toIntOrNull() ?: 12).coerceIn(0, 23)
                        val finalMin = (minStr.toIntOrNull() ?: 0).coerceIn(0, 59)

                        val dueMillis = if (isScheduled) {
                            if (isCustomExactDate) {
                                Calendar.getInstance().apply {
                                    set(Calendar.YEAR, exactYear)
                                    set(Calendar.MONTH, exactMonth - 1)
                                    set(Calendar.DAY_OF_MONTH, exactDay)
                                    set(Calendar.HOUR_OF_DAY, finalHour)
                                    set(Calendar.MINUTE, finalMin)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                            } else {
                                Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_YEAR, scheduledDaysOffset)
                                    set(Calendar.HOUR_OF_DAY, finalHour)
                                    set(Calendar.MINUTE, finalMin)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                            }
                        } else null

                        val dueTimeStr = if (isScheduled) {
                            String.format(Locale.getDefault(), "%02d:%02d", finalHour, finalMin)
                        } else null

                        onAddTask(
                            title,
                            description,
                            priority,
                            selectedCategory,
                            estimatedMinutesStr.toIntOrNull() ?: 0,
                            "[]",
                            dueMillis,
                            dueTimeStr,
                            isScheduled && reminderEnabled,
                            reminderLeadMinutes,
                            reminderType,
                            reminderSound,
                            if (linkToWeeklyPlan) selectedWeekNumber else null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                enabled = title.isNotBlank()
            ) {
                Text(if (isScheduled) "Schedule Task & Set Alert" else "Create Task", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskBottomSheet(
    task: TaskEntity,
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onSaveTask: (TaskEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var priority by remember { mutableStateOf(task.priority) }
    var selectedCategory by remember { mutableStateOf(task.category) }
    var estimatedMinutesStr by remember { mutableStateOf(if (task.estimatedMinutes > 0) task.estimatedMinutes.toString() else "") }

    var isScheduled by remember { mutableStateOf(task.dueDateMillis != null) }
    val initialCal = remember {
        Calendar.getInstance().apply {
            if (task.dueDateMillis != null) timeInMillis = task.dueDateMillis
        }
    }
    var exactYear by remember { mutableIntStateOf(initialCal.get(Calendar.YEAR)) }
    var exactMonth by remember { mutableIntStateOf(initialCal.get(Calendar.MONTH) + 1) }
    var exactDay by remember { mutableIntStateOf(initialCal.get(Calendar.DAY_OF_MONTH)) }
    var hourStr by remember { mutableStateOf(String.format(Locale.getDefault(), "%02d", initialCal.get(Calendar.HOUR_OF_DAY))) }
    var minStr by remember { mutableStateOf(String.format(Locale.getDefault(), "%02d", initialCal.get(Calendar.MINUTE))) }

    var reminderEnabled by remember { mutableStateOf(task.reminderEnabled) }
    var reminderType by remember { mutableStateOf(task.reminderType) }
    var reminderSound by remember { mutableStateOf(task.reminderSound) }
    var reminderLeadMinutes by remember { mutableIntStateOf(0) }

    var linkToWeeklyPlan by remember { mutableStateOf(task.weeklyPlanWeekNumber != null) }
    var selectedWeekNumber by remember { mutableIntStateOf(task.weeklyPlanWeekNumber ?: 1) }

    val soundOptions = listOf("Default Bell", "Sunrise Melody", "Digital Chime", "Radar Alert", "Zen Gong")
    val reminderTypeOptions = listOf("NOTIFICATION" to "🔔 Notification", "ALARM" to "⏰ Alarm", "BOTH" to "🔔+⏰ Both")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CharcoalCardElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Edit Task / Event",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = CharcoalBorder)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notes / Micro-steps") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanAccent, unfocusedBorderColor = CharcoalBorder)
            )

            // Scheduling
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Scheduled Date & Time", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Switch(
                            checked = isScheduled,
                            onCheckedChange = { isScheduled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent)
                        )
                    }

                    if (isScheduled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = exactDay.toString(),
                                onValueChange = { exactDay = (it.toIntOrNull() ?: 1).coerceIn(1, 31) },
                                label = { Text("Day") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = exactMonth.toString(),
                                onValueChange = { exactMonth = (it.toIntOrNull() ?: 1).coerceIn(1, 12) },
                                label = { Text("Month") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = exactYear.toString(),
                                onValueChange = { exactYear = it.toIntOrNull() ?: 2026 },
                                label = { Text("Year") },
                                modifier = Modifier.weight(1.2f),
                                singleLine = true
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = hourStr,
                                onValueChange = { hourStr = it.filter { c -> c.isDigit() }.take(2) },
                                label = { Text("Hour (0-23)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            OutlinedTextField(
                                value = minStr,
                                onValueChange = { minStr = it.filter { c -> c.isDigit() }.take(2) },
                                label = { Text("Min (0-59)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        HorizontalDivider(color = CharcoalBorder.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Reminder Active", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { reminderEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = VioletPrimary)
                            )
                        }

                        if (reminderEnabled) {
                            Text("Alert Style", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                reminderTypeOptions.forEach { (typeKey, label) ->
                                    val isSelected = reminderType == typeKey
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) VioletPrimary else CharcoalCard,
                                        modifier = Modifier.weight(1f).clickable { reminderType = typeKey }
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.White else TextSecondary,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Text("Sound", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                soundOptions.forEach { sound ->
                                    val isSelected = reminderSound == sound
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) CyanAccent.copy(alpha = 0.25f) else CharcoalCard,
                                        modifier = Modifier.clickable { reminderSound = sound }
                                    ) {
                                        Text(
                                            text = sound,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) CyanAccent else TextSecondary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Weekly Plan Link
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Belongs to Weekly Plan", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Switch(
                            checked = linkToWeeklyPlan,
                            onCheckedChange = { linkToWeeklyPlan = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AmberWarning)
                        )
                    }
                    if (linkToWeeklyPlan) {
                        OutlinedTextField(
                            value = selectedWeekNumber.toString(),
                            onValueChange = { selectedWeekNumber = (it.toIntOrNull() ?: 1).coerceIn(1, 53) },
                            label = { Text("Week Number (1-53)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Priority
            Text("Priority", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskPriority.values().forEach { prio ->
                    val isSelected = priority == prio
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) getPriorityColor(prio) else CharcoalCard,
                        modifier = Modifier.weight(1f).clickable { priority = prio }
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

            // Category
            Text("Category", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCategories.filter { it != "All" }.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CyanAccent.copy(alpha = 0.25f) else CharcoalCard,
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) CyanAccent else TextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = estimatedMinutesStr,
                onValueChange = { text -> estimatedMinutesStr = text.filter { it.isDigit() } },
                label = { Text("Est. Minutes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    val finalHour = (hourStr.toIntOrNull() ?: 12).coerceIn(0, 23)
                    val finalMin = (minStr.toIntOrNull() ?: 0).coerceIn(0, 59)
                    val dueMillis = if (isScheduled) {
                        Calendar.getInstance().apply {
                            set(Calendar.YEAR, exactYear)
                            set(Calendar.MONTH, exactMonth - 1)
                            set(Calendar.DAY_OF_MONTH, exactDay)
                            set(Calendar.HOUR_OF_DAY, finalHour)
                            set(Calendar.MINUTE, finalMin)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                    } else null

                    val dueTimeStr = if (isScheduled) {
                        String.format(Locale.getDefault(), "%02d:%02d", finalHour, finalMin)
                    } else null

                    val updated = task.copy(
                        title = title,
                        description = description,
                        priority = priority,
                        category = selectedCategory,
                        estimatedMinutes = estimatedMinutesStr.toIntOrNull() ?: 0,
                        dueDateMillis = dueMillis,
                        dueTimeString = dueTimeStr,
                        reminderEnabled = isScheduled && reminderEnabled,
                        reminderType = reminderType,
                        reminderSound = reminderSound,
                        weeklyPlanWeekNumber = if (linkToWeeklyPlan) selectedWeekNumber else null
                    )
                    onSaveTask(updated)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun getPriorityColor(priority: TaskPriority): Color = when (priority) {
    TaskPriority.URGENT -> RoseUrgent
    TaskPriority.HIGH -> AmberWarning
    TaskPriority.MEDIUM -> CyanAccent
    TaskPriority.LOW -> TextSecondary
}

