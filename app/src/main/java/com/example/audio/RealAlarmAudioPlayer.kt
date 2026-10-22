package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

data class AlarmRingingState(
    val isRinging: Boolean = false,
    val alarmId: Long = 0L,
    val label: String = "Alarm",
    val hour: Int = 0,
    val minute: Int = 0,
    val soundName: String = "DIGITAL_BEEP"
)

enum class AlarmSoundType(val displayName: String, val description: String) {
    DIGITAL_BEEP("Digital Beep", "Loud rhythmic dual-tone electronic alarm clock beep"),
    RADAR_PULSE("Radar Pulse", "Ascending frequency pulse with high audibility"),
    CLASSIC_BELL("Twin Bell", "Traditional mechanical clock ringing resonance"),
    GENTLE_CHIME("Gentle Chime", "Soothing harmonic chords for stress-free waking"),
    SUNRISE_MELODY("Sunrise Melody", "Uplifting major chord ascending waking melody"),
    ARCADE_ALERT("Arcade Alert", "Energetic 8-bit rhythmic retro alarm cadence"),
    CYBER_SYNTH("Cyber Synth", "Pulsing futuristic electronic focus alarm"),
    HEAVY_PULSE("Heavy Pulse", "Deep bass rhythmic low-frequency alert beats"),
    ROOSTER_MORNING("Morning Roost", "Harmonic acoustic rooster wake-up cadence"),
    SYSTEM_DEFAULT("System Ringtone", "Your phone's default system alarm sound")
}

object RealAlarmAudioPlayer {
    private const val TAG = "RealAlarmAudioPlayer"
    private const val SAMPLE_RATE = 44100
    private const val BUFFER_SIZE = 4096

    @Volatile
    private var audioTrack: AudioTrack? = null

    @Volatile
    private var systemRingtone: Ringtone? = null

    @Volatile
    private var isPlaying: Boolean = false

    private var soundJob: Job? = null
    private var vibratorJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _ringingState = MutableStateFlow(AlarmRingingState())
    val ringingState: StateFlow<AlarmRingingState> = _ringingState.asStateFlow()

    fun isAlarmActive(): Boolean = isPlaying

    /**
     * Start playing real alarm sound on STREAM_ALARM with vibration.
     */
    fun startAlarm(
        context: Context,
        alarmId: Long = 0L,
        label: String = "Alarm",
        hour: Int = 0,
        minute: Int = 0,
        soundName: String = "DIGITAL_BEEP"
    ) {
        stopAlarm(context)
        isPlaying = true

        _ringingState.value = AlarmRingingState(
            isRinging = true,
            alarmId = alarmId,
            label = label,
            hour = hour,
            minute = minute,
            soundName = soundName
        )

        val soundType = try {
            AlarmSoundType.valueOf(soundName)
        } catch (_: Exception) {
            AlarmSoundType.DIGITAL_BEEP
        }

        if (soundType == AlarmSoundType.SYSTEM_DEFAULT) {
            playSystemRingtone(context)
        } else {
            playSynthesizedAlarm(soundType)
        }

        startVibration(context)
    }

    /**
     * Preview sound for a limited duration (e.g. 3 seconds).
     */
    fun previewSound(context: Context, soundType: AlarmSoundType, durationMillis: Long = 3000L) {
        stopAlarm(context)
        isPlaying = true

        if (soundType == AlarmSoundType.SYSTEM_DEFAULT) {
            playSystemRingtone(context)
        } else {
            playSynthesizedAlarm(soundType)
        }

        scope.launch {
            delay(durationMillis)
            if (isPlaying && !_ringingState.value.isRinging) {
                stopAlarm(context)
            }
        }
    }

    /**
     * Immediately stop all alarm sounds, audio tracks, ringtones, and vibration.
     */
    fun stopAlarm(context: Context? = null) {
        isPlaying = false
        _ringingState.value = AlarmRingingState(isRinging = false)

        soundJob?.cancel()
        soundJob = null

        vibratorJob?.cancel()
        vibratorJob = null

        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing AudioTrack", e)
        } finally {
            audioTrack = null
        }

