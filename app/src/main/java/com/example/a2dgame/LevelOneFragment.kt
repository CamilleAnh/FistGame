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
import android.media.MediaPlayer
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
    private var mediaPlayer: MediaPlayer? = null
    private var soundManager: SoundManager? = null
    private var currentLevelId: Int = 1

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
        currentLevelId = levelId
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
            renderBoard()
        }

        binding.btnBackLevelSelect.setOnClickListener {
            findNavController().popBackStack(R.id.SecondFragment, false)
        }

        binding.btnNextLevel.setOnClickListener {
            val nextLevelId = levelId + 1
            saveHighestLevel(nextLevelId)
            val bundle = Bundle().apply { putInt("levelId", nextLevelId) }
            val navOptions = NavOptions.Builder().setPopUpTo(R.id.LevelOneFragment, true).build()
            findNavController().navigate(R.id.action_LevelOneFragment_self, bundle, navOptions)
        }

        setupSettings()
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
            if (isChecked) playBackgroundMusic() else mediaPlayer?.pause()
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

    private fun renderBoard() {
        wiggleAnimators.values.forEach { it.cancel() }
        wiggleAnimators.clear()
        
        binding.glGameBoard.removeAllViews()
        val tubes = engine.getTubes()
        val activeTubes = tubes.filter { !it.isArchived }
        
        val cols = 4 
        binding.glGameBoard.columnCount = cols
        
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val horizontalPadding = (80 * displayMetrics.density).toInt()
        val tubeWidth = (screenWidth - horizontalPadding) / cols
        val blockHeight = (tubeWidth * 0.75).toInt() 
        val tubeHeight = (blockHeight * 4) + (32 * displayMetrics.density).toInt()

        binding.llBoxes.isVisible = engine.isBagMechanismEnabled
        binding.tvPackedProgress.text = engine.getProgressText()
        
        if (engine.isBagMechanismEnabled) {
            engine.getBoxSlots().forEachIndexed { i, box ->
                val tv = if (i == 0) binding.tvBoxA else binding.tvBoxB
                tv.isVisible = true
                updateBoxUI(tv, box)
            }
        }

        activeTubes.forEach { tube ->
            val tubeContainer = FrameLayout(requireContext()).apply {
                tag = tube.id
                layoutParams = GridLayout.LayoutParams().apply {
                    width = tubeWidth
                    height = tubeHeight
                    setMargins(10, 16, 10, 16)
                }
                setOnClickListener { handleTubeTap(tube.id) }
            }

            val tubeLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM
                clipChildren = false
                setPadding(12, 12, 12, 20) 
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                background = ContextCompat.getDrawable(context, R.drawable.carton_box_bg)
            }

            tube.blocks.forEachIndexed { i, fruit ->
                val isHidden = i < tube.hiddenLayers
                val blockView = FrameLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, blockHeight).apply {
                        setMargins(4, -4, 4, 0)
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
                tubeLayout.addView(blockView, 0)
            }
            
            tubeContainer.addView(tubeLayout)
            binding.glGameBoard.addView(tubeContainer)
            
            if (engine.selectedTubeIndex == tube.id) {
                animateSelection(tubeContainer, true)
            }
        }
        updateStatusUI()
    }

    private fun handleTubeTap(index: Int) {
        if (isAnimating || engine.isGameOver) return
        
        val tubes = engine.getTubes()
        val clickedTube = tubes.find { it.id == index } ?: return
        val selectedIdx = engine.selectedTubeIndex

        if (selectedIdx == null) {
            if (!clickedTube.isEmpty() && !clickedTube.isFrozen && !clickedTube.hasCobweb && !clickedTube.isComplete()) {
                engine.selectedTubeIndex = index
                soundManager?.play("pickup")
                val view = binding.glGameBoard.findViewWithTag<View>(index)
                animateSelection(view, true)
            }
        } else if (selectedIdx == index) {
            engine.selectedTubeIndex = null
            soundManager?.play("drop")
            val view = binding.glGameBoard.findViewWithTag<View>(index)
            animateSelection(view, false)
        } else {
            val srcTube = tubes.find { it.id == selectedIdx }!!
            if (engine.canMove(srcTube, clickedTube)) {
                animateMoveSequence(selectedIdx, index)
            } else {
                val oldView = binding.glGameBoard.findViewWithTag<View>(selectedIdx)
                animateSelection(oldView, false)
                
                if (!clickedTube.isEmpty() && !clickedTube.isFrozen && !clickedTube.hasCobweb && !clickedTube.isComplete()) {
                    engine.selectedTubeIndex = index
                    soundManager?.play("pickup")
                    val view = binding.glGameBoard.findViewWithTag<View>(index)
                    animateSelection(view, true)
                } else {
                    engine.selectedTubeIndex = null
                    soundManager?.play("drop")
                }
            }
        }
    }

    private fun animateSelection(tubeView: View?, isSelected: Boolean) {
        val tubeLayout = (tubeView as? ViewGroup)?.getChildAt(0) as? ViewGroup ?: return
        val density = resources.displayMetrics.density
        val tubeId = tubeView.tag as Int
        val tube = engine.getTubes().find { it.id == tubeId } ?: return
        val color = tube.peekColor()
        
        for (i in 0 until tubeLayout.childCount) {
            val block = tubeLayout.getChildAt(i)
            val blockIndex = tubeLayout.childCount - 1 - i
            
            val isPartOfTopStack = blockIndex < tube.blocks.size && 
                                   tube.blocks[blockIndex] == color &&
                                   blockIndex >= tube.hiddenLayers
            
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
            }
        }
        
        tubeView.animate()
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
        engine.selectedTubeIndex = null
        
        val srcView = binding.glGameBoard.findViewWithTag<ViewGroup>(srcId)
        val dstView = binding.glGameBoard.findViewWithTag<ViewGroup>(dstId)
        val srcLayout = srcView?.getChildAt(0) as? ViewGroup
        val dstLayout = dstView?.getChildAt(0) as? ViewGroup
        
        if (srcLayout == null || dstLayout == null || srcView == null || dstView == null) {
            isAnimating = false
            renderBoard()
            return
        }

        val srcTube = engine.getTubes().find { it.id == srcId }!!
        val dstTube = engine.getTubes().find { it.id == dstId }!!
        val color = srcTube.peekColor()
        
        val movingViews = mutableListOf<View>()
        for (i in 0 until srcLayout.childCount) {
            val block = srcLayout.getChildAt(i)
            val idx = srcLayout.childCount - 1 - i
            if (idx < srcTube.blocks.size && srcTube.blocks[idx] == color && idx >= srcTube.hiddenLayers && (dstTube.blocks.size + movingViews.size) < dstTube.capacity) {
                movingViews.add(block)
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
            val targetSlotIndex = dstTube.blocks.size + (movingViews.size - 1 - index)
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
                    engine.executeMove(srcTube, dstTube)
                    
                    if (dstTube.isComplete()) {
                        renderBoard()
                        val newDstView = binding.glGameBoard.findViewWithTag<View>(dstId)
                        if (newDstView != null) {
                            playCompletionAnimation(newDstView, dstId)
                        } else {
                            engine.archiveTube(dstId)
                            renderBoard()
                            checkGameResults()
                            isAnimating = false
                        }
                    } else {
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
        srcView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
    }

    private fun playCompletionAnimation(view: View, tubeId: Int) {
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
                        engine.archiveTube(tubeId)
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
        val prefs = requireContext().getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        val isMusicOn = prefs.getBoolean("music_on", true)
        
        if (isMusicOn) {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.nhacnen)
                mediaPlayer?.isLooping = true
            }
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        } else {
            mediaPlayer?.pause()
        }
    }

    private fun checkGameResults() {
        if (engine.isWin) {
            Toast.makeText(context, "Level Complete!", Toast.LENGTH_LONG).show()
            binding.btnNextLevel.isVisible = true
        } else if (engine.isGameOver) {
            Toast.makeText(context, "No more moves!", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateStatusUI() {
        binding.tvPackedProgress.text = engine.getProgressText()
    }

    private fun updateBoxUI(tv: TextView, box: LevelOneEngine.BoxSlot) {
        val isFilled = box.filled > 0
        tv.text = if (isFilled) box.targetColor.fruitIcon else "📦"
        tv.alpha = if (isFilled) 1.0f else 0.5f
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
        playBackgroundMusic()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        soundManager?.release()
        _binding = null
    }
}
