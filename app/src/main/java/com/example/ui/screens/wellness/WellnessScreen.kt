package com.example.ui.screens.wellness

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.WellnessLogEntity
import com.example.data.entity.WellnessRoutineEntity
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
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject

data class RoutineStep(
    val name: String,
    val durationSeconds: Int,
    val cue: String
)

fun parseSteps(json: String): List<RoutineStep> {
    return try {
        val arr = JSONArray(json)
        val list = mutableListOf<RoutineStep>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                RoutineStep(
                    name = obj.optString("name", "Step ${i + 1}"),
                    durationSeconds = obj.optInt("durationSeconds", 30),
                    cue = obj.optString("cue", "")
                )
            )
        }
        list
    } catch (e: Exception) {
        listOf(RoutineStep("Gentle Flow", 180, "Breathe mindfully."))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessScreen(
    viewModel: RacerViewModel,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.allWellnessRoutines.collectAsState()
    val logs by viewModel.allWellnessLogs.collectAsState()

    var selectedCategory by remember { mutableStateOf("ALL") }
    var activePlayerRoutine by remember { mutableStateOf<WellnessRoutineEntity?>(null) }

    val categories = listOf(
        "ALL" to "All Practices",
        "YOGA" to "Yoga Flow",
        "MOBILITY" to "Mobility & Spine",
        "STRETCH" to "Deep Stretch",
        "BREATHING" to "Breathwork",
        "MENTAL_HEALTH" to "Mental Reset"
    )

    val filteredRoutines = remember(routines, selectedCategory) {
        if (selectedCategory == "ALL") routines
        else routines.filter { it.category == selectedCategory }
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
                        text = "Mental Health & Movement",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Yoga, posture decompression, deep somatic stretches & breathwork.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Category Filter chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(categories) { (catKey, label) ->
                        val isSelected = selectedCategory == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = catKey },
                            label = { Text(label, fontSize = 12.sp) },
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

            // Quick Stats Banner
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = CyanAccent.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(CyanAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = CyanAccent)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${logs.size} Mindful Sessions Completed",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                val totalSecs = logs.sumOf { it.durationSeconds }
                                Text(
                                    text = "${totalSecs / 60} mins total practice time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Practice items
            items(filteredRoutines, key = { it.id }) { routine ->
                WellnessRoutineCard(
                    routine = routine,
                    onStart = { activePlayerRoutine = routine }
                )
            }

            // Recent session logs
            if (logs.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Recent Mindful Logs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                items(logs.take(5)) { log ->
                    WellnessLogItemCard(log)
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (activePlayerRoutine != null) {
            WellnessPlayerBottomSheet(
                routine = activePlayerRoutine!!,
                onDismiss = { activePlayerRoutine = null },
                onComplete = { rating, notes, durationSecs ->
                    viewModel.logWellnessSession(
                        routineId = activePlayerRoutine!!.id,
                        routineTitle = activePlayerRoutine!!.title,
                        category = activePlayerRoutine!!.category,
                        durationSeconds = durationSecs,
                        feelingRating = rating,
                        notes = notes
                    )
                    activePlayerRoutine = null
                }
            )
        }
    }
}

@Composable
fun WellnessRoutineCard(
    routine: WellnessRoutineEntity,
    onStart: () -> Unit
) {
    val steps = remember(routine.stepsJson) { parseSteps(routine.stepsJson) }
    val tintColor = try {
        Color(android.graphics.Color.parseColor(routine.colorHex))
    } catch (e: Exception) {
        CyanAccent
    }

    val icon: ImageVector = when (routine.category) {
        "YOGA" -> Icons.Default.SelfImprovement
        "MOBILITY" -> Icons.Default.AccessibilityNew
        "STRETCH" -> Icons.Default.FitnessCenter
        "BREATHING" -> Icons.Default.Air
        else -> Icons.Default.Psychology
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStart() },
        borderColor = tintColor.copy(alpha = 0.35f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(tintColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = tintColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = routine.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${routine.category.replace('_', ' ')} • ${routine.durationMinutes} MINS • ${steps.size} STEPS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = tintColor
                        )
                    }
                }

                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = tintColor),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = routine.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun WellnessLogItemCard(log: WellnessLogEntity) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = log.routineTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "${log.category} • ${log.durationSeconds / 60}m ${log.durationSeconds % 60}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (log.reflectionNotes.isNotBlank()) {
                    Text(
                        text = "\"${log.reflectionNotes}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            Row {
                repeat(log.feelingRating) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = AmberWarning,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessPlayerBottomSheet(
    routine: WellnessRoutineEntity,
    onDismiss: () -> Unit,
    onComplete: (rating: Int, notes: String, durationSecs: Int) -> Unit
) {
    val steps = remember(routine.stepsJson) { parseSteps(routine.stepsJson) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var stepRemainingSecs by remember {
        mutableIntStateOf(if (steps.isNotEmpty()) steps[0].durationSeconds else 60)
    }
    var isPaused by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var totalElapsedSecs by remember { mutableIntStateOf(0) }

    var feelingRating by remember { mutableIntStateOf(5) }
    var reflectionNote by remember { mutableStateOf("") }

    val currentStep = steps.getOrNull(currentStepIndex) ?: RoutineStep("Mindful Rest", 30, "Rest and breathe.")

    val tintColor = try {
        Color(android.graphics.Color.parseColor(routine.colorHex))
    } catch (e: Exception) {
        CyanAccent
    }

    LaunchedEffect(currentStepIndex) {
        stepRemainingSecs = steps.getOrNull(currentStepIndex)?.durationSeconds ?: 30
    }

    LaunchedEffect(isPaused, isFinished, stepRemainingSecs) {
        if (!isPaused && !isFinished) {
            delay(1000L)
            totalElapsedSecs++
            if (stepRemainingSecs > 1) {
                stepRemainingSecs--
            } else {
                if (currentStepIndex < steps.size - 1) {
                    currentStepIndex++
                } else {
                    isFinished = true
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CharcoalCardElevated,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isFinished) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = routine.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Indicator
                Text(
                    text = "Step ${currentStepIndex + 1} of ${steps.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = tintColor,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Step Name & Countdown Circle
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(tintColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val mins = stepRemainingSecs / 60
                        val secs = stepRemainingSecs % 60
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Remaining",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = currentStep.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentStep.cue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Controls: Pause / Next Step
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { isPaused = !isPaused },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaused) EmeraldSuccess else CharcoalBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isPaused) "Resume" else "Pause", color = TextPrimary)
                    }

                    Button(
                        onClick = {
                            if (currentStepIndex < steps.size - 1) {
                                currentStepIndex++
                            } else {
                                isFinished = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = tintColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (currentStepIndex < steps.size - 1) "Next Step" else "Complete Practice")
                    }
                }
            } else {
                // Practice Completed State
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Practice Complete!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "You practiced for ${totalElapsedSecs / 60}m ${totalElapsedSecs % 60}s. How do you feel?",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Feeling 1 to 5 rating
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = { feelingRating = star },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "$star Stars",
                                tint = if (star <= feelingRating) AmberWarning else TextMuted,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = reflectionNote,
                    onValueChange = { reflectionNote = it },
                    label = { Text("Reflection or physical sensation note") },
                    placeholder = { Text("e.g. Neck feels lighter, nervous system calm...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CharcoalBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onComplete(feelingRating, reflectionNote, totalElapsedSecs)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save Mindful Log", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
