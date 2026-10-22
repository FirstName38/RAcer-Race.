package com.example.ui.screens.wellness

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoCalm
import com.example.ui.theme.RoseUrgent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import kotlinx.coroutines.delay

enum class BoxPhase(val title: String, val durationSecs: Int, val cue: String, val color: Color) {
    INHALE("INHALE", 4, "Breathe in deeply through your nose. Fill your belly and ribcage.", CyanAccent),
    HOLD_FULL("HOLD", 4, "Gently hold your breath with full lungs. Relax your shoulders.", IndigoCalm),
    EXHALE("EXHALE", 4, "Exhale slowly and smoothly through your mouth. Empty completely.", EmeraldSuccess),
    HOLD_EMPTY("HOLD", 4, "Pause in stillness with empty lungs. Soften your jaw.", VioletPrimary)
}

enum class FourSevenEightPhase(val title: String, val durationSecs: Int, val cue: String, val color: Color) {
    INHALE("INHALE", 4, "Quietly inhale through your nose to a count of 4.", CyanAccent),
    HOLD("HOLD", 7, "Hold your breath calmly for 7 seconds. Let peace settle in.", IndigoCalm),
    EXHALE("EXHALE", 8, "Make a gentle whoosh sound, exhaling completely for 8 seconds.", EmeraldSuccess)
}

