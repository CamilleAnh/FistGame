package com.example.a2dgame

import java.util.Stack

/**
 * Engine xử lý logic cho Màn 1: Sắp xếp màu vào ống (Tube Sorting).
 * Tuân thủ LEVEL1_PRINCIPLES.md.
 */
class LevelOneEngine {

    enum class ColorId(val colorHex: String) {
        PURPLE("#9C27B0"),
        RED("#F44336"),
        BLUE("#2196F3"),
        GREEN("#4CAF50"),
        ORANGE("#FF9800"),
        EMPTY("#333333")
    }

    data class Tube(
        val id: Int,
        val capacity: Int = 4,
        val blocks: Stack<ColorId> = Stack()
    ) {
        fun isFull() = blocks.size >= capacity
        fun isEmpty() = blocks.isEmpty()
        fun peekColor() = if (isEmpty()) ColorId.EMPTY else blocks.peek()
        
        fun isComplete(): Boolean {
            if (isEmpty()) return true
            if (blocks.size < capacity) return false
            val firstColor = blocks[0]
            return blocks.all { it == firstColor }
        }
    }

    private val tubes = mutableListOf<Tube>()
    var selectedTubeIndex: Int? = null
    var isGameOver: Boolean = false

    init {
        setupLevel()
    }

    private fun setupLevel() {
        val colorsUsed = listOf(ColorId.PURPLE, ColorId.RED, ColorId.BLUE, ColorId.GREEN)
        
        // Tạo 4 ống có màu xáo trộn và 2 ống rỗng (Tổng 6 ống)
        val allBlocks = mutableListOf<ColorId>()
        colorsUsed.forEach { color ->
            repeat(4) { allBlocks.add(color) }
        }
        allBlocks.shuffle()

        // Khởi tạo 6 ống
        for (i in 0 until 6) {
            tubes.add(Tube(id = i))
        }

        // Chia màu vào 4 ống đầu tiên
        var blockIdx = 0
        for (i in 0 until 4) {
            repeat(4) {
                tubes[i].blocks.push(allBlocks[blockIdx++])
            }
        }
    }

    fun getTubes(): List<Tube> = tubes

    /**
     * Xử lý khi người chơi nhấn vào một ống.
     * @return true nếu có sự thay đổi trạng thái cần render lại.
     */
    fun handleTubeClick(index: Int): Boolean {
        if (isGameOver) return false

        val sourceIdx = selectedTubeIndex
        if (sourceIdx == null) {
            // Bước 1: Chọn ống nguồn
            if (!tubes[index].isEmpty()) {
                selectedTubeIndex = index
                return true
            }
        } else {
            // Bước 2: Chọn ống đích
            if (sourceIdx == index) {
                // Hủy chọn nếu nhấn lại cùng ống
                selectedTubeIndex = null
                return true
            }

            val sourceTube = tubes[sourceIdx]
            val destTube = tubes[index]

            if (canMove(sourceTube, destTube)) {
                moveBlock(sourceTube, destTube)
                selectedTubeIndex = null
                checkWinCondition()
                return true
            } else {
                // Nếu không di chuyển được, chọn ống mới làm nguồn
                if (!destTube.isEmpty()) {
                    selectedTubeIndex = index
                } else {
                    selectedTubeIndex = null
                }
                return true
            }
        }
        return false
    }

    private fun canMove(source: Tube, dest: Tube): Boolean {
        if (source.isEmpty()) return false
        if (dest.isFull()) return false
        
        // Quy tắc: Chỉ có thể chuyển vào ống rỗng hoặc ống có cùng màu ở trên cùng
        return dest.isEmpty() || dest.peekColor() == source.peekColor()
    }

    private fun moveBlock(source: Tube, dest: Tube) {
        val colorToMove = source.peekColor()
        // Di chuyển tất cả các khối cùng màu liên tiếp ở trên cùng nếu đích còn chỗ
        while (!source.isEmpty() && source.peekColor() == colorToMove && !dest.isFull()) {
            dest.blocks.push(source.blocks.pop())
        }
    }

    private fun checkWinCondition() {
        isGameOver = tubes.all { it.isComplete() }
    }
}
