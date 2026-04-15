package com.yourname.fruitsort

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import androidx.core.content.edit
import androidx.navigation.fragment.findNavController
import com.yourname.fruitsort.databinding.FragmentFirstBinding
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.constraintlayout.widget.ConstraintLayout

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

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

        binding.btnShop.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_ShopFragment)
        }

        binding.tvPrivacyPolicy.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_PrivacyPolicyFragment)
        }

        setupSettings()
        startBgMusic()
        updateGoldDisplay()
        
        // Cập nhật text từ strings.xml để đảm bảo hiển thị đúng ngôn ngữ vừa đổi
        refreshUIText()
    }

    private fun refreshUIText() {
        binding.btnSettings.text = getString(R.string.action_settings)
        binding.tvPrivacyPolicy.text = getString(R.string.privacy_policy_title).ifEmpty { "Privacy Policy" }
    }

    private fun updateGoldDisplay() {
        val gold = GoldManager.getGold(requireContext())
        binding.tvHomeGold.text = getString(R.string.gold_display_format, gold)
    }

    private fun startBgMusic() {
        GlobalMusicPlayer.playIfEnabled(requireContext(), R.raw.nhacnen)
    }

    private fun stopBgMusic() {
        GlobalMusicPlayer.pause()
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

        // Xử lý đổi ngôn ngữ
        settingsBinding.btnLangEn.setOnClickListener {
            changeLanguage("en")
        }

        settingsBinding.btnLangVi.setOnClickListener {
            changeLanguage("vi")
        }

        updateLanguageButtons(LanguageManager.getSavedLanguage(requireContext()))

        settingsBinding.btnResetAll.setOnClickListener {
            prefs.edit { clear() }
            settingsBinding.switchMusic.isChecked = true
            settingsBinding.switchSound.isChecked = true
            settingsBinding.switchVibration.isChecked = true
            changeLanguage("en")
        }
    }

    private fun changeLanguage(langCode: String) {
        if (langCode == LanguageManager.getSavedLanguage(requireContext())) return
        
        LanguageManager.setLocale(requireContext(), langCode)
        
        // Khởi động lại Activity để áp dụng ngôn ngữ mới cho toàn bộ app
        activity?.recreate()
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
            settingsBinding.btnLangEn.isEnabled = false
            settingsBinding.btnLangVi.alpha = 0.5f
            settingsBinding.btnLangVi.isEnabled = true
        } else {
            settingsBinding.btnLangEn.alpha = 0.5f
            settingsBinding.btnLangEn.isEnabled = true
            settingsBinding.btnLangVi.alpha = 1.0f
            settingsBinding.btnLangVi.isEnabled = false
        }
    }

    override fun onResume() {
        super.onResume()
        startBgMusic()
        updateGoldDisplay()
    }

    override fun onPause() {
        super.onPause()
        GlobalMusicPlayer.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
