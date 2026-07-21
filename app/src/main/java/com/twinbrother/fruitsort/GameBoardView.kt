package com.twinbrother.fruitsort

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.graphics.withTranslation
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.min
import kotlin.random.Random

class GameBoardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var engine: LevelOneEngine? = null
    private var skinStyle: SkinManager.BoxSkinStyle = SkinManager.getSelectedStyle(context)
    private var selectedBoxId: Int? = null
    
    private val boxRects = mutableMapOf<Int, RectF>()
    private val gameDensity = resources.displayMetrics.density
    
    private var onBoxClickListener: ((Int) -> Unit)? = null
    
    // Animation properties
    private val selectionAnimators = mutableMapOf<Int, ValueAnimator>()
    private val selectionOffsets = mutableMapOf<Int, Float>()
    
    // Pre-allocated drawing objects
    private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f * gameDensity
        color = Color.parseColor("#FFD54F")
        setShadowLayer(8f * gameDensity, 0f, 0f, Color.parseColor("#FF9800"))
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
        typeface = Typeface.DEFAULT_BOLD
    }
    private val icePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xAA80D8FF.toInt()
        style = Paint.Style.FILL
    }
    private val iceBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF40C4FF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2f * gameDensity
    }
    private val webPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xDDFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * gameDensity
    }
    private val hiddenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xEE1E1E2C.toInt()
    }
    private val tempRect = RectF()

    // Move animation with Parabolic Arc
    private val movingBlocks = mutableListOf<MovingBlock>()
    private val suppressedBlocks = mutableMapOf<Int, Int>()

    // Particle explosion system
    private val particles = mutableListOf<Particle>()

    // Hint highlight system
    private var hintSrcId: Int? = null
    private var hintDstId: Int? = null
    private var hintStartTime: Long = 0
    private val hintDuration = 2000L // 2 seconds
    private val hintPaintSrc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f * gameDensity
        color = 0xFF4CAF50.toInt() // green for source
    }
    private val hintPaintDst = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f * gameDensity
        color = 0xFFFFD54F.toInt() // gold for destination
    }

    // Combo popup system
    private var comboText: String? = null
    private var comboStartTime: Long = 0
    private val comboDuration = 1200L
    private val comboPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = 0xFFFF9800.toInt()
        typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(8f, 0f, 4f, 0x99000000.toInt())
    }

    data class MovingBlock(
        val colorId: LevelOneEngine.ColorId,
        var startX: Float,
        var startY: Float,
        val targetX: Float,
        val targetY: Float,
        val targetId: Int,
        val startTime: Long,
        val duration: Long = 380
    )

    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var size: Float,
        var alpha: Int,
        val color: Int
    )

    fun setEngine(engine: LevelOneEngine?) {
        this.engine = engine
        invalidate()
    }

    fun setSkinStyle(style: SkinManager.BoxSkinStyle) {
        this.skinStyle = style
        invalidate()
    }

    fun setSelectedBox(id: Int?) {
        if (selectedBoxId != id) {
            selectedBoxId?.let { animateSelection(it, false) }
            selectedBoxId = id
            selectedBoxId?.let { animateSelection(it, true) }
        }
    }

    fun setOnBoxClickListener(listener: (Int) -> Unit) {
        onBoxClickListener = listener
    }

    fun animateMove(colorId: LevelOneEngine.ColorId, fromId: Int, toId: Int, count: Int = 1) {
        val fromRect = boxRects[fromId] ?: return
        val targetX: Float
        val targetY: Float
        
        if (toId == -1) {
            targetX = width / 2f
            targetY = height / 2f
        } else {
            val toRect = boxRects[toId] ?: return
            targetX = toRect.centerX()
            targetY = toRect.top
        }
        
        suppressedBlocks[toId] = (suppressedBlocks[toId] ?: 0) + count
        
        val now = System.currentTimeMillis()
        repeat(count) { i ->
            movingBlocks.add(MovingBlock(
                colorId = colorId,
                startX = fromRect.centerX(),
                startY = fromRect.top,
                targetX = targetX,
                targetY = targetY,
                targetId = toId,
                startTime = now + i * 45
            ))
        }
        invalidate()
    }

    fun spawnBurstParticles(centerX: Float, centerY: Float, count: Int = 12) {
        val colors = intArrayOf(
            0xFFFFD54F.toInt(), 0xFFFF9800.toInt(), 0xFF4CAF50.toInt(),
            0xFF2979FF.toInt(), 0xFFE91E63.toInt()
        )
        repeat(count) {
            val angle = Random.nextDouble(0.0, 2.0 * PI)
            val speed = Random.nextFloat() * 6f * gameDensity + 2f * gameDensity
            particles.add(Particle(
                x = centerX,
                y = centerY,
                vx = (cos(angle) * speed).toFloat(),
                vy = (sin(angle) * speed).toFloat(),
                size = Random.nextFloat() * 5f * gameDensity + 3f * gameDensity,
                alpha = 255,
                color = colors.random()
            ))
        }
        invalidate()
    }

    fun clearAnimations() {
        movingBlocks.clear()
        suppressedBlocks.clear()
        hintSrcId = null
        hintDstId = null
        comboText = null
        invalidate()
    }

    fun showHintHighlight(srcId: Int, dstId: Int) {
        hintSrcId = srcId
        hintDstId = dstId
        hintStartTime = System.currentTimeMillis()
        invalidate()
    }

    fun showComboPopup(combo: Int) {
        comboText = "COMBO x$combo! \uD83D\uDD25"
        comboStartTime = System.currentTimeMillis()
        invalidate()
    }

    private fun animateSelection(id: Int, isSelected: Boolean) {
        selectionAnimators[id]?.cancel()
        val startValue = selectionOffsets[id] ?: 0f
        val endValue = if (isSelected) -10f * gameDensity else 0f
        
        val animator = ValueAnimator.ofFloat(startValue, endValue).apply {
            duration = 200
            interpolator = OvershootInterpolator()
            addUpdateListener {
                selectionOffsets[id] = it.animatedValue as Float
                invalidate()
            }
        }
        selectionAnimators[id] = animator
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val engine = this.engine ?: return
        
        val boxes = engine.getBoxes().filter { !it.isArchived }
        if (boxes.isEmpty() && movingBlocks.isEmpty() && particles.isEmpty()) return

        val cols = when {
            boxes.size <= 7 -> 3
            boxes.size <= 14 -> 4
            boxes.size <= 24 -> 5
            else -> 6
        }
        
        // Calculate exact total rows needed
        var remaining = boxes.size
        var totalRows = 0
        while (remaining > 0) {
            val isEven = (totalRows % 2 == 0)
            val rCols = if (isEven) cols else (cols - 1)
            remaining -= rCols
            totalRows++
        }
        if (totalRows == 0) totalRows = 1

        val horizontalGap = 8f * gameDensity
        val totalAvailableWidth = width - 24f * gameDensity
        val baseBoxWidth = (totalAvailableWidth - (cols - 1) * horizontalGap) / cols
        val baseBoxHeight = baseBoxWidth * 1.3f
        val baseStepY = baseBoxHeight + 10f * gameDensity
        val topPad = 8f * gameDensity

        // Dynamic Height Auto-Scaling
        val reqHeight = totalRows * baseStepY + topPad * 2
        val availableHeight = height.toFloat()
        val scale = if (availableHeight > 0 && reqHeight > availableHeight) {
            (availableHeight - topPad * 2) / (reqHeight - topPad * 2)
        } else {
            1.0f
        }.coerceIn(0.5f, 1.0f)

        val boxWidth = baseBoxWidth * scale
        val boxHeight = baseBoxHeight * scale
        val stepY = baseStepY * scale

        boxRects.clear()
        
        if (engine.isBossLevel && engine.currentBossType == 1) {
            drawBossType1Layout(canvas, engine, boxes, boxWidth, boxHeight)
        } else {
            drawGridLayout(canvas, boxes, cols, boxWidth, boxHeight, horizontalGap * scale, stepY, topPad)
        }

        drawMovingBlocks(canvas)
        drawParticles(canvas)
        drawHintHighlights(canvas)
        drawComboPopup(canvas)
        updateAnimations()
    }

    private fun drawGridLayout(
        canvas: Canvas, 
        boxes: List<LevelOneEngine.Box>, 
        cols: Int,
        boxWidth: Float, 
        boxHeight: Float, 
        horizontalGap: Float, 
        stepY: Float, 
        firstRowTopPad: Float
    ) {
        var currentIdx = 0
        var r = 0

        while (currentIdx < boxes.size) {
            val isEvenRow = (r % 2 == 0)
            val rowCols = if (isEvenRow) cols else (cols - 1)
            val rowWidth = rowCols * boxWidth + (rowCols - 1) * horizontalGap
            val startX = (width - rowWidth) / 2f
            
            for (c in 0 until rowCols) {
                if (currentIdx >= boxes.size) break
                val box = boxes[currentIdx++]
                val x = startX + (c * (boxWidth + horizontalGap))
                val y = firstRowTopPad + r * stepY
                
                val rect = boxRects.getOrPut(box.id) { RectF() }
                rect.set(x, y, x + boxWidth, y + boxHeight)
                drawBox(canvas, box, rect)
            }
            r++
        }
    }

    private fun drawBossType1Layout(
        canvas: Canvas, 
        engine: LevelOneEngine, 
        boxes: List<LevelOneEngine.Box>, 
        boxWidth: Float, 
        boxHeight: Float
    ) {
        val sw = width.toFloat()
        val center_x = sw / 2f
        val center_y = height / 2f
        val rx = (sw - boxWidth - 36f * gameDensity) / 2f
        val ry = (height - boxHeight - 36f * gameDensity) / 2f
        
        val truck = engine.getBoxSlots().getOrNull(0)
        if (truck != null) {
            val megaSize = boxWidth * 2.2f
            tempRect.set(center_x - megaSize / 2f, center_y - megaSize / 2f, center_x + megaSize / 2f, center_y + megaSize / 2f)
            drawMegaTruck(canvas, truck, tempRect)
        }

        boxes.forEachIndexed { index, box ->
            val angle = index.toDouble() * 2.0 * Math.PI / boxes.size
            val x = center_x + rx * cos(angle).toFloat()
            val y = center_y + ry * sin(angle).toFloat()
            
            val rect = boxRects.getOrPut(box.id) { RectF() }
            rect.set(x - boxWidth / 2f, y - boxHeight / 2f, x + boxWidth / 2f, y + boxHeight / 2f)
            drawBox(canvas, box, rect)
        }
    }

    private fun drawMegaTruck(canvas: Canvas, slot: LevelOneEngine.BoxSlot, rect: RectF) {
        boxPaint.color = skinStyle.blockBgColor
        boxPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(rect, 16f * gameDensity, 16f * gameDensity, boxPaint)
        
        boxPaint.color = Color.WHITE
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 2f * gameDensity
        canvas.drawRoundRect(rect, 16f * gameDensity, 16f * gameDensity, boxPaint)

        SkinManager.draw2DFruitIcon(canvas, slot.targetColor, rect, gameDensity)
        
        val suppressed = suppressedBlocks[-1] ?: 0
        val displayCount = (slot.filled - suppressed).coerceAtLeast(0)
        
        textPaint.textSize = 22f * resources.displayMetrics.scaledDensity
        canvas.drawText("$displayCount/${slot.capacity}", rect.centerX(), rect.bottom - 16f * gameDensity, textPaint)
    }

    private fun drawBox(canvas: Canvas, box: LevelOneEngine.Box, rect: RectF) {
        val offset = selectionOffsets[box.id] ?: 0f
        canvas.withTranslation(y = offset) {
            val bodyDrawable = SkinManager.makeBoxBodyDrawable(skinStyle, gameDensity)
            bodyDrawable.setBounds(rect.left.toInt(), rect.top.toInt(), rect.right.toInt(), rect.bottom.toInt())
            bodyDrawable.draw(canvas)
            
            val padding = 5f * gameDensity
            val interiorRect = RectF(rect.left + padding, rect.top + padding, rect.right - padding, rect.bottom - padding)
            val blockHeight = (interiorRect.height() - 5f * gameDensity) / 4f
            
            val suppressed = suppressedBlocks[box.id] ?: 0
            val blocksToDraw = box.blocks.size - suppressed
            
            for (i in 0 until blocksToDraw) {
                val fruitColor = box.blocks.elementAt(i)
                val blockY = interiorRect.bottom - (i + 1) * blockHeight
                tempRect.set(interiorRect.left + 2f * gameDensity, blockY, interiorRect.right - 2f * gameDensity, blockY + blockHeight)
                
                val isHidden = i < box.hiddenLayers
                drawBlock(canvas, fruitColor, tempRect, isHidden)
            }
            
            if (box.hasCobweb) drawVectorCobweb(canvas, rect)
            if (box.isFrozen) drawIceCrystalOverlay(canvas, rect)
            if (box.isLockedByChain) drawMetallicChains(canvas, rect)
            
            if (selectedBoxId == box.id) {
                glowPaint.strokeWidth = 3.5f * gameDensity
                canvas.drawRoundRect(rect, 10f * gameDensity, 10f * gameDensity, glowPaint)
            }
        }
    }

    private fun drawBlock(canvas: Canvas, colorId: LevelOneEngine.ColorId, rect: RectF, isHidden: Boolean) {
        if (isHidden) {
            canvas.drawRoundRect(rect, 5f * gameDensity, 5f * gameDensity, hiddenPaint)
            val goldBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFC107.toInt() }
            canvas.drawCircle(rect.centerX(), rect.centerY(), rect.height() * 0.28f, goldBadgePaint)
            textPaint.textSize = rect.height() * 0.4f
            textPaint.color = Color.BLACK
            canvas.drawText("?", rect.centerX(), rect.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2f), textPaint)
            textPaint.color = Color.WHITE
        } else {
            val blockDrawable = SkinManager.makeBlockDrawable(skinStyle, gameDensity)
            blockDrawable.setBounds(rect.left.toInt(), rect.top.toInt(), rect.right.toInt(), rect.bottom.toInt())
            blockDrawable.draw(canvas)
            
            SkinManager.draw2DFruitIcon(canvas, colorId, rect, gameDensity)
        }
    }

    private fun drawVectorCobweb(canvas: Canvas, rect: RectF) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val r = Math.min(rect.width(), rect.height()) * 0.42f
        
        for (i in 0 until 8) {
            val angle = i * PI / 4.0
            val ex = cx + r * cos(angle).toFloat()
            val ey = cy + r * sin(angle).toFloat()
            canvas.drawLine(cx, cy, ex, ey, webPaint)
        }
        val loopCount = 3
        for (l in 1..loopCount) {
            val lr = r * (l.toFloat() / loopCount)
            canvas.drawCircle(cx, cy, lr, webPaint)
        }
    }

    private fun drawIceCrystalOverlay(canvas: Canvas, rect: RectF) {
        canvas.drawRoundRect(rect, 8f * gameDensity, 8f * gameDensity, icePaint)
        canvas.drawRoundRect(rect, 8f * gameDensity, 8f * gameDensity, iceBorderPaint)
        val crackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 1.5f * gameDensity
        }
        canvas.drawLine(rect.left + rect.width() * 0.2f, rect.top + rect.height() * 0.1f, rect.centerX(), rect.centerY(), crackPaint)
        canvas.drawLine(rect.centerX(), rect.centerY(), rect.right - rect.width() * 0.2f, rect.bottom - rect.height() * 0.1f, crackPaint)
    }

    private fun drawMetallicChains(canvas: Canvas, rect: RectF) {
        val chainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFB0BEC5.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 3.5f * gameDensity
        }
        canvas.drawLine(rect.left, rect.top, rect.right, rect.bottom, chainPaint)
        canvas.drawLine(rect.right, rect.top, rect.left, rect.bottom, chainPaint)
    }

    private fun drawMovingBlocks(canvas: Canvas) {
        if (movingBlocks.isEmpty()) return
        val currentTime = System.currentTimeMillis()
        for (block in movingBlocks) {
            val elapsedTime = currentTime - block.startTime
            if (elapsedTime < 0) continue
            val rawProgress = (elapsedTime.toFloat() / block.duration).coerceIn(0f, 1f)
            
            val arcHeight = 50f * gameDensity
            val x = block.startX + (block.targetX - block.startX) * rawProgress
            val linearY = block.startY + (block.targetY - block.startY) * rawProgress
            val arcY = linearY - (arcHeight * sin(rawProgress * PI)).toFloat()
            
            val size = 30f * gameDensity 
            tempRect.set(x - size / 2f, arcY - size / 2f, x + size / 2f, arcY + size / 2f)
            drawBlock(canvas, block.colorId, tempRect, false)
        }
    }

    private fun drawParticles(canvas: Canvas) {
        if (particles.isEmpty()) return
        val pPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx
            p.y += p.vy
            p.alpha = (p.alpha - 10).coerceAtLeast(0)
            if (p.alpha <= 0) {
                iterator.remove()
                continue
            }
            pPaint.color = p.color
            pPaint.alpha = p.alpha
            canvas.drawCircle(p.x, p.y, p.size, pPaint)
        }
    }

    private fun updateAnimations() {
        if (movingBlocks.isEmpty() && particles.isEmpty()) return
        
        val currentTime = System.currentTimeMillis()
        val iterator = movingBlocks.iterator()
        var changed = false
        while (iterator.hasNext()) {
            val block = iterator.next()
            if (currentTime >= block.startTime + block.duration) {
                val currentSuppressed = suppressedBlocks[block.targetId] ?: 0
                if (currentSuppressed > 0) {
                    suppressedBlocks[block.targetId] = currentSuppressed - 1
                }
                spawnBurstParticles(block.targetX, block.targetY, 8)
                iterator.remove()
                changed = true
            }
        }
        
        postInvalidateOnAnimation()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y
            
            for ((id, rect) in boxRects) {
                val offset = selectionOffsets[id] ?: 0f
                val adjustedRect = RectF(rect.left, rect.top + offset, rect.right, rect.bottom + offset)
                
                if (adjustedRect.contains(x, y)) {
                    onBoxClickListener?.invoke(id)
                    performClick()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    // ─── Hint & Combo Rendering ─────────────────────────────────────────

    private fun drawHintHighlights(canvas: Canvas) {
        val srcId = hintSrcId ?: return
        val dstId = hintDstId ?: return
        val elapsed = System.currentTimeMillis() - hintStartTime
        if (elapsed > hintDuration) {
            hintSrcId = null
            hintDstId = null
            return
        }

        // Pulsing alpha effect
        val phase = (elapsed % 500) / 500f
        val alpha = (128 + 127 * sin(phase * 2 * PI)).toInt().coerceIn(80, 255)

        boxRects[srcId]?.let { rect ->
            hintPaintSrc.alpha = alpha
            val r = 8f * gameDensity
            canvas.drawRoundRect(rect, r, r, hintPaintSrc)
        }
        boxRects[dstId]?.let { rect ->
            hintPaintDst.alpha = alpha
            val r = 8f * gameDensity
            canvas.drawRoundRect(rect, r, r, hintPaintDst)
        }
        invalidate()
    }

    private fun drawComboPopup(canvas: Canvas) {
        val text = comboText ?: return
        val elapsed = System.currentTimeMillis() - comboStartTime
        if (elapsed > comboDuration) {
            comboText = null
            return
        }

        val progress = elapsed.toFloat() / comboDuration
        // Scale up then hold then fade
        val scale = if (progress < 0.2f) progress / 0.2f else 1f
        val alpha = if (progress > 0.7f) ((1f - progress) / 0.3f * 255).toInt() else 255

        comboPaint.textSize = 28f * gameDensity * scale
        comboPaint.alpha = alpha.coerceIn(0, 255)

        val cx = width / 2f
        val cy = height * 0.35f - (progress * 30f * gameDensity) // float upward
        canvas.drawText(text, cx, cy, comboPaint)
        invalidate()
    }
}
