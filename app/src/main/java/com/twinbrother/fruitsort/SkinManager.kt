package com.twinbrother.fruitsort

import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable

/**
 * SkinManager – Quản lý danh sách skin, trạng thái mua/chọn.
 * Mỗi skin định nghĩa cả giao diện của hộp trong game và bộ vẽ fruit 2.5D.
 */
object SkinManager {

    // ─── Data Classes ────────────────────────────────────────────────────────

    enum class SkinRarity(val label: String, val badgeColor: Int) {
        COMMON("COMMON", 0xFF78909C.toInt()),
        RARE("RARE", 0xFF1E88E5.toInt()),
        EPIC("EPIC", 0xFF8E24AA.toInt()),
        LEGENDARY("LEGENDARY", 0xFFFF8F00.toInt()),
        MYTHIC("MYTHIC", 0xFFD81B60.toInt())
    }

    data class SkinItem(
        val id: String,
        val name: String,
        val emoji: String,
        val bgColor: Int,         // ARGB int for preview circle
        val priceCoin: Int = 0,
        val priceGem: Int = 0,
        val isFree: Boolean = false,
        val rarity: SkinRarity = SkinRarity.COMMON,
        val style: BoxSkinStyle   // Áp dụng vào hộp trong game
    )

    enum class ThemeType {
        CLASSIC,
        TECHNOLOGY,
        DREAM,
        SPACE
    }

    data class BoxSkinStyle(
        val boxBodyColor: Int,
        val boxTopColor: Int,
        val boxFrontStartColor: Int,
        val boxFrontEndColor: Int,
        val boxStrokeColor: Int,
        val boxCornerRadius: Float = 10f,
        val blockBgColor: Int,
        val blockCornerRadius: Float = 4f,
        val blockStrokeColor: Int,
        val themeType: ThemeType = ThemeType.CLASSIC,
        val isWood: Boolean = false
    )

    // ─── Skin Definitions ────────────────────────────────────────────────────

