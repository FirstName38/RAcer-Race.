package com.example.ui.screens.journal

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.entity.JournalEntryEntity
import com.example.data.model.JournalMood
import com.example.ui.components.GlassCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CharcoalBorder
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun JournalScreen(
    viewModel: RacerViewModel,
    modifier: Modifier = Modifier
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displaySdf = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }

    var activeTab by remember { mutableIntStateOf(0) } // 0: Daily Editor, 1: Compare Days

    var currentDateCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    val currentDateStr = sdf.format(currentDateCalendar.time)

    val entries by viewModel.allJournalEntries.collectAsState()
    val existingEntry = entries.find { it.dateString == currentDateStr }

    var learnedText by remember { mutableStateOf("") }
    var feltText by remember { mutableStateOf("") }
    var wantToDoText by remember { mutableStateOf("") }
    var gratitudeText by remember { mutableStateOf("") }
    var freeformNotes by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf(JournalMood.GOOD) }
    var showSavedBanner by remember { mutableStateOf(false) }

    LaunchedEffect(currentDateStr, existingEntry) {
        if (existingEntry != null) {
            learnedText = existingEntry.learnedText
            feltText = existingEntry.feltText
            wantToDoText = existingEntry.wantToDoText
            gratitudeText = existingEntry.gratitudeText
            freeformNotes = existingEntry.freeformNotes
            mood = existingEntry.mood
        } else {
            learnedText = ""
            feltText = ""
            wantToDoText = ""
            gratitudeText = ""
            freeformNotes = ""
            mood = JournalMood.GOOD
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Header and Mode Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Reflective Journal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Daily introspection, gratitude & multi-day pattern comparison.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TabRow(
            selectedTabIndex = activeTab,
            containerColor = CharcoalCard,
            contentColor = CyanAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = CyanAccent
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Daily Entry", fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Compare Days", fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeTab == 0) {
            // SINGLE DAY VIEW
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    // Date Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            val prev = (currentDateCalendar.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
                            currentDateCalendar = prev
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day", tint = CyanAccent)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = displaySdf.format(currentDateCalendar.time),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (currentDateStr == sdf.format(Date())) "Today" else currentDateStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }

                        IconButton(onClick = {
                            val next = (currentDateCalendar.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
                            currentDateCalendar = next
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day", tint = CyanAccent)
                        }
                    }
                }

                if (showSavedBanner) {
                    item {
                        Surface(
                            color = EmeraldSuccess.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Entry saved securely on-device.", color = EmeraldSuccess, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Mood Selector
                item {
                    Column {
                        Text(
                            text = "Daily State of Mind",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            JournalMood.entries.forEach { itemMood ->
                                val isSelected = mood == itemMood
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { mood = itemMood }
                                        .background(
                                            if (isSelected) VioletPrimary.copy(alpha = 0.25f) else CharcoalCard
                                        )
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.Transparent
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = itemMood.emoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = itemMood.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected) VioletPrimary else TextMuted,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Prompt 1: What did I learn today?
                item {
                    JournalPromptCard(
                        title = "What did I learn today?",
                        value = learnedText,
                        onValueChange = { learnedText = it },
                        placeholder = "Key insights, lessons, breakthroughs...",
                        accentColor = CyanAccent
                    )
                }

                // Prompt 2: How did I feel?
                item {
                    JournalPromptCard(
                        title = "How did I feel? (Emotional Climate)",
                        value = feltText,
                        onValueChange = { feltText = it },
                        placeholder = "Energy levels, obstacles, flow triggers...",
                        accentColor = VioletPrimary
                    )
                }

                // Prompt 3: What do I want to do next?
                item {
                    JournalPromptCard(
                        title = "What do I want to accomplish next?",
                        value = wantToDoText,
                        onValueChange = { wantToDoText = it },
                        placeholder = "Tomorrow's top priority, focus intent...",
                        accentColor = VioletPrimary
                    )
                }

                // Prompt 4: Gratitude
                item {
                    JournalPromptCard(
                        title = "What am I grateful for today?",
                        value = gratitudeText,
                        onValueChange = { gratitudeText = it },
                        placeholder = "Small wins, supportive moments, simple comforts...",
                        accentColor = EmeraldSuccess
                    )
                }

                // Freeform notes
                item {
                    JournalPromptCard(
                        title = "Freeform Notes & Deep Reflections",
                        value = freeformNotes,
                        onValueChange = { freeformNotes = it },
                        placeholder = "Stream of consciousness, brainstorms...",
                        accentColor = AmberWarning
                    )
                }

                item {
                    Button(
                        onClick = {
                            viewModel.saveJournal(
                                dateString = currentDateStr,
                                learnedText = learnedText,
                                feltText = feltText,
                                wantToDoText = wantToDoText,
                                gratitudeText = gratitudeText,
                                freeformNotes = freeformNotes,
                                mood = mood
                            )
                            showSavedBanner = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Reflection", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        } else {
            // COMPARE DAYS MULTI-COLUMN MATRIX
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Side-by-Side Day Progression",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Scroll horizontally to compare past reflections. Tap any day card to open and edit full details.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                item {
                    val pastDays = remember(entries) {
                        val cal = Calendar.getInstance()
                        (0..14).map { offset ->
                            val c = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -offset) }
                            val dStr = sdf.format(c.time)
                            val entry = entries.find { it.dateString == dStr }
                            Triple(dStr, displaySdf.format(c.time), entry)
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(pastDays) { (dStr, dDisplay, entry) ->
                            CompareDayColumnCard(
                                dateString = dStr,
                                displayDate = dDisplay,
                                entry = entry,
                                onClick = {
                                    val parsedCal = Calendar.getInstance().apply {
                                        try { time = sdf.parse(dStr) ?: Date() } catch (e: Exception) {}
                                    }
                                    currentDateCalendar = parsedCal
                                    activeTab = 0
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

@Composable
fun JournalPromptCard(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    accentColor: Color
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = accentColor.copy(alpha = 0.35f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = TextMuted, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = CharcoalBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                minLines = 2
            )
        }
    }
}

@Composable
fun CompareDayColumnCard(
    dateString: String,
    displayDate: String,
    entry: JournalEntryEntity?,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() },
        borderColor = if (entry != null) CyanAccent.copy(alpha = 0.4f) else CharcoalBorder
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (entry != null) {
                    Text(text = entry.mood.emoji, fontSize = 18.sp)
                }
            }

            Text(
                text = displayDate.take(10),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (entry != null) {
                if (entry.learnedText.isNotBlank()) {
                    Text("💡 Learned:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CyanAccent)
                    Text(entry.learnedText, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (entry.gratitudeText.isNotBlank()) {
                    Text("🙏 Gratitude:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                    Text(entry.gratitudeText, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (entry.wantToDoText.isNotBlank()) {
                    Text("🎯 Next:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = VioletPrimary)
                    Text(entry.wantToDoText, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2)
                }
            } else {
                Text(
                    text = "No entry logged for this day.\nTap to write reflection.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = CyanAccent.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tap to View / Edit",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
