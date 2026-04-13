package com.yourname.fruitsort

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

/**
 * Privacy Policy screen – hiển thị nội dung chính sách bảo mật trong WebView.
 *
 * Google Play yêu cầu bắt buộc với mọi app có quảng cáo hoặc thu thập dữ liệu.
 *
 * TODO (PRODUCTION): Thay PRIVACY_POLICY_URL bằng URL trang Privacy Policy thật của bạn
 *   - Có thể tạo miễn phí tại: https://app.termly.io hoặc https://privacypolicygenerator.info
 *   - Hoặc host file HTML trên GitHub Pages / Notion / trang cá nhân.
 */
class PrivacyPolicyFragment : Fragment() {

    // TODO (PRODUCTION): Đặt URL thật của trang Privacy Policy vào đây
    private val PRIVACY_POLICY_URL = "https://example.com/privacy-policy"

    // Fallback: nội dung HTML nội tuyến dùng khi chưa có URL thật / offline
    private val FALLBACK_HTML = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
          body { font-family: Arial, sans-serif; padding: 20px; line-height: 1.6; color: #222; }
          h1 { color: #2e7d32; font-size: 22px; }
          h2 { color: #388e3c; font-size: 18px; margin-top: 24px; }
          p { margin: 8px 0; }
        </style>
        </head>
        <body>
        <h1>Privacy Policy – Fruit Sort Puzzle</h1>
        <p><em>Last updated: April 2026</em></p>

        <h2>1. Information We Collect</h2>
        <p>Fruit Sort Puzzle does not collect personal information directly.
        However, we use Google AdMob for advertising, which may collect
        device identifiers and usage data to serve relevant ads.</p>

        <h2>2. Advertising</h2>
        <p>We use Google AdMob to display ads. AdMob may use cookies or
        similar technologies and may share data with third parties.
        Please refer to <a href="https://policies.google.com/privacy">
        Google's Privacy Policy</a> for details.</p>

        <h2>3. Data Storage</h2>
        <p>Game progress (levels unlocked, gold balance) is stored locally
        on your device only and is never transmitted to any server.</p>

        <h2>4. Children's Privacy</h2>
        <p>This app is intended for general audiences. We do not knowingly
        collect personal information from children under 13.</p>

        <h2>5. Contact Us</h2>
        <p>If you have questions about this privacy policy, please contact us at:
        <br><strong>your-email@example.com</strong></p>
        </body>
        </html>
    """.trimIndent()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_privacy_policy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val webView = view.findViewById<WebView>(R.id.webview_privacy)
        val progressBar = view.findViewById<ProgressBar>(R.id.pb_privacy_loading)
        val btnBack = view.findViewById<android.widget.ImageButton>(R.id.btn_privacy_back)

        btnBack.setOnClickListener { findNavController().popBackStack() }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
            }
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                // Nếu không tải được URL, hiển thị fallback HTML
                webView.loadDataWithBaseURL(null, FALLBACK_HTML, "text/html", "UTF-8", null)
            }
        }

        webView.settings.apply {
            javaScriptEnabled = false
            builtInZoomControls = false
        }

        // Thử load URL thật, nếu fail sẽ trigger onReceivedError → fallback
        webView.loadUrl(PRIVACY_POLICY_URL)
    }
}
