package com.krstech.chesshelp.vision

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BoardRectTest {

    @Test
    fun `cell bounds tile the board without gaps`() {
        for (size in intArrayOf(378, 656, 720, 987, 1002, 1080, 1344)) {
            val rect = BoardRect(39, 540, size, 0.9f)
            assertEquals(size / 8f, rect.cell)
            assertEquals(39 + size, rect.right)
            assertEquals(540 + size, rect.bottom)
            assertEquals(rect.left, rect.cellLeft(0))
            assertEquals(rect.top, rect.cellTop(0))
            assertEquals(rect.right, rect.cellRight(7))
            assertEquals(rect.bottom, rect.cellBottom(7))
            for (i in 0..7) {
                assertEquals(rect.cellLeft(i + 1), rect.cellRight(i))
                assertEquals(rect.cellTop(i + 1), rect.cellBottom(i))
                val width = rect.cellRight(i) - rect.cellLeft(i)
                assertTrue(width == size / 8 || width == size / 8 + 1, "size $size col $i width $width")
                val centre = rect.cellCenterX(i)
                assertTrue(centre > rect.cellLeft(i) && centre < rect.cellRight(i))
                assertEquals(i, rect.colAt(centre.toInt()))
                assertEquals(i, rect.rowAt(rect.cellCenterY(i).toInt()))
            }
        }
    }

    @Test
    fun `point lookups`() {
        val rect = BoardRect(100, 200, 800, 1f)
        assertTrue(rect.contains(100, 200))
        assertTrue(rect.contains(899, 999))
        assertFalse(rect.contains(900, 500))
        assertFalse(rect.contains(500, 199))
        assertEquals(0, rect.colAt(100))
        assertEquals(7, rect.colAt(899))
        assertEquals(-1, rect.colAt(99))
        assertEquals(-1, rect.colAt(900))
        assertEquals(3, rect.rowAt(200 + 350))
        assertEquals(-1, rect.rowAt(1000))
    }

    @Test
    fun `fitsIn checks the whole rectangle`() {
        assertTrue(BoardRect(0, 0, 1080, 1f).fitsIn(1080, 2400))
        assertTrue(BoardRect(0, 1320, 1080, 1f).fitsIn(1080, 2400))
        assertFalse(BoardRect(1, 0, 1080, 1f).fitsIn(1080, 2400))
        assertFalse(BoardRect(0, 1321, 1080, 1f).fitsIn(1080, 2400))
        assertFalse(BoardRect(-1, 0, 500, 1f).fitsIn(1080, 2400))
        assertFalse(BoardRect(0, 0, 4, 1f).fitsIn(1080, 2400))
    }
}
