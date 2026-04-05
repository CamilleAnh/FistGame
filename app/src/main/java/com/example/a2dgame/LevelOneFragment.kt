package com.example.a2dgame

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.a2dgame.databinding.FragmentLevelOneBinding

/**
 * Fragment hiển thị gameplay với đầy đủ hiệu ứng cơ chế đặc biệt.
 */
class LevelOneFragment : Fragment() {

    private var _binding: FragmentLevelOneBinding? = null
    private val binding get() = _binding!!
    
    private val args: LevelOneFragmentArgs by navArgs()
    private lateinit var engine: LevelOneEngine

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
        
        binding.tvLevelName.text = "LEVEL $levelId"
        renderBoard()

        binding.btnReset.setOnClickListener {
            engine = LevelOneEngine(levelId)
            renderBoard()
        }

        binding.btnBackLevelSelect.text = "🏠"
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

    private fun saveHighestLevel(level: Int) {
        val prefs = requireContext().getSharedPreferences("game_prefs", 0)
        val currentHighest = prefs.getInt("highest_level", 1)
        if (level > currentHighest) prefs.edit().putInt("highest_level", level).apply()
    }

    private fun renderBoard() {
        binding.glGameBoard.removeAllViews()
        val tubes = engine.getTubes()
        val activeTubes = tubes.filter { !it.isArchived }

        val isBagEnabled = engine.isBagMechanismEnabled
        binding.llBoxes.isVisible = isBagEnabled
        binding.tvPackedProgress.isVisible = true 
        
        val timerText = if (engine.turnsLeft != -1) " | Lượt: ${engine.turnsLeft}" else ""
        binding.tvPackedProgress.text = engine.getProgressText() + timerText
        
        if (isBagEnabled) {
            val boxSlots = engine.getBoxSlots()
            if (boxSlots.isNotEmpty()) {
                binding.tvBoxA.isVisible = true
                updateBoxUI(binding.tvBoxA, boxSlots[0])
                val params = binding.tvBoxA.layoutParams as LinearLayout.LayoutParams
                params.weight = if (boxSlots.size == 1) 2f else 1f
                binding.tvBoxA.layoutParams = params
            } else binding.tvBoxA.isVisible = false

            if (boxSlots.size >= 2) {
                binding.tvBoxB.isVisible = true
                updateBoxUI(binding.tvBoxB, boxSlots[1])
            } else binding.tvBoxB.isVisible = false
        }
        
        binding.glGameBoard.columnCount = if (activeTubes.size <= 4) 2 else 3

        activeTubes.forEach { tube ->
            val index = tube.id
            val tubeContainer = FrameLayout(requireContext()).apply {
                val params = GridLayout.LayoutParams().apply {
                    width = 140
                    height = 450
                    setMargins(15, 20, 15, 20)
                }
                layoutParams = params
            }

            val tubeLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                
                val border = GradientDrawable().apply {
                    setColor(Color.parseColor("#444444"))
                    setStroke(6, if (engine.selectedTubeIndex == index) Color.YELLOW else Color.WHITE)
                    cornerRadius = 20f
                }
                background = border
                setPadding(10, 10, 10, 15)
                
                setOnClickListener {
                    if (engine.handleTubeClick(index)) {
                        renderBoard()
                        if (engine.isGameOver) {
                            saveHighestLevel(args.levelId + 1)
                            Toast.makeText(context, "MÀN CHƠI HOÀN THÀNH!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            if (tube.capacity < 4) {
                tubeContainer.layoutParams.height = 250 
            }

            tube.blocks.forEachIndexed { blockIdx, colorId ->
                val blockFrame = FrameLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 90).apply { setMargins(0, 3, 0, 3) }
                }

                val blockView = View(context).apply {
                    layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    val isHidden = blockIdx < tube.hiddenLayers
                    val shape = GradientDrawable().apply {
                        setColor(if (isHidden) Color.DKGRAY else Color.parseColor(colorId.colorHex))
                        cornerRadius = 12f
                    }
                    background = shape
                }
                blockFrame.addView(blockView)

                if (blockIdx < tube.hiddenLayers) {
                    val tvHint = TextView(context).apply {
                        text = "?"
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER
                        textSize = 20f
                        setTypeface(null, Typeface.BOLD)
                    }
                    blockFrame.addView(tvHint)
                }
                
                tubeLayout.addView(blockFrame, 0)
            }
            tubeContainer.addView(tubeLayout)

            if (tube.isFrozen) {
                val iceOverlay = View(context).apply {
                    layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#88ADF4FF")) 
                        cornerRadius = 20f
                        setStroke(4, Color.CYAN)
                    }
                }
                tubeContainer.addView(iceOverlay)
            }

            if (tube.hasCobweb) {
                val cobwebText = TextView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    text = "🕸️"
                    textSize = 30f
                    gravity = Gravity.CENTER
                }
                tubeContainer.addView(cobwebText)
            }

            binding.glGameBoard.addView(tubeContainer)
        }
        updateStatusUI()
    }

    private fun updateBoxUI(textView: TextView, box: LevelOneEngine.BoxSlot) {
        textView.text = "TÚI ${box.targetColor.displayName}\n${box.filled}/${box.capacity}"
        textView.background = GradientDrawable().apply {
            setColor(Color.parseColor(box.targetColor.colorHex))
            setStroke(6, Color.WHITE)
            cornerRadius = 16f
        }
        textView.setTextColor(if (isColorDark(box.targetColor.colorHex)) Color.WHITE else Color.BLACK)
        textView.setPadding(10, 20, 10, 20)
    }

    private fun isColorDark(hex: String): Boolean {
        return try {
            val color = Color.parseColor(hex)
            val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
            darkness >= 0.5
        } catch (e: Exception) { true }
    }

    private fun updateStatusUI() {
        if (engine.isGameOver) {
            binding.tvInstruction.text = "CHÚC MỪNG! BẠN ĐÃ HOÀN THÀNH MÀN ${engine.levelId}"
            binding.tvInstruction.setTextColor(Color.GREEN)
            binding.btnNextLevel.isVisible = true 
        } else {
            binding.tvInstruction.text = when {
                engine.levelId >= 120 -> "Phá băng bằng cách đổ cùng màu vào ống."
                engine.levelId >= 80 -> "Chạm vào mạng nhện 🕸️ để dọn dẹp."
                engine.levelId >= 20 -> "Cẩn thận! Một số khối bị ẩn đáy (?)."
                else -> "Xếp các màu giống nhau vào cùng 1 ống."
            }
            binding.tvInstruction.setTextColor(Color.parseColor("#AAAAAA"))
            binding.btnNextLevel.isVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
