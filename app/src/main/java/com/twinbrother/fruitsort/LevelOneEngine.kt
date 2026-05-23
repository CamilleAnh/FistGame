package com.twinbrother.fruitsort

import java.util.Stack
import kotlin.random.Random
import kotlin.math.max

/**
 * Engine logic V3.6: Mega Truck "Direct Pour" Mechanism.
 * Ở màn Boss 1, xe tải đóng vai trò như một hố thu hoạch trực tiếp từng quả.
 */
class LevelOneEngine(val levelId: Int = 1) {

    enum class ColorId(val colorHex: String, val displayName: String, val fruitIcon: String) {
        STRAWBERRY("#FF4B4B", "STRAWBERRY", "🍓"), ORANGE("#FFA726", "ORANGE", "🍊"), 
        APPLE_GREEN("#66BB6A", "APPLE", "🍏"), BANANA("#FDD835", "BANANA", "🍌"), 
        PEACH("#FFAB91", "PEACH", "🍑"), MANGO("#FF9800", "MANGO", "🥭"),
        GRAPE("#AB47BC", "GRAPE", "🍇"), WATERMELON("#EF5350", "WATERMELON", "🍉"), 
        PINEAPPLE("#FFEE58", "PINEAPPLE", "🍍"), BLUEBERRY("#5C6BC0", "BLUEBERRY", "🫐"), 
        PEAR("#D4E157", "PEAR", "🍐"), COCONUT("#795548", "COCONUT", "🥥"),
        KIWI("#9CCC65", "KIWI", "🥝"), CHERRY("#F44336", "CHERRY", "🍒"), 
        LEMON("#FFF176", "LEMON", "🍋"), AVOCADO("#99CC33", "AVOCADO", "🥑"),
        TOMATO("#E53935", "TOMATO", "🍅"), CORN("#FFB300", "CORN", "🌽"),
        CARROT("#FB8C00", "CARROT", "🥕"), EGGPLANT("#8E24AA", "EGGPLANT", "🍆"),
        BROCCOLI("#43A047", "BROCCOLI", "🥦"), POTATO("#8D6E63", "POTATO", "🥔"),
        CHILI("#E53935", "CHILI", "🌶️"), SWEET_POTATO("#D81B60", "SWEET POTATO", "🍠"),
        ONION("#F4511E", "ONION", "🧅"), MUSHROOM("#D32F2F", "MUSHROOM", "🍄"),
        BELL_PEPPER("#7CB342", "BELL PEPPER", "🫑"), CUCUMBER("#388E3C", "CUCUMBER", "🥒"),
        GARLIC("#BDBDBD", "GARLIC", "🧄"), PEANUT("#A1887F", "PEANUT", "🥜"),
        EMPTY("#333333", "EMPTY", "");

        companion object {
            val allFruits by lazy { values().filter { it != EMPTY } }
        }
    }

    data class Box(
        val id: Int,
        var capacity: Int = 4,
        val blocks: Stack<ColorId> = Stack(),
        var isArchived: Boolean = false,
        var isFrozen: Boolean = false,
        var isLockedByChain: Boolean = false,
        var hasCobweb: Boolean = false,
        var hiddenLayers: Int = 0
    ) {
        fun isEmpty() = blocks.isEmpty()
        fun isFull() = blocks.size >= capacity
        fun peekColor() = if (blocks.isEmpty()) ColorId.EMPTY else blocks.peek()
        fun isComplete(): Boolean {
            if (isArchived || isFrozen || isLockedByChain || hasCobweb) return false
            if (hiddenLayers > 0) return false
            if (blocks.size < 4) return false 
            val firstColor = blocks[0]
            return blocks.all { it == firstColor }
        }
    }

    data class BoxSlot(
        val id: Int,
        var targetColor: ColorId,
        var capacity: Int = 1, // Ở Boss 1, capacity sẽ là tổng số quả (vd: 32 quả)
        var filled: Int = 0,
        var turnsLeft: Int = 25
    ) {
        fun remaining() = capacity - filled
    }

    private val boxes = mutableListOf<Box>()
    private val boxSlots = mutableListOf<BoxSlot>()
    private val random = Random(levelId.toLong())
    
    var completedBoxesCount = 0
    var totalFullBoxesCount = 0
    private var colorsUsed = listOf<ColorId>()
    
    var selectedBoxIndex: Int? = null
    var isGameOver = false
    var isWin = false
    var isBagMechanismEnabled = false
    var isBossLevel = false
    var currentBossType = 0 

