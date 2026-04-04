package com.example.a2dgame

import kotlin.random.Random
import java.util.Stack

/**
 * Engine xử lý logic cho các màn chơi (Tube Sorting).
 * Cơ chế đóng gói: Chỉ thu hồi khi có túi đúng màu.
 */
class LevelOneEngine(val levelId: Int = 1) {

    enum class ColorId(val colorHex: String, val displayName: String) {
        PURPLE("#9C27B0", "TÍM"),
        RED("#F44336", "ĐỎ"),
        BLUE("#2196F3", "XANH DƯƠNG"),
        GREEN("#4CAF50", "XANH LÁ"),
        ORANGE("#FF9800", "CAM"),
        CYAN("#00BCD4", "CYAN"),
        YELLOW("#FFEB3B", "VÀNG"),
        PINK("#E91E63", "HỒNG"),
        EMPTY("#333333", "TRỐNG")
    }

    data class Tube(
        val id: Int,
        val capacity: Int = 4,
        val blocks: Stack<ColorId> = Stack(),
        var isArchived: Boolean = false
    ) {
        fun isFull() = blocks.size >= capacity
        fun isEmpty() = isArchived || blocks.isEmpty()
        fun peekColor() = if (isEmpty()) ColorId.EMPTY else blocks.peek()
        
        fun isComplete(): Boolean {
            if (isEmpty()) return false
            if (blocks.size < capacity) return false
            val firstColor = blocks[0]
            return blocks.all { it == firstColor }
        }
    }

    data class BoxSlot(
        val id: Int,
        var targetColor: ColorId,
        var capacity: Int,
        var filled: Int = 0
    ) {
        fun remaining() = capacity - filled
        fun getName() = "TÚI ${targetColor.displayName}"
    }

    private val tubes = mutableListOf<Tube>()
    private val random = Random(System.currentTimeMillis())
    private val boxSlots = mutableListOf<BoxSlot>()
    private var completedTubesCount = 0
    private var totalColorsInLevel = 0
    private var colorsUsed = listOf<ColorId>()
    
    var selectedTubeIndex: Int? = null
    var isGameOver: Boolean = false
    var isBagMechanismEnabled: Boolean = false

    init {
        setupLevel(levelId)
    }

    private fun setupLevel(level: Int) {
        totalColorsInLevel = when {
            level <= 3 -> 3
            level <= 6 -> 4
            level <= 9 -> 5
            else -> 6
        }
        
        isBagMechanismEnabled = level >= 10
        
        val numEmptyTubes = 2
        val totalTubesCount = totalColorsInLevel + numEmptyTubes
        
        val allAvailableColors = ColorId.values().filter { it != ColorId.EMPTY }
        colorsUsed = allAvailableColors.shuffled(random).take(totalColorsInLevel)
        
        val allBlocks = mutableListOf<ColorId>()
        colorsUsed.forEach { color ->
            repeat(4) { allBlocks.add(color) }
        }
        allBlocks.shuffle(random)

        tubes.clear()
        for (i in 0 until totalTubesCount) {
            tubes.add(Tube(id = i))
        }

        var blockIdx = 0
        for (i in 0 until totalColorsInLevel) {
            repeat(4) {
                tubes[i].blocks.push(allBlocks[blockIdx++])
            }
        }

        completedTubesCount = 0
        
        if (isBagMechanismEnabled) {
            setupInitialBoxes()
        }
    }

    private fun setupInitialBoxes() {
        boxSlots.clear()
        val pool = colorsUsed.shuffled(random)
        // Lấy 2 màu đầu tiên để làm túi ban đầu
        boxSlots.add(createBox(0, pool[0]))
        boxSlots.add(createBox(1, pool[1]))
    }

    private fun createBox(id: Int, color: ColorId): BoxSlot {
        return BoxSlot(id = id, targetColor = color, capacity = 1, filled = 0)
    }

