package com.example.ui.screens.wellness

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.sin

@Composable
fun ExerciseMotionVisualizer(
    stepName: String,
    category: String,
    remainingSeconds: Int,
    totalSeconds: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "exerciseMotion")

    // Continuous rotation for rotational exercises (neck, shoulders)
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulse wave for stretching & spine decompression
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val progress = ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val centerOffset = center
            val radius = size.minDimension / 2.2f

            // Background circular track
            drawCircle(
                color = Color(0xFF26262B),
                radius = radius,
                style = Stroke(width = 6.dp.toPx())
            )

            // Progress Arc around the visualizer
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )

            val lowerName = stepName.lowercase()
            if (lowerName.contains("neck") || lowerName.contains("shoulder") || lowerName.contains("rotation")) {
                // Rotational guide: Animated rotating circular arc arrows
                rotate(degrees = rotationAngle) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color.Transparent, color.copy(alpha = 0.2f), color)
                        ),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color.Transparent, color.copy(alpha = 0.2f), color)
                        ),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            } else if (lowerName.contains("spine") || lowerName.contains("cat-cow") || lowerName.contains("decompression")) {
                // Spinal wave animation
                val waveRadius = radius * 0.7f * pulseScale
                drawCircle(
                    color = color.copy(alpha = 0.2f),
                    radius = waveRadius,
                    center = centerOffset
                )
                // Draw 5 vertebral points
                for (i in -2..2) {
                    val y = centerOffset.y + (i * 20f)
                    val x = centerOffset.x + (sin(rotationAngle * 0.05 + i) * 10f).toFloat()
                    drawCircle(color = color, radius = 5.dp.toPx(), center = Offset(x, y))
                }
            } else if (lowerName.contains("eye") || lowerName.contains("20-20-20")) {
                // Eye focus distance target: concentric pulsing target rings
                val r1 = radius * 0.35f * pulseScale
                val r2 = radius * 0.7f * pulseScale
                drawCircle(color = CyanAccent.copy(alpha = 0.25f), radius = r1)
                drawCircle(color = CyanAccent.copy(alpha = 0.5f), radius = r2, style = Stroke(width = 2.dp.toPx()))
                drawCircle(color = CyanAccent, radius = 6.dp.toPx())
            } else {
                // Default Yoga / Stretch soothing breath pulse core
                val coreRadius = (radius * 0.65f) * pulseScale
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.35f), Color.Transparent),
                        center = centerOffset,
                        radius = coreRadius
                    ),
                    radius = coreRadius,
                    center = centerOffset
                )
            }
        }

        // Center Countdown & Remaining label
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            Text(
                text = String.format("%02d:%02d", mins, secs),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "Hold & Follow",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
