package com.yourname.fruitsort

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.yourname.fruitsort.databinding.ActivityMainBinding
import com.google.android.gms.ads.MobileAds
import androidx.core.view.WindowCompat
import android.graphics.Color

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun attachBaseContext(newBase: Context) {
        // Load ngôn ngữ đã lưu trước khi khởi tạo Activity
        super.attachBaseContext(LanguageManager.loadLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bật chế độ Edge-to-Edge hiện đại
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // Khởi tạo Google Mobile Ads SDK, sau đó pre-load Rewarded + Interstitial
        MobileAds.initialize(this) {
            AdManager.initialize(this)
        }

        // Khởi tạo Google Play Billing (mua Remove Ads)
        BillingManager.initialize(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        GlobalMusicPlayer.releaseAll()
        BillingManager.destroy()
    }
}