package com.twinbrother.fruitsort


import kotlin.random.Random

/**
 * Engine logic V3.8.2: Sửa lỗi hiển thị Boss 4 (Level 80) và tối ưu hóa logic di chuyển.
 */
class LevelOneEngine(val levelId: Int = 1) {

    enum class ColorId(val fruitIcon: String) {
        STRAWBERRY("🍓"), ORANGE("🍊"), 
        APPLE_GREEN("🍏"), BANANA("🍌"), 
        PEACH("🍑"), MANGO("🥭"),
        GRAPE("🍇"), WATERMELON("🍉"), 
        PINEAPPLE("🍍"), BLUEBERRY("🫐"), 
        PEAR("🍐"), COCONUT("🥥"),
        KIWI("🥝"), CHERRY("🍒"), 
        LEMON("🍋"), AVOCADO("🥑"),
        TOMATO("🍅"), CORN("🌽"),
        CARROT("🥕"), EGGPLANT("🍆"),
        BROCCOLI("🥦"), POTATO("🥔"),
        CHILI("🌶️"), SWEET_POTATO("🍠"),
        ONION("🧅"), MUSHROOM("🍄"),
        BELL_PEPPER("🫑"), CUCUMBER("🥒"),
        GARLIC("🧄"), PEANUT("🥜"),
        EMPTY("");

        companion object {
            val allFruits by lazy { entries.filter { it != EMPTY } }
        }
    }

