package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FocusMode
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryGradient
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = CharcoalBorder,
    backgroundColor: Color = CharcoalCard,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val cardModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .clickable { onClick() }
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
    } else {
        modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
    }

    Box(
        modifier = cardModifier.padding(16.dp),
        content = content
    )
}

@Composable
fun GlowingOrb(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    isPulsing: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val currentScale = if (isPulsing) scale else 1f

    val orbModifier = if (onClick != null) {
        modifier
            .size(size)
            .clip(CircleShape)
            .clickable { onClick() }
    } else {
        modifier.size(size)
    }

    Box(
        modifier = orbModifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = (this.size.minDimension / 2f) * currentScale
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Outer soft glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        VioletPrimary.copy(alpha = 0.45f),
                        CyanAccent.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.35f
                ),
                radius = radius * 1.35f,
                center = center
            )

            // Inner core orb
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(VioletPrimary, CyanAccent),
                    start = Offset(center.x - radius, center.y - radius),
                    end = Offset(center.x + radius, center.y + radius)
                ),
                radius = radius * 0.75f,
                center = center
            )
        }
    }
}

@Composable
fun FocusTimerDial(
    currentSeconds: Int,
    totalSeconds: Int,
    mode: FocusMode,
    isRunning: Boolean,
    hideRemainingTime: Boolean = false,
    adhdMilestone: Int = 0,
    modifier: Modifier = Modifier
) {
    val progress = if (mode == FocusMode.STOPWATCH) {
        ((currentSeconds % 60).toFloat() / 60f)
    } else {
        if (totalSeconds > 0) {
            (1f - (currentSeconds.toFloat() / totalSeconds.toFloat())).coerceIn(0f, 1f)
        } else 0f
    }

    Box(
        modifier = modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val arcSize = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2)
            val topLeft = Offset(strokeWidth, strokeWidth)

            // Background track
            drawArc(
                color = CharcoalBorder,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active glowing progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(VioletPrimary, CyanAccent, VioletPrimary)
                ),
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Timer Center Information
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hideRemainingTime && mode != FocusMode.STOPWATCH) {
                Text(
                    text = "In Flow",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
                Text(
                    text = "Timer hidden for peace",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            } else {
                val hours = currentSeconds / 3600
                val mins = (currentSeconds % 3600) / 60
                val secs = currentSeconds % 60
                val timeFormatted = if (hours > 0) {
                    String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", hours, mins, secs)
                } else {
                    String.format(java.util.Locale.getDefault(), "%02d:%02d", mins, secs)
                }

                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CharcoalCardElevated
            ) {
                Text(
                    text = mode.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            if (mode == FocusMode.ADHD && adhdMilestone > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Milestone",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Milestone $adhdMilestone/4",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldSuccess
                    )
                }
            }
        }
    }
}

@Composable
fun WallpaperBackground(
    wallpaperId: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                when (wallpaperId) {
                    "aurora" -> Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1035), Color(0xFF0F1A2E), Color(0xFF0D0D15))
                    )
                    "neon_space" -> Brush.radialGradient(
                        colors = listOf(Color(0xFF181B36), Color(0xFF0C0D17))
                    )
                    "rain_window" -> Brush.verticalGradient(
                        colors = listOf(Color(0xFF131E29), Color(0xFF0E131C))
                    )
                    "cozy_room" -> Brush.verticalGradient(
                        colors = listOf(Color(0xFF24171E), Color(0xFF120E15))
                    )
                    "forest_mist" -> Brush.verticalGradient(
                        colors = listOf(Color(0xFF11221E), Color(0xFF0C1412))
                    )
                    else -> Brush.verticalGradient(
                        colors = listOf(Color(0xFF14141E), Color(0xFF0E0E14))
                    )
                }
            )
    ) {
        content()
    }
}

@Composable
fun StreakPill(streakCount: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CharcoalCardElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Streak",
                tint = AmberWarning,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$streakCount Days",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
}