    fun getTubes(): List<Tube> = tubes
    fun getBoxSlots(): List<BoxSlot> = boxSlots
    fun getProgressText(): String {
        return if (isBagMechanismEnabled) {
            "Đã đóng gói: $completedTubesCount/$totalColorsInLevel ống"
        } else {
            "Hoàn thành: $completedTubesCount/$totalColorsInLevel màu"
        }
    }

    fun handleTubeClick(index: Int): Boolean {
        if (isGameOver) return false
        val clickedTube = tubes.find { it.id == index } ?: return false
        if (clickedTube.isArchived || clickedTube.isComplete()) return false

        val sourceIdx = selectedTubeIndex
        if (sourceIdx == null) {
            if (!clickedTube.isEmpty()) {
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
                moveBlocks(sourceTube, destTube)
                selectedTubeIndex = null
                processCompletedTubes()
                checkWinCondition()
                return true
            } else {
                selectedTubeIndex = if (!destTube.isEmpty()) index else null
                return true
            }
        }
        return false
    }

    private fun canMove(source: Tube, dest: Tube): Boolean {
        if (source.isEmpty()) return false
        if (dest.isFull()) return false
        return dest.isEmpty() || dest.peekColor() == source.peekColor()
    }

    private fun moveBlocks(source: Tube, dest: Tube) {
        val colorToMove = source.peekColor()
        while (!source.isEmpty() && source.peekColor() == colorToMove && !dest.isFull()) {
            dest.blocks.push(source.blocks.pop())
        }
    }

    fun processCompletedTubes() {
        val completedWaiting = tubes.filter { !it.isArchived && it.isComplete() }
        if (completedWaiting.isEmpty()) return

        var somethingPacked = false
        completedWaiting.forEach { tube ->
            val color = tube.blocks[0]
            if (isBagMechanismEnabled) {
                val box = boxSlots.find { it.targetColor == color && it.remaining() > 0 }
                if (box != null) {
                    box.filled += 1
                    tube.isArchived = true
                    tube.blocks.clear()
                    completedTubesCount++
                    somethingPacked = true
                    if (box.remaining() <= 0) replaceBoxWithNextNeededColor(box.id)
                }
            } else {
                tube.isArchived = true
                tube.blocks.clear()
                completedTubesCount++
                somethingPacked = true
            }
        }
        if (somethingPacked) processCompletedTubes()
    }

    /**
     * CẬP NHẬT LOGIC: Chỉ giữ lại 1 túi nếu không còn màu khác biệt để yêu cầu.
     */
    private fun replaceBoxWithNextNeededColor(boxId: Int) {
        val idx = boxSlots.indexOfFirst { it.id == boxId }
        if (idx < 0) return
        
        // 1. Lấy tất cả màu của các ống CHƯA bị archived (vẫn còn trên bàn chơi)
        val colorsOnBoard = tubes.filter { !it.isArchived && !it.isEmpty() }
            .flatMap { it.blocks }
            .distinct()

        // 2. Lấy danh sách các màu của túi khác đang yêu cầu để tránh trùng lặp
        val otherBoxColor = if (boxSlots.size > 1) {
            boxSlots.getOrNull(1 - idx)?.targetColor
        } else {
            null
        }

        // 3. Ưu tiên màu của các ống đã hoàn thành nhưng đang "chờ" túi
        val waitingColors = tubes.filter { !it.isArchived && it.isComplete() }
            .map { it.blocks[0] }
            .filter { it != otherBoxColor }

        val finalPool = if (waitingColors.isNotEmpty()) {
            waitingColors
        } else {
            // Nếu không có ống chờ, lấy bất kỳ màu nào còn tồn tại trên bàn (không trùng túi kia)
            colorsOnBoard.filter { it != otherBoxColor }
        }

        if (finalPool.isNotEmpty()) {
            val newColor = finalPool.shuffled(random).first()
            boxSlots[idx] = createBox(boxId, newColor)
        } else {
            // KHÔNG CÒN MÀU NÀO KHÁC BIỆT: Xóa túi này đi để chỉ còn 1 túi duy nhất
            boxSlots.removeAt(idx)
        }
    }

    private fun checkWinCondition() {
        isGameOver = completedTubesCount >= totalColorsInLevel
    }
}