    data class Box(
        val id: Int,
        var capacity: Int = 4,
        val blocks: ArrayDeque<ColorId> = ArrayDeque(),
        var isArchived: Boolean = false,
        var isFrozen: Boolean = false,
        var isLockedByChain: Boolean = false,
        var hasCobweb: Boolean = false,
        var hiddenLayers: Int = 0
    ) {
        fun isEmpty() = blocks.isEmpty()
        fun peekColor() = blocks.lastOrNull() ?: ColorId.EMPTY
        fun isComplete() = blocks.size == capacity && blocks.distinct().size == 1
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

    data class MoveRecord(
        val srcId: Int,
        val dstId: Int,
        val color: ColorId,
        val count: Int,
        val srcHiddenBefore: Int,
        val dstWasFrozen: Boolean,
        val bagTurnsSnapshot: List<Int>
    )

    private val boxes = mutableListOf<Box>()
    private val boxSlots = mutableListOf<BoxSlot>()
    private val random = Random(levelId.toLong())
    
    var completedBoxesCount = 0
    var totalFullBoxesCount = 0
    private var colorsUsed = listOf<ColorId>()
    
    var isGameOver = false
    var isWin = false
    var isBagMechanismEnabled = false
    var isBossLevel = false
    var currentBossType = 0 

    val moveHistory = ArrayDeque<MoveRecord>()
    var comboCounter = 0
    var maxCombo = 0
    private var moveCountAtLastArchive = -2
    var totalMoveCount = 0
    var initialTotalTurns = 0  // sum of all bag turnsLeft at start, for star calc

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

        generateFilledAndShuffledLevel(totalFullBoxesCount)
        applyDifficultyScaling()

        completedBoxesCount = 0
        setupInitialBags()
        
        initialTotalTurns = boxSlots.sumOf { it.turnsLeft }
        moveHistory.clear()
        comboCounter = 0
        maxCombo = 0
        moveCountAtLastArchive = -2
        totalMoveCount = 0
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
                if (it.blocks.isNotEmpty()) {
                    it.hiddenLayers = (it.blocks.size - 1).coerceAtLeast(0)
                }
            }
        }
    }

    /**
     * Builds a board by applying the inverse of legal player moves to a solved board.
     *
     * A plain random distribution of fruit can create impossible water-sort states. Here a
     * reverse step removes a contiguous top run from one box and puts it on a different box.
     * Reversing that step is always a legal player move: the moved run is on top and its
     * destination is either empty or already has the same color on top.
     */
    private fun generateFilledAndShuffledLevel(totalFull: Int) {
        boxes.forEach { it.blocks.clear() }

        repeat(totalFull) { index ->
            val color = colorsUsed[index %  colorsUsed.size]
            repeat(4) { boxes[index].blocks.addLast(color) }
        }

        // Increase reverse steps significantly to ensure better mixing.
        // Previously (totalFull * 3).coerceIn(8, 24), now much higher.
        val reverseSteps = (totalFull * 10).coerceIn(30, 80)
        repeat(reverseSteps) { reverseLegalMove() }
        
        // Post-process to ensure no box has 4 identical items (which is already solved)
        // or 3 identical items (which is too easy).
        ensureDiversity()
    }

    private fun reverseLegalMove() {
        repeat(50) { // More attempts to find a valid move
            val destination = boxes.filter { it.blocks.isNotEmpty() }.randomOrNull(random) ?: return
            val color = destination.peekColor()
            val topRun = destination.blocks.toList().asReversed().takeWhile { it == color }.size
            
            // Pick a source that isn't the same box and isn't full
            val source = boxes
                .filter { it.id != destination.id && it.blocks.size < it.capacity && it.peekColor() != color }
                .randomOrNull(random)
                ?: return@repeat

            // Limit maxAmount to 2 to prevent moving whole stacks of 4 together
            val maxAmount = minOf(
                topRun,
                source.capacity - source.blocks.size,
                if (source.isEmpty()) 2 else source.capacity // Limit to 2 if target is empty
            )
            
            if (maxAmount == 0) return@repeat
            
            val amount = if (maxAmount > 1) random.nextInt(1, maxAmount + 1) else 1
            repeat(amount) {
                val block = destination.blocks.removeLastOrNull() ?: return
                source.blocks.addLast(block)
            }
            return
        }
    }

    private fun ensureDiversity() {
        // Break up boxes that have 3 or 4 of the same color
        repeat(15) {
            val badBox = boxes.find { 
                !it.isArchived && it.blocks.size >= 3 && it.blocks.distinct().size == 1 
            } ?: return@repeat
            
            // Try to move 1-2 items from this box to another one to mix it up
            reverseLegalMoveSpecific(badBox)
        }
    }

    private fun reverseLegalMoveSpecific(destination: Box) {
        val color = destination.peekColor()
        val source = boxes
            .filter { it.id != destination.id && it.blocks.size < it.capacity && it.peekColor() != color }
            .randomOrNull(random) ?: return

        // Move 1 or 2 to break the set
        val amount = random.nextInt(1, 3).coerceAtMost(destination.blocks.size)
        repeat(amount) {
            val block = destination.blocks.removeLastOrNull() ?: return@repeat
            source.blocks.addLast(block)
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
        val src = boxes.find { it.id == srcIndex } ?: return 0
        val truck = boxSlots.getOrNull(0) ?: return 0
        if (src.isEmpty() || src.isFrozen || src.isLockedByChain || src.hasCobweb) return 0
        if (src.peekColor() != truck.targetColor) return 0
        
        var countMoved = 0
        val originalHiddenLimit = src.hiddenLayers
        while (!src.isEmpty() && src.peekColor() == truck.targetColor && truck.remaining() > 0 && (src.blocks.size - 1) >= src.hiddenLayers) {
            src.blocks.removeLastOrNull()
            truck.filled++
            countMoved++
            
            // Logic to reveal next block if previous one was visible
            if (src.hiddenLayers >= src.blocks.size && !src.isEmpty()) {
                src.hiddenLayers = (src.blocks.size - 1).coerceAtMost(originalHiddenLimit).coerceAtLeast(0)
            }
        }
        if (src.isEmpty()) src.hiddenLayers = 0
        if (countMoved > 0) consumeTurn()
        if (truck.remaining() <= 0) { isGameOver = true; isWin = true }
        return countMoved
    }

    fun canMove(s: Box, d: Box) = !s.isEmpty() && !d.isArchived && !d.isComplete() && !d.isLockedByChain && d.blocks.size < d.capacity && 
            (d.isEmpty() || d.peekColor() == s.peekColor()) && ((s.blocks.size - 1) >= s.hiddenLayers || (isBossLevel && currentBossType == 4))

    fun executeMove(s: Box, d: Box): Int {
        // Save undo snapshot BEFORE executing
        val snapshot = MoveRecord(
            srcId = s.id,
            dstId = d.id,
            color = s.peekColor(),
            count = 0, // updated below
            srcHiddenBefore = s.hiddenLayers,
            dstWasFrozen = d.isFrozen,
            bagTurnsSnapshot = boxSlots.map { it.turnsLeft }
        )
        
        val color = s.peekColor()
        val originalHiddenLimit = s.hiddenLayers
        var countMoved = 0
        while (!s.isEmpty() && s.peekColor() == color && ((s.blocks.size - 1) >= originalHiddenLimit || (isBossLevel && currentBossType == 4)) && d.blocks.size < d.capacity) {
            val block = s.blocks.removeLastOrNull() ?: break
            d.blocks.addLast(block)
            countMoved++
            if (s.hiddenLayers >= s.blocks.size && !s.isEmpty()) {
                s.hiddenLayers = (s.blocks.size - 1).coerceAtMost(originalHiddenLimit).coerceAtLeast(0)
            }
        }
        if (s.isEmpty()) s.hiddenLayers = 0
        if (d.isFrozen) d.isFrozen = false
        consumeTurn()
        
        // Save to history with actual count
        if (countMoved > 0) {
            moveHistory.addLast(snapshot.copy(count = countMoved))
            if (moveHistory.size > 5) moveHistory.removeFirst() // limit undo depth to 5
            totalMoveCount++
        }
        
        return countMoved
    }

    fun undoLastMove(): Boolean {
        if (moveHistory.isEmpty() || isGameOver) return false
        val record = moveHistory.removeLast()
        val src = boxes.find { it.id == record.srcId } ?: return false
        val dst = boxes.find { it.id == record.dstId } ?: return false
        
        // Reverse the block transfer: move blocks back from dst to src
        repeat(record.count) {
            val block = dst.blocks.removeLastOrNull() ?: return@repeat
            src.blocks.addLast(block)
        }
        
        // Restore source hidden layers
        src.hiddenLayers = record.srcHiddenBefore
        // Restore destination frozen state
        dst.isFrozen = record.dstWasFrozen
        
        // Restore bag turns
        record.bagTurnsSnapshot.forEachIndexed { i, turns ->
            boxSlots.getOrNull(i)?.turnsLeft = turns
        }
        
        totalMoveCount--
        return true
    }

    fun consumeTurn() {
        if (!isBagMechanismEnabled) return
        boxSlots.forEach { box ->
            if (box.turnsLeft > 0) {
                box.turnsLeft--
                if (box.turnsLeft <= 0 && !isGameOver) { isGameOver = true; isWin = false }
            }
        }
    }

    fun archiveBox(id: Int) {
        if (isBossLevel && currentBossType == 1) return
        val box = boxes.find { it.id == id } ?: return
        if (box.isArchived) return
        val color = box.blocks.firstOrNull() ?: ColorId.EMPTY
        val bag = boxSlots.find { it.targetColor == color && it.remaining() > 0 }
        if (bag != null) {
            bag.filled++
            box.isArchived = true
            box.blocks.clear()
            completedBoxesCount++
            if (totalMoveCount - moveCountAtLastArchive <= 1) {
                comboCounter++
            } else {
                comboCounter = 1
            }
            moveCountAtLastArchive = totalMoveCount
            if (comboCounter > maxCombo) maxCombo = comboCounter
            boxes.forEach { it.isLockedByChain = false; if (isBossLevel && currentBossType == 3) it.isFrozen = false }
            if (bag.remaining() <= 0) replaceBag(bag.id)
        } else if (!isBagMechanismEnabled) {
            box.isArchived = true; box.blocks.clear(); completedBoxesCount++
            if (totalMoveCount - moveCountAtLastArchive <= 1) {
                comboCounter++
            } else {
                comboCounter = 1
            }
            moveCountAtLastArchive = totalMoveCount
            if (comboCounter > maxCombo) maxCombo = comboCounter
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
        if (isBossLevel && currentBossType == 1) return emptyList()
        
        val archived = mutableListOf<Int>()
        var changed = true
        while (changed && !isGameOver) {
            changed = false
            boxes.filter { !it.isArchived && it.isComplete() }.forEach { box ->
                val color = box.blocks.firstOrNull()
                if (!isBagMechanismEnabled || boxSlots.any { it.targetColor == color && it.remaining() > 0 }) {
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
            repeat(size) { if (idx < allBlocks.size) box.blocks.addLast(allBlocks[idx++]) }
        }
    }

    fun calculateStars(): Int {
        if (!isBagMechanismEnabled) return 3 // early levels without bags always get 3 stars
        if (initialTotalTurns <= 0) return 3
        val remainingTurns = boxSlots.sumOf { it.turnsLeft.coerceAtLeast(0) }
        val remainingPercent = remainingTurns.toFloat() / initialTotalTurns
        return when {
            remainingPercent >= 0.40f -> 3
            remainingPercent >= 0.15f -> 2
            else -> 1
        }
    }

    fun findBestHint(): Pair<Int, Int>? {
        if (isGameOver) return null
        val active = boxes.filter { !it.isArchived }
        var bestScore = -1
        var bestPair: Pair<Int, Int>? = null
        
        for (src in active) {
            if (src.isEmpty() || src.isFrozen || src.isLockedByChain || src.hasCobweb) continue
            if ((src.blocks.size - 1) < src.hiddenLayers && !(isBossLevel && currentBossType == 4)) continue
            
            for (dst in active) {
                if (src.id == dst.id) continue
                if (!canMove(src, dst)) continue
                
                // Score this move
                var score = 0
                val color = src.peekColor()
                val srcTopRun = src.blocks.toList().asReversed().takeWhile { it == color }.size
                val movable = minOf(srcTopRun, dst.capacity - dst.blocks.size)
                
                // Highest priority: completing a box (4/4 same color)
                if (dst.blocks.size + movable == dst.capacity && (dst.isEmpty() || dst.peekColor() == color) && dst.blocks.all { it == color || dst.isEmpty() }) {
                    val wouldComplete = dst.blocks.count { it == color } + movable == dst.capacity
                    if (wouldComplete) score += 1000
                }
                
                // High priority: adding to matching non-empty stack
                if (!dst.isEmpty() && dst.peekColor() == color) score += 500
                
                // Medium: emptying source box
                if (src.blocks.size == movable) score += 300
                
                // Low: moving to empty box (sometimes wasteful)
                if (dst.isEmpty()) score += 100
                
                // Bonus: moving larger runs
                score += movable * 10
                
                if (score > bestScore) {
                    bestScore = score
                    bestPair = Pair(src.id, dst.id)
                }
            }
        }
        return bestPair
    }
}
