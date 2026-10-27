package com.example.ui.screens.routine

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DailyRoutineEntity
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRoutineScreen(
    viewModel: RacerViewModel,
    onNavigateToFocus: () -> Unit,
    onNavigateToWellness: () -> Unit,
    onNavigateToJournal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.allDailyRoutines.collectAsState()
    var selectedSlot by remember { mutableStateOf("ALL") }
    var showAddSheet by remember { mutableStateOf(false) }

    val slots = listOf(
        "ALL" to "Full Day",
        "MORNING" to "🌅 Morning",
        "DEEP_WORK" to "⚡ Deep Work",
        "MIDDAY" to "☀️ Midday",
        "AFTERNOON" to "🎯 Afternoon",
        "EVENING" to "🌙 Evening",
        "NIGHT" to "😴 Wind Down"
    )

    val filteredList = remember(routines, selectedSlot) {
        if (selectedSlot == "ALL") routines
        else routines.filter { it.timeSlot == selectedSlot }
    }

    val totalCount = routines.size
    val completedCount = routines.count { it.isCompletedToday }
    val progressRate = if (totalCount > 0) (completedCount.toFloat() / totalCount * 100f) else 0f

    // 70% threshold rule: Red if < 70%, Green if >= 70%
    val isAboveThreshold = progressRate >= 70f
    val progressColor by animateColorAsState(
        targetValue = if (isAboveThreshold) EmeraldSuccess else RoseUrgent,
        label = "routineProgressColor"
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Routine Schedule",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Structured circadian flow & high-energy anchors",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.resetDailyRoutines() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CharcoalCardElevated)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reset Routine",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Overall Routine Completion Meter (<70% Red vs >=70% Green)
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = progressColor.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Daily Routine Progress",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (totalCount == 0) "No routine steps added yet" else "$completedCount of $totalCount steps completed today",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            // 70% Badge Indicator
                            if (totalCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = progressColor.copy(alpha = 0.18f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, progressColor.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = if (isAboveThreshold) {
                                            "🟢 ${progressRate.toInt()}% (Above 70% Target)"
                                        } else {
                                            "🔴 ${progressRate.toInt()}% (Below 70% Target)"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = progressColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CharcoalCardElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
                                ) {
                                    Text(
                                        text = "Custom Routine",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress track
                        LinearProgressIndicator(
                            progress = { (progressRate / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = progressColor,
                            trackColor = CharcoalBorder,
                            strokeCap = StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isAboveThreshold) "Target Achieved! Optimal momentum." else "Keep going to reach the 70% green threshold.",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAboveThreshold) EmeraldSuccess else RoseUrgent,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Benchmark: 70%",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Time Slot Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    slots.forEach { (slotKey, label) ->
                        val isSelected = selectedSlot == slotKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSlot = slotKey },
                            label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent.copy(alpha = 0.25f),
                                selectedLabelColor = CyanAccent,
                                containerColor = CharcoalCardElevated,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) CyanAccent else CharcoalBorder
                            )
                        )
                    }
                }
            }

            // Routine Timeline List
            if (filteredList.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No routine blocks in this slot.", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showAddSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                            ) {
                                Text("Add Custom Step", color = Color.White)
                            }
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { routine ->
                    DailyRoutineItemCard(
                        routine = routine,
                        onToggle = { viewModel.toggleDailyRoutine(routine.id) },
                        onDelete = { viewModel.deleteDailyRoutine(routine.id) },
                        onExecuteAction = { action ->
                            when (action) {
                                "BREATHING" -> onNavigateToWellness()
                                "YOGA", "STRETCH" -> onNavigateToWellness()
                                "FOCUS" -> onNavigateToFocus()
                                "JOURNAL" -> onNavigateToJournal()
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Add Routine FAB
        FloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = VioletPrimary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Routine Step")
        }

        if (showAddSheet) {
            AddDailyRoutineSheet(
                onDismiss = { showAddSheet = false },
                onAdd = { title, slot, time, dur, cue, action, color ->
                    viewModel.addDailyRoutine(
                        title = title,
                        timeSlot = slot,
                        scheduledTime = time,
                        durationMinutes = dur,
                        cueNote = cue,
                        actionType = action,
                        colorHex = color
                    )
                    showAddSheet = false
                }
            )
        }
    }
}

@Composable
fun DailyRoutineItemCard(
    routine: DailyRoutineEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onExecuteAction: (String) -> Unit
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(routine.colorHex))
    } catch (_: Exception) {
        CyanAccent
    }

    val icon: ImageVector = when (routine.iconName) {
        "wb_sunny" -> Icons.Default.WbSunny
        "air" -> Icons.Default.Air
        "self_improvement" -> Icons.Default.SelfImprovement
        "check_circle" -> Icons.Default.CheckCircle
        "timer" -> Icons.Default.Timer
        "accessibility_new" -> Icons.Default.AccessibilityNew
        "restaurant" -> Icons.Default.Restaurant
        "directions_walk" -> Icons.AutoMirrored.Filled.DirectionsWalk
        "edit_note" -> Icons.Default.EditNote
        "bedtime" -> Icons.Default.Bedtime
        else -> Icons.Default.Schedule
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        borderColor = if (routine.isCompletedToday) EmeraldSuccess.copy(alpha = 0.5f) else accentColor.copy(alpha = 0.25f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Scheduled time tag + Icon + Title
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Time pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = routine.scheduledTime,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (routine.isCompletedToday) EmeraldSuccess.copy(alpha = 0.2f) else accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (routine.isCompletedToday) EmeraldSuccess else accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = routine.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (routine.isCompletedToday) TextMuted else TextPrimary,
                            textDecoration = if (routine.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Text(
                            text = "${routine.timeSlot.replace('_', ' ')} • ${routine.durationMinutes} MINS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                // Completion Toggle Checkbox Circle
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (routine.isCompletedToday) EmeraldSuccess else CharcoalCardElevated)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Complete Routine",
                        tint = if (routine.isCompletedToday) Color.Black else TextDisabled,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (routine.cueNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = routine.cueNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }

            // Action Launch Button & Delete
            if (routine.actionType != "NONE" || routine.cueNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (routine.actionType != "NONE") {
                        Button(
                            onClick = { onExecuteAction(routine.actionType) },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            val actionLabel = when (routine.actionType) {
                                "BREATHING" -> "Practice Box Breath"
                                "YOGA" -> "Start Yoga Flow"
                                "STRETCH" -> "Start Desk Stretch"
                                "FOCUS" -> "Launch Focus Timer"
                                "JOURNAL" -> "Open Daily Journal"
                                else -> "Launch Activity"
                            }
                            Text(actionLabel, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = accentColor, modifier = Modifier.size(12.dp))
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDailyRoutineSheet(
    onDismiss: () -> Unit,
    onAdd: (title: String, slot: String, time: String, duration: Int, cue: String, action: String, color: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedSlot by remember { mutableStateOf("MORNING") }
    var scheduledTime by remember { mutableStateOf("07:00") }
    var durationMinutes by remember { mutableIntStateOf(15) }
    var cueNote by remember { mutableStateOf("") }
    var actionType by remember { mutableStateOf("NONE") }
    var selectedColor by remember { mutableStateOf("#8B5CF6") }

    val slotOptions = listOf(
        "MORNING" to "Morning",
        "DEEP_WORK" to "Deep Work",
        "MIDDAY" to "Midday",
        "AFTERNOON" to "Afternoon",
        "EVENING" to "Evening",
        "NIGHT" to "Night"
    )

    val actionOptions = listOf(
        "NONE" to "None",
        "BREATHING" to "Box Breath",
        "FOCUS" to "Focus Timer",
        "YOGA" to "Yoga Flow",
        "STRETCH" to "Desk Stretch",
        "JOURNAL" to "Journal"
    )

    val timePresets = listOf("06:30", "07:00", "08:30", "09:00", "12:30", "14:30", "18:00", "21:00", "22:30")
    val colorOptions = listOf("#8B5CF6", "#EC4899", "#06B6D4", "#10B981", "#F59E0B", "#6366F1")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CharcoalCardElevated,
        contentColor = TextPrimary
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add Routine Step", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Routine Step Title") },
                    placeholder = { Text("e.g. 500ml Hydration, Box Breathing 4-4-4-4...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CharcoalBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }

            // Time Slot selection
            item {
                Text("Circadian Time Slot", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    slotOptions.forEach { (slotKey, label) ->
                        FilterChip(
                            selected = selectedSlot == slotKey,
                            onClick = { selectedSlot = slotKey },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent.copy(alpha = 0.25f),
                                selectedLabelColor = CyanAccent
                            )
                        )
                    }
                }
            }

            // Scheduled Time & Duration
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = scheduledTime,
                        onValueChange = { scheduledTime = it },
                        label = { Text("Time (HH:mm)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = durationMinutes.toString(),
                        onValueChange = { durationMinutes = it.toIntOrNull() ?: 15 },
                        label = { Text("Duration (Mins)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    timePresets.forEach { preset ->
                        FilterChip(
                            selected = scheduledTime == preset,
                            onClick = { scheduledTime = preset },
                            label = { Text(preset, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent.copy(alpha = 0.25f),
                                selectedLabelColor = CyanAccent
                            )
                        )
                    }
                }
            }

            // Cue / Trigger Note
            item {
                OutlinedTextField(
                    value = cueNote,
                    onValueChange = { cueNote = it },
                    label = { Text("Habit Cue / Action Note (Optional)") },
                    placeholder = { Text("e.g. Immediately upon stepping out of bed...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CharcoalBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }

            // Action link
            item {
                Text("Linked In-App Activity", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    actionOptions.forEach { (actionKey, label) ->
                        FilterChip(
                            selected = actionType == actionKey,
                            onClick = { actionType = actionKey },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VioletPrimary.copy(alpha = 0.25f),
                                selectedLabelColor = VioletPrimary
                            )
                        )
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
                        if (title.isNotBlank()) {
                            onAdd(title, selectedSlot, scheduledTime, durationMinutes, cueNote, actionType, selectedColor)
                        }
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Routine Step", fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
