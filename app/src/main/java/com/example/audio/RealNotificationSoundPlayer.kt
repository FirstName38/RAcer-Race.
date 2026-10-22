package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class NotificationSoundType(val id: String, val displayName: String, val description: String) {
    ZEN_BELL("zen_bell", "Zen Bell", "Single meditative singing bowl resonance with long smooth decay"),
    WATER_DROP("water_drop", "Water Drop", "Clean acoustic resonant drop ping"),
    CRYSTAL_SPARKLE("crystal_sparkle", "Crystal Sparkle", "Shimmering three-note crystalline chime"),
    BAMBOO_TAP("bamboo_tap", "Bamboo Tap", "Crisp hollow woodblock acoustic percussion"),
    COSMIC_BLIP("cosmic_blip", "Cosmic Blip", "Subtle futuristic high-tech notification chirp"),
    HARP_GLISSANDO("harp_glissando", "Harp Glissando", "Delicate ascending plucked strings"),
    GENTLE_POP("gentle_pop", "Gentle Pop", "Playful soft bubble pop"),
    SYSTEM_NOTIFICATION("system_notification", "System Default", "Phone's default notification tone")
}

object RealNotificationSoundPlayer {
    private const val TAG = "RealNotificationSoundPlayer"
    private const val SAMPLE_RATE = 44100
    private val scope = CoroutineScope(Dispatchers.Default)
    private var currentJob: Job? = null

    fun playNotification(
        context: Context,
        soundName: String = "zen_bell",
        withVibrate: Boolean = true
    ) {
        val type = NotificationSoundType.entries.find { it.id.equals(soundName, ignoreCase = true) }
            ?: NotificationSoundType.ZEN_BELL

        if (withVibrate) {
            triggerShortVibration(context)
        }

        if (type == NotificationSoundType.SYSTEM_NOTIFICATION) {
            playSystemNotification(context)
            return
        }

        currentJob?.cancel()
        currentJob = scope.launch {
            try {
                val samples = generateNotificationSamples(type)
                val bufferSize = samples.size * 2

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.setVolume(1.0f)
                track.play()
            } catch (e: Exception) {
                Log.e(TAG, "Error playing notification audio", e)
            }
        }
    }

    private fun playSystemNotification(context: Context) {
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, notificationUri)
            ringtone?.play()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play system notification", e)
        }
    }

    private fun triggerShortVibration(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(120)
            }
        } catch (_: Exception) {}
    }

    private fun generateNotificationSamples(type: NotificationSoundType): ShortArray {
        val durationSeconds = when (type) {
            NotificationSoundType.WATER_DROP -> 0.45
            NotificationSoundType.BAMBOO_TAP -> 0.35
            NotificationSoundType.GENTLE_POP -> 0.28
            NotificationSoundType.COSMIC_BLIP -> 0.40
            NotificationSoundType.CRYSTAL_SPARKLE -> 0.85
            NotificationSoundType.HARP_GLISSANDO -> 1.10
            NotificationSoundType.ZEN_BELL -> 1.40
            NotificationSoundType.SYSTEM_NOTIFICATION -> 0.50
        }

        val totalSamples = (SAMPLE_RATE * durationSeconds).toInt()
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val sampleVal = when (type) {
                NotificationSoundType.WATER_DROP -> {
                    // Quick pitch bend downwards (1400Hz -> 900Hz) with fast exponential decay
                    val progress = (t / durationSeconds).coerceIn(0.0, 1.0)
                    val freq = 1400.0 - progress * 500.0
                    val env = exp(-t * 12.0)
                    sin(2.0 * PI * freq * t) * env
                }
                NotificationSoundType.ZEN_BELL -> {
                    // Singing bowl with fundamental (587.33Hz D5) + shimmer harmonic (1174Hz) + slow decay
                    val env = exp(-t * 2.8)
                    val fundamental = sin(2.0 * PI * 587.33 * t)
                    val overtone = sin(2.0 * PI * 1174.66 * t) * 0.35
                    (fundamental + overtone) * env * 0.85
                }
                NotificationSoundType.CRYSTAL_SPARKLE -> {
                    // Three quick staggered chimes: E6 (1318Hz), G#6 (1661Hz), B6 (1975Hz)
                    var sum = 0.0
                    if (t >= 0.0) sum += sin(2.0 * PI * 1318.5 * (t - 0.0)) * exp(-(t - 0.0) * 8.0)
                    if (t >= 0.12) sum += sin(2.0 * PI * 1661.2 * (t - 0.12)) * exp(-(t - 0.12) * 8.0) * 0.9
                    if (t >= 0.24) sum += sin(2.0 * PI * 1975.5 * (t - 0.24)) * exp(-(t - 0.24) * 8.0) * 0.8
                    sum * 0.65
                }
                NotificationSoundType.BAMBOO_TAP -> {
                    // High-attack resonant wood strike (780Hz) + sharp initial transient
                    val env = exp(-t * 25.0)
                    val body = sin(2.0 * PI * 780.0 * t) + sin(2.0 * PI * 1120.0 * t) * 0.4
                    body * env * 0.9
                }
                NotificationSoundType.COSMIC_BLIP -> {
                    // Modern subtle tech chirp: 880Hz ascending quickly to 1760Hz
                    val freq = 880.0 + (t / durationSeconds) * 880.0
                    val env = sin((t / durationSeconds) * PI) // Hanning envelope
                    sin(2.0 * PI * freq * t) * env * 0.75
                }
                NotificationSoundType.HARP_GLISSANDO -> {
                    // Rapid 4-note ascending harp arpeggio: C5 (523), E5 (659), G5 (784), C6 (1046)
                    val notes = listOf(523.25, 659.25, 783.99, 1046.50)
                    val noteDuration = 0.08
                    var current = 0.0
                    for ((idx, f) in notes.withIndex()) {
                        val start = idx * noteDuration
                        if (t >= start) {
                            val dt = t - start
                            current += sin(2.0 * PI * f * dt) * exp(-dt * 4.5) * 0.5
                        }
                    }
                    current * 0.75
                }
                NotificationSoundType.GENTLE_POP -> {
                    // Short soft bubble pop: 450Hz upward to 750Hz in 0.05s
                    val env = exp(-t * 18.0)
                    val freq = 450.0 + (t / durationSeconds) * 300.0
                    sin(2.0 * PI * freq * t) * env * 0.85
                }
                NotificationSoundType.SYSTEM_NOTIFICATION -> 0.0
            }

            buffer[i] = (sampleVal * 30000.0).toInt().coerceIn(-32768, 32767).toShort()
        }

        return buffer
    }
}
