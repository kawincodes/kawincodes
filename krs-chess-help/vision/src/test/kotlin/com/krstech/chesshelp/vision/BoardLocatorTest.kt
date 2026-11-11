package com.krstech.chesshelp.vision

import java.awt.Color
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BoardLocatorTest {

    private fun assertLocated(scene: Scene, frame: Frame = scene.render(), hint: BoardRect? = null): BoardRect {
        val rect = BoardLocator.locate(frame, hint)
        assertNotNull(rect, "board not found: $scene")
        val message = "$scene -> $rect"
        assertTrue(abs(rect.left - scene.left) <= TOLERANCE, message)
        assertTrue(abs(rect.top - scene.top) <= TOLERANCE, message)
        assertTrue(abs(rect.right - (scene.left + scene.size)) <= TOLERANCE, message)
        assertTrue(abs(rect.bottom - (scene.top + scene.size)) <= TOLERANCE, message)
        assertTrue(rect.confidence > 0.75f && rect.confidence <= 1f, message)
        assertTrue(rect.fitsIn(frame.width, frame.height), message)
        return rect
    }

    @Test
    fun `finds boards of every theme, size and offset among UI clutter`() {
        for (scene in SyntheticScenes.plain) assertLocated(scene)
    }

    @Test
    fun `finds boards covered with highlights, dots, rings, check glow and arrows`() {
        for (scene in SyntheticScenes.decorated) assertLocated(scene)
    }

    @Test
    fun `finds empty, sparse, textured and edge-to-edge boards`() {
        for (scene in SyntheticScenes.special) assertLocated(scene)
    }

    @Test
    fun `every theme works on every page background`() {
        val pages = listOf(Color(255, 255, 255), Color(0, 0, 0), Color(128, 128, 128))
        for ((k, theme) in Themes.ALL.withIndex()) for (page in pages) {
            // the page colour also equals one of the square colours once per theme
            for (bg in listOf(page, if (k % 2 == 0) theme.dark else theme.light)) {
                assertLocated(Scene(720, 1280, 44 + k, 300 + 3 * k, 600 + 2 * k, theme, Positions.MIDDLE, pageColor = bg, clutter = false, name = "page $bg"))
            }
        }
    }

    @Test
    fun `returns null for screens without a board`() {
        val frames = mapOf(
            "app screen" to Clutter.appWithoutBoard(1080, 2400, Color(48, 46, 43), 3),
            "light app screen" to Clutter.appWithoutBoard(1080, 2400, Color(240, 240, 240), 4),
            "landscape app screen" to Clutter.appWithoutBoard(2000, 1200, Color(30, 30, 34), 5),
            "text page" to Clutter.textPage(1080, 2400, 6),
            "small text page" to Clutter.textPage(720, 1560, 7),
            "gradient" to Clutter.gradient(1080, 2400),
            "photo noise" to Clutter.photoNoise(1080, 2400, 8),
            "landscape photo noise" to Clutter.photoNoise(2000, 1200, 9),
            "icon grid 4" to Clutter.iconGrid(1080, 2400, 4, 10),
            "icon grid 5" to Clutter.iconGrid(1440, 3120, 5, 11),
            "icon grid 8" to Clutter.iconGrid(2000, 1200, 8, 12),
            "keyboard" to Clutter.keyboard(1080, 2400, 13),
            "small keyboard" to Clutter.keyboard(720, 1560, 14),
            "horizontal stripes" to Clutter.stripes(1080, 2400, 135, true),
            "vertical stripes" to Clutter.stripes(1080, 2400, 120, false),
            "tile grid" to Clutter.tileGrid(1080, 2400, 15),
        )
        for ((name, frame) in frames) assertNull(BoardLocator.locate(frame), "false positive on: $name")
    }

    @Test
    fun `rejects checker patterns that are not an 8x8 board`() {
        val page = Color(40, 40, 44)
        val frames = mapOf(
            "5x5 checker" to Clutter.checker(1080, 2400, 40, 600, 200, 5, 5, Themes.GREEN, page, 1),
            "6x6 checker" to Clutter.checker(1080, 2400, 60, 600, 160, 6, 6, Themes.BROWN, page, 2),
            "7x7 checker" to Clutter.checker(1080, 2400, 15, 600, 150, 7, 7, Themes.BLUE, page, 3),
            "8x4 checker strip" to Clutter.checker(1080, 2400, 0, 800, 135, 8, 4, Themes.GREEN, page, 4),
            "4x8 checker strip" to Clutter.checker(1080, 2400, 270, 600, 135, 4, 8, Themes.GREEN, page, 5),
            "9x9 checker" to Clutter.checker(1080, 2400, 0, 500, 120, 9, 9, Themes.GREY, page, 6),
            "10x10 draughts board" to Clutter.checker(1080, 2400, 40, 500, 100, 10, 10, Themes.BROWN, page, 7),
            "12x12 checker" to Clutter.checker(1080, 2400, 30, 500, 85, 12, 12, Themes.PURPLE, page, 8),
            "8x12 checker" to Clutter.checker(1080, 2400, 20, 300, 130, 8, 12, Themes.BLUE, page, 9),
            "checker wallpaper" to Clutter.checker(1080, 2400, 0, 0, 90, 12, 27, Themes.GREY, page, 10),
        )
        for ((name, frame) in frames) assertNull(BoardLocator.locate(frame), "false positive on: $name")
    }

    @Test
    fun `tiny, uniform and degenerate frames return null`() {
        assertNull(BoardLocator.locate(Frame(1, 1, IntArray(1))))
        assertNull(BoardLocator.locate(Frame(64, 64, IntArray(64 * 64) { -1 })))
        assertNull(BoardLocator.locate(Frame(1080, 2400, IntArray(1080 * 2400) { 0xFF336699.toInt() })))
        assertNull(BoardLocator.locate(Frame(2400, 40, IntArray(2400 * 40) { 0xFF000000.toInt() })))
    }

    @Test
    fun `a correct hint is confirmed and gives the same rectangle`() {
        for (scene in listOf(SyntheticScenes.plain[1], SyntheticScenes.decorated[0], SyntheticScenes.special[0], SyntheticScenes.plain[11])) {
            val frame = scene.render()
            val found = assertLocated(scene, frame)
            val again = assertLocated(scene, frame, hint = found)
            assertEquals(found.left, again.left, scene.toString())
            assertEquals(found.top, again.top, scene.toString())
            assertEquals(found.size, again.size, scene.toString())
            // a hint that is a pixel or two off is pulled back onto the grid
            val nudged = BoardRect(found.left + 2, found.top - 1, found.size + 1, found.confidence)
            assertLocated(scene, frame, hint = nudged)
        }
    }

    @Test
    fun `a stale hint falls back to the full search`() {
        val scene = SyntheticScenes.plain[1]
        val frame = scene.render()
        for (hint in listOf(
            BoardRect(scene.left + 60, scene.top + 200, scene.size - 120, 1f),     // wrong place and size
            BoardRect(scene.left + scene.size / 8, scene.top, scene.size, 1f),     // shifted by exactly one square
            BoardRect(scene.left, scene.top - scene.size / 8, scene.size, 1f),
            BoardRect(-500, -500, 4000, 1f),                                       // outside the frame
            BoardRect(10, 10, 3, 1f),                                              // degenerate
        )) {
            assertLocated(scene, frame, hint = hint)
        }
    }

    @Test
    fun `a hint cannot conjure a board`() {
        val hint = BoardRect(39, 540, 1002, 1f)
        assertNull(BoardLocator.locate(Clutter.appWithoutBoard(1080, 2400, Color(48, 46, 43), 3), hint))
        assertNull(BoardLocator.locate(Clutter.photoNoise(1080, 2400, 8), hint))
        // a board that was there but is now covered by something else
        assertNull(BoardLocator.locate(Clutter.checker(1080, 2400, 39, 540, 1002 / 12, 12, 12, Themes.BROWN, Color(48, 46, 43), 3), hint))
    }

    @Test
    fun `a board that moved is found again despite the old hint`() {
        val before = SyntheticScenes.plain[1]
        val after = before.copy(left = before.left + 21, top = before.top - 133)
        val old = assertLocated(before)
        assertLocated(after, after.render(), hint = old)
    }

    @Test
    fun `full search of a 1080x2400 frame stays within the time budget`() {
        val scene = SyntheticScenes.decorated[0]
        val frame = scene.render()
        val rect = assertLocated(scene, frame)
        repeat(8) { BoardLocator.locate(frame) }     // JIT warm-up
        var best = Long.MAX_VALUE
        var bestHinted = Long.MAX_VALUE
        repeat(9) {
            var t = System.nanoTime()
            BoardLocator.locate(frame)
            best = minOf(best, System.nanoTime() - t)
            t = System.nanoTime()
            BoardLocator.locate(frame, rect)
            bestHinted = minOf(bestHinted, System.nanoTime() - t)
        }
        // best-of-N so that a busy build machine does not make the check flaky
        assertTrue(best < 200_000_000L, "full locate took ${best / 1_000_000} ms")
        assertTrue(bestHinted < 20_000_000L, "hinted locate took ${bestHinted / 1_000_000} ms")
    }

    private companion object {
        const val TOLERANCE = 2
    }
}
