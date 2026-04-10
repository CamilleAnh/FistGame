package com.example.a2dgame

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.a2dgame.databinding.FragmentShopBinding

class ShopFragment : Fragment() {

    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun setupUI() {
        binding.btnShopBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Nút Mua Reroll
        binding.btnBuyReroll.setOnClickListener {
            handlePurchase(
                price = GoldManager.PRICE_REROLL,
                buyAction = { GoldManager.buyReroll(requireContext()) }
            )
        }

        // Nút Mua Reveal
        binding.btnBuyReveal.setOnClickListener {
            handlePurchase(
                price = GoldManager.PRICE_REVEAL,
                buyAction = { GoldManager.buyReveal(requireContext()) }
            )
        }

        // Nút Mua Shuffle
        binding.btnBuyShuffle.setOnClickListener {
            handlePurchase(
                price = GoldManager.PRICE_SHUFFLE,
                buyAction = { GoldManager.buyShuffle(requireContext()) }
            )
        }

        refreshUI()
    }

    private fun handlePurchase(price: Int, buyAction: () -> Boolean) {
        val gold = GoldManager.getGold(requireContext())
        if (gold < price) {
            Toast.makeText(context, getString(R.string.shop_insufficient_gold), Toast.LENGTH_SHORT).show()
            // Shake animation cho tv_shop_gold để báo không đủ tiền
            shakeView(binding.tvShopGold)
            return
        }
        val success = buyAction()
        if (success) {
            Toast.makeText(context, getString(R.string.shop_purchase_success), Toast.LENGTH_SHORT).show()
            refreshUI()
            // Bounce animation cho gold text
            bounceView(binding.tvShopGold)
        }
    }

    private fun refreshUI() {
        val ctx = requireContext()
        val gold = GoldManager.getGold(ctx)
        binding.tvShopGold.text = getString(R.string.gold_display_format, gold)

        val rerollCount = GoldManager.getRerollCount(ctx)
        val revealCount = GoldManager.getRevealCount(ctx)
        val shuffleCount = GoldManager.getShuffleCount(ctx)

        binding.tvRerollOwned.text = getString(R.string.shop_owned_format, rerollCount)
        binding.tvRevealOwned.text = getString(R.string.shop_owned_format, revealCount)
        binding.tvShuffleOwned.text = getString(R.string.shop_owned_format, shuffleCount)

        // Dim nút khi không đủ tiền
        binding.btnBuyReroll.alpha = if (gold >= GoldManager.PRICE_REROLL) 1.0f else 0.5f
        binding.btnBuyReveal.alpha = if (gold >= GoldManager.PRICE_REVEAL) 1.0f else 0.5f
        binding.btnBuyShuffle.alpha = if (gold >= GoldManager.PRICE_SHUFFLE) 1.0f else 0.5f
    }

    private fun shakeView(view: View) {
        val shake = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0f, 12f, -12f, 8f, -8f, 0f).apply {
            duration = 400
        }
        shake.start()
    }

    private fun bounceView(view: View) {
        val pvhX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.3f, 1f)
        val pvhY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.3f, 1f)
        ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY).apply {
            duration = 350
            interpolator = OvershootInterpolator()
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