    init { setupLevel() }

    private fun setupLevel() {
        isBossLevel = (levelId > 0 && levelId % 20 == 0)
        if (isBossLevel) currentBossType = ((levelId / 20 - 1) % 4) + 1

        var numColors = when {
            levelId < 20 -> 3
            levelId < 100 -> 6
            levelId < 300 -> 9
            else -> 12
        }
        if (isBossLevel) numColors = (numColors + 2).coerceAtMost(16)

        val baseMultiplier = 1.1 + (levelId / 1000.0) * 0.5 
        val finalMultiplier = if (isBossLevel) baseMultiplier + 0.2 else baseMultiplier

        totalFullBoxesCount = (numColors * finalMultiplier).toInt()
        isBagMechanismEnabled = levelId >= 20
        
        // Cụ thể cho Boss Type 1: Tối thiểu 10 hộp full, và KHÔNG CÓ hộp trống
        if (isBossLevel && currentBossType == 1) {
            totalFullBoxesCount = maxOf(totalFullBoxesCount, 12)
        }
        val totalBoxesCount = totalFullBoxesCount + (if (isBossLevel && currentBossType == 1) 0 else 3)
        
        val allAvailable = ColorId.allFruits.shuffled(random)
        colorsUsed = allAvailable.take(numColors)

        boxes.clear()
        repeat(totalBoxesCount) { boxes.add(Box(it)) }

        generateFilledAndShuffledLevel(numColors, totalFullBoxesCount)
        applyBossMechanics()

        completedBoxesCount = 0
        setupInitialBags()
    }

    private fun applyBossMechanics() {
        if (!isBossLevel) {
            if (levelId >= 20) boxes.filter { it.blocks.size >= 2 }.forEach { it.hiddenLayers = 1 }
            return
        }
        when (currentBossType) {
            1 -> { /* T1: No hidden layers */ }
            2 -> boxes.filter { !it.isEmpty() }.forEach { it.hasCobweb = true }
            3 -> boxes.filter { !it.isEmpty() }.shuffled(random).take(3).forEach { it.isFrozen = true }
            4 -> boxes.forEach { if (!it.isEmpty()) it.hiddenLayers = it.blocks.size }
        }
    }

    private var type1TargetColor: ColorId? = null

    private fun generateFilledAndShuffledLevel(numColors: Int, totalFull: Int) {
        val pool = mutableListOf<ColorId>()
        if (isBossLevel && currentBossType == 1) {
            type1TargetColor = colorsUsed.random(random)
            val targetBoxes = random.nextInt(4, 6) // 16 to 20 fruits
            repeat(targetBoxes * 4) { pool.add(type1TargetColor!!) }
            val remainingBoxes = totalFull - targetBoxes
            val otherColors = colorsUsed.filter { it != type1TargetColor }
            for (i in 0 until remainingBoxes) {
                val color = otherColors[i % otherColors.size]
                repeat(4) { pool.add(color) }
            }
        } else {
            for (i in 0 until totalFull) {
                val color = colorsUsed[i % numColors]
                repeat(4) { pool.add(color) }
            }
        }
        val shuffled = pool.shuffled(random).toMutableList()
        var idx = 0
        for (i in 0 until totalFull) {
            boxes[i].blocks.clear()
            repeat(4) { boxes[i].blocks.push(shuffled[idx++]) }
        }
        
        if (isBossLevel && currentBossType == 1) {
            val target = type1TargetColor!!
            val hasTopTarget = boxes.any { it.peekColor() == target }
            if (!hasTopTarget) {
                for (box in boxes) {
                    val targetIdx = box.blocks.indexOf(target)
                    if (targetIdx != -1) {
                        val topColor = box.blocks.peek()
                        box.blocks[box.blocks.size - 1] = target
                        box.blocks[targetIdx] = topColor
                        break
                    }
                }
            }
        }
    }

    private fun setupInitialBags() {
        boxSlots.clear()
        if (isBossLevel && currentBossType == 1) {
            // MEGA TRUCK: Chọn 1 màu làm mục tiêu duy nhất. Capacity = Tổng số quả màu đó có trên bàn.
            val target = type1TargetColor ?: colorsUsed.random(random)
            // Tìm chính xác số quả màu đó có trong pool
            var exactCount = 0
            boxes.forEach { box -> box.blocks.forEach { if (it == target) exactCount++ } }
            
            boxSlots.add(BoxSlot(0, target, capacity = exactCount, turnsLeft = exactCount * 4))
        } else {
            val available = boxes.filter { !it.isEmpty() }.map { it.peekColor() }.distinct().filter { it != ColorId.EMPTY }
            if (available.isNotEmpty()) {
                val p = available.shuffled(random)
                boxSlots.add(BoxSlot(0, p[0], capacity = 1, turnsLeft = 25))
                if (p.size > 1) boxSlots.add(BoxSlot(1, p[1], capacity = 1, turnsLeft = 25))
            }
        }
    }

