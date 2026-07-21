package com.twinbrother.fruitsort

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.twinbrother.fruitsort.databinding.FragmentSecondBinding
import com.twinbrother.fruitsort.databinding.ItemLevelCardBinding

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private val totalLevels = 1000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startBgMusic()

        setupLevelList()
        updateCurrencyDisplay()
        setupNavBar()
    }

    // ─── Level RecyclerView ───────────────────────────────────────────────────

    private fun setupLevelList() {
        val prefs         = requireContext().getSharedPreferences("game_prefs", 0)
        val highestLevel  = prefs.getInt("highest_level", 1)

        val adapter = LevelCardAdapter(
            totalLevels  = totalLevels,
            highestLevel = highestLevel
        ) { levelId ->
            val bundle = Bundle().apply { putInt("levelId", levelId) }
            findNavController().navigate(R.id.action_SecondFragment_to_LevelOneFragment, bundle)
        }

        binding.rvLevels.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter  = adapter
            setHasFixedSize(true)
        }

        // Scroll to current progress (show 2 levels above highest)
        if (highestLevel > 3) {
            (binding.rvLevels.layoutManager as? LinearLayoutManager)
                ?.scrollToPositionWithOffset(highestLevel - 2, 0)
        }
    }

    // ─── Currency display ─────────────────────────────────────────────────────

    private fun updateCurrencyDisplay() {
        if (_binding == null) return
        val gold = GoldManager.getGold(requireContext())
        binding.tvGold.text = "🪙 ${formatGold(gold)}"
        val gems = GoldManager.getGems(requireContext())
        binding.tvGems.text = "💎 ${formatGold(gems)}"
    }

    private fun formatGold(gold: Int): String =
        if (gold >= 10000) "${gold / 1000}K" else gold.toString()

    // ─── Bottom navigation bar ────────────────────────────────────────────────

    private fun setupNavBar() {
        // Home → back to main menu
        binding.navBtnHome.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        // Play / Levels → already here, scroll to current active level
        binding.navBtnPlay.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("game_prefs", android.content.Context.MODE_PRIVATE)
            val highest = prefs.getInt("highest_level", 1)
            val targetPos = (highest - 1).coerceAtLeast(0)
            binding.rvLevels.smoothScrollToPosition(targetPos)
        }

        // Shop → ShopFragment
        binding.navBtnShop.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_ShopFragment)
        }

        // Skins → show bottom sheet
        binding.navBtnSkins.setOnClickListener {
            val sheet = SkinShopBottomSheet()
            sheet.onDismissCallback = { updateCurrencyDisplay() }
            sheet.show(childFragmentManager, "SkinShop")
        }
    }

    // ─── Music ───────────────────────────────────────────────────────────────

    private fun startBgMusic() {
        GlobalMusicPlayer.playIfEnabled(requireContext(), R.raw.nhacnen)
    }

    override fun onResume() {
        super.onResume()
        startBgMusic()
        updateCurrencyDisplay()       // Refresh gold after returning from shop
    }

    override fun onPause() {
        super.onPause()
        GlobalMusicPlayer.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ─── Companion ────────────────────────────────────────────────────────────

    companion object {
        /** Fruit / vegetable icons cycling per level */
        val FRUIT_ICONS = listOf(
            "🌽", "🍍", "🥕", "🍓", "🍉", "🍇", "🍎", "🍊",
            "🍋", "🫐", "🥝", "🍑", "🍒", "🍆", "🥦",
            "🌶️", "🍅", "🥑", "🫒", "🍌"
        )
    }

    // ─── Level Card Adapter ──────────────────────────────────────────────────

    private inner class LevelCardAdapter(
        private val totalLevels:  Int,
        private val highestLevel: Int,
        private val onLevelClick: (Int) -> Unit
    ) : RecyclerView.Adapter<LevelCardAdapter.LevelViewHolder>() {

        override fun getItemCount() = totalLevels

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelViewHolder {
            val b = ItemLevelCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return LevelViewHolder(b)
        }

        override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
            holder.bind(position + 1)   // levels are 1-indexed
        }

        inner class LevelViewHolder(val b: ItemLevelCardBinding) :
            RecyclerView.ViewHolder(b.root) {

            fun bind(levelId: Int) {
                // Badge & fruit
                b.tvLevelBadge.text  = levelId.toString()
                b.tvFruitIcon.text   = FRUIT_ICONS[(levelId - 1) % FRUIT_ICONS.size]
                b.tvLevelName.text   = b.root.context.getString(R.string.level_name_format, levelId)

                when {
                    levelId < highestLevel -> {
                        // ✅ Completed — show actual stars earned
                        val savedStars = GoldManager.getLevelStars(b.root.context, levelId)
                        b.tvStars.text = when {
                            savedStars >= 3 -> "⭐⭐⭐"
                            savedStars == 2 -> "⭐⭐☆"
                            savedStars == 1 -> "⭐☆☆"
                            else -> "⭐⭐⭐" // default for levels completed before star system
                        }
                        b.lockOverlay.visibility  = View.GONE
                        b.ivLockIcon.visibility   = View.GONE
                        b.root.setCardBackgroundColor(Color.parseColor("#FFF3E0"))
                        b.root.strokeColor   = Color.parseColor("#FFCC80")
                        b.root.strokeWidth   = 1
                        b.root.isClickable   = true
                        b.root.setOnClickListener { onLevelClick(levelId) }
                    }
                    levelId == highestLevel -> {
                        // 🎮 Current level — orange border, no stars yet
                        b.tvStars.text       = "☆☆☆"
                        b.lockOverlay.visibility  = View.GONE
                        b.ivLockIcon.visibility   = View.GONE
                        b.root.setCardBackgroundColor(Color.parseColor("#FFF8E1"))
                        b.root.strokeColor   = Color.parseColor("#FF8F00")
                        b.root.strokeWidth   = 3
                        b.root.isClickable   = true
                        b.root.setOnClickListener { onLevelClick(levelId) }
                    }
                    else -> {
                        // 🔒 Locked — grey, no click
                        b.tvStars.text       = ""
                        b.lockOverlay.visibility  = View.VISIBLE
                        b.ivLockIcon.visibility   = View.VISIBLE
                        b.root.setCardBackgroundColor(Color.parseColor("#ECEFF1"))
                        b.root.strokeColor   = Color.parseColor("#B0BEC5")
                        b.root.strokeWidth   = 1
                        b.root.isClickable   = false
                        b.root.setOnClickListener(null)
                    }
                }
            }
        }
    }
}
