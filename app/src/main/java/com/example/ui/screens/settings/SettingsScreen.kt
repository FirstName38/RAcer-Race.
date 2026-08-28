package com.example.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

        // Appearance & Aesthetic Theme
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = VioletPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Visual Experience", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }

                    Text("Dark-first soothing theme active (#12121A) with Violet/Cyan luminous gradients to lower visual sensory overload.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
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