    /**
     * Logic đặc biệt cho Mega Truck: Đổ từng quả vào xe.
     * Trả về số lượng quả thực tế đã được chuyển đi.
     */
    fun pourFruitsToTruck(srcIndex: Int): Int {
        if (isGameOver || !isBossLevel || currentBossType != 1) return 0
        val src = boxes.getOrNull(srcIndex) ?: return 0
        val truck = boxSlots.getOrNull(0) ?: return 0
        
        if (src.isEmpty() || src.isFrozen || src.isLockedByChain || src.hasCobweb) return 0
        if (src.peekColor() != truck.targetColor) return 0

        var countMoved = 0
        while (!src.isEmpty() && src.peekColor() == truck.targetColor && truck.remaining() > 0 && (src.blocks.size - 1) >= src.hiddenLayers) {
            src.blocks.pop()
            truck.filled++
            countMoved++
        }
        
        if (src.isEmpty()) src.hiddenLayers = 0
        if (countMoved > 0) consumeTurn()

        // Check thắng Boss 1: Xe tải đầy quả
        if (truck.remaining() <= 0) {
            isGameOver = true
            isWin = true
        }
        return countMoved
    }

    fun handleBoxClick(index: Int): Boolean {
        if (isGameOver) return false
        val clicked = boxes.getOrNull(index) ?: return false
        if (clicked.isArchived) return false
        if (clicked.hasCobweb) { clicked.hasCobweb = false; return consumeTurn() }
        if (clicked.isLockedByChain || (clicked.isComplete() && !isBossLevel)) return false

        val srcIdx = selectedBoxIndex
        if (srcIdx == null) {
            if (!clicked.isEmpty() && !clicked.isFrozen && ((clicked.blocks.size - 1) >= clicked.hiddenLayers || (isBossLevel && currentBossType == 4))) {
                selectedBoxIndex = index
                return true
            }
        } else {
            if (srcIdx == index) { selectedBoxIndex = null; return true }
            val src = boxes[srcIdx]
            if (canMove(src, clicked)) { executeMove(src, clicked); selectedBoxIndex = null; return true }
            selectedBoxIndex = if (!clicked.isEmpty() && !clicked.isFrozen) index else null
        }
        return false
    }

    fun canMove(s: Box, d: Box) = !s.isEmpty() && d.blocks.size < d.capacity && 
            (d.isEmpty() || d.peekColor() == s.peekColor()) && ((s.blocks.size - 1) >= s.hiddenLayers || (isBossLevel && currentBossType == 4)) && !d.isFrozen

    fun executeMove(s: Box, d: Box) {
        val color = s.peekColor()
        val originalHiddenLimit = s.hiddenLayers 
        while (!s.isEmpty() && s.peekColor() == color && ((s.blocks.size - 1) >= originalHiddenLimit || (isBossLevel && currentBossType == 4)) && d.blocks.size < d.capacity) {
            d.blocks.push(s.blocks.pop())
            if (s.hiddenLayers >= s.blocks.size) s.hiddenLayers = (s.blocks.size - 1).coerceAtLeast(0)
        }
        if (s.isEmpty()) s.hiddenLayers = 0
        consumeTurn()
    }

    private fun consumeTurn(): Boolean {
        if (!isBagMechanismEnabled) return true
        boxSlots.forEach { box ->
            if (box.turnsLeft > 0) {
                box.turnsLeft--
                if (box.turnsLeft <= 0 && !isGameOver) { isGameOver = true; isWin = false }
            }
        }
        return true
    }

