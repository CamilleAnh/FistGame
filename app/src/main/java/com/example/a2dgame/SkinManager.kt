package com.example.a2dgame

import android.graphics.drawable.GradientDrawable

/**
 * SkinManager – Quản lý danh sách skin, trạng thái mua/chọn.
 * Mỗi skin định nghĩa cả giao diện của hộp trong game.
 * Dữ liệu lưu trong SharedPreferences "skin_prefs".
 */
object SkinManager {

    // ─── Data Classes ────────────────────────────────────────────────────────

    data class SkinItem(
        val id: String,
        val name: String,
        val emoji: String,
        val bgColor: Int,         // ARGB int for preview circle
        val priceCoin: Int = 0,
        val isFree: Boolean = false,
        val style: BoxSkinStyle   // Áp dụng vào hộp trong game
    )

    /**
     * Định nghĩa ngoại hình của hộp khi skin được trang bị.
     * @param boxBodyColor       Màu thân hộp chính (phía sau/bên)
     * @param boxTopColor        Màu viền trên / rim của hộp
     * @param boxFrontStartColor Màu gradient bắt đầu của mặt trước hộp
     * @param boxFrontEndColor   Màu gradient kết thúc của mặt trước hộp
     * @param boxStrokeColor     Màu viền ngoài hộp
     * @param boxCornerRadius    Độ bo góc hộp (dp, float)
     * @param blockBgColor       Màu nền của từng block trái cây
     * @param blockCornerRadius  Độ bo góc block (dp)
     * @param blockStrokeColor   Màu viền block
     */
    data class BoxSkinStyle(
        val boxBodyColor: Int,
        val boxTopColor: Int,
        val boxFrontStartColor: Int,
        val boxFrontEndColor: Int,
        val boxStrokeColor: Int,
        val boxCornerRadius: Float = 10f,
        val blockBgColor: Int,
        val blockCornerRadius: Float = 4f,
        val blockStrokeColor: Int
    )

    // ─── Skin Definitions ────────────────────────────────────────────────────

