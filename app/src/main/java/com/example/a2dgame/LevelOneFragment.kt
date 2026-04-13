package com.yourname.fruitsort

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Path
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.appcompat.widget.PopupMenu
import com.yourname.fruitsort.databinding.FragmentLevelOneBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlin.random.Random

class LevelOneFragment : Fragment() {

    private var _binding: FragmentLevelOneBinding? = null
    private val binding get() = _binding!!
    
    private val args: LevelOneFragmentArgs by navArgs()
    private lateinit var engine: LevelOneEngine
    private var soundManager: SoundManager? = null

    // Power-up counts
    private var powerupReroll    = 1
    private var powerupMagnify   = 1
    private var powerupReshuffle = 1
    private var isMagnifyMode    = false

    private var isWinDialogShowing = false
    private var isLoseDialogShowing = false

    private val wiggleAnimators = mutableMapOf<View, Animator>()
    
    private var activeAnimationsCount = 0
    private val animatingBoxes = mutableSetOf<Int>()
    private val pendingIncomingMap = mutableMapOf<Int, Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLevelOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val levelId = args.levelId
        engine = LevelOneEngine(levelId)
        soundManager = SoundManager(requireContext())
        
        binding.tvLevelName.text = getString(R.string.level_name_format, levelId)
        
        loadBannerAd()
        playBackgroundMusic()
        renderBoard()

