package com.twinbrother.fruitsort

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelOneEngineTest {

    @Test
    fun generatedLevels_preserveEveryFruitAndHaveWorkingSpace() {
        listOf(1, 15, 20, 80, 100, 500, 1000).forEach { levelId ->
            val engine = LevelOneEngine(levelId)
            val boxes = engine.getBoxes()
            val fruitCount = boxes.sumOf { it.blocks.size }

            assertEquals(engine.totalFullBoxesCount * 4, fruitCount)
            assertTrue("Level $levelId needs an empty slot", boxes.any { it.blocks.size < it.capacity })
            assertFalse("Level $levelId must not start completed", engine.isWin)
        }
    }

    @Test
    fun generatedEarlyLevels_offerAtLeastOneLegalMove() {
        (1..14).forEach { levelId ->
            val engine = LevelOneEngine(levelId)
            val boxes = engine.getBoxes()
            val hasMove = boxes.any { source ->
                boxes.any { destination -> source.id != destination.id && engine.canMove(source, destination) }
            }

            assertTrue("Level $levelId should have a legal opening move", hasMove)
        }
    }

    @Test
    fun executeMove_transfersTheWholeVisibleTopRun() {
        val engine = LevelOneEngine(1)
        val source = engine.getBoxes()[0]
        val destination = engine.getBoxes()[1]
        source.blocks.clear()
        destination.blocks.clear()
        source.blocks.addAll(listOf(LevelOneEngine.ColorId.ORANGE, LevelOneEngine.ColorId.STRAWBERRY, LevelOneEngine.ColorId.STRAWBERRY))
        destination.blocks.push(LevelOneEngine.ColorId.STRAWBERRY)

        assertTrue(engine.canMove(source, destination))
        engine.executeMove(source, destination)

        assertEquals(listOf(LevelOneEngine.ColorId.ORANGE), source.blocks.toList())
        assertEquals(3, destination.blocks.size)
        assertTrue(destination.blocks.all { it == LevelOneEngine.ColorId.STRAWBERRY })
    }

    @Test
    fun bagTimer_endsTheGameWhenAnyBagRunsOutOfTurns() {
        val engine = LevelOneEngine(20)
        assertTrue(engine.isBagMechanismEnabled)

        repeat(25) { engine.consumeTurn() }

        assertTrue(engine.isGameOver)
        assertFalse(engine.isWin)
    }

    @Test
    fun clearingCobweb_consumesOneTurn() {
        val engine = LevelOneEngine(20)
        val box = engine.getBoxes().first()
        val turnsBefore = engine.getBoxSlots().map { it.turnsLeft }
        box.hasCobweb = true

        assertTrue(engine.clearCobweb(box.id))
        assertFalse(box.hasCobweb)
        assertEquals(turnsBefore.map { it - 1 }, engine.getBoxSlots().map { it.turnsLeft })
    }
}