@Composable
fun BoxBreathingStudio(
    onFinish: (durationSeconds: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var is478Mode by remember { mutableStateOf(false) }

    // Box Breathing State
    var currentBoxPhaseIndex by remember { mutableIntStateOf(0) }
    val boxPhases = BoxPhase.entries
    val currentBoxPhase = boxPhases[currentBoxPhaseIndex]

    // 4-7-8 State
    var current478PhaseIndex by remember { mutableIntStateOf(0) }
    val fourSevenEightPhases = FourSevenEightPhase.entries
    val current478Phase = fourSevenEightPhases[current478PhaseIndex]

    val activeDuration = if (is478Mode) current478Phase.durationSecs else currentBoxPhase.durationSecs
    var secondsCountdown by remember(currentBoxPhaseIndex, current478PhaseIndex, is478Mode) {
        mutableIntStateOf(activeDuration)
    }

    var isPaused by remember { mutableStateOf(false) }
    var cycleCount by remember { mutableIntStateOf(1) }
    val targetCycles = 6
    var totalElapsedSecs by remember { mutableIntStateOf(0) }

    // Visual scale animation for expansion/contraction
    val animScale = remember { Animatable(0.5f) }

    // Trigger haptic feedback on phase change
    fun triggerPhaseHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            }
        } catch (_: Exception) {}
    }

    // Phase transition countdown loop (exact 4-second steps)
    LaunchedEffect(isPaused, secondsCountdown, is478Mode, currentBoxPhaseIndex, current478PhaseIndex) {
        if (!isPaused) {
            delay(1000L)
            totalElapsedSecs++
            if (secondsCountdown > 1) {
                secondsCountdown--
            } else {
                // Next phase transition
                triggerPhaseHaptic()
                if (!is478Mode) {
                    if (currentBoxPhaseIndex < boxPhases.size - 1) {
                        currentBoxPhaseIndex++
                    } else {
                        currentBoxPhaseIndex = 0
                        cycleCount++
                    }
                    secondsCountdown = boxPhases[currentBoxPhaseIndex].durationSecs
                } else {
                    if (current478PhaseIndex < fourSevenEightPhases.size - 1) {
                        current478PhaseIndex++
                    } else {
                        current478PhaseIndex = 0
                        cycleCount++
                    }
                    secondsCountdown = fourSevenEightPhases[current478PhaseIndex].durationSecs
                }
            }
        }
    }

    // Animate box/circle size based on breathing phase
    LaunchedEffect(currentBoxPhaseIndex, current478PhaseIndex, is478Mode) {
        if (!is478Mode) {
            when (currentBoxPhase) {
                BoxPhase.INHALE -> animScale.animateTo(1.0f, tween(4000, easing = LinearEasing))
                BoxPhase.HOLD_FULL -> { /* stay expanded */ }
                BoxPhase.EXHALE -> animScale.animateTo(0.45f, tween(4000, easing = LinearEasing))
                BoxPhase.HOLD_EMPTY -> { /* stay compact */ }
            }
        } else {
            when (current478Phase) {
                FourSevenEightPhase.INHALE -> animScale.animateTo(1.0f, tween(4000, easing = LinearEasing))
                FourSevenEightPhase.HOLD -> { /* hold */ }
                FourSevenEightPhase.EXHALE -> animScale.animateTo(0.45f, tween(8000, easing = LinearEasing))
            }
        }
    }

    val activeColor = if (!is478Mode) currentBoxPhase.color else current478Phase.color
    val activeTitle = if (!is478Mode) currentBoxPhase.title else current478Phase.title
    val activeCue = if (!is478Mode) currentBoxPhase.cue else current478Phase.cue

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector: Box Breathing (4-4-4-4) vs 4-7-8 Relax
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CharcoalCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (!is478Mode) CyanAccent.copy(alpha = 0.25f) else Color.Transparent,
                        modifier = Modifier.clickable {
                            is478Mode = false
                            currentBoxPhaseIndex = 0
                            secondsCountdown = 4
                        }
                    ) {
                        Text(
                            text = "Box Breath 4-4-4-4",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (!is478Mode) CyanAccent else TextMuted,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (is478Mode) EmeraldSuccess.copy(alpha = 0.25f) else Color.Transparent,
                        modifier = Modifier.clickable {
                            is478Mode = true
                            current478PhaseIndex = 0
                            secondsCountdown = 4
                        }
                    ) {
                        Text(
                            text = "4-7-8 Deep Sleep",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (is478Mode) EmeraldSuccess else TextMuted,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cycle counter & progress badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = activeColor.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, activeColor.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "CYCLE $cycleCount OF $targetCycles",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = activeColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Animated Interactive Box Canvas
        Box(
            modifier = Modifier
                .size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(240.dp)) {
                val boxSize = size.width
                val cornerRadius = 24.dp.toPx()
                val strokeWidth = 5.dp.toPx()

                // Background subtle guide box
                drawRoundRect(
                    color = Color(0xFF26262B),
                    size = Size(boxSize, boxSize),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = 2.dp.toPx())
                )

                if (!is478Mode) {
                    // Highlight the active perimeter side based on 4-second box phase!
                    // Phase 0: Top Edge (Inhale 4s)
                    // Phase 1: Right Edge (Hold 4s)
                    // Phase 2: Bottom Edge (Exhale 4s)
                    // Phase 3: Left Edge (Hold Empty 4s)
                    val activeStrokeWidth = 8.dp.toPx()
                    val progressInPhase = (activeDuration - secondsCountdown + 1).toFloat() / activeDuration.toFloat()

                    when (currentBoxPhase) {
                        BoxPhase.INHALE -> {
                            // Top edge from left to right
                            val endX = boxSize * progressInPhase.coerceIn(0.1f, 1f)
                            drawLine(
                                color = CyanAccent,
                                start = Offset(0f, 0f),
                                end = Offset(endX, 0f),
                                strokeWidth = activeStrokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                        BoxPhase.HOLD_FULL -> {
                            // Draw completed top edge + Right edge moving down
                            drawLine(
                                color = CyanAccent.copy(alpha = 0.6f),
                                start = Offset(0f, 0f),
                                end = Offset(boxSize, 0f),
                                strokeWidth = strokeWidth
                            )
                            val endY = boxSize * progressInPhase.coerceIn(0.1f, 1f)
                            drawLine(
                                color = IndigoCalm,
                                start = Offset(boxSize, 0f),
                                end = Offset(boxSize, endY),
                                strokeWidth = activeStrokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                        BoxPhase.EXHALE -> {
                            // Completed top & right edges + Bottom edge moving right to left
                            drawLine(color = CyanAccent.copy(alpha = 0.4f), start = Offset(0f, 0f), end = Offset(boxSize, 0f), strokeWidth = strokeWidth)
                            drawLine(color = IndigoCalm.copy(alpha = 0.4f), start = Offset(boxSize, 0f), end = Offset(boxSize, boxSize), strokeWidth = strokeWidth)
                            val endX = boxSize * (1f - progressInPhase).coerceIn(0f, 0.9f)
                            drawLine(
                                color = EmeraldSuccess,
                                start = Offset(boxSize, boxSize),
                                end = Offset(endX, boxSize),
                                strokeWidth = activeStrokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                        BoxPhase.HOLD_EMPTY -> {
                            // Completed 3 edges + Left edge moving bottom to top
                            drawLine(color = CyanAccent.copy(alpha = 0.3f), start = Offset(0f, 0f), end = Offset(boxSize, 0f), strokeWidth = strokeWidth)
                            drawLine(color = IndigoCalm.copy(alpha = 0.3f), start = Offset(boxSize, 0f), end = Offset(boxSize, boxSize), strokeWidth = strokeWidth)
                            drawLine(color = EmeraldSuccess.copy(alpha = 0.3f), start = Offset(boxSize, boxSize), end = Offset(0f, boxSize), strokeWidth = strokeWidth)
                            val endY = boxSize * (1f - progressInPhase).coerceIn(0f, 0.9f)
                            drawLine(
                                color = VioletPrimary,
                                start = Offset(0f, boxSize),
                                end = Offset(0f, endY),
                                strokeWidth = activeStrokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                } else {
                    // 4-7-8 mode: Circular breathing pulse arc
                    val sweep = 360f * ((activeDuration - secondsCountdown + 1).toFloat() / activeDuration.toFloat())
                    drawArc(
                        color = activeColor,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner breathing core (smoothly scaling)
                val innerRadius = (boxSize * 0.38f) * animScale.value
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            activeColor.copy(alpha = 0.35f),
                            activeColor.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = innerRadius
                    ),
                    radius = innerRadius,
                    center = center
                )
                drawCircle(
                    color = activeColor.copy(alpha = 0.6f),
                    radius = innerRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Giant Countdown & Phase Text inside Box
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$secondsCountdown",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = TextPrimary
                )
                Text(
                    text = activeTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = activeColor,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "${activeDuration}s step",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Phase Guidance Text
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = CharcoalCardElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = activeCue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Controls: Pause / Next Phase / Complete
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isPaused = !isPaused },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPaused) EmeraldSuccess else CharcoalBorder
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    tint = TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isPaused) "Resume" else "Pause", color = TextPrimary)
            }

            Button(
                onClick = {
                    triggerPhaseHaptic()
                    if (!is478Mode) {
                        currentBoxPhaseIndex = (currentBoxPhaseIndex + 1) % boxPhases.size
                        secondsCountdown = boxPhases[currentBoxPhaseIndex].durationSecs
                    } else {
                        current478PhaseIndex = (current478PhaseIndex + 1) % fourSevenEightPhases.size
                        secondsCountdown = fourSevenEightPhases[current478PhaseIndex].durationSecs
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next Phase", tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Next Phase", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onFinish(totalElapsedSecs) },
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Finish", color = TextPrimary)
            }
        }
    }
}
