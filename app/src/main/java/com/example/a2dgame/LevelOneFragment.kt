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
 * Fragment hiển thị gameplay với logic Thắng/Thua rõ ràng.
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
        binding.tvLevelName.text = getString(R.string.level_name_format, levelId)
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
        binding.tvPackedProgress.text = engine.getProgressText()
        
        if (isBagEnabled) {
            val boxSlots = engine.getBoxSlots()
            if (boxSlots.isNotEmpty()) {
                binding.tvBoxA.isVisible = true
                updateBoxUI(binding.tvBoxA, boxSlots[0])
            } else binding.tvBoxA.isVisible = false

            if (boxSlots.size >= 2) {
                binding.tvBoxB.isVisible = true
                updateBoxUI(binding.tvBoxB, boxSlots[1])
            } else binding.tvBoxB.isVisible = false
        }
        
        binding.glGameBoard.columnCount = 4
        
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val padding = (48 * displayMetrics.density).toInt() 
        val tubeWidth = (screenWidth - padding) / 4

        activeTubes.forEach { tube ->
            val index = tube.id
            val tubeContainer = FrameLayout(requireContext()).apply {
                val params = GridLayout.LayoutParams().apply {
                    width = tubeWidth
                    height = (tubeWidth * 2.5).toInt() // GIẢM CHIỀU CAO ĐỂ TRÁNH TRÀN VIỀN
                    setMargins(4, 6, 4, 6)
                }
                layoutParams = params
            }

            val tubeLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                val border = GradientDrawable().apply {
                    setColor(Color.parseColor("#444444"))
                    setStroke(4, if (engine.selectedTubeIndex == index) Color.YELLOW else Color.WHITE)
                    cornerRadius = 12f
                }
                background = border
                setPadding(6, 6, 6, 8)
                
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

            tube.blocks.forEachIndexed { blockIdx, colorId ->
                val blockFrame = FrameLayout(requireContext()).apply {
                    val blockHeight = (tubeWidth * 0.55).toInt() // GIẢM CHIỀU CAO KHỐI
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, blockHeight).apply { setMargins(0, 2, 0, 2) }
                }
                val isHidden = blockIdx < tube.hiddenLayers
                val blockView = View(context).apply {
                    layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    val shape = GradientDrawable().apply {
                        setColor(if (isHidden) Color.DKGRAY else Color.parseColor(colorId.colorHex))
                        cornerRadius = 8f
                    }
                    background = shape
                }
                blockFrame.addView(blockView)
                if (isHidden) {
                    val tvHint = TextView(context).apply {
                        text = "?"
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER
                        textSize = 16f
                        setTypeface(null, Typeface.BOLD)
                    }
                    blockFrame.addView(tvHint)
                }
                tubeLayout.addView(blockFrame, 0)
            }
            tubeContainer.addView(tubeLayout)

            if (tube.isFrozen) {
                val ice = View(context).apply {
                    layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#88ADF4FF"))
                        cornerRadius = 12f
                        setStroke(4, Color.CYAN)
                    }
                }
                tubeContainer.addView(ice)
            }
            if (tube.hasCobweb) {
                val spider = TextView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    text = "🕸️"
                    textSize = 24f
                    gravity = Gravity.CENTER
                }
                tubeContainer.addView(spider)
            }
            binding.glGameBoard.addView(tubeContainer)
        }
        updateStatusUI()
    }

    private fun updateBoxUI(textView: TextView, box: LevelOneEngine.BoxSlot) {
        textView.text = "TÚI ${box.targetColor.displayName}\n${box.filled}/${box.capacity}\n(Lượt: ${box.turnsLeft})"
        textView.background = GradientDrawable().apply {
            setColor(Color.parseColor(box.targetColor.colorHex))
            setStroke(6, if (box.turnsLeft <= 3) Color.RED else Color.WHITE)
            cornerRadius = 16f
        }
        textView.setTextColor(if (isColorDark(box.targetColor.colorHex)) Color.WHITE else Color.BLACK)
        textView.setPadding(8, 12, 8, 12)
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
            if (engine.isWin) {
                binding.tvInstruction.text = getString(R.string.congratulations_format, engine.levelId)
                binding.tvInstruction.setTextColor(Color.GREEN)
                binding.btnNextLevel.isVisible = true 
            } else {
                binding.tvInstruction.text = getString(R.string.game_over_title)
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
            binding.tvInstruction.setTextColor(Color.parseColor("#AAAAAA"))
            binding.btnNextLevel.isVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
