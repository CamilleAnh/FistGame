package com.example.a2dgame

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.a2dgame.databinding.FragmentLevelOneBinding

/**
 * Fragment hiển thị gameplay các màn chơi.
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
            
            val bundle = Bundle().apply {
                putInt("levelId", nextLevelId)
            }
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.LevelOneFragment, true)
                .build()
                
            findNavController().navigate(R.id.action_LevelOneFragment_self, bundle, navOptions)
        }
    }

    private fun saveHighestLevel(level: Int) {
        val prefs = requireContext().getSharedPreferences("game_prefs", 0)
        val currentHighest = prefs.getInt("highest_level", 1)
        if (level > currentHighest) {
            prefs.edit().putInt("highest_level", level).apply()
        }
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
            
            // Xử lý hiển thị túi A
            if (boxSlots.isNotEmpty()) {
                binding.tvBoxA.isVisible = true
                updateBoxUI(binding.tvBoxA, boxSlots[0])
                // Nếu chỉ còn 1 túi, cho nó chiếm toàn bộ chiều ngang
                val params = binding.tvBoxA.layoutParams as LinearLayout.LayoutParams
                params.weight = if (boxSlots.size == 1) 2f else 1f
                binding.tvBoxA.layoutParams = params
            } else {
                binding.tvBoxA.isVisible = false
            }

            // Xử lý hiển thị túi B
            if (boxSlots.size >= 2) {
                binding.tvBoxB.isVisible = true
                updateBoxUI(binding.tvBoxB, boxSlots[1])
            } else {
                binding.tvBoxB.isVisible = false
            }
        }
        
        binding.glGameBoard.columnCount = if (activeTubes.size <= 4) 2 else 3

        activeTubes.forEach { tube ->
            val index = tube.id
            val tubeLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM
                val params = GridLayout.LayoutParams().apply {
                    width = 130 
                    height = 420
                    setMargins(20, 25, 20, 25)
                }
                layoutParams = params
                
                val border = GradientDrawable().apply {
                    setColor(Color.parseColor("#444444"))
                    setStroke(6, if (engine.selectedTubeIndex == index) Color.YELLOW else Color.WHITE)
                    cornerRadius = 15f
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

            tube.blocks.forEach { colorId ->
                val blockView = View(context).apply {
                    val bParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        85
                    ).apply { setMargins(0, 3, 0, 3) }
                    layoutParams = bParams
                    val shape = GradientDrawable().apply {
                        setColor(Color.parseColor(colorId.colorHex))
                        cornerRadius = 10f
                    }
                    background = shape
                }
                tubeLayout.addView(blockView)
            }
            binding.glGameBoard.addView(tubeLayout)
        }

        updateStatusUI()
    }

    private fun updateBoxUI(textView: android.widget.TextView, box: LevelOneEngine.BoxSlot) {
        textView.text = "${box.getName()}\n${box.filled}/${box.capacity}"
        val bg = GradientDrawable().apply {
            setColor(Color.parseColor(box.targetColor.colorHex))
            setStroke(6, Color.WHITE)
            cornerRadius = 16f
        }
        textView.background = bg
        textView.setTextColor(if (isColorDark(box.targetColor.colorHex)) Color.WHITE else Color.BLACK)
        textView.setPadding(10, 20, 10, 20)
    }

    private fun isColorDark(hex: String): Boolean {
        return try {
            val color = Color.parseColor(hex)
            val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
            darkness >= 0.5
        } catch (e: Exception) {
            true
        }
    }

    private fun updateStatusUI() {
        if (engine.isGameOver) {
            binding.tvInstruction.text = "CHÚC MỪNG! BẠN ĐÃ HOÀN THÀNH MÀN ${engine.levelId}"
            binding.tvInstruction.setTextColor(Color.GREEN)
            binding.btnNextLevel.isVisible = true
        } else {
            binding.tvInstruction.text = if (engine.levelId < 10) {
                "Màn ${engine.levelId}: Xếp các màu giống nhau vào cùng 1 ống."
            } else {
                "Màn ${engine.levelId}: Đóng gói các ống màu vào túi để thắng."
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
