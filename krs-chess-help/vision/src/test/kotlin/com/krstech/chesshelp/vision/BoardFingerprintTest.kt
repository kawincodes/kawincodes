package com.krstech.chesshelp.vision

import java.awt.Color
import java.util.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoardFingerprintTest {

    private val scene = SyntheticScenes.plain[1]

    private fun move(cells: String, from: Int, to: Int): String {
        val chars = cells.toCharArray()
        require(chars[from] != '.') { "no piece on $from" }
        chars[to] = chars[from]
        chars[from] = '.'
        return String(chars)
    }

    @Test
    fun `identical frames have distance zero`() {
        val frame = scene.render()
        val a = BoardFingerprint.of(frame, scene.rect)
        val b = BoardFingerprint.of(scene.render(), scene.rect)
        assertEquals(BoardFingerprint.GRID * BoardFingerprint.GRID, a.size)
        assertTrue(a.all { it in 0..255 })
        assertEquals(0f, BoardFingerprint.distance(a, a))
        assertEquals(0f, BoardFingerprint.distance(a, b))
    }

    @Test
    fun `one moved piece is a change in every theme`() {
        for (theme in Themes.ALL) {
            val before = Scene(1080, 2400, 39, 540, 1002, theme, Positions.MIDDLE, name = theme.name)
            // a quiet move, a capture, and a move onto a same-coloured square
            for ((from, to) in listOf(35 to 27, 42 to 25, 53 to 37, 8 to 16, 61 to 47)) {
                val after = before.copy(cells = move(before.cells, from, to))
                val d = BoardFingerprint.distance(
                    BoardFingerprint.of(before.render(), before.rect),
                    BoardFingerprint.of(after.render(), after.rect),
                )
                assertTrue(d > 2 * BoardFingerprint.CHANGE_THRESHOLD, "${theme.name} $from->$to: distance $d")
                assertTrue(d <= 1f)
            }
        }
    }

    @Test
    fun `the faintest realistic move still registers`() {
        // a single light pawn hopping between light squares of the palest theme, on a small board
        val a = Scene(720, 1560, 171, 300, 378, Themes.PALE, Positions.EMPTY.replaceRange(36, 37, "L"), clutter = false)
        val b = a.copy(cells = Positions.EMPTY.replaceRange(27, 28, "L"))
        val d = BoardFingerprint.distance(BoardFingerprint.of(a.render(), a.rect), BoardFingerprint.of(b.render(), b.rect))
        assertTrue(d > BoardFingerprint.CHANGE_THRESHOLD, "distance $d")
    }

    @Test
    fun `a piece caught in mid-animation is a change`() {
        val before = scene.render()
        val image = scene.renderImage()
        // slide the piece on view cell 35 half a square to the right
        val cell = scene.size / 8
        val x = scene.cellX(3)
        val y = scene.cellY(4)
        val piece = image.getSubimage(x, y, cell, cell)
        val copy = java.awt.image.BufferedImage(cell, cell, java.awt.image.BufferedImage.TYPE_INT_RGB)
        copy.graphics.drawImage(piece, 0, 0, null)
        image.graphics.drawImage(copy, x + cell / 2, y, null)
        val d = BoardFingerprint.distance(BoardFingerprint.of(before, scene.rect), BoardFingerprint.of(image.toFrame(), scene.rect))
        assertTrue(d > BoardFingerprint.CHANGE_THRESHOLD, "distance $d")
    }

    @Test
    fun `changes outside the board are ignored`() {
        val a = BoardFingerprint.of(scene.render(), scene.rect)
        // a completely different UI around the same board
        val otherUi = scene.copy(clutterSeed = 999)
        assertEquals(0f, BoardFingerprint.distance(a, BoardFingerprint.of(otherUi.render(), otherUi.rect)))
        // random overlay rectangles and noise everywhere except on the squares
        val image = scene.renderImage()
        val g = image.createGraphics()
        val random = Random(5)
        g.clip = java.awt.geom.Area(java.awt.Rectangle(0, 0, scene.width, scene.height)).also {
            it.subtract(java.awt.geom.Area(java.awt.Rectangle(scene.left, scene.top, scene.size, scene.size)))
        }
        repeat(200) {
            g.color = Color(random.nextInt(256), random.nextInt(256), random.nextInt(256))
            g.fillRect(random.nextInt(scene.width), random.nextInt(scene.height), 10 + random.nextInt(200), 10 + random.nextInt(120))
        }
        g.dispose()
        assertEquals(0f, BoardFingerprint.distance(a, BoardFingerprint.of(image.toFrame(), scene.rect)))
    }

    @Test
    fun `faint pixel noise on the board stays below the threshold`() {
        val a = BoardFingerprint.of(scene.render(), scene.rect)
        val image = scene.renderImage()
        Scene.addNoise(image, 0, 0, scene.width, scene.height, 2, Random(3))
        val d = BoardFingerprint.distance(a, BoardFingerprint.of(image.toFrame(), scene.rect))
        assertTrue(d < BoardFingerprint.CHANGE_THRESHOLD / 2, "distance $d")
    }

    @Test
    fun `distance is symmetric, bounded, and maximal for incomparable input`() {
        val a = BoardFingerprint.of(scene.render(), scene.rect)
        val moved = scene.copy(cells = move(scene.cells, 35, 27))
        val b = BoardFingerprint.of(moved.render(), moved.rect)
        assertEquals(BoardFingerprint.distance(a, b), BoardFingerprint.distance(b, a))
        val black = BoardFingerprint.of(Frame(400, 400, IntArray(160_000) { 0xFF000000.toInt() }), BoardRect(0, 0, 400, 1f))
        val white = BoardFingerprint.of(Frame(400, 400, IntArray(160_000) { -1 }), BoardRect(0, 0, 400, 1f))
        assertEquals(1f, BoardFingerprint.distance(black, white))
        assertEquals(1f, BoardFingerprint.distance(a, IntArray(3)))
        assertEquals(1f, BoardFingerprint.distance(IntArray(0), IntArray(0)))
        assertTrue(BoardFingerprint.CHANGE_THRESHOLD > 0f && BoardFingerprint.CHANGE_THRESHOLD < 0.1f)
    }

    @Test
    fun `rectangles reaching outside the frame do not crash`() {
        val frame = scene.render()
        BoardFingerprint.of(frame, BoardRect(-200, -200, 900, 1f))
        BoardFingerprint.of(frame, BoardRect(600, 2000, 900, 1f))
        BoardFingerprint.of(frame, BoardRect(0, 0, 4, 1f))
    }

    @Test
    fun `fingerprinting takes well under five milliseconds`() {
        val frame = scene.render()
        repeat(200) { BoardFingerprint.of(frame, scene.rect) }
        var best = Long.MAX_VALUE
        repeat(50) {
            val t = System.nanoTime()
            val a = BoardFingerprint.of(frame, scene.rect)
            BoardFingerprint.distance(a, a)
            best = minOf(best, System.nanoTime() - t)
        }
        assertTrue(best < 5_000_000L, "fingerprint took ${best / 1000} us")
    }
}
