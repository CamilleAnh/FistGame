package com.yourname.fruitsort

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private val sounds = mutableMapOf<String, Int>()
    private var isEnabled = true
    private val appContext = context.applicationContext

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attrs)
            .build()

        // Load sounds dynamically to avoid "Unresolved reference" errors if files are missing
        loadSound(context, "pickup", "sfx_pickup")
        loadSound(context, "drop", "sfx_drop")
        loadSound(context, "move", "sfx_move")
        loadSound(context, "complete", "sfx_complete")
        
        val prefs = context.getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        isEnabled = prefs.getBoolean("sound_on", true)
    }

    private fun loadSound(context: Context, key: String, resName: String) {
        val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
        if (resId != 0) {
            sounds[key] = soundPool.load(context, resId, 1)
        } else {
            Log.w("SoundManager", "Resource not found: $resName")
        }
    }

    fun play(name: String) {
        if (!isEnabled) return
        val id = sounds[name] ?: return
        // Slight pitch variation for juicy feel (0.95 to 1.05)
        val pitch = 0.95f + (Math.random().toFloat() * 0.1f)
        soundPool.play(id, 1f, 1f, 1, 0, pitch)
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    /**
     * Win sound: plays sfx_win.mp3 via MediaPlayer
     */
    fun playWin() {
        if (!isEnabled) return
        playOneShot("sfx_win")
    }

    /**
     * Lose sound: plays sfx_gameover.mp3 via MediaPlayer
     */
    fun playLose() {
        if (!isEnabled) return
        playOneShot("sfx_gameover")
    }

    /**
     * Play a raw resource file once and auto-release when done.
     */
    private fun playOneShot(resName: String) {
        val resId = appContext.resources.getIdentifier(resName, "raw", appContext.packageName)
        if (resId == 0) {
            Log.w("SoundManager", "Resource not found: $resName")
            return
        }
        try {
            val mp = MediaPlayer.create(appContext, resId) ?: return
            mp.setOnCompletionListener { it.release() }
            mp.start()
        } catch (e: Exception) {
            Log.e("SoundManager", "Error playing $resName: ${e.message}")
        }
    }

    fun release() {
        soundPool.release()
    }
}
