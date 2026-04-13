package com.yourname.fruitsort

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.*

/**
 * BillingManager – Singleton quản lý Google Play In-App Purchase.
 *
 * Sản phẩm: "remove_ads" – một lần mua, vĩnh viễn (NON_CONSUMABLE / ONE_TIME_PURCHASE)
 *
 * Cách dùng:
 *   1. Gọi BillingManager.initialize(context) trong MainActivity.onCreate()
 *   2. Gọi BillingManager.launchPurchaseFlow(activity, onResult) để mở hộp thoại mua
 *   3. Gọi BillingManager.restorePurchases(context, onResult) để khôi phục
 *   4. Kiểm tra GoldManager.isVip(context) để biết đã mua chưa
 *
 * TODO (PRODUCTION): Tạo sản phẩm "remove_ads" trên Google Play Console trước khi release:
 *   Play Console → [App] → Monetize → Products → In-app products → Create product
 *   Product ID: remove_ads   |   Type: One-time   |   Price: ~19,000đ (~$0.99)
 */
object BillingManager : PurchasesUpdatedListener {

    private const val TAG = "BillingManager"

    // TODO (PRODUCTION): Phải khớp chính xác với Product ID tạo trên Play Console
    const val PRODUCT_REMOVE_ADS = "remove_ads"

    private var billingClient: BillingClient? = null
    private var productDetails: ProductDetails? = null
    private var applicationContext: Context? = null

    // Callback khi mua thành công / thất bại – set tạm thời khi user nhấn mua
    private var purchaseCallback: ((success: Boolean, message: String) -> Unit)? = null

    val isReady: Boolean get() = billingClient?.isReady == true
    val priceText: String get() = productDetails?.oneTimePurchaseOfferDetails?.formattedPrice ?: "..."

    // ─────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
        val client = BillingClient.newBuilder(context.applicationContext)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .build()
        billingClient = client

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing connected.")
                    queryProductDetails()
                    // Xử lý các giao dịch pending (nếu có từ lần trước)
                    restorePurchasesInternal(context.applicationContext)
                } else {
                    Log.w(TAG, "Billing setup failed: ${result.debugMessage}")
                }
            }
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing disconnected. Will retry on next operation.")
                billingClient = null
            }
        })
    }

    // ─────────────────────────────────────────────
    // QUERY PRODUCT DETAILS (lấy giá thật từ Play)
    // ─────────────────────────────────────────────

    private fun queryProductDetails() {
        val client = billingClient ?: return
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_REMOVE_ADS)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { result, detailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetails = detailsList.firstOrNull()
                Log.d(TAG, "Product loaded: ${productDetails?.name} – ${productDetails?.oneTimePurchaseOfferDetails?.formattedPrice}")
            } else {
                Log.w(TAG, "Query product failed: ${result.debugMessage}")
            }
        }
    }

    // ─────────────────────────────────────────────
    // LAUNCH PURCHASE FLOW
    // ─────────────────────────────────────────────

    /**
     * Mở hộp thoại mua của Google Play.
     * onResult(success, message): gọi trên main thread sau khi mua xong.
     */
    fun launchPurchaseFlow(
        activity: Activity,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val client = billingClient
        if (client == null || !client.isReady) {
            onResult(false, "Google Play chưa sẵn sàng, thử lại sau.")
            return
        }

        val details = productDetails
        if (details == null) {
            // Thử query lại rồi thông báo
            queryProductDetails()
            onResult(false, "Đang tải thông tin sản phẩm, thử lại sau vài giây.")
            return
        }

        purchaseCallback = onResult

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val result = client.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            purchaseCallback = null
            onResult(false, "Không thể mở cửa hàng: ${result.debugMessage}")
        }
    }

    // ─────────────────────────────────────────────
    // PURCHASES UPDATED LISTENER (callback từ Play)
    // ─────────────────────────────────────────────

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        val ctx = applicationContext ?: return
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it, ctx) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                purchaseCallback?.invoke(false, "Bạn đã huỷ mua hàng.")
                purchaseCallback = null
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                // Người dùng đã mua rồi nhưng chưa được xác nhận – acknowledge và kích hoạt
                restorePurchasesInternal(ctx)
                purchaseCallback?.invoke(true, "Bạn đã sở hữu sản phẩm này!")
                purchaseCallback = null
            }
            else -> {
                purchaseCallback?.invoke(false, "Lỗi: ${result.debugMessage}")
                purchaseCallback = null
            }
        }
    }

    // ─────────────────────────────────────────────
    // HANDLE & ACKNOWLEDGE PURCHASE
    // ─────────────────────────────────────────────

    private fun handlePurchase(purchase: Purchase, context: Context) {
        if (purchase.products.contains(PRODUCT_REMOVE_ADS)) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                // Kích hoạt VIP ngay
                GoldManager.setVip(context, true)
                Log.d(TAG, "Remove Ads activated!")

                // Acknowledge để Google biết đã xử lý (bắt buộc trong 3 ngày, không thì hoàn tiền)
                if (!purchase.isAcknowledged) {
                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient?.acknowledgePurchase(ackParams) { ackResult ->
                        if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.d(TAG, "Purchase acknowledged.")
                        } else {
                            Log.w(TAG, "Acknowledge failed: ${ackResult.debugMessage}")
                        }
                    }
                }

                purchaseCallback?.invoke(true, "🎉 Đã kích hoạt! Quảng cáo đã được xoá vĩnh viễn.")
                purchaseCallback = null
            }
        }
    }

    // ─────────────────────────────────────────────
    // RESTORE PURCHASES (khôi phục khi đổi máy / reinstall)
    // ─────────────────────────────────────────────

    /**
     * Gọi chủ động khi user nhấn "Khôi phục mua hàng" trong Shop.
     */
    fun restorePurchases(
        context: Context,
        onResult: (restored: Boolean, message: String) -> Unit
    ) {
        val client = billingClient
        if (client == null || !client.isReady) {
            onResult(false, "Google Play chưa sẵn sàng.")
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasRemoveAds = purchases.any { p ->
                    p.products.contains(PRODUCT_REMOVE_ADS) &&
                    p.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (hasRemoveAds) {
                    purchases.filter { it.products.contains(PRODUCT_REMOVE_ADS) }
                             .forEach { handlePurchase(it, context) }
                    onResult(true, context.getString(R.string.shop_restore_success))
                } else {
                    onResult(false, context.getString(R.string.shop_restore_nothing))
                }
            } else {
                onResult(false, "Lỗi khi kiểm tra: ${result.debugMessage}")
            }
        }
    }

    /** Tự động restore khi start billing (silent) */
    private fun restorePurchasesInternal(context: Context) {
        val client = billingClient ?: return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.filter { it.products.contains(PRODUCT_REMOVE_ADS) }
                         .forEach { handlePurchase(it, context) }
            }
        }
    }

    fun destroy() {
        billingClient?.endConnection()
        billingClient = null
        applicationContext = null
    }
}
