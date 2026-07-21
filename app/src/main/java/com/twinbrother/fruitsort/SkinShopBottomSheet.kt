package com.twinbrother.fruitsort

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.twinbrother.fruitsort.databinding.DialogSkinShopBinding
import com.twinbrother.fruitsort.databinding.ItemSkinCardBinding

/**
 * SkinShopBottomSheet – Popup chọn & mua skin xe tải.
 * Hiển thị 2 cột skin, mỗi card có preview + nút mua/trang bị.
 */
class SkinShopBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DialogSkinShopBinding? = null
    private val binding get() = _binding!!

    /** Callback thông báo SecondFragment khi dialog đóng (để refresh gold) */
    var onDismissCallback: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSkinShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshGold()

        val adapter = SkinAdapter()
        binding.rvSkins.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSkins.adapter = adapter
    }

    private fun refreshGold() {
        if (_binding == null) return
        val ctx = requireContext()
        val gold = GoldManager.getGold(ctx)
        binding.tvSkinGold.text = "🪙 %,d".format(gold)
        val gems = GoldManager.getGems(ctx)
        binding.tvSkinGems.text = "💎 %,d".format(gems)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ─── Inner Adapter ─────────────────────────────────────────────────────

    inner class SkinAdapter : RecyclerView.Adapter<SkinAdapter.SkinViewHolder>() {

        override fun getItemCount() = SkinManager.ALL_SKINS.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkinViewHolder {
            val b = ItemSkinCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SkinViewHolder(b)
        }

        override fun onBindViewHolder(holder: SkinViewHolder, position: Int) {
            holder.bind(SkinManager.ALL_SKINS[position])
        }

        inner class SkinViewHolder(val b: ItemSkinCardBinding) :
            RecyclerView.ViewHolder(b.root) {

            fun bind(skin: SkinManager.SkinItem) {
                val ctx = b.root.context
                val density = ctx.resources.displayMetrics.density
                val isOwned    = SkinManager.isOwned(ctx, skin.id)
                val isSelected = SkinManager.getSelectedId(ctx) == skin.id

                // Rarity Badge
                b.tvSkinRarity.text = skin.rarity.label
                val badgeDrawable = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    setColor(skin.rarity.badgeColor)
                    cornerRadius = 8 * density
                }
                b.tvSkinRarity.background = badgeDrawable

                // Preview: hiển thị hình dạng hộp thực của skin
                b.viewSkinColor.background = SkinManager.makeBoxBodyDrawable(skin.style, density)
                b.tvSkinEmoji.text = skin.emoji

                // Name
                b.tvSkinName.text = skin.name

                // Price / status text
                when {
                    isSelected -> {
                        b.tvSkinPrice.text = "✓ EQUIPPED"
                        b.tvSkinPrice.setTextColor(Color.parseColor("#4CAF50"))
                    }
                    isOwned -> {
                        b.tvSkinPrice.text = "OWNED"
                        b.tvSkinPrice.setTextColor(Color.parseColor("#78909C"))
                    }
                    else -> {
                        b.tvSkinPrice.text = "🪙 ${skin.priceCoin} / 💎 ${skin.priceGem}"
                        b.tvSkinPrice.setTextColor(Color.parseColor("#FF8F00"))
                    }
                }

                // Card border glow when selected
                b.root.strokeColor = if (isSelected)
                    Color.parseColor("#FFD700") else Color.parseColor("#3D376A")
                b.root.strokeWidth = if (isSelected) (2.5f * density).toInt() else (1.5f * density).toInt()
                b.root.setCardBackgroundColor(
                    if (isSelected) Color.parseColor("#2A2450") else Color.parseColor("#1A1835")
                )

                // Action button
                when {
                    isSelected -> {
                        b.btnSkinAction.text = "✓ USING"
                        b.btnSkinAction.isEnabled = false
                        b.btnSkinAction.setBackgroundColor(Color.parseColor("#4CAF50"))
                    }
                    isOwned -> {
                        b.btnSkinAction.text = "EQUIP"
                        b.btnSkinAction.isEnabled = true
                        b.btnSkinAction.setBackgroundColor(Color.parseColor("#2196F3"))
                        b.btnSkinAction.setOnClickListener {
                            SkinManager.setSelected(ctx, skin.id)
                            notifyDataSetChanged()
                        }
                    }
                    else -> {
                        val hasGold = GoldManager.getGold(ctx) >= skin.priceCoin
                        val hasGems = GoldManager.getGems(ctx) >= skin.priceGem
                        val canBuy = hasGold || hasGems

                        b.btnSkinAction.text = if (hasGold) "BUY (🪙)" else if (hasGems) "BUY (💎)" else "BUY"
                        b.btnSkinAction.isEnabled = canBuy
                        b.btnSkinAction.setBackgroundColor(
                            if (canBuy) Color.parseColor("#FF8F00") else Color.parseColor("#90A4AE")
                        )
                        b.btnSkinAction.setOnClickListener {
                            var success = false
                            if (hasGold) {
                                success = SkinManager.purchase(ctx, skin.id)
                            } else if (hasGems) {
                                success = SkinManager.purchaseWithGems(ctx, skin.id)
                            }

                            if (success) {
                                SkinManager.setSelected(ctx, skin.id)
                                refreshGold()
                                notifyDataSetChanged()
                                Toast.makeText(ctx, "🎉 ${skin.name} Unlocked!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(ctx, "Insufficient Gold or Gems 🪙💎", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}
