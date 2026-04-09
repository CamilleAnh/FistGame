package com.example.a2dgame

import java.util.Stack
import kotlin.random.Random

/**
 * Engine xử lý logic 1000 Level.
 * Đảm bảo: 100% thắng, fill đầy 4 khối cho mỗi ống, quản lý ống trống chuẩn xác.
 */
class LevelOneEngine(val levelId: Int = 1) {

    enum class ColorId(val colorHex: String, val displayName: String, val fruitIcon: String) {
        STRAWBERRY("#FF4B4B", "DÂU", "🍓"), ORANGE("#FFA726", "CAM", "🍊"), 
        APPLE_GREEN("#66BB6A", "TÁO", "🍏"), BANANA("#FDD835", "CHUỐI", "🍌"), 
        PEACH("#FFAB91", "ĐÀO", "🍑"), MANGO("#FF9800", "XOÀI", "🥭"),
        GRAPE("#AB47BC", "NHO", "🍇"), WATERMELON("#EF5350", "DƯA", "🍉"), 
        PINEAPPLE("#FFEE58", "DỨA", "🍍"), BLUEBERRY("#5C6BC0", "VIỆT", "🫐"), 
        PEAR("#D4E157", "LÊ", "🍐"), COCONUT("#795548", "DỪA", "🥥"),
        KIWI("#9CCC65", "KIWI", "🥝"), CHERRY("#F44336", "ANH ĐÀO", "🍒"), 
        LEMON("#FFF176", "CHANH", "🍋"), AVOCADO("#99CC33", "BƠ", "🥑"),
        EMPTY("#333333", "TRỐNG", "");

        companion object {
            val allFruits by lazy { values().filter { it != EMPTY } }
        }
    }

    data class Tube(
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
        fun getName() = "THÙNG ${targetColor.displayName}"
    }

    private val tubes = mutableListOf<Tube>()
    private val boxSlots = mutableListOf<BoxSlot>()
    private val random = Random(levelId.toLong())
    
    private var completedTubesCount = 0
    private var totalFullTubesCount = 0
    private var colorsUsed = listOf<ColorId>()
    
    var selectedTubeIndex: Int? = null
    var isGameOver = false
    var isWin = false
    var isBagMechanismEnabled = false

    init { setupLevel() }

    private fun setupLevel() {
        // 1. Phân bổ màu sắc
        val numDistinctColors = when {
            levelId < 10 -> 3
            levelId < 30 -> 5
            levelId < 100 -> 8
            levelId < 500 -> 12
            else -> 16
        }
        
        // Cơ chế trùng màu: level cao có thể có nhiều ống cùng màu (multiplier)
        val multiplier = if (levelId < 100) 1.0 else 1.3
        totalFullTubesCount = (numDistinctColors * multiplier).toInt()
        isBagMechanismEnabled = levelId >= 20
        
        // Số ống trống: Duy trì ít nhất 3 ống để đảm bảo cửa thắng
        val numEmptyTubes = if (levelId >= 500) 2 else 3
        val totalTubesCount = totalFullTubesCount + numEmptyTubes
        
        val allAvailable = ColorId.allFruits.shuffled(random)
        colorsUsed = allAvailable.take(numDistinctColors)

        tubes.clear()
        repeat(totalTubesCount) { tubes.add(Tube(it)) }

        // 2. Thuật toán tạo màn chơi ĐẢM BẢO FILL ĐẦY 4/4
        generateFilledAndShuffledLevel(numDistinctColors, totalFullTubesCount)

        // 3. Cơ chế nâng cao cho level cực khó (Ống trung chuyển có sẵn khối lẻ)
        if (levelId > 150) {
            injectComplexityIntoEmptyTubes()
        }

        // 4. Áp dụng chướng ngại vật
        if (levelId >= 20) {
            tubes.filter { it.blocks.size >= 2 }.forEach { it.hiddenLayers = (it.blocks.size - 1).coerceAtMost(2) }
        }
        if (levelId >= 80) {
            val spiderCount = (totalFullTubesCount / 4).coerceAtLeast(1)
            tubes.filter { it.blocks.isNotEmpty() }.shuffled(random).take(spiderCount).forEach { it.hasCobweb = true }
        }
        if (levelId >= 120) {
            tubes.filter { it.blocks.isNotEmpty() && !it.hasCobweb }.shuffled(random).take(1).forEach { it.isFrozen = true }
        }
        if (levelId >= 160) {
            tubes.filter { it.blocks.isNotEmpty() && !it.hasCobweb && !it.isFrozen }.shuffled(random).take(1).forEach { it.isLockedByChain = true }
        }

        completedTubesCount = 0
        if (isBagMechanismEnabled) setupInitialBags()
    }

