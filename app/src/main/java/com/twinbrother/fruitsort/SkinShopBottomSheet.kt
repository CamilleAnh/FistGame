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
import com.example.a2dgame.SkinManager

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
        val gold = GoldManager.getGold(requireContext())
        binding.tvSkinGold.text = "🪙 $gold"
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

                // Preview: hiển thị hình dạng hộp thực của skin
                b.viewSkinColor.background = SkinManager.makeBoxBodyDrawable(skin.style, density)
                b.tvSkinEmoji.text = skin.emoji

                // Name
                b.tvSkinName.text = skin.name

                // Price / status text
                when {
                    isSelected -> {
                        b.tvSkinPrice.text = "✓ Equipped"
                        b.tvSkinPrice.setTextColor(Color.parseColor("#4CAF50"))
                    }
                    isOwned -> {
                        b.tvSkinPrice.text = "Owned"
                        b.tvSkinPrice.setTextColor(Color.parseColor("#78909C"))
                    }
                    else -> {
                        b.tvSkinPrice.text = "🪙 ${skin.priceCoin}"
                        b.tvSkinPrice.setTextColor(Color.parseColor("#FF8F00"))
                    }
                }

                // Card border glow when selected
                b.root.strokeColor = if (isSelected)
                    Color.parseColor("#FFD700") else Color.parseColor("#FFE0B2")
                b.root.strokeWidth = if (isSelected) 3 else 1

                // Action button
                when {
                    isSelected -> {
                        b.btnSkinAction.text = "✓ Using"
                        b.btnSkinAction.isEnabled = false
                        b.btnSkinAction.setBackgroundColor(Color.parseColor("#4CAF50"))
                    }
                    isOwned -> {
                        b.btnSkinAction.text = "Equip"
                        b.btnSkinAction.isEnabled = true
                        b.btnSkinAction.setBackgroundColor(Color.parseColor("#2196F3"))
                        b.btnSkinAction.setOnClickListener {
                            SkinManager.setSelected(ctx, skin.id)
                            notifyDataSetChanged()
                        }
                    }
                    else -> {
                        val hasGold = GoldManager.getGold(ctx) >= skin.priceCoin
                        b.btnSkinAction.text = "Buy"
                        b.btnSkinAction.isEnabled = hasGold
                        b.btnSkinAction.setBackgroundColor(
                            if (hasGold) Color.parseColor("#FF8F00") else Color.parseColor("#90A4AE")
                        )
                        b.btnSkinAction.setOnClickListener {
                            if (SkinManager.purchase(ctx, skin.id)) {
                                SkinManager.setSelected(ctx, skin.id)
                                refreshGold()
                                notifyDataSetChanged()
                                Toast.makeText(ctx, "🎉 ${skin.name} unlocked!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(ctx, "Not enough gold 🪙", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}
