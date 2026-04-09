package com.example.a2dgame

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import androidx.core.content.edit
import androidx.navigation.fragment.findNavController
import com.example.a2dgame.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private var bgMusic: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPlay.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        setupSettings()
        startBgMusic()
    }

    private fun startBgMusic() {
        val prefs = requireContext().getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        val isMusicOn = prefs.getBoolean("music_on", true)
        
        if (isMusicOn) {
            if (bgMusic == null) {
                bgMusic = MediaPlayer.create(requireContext(), R.raw.nhacnen)
                bgMusic?.isLooping = true
            }
            if (bgMusic?.isPlaying == false) {
                bgMusic?.start()
            }
        }
    }

    private fun stopBgMusic() {
        bgMusic?.pause()
    }

    private fun setupSettings() {
        val prefs = requireContext().getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        val settingsBinding = binding.layoutSettings

        binding.btnSettings.setOnClickListener {
            showSettings(true)
        }

        settingsBinding.btnCloseSettings.setOnClickListener {
            showSettings(false)
        }

        settingsBinding.switchMusic.isChecked = prefs.getBoolean("music_on", true)
        settingsBinding.switchSound.isChecked = prefs.getBoolean("sound_on", true)
        settingsBinding.switchVibration.isChecked = prefs.getBoolean("vibration_on", true)

        settingsBinding.switchMusic.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("music_on", isChecked) }
            if (isChecked) startBgMusic() else stopBgMusic()
        }
        
        settingsBinding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("sound_on", isChecked) }
        }
        
        settingsBinding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("vibration_on", isChecked) }
        }

        settingsBinding.btnLangEn.setOnClickListener {
            prefs.edit { putString("language", "en") }
            updateLanguageButtons("en")
        }

        settingsBinding.btnLangVi.setOnClickListener {
            prefs.edit { putString("language", "vi") }
            updateLanguageButtons("vi")
        }

        updateLanguageButtons(prefs.getString("language", "en") ?: "en")

        settingsBinding.btnResetAll.setOnClickListener {
            prefs.edit { clear() }
            settingsBinding.switchMusic.isChecked = true
            settingsBinding.switchSound.isChecked = true
            settingsBinding.switchVibration.isChecked = true
            updateLanguageButtons("en")
            startBgMusic()
        }
    }

    private fun showSettings(show: Boolean) {
        val settingsCard = binding.layoutSettings.settingsCard
        val overlay = binding.layoutSettings.root

        if (show) {
            overlay.visibility = View.VISIBLE
            settingsCard.scaleX = 0.5f
            settingsCard.scaleY = 0.5f
            settingsCard.alpha = 0f
            
            settingsCard.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(AnticipateOvershootInterpolator())
                .start()
        } else {
            settingsCard.animate()
                .scaleX(0.5f)
                .scaleY(0.5f)
                .alpha(0f)
                .setDuration(250)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        overlay.visibility = View.GONE
                        settingsCard.animate().setListener(null)
                    }
                })
                .start()
        }
    }

    private fun updateLanguageButtons(lang: String) {
        val settingsBinding = binding.layoutSettings
        if (lang == "en") {
            settingsBinding.btnLangEn.alpha = 1.0f
            settingsBinding.btnLangVi.alpha = 0.5f
        } else {
            settingsBinding.btnLangEn.alpha = 0.5f
            settingsBinding.btnLangVi.alpha = 1.0f
        }
    }

    override fun onResume() {
        super.onResume()
        startBgMusic()
    }

    override fun onPause() {
        super.onPause()
        // We might want to keep music playing if navigating to another fragment, 
        // but for safety in this fragment:
        // bgMusic?.pause() 
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bgMusic?.release()
        bgMusic = null
        _binding = null
    }
}
