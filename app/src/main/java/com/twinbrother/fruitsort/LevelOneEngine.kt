package com.twinbrother.fruitsort

import java.util.Stack
import kotlin.random.Random

/**
 * Engine logic V3.8.2: Sửa lỗi hiển thị Boss 4 (Level 80) và tối ưu hóa logic di chuyển.
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
        fun peekColor() = if (blocks.isEmpty()) ColorId.EMPTY else blocks.peek()
        fun isComplete() = blocks.size == 4 && blocks.distinct().size == 1
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
    var currentBossType = 0 

    init { setupLevel() }

    private fun setupLevel() {
        isBossLevel = (levelId > 0 && levelId % 20 == 0)
        if (isBossLevel) currentBossType = ((levelId / 20 - 1) % 4) + 1

        var numColors = when {
            levelId < 15 -> 3
            levelId < 40 -> 4
            levelId < 100 -> 6
            levelId < 250 -> 8
            levelId < 500 -> 11
            else -> 14
        }
        if (isBossLevel) numColors = (numColors + 2).coerceAtMost(16)

        val baseMultiplier = 1.2 + (levelId / 1000.0) * 0.7 
        val finalMultiplier = if (isBossLevel) baseMultiplier + 0.3 else baseMultiplier

        totalFullBoxesCount = (numColors * finalMultiplier).toInt()
        isBagMechanismEnabled = levelId >= 20
        val totalBoxesCount = totalFullBoxesCount + (if (levelId > 400) 2 else 3)
        
        val allAvailable = ColorId.allFruits.shuffled(random)
        colorsUsed = allAvailable.take(numColors)

        boxes.clear()
        repeat(totalBoxesCount) { boxes.add(Box(it)) }

        generateFilledAndShuffledLevel(numColors, totalFullBoxesCount)
        applyDifficultyScaling()

        completedBoxesCount = 0
        setupInitialBags()
    }

    private fun applyDifficultyScaling() {
        if (!isBossLevel) {
            if (levelId >= 15) {
                val hideRate = (0.2 + (levelId / 500.0)).coerceAtMost(0.9)
                boxes.filter { it.blocks.size >= 2 }.shuffled(random).take((totalFullBoxesCount * hideRate).toInt()).forEach { 
                    it.hiddenLayers = if (levelId > 180) 2 else 1
                }
            }
            if (levelId >= 45) {
                val webCount = (levelId / 70).coerceAtLeast(1).coerceAtMost(7)
                boxes.filter { !it.isEmpty() }.shuffled(random).take(webCount).forEach { it.hasCobweb = true }
            }
            if (levelId >= 90) {
                val iceCount = (levelId / 140).coerceAtLeast(1).coerceAtMost(5)
                boxes.filter { !it.isEmpty() && !it.hasCobweb }.shuffled(random).take(iceCount).forEach { it.isFrozen = true }
            }
            return
        }

        // MÀN BOSS
        when (currentBossType) {
            1 -> boxes.forEach { if (it.blocks.size >= 2) it.hiddenLayers = it.blocks.size - 1 }
            2 -> boxes.filter { !it.isEmpty() }.forEach { it.hasCobweb = true }
            3 -> boxes.filter { !it.isEmpty() }.shuffled(random).take(6).forEach { it.isFrozen = true }
            // Boss 4 (Level 80): Luôn hiển thị 1 quả trên cùng để người chơi có thể bắt đầu chơi
            4 -> boxes.forEach { 
                if (it.blocks.size >= 1) {
                    it.hiddenLayers = (it.blocks.size - 1).coerceAtLeast(0)
                }
            }
        }
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

    private fun setupInitialBags() {
        boxSlots.clear()
        if (isBossLevel && currentBossType == 1) {
            val target = colorsUsed.random(random)
            var exactCount = 0
            boxes.forEach { box -> box.blocks.forEach { if (it == target) exactCount++ } }
            boxSlots.add(BoxSlot(0, target, capacity = exactCount, turnsLeft = 25))
        } else {
            val available = boxes.filter { !it.isEmpty() }.map { it.peekColor() }.distinct().filter { it != ColorId.EMPTY }
            if (available.isNotEmpty()) {
                val p = available.shuffled(random)
                boxSlots.add(BoxSlot(0, p[0], capacity = 1, turnsLeft = 25))
                if (p.size > 1) boxSlots.add(BoxSlot(1, p[1], capacity = 1, turnsLeft = 25))
            }
        }
    }

    fun clearCobweb(index: Int): Boolean {
        val box = boxes.getOrNull(index) ?: return false
        if (box.hasCobweb) {
            box.hasCobweb = false
            consumeTurn()
            return true
        }
        return false
    }

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
        if (truck.remaining() <= 0) { isGameOver = true; isWin = true }
        return countMoved
    }

    fun canMove(s: Box, d: Box) = !s.isEmpty() && d.blocks.size < 4 && 
            (d.isEmpty() || d.peekColor() == s.peekColor()) && ((s.blocks.size - 1) >= s.hiddenLayers || (isBossLevel && currentBossType == 4))

    fun executeMove(s: Box, d: Box) {
        val color = s.peekColor()
        val originalHiddenLimit = s.hiddenLayers 
        while (!s.isEmpty() && s.peekColor() == color && ((s.blocks.size - 1) >= originalHiddenLimit || (isBossLevel && currentBossType == 4)) && d.blocks.size < 4) {
            d.blocks.push(s.blocks.pop())
            if (s.hiddenLayers >= s.blocks.size && !s.isEmpty()) {
                s.hiddenLayers = (s.blocks.size - 1).coerceAtMost(originalHiddenLimit).coerceAtLeast(0)
            }
        }
        if (s.isEmpty()) s.hiddenLayers = 0
        if (d.isFrozen) d.isFrozen = false
        consumeTurn()
    }

    fun consumeTurn(): Boolean {
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
        if (isBossLevel && currentBossType == 1) return
        val box = boxes.find { it.id == id } ?: return
        if (box.isArchived) return
        val color = if (box.blocks.isNotEmpty()) box.blocks[0] else ColorId.EMPTY
        val bag = boxSlots.find { it.targetColor == color && it.remaining() > 0 }
        if (bag != null) {
            bag.filled++
            box.isArchived = true
            box.blocks.clear()
            completedBoxesCount++
            boxes.forEach { it.isLockedByChain = false; if (isBossLevel && currentBossType == 3) it.isFrozen = false }
            if (bag.remaining() <= 0) replaceBag(bag.id)
        } else if (!isBagMechanismEnabled) {
            box.isArchived = true; box.blocks.clear(); completedBoxesCount++
        }
        if (completedBoxesCount >= totalFullBoxesCount) { isGameOver = true; isWin = true }
    }

    private fun replaceBag(id: Int) {
        if (isBossLevel && currentBossType == 1) return 
        val idx = boxSlots.indexOfFirst { it.id == id }
        if (idx == -1) return
        val other = if (boxSlots.size > 1) boxSlots[1 - idx].targetColor else null
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
                if (!isBagMechanismEnabled || boxSlots.any { it.targetColor == color && it.remaining() > 0 }) {
                    archiveBox(box.id); archived.add(box.id); changed = true
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
            if ((src.blocks.size - 1) < src.hiddenLayers && !(isBossLevel && currentBossType == 4)) continue
            if (isBossLevel && currentBossType == 1 && src.peekColor() == boxSlots.getOrNull(0)?.targetColor) return false
            for (dst in active) { if (src.id != dst.id && canMove(src, dst)) return false }
        }
        return true
    }

    fun rerollBags() {
        val pool = boxes.filter { !it.isArchived && !it.isEmpty() }.flatMap { it.blocks }.distinct()
        if (pool.isEmpty()) return
        boxSlots.forEachIndexed { i, box ->
            val otherColor = if (boxSlots.size > 1) boxSlots[1 - i].targetColor else null
            val newColor = pool.firstOrNull { it != otherColor && it != box.targetColor } ?: pool.firstOrNull() ?: return@forEachIndexed
            boxSlots[i] = BoxSlot(box.id, newColor, capacity = box.capacity, turnsLeft = 25)
        }
    }

    fun revealHiddenLayers(boxId: Int) { boxes.find { it.id == boxId }?.let { it.hiddenLayers = 0 } }

    fun shuffleAllBoxes() {
        val activeBoxes = boxes.filter { !it.isArchived }
        val boxSizes = activeBoxes.map { it.blocks.size }
        val allBlocks = activeBoxes.flatMap { it.blocks.toList() }.toMutableList().also { it.shuffle(random) }
        var idx = 0
        activeBoxes.forEach { box ->
            val size = boxSizes[activeBoxes.indexOf(box)]
            box.blocks.clear()
            repeat(size) { if (idx < allBlocks.size) box.blocks.push(allBlocks[idx++]) }
        }
    }
}