    fun archiveBox(id: Int) {
        if (isBossLevel && currentBossType == 1) return // Boss 1 không dùng cơ chế archive box
        val box = boxes.find { it.id == id } ?: return
        if (box.isArchived) return
        val color = if (box.blocks.isNotEmpty()) box.blocks[0] else ColorId.EMPTY
        val bag = boxSlots.find { it.targetColor == color && it.remaining() > 0 }
        if (bag != null) {
            bag.filled++
            box.isArchived = true
            box.blocks.clear()
            completedBoxesCount++
            boxes.forEach { it.isLockedByChain = false }
            
            // Unfreeze one box for Type 3
            if (isBossLevel && currentBossType == 3) {
                boxes.firstOrNull { it.isFrozen }?.let { it.isFrozen = false }
            }
            
            if (bag.remaining() <= 0) replaceBag(bag.id)
        } else if (!isBagMechanismEnabled) {
            box.isArchived = true
            box.blocks.clear()
            completedBoxesCount++
        }
        if (completedBoxesCount >= totalFullBoxesCount) { isGameOver = true; isWin = true }
    }

    private fun replaceBag(id: Int) {
        if (isBossLevel && currentBossType == 1) return 
        val idx = boxSlots.indexOfFirst { it.id == id }
        if (idx == -1) return
        val other = if (boxSlots.size > 1) boxSlots[1 - idx].targetColor else null
        val completedWaiting = boxes.filter { !it.isArchived && it.isComplete() }.map { it.blocks[0] }.filter { it != other && boxSlots.none { b -> b.targetColor == it } }
        if (completedWaiting.isNotEmpty()) {
            boxSlots[idx] = BoxSlot(id, completedWaiting.random(random), turnsLeft = 25)
            return
        }
        val onBoard = boxes.filter { !it.isArchived && !it.isEmpty() }.flatMap { it.blocks }.distinct()
        val pool = onBoard.filter { it != other }
        if (pool.isNotEmpty()) boxSlots[idx] = BoxSlot(id, pool.shuffled(random).first(), turnsLeft = 25)
        else boxSlots.removeAt(idx)
    }

    fun archiveAllReady(): List<Int> {
        val archived = mutableListOf<Int>()
        var changed = true
        while (changed && !isGameOver) {
            changed = false
            boxes.filter { !it.isArchived && it.isComplete() }.forEach { box ->
                val color = box.blocks[0]
                val canArchive = !isBagMechanismEnabled || boxSlots.any { it.targetColor == color && it.remaining() > 0 }
                if (canArchive) { archiveBox(box.id); archived.add(box.id); changed = true }
            }
        }
        return archived
    }

    fun getBoxes() = boxes
    fun getBoxSlots() = boxSlots

    fun isDeadlocked(): Boolean {
        if (isGameOver) return false
        val active = boxes.filter { !it.isArchived }
        if (active.any { it.hasCobweb }) return false
        for (src in active) {
            if (src.isEmpty() || src.isFrozen || src.isLockedByChain) continue
            if ((src.blocks.size - 1) < src.hiddenLayers) continue
            // Boss 1: Có thể di chuyển vào xe tải
            if (isBossLevel && currentBossType == 1) {
                val truck = boxSlots.getOrNull(0)
                if (truck != null && src.peekColor() == truck.targetColor) return false
            }
            for (dst in active) {
                if (src.id == dst.id) continue
                if (canMove(src, dst)) return false
            }
        }
        return true
    }

    fun rerollBags() {
        if (boxSlots.isEmpty()) return
        val pool = boxes.filter { !it.isArchived && !it.isEmpty() }.flatMap { it.blocks }.distinct()
        if (pool.isEmpty()) return
        boxSlots.forEachIndexed { i, box ->
            val otherColor = if (boxSlots.size > 1) boxSlots[1 - i].targetColor else null
            val newColor = pool.firstOrNull { it != otherColor && it != box.targetColor } ?: pool.firstOrNull() ?: return@forEachIndexed
            boxSlots[i] = BoxSlot(box.id, newColor, capacity = box.capacity, turnsLeft = 30)
        }
    }

    fun revealHiddenLayers(boxId: Int) {
        val box = boxes.find { it.id == boxId } ?: return
        if (!box.isArchived) box.hiddenLayers = 0
    }

    fun shuffleAllBoxes() {
        val activeBoxes = boxes.filter { !it.isArchived }
        val boxSizes = activeBoxes.map { it.blocks.size }
        val boxHiddenLayers = activeBoxes.map { it.hiddenLayers }
        val allBlocks = activeBoxes.flatMap { it.blocks.toList() }.toMutableList().also { it.shuffle(random) }
        var idx = 0
        activeBoxes.forEachIndexed { i, box ->
            box.blocks.clear()
            repeat(boxSizes[i]) { if (idx < allBlocks.size) box.blocks.push(allBlocks[idx++]) }
            box.hiddenLayers = minOf(boxHiddenLayers[i], (box.blocks.size - 1).coerceAtLeast(0))
        }
    }
}