        try {
            systemRingtone?.apply {
                if (isPlaying) stop()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping system ringtone", e)
        } finally {
            systemRingtone = null
        }

        context?.let { ctx ->
            stopVibration(ctx)
        }
    }

    private fun playSystemRingtone(context: Context) {
        try {
            var uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (uri == null) {
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, uri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ringtone.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }
            ringtone.play()
            systemRingtone = ringtone
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play system ringtone, falling back to digital beep", e)
            playSynthesizedAlarm(AlarmSoundType.DIGITAL_BEEP)
        }
    }

    private fun playSynthesizedAlarm(type: AlarmSoundType) {
        soundJob?.cancel()
        soundJob = scope.launch {
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(BUFFER_SIZE * 2)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
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
                    .setBufferSizeInBytes(minBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.setVolume(1.0f)
                track.play()
                audioTrack = track

                val buffer = ShortArray(BUFFER_SIZE)
                var sampleIndex = 0L

                while (isActive && isPlaying) {
                    for (i in buffer.indices) {
                        val t = (sampleIndex + i).toDouble() / SAMPLE_RATE
                        val sampleValue = generateAlarmSample(t, type)
                        buffer[i] = (sampleValue * 32000.0).toInt().coerceIn(-32768, 32767).toShort()
                    }
                    sampleIndex += buffer.size
                    track.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in synthesized alarm loop", e)
            }
        }
    }

    private fun generateAlarmSample(t: Double, type: AlarmSoundType): Double {
        return when (type) {
            AlarmSoundType.DIGITAL_BEEP -> {
                // Repeating pattern: Beep (0.12s), Gap (0.08s), Beep (0.12s), Gap (0.08s),
                // Beep (0.12s), Gap (0.08s), Beep (0.12s), Pause (0.4s) -> Total 1.1s cycle
                val cyclePos = t % 1.1
                val isBeep = (cyclePos in 0.0..0.12) ||
                        (cyclePos in 0.20..0.32) ||
                        (cyclePos in 0.40..0.52) ||
                        (cyclePos in 0.60..0.72)

                if (isBeep) {
                    // Crisp square-softened dual tone (880 Hz + 1760 Hz harmonic)
                    val s1 = sin(2.0 * PI * 880.0 * t)
                    val s2 = sin(2.0 * PI * 1760.0 * t) * 0.4
                    (s1 + s2) * 0.85
                } else {
                    0.0
                }
            }
            AlarmSoundType.RADAR_PULSE -> {
                // Ascending frequency radar sweep every 0.8s
                val cyclePos = t % 0.8
                if (cyclePos < 0.35) {
                    val progress = cyclePos / 0.35
                    val freq = 600.0 + progress * 800.0 // 600Hz -> 1400Hz sweep
                    sin(2.0 * PI * freq * t) * (1.0 - progress * 0.2)
                } else {
                    0.0
                }
            }
            AlarmSoundType.CLASSIC_BELL -> {
                // Rapid modulated twin-bell resonance (650Hz + 850Hz modulated at 16Hz)
                val cyclePos = t % 1.5
                if (cyclePos < 0.9) {
                    val mod = 0.5 + 0.5 * sin(2.0 * PI * 18.0 * t) // 18Hz tremolo
                    val s1 = sin(2.0 * PI * 700.0 * t)
                    val s2 = sin(2.0 * PI * 840.0 * t) * 0.7
                    (s1 + s2) * mod * 0.8
                } else {
                    0.0
                }
            }
            AlarmSoundType.GENTLE_CHIME -> {
                // Warm, musical triads (C5 523Hz, E5 659Hz, G5 784Hz) decaying smoothly
                val cyclePos = t % 2.5
                if (cyclePos < 2.0) {
                    val decay = kotlin.math.exp(-cyclePos * 1.8)
                    val s1 = sin(2.0 * PI * 523.25 * t)
                    val s2 = sin(2.0 * PI * 659.25 * t) * 0.6
                    val s3 = sin(2.0 * PI * 783.99 * t) * 0.4
                    (s1 + s2 + s3) * decay * 0.75
                } else {
                    0.0
                }
            }
            AlarmSoundType.SUNRISE_MELODY -> {
                // Ascending melodic arpeggio (D4 293Hz, F#4 369Hz, A4 440Hz, D5 587Hz) repeating every 1.6s
                val cyclePos = t % 1.6
                val noteIdx = ((cyclePos / 0.35).toInt()).coerceIn(0, 3)
                val noteT = cyclePos % 0.35
                val freqs = doubleArrayOf(293.66, 369.99, 440.00, 587.33)
                val f = freqs[noteIdx]
                val decay = kotlin.math.exp(-noteT * 4.0)
                sin(2.0 * PI * f * t) * decay * 0.85
            }
            AlarmSoundType.ARCADE_ALERT -> {
                // Upbeat 8-bit retro arcade fanfare with octave hops
                val cyclePos = t % 0.9
                val step = ((cyclePos / 0.15).toInt()).coerceIn(0, 5)
                val stepFreqs = doubleArrayOf(440.0, 554.37, 659.25, 880.0, 1108.73, 1318.51)
                val f = stepFreqs[step]
                val isSquare = sin(2.0 * PI * f * t) > 0
                if (isSquare) 0.65 else -0.65
            }
            AlarmSoundType.CYBER_SYNTH -> {
                // Pulsing futuristic bass/lead dual saw modulation
                val cyclePos = t % 0.6
                val env = 0.5 + 0.5 * sin(2.0 * PI * 10.0 * t) // 10Hz LFO
                val f1 = 220.0 + env * 110.0
                val f2 = 440.0 + env * 220.0
                (sin(2.0 * PI * f1 * t) * 0.6 + sin(2.0 * PI * f2 * t) * 0.4) * 0.8
            }
            AlarmSoundType.HEAVY_PULSE -> {
                // Deep low-frequency urgent alert double-thump (180Hz + 90Hz sub)
                val cyclePos = t % 1.0
                val isPulse = (cyclePos in 0.0..0.18) || (cyclePos in 0.28..0.46)
                if (isPulse) {
                    val pTime = if (cyclePos < 0.25) cyclePos else cyclePos - 0.28
                    val decay = kotlin.math.exp(-pTime * 10.0)
                    (sin(2.0 * PI * 180.0 * t) + sin(2.0 * PI * 90.0 * t) * 0.7) * decay * 0.9
                } else {
                    0.0
                }
            }
            AlarmSoundType.ROOSTER_MORNING -> {
                // Organic harmonic wake-up cadence (480Hz modulated up to 960Hz)
                val cyclePos = t % 2.0
                if (cyclePos < 1.2) {
                    val mod = (cyclePos / 1.2)
                    val f = 480.0 + mod * 480.0
                    val s1 = sin(2.0 * PI * f * t)
                    val s2 = sin(2.0 * PI * (f * 1.5) * t) * 0.4
                    (s1 + s2) * (1.0 - mod * 0.3) * 0.8
                } else {
                    0.0
                }
            }
            AlarmSoundType.SYSTEM_DEFAULT -> 0.0
        }
    }

    private fun startVibration(context: Context) {
        vibratorJob?.cancel()
        vibratorJob = scope.launch {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            val pattern = longArrayOf(0, 500, 250, 500, 250, 500, 1000)
            while (isActive && isPlaying) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(pattern, -1)
                    }
                } catch (_: Exception) {}
                delay(3000L)
            }
        }
    }

    private fun stopVibration(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.cancel()
        } catch (_: Exception) {}
    }
}
