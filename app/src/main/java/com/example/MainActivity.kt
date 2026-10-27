package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.AlarmRingingDialog
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
import com.example.ui.screens.routine.DailyRoutineScreen
import com.example.ui.screens.sessions.SessionDashboardScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.tasks.TasksScreen
import com.example.ui.screens.theme.ThemeConfigScreen
import com.example.ui.screens.usage.AppUsageScreen
import com.example.ui.screens.manifestation.ManifestationScreen
import com.example.ui.screens.wellness.WellnessScreen
import com.example.ui.theme.AppWallpaperBackdrop
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RacerTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.ui.theme.parseHexColor
import com.example.ui.viewmodel.RacerViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: RacerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appTheme by viewModel.currentAppTheme.collectAsState()
            val appWallpaper by viewModel.currentAppWallpaper.collectAsState()
            val appFont by viewModel.currentAppFont.collectAsState()
            val customWallpaperUri by viewModel.customWallpaperUri.collectAsState()
            val customAccentHex by viewModel.customAccentColorHex.collectAsState()
            val customScrimAlpha by viewModel.customWallpaperScrimAlpha.collectAsState()

            val customAccentColor = remember(customAccentHex) {
                parseHexColor(customAccentHex)
            }

            RacerTheme(
                appTheme = appTheme,
                appWallpaper = appWallpaper,
                appFont = appFont,
                customAccentColor = customAccentColor
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppWallpaperBackdrop(
                        wallpaper = appWallpaper,
                        theme = appTheme,
                        customGalleryUri = customWallpaperUri,
                        customScrimAlpha = customScrimAlpha,
                        customAccent = customAccentColor
                    )
                    RacerMainApp(viewModel = viewModel)
                }
            }
        }
    }
}

