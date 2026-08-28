package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.example.data.model.FocusSound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

object FocusAudioSynthesizer {
    private const val TAG = "FocusAudioSynthesizer"
    private const val SAMPLE_RATE = 44100
    private const val BUFFER_SIZE_FRAMES = 4096

    @Volatile
    private var audioTrack: AudioTrack? = null

    @Volatile
    private var currentSound: FocusSound = FocusSound.NONE

    @Volatile
    private var isPlaying: Boolean = false

    @Volatile
    private var targetVolume: Float = 0.8f

    @Volatile
    private var currentVolume: Float = 0.8f

    private var audioJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun getCurrentSound(): FocusSound = currentSound
    fun isAudioPlaying(): Boolean = isPlaying
    fun getVolume(): Float = targetVolume

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        targetVolume = clamped
        currentVolume = clamped
        try {
            audioTrack?.setVolume(clamped)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting volume", e)
        }
    }

    fun playSound(sound: FocusSound, volume: Float = targetVolume, fadeIn: Boolean = true) {
        if (sound == FocusSound.NONE) {
            stopSound()
            return
        }

        if (isPlaying && currentSound == sound) {
            setVolume(volume)
            return
        }

        stopSound()
        currentSound = sound
        targetVolume = volume.coerceIn(0f, 1f)
        currentVolume = if (fadeIn) 0.05f else targetVolume

        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(BUFFER_SIZE_FRAMES * 2)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
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

            track.setVolume(currentVolume)
            track.play()
            audioTrack = track
            isPlaying = true

            audioJob = scope.launch {
                synthesizeAudioLoop(sound)
            }

            if (fadeIn) {
                scope.launch {
                    val steps = 20
                    val delayMs = 50L
                    for (i in 1..steps) {
                        if (!isPlaying) break
                        currentVolume = (targetVolume * (i.toFloat() / steps)).coerceIn(0f, 1f)
                        try {
                            audioTrack?.setVolume(currentVolume)
                        } catch (e: Exception) {
                            // ignore
                        }
                        delay(delayMs)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting procedural audio", e)
            isPlaying = false
            audioTrack = null
        }
    }

    fun stopSound(fadeOut: Boolean = false) {
        if (!isPlaying && audioTrack == null) return

        if (fadeOut && isPlaying) {
            scope.launch {
                val steps = 10
                for (i in (steps - 1) downTo 0) {
                    val v = (currentVolume * (i.toFloat() / steps)).coerceIn(0f, 1f)
                    try {
                        audioTrack?.setVolume(v)
                    } catch (e: Exception) {
                        // ignore
                    }
                    delay(30L)
                }
                cleanupTrack()
            }
        } else {
            cleanupTrack()
        }
    }

    /**
     * Plays a high-quality synthesized sound reminder when Focus Session Ends and Short Break Begins.
     */
    fun playSessionEndBreakStartChime() {
        scope.launch(Dispatchers.Default) {
            playToneSequence(
                frequencies = listOf(587.33, 880.0), // D5 -> A5 gentle transition
                durationsMs = listOf(350, 600),
                volume = 0.85f
            )
        }
    }

    /**
     * Plays a distinct harmonic chime for Long Break start.
     */
    fun playLongBreakChime() {
        scope.launch(Dispatchers.Default) {
            playToneSequence(
                frequencies = listOf(440.0, 554.37, 659.25, 880.0), // A major chord chime
                durationsMs = listOf(300, 300, 300, 800),
                volume = 0.9f
            )
        }
    }

    /**
     * Plays a bright ascending chime when Break Ends and Focus Session Starts.
     */
    fun playBreakEndSessionStartChime() {
        scope.launch(Dispatchers.Default) {
            playToneSequence(
                frequencies = listOf(523.25, 659.25, 783.99), // C5 -> E5 -> G5 motivating chime
                durationsMs = listOf(200, 200, 600),
                volume = 0.85f
            )
        }
    }

    private fun playToneSequence(frequencies: List<Double>, durationsMs: List<Int>, volume: Float) {
        try {
            val totalFrames = durationsMs.sumOf { (it * SAMPLE_RATE) / 1000 }
            val buffer = ShortArray(totalFrames)
            var currentFrame = 0

            for (idx in frequencies.indices) {
                val freq = frequencies[idx]
                val toneFrames = (durationsMs[idx] * SAMPLE_RATE) / 1000
                var phase = 0.0

                for (f in 0 until toneFrames) {
                    val progress = f.toDouble() / toneFrames
                    // Envelope: fast attack, exponential decay
                    val attack = (f.toDouble() / (SAMPLE_RATE * 0.015)).coerceAtMost(1.0)
                    val decay = exp(-progress * 4.0)
                    val env = attack * decay

                    val sample1 = sin(phase)
                    val sample2 = sin(phase * 2.0) * 0.35 // harmonic
                    val sample3 = sin(phase * 3.0) * 0.15
                    val sample = (sample1 + sample2 + sample3) * env * volume

                    val clipped = sample.coerceIn(-0.99, 0.99)
                    if (currentFrame + f < buffer.size) {
                        buffer[currentFrame + f] = (clipped * 32767.0).toInt().toShort()
                    }
                    phase += 2.0 * PI * freq / SAMPLE_RATE
                    if (phase > 2.0 * PI) phase -= 2.0 * PI
                }
                currentFrame += toneFrames
            }

            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(buffer.size * 2)

            val alertTrack = AudioTrack.Builder()
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
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            alertTrack.write(buffer, 0, buffer.size)
            alertTrack.play()
            Thread.sleep((durationsMs.sum() + 200).toLong())
            alertTrack.stop()
            alertTrack.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alert tone sequence", e)
        }
    }

    private fun cleanupTrack() {
        isPlaying = false
        currentSound = FocusSound.NONE
        audioJob?.cancel()
        audioJob = null
        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    pause()
                    flush()
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up AudioTrack", e)
        } finally {
            audioTrack = null
        }
    }

    private fun synthesizeAudioLoop(sound: FocusSound) {
        val buffer = ShortArray(BUFFER_SIZE_FRAMES)
        var phase1 = 0.0
        var phase2 = 0.0
        var phase3 = 0.0
        var filterState = 0.0
        var pinkB0 = 0.0
        var pinkB1 = 0.0
        var pinkB2 = 0.0
        var chimeTime = 0
        var lofiChordIdx = 0
        var lofiSampleCount = 0

        // Lo-fi chord frequencies (Dm9, G13, Cmaj7, Am9)
        val chords = listOf(
            listOf(146.83, 174.61, 220.00, 261.63, 329.63), // Dm9
            listOf(196.00, 246.94, 293.66, 329.63, 440.00), // G13
            listOf(130.81, 164.81, 196.00, 246.94, 293.66), // Cmaj7
            listOf(110.00, 130.81, 164.81, 196.00, 246.94)  // Am9
        )

        while (isPlaying && scope.isActive) {
            for (i in 0 until BUFFER_SIZE_FRAMES) {
                var sample = 0.0

                when (sound) {
                    FocusSound.RAIN -> {
                        // Pink noise with dynamic lowpass and raindrop ticks
                        val white = Random.nextDouble(-1.0, 1.0)
                        pinkB0 = 0.99886 * pinkB0 + white * 0.0555179
                        pinkB1 = 0.99332 * pinkB1 + white * 0.0750759
                        pinkB2 = 0.96900 * pinkB2 + white * 0.1538520
                        val pink = (pinkB0 + pinkB1 + pinkB2 + white * 0.5362) * 0.15
                        
                        // Droplet clicks
                        var drop = 0.0
                        if (Random.nextDouble() < 0.002) {
                            drop = sin(phase1) * Random.nextDouble(0.3, 0.7)
                        }
                        phase1 += 2.0 * PI * 1800.0 / SAMPLE_RATE
                        if (phase1 > 2.0 * PI) phase1 -= 2.0 * PI

                        sample = (pink * 0.75 + drop * 0.25)
                    }

                    FocusSound.WHITE_NOISE -> {
                        val white = Random.nextDouble(-1.0, 1.0)
                        pinkB0 = 0.99886 * pinkB0 + white * 0.0555179
                        pinkB1 = 0.99332 * pinkB1 + white * 0.0750759
                        val smooth = (pinkB0 + pinkB1 + white * 0.3) * 0.2
                        sample = smooth
                    }

                    FocusSound.FOREST -> {
                        // Wind gentle modulation + bird chirps
                        phase1 += 2.0 * PI * 0.2 / SAMPLE_RATE // wind LFO
                        val windMod = (sin(phase1) + 1.2) * 0.5
                        val white = Random.nextDouble(-1.0, 1.0) * 0.1 * windMod

                        // Chirp
                        var bird = 0.0
                        if (chimeTime in 1..4000) {
                            val freq = 2400.0 + sin(chimeTime * 0.03) * 400.0
                            phase2 += 2.0 * PI * freq / SAMPLE_RATE
                            val env = exp(-chimeTime.toDouble() / 1500.0)
                            bird = sin(phase2) * env * 0.35
                            chimeTime++
                        } else if (Random.nextDouble() < 0.00015) {
                            chimeTime = 1
                            phase2 = 0.0
                        }

                        sample = white + bird
                    }

                    FocusSound.DEEP_FOCUS -> {
                        // 120Hz carrier + 40Hz isochronic gamma/alpha pulse
                        phase1 += 2.0 * PI * 120.0 / SAMPLE_RATE
                        phase2 += 2.0 * PI * 40.0 / SAMPLE_RATE
                        phase3 += 2.0 * PI * 60.0 / SAMPLE_RATE

                        val carrier = sin(phase1) * 0.35 + sin(phase3) * 0.15
                        val pulse = (sin(phase2) + 1.0) * 0.5
                        val subBass = sin(phase1 * 0.5) * 0.2
                        sample = (carrier * pulse + subBass) * 0.45
                    }

                    FocusSound.LO_FI -> {
                        lofiSampleCount++
                        if (lofiSampleCount > SAMPLE_RATE * 3) {
                            lofiSampleCount = 0
                            lofiChordIdx = (lofiChordIdx + 1) % chords.size
                        }
                        val curChord = chords[lofiChordIdx]
                        val env = exp(-(lofiSampleCount.toDouble() % (SAMPLE_RATE * 1.5)) / (SAMPLE_RATE * 0.8))
                        var chordSum = 0.0
                        for ((idx, freq) in curChord.withIndex()) {
                            val p = (phase1 * (idx + 1) + phase2) % (2.0 * PI)
                            chordSum += sin(2.0 * PI * freq * (lofiSampleCount.toDouble() / SAMPLE_RATE)) * 0.2
                        }
                        val vinylNoise = Random.nextDouble(-1.0, 1.0) * 0.03
                        sample = (chordSum * env * 0.5) + vinylNoise
                    }

                    FocusSound.CHIME -> {
                        chimeTime++
                        if (chimeTime > SAMPLE_RATE * 6) {
                            chimeTime = 0
                        }
                        val env = exp(-chimeTime.toDouble() / (SAMPLE_RATE * 1.8))
                        val tone1 = sin(2.0 * PI * 528.0 * (chimeTime.toDouble() / SAMPLE_RATE))
                        val tone2 = sin(2.0 * PI * 1056.0 * (chimeTime.toDouble() / SAMPLE_RATE)) * 0.4
                        val tone3 = sin(2.0 * PI * 1584.0 * (chimeTime.toDouble() / SAMPLE_RATE)) * 0.2
                        sample = (tone1 + tone2 + tone3) * env * 0.35
                    }

                    FocusSound.COZY_FIRE -> {
                        val white = Random.nextDouble(-1.0, 1.0)
                        filterState = filterState * 0.95 + white * 0.05
                        var crackle = 0.0
                        if (Random.nextDouble() < 0.003) {
                            crackle = Random.nextDouble(-0.6, 0.6)
                        }
                        sample = filterState * 0.3 + crackle * 0.4
                    }

                    FocusSound.STREAM -> {
                        phase1 += 2.0 * PI * 1.5 / SAMPLE_RATE
                        val lfo = (sin(phase1) + 1.0) * 0.5
                        val white = Random.nextDouble(-1.0, 1.0)
                        filterState = filterState * (0.85 + lfo * 0.08) + white * 0.1
                        sample = filterState * 0.4
                    }

                    FocusSound.NONE -> {
                        sample = 0.0
                    }
                }

                // Soft clip and convert to 16-bit PCM
                val clipped = sample.coerceIn(-0.99, 0.99)
                buffer[i] = (clipped * 32767.0).toInt().toShort()
            }

            try {
                audioTrack?.write(buffer, 0, BUFFER_SIZE_FRAMES)
            } catch (e: Exception) {
                break
            }
        }
    }
}