@Composable
fun IconOrEmojiBadge(
    iconOrEmoji: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    tint: Color = CyanAccent,
    background: Color = CharcoalCardElevated,
    onClick: (() -> Unit)? = null
) {
    val isEmoji = iconOrEmoji.any { Character.isSurrogate(it) || it.code > 127 }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (isEmoji) {
            Text(
                text = iconOrEmoji,
                fontSize = (size.value * 0.5f).sp
            )
        } else {
            val iconVector = when (iconOrEmoji) {
                "flame", "local_fire_department" -> androidx.compose.material.icons.Icons.Default.LocalFireDepartment
                "star" -> androidx.compose.material.icons.Icons.Default.Star
                "fitness_center", "dumbbell" -> androidx.compose.material.icons.Icons.Default.FitnessCenter
                "psychology", "brain" -> androidx.compose.material.icons.Icons.Default.Psychology
                "favorite", "heart" -> androidx.compose.material.icons.Icons.Default.Favorite
                "wb_sunny", "sun" -> androidx.compose.material.icons.Icons.Default.WbSunny
                "bedtime", "moon" -> androidx.compose.material.icons.Icons.Default.Bedtime
                "water_drop", "water" -> androidx.compose.material.icons.Icons.Default.WaterDrop
                "menu_book", "book" -> androidx.compose.material.icons.Icons.Default.MenuBook
                "spa", "leaf" -> androidx.compose.material.icons.Icons.Default.Spa
                "bolt", "lightning" -> androidx.compose.material.icons.Icons.Default.Bolt
                else -> androidx.compose.material.icons.Icons.Default.Check
            }
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size((size.value * 0.55f).dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconEmojiPickerSheet(
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val presetIcons = listOf(
        "spa" to "Leaf",
        "flame" to "Flame",
        "star" to "Star",
        "dumbbell" to "Fitness",
        "brain" to "Focus",
        "heart" to "Health",
        "sun" to "Morning",
        "moon" to "Night",
        "water" to "Hydrate",
        "book" to "Study",
        "bolt" to "Energy",
        "check" to "Task"
    )

    val emojiPalette = listOf(
        "🎯", "🔥", "🧘", "⚡", "💡", "📚", "🏃", "💧", "🌿", "🌙",
        "☀️", "🏆", "🎨", "💻", "🧠", "🍎", "🛡️", "💎", "🚀", "⏳",
        "🎵", "📝", "☕", "🌟", "🔔", "🏋️", "🥗", "💤", "🐾", "✨"
    )

    var customInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CharcoalCardElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Choose Icon or Emoji",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Custom Emoji / Text Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customInput,
                    onValueChange = { customInput = it },
                    label = { Text("Type any custom emoji or symbol") },
                    placeholder = { Text("e.g. 🚴, 🎸, 🥋") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CharcoalBorder
                    )
                )
                if (customInput.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = VioletPrimary,
                        modifier = Modifier.clickable {
                            onSelect(customInput.trim())
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = "Apply",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            // Standard Preset Icons
            Text(
                text = "Essential Icons",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
            ) {
                presetIcons.forEach { (iconKey, label) ->
                    val isChosen = selected == iconKey
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            onSelect(iconKey)
                            onDismiss()
                        }
                    ) {
                        IconOrEmojiBadge(
                            iconOrEmoji = iconKey,
                            size = 46.dp,
                            tint = if (isChosen) CyanAccent else TextPrimary,
                            background = if (isChosen) CyanAccent.copy(alpha = 0.25f) else CharcoalCard
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(label, fontSize = 10.sp, color = if (isChosen) CyanAccent else TextMuted)
                    }
                }
            }

            // 30 Emoji Palette
            Text(
                text = "Quick Emoji Palette",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                emojiPalette.take(15).forEach { emo ->
                    Surface(
                        shape = CircleShape,
                        color = if (selected == emo) VioletPrimary.copy(alpha = 0.35f) else CharcoalCard,
                        modifier = Modifier.clickable {
                            onSelect(emo)
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = emo,
                            fontSize = 22.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                emojiPalette.drop(15).forEach { emo ->
                    Surface(
                        shape = CircleShape,
                        color = if (selected == emo) VioletPrimary.copy(alpha = 0.35f) else CharcoalCard,
                        modifier = Modifier.clickable {
                            onSelect(emo)
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = emo,
                            fontSize = 22.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

