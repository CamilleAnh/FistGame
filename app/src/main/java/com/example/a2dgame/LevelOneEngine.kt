package com.example.a2dgame

import kotlin.random.Random
import java.util.Stack

/**
 * Engine xử lý logic 1000 Level theo LEVEL_PRINCIPLES.md.
 * Cập nhật: Chỉ di chuyển khối màu đã mở khóa, không kéo theo khối đang ẩn.
 */
class LevelOneEngine(val levelId: Int = 1) {

    enum class ColorId(val colorHex: String, val displayName: String) {
        PURPLE("#9C27B0", "TÍM"), RED("#F44336", "ĐỎ"), BLUE("#2196F3", "XANH DƯƠNG"),
        GREEN("#4CAF50", "XANH LÁ"), ORANGE("#FF9800", "CAM"), CYAN("#00BCD4", "CYAN"),
        YELLOW("#FFEB3B", "VÀNG"), PINK("#E91E63", "HỒNG"), 
        BROWN("#795548", "NÂU"), TEAL("#009688", "TEAL"), LIME("#CDDC39", "CHANH"),
        INDIGO("#3F51B5", "CHÀM"), DEEP_ORANGE("#FF5722", "CAM ĐẬM"), AMBER("#FFC107", "HỔ PHÁCH"),
        LIGHT_BLUE("#03A9F4", "XANH NHẠT"), LIGHT_GREEN("#8BC34A", "LÁ NHẠT"),
        EMPTY("#333333", "TRỐNG")
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
        fun isFull() = blocks.size >= capacity
        fun isEmpty() = isArchived || blocks.isEmpty()
        fun peekColor() = if (isEmpty()) ColorId.EMPTY else blocks.peek()
        
        fun isComplete(): Boolean {
            if (isEmpty() || isFrozen || isLockedByChain || hasCobweb) return false
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
        var turnsLeft: Int = 15
    ) {
        fun remaining() = capacity - filled
        fun getName() = "TÚI ${targetColor.displayName}"
    }

    private val tubes = mutableListOf<Tube>()
    private val random = Random(levelId.toLong())
    private val boxSlots = mutableListOf<BoxSlot>()
    private var completedTubesCount = 0
    private var totalFullTubesCount = 0
    private var colorsInLevel = mutableListOf<ColorId>()
    
    var selectedTubeIndex: Int? = null
    var isGameOver: Boolean = false
    var isWin: Boolean = false
    var isBagMechanismEnabled: Boolean = false

    init {
        setupLevel(levelId)
    }

    private fun setupLevel(level: Int) {
        val numDistinctColors = when {
            level < 5 -> 3
            level < 15 -> 4
            level < 30 -> 6
            level < 60 -> 8
            level < 100 -> 10
            level < 200 -> 12
            level < 400 -> 14
            else -> 16
        }

        val multiplier = when {
            level < 50 -> 1.0
            level < 150 -> 1.2
            level < 300 -> 1.5
            else -> 1.8
        }
        totalFullTubesCount = (numDistinctColors * multiplier).toInt()
        
        isBagMechanismEnabled = level >= 20
        val numEmptyTubes = 3
        val totalTubesCount = totalFullTubesCount + numEmptyTubes
        
        val allAvailableColors = ColorId.values().filter { it != ColorId.EMPTY }
        colorsInLevel = allAvailableColors.shuffled(random).take(numDistinctColors).toMutableList()

        tubes.clear()
        for (i in 0 until totalTubesCount) {
            tubes.add(Tube(id = i, capacity = 4))
        }

        generateFilledLevel(numDistinctColors, totalFullTubesCount)

        if (level >= 20) {
            tubes.filter { !it.isEmpty() }.forEach { 
                it.hiddenLayers = (it.blocks.size - 1).coerceAtMost(2).coerceAtLeast(0)
            }
        }
        if (level >= 80) {
            val count = (totalFullTubesCount / 4).coerceAtLeast(1)
            tubes.filter { !it.isEmpty() }.shuffled(random).take(count).forEach { it.hasCobweb = true }
        }
        if (level >= 120) {
            tubes.filter { !it.isEmpty() && !it.hasCobweb }.shuffled(random).take(1).forEach { it.isFrozen = true }
        }

        completedTubesCount = 0
        if (isBagMechanismEnabled) setupInitialBoxes()
    }

    private fun generateFilledLevel(numDistinct: Int, totalFull: Int) {
        val allBlocks = mutableListOf<ColorId>()
        for (i in 0 until totalFull) {
            val color = colorsInLevel[i % numDistinct]
            repeat(4) { allBlocks.add(color) }
        }
        allBlocks.shuffle(random)

        for (i in 0 until totalFull) {
            repeat(4) {
                tubes[i].blocks.push(allBlocks.removeAt(0))
            }
        }
    }

    private fun setupInitialBoxes() {
        boxSlots.clear()
        val colorsOnBoard = tubes.filter { !it.isArchived && !it.isEmpty() }.flatMap { it.blocks }.distinct()
        if (colorsOnBoard.isNotEmpty()) {
            val pool = colorsOnBoard.shuffled(random)
            boxSlots.add(createBox(0, pool[0]))
            if (pool.size > 1) boxSlots.add(createBox(1, pool[1]))
        }
    }

