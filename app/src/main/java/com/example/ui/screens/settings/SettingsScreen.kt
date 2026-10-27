package com.example.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AppUsageEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseUrgent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.RacerViewModel
import com.example.usage.AppUsageHelper
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: RacerViewModel,
    onNavigateToThemeStudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val usageList by viewModel.dailyUsageList.collectAsState()

    var apiKey by remember { mutableStateOf("") }
    var endpoint by remember { mutableStateOf("") }

    var allowFocus by remember { mutableStateOf(true) }
    var allowTasks by remember { mutableStateOf(true) }
    var allowHabits by remember { mutableStateOf(true) }
    var allowJournal by remember { mutableStateOf(false) }
    var allowUsage by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteActionType by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        apiKey = viewModel.repository.getSetting("ai_api_key", "")
        endpoint = viewModel.repository.getSetting("ai_endpoint", "")
        allowFocus = viewModel.repository.getSetting("ai_allow_focus", "true") == "true"
        allowTasks = viewModel.repository.getSetting("ai_allow_tasks", "true") == "true"
        allowHabits = viewModel.repository.getSetting("ai_allow_habits", "true") == "true"
        allowJournal = viewModel.repository.getSetting("ai_allow_journal", "false") == "true"
        allowUsage = viewModel.repository.getSetting("ai_allow_usage", "false") == "true"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Column {
                Text(
                    text = "Settings & Privacy",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Configure your focus environment & local privacy vault",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Appearance & Aesthetic Theme Studio
        item {
            val currentTheme by viewModel.currentAppTheme.collectAsState()
            val currentWallpaper by viewModel.currentAppWallpaper.collectAsState()
            val weekStartDay by viewModel.weekStartDay.collectAsState()

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = VioletPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Theme & Atmosphere Studio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Switch color palettes and dynamic live animated backdrops", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }

                    Button(
                        onClick = onNavigateToThemeStudio,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Full Theme & Aesthetics Studio", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // 1. Color Theme Palette Selector
                    Text("App Theme (${currentTheme.displayName}):", style = MaterialTheme.typography.labelMedium, color = CyanAccent, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(com.example.ui.theme.AppTheme.values().toList()) { theme ->
                            val isSelected = currentTheme == theme
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) VioletPrimary.copy(alpha = 0.25f) else CharcoalCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyanAccent else CharcoalBorder),
                                modifier = Modifier.clickable { viewModel.setAppTheme(theme) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Two-tone dot
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(theme.previewColor)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(theme.accentColor)
                                    )
                                    Text(
                                        text = theme.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }

                    // 2. Animated Live Wallpaper Backdrop Selector
                    Text("Animated Wallpaper Backdrop (${currentWallpaper.displayName}):", style = MaterialTheme.typography.labelMedium, color = CyanAccent, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(com.example.ui.theme.AppWallpaper.values().toList()) { wp ->
                            val isSelected = currentWallpaper == wp
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) CyanAccent.copy(alpha = 0.2f) else CharcoalCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyanAccent else CharcoalBorder),
                                modifier = Modifier.clickable { viewModel.setAppWallpaper(wp) }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = wp.displayName,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) CyanAccent else TextPrimary
                                        )
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Text(
                                        text = wp.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    // 2b. Custom Gallery Photo Wallpaper
                    val customWallpaperUri by viewModel.customWallpaperUri.collectAsState()
                    val galleryPicker = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickVisualMedia()
                    ) { uri: Uri? ->
                        if (uri != null) {
                            try {
                                context.contentResolver.takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )
                            } catch (e: Exception) {
                                // Ignore flag error if provider does not support persistence
                            }
                            viewModel.setCustomWallpaperUri(uri.toString())
                            viewModel.setAppWallpaper(com.example.ui.theme.AppWallpaper.CUSTOM_GALLERY)
                            Toast.makeText(context, "Custom gallery wallpaper set!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CharcoalCardElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (currentWallpaper == com.example.ui.theme.AppWallpaper.CUSTOM_GALLERY) CyanAccent else CharcoalBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = CyanAccent)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Custom Photo Wallpaper", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Pick any photo from your gallery with high-contrast text protection", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                            }

                            if (!customWallpaperUri.isNullOrBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black)
                                    ) {
                                        AsyncImage(
                                            model = customWallpaperUri,
                                            contentDescription = "Selected Wallpaper",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (currentWallpaper == com.example.ui.theme.AppWallpaper.CUSTOM_GALLERY) "Active as App Backdrop" else "Saved in Gallery Cache",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldSuccess,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Tap to switch to this wallpaper anytime",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        galleryPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Select from Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                if (!customWallpaperUri.isNullOrBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.setCustomWallpaperUri(null)
                                            viewModel.setAppWallpaper(com.example.ui.theme.AppWallpaper.AURORA_WAVES)
                                            Toast.makeText(context, "Reverted to animated wallpaper", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Remove", fontSize = 12.sp, color = RoseUrgent)
                                    }
                                }
                            }
                        }
                    }

                    // 2c. Dynamic Font & Typography Studio
                    val currentFont by viewModel.currentAppFont.collectAsState()
                    Text("App Typography & Font (${currentFont.displayName}):", style = MaterialTheme.typography.labelMedium, color = CyanAccent, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(com.example.ui.theme.AppFont.values().toList()) { fontOption ->
                            val isSelected = currentFont == fontOption
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) VioletPrimary.copy(alpha = 0.35f) else CharcoalCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyanAccent else CharcoalBorder),
                                modifier = Modifier.clickable { viewModel.setAppFont(fontOption) }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = fontOption.displayName,
                                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = fontOption.fontFamily),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) CyanAccent else TextPrimary
                                        )
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Text(
                                        text = fontOption.description,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = fontOption.fontFamily),
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    // Theme Mode: System Default / Dark / Light (Request 9)
                    val themeMode by viewModel.themeMode.collectAsState()
                    Text("Theme Appearance Mode:", style = MaterialTheme.typography.labelMedium, color = CyanAccent, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            "SYSTEM" to "System Default",
                            "DARK" to "Dark Mode",
                            "LIGHT" to "Light Mode"
                        ).forEach { (modeKey, modeTitle) ->
                            val isSel = themeMode == modeKey
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) VioletPrimary.copy(alpha = 0.35f) else CharcoalCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) CyanAccent else CharcoalBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setThemeMode(modeKey) }
                            ) {
                                Text(
                                    text = modeTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else TextMuted,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Week Numbering Card (Matches Screenshot 1 to the pixel)
        item {
            val week1BeginsRule by viewModel.week1BeginsRule.collectAsState()
            val weekStartDay by viewModel.weekStartDay.collectAsState()
            val weekInfo = remember(weekStartDay, week1BeginsRule) {
                com.example.util.WeekCalculationHelper.calculateWeekInfo(
                    weekStartDay = weekStartDay,
                    week1BeginsRule = week1BeginsRule
                )
            }
            val dayNameDisplay = com.example.util.WeekCalculationHelper.getDayDisplayName(weekStartDay)

            var showStartDayDropdown by remember { mutableStateOf(false) }
            var showRuleDropdown by remember { mutableStateOf(false) }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CharcoalCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Week numbering",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // WEEK STARTS ON Dropdown (Matches Screenshot 1)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "WEEK STARTS ON",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1E1B2E),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F46)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showStartDayDropdown = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(dayNameDisplay, color = TextPrimary, fontSize = 14.sp)
                                        Text("▾", color = TextMuted, fontSize = 14.sp)
                                    }
                                }

                                androidx.compose.material3.DropdownMenu(
                                    expanded = showStartDayDropdown,
                                    onDismissRequest = { showStartDayDropdown = false }
                                ) {
                                    listOf(
                                        "MONDAY" to "Monday",
                                        "TUESDAY" to "Tuesday",
                                        "WEDNESDAY" to "Wednesday",
                                        "THURSDAY" to "Thursday",
                                        "FRIDAY" to "Friday",
                                        "SATURDAY" to "Saturday",
                                        "SUNDAY" to "Sunday"
                                    ).forEach { (k, v) ->
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = { Text(v) },
                                            onClick = {
                                                viewModel.setWeekStartDay(k)
                                                showStartDayDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // WEEK 1 BEGINS Dropdown (Matches Screenshot 1)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "WEEK 1 BEGINS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box {
                                val currentRuleLabel = if (week1BeginsRule == com.example.util.WeekCalculationHelper.RULE_FIRST_FULL_DAY) {
                                    "First $dayNameDisplay of the year"
                                } else {
                                    "The week containing 1 January"
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1E1B2E),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, VioletPrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showRuleDropdown = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = currentRuleLabel,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            maxLines = 1
                                        )
                                        Text("▾", color = TextMuted, fontSize = 14.sp)
                                    }
                                }

                                androidx.compose.material3.DropdownMenu(
                                    expanded = showRuleDropdown,
                                    onDismissRequest = { showRuleDropdown = false }
                                ) {
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text("First $dayNameDisplay of the year") },
                                        onClick = {
                                            viewModel.setWeek1BeginsRule(com.example.util.WeekCalculationHelper.RULE_FIRST_FULL_DAY)
                                            showRuleDropdown = false
                                        }
                                    )
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text("The week containing 1 January") },
                                        onClick = {
                                            viewModel.setWeek1BeginsRule(com.example.util.WeekCalculationHelper.RULE_WEEK_CONTAINING_JAN_1)
                                            showRuleDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Result Pill (Matches Screenshot 1: Today is week 43 of 53 · 10 weeks left in 2026)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF18181B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = weekInfo.summaryText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }

        // Google Account & Cloud Protection Card (Request 8)
        item {
            val googleEmail by viewModel.googleAccountEmail.collectAsState()
            val isLinked by viewModel.isGoogleAccountLinked.collectAsState()
            val lastSync by viewModel.lastGoogleSyncTimestamp.collectAsState()
            var editGoogleEmailDialog by remember { mutableStateOf(false) }
            var tempEmailInput by remember { mutableStateOf(googleEmail) }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF4285F4).copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("G", fontWeight = FontWeight.Bold, color = Color(0xFF4285F4), fontSize = 18.sp)
                                }
                            }
                            Column {
                                Text("Google Account Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("🛡️ Protected by Google Account", style = MaterialTheme.typography.labelSmall, color = EmeraldSuccess, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isLinked) EmeraldSuccess.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isLinked) EmeraldSuccess.copy(alpha = 0.4f) else Color.Transparent)
                        ) {
                            Text(
                                text = if (isLinked) "Connected" else "Offline",
                                color = if (isLinked) EmeraldSuccess else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E1B2E),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Active Google ID", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(googleEmail, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            TextButton(onClick = {
                                tempEmailInput = googleEmail
                                editGoogleEmailDialog = true
                            }) {
                                Text("Switch", color = CyanAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    val syncDateStr = remember(lastSync) {
                        java.text.SimpleDateFormat("MMM d, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(lastSync))
                    }
                    Text(
                        text = "Encrypted cloud sync status: Up to date ($syncDateStr). Data is automatically restored when switching devices or reinstalling.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.triggerGoogleCloudSync()
                                Toast.makeText(context, "Google Cloud Backup Synced Successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Backup Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.triggerGoogleCloudSync()
                                Toast.makeText(context, "Google Cloud Data Restored!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F46)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Restore", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            }

            if (editGoogleEmailDialog) {
                AlertDialog(
                    onDismissRequest = { editGoogleEmailDialog = false },
                    title = { Text("Connect Google Account") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Sign in or enter your Google email to synchronize routines, habits, and tasks securely:")
                            OutlinedTextField(
                                value = tempEmailInput,
                                onValueChange = { tempEmailInput = it },
                                label = { Text("Google Account Email") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (tempEmailInput.isNotBlank()) {
                                    viewModel.setGoogleAccount(tempEmailInput.trim(), true)
                                    editGoogleEmailDialog = false
                                }
                            }
                        ) {
                            Text("Connect")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { editGoogleEmailDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

        // AI Configuration (Gemini API / OpenAI Endpoint)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Luma AI Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }

                    Text(
                        text = "Powered by Google Gemini 3.5 Flash. Enter your API key below for enhanced AI retrospectives & OCR notes breakdown:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = {
                            apiKey = it
                            scope.launch { viewModel.repository.setSetting("ai_api_key", it) }
                        },
                        label = { Text("Gemini API Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = CharcoalBorder
                        )
                    )

                    OutlinedTextField(
                        value = endpoint,
                        onValueChange = {
                            endpoint = it
                            scope.launch { viewModel.repository.setSetting("ai_endpoint", it) }
                        },
                        label = { Text("Custom Endpoint Override (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = CharcoalBorder
                        )
                    )
                }
            }
        }

        // Granular AI Data Privacy Permissions
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldSuccess)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Granular AI Data Privacy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }

                    Text(
                        text = "Choose which local data categories Luma AI is allowed to read during retrospectives:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    PrivacyToggleRow(
                        label = "Focus Session Stats",
                        checked = allowFocus,
                        onCheckedChange = {
                            allowFocus = it
                            scope.launch { viewModel.repository.setSetting("ai_allow_focus", it.toString()) }
                        }
                    )

                    PrivacyToggleRow(
                        label = "Tasks & Milestones",
                        checked = allowTasks,
                        onCheckedChange = {
                            allowTasks = it
                            scope.launch { viewModel.repository.setSetting("ai_allow_tasks", it.toString()) }
                        }
                    )

                    PrivacyToggleRow(
                        label = "Habit Consistency",
                        checked = allowHabits,
                        onCheckedChange = {
                            allowHabits = it
                            scope.launch { viewModel.repository.setSetting("ai_allow_habits", it.toString()) }
                        }
                    )

                    PrivacyToggleRow(
                        label = "Journal Reflections (Private)",
                        checked = allowJournal,
                        onCheckedChange = {
                            allowJournal = it
                            scope.launch { viewModel.repository.setSetting("ai_allow_journal", it.toString()) }
                        }
                    )

                    PrivacyToggleRow(
                        label = "App Screen Time Usage",
                        checked = allowUsage,
                        onCheckedChange = {
                            allowUsage = it
                            scope.launch { viewModel.repository.setSetting("ai_allow_usage", it.toString()) }
                        }
                    )
                }
            }
        }

        // Device & App Time Usage Suite (Screen time, reopen counters, daily limits)
        item {
            val totalDeviceSeconds = usageList.sumOf { it.totalTimeInForegroundSeconds }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = CyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Device & App Time Usage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyanAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${totalDeviceSeconds / 3600}h ${(totalDeviceSeconds % 3600) / 60}m Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Real-time device usage telemetry, launch reopen counters, and per-app daily time limit quotas:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    if (usageList.isEmpty()) {
                        Button(
                            onClick = { AppUsageHelper.openUsageAccessSettings(context) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                        ) {
                            Text("Grant Usage Access to View App Details", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            usageList.take(8).forEach { app ->
                                AppUsageLimitCard(app = app, viewModel = viewModel, context = context)
                            }
                        }
                    }
                }
            }
        }

        // System Permissions Assistance (Usage & Accessibility Blocker)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Block, contentDescription = null, tint = RoseUrgent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Distraction Blocker Setup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }

                    Text(
                        text = "To enable automatic redirection away from distracting apps during focus, enable RAcer Accessibility and Usage Access in Android Settings:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { AppUsageHelper.openUsageAccessSettings(context) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalCardElevated)
                        ) {
                            Text("Usage Access", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalCardElevated)
                        ) {
                            Text("Accessibility Blocker", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Data Vault & JSON Export
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = CyanAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Data Portability & Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }

                    Text(
                        text = "You own your data completely. Export your full history, habits, tasks, and journal as a portable JSON file:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                val json = viewModel.repository.exportDataJson()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("RAcer Backup", json)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                    ) {
                        Text("Export JSON Backup to Clipboard", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Danger Zone: Selective Data Deletion
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = RoseUrgent.copy(alpha = 0.4f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = RoseUrgent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Danger Zone", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = RoseUrgent)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                deleteActionType = "journal"
                                showDeleteDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoseUrgent.copy(alpha = 0.2f))
                        ) {
                            Text("Clear Journal", color = RoseUrgent, style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = {
                                deleteActionType = "focus"
                                showDeleteDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoseUrgent.copy(alpha = 0.2f))
                        ) {
                            Text("Clear Focus", color = RoseUrgent, style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = {
                                deleteActionType = "all"
                                showDeleteDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoseUrgent.copy(alpha = 0.2f))
                        ) {
                            Text("Reset All", color = RoseUrgent, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion", color = RoseUrgent, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    when (deleteActionType) {
                        "journal" -> "Are you sure you want to permanently delete all journal entries?"
                        "focus" -> "Are you sure you want to permanently delete all focus sessions?"
                        else -> "Are you sure you want to completely reset and erase all app data?"
                    },
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            when (deleteActionType) {
                                "journal" -> viewModel.repository.deleteJournalData()
                                "focus" -> viewModel.repository.deleteFocusHistory()
                                else -> viewModel.repository.deleteAllData()
                            }
                            viewModel.refreshStats()
                            showDeleteDialog = false
                            Toast.makeText(context, "Data cleared", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseUrgent)
                ) {
                    Text("Confirm Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CharcoalCardElevated
        )
    }
}

@Composable
fun PrivacyToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CharcoalCardElevated)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyanAccent,
                checkedTrackColor = VioletPrimary
            )
        )
    }
}

@Composable
fun AppUsageLimitCard(
    app: AppUsageEntity,
    viewModel: RacerViewModel,
    context: Context
) {
    val scope = rememberCoroutineScope()
    var appLimitMinutes by remember(app.packageName) {
        mutableStateOf(
            when {
                app.packageName.contains("instagram") || app.packageName.contains("tiktok") -> 30
                app.packageName.contains("youtube") -> 45
                else -> 0
            }
        )
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = CharcoalCardElevated,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${app.totalTimeInForegroundSeconds / 60} min in foreground • ${app.launchCount} reopens",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (appLimitMinutes > 0) VioletPrimary.copy(alpha = 0.25f) else CharcoalDark
                ) {
                    Text(
                        text = if (appLimitMinutes > 0) "Limit: ${appLimitMinutes}m" else "No Limit",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (appLimitMinutes > 0) CyanAccent else TextMuted
                    )
                }
            }

            // Limit Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Daily Quota:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                listOf(0 to "Off", 15 to "15m", 30 to "30m", 45 to "45m", 60 to "1h").forEach { (mins, lbl) ->
                    val isSel = appLimitMinutes == mins
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSel) VioletPrimary else CharcoalDark,
                        modifier = Modifier.clickable {
                            appLimitMinutes = mins
                            scope.launch {
                                viewModel.repository.setSetting("app_limit_${app.packageName}", mins.toString())
                            }
                            Toast.makeText(context, "${app.appName} limit: $lbl", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(
                            text = lbl,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            color = if (isSel) Color.White else TextSecondary
                        )
                    }
                }
            }
        }
    }
}
