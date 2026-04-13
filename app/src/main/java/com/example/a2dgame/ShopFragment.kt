package com.yourname.fruitsort

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
import com.yourname.fruitsort.databinding.FragmentShopBinding

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

        setupRemoveAds()

        // --- FREE GOLD SECTION ---

        // Nút Nhận Thưởng Hàng Ngày
        binding.btnClaimDaily.setOnClickListener {
            if (GoldManager.canClaimDaily(requireContext())) {
                val reward = GoldManager.claimDaily(requireContext())
                Toast.makeText(context, getString(R.string.gold_added_format, reward), Toast.LENGTH_SHORT).show()
                bounceView(binding.tvShopGold)
                refreshUI()
            } else {
                Toast.makeText(context, getString(R.string.shop_daily_claimed_toast), Toast.LENGTH_SHORT).show()
            }
        }

        // Nút Xem Ads Nhận Vàng
        binding.btnWatchAdGold.setOnClickListener {
            val watched = GoldManager.getAdsWatchedToday(requireContext())
            if (watched < GoldManager.MAX_DAILY_ADS) {
                binding.btnWatchAdGold.isEnabled = false
                binding.btnWatchAdGold.text = "⏳ Đang tải..."

                AdManager.showRewardedAd(
                    activity = requireActivity(),
                    onRewarded = {
                        if (_binding == null) return@showRewardedAd
                        GoldManager.watchAdForGold(requireContext())
                        Toast.makeText(context, getString(R.string.gold_added_format, GoldManager.REWARD_DAILY_AD), Toast.LENGTH_SHORT).show()
                        bounceView(binding.tvShopGold)
                        refreshUI()
                    },
                    onFailed = {
                        if (_binding == null) return@showRewardedAd
                        Toast.makeText(context, "Video chưa sẵn sàng, thử lại sau!", Toast.LENGTH_SHORT).show()
                        refreshUI()  // re-enable button
                    }
                )
            } else {
                Toast.makeText(context, getString(R.string.shop_ad_limit_reached), Toast.LENGTH_SHORT).show()
            }
        }

        // --- POWER-UPS SECTION ---

        binding.btnBuyReroll.setOnClickListener {
            handlePurchase(
                price = GoldManager.PRICE_REROLL,
                buyAction = { GoldManager.buyReroll(requireContext()) }
            )
        }

        binding.btnBuyReveal.setOnClickListener {
            handlePurchase(
                price = GoldManager.PRICE_REVEAL,
                buyAction = { GoldManager.buyReveal(requireContext()) }
            )
        }

        binding.btnBuyShuffle.setOnClickListener {
            handlePurchase(
                price = GoldManager.PRICE_SHUFFLE,
                buyAction = { GoldManager.buyShuffle(requireContext()) }
            )
        }

        refreshUI()
    }

    private fun setupRemoveAds() {
        refreshRemoveAdsUI()

        // Nút mua Remove Ads
        binding.btnBuyRemoveAds.setOnClickListener {
            if (GoldManager.isVip(requireContext())) return@setOnClickListener

            binding.btnBuyRemoveAds.isEnabled = false
            binding.btnBuyRemoveAds.text = "⏳ Đang xử lý..."

            BillingManager.launchPurchaseFlow(requireActivity()) { success, message ->
                if (_binding == null) return@launchPurchaseFlow
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                refreshRemoveAdsUI()
            }
        }

        // Nút khôi phục
        binding.tvRestorePurchases.setOnClickListener {
            binding.tvRestorePurchases.alpha = 0.5f
            BillingManager.restorePurchases(requireContext()) { restored, message ->
                if (_binding == null) return@restorePurchases
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                binding.tvRestorePurchases.alpha = 1.0f
                refreshRemoveAdsUI()
            }
        }
    }

    private fun refreshRemoveAdsUI() {
        if (_binding == null) return
        val isVip = GoldManager.isVip(requireContext())

        if (isVip) {
            // Đã mua
            binding.tvRemoveAdsActiveBadge.visibility = View.VISIBLE
            binding.btnBuyRemoveAds.isEnabled = false
            binding.btnBuyRemoveAds.text = getString(R.string.shop_remove_ads_active)
            binding.btnBuyRemoveAds.backgroundTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#388E3C"))
            binding.tvRestorePurchases.visibility = View.GONE
        } else {
            // Chưa mua – hiện giá thật từ Play Store
            binding.tvRemoveAdsActiveBadge.visibility = View.GONE
            binding.btnBuyRemoveAds.isEnabled = true
            val price = BillingManager.priceText
            binding.btnBuyRemoveAds.text = if (price == "...") getString(R.string.shop_remove_ads_loading)
                                           else "🚫 Xoá Quảng Cáo – $price"
            binding.btnBuyRemoveAds.backgroundTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFD54F"))
            binding.tvRestorePurchases.visibility = View.VISIBLE
        }
    }

    private fun handlePurchase(price: Int, buyAction: () -> Boolean) {
        val gold = GoldManager.getGold(requireContext())
        if (gold < price) {
            Toast.makeText(context, getString(R.string.shop_insufficient_gold), Toast.LENGTH_SHORT).show()
            shakeView(binding.tvShopGold)
            return
        }
        val success = buyAction()
        if (success) {
            Toast.makeText(context, getString(R.string.shop_purchase_success), Toast.LENGTH_SHORT).show()
            refreshUI()
            bounceView(binding.tvShopGold)
        }
    }

    private fun refreshUI() {
        if (_binding == null) return
        val ctx = requireContext()
        val gold = GoldManager.getGold(ctx)
        binding.tvShopGold.text = getString(R.string.gold_display_format, gold)

        // Update Daily & Ads status
        val canClaim = GoldManager.canClaimDaily(ctx)
        binding.btnClaimDaily.isEnabled = canClaim
        binding.btnClaimDaily.text = if (canClaim) getString(R.string.shop_btn_claim) else getString(R.string.shop_btn_claimed)
        binding.cardDailyClaim.alpha = if (canClaim) 1.0f else 0.6f

        val watched = GoldManager.getAdsWatchedToday(ctx)
        binding.tvAdLimit.text = getString(R.string.shop_ad_limit_format, watched, GoldManager.MAX_DAILY_ADS)
        binding.btnWatchAdGold.isEnabled = watched < GoldManager.MAX_DAILY_ADS
        binding.btnWatchAdGold.text = getString(R.string.shop_ad_reward, GoldManager.REWARD_DAILY_AD)
        binding.cardWatchAd.alpha = if (watched < GoldManager.MAX_DAILY_ADS) 1.0f else 0.6f

        // Update Power-ups
        val rerollCount = GoldManager.getRerollCount(ctx)
        val revealCount = GoldManager.getRevealCount(ctx)
        val shuffleCount = GoldManager.getShuffleCount(ctx)

        binding.tvRerollOwned.text = getString(R.string.shop_owned_format, rerollCount)
        binding.tvRevealOwned.text = getString(R.string.shop_owned_format, revealCount)
        binding.tvShuffleOwned.text = getString(R.string.shop_owned_format, shuffleCount)

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
