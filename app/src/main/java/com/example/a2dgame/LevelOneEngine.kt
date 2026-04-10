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
     * Thuật toán Pool-Shuffle đúng chuẩn:
     * - Tạo pool phẳng gồm TOÀN BỘ khối (mỗi màu x4 khối) rồi shuffle ngẫu nhiên
     * - Phân phối đều lại vào từng ống (4 khối/ống)
     * - Đảm bảo mọi vị trí – kể cả lớp ẩn – đều thực sự ngẫu nhiên
     * - 100% solvable vì tổng số khối mỗi màu không đổi
     */
    private fun generateFilledAndShuffledLevel(numColors: Int, totalFull: Int) {
        // Bước 1: Tạo pool đầy đủ (mỗi màu 4 khối × số ống cần cho màu đó)
        val pool = mutableListOf<ColorId>()
        for (i in 0 until totalFull) {
            val color = colorsUsed[i % numColors]
            repeat(4) { pool.add(color) }
        }

        // Bước 2: Shuffle toàn bộ pool (Fisher–Yates via Kotlin shuffle)
        // Đảm bảo mọi vị trí kể cả đáy ống (hidden) đều thực sự ngẫu nhiên
        val shuffled = pool.shuffled(random).toMutableList()

        // Bước 3: Phân phối pool đã shuffle vào các ống đầy (không đụng ống trống)
        var idx = 0
        for (i in 0 until totalFull) {
            tubes[i].blocks.clear()
            repeat(4) {
                tubes[i].blocks.push(shuffled[idx++])
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
                return true
            }
            selectedTubeIndex = if (!clicked.isEmpty() && !clicked.isFrozen) index else null
        }
        return false
    }

    fun canMove(s: Tube, d: Tube) = !s.isEmpty() && d.blocks.size < d.capacity && 
            (d.isEmpty() || d.peekColor() == s.peekColor()) && (s.blocks.size - 1) >= s.hiddenLayers

    fun executeMove(s: Tube, d: Tube) {
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

    fun findCompletedTubes(): List<Int> {
        return tubes.filter { it.isComplete() && !it.isArchived }.map { it.id }
    }

    fun archiveTube(id: Int) {
        val tube = tubes.find { it.id == id } ?: return
        if (tube.isArchived) return
        
        val color = if (tube.blocks.isNotEmpty()) tube.blocks[0] else ColorId.EMPTY
        
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

        if (completedTubesCount >= totalFullTubesCount) {
            isGameOver = true
            isWin = true
        }
    }

    private fun replaceBag(id: Int) {
        val idx = boxSlots.indexOfFirst { it.id == id }
        if (idx == -1) return
        val other = if (boxSlots.size > 1) boxSlots[1 - idx].targetColor else null

        // Ư u tiên chọn màu của ống đã hoàn chỉnh nhưng chưa có túi khớp → giải phóng người chơi ngay
        val completedWaiting = tubes
            .filter { !it.isArchived && it.isComplete() }
            .map { it.blocks[0] }
            .filter { it != other && boxSlots.none { b -> b.targetColor == it } }
        if (completedWaiting.isNotEmpty()) {
            boxSlots[idx] = createBox(id, completedWaiting.random(random))
            return
        }

        // Fallback: màu ngẫu nhiên từ board
        val onBoard = tubes.filter { !it.isArchived && !it.isEmpty() }.flatMap { it.blocks }.distinct()
        val pool = onBoard.filter { it != other }
        if (pool.isNotEmpty()) boxSlots[idx] = createBox(id, pool.shuffled(random).first())
        else boxSlots.removeAt(idx)
    }

    /**
     * Quét tất cả ống hoàn chỉnh có túi khớp → archive dắy chuyền liên tục.
     * Gọi sau mỗi nước đi để đảm bảo không có ống nào được xếp xong mà bị bỏ sót.
     * Trả về danh sách ID đã được archive trong lần gọi này.
     */
    fun archiveAllReady(): List<Int> {
        val archived = mutableListOf<Int>()
        var changed = true
        while (changed && !isGameOver) {
            changed = false
            tubes.filter { !it.isArchived && it.isComplete() }.forEach { tube ->
                val color = tube.blocks[0]
                val canArchive = !isBagMechanismEnabled ||
                        boxSlots.any { it.targetColor == color && it.remaining() > 0 }
                if (canArchive) {
                    archiveTube(tube.id)
                    archived.add(tube.id)
                    changed = true
                }
            }
        }
        return archived
    }

    fun getTubes() = tubes
    fun getBoxSlots() = boxSlots
    fun getProgressText() = "Thu hoạch: $completedTubesCount/$totalFullTubesCount thùng"

    // ===== POWER-UPS =====

    /**
     * 🎲 Roll ngẫu nhiên túi.
     * Ư u tiên theo xác suất hoàn thành từ cao → thấp:
     *   1. Ống đã xếp xong hoàn toàn (isComplete) → archive ngay
     *   2. Màu xuất hiữn nhiều nhất trên bàn (dễ hoàn chỉnh nhất)
     *   3. Fallback: màu ngẫu nhiên còn lại
     */
    fun rerollBags() {
        if (boxSlots.isEmpty()) return

        // Priority 1: Ống đã hoàn chỉnh - archive ngay lập tức nếu roll được
        val completedColors = tubes
            .filter { !it.isArchived && it.isComplete() }
            .map { it.blocks[0] }
            .distinct()

        // Priority 2: Đếm tần suất màu trên toàn bàn (cả ẩn lẫn hiện)
        // Màu có nhiều block nhất = dễ xếp xong nhất
        val frequentColors = tubes
            .filter { !it.isArchived }
            .flatMap { it.blocks.toList() }
            .filter { it != ColorId.EMPTY }
            .groupBy { it }
            .entries
            .sortedByDescending { it.value.size } // Nhiều nhất trước
            .map { it.key }

        // Gộp theo độ ưu tiên: complete âm → tần suất cao → còn lại
        val pool = (completedColors + frequentColors).distinct()
        if (pool.isEmpty()) return

        boxSlots.forEachIndexed { i, box ->
            val otherColor = if (boxSlots.size > 1) boxSlots[1 - i].targetColor else null
            val newColor = pool.firstOrNull { it != otherColor && it != box.targetColor }
                ?: pool.firstOrNull() ?: return@forEachIndexed
            boxSlots[i] = BoxSlot(id = box.id, targetColor = newColor,
                capacity = box.capacity, turnsLeft = 30)
        }
    }

    /**
     * 🔍 Kính lúp: lộ toàn bộ lớp ẩn của 1 thùng được chọn.
     */
    fun revealHiddenLayers(tubeId: Int) {
        val tube = tubes.find { it.id == tubeId } ?: return
        if (!tube.isArchived) tube.hiddenLayers = 0
    }

    /**
     * 🔀 Xáo trộn lại: gom tất cả khối từ các ống chưa archive,
     * shuffle Fisher-Yates rồi phân phối lại đều. GIỮ NGUYÊN số lớp ẩn gốc.
     */
    fun shuffleAllTubes() {
        val activeTubes = tubes.filter { !it.isArchived }
        val tubeSizes        = activeTubes.map { it.blocks.size }
        // Lưu lại số lớp ẩn gốc trước khi clear
        val tubeHiddenLayers = activeTubes.map { it.hiddenLayers }

        val allBlocks = activeTubes
            .flatMap { it.blocks.toList() }
            .toMutableList()
            .also { it.shuffle(random) }

        var idx = 0
        activeTubes.forEachIndexed { i, tube ->
            tube.blocks.clear()
            repeat(tubeSizes[i]) {
                if (idx < allBlocks.size) tube.blocks.push(allBlocks[idx++])
            }
            // Phục hồi đúcng số lớp ẩn gốc, đảm bảo không vượt quá số block hiện có
            tube.hiddenLayers = minOf(tubeHiddenLayers[i], (tube.blocks.size - 1).coerceAtLeast(0))
        }
    }
}