        binding.btnReset.setOnClickListener {
            if (activeAnimationsCount > 0) return@setOnClickListener
            engine = LevelOneEngine(levelId)
            powerupReroll    = 1
            powerupMagnify   = 1
            powerupReshuffle = 1
            isMagnifyMode    = false
            updatePowerupButtons()
            renderBoard()
        }

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        setupSettings()
        setupPowerups()
        updateGoldDisplay()
    }

    private fun updateGoldDisplay() {
        val gold = GoldManager.getGold(requireContext())
        binding.tvGameGold.text = getString(R.string.gold_display_format, gold)
    }

    private fun setupSettings() {
        val prefs = requireContext().getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        val settingsBinding = binding.layoutSettings

        binding.btnSettings.setOnClickListener { anchor ->
            val popup = PopupMenu(requireContext(), anchor)
            popup.menu.add(0, 1, 0, getString(R.string.home_menu))
            popup.menu.add(0, 2, 1, getString(R.string.reset_level))
            popup.menu.add(0, 3, 2, getString(R.string.settings_menu))
            
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        findNavController().popBackStack(R.id.SecondFragment, false)
                        true
                    }
                    2 -> {
                        if (activeAnimationsCount > 0) return@setOnMenuItemClickListener true
                        engine = LevelOneEngine(args.levelId)
                        renderBoard()
                        true
                    }
                    3 -> {
                        showSettings(true)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        settingsBinding.btnCloseSettings.setOnClickListener { showSettings(false) }
        
        settingsBinding.btnLangEn.setOnClickListener { changeLanguage("en") }
        settingsBinding.btnLangVi.setOnClickListener { changeLanguage("vi") }

        updateLanguageButtons(LanguageManager.getSavedLanguage(requireContext()))
    }

    private fun changeLanguage(langCode: String) {
        if (langCode == LanguageManager.getSavedLanguage(requireContext())) return
        LanguageManager.setLocale(requireContext(), langCode)
        activity?.recreate()
    }

    private fun showSettings(show: Boolean) {
        val settingsCard = binding.layoutSettings.settingsCard
        val overlay = binding.layoutSettings.root
        if (show) {
            overlay.visibility = View.VISIBLE
            settingsCard.alpha = 0f
            settingsCard.animate().alpha(1f).setDuration(300).start()
        } else {
            overlay.visibility = View.GONE
        }
    }

    private fun updateLanguageButtons(lang: String) {
        val settingsBinding = binding.layoutSettings
        settingsBinding.btnLangEn.alpha = if (lang == "en") 1.0f else 0.5f
        settingsBinding.btnLangVi.alpha = if (lang == "vi") 1.0f else 0.5f
    }

    private fun setupPowerups() {
        val ctx = requireContext()
        powerupReroll    = 1 + GoldManager.getRerollCount(ctx)
        powerupMagnify   = 1 + GoldManager.getRevealCount(ctx)
        powerupReshuffle = 1 + GoldManager.getShuffleCount(ctx)
        updatePowerupButtons()

        binding.btnRerollBags.setOnClickListener {
            if (powerupReroll <= 0 || activeAnimationsCount > 0) return@setOnClickListener
            powerupReroll--
            engine.rerollBags()
            updatePowerupButtons()
            renderBoard()
        }

        binding.btnMagnify.setOnClickListener {
            if (powerupMagnify <= 0 || activeAnimationsCount > 0) return@setOnClickListener
            isMagnifyMode = !isMagnifyMode
            updatePowerupButtons()
        }

        binding.btnReshuffle.setOnClickListener {
            if (powerupReshuffle <= 0 || activeAnimationsCount > 0) return@setOnClickListener
            powerupReshuffle--
            engine.shuffleAllBoxes()
            updatePowerupButtons()
            renderBoard()
        }
    }

    private fun updatePowerupButtons() {
        binding.btnRerollBags.text = "🎲 x$powerupReroll"
        binding.btnMagnify.text = if (isMagnifyMode) "🔍 ✓" else "🔍 x$powerupMagnify"
        binding.btnReshuffle.text = "🔀 x$powerupReshuffle"
    }

    private fun renderBoard() {
        binding.glGameBoard.removeAllViews()
        val boxes = engine.getBoxes().filter { !it.isArchived }
        
        val cols = 4 
        binding.glGameBoard.columnCount = cols
        val boxWidth = (resources.displayMetrics.widthPixels - (80 * resources.displayMetrics.density).toInt()) / cols
        val blockHeight = (boxWidth * 0.52).toInt()
        val boxHeight = (blockHeight * 4) + (16 * resources.displayMetrics.density).toInt()

        if (engine.isBagMechanismEnabled) {
            val slots = engine.getBoxSlots()
            slots.getOrNull(0)?.let { updateBoxUI(binding.tvBoxA, it) }
            slots.getOrNull(1)?.let { updateBoxUI(binding.tvBoxB, it) }
        }

        boxes.forEach { box ->
            val boxContainer = FrameLayout(requireContext()).apply {
                tag = box.id
                layoutParams = GridLayout.LayoutParams().apply {
                    width = boxWidth
                    height = boxHeight
                    setMargins(10, 6, 10, 6)
                }
                setOnClickListener { handleBoxTap(box.id) }
            }

            val boxLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM
                background = ContextCompat.getDrawable(context, R.drawable.carton_box_bg)
                setPadding(8, 6, 8, 12)
                layoutParams = FrameLayout.LayoutParams(-1, -1)
            }

            box.blocks.forEachIndexed { i, fruit ->
                val isHidden = i < box.hiddenLayers
                val blockView = FrameLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(-1, blockHeight).apply { setMargins(3, -2, 3, 0) }
                    background = if (isHidden) GradientDrawable().apply { setColor(0x55000000); cornerRadius = 12f }
                                 else ContextCompat.getDrawable(context, R.drawable.item_fruit_box)
                    
                    addView(TextView(context).apply {
                        gravity = Gravity.CENTER
                        text = if (isHidden) "?" else fruit.fruitIcon
                        textSize = if (isHidden) 16f else 24f
                        setTextColor(if (isHidden) Color.WHITE else Color.BLACK)
                    })
                }
                boxLayout.addView(blockView, 0)
            }
            boxContainer.addView(boxLayout)
            binding.glGameBoard.addView(boxContainer)
        }
        updateStatusUI()
    }

    private fun handleBoxTap(index: Int) {
        if (engine.isGameOver || animatingBoxes.contains(index)) return
        if (isMagnifyMode) {
            engine.revealHiddenLayers(index)
            isMagnifyMode = false
            powerupMagnify--
            updatePowerupButtons()
            renderBoard()
            return
        }
        
        val selectedIdx = engine.selectedBoxIndex
        if (selectedIdx == null) {
            engine.selectedBoxIndex = index
            renderBoard()
        } else if (selectedIdx == index) {
            engine.selectedBoxIndex = null
            renderBoard()
        } else {
            val src = engine.getBoxes().find { it.id == selectedIdx }!!
            val dst = engine.getBoxes().find { it.id == index }!!
            if (engine.canMove(src, dst)) {
                animateMoveSequence(selectedIdx, index)
            } else {
                engine.selectedBoxIndex = index
                renderBoard()
            }
        }
    }

    private fun animateMoveSequence(srcId: Int, dstId: Int) {
        activeAnimationsCount++
        animatingBoxes.add(srcId)
        animatingBoxes.add(dstId)
        engine.executeMove(engine.getBoxes().find { it.id == srcId }!!, engine.getBoxes().find { it.id == dstId }!!)
        
        // Simplified move animation for brevity - in real app use the Path animation logic
        binding.root.postDelayed({
            animatingBoxes.remove(srcId)
            animatingBoxes.remove(dstId)
            activeAnimationsCount--
            engine.archiveAllReady()
            renderBoard()
            checkGameResults()
        }, 300)
    }

    private fun checkGameResults() {
        if (engine.isWin) showWinDialog()
        else if (engine.isGameOver || engine.isDeadlocked()) showLoseDialog()
    }

    private fun showWinDialog() {
        isWinDialogShowing = true
        binding.layoutWinDialog.root.visibility = View.VISIBLE
        binding.layoutWinDialog.tvWinSubtitle.text = getString(R.string.win_dialog_subtitle, args.levelId)
        binding.layoutWinDialog.btnWinContinue.setOnClickListener {
            GoldManager.addGold(requireContext(), 50)
            navigateToNextLevel()
        }
    }

    private fun showLoseDialog() {
        isLoseDialogShowing = true
        binding.layoutLoseDialog.root.visibility = View.VISIBLE
        binding.layoutLoseDialog.btnLoseRetry.setOnClickListener {
            activity?.recreate()
        }
    }

    private fun navigateToNextLevel() {
        val nextLevelId = args.levelId + 1
        val bundle = Bundle().apply { putInt("levelId", nextLevelId) }
        findNavController().navigate(R.id.action_LevelOneFragment_self, bundle)
    }

    private fun updateStatusUI() {
        binding.tvPackedProgress.text = engine.getProgressText()
    }

    private fun updateBoxUI(tv: TextView, box: LevelOneEngine.BoxSlot) {
        tv.visibility = View.VISIBLE
        tv.text = getString(R.string.bag_info_format, box.targetColor.fruitIcon, box.filled, box.capacity, box.turnsLeft)
    }

    private fun loadBannerAd() {
        // VIP: ẩn banner hoàn toàn
        if (GoldManager.isVip(requireContext())) {
            binding.adContainer.visibility = View.GONE
            return
        }
        val adView = AdView(requireContext()).apply {
            setAdSize(AdSize.BANNER)
            // TODO (PRODUCTION): Thay bằng Banner Ad Unit ID thật từ AdMob Dashboard
            adUnitId = "ca-app-pub-3940256099942544/6300978111"
        }
        binding.adContainer.addView(adView)
        adView.loadAd(AdRequest.Builder().build())
    }

    private fun playBackgroundMusic() {
        GlobalMusicPlayer.playIfEnabled(requireContext(), R.raw.nhacnen)
    }

    override fun onResume() {
        super.onResume()
        GlobalMusicPlayer.resumeIfEnabled(requireContext())
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
