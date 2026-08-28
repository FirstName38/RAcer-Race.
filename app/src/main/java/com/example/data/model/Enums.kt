package com.example.data.model

enum class FocusMode(val displayName: String) {
    POMODORO("Pomodoro"),
    ADHD("ADHD Gentle"),
    CUSTOM("Custom"),
    STOPWATCH("Stopwatch")
}

enum class TaskPriority(val displayName: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    URGENT("Urgent", 4)
}

enum class HabitFrequency(val displayName: String) {
    DAILY("Every Day"),
    WEEKDAYS("Weekdays Only"),
    WEEKLY_TARGET("Weekly Target"),
    MONTHLY_TARGET("Monthly Target")
}

enum class JournalMood(val emoji: String, val label: String) {
    GREAT("✨", "Great"),
    GOOD("🌿", "Good"),
    NEUTRAL("☁️", "Neutral"),
    TIRED("🌙", "Tired"),
    OVERWHELMED("🌊", "Overwhelmed")
}

enum class AIProvider(val displayName: String) {
    GEMINI("Gemini API (Google AI)"),
    CUSTOM_OPENAI("Custom OpenAI Compatible Endpoint")
}

enum class FocusSound(val id: String, val displayName: String, val description: String) {
    NONE("none", "Silence", "No background sound"),
    RAIN("rain", "Gentle Rain", "Soothing rhythmic rainfall on glass"),
    FOREST("forest", "Mystic Forest", "Wind through trees with birdsong"),
    WHITE_NOISE("white_noise", "Pure White Noise", "Full spectrum calming mask"),
    DEEP_FOCUS("deep_focus", "Deep Focus Waves", "40Hz binaural isochronic tone"),
    LO_FI("lo_fi", "Lo-Fi Dream", "Slow nostalgic Rhodes electric chords"),
    CHIME("chime", "Tibetan Chimes", "Harmonic ringing resonance"),
    COZY_FIRE("cozy_fire", "Cozy Campfire", "Warm crackling embers & low hum"),
    STREAM("stream", "Mountain Stream", "Flowing fresh water oscillations")
}

enum class FocusWallpaper(val id: String, val displayName: String) {
    DARK_MINIMAL("dark_minimal", "Deep Charcoal Minimal"),
    AURORA("aurora", "Violet Aurora"),
    NEON_SPACE("neon_space", "Cosmic Starlight"),
    RAIN_WINDOW("rain_window", "Rainy Window"),
    COZY_ROOM("cozy_room", "Cozy Midnight Study"),
    FOREST_MIST("forest_mist", "Forest Twilight")
}
