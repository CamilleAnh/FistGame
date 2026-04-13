package com.yourname.fruitsort

import android.content.Context
import android.media.MediaPlayer

/**
 * Singleton quản lý nhạc nền toàn cục.
 *
 * Lý do: Mỗi Fragment tạo MediaPlayer riêng → khi chuyển màn có 2-3 instance
 * cùng tồn tại → cạnh tranh audio driver → stutter/giật nhạc.
 * Giải pháp: Chỉ 1 MediaPlayer duy nhất cho toàn app, tái sử dụng qua các màn.
 */
object GlobalMusicPlayer {

    private var player: MediaPlayer? = null
    private var currentResId: Int = -1

    /**
     * Phát nhạc nền nếu setting cho phép.
     * Nếu đúng bài đang chơi → chỉ resume, không tạo lại.
     * Nếu bài khác → release rồi tạo mới.
     */
    fun playIfEnabled(context: Context, resId: Int) {
        if (!isMusicEnabled(context)) {
            pause()
            return
        }

        if (player != null && currentResId == resId) {
            // Cùng bài – chỉ resume nếu đang tạm dừng
            if (player?.isPlaying == false) {
                player?.start()
            }
            return
        }

        // Bài mới hoặc chưa khởi tạo → tạo MediaPlayer
        releaseInternal()
        val mp = MediaPlayer.create(context.applicationContext, resId) ?: return
        mp.isLooping = true
        mp.setVolume(0.35f, 0.35f) // Âm lượng 35% theo AUDIO_SYSTEM_PRINCIPLES
        mp.start()
        player = mp
        currentResId = resId
    }

    /** Tiếp tục nhạc (sau khi onResume) */
    fun resumeIfEnabled(context: Context) {
        if (!isMusicEnabled(context)) return
        if (player?.isPlaying == false) {
            player?.start()
        }
    }

    /** Tạm dừng nhạc (onPause) */
    fun pause() {
        if (player?.isPlaying == true) {
            player?.pause()
        }
    }

    /** Bật/tắt nhạc theo setting */
    fun setEnabled(context: Context, enabled: Boolean) {
        if (enabled) {
            if (currentResId != -1) resumeIfEnabled(context)
        } else {
            pause()
        }
    }

    /** Gọi trong MainActivity.onDestroy() để giải phóng hoàn toàn */
    fun releaseAll() {
        releaseInternal()
    }

    private fun isMusicEnabled(context: Context): Boolean {
        return context.applicationContext
            .getSharedPreferences("game_settings", Context.MODE_PRIVATE)
            .getBoolean("music_on", true)
    }

    private fun releaseInternal() {
        player?.release()
        player = null
        currentResId = -1
    }
}