private data class ExploreHubItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RacerMainApp(viewModel: RacerViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "main_tabs"

    val coroutineScope = rememberCoroutineScope()
    var showExploreSheet by remember { mutableStateOf(false) }
    val timerState by viewModel.timerState.collectAsState()

    // 4 Main Horizontal Swipe Navigation Tabs
    val dockItems = listOf(
        Screen.Home,
        Screen.Focus,
        Screen.Tasks,
        Screen.LumaAI
    )
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { dockItems.size })

    // Full Explore Hub items list
    val exploreItems = listOf(
        ExploreHubItem("App Usage & Telemetry", "Categorize apps, limits & clock time graphs", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFFF43F5E), Screen.AppUsage.route),
        ExploreHubItem("Manifestation Corner", "Vision board, photos & daily affirmations", Icons.Default.SelfImprovement, Color(0xFF8B5CF6), Screen.Manifestation.route),
        ExploreHubItem("Theme & Aesthetics Studio", "Custom gallery wallpaper, accent colors & typography", Icons.Default.Palette, Color(0xFFEC4899), Screen.ThemeConfig.route),
        ExploreHubItem("Calendar & Timeline", "Deadlines, exams & time blocking", Icons.Default.CalendarMonth, CyanAccent, Screen.Calendar.route),
        ExploreHubItem("Focus Room", "Virtual co-working study partners", Icons.Default.MeetingRoom, VioletPrimary, Screen.FocusRoom.route),
        ExploreHubItem("Habit Tracker", "Streaks, frequency & daily identity", Icons.Default.Repeat, EmeraldSuccess, Screen.Habits.route),
        ExploreHubItem("Daily Journal", "Evening gratitude, mood & wins", Icons.Default.EditNote, Color(0xFFF59E0B), Screen.Journal.route),
        ExploreHubItem("Sensory & Wellness", "Breathing resets & micro-movement", Icons.Default.SelfImprovement, Color(0xFF38BDF8), Screen.Wellness.route),
        ExploreHubItem("Insights & Telemetry", "Deep focus patterns & analytics", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFFFF69B4), Screen.Insights.route),
        ExploreHubItem("ADHD Visual Clock", "Time blindness sweep & rhythm", Icons.Default.AccessTime, Color(0xFF34D399), Screen.Clock.route),
        ExploreHubItem("Weekly Objectives", "Structured milestones & review", Icons.Default.CalendarMonth, Color(0xFFA78BFA), Screen.WeeklyPlan.route),
        ExploreHubItem("Daily Routine", "Time-blocked morning & evening", Icons.Default.Schedule, Color(0xFFF97316), Screen.DailyRoutine.route),
        ExploreHubItem("Session Dashboard", "Focus duration history & logs", Icons.Default.Timer, Color(0xFF06B6D4), Screen.SessionDashboard.route),
        ExploreHubItem("Settings & Privacy", "Audio levels, blocker & data controls", Icons.Default.Settings, Color(0xFFCBD5E1), Screen.Settings.route)
    )

    // Luma AI breathing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "luma_dock_glow")
    val lumaGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "luma_alpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (currentRoute != "main_tabs") {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (currentRoute == "main_tabs") "FocusFlow" else {
                                exploreItems.firstOrNull { it.route == currentRoute }?.title ?: "FocusFlow"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (timerState.isRunning) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmeraldSuccess.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f))
                            ) {
                                val mins = timerState.currentSeconds / 60
                                val secs = timerState.currentSeconds % 60
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldSuccess)
                                    )
                                    Text(
                                        text = "%02d:%02d".format(mins, secs),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    // Quick button to Theme Studio
                    IconButton(onClick = { navController.navigate(Screen.ThemeConfig.route) }) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Theme Studio",
                            tint = if (currentRoute == Screen.ThemeConfig.route) CyanAccent else TextSecondary
                        )
                    }
                    // Quick button to Calendar
                    IconButton(onClick = { navController.navigate(Screen.Calendar.route) }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar",
                            tint = if (currentRoute == Screen.Calendar.route) CyanAccent else TextSecondary
                        )
                    }
                    // Explore Hub Launcher Button
                    IconButton(onClick = { showExploreSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = "Explore Hub",
                            tint = if (showExploreSheet) CyanAccent else TextPrimary
                        )
                    }
                    // Quick Settings Button
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (currentRoute == Screen.Settings.route) CyanAccent else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            // Animated Floating Island Dock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                    ),
                    shadowElevation = 14.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 4 Primary Destination Tabs with bouncy animated physics
                        dockItems.forEachIndexed { index, screen ->
                            val isSelected = if (currentRoute == "main_tabs") (pagerState.currentPage == index) else false
                            val isLuma = screen == Screen.LumaAI

                            val scale by animateFloatAsState(
                                targetValue = if (isSelected) 1.14f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "dock_scale_${screen.route}"
                            )

                            Column(
                                modifier = Modifier
                                    .scale(scale)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (currentRoute != "main_tabs") {
                                            navController.popBackStack("main_tabs", inclusive = false)
                                        }
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isLuma) {
                                        // Glowing pulsing ring for Luma AI
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.radialGradient(
                                                        colors = listOf(
                                                            CyanAccent.copy(alpha = lumaGlowAlpha * 0.5f),
                                                            Color.Transparent
                                                        )
                                                    )
                                                )
                                        )
                                    }

                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(22.dp),
                                        tint = when {
                                            isSelected && isLuma -> CyanAccent
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isLuma -> CyanAccent.copy(alpha = 0.85f)
                                            else -> TextMuted
                                        }
                                    )
                                }

                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        isSelected && isLuma -> CyanAccent
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        else -> TextMuted
                                    }
                                )

                                // Active indicator dot
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .width(14.dp)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(if (isLuma) CyanAccent else MaterialTheme.colorScheme.primary)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(3.dp))
                                }
                            }
                        }

                        // 5th Tab: Explore Hub Trigger Button
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    showExploreSheet = true
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (showExploreSheet) VioletPrimary.copy(alpha = 0.35f) else Color.Transparent,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = "Explore Hub",
                                        modifier = Modifier.size(20.dp),
                                        tint = if (showExploreSheet) CyanAccent else TextMuted
                                    )
                                }
                            }

                            Text(
                                text = "Hub",
                                fontSize = 10.sp,
                                fontWeight = if (showExploreSheet) FontWeight.Bold else FontWeight.Medium,
                                color = if (showExploreSheet) CyanAccent else TextMuted
                            )

                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dynamic Fade-in Scale Transition Navigation Host
            NavHost(
                navController = navController,
                startDestination = "main_tabs",
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    fadeIn(animationSpec = tween(320)) + scaleIn(initialScale = 0.92f, animationSpec = tween(320))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(260)) + scaleOut(targetScale = 1.05f, animationSpec = tween(260))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(320)) + scaleIn(initialScale = 1.05f, animationSpec = tween(320))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(260)) + scaleOut(targetScale = 0.92f, animationSpec = tween(260))
                }
            ) {
                // Root destination: Horizontal swipe between the 4 primary tabs
                composable("main_tabs") {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToFocus = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                                onNavigateToFocusRoom = { navController.navigate(Screen.FocusRoom.route) },
                                onNavigateToTasks = { coroutineScope.launch { pagerState.animateScrollToPage(2) } },
                                onNavigateToHabits = { navController.navigate(Screen.Habits.route) },
                                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                                onNavigateToAI = { coroutineScope.launch { pagerState.animateScrollToPage(3) } },
                                onNavigateToSessions = { navController.navigate(Screen.SessionDashboard.route) },
                                onNavigateToRoutine = { navController.navigate(Screen.DailyRoutine.route) },
                                onNavigateToWeeklyPlan = { navController.navigate(Screen.WeeklyPlan.route) }
                            )
                            1 -> FocusScreen(
                                viewModel = viewModel,
                                onNavigateToSessions = { navController.navigate(Screen.SessionDashboard.route) }
                            )
                            2 -> TasksScreen(
                                viewModel = viewModel,
                                onNavigateToFocus = { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
                            )
                            3 -> LumaAIScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }

                // Dedicated Theme & Aesthetics Configuration Screen
                composable(Screen.ThemeConfig.route) {
                    ThemeConfigScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.WeeklyPlan.route) {
                    com.example.ui.screens.weekly.WeeklyPlanScreen(viewModel = viewModel)
                }
                composable(Screen.DailyRoutine.route) {
                    DailyRoutineScreen(
                        viewModel = viewModel,
                        onNavigateToFocus = {
                            navController.popBackStack("main_tabs", inclusive = false)
                            coroutineScope.launch { pagerState.scrollToPage(1) }
                        },
                        onNavigateToWellness = { navController.navigate(Screen.Wellness.route) },
                        onNavigateToJournal = { navController.navigate(Screen.Journal.route) }
                    )
                }
                composable(Screen.SessionDashboard.route) {
                    SessionDashboardScreen(
                        viewModel = viewModel,
                        onNavigateToFocus = {
                            navController.popBackStack("main_tabs", inclusive = false)
                            coroutineScope.launch { pagerState.scrollToPage(1) }
                        }
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
                        onNavigateToAI = {
                            navController.popBackStack("main_tabs", inclusive = false)
                            coroutineScope.launch { pagerState.scrollToPage(3) }
                        }
                    )
                }
                composable(Screen.Clock.route) {
                    ClockScreen(viewModel = viewModel)
                }
                composable(Screen.FocusRoom.route) {
                    FocusRoomScreen(viewModel = viewModel)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToThemeStudio = { navController.navigate(Screen.ThemeConfig.route) }
                    )
                }
                composable(Screen.Calendar.route) {
                    CalendarScreen(viewModel = viewModel)
                }
                composable(Screen.Wellness.route) {
                    WellnessScreen(viewModel = viewModel)
                }
                composable(Screen.AppUsage.route) {
                    AppUsageScreen(viewModel = viewModel)
                }
                composable(Screen.Manifestation.route) {
                    ManifestationScreen(viewModel = viewModel)
                }
            }

            // High Priority Live Alarm Ringing Dialog
            val alarmRingingState by viewModel.alarmRingingState.collectAsState()
            if (alarmRingingState.isRinging) {
                AlarmRingingDialog(
                    ringingState = alarmRingingState,
                    onDismiss = { viewModel.dismissRingingAlarm() },
                    onSnooze = { alarmId -> viewModel.snoozeRingingAlarm(alarmId, 5) }
                )
            }
        }
    }

    // Explore Hub Animated Modal Bottom Sheet
    if (showExploreSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showExploreSheet = false },
            sheetState = sheetState,
            containerColor = CharcoalCardElevated,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TextMuted)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Explore FocusFlow Hub",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Direct access to all ADHD cognitive tools & features",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(exploreItems) { item ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CharcoalCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showExploreSheet = false
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(item.accentColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = item.accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
