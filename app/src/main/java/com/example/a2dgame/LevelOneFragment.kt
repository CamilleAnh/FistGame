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
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.appcompat.widget.PopupMenu
import com.yourname.fruitsort.databinding.FragmentLevelOneBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import kotlin.random.Random

import com.example.a2dgame.SkinManager

class LevelOneFragment : Fragment() {

    private var _binding: FragmentLevelOneBinding? = null
    private val binding get() = _binding!!
    
    private val args: LevelOneFragmentArgs by navArgs()
    private lateinit var engine: LevelOneEngine
    private var soundManager: SoundManager? = null

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
        soundManager?.setEnabled(true)
        
        setupUIForBossStatus()
        
        loadBannerAd()
        playBackgroundMusic()
        renderBoard()

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        setupSettings()
        setupPowerups()
        updateGoldDisplay()
        setupTruckIdleAnimations()
    }

    private fun setupUIForBossStatus() {
        if (engine.isBossLevel) {
            binding.tvLevelName.text = "👹 BOSS LV ${args.levelId}"
            binding.tvLevelName.setTextColor(Color.RED)
        } else {
            binding.tvLevelName.text = getString(R.string.level_name_format, args.levelId)
            binding.tvLevelName.setTextColor(Color.WHITE)
        }
    }

    private fun updateGoldDisplay() {
        val gold = GoldManager.getGold(requireContext())
        binding.tvGameGold.text = getString(R.string.gold_display_format, gold)
    }

    private fun setupSettings() {
        val settingsBinding = binding.layoutSettings
        binding.btnSettings.setOnClickListener { anchor ->
            val popup = PopupMenu(requireContext(), anchor)
            popup.menu.add(0, 1, 0, getString(R.string.home_menu))
            popup.menu.add(0, 2, 1, getString(R.string.reset_level))
            popup.menu.add(0, 3, 2, getString(R.string.settings_menu))
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> { findNavController().popBackStack(R.id.SecondFragment, false); true }
                    2 -> { if (activeAnimationsCount > 0) return@setOnMenuItemClickListener true; engine = LevelOneEngine(args.levelId); renderBoard(); true }
                    3 -> { showSettings(true); true }
                    else -> false
                }
            }
            popup.show()
        }
        settingsBinding.btnCloseSettings.setOnClickListener { showSettings(false) }
        settingsBinding.btnLangEn.setOnClickListener { changeLanguage("en") }
        settingsBinding.btnLangVi.setOnClickListener { changeLanguage("vi") }
        val prefs = requireContext().getSharedPreferences("game_settings", android.content.Context.MODE_PRIVATE)
        settingsBinding.switchMusic.isChecked = prefs.getBoolean("music_on", true)
        settingsBinding.switchSound.isChecked = prefs.getBoolean("sound_on", true)
        settingsBinding.switchVibration.isChecked = prefs.getBoolean("vibration_on", true)
        settingsBinding.switchMusic.setOnCheckedChangeListener { _, isChecked -> prefs.edit().putBoolean("music_on", isChecked).apply(); GlobalMusicPlayer.setEnabled(requireContext(), isChecked) }
        settingsBinding.switchSound.setOnCheckedChangeListener { _, isChecked -> prefs.edit().putBoolean("sound_on", isChecked).apply(); soundManager?.setEnabled(isChecked) }
        settingsBinding.switchVibration.setOnCheckedChangeListener { _, isChecked -> prefs.edit().putBoolean("vibration_on", isChecked).apply() }
    }

    private fun changeLanguage(langCode: String) {
        if (langCode == LanguageManager.getSavedLanguage(requireContext())) return
        LanguageManager.setLocale(requireContext(), langCode)
        activity?.recreate()
    }

    private fun showSettings(show: Boolean) {
        val overlay = binding.layoutSettings.root
        overlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setupPowerups() {
        val ctx = requireContext()
        powerupReroll    = 1 + GoldManager.getRerollCount(ctx)
        powerupMagnify   = 1 + GoldManager.getRevealCount(ctx)
        powerupReshuffle = 1 + GoldManager.getShuffleCount(ctx)
        updatePowerupButtons()
        binding.btnRerollBags.setOnClickListener { if (powerupReroll > 0 && activeAnimationsCount == 0) { powerupReroll--; engine.rerollBags(); engine.archiveAllReady(); renderBoard(); updatePowerupButtons() } }
        binding.btnMagnify.setOnClickListener { if (powerupMagnify > 0 && activeAnimationsCount == 0) { isMagnifyMode = !isMagnifyMode; updatePowerupButtons() } }
        binding.btnReshuffle.setOnClickListener { if (powerupReshuffle > 0 && activeAnimationsCount == 0) { powerupReshuffle--; engine.shuffleAllBoxes(); renderBoard(); updatePowerupButtons() } }
    }

    private fun updatePowerupButtons() {
        binding.btnRerollBags.text = "🎲 x$powerupReroll"
        binding.btnMagnify.text = if (isMagnifyMode) "🔍 ✓" else "🔍 x$powerupMagnify"
        binding.btnReshuffle.text = "🔀 x$powerupReshuffle"
    }

    private fun renderBoard() {
        binding.glGameBoard.removeAllViews()
        val boxes = engine.getBoxes().filter { !it.isArchived }
        val skinStyle = SkinManager.getSelectedStyle(requireContext())
        val density   = resources.displayMetrics.density
        
        var cols = when { boxes.size <= 15 -> 5; boxes.size <= 25 -> 6; else -> 7 }
        if (engine.isBossLevel && engine.currentBossType == 1 && cols < 6) cols = 6
        
        val boxWidth = (resources.displayMetrics.widthPixels - (24 * density).toInt()) / cols
        val boxHeight = (boxWidth * 1.5f).toInt()
        val blockHeight = (boxWidth * 0.38f).toInt()
        val stepY = boxHeight + (16 * density).toInt()

        if (engine.isBagMechanismEnabled) {
            if (engine.isBossLevel && engine.currentBossType == 1) {
                binding.llBoxes.visibility = View.GONE
            } else {
                binding.llBoxes.visibility = View.VISIBLE
                val slots = engine.getBoxSlots()
                binding.truckContainerB.visibility = if (slots.size >= 2) View.VISIBLE else View.INVISIBLE
                binding.truckContainerA.visibility = if (slots.size >= 1) View.VISIBLE else View.INVISIBLE
                binding.truckContainerA.updateLayoutParams<LinearLayout.LayoutParams> { width = 0; weight = 1f; marginEnd = (4 * density).toInt() }
                binding.imgTruckA.scaleX = 1.0f; binding.imgTruckA.scaleY = 1.0f
                binding.tvBoxAFruit.textSize = 32f
                binding.tvBoxAInfo.textSize = 12f
                slots.getOrNull(0)?.let { updateBoxUI(binding.tvBoxAFruit, binding.tvBoxAInfo, binding.tvBoxATurns, it) }
                slots.getOrNull(1)?.let { updateBoxUI(binding.tvBoxBFruit, binding.tvBoxBInfo, binding.tvBoxBTurns, it) }
            }
        } else { binding.llBoxes.visibility = View.GONE }

        val isDarkSkin = ColorUtils.calculateLuminance(skinStyle.blockBgColor) < 0.5
        val blockTextColor = if (isDarkSkin) Color.WHITE else Color.BLACK

        val createBoxContainer: (LevelOneEngine.Box, Int, Int) -> Unit = { box, leftMar, topMar ->
            val boxContainer = FrameLayout(requireContext()).apply {
                tag = box.id
                layoutParams = FrameLayout.LayoutParams(boxWidth, boxHeight).apply { leftMargin = leftMar; topMargin = topMar }
                setOnClickListener { handleBoxTap(box.id) }
            }
            val boxLayout = FrameLayout(requireContext()).apply { layoutParams = FrameLayout.LayoutParams(-1, -1) }
            val boxBody = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL; gravity = Gravity.BOTTOM
                background = SkinManager.makeBoxBodyDrawable(skinStyle, density)
                setPadding((8 * density).toInt(), (2 * density).toInt(), (8 * density).toInt(), (16 * density).toInt())
                layoutParams = FrameLayout.LayoutParams(-1, -1)
            }
            val pending = pendingIncomingMap[box.id] ?: 0
            val visibleCount = (box.blocks.size - pending).coerceAtLeast(0)
            for (bIdx in 0 until visibleCount) {
                val fruit = box.blocks[bIdx]
                val isHidden = bIdx < box.hiddenLayers && !(engine.isBossLevel && engine.currentBossType == 4 && engine.selectedBoxIndex == box.id && bIdx == box.blocks.size - 1)
                val blockView = FrameLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(-1, blockHeight).apply { setMargins(2, -(blockHeight * 0.20).toInt(), 2, 0) }
                    background = if (isHidden) GradientDrawable().apply { setColor(0xCC333333.toInt()); cornerRadius = 6 * density } else SkinManager.makeBlockDrawable(skinStyle, density)
                    addView(TextView(requireContext()).apply { gravity = Gravity.CENTER; text = if (isHidden) "?" else fruit.fruitIcon; textSize = 16f; setTextColor(if (isHidden) Color.WHITE else blockTextColor); setShadowLayer(2f, 1f, 1f, 0x88000000.toInt()) })
                }
                boxBody.addView(blockView, 0)
            }
            boxLayout.addView(boxBody)
            if (box.hasCobweb) boxLayout.addView(TextView(requireContext()).apply { text = "🕸️"; textSize = 36f; gravity = Gravity.CENTER; translationZ = 10f; layoutParams = FrameLayout.LayoutParams(-1, -1) })
            if (box.isFrozen) boxLayout.addView(FrameLayout(requireContext()).apply { background = GradientDrawable().apply { setColor(0x7780D8FF.toInt()); cornerRadius = 8 * density; setStroke((2 * density).toInt(), Color.WHITE) }; translationZ = 15f; layoutParams = FrameLayout.LayoutParams(-1, -1); addView(TextView(requireContext()).apply { text = "❄️"; textSize = 24f; gravity = Gravity.CENTER }) })
            if (box.isLockedByChain) boxLayout.addView(TextView(requireContext()).apply { text = "⛓️"; textSize = 32f; gravity = Gravity.CENTER; translationZ = 20f; layoutParams = FrameLayout.LayoutParams(-1, -1) })
            boxContainer.addView(boxLayout); binding.glGameBoard.addView(boxContainer)
        }

        if (engine.isBossLevel && engine.currentBossType == 1) {
            val sw = resources.displayMetrics.widthPixels.toFloat()
            val availableWidth = sw - 24 * density
            val center_x = availableWidth / 2f
            val center_y = center_x * 1.3f // Giãn khoảng cách dọc
            val rx = center_x - boxWidth / 2f - 4 * density
            val ry = center_y - boxHeight / 2f + 16 * density // Mở rộng bán kính dọc thêm chút
            
            val megaSlot = engine.getBoxSlots().getOrNull(0)
            if (megaSlot != null) {
                val megaWidth = (boxWidth * 2.5f).toInt()
                val megaHeight = (boxWidth * 2.5f).toInt()
                val megaContainer = FrameLayout(requireContext()).apply {
                    tag = "mega_container"
                    layoutParams = FrameLayout.LayoutParams(megaWidth, megaHeight).apply {
                        leftMargin = (center_x - megaWidth / 2f + 12 * density).toInt()
                        topMargin = (center_y - megaHeight / 2f).toInt()
                    }
                }
                val megaBody = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
                    background = SkinManager.makeBoxBodyDrawable(skinStyle, density)
                    layoutParams = FrameLayout.LayoutParams(-1, -1)
                }
                megaBody.addView(TextView(requireContext()).apply { text = megaSlot.targetColor.fruitIcon; textSize = 64f; gravity = Gravity.CENTER; setShadowLayer(10f, 0f, 0f, Color.argb(100,0,0,0)) })
                megaBody.addView(TextView(requireContext()).apply { text = "${megaSlot.filled} / ${megaSlot.capacity}"; textSize = 28f; setTextColor(Color.WHITE); setTypeface(null, android.graphics.Typeface.BOLD); gravity = Gravity.CENTER; setShadowLayer(8f, 2f, 2f, Color.BLACK) })
                megaBody.addView(TextView(requireContext()).apply { text = "⏳ ${megaSlot.turnsLeft}"; textSize = 20f; setTextColor(Color.YELLOW); setTypeface(null, android.graphics.Typeface.BOLD); gravity = Gravity.CENTER; setShadowLayer(8f, 2f, 2f, Color.BLACK) })
                megaContainer.addView(megaBody)
                binding.glGameBoard.addView(megaContainer)
            }
            boxes.forEachIndexed { index, box ->
                val angle = index.toDouble() * 2.0 * Math.PI / boxes.size
                var x = center_x + rx * Math.cos(angle)
                var y = center_y + ry * Math.sin(angle)
                createBoxContainer(box, (x - boxWidth / 2f + 12 * density).toInt(), (y - boxHeight / 2f).toInt())
            }
        } else {
            var currentIdx = 0
            val rows = (boxes.size + cols - 1) / cols
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (currentIdx >= boxes.size) break
                    val box = boxes[currentIdx++]
                    createBoxContainer(box, (12 * density).toInt() + (c * boxWidth), r * stepY)
                }
            }
        }
        updateStatusUI()
    }

    private fun handleBoxTap(index: Int) {
        if (engine.isGameOver || animatingBoxes.contains(index)) return
        val clickedBox = engine.getBoxes().find { it.id == index } ?: return
        if (isMagnifyMode) { if (clickedBox.hiddenLayers > 0) { powerupMagnify--; engine.revealHiddenLayers(index); soundManager?.play("complete") }; isMagnifyMode = false; updatePowerupButtons(); renderBoard(); return }
        
        // --- LOGIC MEGA TRUCK DIRECT POUR ---
        if (engine.isBossLevel && engine.currentBossType == 1) {
            val truck = engine.getBoxSlots().getOrNull(0)
            if (truck != null && !clickedBox.isEmpty() && clickedBox.peekColor() == truck.targetColor && !clickedBox.isFrozen && !clickedBox.isLockedByChain && !clickedBox.hasCobweb) {
                animatePourToTruck(index)
                return
            }
        }

        val selectedIdx = engine.selectedBoxIndex
        if (selectedIdx == null) {
            if (clickedBox.hasCobweb) { clickedBox.hasCobweb = false; renderBoard(); return }
            if (!clickedBox.isEmpty() && !clickedBox.isFrozen && !clickedBox.isLockedByChain && !clickedBox.isComplete()) { 
                engine.selectedBoxIndex = index
                animateSelection(binding.glGameBoard.findViewWithTag(index), true)
                // For Type 4 blind mode, picking up reveals the fruit, so we re-render
                if (engine.isBossLevel && engine.currentBossType == 4) renderBoard()
            }
        } else if (selectedIdx == index) { 
            engine.selectedBoxIndex = null
            animateSelection(binding.glGameBoard.findViewWithTag(index), false)
            if (engine.isBossLevel && engine.currentBossType == 4) renderBoard()
        }
        else { 
            val srcBox = engine.getBoxes().find { it.id == selectedIdx }!!
            if (engine.canMove(srcBox, clickedBox)) {
                animateMoveSequence(selectedIdx, index) 
            } else { 
                animateSelection(binding.glGameBoard.findViewWithTag(selectedIdx), false)
                if (!clickedBox.isEmpty() && !clickedBox.isFrozen && !clickedBox.hasCobweb && !clickedBox.isLockedByChain && !clickedBox.isComplete()) { 
                    engine.selectedBoxIndex = index
                    animateSelection(binding.glGameBoard.findViewWithTag(index), true) 
                } else {
                    engine.selectedBoxIndex = null 
                }
                if (engine.isBossLevel && engine.currentBossType == 4) renderBoard()
            } 
        }
    }

    private fun animatePourToTruck(srcId: Int) {
        activeAnimationsCount++; animatingBoxes.add(srcId)
        val srcView = binding.glGameBoard.findViewWithTag<ViewGroup>(srcId)
        val srcBody = (srcView?.getChildAt(0) as? ViewGroup)?.getChildAt(0) as? ViewGroup
        if (srcBody == null) { activeAnimationsCount--; return }
        
        val count = engine.pourFruitsToTruck(srcId)
        if (count == 0) { activeAnimationsCount--; animatingBoxes.remove(srcId); return }
        
        val movingViews = mutableListOf<View>()
        for (i in 0 until count) movingViews.add(srcBody.getChildAt(i))
        
        val rootLoc = IntArray(2); binding.root.getLocationOnScreen(rootLoc)
        val moveAnimators = mutableListOf<Animator>(); val density = resources.displayMetrics.density
        
        val targetX: Float
        val targetY: Float
        val targetScale: Float
        val megaContainer = binding.glGameBoard.findViewWithTag<View>("mega_container")
        if (megaContainer != null) {
            val megaLoc = IntArray(2); megaContainer.getLocationOnScreen(megaLoc)
            targetX = (megaLoc[0] - rootLoc[0]).toFloat() + (megaContainer.width / 2f)
            targetY = (megaLoc[1] - rootLoc[1]).toFloat() + (megaContainer.height / 2f)
            targetScale = 0f // Make it shrink into the center
            
            // Wiggle mega container at the end
            megaContainer.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).withEndAction {
                soundManager?.play("complete") // Vibration handled here or separately
                megaContainer.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
            }.startDelay = 600
            
            // Use vibration
            val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator?.vibrate(100)
            }
        } else {
            val truckLoc = IntArray(2); binding.imgTruckA.getLocationOnScreen(truckLoc)
            targetX = (truckLoc[0] - rootLoc[0]).toFloat() + (binding.imgTruckA.width / 2f)
            targetY = (truckLoc[1] - rootLoc[1]).toFloat() + (binding.imgTruckA.height / 2f)
            targetScale = 0f
        }

        soundManager?.play("move")
        movingViews.forEachIndexed { i, block ->
            val blockLoc = IntArray(2); block.getLocationOnScreen(blockLoc)
            val w = block.width; val h = block.height; (block.parent as ViewGroup).removeView(block); (binding.root as ViewGroup).addView(block, ViewGroup.LayoutParams(w, h))
            block.x = (blockLoc[0] - rootLoc[0]).toFloat(); block.y = (blockLoc[1] - rootLoc[1]).toFloat()
            val destX = targetX - (w / 2f)
            val destY = targetY - (h / 2f)
            val path = Path().apply { moveTo(block.x, block.y); quadTo((block.x + destX) / 2, (destY) - 200 * density, destX, destY) }
            moveAnimators.add(ObjectAnimator.ofFloat(block, View.X, View.Y, path).apply { duration = 500 + i * 80L; interpolator = DecelerateInterpolator() })
            moveAnimators.add(ObjectAnimator.ofFloat(block, View.ALPHA, 1f, 0f).apply { duration = 500 + i * 80L; startDelay = 200L })
            moveAnimators.add(ObjectAnimator.ofFloat(block, View.SCALE_X, 1f, targetScale).apply { duration = 500 + i * 80L; startDelay = 100L })
            moveAnimators.add(ObjectAnimator.ofFloat(block, View.SCALE_Y, 1f, targetScale).apply { duration = 500 + i * 80L; startDelay = 100L })
        }
        
        AnimatorSet().apply {
            playTogether(moveAnimators)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    movingViews.forEach { (binding.root as ViewGroup).removeView(it) }
                    animatingBoxes.remove(srcId); activeAnimationsCount--
                    renderBoard(); checkGameResults()
                }
            })
            start()
        }
    }

    private fun animateSelection(boxView: View?, isSelected: Boolean) {
        boxView?.animate()?.translationZ(if (isSelected) 20 * resources.displayMetrics.density else 0f)?.scaleX(if (isSelected) 1.1f else 1.0f)?.scaleY(if (isSelected) 1.1f else 1.0f)?.setDuration(250)?.start()
    }

    private fun animateMoveSequence(srcId: Int, dstId: Int) {
        activeAnimationsCount++; animatingBoxes.add(srcId); animatingBoxes.add(dstId); engine.selectedBoxIndex = null
        val srcView = binding.glGameBoard.findViewWithTag<ViewGroup>(srcId); val dstView = binding.glGameBoard.findViewWithTag<ViewGroup>(dstId)
        val srcBody = (srcView?.getChildAt(0) as? ViewGroup)?.getChildAt(0) as? ViewGroup
        if (srcBody == null || dstView == null) { activeAnimationsCount--; renderBoard(); return }
        val srcBox = engine.getBoxes().find { it.id == srcId }!!; val dstBox = engine.getBoxes().find { it.id == dstId }!!
        val movingViews = mutableListOf<View>(); val color = srcBox.peekColor()
        for (i in 0 until srcBody.childCount) {
            val idx = srcBox.blocks.size - 1 - i
            if (idx >= srcBox.hiddenLayers && srcBox.blocks[idx] == color && (dstBox.blocks.size + movingViews.size) < dstBox.capacity) movingViews.add(srcBody.getChildAt(i)) else break
        }
        if (movingViews.isEmpty()) { activeAnimationsCount--; return }
        pendingIncomingMap[dstId] = (pendingIncomingMap[dstId] ?: 0) + movingViews.size; engine.executeMove(srcBox, dstBox)
        val rootLoc = IntArray(2); binding.root.getLocationOnScreen(rootLoc)
        val dstLoc = IntArray(2); dstView.getLocationOnScreen(dstLoc)
        val moveAnimators = mutableListOf<Animator>(); val density = resources.displayMetrics.density
        movingViews.forEachIndexed { i, block ->
            val blockLoc = IntArray(2); block.getLocationOnScreen(blockLoc)
            val w = block.width; val h = block.height; (block.parent as ViewGroup).removeView(block); (binding.root as ViewGroup).addView(block, ViewGroup.LayoutParams(w, h))
            block.x = (blockLoc[0] - rootLoc[0]).toFloat(); block.y = (blockLoc[1] - rootLoc[1]).toFloat()
            val targetX = (dstLoc[0] - rootLoc[0]).toFloat() + (dstView.width - w) / 2f
            val targetY = (dstLoc[1] - rootLoc[1]).toFloat() + dstView.height - (20 * density) - (dstBox.blocks.size - movingViews.size + (movingViews.size - 1 - i) + 1) * h
            val path = Path().apply { moveTo(block.x, block.y); quadTo((block.x + targetX) / 2, (dstLoc[1] - rootLoc[1]).toFloat() - 150 * density, targetX, targetY) }
            moveAnimators.add(ObjectAnimator.ofFloat(block, View.X, View.Y, path).apply { duration = 400 + i * 50L; interpolator = AnticipateOvershootInterpolator(0.8f) })
        }
        AnimatorSet().apply { playTogether(moveAnimators); addListener(object : AnimatorListenerAdapter() { override fun onAnimationEnd(animation: Animator) { movingViews.forEach { (binding.root as ViewGroup).removeView(it) }; pendingIncomingMap[dstId] = (pendingIncomingMap[dstId] ?: 0) - movingViews.size; finalizeMove(srcId, dstId) } }); start() }
    }

    private fun finalizeMove(srcId: Int, dstId: Int) {
        val box = engine.getBoxes().find { it.id == dstId }!!
        if (box.isComplete() && (pendingIncomingMap[dstId] ?: 0) == 0) { renderBoard(); playCompletionAnimation(binding.glGameBoard.findViewWithTag(dstId), dstId, srcId) }
        else { engine.archiveAllReady(); animatingBoxes.remove(srcId); animatingBoxes.remove(dstId); activeAnimationsCount--; renderBoard(); checkGameResults() }
    }

    private fun playCompletionAnimation(view: View, boxId: Int, srcBoxId: Int) {
        view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(500).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(200).withEndAction {
                val box = engine.getBoxes().find { it.id == boxId }; val color = box?.blocks?.firstOrNull()
                val bag = engine.getBoxSlots().find { it.targetColor == color && it.remaining() > 0 }
                val truckView = if (bag != null && bag.filled + 1 >= bag.capacity) (if (engine.getBoxSlots().indexOf(bag) == 0) binding.truckContainerA else binding.truckContainerB) else null
                engine.archiveBox(boxId); engine.archiveAllReady(); animatingBoxes.remove(srcBoxId); animatingBoxes.remove(boxId)
                if (truckView != null && truckView.visibility == View.VISIBLE) animateTruckCompletion(truckView) { activeAnimationsCount--; renderBoard(); checkGameResults() }
                else { activeAnimationsCount--; renderBoard(); checkGameResults() }
            }.start()
        }.start()
        spawnParticles(view)
    }

    private fun animateTruckCompletion(truckView: View, onEnd: () -> Unit) {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        ObjectAnimator.ofFloat(truckView, View.TRANSLATION_X, 0f, screenWidth).apply { duration = 700; interpolator = AnticipateOvershootInterpolator(); addListener(object : AnimatorListenerAdapter() { override fun onAnimationEnd(animation: Animator) { truckView.translationX = -screenWidth; onEnd(); ObjectAnimator.ofFloat(truckView, View.TRANSLATION_X, -screenWidth, 0f).apply { duration = 800; interpolator = OvershootInterpolator(); start() } } }); start() }
    }

    private fun spawnParticles(anchor: View) {
        val root = binding.root as ViewGroup; val loc = IntArray(2); anchor.getLocationOnScreen(loc)
        repeat(15) { val p = TextView(requireContext()).apply { text = "✨"; textSize = 24f }; root.addView(p); p.x = loc[0] + anchor.width / 2f; p.y = loc[1] + anchor.height / 2f; p.animate().translationXBy(Random.nextInt(-300, 300).toFloat()).translationYBy(Random.nextInt(-300, 300).toFloat()).alpha(0f).scaleX(2f).scaleY(2f).setDuration(1000).withEndAction { root.removeView(p) }.start() }
    }

    private fun checkGameResults() { if (engine.isWin) showWinDialog() else if (engine.isGameOver || engine.isDeadlocked()) showLoseDialog() }
    private fun showWinDialog() { soundManager?.playWin(); isWinDialogShowing = true; binding.layoutWinDialog.root.visibility = View.VISIBLE; binding.layoutWinDialog.btnWinContinue.setOnClickListener { GoldManager.addGold(requireContext(), if (engine.isBossLevel) 150 else 50); navigateToNextLevel() } }
    private fun showLoseDialog() { soundManager?.playLose() ; isLoseDialogShowing = true; binding.layoutLoseDialog.root.visibility = View.VISIBLE; binding.layoutLoseDialog.btnLoseRetry.setOnClickListener { activity?.recreate() }; binding.layoutLoseDialog.btnLoseBack.setOnClickListener { findNavController().popBackStack() } }
    private fun navigateToNextLevel() { findNavController().navigate(R.id.action_LevelOneFragment_self, Bundle().apply { putInt("levelId", args.levelId + 1) }) }
    private fun updateStatusUI() { 
        if (engine.isBossLevel && engine.currentBossType == 1) {
            binding.tvPackedProgress.visibility = View.GONE
        } else {
            binding.tvPackedProgress.visibility = View.VISIBLE
            binding.tvPackedProgress.text = if (engine.isBagMechanismEnabled) getString(R.string.progress_packed, engine.completedBoxesCount, engine.totalFullBoxesCount) else getString(R.string.progress_completed, engine.completedBoxesCount, engine.totalFullBoxesCount) 
            binding.tvPackedProgress.setTextColor(Color.WHITE)
            binding.tvPackedProgress.setShadowLayer(8f, 2f, 2f, Color.BLACK)
        }
    }
    private fun updateBoxUI(tvFruit: TextView, tvInfo: TextView, tvTurns: TextView, box: LevelOneEngine.BoxSlot) { tvFruit.text = box.targetColor.fruitIcon; tvInfo.text = getString(R.string.bag_numeric_format, box.filled, box.capacity); tvTurns.text = "${box.turnsLeft}" }
    private fun loadBannerAd() { if (GoldManager.isVip(requireContext())) { binding.adContainer.visibility = View.GONE; return }; val adView = AdView(requireContext()).apply { setAdSize(AdSize.BANNER); adUnitId = "ca-app-pub-3940256099942544/6300978111" }; binding.adContainer.addView(adView); adView.loadAd(AdRequest.Builder().build()) }
    private fun setupTruckIdleAnimations() { val bA = ObjectAnimator.ofFloat(binding.imgTruckA, View.TRANSLATION_Y, 0f, 6f, 0f).apply { duration = 2000; repeatCount = ObjectAnimator.INFINITE; repeatMode = ObjectAnimator.REVERSE }; val bB = ObjectAnimator.ofFloat(binding.imgTruckB, View.TRANSLATION_Y, 0f, 6f, 0f).apply { duration = 2200; repeatCount = ObjectAnimator.INFINITE; repeatMode = ObjectAnimator.REVERSE }; bA.start(); bB.start() }
    private fun playBackgroundMusic() { GlobalMusicPlayer.playIfEnabled(requireContext(), R.raw.nhacnen) }
    override fun onResume() { super.onResume(); GlobalMusicPlayer.resumeIfEnabled(requireContext()) }
    override fun onPause() { super.onPause(); GlobalMusicPlayer.pause() }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