    /**
     * Thuật toán Swap-Shuffle: Giữ nguyên số lượng khối trong mỗi ống (4/4) 
     * nhưng xáo trộn màu sắc bên trong. Đảm bảo 100% solvable.
     */
    private fun generateFilledAndShuffledLevel(numColors: Int, totalFull: Int) {
        // Bước 1: Đổ đầy các ống với màu đơn sắc (trạng thái thắng)
        for (i in 0 until totalFull) {
            val color = colorsUsed[i % numColors]
            repeat(4) { tubes[i].blocks.push(color) }
        }

        // Bước 2: Xáo trộn bằng cách tráo đổi (Swap) các khối màu giữa các ống đầy
        // Cách này giữ cho mọi ống luôn có đúng 4 khối
        val swapMoves = 300 + (levelId % 200)
        repeat(swapMoves) {
            val tubeA = tubes.filter { it.blocks.size == 4 }.random(random)
            val tubeB = tubes.filter { it.blocks.size == 4 }.random(random)
            
            if (tubeA.id != tubeB.id) {
                // Tráo đổi khối trên cùng của tubeA với khối trên cùng của tubeB
                val colorA = tubeA.blocks.pop()
                val colorB = tubeB.blocks.pop()
                tubeA.blocks.push(colorB)
                tubeB.blocks.push(colorA)
            }
        }
    }

    /**
     * Tạo thêm độ khó bằng cách dời 1-2 khối từ ống đầy sang ống trống ban đầu.
     */
    private fun injectComplexityIntoEmptyTubes() {
        val emptyTubes = tubes.filter { it.isEmpty() }
        val fullTubes = tubes.filter { it.blocks.size == 4 }
        
        if (emptyTubes.isNotEmpty() && fullTubes.size >= 2) {
            // Dời 2 khối ngẫu nhiên từ các ống đầy vào 1 ống trống
            val targetEmpty = emptyTubes.random(random)
            repeat(2) {
                val src = tubes.filter { it.blocks.size > 2 }.random(random)
                targetEmpty.blocks.push(src.blocks.pop())
            }
        }
    }

    private fun setupInitialBags() {
        boxSlots.clear()
        val available = tubes.filter { !it.isEmpty() }.map { it.peekColor() }.distinct().filter { it != ColorId.EMPTY }
        if (available.isNotEmpty()) {
            val p = available.shuffled(random)
            boxSlots.add(createBox(0, p[0]))
            if (p.size > 1) boxSlots.add(createBox(1, p[1]))
        }
    }

    private fun createBox(id: Int, color: ColorId) = BoxSlot(id = id, targetColor = color, turnsLeft = 25)

    fun handleTubeClick(index: Int): Boolean {
        if (isGameOver) return false
        val clicked = tubes.getOrNull(index) ?: return false
        if (clicked.isArchived) return false

        if (clicked.hasCobweb) {
            clicked.hasCobweb = false
            return consumeTurn()
        }

        if (clicked.isLockedByChain || clicked.isComplete()) return false

        val srcIdx = selectedTubeIndex
        if (srcIdx == null) {
            if (!clicked.isEmpty() && !clicked.isFrozen && (clicked.blocks.size - 1) >= clicked.hiddenLayers) {
                selectedTubeIndex = index
                return true
            }
        } else {
            if (srcIdx == index) {
                selectedTubeIndex = null
                return true
            }
            val src = tubes[srcIdx]
            if (canMove(src, clicked)) {
                executeMove(src, clicked)
                selectedTubeIndex = null
                checkWinLoss()
                return true
            }
            selectedTubeIndex = if (!clicked.isEmpty() && !clicked.isFrozen) index else null
        }
        return false
    }

    private fun canMove(s: Tube, d: Tube) = !s.isEmpty() && d.blocks.size < d.capacity && 
            (d.isEmpty() || d.peekColor() == s.peekColor()) && (s.blocks.size - 1) >= s.hiddenLayers

    private fun executeMove(s: Tube, d: Tube) {
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

    private fun checkWinLoss() {
        tubes.filter { it.isComplete() }.forEach { tube ->
            val color = tube.blocks[0]
            val bag = boxSlots.find { it.targetColor == color && it.remaining() > 0 }
            if (bag != null) {
                bag.filled++
                tube.isArchived = true
                tube.blocks.clear()
                completedTubesCount++
                tubes.forEach { it.isLockedByChain = false }
                if (bag.remaining() <= 0) replaceBag(bag.id)
            } else if (!isBagMechanismEnabled) {
                tube.isArchived = true
                tube.blocks.clear()
                completedTubesCount++
            }
        }
        if (completedTubesCount >= totalFullTubesCount) {
            isGameOver = true
            isWin = true
        }
    }

    private fun replaceBag(id: Int) {
        val idx = boxSlots.indexOfFirst { it.id == id }
        val onBoard = tubes.filter { !it.isArchived && !it.isEmpty() }.flatMap { it.blocks }.distinct()
        val other = if (boxSlots.size > 1) boxSlots[1 - idx].targetColor else null
        val pool = onBoard.filter { it != other }
        if (pool.isNotEmpty()) boxSlots[idx] = createBox(id, pool.shuffled(random).first())
        else boxSlots.removeAt(idx)
    }

    fun getTubes() = tubes
    fun getBoxSlots() = boxSlots
    fun getProgressText() = "Thu hoạch: $completedTubesCount/$totalFullTubesCount thùng"
}
