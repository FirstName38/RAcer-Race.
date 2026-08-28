package com.example.ai

import android.graphics.Bitmap
import com.example.data.entity.ChatMessageEntity
import com.example.data.repository.RacerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LumaAIService(private val repository: RacerRepository) {

    suspend fun askLuma(
        userMessage: String,
        imageBitmap: Bitmap? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = repository.getSetting("ai_api_key", "")
        val endpoint = repository.getSetting("ai_endpoint", "")

        // Gather allowed context based on privacy toggles
        val contextPrompt = buildGroundingContext()

        val fullPrompt = buildString {
            append("You are Luma, an empathetic, highly intelligent, ADHD-friendly study coach and productivity companion in the RAcer app.\n")
            append("Your specializations: Personalized Study Plans, Exam Preparation Strategies, Spaced Repetition, Active Recall, Syllabus Deconstruction, Focus Rhythm Optimization, and Cognitive Fatigue Management.\n")
            append("Style: Warm, encouraging, structured, practical, formatted with clean bullet points and clear emojis. Zero guilt, low cognitive friction.\n\n")
            append("When discussing study plans or exam prep:\n")
            append("- Deconstruct large subjects into realistic, high-yield chunks.\n")
            append("- Recommend specific Focus Timer intervals (e.g. 25m Pomodoro or 15m ADHD gentle blocks with 5m active breaks).\n")
            append("- Emphasize active recall, past-paper practice, and spaced revision over passive reading.\n")
            append("- Suggest practical rest, hydration, and sleep hygiene before exams.\n\n")
            append("--- USER'S PERMITTED DATA CONTEXT ---\n")
            append(contextPrompt)
            append("\n--- USER QUERY ---\n")
            append(userMessage)
        }

        val result = GeminiApiClient.generateContent(
            prompt = fullPrompt,
            apiKeyOverride = apiKey.ifBlank { null },
            endpointOverride = endpoint.ifBlank { null },
            bitmap = imageBitmap
        )

        val responseText = result.getOrElse { error ->
            // High quality offline fallback with rich academic planning algorithms
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
        val q = query.lowercase(Locale.getDefault())
        val stats = repository.calculateStats()

        return when {
            "study" in q || "exam" in q || "revision" in q || "test" in q || "syllabus" in q -> {
                """
                📚 Luma's Study & Exam Coach Strategy:
                
                1. 🎯 Break down the material: Group topics into "Must-Master" (60% weightage), "High-Yield" (30%), and "Edge Cases" (10%).
                2. ⏱️ 25/5 Pomodoro Cycle: 25 minutes of active problem solving + 5 minutes screen-free eye rest.
                3. 🧠 Active Recall > Re-reading: Close the notes and write down key concepts from memory.
                4. 💡 Need a tailored plan? Use the "Study Plan" or "Exam Plan" button above for a complete countdown roadmap!
                """.trimIndent()
            }
            "productive" in q || "week" in q || "progress" in q -> {
                "You have logged ${stats.weekSeconds / 60} minutes of deep focus this week with a ${stats.completionRate.toInt()}% completion rate. Your current focus streak is ${stats.currentStreak} day(s). You focus best around ${stats.bestHourOfDay}:00."
            }
            "work on" in q || "task" in q || "today" in q || "plan" in q -> {
                val tasks = repository.allActiveTasks.firstOrNull() ?: emptyList()
                val pending = tasks.filter { !it.isCompleted }
                if (pending.isNotEmpty()) {
                    val top = pending.maxByOrNull { it.priority.level } ?: pending.first()
                    "I recommend focusing on: '${top.title}'. It is marked as ${top.priority.displayName} priority. Let's start with a gentle 15-minute ADHD focus block."
                } else {
                    "Your task list is clear! Enjoy the calm or create a new small milestone for today."
                }
            }
            "struggling" in q || "focus" in q || "distract" in q || "adhd" in q -> {
                "When focus feels hard, the key is lowering the barrier to entry. Switch to ADHD Gentle Mode in the Focus tab, turn on 'Hide Timer', pick 'Deep Focus Waves' or 'Rain', and commit to just 5 minutes. You don't have to finish everything—just start."
            }
            "habit" in q || "streak" in q -> {
                "Your focus streak is currently ${stats.currentStreak} days. Remember that consistency is about returning gently when interrupted, not perfection."
            }
            else -> {
                "I'm here with you in your study & focus flow! You have ${stats.todaySeconds / 60} minutes of focus logged today. Ask me to formulate a study plan, build an exam revision countdown, or deconstruct a difficult topic."
            }
        }
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
}
