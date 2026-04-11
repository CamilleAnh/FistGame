package com.example.a2dgame

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
import com.example.a2dgame.databinding.FragmentLevelOneBinding
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

    // Power-up: free 1-lượt/màn + inventory từ Shop
    private var powerupReroll    = 1
    private var powerupMagnify   = 1
    private var powerupReshuffle = 1
    private var isMagnifyMode    = false

    // Win Dialog state
    private var isWinDialogShowing = false

    private val wiggleAnimators = mutableMapOf<View, Animator>()
    private var isAnimating = false

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
        
        binding.glGameBoard.clipChildren = false
        binding.glGameBoard.clipToPadding = false
        (binding.root as ViewGroup).clipChildren = false
        
        loadBannerAd()
        playBackgroundMusic()
        renderBoard()

        binding.btnReset.setOnClickListener {
            if (isAnimating) return@setOnClickListener
            engine = LevelOneEngine(levelId)
            // Reset power-up counts
            powerupReroll    = 1
            powerupMagnify   = 1
            powerupReshuffle = 1
            isMagnifyMode    = false
            updatePowerupButtons()
            renderBoard()
        }

        binding.btnBackLevelSelect.setOnClickListener {
            findNavController().popBackStack(R.id.SecondFragment, false)
        }

        binding.btnNextLevel.setOnClickListener {
            navigateToNextLevel()
        }

        setupSettings()
        setupPowerups()
        updateGoldDisplay()
    }

    private fun navigateToNextLevel() {
        val nextLevelId = args.levelId + 1
        saveHighestLevel(nextLevelId)
        val bundle = Bundle().apply { putInt("levelId", nextLevelId) }
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.LevelOneFragment, true).build()
        findNavController().navigate(R.id.action_LevelOneFragment_self, bundle, navOptions)
    }

    private fun updateGoldDisplay() {
        val gold = GoldManager.getGold(requireContext())
        binding.tvGameGold.text = getString(R.string.gold_display_format, gold)
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

        // Initialize switches from saved preferences
        settingsBinding.switchMusic.isChecked = prefs.getBoolean("music_on", true)
        settingsBinding.switchSound.isChecked = prefs.getBoolean("sound_on", true)
        settingsBinding.switchVibration.isChecked = prefs.getBoolean("vibration_on", true)

        settingsBinding.switchMusic.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("music_on", isChecked) }
            GlobalMusicPlayer.setEnabled(requireContext(), isChecked)
        }
        
        settingsBinding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("sound_on", isChecked) }
            soundManager?.setEnabled(isChecked)
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
            playBackgroundMusic()
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

    // ===== POWER-UPS =====

    private fun setupPowerups() {
        // Lấy inventory từ GoldManager cộng vào free 1-lượt
        val ctx = requireContext()
        powerupReroll    = 1 + GoldManager.getRerollCount(ctx)
        powerupMagnify   = 1 + GoldManager.getRevealCount(ctx)
        powerupReshuffle = 1 + GoldManager.getShuffleCount(ctx)

        updatePowerupButtons()

        // 🎲 Roll túi ngẫu nhiên
        binding.btnRerollBags.setOnClickListener {
            if (powerupReroll <= 0 || isAnimating) return@setOnClickListener
            powerupReroll--
            // Nếu còn dùng hết free thì trừ inventory
            if (powerupReroll >= 1) GoldManager.useReroll(requireContext())
            engine.rerollBags()
            engine.archiveAllReady()
            soundManager?.play("pickup")
            updatePowerupButtons()
            renderBoard()
        }

        // 🔍 Kính lúp – bật/tắt chế độ chọn hộp để lộ ẩn
        binding.btnMagnify.setOnClickListener {
            if (powerupMagnify <= 0 || isAnimating) return@setOnClickListener
            isMagnifyMode = !isMagnifyMode
            // Viền sáng lên khi đang ở chế độ chọn
            binding.btnMagnify.alpha = if (isMagnifyMode) 1.0f else 0.7f
            binding.btnMagnify.text  = if (isMagnifyMode) "🔍 ✓" else "🔍 ×$powerupMagnify"
        }

        // 🔀 Xáo trộn lại tất cả
        binding.btnReshuffle.setOnClickListener {
            if (powerupReshuffle <= 0 || isAnimating) return@setOnClickListener
            powerupReshuffle--
            if (powerupReshuffle >= 1) GoldManager.useShuffle(requireContext())
            engine.shuffleAllBoxes()
            soundManager?.play("move")
            updatePowerupButtons()
            renderBoard()
        }
    }

    private fun updatePowerupButtons() {
        binding.btnRerollBags.apply {
            text    = "🎲 ×$powerupReroll"
            isEnabled = powerupReroll > 0
            alpha   = if (powerupReroll > 0) 0.9f else 0.35f
        }
        binding.btnMagnify.apply {
            text    = if (isMagnifyMode) "🔍 ✓" else "🔍 ×$powerupMagnify"
            isEnabled = powerupMagnify > 0
            alpha   = if (powerupMagnify > 0) 0.9f else 0.35f
        }
        binding.btnReshuffle.apply {
            text    = "🔀 ×$powerupReshuffle"
            isEnabled = powerupReshuffle > 0
            alpha   = if (powerupReshuffle > 0) 0.9f else 0.35f
        }
    }

    private fun renderBoard() {
        wiggleAnimators.values.forEach { it.cancel() }
        wiggleAnimators.clear()
        
        binding.glGameBoard.removeAllViews()
        val boxes = engine.getBoxes()
        val activeBoxes = boxes.filter { !it.isArchived }
        
        val cols = 4 
        binding.glGameBoard.columnCount = cols
        
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val horizontalPadding = (80 * displayMetrics.density).toInt()
        val boxWidth = (screenWidth - horizontalPadding) / cols
        val blockHeight = (boxWidth * 0.52).toInt()
        val boxHeight = (blockHeight * 4) + (16 * displayMetrics.density).toInt()

        // Reset cả 2 ô trước khi vẽ lại tránh hiện thị cũ
        binding.tvBoxA.visibility = View.GONE
        binding.tvBoxB.visibility = View.GONE
        binding.llBoxes.isVisible = engine.isBagMechanismEnabled

        if (engine.isBagMechanismEnabled) {
            val slots = engine.getBoxSlots()
            slots.getOrNull(0)?.let { box ->
                binding.tvBoxA.visibility = View.VISIBLE
                updateBoxUI(binding.tvBoxA, box)
            }
            slots.getOrNull(1)?.let { box ->
                binding.tvBoxB.visibility = View.VISIBLE
                updateBoxUI(binding.tvBoxB, box)
            }
        }

        activeBoxes.forEach { box ->
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
                clipChildren = false
                setPadding(8, 6, 8, 12)
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                background = ContextCompat.getDrawable(context, R.drawable.carton_box_bg)
            }

            box.blocks.forEachIndexed { i, fruit ->
                val isHidden = i < box.hiddenLayers
                val blockView = FrameLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, blockHeight).apply {
                        setMargins(3, -2, 3, 0)
                    }
                    
                    background = if (isHidden) {
                        GradientDrawable().apply {
                            setColor("#55000000".toColorInt())
                            cornerRadius = 4f * resources.displayMetrics.density
                        }
                    } else {
                        ContextCompat.getDrawable(context, R.drawable.item_fruit_box)
                    }

                    addView(TextView(context).apply {
                        layoutParams = FrameLayout.LayoutParams(-1, -1)
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
            
            if (engine.selectedBoxIndex == box.id) {
                animateSelection(boxContainer, true)
            }
        }
        updateStatusUI()
    }

    private fun handleBoxTap(index: Int) {
        if (isAnimating || engine.isGameOver) return

        // 🔍 Kính lúp mode: tap vào hộp để lộ lớp ẩn
        if (isMagnifyMode) {
            val box = engine.getBoxes().find { it.id == index }
            if (box != null && !box.isArchived && box.hiddenLayers > 0) {
                powerupMagnify--
                engine.revealHiddenLayers(index)
                soundManager?.play("complete")
            }
            // Thoát mode dù hộp có ẩn hay không
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
        val boxId = boxView.tag as Int
        val box = engine.getBoxes().find { it.id == boxId } ?: return
        val color = box.peekColor()

        // Tính số block LIÊN TIẾP từ ĐỈNH stack có cùng màu (không tính block cùng màu nhưng không liên tiếp)
        // blocks[blocks.size-1] = đỉnh, blocks[0] = đáy
        var consecutiveCount = 0
        for (idx in box.blocks.indices.reversed()) {
            if (idx < box.hiddenLayers) break
            if (box.blocks[idx] == color) consecutiveCount++
            else break
        }
        // topStackRange: các blockIndex (trong blocks[]) thuộc top-stack liên tiếp
        // blockIndex cao nhất = đỉnh = blocks.size-1
        val topStackMinIndex = box.blocks.size - consecutiveCount

        // boxLayout: getChildAt(0) = đỉnh stack (blocks[blocks.size-1])
        //            getChildAt(childCount-1) = đáy stack (blocks[0])
        // i trong loop → blockIndex trong blocks[] = blocks.size - 1 - i
        for (i in 0 until boxLayout.childCount) {
            val block = boxLayout.getChildAt(i)
            // blockIndex là vị trí tương ứng trong blocks[]
            val blockIndex = box.blocks.size - 1 - i
            
            // Chỉ animate block nằm trong vùng liên tiếp từ đỉnh
            val isPartOfTopStack = blockIndex >= 0 &&
                                   blockIndex >= topStackMinIndex &&
                                   blockIndex >= box.hiddenLayers
            
            if (isPartOfTopStack) {
                if (isSelected) {
                    block.animate()
                        .translationY(-15 * density)
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(200)
                        .setInterpolator(OvershootInterpolator())
                        .withEndAction { startWiggle(block) }
                        .start()
                } else {
                    stopWiggle(block)
                    block.animate()
                        .translationY(0f)
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(200)
                        .start()
                }
            } else if (!isSelected) {
                // Đảm bảo reset animation cho tất cả block (phòng trường hợp state cũ)
                stopWiggle(block)
                block.animate()
                    .translationY(0f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
            }
        }
        
        // Scale boxLayout (inner) thay vì boxContainer để tránh overlap vùng touch
        boxLayout.animate()
            .scaleX(if (isSelected) 1.05f else 1.0f)
            .scaleY(if (isSelected) 1.05f else 1.0f)
            .setDuration(200)
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
        isAnimating = true
        engine.selectedBoxIndex = null
        
        val srcView = binding.glGameBoard.findViewWithTag<ViewGroup>(srcId)
        val dstView = binding.glGameBoard.findViewWithTag<ViewGroup>(dstId)
        val srcLayout = srcView?.getChildAt(0) as? ViewGroup
        val dstLayout = dstView?.getChildAt(0) as? ViewGroup
        
        if (srcLayout == null || dstLayout == null || srcView == null || dstView == null) {
            isAnimating = false
            renderBoard()
            return
        }

        val srcBox = engine.getBoxes().find { it.id == srcId }!!
        val dstBox = engine.getBoxes().find { it.id == dstId }!!
        val color = srcBox.peekColor()
        
        val movingViews = mutableListOf<View>()
        for (i in 0 until srcLayout.childCount) {
            val block = srcLayout.getChildAt(i)
            // blockIndex trong blocks[]: getChildAt(0) = đỉnh = blocks[blocks.size-1]
            val blockIndex = srcBox.blocks.size - 1 - i
            if (blockIndex >= 0 &&
                blockIndex >= srcBox.hiddenLayers &&
                srcBox.blocks[blockIndex] == color &&
                (dstBox.blocks.size + movingViews.size) < dstBox.capacity) {
                movingViews.add(block)
            } else if (blockIndex >= 0 && srcBox.blocks[blockIndex] != color) {
                // Dừng ngay khi gặp block khác màu (chỉ lấy liên tiếp)
                break
            }
        }

        val rootLoc = IntArray(2)
        binding.root.getLocationOnScreen(rootLoc)
        
        val srcLoc = IntArray(2)
        val dstLoc = IntArray(2)
        srcView.getLocationOnScreen(srcLoc)
        dstView.getLocationOnScreen(dstLoc)
        
        val moveAnimators = mutableListOf<Animator>()
        val density = resources.displayMetrics.density
        
        soundManager?.play("move")

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
            
            block.x = (blockLoc[0] - rootLoc[0]).toFloat()
            block.y = (blockLoc[1] - rootLoc[1]).toFloat()
            
            val targetX = (dstLoc[0] - rootLoc[0]).toFloat() + (dstView.width - originalWidth) / 2f
            val targetSlotIndex = dstBox.blocks.size + (movingViews.size - 1 - index)
            val paddingBottom = 20 * density
            val overlapAdjustment = targetSlotIndex * 4 * density
            val targetY = (dstLoc[1] - rootLoc[1]).toFloat() + dstView.height - paddingBottom - (targetSlotIndex + 1) * originalHeight + overlapAdjustment

            val path = Path().apply {
                moveTo(block.x, block.y)
                val arcHeight = 150 * density
                quadTo((block.x + targetX) / 2, (dstLoc[1] - rootLoc[1]).toFloat() - arcHeight, targetX, targetY)
            }
            
            val animator = ObjectAnimator.ofFloat(block, View.X, View.Y, path).apply {
                duration = 400 + index * 100L
                interpolator = AccelerateDecelerateInterpolator()
            }
            moveAnimators.add(animator)
        }
        
        android.animation.AnimatorSet().apply {
            playTogether(moveAnimators)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    movingViews.forEach { (binding.root as ViewGroup).removeView(it) }
                    engine.executeMove(srcBox, dstBox)
                    
                    if (dstBox.isComplete()) {
                        renderBoard()
                        val newDstView = binding.glGameBoard.findViewWithTag<View>(dstId)
                        if (newDstView != null) {
                            playCompletionAnimation(newDstView, dstId)
                        } else {
                            engine.archiveBox(dstId)
                            engine.archiveAllReady() // cascade
                            renderBoard()
                            checkGameResults()
                            isAnimating = false
                        }
                    } else {
                        engine.archiveAllReady() // archive các hộp đầy khớp túi dù dst chưa đầy
                        soundManager?.play("drop")
                        renderBoard()
                        val updatedDstView = binding.glGameBoard.findViewWithTag<View>(dstId)
                        if (updatedDstView != null) animateContainerBounce(updatedDstView)
                        checkGameResults()
                        isAnimating = false
                    }
                }
            })
            start()
        }
        // Reset scale trên boxLayout bên trong (không phải srcView container)
        val srcBoxLayout = srcView.getChildAt(0)
        srcBoxLayout?.animate()?.scaleX(1.0f)?.scaleY(1.0f)?.setDuration(200)?.start()
    }

    private fun playCompletionAnimation(view: View, boxId: Int) {
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
                        engine.archiveBox(boxId)
                        engine.archiveAllReady() // cascade: archive các hộp khác đã sẵn sàng
                        renderBoard()
                        checkGameResults()
                        isAnimating = false
                    }
                })
                start()
            }
        }
    }

    private fun spawnParticles(anchor: View) {
        val root = binding.root as ViewGroup
        val loc = IntArray(2)
        anchor.getLocationOnScreen(loc)
        val rootLoc = IntArray(2)
        root.getLocationOnScreen(rootLoc)
        
        val centerX = (loc[0] - rootLoc[0]) + anchor.width / 2f
        val centerY = (loc[1] - rootLoc[1]) + anchor.height / 2f
        
        val emojis = listOf("✨", "⭐", "🎉", "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝")
        
        repeat(20) {
            val particle = TextView(context).apply {
                text = emojis.random()
                textSize = 18f
            }
            root.addView(particle)
            particle.x = centerX - 10 * resources.displayMetrics.density
            particle.y = centerY - 10 * resources.displayMetrics.density
            
            val angle = Random.nextDouble(0.0, Math.PI * 2)
            val velocity = Random.nextFloat() * 500f + 200f
            val destX = centerX + (Math.cos(angle) * velocity).toFloat()
            val destY = centerY + (Math.sin(angle) * velocity).toFloat() - 200f
            
            particle.animate()
                .x(destX)
                .y(destY)
                .alpha(0f)
                .scaleX(1.5f)
                .scaleY(1.5f)
                .rotation(Random.nextFloat() * 360f)
                .setDuration(1000 + Random.nextLong(500))
                .setInterpolator(AccelerateDecelerateInterpolator())
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
        } else if (engine.isGameOver) {
            Toast.makeText(context, getString(R.string.game_over_toast), Toast.LENGTH_LONG).show()
        }
    }

    private fun showWinDialog() {
        if (isWinDialogShowing) return
        isWinDialogShowing = true
        val levelId = args.levelId

        val dialog = binding.layoutWinDialog
        val dialogRoot = dialog.root
        val winCard = dialog.winCard

        // Set subtitle
        dialog.tvWinSubtitle.text = getString(R.string.win_dialog_subtitle, levelId)

        // Animate in
        dialogRoot.visibility = View.VISIBLE
        dialogRoot.alpha = 0f
        winCard.scaleX = 0.5f
        winCard.scaleY = 0.5f
        dialogRoot.animate().alpha(1f).setDuration(200).start()
        winCard.animate()
            .scaleX(1f).scaleY(1f)
            .setDuration(400)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()

        // Nút Nhận 50 Vàng
        dialog.btnWinTake50.setOnClickListener {
            GoldManager.addGold(requireContext(), GoldManager.REWARD_BASE)
            updateGoldDisplay()
            dismissWinDialogAndProceed()
        }

        // Nút Xem Video x3 (150 Vàng – mock delay 2s)
        dialog.btnWinWatchX3.setOnClickListener {
            dialog.btnWinWatchX3.isEnabled = false
            dialog.btnWinTake50.isEnabled = false
            dialog.pbWinLoading.visibility = View.VISIBLE
            dialog.btnWinWatchX3.text = getString(R.string.win_dialog_watching)

            // Mock xem ads: delay 2 giây
            binding.root.postDelayed({
                if (_binding == null) return@postDelayed
                GoldManager.addGold(requireContext(), GoldManager.REWARD_X3)
                updateGoldDisplay()
                dialog.pbWinLoading.visibility = View.GONE
                // Hiện số vàng nhận được
                dialog.tvWinSubtitle.text = getString(R.string.gold_added_format, GoldManager.REWARD_X3)
                binding.root.postDelayed({ dismissWinDialogAndProceed() }, 800)
            }, 2000)
        }

        // Nút Chơi tiếp (cũng nhận thưởng cơ bản nếu chưa nhận)
        dialog.btnWinContinue.setOnClickListener {
            GoldManager.addGold(requireContext(), GoldManager.REWARD_BASE)
            updateGoldDisplay()
            dismissWinDialogAndProceed()
        }
    }

    private fun dismissWinDialogAndProceed() {
        isWinDialogShowing = false
        binding.layoutWinDialog.root.visibility = View.GONE
        navigateToNextLevel()
    }

    private fun updateStatusUI() {
        binding.tvPackedProgress.text = engine.getProgressText()
    }

    private fun updateBoxUI(tv: TextView, box: LevelOneEngine.BoxSlot) {
        val density = resources.displayMetrics.density
        val isUrgent = box.turnsLeft <= 8
        val borderColor = if (isUrgent) android.graphics.Color.RED
                          else android.graphics.Color.parseColor("#FFD54F")

        // Nền + viền màu động
        tv.background = GradientDrawable().apply {
            setColor(android.graphics.Color.parseColor("#CC1A1A2E"))
            setStroke((2 * density).toInt(), borderColor)
            cornerRadius = 10 * density
        }
        tv.setPadding(
            (8 * density).toInt(), (6 * density).toInt(),
            (8 * density).toInt(), (6 * density).toInt()
        )
        tv.gravity = android.view.Gravity.CENTER
        tv.textSize = 11f
        tv.setTextColor(android.graphics.Color.WHITE)

        // Nội dung: icon + tên + tiến trình + lượt còn lại
        val icon = box.targetColor.fruitIcon
        val name = box.targetColor.displayName
        val turnsColor = if (isUrgent) "🔴" else "🟡"  // đỏ đỏ hoặc vàng
        tv.text = "$icon $name\n${box.filled}/${box.capacity} ưu | $turnsColor ${box.turnsLeft} lượt"
    }

    private fun saveHighestLevel(level: Int) {
        val prefs = requireContext().getSharedPreferences("game_prefs", 0)
        val currentHigh = prefs.getInt("highest_level", 1)
        if (level > currentHigh) {
            prefs.edit { putInt("highest_level", level) }
        }
    }

    private fun loadBannerAd() {
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
        soundManager?.release()
        // Không release mediaPlayer – GlobalMusicPlayer quản lý
        _binding = null
    }
}
