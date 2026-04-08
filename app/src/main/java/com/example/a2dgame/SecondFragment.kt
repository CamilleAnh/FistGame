package com.example.a2dgame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.a2dgame.databinding.FragmentSecondBinding
import com.example.a2dgame.databinding.ItemLevelBinding
import com.example.a2dgame.databinding.ItemLevelPageBinding
import com.google.android.material.tabs.TabLayoutMediator

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private val levelsPerPage = 20
    private val totalPages = 5 // 100 levels

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("game_prefs", 0)
        val highestLevel = prefs.getInt("highest_level", 1)

        val adapter = LevelPagerAdapter(highestLevel) { levelId ->
            val bundle = Bundle().apply { putInt("levelId", levelId) }
            findNavController().navigate(R.id.action_SecondFragment_to_LevelOneFragment, bundle)
        }
        
        binding.vpLevels.adapter = adapter

        // Kết nối ViewPager2 với TabLayout (Chỉ báo dấu chấm)
        TabLayoutMediator(binding.pageIndicator, binding.vpLevels) { _, _ -> }.attach()

        binding.btnBackHome.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        binding.btnNext.setOnClickListener {
            if (binding.vpLevels.currentItem < totalPages - 1) {
                binding.vpLevels.currentItem += 1
            }
        }

        binding.btnPrev.setOnClickListener {
            if (binding.vpLevels.currentItem > 0) {
                binding.vpLevels.currentItem -= 1
            }
        }
        
        // Cập nhật trạng thái nút khi đổi trang
        binding.vpLevels.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.btnPrev.alpha = if (position > 0) 1.0f else 0.5f
                binding.btnPrev.isEnabled = position > 0
                binding.btnNext.alpha = if (position < totalPages - 1) 1.0f else 0.5f
                binding.btnNext.isEnabled = position < totalPages - 1
            }
        })
    }

    inner class LevelPagerAdapter(
        private val highestLevel: Int,
        private val onLevelClick: (Int) -> Unit
    ) : RecyclerView.Adapter<LevelPagerAdapter.PageViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
            val binding = ItemLevelPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return PageViewHolder(binding)
        }

        override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
            holder.bind(position)
        }

        override fun getItemCount(): Int = totalPages

        inner class PageViewHolder(val pageBinding: ItemLevelPageBinding) : RecyclerView.ViewHolder(pageBinding.root) {
            fun bind(pageIndex: Int) {
                pageBinding.glLevels.removeAllViews()
                val startLevel = (pageIndex * levelsPerPage) + 1
                
                for (i in 0 until levelsPerPage) {
                    val levelId = startLevel + i
                    val itemBinding = ItemLevelBinding.inflate(LayoutInflater.from(pageBinding.root.context), pageBinding.glLevels, false)
                    
                    itemBinding.tvLevelNumber.text = levelId.toString()
                    
                    if (levelId <= highestLevel) {
                        itemBinding.flLevelItem.setBackgroundResource(R.drawable.level_item_unlocked)
                        itemBinding.tvLevelNumber.visibility = View.VISIBLE
                        itemBinding.ivLock.visibility = View.GONE
                        itemBinding.flLevelItem.setOnClickListener { onLevelClick(levelId) }
                    } else {
                        itemBinding.flLevelItem.setBackgroundResource(R.drawable.level_item_locked)
                        itemBinding.tvLevelNumber.visibility = View.GONE
                        itemBinding.ivLock.visibility = View.VISIBLE
                        itemBinding.flLevelItem.setOnClickListener(null)
                    }
                    
                    pageBinding.glLevels.addView(itemBinding.root)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
