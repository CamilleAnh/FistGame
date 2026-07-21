package com.twinbrother.fruitsort

import android.animation.*
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.twinbrother.fruitsort.databinding.FragmentLevelOneBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.launch

class LevelOneFragment : Fragment() {

    private var _binding: FragmentLevelOneBinding? = null
    private val binding get() = _binding!!
    
    private val args: LevelOneFragmentArgs by navArgs()
    private val viewModel: GameViewModel by viewModels()
    private var soundManager: SoundManager? = null
    private var hasShownResult = false
    private var isAnimating = false
    private val runningAnimators = mutableListOf<ObjectAnimator>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLevelOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.gameBoardView.clearAnimations()
        viewModel.initLevel(args.levelId, requireContext())
        soundManager = SoundManager(requireContext())
        
        setupObservers()
        setupListeners()
        loadBannerAd()
        playBackgroundMusic()
        setupTruckIdleAnimations()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.engine.collect { engine ->
                        engine?.let {
                            binding.gameBoardView.setEngine(it)
                            binding.gameBoardView.setSkinStyle(SkinManager.getSelectedStyle(requireContext()))
                            updateStatusUI(it)
                            updateTrucksUI(it)
                            setupUIForBossStatus(it)
                            updateGoldDisplay()
                            checkGameResults(it)
                        }
                    }
                }
                launch {
                    // Observe version counter to refresh UI when engine state mutates
                    viewModel.stateVersion.collect { _ ->
                        viewModel.engine.value?.let { engine ->
                            binding.gameBoardView.setEngine(engine)
                            binding.gameBoardView.setSkinStyle(SkinManager.getSelectedStyle(requireContext()))
                            updateStatusUI(engine)
                            updateTrucksUI(engine)
                            updateGoldDisplay()
                            checkGameResults(engine)
                        }
                    }
                }
                launch {
                    viewModel.selectedBoxIndex.collect { index ->
                        binding.gameBoardView.setSelectedBox(index)
                    }
                }
                launch {
                    viewModel.powerupState.collect { state ->
                        updatePowerupButtons(state)
                    }
                }
                launch {
                    viewModel.isMagnifyMode.collect { isMode ->
                        binding.tvBadgeMagnify.text = if (isMode) "✓" else viewModel.powerupState.value.reveal.toString()
                    }
                }
            }
        }
    }

    private fun updateGoldDisplay() {
        val gold = GoldManager.getGold(requireContext())
        binding.tvGameGold.text = "%,d".format(gold)
        val gems = GoldManager.getGems(requireContext())
        binding.tvGameGems.text = "%,d".format(gems)
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack(R.id.SecondFragment, false)
        }

        binding.gameBoardView.setOnBoxClickListener { id ->
            handleBoxTap(id)
        }

        binding.btnResetLevel.setOnClickListener {
            binding.gameBoardView.clearAnimations()
            viewModel.resetLevel(requireContext())
        }

        binding.btnSettings.setOnClickListener { showSettings(true) }
        binding.layoutSettings.btnCloseSettings.setOnClickListener { showSettings(false) }
        binding.layoutSettings.btnLangEn.setOnClickListener { changeLanguage("en") }
        binding.layoutSettings.btnLangVi.setOnClickListener { changeLanguage("vi") }

        setupPowerupListeners()
    }

    private fun setupPowerupListeners() {
        binding.btnRerollBags.setOnClickListener {
            val engine = viewModel.engine.value ?: return@setOnClickListener
            if (viewModel.consumeReroll(requireContext())) {
                engine.rerollBags()
                engine.archiveAllReady()
                viewModel.triggerStateUpdate()
            }
        }
        binding.btnMagnify.setOnClickListener {
            viewModel.setMagnifyMode(!viewModel.isMagnifyMode.value)
        }
        binding.btnReshuffle.setOnClickListener {
            val engine = viewModel.engine.value ?: return@setOnClickListener
            if (viewModel.consumeShuffle(requireContext())) {
                engine.shuffleAllBoxes()
                viewModel.triggerStateUpdate()
            }
        }
        // Undo button
        binding.btnUndo.setOnClickListener {
            val engine = viewModel.engine.value ?: return@setOnClickListener
            if (engine.isGameOver) return@setOnClickListener
            if (viewModel.consumeUndo(requireContext())) {
                if (engine.undoLastMove()) {
                    soundManager?.play("drop")
                    viewModel.triggerStateUpdate()
                    updateUndoBadge()
                }
            }
        }
        // Hint guide button
        binding.btnHintGuide.setOnClickListener {
            val engine = viewModel.engine.value ?: return@setOnClickListener
            if (engine.isGameOver) return@setOnClickListener
            if (viewModel.consumeHint(requireContext())) {
                val hint = engine.findBestHint()
                if (hint != null) {
                    binding.gameBoardView.showHintHighlight(hint.first, hint.second)
                    soundManager?.play("pickup")
                    updateHintBadge()
                }
            }
        }
    }

    private fun updateUndoBadge() {
        val state = viewModel.powerupState.value
        val engine = viewModel.engine.value
        val historySize = engine?.moveHistory?.size ?: 0
        binding.tvBadgeUndo.text = if (state.freeUndo > 0) "${state.freeUndo}" else if (historySize > 0) "💎" else "0"
    }

    private fun updateHintBadge() {
        val state = viewModel.powerupState.value
        binding.tvBadgeHint.text = if (state.freeHint > 0) "${state.freeHint}" else "💎"
    }

    private fun handleBoxTap(id: Int) {
        val engine = viewModel.engine.value ?: return
        if (engine.isGameOver || isAnimating) return

        val clickedBox = engine.getBoxes().find { it.id == id } ?: return
        
        if (viewModel.isMagnifyMode.value) {
            if (clickedBox.hiddenLayers > 0 && viewModel.consumeReveal(requireContext())) {
                engine.revealHiddenLayers(id)
                soundManager?.play("complete")
            }
            viewModel.setMagnifyMode(false)
            viewModel.triggerStateUpdate()
            return
        }

        // Logic for Boss Type 1 (direct pour)
        if (engine.isBossLevel && engine.currentBossType == 1) {
            val truck = engine.getBoxSlots().getOrNull(0)
            if (truck != null && !clickedBox.isEmpty() && clickedBox.peekColor() == truck.targetColor && !clickedBox.isFrozen && !clickedBox.hasCobweb) {
                // Animate pour to center truck
                val fruitColor = clickedBox.peekColor()
                val movedCount = engine.pourFruitsToTruck(id)
                
                if (movedCount > 0) {
                    isAnimating = true
                    binding.gameBoardView.animateMove(fruitColor, id, -1, movedCount)
                    binding.root.postDelayed({
                        isAnimating = false
                        viewModel.triggerStateUpdate()
                    }, 450)
                }
                return
            }
        }

        val selectedIdx = viewModel.selectedBoxIndex.value
        if (selectedIdx == null) {
            if (clickedBox.hasCobweb) {
                if (engine.clearCobweb(id)) {
                    soundManager?.play("drop")
                    viewModel.triggerStateUpdate()
                }
                return
            }
            if (!clickedBox.isEmpty() && !clickedBox.isFrozen && !clickedBox.isLockedByChain && !clickedBox.isComplete()) {
                viewModel.setSelectedBox(id)
            }
        } else if (selectedIdx == id) {
            viewModel.setSelectedBox(null)
        } else {
            val srcBox = engine.getBoxes().find { it.id == selectedIdx } ?: run {
                viewModel.setSelectedBox(null)
                return
            }
            if (engine.canMove(srcBox, clickedBox)) {
                val fruitColor = srcBox.peekColor()
                val movedCount = engine.executeMove(srcBox, clickedBox)
                
                if (movedCount > 0) {
                    binding.gameBoardView.animateMove(fruitColor, selectedIdx, id, movedCount)
                    val archivedIds = engine.archiveAllReady()
                    viewModel.setSelectedBox(null)
                    
                    // Check for combo and show popup
                    if (archivedIds.isNotEmpty() && engine.comboCounter >= 2) {
                        binding.gameBoardView.showComboPopup(engine.comboCounter)
                        // Combo bonus gold
                        val comboBonus = when {
                            engine.comboCounter >= 4 -> 50
                            engine.comboCounter >= 3 -> 25
                            engine.comboCounter >= 2 -> 10
                            else -> 0
                        }
                        if (comboBonus > 0) GoldManager.addGold(requireContext(), comboBonus)
                        // Gem bonus for x4+ combo
                        if (engine.comboCounter >= 4) GoldManager.addGems(requireContext(), GoldManager.GEM_REWARD_COMBO_4)
                    }
                    
                    // Delay state update slightly so the animation can be seen starting
                    binding.root.postDelayed({
                        viewModel.triggerStateUpdate()
                    }, 100)
                    
                    soundManager?.play("move")
                }
            } else {
                if (!clickedBox.isEmpty() && !clickedBox.isFrozen && !clickedBox.isComplete()) {
                    viewModel.setSelectedBox(id)
                } else {
                    viewModel.setSelectedBox(null)
                }
            }
        }
    }

    private fun setupUIForBossStatus(engine: LevelOneEngine) {
        if (engine.isBossLevel) {
            binding.tvLevelName.text = getString(R.string.boss_level_format, args.levelId)
            binding.tvLevelName.setTextColor(Color.RED)
        } else {
            binding.tvLevelName.text = getString(R.string.level_name_format, args.levelId)
            binding.tvLevelName.setTextColor(Color.WHITE)
        }
    }

    private fun updatePowerupButtons(state: GameViewModel.PowerupState) {
        binding.tvBadgeReroll.text = "${state.reroll}"
        binding.tvBadgeMagnify.text = if (viewModel.isMagnifyMode.value) "✓" else "${state.reveal}"
        binding.tvBadgeShuffle.text = "${state.shuffle}"
        updateUndoBadge()
        updateHintBadge()
    }

    private fun updateTrucksUI(engine: LevelOneEngine) {
        if (engine.isBagMechanismEnabled) {
            if (engine.isBossLevel && engine.currentBossType == 1) {
                binding.llBoxes.visibility = View.GONE
            } else {
                binding.llBoxes.visibility = View.VISIBLE
                val slots = engine.getBoxSlots()
                binding.truckContainerB.visibility = if (slots.size >= 2) View.VISIBLE else View.INVISIBLE
                binding.truckContainerA.visibility = if (slots.size >= 1) View.VISIBLE else View.INVISIBLE
                
                slots.getOrNull(0)?.let { updateBoxUI(binding.tvBoxAFruit, binding.tvBoxAInfo, binding.tvBoxATurns, it) }
                slots.getOrNull(1)?.let { updateBoxUI(binding.tvBoxBFruit, binding.tvBoxBInfo, binding.tvBoxBTurns, it) }
            }
        } else {
            binding.llBoxes.visibility = View.GONE
        }
    }

    private fun updateBoxUI(tvFruit: TextView, tvInfo: TextView, tvTurns: TextView, box: LevelOneEngine.BoxSlot) {
        tvFruit.text = SkinManager.getIconForColor(box.targetColor, requireContext())
        tvInfo.text = getString(R.string.bag_numeric_format, box.filled, box.capacity)
        tvTurns.text = "${box.turnsLeft}"
    }

    private fun updateStatusUI(engine: LevelOneEngine) {
        if (engine.isBossLevel && engine.currentBossType == 1) {
            binding.tvPackedProgress.visibility = View.GONE
        } else {
            binding.tvPackedProgress.visibility = View.VISIBLE
            binding.tvPackedProgress.text = if (engine.isBagMechanismEnabled) 
                getString(R.string.progress_packed, engine.completedBoxesCount, engine.totalFullBoxesCount) 
            else 
                getString(R.string.progress_completed, engine.completedBoxesCount, engine.totalFullBoxesCount)
        }
    }

    private fun checkGameResults(engine: LevelOneEngine) {
        if (hasShownResult) return
        if (engine.isGameOver) {
            hasShownResult = true
            if (engine.isWin) showWinDialog() else showLoseDialog()
        } else if (engine.isDeadlocked()) {
            hasShownResult = true
            showLoseDialog()
        }
    }

    private fun showWinDialog() {
        soundManager?.playWin()
        saveProgress()
        
        val engine = viewModel.engine.value
        val stars = engine?.calculateStars() ?: 3
        
        // Save star rating
        GoldManager.setLevelStars(requireContext(), args.levelId, stars)
        
        // Calculate gems earned
        var gemsEarned = 0
        if (stars >= 3) gemsEarned += GoldManager.GEM_REWARD_3_STARS
        if (engine?.isBossLevel == true) gemsEarned += GoldManager.GEM_REWARD_BOSS
        if (gemsEarned > 0) GoldManager.addGems(requireContext(), gemsEarned)
        
        // Update star display
        binding.layoutWinDialog.tvWinStars.text = when (stars) {
            3 -> getString(R.string.star_rating_3)
            2 -> getString(R.string.star_rating_2)
            else -> getString(R.string.star_rating_1)
        }
        
        // Show gems earned
        if (gemsEarned > 0) {
            binding.layoutWinDialog.tvWinGems.visibility = View.VISIBLE
            binding.layoutWinDialog.tvWinGems.text = getString(R.string.gems_earned, gemsEarned)
        } else {
            binding.layoutWinDialog.tvWinGems.visibility = View.GONE
        }
        
        binding.layoutWinDialog.root.visibility = View.VISIBLE
        binding.layoutWinDialog.btnWinContinue.setOnClickListener {
            GoldManager.addGold(requireContext(), 50)
            navigateToNextLevel()
        }
        binding.layoutWinDialog.btnWinWatchX3.setOnClickListener {
            binding.layoutWinDialog.btnWinWatchX3.isEnabled = false
            binding.layoutWinDialog.pbWinLoading.visibility = View.VISIBLE
            binding.layoutWinDialog.tvWinWatching.visibility = View.VISIBLE
            AdManager.showRewardedAd(
                activity = requireActivity(),
                onRewarded = {
                    if (_binding == null) return@showRewardedAd
                    GoldManager.addGold(requireContext(), 150)
                    binding.layoutWinDialog.pbWinLoading.visibility = View.GONE
                    binding.layoutWinDialog.tvWinWatching.visibility = View.GONE
                    navigateToNextLevel()
                },
                onFailed = {
                    if (_binding == null) return@showRewardedAd
                    binding.layoutWinDialog.btnWinWatchX3.isEnabled = true
                    binding.layoutWinDialog.pbWinLoading.visibility = View.GONE
                    binding.layoutWinDialog.tvWinWatching.visibility = View.GONE
                }
            )
        }
    }

    private fun saveProgress() {
        val prefs = requireContext().getSharedPreferences("game_prefs", 0)
        val current = prefs.getInt("highest_level", 1)
        val nextLevel = args.levelId + 1
        if (nextLevel > current) {
            prefs.edit().putInt("highest_level", nextLevel).apply()
        }
    }

    private fun navigateToNextLevel() {
        findNavController().navigate(
            R.id.action_LevelOneFragment_self,
            Bundle().apply { putInt("levelId", args.levelId + 1) }
        )
    }

    private fun showLoseDialog() {
        soundManager?.playLose()
        binding.layoutLoseDialog.root.visibility = View.VISIBLE
        binding.layoutLoseDialog.btnLoseRetry.setOnClickListener {
            hasShownResult = false
            binding.layoutLoseDialog.root.visibility = View.GONE
            viewModel.resetLevel(requireContext())
        }
        binding.layoutLoseDialog.btnLoseBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun showSettings(show: Boolean) {
        binding.layoutSettings.root.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun changeLanguage(langCode: String) {
        if (langCode == LanguageManager.getSavedLanguage(requireContext())) return
        LanguageManager.setLocale(requireContext(), langCode)
        activity?.recreate()
    }

    private fun loadBannerAd() {
        if (GoldManager.isVip(requireContext())) {
            binding.adContainer.visibility = View.GONE
            return
        }
        val adView = AdView(requireContext()).apply { 
            setAdSize(AdSize.BANNER)
            adUnitId = "ca-app-pub-3940256099942544/6300978111" 
        }
        binding.adContainer.addView(adView)
        adView.loadAd(AdRequest.Builder().build())
    }

    private fun setupTruckIdleAnimations() {
        ObjectAnimator.ofFloat(binding.imgTruckA, View.TRANSLATION_Y, 0f, 6f, 0f).apply {
            duration = 2000; repeatCount = -1; repeatMode = ValueAnimator.REVERSE
            runningAnimators.add(this)
        }.start()
        ObjectAnimator.ofFloat(binding.imgTruckB, View.TRANSLATION_Y, 0f, 6f, 0f).apply {
            duration = 2200; repeatCount = -1; repeatMode = ValueAnimator.REVERSE
            runningAnimators.add(this)
        }.start()
    }

    private fun playBackgroundMusic() { GlobalMusicPlayer.playIfEnabled(requireContext(), R.raw.nhacnen) }
    override fun onResume() { 
        super.onResume()
        GlobalMusicPlayer.resumeIfEnabled(requireContext())
        binding.gameBoardView.setSkinStyle(SkinManager.getSelectedStyle(requireContext()))
        updateGoldDisplay()
        viewModel.refreshPowerupCounts(requireContext())
    }
    override fun onPause() { super.onPause(); GlobalMusicPlayer.pause() }
    override fun onDestroyView() {
        super.onDestroyView()
        runningAnimators.forEach { it.cancel() }
        runningAnimators.clear()
        soundManager?.release()
        soundManager = null
        _binding = null
    }
}
