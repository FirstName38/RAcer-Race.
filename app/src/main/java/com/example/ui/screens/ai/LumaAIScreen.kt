package com.example.ui.screens.ai

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ChatMessageEntity
import com.example.data.model.TaskPriority
import com.example.ui.components.GlassCard
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.RacerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LumaAIScreen(
    viewModel: RacerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val messages by viewModel.chatMessages.collectAsState()
    val isAILoading by viewModel.isAILoading.collectAsState()

    var userText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Dialog state
    var showStudyPlanDialog by remember { mutableStateOf(false) }
    var showExamPlanDialog by remember { mutableStateOf(false) }
    var showMentalHealthDialog by remember { mutableStateOf(false) }
    var quickTaskTitle by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        if (uri != null) {
            selectedBitmap = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyanAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Luma Study & Focus Coach",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Personalized Study Plans, Exam Prep & ADHD Coaching",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }

        // Quick Planning Action Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionPill(
                icon = Icons.Default.Psychology,
                label = "🧠 ADHD & Mental Health",
                accentColor = VioletPrimary,
                onClick = { showMentalHealthDialog = true }
            )
            ActionPill(
                icon = Icons.Default.School,
                label = "📚 Study Plan",
                accentColor = CyanAccent,
                onClick = { showStudyPlanDialog = true }
            )
            ActionPill(
                icon = Icons.Default.CalendarMonth,
                label = "🎯 Exam Plan",
                accentColor = PinkAccent,
                onClick = { showExamPlanDialog = true }
            )
            ActionPill(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                label = "Active Recall Plan",
                onClick = {
                    userText = "Design an Active Recall & Spaced Repetition schedule for: "
                }
            )
            ActionPill(
                icon = Icons.Default.Psychology,
                label = "ADHD Micro-Steps",
                onClick = { userText = "Break down my study goal into 3 easy micro-steps: " }
            )
            ActionPill(
                icon = Icons.Default.Timeline,
                label = "Daily Review",
                onClick = { viewModel.generateDailyRetrospective() }
            )
            ActionPill(
                icon = Icons.Default.AutoAwesome,
                label = "Weekly Review",
                onClick = { viewModel.generateWeeklyRetrospective() }
            )
        }

        // Chat Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Hi! I'm Luma, your Study & Exam Coach.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "I can build tailored study plans, exam countdown roadmaps, spaced repetition schedules, or deconstruct complex textbook chapters into easy focus blocks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showStudyPlanDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Study Plan", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { showExamPlanDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PinkAccent),
                                    border = BorderStroke(1.dp, PinkAccent.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Exam Prep", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                ChatBubble(
                    message = msg,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(msg.message))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onAddTask = {
                        quickTaskTitle = it
                    }
                )
            }

            if (isAILoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CharcoalCardElevated
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = CyanAccent,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Luma is formulating your plan...", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Attached Image Preview
        if (selectedBitmap != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalCardElevated)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    bitmap = selectedBitmap!!.asImageBitmap(),
                    contentDescription = "Uploaded note",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Study note attached for OCR analysis", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    selectedImageUri = null
                    selectedBitmap = null
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextMuted)
                }
            }
        }

        // Input Field Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { galleryLauncher.launch("image/*") }
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Attach Notes",
                    tint = if (selectedBitmap != null) CyanAccent else TextMuted
                )
            }

            OutlinedTextField(
                value = userText,
                onValueChange = { userText = it },
                placeholder = { Text("Ask Luma (e.g. study plan, exam prep)...", color = TextMuted, style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = CharcoalBorder,
                    focusedContainerColor = CharcoalCard,
                    unfocusedContainerColor = CharcoalCard
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (userText.isNotBlank() || selectedBitmap != null) {
                        val textToSend = if (userText.isNotBlank()) userText else "Analyze this study note and formulate a study plan"
                        viewModel.askLuma(textToSend, selectedBitmap)
                        userText = ""
                        selectedImageUri = null
                        selectedBitmap = null
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(VioletPrimary)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }

    // --- Study Plan Generator Dialog ---
    if (showStudyPlanDialog) {
        StudyPlanGeneratorDialog(
            onDismiss = { showStudyPlanDialog = false },
            onGenerate = { subject, hours, weeks, topics, style ->
                viewModel.generateStudyPlan(subject, hours, weeks, topics, style)
                showStudyPlanDialog = false
            }
        )
    }

    // --- Exam Plan Generator Dialog ---
    if (showExamPlanDialog) {
        ExamPlanGeneratorDialog(
            onDismiss = { showExamPlanDialog = false },
            onGenerate = { examName, examDate, daysLeft, topics, targetGoal, addToCalendar ->
                viewModel.generateExamPlan(examName, examDate, daysLeft, topics, targetGoal)
                if (addToCalendar) {
                    viewModel.setSpecialDate(
                        dateString = examDate,
                        title = "🎯 $examName",
                        note = "Target Goal: $targetGoal | High Yield: $topics",
                        colorHex = "#EC4899"
                    )
                    Toast.makeText(context, "Added $examName to RAcer Calendar!", Toast.LENGTH_SHORT).show()
                }
                showExamPlanDialog = false
            }
        )
    }

    // --- ADHD & Mental Health State Analysis Dialog ---
    if (showMentalHealthDialog) {
        MentalHealthAnalysisDialog(
            onDismiss = { showMentalHealthDialog = false },
            onAnalyze = { state, notes ->
                viewModel.generateMentalHealthAnalysis(state, notes)
                showMentalHealthDialog = false
            }
        )
    }

    // --- Quick Add Task from Plan Dialog ---
    if (quickTaskTitle != null) {
        QuickAddTaskDialog(
            initialTitle = quickTaskTitle!!,
            onDismiss = { quickTaskTitle = null },
            onAdd = { title, priority, estimatedMin ->
                viewModel.addTask(
                    title = title,
                    priority = priority,
                    estimatedMinutes = estimatedMin,
                    category = "Study"
                )
                Toast.makeText(context, "Added '$title' to Tasks!", Toast.LENGTH_SHORT).show()
                quickTaskTitle = null
            }
        )
    }
}

@Composable
fun ActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accentColor: Color = CyanAccent,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CharcoalCardElevated,
        border = BorderStroke(1.dp, CharcoalBorder),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessageEntity,
    onCopy: () -> Unit,
    onAddTask: (String) -> Unit
) {
    val isUser = message.sender == "USER"
    val isPlan = message.category in listOf("study_plan", "exam_plan")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) VioletPrimary else CharcoalCardElevated,
            border = if (!isUser && isPlan) BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)) else null,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (!isUser && isPlan) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (message.category == "study_plan") "📚 STUDY PLAN" else "🎯 EXAM ROADMAP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (message.category == "study_plan") CyanAccent else PinkAccent
                        )

                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else TextPrimary,
                    lineHeight = 20.sp
                )

                if (!isUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMuted, modifier = Modifier.size(14.dp))
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                val firstLine = message.message.lines().firstOrNull { it.isNotBlank() } ?: "Study Focus Session"
                                val clean = firstLine.replace("#", "").replace("*", "").trim().take(40)
                                onAddTask(clean)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = "Add to Tasks", tint = CyanAccent, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// --- Interactive Dialogs ---

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyPlanGeneratorDialog(
    onDismiss: () -> Unit,
    onGenerate: (subject: String, hours: Float, weeks: Int, topics: String, style: String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var hoursPerDay by remember { mutableFloatStateOf(2.0f) }
    var targetWeeks by remember { mutableIntStateOf(2) }
    var focusTopics by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Pomodoro (25/5)") }

    val styles = listOf("Pomodoro (25/5)", "Deep Work (50/10)", "ADHD Gentle (15m)", "Active Recall & Flashcards")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, contentDescription = null, tint = CyanAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Study Plan Generator", color = TextPrimary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject / Course Name") },
                    placeholder = { Text("e.g. Organic Chemistry, Algorithms, History") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CharcoalBorder
                    ),
                    singleLine = true
                )

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Daily Study Time:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text("${String.format(Locale.getDefault(), "%.1f", hoursPerDay)} hrs/day", style = MaterialTheme.typography.bodySmall, color = CyanAccent, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = hoursPerDay,
                        onValueChange = { hoursPerDay = it },
                        valueRange = 0.5f..8.0f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanAccent,
                            activeTrackColor = CyanAccent
                        )
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Plan Duration:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text("$targetWeeks week(s)", style = MaterialTheme.typography.bodySmall, color = VioletPrimary, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 2, 4, 8).forEach { weeks ->
                            FilterChip(
                                selected = targetWeeks == weeks,
                                onClick = { targetWeeks = weeks },
                                label = { Text("${weeks}w") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VioletPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = focusTopics,
                    onValueChange = { focusTopics = it },
                    label = { Text("Challenging Topics / Focus Areas") },
                    placeholder = { Text("e.g. Chapter 4-7, Proofs, Formulas") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CharcoalBorder
                    ),
                    maxLines = 2
                )

                Column {
                    Text("Preferred Study Pacing:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        styles.forEach { style ->
                            FilterChip(
                                selected = selectedStyle == style,
                                onClick = { selectedStyle = style },
                                label = { Text(style, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanAccent.copy(alpha = 0.3f),
                                    selectedLabelColor = CyanAccent
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isNotBlank()) {
                        onGenerate(subject, hoursPerDay, targetWeeks, focusTopics, selectedStyle)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                enabled = subject.isNotBlank()
            ) {
                Text("Generate Plan", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = CharcoalCardElevated
    )
}

@Composable
fun ExamPlanGeneratorDialog(
    onDismiss: () -> Unit,
    onGenerate: (examName: String, examDate: String, daysLeft: Int, topics: String, targetGoal: String, addToCalendar: Boolean) -> Unit
) {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_MONTH, 14)
    val defaultDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

    var examName by remember { mutableStateOf("") }
    var examDateStr by remember { mutableStateOf(defaultDate) }
    var daysRemaining by remember { mutableIntStateOf(14) }
    var highYieldTopics by remember { mutableStateOf("") }
    var targetGoal by remember { mutableStateOf("Top Grade (A / High Confidence)") }
    var addToCalendar by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = PinkAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exam Preparation Roadmap", color = TextPrimary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = examName,
                    onValueChange = { examName = it },
                    label = { Text("Exam Name") },
                    placeholder = { Text("e.g. Final Exam, MCAT, AP Physics, Midterm") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PinkAccent,
                        unfocusedBorderColor = CharcoalBorder
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = examDateStr,
                        onValueChange = { examDateStr = it },
                        label = { Text("Exam Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PinkAccent,
                            unfocusedBorderColor = CharcoalBorder
                        ),
                        singleLine = true
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Days Remaining:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text("$daysRemaining days", style = MaterialTheme.typography.bodySmall, color = PinkAccent, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = daysRemaining.toFloat(),
                        onValueChange = { daysRemaining = it.toInt() },
                        valueRange = 1f..60f,
                        steps = 58,
                        colors = SliderDefaults.colors(
                            thumbColor = PinkAccent,
                            activeTrackColor = PinkAccent
                        )
                    )
                }

                OutlinedTextField(
                    value = highYieldTopics,
                    onValueChange = { highYieldTopics = it },
                    label = { Text("Weak / High-Yield Topics") },
                    placeholder = { Text("e.g. Thermodynamics, Reaction Mechanisms") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PinkAccent,
                        unfocusedBorderColor = CharcoalBorder
                    ),
                    maxLines = 2
                )

                OutlinedTextField(
                    value = targetGoal,
                    onValueChange = { targetGoal = it },
                    label = { Text("Target Ambition / Score") },
                    placeholder = { Text("e.g. 95%+, Pass with high confidence") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PinkAccent,
                        unfocusedBorderColor = CharcoalBorder
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = addToCalendar,
                        onCheckedChange = { addToCalendar = it },
                        colors = CheckboxDefaults.colors(checkedColor = PinkAccent)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("📌 Add Exam Date to RAcer Calendar Matrix", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (examName.isNotBlank()) {
                        onGenerate(examName, examDateStr, daysRemaining, highYieldTopics, targetGoal, addToCalendar)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PinkAccent),
                enabled = examName.isNotBlank()
            ) {
                Text("Build Exam Roadmap", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = CharcoalCardElevated
    )
}

@Composable
fun QuickAddTaskDialog(
    initialTitle: String,
    onDismiss: () -> Unit,
    onAdd: (title: String, priority: TaskPriority, estimatedMinutes: Int) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var priority by remember { mutableStateOf(TaskPriority.HIGH) }
    var estimatedMinutes by remember { mutableIntStateOf(25) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = CyanAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Plan Item as Task", color = TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CharcoalBorder
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskPriority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.displayName, fontSize = 12.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(15, 25, 45, 60).forEach { mins ->
                        FilterChip(
                            selected = estimatedMinutes == mins,
                            onClick = { estimatedMinutes = mins },
                            label = { Text("${mins}m") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(title, priority, estimatedMinutes) },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Add Task", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = CharcoalCardElevated
    )
}

@Composable
fun MentalHealthAnalysisDialog(
    onDismiss: () -> Unit,
    onAnalyze: (currentState: String, notes: String) -> Unit
) {
    var selectedState by remember { mutableStateOf("ADHD Hyper or Zero paralysis") }
    var notes by remember { mutableStateOf("") }

    val states = listOf(
        "ADHD Hyper or Zero paralysis",
        "Hyperfocused / High dopamine",
        "Zero energy / Dopamine crash",
        "Overwhelmed / Too many tasks",
        "Anxious about upcoming deadline",
        "Burned out / Mental fatigue"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = VioletPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ADHD Mental Health Analysis", color = TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Tell Luma how your ADHD / energy feels right now. Luma will analyze your state and provide neurodivergent-friendly actionable steps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Text("Current Energy / Mental State:", style = MaterialTheme.typography.labelSmall, color = CyanAccent, fontWeight = FontWeight.Bold)

                states.forEach { state ->
                    val isSel = selectedState == state
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) VioletPrimary.copy(alpha = 0.25f) else CharcoalCard,
                        border = BorderStroke(1.dp, if (isSel) CyanAccent else CharcoalBorder),
                        modifier = Modifier.fillMaxWidth().clickable { selectedState = state }
                    ) {
                        Text(
                            text = state,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSel) CyanAccent else TextPrimary,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Extra details / How you feel") },
                    placeholder = { Text("e.g. Stuck on task 1, feeling hyperactive but can't start...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VioletPrimary,
                        unfocusedBorderColor = CharcoalBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAnalyze(selectedState, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
            ) {
                Text("Analyze with Luma", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = CharcoalCardElevated
    )
}