    val ALL_SKINS = listOf(
        SkinItem(
            id = "classic", name = "Classic Wood", emoji = "📦",
            bgColor = 0xFF8D6E63.toInt(), isFree = true, rarity = SkinRarity.COMMON,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF5D4037.toInt(),
                boxTopColor        = 0xFF8D6E63.toInt(),
                boxFrontStartColor = 0xFFD7CCC8.toInt(),
                boxFrontEndColor   = 0xFFA1887F.toInt(),
                boxStrokeColor     = 0xFF3E2723.toInt(),
                boxCornerRadius    = 10f,
                blockBgColor       = 0xFFD2B48C.toInt(),
                blockCornerRadius  = 4f,
                blockStrokeColor   = 0xFFA0522D.toInt(),
                isWood             = true,
                themeType          = ThemeType.CLASSIC
            )
        ),
        SkinItem(
            id = "bamboo_rustic", name = "Bamboo Village", emoji = "🎍",
            bgColor = 0xFF558B2F.toInt(), priceCoin = 400, priceGem = 8, rarity = SkinRarity.COMMON,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF33691E.toInt(),
                boxTopColor        = 0xFF689F38.toInt(),
                boxFrontStartColor = 0xFFDCEDC8.toInt(),
                boxFrontEndColor   = 0xFFAED581.toInt(),
                boxStrokeColor     = 0xFF1B5E20.toInt(),
                boxCornerRadius    = 12f,
                blockBgColor       = 0xFFF1F8E9.toInt(),
                blockCornerRadius  = 6f,
                blockStrokeColor   = 0xFF7CB342.toInt(),
                isWood             = true,
                themeType          = ThemeType.CLASSIC
            )
        ),
        SkinItem(
            id = "ocean_breeze", name = "Ocean Breeze", emoji = "🌊",
            bgColor = 0xFF03A9F4.toInt(), priceCoin = 800, priceGem = 15, rarity = SkinRarity.COMMON,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF0288D1.toInt(),
                boxTopColor        = 0xFF4FC3F7.toInt(),
                boxFrontStartColor = 0xFFB3E5FC.toInt(),
                boxFrontEndColor   = 0xFF03A9F4.toInt(),
                boxStrokeColor     = 0xFF01579B.toInt(),
                boxCornerRadius    = 10f,
                blockBgColor       = 0xFFE1F5FE.toInt(),
                blockCornerRadius  = 7f,
                blockStrokeColor   = 0xFF81D4FA.toInt(),
                themeType          = ThemeType.CLASSIC
            )
        ),
        SkinItem(
            id = "forest_wood", name = "Forest Pine", emoji = "🌲",
            bgColor = 0xFF388E3C.toInt(), priceCoin = 1200, priceGem = 20, rarity = SkinRarity.COMMON,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF1B5E20.toInt(),
                boxTopColor        = 0xFF4CAF50.toInt(),
                boxFrontStartColor = 0xFFC8E6C9.toInt(),
                boxFrontEndColor   = 0xFF81C784.toInt(),
                boxStrokeColor     = 0xFF1B5E20.toInt(),
                boxCornerRadius    = 10f,
                blockBgColor       = 0xFFE8F5E9.toInt(),
                blockCornerRadius  = 4f,
                blockStrokeColor   = 0xFF4CAF50.toInt(),
                isWood             = true,
                themeType          = ThemeType.CLASSIC
            )
        ),
        SkinItem(
            id = "candy_cloud", name = "Candy Cloud", emoji = "🍬",
            bgColor = 0xFFFF80AB.toInt(), priceCoin = 2500, priceGem = 40, rarity = SkinRarity.RARE,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFFE91E8C.toInt(),
                boxTopColor        = 0xFFFF80AB.toInt(),
                boxFrontStartColor = 0xFFFFCDD2.toInt(),
                boxFrontEndColor   = 0xFFF48FB1.toInt(),
                boxStrokeColor     = 0xFFC2185B.toInt(),
                boxCornerRadius    = 18f,
                blockBgColor       = 0xFFFCE4EC.toInt(),
                blockCornerRadius  = 12f,
                blockStrokeColor   = 0xFFF48FB1.toInt(),
                themeType          = ThemeType.DREAM
            )
        ),
        SkinItem(
            id = "royal_gold", name = "Royal Gold", emoji = "👑",
            bgColor = 0xFFFFD700.toInt(), priceCoin = 4000, priceGem = 65, rarity = SkinRarity.RARE,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF212121.toInt(),
                boxTopColor        = 0xFFFFD700.toInt(),
                boxFrontStartColor = 0xFF37474F.toInt(),
                boxFrontEndColor   = 0xFF263238.toInt(),
                boxStrokeColor     = 0xFFFFD700.toInt(),
                boxCornerRadius    = 8f,
                blockBgColor       = 0xFFFFF8E1.toInt(),
                blockCornerRadius  = 12f,
                blockStrokeColor   = 0xFFFFD700.toInt(),
                themeType          = ThemeType.DREAM
            )
        ),
        SkinItem(
            id = "cyberpunk", name = "Cyberpunk Neon", emoji = "🔋",
            bgColor = 0xFF00E5FF.toInt(), priceCoin = 6500, priceGem = 95, rarity = SkinRarity.EPIC,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF000000.toInt(),
                boxTopColor        = 0xFF00E5FF.toInt(),
                boxFrontStartColor = 0xFF263238.toInt(),
                boxFrontEndColor   = 0xFF101518.toInt(),
                boxStrokeColor     = 0xFF00E5FF.toInt(),
                boxCornerRadius    = 4f,
                blockBgColor       = 0xFF212121.toInt(),
                blockCornerRadius  = 2f,
                blockStrokeColor   = 0xFFE040FB.toInt(),
                themeType          = ThemeType.TECHNOLOGY
            )
        ),
        SkinItem(
            id = "matrix_code", name = "Matrix Code", emoji = "💻",
            bgColor = 0xFF00E676.toInt(), priceCoin = 9000, priceGem = 130, rarity = SkinRarity.EPIC,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF000000.toInt(),
                boxTopColor        = 0xFF00E676.toInt(),
                boxFrontStartColor = 0xFF1B5E20.toInt(),
                boxFrontEndColor   = 0xFF000000.toInt(),
                boxStrokeColor     = 0xFF00E676.toInt(),
                boxCornerRadius    = 4f,
                blockBgColor       = 0xFF1B5E20.toInt(),
                blockCornerRadius  = 2f,
                blockStrokeColor   = 0xFF00E676.toInt(),
                themeType          = ThemeType.TECHNOLOGY
            )
        ),
        SkinItem(
            id = "galactic", name = "Galactic Cosmic", emoji = "🚀",
            bgColor = 0xFF7C4DFF.toInt(), priceCoin = 14000, priceGem = 200, rarity = SkinRarity.LEGENDARY,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF1A237E.toInt(),
                boxTopColor        = 0xFF7C4DFF.toInt(),
                boxFrontStartColor = 0xFF3949AB.toInt(),
                boxFrontEndColor   = 0xFF283593.toInt(),
                boxStrokeColor     = 0xFF534BAE.toInt(),
                boxCornerRadius    = 8f,
                blockBgColor       = 0xFF3F51B5.toInt(),
                blockCornerRadius  = 6f,
                blockStrokeColor   = 0xFF00E5FF.toInt(),
                themeType          = ThemeType.SPACE
            )
        ),
        SkinItem(
            id = "abyssal_gem", name = "Abyssal Crystal", emoji = "💎",
            bgColor = 0xFF0277BD.toInt(), priceCoin = 20000, priceGem = 300, rarity = SkinRarity.MYTHIC,
            style = BoxSkinStyle(
                boxBodyColor       = 0xFF0277BD.toInt(),
                boxTopColor        = 0xFFE1F5FE.toInt(),
                boxFrontStartColor = 0xFF66C2FF.toInt(),
                boxFrontEndColor   = 0xFF004E90.toInt(),
                boxStrokeColor     = 0xFF00C8FF.toInt(),
                boxCornerRadius    = 14f,
                blockBgColor       = 0xFFE1F5FE.toInt(),
                blockCornerRadius  = 10f,
                blockStrokeColor   = 0xFF64B5F6.toInt(),
                themeType          = ThemeType.SPACE
            )
        )
    )

    fun getIconForColor(color: LevelOneEngine.ColorId, context: android.content.Context): String {
        return color.fruitIcon
    }

    // ─── Prefs Keys ──────────────────────────────────────────────────────────

    private const val PREFS        = "skin_prefs"
    private const val KEY_SELECTED = "selected_skin"
    private const val KEY_PREFIX   = "owned_"

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

    fun setSelected(ctx: android.content.Context, id: String) {
        ctx.getSharedPreferences(PREFS, 0).edit().putString(KEY_SELECTED, id).apply()
    }

    fun purchase(ctx: android.content.Context, id: String): Boolean {
        val skin = ALL_SKINS.find { it.id == id } ?: return false
        if (isOwned(ctx, id)) return true
        if (!GoldManager.spendGold(ctx, skin.priceCoin)) return false
        ctx.getSharedPreferences(PREFS, 0).edit().putBoolean(KEY_PREFIX + id, true).apply()
        return true
    }

    fun purchaseWithGems(ctx: android.content.Context, id: String): Boolean {
        val skin = ALL_SKINS.find { it.id == id } ?: return false
        if (isOwned(ctx, id)) return true
        if (skin.priceGem <= 0 || !GoldManager.spendGems(ctx, skin.priceGem)) return false
        ctx.getSharedPreferences(PREFS, 0).edit().putBoolean(KEY_PREFIX + id, true).apply()
        return true
    }

    fun makeCircleDrawable(color: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }

    fun makeBoxBodyDrawable(style: BoxSkinStyle, density: Float): Drawable =
        CrateDrawable(style, density)

    // ─── High-Definition 2.5D Fruit Canvas Drawing Engine ───────────────────

    private val fruitPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val leafPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF388E3C.toInt() }
    private val stemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF5D4037.toInt(); strokeWidth = 3f; style = Paint.Style.STROKE }
    private val shinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x88FFFFFF.toInt(); style = Paint.Style.FILL }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }

    fun draw2DFruitIcon(canvas: Canvas, colorId: LevelOneEngine.ColorId, rect: RectF, density: Float) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val radius = (Math.min(rect.width(), rect.height()) / 2f) * 0.75f
        if (radius <= 0f) return

        // Drop Shadow underneath 2.5D fruit
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x40000000.toInt()
            style = Paint.Style.FILL
        }
        val shadowRect = RectF(cx - radius * 0.75f, cy + radius * 0.55f, cx + radius * 0.75f, cy + radius * 0.95f)
        canvas.drawOval(shadowRect, shadowPaint)

        val primaryColor = getFruitPrimaryColor(colorId)
        val darkColor = getFruitDarkColor(colorId)

        // Radial gradient sphere body
        val gradient = RadialGradient(
            cx - radius * 0.3f, cy - radius * 0.3f, radius * 1.3f,
            intColorArray(primaryColor, darkColor),
            floatArrayOf(0.0f, 1.0f),
            Shader.TileMode.CLAMP
        )
        fruitPaint.shader = gradient
        fruitPaint.style = Paint.Style.FILL

        // Specific fruit shape rendering
        when (colorId) {
            LevelOneEngine.ColorId.BANANA -> {
                val path = Path().apply {
                    moveTo(cx - radius, cy - radius * 0.2f)
                    quadTo(cx, cy + radius * 1.1f, cx + radius, cy - radius * 0.5f)
                    quadTo(cx, cy + radius * 0.5f, cx - radius, cy - radius * 0.2f)
                }
                canvas.drawPath(path, fruitPaint)
            }
            LevelOneEngine.ColorId.WATERMELON -> {
                // Semicircle watermelon slice
                val sliceRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
                canvas.drawArc(sliceRect, 0f, 180f, true, fruitPaint)
                // Green rind
                val rindPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFF1B5E20.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = 3f * density
                }
                canvas.drawArc(sliceRect, 0f, 180f, false, rindPaint)
            }
            LevelOneEngine.ColorId.STRAWBERRY, LevelOneEngine.ColorId.TOMATO, LevelOneEngine.ColorId.APPLE_GREEN -> {
                // Sphere fruit with leaf crown
                canvas.drawCircle(cx, cy + radius * 0.1f, radius, fruitPaint)
                // Stem & Leaf
                canvas.drawLine(cx, cy - radius * 0.9f, cx, cy - radius * 1.2f, stemPaint)
                canvas.drawCircle(cx - radius * 0.3f, cy - radius * 0.9f, radius * 0.3f, leafPaint)
                canvas.drawCircle(cx + radius * 0.3f, cy - radius * 0.9f, radius * 0.3f, leafPaint)
            }
            LevelOneEngine.ColorId.GRAPE -> {
                // Cluster of 3 grapes
                val rSmall = radius * 0.6f
                canvas.drawCircle(cx - rSmall * 0.5f, cy - rSmall * 0.4f, rSmall, fruitPaint)
                canvas.drawCircle(cx + rSmall * 0.5f, cy - rSmall * 0.4f, rSmall, fruitPaint)
                canvas.drawCircle(cx, cy + rSmall * 0.5f, rSmall, fruitPaint)
            }
            else -> {
                // Standard glossy 3D sphere fruit
                canvas.drawCircle(cx, cy, radius, fruitPaint)
                // Leaf for fruits
                if (hasLeaf(colorId)) {
                    canvas.drawCircle(cx + radius * 0.4f, cy - radius * 0.7f, radius * 0.35f, leafPaint)
                }
            }
        }

        // Glossy Specular Highlight Spot
        fruitPaint.shader = null
        canvas.drawCircle(cx - radius * 0.35f, cy - radius * 0.35f, radius * 0.25f, shinePaint)

        // Overlay crisp Emoji icon if text scale is small or preferred
        if (rect.width() > 40f * density) {
            textPaint.textSize = (rect.height() * 0.45f)
            val yPos = cy - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText(colorId.fruitIcon, cx, yPos, textPaint)
        }
    }

    private fun intColorArray(c1: Int, c2: Int) = intArrayOf(c1, c2)

    private fun getFruitPrimaryColor(colorId: LevelOneEngine.ColorId): Int = when (colorId) {
        LevelOneEngine.ColorId.STRAWBERRY -> 0xFFFF1744.toInt()
        LevelOneEngine.ColorId.ORANGE -> 0xFFFF9100.toInt()
        LevelOneEngine.ColorId.APPLE_GREEN -> 0xFF76FF03.toInt()
        LevelOneEngine.ColorId.BANANA -> 0xFFFFEA00.toInt()
        LevelOneEngine.ColorId.PEACH -> 0xFFFF8A80.toInt()
        LevelOneEngine.ColorId.MANGO -> 0xFFFFAB00.toInt()
        LevelOneEngine.ColorId.GRAPE -> 0xFF7C4DFF.toInt()
        LevelOneEngine.ColorId.WATERMELON -> 0xFFFF1744.toInt()
        LevelOneEngine.ColorId.PINEAPPLE -> 0xFFFFC400.toInt()
        LevelOneEngine.ColorId.BLUEBERRY -> 0xFF3D5AFE.toInt()
        LevelOneEngine.ColorId.PEAR -> 0xFFC6FF00.toInt()
        LevelOneEngine.ColorId.COCONUT -> 0xFF8D6E63.toInt()
        LevelOneEngine.ColorId.KIWI -> 0xFFAEEA00.toInt()
        LevelOneEngine.ColorId.CHERRY -> 0xFFD50000.toInt()
        LevelOneEngine.ColorId.LEMON -> 0xFFFFFF00.toInt()
        LevelOneEngine.ColorId.AVOCADO -> 0xFF76FF03.toInt()
        LevelOneEngine.ColorId.TOMATO -> 0xFFFF3D00.toInt()
        LevelOneEngine.ColorId.CORN -> 0xFFFFD600.toInt()
        LevelOneEngine.ColorId.CARROT -> 0xFFFF6D00.toInt()
        LevelOneEngine.ColorId.EGGPLANT -> 0xFF7B1FA2.toInt()
        LevelOneEngine.ColorId.BROCCOLI -> 0xFF00C853.toInt()
        LevelOneEngine.ColorId.POTATO -> 0xFF8D6E63.toInt()
        LevelOneEngine.ColorId.CHILI -> 0xFFD50000.toInt()
        LevelOneEngine.ColorId.SWEET_POTATO -> 0xFFAB47BC.toInt()
        LevelOneEngine.ColorId.ONION -> 0xFFFFB74D.toInt()
        LevelOneEngine.ColorId.MUSHROOM -> 0xFFE0E0E0.toInt()
        LevelOneEngine.ColorId.BELL_PEPPER -> 0xFF00E676.toInt()
        LevelOneEngine.ColorId.CUCUMBER -> 0xFF2E7D32.toInt()
        LevelOneEngine.ColorId.GARLIC -> 0xFFECEFF1.toInt()
        LevelOneEngine.ColorId.PEANUT -> 0xFFD7CCC8.toInt()
        else -> 0xFF9E9E9E.toInt()
    }

    private fun getFruitDarkColor(colorId: LevelOneEngine.ColorId): Int = when (colorId) {
        LevelOneEngine.ColorId.STRAWBERRY -> 0xFFB71C1C.toInt()
        LevelOneEngine.ColorId.ORANGE -> 0xFFE65100.toInt()
        LevelOneEngine.ColorId.APPLE_GREEN -> 0xFF33691E.toInt()
        LevelOneEngine.ColorId.BANANA -> 0xFFF57F17.toInt()
        LevelOneEngine.ColorId.GRAPE -> 0xFF311B92.toInt()
        LevelOneEngine.ColorId.WATERMELON -> 0xFF880E4F.toInt()
        LevelOneEngine.ColorId.BLUEBERRY -> 0xFF1A237E.toInt()
        else -> 0xFF424242.toInt()
    }

    private fun hasLeaf(colorId: LevelOneEngine.ColorId) = when (colorId) {
        LevelOneEngine.ColorId.ORANGE, LevelOneEngine.ColorId.PEACH, LevelOneEngine.ColorId.MANGO, LevelOneEngine.ColorId.CHERRY -> true
        else -> false
    }

    class CrateDrawable(
        private val boxStyle: BoxSkinStyle,
        private val density: Float
    ) : Drawable() {

        private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val interiorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(80, 0, 0, 0)
        }
        private val deepSlotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(60, 0, 0, 0)
        }
        private val slotStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(50, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 1 * density
        }
        private val outerStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = boxStyle.boxStrokeColor
            style = Paint.Style.STROKE
            strokeWidth = 2 * density
        }

        private var bodyShader: Shader? = null
        private var glossShader: Shader? = null

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            if (bounds.width() == 0 || bounds.height() == 0) return
            
            bodyShader = LinearGradient(
                bounds.left.toFloat(), bounds.top.toFloat(),
                bounds.left.toFloat(), bounds.bottom.toFloat(),
                boxStyle.boxFrontStartColor, boxStyle.boxFrontEndColor,
                Shader.TileMode.CLAMP
            )

            val cornerRadius = boxStyle.boxCornerRadius * density
            glossShader = LinearGradient(
                bounds.left.toFloat(), bounds.top.toFloat(),
                bounds.left.toFloat(), bounds.top + cornerRadius * 2,
                Color.argb(120, 255, 255, 255),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }

        private val rectF = RectF()
        private val interiorRect = RectF()
        private val slotRect = RectF()

        override fun draw(canvas: Canvas) {
            val rect = bounds
            if (rect.width() == 0 || rect.height() == 0) return
            
            val cornerRadius = boxStyle.boxCornerRadius * density
            rectF.set(rect)

            if (boxStyle.isWood) {
                drawWoodenPallet(canvas, rectF)
            } else {
                bodyPaint.shader = bodyShader
                
                rectF.inset(outerStrokePaint.strokeWidth / 2f, outerStrokePaint.strokeWidth / 2f)
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, bodyPaint)
                
                val glossPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                glossPaint.shader = glossShader
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, glossPaint)
                
                val padding = 6 * density
                interiorRect.set(
                    rectF.left + padding,
                    rectF.top + padding,
                    rectF.right - padding,
                    rectF.bottom - padding
                )
                val cornerInt = cornerRadius * 0.6f
                canvas.drawRoundRect(interiorRect, cornerInt, cornerInt, interiorPaint)
                
                val innerStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 1 * density
                    color = Color.argb(80, 255, 255, 255)
                }
                canvas.drawRoundRect(interiorRect, cornerInt, cornerInt, innerStrokePaint)
                
                val totalHeight = interiorRect.height()
                val slotGap = 2 * density
                val slotHeight = (totalHeight - 3 * slotGap) / 4f
                
                for (i in 0..3) {
                    val yTop = interiorRect.top + i * (slotHeight + slotGap)
                    slotRect.set(
                        interiorRect.left + 2 * density,
                        yTop,
                        interiorRect.right - 2 * density,
                        yTop + slotHeight
                    )
                    canvas.drawRoundRect(slotRect, 4 * density, 4 * density, deepSlotPaint)
                    canvas.drawRoundRect(slotRect, 4 * density, 4 * density, slotStrokePaint)
                }
                
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, outerStrokePaint)
            }
        }

        private fun drawWoodenPallet(canvas: Canvas, rectF: RectF) {
            val w = rectF.width()
            val h = rectF.height()
            val woodPaint = Paint(Paint.ANTI_ALIAS_FLAG)

            val baseHeight = h * 0.18f
            val baseTop = rectF.bottom - baseHeight
            val baseRect = RectF(rectF.left, baseTop, rectF.right, rectF.bottom)
            val baseBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#4E342E")
            }
            canvas.drawRoundRect(baseRect, 4 * density, 4 * density, baseBgPaint)

            val basePlankCount = 3
            val baseGap = 1.5f * density
            val basePlankH = (baseHeight - (basePlankCount - 1) * baseGap) / basePlankCount
            for (i in 0 until basePlankCount) {
                val pTop = baseTop + i * (basePlankH + baseGap)
                val plankRect = RectF(rectF.left + 1 * density, pTop, rectF.right - 1 * density, pTop + basePlankH)
                woodPaint.shader = LinearGradient(
                    plankRect.left, plankRect.top, plankRect.left, plankRect.bottom,
                    Color.parseColor("#D7CCC8"),
                    Color.parseColor("#A1887F"),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(plankRect, 2 * density, 2 * density, woodPaint)
            }

            val postWidth = w * 0.08f
            val mainAreaBottom = baseTop
            val leftPostRect = RectF(rectF.left, rectF.top, rectF.left + postWidth, mainAreaBottom)
            woodPaint.shader = LinearGradient(
                leftPostRect.left, leftPostRect.top, leftPostRect.right, leftPostRect.top,
                Color.parseColor("#BCAAA4"),
                Color.parseColor("#8D6E63"),
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(leftPostRect, 2 * density, 2 * density, woodPaint)
            val rightPostRect = RectF(rectF.right - postWidth, rectF.top, rectF.right, mainAreaBottom)
            woodPaint.shader = LinearGradient(
                rightPostRect.left, rightPostRect.top, rightPostRect.right, rightPostRect.top,
                Color.parseColor("#8D6E63"),
                Color.parseColor("#BCAAA4"),
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(rightPostRect, 2 * density, 2 * density, woodPaint)

            val interiorRect = RectF(rectF.left + postWidth, rectF.top, rectF.right - postWidth, mainAreaBottom)
            val interiorBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#3E2723") }
            canvas.drawRect(interiorRect, interiorBg)
        }

        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: ColorFilter?) {}
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }

    fun makeBlockDrawable(style: BoxSkinStyle, density: Float): Drawable {
        if (style.isWood) {
            return CartonBoxDrawable(density)
        }
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(style.blockBgColor)
            cornerRadius = style.blockCornerRadius * density
            setStroke((1.5f * density).toInt(), style.blockStrokeColor)
        }
    }

    class CartonBoxDrawable(
        private val density: Float
    ) : Drawable() {

        private val rectF = RectF()
        private val cardboardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A0845C")
            style = Paint.Style.STROKE
        }

        override fun draw(canvas: Canvas) {
            val rect = bounds
            if (rect.width() == 0 || rect.height() == 0) return
            rectF.set(rect)
            val cornerR = 4 * density

            cardboardPaint.shader = LinearGradient(
                rectF.left, rectF.top, rectF.left, rectF.bottom,
                Color.parseColor("#F0DFC0"),
                Color.parseColor("#C9A96E"),
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(rectF, cornerR, cornerR, cardboardPaint)
            borderPaint.strokeWidth = 1f * density
            canvas.drawRoundRect(rectF, cornerR, cornerR, borderPaint)
        }

        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: ColorFilter?) {}
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }
}
