package com.example.ui.screens.wellness

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.WellnessLogEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.RacerViewModel
import kotlinx.coroutines.delay

enum class BreathTechnique(
    val title: String,
    val description: String,
    val inhaleSec: Int,
    val holdSec: Int,
    val exhaleSec: Int,
    val postHoldSec: Int,
    val totalRounds: Int
) {
    BOX(
        title = "Box breathing",
        description = "Even in, even hold, even out. Steadies a racing mind before hard work.",
        inhaleSec = 4,
        holdSec = 4,
        exhaleSec = 4,
        postHoldSec = 4,
        totalRounds = 6
    ),
    FOUR_SEVEN_EIGHT(
        title = "4-7-8",
        description = "Natural tranquilizer for the nervous system. Calms anxiety and resets fight-or-flight.",
        inhaleSec = 4,
        holdSec = 7,
        exhaleSec = 8,
        postHoldSec = 0,
        totalRounds = 4
    ),
    PHYSIOLOGICAL_SIGH(
        title = "Physiological sigh",
        description = "Double inhale through the nose, long sigh exhale through mouth. Rapid vagal reset.",
        inhaleSec = 3,
        holdSec = 1,
        exhaleSec = 6,
        postHoldSec = 0,
        totalRounds = 5
    ),
    COHERENT(
        title = "Coherent 5-5",
        description = "Smooth equal rhythm. Synchronizes heart rate variability and mental clarity.",
        inhaleSec = 5,
        holdSec = 0,
        exhaleSec = 5,
        postHoldSec = 0,
        totalRounds = 6
    )
}