    private fun createBox(id: Int, color: ColorId) = BoxSlot(id = id, targetColor = color, turnsLeft = 15)

    fun handleTubeClick(index: Int): Boolean {
        if (isGameOver) return false
        val clickedTube = tubes.find { it.id == index } ?: return false
        if (clickedTube.isArchived) return false

        if (clickedTube.hasCobweb) {
            clickedTube.hasCobweb = false
            checkLossCondition()
            return true
        }

        if (clickedTube.isLockedByChain || clickedTube.isComplete()) return false

        val sourceIdx = selectedTubeIndex
        if (sourceIdx == null) {
            if (!clickedTube.isEmpty() && !clickedTube.isFrozen) {
                selectedTubeIndex = index
                return true
            }
        } else {
            if (sourceIdx == index) {
                selectedTubeIndex = null
                return true
            }

            val sourceTube = tubes.find { it.id == sourceIdx }!!
            val destTube = clickedTube

            if (canMove(sourceTube, destTube)) {
                if (destTube.isFrozen) {
                    if (sourceTube.peekColor() == destTube.peekColor() || destTube.isEmpty()) {
                        destTube.isFrozen = false
                    } else {
                        destTube.isArchived = true 
                    }
                }

                moveBlocks(sourceTube, destTube)
                selectedTubeIndex = null
                
                processCompletedTubes()
                checkWinCondition()
                
                if (!isGameOver) {
                    checkLossCondition()
                }
                return true
            } else {
                selectedTubeIndex = if (!destTube.isEmpty() && !destTube.isFrozen) index else null
                return true
            }
        }
        return false
    }

    private fun checkLossCondition() {
        if (isBagMechanismEnabled) {
            var anyBagExpired = false
            boxSlots.forEach { box ->
                if (box.turnsLeft > 0) {
                    box.turnsLeft--
                    if (box.turnsLeft <= 0) anyBagExpired = true
                }
            }
            if (anyBagExpired && !isGameOver) {
                isGameOver = true
                isWin = false
            }
        }
    }

    private fun canMove(source: Tube, dest: Tube): Boolean {
        if (source.isEmpty()) return false
        if (dest.isFull()) return false
        return dest.isEmpty() || dest.peekColor() == source.peekColor()
    }

    /**
     * Cập nhật logic: Di chuyển hết các khối cùng màu nhưng dừng lại ở khối ẩn đầu tiên.
     */
    private fun moveBlocks(source: Tube, dest: Tube) {
        val colorToMove = source.peekColor()
        if (colorToMove == ColorId.EMPTY) return

        // Quy tắc: Chỉ di chuyển các khối ĐÃ LỘ DIỆN (có index >= hiddenLayers)
        while (!source.isEmpty() && 
               source.peekColor() == colorToMove && 
               (source.blocks.size - 1) >= source.hiddenLayers && 
               !dest.isFull()) {
            
            dest.blocks.push(source.blocks.pop())
            
            // Ngay khi lớp trên bị lấy đi, nếu lớp ẩn lộ diện -> Reveal nó
            if (source.hiddenLayers >= source.blocks.size && source.blocks.size > 0) {
                source.hiddenLayers = source.blocks.size - 1
            }
        }
        if (source.isEmpty()) source.hiddenLayers = 0
    }

    fun processCompletedTubes() {
        val completedWaiting = tubes.filter { !it.isArchived && it.isComplete() }
        completedWaiting.forEach { tube ->
            val color = tube.blocks[0]
            if (isBagMechanismEnabled) {
                val box = boxSlots.find { it.targetColor == color && it.remaining() > 0 }
                if (box != null) {
                    box.filled += 1
                    tube.isArchived = true
                    tube.blocks.clear()
                    tube.hiddenLayers = 0
                    completedTubesCount++
                    if (box.remaining() <= 0) replaceBoxWithNextNeededColor(box.id)
                }
            } else {
                tube.isArchived = true
                tube.blocks.clear()
                tube.hiddenLayers = 0
                completedTubesCount++
            }
        }
    }

    private fun replaceBoxWithNextNeededColor(boxId: Int) {
        val idx = boxSlots.indexOfFirst { it.id == boxId }
        if (idx < 0) return
        val colorsOnBoard = tubes.filter { !it.isArchived && !it.isEmpty() }.flatMap { it.blocks }.distinct()
        val otherBoxColor = if (boxSlots.size > 1) boxSlots[1 - idx].targetColor else null
        val finalPool = colorsOnBoard.filter { it != otherBoxColor }

        if (finalPool.isNotEmpty()) {
            boxSlots[idx] = createBox(boxId, finalPool.shuffled(random).first())
        } else if (colorsOnBoard.isNotEmpty()) {
            boxSlots[idx] = createBox(boxId, colorsOnBoard.first())
        } else {
            boxSlots.removeAt(idx)
        }
    }

    private fun checkWinCondition() {
        if (completedTubesCount >= totalFullTubesCount) {
            isGameOver = true
            isWin = true
        }
    }

    fun getTubes() = tubes
    fun getBoxSlots() = boxSlots
    fun getProgressText() = "Đã đóng gói: $completedTubesCount/$totalFullTubesCount ống"
}
