package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Focus : Screen("focus", "Focus", Icons.Default.Timer)
    data object Tasks : Screen("tasks", "Tasks", Icons.Default.CheckCircle)
    data object Habits : Screen("habits", "Habit & Challenge", Icons.Default.Repeat)
    data object Wellness : Screen("wellness", "Mental & Yoga", Icons.Default.SelfImprovement)
    data object Journal : Screen("journal", "Journal", Icons.Default.EditNote)
    data object Insights : Screen("insights", "Insights", Icons.AutoMirrored.Filled.TrendingUp)
    data object Clock : Screen("clock", "Clock", Icons.Default.AccessTime)
    data object FocusRoom : Screen("focus_room", "Focus Room", Icons.Default.MeetingRoom)
    data object LumaAI : Screen("luma_ai", "Luma AI", Icons.Default.AutoAwesome)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
}

val MainNavigationItems = listOf(
    Screen.Home,
    Screen.Focus,
    Screen.Tasks,
    Screen.Habits,
    Screen.Wellness,
    Screen.Journal,
    Screen.Insights,
    Screen.Clock,
    Screen.FocusRoom,
    Screen.LumaAI,
    Screen.Settings
)
