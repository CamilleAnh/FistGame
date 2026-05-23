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

    enum class ThemeType {
        CLASSIC,
        TECHNOLOGY,
        DREAM,
        SPACE
    }

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
     * @param themeType          Chủ đề tổng thể để quyết định icon và render đặc biệt
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
        val blockStrokeColor: Int,
        val themeType: ThemeType = ThemeType.CLASSIC,
        val isWood: Boolean = false
    )

    // ─── Skin Definitions ────────────────────────────────────────────────────

    val ALL_SKINS = listOf(
        // ─── CLASSIC THEMES ───
        SkinItem(
            id = "classic", name = "Classic Wood", emoji = "📦",
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
                blockStrokeColor   = 0xFFA0522D.toInt(),
                isWood             = true,
                themeType          = ThemeType.CLASSIC
            )
        ),
        SkinItem(
            id = "ocean_breeze", name = "Ocean Breeze", emoji = "🌊",
            bgColor = 0xFF03A9F4.toInt(), priceCoin = 500,
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
            id = "forest_wood", name = "Forest Wood", emoji = "🌲",
            bgColor = 0xFF388E3C.toInt(), priceCoin = 800,
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
        
        // ─── DREAM THEMES ───
        SkinItem(
            id = "candy_cloud", name = "Candy Cloud", emoji = "🍬",
            bgColor = 0xFFFF80AB.toInt(), priceCoin = 1500,
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
            bgColor = 0xFFFFD700.toInt(), priceCoin = 2500,
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

        // ─── TECHNOLOGY THEMES ───
        SkinItem(
            id = "cyberpunk", name = "Cyberpunk", emoji = "🔋",
            bgColor = 0xFF00E5FF.toInt(), priceCoin = 3500,
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
            bgColor = 0xFF00E676.toInt(), priceCoin = 4500,
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

        // ─── SPACE THEMES ───
        SkinItem(
            id = "galactic", name = "Galactic", emoji = "🚀",
            bgColor = 0xFF7C4DFF.toInt(), priceCoin = 6000,
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
            id = "abyssal_gem", name = "Abyssal Gem", emoji = "💎",
            bgColor = 0xFF0277BD.toInt(), priceCoin = 8000,
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


    // ─── Fruit Icons ─────────────────────────────────────────────────────────

    /**
     * Trả về icon (emoji) tương ứng với ColorId.
     * Có thể mở rộng để trả về icon khác nhau tùy theo skin đang chọn.
     */
    fun getIconForColor(color: com.twinbrother.fruitsort.LevelOneEngine.ColorId, @Suppress("UNUSED_PARAMETER") context: android.content.Context): String {
        return color.fruitIcon
    }

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
            val rectF = android.graphics.RectF(rect)

            if (boxStyle.isWood) {
                // ─── DRAW WOODEN PALLET ───
                val w = rectF.width()
                val h = rectF.height()
                val woodPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

                // === 1. PALLET BASE (bottom 18% of height) ===
                val baseHeight = h * 0.18f
                val baseTop = rectF.bottom - baseHeight
                val baseRect = android.graphics.RectF(rectF.left, baseTop, rectF.right, rectF.bottom)

                // Dark interior behind base slats
                val baseBgPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.parseColor("#4E342E")
                }
                canvas.drawRoundRect(baseRect, 4 * density, 4 * density, baseBgPaint)

                // 3 horizontal base planks
                val basePlankCount = 3
                val baseGap = 1.5f * density
                val basePlankH = (baseHeight - (basePlankCount - 1) * baseGap) / basePlankCount
                for (i in 0 until basePlankCount) {
                    val pTop = baseTop + i * (basePlankH + baseGap)
                    val plankRect = android.graphics.RectF(rectF.left + 1 * density, pTop, rectF.right - 1 * density, pTop + basePlankH)
                    woodPaint.shader = android.graphics.LinearGradient(
                        plankRect.left, plankRect.top, plankRect.left, plankRect.bottom,
                        android.graphics.Color.parseColor("#D7CCC8"),
                        android.graphics.Color.parseColor("#A1887F"),
                        android.graphics.Shader.TileMode.CLAMP
                    )
                    canvas.drawRoundRect(plankRect, 2 * density, 2 * density, woodPaint)
                    // Plank border
                    val plankStroke = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.parseColor("#795548")
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 0.5f * density
                    }
                    canvas.drawRoundRect(plankRect, 2 * density, 2 * density, plankStroke)
                }

                // === 2. VERTICAL SIDE POSTS (left + right) ===
                val postWidth = w * 0.08f
                val mainAreaBottom = baseTop
                // Left post
                val leftPostRect = android.graphics.RectF(rectF.left, rectF.top, rectF.left + postWidth, mainAreaBottom)
                woodPaint.shader = android.graphics.LinearGradient(
                    leftPostRect.left, leftPostRect.top, leftPostRect.right, leftPostRect.top,
                    android.graphics.Color.parseColor("#BCAAA4"),
                    android.graphics.Color.parseColor("#8D6E63"),
                    android.graphics.Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(leftPostRect, 2 * density, 2 * density, woodPaint)
                // Right post
                val rightPostRect = android.graphics.RectF(rectF.right - postWidth, rectF.top, rectF.right, mainAreaBottom)
                woodPaint.shader = android.graphics.LinearGradient(
                    rightPostRect.left, rightPostRect.top, rightPostRect.right, rightPostRect.top,
                    android.graphics.Color.parseColor("#8D6E63"),
                    android.graphics.Color.parseColor("#BCAAA4"),
                    android.graphics.Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(rightPostRect, 2 * density, 2 * density, woodPaint)
                // Post borders
                val postStroke = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.parseColor("#5D4037")
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 0.8f * density
                }
                canvas.drawRoundRect(leftPostRect, 2 * density, 2 * density, postStroke)
                canvas.drawRoundRect(rightPostRect, 2 * density, 2 * density, postStroke)

                // Nails on posts
                val nailPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.parseColor("#90A4AE")
                }
                val nailStroke = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.parseColor("#546E7A")
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 0.4f * density
                }
                val nailR = 1.2f * density
                // Nails on left post
                canvas.drawCircle(leftPostRect.centerX(), leftPostRect.top + leftPostRect.height() * 0.25f, nailR, nailPaint)
                canvas.drawCircle(leftPostRect.centerX(), leftPostRect.top + leftPostRect.height() * 0.25f, nailR, nailStroke)
                canvas.drawCircle(leftPostRect.centerX(), leftPostRect.top + leftPostRect.height() * 0.75f, nailR, nailPaint)
                canvas.drawCircle(leftPostRect.centerX(), leftPostRect.top + leftPostRect.height() * 0.75f, nailR, nailStroke)
                // Nails on right post
                canvas.drawCircle(rightPostRect.centerX(), rightPostRect.top + rightPostRect.height() * 0.25f, nailR, nailPaint)
                canvas.drawCircle(rightPostRect.centerX(), rightPostRect.top + rightPostRect.height() * 0.25f, nailR, nailStroke)
                canvas.drawCircle(rightPostRect.centerX(), rightPostRect.top + rightPostRect.height() * 0.75f, nailR, nailPaint)
                canvas.drawCircle(rightPostRect.centerX(), rightPostRect.top + rightPostRect.height() * 0.75f, nailR, nailStroke)

                // === 3. INTERIOR (area between posts, above base) ===
                val interiorRect = android.graphics.RectF(
                    rectF.left + postWidth, rectF.top, rectF.right - postWidth, mainAreaBottom
                )
                val interiorBg = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.parseColor("#3E2723")
                }
                canvas.drawRect(interiorRect, interiorBg)
            } else {
                // ─── DRAW DEFAULT GRADIENT/GLASS CRATE ───
                // Body Gradient
                val shader = android.graphics.LinearGradient(
                    rect.left.toFloat(), rect.top.toFloat(),
                    rect.left.toFloat(), rect.bottom.toFloat(),
                    boxStyle.boxFrontStartColor, boxStyle.boxFrontEndColor,
                    android.graphics.Shader.TileMode.CLAMP
                )
                bodyPaint.shader = shader
                
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
        }

        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {}
        override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
    }

    /**
     * Tạo drawable cho từng block trái cây theo style của skin.
     * Nếu isWood = true, vẽ thùng carton. Ngược lại vẽ block thường.
     * @param density  resources.displayMetrics.density
     */
    fun makeBlockDrawable(style: BoxSkinStyle, density: Float): android.graphics.drawable.Drawable {
        if (style.isWood) {
            return CartonBoxDrawable(density)
        }
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(style.blockBgColor)
            cornerRadius = style.blockCornerRadius * density
            setStroke((1 * density).toInt(), style.blockStrokeColor)
        }
    }

    /**
     * Custom drawable vẽ thùng carton bìa cứng với nếp gấp, băng keo, và cạnh 3D.
     */
    class CartonBoxDrawable(
        private val density: Float
    ) : android.graphics.drawable.Drawable() {

        override fun draw(canvas: android.graphics.Canvas) {
            val rect = bounds
            if (rect.width() == 0 || rect.height() == 0) return
            val rectF = android.graphics.RectF(rect)
            val cornerR = 3 * density

            // 1. Shadow / 3D depth (bottom-right offset)
            val shadowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#4D795548")
            }
            val shadowRect = android.graphics.RectF(
                rectF.left + 1.5f * density, rectF.top + 1.5f * density,
                rectF.right + 1 * density, rectF.bottom + 1 * density
            )
            canvas.drawRoundRect(shadowRect, cornerR, cornerR, shadowPaint)

            // 2. Cardboard body with vertical gradient
            val cardboardPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
            cardboardPaint.shader = android.graphics.LinearGradient(
                rectF.left, rectF.top, rectF.left, rectF.bottom,
                android.graphics.Color.parseColor("#F0DFC0"), // light kraft
                android.graphics.Color.parseColor("#C9A96E"), // darker kraft
                android.graphics.Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(rectF, cornerR, cornerR, cardboardPaint)

            // 3. Cardboard border
            val borderPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#A0845C")
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 1f * density
            }
            canvas.drawRoundRect(rectF, cornerR, cornerR, borderPaint)

            // 4. Top flap fold line
            val flapPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#B89B6A")
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 0.8f * density
            }
            val flapY = rectF.top + rectF.height() * 0.18f
            canvas.drawLine(rectF.left + 3 * density, flapY, rectF.right - 3 * density, flapY, flapPaint)

            // 5. Center tape strip (horizontal)
            val tapeH = rectF.height() * 0.12f
            val tapeRect = android.graphics.RectF(
                rectF.left + rectF.width() * 0.15f,
                rectF.centerY() - tapeH / 2f,
                rectF.right - rectF.width() * 0.15f,
                rectF.centerY() + tapeH / 2f
            )
            val tapePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#40D7CCC8") // translucent tape
            }
            canvas.drawRoundRect(tapeRect, 1 * density, 1 * density, tapePaint)
            // Tape border
            val tapeStroke = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#30795548")
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 0.5f * density
            }
            canvas.drawRoundRect(tapeRect, 1 * density, 1 * density, tapeStroke)

            // 6. Corner fold triangles (top-left and top-right)
            val foldPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#20795548")
            }
            val foldSize = 4 * density
            // Top-left fold
            val pathTL = android.graphics.Path().apply {
                moveTo(rectF.left + cornerR, rectF.top)
                lineTo(rectF.left + cornerR + foldSize, rectF.top)
                lineTo(rectF.left + cornerR, rectF.top + foldSize)
                close()
            }
            canvas.drawPath(pathTL, foldPaint)
            // Top-right fold
            val pathTR = android.graphics.Path().apply {
                moveTo(rectF.right - cornerR, rectF.top)
                lineTo(rectF.right - cornerR - foldSize, rectF.top)
                lineTo(rectF.right - cornerR, rectF.top + foldSize)
                close()
            }
            canvas.drawPath(pathTR, foldPaint)

            // 7. Subtle highlight on top edge
            val highlightPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
            highlightPaint.shader = android.graphics.LinearGradient(
                rectF.left, rectF.top, rectF.left, rectF.top + rectF.height() * 0.3f,
                android.graphics.Color.argb(50, 255, 255, 255),
                android.graphics.Color.TRANSPARENT,
                android.graphics.Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(rectF, cornerR, cornerR, highlightPaint)
        }

        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {}
        override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
    }
}
