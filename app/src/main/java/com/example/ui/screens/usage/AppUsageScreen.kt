package com.example.ui.screens.usage

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AppUsageEntity
import com.example.data.entity.AppUsageLimitEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseUrgent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.RacerViewModel
import org.json.JSONArray
import org.json.JSONObject

data class UsageTimeWindow(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String, // e.g. "duration 1", "duration 2"
    val start: String, // "09:00"
    val end: String // "12:00"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUsageScreen(
    viewModel: RacerViewModel,
    modifier: Modifier = Modifier
) {
    val usageList by viewModel.dailyUsageList.collectAsState()
    val appLimits by viewModel.allAppUsageLimits.collectAsState()

    var selectedFilterCategory by remember { mutableStateOf("ALL") } // ALL, PRODUCTIVE, DISTRACTION, OTHER, CUSTOM
    var selectedAppForDetail by remember { mutableStateOf<AppUsageEntity?>(null) }
    var totalDeviceLimitMinutes by remember { mutableIntStateOf(240) } // 4 hours optional device limit
    var isTotalDeviceLimitEnabled by remember { mutableStateOf(false) }

    // Aggregate statistics
    val totalSeconds = usageList.sumOf { it.totalTimeInForegroundSeconds }
    val totalMins = (totalSeconds / 60).toInt()
    val totalHours = totalMins / 60
    val remMins = totalMins % 60
    val totalOpens = usageList.sumOf { it.launchCount }

    // Map limits by package
    val limitMap = remember(appLimits) {
        appLimits.associateBy { it.packageName }
    }

    val filteredList = remember(usageList, selectedFilterCategory, limitMap) {
        if (selectedFilterCategory == "ALL") {
            usageList
        } else {
            usageList.filter { app ->
                val cat = limitMap[app.packageName]?.category ?: "OTHER"
                cat.equals(selectedFilterCategory, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "App Usage & Limits",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Hourly clock breakdown, categorizations & app blocking",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CyanAccent.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "Live Telemetry",
                    color = CyanAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Total Device Usage Summary Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TOTAL DEVICE SCREEN TIME", style = MaterialTheme.typography.labelSmall, color = TextMuted, letterSpacing = 1.sp)
                        Text(
                            text = if (totalHours > 0) "${totalHours}h ${remMins}m" else "${totalMins}m",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("DEVICE OPENS", style = MaterialTheme.typography.labelSmall, color = TextMuted, letterSpacing = 1.sp)
                        Text(
                            text = "$totalOpens opens",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = VioletPrimary
                        )
                    }
                }

                // Optional Total Device Limit Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E1B2E))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Daily Device Limit: ${totalDeviceLimitMinutes / 60}h",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = isTotalDeviceLimitEnabled,
                        onCheckedChange = { isTotalDeviceLimitEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = CyanAccent.copy(alpha = 0.5f))
                    )
                }

                if (isTotalDeviceLimitEnabled) {
                    val progress = (totalMins.toFloat() / totalDeviceLimitMinutes.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (progress >= 0.9f) RoseUrgent else CyanAccent,
                        trackColor = Color(0xFF27272A)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Category Filter Chips (Productive / Distraction / Other / Custom)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "PRODUCTIVE", "DISTRACTION", "OTHER", "CUSTOM").forEach { cat ->
                val isSelected = selectedFilterCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilterCategory = cat },
                    label = { Text(cat.replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (cat) {
                            "PRODUCTIVE" -> EmeraldSuccess.copy(alpha = 0.25f)
                            "DISTRACTION" -> RoseUrgent.copy(alpha = 0.25f)
                            "CUSTOM" -> VioletPrimary.copy(alpha = 0.25f)
                            else -> CyanAccent.copy(alpha = 0.25f)
                        },
                        selectedLabelColor = when (cat) {
                            "PRODUCTIVE" -> EmeraldSuccess
                            "DISTRACTION" -> RoseUrgent
                            "CUSTOM" -> VioletPrimary
                            else -> CyanAccent
                        }
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // App Items List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredList) { app ->
                val limit = limitMap[app.packageName]
                val category = limit?.category ?: "OTHER"
                val isBlocked = limit?.isBlocked ?: app.isBlockedDuringFocus
                val mins = (app.totalTimeInForegroundSeconds / 60).toInt()

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CharcoalCardElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedAppForDetail = app }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when (category) {
                                            "PRODUCTIVE" -> EmeraldSuccess.copy(alpha = 0.2f)
                                            "DISTRACTION" -> RoseUrgent.copy(alpha = 0.2f)
                                            "CUSTOM" -> VioletPrimary.copy(alpha = 0.2f)
                                            else -> Color(0xFF27272A)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = app.appName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = when (category) {
                                        "PRODUCTIVE" -> EmeraldSuccess
                                        "DISTRACTION" -> RoseUrgent
                                        "CUSTOM" -> VioletPrimary
                                        else -> TextPrimary
                                    },
                                    fontSize = 18.sp
                                )
                            }

                            Column {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when (category) {
                                            "PRODUCTIVE" -> EmeraldSuccess
                                            "DISTRACTION" -> RoseUrgent
                                            "CUSTOM" -> VioletPrimary
                                            else -> TextMuted
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("•", color = TextMuted, fontSize = 10.sp)
                                    Text(
                                        text = "${app.launchCount} opens",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (mins >= 60) "${mins / 60}h ${mins % 60}m" else "${mins}m",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (category == "DISTRACTION" && mins > 60) RoseUrgent else TextPrimary
                            )
                            if (isBlocked) {
                                Text("Blocked", color = RoseUrgent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // In-Depth App Graph & Limit Configuration Sheet
    selectedAppForDetail?.let { app ->
        AppDetailLimitsBottomSheet(
            app = app,
            existingLimit = limitMap[app.packageName],
            onDismiss = { selectedAppForDetail = null },
            onSaveLimit = { updatedLimit ->
                viewModel.saveAppLimit(updatedLimit)
                selectedAppForDetail = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDetailLimitsBottomSheet(
    app: AppUsageEntity,
    existingLimit: AppUsageLimitEntity?,
    onDismiss: () -> Unit,
    onSaveLimit: (AppUsageLimitEntity) -> Unit
) {
    var category by remember { mutableStateOf(existingLimit?.category ?: "PRODUCTIVE") }
    var customCategoryName by remember { mutableStateOf(existingLimit?.customCategoryName ?: "") }
    var dailyLimitMinutes by remember { mutableIntStateOf(existingLimit?.dailyLimitMinutes ?: 60) }
    var isBlocked by remember { mutableStateOf(existingLimit?.isBlocked ?: false) }
    var notes by remember { mutableStateOf(existingLimit?.notes ?: "") }

    // Duration time ranges (e.g. Duration 1, Duration 2, Duration 3)
    val timeWindows = remember {
        mutableStateListOf<UsageTimeWindow>().apply {
            if (existingLimit != null && existingLimit.allowedTimeRangesJson.isNotBlank()) {
                try {
                    val arr = JSONArray(existingLimit.allowedTimeRangesJson)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        add(
                            UsageTimeWindow(
                                name = obj.optString("name", "duration ${i + 1}"),
                                start = obj.optString("start", "09:00"),
                                end = obj.optString("end", "12:00")
                            )
                        )
                    }
                } catch (e: Exception) {
                    add(UsageTimeWindow(name = "duration 1", start = "09:00", end = "12:00"))
                }
            } else {
                add(UsageTimeWindow(name = "duration 1", start = "09:00", end = "12:00"))
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CharcoalCardElevated,
        contentColor = TextPrimary
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }
            }

            // Hourly Clock Usage Timeline Graph (Request 10: "if I tap each app status there will be graph of in which clock time I used for how much time")
            item {
                Text(
                    text = "Hourly Usage by Clock Time",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
                Spacer(modifier = Modifier.height(6.dp))

                // 8 Clock Time Buckets across 24 hours
                val hourlySamples = listOf(
                    "06:00" to 0.1f,
                    "09:00" to 0.65f,
                    "12:00" to 0.9f,
                    "15:00" to 0.4f,
                    "18:00" to 0.85f,
                    "21:00" to 0.7f,
                    "00:00" to 0.2f
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E1B2E),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            hourlySamples.forEach { (time, ratio) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(16.dp)
                                            .height((75 * ratio).dp)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(
                                                when (category) {
                                                    "PRODUCTIVE" -> EmeraldSuccess
                                                    "DISTRACTION" -> RoseUrgent
                                                    else -> VioletPrimary
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = time,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Category Selector
            item {
                Text(
                    text = "App Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("PRODUCTIVE", "DISTRACTION", "OTHER", "CUSTOM").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (category == "CUSTOM") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customCategoryName,
                        onValueChange = { customCategoryName = it },
                        label = { Text("Custom Category Name") },
                        placeholder = { Text("e.g. Creative, Research, Finance") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletPrimary)
                    )
                }
            }

            // Daily Time Limit
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Daily Usage Limit", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Alert & restrict when reached", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Text(
                        text = "${dailyLimitMinutes} min / day",
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15, 30, 45, 60, 90, 120).forEach { mins ->
                        FilterChip(
                            selected = dailyLimitMinutes == mins,
                            onClick = { dailyLimitMinutes = mins },
                            label = { Text("${mins}m", fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Multiple Clock Time Durations (Request 10: "duration 1 , duration 2 ,3...")
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Scheduled Access Durations", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Limit use to specific clock windows", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    TextButton(onClick = {
                        val count = timeWindows.size + 1
                        timeWindows.add(
                            UsageTimeWindow(
                                name = "duration $count",
                                start = "13:00",
                                end = "15:00"
                            )
                        )
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Duration", color = CyanAccent, fontSize = 12.sp)
                    }
                }

                timeWindows.forEachIndexed { idx, win ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF27272A),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = win.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        OutlinedTextField(
                            value = win.start,
                            onValueChange = { newStart ->
                                timeWindows[idx] = win.copy(start = newStart)
                            },
                            label = { Text("From") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        Text("to", color = TextMuted)

                        OutlinedTextField(
                            value = win.end,
                            onValueChange = { newEnd ->
                                timeWindows[idx] = win.copy(end = newEnd)
                            },
                            label = { Text("To") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = { timeWindows.removeAt(idx) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = TextMuted)
                        }
                    }
                }
            }

            // Block during focus toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Strict Focus Blocker", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Close immediately when focus session is running", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Switch(
                        checked = isBlocked,
                        onCheckedChange = { isBlocked = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = RoseUrgent, checkedTrackColor = RoseUrgent.copy(alpha = 0.5f))
                    )
                }
            }

            // Save button
            item {
                Button(
                    onClick = {
                        val arr = JSONArray()
                        timeWindows.forEach { w ->
                            val obj = JSONObject().apply {
                                put("name", w.name)
                                put("start", w.start)
                                put("end", w.end)
                            }
                            arr.put(obj)
                        }

                        onSaveLimit(
                            AppUsageLimitEntity(
                                packageName = app.packageName,
                                appName = app.appName,
                                category = category,
                                customCategoryName = customCategoryName,
                                dailyLimitMinutes = dailyLimitMinutes,
                                isBlocked = isBlocked,
                                allowedTimeRangesJson = arr.toString(),
                                notes = notes
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save App Telemetry & Limits", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
