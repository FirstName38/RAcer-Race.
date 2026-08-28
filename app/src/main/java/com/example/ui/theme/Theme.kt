package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VioletPrimary,
    onPrimary = Color.White,
    primaryContainer = VioletDark,
    onPrimaryContainer = Color.White,
    secondary = CyanAccent,
    onSecondary = CharcoalDark,
    secondaryContainer = CyanDark,
    onSecondaryContainer = Color.White,
    tertiary = IndigoCalm,
    onTertiary = Color.White,
    background = CharcoalDark,
    onBackground = TextPrimary,
    surface = CharcoalCard,
    onSurface = TextPrimary,
    surfaceVariant = CharcoalCardElevated,
    onSurfaceVariant = TextSecondary,
    outline = CharcoalBorder,
    outlineVariant = CharcoalSubtle,
    error = RoseUrgent,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = VioletDark,
    onPrimary = Color.White,
    primaryContainer = VioletLight,
    onPrimaryContainer = CharcoalDark,
    secondary = CyanDark,
    onSecondary = Color.White,
    secondaryContainer = CyanLight,
    onSecondaryContainer = CharcoalDark,
    tertiary = IndigoCalm,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = RoseUrgent,
    onError = Color.White
)

@Composable
fun RacerTheme(
    darkTheme: Boolean = true, // Default to dark-first interface for calm digital focus
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep alias for compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    RacerTheme(darkTheme = true, content = content)
}
