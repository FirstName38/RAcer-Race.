package com.example.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin

enum class AppTheme(val id: String, val displayName: String, val previewColor: Color, val accentColor: Color) {
    MIDNIGHT_OLED("midnight_oled", "Midnight OLED", Color(0xFF000000), Color(0xFF8B5CF6)),
    CYBERPUNK_NEON("cyberpunk_neon", "Cyberpunk Neon", Color(0xFF090A15), Color(0xFFFF2A85)),
    FOREST_ZEN("forest_zen", "Forest Zen", Color(0xFF07120E), Color(0xFF10B981)),
    SUNSET_AMBER("sunset_amber", "Sunset Amber", Color(0xFF140A17), Color(0xFFF97316)),
    LAVENDER_MIST("lavender_mist", "Lavender Mist", Color(0xFF0F0D1F), Color(0xFFA78BFA)),
    NORDIC_FROST("nordic_frost", "Nordic Frost", Color(0xFF090E17), Color(0xFF38BDF8)),
    MINIMAL_LIGHT("minimal_light", "Paper Light", Color(0xFFF8FAFC), Color(0xFF6366F1))
}

enum class AppWallpaper(val id: String, val displayName: String, val description: String) {
    MINIMAL_CLEAN("minimal_clean", "Pure Minimal", "Deep clean gradient with subtle focus vignette"),
    AURORA_WAVES("aurora_waves", "Aurora Waves", "Atmospheric oscillating northern light ribbons"),
    STARRY_NEBULA("starry_nebula", "Starry Nebula", "Deep space starlight points and cosmic dust glow"),
    CYBER_GRID("cyber_grid", "Cyber Grid", "Isometric neon horizon lines with subtle tech matrix"),
    ZEN_GARDEN("zen_garden", "Zen Garden", "Flowing organic wave contours for maximum calm"),
    SUNSET_HORIZON("sunset_horizon", "Sunset Horizon", "Warm dusk gradient with golden twilight radiance")
}

data class DynamicThemeState(
    val theme: AppTheme = AppTheme.MIDNIGHT_OLED,
    val wallpaper: AppWallpaper = AppWallpaper.AURORA_WAVES
)

val LocalAppTheme = staticCompositionLocalOf { AppTheme.MIDNIGHT_OLED }
val LocalAppWallpaper = staticCompositionLocalOf { AppWallpaper.AURORA_WAVES }

fun getThemeColorScheme(theme: AppTheme): ColorScheme {
    return when (theme) {
        AppTheme.MIDNIGHT_OLED -> darkColorScheme(
            primary = Color(0xFF8B5CF6),
            onPrimary = Color.White,
            secondary = Color(0xFF06B6D4),
            onSecondary = Color.Black,
            tertiary = Color(0xFF10B981),
            background = Color(0xFF000000),
            onBackground = Color(0xFFF8FAFC),
            surface = Color(0xFF0B0C12),
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = Color(0xFF151624),
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF222438),
            outlineVariant = Color(0xFF181A2A),
            error = Color(0xFFEF4444)
        )
        AppTheme.CYBERPUNK_NEON -> darkColorScheme(
            primary = Color(0xFFFF2A85),
            onPrimary = Color.White,
            secondary = Color(0xFF00F0FF),
            onSecondary = Color.Black,
            tertiary = Color(0xFF00FF88),
            background = Color(0xFF080914),
            onBackground = Color(0xFFF8FAFC),
            surface = Color(0xFF101224),
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = Color(0xFF1A1C38),
            onSurfaceVariant = Color(0xFFA5B4FC),
            outline = Color(0xFFFF2A85).copy(alpha = 0.4f),
            outlineVariant = Color(0xFF2A2D54),
            error = Color(0xFFFF0055)
        )
        AppTheme.FOREST_ZEN -> darkColorScheme(
            primary = Color(0xFF10B981),
            onPrimary = Color.Black,
            secondary = Color(0xFF34D399),
            onSecondary = Color.Black,
            tertiary = Color(0xFFF59E0B),
            background = Color(0xFF07120E),
            onBackground = Color(0xFFF0FDF4),
            surface = Color(0xFF0E2019),
            onSurface = Color(0xFFF0FDF4),
            surfaceVariant = Color(0xFF173026),
            onSurfaceVariant = Color(0xFF86EFAC),
            outline = Color(0xFF1F4435),
            outlineVariant = Color(0xFF153327),
            error = Color(0xFFEF4444)
        )
        AppTheme.SUNSET_AMBER -> darkColorScheme(
            primary = Color(0xFFF97316),
            onPrimary = Color.White,
            secondary = Color(0xFFFB7185),
            onSecondary = Color.White,
            tertiary = Color(0xFFFBBF24),
            background = Color(0xFF140A17),
            onBackground = Color(0xFFFFF1F2),
            surface = Color(0xFF221126),
            onSurface = Color(0xFFFFF1F2),
            surfaceVariant = Color(0xFF331A38),
            onSurfaceVariant = Color(0xFFFDBA74),
            outline = Color(0xFF4A2552),
            outlineVariant = Color(0xFF2C1630),
            error = Color(0xFFEF4444)
        )
        AppTheme.LAVENDER_MIST -> darkColorScheme(
            primary = Color(0xFFA78BFA),
            onPrimary = Color.Black,
            secondary = Color(0xFF38BDF8),
            onSecondary = Color.Black,
            tertiary = Color(0xFFF472B6),
            background = Color(0xFF0F0D1F),
            onBackground = Color(0xFFF5F3FF),
            surface = Color(0xFF191633),
            onSurface = Color(0xFFF5F3FF),
            surfaceVariant = Color(0xFF25214C),
            onSurfaceVariant = Color(0xFFC4B5FD),
            outline = Color(0xFF383266),
            outlineVariant = Color(0xFF1F1B3B),
            error = Color(0xFFF43F5E)
        )
        AppTheme.NORDIC_FROST -> darkColorScheme(
            primary = Color(0xFF38BDF8),
            onPrimary = Color.Black,
            secondary = Color(0xFF818CF8),
            onSecondary = Color.Black,
            tertiary = Color(0xFF67E8F9),
            background = Color(0xFF090E17),
            onBackground = Color(0xFFF0F9FF),
            surface = Color(0xFF111B2B),
            onSurface = Color(0xFFF0F9FF),
            surfaceVariant = Color(0xFF1A2A42),
            onSurfaceVariant = Color(0xFFBAE6FD),
            outline = Color(0xFF253D5E),
            outlineVariant = Color(0xFF16253B),
            error = Color(0xFFFB7185)
        )
        AppTheme.MINIMAL_LIGHT -> lightColorScheme(
            primary = Color(0xFF6366F1),
            onPrimary = Color.White,
            secondary = Color(0xFF0284C7),
            onSecondary = Color.White,
            tertiary = Color(0xFF059669),
            background = Color(0xFFF8FAFC),
            onBackground = Color(0xFF0F172A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFF475569),
            outline = Color(0xFFCBD5E1),
            outlineVariant = Color(0xFFE2E8F0),
            error = Color(0xFFEF4444)
        )
    }
}

