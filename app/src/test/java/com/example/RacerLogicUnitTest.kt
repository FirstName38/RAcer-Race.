package com.example

import com.example.data.model.FocusMode
import com.example.data.model.FocusSound
import com.example.data.model.FocusWallpaper
import com.example.data.model.JournalMood
import com.example.data.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RacerLogicUnitTest {

    @Test
    fun testFocusModesDefaults() {
        assertEquals("Pomodoro", FocusMode.POMODORO.displayName)
        assertEquals("ADHD Gentle", FocusMode.ADHD.displayName)
        assertEquals("Custom", FocusMode.CUSTOM.displayName)
        assertEquals("Stopwatch", FocusMode.STOPWATCH.displayName)
    }

    @Test
    fun testFocusSounds() {
        val sounds = FocusSound.values()
        assertTrue(sounds.contains(FocusSound.RAIN))
        assertTrue(sounds.contains(FocusSound.WHITE_NOISE))
        assertTrue(sounds.contains(FocusSound.DEEP_FOCUS))
        assertTrue(sounds.contains(FocusSound.LO_FI))
        assertTrue(sounds.contains(FocusSound.FOREST))
        assertTrue(sounds.contains(FocusSound.COZY_FIRE))
    }

    @Test
    fun testFocusWallpapers() {
        val wallpapers = FocusWallpaper.values()
        assertTrue(wallpapers.any { it.id == "dark_minimal" })
        assertTrue(wallpapers.any { it.id == "aurora" })
        assertTrue(wallpapers.any { it.id == "neon_space" })
    }

    @Test
    fun testJournalMoods() {
        assertEquals("✨", JournalMood.GREAT.emoji)
        assertEquals("🌿", JournalMood.GOOD.emoji)
        assertEquals("☁️", JournalMood.NEUTRAL.emoji)
        assertEquals("🌙", JournalMood.TIRED.emoji)
        assertEquals("🌊", JournalMood.OVERWHELMED.emoji)
    }

    @Test
    fun testTaskPriorityLevels() {
        assertEquals("Urgent", TaskPriority.URGENT.displayName)
        assertEquals(4, TaskPriority.URGENT.level)
        assertEquals("High", TaskPriority.HIGH.displayName)
        assertEquals(3, TaskPriority.HIGH.level)
        assertEquals("Medium", TaskPriority.MEDIUM.displayName)
        assertEquals(2, TaskPriority.MEDIUM.level)
        assertEquals("Low", TaskPriority.LOW.displayName)
        assertEquals(1, TaskPriority.LOW.level)
    }
}
