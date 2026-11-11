package com.krstech.chesshelp.vision

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class FrameOpsTest {

    private fun gradientFrame(w: Int, h: Int) = Frame(w, h, IntArray(w * h) {
        val x = it % w
        val y = it / w
        (0xFF shl 24) or ((x * 255 / maxOf(1, w - 1)) shl 16) or ((y * 255 / maxOf(1, h - 1)) shl 8) or ((x + y) and 0xFF)
    })

    private fun channels(p: Int) = intArrayOf((p ushr 24) and 0xFF, (p ushr 16) and 0xFF, (p ushr 8) and 0xFF, p and 0xFF)

    @Test
    fun `frame validates its dimensions`() {
        assertFailsWith<IllegalArgumentException> { Frame(4, 4, IntArray(15)) }
        assertFailsWith<IllegalArgumentException> { Frame(0, 4, IntArray(0)) }
        assertFailsWith<IllegalArgumentException> { Frame(4, -1, IntArray(0)) }
        val f = Frame(3, 2, intArrayOf(1, 2, 3, 4, 5, 6))
        assertEquals(6, f.pixel(2, 1))
        assertEquals(2, f.pixel(1, 0))
    }

    @Test
    fun `crop copies exactly the requested region`() {
        val frame = gradientFrame(200, 120)
        val crop = FrameOps.crop(frame, 30, 20, 50, 40)
        assertEquals(50, crop.width)
        assertEquals(40, crop.height)
        assertEquals(2000, crop.pixels.size)
        for (y in 0 until 40) for (x in 0 until 50) assertEquals(frame.pixel(30 + x, 20 + y), crop.pixel(x, y))
    }

    @Test
    fun `crop clips to the frame and rejects regions outside it`() {
        val frame = gradientFrame(200, 120)
        val clipped = FrameOps.crop(frame, 180, 100, 100, 100)
        assertEquals(20, clipped.width)
        assertEquals(20, clipped.height)
        assertEquals(frame.pixel(199, 119), clipped.pixel(19, 19))
        val negative = FrameOps.crop(frame, -10, -10, 30, 30)
        assertEquals(20, negative.width)
        assertEquals(frame.pixel(0, 0), negative.pixel(0, 0))
        assertFailsWith<IllegalArgumentException> { FrameOps.crop(frame, 200, 0, 10, 10) }
        assertFailsWith<IllegalArgumentException> { FrameOps.crop(frame, 0, 0, 0, 10) }
        assertFailsWith<IllegalArgumentException> { FrameOps.crop(frame, -50, -50, 20, 20) }
    }

    @Test
    fun `downscale keeps the aspect ratio and respects the limit`() {
        val portrait = FrameOps.downscale(gradientFrame(1080, 2400), 768)
        assertEquals(768, portrait.height)
        assertEquals(346, portrait.width)
        val landscape = FrameOps.downscale(gradientFrame(2000, 1200), 1000)
        assertEquals(1000, landscape.width)
        assertEquals(600, landscape.height)
        val odd = FrameOps.downscale(gradientFrame(1081, 2402), 512)
        assertEquals(512, odd.height)
        assertEquals(230, odd.width)
        val sliver = FrameOps.downscale(gradientFrame(3000, 2), 100)
        assertEquals(100, sliver.width)
        assertEquals(1, sliver.height)
        assertFailsWith<IllegalArgumentException> { FrameOps.downscale(gradientFrame(10, 10), 0) }
    }

    @Test
    fun `downscale never upscales`() {
        val frame = gradientFrame(300, 200)
        assertSame(frame, FrameOps.downscale(frame, 300))
        assertSame(frame, FrameOps.downscale(frame, 4000))
    }

    @Test
    fun `downscale averages areas instead of picking pixels`() {
        // one-pixel black/white checker: any point sampling would give pure black or pure white
        val checker = Frame(400, 400, IntArray(160_000) { if ((it % 400 + it / 400) % 2 == 0) -1 else 0xFF000000.toInt() })
        for (target in intArrayOf(200, 100, 80, 37)) {
            val small = FrameOps.downscale(checker, target)
            assertEquals(target, small.width)
            for (p in small.pixels) {
                val c = channels(p)
                assertEquals(255, c[0])
                // odd ratios leave at most a one-pixel surplus of one colour in each output cell
                for (k in 1..3) assertTrue(abs(c[k] - 127.5) <= 16, "target $target: channel ${c[k]}")
            }
        }
        // a uniform image stays exactly uniform at any ratio
        val flat = Frame(333, 517, IntArray(333 * 517) { 0xFF123456.toInt() })
        for (p in FrameOps.downscale(flat, 100).pixels) assertEquals(0xFF123456.toInt(), p)
    }

    @Test
    fun `downscale preserves the mean colour`() {
        val frame = gradientFrame(640, 360)
        val small = FrameOps.downscale(frame, 97)
        fun mean(f: Frame, shift: Int) = f.pixels.sumOf { ((it ushr shift) and 0xFF).toLong() }.toDouble() / f.pixels.size
        for (shift in intArrayOf(16, 8)) assertTrue(abs(mean(frame, shift) - mean(small, shift)) < 1.5)
    }

    @Test
    fun `cropBoard returns a square of the requested size showing the squares area`() {
        val scene = Scene(1080, 2400, 39, 540, 1002, Themes.GREEN, Positions.EMPTY, coords = false)
        val frame = scene.render()
        for (size in intArrayOf(8, 64, 512, 1002, 1200)) {
            val board = FrameOps.cropBoard(frame, scene.rect, size)
            assertEquals(size, board.width)
            assertEquals(size, board.height)
        }
        // at 8x8 every output pixel is the average of one square: the theme colours in checker order
        val tiny = FrameOps.cropBoard(frame, scene.rect, 8)
        for (row in 0..7) for (col in 0..7) {
            val expected = if ((row + col) % 2 == 0) Themes.GREEN.light else Themes.GREEN.dark
            val c = channels(tiny.pixel(col, row))
            assertTrue(
                abs(c[1] - expected.red) <= 3 && abs(c[2] - expected.green) <= 3 && abs(c[3] - expected.blue) <= 3,
                "square ($row,$col): ${c.toList()} vs $expected",
            )
        }
        // same size: a plain copy
        val same = FrameOps.cropBoard(frame, scene.rect, scene.size)
        assertEquals(frame.pixel(scene.left + 500, scene.top + 17), same.pixel(500, 17))
        assertEquals(frame.pixel(scene.left, scene.top), same.pixel(0, 0))
    }

    @Test
    fun `cropBoard keeps pieces where they are`() {
        val scene = Scene(1080, 2400, 39, 540, 1002, Themes.BROWN, Positions.MIDDLE)
        val frame = scene.render()
        val board = FrameOps.cropBoard(frame, scene.rect, 512)
        val snapshot = BoardSampler.sample(board, BoardRect(0, 0, 512, 1f))
        assertEquals(Positions.MIDDLE, cellsToString(snapshot.cells))
    }

    @Test
    fun `cropBoard clips rectangles that leave the frame`() {
        val frame = gradientFrame(300, 300)
        val board = FrameOps.cropBoard(frame, BoardRect(200, 200, 400, 1f), 64)
        assertEquals(64, board.width)
        assertEquals(64, board.height)
        assertFailsWith<IllegalArgumentException> { FrameOps.cropBoard(frame, BoardRect(400, 400, 100, 1f), 64) }
        assertFailsWith<IllegalArgumentException> { FrameOps.cropBoard(frame, BoardRect(0, 0, 100, 1f), 0) }
    }
}