    val ALL_SKINS = listOf(
        SkinItem(
            id = "classic", name = "Classic", emoji = "📦",
            bgColor = 0xFF8D6E63.toInt(), isFree = true,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF5D4037.toInt(),
                boxTopColor        = 0xFF8D6E63.toInt(),
                boxFrontStartColor = 0xFFD7CCC8.toInt(),
                boxFrontEndColor   = 0xFFA1887F.toInt(),
                boxStrokeColor     = 0xFF3E2723.toInt(),
                boxCornerRadius    = 10f,
                blockBgColor       = 0xFFD2B48C.toInt(),
                blockCornerRadius  = 4f,
                blockStrokeColor   = 0xFFA0522D.toInt()
            )
        ),
        SkinItem(
            id = "blossom", name = "Blossom", emoji = "🌸",
            bgColor = 0xFFFF80AB.toInt(), priceCoin = 300,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFFE91E8C.toInt(),
                boxTopColor        = 0xFFFF80AB.toInt(),
                boxFrontStartColor = 0xFFFFCDD2.toInt(),
                boxFrontEndColor   = 0xFFF48FB1.toInt(),
                boxStrokeColor     = 0xFFC2185B.toInt(),
                boxCornerRadius    = 18f,
                blockBgColor       = 0xFFFCE4EC.toInt(),
                blockCornerRadius  = 12f,
                blockStrokeColor   = 0xFFF48FB1.toInt()
            )
        ),
        SkinItem(
            id = "rocket", name = "Rocket", emoji = "🚀",
            bgColor = 0xFF7C4DFF.toInt(), priceCoin = 600,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF4527A0.toInt(),
                boxTopColor        = 0xFF7C4DFF.toInt(),
                boxFrontStartColor = 0xFFE8EAF6.toInt(),
                boxFrontEndColor   = 0xFF9FA8DA.toInt(),
                boxStrokeColor     = 0xFF311B92.toInt(),
                boxCornerRadius    = 6f,
                blockBgColor       = 0xFFEDE7F6.toInt(),
                blockCornerRadius  = 3f,
                blockStrokeColor   = 0xFF7C4DFF.toInt()
            )
        ),
        SkinItem(
            id = "rainbow", name = "Rainbow", emoji = "🌈",
            bgColor = 0xFFFFB300.toInt(), priceCoin = 1000,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFFFF6F00.toInt(),
                boxTopColor        = 0xFFFFCA28.toInt(),
                boxFrontStartColor = 0xFFFFF9C4.toInt(),
                boxFrontEndColor   = 0xFFFFE082.toInt(),
                boxStrokeColor     = 0xFFE65100.toInt(),
                boxCornerRadius    = 14f,
                blockBgColor       = 0xFFFFFDE7.toInt(),
                blockCornerRadius  = 10f,
                blockStrokeColor   = 0xFFFFCA28.toInt()
            )
        ),
        SkinItem(
            id = "royal", name = "Royal", emoji = "👑",
            bgColor = 0xFFFFD700.toInt(), priceCoin = 1500,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF212121.toInt(),
                boxTopColor        = 0xFFFFD700.toInt(),
                boxFrontStartColor = 0xFF37474F.toInt(),
                boxFrontEndColor   = 0xFF263238.toInt(),
                boxStrokeColor     = 0xFFFFD700.toInt(),
                boxCornerRadius    = 8f,
                blockBgColor       = 0xFF263238.toInt(),
                blockCornerRadius  = 6f,
                blockStrokeColor   = 0xFFFFD700.toInt()
            )
        ),

        // ─── Premium Glass Skins (From Concept Art) ─────────────────────────

        SkinItem(
            id = "abyssal_gem", name = "Abyssal Gem", emoji = "💎",
            bgColor = 0xFF0277BD.toInt(), priceCoin = 600,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF0277BD.toInt(),
                boxTopColor        = 0xFFE1F5FE.toInt(),
                boxFrontStartColor = 0xFF66C2FF.toInt(),
                boxFrontEndColor   = 0xFF004E90.toInt(),
                boxStrokeColor     = 0xFF00C8FF.toInt(),
                boxCornerRadius    = 14f,
                blockBgColor       = 0xFFE1F5FE.toInt(),
                blockCornerRadius  = 10f,
                blockStrokeColor   = 0xFF64B5F6.toInt()
            )
        ),
        SkinItem(
            id = "coral_treasure", name = "Coral Treasure", emoji = "🪸",
            bgColor = 0xFFFF8F00.toInt(), priceCoin = 750,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFFFF8F00.toInt(),
                boxTopColor        = 0xFFFFF8E1.toInt(),
                boxFrontStartColor = 0xFFFFD54F.toInt(),
                boxFrontEndColor   = 0xFFFF6F00.toInt(),
                boxStrokeColor     = 0xFFAC5D0A.toInt(),
                boxCornerRadius    = 16f,
                blockBgColor       = 0xFFFFF3E0.toInt(),
                blockCornerRadius  = 12f,
                blockStrokeColor   = 0xFFFFB300.toInt()
            )
        ),
        SkinItem(
            id = "aqua_dream", name = "Aqua Dream", emoji = "🐬",
            bgColor = 0xFF00ACC1.toInt(), priceCoin = 500,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF00ACC1.toInt(),
                boxTopColor        = 0xFFE0F7FA.toInt(),
                boxFrontStartColor = 0xFF80DEEA.toInt(),
                boxFrontEndColor   = 0xFF00838F.toInt(),
                boxStrokeColor     = 0xFF00E5FF.toInt(),
                boxCornerRadius    = 14f,
                blockBgColor       = 0xFFE0F7FA.toInt(),
                blockCornerRadius  = 10f,
                blockStrokeColor   = 0xFF4DD0E1.toInt()
            )
        ),
        SkinItem(
            id = "jelly_jewel", name = "Jelly Jewel", emoji = "🐙",
            bgColor = 0xFFAB47BC.toInt(), priceCoin = 400,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFFAB47BC.toInt(),
                boxTopColor        = 0xFFF3E5F5.toInt(),
                boxFrontStartColor = 0xFFF48FB1.toInt(),
                boxFrontEndColor   = 0xFF8E24AA.toInt(),
                boxStrokeColor     = 0xFFD81B60.toInt(),
                boxCornerRadius    = 12f,
                blockBgColor       = 0xFFFCE4EC.toInt(),
                blockCornerRadius  = 8f,
                blockStrokeColor   = 0xFFF06292.toInt()
            )
        ),
        SkinItem(
            id = "pearl_splash", name = "Pearl Splash", emoji = "🦪",
            bgColor = 0xFFEC407A.toInt(), priceCoin = 900,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFFEC407A.toInt(),
                boxTopColor        = 0xFFFCE4EC.toInt(),
                boxFrontStartColor = 0xFFF8BBD0.toInt(),
                boxFrontEndColor   = 0xFFC2185B.toInt(),
                boxStrokeColor     = 0xFFFF80AB.toInt(),
                boxCornerRadius    = 14f,
                blockBgColor       = 0xFFFCE4EC.toInt(),
                blockCornerRadius  = 10f,
                blockStrokeColor   = 0xFFF48FB1.toInt()
            )
        ),

        // ─── Skin cũ ────────────────────────────

        SkinItem(
            id = "watermelon_crush", name = "Watermelon Crush", emoji = "🍉",
            bgColor = 0xFFE53935.toInt(), priceCoin = 450,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF388E3C.toInt(),
                boxTopColor        = 0xFFF48FB1.toInt(),
                boxFrontStartColor = 0xFFFFCDD2.toInt(),
                boxFrontEndColor   = 0xFFE53935.toInt(),
                boxStrokeColor     = 0xFFB71C1C.toInt(),
                boxCornerRadius    = 14f,
                blockBgColor       = 0xFFFCE4EC.toInt(),
                blockCornerRadius  = 10f,
                blockStrokeColor   = 0xFFEF9A9A.toInt()
            )
        ),
        SkinItem(
            id = "citrus_burst", name = "Citrus Burst", emoji = "🍊",
            bgColor = 0xFFFF9800.toInt(), priceCoin = 500,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFFE65100.toInt(),
                boxTopColor        = 0xFFFFB300.toInt(),
                boxFrontStartColor = 0xFFFFE082.toInt(),
                boxFrontEndColor   = 0xFFFF9800.toInt(),
                boxStrokeColor     = 0xFFE65100.toInt(),
                boxCornerRadius    = 12f,
                blockBgColor       = 0xFFFFF8E1.toInt(),
                blockCornerRadius  = 8f,
                blockStrokeColor   = 0xFFFFCC02.toInt()
            )
        ),
        SkinItem(
            id = "grape_galaxy", name = "Grape Galaxy", emoji = "🍇",
            bgColor = 0xFF8E24AA.toInt(), priceCoin = 600,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF4A148C.toInt(),
                boxTopColor        = 0xFFAB47BC.toInt(),
                boxFrontStartColor = 0xFFE1BEE7.toInt(),
                boxFrontEndColor   = 0xFF8E24AA.toInt(),
                boxStrokeColor     = 0xFF4A148C.toInt(),
                boxCornerRadius    = 10f,
                blockBgColor       = 0xFFF3E5F5.toInt(),
                blockCornerRadius  = 6f,
                blockStrokeColor   = 0xFFBA68C8.toInt()
            )
        ),
        SkinItem(
            id = "tropical_fish", name = "Tropical Fish", emoji = "🐠",
            bgColor = 0xFF00ACC1.toInt(), priceCoin = 500,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF00ACC1.toInt(),
                boxTopColor        = 0xFF4DD0E1.toInt(),
                boxFrontStartColor = 0xFF80DEEA.toInt(),
                boxFrontEndColor   = 0xFFFFEB3B.toInt(),
                boxStrokeColor     = 0xFF006064.toInt(),
                boxCornerRadius    = 12f,
                blockBgColor       = 0xFFE0F7FA.toInt(),
                blockCornerRadius  = 8f,
                blockStrokeColor   = 0xFF80DEEA.toInt()
            )
        ),
        SkinItem(
            id = "coral_candy", name = "Coral Candy", emoji = "🐚",
            bgColor = 0xFFFF7043.toInt(), priceCoin = 350,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFFF4511E.toInt(),
                boxTopColor        = 0xFFFF8A65.toInt(),
                boxFrontStartColor = 0xFFFFCCBC.toInt(),
                boxFrontEndColor   = 0xFFFF7043.toInt(),
                boxStrokeColor     = 0xFFBF360C.toInt(),
                boxCornerRadius    = 16f,
                blockBgColor       = 0xFFFBE9E7.toInt(),
                blockCornerRadius  = 12f,
                blockStrokeColor   = 0xFFFF8A65.toInt()
            )
        ),
        SkinItem(
            id = "ocean_breeze", name = "Ocean Breeze", emoji = "🌊",
            bgColor = 0xFF03A9F4.toInt(), priceCoin = 200,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF0288D1.toInt(),
                boxTopColor        = 0xFF4FC3F7.toInt(),
                boxFrontStartColor = 0xFFB3E5FC.toInt(),
                boxFrontEndColor   = 0xFF03A9F4.toInt(),
                boxStrokeColor     = 0xFF01579B.toInt(),
                boxCornerRadius    = 10f,
                blockBgColor       = 0xFFE1F5FE.toInt(),
                blockCornerRadius  = 7f,
                blockStrokeColor   = 0xFF81D4FA.toInt()
            )
        )
    )


    // ─── Prefs Keys ──────────────────────────────────────────────────────────

    private const val PREFS        = "skin_prefs"
    private const val KEY_SELECTED = "selected_skin"
    private const val KEY_PREFIX   = "owned_"

    // ─── Queries ─────────────────────────────────────────────────────────────

    fun getSelected(ctx: android.content.Context): SkinItem =
        ALL_SKINS.find { it.id == getSelectedId(ctx) } ?: ALL_SKINS.first()

    fun getSelectedId(ctx: android.content.Context): String =
        ctx.getSharedPreferences(PREFS, 0).getString(KEY_SELECTED, "classic") ?: "classic"

    fun getSelectedStyle(ctx: android.content.Context): BoxSkinStyle =
        getSelected(ctx).style

    fun isOwned(ctx: android.content.Context, id: String): Boolean {
        val skin = ALL_SKINS.find { it.id == id } ?: return false
        if (skin.isFree) return true
        return ctx.getSharedPreferences(PREFS, 0).getBoolean(KEY_PREFIX + id, false)
    }

    // ─── Mutations ───────────────────────────────────────────────────────────

    fun setSelected(ctx: android.content.Context, id: String) {
        ctx.getSharedPreferences(PREFS, 0).edit().putString(KEY_SELECTED, id).apply()
    }

    /**
     * Attempt to purchase a skin using gold.
     * @return true if successful (enough gold or already owned), false if insufficient gold.
     */
    fun purchase(ctx: android.content.Context, id: String): Boolean {
        val skin = ALL_SKINS.find { it.id == id } ?: return false
        if (isOwned(ctx, id)) return true
        // Note: GoldManager should be defined elsewhere in your project
        // if (!GoldManager.spendGold(ctx, skin.priceCoin)) return false
        ctx.getSharedPreferences(PREFS, 0).edit().putBoolean(KEY_PREFIX + id, true).apply()
        return true
    }

    // ─── Drawable Helpers ─────────────────────────────────────────────────────

    /** Build a colored oval GradientDrawable for skin preview circles. */
    fun makeCircleDrawable(color: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }

    /**
     * Tạo drawable cho thân hộp.
     * @param density  resources.displayMetrics.density
     */
    fun makeBoxBodyDrawable(style: BoxSkinStyle, density: Float): android.graphics.drawable.Drawable =
        CrateDrawable(style, density)

    class CrateDrawable(
        private val boxStyle: BoxSkinStyle,
        private val density: Float
    ) : android.graphics.drawable.Drawable() {

        private val bodyPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        private val interiorPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(80, 0, 0, 0)
        }
        private val deepSlotPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(60, 0, 0, 0)
        }
        private val slotStrokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(50, 255, 255, 255)
            this.style = android.graphics.Paint.Style.STROKE
            strokeWidth = 1 * density
        }
        private val outerStrokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = boxStyle.boxStrokeColor
            this.style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2 * density
        }

        override fun draw(canvas: android.graphics.Canvas) {
            val rect = bounds
            if (rect.width() == 0 || rect.height() == 0) return
            
            val cornerRadius = boxStyle.boxCornerRadius * density

            // Body Gradient
            val shader = android.graphics.LinearGradient(
                rect.left.toFloat(), rect.top.toFloat(),
                rect.left.toFloat(), rect.bottom.toFloat(),
                boxStyle.boxFrontStartColor, boxStyle.boxFrontEndColor,
                android.graphics.Shader.TileMode.CLAMP
            )
            bodyPaint.shader = shader
            
            val rectF = android.graphics.RectF(rect)
            rectF.inset(outerStrokePaint.strokeWidth / 2f, outerStrokePaint.strokeWidth / 2f)
            
            // 1. Draw Crate Exterior
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, bodyPaint)
            
            // Gloss highlight at the top to simulate 3D glass edge
            val glossPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
            glossPaint.shader = android.graphics.LinearGradient(
                rectF.left, rectF.top,
                rectF.left, rectF.top + cornerRadius * 2,
                android.graphics.Color.argb(120, 255, 255, 255),
                android.graphics.Color.TRANSPARENT,
                android.graphics.Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, glossPaint)
            
            // 2. Draw Interior Dark Area
            val padding = 6 * density
            val interiorRect = android.graphics.RectF(
                rectF.left + padding,
                rectF.top + padding,
                rectF.right - padding,
                rectF.bottom - padding
            )
            val cornerInt = cornerRadius * 0.6f
            canvas.drawRoundRect(interiorRect, cornerInt, cornerInt, interiorPaint)
            
            // Inner rim highlight (glass illusion)
            val innerStrokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 1 * density
                color = android.graphics.Color.argb(80, 255, 255, 255)
            }
            canvas.drawRoundRect(interiorRect, cornerInt, cornerInt, innerStrokePaint)
            
            // 3. Draw 4 Slots Separators
            val totalHeight = interiorRect.height()
            val slotGap = 2 * density
            // Calculate slot height for 4 slots
            val slotHeight = (totalHeight - 3 * slotGap) / 4f
            
            for (i in 0..3) {
                val yTop = interiorRect.top + i * (slotHeight + slotGap)
                val slotRect = android.graphics.RectF(
                    interiorRect.left + 2 * density,
                    yTop,
                    interiorRect.right - 2 * density,
                    yTop + slotHeight
                )
                canvas.drawRoundRect(slotRect, 4 * density, 4 * density, deepSlotPaint)
                canvas.drawRoundRect(slotRect, 4 * density, 4 * density, slotStrokePaint)
                
                // Add a subtle bottom glow to each slot to simulate depth transparency
                val slotGlow = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                slotGlow.shader = android.graphics.LinearGradient(
                    slotRect.left, slotRect.bottom - 6 * density,
                    slotRect.left, slotRect.bottom,
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.argb(40, 255, 255, 255),
                    android.graphics.Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(slotRect, 4 * density, 4 * density, slotGlow)
            }
            
            // 4. Draw Crate Outer Stroke
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, outerStrokePaint)
        }

        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {}
        override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
    }

    /**
     * Tạo drawable cho từng block trái cây theo style của skin.
     * @param density  resources.displayMetrics.density
     */
    fun makeBlockDrawable(style: BoxSkinStyle, density: Float): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(style.blockBgColor)
            cornerRadius = style.blockCornerRadius * density
            setStroke((1 * density).toInt(), style.blockStrokeColor)
        }
}
