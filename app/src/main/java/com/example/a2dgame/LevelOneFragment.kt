package com.example.a2dgame

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.a2dgame.databinding.FragmentLevelOneBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlin.math.ceil

class LevelOneFragment : Fragment() {

    private var _binding: FragmentLevelOneBinding? = null
    private val binding get() = _binding!!
    
    private val args: LevelOneFragmentArgs by navArgs()
    private lateinit var engine: LevelOneEngine
    private var mediaPlayer: MediaPlayer? = null
    private var currentLevelId: Int = 1

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
        binding.tvLevelName.text = getString(R.string.level_name_format, levelId)
        
        loadBannerAd()
        playBackgroundMusic(levelId)
        renderBoard()

        binding.btnReset.setOnClickListener {
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
    }

    private fun playBackgroundMusic(levelId: Int) {
        // Dừng nhạc cũ nếu đang chạy
        mediaPlayer?.release()
        mediaPlayer = null
        
        // Chỉ chơi nhạc Sunny Orchard Shuffle cho level 1 - 100
        if (levelId in 1..100) {
            val player = MediaPlayer.create(requireContext(), R.raw.nhacnen)
            if (player != null) {
                mediaPlayer = player
                player.isLooping = true
                player.setVolume(0.35f, 0.35f) // Âm lượng 35%
                player.start()
            }
        }
    }

    private fun loadBannerAd() {
        val adView = AdView(requireContext())
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
        adView.setAdSize(AdSize.BANNER)
        binding.adContainer.removeAllViews()
        binding.adContainer.addView(adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun saveHighestLevel(level: Int) {
        val prefs = requireContext().getSharedPreferences("game_prefs", 0)
        val currentHighest = prefs.getInt("highest_level", 1)
        if (level > currentHighest) prefs.edit().putInt("highest_level", level).apply()
    }

    private fun renderBoard() {
        binding.glGameBoard.removeAllViews()
        val tubes = engine.getTubes()
        val activeTubes = tubes.filter { !it.isArchived }
        if (activeTubes.isEmpty()) return

        val tubeCount = activeTubes.size
        val cols = 4 
        binding.glGameBoard.columnCount = cols
        
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        
        val horizontalPadding = (100 * displayMetrics.density).toInt()
        val tubeWidth = (screenWidth - horizontalPadding) / cols
        
        val blockHeight = (tubeWidth * 0.5).toInt()
        val maxCapacity = 4
        val tubeHeight = (blockHeight * maxCapacity) + (4 * displayMetrics.density).toInt()

        val isBagEnabled = engine.isBagMechanismEnabled
        binding.llBoxes.isVisible = isBagEnabled
        binding.tvPackedProgress.text = engine.getProgressText()
        
        if (isBagEnabled) {
            val boxSlots = engine.getBoxSlots()
            boxSlots.forEachIndexed { i, box ->
                val tv = if (i == 0) binding.tvBoxA else binding.tvBoxB
                tv.isVisible = true
                updateBoxUI(tv, box)
            }
            if (boxSlots.size < 2) binding.tvBoxB.isVisible = false
            if (boxSlots.isEmpty()) binding.tvBoxA.isVisible = false
        }

        activeTubes.forEach { tube ->
            val index = tube.id
            val tubeContainer = FrameLayout(requireContext()).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = tubeWidth
                    height = tubeHeight
                    setMargins(2, 4, 2, 4)
                }
            }

            val tubeLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                
                val border = GradientDrawable().apply {
                    setColor(Color.TRANSPARENT) 
                    setStroke(2, if (engine.selectedTubeIndex == index) Color.YELLOW else Color.WHITE)
                    cornerRadius = 4f
                }
                background = border
                
                setOnClickListener {
                    if (engine.handleTubeClick(index)) {
                        renderBoard()
                        if (engine.isGameOver) {
                            if (engine.isWin) {
                                saveHighestLevel(args.levelId + 1)
                                Toast.makeText(context, getString(R.string.game_complete_toast), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, getString(R.string.game_over_toast), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }

            for (i in 0 until 4) {
                val blockFrame = FrameLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(tubeWidth, blockHeight).apply { 
                        setMargins(0, 0, 0, 0)
                    }
                    background = ContextCompat.getDrawable(context, R.drawable.crate_bg)
                }

                if (i < tube.blocks.size) {
                    val fruitColor = tube.blocks[i]
                    val isHidden = i < tube.hiddenLayers
                    
                    if (isHidden) {
                        val tvHint = TextView(context).apply {
                            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                            text = "?"
                            setTextColor(Color.WHITE)
                            gravity = Gravity.CENTER
                            textSize = 14f
                            setTypeface(null, Typeface.BOLD)
                            setBackgroundColor(Color.parseColor("#CC333333"))
                        }
                        blockFrame.addView(tvHint)
                    } else {
                        val tvFruit = TextView(context).apply {
                            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                            text = fruitColor.fruitIcon
                            gravity = Gravity.CENTER
                            textSize = 30f
                            includeFontPadding = false
                        }
                        blockFrame.addView(tvFruit)
                    }
                }
                tubeLayout.addView(blockFrame, 0)
            }
            tubeContainer.addView(tubeLayout)

            if (tube.isFrozen) {
                val ice = View(context).apply {
                    layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#88ADF4FF"))
                        cornerRadius = 4f
                        setStroke(3, Color.CYAN)
                    }
                }
                tubeContainer.addView(ice)
            }
            if (tube.hasCobweb) {
                val spider = TextView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    text = "🕸️"
                    textSize = 20f
                    gravity = Gravity.CENTER
                }
                tubeContainer.addView(spider)
            }
            binding.glGameBoard.addView(tubeContainer)
        }
        updateStatusUI()
    }

    private fun updateBoxUI(textView: TextView, box: LevelOneEngine.BoxSlot) {
        textView.text = getString(R.string.bag_info_format, box.targetColor.displayName, box.filled, box.capacity, box.turnsLeft)
        textView.background = GradientDrawable().apply {
            setColor(Color.parseColor("#8D6E63"))
            setStroke(4, if (box.turnsLeft <= 5) Color.RED else Color.parseColor("#FFD54F"))
            cornerRadius = 12f
        }
        textView.setTextColor(Color.WHITE)
        textView.setPadding(8, 8, 8, 8)
    }

    private fun updateStatusUI() {
        if (engine.isGameOver) {
            if (engine.isWin) {
                binding.tvInstruction.text = "CHÚC MỪNG! THU HOẠCH XONG MÀN ${engine.levelId}"
                binding.tvInstruction.setTextColor(Color.GREEN)
                binding.btnNextLevel.isVisible = true 
            } else {
                binding.tvInstruction.text = "HẾT THỜI GIAN THU HOẠCH!"
                binding.tvInstruction.setTextColor(Color.RED)
                binding.btnNextLevel.isVisible = false
            }
        } else {
            binding.tvInstruction.text = when {
                engine.levelId >= 120 -> getString(R.string.instruction_ice)
                engine.levelId >= 80 -> getString(R.string.instruction_cobweb)
                engine.levelId >= 20 -> getString(R.string.instruction_hidden)
                else -> getString(R.string.instruction_default)
            }
            binding.tvInstruction.setTextColor(Color.WHITE)
            binding.btnNextLevel.isVisible = false
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        if (mediaPlayer != null) {
            // Resume nhạc đã bị pause
            mediaPlayer?.start()
        } else {
            // Khởi tạo lại nếu MediaPlayer bị mất (ví dụ sau khi app bị kill background)
            playBackgroundMusic(currentLevelId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
        _binding = null
    }
}