@Composable
fun AppWallpaperBackdrop(
    wallpaper: AppWallpaper,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wallpaper_ambient")
    val phase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    ).value

    val primaryTint = theme.accentColor
    val bgBase = if (theme == AppTheme.MINIMAL_LIGHT) Color(0xFFF1F5F9) else Color(0xFF05060A)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Base gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    theme.previewColor,
                    bgBase
                )
            )
        )

        when (wallpaper) {
            AppWallpaper.MINIMAL_CLEAN -> {
                // Subtle radial spotlight at top-center
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryTint.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.15f),
                        radius = w * 0.75f
                    )
                )
            }
            AppWallpaper.AURORA_WAVES -> {
                // Flowing atmospheric aurora ribbons
                val path1 = Path()
                path1.moveTo(0f, h * 0.25f)
                val steps = 20
                for (i in 0..steps) {
                    val x = w * (i.toFloat() / steps)
                    val y = h * 0.25f + sin(phase + i * 0.4f) * 45f
                    path1.lineTo(x, y)
                }
                path1.lineTo(w, 0f)
                path1.lineTo(0f, 0f)
                path1.close()

                drawPath(
                    path = path1,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryTint.copy(alpha = 0.22f), Color.Transparent),
                        startY = 0f,
                        endY = h * 0.35f
                    )
                )

                // Secondary lower ribbon
                val path2 = Path()
                path2.moveTo(0f, h * 0.70f)
                for (i in 0..steps) {
                    val x = w * (i.toFloat() / steps)
                    val y = h * 0.70f + sin(phase * 0.8f + i * 0.35f) * 40f
                    path2.lineTo(x, y)
                }
                path2.lineTo(w, h)
                path2.lineTo(0f, h)
                path2.close()

                drawPath(
                    path = path2,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, primaryTint.copy(alpha = 0.18f)),
                        startY = h * 0.60f,
                        endY = h
                    )
                )
            }
            AppWallpaper.STARRY_NEBULA -> {
                // Ambient colorful nebula dust circles
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryTint.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(w * 0.25f, h * 0.3f),
                        radius = w * 0.6f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(w * 0.8f, h * 0.65f),
                        radius = w * 0.5f
                    )
                )
                // Deterministic starlight speckles
                val starSeeds = listOf(
                    0.12f to 0.18f, 0.35f to 0.08f, 0.75f to 0.15f, 0.90f to 0.28f,
                    0.20f to 0.45f, 0.60f to 0.42f, 0.85f to 0.52f, 0.15f to 0.72f,
                    0.45f to 0.68f, 0.78f to 0.80f, 0.32f to 0.88f, 0.65f to 0.92f
                )
                starSeeds.forEachIndexed { index, (sx, sy) ->
                    val twinkle = 0.4f + 0.6f * sin(phase * 1.5f + index)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.35f * twinkle),
                        radius = 2.2f,
                        center = Offset(w * sx, h * sy)
                    )
                }
            }
            AppWallpaper.CYBER_GRID -> {
                // Perspective grid lines
                val gridColor = primaryTint.copy(alpha = 0.12f)
                val numVert = 8
                for (i in 0..numVert) {
                    val x = w * (i.toFloat() / numVert)
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1f
                    )
                }
                val numHoriz = 16
                for (j in 0..numHoriz) {
                    val y = h * (j.toFloat() / numHoriz)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                }
                // Center glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryTint.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.7f
                    )
                )
            }
            AppWallpaper.ZEN_GARDEN -> {
                // Concentric ripples radiating calmly from center
                val rippleCenter = Offset(w * 0.5f, h * 0.45f)
                val maxR = w * 0.9f
                for (r in 1..6) {
                    val radius = (maxR / 6) * r + sin(phase + r) * 12f
                    drawCircle(
                        color = primaryTint.copy(alpha = 0.08f),
                        center = rippleCenter,
                        radius = radius,
                        style = Stroke(width = 1.5f)
                    )
                }
            }
            AppWallpaper.SUNSET_HORIZON -> {
                // Warm sunset gradient shift
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2A0826),
                            Color(0xFF381220),
                            Color(0xFF1B0B1D)
                        )
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFF97316).copy(alpha = 0.28f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.6f),
                        radius = w * 0.8f
                    )
                )
            }
        }
    }
}
