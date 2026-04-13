package com.yourname.fruitsort

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import kotlin.math.*

/**
 * Synthesizes musical notes using sine waves + ADSR envelope via AudioTrack.
 * Produces actual musical notes (not beeps) at exact frequencies.
 */
object SoundSynthesizer {

    private const val SAMPLE_RATE = 44100

    // Musical note frequencies (Hz)
    private const val C4  = 261.63
    private const val D4  = 293.66
    private const val E4  = 329.63
    private const val F4  = 349.23
    private const val G4  = 392.00
    private const val A4  = 440.00
    private const val B4  = 493.88
    private const val C5  = 523.25
    private const val D5  = 587.33
    private const val E5  = 659.25
    private const val G5  = 783.99
    private const val A5  = 880.00
    private const val C6  = 1046.50

    /**
     * Generate a single note with ADSR envelope.
     * @param freq       frequency in Hz
     * @param durationMs total note duration in ms
     * @param volume     amplitude 0.0 to 1.0
     * @param harmonics  add overtones for richer timbre (true = bell-like)
     */
    private fun generateNote(
        freq: Double,
        durationMs: Int,
        volume: Float = 0.8f,
        harmonics: Boolean = true
    ): ShortArray {
        val numSamples = (SAMPLE_RATE * durationMs / 1000.0).toInt()
        val samples = ShortArray(numSamples)
        val attackMs  = minOf(30, durationMs / 6)
        val decayMs   = minOf(60, durationMs / 4)
        val releaseMs = minOf(80, durationMs / 4)
        val attackSamples  = (SAMPLE_RATE * attackMs  / 1000.0).toInt()
        val decaySamples   = (SAMPLE_RATE * decayMs   / 1000.0).toInt()
        val releaseSamples = (SAMPLE_RATE * releaseMs / 1000.0).toInt()
        val sustainLevel = 0.65f

        for (i in 0 until numSamples) {
            // === ADSR Envelope ===
            val env = when {
                i < attackSamples -> i.toFloat() / attackSamples
                i < attackSamples + decaySamples -> {
                    val t = (i - attackSamples).toFloat() / decaySamples
                    1f - t * (1f - sustainLevel)
                }
                i >= numSamples - releaseSamples -> {
                    val t = (i - (numSamples - releaseSamples)).toFloat() / releaseSamples
                    sustainLevel * (1f - t)
                }
                else -> sustainLevel
            }

            // === Synthesis: fundamental + harmonics for bell/xylophone timbre ===
            val t = i.toDouble() / SAMPLE_RATE
            val fundamental = sin(2.0 * PI * freq * t)
            val wave = if (harmonics) {
                // Add 2nd and 3rd harmonics with decaying amplitude = bell-like
                val decay = exp(-3.0 * i / numSamples)  // harmonics fade faster
                fundamental * 0.7 +
                sin(2.0 * PI * freq * 2 * t) * 0.2 * decay +
                sin(2.0 * PI * freq * 3 * t) * 0.1 * decay
            } else {
                fundamental
            }

            samples[i] = (wave * env * volume * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    /**
     * Play a sequence of notes asynchronously using AudioTrack.
     * @param notes list of Pair(frequencyHz, durationMs)
     * @param gapMs silence gap between notes (ms)
     */
    private fun playSequence(
        notes: List<Pair<Double, Int>>,
        gapMs: Int = 20,
        volume: Float = 0.8f,
        harmonics: Boolean = true
    ) {
        Thread {
            for ((freq, dur) in notes) {
                val pcm = generateNote(freq, dur, volume, harmonics)
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
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
                    .setBufferSizeInBytes(pcm.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()
                // Wait for playback to finish before next note
                Thread.sleep(dur.toLong() + gapMs)
                track.stop()
                track.release()
            }
        }.start()
    }

    /**
     * Win jingle: C5 - E5 - G5 - C6 ascending triumphant arpeggio + sparkle ending
     */
    fun playWin() {
        val notes = listOf(
            C5 to 130,
            E5 to 130,
            G5 to 130,
            C6 to 380    // long bright final note
        )
        playSequence(notes, gapMs = 10, volume = 0.85f, harmonics = true)
    }

    /**
     * Lose jingle: G4 - E4 - C4 descending sad minor fall
     */
    fun playLose() {
        val notes = listOf(
            G4 to 200,
            E4 to 200,
            C4 to 180,
            A4 * 0.5 to 450  // low muted final note (A3 approximation)
        )
        playSequence(notes, gapMs = 30, volume = 0.7f, harmonics = false)
    }
}
