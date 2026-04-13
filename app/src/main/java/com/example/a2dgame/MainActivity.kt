package com.yourname.fruitsort

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.yourname.fruitsort.databinding.ActivityMainBinding
import com.google.android.gms.ads.MobileAds

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun attachBaseContext(newBase: Context) {
        // Load ngôn ngữ đã lưu trước khi khởi tạo Activity
        super.attachBaseContext(LanguageManager.loadLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo Google Mobile Ads SDK, sau đó pre-load Rewarded + Interstitial
        MobileAds.initialize(this) {
            AdManager.initialize(this)
        }

        // Khởi tạo Google Play Billing (mua Remove Ads)
        BillingManager.initialize(this)

        // Hide system UI for full screen game experience
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        GlobalMusicPlayer.releaseAll()
        BillingManager.destroy()
    }
}