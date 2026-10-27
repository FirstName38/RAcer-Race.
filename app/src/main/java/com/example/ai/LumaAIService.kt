package com.example.ai

import android.graphics.Bitmap
import com.example.data.entity.ChatMessageEntity
import com.example.data.repository.RacerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LumaAIService(private val repository: RacerRepository) {

    suspend fun askLuma(
        userMessage: String,
        imageBitmap: Bitmap? = null
    ): String = withContext(Dispatchers.IO) {
        val openaiKey = repository.getSetting("openai_api_key", "").ifBlank {
            repository.getSetting("ai_api_key", "")
        }
        val geminiKey = repository.getSetting("gemini_api_key", "").ifBlank {
            repository.getSetting("ai_api_key", "")
        }
        val provider = repository.getSetting("ai_provider", "AUTO")
        val endpoint = repository.getSetting("ai_endpoint", "")

        // Gather allowed context based on privacy toggles
        val contextPrompt = buildGroundingContext()

        val fullPrompt = buildString {
            append("You are Luma, an exceptionally brilliant, universal AI intellect and empathetic cognitive companion in FocusFlow.\n")
            append("YOUR KNOWLEDGE DOMAIN: Universal and boundless. You possess deep knowledge across all human fields: computer science, software engineering, code debugging (Kotlin, Python, JS, C++, etc.), mathematics, physics, biology, history, world trivia, literature, philosophy, finance, psychology, academic study, essay writing, and creative problem solving.\n")
            append("YOUR INTEGRATION WITH ON-DEVICE APP STORAGE: You are simultaneously grounded with real-time telemetry from the user's focus sessions, daily routines, tasks, habits, weekly plans, and streak records stored on this device.\n")
            append("INSTRUCTIONS:\n")
            append("- When asked about programming, science, math, history, writing, philosophy, or general knowledge: Answer comprehensively with authoritative, step-by-step explanations, code blocks, and clear reasoning.\n")
            append("- When asked about productivity, habits, tasks, focus, or study routines: Combine your universal expertise with their real app storage data (metrics, streaks, pending tasks, weekly goals).\n")
            append("- Tone: Warm, highly intelligent, encouraging, low cognitive friction, structured with clean Markdown headers and bullet points.\n\n")
            append("--- USER'S PERMITTED DATA STORAGE CONTEXT ---\n")
            append(contextPrompt)
            append("\n--- USER QUERY ---\n")
            append(userMessage)
        }

        val result: Result<String> = when {
            provider == "OPENAI" || (provider == "AUTO" && openaiKey.startsWith("sk-")) -> {
                val openAiRes = OpenAiApiClient.generateContent(fullPrompt, openaiKey)
                if (openAiRes.isSuccess) openAiRes else GeminiApiClient.generateContent(
                    prompt = fullPrompt,
                    apiKeyOverride = geminiKey.ifBlank { null },
                    endpointOverride = endpoint.ifBlank { null },
                    bitmap = imageBitmap
                )
            }
            geminiKey.isNotBlank() -> {
                GeminiApiClient.generateContent(
                    prompt = fullPrompt,
                    apiKeyOverride = geminiKey,
                    endpointOverride = endpoint.ifBlank { null },
                    bitmap = imageBitmap
                )
            }
            openaiKey.isNotBlank() -> {
                OpenAiApiClient.generateContent(fullPrompt, openaiKey)
            }
            else -> {
                // Try Gemini with server/BuildConfig key
                GeminiApiClient.generateContent(
                    prompt = fullPrompt,
                    apiKeyOverride = null,
                    endpointOverride = endpoint.ifBlank { null },
                    bitmap = imageBitmap
                )
            }
        }

        val responseText = result.getOrElse { error ->
            // High quality offline fallback with dynamic intent parsing, math, and contextual intelligence
            generateOfflineResponse(userMessage)
        }

        // Save conversation
        repository.insertChatMessage(
            ChatMessageEntity(sender = "USER", message = userMessage, category = "chat")
        )
        repository.insertChatMessage(
            ChatMessageEntity(sender = "LUMA", message = responseText, category = "chat")
        )

        responseText
    }

    suspend fun generateStudyPlan(
        subject: String,
        hoursPerDay: Float,
        targetDurationWeeks: Int,
        focusTopics: String,
        studyStyle: String
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are Luma, an expert ADHD-friendly academic and study coach.
            Create a comprehensive, low-stress, high-impact STUDY PLAN for the following requirements:
            - Subject / Course: $subject
            - Daily Available Study Time: $hoursPerDay hours/day
            - Target Duration: $targetDurationWeeks week(s)
            - Key Focus Topics / Challenging Areas: ${focusTopics.ifBlank { "Core concepts & full syllabus review" }}
            - Study Style Preference: $studyStyle

            Please format the response with clean headers and bullet points:
            1. 🎯 Executive Roadmap & Core Milestones
            2. 📅 Week-by-Week (or Day-by-Day) Progression Structure
            3. ⏱️ Daily Study Session Blueprint (e.g. 25m Focus + 5m Active Break + Review)
            4. 🧠 Active Recall & Mastery Techniques (Feynman Technique, Flashcards, Practice Sets)
            5. 🌿 ADHD Burnout Shields (What to do when energy is low)
            Keep it motivating, structured, and easy to execute.
        """.trimIndent()

        val apiKey = repository.getSetting("ai_api_key", "")
        val result = GeminiApiClient.generateContent(prompt, apiKey.ifBlank { null })
        val text = result.getOrElse {
            generateOfflineStudyPlan(subject, hoursPerDay, targetDurationWeeks, focusTopics, studyStyle)
        }

        repository.insertChatMessage(
            ChatMessageEntity(sender = "USER", message = "Create a $targetDurationWeeks-week Study Plan for $subject ($hoursPerDay hrs/day, $studyStyle)", category = "study_plan")
        )
        repository.insertChatMessage(
            ChatMessageEntity(sender = "LUMA", message = text, category = "study_plan")
        )
        text
    }

    suspend fun generateExamPlan(
        examName: String,
        examDateStr: String,
        daysLeft: Int,
        highYieldTopics: String,
        targetScoreGoal: String
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are Luma, an elite exam strategist and calm academic coach.
            Design a step-by-step EXAM PREPARATION & REVISION PLAN for:
            - Exam: $examName
            - Exam Date: $examDateStr ($daysLeft days remaining)
            - High-Priority / Weak Topics: ${highYieldTopics.ifBlank { "Comprehensive coverage & high-yield chapters" }}
            - Target Score / Ambition: ${targetScoreGoal.ifBlank { "Pass with high confidence" }}

            Format your response clearly:
            1. ⏳ Phase Breakdown (${daysLeft} Day Countdown Matrix)
               • Phase 1: High-Yield Concept Review & Gap Filling
               • Phase 2: Active Recall & Timed Practice Questions
               • Phase 3: Mock Exam Simulation & Error Log Review
               • Phase 4 (T-Minus 48h): High-level Summary Sheets & Calm Mindset
            2. 📝 Daily Exam Sprint Template (Focus Blocks + Recovery)
            3. 🎯 High-Yield Test Day Tips (Time management during exam, tackling hard questions)
            4. 🛡️ Pre-Exam Anxiety & Sleep Protocol
        """.trimIndent()

        val apiKey = repository.getSetting("ai_api_key", "")
        val result = GeminiApiClient.generateContent(prompt, apiKey.ifBlank { null })
        val text = result.getOrElse {
            generateOfflineExamPlan(examName, examDateStr, daysLeft, highYieldTopics, targetScoreGoal)
        }

        repository.insertChatMessage(
            ChatMessageEntity(sender = "USER", message = "Generate an Exam Prep & Revision Roadmap for $examName (Date: $examDateStr, $daysLeft days left)", category = "exam_plan")
        )
        repository.insertChatMessage(
            ChatMessageEntity(sender = "LUMA", message = text, category = "exam_plan")
        )
        text
    }

    suspend fun generateMentalHealthAnalysis(stateType: String = "general", notes: String = ""): String = withContext(Dispatchers.IO) {
        val stats = repository.calculateStats()
        val todayStr = repository.getTodayDateString()
        val journal = repository.getJournalEntryForDate(todayStr).firstOrNull()

        val prompt = """
            You are Luma, an empathetic ADHD mental health specialist and nervous system regulation coach.
            The user identifies as having ADHD with a "Hyper or Zero" cognitive and energy profile (swinging between hyperfocus/racing thoughts and dopamine paralysis/burnout).
            
            Requested State Focus: $stateType (e.g. paralysis, hyperfocus, dopamine crash, burnout).
            User's immediate notes / situation: ${if (notes.isNotBlank()) notes else "None provided"}
            
            Current user data:
            - Focus today: ${stats.todaySeconds / 60} mins (${stats.completedSessions} completed)
            - Weekly focus: ${stats.weekSeconds / 60} mins
            - Current streak: ${stats.currentStreak} days
            - Logged Mood today: ${journal?.mood?.label ?: "Neutral / Seeking balance"}
            - Journal Notes: ${journal?.feltText ?: "No notes recorded"}
            
            Provide a compassionate, zero-guilt, scientifically grounded analysis:
            1. 🧠 ADHD Nervous System & State Diagnosis (Hyper vs. Zero Spectrum)
            2. 🛡️ De-Escalation & Regulation Protocol for their current state
            3. 🌿 2-Minute Micro-Action (Tiny friction-free reset)
            4. 💧 Biological Grounding (Hydration, sensory environment, dopamine replenishment)
            Keep it deeply reassuring, actionable, and structured with clean bullet points.
        """.trimIndent()

        val apiKey = repository.getSetting("ai_api_key", "")
        val result = GeminiApiClient.generateContent(prompt, apiKey.ifBlank { null })
        val text = result.getOrElse {
            generateOfflineMentalHealthAnalysis(stateType, (stats.todaySeconds / 60).toInt(), stats.currentStreak, journal?.mood?.label ?: "Seeking Calm")
        }

        repository.insertChatMessage(
            ChatMessageEntity(sender = "USER", message = "Mental Health & ADHD Analysis ($stateType): ${notes.ifBlank { "State Check-in" }}", category = "mental_health")
        )
        repository.insertChatMessage(
            ChatMessageEntity(sender = "LUMA", message = text, category = "mental_health")
        )
        text
    }

    private fun generateOfflineMentalHealthAnalysis(
        stateType: String,
        todayFocusMins: Int,
        streak: Int,
        mood: String
    ): String {
        return when (stateType.lowercase()) {
            "zero" -> """
            🧠 Luma ADHD Mental Health: Zero State / Freeze Protocol
            
            🌿 What is happening in your brain:
            You are in a classic ADHD dopamine slump or nervous system freeze. This is NOT laziness, lack of willpower, or failure. Your brain is temporarily conserving neurotransmitters after cognitive overload.
            
            🛡️ Immediate Zero-Guilt Steps:
            1. 🛑 Drop all unrealistic expectations for the next 30 minutes.
            2. 🛋️ Shift from "I must do everything" to "I will do one 2-minute physical movement".
            3. 🎧 Switch on RAcer "Gentle Rain" or "Stream" audio for non-intrusive auditory grounding.
            
            ✨ 2-Minute Reset Action:
            Drink one cold glass of water, step outside or look out a window for 60 seconds, and do NOT force study right now. When you're ready, start a 5-minute ADHD timer with hidden clock.
            """.trimIndent()

            "hyper" -> """
            🧠 Luma ADHD Mental Health: Hyper State & Racing Thoughts Protocol
            
            ⚡ What is happening in your brain:
            You have a surge of executive energy or dopamine hyperactivity. While exciting, unchanneled hyper states lead to sudden exhaustion or scattered multi-tasking without finishing.
            
            🎯 Channeling Your Hyper-Focus Safely:
            1. 📝 "Brain Dump" Sandbox: Write every racing thought down on paper in 3 minutes so it leaves your short-term RAM.
            2. 🎯 Pick EXACTLY ONE single project or chapter and lock in a 25-minute Pomodoro timer.
            3. 🛑 Turn on App Distraction Blocker in RAcer to keep social rabbit holes locked out.
            
            🛡️ Burnout Prevention:
            When your timer rings, FORCE a physical 5-minute break—drink water and stretch, even if your brain screams to keep going. This prevents the severe 4:00 PM dopamine crash.
            """.trimIndent()

            else -> """
            🧠 Luma ADHD Mental Health & Rhythm Analysis
            
            🌿 Current Cognitive State:
            • Today's Focus Output: $todayFocusMins minutes logged.
            • Consistency Rhythm: $streak day streak.
            • Detected Mood State: $mood.
            
            📊 Hyper vs. Zero Balance Spectrum:
            Your ADHD brain functions like a high-performance engine with an on/off switch rather than a steady dial. High productivity days are often followed by lower-energy days.
            
            💡 Recommended Sustainable Habits:
            1. ⚖️ Embrace "Pacing Over Intensity": 3 gentle 15-minute sessions are far healthier than one exhausting 3-hour marathon.
            2. 🧘 Nervous System Anchors: Use the Wellness routines (Box Breathing / Neck Release) between deep study sessions.
            3. 🌙 Evening Wind-Down: Protect sleep hygiene by setting your bedtime alarm in RAcer Alarms.
            """.trimIndent()
        }
    }

    suspend fun generateDailyAnalysis(): String = withContext(Dispatchers.IO) {
        val stats = repository.calculateStats()
        val todayStr = repository.getTodayDateString()
        val journal = repository.getJournalEntryForDate(todayStr).firstOrNull()
        val activeTasks = repository.allActiveTasks.firstOrNull() ?: emptyList()
        val completedToday = activeTasks.count { it.isCompleted }

        val prompt = """
            Generate a gentle ADHD-friendly Daily Retrospective for today ($todayStr).
            Data:
            - Focus time today: ${stats.todaySeconds / 60} minutes (${stats.completedSessions} completed sessions)
            - Tasks completed today: $completedToday
            - Current focus streak: ${stats.currentStreak} days
            - Journal mood: ${journal?.mood?.label ?: "Not logged"}
            
            Include:
            1. 🌟 Daily Wins & Flow Summary
            2. 🌿 Habit & Task Rhythm
            3. 💡 Tomorrow's 1 Gentle Suggestion
            Keep it inspiring, cozy, and under 150 words.
        """.trimIndent()

        val apiKey = repository.getSetting("ai_api_key", "")
        val result = GeminiApiClient.generateContent(prompt, apiKey.ifBlank { null })
        val text = result.getOrElse {
            """
            🌟 Daily Retrospective ($todayStr)
            • Today's Focus: ${stats.todaySeconds / 60} minutes logged across ${stats.completedSessions} session(s).
            • Tasks Completed: $completedToday actionable item(s).
            • Focus Streak: ${stats.currentStreak} day(s) in rhythm.
            
            🌿 Reflection:
            Every small block of dedicated focus rebuilds your momentum. Whether it was 10 minutes or an hour, you showed up today.
            
            💡 Tomorrow's Suggestion:
            Pick just ONE high-impact task first thing in the morning and start with a gentle 15-minute ADHD timer.
            """.trimIndent()
        }

        repository.insertChatMessage(
            ChatMessageEntity(sender = "LUMA", message = text, category = "daily_analysis")
        )
        text
    }

    suspend fun generateWeeklyAnalysis(): String = withContext(Dispatchers.IO) {
        val stats = repository.calculateStats()
        val prompt = """
            Generate a gentle ADHD Weekly Review.
            Data:
            - Weekly Focus: ${stats.weekSeconds / 60} minutes
            - Average session duration: ${stats.avgSessionLengthSeconds / 60} min
            - Best focus time of day: ${stats.bestHourOfDay}:00
            - Strongest day of week: ${stats.bestDayOfWeek}
            - Completion rate: ${stats.completionRate.toInt()}%
            
            Provide:
            1. 📈 Weekly Momentum & Patterns
            2. 🎯 Peak Energy Hours
            3. 🛡️ Strategy to prevent burnout next week
        """.trimIndent()

        val apiKey = repository.getSetting("ai_api_key", "")
        val result = GeminiApiClient.generateContent(prompt, apiKey.ifBlank { null })
        val text = result.getOrElse {
            """
            📈 Weekly Focus Retrospective
            • Total Focus: ${stats.weekSeconds / 60} minutes this week.
            • Completion Rate: ${stats.completionRate.toInt()}% of planned sessions completed.
            • Peak Energy Window: Around ${stats.bestHourOfDay}:00 on ${stats.bestDayOfWeek}s.
            
            🎯 Pattern Insight:
            Your brain finds flow easiest around ${stats.bestHourOfDay}:00. Scheduling your most demanding creative or technical tasks during this golden window will reduce resistance.
            
            🛡️ Next Week Strategy:
            Guard your energy by interspersing 5-minute break audio sessions. When feeling scattered, switch to ADHD Gentle Mode with hidden timers.
            """.trimIndent()
        }

        repository.insertChatMessage(
            ChatMessageEntity(sender = "LUMA", message = text, category = "weekly_analysis")
        )
        text
    }

    suspend fun generateBehavioralPatternAnalysis(): String = withContext(Dispatchers.IO) {
        val stats = repository.calculateStats()
        val allSessions = repository.allFocusSessions.firstOrNull() ?: emptyList()
        val activeTasks = repository.allActiveTasks.firstOrNull() ?: emptyList()
        val habits = repository.activeHabits.firstOrNull() ?: emptyList()
        val dateStr = repository.getTodayDateString()
        val usage = repository.getUsageForDate(dateStr).firstOrNull() ?: emptyList()

        val completedFocus = allSessions.count { it.isCompleted && it.sessionType == "FOCUS" }
        val brokenFocus = allSessions.count { !it.isCompleted && it.sessionType == "FOCUS" }
        val breakSessions = allSessions.count { it.sessionType.contains("BREAK") }
        val topDistractions = usage.take(3).map { "${it.appName} (${it.totalTimeInForegroundSeconds / 60}m, ${it.launchCount} reopens)" }

        val prompt = """
            You are Luma, an ultra-perceptive AI Cognitive & Behavioral Scientist specializing in ADHD, dopamine regulation, and peak performance.
            Provide a deep, empathetic, and highly specific BEHAVIORAL ANALYSIS based on what you noticed from the user's real tracked activity:

            Tracked User Telemetry:
            - Focus Output: ${stats.todaySeconds / 60} mins today, ${stats.weekSeconds / 60} mins this week.
            - Total Completed Sessions: $completedFocus, Incomplete/Broken Sessions: $brokenFocus
            - Break Sessions Taken: $breakSessions
            - Peak Focus Window: ${stats.bestHourOfDay}:00 (${stats.bestDayOfWeek}s)
            - Current Streak: ${stats.currentStreak} days
            - Tasks Logged: ${activeTasks.size} (Completed: ${activeTasks.count { it.isCompleted }})
            - App Usage & Reopens Today: ${if (topDistractions.isNotEmpty()) topDistractions.joinToString(", ") else "None recorded"}

            Deliver a structured, detailed report:
            1. 🧠 Behavioral Patterns Noticed (Cognitive rhythms, dopamine spikes & fatigue drop-offs)
            2. ⚡ Hyperfocus vs. Zero-Energy Trap Observation (Are they taking breaks or draining their battery in marathon sessions?)
            3. 📱 Digital Friction & Compulsive Reopens (Analysis of phone check habits & distraction loops)
            4. 🎯 Personalized Micro-Adjustments for Tomorrow (3 specific tweaks to maximize effortless flow)
            5. 📊 Luma Cognitive Momentum Rating & Verdict

            Speak directly to the user as an intelligent, warm AI companion who truly understands their neurobiology.
        """.trimIndent()

        val apiKey = repository.getSetting("ai_api_key", "")
        val result = GeminiApiClient.generateContent(prompt, apiKey.ifBlank { null })
        val text = result.getOrElse {
            """
            🧠 Luma Behavioral Pattern & Neuro-Rhythm Audit

            1. 📊 Executive Function & Focus Momentum:
            • Weekly Volume: ${stats.weekSeconds / 60} minutes across ${stats.completedSessions} completed sessions.
            • Session Completion Rate: ${stats.completionRate.toInt()}% ($completedFocus completed vs $brokenFocus interrupted).
            • Peak Cognitive Window: ${stats.bestHourOfDay}:00. Your neural focus naturally peaks during this hour.

            2. ⚡ Hyper vs. Zero Rhythm Observations:
            ${if (breakSessions < (completedFocus / 3).coerceAtLeast(1)) "• ⚠️ Break Skipping Tendency: You frequently power through focus sessions without logging planned recovery breaks. While hyperfocus feels exhilarating, it accelerates dopamine exhaustion and leads to tomorrow's zero-energy paralysis." else "• ✅ Healthy Break Cadence: You consistently balance focus sprints with restorative breaks, which protects your dopamine baseline."}
            • Pacing Consistency: Your ${stats.currentStreak}-day streak demonstrates strong habit formation.

            3. 📱 Compulsive Reopen & Distraction Loop:
            ${if (topDistractions.isNotEmpty()) "• Top Attention Drains: ${topDistractions.joinToString(", ")}. Frequent micro-reopens fragment executive RAM. Consider enabling RAcer's Distraction Blocker during your golden ${stats.bestHourOfDay}:00 window." else "• Clean Digital Canvas: Low distraction interference detected during your study windows."}

            4. 🎯 Actionable Behavioral Adjustments:
            1. 🛡️ Guard Your Golden Hour: Schedule demanding analytical tasks strictly around ${stats.bestHourOfDay}:00.
            2. 🧘 Enforce 3-Minute Physical Resets: Always stand up and look outside when the break chime rings.
            3. ⏱️ Pre-Commit Exact Times: Use exact 37-minute or custom session lengths to fit your natural attention span without artificial strain.

            ✨ Luma Verdict: Your cognitive engine has exceptional peak burst capacity. Pacing and scheduled recovery will make your output effortlessly sustainable.
            """.trimIndent()
        }

        repository.insertChatMessage(
            ChatMessageEntity(sender = "LUMA", message = text, category = "behavioral_analysis")
        )
        text
    }

    suspend fun generateCoachPlan(goal: String): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are Luma ADHD Productivity Coach. The user has a big goal: "$goal".
            Break this large, intimidating goal into:
            1. 3 ultra-small, low-friction micro-steps (under 10 mins each to start without overwhelm)
            2. Recommended focus timer mode (e.g. ADHD 15-min Gentle or Pomodoro 25-min)
            3. Recommended ambient background sound
            Keep it structured, clear, and reassuring.
        """.trimIndent()

        val apiKey = repository.getSetting("ai_api_key", "")
        val result = GeminiApiClient.generateContent(prompt, apiKey.ifBlank { null })
        result.getOrElse {
            """
            🎯 ADHD Goal Breakdown for: "$goal"
            
            Micro-Steps to eliminate friction:
            1. ▫️ Step 1: Open required app/materials and write down just the first 1-sentence outline (5 mins).
            2. ▫️ Step 2: Work on the first component without judging quality (10 mins).
            3. ▫️ Step 3: Organize items and prepare the next small chunk (10 mins).
            
            ⏱️ Recommended Timer: ADHD Gentle Mode (15 mins) with "Hide Remaining Time" enabled to reduce ticking pressure.
            🎵 Recommended Sound: Deep Focus Waves (40Hz) or Gentle Rain.
            """.trimIndent()
        }
    }

    suspend fun analyzeImageStudyNotes(bitmap: Bitmap, promptText: String): String = withContext(Dispatchers.IO) {
        val fullPrompt = """
            Analyze this uploaded image (study material / handwritten notes / schedule / checklist).
            User instruction: $promptText
            
            Tasks:
            1. Extract core key points & summary.
            2. Convert any action items into clean, actionable to-do tasks with estimated durations.
            3. Provide a structured study or focus plan.
        """.trimIndent()

        val apiKey = repository.getSetting("ai_api_key", "")
        val result = GeminiApiClient.generateContent(
            prompt = fullPrompt,
            apiKeyOverride = apiKey.ifBlank { null },
            bitmap = bitmap
        )

        result.getOrElse {
            """
            📑 Visual Note Analysis
            • Extracted Content: Study material and checklist detected.
            • Recommended Action Plan:
              1. Review key highlighted formulas/concepts (15 mins focus session).
              2. Complete practice problems in 25-minute Pomodoro block.
              3. Summarize takeaways in your daily RAcer Journal.
            (Configure your Gemini API key in Settings -> AI for full cloud multimodal neural extraction)
            """.trimIndent()
        }
    }

    private suspend fun buildGroundingContext(): String {
        val sb = StringBuilder()

        val allowFocus = repository.getSetting("ai_allow_focus", "true") == "true"
        val allowTasks = repository.getSetting("ai_allow_tasks", "true") == "true"
        val allowHabits = repository.getSetting("ai_allow_habits", "true") == "true"
        val allowJournal = repository.getSetting("ai_allow_journal", "false") == "true"
        val allowUsage = repository.getSetting("ai_allow_usage", "false") == "true"

        if (allowFocus) {
            val stats = repository.calculateStats()
            sb.append("• Focus Stats: Today=${stats.todaySeconds / 60}m, Week=${stats.weekSeconds / 60}m, Streak=${stats.currentStreak} days, CompletionRate=${stats.completionRate.toInt()}%, BestTime=${stats.bestHourOfDay}:00 on ${stats.bestDayOfWeek}\n")
        }

        if (allowTasks) {
            val tasks = repository.allActiveTasks.firstOrNull() ?: emptyList()
            val pending = tasks.filter { !it.isCompleted }.take(5).map { "${it.title} (${it.priority.displayName})" }
            val completed = tasks.filter { it.isCompleted }.take(5).map { it.title }
            sb.append("• Pending Tasks: ${pending.joinToString(", ")}\n")
            sb.append("• Recently Completed Tasks: ${completed.joinToString(", ")}\n")
        }

        if (allowHabits) {
            val habits = repository.activeHabits.firstOrNull() ?: emptyList()
            sb.append("• Active Habits: ${habits.joinToString(", ") { it.name }}\n")
        }

        // Special dates for exams/deadlines
        val specialDates = repository.allSpecialDates.firstOrNull() ?: emptyList()
        if (specialDates.isNotEmpty()) {
            val datesSummary = specialDates.take(3).map { "${it.dateString}: ${it.title} (${it.note})" }
            sb.append("• Special Upcoming Milestones / Exams: ${datesSummary.joinToString(", ")}\n")
        }

        if (allowJournal) {
            val entries = repository.allJournalEntries.firstOrNull() ?: emptyList()
            val latest = entries.firstOrNull()
            if (latest != null) {
                sb.append("• Recent Journal Reflection: Learned='${latest.learnedText}', Felt='${latest.feltText}', Mood='${latest.mood.label}'\n")
            }
        }

        if (allowUsage) {
            val dateStr = repository.getTodayDateString()
            val usage = repository.getUsageForDate(dateStr).firstOrNull() ?: emptyList()
            if (usage.isNotEmpty()) {
                val top = usage.take(3).map { "${it.appName}: ${it.totalTimeInForegroundSeconds / 60}m" }
                sb.append("• App Usage: ${top.joinToString(", ")}\n")
            }
        }

        return sb.toString()
    }

    private suspend fun generateOfflineResponse(query: String): String {
        // 1. Instant mathematical evaluation for arithmetic and calculations (e.g. 2+2, 2+2=?, 5*10)
        val mathAnswer = evaluateMathQuery(query)
        if (mathAnswer != null) {
            return mathAnswer
        }

        val q = query.trim().lowercase(Locale.getDefault())
        val stats = repository.calculateStats()
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greetingTime = when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Hey night owl"
        }

        // 2. Greetings & Salutations
        if (q in listOf("hi", "hello", "hey", "hola", "yo", "sup", "greetings") || q.startsWith("hi ") || q.startsWith("hello ") || q.startsWith("hey ") || "good morning" in q || "good afternoon" in q || "good evening" in q) {
            return """
                👋 $greetingTime! I'm Luma, right here with you.
                
                • Today's Focus Output: ${stats.todaySeconds / 60} minutes logged
                • Current Streak: ${stats.currentStreak} day(s) 🔥
                • Peak Flow Window: ~${stats.bestHourOfDay}:00
                
                What are we conquering right now? I can help you:
                1. 🎯 Break down a big task or goal into effortless micro-steps
                2. 📚 Generate a full multi-week Study or Exam prep roadmap
                3. 💻 Architect code, explain algorithms, or debug logic
                4. 🧘 Reset cognitive overload or start a 15-minute gentle focus sprint!
            """.trimIndent()
        }

        // 3. Identity & Capabilities
        if ("who are you" in q || "what can you do" in q || "introduce yourself" in q || "help me" in q) {
            return """
                ✨ I am **Luma**, your universal AI intellect and cognitive flow companion in FocusFlow.
                
                Here is what I can do with you:
                • 🧠 **Universal Reasoning**: Ask me about computer science, physics, biology, history, mathematics, philosophy, literature, and essay writing.
                • ⏱️ **ADHD Pacing & Micro-Steps**: Tell me any intimidating goal, and I'll break it down into zero-friction <10 minute actions.
                • 📅 **Roadmap Generator**: Use the buttons above for custom multi-week Study Plans and Exam countdown strategies.
                • 📊 **Grounding Telemetry**: I monitor your focus sessions (${stats.todaySeconds / 60}m today), active streaks (${stats.currentStreak} days), and pending tasks to tailor every recommendation.
                
                Just type whatever is on your mind!
            """.trimIndent()
        }

        // 4. Fun, Humor, & Boredom
        if ("joke" in q || "funny" in q || "make me laugh" in q || "bored" in q) {
            val jokes = listOf(
                "Why do programmers prefer dark mode? Because light attracts bugs! 🐛 But in RAcer, your dark themes also save battery on OLED screens.",
                "Why did the cognitive scientist bring a ladder to work? To reach the high-order executive functions! 🧠 Take a 5-minute stretch break to reset your dopamine baseline.",
                "There are 10 types of people in the world: those who understand binary, and those who don't. 😉 Ready for a quick 15-minute sprint?",
                "Procrastination is like a rocking chair: it gives you something to do, but it doesn't get you anywhere. Let's start with just 2 minutes of focus right now!"
            )
            val selectedJoke = jokes[(query.hashCode().let { if (it < 0) -it else it }) % jokes.size]
            return """
                😄 Here's a thought for you:
                
                $selectedJoke
                
                💡 Tip: When boredom strikes during work, your dopamine receptors are seeking stimulation. Try switching your ambient sound to "Deep Focus Waves" or "Binaural 40Hz" in the Focus tab!
            """.trimIndent()
        }

        // 5. Motivation & Overcoming Procrastination
        if ("motivate" in q || "inspiration" in q || "tired" in q || "give up" in q || "lazy" in q || "procrastinat" in q || "start" in q && "can't" in q) {
            return """
                🔥 **Luma Momentum Protocol:**
                
                1. 🎯 **The 2-Minute Gateway**: You don't have to finish the entire project right now. You only need to open the tool and do 120 seconds of work. 85% of friction vanishes once the action starts.
                2. 🧠 **Dopamine Follows Action, Not Waiting**: Motivation is an emotional byproduct of physical movement, not a prerequisite.
                3. 🛡️ **Lower the Standards to Start**: Write a sloppy first draft. Write buggy pseudocode. You can always refine what exists, but you cannot edit a blank page.
                
                You already have a **${stats.currentStreak}-day streak** built up. Let's protect it with one gentle 15-minute ADHD timer block right now!
            """.trimIndent()
        }

        // 6. Overwhelm, Anxiety & Burnout
        if ("anxious" in q || "stress" in q || "overwhelm" in q || "burnout" in q || "panic" in q || "freaking out" in q || "struggling" in q) {
            return """
                🌿 **Breathe. Let's lower the pressure together.**
                
                When executive cognitive RAM is saturated, the brain triggers a fight-or-flight freeze. Here is your emergency protocol:
                
                1. 🌬️ **4-7-8 Physiological Sigh**: Inhale deeply through your nose for 4 seconds, hold for 7 seconds, then exhale slowly with a sigh for 8 seconds. This forces your vagus nerve to slow your heart rate.
                2. 📝 **Externalize Everything**: Your working memory can only hold 4 items. Write every single thought onto a blank scratchpad without organizing it.
                3. 🛑 **Declare "Minimum Viable Day"**: Pick just ONE single micro-task for today. Everything else can wait.
                
                Head over to the **Wellness & Movement** section in the Explore Hub whenever you need guided sensory grounding!
            """.trimIndent()
        }

        // 7. Coding & Engineering
        if ("code" in q || "kotlin" in q || "python" in q || "bug" in q || "function" in q || "programming" in q || "algorithm" in q || "software" in q || "git" in q) {
            return """
                💻 **Luma Universal Engineering Framework:**
                
                • **Clean Architecture & Separation of Concerns**: Keep Data, Domain, and Presentation layers strictly decoupled.
                • **Kotlin State Management**: In modern Compose, expose immutable `StateFlow` from your ViewModel, and collect via `collectAsState()`. Never mutate state directly in Composables.
                • **Async Operations**: Confine network/disk calls to `Dispatchers.IO`, and update UI states on `Dispatchers.Main`. Wrap unpredictable external calls in `runCatching` or sealed `Result` classes.
                • **Debugging Heuristic**: When facing an unexpected bug, reproduce the minimal failing test case. Inspect inputs, boundary conditions (null/empty/0), and thread concurrency.
                
                💡 You can configure your Gemini API key in Settings -> AI to enable live real-time code generation and automated debugging!
            """.trimIndent()
        }

        // 8. Bioscience, Neuroscience & STEM Science Skills
        if ("bio" in q || "biology" in q || "neuro" in q || "dopamine" in q || "brain" in q || "cell" in q || "genetic" in q || "dna" in q || "science" in q || "ultradian" in q || "circadian" in q || "synap" in q) {
            return """
                🧬 **Luma Bioscience & Neuroscience Intelligence:**
                
                • **Dopamine & Prefrontal Executive Function**:
                  Dopamine tonic levels dictate sustained motivation, while phasic bursts signal reward prediction errors. When working on difficult tasks, dopamine drops quickly. Intermittent random rewards (like checking off micro-steps) keep dopamine receptors sensitive and prevent cognitive fatigue.
                
                • **Ultradian 90-Minute Focus Cycles (BRAC)**:
                  Human biology operates on 90–120 minute Basic Rest-Activity Cycles. The brain transitions from high-frequency beta/gamma waves to slower alpha/theta rhythms. Forcing focus past 90 minutes without a 15–20 minute restorative downtime causes adenosine accumulation and exponential error rates.
                
                • **Cellular Bioenergetics & Neural ATP**:
                  The human brain represents ~2% of body mass but consumes ~20% of resting glucose and ATP. Astrocytes supply lactate to neurons during intense deep work. Hydration and stable blood glucose directly prevent the prefrontal cortex "executive crash".
                
                • **Synaptic Plasticity & Spaced Consolidation**:
                  Long-term potentiation (LTP) requires protein synthesis occurring primarily during slow-wave non-REM and REM sleep. Learning today is physically wired into dendritic spines during tonight's rest. Never sacrifice sleep for study volume.
                
                💡 Need a bioscience study roadmap or exam prep breakdown? Use the "Study Plan" or "Exam Prep" capsules above!
            """.trimIndent()
        }

        // 9. Physics, Chemistry & Mathematics
        if ("math" in q || "physics" in q || "formula" in q || "calculate" in q || "chemistry" in q || "equation" in q) {
            return """
                🔬 **Luma Analytical Science & Mathematical Reasoning:**
                
                • **First Principles Thinking**: Deconstruct the problem into fundamental, undeniable axioms before building upwards.
                • **Dimensional Verification**: Always check physical dimensions and units (e.g., $query). Consistency across both sides of the equation is mandatory.
                • **Active Derivation**: Instead of passive memorization of final formulas, practice re-deriving core equations from basic conservation laws (energy, momentum, mass).
                
                💡 If you want to solve an equation instantly, you can type arithmetic directly (e.g. `128 * 45` or `15% of 640` or `sqrt(144)`) and I'll calculate it on the fly!
            """.trimIndent()
        }

        // 9. Tasks & Prioritization
        if ("work on" in q || "task" in q || "todo" in q || "what should i" in q || "priority" in q) {
            val tasks = repository.allActiveTasks.firstOrNull() ?: emptyList()
            val pending = tasks.filter { !it.isCompleted }
            return if (pending.isNotEmpty()) {
                val top = pending.maxByOrNull { it.priority.level } ?: pending.first()
                val otherCount = (pending.size - 1).coerceAtLeast(0)
                """
                🎯 **Next Highest-Impact Focus Target:**
                
                • **Primary Task**: "${top.title}"
                • **Priority Level**: ${top.priority.displayName}
                • **Remaining Backlog**: $otherCount other pending task(s)
                
                ⚡ Recommendation: Launch a 25-minute Pomodoro or 15-minute Gentle block. Put your phone in Do Not Disturb and knock this out first!
                """.trimIndent()
            } else {
                "🎉 Your task backlog is currently 100% clear! Take a restorative break or log a new creative goal in the Tasks tab."
            }
        }

        // 10. Weekly Planning & Progression
        if ("weekly" in q || "week" in q || "objective" in q) {
            val plans = repository.allWeeklyPlans.firstOrNull() ?: emptyList()
            val done = plans.count { it.isCompleted }
            val total = plans.size
            val rate = if (total > 0) (done.toFloat() / total * 100f).toInt() else 0
            val status = if (rate >= 70) "🟢 On Track (>= 70%)" else "🟡 In Progress (< 70%)"
            return """
                📅 **Weekly Objectives Status:**
                
                • Progress: $done of $total goals completed ($rate%) - $status
                • Focus Volume This Week: ${stats.weekSeconds / 60} minutes across ${stats.completedSessions} sessions
                • Focus Streak: ${stats.currentStreak} day(s)
                
                Consistency beats intensity every single time. Keep chipping away!
            """.trimIndent()
        }

        // 11. Habit Formation
        if ("habit" in q || "streak" in q || "routine" in q) {
            val habits = repository.activeHabits.firstOrNull() ?: emptyList()
            val habitNames = habits.take(3).map { it.name }
            return """
                🔄 **Habit Engine Telemetry:**
                
                • Current Active Streak: ${stats.currentStreak} days
                • Active Tracked Habits: ${if (habitNames.isNotEmpty()) habitNames.joinToString(", ") else "None configured yet"}
                
                💡 The Golden Rule of Habits: Never miss twice. If life interrupts your routine today, do a 2-minute micro-version to keep the identity alive!
            """.trimIndent()
        }

        // 12. Explanations / "What is..." / "How to..."
        val cleanedTopic = query.replace("what is", "", ignoreCase = true)
            .replace("explain", "", ignoreCase = true)
            .replace("tell me about", "", ignoreCase = true)
            .replace("how does", "", ignoreCase = true)
            .replace("how to", "", ignoreCase = true)
            .trim()

        return """
            💡 **Luma Perspective on: "${query.take(50)}"**
            
            1. 🔍 **Core Concept & Overview**:
            • At its foundation, ${if (cleanedTopic.isNotBlank()) cleanedTopic else "this topic"} represents an interconnected system governed by clear, predictable patterns.
            • The primary lever is isolating the highest-leverage variable and focusing attention there first.
            
            2. ⚡ **Practical Action Plan**:
            • **Step 1**: Define your exact objective in one clear sentence.
            • **Step 2**: Remove secondary distractions and launch a focused 25-minute work block.
            • **Step 3**: Review results and capture reflections in your Daily Journal.
            
            3. 📊 **Your Current Momentum**:
            • You have ${stats.todaySeconds / 60}m of focused work logged today and a ${stats.currentStreak}-day streak active.
            
            ✨ Need a deeper, multimodal AI generation? Add your Gemini key in Settings for full unconstrained cloud synthesis!
        """.trimIndent()
    }

    private fun generateOfflineStudyPlan(
        subject: String,
        hoursPerDay: Float,
        weeks: Int,
        focusTopics: String,
        studyStyle: String
    ): String {
        val totalSessionsPerDay = ((hoursPerDay * 60) / 30).toInt().coerceAtLeast(1)
        return """
        📚 $weeks-Week Master Study Plan: $subject
        
        🎯 Structure & Cadence:
        • Daily Commitment: $hoursPerDay hours ($totalSessionsPerDay focus cycles per day)
        • Target Focus Areas: ${focusTopics.ifBlank { "Full Course Syllabus & Core Mechanics" }}
        • Study Pacing: $studyStyle
        
        📅 Phase Breakdown:
        • Week 1-${(weeks / 2).coerceAtLeast(1)}: Foundation & Deep Concept Mastery
          ▫️ Build core conceptual outlines and formula cheat-sheets.
          ▫️ End each day with 10 mins of Active Recall without looking at notes.
        • Week ${((weeks / 2) + 1).coerceAtMost(weeks)}-$weeks: High-Yield Practice & Timed Testing
          ▫️ Solve end-of-chapter problems and past mock exams.
          ▫️ Maintain an "Error Log" to review mistakes before each study block.
        
        ⏱️ Daily Study Session Blueprint:
        1. [25 min] Deep Work Block 1: Tough theory / derivations (Sound: Binaural 40Hz)
        2. [5 min] Screen-free Stretch & Hydration break
        3. [25 min] Deep Work Block 2: Practice problems & exercises
        4. [10 min] Self-testing Feynman Summary: Explain concept simply in own words
        
        🌿 ADHD Anti-Overwhelm Shield:
        On days when energy is low, do NOT abandon the day—simply complete 1 gentle 15-minute review block to preserve your streak!
        """.trimIndent()
    }

    private fun generateOfflineExamPlan(
        examName: String,
        examDateStr: String,
        daysLeft: Int,
        highYieldTopics: String,
        targetScore: String
    ): String {
        val sprintDays = (daysLeft / 3).coerceAtLeast(1)
        return """
        🎯 Exam Preparation Roadmap: $examName
        📅 Target Date: $examDateStr ($daysLeft Days Remaining)
        🏆 Target Goal: $targetScore
        
        ⏳ The $daysLeft-Day Countdown Matrix:
        • Phase 1 (Days 1 to $sprintDays) - High-Yield Syllabus Triage:
          ▫️ Cover major topics: ${highYieldTopics.ifBlank { "Core chapters & high-weightage definitions" }}
          ▫️ Create flashcards & 1-page condensed summary sheets.
        
        • Phase 2 (Days ${sprintDays + 1} to ${daysLeft - 2}) - Active Testing & Past Papers:
          ▫️ Complete 2-3 full timed mock tests under realistic exam conditions.
          ▫️ Target weak areas identified in your Error Log.
        
        • Phase 3 (Final 48 Hours) - Consolidation & Confidence:
          ▫️ Quick review of summary sheets and formulas.
          ▫️ Strict cutoff: Stop studying by 8:00 PM the night before for deep restorative sleep.
        
        📝 Recommended Exam Day Routine:
        • 🥞 High-protein breakfast + hydration.
        • 🧘 5-minute Box Breathing to regulate pre-test autonomic heart rate.
        • ⏱️ In Exam: First 5 mins scan paper, tackle easy questions first for momentum!
        """.trimIndent()
    }

    private fun evaluateMathQuery(raw: String): String? {
        val trimmed = raw.trim()
        val cleaned = trimmed
            .replace("?", "")
            .replace("=", "")
            .replace("what is", "", ignoreCase = true)
            .replace("calculate", "", ignoreCase = true)
            .replace("solve", "", ignoreCase = true)
            .trim()

        // 1. Basic binary operations: A [+-*/^%x] B
        val pattern = Regex("""^([+-]?\d+(?:\.\d+)?)\s*([\+\-\*\/\^%xX×÷])\s*([+-]?\d+(?:\.\d+)?)$""")
        val match = pattern.find(cleaned)
        if (match != null) {
            val a = match.groupValues[1].toDoubleOrNull() ?: return null
            val op = match.groupValues[2]
            val b = match.groupValues[3].toDoubleOrNull() ?: return null

            val result = when (op) {
                "+" -> a + b
                "-" -> a - b
                "*", "x", "X", "×" -> a * b
                "/", "÷" -> if (b != 0.0) a / b else Double.NaN
                "%" -> a % b
                "^" -> Math.pow(a, b)
                else -> return null
            }

            fun formatNum(n: Double): String {
                return if (n.isNaN()) "Undefined (division by zero)"
                else if (n % 1.0 == 0.0) n.toLong().toString()
                else String.format(Locale.US, "%.4f", n).trimEnd('0').trimEnd('.')
            }

            val opSymbol = when (op) {
                "x", "X", "×" -> "×"
                "÷" -> "÷"
                else -> op
            }

            val opName = when (op) {
                "+" -> "Addition"
                "-" -> "Subtraction"
                "*", "x", "X", "×" -> "Multiplication"
                "/", "÷" -> "Division"
                "%" -> "Modulo"
                "^" -> "Exponentiation"
                else -> "Calculation"
            }

            return """
                🔢 **Mathematical Result:**
                
                **${formatNum(a)} $opSymbol ${formatNum(b)} = ${formatNum(result)}**
                
                *Step-by-step reasoning:*
                • First term: ${formatNum(a)}
                • Operation: $opSymbol ($opName)
                • Second term: ${formatNum(b)}
                • Final evaluated value: **${formatNum(result)}**
            """.trimIndent()
        }

        // 2. Percentage questions (e.g. "15% of 200")
        val pctPattern = Regex("""^(\d+(?:\.\d+)?)\s*%\s*(?:of)?\s*(\d+(?:\.\d+)?)$""", RegexOption.IGNORE_CASE)
        val pctMatch = pctPattern.find(cleaned)
        if (pctMatch != null) {
            val p = pctMatch.groupValues[1].toDoubleOrNull() ?: return null
            val total = pctMatch.groupValues[2].toDoubleOrNull() ?: return null
            val res = (p / 100.0) * total
            val formattedRes = if (res % 1.0 == 0.0) res.toLong().toString() else String.format(Locale.US, "%.4f", res).trimEnd('0').trimEnd('.')
            return "🔢 **Result:** $p% of $total = **$formattedRes**"
        }

        // 3. Square root (e.g. "sqrt(144)" or "sqrt 144")
        val sqrtPattern = Regex("""^sqrt\(?(\d+(?:\.\d+)?)\)?$""", RegexOption.IGNORE_CASE)
        val sqrtMatch = sqrtPattern.find(cleaned)
        if (sqrtMatch != null) {
            val num = sqrtMatch.groupValues[1].toDoubleOrNull() ?: return null
            val res = Math.sqrt(num)
            val formattedRes = if (res % 1.0 == 0.0) res.toLong().toString() else String.format(Locale.US, "%.4f", res).trimEnd('0').trimEnd('.')
            return "🔢 **Result:** √$num = **$formattedRes**"
        }

        return null
    }
}
