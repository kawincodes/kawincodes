package com.krstech.chesshelp.capture

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoardImageLayoutTest {

    @Test
    fun imageIsTheBoardPlusALeftAndTopMargin() {
        assertEquals(768, BoardImageLayout.BOARD_SIZE)
        assertEquals(812, BoardImageLayout.IMAGE_SIZE)
        assertEquals(BoardImageLayout.IMAGE_SIZE, BoardImageLayout.BOARD_ORIGIN + BoardImageLayout.BOARD_SIZE)
        assertEquals(BoardImageLayout.MARGIN, BoardImageLayout.BOARD_ORIGIN)
        assertEquals(BoardImageLayout.BOARD_SIZE, BoardImageLayout.CELL_SIZE * 8, "cells are whole pixels")
    }

    @Test
    fun rowLabelsRunTopToBottomCentredBesideEachRow() {
        val labels = BoardImageLayout.rowLabels()
        assertEquals(listOf("r1", "r2", "r3", "r4", "r5", "r6", "r7", "r8"), labels.map { it.text })
        labels.forEachIndexed { row, label ->
            assertEquals(22f, label.centerX)
            val top = BoardImageLayout.BOARD_ORIGIN + row * BoardImageLayout.CELL_SIZE
            assertEquals(top + BoardImageLayout.CELL_SIZE / 2f, label.centerY)
        }
        assertEquals(44f + 48f, labels.first().centerY)
        assertEquals(812f - 48f, labels.last().centerY)
    }

    @Test
    fun columnLabelsRunLeftToRightCentredAboveEachColumn() {
        val labels = BoardImageLayout.columnLabels()
        assertEquals(listOf("c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8"), labels.map { it.text })
        labels.forEachIndexed { col, label ->
            assertEquals(22f, label.centerY)
            val left = BoardImageLayout.BOARD_ORIGIN + col * BoardImageLayout.CELL_SIZE
            assertEquals(left + BoardImageLayout.CELL_SIZE / 2f, label.centerX)
        }
        assertEquals(44f + 48f, labels.first().centerX)
        assertEquals(812f - 48f, labels.last().centerX)
    }

    @Test
    fun labelsFitInsideTheMargin() {
        // A text line is about 1.2x its size tall and bold digits/letters about 0.6x wide.
        val lineHeight = BoardImageLayout.LABEL_TEXT_SIZE * 1.2f
        val textWidth = BoardImageLayout.LABEL_TEXT_SIZE * 0.6f * 2
        assertTrue(lineHeight <= BoardImageLayout.MARGIN)
        assertTrue(textWidth <= BoardImageLayout.MARGIN)
        assertTrue(lineHeight <= BoardImageLayout.CELL_SIZE)
    }

    @Test
    fun baselineCentresTheTextLine() {
        // Ascent -24 and descent +6: the line spans [baseline - 24, baseline + 6], centre = baseline - 9.
        val baseline = BoardImageLayout.baseline(centerY = 100f, ascent = -24f, descent = 6f)
        assertEquals(109f, baseline)
        assertEquals(100f, (baseline - 24f + baseline + 6f) / 2f)
    }
}
