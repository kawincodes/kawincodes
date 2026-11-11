package com.krstech.chesshelp.vision

import java.awt.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BoardSamplerTest {

    private fun assertCells(scene: Scene, snapshot: BoardSnapshot, ignore: Set<Int> = emptySet()) {
        val got = cellsToString(snapshot.cells)
        val wrong = (0 until 64).filter { it !in ignore && got[it] != scene.cells[it] }
        assertTrue(
            wrong.isEmpty(),
            "$scene: wrong cells " + wrong.joinToString { "(${it / 8},${it % 8}) expected ${scene.cells[it]} got ${got[it]}" } +
                "\n" + snapshot.toAscii(),
        )
    }

    private fun assertPlausible(scene: Scene, snapshot: BoardSnapshot) {
        assertEquals(64, snapshot.cells.size)
        assertEquals(64, snapshot.confidence.size)
        for (c in snapshot.confidence) assertTrue(c in 0f..1f, "$scene: confidence $c out of range")
        assertTrue(snapshot.reliable, "$scene: snapshot flagged unreliable")
        assertTrue(snapshot.confidence.average() > 0.8, "$scene: mean confidence ${snapshot.confidence.average()}")
    }

    @Test
    fun `reads every cell of plain boards in all themes and sizes`() {
        for (scene in SyntheticScenes.plain) {
            val snapshot = BoardSampler.sample(scene.render(), scene.rect)
            assertCells(scene, snapshot)
            assertPlausible(scene, snapshot)
        }
    }

    @Test
    fun `highlights, dots, capture hints, check glow and labels do not change the reading`() {
        for (scene in SyntheticScenes.decorated.filter { it.arrow == null }) {
            val snapshot = BoardSampler.sample(scene.render(), scene.rect)
            assertCells(scene, snapshot)
            assertPlausible(scene, snapshot)
            for (dot in scene.dots) assertEquals(CellState.EMPTY, snapshot.cells[dot], "$scene: dot at $dot")
        }
    }

    @Test
    fun `squares away from a drawn arrow are still read correctly`() {
        // The arrow itself may read as a piece on the empty squares it crosses; nothing else may change.
        val crossed = mapOf(
            SyntheticScenes.decorated[2] to setOf(52, 44, 36, 28, 20),
            SyntheticScenes.decorated[3] to setOf(58, 51, 44, 37, 30),
        )
        for ((scene, ignore) in crossed) {
            assertNotNull(scene.arrow)
            assertCells(scene, BoardSampler.sample(scene.render(), scene.rect), ignore)
        }
    }

    @Test
    fun `empty, sparse and textured boards`() {
        for (scene in SyntheticScenes.special) {
            val snapshot = BoardSampler.sample(scene.render(), scene.rect)
            assertCells(scene, snapshot)
            assertPlausible(scene, snapshot)
        }
    }

    @Test
    fun `the located rectangle reads the same as the true rectangle`() {
        for (scene in SyntheticScenes.plain + SyntheticScenes.decorated.filter { it.arrow == null }) {
            val frame = scene.render()
            val rect = BoardLocator.locate(frame)
            assertNotNull(rect, "not located: $scene")
            assertCells(scene, BoardSampler.sample(frame, rect))
        }
    }

    @Test
    fun `a rectangle that is off by two pixels still reads correctly`() {
        val scene = SyntheticScenes.decorated[0]
        val frame = scene.render()
        for ((dx, dy, ds) in listOf(Triple(2, 2, 0), Triple(-2, -2, 0), Triple(2, -2, -2), Triple(-2, 0, 3), Triple(0, 2, -3))) {
            val rect = BoardRect(scene.left + dx, scene.top + dy, scene.size + ds, 1f)
            assertCells(scene, BoardSampler.sample(frame, rect))
        }
    }

    @Test
    fun `every piece shape is recognised on both square colours in every theme`() {
        // one board per theme: light pieces on the top four rows, dark pieces on the bottom four
        val cells = "LLLLLLLL".repeat(2) + "........".repeat(4) + "DDDDDDDD".repeat(2)
        val swapped = "DDDDDDDD".repeat(2) + "........".repeat(4) + "LLLLLLLL".repeat(2)
        for (theme in Themes.ALL) for (position in listOf(cells, swapped)) {
            for (size in intArrayOf(378, 656, 1000)) {
                val scene = Scene(1080, 1400, 40, 150, size, theme, position, clutter = false, name = "shapes $size")
                assertCells(scene, BoardSampler.sample(scene.render(), scene.rect))
            }
        }
    }

    @Test
    fun `more pieces than a chess set has is flagged unreliable`() {
        val scene = Scene(1080, 2400, 0, 420, 1080, Themes.GREEN, Positions.CROWDED, name = "crowded")
        val snapshot = BoardSampler.sample(scene.render(), scene.rect)
        assertCells(scene, snapshot)
        assertFalse(snapshot.reliable, "40 pieces must not be reported as reliable")
    }

    @Test
    fun `a region that is not a board is flagged unreliable`() {
        val noise = BoardSampler.sample(Clutter.photoNoise(1080, 2400, 8), BoardRect(40, 500, 1000, 1f))
        assertFalse(noise.reliable, "photo noise:\n" + noise.toAscii())
        val icons = BoardSampler.sample(Clutter.iconGrid(1080, 2400, 8, 10), BoardRect(0, 60, 1080, 1f))
        assertFalse(icons.reliable, "icon grid:\n" + icons.toAscii())
    }

    @Test
    fun `degenerate rectangles do not crash and are unreliable`() {
        val scene = SyntheticScenes.plain[0]
        val frame = scene.render()
        val tiny = BoardSampler.sample(frame, BoardRect(10, 10, 40, 1f))
        assertFalse(tiny.reliable)
        assertTrue(tiny.cells.all { it == CellState.EMPTY })
        // partly or completely outside the frame: must simply not throw
        BoardSampler.sample(frame, BoardRect(-300, -300, 1000, 1f))
        BoardSampler.sample(frame, BoardRect(700, 2000, 1000, 1f))
        BoardSampler.sample(frame, BoardRect(5000, 5000, 800, 1f))
        BoardSampler.sample(frame, BoardRect(0, 0, 0, 1f))
    }

    @Test
    fun `a flat area reads as sixty-four empty squares`() {
        val frame = Frame(800, 800, IntArray(800 * 800) { 0xFF8FA3B5.toInt() })
        val snapshot = BoardSampler.sample(frame, BoardRect(0, 0, 800, 1f))
        assertTrue(snapshot.cells.all { it == CellState.EMPTY })
    }

    @Test
    fun `toAscii prints eight rows of eight characters, top row first`() {
        val scene = Scene(720, 1000, 20, 100, 680, Themes.BROWN, Positions.MIDDLE, pageColor = Color(30, 30, 30), clutter = false)
        val ascii = BoardSampler.sample(scene.render(), scene.rect).toAscii()
        val lines = ascii.split("\n")
        assertEquals(8, lines.size)
        assertEquals(Positions.MIDDLE.chunked(8), lines)
    }

    @Test
    fun `sampling a 1080 wide board is fast`() {
        val scene = SyntheticScenes.decorated[0]
        val frame = scene.render()
        repeat(10) { BoardSampler.sample(frame, scene.rect) }
        var best = Long.MAX_VALUE
        repeat(9) {
            val t = System.nanoTime()
            BoardSampler.sample(frame, scene.rect)
            best = minOf(best, System.nanoTime() - t)
        }
        assertTrue(best < 40_000_000L, "sample took ${best / 1_000_000} ms")
    }
}
