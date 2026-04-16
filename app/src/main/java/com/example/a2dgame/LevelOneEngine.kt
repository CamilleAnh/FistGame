package com.yourname.fruitsort

import java.util.Stack
import kotlin.random.Random

/**
 * Engine xử lý logic Xếp Hộp Trái Cây (1000 Level).
 * Cập nhật Cơ chế Boss & Siết độ khó (V3.3) - Giữ nguyên cấu trúc render.
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
        var capacity: Int = 1,
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

    init { setupLevel() }

    private fun setupLevel() {
        // Nhận diện Boss mỗi 20 màn
        isBossLevel = (levelId > 0 && levelId % 20 == 0)

        // 1. Phân bổ màu sắc
        var numDistinctColors = when {
            levelId < 10 -> 3
            levelId < 30 -> 5
            levelId < 100 -> 8
            levelId < 500 -> 12
            else -> 16
        }
        if (isBossLevel) numDistinctColors = (numDistinctColors + 1).coerceAtMost(16)

        // 2. Hệ số nhân số lượng hộp (Difficulty Scaling V3.3)
        val multiplier = when {
            levelId < 100 -> 1.2
            levelId < 400 -> 1.3
            levelId < 800 -> 1.5
            else -> 1.8
        }
        totalFullBoxesCount = (numDistinctColors * multiplier).toInt()
        isBagMechanismEnabled = levelId >= 20
        
        val numEmptyBoxes = if (levelId >= 500) 2 else 3
        val totalBoxesCount = totalFullBoxesCount + numEmptyBoxes
        
        val allAvailable = ColorId.allFruits.shuffled(random)
        colorsUsed = allAvailable.take(numDistinctColors)

        boxes.clear()
        repeat(totalBoxesCount) { boxes.add(Box(it)) }

        generateFilledAndShuffledLevel(numDistinctColors, totalFullBoxesCount)

        // 3. Cơ chế nâng cao cho level cực khó
        if (levelId > 150) injectComplexityIntoEmptyBoxes()

        // 4. Áp dụng chướng ngại vật (Siết chặt độ ẩn theo V3.3)
        if (levelId >= 20) {
            boxes.filter { it.blocks.size >= 2 }.forEach { 
                // Màn Boss ẩn sâu hơn (chỉ lộ 1 quả trên đỉnh)
                it.hiddenLayers = if (isBossLevel) (it.blocks.size - 1) else 2.coerceAtMost(it.blocks.size - 1)
            }
        }
        
        if (levelId >= 80 || (isBossLevel && levelId >= 40)) {
            val spiderCount = if (isBossLevel) (totalFullBoxesCount / 3) else (totalFullBoxesCount / 4)
            boxes.filter { it.blocks.isNotEmpty() }.shuffled(random).take(spiderCount.coerceAtLeast(1)).forEach { it.hasCobweb = true }
        }
        
        if (levelId >= 120 || (isBossLevel && levelId >= 60)) {
            boxes.filter { it.blocks.isNotEmpty() && !it.hasCobweb }.shuffled(random).take(if (isBossLevel) 2 else 1).forEach { it.isFrozen = true }
        }

        completedBoxesCount = 0
        if (isBagMechanismEnabled) setupInitialBags()
    }

    private fun generateFilledAndShuffledLevel(numColors: Int, totalFull: Int) {
        val pool = mutableListOf<ColorId>()
        for (i in 0 until totalFull) {
            val color = colorsUsed[i % numColors]
            repeat(4) { pool.add(color) }
        }
        val shuffled = pool.shuffled(random).toMutableList()
        var idx = 0
        for (i in 0 until totalFull) {
            boxes[i].blocks.clear()
            repeat(4) { boxes[i].blocks.push(shuffled[idx++]) }
        }
    }

    private fun injectComplexityIntoEmptyBoxes() {
        val emptyBoxes = boxes.filter { it.isEmpty() }
        val fullBoxes = boxes.filter { it.blocks.size == 4 }
        if (emptyBoxes.isNotEmpty() && fullBoxes.size >= 2) {
            val targetEmpty = emptyBoxes.random(random)
            repeat(2) {
                val src = boxes.filter { it.blocks.size > 2 }.random(random)
                targetEmpty.blocks.push(src.blocks.pop())
            }
        }
    }

    private fun setupInitialBags() {
        boxSlots.clear()
        val available = boxes.filter { !it.isEmpty() }.map { it.peekColor() }.distinct().filter { it != ColorId.EMPTY }
        if (available.isNotEmpty()) {
            val p = available.shuffled(random)
            boxSlots.add(createBox(0, p[0]))
            if (p.size > 1) boxSlots.add(createBox(1, p[1]))
        }
    }

    // Túi của Boss chỉ có 15 lượt, màn thường 25 lượt
    private fun createBox(id: Int, color: ColorId) = BoxSlot(
        id = id, 
        targetColor = color, 
        turnsLeft = if (isBossLevel) 15 else 25
    )

    fun handleBoxClick(index: Int): Boolean {
        if (isGameOver) return false
        val clicked = boxes.getOrNull(index) ?: return false
        if (clicked.isArchived) return false
        if (clicked.hasCobweb) {
            clicked.hasCobweb = false
            return consumeTurn()
        }
        if (clicked.isLockedByChain || clicked.isComplete()) return false
        val srcIdx = selectedBoxIndex
        if (srcIdx == null) {
            if (!clicked.isEmpty() && !clicked.isFrozen && (clicked.blocks.size - 1) >= clicked.hiddenLayers) {
                selectedBoxIndex = index
                return true
            }
        } else {
            if (srcIdx == index) {
                selectedBoxIndex = null
                return true
            }
            val src = boxes[srcIdx]
            if (canMove(src, clicked)) {
                executeMove(src, clicked)
                selectedBoxIndex = null
                return true
            }
            selectedBoxIndex = if (!clicked.isEmpty() && !clicked.isFrozen) index else null
        }
        return false
    }

    fun canMove(s: Box, d: Box) = !s.isEmpty() && d.blocks.size < d.capacity && 
            (d.isEmpty() || d.peekColor() == s.peekColor()) && (s.blocks.size - 1) >= s.hiddenLayers

    fun executeMove(s: Box, d: Box) {
        val color = s.peekColor()
        val originalHiddenLimit = s.hiddenLayers 
        while (!s.isEmpty() && s.peekColor() == color && (s.blocks.size - 1) >= originalHiddenLimit && d.blocks.size < d.capacity) {
            d.blocks.push(s.blocks.pop())
            if (s.hiddenLayers >= s.blocks.size && !s.isEmpty()) s.hiddenLayers = s.blocks.size - 1
        }
        if (s.isEmpty()) s.hiddenLayers = 0
        if (d.isFrozen) d.isFrozen = false
        consumeTurn()
    }

    private fun consumeTurn(): Boolean {
        if (!isBagMechanismEnabled) return true
        boxSlots.forEach { box ->
            if (box.turnsLeft > 0) {
                box.turnsLeft--
                if (box.turnsLeft <= 0 && !isGameOver) {
                    isGameOver = true
                    isWin = false
                }
            }
        }
        return true
    }

    fun archiveBox(id: Int) {
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
            if (bag.remaining() <= 0) replaceBag(bag.id)
        } else if (!isBagMechanismEnabled) {
            box.isArchived = true
            box.blocks.clear()
            completedBoxesCount++
        }
        if (completedBoxesCount >= totalFullBoxesCount) {
            isGameOver = true
            isWin = true
        }
    }

    private fun replaceBag(id: Int) {
        val idx = boxSlots.indexOfFirst { it.id == id }
        if (idx == -1) return
        val other = if (boxSlots.size > 1) boxSlots[1 - idx].targetColor else null
        val completedWaiting = boxes.filter { !it.isArchived && it.isComplete() }.map { it.blocks[0] }.filter { it != other && boxSlots.none { b -> b.targetColor == it } }
        if (completedWaiting.isNotEmpty()) {
            boxSlots[idx] = createBox(id, completedWaiting.random(random))
            return
        }
        val onBoard = boxes.filter { !it.isArchived && !it.isEmpty() }.flatMap { it.blocks }.distinct()
        val pool = onBoard.filter { it != other }
        if (pool.isNotEmpty()) boxSlots[idx] = createBox(id, pool.shuffled(random).first())
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
                if (canArchive) {
                    archiveBox(box.id)
                    archived.add(box.id)
                    changed = true
                }
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
            for (dst in active) {
                if (src.id == dst.id) continue
                if (canMove(src, dst)) return false
            }
        }
        return true
    }

    fun rerollBags() {
        if (boxSlots.isEmpty()) return
        val completedColors = boxes.filter { !it.isArchived && it.isComplete() }.map { it.blocks[0] }.distinct()
        val frequentColors = boxes.filter { !it.isArchived }.flatMap { it.blocks.toList() }.filter { it != ColorId.EMPTY }.groupBy { it }.entries.sortedByDescending { it.value.size }.map { it.key }
        val pool = (completedColors + frequentColors).distinct()
        if (pool.isEmpty()) return
        boxSlots.forEachIndexed { i, box ->
            val otherColor = if (boxSlots.size > 1) boxSlots[1 - i].targetColor else null
            val newColor = pool.firstOrNull { it != otherColor && it != box.targetColor } ?: pool.firstOrNull() ?: return@forEachIndexed
            boxSlots[i] = BoxSlot(id = box.id, targetColor = newColor, capacity = box.capacity, turnsLeft = if (isBossLevel) 20 else 30)
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
