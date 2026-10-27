package com.example.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.sin

enum class AppTheme(val id: String, val displayName: String, val previewColor: Color, val accentColor: Color) {
    MIDNIGHT_OLED("midnight_oled", "Midnight OLED", Color(0xFF000000), Color(0xFF8B5CF6)),
    CYBERPUNK_NEON("cyberpunk_neon", "Cyberpunk Neon", Color(0xFF090A15), Color(0xFFFF2A85)),
    FOREST_ZEN("forest_zen", "Forest Zen", Color(0xFF07120E), Color(0xFF10B981)),
    SUNSET_AMBER("sunset_amber", "Sunset Amber", Color(0xFF140A17), Color(0xFFF97316)),
    LAVENDER_MIST("lavender_mist", "Lavender Mist", Color(0xFF0F0D1F), Color(0xFFA78BFA)),
    NORDIC_FROST("nordic_frost", "Nordic Frost", Color(0xFF090E17), Color(0xFF38BDF8)),
    OBSIDIAN_GOLD("obsidian_gold", "Obsidian Gold", Color(0xFF08080A), Color(0xFFF59E0B)),
    MATRIX_EMERALD("matrix_emerald", "Matrix Emerald", Color(0xFF030A05), Color(0xFF00FF66)),
    SAKURA_BLOSSOM("sakura_blossom", "Sakura Blossom", Color(0xFF130814), Color(0xFFFF69B4)),
    SYNTHWAVE_80S("synthwave_80s", "Synthwave 80s", Color(0xFF0E0720), Color(0xFFEC4899)),
    DEEP_ABYSS("deep_abyss", "Deep Abyss", Color(0xFF040A14), Color(0xFF06B6D4)),
    MINIMAL_LIGHT("minimal_light", "Paper Light", Color(0xFFF8FAFC), Color(0xFF6366F1))
}

enum class AppWallpaper(val id: String, val displayName: String, val description: String) {
    MINIMAL_CLEAN("minimal_clean", "Pure Minimal", "Deep clean gradient with subtle focus vignette"),
    AURORA_WAVES("aurora_waves", "Aurora Waves", "Atmospheric oscillating northern light ribbons"),
    STARRY_NEBULA("starry_nebula", "Starry Nebula", "Deep space starlight points and cosmic dust glow"),
    CYBER_GRID("cyber_grid", "Cyber Grid", "Isometric neon horizon lines with subtle tech matrix"),
    ZEN_GARDEN("zen_garden", "Zen Garden", "Flowing organic wave contours for maximum calm"),
    SUNSET_HORIZON("sunset_horizon", "Sunset Horizon", "Warm dusk gradient with golden twilight radiance"),
    MATRIX_RAIN("matrix_rain", "Matrix Code Rain", "Animated phosphor cyber drops falling down the screen"),
    COSMIC_DUST("cosmic_dust", "Cosmic Stardust", "Gently floating stellar dust particles and ambient glow"),
    CUSTOM_GALLERY("custom_gallery", "Custom Photo", "Your personalized wallpaper loaded from your gallery")
}

enum class AppFont(val id: String, val displayName: String, val description: String, val fontFamily: FontFamily) {
    MODERN_SANS("modern_sans", "Modern Sans", "Crisp, balanced & versatile geometric sans-serif", FontFamily.SansSerif),
    EDITORIAL_SERIF("editorial_serif", "Editorial Serif", "Refined, intellectual & bookish literary serif", FontFamily.Serif),
    CYBER_MONO("cyber_mono", "Cyber Matrix Mono", "Futuristic monospace terminal aesthetic", FontFamily.Monospace),
    CREATIVE_CURSIVE("creative_cursive", "Creative Script", "Playful expressive handwritten aesthetic", FontFamily.Cursive)
}

data class DynamicThemeState(
    val theme: AppTheme = AppTheme.MIDNIGHT_OLED,
    val wallpaper: AppWallpaper = AppWallpaper.AURORA_WAVES,
    val font: AppFont = AppFont.MODERN_SANS,
    val customGalleryUri: String? = null
)

