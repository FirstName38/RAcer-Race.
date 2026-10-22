package com.example.ui.screens.weekly

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.WeeklyPlanEntity
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WeekDayInfo(
    val code: String, // "MON", "TUE", etc.
    val label: String, // "Mon", "Tue"
    val dayNumber: Int, // 1..31
    val dateString: String, // "Oct 22"
    val isToday: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyPlanScreen(
    viewModel: RacerViewModel,
    modifier: Modifier = Modifier
) {
    val allPlans by viewModel.allWeeklyPlans.collectAsState()
    val weekStartDay by viewModel.weekStartDay.collectAsState()

    var weekOffset by remember { mutableIntStateOf(0) }
    var selectedDayFilter by remember { mutableStateOf("ALL") } // "ALL", "MON", "TUE", etc.
    var showAddPlanSheet by remember { mutableStateOf(false) }
    var planToEdit by remember { mutableStateOf<WeeklyPlanEntity?>(null) }
    var showWeekStartPicker by remember { mutableStateOf(false) }

    // Calculate Week Details based on user's chosen start day
    val calendar = remember(weekStartDay, weekOffset) {
        val firstDay = when (weekStartDay.uppercase()) {
            "SUNDAY" -> Calendar.SUNDAY
            "SATURDAY" -> Calendar.SATURDAY
            else -> Calendar.MONDAY
        }
        Calendar.getInstance().apply {
            firstDayOfWeek = firstDay
            minimalDaysInFirstWeek = 4
            add(Calendar.WEEK_OF_YEAR, weekOffset)
        }
    }

    val currentYear = calendar.get(Calendar.YEAR)
    val currentWeekNumber = calendar.get(Calendar.WEEK_OF_YEAR)
    val maxWeeksInYear = calendar.getActualMaximum(Calendar.WEEK_OF_YEAR)
    val weeksRemaining = (maxWeeksInYear - currentWeekNumber).coerceAtLeast(0)

    // Compute the 7 days of this active week
    val weekDays = remember(calendar, weekStartDay) {
        val calClone = calendar.clone() as Calendar
        val firstDay = when (weekStartDay.uppercase()) {
            "SUNDAY" -> Calendar.SUNDAY
            "SATURDAY" -> Calendar.SATURDAY
            else -> Calendar.MONDAY
        }
        calClone.set(Calendar.DAY_OF_WEEK, firstDay)
        val todayCal = Calendar.getInstance()
        val dFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val codeFormat = SimpleDateFormat("EEE", Locale.US)

        (0..6).map {
            val code = codeFormat.format(calClone.time).uppercase().take(3)
            val label = SimpleDateFormat("EEE", Locale.getDefault()).format(calClone.time)
            val dayNum = calClone.get(Calendar.DAY_OF_MONTH)
            val dStr = dFormat.format(calClone.time)
            val isToday = calClone.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                    calClone.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
            val info = WeekDayInfo(code, label, dayNum, dStr, isToday)
            calClone.add(Calendar.DAY_OF_YEAR, 1)
            info
        }
    }

    // Filter plans for this week
    val currentWeekPlans = allPlans.filter {
        (it.weekNumber == currentWeekNumber && it.year == currentYear) ||
                (it.weekNumber == 0 && weekOffset == 0) // fallback
    }

    val displayedPlans = if (selectedDayFilter == "ALL") {
        currentWeekPlans
    } else {
        currentWeekPlans.filter { it.dayOfWeek == selectedDayFilter || it.dayOfWeek == "ALL" }
    }

    // 70% threshold calculations
    val totalCount = currentWeekPlans.size
    val completedCount = currentWeekPlans.count { it.isCompleted }
    val progressRate = if (totalCount > 0) (completedCount.toFloat() / totalCount * 100f) else 0f
    val isAbove70 = progressRate >= 70f
    val thresholdColor = if (isAbove70) EmeraldSuccess else RoseUrgent

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Title & Week Switcher Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Weekly Plan",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Strategic weekly milestones, calendar rhythm & goals",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    // Week Start Day selector chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CharcoalCardElevated,
                        border = BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { showWeekStartPicker = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Starts: ${weekStartDay.lowercase().replaceFirstChar { it.uppercase() }}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Week Number & Weeks Left Badge Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = CharcoalCardElevated,
                    borderColor = thresholdColor.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { weekOffset-- },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Week", tint = CyanAccent)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = "Week $currentWeekNumber of $maxWeeksInYear",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "$weeksRemaining weeks left in $currentYear",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CyanAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (weekOffset != 0) {
                                    Button(
                                        onClick = { weekOffset = 0 },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalBorder),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Current", fontSize = 11.sp, color = TextPrimary)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                IconButton(
                                    onClick = { weekOffset++ },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Week", tint = CyanAccent)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 70% threshold progress indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$completedCount of $totalCount goals achieved",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = thresholdColor.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = "${progressRate.toInt()}% " + (if (isAbove70) "Optimal (>=70%)" else "Needs Attention (<70%)"),
                                    color = thresholdColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { if (totalCount > 0) (completedCount.toFloat() / totalCount).coerceIn(0f, 1f) else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = thresholdColor,
                            trackColor = CharcoalBorder
                        )
                    }
                }
            }

            // Interactive Weekly Calendar Bar
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Week Calendar View",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tap a day to filter",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // All Week Filter Chip
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedDayFilter == "ALL") VioletPrimary else CharcoalCardElevated,
                            border = BorderStroke(1.dp, if (selectedDayFilter == "ALL") VioletPrimary else CharcoalBorder),
                            modifier = Modifier
                                .clickable { selectedDayFilter = "ALL" }
                                .padding(vertical = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("ALL", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (selectedDayFilter == "ALL") Color.White else TextMuted)
                                Text("Week", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (selectedDayFilter == "ALL") Color.White else TextPrimary)
                            }
                        }

                        weekDays.forEach { day ->
                            val isSelected = selectedDayFilter == day.code
                            val dayPlanCount = currentWeekPlans.count { it.dayOfWeek == day.code }
                            val dayDoneCount = currentWeekPlans.count { it.dayOfWeek == day.code && it.isCompleted }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) VioletPrimary else CharcoalCardElevated,
                                border = BorderStroke(
                                    1.dp,
                                    if (day.isToday) CyanAccent else if (isSelected) VioletPrimary else CharcoalBorder
                                ),
                                modifier = Modifier
                                    .clickable { selectedDayFilter = day.code }
                                    .padding(vertical = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = day.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else if (day.isToday) CyanAccent else TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${day.dayNumber}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                    if (dayPlanCount > 0) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (dayDoneCount >= dayPlanCount) EmeraldSuccess else CyanAccent)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Plan List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedDayFilter == "ALL") "All Weekly Objectives (${displayedPlans.size})" else "Plans for $selectedDayFilter (${displayedPlans.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Button(
                        onClick = { showAddPlanSheet = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary.copy(alpha = 0.25f)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Plan", fontSize = 12.sp, color = VioletPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Plan Items
            if (displayedPlans.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No plans listed for this selection", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Tap '+ Add Plan' to map out your weekly focus target.", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
            } else {
                items(displayedPlans) { plan ->
                    WeeklyPlanCard(
                        plan = plan,
                        onToggle = { viewModel.toggleWeeklyPlan(plan.id, !plan.isCompleted) },
                        onEdit = { planToEdit = plan },
                        onDelete = { viewModel.deleteWeeklyPlan(plan.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button to Add Plan
        FloatingActionButton(
            onClick = { showAddPlanSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = VioletPrimary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Weekly Plan")
        }

        // Bottom Sheet: Add / Edit Plan
        if (showAddPlanSheet || planToEdit != null) {
            AddOrEditWeeklyPlanBottomSheet(
                existingPlan = planToEdit,
                defaultDay = if (selectedDayFilter == "ALL") "ALL" else selectedDayFilter,
                weekNumber = currentWeekNumber,
                year = currentYear,
                onDismiss = {
                    showAddPlanSheet = false
                    planToEdit = null
                },
                onSave = { title, desc, day, prio, cat, targetMins ->
                    if (planToEdit != null) {
                        viewModel.updateWeeklyPlan(
                            planToEdit!!.copy(
                                title = title,
                                description = desc,
                                dayOfWeek = day,
                                priority = prio,
                                category = cat,
                                targetTimeMinutes = targetMins
                            )
                        )
                    } else {
                        viewModel.addWeeklyPlan(
                            title = title,
                            description = desc,
                            dayOfWeek = day,
                            priority = prio,
                            category = cat,
                            targetMinutes = targetMins,
                            weekNumber = currentWeekNumber,
                            year = currentYear
                        )
                    }
                    showAddPlanSheet = false
                    planToEdit = null
                }
            )
        }

        // Dialog: Pick Week Start Day
        if (showWeekStartPicker) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showWeekStartPicker = false },
                containerColor = CharcoalCardElevated,
                title = {
                    Text("First Day of the Week", fontWeight = FontWeight.Bold, color = TextPrimary)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Choose which day begins your weekly cycle. Week numbers and calendar columns will adapt automatically:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        val days = listOf("MONDAY" to "Monday (ISO Standard)", "SUNDAY" to "Sunday (US & Traditional)", "SATURDAY" to "Saturday (Custom)")
                        days.forEach { (code, label) ->
                            val isSelected = weekStartDay.equals(code, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) VioletPrimary.copy(alpha = 0.2f) else CharcoalCard,
                                border = BorderStroke(1.dp, if (isSelected) VioletPrimary else CharcoalBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setWeekStartDay(code)
                                        showWeekStartPicker = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) VioletPrimary else TextMuted
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(label, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showWeekStartPicker = false },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    }
}

@Composable
fun WeeklyPlanCard(
    plan: WeeklyPlanEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val prioColor = when (plan.priority) {
        "HIGH" -> RoseUrgent
        "MEDIUM" -> AmberWarning
        else -> CyanAccent
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        borderColor = if (plan.isCompleted) EmeraldSuccess.copy(alpha = 0.4f) else CharcoalBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (plan.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle",
                    tint = if (plan.isCompleted) EmeraldSuccess else CyanAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (plan.isCompleted) TextDisabled else TextPrimary,
                    textDecoration = if (plan.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (plan.description.isNotBlank()) {
                    Text(
                        text = plan.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = prioColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = plan.priority,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = prioColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CharcoalCardElevated
                    ) {
                        Text(
                            text = if (plan.dayOfWeek == "ALL") "All Week" else plan.dayOfWeek,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (plan.category.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CharcoalCardElevated
                        ) {
                            Text(
                                text = plan.category,
                                fontSize = 9.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextMuted, modifier = Modifier.size(16.dp))
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditWeeklyPlanBottomSheet(
    existingPlan: WeeklyPlanEntity?,
    defaultDay: String,
    weekNumber: Int,
    year: Int,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, day: String, priority: String, category: String, targetMins: Int) -> Unit
) {
    var title by remember { mutableStateOf(existingPlan?.title ?: "") }
    var description by remember { mutableStateOf(existingPlan?.description ?: "") }
    var dayOfWeek by remember { mutableStateOf(existingPlan?.dayOfWeek ?: defaultDay) }
    var priority by remember { mutableStateOf(existingPlan?.priority ?: "HIGH") }
    var category by remember { mutableStateOf(existingPlan?.category ?: "Productivity") }
    var targetMinsString by remember { mutableStateOf(if ((existingPlan?.targetTimeMinutes ?: 0) > 0) existingPlan!!.targetTimeMinutes.toString() else "") }

    val daysList = listOf("ALL" to "All Week", "MON" to "Mon", "TUE" to "Tue", "WED" to "Wed", "THU" to "Thu", "FRI" to "Fri", "SAT" to "Sat", "SUN" to "Sun")
    val priorities = listOf("HIGH", "MEDIUM", "LOW")
    val categories = listOf("Productivity", "Work", "Study", "Wellness", "Planning", "Personal")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CharcoalCardElevated,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingPlan != null) "Edit Weekly Plan" else "New Weekly Objective",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Goal / Action Title") },
                placeholder = { Text("e.g. Finish Chapter 4 & 5 Practice Sets") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VioletPrimary,
                    unfocusedBorderColor = CharcoalBorder
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Details / Deliverable (Optional)") },
                placeholder = { Text("e.g. Solve 30 questions under timed conditions") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VioletPrimary,
                    unfocusedBorderColor = CharcoalBorder
                )
            )

            // Day of Week
            Column {
                Text("Assigned Day / Scope", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(daysList) { (code, label) ->
                        FilterChip(
                            selected = dayOfWeek == code,
                            onClick = { dayOfWeek = code },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VioletPrimary.copy(alpha = 0.25f),
                                selectedLabelColor = VioletPrimary
                            )
                        )
                    }
                }
            }

            // Priority
            Column {
                Text("Priority Level", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    priorities.forEach { prio ->
                        val isSelected = priority == prio
                        val color = if (prio == "HIGH") RoseUrgent else if (prio == "MEDIUM") AmberWarning else CyanAccent
                        FilterChip(
                            selected = isSelected,
                            onClick = { priority = prio },
                            label = { Text(prio, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(alpha = 0.25f),
                                selectedLabelColor = color
                            )
                        )
                    }
                }
            }

            // Category
            Column {
                Text("Category", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                                selectedLabelColor = CyanAccent
                            )
                        )
                    }
                }
            }

            // Optional target time
            OutlinedTextField(
                value = targetMinsString,
                onValueChange = { targetMinsString = it.filter { char -> char.isDigit() } },
                label = { Text("Target Focus Minutes (Optional)") },
                placeholder = { Text("e.g. 60 (leave empty if none)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VioletPrimary,
                    unfocusedBorderColor = CharcoalBorder
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val mins = targetMinsString.toIntOrNull() ?: 0
                        onSave(title.trim(), description.trim(), dayOfWeek, priority, category, mins)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
            ) {
                Text(if (existingPlan != null) "Update Plan" else "Save Weekly Plan", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
