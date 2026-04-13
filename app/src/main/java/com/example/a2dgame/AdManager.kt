package com.yourname.fruitsort

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * AdManager – Singleton quản lý toàn bộ quảng cáo (Rewarded + Interstitial).
 *
 * Nguyên tắc (AD_SYSTEM_PRINCIPLES.md):
 *  - Pre-load ngay khi khởi động app.
 *  - Sau khi show xong, tự load bài tiếp theo.
 *  - Không bao giờ block UI chờ ad load.
 *  - Interstitial chỉ hiện mỗi INTERSTITIAL_INTERVAL lần chuyển level.
 *
 * Cách dùng:
 *  1. Gọi AdManager.initialize(context) trong MainActivity.onCreate()
 *  2. Gọi AdManager.showRewardedAd(activity, onRewarded, onFailed) khi cần Rewarded
 *  3. Gọi AdManager.onLevelTransition(activity, onDismissed) khi chuyển level
 */
object AdManager {

    private const val TAG = "AdManager"

    // ────────────────────────────────────────────────────────────────────
    // TODO (PRODUCTION): Thay các Test IDs dưới đây bằng Production IDs thật
    //   trước khi upload lên Google Play. Lấy tại:
    //   https://apps.admob.com → Apps → [Fruit Sort Puzzle] → Ad units
    //
    //   ⚠️ Test IDs chỉ dùng trong quá trình dev, KHÔNG kiếm được doanh thu thật!
    // ────────────────────────────────────────────────────────────────────
    private const val REWARDED_AD_UNIT_ID     = "ca-app-pub-3940256099942544/5224354917"  // Google Test Rewarded
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"  // Google Test Interstitial

    /** Hiển thị Interstitial sau mỗi bao nhiêu lần chuyển level */
    private const val INTERSTITIAL_INTERVAL = 3

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var levelTransitionCount = 0

    // ─────────────────────────────────────────────
    // INIT – Gọi 1 lần từ MainActivity.onCreate()
    // ─────────────────────────────────────────────

    fun initialize(context: Context) {
        loadRewardedAd(context)
        loadInterstitialAd(context)
    }

    // ─────────────────────────────────────────────
    // REWARDED AD
    // ─────────────────────────────────────────────

    fun loadRewardedAd(context: Context) {
        val request = AdRequest.Builder().build()
        RewardedAd.load(
            context.applicationContext,
            REWARDED_AD_UNIT_ID,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded.")
                    rewardedAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Rewarded ad failed to load: ${error.message}")
                    rewardedAd = null
                }
            }
        )
    }

    /**
     * Hiển thị Rewarded Ad nếu đã sẵn sàng.
     * onRewarded – Người dùng đã xem đủ, cấp phần thưởng.
     * onFailed   – Ad chưa load / thất bại; gọi ngay, KHÔNG cấp thưởng.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onFailed: () -> Unit
    ) {
        // VIP: skip ad, cấp thưởng luôn
        if (GoldManager.isVip(activity)) {
            onRewarded()
            return
        }

        val ad = rewardedAd
        if (ad == null) {
            Log.w(TAG, "Rewarded ad not ready.")
            loadRewardedAd(activity)  // pre-load lần sau
            onFailed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewardedAd(activity)  // tự load lại ngay
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Rewarded failed to show: ${error.message}")
                rewardedAd = null
                loadRewardedAd(activity)
                onFailed()
            }
        }

        ad.show(activity) { _ ->
            // RewardItem không quan trọng loại – luôn cấp thưởng khi callback này được gọi
            onRewarded()
        }
    }

    // ─────────────────────────────────────────────
    // INTERSTITIAL AD
    // ─────────────────────────────────────────────

    fun loadInterstitialAd(context: Context) {
        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            context.applicationContext,
            INTERSTITIAL_AD_UNIT_ID,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded.")
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Interstitial failed to load: ${error.message}")
                    interstitialAd = null
                }
            }
        )
    }

    /**
     * Gọi khi người dùng chuyển sang level mới.
     * Tự động hiển thị Interstitial sau mỗi INTERSTITIAL_INTERVAL lần.
     * onDismissed – Gọi sau khi ad đóng (hoặc ngay nếu không show).
     */
    fun onLevelTransition(activity: Activity, onDismissed: () -> Unit) {
        // VIP: skip Interstitial hoàn toàn
        if (GoldManager.isVip(activity)) {
            onDismissed()
            return
        }

        levelTransitionCount++
        val shouldShow = (levelTransitionCount % INTERSTITIAL_INTERVAL == 0)
        val ad = interstitialAd

        if (shouldShow && ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onDismissed()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Interstitial failed to show: ${error.message}")
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onDismissed()
                }
            }
            ad.show(activity)
        } else {
            onDismissed()
        }
    }
}