val LocalAppTheme = staticCompositionLocalOf { AppTheme.MIDNIGHT_OLED }
val LocalAppWallpaper = staticCompositionLocalOf { AppWallpaper.AURORA_WAVES }
val LocalAppFont = staticCompositionLocalOf { AppFont.MODERN_SANS }

fun getAppTypography(font: AppFont): Typography {
    val f = font.fontFamily
    return Typography(
        displayLarge = TextStyle(fontFamily = f, fontWeight = FontWeight.Bold, fontSize = 52.sp, lineHeight = 60.sp, letterSpacing = (-0.5).sp),
        displayMedium = TextStyle(fontFamily = f, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 48.sp, letterSpacing = (-0.25).sp),
        displaySmall = TextStyle(fontFamily = f, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.sp),
        headlineLarge = TextStyle(fontFamily = f, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.sp),
        headlineMedium = TextStyle(fontFamily = f, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp),
        headlineSmall = TextStyle(fontFamily = f, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 28.sp, letterSpacing = 0.sp),
        titleLarge = TextStyle(fontFamily = f, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 26.sp, letterSpacing = 0.1.sp),
        titleMedium = TextStyle(fontFamily = f, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
        titleSmall = TextStyle(fontFamily = f, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        bodyLarge = TextStyle(fontFamily = f, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.25.sp),
        bodyMedium = TextStyle(fontFamily = f, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
        bodySmall = TextStyle(fontFamily = f, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
        labelLarge = TextStyle(fontFamily = f, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        labelMedium = TextStyle(fontFamily = f, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
        labelSmall = TextStyle(fontFamily = f, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
    )
}

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
        AppTheme.OBSIDIAN_GOLD -> darkColorScheme(
            primary = Color(0xFFF59E0B),
            onPrimary = Color.Black,
            secondary = Color(0xFFD97706),
            onSecondary = Color.Black,
            tertiary = Color(0xFFFCD34D),
            background = Color(0xFF08080A),
            onBackground = Color(0xFFFFFBEB),
            surface = Color(0xFF141310),
            onSurface = Color(0xFFFFFBEB),
            surfaceVariant = Color(0xFF221F1A),
            onSurfaceVariant = Color(0xFFFDE68A),
            outline = Color(0xFF453A22),
            outlineVariant = Color(0xFF2E2616),
            error = Color(0xFFEF4444)
        )
        AppTheme.MATRIX_EMERALD -> darkColorScheme(
            primary = Color(0xFF00FF66),
            onPrimary = Color.Black,
            secondary = Color(0xFF10B981),
            onSecondary = Color.Black,
            tertiary = Color(0xFF34D399),
            background = Color(0xFF030A05),
            onBackground = Color(0xFFDCFCE7),
            surface = Color(0xFF08140B),
            onSurface = Color(0xFFDCFCE7),
            surfaceVariant = Color(0xFF0E2214),
            onSurfaceVariant = Color(0xFF86EFAC),
            outline = Color(0xFF154323),
            outlineVariant = Color(0xFF0B2B15),
            error = Color(0xFFFF3366)
        )
        AppTheme.SAKURA_BLOSSOM -> darkColorScheme(
            primary = Color(0xFFFF69B4),
            onPrimary = Color.White,
            secondary = Color(0xFFFB7185),
            onSecondary = Color.White,
            tertiary = Color(0xFFFDA4AF),
            background = Color(0xFF130814),
            onBackground = Color(0xFFFFF1F2),
            surface = Color(0xFF200F21),
            onSurface = Color(0xFFFFF1F2),
            surfaceVariant = Color(0xFF321635),
            onSurfaceVariant = Color(0xFFFBCFE8),
            outline = Color(0xFF522254),
            outlineVariant = Color(0xFF351538),
            error = Color(0xFFF43F5E)
        )
        AppTheme.SYNTHWAVE_80S -> darkColorScheme(
            primary = Color(0xFFEC4899),
            onPrimary = Color.White,
            secondary = Color(0xFF06B6D4),
            onSecondary = Color.Black,
            tertiary = Color(0xFFA855F7),
            background = Color(0xFF0E0720),
            onBackground = Color(0xFFFAF5FF),
            surface = Color(0xFF1A0E38),
            onSurface = Color(0xFFFAF5FF),
            surfaceVariant = Color(0xFF281555),
            onSurfaceVariant = Color(0xFFE9D5FF),
            outline = Color(0xFF4C248A),
            outlineVariant = Color(0xFF30155A),
            error = Color(0xFFFF2A85)
        )
        AppTheme.DEEP_ABYSS -> darkColorScheme(
            primary = Color(0xFF06B6D4),
            onPrimary = Color.Black,
            secondary = Color(0xFF3B82F6),
            onSecondary = Color.White,
            tertiary = Color(0xFF38BDF8),
            background = Color(0xFF040A14),
            onBackground = Color(0xFFF0F9FF),
            surface = Color(0xFF091426),
            onSurface = Color(0xFFF0F9FF),
            surfaceVariant = Color(0xFF0F203C),
            onSurfaceVariant = Color(0xFFBAE6FD),
            outline = Color(0xFF1E3A66),
            outlineVariant = Color(0xFF112544),
            error = Color(0xFFEF4444)
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

fun parseHexColor(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = if (clean.length == 6) {
            android.graphics.Color.parseColor("#$clean")
        } else if (clean.length == 8) {
            android.graphics.Color.parseColor("#$clean")
        } else null
        colorInt?.let { Color(it) }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun AppWallpaperBackdrop(
    wallpaper: AppWallpaper,
    theme: AppTheme,
    customGalleryUri: String? = null,
    customScrimAlpha: Float = 0.65f,
    customAccent: Color? = null,
    modifier: Modifier = Modifier
) {
    if (wallpaper == AppWallpaper.CUSTOM_GALLERY && !customGalleryUri.isNullOrBlank()) {
        Box(modifier = modifier.fillMaxSize()) {
            AsyncImage(
                model = customGalleryUri,
                contentDescription = "Custom Gallery Wallpaper",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Dark scrim overlay for high contrast readability of all app text & icons
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = customScrimAlpha.coerceIn(0.2f, 0.95f)))
            )
        }
        return
    }

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

    val primaryTint = customAccent ?: theme.accentColor
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
            AppWallpaper.MATRIX_RAIN -> {
                // Animated vertical phosphor green code drops
                val columns = 14
                val colWidth = w / columns
                for (c in 0 until columns) {
                    val speed = 1.0f + (c % 5) * 0.3f
                    val dropY = ((phase * speed * 200f + c * 90f) % (h + 120f)) - 60f
                    val trailLength = 160f
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF00FF66).copy(alpha = 0.35f), Color.White.copy(alpha = 0.75f)),
                            startY = dropY - trailLength,
                            endY = dropY
                        ),
                        start = Offset(c * colWidth + colWidth * 0.5f, dropY - trailLength),
                        end = Offset(c * colWidth + colWidth * 0.5f, dropY),
                        strokeWidth = 2f
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = 2.5f,
                        center = Offset(c * colWidth + colWidth * 0.5f, dropY)
                    )
                }
            }
            AppWallpaper.COSMIC_DUST -> {
                // Floating cosmic particles and gentle radial aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryTint.copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.35f + sin(phase) * 30f),
                        radius = w * 0.85f
                    )
                )
                val particleCount = 20
                for (p in 0 until particleCount) {
                    val seedX = (p * 37 % 100) / 100f
                    val seedY = (p * 59 % 100) / 100f
                    val px = w * seedX + sin(phase + p) * 15f
                    val py = (h * seedY + (phase * 30f + p * 40f) % h) % h
                    val glow = 0.3f + 0.5f * sin(phase * 2f + p)
                    drawCircle(
                        color = primaryTint.copy(alpha = glow * 0.45f),
                        radius = 3f + (p % 3),
                        center = Offset(px, py)
                    )
                }
            }
            AppWallpaper.CUSTOM_GALLERY -> {
                // Fallback if no uri is set
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryTint.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.7f
                    )
                )
            }
        }
    }
}
