package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.StreakPill
import com.example.ui.navigation.Screen
import com.example.ui.screens.ai.LumaAIScreen
import com.example.ui.screens.calendar.CalendarScreen
import com.example.ui.screens.clock.ClockScreen
import com.example.ui.screens.focus.FocusScreen
import com.example.ui.screens.focusroom.FocusRoomScreen
import com.example.ui.screens.habits.HabitsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.insights.InsightsScreen
import com.example.ui.screens.journal.JournalScreen
import com.example.ui.screens.sessions.SessionDashboardScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.tasks.TasksScreen
import com.example.ui.screens.wellness.WellnessScreen
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RacerTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.RacerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RacerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RacerTheme {
                RacerMainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacerMainApp(viewModel: RacerViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    var showQuickMenu by remember { mutableStateOf(false) }
    val timerState by viewModel.timerState.collectAsState()

    // Primary curated Navigation items
    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Focus,
        Screen.FocusRoom,
        Screen.Tasks,
        Screen.Calendar,
        Screen.LumaAI
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "RAcer",
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (timerState.isRunning) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldSuccess.copy(alpha = 0.2f)
                            ) {
                                val mins = timerState.currentSeconds / 60
                                val secs = timerState.currentSeconds % 60
                                Text(
                                    text = "● %02d:%02d".format(mins, secs),
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Quick jump to Habits
                    IconButton(onClick = { navController.navigate(Screen.Habits.route) }) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Habits",
                            tint = if (currentRoute == Screen.Habits.route) CyanAccent else TextSecondary
                        )
                    }
                    // Quick jump to Journal
                    IconButton(onClick = { navController.navigate(Screen.Journal.route) }) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Journal",
                            tint = if (currentRoute == Screen.Journal.route) CyanAccent else TextSecondary
                        )
                    }
                    // Quick More Menu (Insights, Clock, Settings)
                    Box {
                        IconButton(onClick = { showQuickMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More features",
                                tint = TextPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showQuickMenu,
                            onDismissRequest = { showQuickMenu = false },
                            modifier = Modifier.background(CharcoalCardElevated)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Session Dashboard & History", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = CyanAccent) },
                                onClick = {
                                    showQuickMenu = false
                                    navController.navigate(Screen.SessionDashboard.route)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Mental Health & Movement", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent) },
                                onClick = {
                                    showQuickMenu = false
                                    navController.navigate(Screen.Wellness.route)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Insights & Analytics", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = CyanAccent) },
                                onClick = {
                                    showQuickMenu = false
                                    navController.navigate(Screen.Insights.route)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("ADHD Clock & Visual Rhythm", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = CyanAccent) },
                                onClick = {
                                    showQuickMenu = false
                                    navController.navigate(Screen.Clock.route)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings & Soundpacks", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = CyanAccent) },
                                onClick = {
                                    showQuickMenu = false
                                    navController.navigate(Screen.Settings.route)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CharcoalCard,
                contentColor = TextPrimary,
                tonalElevation = 8.dp
            ) {
                bottomNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanAccent,
                            selectedTextColor = CyanAccent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = VioletPrimary.copy(alpha = 0.25f)
                        )
                    )
                }
            }
        },
        containerColor = CharcoalDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CharcoalDark)
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToFocus = { navController.navigate(Screen.Focus.route) },
                        onNavigateToFocusRoom = { navController.navigate(Screen.FocusRoom.route) },
                        onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                        onNavigateToHabits = { navController.navigate(Screen.Habits.route) },
                        onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                        onNavigateToJournal = { navController.navigate(Screen.Journal.route) },
                        onNavigateToAI = { navController.navigate(Screen.LumaAI.route) },
                        onNavigateToSessions = { navController.navigate(Screen.SessionDashboard.route) }
                    )
                }
                composable(Screen.Focus.route) {
                    FocusScreen(
                        viewModel = viewModel,
                        onNavigateToSessions = { navController.navigate(Screen.SessionDashboard.route) }
                    )
                }
                composable(Screen.SessionDashboard.route) {
                    SessionDashboardScreen(
                        viewModel = viewModel,
                        onNavigateToFocus = { navController.navigate(Screen.Focus.route) }
                    )
                }
                composable(Screen.Tasks.route) {
                    TasksScreen(
                        viewModel = viewModel,
                        onNavigateToFocus = { navController.navigate(Screen.Focus.route) }
                    )
                }
                composable(Screen.Habits.route) {
                    HabitsScreen(viewModel = viewModel)
                }
                composable(Screen.Journal.route) {
                    JournalScreen(viewModel = viewModel)
                }
                composable(Screen.Insights.route) {
                    InsightsScreen(
                        viewModel = viewModel,
                        onNavigateToAI = { navController.navigate(Screen.LumaAI.route) }
                    )
                }
                composable(Screen.Clock.route) {
                    ClockScreen(viewModel = viewModel)
                }
                composable(Screen.FocusRoom.route) {
                    FocusRoomScreen(viewModel = viewModel)
                }
                composable(Screen.LumaAI.route) {
                    LumaAIScreen(viewModel = viewModel)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = viewModel)
                }
                composable(Screen.Calendar.route) {
                    CalendarScreen(viewModel = viewModel)
                }
                composable(Screen.Wellness.route) {
                    WellnessScreen(viewModel = viewModel)
                }
            }
        }
    }
}