data class MovePractice(
    val id: String,
    val title: String,
    val subtitle: String,
    val durationMinutes: Int,
    val steps: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessScreen(
    viewModel: RacerViewModel,
    modifier: Modifier = Modifier
) {
    // 0: Breathe, 1: Move, 2: Ground (matches Screenshot 2, 3, 4)
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabNames = listOf("Breathe", "Move", "Ground")

    // Move Practices (matches Screenshot 3 + user requested extras)
    val movePractices = listOf(
        MovePractice(
            id = "desk_reset",
            title = "Desk reset",
            subtitle = "For when you have been folded over a laptop for two hours.",
            durationMinutes = 4,
            steps = listOf(
                "Interlace hands behind back, open chest (45s)",
                "Gentle seated spinal twist left & right (60s)",
                "Wrist extensions and finger spreads (45s)",
                "Standing hip hinge with deep forward fold (90s)"
            )
        ),
        MovePractice(
            id = "morning_mobility",
            title = "Morning mobility",
            subtitle = "Wake the spine up before the day asks anything of it.",
            durationMinutes = 6,
            steps = listOf(
                "Cat-cow gentle spine ripples (60s)",
                "World's greatest stretch (left side) (60s)",
                "World's greatest stretch (right side) (60s)",
                "Deep squat with thoracic reach (90s)",
                "Standing side bends with full inhales (90s)"
            )
        ),
        MovePractice(
            id = "evening_wind_down",
            title = "Evening wind-down",
            subtitle = "Signals to the body that the day is closing.",
            durationMinutes = 5,
            steps = listOf(
                "Supported child's pose with forehead grounded (90s)",
                "Puppy pose shoulder melts (60s)",
                "Supine figure four hip release (left & right) (90s)",
                "Legs up the wall or gentle corpse posture (60s)"
            )
        ),
        MovePractice(
            id = "neck_shoulder",
            title = "Neck & shoulder release",
            subtitle = "Relieves tension from long typing sprints.",
            durationMinutes = 3,
            steps = listOf(
                "Chin tucks with gentle neck rotations (45s)",
                "Ear to shoulder lateral trap release (45s)",
                "Eagle arms stretch upper back (45s)",
                "Shoulder blade pinches with deep exhales (45s)"
            )
        ),
        MovePractice(
            id = "standing_hip_opener",
            title = "Standing hip opener",
            subtitle = "Counters long hours of sitting.",
            durationMinutes = 5,
            steps = listOf(
                "Standing quad pull with balance lock (60s)",
                "High lunge hip flexor drive (left) (60s)",
                "High lunge hip flexor drive (right) (60s)",
                "Wide stance warrior II hold (left & right) (120s)"
            )
        ),
        MovePractice(
            id = "sun_salutation",
            title = "Sun salutation flow",
            subtitle = "Gentle energizing full body sequence.",
            durationMinutes = 7,
            steps = listOf(
                "Mountain pose grounding & arms sweep (60s)",
                "Forward fold & half lift (60s)",
                "Plank to Cobra heart opener (90s)",
                "Downward-facing dog pedal (120s)",
                "Stepping forward to mountain (90s)"
            )
        ),
        MovePractice(
            id = "post_sprint_stretch",
            title = "Post-sprint stretch",
            subtitle = "Hamstrings, chest and spine reset.",
            durationMinutes = 4,
            steps = listOf(
                "Standing hamstring reach (60s)",
                "Chest doorframe opener (60s)",
                "Standing spinal decompression (60s)",
                "Palms on thighs deep reset sigh (60s)"
            )
        )
    )

    var activeMovePractice by remember { mutableStateOf<MovePractice?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Small Category Header
        Text(
            text = "MENTAL & YOGA",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Big Main Header
        Text(
            text = "Down-shift on purpose",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Subtitle
        Text(
            text = "Two minutes of breathing changes more than an hour of trying harder. Pick one and stop when it's enough.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 3 Segmented Tabs: [Breathe] [Move] [Ground] (Matches Screenshot 2, 3, 4)
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CharcoalCardElevated,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tabNames.forEachIndexed { index, name ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFF1E1B2E) else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) TextPrimary else TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Content Area by Tab
        when (selectedTab) {
            0 -> BreatheTabContent(viewModel = viewModel)
            1 -> MoveTabContent(
                practices = movePractices,
                onSelectPractice = { activeMovePractice = it }
            )
            2 -> GroundTabContent(viewModel = viewModel)
        }
    }

    // Active Move Practice Modal
    activeMovePractice?.let { practice ->
        MovePracticeDetailSheet(
            practice = practice,
            onDismiss = { activeMovePractice = null },
            onComplete = {
                viewModel.awardCoins(20, "Completed Yoga / Mobility: ${practice.title}")
                activeMovePractice = null
            }
        )
    }
}

@Composable
private fun BreatheTabContent(viewModel: RacerViewModel) {
    var selectedTechnique by remember { mutableStateOf(BreathTechnique.BOX) }
    var isRunning by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf("INHALE") } // INHALE, HOLD, EXHALE, REST
    var countdownSec by remember { mutableIntStateOf(selectedTechnique.inhaleSec) }
    var currentRound by remember { mutableIntStateOf(1) }

    // Reset countdown when switching technique
    LaunchedEffect(selectedTechnique) {
        isRunning = false
        currentPhase = "INHALE"
        countdownSec = selectedTechnique.inhaleSec
        currentRound = 1
    }

    // Animated Breathing Timer Engine
    LaunchedEffect(isRunning, currentPhase, countdownSec, currentRound) {
        if (!isRunning) return@LaunchedEffect

        delay(1000L)
        if (countdownSec > 1) {
            countdownSec -= 1
        } else {
            // Transition to next phase
            when (currentPhase) {
                "INHALE" -> {
                    if (selectedTechnique.holdSec > 0) {
                        currentPhase = "HOLD"
                        countdownSec = selectedTechnique.holdSec
                    } else {
                        currentPhase = "EXHALE"
                        countdownSec = selectedTechnique.exhaleSec
                    }
                }
                "HOLD" -> {
                    currentPhase = "EXHALE"
                    countdownSec = selectedTechnique.exhaleSec
                }
                "EXHALE" -> {
                    if (selectedTechnique.postHoldSec > 0) {
                        currentPhase = "HOLD"
                        countdownSec = selectedTechnique.postHoldSec
                    } else {
                        // Complete 1 round
                        if (currentRound < selectedTechnique.totalRounds) {
                            currentRound += 1
                            currentPhase = "INHALE"
                            countdownSec = selectedTechnique.inhaleSec
                        } else {
                            isRunning = false
                            viewModel.awardCoins(15, "Completed breathing session: ${selectedTechnique.title}")
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Technique Pills Row (Matches Screenshot 2)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(BreathTechnique.entries) { tech ->
                val isSelected = tech == selectedTechnique
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Color(0xFF2E1065) else CharcoalCard,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) VioletPrimary else Color(0xFF27272A)
                    ),
                    modifier = Modifier.clickable {
                        selectedTechnique = tech
                    }
                ) {
                    Text(
                        text = tech.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFFD8B4FE) else TextSecondary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Subtitle description for chosen technique
        Text(
            text = selectedTechnique.description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Animated Circular Dial (Matches Screenshot 2: INHALE, large number 4, Round 1 of 6)
        val targetScale = when (currentPhase) {
            "INHALE" -> 1.18f
            "HOLD" -> 1.18f
            "EXHALE" -> 0.88f
            else -> 1.0f
        }
        val animatedScale by animateFloatAsState(
            targetValue = if (isRunning) targetScale else 1.0f,
            animationSpec = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            label = "breath_scale"
        )

        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(animatedScale)
                .clip(CircleShape)
                .background(Color(0xFF1E1B2E))
                .border(
                    width = 2.dp,
                    color = when (currentPhase) {
                        "INHALE" -> VioletPrimary
                        "HOLD" -> CyanAccent
                        "EXHALE" -> EmeraldSuccess
                        else -> VioletPrimary.copy(alpha = 0.5f)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = currentPhase,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "$countdownSec",
                    fontSize = 46.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Round $currentRound of ${selectedTechnique.totalRounds}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Control Buttons: [Start / Pause] and [Reset] (Matches Screenshot 2)
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = VioletPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(46.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRunning) "Pause" else "Start",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            OutlinedButton(
                onClick = {
                    isRunning = false
                    currentPhase = "INHALE"
                    countdownSec = selectedTechnique.inhaleSec
                    currentRound = 1
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F46)),
                modifier = Modifier.height(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset", fontWeight = FontWeight.Medium, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun MoveTabContent(
    practices: List<MovePractice>,
    onSelectPractice: (MovePractice) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(practices) { practice ->
            // Card matching Screenshot 3
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CharcoalCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPractice(practice) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = practice.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = practice.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Text(
                        text = "${practice.durationMinutes} min",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA78BFA),
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GroundTabContent(viewModel: RacerViewModel) {
    // 5-4-3-2-1 Sensory Grounding List (Matches Screenshot 4)
    val groundingSteps = listOf(
        Triple(5, "things you can see", "Name them silently, slowly."),
        Triple(4, "things you can touch", "Press your feet down. Feel the fabric."),
        Triple(3, "things you can hear", "Near sounds, then far ones."),
        Triple(2, "things you can smell", "Or two smells you like."),
        Triple(1, "thing you can taste", "One sip of water counts.")
    )

    var checkedSteps by remember { mutableStateOf(setOf<Int>()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Notice text from Screenshot 4
            Text(
                text = "When your head is loud, walk down this list slowly. Out loud works better than silently.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        items(groundingSteps) { (number, title, subtitle) ->
            val isChecked = checkedSteps.contains(number)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isChecked) Color(0xFF1E1B2E) else CharcoalCardElevated,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isChecked) VioletPrimary.copy(alpha = 0.6f) else Color(0xFF27272A)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        checkedSteps = if (isChecked) {
                            checkedSteps - number
                        } else {
                            val next = checkedSteps + number
                            if (next.size == 5) {
                                viewModel.awardCoins(25, "Completed 5-4-3-2-1 Grounding Reset")
                            }
                            next
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Badge with Step Number (Matches Screenshot 4)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isChecked) EmeraldSuccess.copy(alpha = 0.25f) else Color(0xFF2E1065)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChecked) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "$number",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFFD8B4FE)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovePracticeDetailSheet(
    practice: MovePractice,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    var timerRunning by remember { mutableStateOf(false) }
    var secondsRemaining by remember { mutableIntStateOf(practice.durationMinutes * 60) }
    var currentStepIdx by remember { mutableIntStateOf(0) }

    LaunchedEffect(timerRunning, secondsRemaining) {
        if (!timerRunning || secondsRemaining <= 0) return@LaunchedEffect
        delay(1000L)
        secondsRemaining -= 1
        if (secondsRemaining == 0) {
            timerRunning = false
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = practice.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${practice.durationMinutes} minute sequence",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            // Timer display
            val mins = secondsRemaining / 60
            val secs = secondsRemaining % 60
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1B2E))
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "%02d:%02d".format(mins, secs),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = VioletPrimary
                )
            }

            // Sequence steps
            Text(
                text = "Guided Movements",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )

            practice.steps.forEachIndexed { idx, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (idx == currentStepIdx) VioletPrimary.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { currentStepIdx = idx }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (idx == currentStepIdx) VioletPrimary else Color(0xFF27272A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${idx + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (idx == currentStepIdx) TextPrimary else TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { timerRunning = !timerRunning },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(if (timerRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (timerRunning) "Pause" else "Start Timer")
                }

                Button(
                    onClick = onComplete,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Done (+20 Coins)")
                }
            }
        }
    }
}
