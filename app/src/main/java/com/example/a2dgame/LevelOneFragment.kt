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
        soundManager?.setEnabled(true)
        
        binding.tvLevelName.text = getString(R.string.level_name_format, levelId)
        
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

        val prefs = requireContext().getSharedPreferences("game_settings", android.content.Context.MODE_PRIVATE)
        
        settingsBinding.switchMusic.isChecked = prefs.getBoolean("music_on", true)
        settingsBinding.switchSound.isChecked = prefs.getBoolean("sound_on", true)
        settingsBinding.switchVibration.isChecked = prefs.getBoolean("vibration_on", true)

        settingsBinding.switchMusic.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("music_on", isChecked).apply()
            GlobalMusicPlayer.setEnabled(requireContext(), isChecked)
        }

        settingsBinding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("sound_on", isChecked).apply()
            soundManager?.setEnabled(isChecked)
        }

        settingsBinding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibration_on", isChecked).apply()
        }

        val btnResetAll = settingsBinding.root.findViewById<android.widget.Button>(R.id.btn_reset_all)
        btnResetAll?.setOnClickListener {
            settingsBinding.switchMusic.isChecked = true
            settingsBinding.switchSound.isChecked = true
            settingsBinding.switchVibration.isChecked = true
        }
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
            engine.archiveAllReady()
            soundManager?.play("pickup")
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
            soundManager?.play("move")
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
        val totalBoxes = boxes.size
        
        val cols = when {
            totalBoxes <= 15 -> 5
            totalBoxes <= 25 -> 6
            else -> 7
        }
        
        val screenWidth = resources.displayMetrics.widthPixels
        val sideMargins = (24 * resources.displayMetrics.density).toInt()
        val boxWidth = (screenWidth - sideMargins) / cols
        
        // Tăng chiều cao hộp rõ rệt theo yêu cầu
        val boxHeight = (boxWidth * 1.5f).toInt()
        val blockHeight = (boxWidth * 0.38f).toInt()
        
        // Logic Honeycomb nguyên bản (So le hàng chẵn hàng lẻ)
        val verticalGap = (16 * resources.displayMetrics.density).toInt()
        val stepY = boxHeight + verticalGap
        val narrowCols = cols - 1
        
        val rowLengths = mutableListOf<Int>()
        var remaining = totalBoxes
        var isWideRow = true
        while (remaining > 0) {
            val c = if (isWideRow) cols else narrowCols
            val toTake = minOf(remaining, c)
            rowLengths.add(toTake)
            remaining -= toTake
            isWideRow = !isWideRow
        }

        if (engine.isBagMechanismEnabled) {
            val slots = engine.getBoxSlots()
            slots.getOrNull(0)?.let { updateBoxUI(binding.tvBoxAFruit, binding.tvBoxAInfo, binding.tvBoxATurns, it) } 
                ?: run { binding.truckContainerA.visibility = View.INVISIBLE }
            slots.getOrNull(1)?.let { updateBoxUI(binding.tvBoxBFruit, binding.tvBoxBInfo, binding.tvBoxBTurns, it) } 
                ?: run { binding.truckContainerB.visibility = View.INVISIBLE }
        }

        var currentBoxIndex = 0
        rowLengths.forEachIndexed { rowIndex, rowItemCount ->
            val rowWidth = rowItemCount * boxWidth
            val leftOffset = (screenWidth - sideMargins - rowWidth) / 2
            val topOffset = rowIndex * stepY
            
            for (i in 0 until rowItemCount) {
                val box = boxes[currentBoxIndex]
                
                val boxContainer = FrameLayout(requireContext()).apply {
                    tag = box.id
                    clipChildren = false
                    clipToPadding = false
                    layoutParams = FrameLayout.LayoutParams(boxWidth, boxHeight).apply {
                        leftMargin = leftOffset + (i * boxWidth)
                        topMargin = topOffset
                    }
                    setOnClickListener { handleBoxTap(box.id) }
                }

                val boxLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.BOTTOM
                    clipChildren = false
                    clipToPadding = false
                    background = ContextCompat.getDrawable(context, R.drawable.isometric_box_bg)
                    setPadding(
                        (8 * resources.displayMetrics.density).toInt(),
                        (2 * resources.displayMetrics.density).toInt(),
                        (8 * resources.displayMetrics.density).toInt(),
                        (16 * resources.displayMetrics.density).toInt()
                    )
                    layoutParams = FrameLayout.LayoutParams(-1, -1)
                }

                val pending = pendingIncomingMap[box.id] ?: 0
                val visibleCount = (box.blocks.size - pending).coerceAtLeast(0)
                
                for (bIdx in 0 until visibleCount) {
                    val fruit = box.blocks[bIdx]
                    val isHidden = bIdx < box.hiddenLayers
                    val blockView = FrameLayout(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(-1, blockHeight).apply { 
                            setMargins(2, - (blockHeight * 0.20).toInt(), 2, 0) 
                        }
                        background = if (isHidden) GradientDrawable().apply { 
                            setColor(0xCC333333.toInt())
                            cornerRadius = 6 * resources.displayMetrics.density
                        } else ContextCompat.getDrawable(context, R.drawable.item_fruit_box)
                        
                        addView(TextView(context).apply {
                            gravity = Gravity.CENTER
                            text = if (isHidden) "?" else fruit.fruitIcon
                            textSize = 16f
                            setTextColor(if (isHidden) Color.WHITE else Color.BLACK)
                            setShadowLayer(2f, 1f, 1f, 0x88000000.toInt())
                        })
                    }
                    boxLayout.addView(blockView, 0)
                }
                
                if (box.isFrozen) {
                    boxLayout.foreground = GradientDrawable().apply {
                        setColor(0x4400BCD4.toInt())
                        cornerRadius = 8 * resources.displayMetrics.density
                    }
                } else {
                    boxLayout.foreground = null
                }

                boxContainer.addView(boxLayout)
                binding.glGameBoard.addView(boxContainer)
                currentBoxIndex++
            }
        }

        if (engine.isBagMechanismEnabled) {
            val slots = engine.getBoxSlots()
            slots.getOrNull(0)?.let { updateBoxUI(binding.tvBoxAFruit, binding.tvBoxAInfo, binding.tvBoxATurns, it) } 
                ?: run { binding.truckContainerA.visibility = View.INVISIBLE }
            slots.getOrNull(1)?.let { updateBoxUI(binding.tvBoxBFruit, binding.tvBoxBInfo, binding.tvBoxBTurns, it) } 
                ?: run { binding.truckContainerB.visibility = View.INVISIBLE }
        }
        
        updateStatusUI()
    }

    private fun handleBoxTap(index: Int) {
        if (engine.isGameOver || animatingBoxes.contains(index)) return
        if (isMagnifyMode) {
            val box = engine.getBoxes().find { it.id == index }
            if (box != null && !box.isArchived && box.hiddenLayers > 0) {
                powerupMagnify--
                engine.revealHiddenLayers(index)
                soundManager?.play("complete")
            }
            isMagnifyMode = false
            updatePowerupButtons()
            renderBoard()
            return
        }

        val boxes = engine.getBoxes()
        val clickedBox = boxes.find { it.id == index } ?: return
        val selectedIdx = engine.selectedBoxIndex

        if (selectedIdx == null) {
            if (!clickedBox.isEmpty() && !clickedBox.isFrozen && !clickedBox.hasCobweb && !clickedBox.isComplete()) {
                engine.selectedBoxIndex = index
                soundManager?.play("pickup")
                val view = binding.glGameBoard.findViewWithTag<View>(index)
                animateSelection(view, true)
            }
        } else if (selectedIdx == index) {
            engine.selectedBoxIndex = null
            soundManager?.play("drop")
            val view = binding.glGameBoard.findViewWithTag<View>(index)
            animateSelection(view, false)
        } else {
            val srcBox = boxes.find { it.id == selectedIdx }!!
            if (engine.canMove(srcBox, clickedBox)) {
                animateMoveSequence(selectedIdx, index)
            } else {
                val oldView = binding.glGameBoard.findViewWithTag<View>(selectedIdx)
                animateSelection(oldView, false)
                
                if (!clickedBox.isEmpty() && !clickedBox.isFrozen && !clickedBox.hasCobweb && !clickedBox.isComplete()) {
                    engine.selectedBoxIndex = index
                    soundManager?.play("pickup")
                    val view = binding.glGameBoard.findViewWithTag<View>(index)
                    animateSelection(view, true)
                } else {
                    engine.selectedBoxIndex = null
                    soundManager?.play("drop")
                }
            }
        }
    }

    private fun animateSelection(boxView: View?, isSelected: Boolean) {
        val boxLayout = (boxView as? ViewGroup)?.getChildAt(0) as? ViewGroup ?: return
        val density = resources.displayMetrics.density
        
        // Vô hiệu hóa clipping cho toàn bộ cây thư mục cha
        var p = boxView.parent
        while (p is ViewGroup) {
            p.clipChildren = false
            p.clipToPadding = false
            if (p.id == android.R.id.content) break // Đã tới root content
            p = p.parent
        }
        val boxId = boxView.tag as Int
        val box = engine.getBoxes().find { it.id == boxId } ?: return
        val color = box.peekColor()

        var consecutiveCount = 0
        for (idx in box.blocks.indices.reversed()) {
            if (idx < box.hiddenLayers) break
            if (box.blocks[idx] == color) consecutiveCount++
            else break
        }
        val topStackMinIndex = box.blocks.size - consecutiveCount

        for (i in 0 until boxLayout.childCount) {
            val block = boxLayout.getChildAt(i)
            val blockIndex = box.blocks.size - 1 - i
            
            val isPartOfTopStack = blockIndex >= 0 &&
                                   blockIndex >= topStackMinIndex &&
                                   blockIndex >= box.hiddenLayers
            
            if (isPartOfTopStack) {
                if (isSelected) {
                    block.animate()
                        .translationY(-35 * density) 
                        .translationZ(30 * density)
                        .scaleX(1.15f)
                        .scaleY(1.15f)
                        .setDuration(250)
                        .setInterpolator(OvershootInterpolator())
                        .withEndAction { startWiggle(block) }
                        .start()
                } else {
                    stopWiggle(block)
                    block.animate()
                        .translationY(0f)
                        .translationZ(0f)
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(200)
                        .start()
                }
            } else if (!isSelected) {
                stopWiggle(block)
                block.animate()
                    .translationY(0f)
                    .translationZ(0f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
            }
        }
        
        // Nâng toàn bộ hộp lên theo trục Z (3D Lift)
        boxView.animate()
            .translationZ(if (isSelected) 20 * density else 0f)
            .scaleX(if (isSelected) 1.1f else 1.0f)
            .scaleY(if (isSelected) 1.1f else 1.0f)
            .setDuration(300)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun startWiggle(view: View) {
        if (wiggleAnimators.containsKey(view)) return
        val anim = ObjectAnimator.ofFloat(view, View.ROTATION, -3f, 3f).apply {
            duration = 150
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }
        wiggleAnimators[view] = anim
    }

    private fun stopWiggle(view: View) {
        wiggleAnimators.remove(view)?.cancel()
        view.rotation = 0f
    }

    private fun animateMoveSequence(srcId: Int, dstId: Int) {
        activeAnimationsCount++
        animatingBoxes.add(srcId)
        animatingBoxes.add(dstId)
        engine.selectedBoxIndex = null
        
        val srcView = binding.glGameBoard.findViewWithTag<ViewGroup>(srcId)
        val dstView = binding.glGameBoard.findViewWithTag<ViewGroup>(dstId)
        val srcLayout = srcView?.getChildAt(0) as? ViewGroup
        val dstLayout = dstView?.getChildAt(0) as? ViewGroup
        
        if (srcLayout == null || dstLayout == null || srcView == null || dstView == null) {
            animatingBoxes.remove(srcId)
            animatingBoxes.remove(dstId)
            activeAnimationsCount--
            renderBoard()
            return
        }

        val srcBox = engine.getBoxes().find { it.id == srcId }!!
        val dstBox = engine.getBoxes().find { it.id == dstId }!!
        val color = srcBox.peekColor()
        
        val movingViews = mutableListOf<View>()
        for (i in 0 until srcLayout.childCount) {
            // blockIndex trong box logic *hiß╗çn tß║íi* tr╞░ß╗¢c khi modify (d├╣ sao ta mß╗¢i lß║Ñy childCount)
            val blockIndex = srcBox.blocks.size - 1 - i
            if (blockIndex >= 0 &&
                blockIndex >= srcBox.hiddenLayers &&
                srcBox.blocks[blockIndex] == color &&
                (dstBox.blocks.size + movingViews.size) < dstBox.capacity) {
                movingViews.add(srcLayout.getChildAt(i))
            } else if (blockIndex >= 0 && srcBox.blocks[blockIndex] != color) {
                // Dß╗½ng ngay khi gß║╖p block kh├íc m├áu
                break
            }
        }
        
        val count = movingViews.size
        if (count == 0) {
            animatingBoxes.remove(srcId)
            animatingBoxes.remove(dstId)
            activeAnimationsCount--
            return
        }

        // IMMEDIATE LOGIC UPDATE & PENDING COUNT
        pendingIncomingMap[dstId] = (pendingIncomingMap[dstId] ?: 0) + count
        engine.executeMove(srcBox, dstBox)

        val rootLoc = IntArray(2)
        binding.root.getLocationOnScreen(rootLoc)
        
        val srcLoc = IntArray(2)
        val dstLoc = IntArray(2)
        srcView.getLocationOnScreen(srcLoc)
        dstView.getLocationOnScreen(dstLoc)
        
        val moveAnimators = mutableListOf<Animator>()
        val density = resources.displayMetrics.density
        
        soundManager?.play("move")

        // === RESET TRANSFORMS NGAY Lß║¼P Tß╗¿C ─æß╗â tr├ính ghost/b├│ng mß╗¥ ===
        // 1. Reset scale cß╗ºa boxLayout nguß╗ôn vß╗ü 1.0 ngay (kh├┤ng animate)
        srcLayout.animate().cancel()
        srcLayout.scaleX = 1.0f
        srcLayout.scaleY = 1.0f
        // 2. Reset transform cß╗ºa c├íc block C├ÆN Lß║áI trong srcLayout (kh├┤ng bay)
        for (i in 0 until srcLayout.childCount) {
            val remaining = srcLayout.getChildAt(i)
            if (!movingViews.contains(remaining)) {
                remaining.animate().cancel()
                remaining.translationY = 0f
                remaining.scaleX = 1.0f
                remaining.scaleY = 1.0f
                remaining.rotation = 0f
                remaining.alpha = 1.0f
            }
        }

        movingViews.forEachIndexed { index, block ->
            val blockLoc = IntArray(2)
            block.getLocationOnScreen(blockLoc)
            
            val originalWidth = block.width
            val originalHeight = block.height
            
            stopWiggle(block)
            val parent = block.parent as ViewGroup
            parent.removeView(block)
            
            val lp = ViewGroup.LayoutParams(originalWidth, originalHeight)
            (binding.root as ViewGroup).addView(block, lp)
            
            // Reset transform tr├¬n block ─æang bay tr╞░ß╗¢c khi ─æß║╖t vß╗ï tr├¡ tuyß╗çt ─æß╗æi
            block.scaleX = 1.0f
            block.scaleY = 1.0f
            block.rotation = 0f
            block.translationX = 0f
            block.translationY = 0f
            block.alpha = 1.0f
            block.x = (blockLoc[0] - rootLoc[0]).toFloat()
            block.y = (blockLoc[1] - rootLoc[1]).toFloat()
            
            val targetX = (dstLoc[0] - rootLoc[0]).toFloat() + (dstView.width - originalWidth) / 2f
            val targetSlotIndex = dstBox.blocks.size - count + (count - 1 - index)
            val paddingBottom = 20 * density
            val overlapAdjustment = targetSlotIndex * 4 * density
            val targetY = (dstLoc[1] - rootLoc[1]).toFloat() + dstView.height - paddingBottom - (targetSlotIndex + 1) * originalHeight + overlapAdjustment

            val path = Path().apply {
                moveTo(block.x, block.y)
                val arcHeight = 150 * density
                quadTo((block.x + targetX) / 2, (dstLoc[1] - rootLoc[1]).toFloat() - arcHeight, targetX, targetY)
            }
            
            val animator = ObjectAnimator.ofFloat(block, View.X, View.Y, path).apply {
                duration = 400 + index * 60L
                interpolator = AnticipateOvershootInterpolator(0.8f)
            }
            
            // Bay kèm hiệu ứng xoay và scale
            val scaleAnimX = ObjectAnimator.ofFloat(block, View.SCALE_X, 1.0f, 1.3f, 1.0f).apply { duration = 400 }
            val scaleAnimY = ObjectAnimator.ofFloat(block, View.SCALE_Y, 1.0f, 1.3f, 1.0f).apply { duration = 400 }
            val rotateAnim = ObjectAnimator.ofFloat(block, View.ROTATION, 0f, 10f, -10f, 0f).apply { duration = 400 }
            
            moveAnimators.add(animator)
            moveAnimators.add(scaleAnimX)
            moveAnimators.add(scaleAnimY)
            moveAnimators.add(rotateAnim)
        }
        
        android.animation.AnimatorSet().apply {
            playTogether(moveAnimators)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    movingViews.forEach { (binding.root as ViewGroup).removeView(it) }
                    
                    val currentPending = pendingIncomingMap[dstId] ?: 0
                    if (currentPending >= count) {
                        pendingIncomingMap[dstId] = currentPending - count
                    } else {
                        pendingIncomingMap[dstId] = 0
                    }
                    
                    val finalDstBox = engine.getBoxes().find { it.id == dstId }!!
                    
                    if (finalDstBox.isComplete() && (pendingIncomingMap[dstId] ?: 0) == 0) {
                        renderBoard()
                        val newDstView = binding.glGameBoard.findViewWithTag<View>(dstId)
                        if (newDstView != null) {
                            playCompletionAnimation(newDstView, dstId, srcId)
                        } else {
                            engine.archiveBox(dstId)
                            engine.archiveAllReady()
                            animatingBoxes.remove(srcId)
                            animatingBoxes.remove(dstId)
                            activeAnimationsCount--
                            renderBoard()
                            checkGameResults()
                        }
                    } else {
                        engine.archiveAllReady()
                        soundManager?.play("drop")
                        animatingBoxes.remove(srcId)
                        animatingBoxes.remove(dstId)
                        activeAnimationsCount--
                        renderBoard()
                        val updatedDstView = binding.glGameBoard.findViewWithTag<View>(dstId)
                        if (updatedDstView != null) animateContainerBounce(updatedDstView)
                        checkGameResults()
                    }
                }
            })
            start()
        }
    }

    private fun playCompletionAnimation(view: View, boxId: Int, srcBoxId: Int = -1) {
        view.post {
            val density = resources.displayMetrics.density
            val width = view.width
            val height = view.height

            soundManager?.play("complete")

            // 1. Scale & Bounce
            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.3f, 1f)
            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.3f, 1f)
            val bounceAnim = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY).apply {
                duration = 600
                interpolator = AnticipateOvershootInterpolator()
            }

            // 2. Shine Sweep Effect
            val shineView = View(context).apply {
                background = GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(Color.TRANSPARENT, Color.parseColor("#B0FFFFFF"), Color.TRANSPARENT)
                )
                alpha = 0f
            }
            (view as ViewGroup).addView(shineView, FrameLayout.LayoutParams(width, height))
            shineView.translationX = -width.toFloat()
            
            val shineAnim = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(shineView, View.ALPHA, 0f, 1f, 0f).apply { duration = 500 },
                    ObjectAnimator.ofFloat(shineView, View.TRANSLATION_X, -width.toFloat(), width.toFloat()).apply { 
                        duration = 500
                        interpolator = LinearInterpolator()
                    }
                )
            }

            // 3. Glow Highlight
            val glowColor = Color.parseColor("#80FFEB3B")
            val glowAnim = ValueAnimator.ofArgb(Color.TRANSPARENT, glowColor, Color.TRANSPARENT).apply {
                duration = 800
                addUpdateListener { animator ->
                    view.foreground = GradientDrawable().apply {
                        setColor(animator.animatedValue as Int)
                        cornerRadius = 8 * density
                    }
                }
            }

            // 4. Checkmark
            val checkmark = TextView(context).apply {
                text = "✅"
                textSize = 40f
                alpha = 0f
                gravity = Gravity.CENTER
            }
            (view as ViewGroup).addView(checkmark, FrameLayout.LayoutParams(-1, -1, Gravity.CENTER))
            
            val checkmarkAnim = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(checkmark, View.ALPHA, 0f, 1f, 1f, 0f).apply { duration = 1000 },
                    ObjectAnimator.ofFloat(checkmark, View.SCALE_X, 0.5f, 1.5f, 1f).apply { duration = 600 },
                    ObjectAnimator.ofFloat(checkmark, View.SCALE_Y, 0.5f, 1.5f, 1f).apply { duration = 600 }
                )
            }

            // 5. Particles
            spawnParticles(view)

            // 6. Vibration & Shake
            triggerVibration(25)
            val shakeX = ObjectAnimator.ofFloat(binding.glGameBoard, View.TRANSLATION_X, 0f, 10f, -10f, 10f, -10f, 0f).apply {
                duration = 400
            }

            AnimatorSet().apply {
                playTogether(bounceAnim, glowAnim, shineAnim, checkmarkAnim, shakeX)
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.foreground = null
                        (view as? ViewGroup)?.removeView(shineView)
                        (view as? ViewGroup)?.removeView(checkmark)
                        
                        val box = engine.getBoxes().find { it.id == boxId }
                        val color = box?.blocks?.firstOrNull()
                        val bag = engine.getBoxSlots().find { it.targetColor == color && it.remaining() > 0 }
                        val bagWillBeFilled = bag != null && bag.filled + 1 >= bag.capacity
                        val truckView = if (bagWillBeFilled) {
                            val isTruckA = engine.getBoxSlots().indexOf(bag) == 0
                            if (isTruckA) binding.truckContainerA else binding.truckContainerB
                        } else null

                        engine.archiveBox(boxId)
                        engine.archiveAllReady()
                        if (srcBoxId != -1) animatingBoxes.remove(srcBoxId)
                        animatingBoxes.remove(boxId)

                        if (truckView != null && truckView.visibility == View.VISIBLE) {
                            animateTruckCompletion(truckView) {
                                activeAnimationsCount--
                                renderBoard()
                                checkGameResults()
                            }
                        } else {
                            activeAnimationsCount--
                            renderBoard()
                            checkGameResults()
                        }
                    }
                })
                start()
            }
        }
    }

    private fun setupTruckIdleAnimations() {
        val bounceA = ObjectAnimator.ofFloat(binding.imgTruckA, View.TRANSLATION_Y, 0f, 6f, 0f).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }
        val bounceB = ObjectAnimator.ofFloat(binding.imgTruckB, View.TRANSLATION_Y, 0f, 6f, 0f).apply {
            duration = 2200
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }
        bounceA.start()
        bounceB.start()
    }

    private fun animateTruckCompletion(truckView: View, onEnd: () -> Unit) {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val bounceY = ObjectAnimator.ofFloat(truckView, View.TRANSLATION_Y, 0f, -30f, 0f).apply { duration = 300 }
        val driveOff = ObjectAnimator.ofFloat(truckView, View.TRANSLATION_X, 0f, screenWidth).apply {
            duration = 700
            interpolator = AnticipateOvershootInterpolator()
        }
        
        // Hiệu ứng khói khi xuất phát
        spawnSmokeParticles(truckView)
        truckView.postDelayed({ soundManager?.play("move") }, 200)
        
        AnimatorSet().apply {
            playSequentially(bounceY, driveOff)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    truckView.translationX = -screenWidth
                    onEnd()
                    
                    truckView.postDelayed({ soundManager?.play("move") }, 50)
                    ObjectAnimator.ofFloat(truckView, View.TRANSLATION_X, -screenWidth, 0f).apply {
                        duration = 800
                        interpolator = OvershootInterpolator()
                        start()
                    }
                }
            })
            start()
        }
    }

    private fun spawnSmokeParticles(anchor: View) {
        val root = binding.root as ViewGroup
        val loc = IntArray(2)
        anchor.getLocationOnScreen(loc)
        val centerX = loc[0].toFloat() + (anchor.width * 0.2f)
        val centerY = loc[1].toFloat() + (anchor.height * 0.8f)
        
        repeat(8) {
            val p = TextView(context).apply {
                text = listOf("💨", "☁️", "🔘").random()
                textSize = 14f
                alpha = 0.8f
            }
            root.addView(p)
            p.x = centerX
            p.y = centerY
            p.animate()
                .translationXBy(-(Random.nextFloat() * 200f))
                .translationYBy(-(Random.nextFloat() * 100f))
                .alpha(0f)
                .scaleX(2f)
                .scaleY(2f)
                .setDuration(800)
                .withEndAction { root.removeView(p) }
                .start()
        }
    }

    private fun spawnParticles(anchor: View) {
        val root = binding.root as ViewGroup
        val loc = IntArray(2)
        anchor.getLocationOnScreen(loc)
        
        val centerX = loc[0].toFloat() + anchor.width / 2f
        val centerY = loc[1].toFloat() + anchor.height / 2f
        
        val emojis = listOf("✨", "⭐", "🎉", "🔥", "🌈", "🎊", "💎", "❤️", "🎈", "🍒", "🍇", "🍑")
        
        repeat(40) { // Tăng gấp đôi số hạt
            val particle = TextView(context).apply {
                text = emojis.random()
                textSize = 24f
            }
            root.addView(particle)
            particle.x = centerX
            particle.y = centerY
            
            val angle = Random.nextDouble(0.0, Math.PI * 2)
            val velocity = Random.nextFloat() * 800f + 300f
            val destX = centerX + (Math.cos(angle) * velocity).toFloat()
            val destY = centerY + (Math.sin(angle) * velocity).toFloat() - 300f
            
            particle.animate()
                .x(destX)
                .y(destY)
                .alpha(0f)
                .scaleX(2.0f)
                .scaleY(2.0f)
                .rotation(Random.nextFloat() * 720f)
                .setDuration(1200 + Random.nextLong(800))
                .setInterpolator(DecelerateInterpolator())
                .withEndAction { root.removeView(particle) }
                .start()
        }
    }

    private fun triggerVibration(ms: Long) {
        val prefs = requireContext().getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("vibration_on", true)) return

        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(ms)
            }
        }
    }

    private fun playBackgroundMusic() {
        GlobalMusicPlayer.playIfEnabled(requireContext(), R.raw.nhacnen)
    }

    private fun checkGameResults() {
        if (engine.isWin) {
            showWinDialog()
        } else if (engine.isGameOver && !engine.isWin) {
            engine.isGameOver = true
            showLoseDialog()
        } else if (engine.isDeadlocked()) {
            engine.isGameOver = true
            showLoseDialog()
        }
    }

    private fun showWinDialog() {
        soundManager?.playWin()
        isWinDialogShowing = true
        
        val dialogCard = binding.layoutWinDialog.winCard
        binding.layoutWinDialog.root.visibility = View.VISIBLE
        binding.layoutWinDialog.tvWinSubtitle.text = getString(R.string.win_dialog_subtitle, args.levelId)
        
        // Hoạt ảnh xuất hiện chuyên nghiệp
        dialogCard.scaleX = 0.5f
        dialogCard.scaleY = 0.5f
        dialogCard.alpha = 0f
        dialogCard.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(500)
            .setInterpolator(OvershootInterpolator())
            .withEndAction { 
                spawnParticles(binding.tvLevelName)
                spawnParticles(dialogCard)
            }
            .start()
        
        val prefs = requireContext().getSharedPreferences("game_prefs", 0)
        val highest = prefs.getInt("highest_level", 1)
        if (args.levelId + 1 > highest) {
            prefs.edit().putInt("highest_level", args.levelId + 1).apply()
        }

        binding.layoutWinDialog.btnWinContinue.setOnClickListener {
            GoldManager.addGold(requireContext(), 50)
            navigateToNextLevel()
        }
    }

    private fun showLoseDialog() {
        soundManager?.playLose()
        val vibrator = requireContext().getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        if (vibrator?.hasVibrator() == true) { vibrator.vibrate(200) }
        
        // Rung lắc bàn chơi trước khi hiện dialog
        ObjectAnimator.ofFloat(binding.glGameBoard, View.TRANSLATION_X, 0f, 18f, -18f, 14f, -14f, 8f, -8f, 0f).apply { duration = 450; start() }
        
        isLoseDialogShowing = true
        val dialogCard = binding.layoutLoseDialog.loseCard
        binding.layoutLoseDialog.root.visibility = View.VISIBLE
        
        // Hoạt ảnh xuất hiện chuyên nghiệp
        dialogCard.scaleX = 0.5f
        dialogCard.scaleY = 0.5f
        dialogCard.alpha = 0f
        dialogCard.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(500)
            .setInterpolator(OvershootInterpolator())
            .start()

        binding.layoutLoseDialog.btnLoseRetry.setOnClickListener {
            activity?.recreate()
        }
        binding.layoutLoseDialog.btnLoseBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun navigateToNextLevel() {
        val nextLevelId = args.levelId + 1
        val bundle = Bundle().apply { putInt("levelId", nextLevelId) }
        findNavController().navigate(R.id.action_LevelOneFragment_self, bundle)
    }

    private fun updateStatusUI() {
        val progressText = if (engine.isBagMechanismEnabled) {
            getString(R.string.progress_packed, engine.completedBoxesCount, engine.totalFullBoxesCount)
        } else {
            getString(R.string.progress_completed, engine.completedBoxesCount, engine.totalFullBoxesCount)
        }
        binding.tvPackedProgress.text = progressText
    }

    private fun updateBoxUI(tvFruit: TextView, tvInfo: TextView, tvTurns: TextView, box: LevelOneEngine.BoxSlot) {
        tvFruit.visibility = View.VISIBLE
        tvInfo.visibility = View.VISIBLE
        tvTurns.visibility = View.VISIBLE
        tvFruit.text = box.targetColor.fruitIcon
        tvInfo.text = getString(R.string.bag_numeric_format, box.filled, box.capacity)
        tvTurns.text = "${box.turnsLeft}" 
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

    private fun animateContainerBounce(view: View) {
        val pvhX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.08f, 1.0f)
        val pvhY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.92f, 1.0f)
        ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY).apply {
            duration = 300
            interpolator = OvershootInterpolator()
            start()
        }
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
