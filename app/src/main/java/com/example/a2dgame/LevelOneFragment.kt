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
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.a2dgame.databinding.FragmentLevelOneBinding

/**
 * Fragment hiển thị gameplay Màn 1: Color Sort.
 * Tuân thủ thiết kế 100% Android Native View.
 */
class LevelOneFragment : Fragment() {

    private var _binding: FragmentLevelOneBinding? = null
    private val binding get() = _binding!!
    
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
        
        engine = LevelOneEngine()
        
        renderBoard()

        binding.btnReset.setOnClickListener {
            engine = LevelOneEngine()
            renderBoard()
        }
    }

    /**
     * Render các ống (Tubes) bằng LinearLayout và TextView.
     */
    private fun renderBoard() {
        binding.glGameBoard.removeAllViews()
        val tubes = engine.getTubes()
        
        tubes.forEachIndexed { index, tube ->
            val tubeLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM
                val params = GridLayout.LayoutParams().apply {
                    width = 120 // Độ rộng ống
                    height = 400 // Chiều cao ống
                    setMargins(20, 20, 20, 20)
                }
                layoutParams = params
                
                // Vẽ khung ống (Tube border)
                val border = GradientDrawable().apply {
                    setColor(Color.parseColor("#444444"))
                    setStroke(4, if (engine.selectedTubeIndex == index) Color.YELLOW else Color.WHITE)
                    cornerRadius = 10f
                }
                background = border
                setPadding(10, 10, 10, 10)
                
                setOnClickListener {
                    if (engine.handleTubeClick(index)) {
                        renderBoard()
                        if (engine.isGameOver) {
                            Toast.makeText(context, "LEVEL COMPLETE!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            // Vẽ các khối màu trong ống (từ dưới lên trên)
            // Vì layout là Gravity.BOTTOM, ta add theo thứ tự trong Stack
            tube.blocks.forEach { colorId ->
                val blockView = View(context).apply {
                    val bParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        80 // Chiều cao mỗi khối
                    ).apply {
                        setMargins(0, 2, 0, 2)
                    }
                    layoutParams = bParams
                    
                    val shape = GradientDrawable().apply {
                        setColor(Color.parseColor(colorId.colorHex))
                        cornerRadius = 8f
                    }
                    background = shape
                }
                tubeLayout.addView(blockView)
            }
            
            binding.glGameBoard.addView(tubeLayout)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}