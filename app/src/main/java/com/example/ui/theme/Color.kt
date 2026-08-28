package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Deep charcoal background palette for digital detox and calm focus
val CharcoalBlack = Color(0xFF0F0F16)
val CharcoalDark = Color(0xFF12121A)
val CharcoalCard = Color(0xFF191924)
val CharcoalCardElevated = Color(0xFF222233)
val CharcoalBorder = Color(0xFF2E2E44)
val CharcoalSubtle = Color(0xFF3B3B54)

// Glowing Violet -> Cyan primary brand gradient
val VioletPrimary = Color(0xFF8B5CF6)
val VioletDark = Color(0xFF6D28D9)
val VioletLight = Color(0xFFA78BFA)
val CyanAccent = Color(0xFF38BDF8)
val CyanDark = Color(0xFF0284C7)
val CyanLight = Color(0xFF7DD3FC)

// Soft functional accent colors
val PinkAccent = Color(0xFFEC4899)
val EmeraldSuccess = Color(0xFF10B981)
val EmeraldDark = Color(0xFF059669)
val AmberWarning = Color(0xFFF59E0B)
val RoseUrgent = Color(0xFFF43F5E)
val IndigoCalm = Color(0xFF6366F1)
val TealAmbient = Color(0xFF14B8A6)

// Text Colors
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)
val TextDisabled = Color(0xFF475569)

// Gradients
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(VioletPrimary, CyanAccent)
)

val GlowGradient = Brush.radialGradient(
    colors = listOf(VioletPrimary.copy(alpha = 0.35f), Color.Transparent)
)

val CardGlowGradient = Brush.verticalGradient(
    colors = listOf(CharcoalCardElevated, CharcoalCard)
)

val CalmNightGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF161626), Color(0xFF0F0F16))
)
