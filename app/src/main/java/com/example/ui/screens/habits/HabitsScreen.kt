package com.example.ui.screens.habits

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.HabitCompletionEntity
import com.example.data.entity.HabitEntity
import com.example.data.model.HabitFrequency
import com.example.ui.components.GlassCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoCalm
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    viewModel: RacerViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.activeHabits.collectAsState()
    val completions by viewModel.allHabitCompletions.collectAsState()
    val todayStr = viewModel.repository.getTodayDateString()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Habits, 2: Challenges
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDetailHabit by remember { mutableStateOf<HabitEntity?>(null) }

    val filteredList = remember(habits, selectedTab) {
        when (selectedTab) {
            1 -> habits.filter { !it.isChallenge }
            2 -> habits.filter { it.isChallenge }
            else -> habits
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Column {
                    Text(
                        text = "Habit & Challenge",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Consistent daily systems & rigorous challenge pacing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Tab Row: All, Habits, Challenges
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CharcoalCard,
                    contentColor = VioletPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = VioletPrimary
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("All", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Habits", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Challenges", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            items(filteredList, key = { it.id }) { habit ->
                val isDoneToday = completions.any { it.habitId == habit.id && it.dateString == todayStr }
                val streak = remember(completions, habit.id) {
                    viewModel.repository.calculateHabitStreak(habit.id, completions)
                }

                HabitChallengeCard(
                    habit = habit,
                    isDoneToday = isDoneToday,
                    currentStreak = streak,
                    onToggleToday = { viewModel.toggleHabitToday(habit.id) },
                    onCardClick = { selectedDetailHabit = habit },
                    onDelete = { viewModel.deleteHabit(habit) },
                    completions = completions.filter { it.habitId == habit.id }
                )
            }

            if (filteredList.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                if (selectedTab == 2) Icons.Default.EmojiEvents else Icons.Default.Repeat,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (selectedTab == 2) "No active challenges. Tap '+' to challenge yourself!" else "No habits yet. Tap '+' to build your calm daily rhythm.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = VioletPrimary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Habit or Challenge")
        }

        if (showAddDialog) {
            AddHabitChallengeBottomSheet(
                onDismiss = { showAddDialog = false },
                onAddHabit = { name, desc, label, freq, color, reminderTime, reminderCtx, reminderOn ->
                    viewModel.addHabit(
                        name = name,
                        description = desc,
                        label = label,
                        frequency = freq,
                        colorHex = color,
                        reminderTimeString = reminderTime,
                        reminderContext = reminderCtx,
                        reminderEnabled = reminderOn
                    )
                    showAddDialog = false
                },
                onAddChallenge = { name, desc, label, days, color, reminderTime, reminderCtx, reminderOn ->
                    viewModel.addChallenge(
                        name = name,
                        description = desc,
                        label = label,
                        challengeDays = days,
                        colorHex = color,
                        reminderTimeString = reminderTime,
                        reminderContext = reminderCtx,
                        reminderEnabled = reminderOn
                    )
                    showAddDialog = false
                },
                onTestAlarm = { label ->
                    viewModel.testHabitAlarm(label)
                }
            )
        }

        if (selectedDetailHabit != null) {
            HabitChallengeCalendarSheet(
                habit = selectedDetailHabit!!,
                completions = completions.filter { it.habitId == selectedDetailHabit!!.id },
                onDismiss = { selectedDetailHabit = null },
                onRecordCompletion = { dateStr, done, score, note ->
                    viewModel.recordHabitCompletion(
                        habitId = selectedDetailHabit!!.id,
                        dateString = dateStr,
                        isCompleted = done,
                        perfectionScore = score,
                        note = note
                    )
                }
            )
        }
    }
}

@Composable
fun HabitChallengeCard(
    habit: HabitEntity,
    isDoneToday: Boolean,
    currentStreak: Int,
    onToggleToday: () -> Unit,
    onCardClick: () -> Unit,
    onDelete: () -> Unit,
    completions: List<HabitCompletionEntity>
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(habit.colorHex))
    } catch (e: Exception) {
        if (habit.isChallenge) RoseUrgent else VioletPrimary
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        borderColor = if (isDoneToday) EmeraldSuccess.copy(alpha = 0.4f) else accentColor.copy(alpha = 0.25f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (habit.isChallenge) Icons.Default.EmojiEvents else Icons.Default.Repeat,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = habit.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = accentColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (habit.isChallenge) "${habit.challengeDays}D CHALLENGE" else habit.label.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (habit.description.isNotBlank()) {
                            Text(
                                text = habit.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onToggleToday,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDoneToday) EmeraldSuccess else CharcoalCardElevated)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Complete Today",
                        tint = if (isDoneToday) Color.Black else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats / Streak & Reminder Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = if (currentStreak > 0) AmberWarning else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (habit.isChallenge) {
                            if (currentStreak > 0) "Day $currentStreak of ${habit.challengeDays}" else "Day 0 (Reset to 0)"
                        } else {
                            "$currentStreak day streak"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (currentStreak > 0) AmberWarning else TextMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!habit.reminderTimeString.isNullOrBlank() || habit.reminderContext.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanAccent.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = habit.reminderTimeString ?: habit.reminderContext,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7-day mini dot history
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val cal = Calendar.getInstance()
                val dFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dayNameFormat = SimpleDateFormat("EE", Locale.getDefault())

                val daysList = (6 downTo 0).map { dayOffset ->
                    val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -dayOffset) }
                    Triple(dFormat.format(c.time), dayNameFormat.format(c.time).take(1), dayOffset == 0)
                }

                daysList.forEach { (dateStr, dayInitial, isToday) ->
                    val isDone = completions.any { it.dateString == dateStr && it.isCompleted }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dayInitial,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) TextPrimary else TextMuted,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDone) EmeraldSuccess else CharcoalCardElevated
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitChallengeBottomSheet(
    onDismiss: () -> Unit,
    onAddHabit: (name: String, desc: String, label: String, freq: HabitFrequency, color: String, reminderTime: String?, reminderCtx: String, reminderOn: Boolean) -> Unit,
    onAddChallenge: (name: String, desc: String, label: String, days: Int, color: String, reminderTime: String?, reminderCtx: String, reminderOn: Boolean) -> Unit,
    onTestAlarm: (label: String) -> Unit = {}
) {
    var isChallengeMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("Health") }
    var challengeDays by remember { mutableIntStateOf(30) }
    var reminderContext by remember { mutableStateOf("Walk after eating") }
    var reminderEnabled by remember { mutableStateOf(true) }
    var reminderTimeString by remember { mutableStateOf("08:30") }
    var selectedColor by remember { mutableStateOf("#8B5CF6") }

    val challengeDayOptions = listOf(7, 14, 21, 30, 60, 75, 100)
    val labelOptions = listOf("Health", "Study", "Deep Work", "Mindfulness", "Fitness", "Discipline")
    val colorOptions = listOf("#8B5CF6", "#EC4899", "#06B6D4", "#10B981", "#F59E0B", "#EF4444")
    val timePresets = listOf("07:00", "08:30", "12:30", "15:00", "18:30", "21:00", "22:30")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CharcoalCardElevated,
        contentColor = TextPrimary
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isChallengeMode) "New Challenge" else "New Habit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }
            }

            // Selector: Habit vs Challenge
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = !isChallengeMode,
                        onClick = { isChallengeMode = false },
                        label = { Text("Gentle Habit", modifier = Modifier.padding(4.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VioletPrimary.copy(alpha = 0.25f),
                            selectedLabelColor = VioletPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = isChallengeMode,
                        onClick = { isChallengeMode = true },
                        label = { Text("Strict Challenge", modifier = Modifier.padding(4.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoseUrgent.copy(alpha = 0.25f),
                            selectedLabelColor = RoseUrgent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Name
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Title / Action Name") },
                    placeholder = { Text(if (isChallengeMode) "e.g. 75 Hard Deep Study" else "e.g. Walk after eating") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isChallengeMode) RoseUrgent else VioletPrimary,
                        unfocusedBorderColor = CharcoalBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }

            // Description
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Rule (Optional)") },
                    placeholder = { Text("e.g. At least 15 min brisk walk, no phone...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VioletPrimary,
                        unfocusedBorderColor = CharcoalBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }

            // Category / Label
            item {
                Text("Category Label", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(labelOptions) { opt ->
                        FilterChip(
                            selected = label == opt,
                            onClick = { label = opt },
                            label = { Text(opt, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent.copy(alpha = 0.25f),
                                selectedLabelColor = CyanAccent
                            )
                        )
                    }
                }
            }

            // If Challenge: Duration days selection
            if (isChallengeMode) {
                item {
                    Text("Target Duration (Days)", style = MaterialTheme.typography.labelMedium, color = RoseUrgent, fontWeight = FontWeight.Bold)
                    Text("If you miss 1 day, your streak will reset to 0 and restart from Day 1.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(challengeDayOptions) { days ->
                            FilterChip(
                                selected = challengeDays == days,
                                onClick = { challengeDays = days },
                                label = { Text("$days Days", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoseUrgent.copy(alpha = 0.25f),
                                    selectedLabelColor = RoseUrgent
                                )
                            )
                        }
                    }
                }
            }

            // Alarm & Reminder Option
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = CyanAccent)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Daily Alarm & Trigger Reminder", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("Rings system alarm & notification at scheduled time", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { reminderEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = CyanAccent.copy(alpha = 0.5f))
                            )
                        }

                        if (reminderEnabled) {
                            Text("Reminder Time (24h or Preset):", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(timePresets) { tPreset ->
                                    FilterChip(
                                        selected = reminderTimeString == tPreset,
                                        onClick = { reminderTimeString = tPreset },
                                        label = { Text(tPreset, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CyanAccent.copy(alpha = 0.25f),
                                            selectedLabelColor = CyanAccent
                                        )
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = reminderTimeString,
                                    onValueChange = { reminderTimeString = it },
                                    label = { Text("Exact Time (HH:mm)") },
                                    placeholder = { Text("08:30") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanAccent,
                                        unfocusedBorderColor = CharcoalBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    singleLine = true
                                )

                                Button(
                                    onClick = { onTestAlarm(if (name.isNotBlank()) name else "Habit Alarm") },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test Alarm", color = CyanAccent, style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            OutlinedTextField(
                                value = reminderContext,
                                onValueChange = { reminderContext = it },
                                label = { Text("Trigger Note / Routine Cue") },
                                placeholder = { Text("e.g. Walk after dinner, 10:00 PM wind down") },
                                modifier = Modifier.fillMaxWidth(),
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
                }
            }

            // Color selection
            item {
                Text("Color Accent", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    colorOptions.forEach { hex ->
                        val c = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(c)
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == hex) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            if (isChallengeMode) {
                                onAddChallenge(name, description, label, challengeDays, selectedColor, if (reminderEnabled) reminderTimeString else null, reminderContext, reminderEnabled)
                            } else {
                                onAddHabit(name, description, label, HabitFrequency.DAILY, selectedColor, if (reminderEnabled) reminderTimeString else null, reminderContext, reminderEnabled)
                            }
                        }
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isChallengeMode) RoseUrgent else VioletPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isChallengeMode) "Start $challengeDays-Day Challenge" else "Create Habit", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitChallengeCalendarSheet(
    habit: HabitEntity,
    completions: List<HabitCompletionEntity>,
    onDismiss: () -> Unit,
    onRecordCompletion: (dateStr: String, isDone: Boolean, score: Int, note: String) -> Unit
) {
    val dFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val monthNameFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    var activeDayDateStr by remember { mutableStateOf(dFormat.format(Date())) }
    var showDayDetailDialog by remember { mutableStateOf(false) }

    val cal = Calendar.getInstance()
    val currentMonthTitle = monthNameFormat.format(cal.time)

    // Build day grid for current month or challenge duration
    val totalDaysToDisplay = if (habit.isChallenge) habit.challengeDays else 30
    val daysList = remember(habit.id, habit.challengeDays) {
        val list = mutableListOf<String>()
        val c = Calendar.getInstance()
        if (habit.isChallenge) {
            // Start from habit start date or 15 days ago
            c.timeInMillis = habit.startDateMillis
        } else {
            c.add(Calendar.DAY_OF_YEAR, -20)
        }
        for (i in 0 until totalDaysToDisplay) {
            list.add(dFormat.format(c.time))
            c.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

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
                    Column {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (habit.isChallenge) "${habit.challengeDays}-Day Challenge Matrix" else "Habit Progress Calendar",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (habit.isChallenge) RoseUrgent else VioletPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Tap any day to check completion & score quality:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Grid of days
                        val completedCount = completions.count { it.isCompleted }
                        Text(
                            text = "$completedCount completed • Quality Avg: ${if (completedCount > 0) completions.map { it.perfectionScore }.average().toInt() else 100}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                    }
                }
            }

            item {
                Text("Day Matrix", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(daysList.indices.toList()) { index ->
                        val dateStr = daysList[index]
                        val comp = completions.find { it.dateString == dateStr && it.isCompleted }
                        val isDone = comp != null
                        val dayNumber = index + 1

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isDone) EmeraldSuccess.copy(alpha = 0.85f) else CharcoalCard
                                )
                                .clickable {
                                    activeDayDateStr = dateStr
                                    showDayDetailDialog = true
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (habit.isChallenge) "Day $dayNumber" else dateStr.takeLast(5),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDone) Color.Black else TextSecondary
                                )
                                if (isDone) {
                                    Text(
                                        text = "${comp?.perfectionScore ?: 100}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = Color.Black.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (showDayDetailDialog) {
            val existingComp = completions.find { it.dateString == activeDayDateStr }
            var isChecked by remember { mutableStateOf(existingComp?.isCompleted ?: true) }
            var score by remember { mutableFloatStateOf((existingComp?.perfectionScore ?: 100).toFloat()) }
            var note by remember { mutableStateOf(existingComp?.note ?: "") }

            ModalBottomSheet(
                onDismissRequest = { showDayDetailDialog = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = CharcoalCard,
                contentColor = TextPrimary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Track Date: $activeDayDateStr",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isChecked = !isChecked }
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { isChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = EmeraldSuccess)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isChecked) "I did it! (Completed)" else "Missed / Did not do",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isChecked) EmeraldSuccess else TextMuted
                        )
                    }

                    if (isChecked) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "How perfectly did you do it? ${score.toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AmberWarning
                        )
                        Slider(
                            value = score,
                            onValueChange = { score = it },
                            valueRange = 0f..100f,
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = AmberWarning,
                                activeTrackColor = AmberWarning,
                                inactiveTrackColor = CharcoalBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("Quality Note / Perfection Details") },
                            placeholder = { Text("e.g. 20 min walk right after lunch, felt energizing") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberWarning,
                                unfocusedBorderColor = CharcoalBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            onRecordCompletion(activeDayDateStr, isChecked, score.toInt(), note)
                            showDayDetailDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Day Record", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
