package com.example.a2dgame

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.a2dgame.databinding.FragmentSecondBinding

/**
 * Fragment màn hình chọn Level.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private var currentPage = 0
    private val levelsPerPage = 20 // 4x5 grid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Quay lại màn hình chính
        binding.btnBackHome.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        binding.btnNext.setOnClickListener {
            currentPage++
            renderLevels()
        }

        binding.btnPrev.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                renderLevels()
            }
        }

        renderLevels()
    }

    private fun renderLevels() {
        val prefs = requireContext().getSharedPreferences("game_prefs", 0)
        val highestLevel = prefs.getInt("highest_level", 1)

        // Cập nhật trạng thái nút Next/Prev
        binding.btnPrev.isEnabled = currentPage > 0
        binding.btnPrev.alpha = if (currentPage > 0) 1.0f else 0.5f
        
        // Giả sử tối đa 5 trang (100 levels)
        binding.btnNext.isEnabled = currentPage < 4
        binding.btnNext.alpha = if (currentPage < 4) 1.0f else 0.5f

        for (i in 0 until binding.glLevels.childCount) {
            val child = binding.glLevels.getChildAt(i)
            if (child is FrameLayout) {
                // Tính toán levelId dựa trên trang hiện tại
                val levelId = (currentPage * levelsPerPage) + i + 1
                
                // Đảm bảo có TextView và ImageView bên trong FrameLayout
                var textView = child.getChildAt(0) as? TextView
                if (textView == null) {
                    textView = TextView(context).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            android.view.Gravity.CENTER
                        )
                        setTextColor(android.graphics.Color.WHITE)
                        textSize = 20f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }
                    child.addView(textView)
                }

                var lockIcon = if (child.childCount > 1) child.getChildAt(1) as? ImageView else null
                if (lockIcon == null) {
                    lockIcon = ImageView(context).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            (30 * resources.displayMetrics.density).toInt(),
                            (30 * resources.displayMetrics.density).toInt(),
                            android.view.Gravity.CENTER
                        )
                        setImageResource(android.R.drawable.ic_lock_lock)
                        setColorFilter(android.graphics.Color.WHITE)
                    }
                    child.addView(lockIcon)
                }

                textView.text = levelId.toString()

                if (levelId <= highestLevel) {
                    // Màn hình đã mở khóa
                    child.setBackgroundResource(R.drawable.level_item_unlocked)
                    textView.visibility = View.VISIBLE
                    lockIcon.visibility = View.GONE
                    
                    child.setOnClickListener {
                        val bundle = Bundle().apply {
                            putInt("levelId", levelId)
                        }
                        findNavController().navigate(R.id.action_SecondFragment_to_LevelOneFragment, bundle)
                    }
                } else {
                    // Màn hình đang bị khóa
                    child.setBackgroundResource(R.drawable.level_item_locked)
                    textView.visibility = View.GONE
                    lockIcon.visibility = View.VISIBLE
                    child.setOnClickListener(null)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
