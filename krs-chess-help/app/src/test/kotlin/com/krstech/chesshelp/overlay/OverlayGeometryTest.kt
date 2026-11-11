package com.krstech.chesshelp.overlay

import com.krstech.chesshelp.settings.FractionPoint
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OverlayGeometryTest {
    // 1080 x 2400 portrait with a status bar/cutout of 96 px and a gesture bar of 60 px.
    private val portrait = OverlayGeometry.insetArea(1080, 2400, 0, 96, 0, 60)
    private val bubble = 154

    @Test
    fun `inset area removes bars and never inverts`() {
        assertEquals(Area(0, 96, 1080, 2340), portrait)
        assertEquals(Area(132, 0, 2268, 1080), OverlayGeometry.insetArea(2400, 1080, 132, 0, 132, 0))
        val silly = OverlayGeometry.insetArea(100, 100, 400, 400, 400, 400)
        assertTrue(silly.width >= 0 && silly.height >= 0)
        assertEquals(Area(0, 0, 100, 100), OverlayGeometry.insetArea(100, 100, -5, -5, -5, -5))
    }

    @Test
    fun `clamp keeps a window inside the area`() {
        assertEquals(IntPoint(300, 500), OverlayGeometry.clamp(300, 500, bubble, bubble, portrait))
        assertEquals(IntPoint(0, 96), OverlayGeometry.clamp(-50, -50, bubble, bubble, portrait))
        assertEquals(IntPoint(1080 - bubble, 2340 - bubble), OverlayGeometry.clamp(5000, 5000, bubble, bubble, portrait))
        assertEquals(IntPoint(0, 96), OverlayGeometry.clamp(0, 0, bubble, bubble, portrait))
    }

    @Test
    fun `a window larger than the area is pinned to its top left`() {
        assertEquals(IntPoint(0, 96), OverlayGeometry.clamp(40, 400, 2000, 4000, portrait))
    }

    @Test
    fun `fractions map to the edges and round trip`() {
        assertEquals(IntPoint(0, 96), OverlayGeometry.fromFraction(FractionPoint(0f, 0f), bubble, bubble, portrait))
        assertEquals(
            IntPoint(1080 - bubble, 2340 - bubble),
            OverlayGeometry.fromFraction(FractionPoint(1f, 1f), bubble, bubble, portrait),
        )
        for (x in listOf(0, 17, 463, 926)) {
            for (y in listOf(96, 1000, 2186)) {
                val fraction = OverlayGeometry.toFraction(x, y, bubble, bubble, portrait)
                val back = OverlayGeometry.fromFraction(fraction, bubble, bubble, portrait)
                assertTrue(abs(back.x - x) <= 1 && abs(back.y - y) <= 1, "($x,$y) -> $fraction -> $back")
            }
        }
    }

    @Test
    fun `a bubble docked at the right edge stays docked after rotation`() {
        val fraction = OverlayGeometry.toFraction(1080 - bubble, 600, bubble, bubble, portrait)
        val landscape = OverlayGeometry.insetArea(2400, 1080, 96, 0, 0, 60)
        val moved = OverlayGeometry.fromFraction(fraction, bubble, bubble, landscape)
        assertEquals(2400 - bubble, moved.x)
        assertTrue(moved.y in landscape.top..(landscape.bottom - bubble))
    }

    @Test
    fun `out of range and NaN fractions stay on screen`() {
        for (fraction in listOf(FractionPoint(-1f, 9f), FractionPoint(Float.NaN, Float.NaN), FractionPoint(2f, -2f))) {
            val p = OverlayGeometry.fromFraction(fraction, bubble, bubble, portrait)
            assertEquals(p, OverlayGeometry.clamp(p.x, p.y, bubble, bubble, portrait), "$fraction")
        }
        assertEquals(FractionPoint(0f, 0f), OverlayGeometry.toFraction(10, 10, 5000, 5000, portrait))
    }

    @Test
    fun `drag starts only beyond the touch slop`() {
        assertFalse(OverlayGeometry.isDrag(0f, 0f, 24))
        assertFalse(OverlayGeometry.isDrag(16f, 16f, 24))
        assertFalse(OverlayGeometry.isDrag(24f, 0f, 24))
        assertTrue(OverlayGeometry.isDrag(18f, 18f, 24))
        assertTrue(OverlayGeometry.isDrag(0f, -25f, 24))
    }

    @Test
    fun `panel width is capped and leaves a margin on narrow screens`() {
        assertEquals(825, OverlayGeometry.panelWidth(1080, 825, 22))
        assertEquals(720 - 44, OverlayGeometry.panelWidth(720, 825, 22))
        assertEquals(1, OverlayGeometry.panelWidth(10, 825, 22))
    }

    @Test
    fun `panel height is at most sixty percent of the screen`() {
        assertEquals(1440, OverlayGeometry.panelMaxHeight(2400, 2244))
        assertEquals(648, OverlayGeometry.panelMaxHeight(1080, 1020))
        assertEquals(500, OverlayGeometry.panelMaxHeight(2400, 500))
        assertEquals(1, OverlayGeometry.panelMaxHeight(0, 0))
    }

    // 360dp-wide phone at density 2: a 300dp panel does not fit beside a 56dp bubble.
    private val narrow = OverlayGeometry.insetArea(720, 1560, 0, 60, 0, 40)

    @Test
    fun `panel goes below a bubble on a narrow phone, aligned to the bubble's side`() {
        val right = OverlayGeometry.panelNear(720 - 112, 300, 112, 600, 700, narrow, 12)
        assertEquals(IntPoint(720 - 600, 300 + 112 + 12), right)
        val left = OverlayGeometry.panelNear(0, 300, 112, 600, 700, narrow, 12)
        assertEquals(IntPoint(0, 300 + 112 + 12), left)
    }

    @Test
    fun `panel goes above the bubble when there is no room below`() {
        val p = OverlayGeometry.panelNear(0, 1300, 112, 600, 700, narrow, 12)
        assertEquals(IntPoint(0, 1300 - 12 - 700), p)
    }

    @Test
    fun `panel sits beside a docked bubble when the phone is wide enough`() {
        val right = OverlayGeometry.panelNear(1080 - bubble, 600, bubble, 825, 1200, portrait, 16)
        assertEquals(IntPoint(1080 - bubble - 16 - 825, 600), right)
        // Too close to the bottom: same side, pushed up to stay on screen.
        val low = OverlayGeometry.panelNear(0, 2000, bubble, 825, 1200, portrait, 16)
        assertEquals(IntPoint(bubble + 16, 2340 - 1200), low)
    }

    @Test
    fun `panel sits beside the bubble on a wide screen`() {
        val landscape = OverlayGeometry.insetArea(2400, 1080, 0, 60, 0, 0)
        val right = OverlayGeometry.panelNear(2400 - bubble, 200, bubble, 825, 600, landscape, 16)
        assertEquals(IntPoint(2400 - bubble - 16 - 825, 200), right)
        val left = OverlayGeometry.panelNear(0, 200, bubble, 825, 600, landscape, 16)
        assertEquals(IntPoint(bubble + 16, 200), left)
    }

    @Test
    fun `panel placement is always inside the area`() {
        val areas = listOf(portrait, OverlayGeometry.insetArea(2400, 1080, 96, 0, 132, 0), OverlayGeometry.insetArea(720, 1280, 0, 48, 0, 96))
        for (area in areas) {
            for (bx in listOf(area.left, (area.left + area.right) / 2, area.right - bubble)) {
                for (by in listOf(area.top, (area.top + area.bottom) / 2, area.bottom - bubble)) {
                    for (h in listOf(200, 600, area.height * 6 / 10)) {
                        val w = OverlayGeometry.panelWidth(area.width, 825, 22)
                        val p = OverlayGeometry.panelNear(bx, by, bubble, w, h, area, 16)
                        assertTrue(p.x >= area.left && p.x + w <= area.right, "x $p in $area")
                        assertTrue(p.y >= area.top && p.y + h <= area.bottom, "y $p in $area")
                    }
                }
            }
        }
    }

    @Test
    fun `arrow window alpha stays below the pass-through limit`() {
        val default = OverlayGeometry.arrowWindowAlpha(0.8f)
        assertTrue(default < 0.8f && default > 0.7f, "$default")
        assertTrue(OverlayGeometry.arrowWindowAlpha(1f) < 0.8f)
        val strict = OverlayGeometry.arrowWindowAlpha(0.5f)
        assertTrue(strict < 0.5f && strict > 0.4f, "$strict")
        assertEquals(0f, OverlayGeometry.arrowWindowAlpha(0f))
        assertEquals(default, OverlayGeometry.arrowWindowAlpha(Float.NaN))
    }
}

class ArrowGeometryTest {
    private fun assertNear(expected: Float, actual: Float, message: String = "") =
        assertTrue(abs(expected - actual) < 1e-3f, "$message expected $expected but was $actual")

    @Test
    fun `cell centres`() {
        assertNear(50f, ArrowGeometry.cellCenter(0f, 100f, 0))
        assertNear(750f, ArrowGeometry.cellCenter(0f, 100f, 7))
        assertNear(140f + 3.5f * 90f, ArrowGeometry.cellCenter(140f, 90f, 3))
    }

    @Test
    fun `degenerate input gives no arrow`() {
        assertNull(ArrowGeometry.polygon(10f, 10f, 10f, 10f, 100f))
        assertNull(ArrowGeometry.polygon(0f, 0f, 100f, 0f, 0f))
    }

    @Test
    fun `horizontal arrow points at the target and is symmetric`() {
        val cell = 100f
        val p = assertNotNull(ArrowGeometry.polygon(50f, 50f, 350f, 50f, cell))
        assertEquals(ArrowGeometry.POINT_COUNT * 2, p.size)
        // Tip (4th point) lies on the axis, just past the target centre but inside the target cell.
        assertNear(50f, p[7], "tip y")
        assertTrue(p[6] > 350f && p[6] < 400f, "tip x ${p[6]}")
        // Shaft starts outside the centre of the from-cell but inside that cell.
        assertTrue(p[0] > 50f && p[0] < 100f, "start x ${p[0]}")
        assertNear(p[0], p[12], "shaft start x")
        // Mirror pairs about the axis y = 50: (0,6) (1,5) (2,4).
        for ((a, b) in listOf(0 to 6, 1 to 5, 2 to 4)) {
            assertNear(p[a * 2], p[b * 2], "x of $a/$b")
            assertNear(50f - p[a * 2 + 1], p[b * 2 + 1] - 50f, "y of $a/$b")
        }
        // The head is wider than the shaft and its base is behind the tip.
        val shaftHalf = abs(p[1] - 50f)
        val headHalf = abs(p[5] - 50f)
        assertTrue(headHalf > shaftHalf * 2, "head $headHalf shaft $shaftHalf")
        assertTrue(p[4] < p[6] && p[2] < p[6])
        assertTrue(headHalf * 2 < cell, "head fits in a cell")
    }

    @Test
    fun `arrow geometry is rotation invariant`() {
        val cell = 80f
        // A knight move up-left and the same vector rotated by 90 degrees must have equal dimensions.
        val a = assertNotNull(ArrowGeometry.polygon(400f, 400f, 320f, 240f, cell))
        val b = assertNotNull(ArrowGeometry.polygon(400f, 400f, 560f, 320f, cell))
        fun lengths(p: FloatArray) = (0 until 7).map { i ->
            val j = (i + 1) % 7
            hypot(p[j * 2] - p[i * 2], p[j * 2 + 1] - p[i * 2 + 1])
        }
        lengths(a).zip(lengths(b)).forEach { (x, y) -> assertNear(x, y, "edge") }
    }

    @Test
    fun `adjacent cell arrow keeps a positive shaft`() {
        val cell = 100f
        val p = assertNotNull(ArrowGeometry.polygon(50f, 50f, 50f, 150f, cell))
        // Pointing down: start y < head base y < tip y.
        assertTrue(p[1] < p[3] && p[3] < p[7], "${p.toList()}")
        assertNear(50f, p[6], "tip x")
    }

    @Test
    fun `scales with the cell size`() {
        val small = assertNotNull(ArrowGeometry.polygon(5f, 5f, 35f, 5f, 10f))
        val large = assertNotNull(ArrowGeometry.polygon(50f, 50f, 350f, 50f, 100f))
        for (i in small.indices) assertNear(small[i] * 10f, large[i], "point $i")
    }
}

class OverlayModelsTest {
    private val start = listOf("rnbqkbnr", "pppppppp", "........", "........", "........", "........", "PPPPPPPP", "RNBQKBNR")

    @Test
    fun `glyph mapping covers both colours with solid glyphs`() {
        val expected = mapOf('K' to '♚', 'Q' to '♛', 'R' to '♜', 'B' to '♝', 'N' to '♞', 'P' to '♟')
        for ((letter, glyph) in expected) {
            for (c in listOf(letter, letter.lowercaseChar())) {
                val text = assertNotNull(PieceGlyphs.glyph(c), "$c")
                assertEquals(glyph, text[0], "$c")
                // Text-presentation selector, so the pawn is not drawn as a colour emoji.
                assertEquals("︎", text.substring(1), "$c")
                assertEquals(letter.toString(), PieceGlyphs.letter(c))
                assertTrue(PieceGlyphs.isPiece(c))
            }
            assertTrue(PieceGlyphs.isWhite(letter))
            assertFalse(PieceGlyphs.isWhite(letter.lowercaseChar()))
        }
    }

    @Test
    fun `non pieces have no glyph`() {
        for (c in listOf('.', ' ', '1', '8', 'x', 'Z', '/', '-')) {
            assertNull(PieceGlyphs.glyph(c), "$c")
            assertNull(PieceGlyphs.letter(c), "$c")
            assertFalse(PieceGlyphs.isPiece(c))
        }
    }

    @Test
    fun `mini board lookup tolerates malformed rows`() {
        val board = MiniBoard(start, 6, 4, 4, 4)
        assertEquals('r', board.pieceAt(0, 0))
        assertEquals('K', board.pieceAt(7, 4))
        assertEquals('.', board.pieceAt(3, 3))
        assertEquals('.', board.pieceAt(-1, 0))
        assertEquals('.', board.pieceAt(8, 0))
        assertEquals('.', board.pieceAt(0, 8))
        val broken = MiniBoard(listOf("rn", ""), null, null, null, null)
        assertEquals('n', broken.pieceAt(0, 1))
        assertEquals('.', broken.pieceAt(0, 5))
        assertEquals('.', broken.pieceAt(1, 0))
        assertEquals('.', broken.pieceAt(5, 5))
    }

    @Test
    fun `mini board move needs four in-range coordinates`() {
        assertTrue(MiniBoard(start, 6, 4, 4, 4).hasMove)
        assertFalse(MiniBoard(start, null, null, null, null).hasMove)
        assertFalse(MiniBoard(start, 6, 4, null, 4).hasMove)
        assertFalse(MiniBoard(start, 6, 4, 8, 4).hasMove)
        assertFalse(MiniBoard(start, -1, 4, 4, 4).hasMove)
        assertFalse(MiniBoard(start, 4, 4, 4, 4).hasMove)
    }

    @Test
    fun `board arrow validity`() {
        assertTrue(BoardArrow(0, 420, 1080, 6, 4, 4, 4).isDrawable)
        assertFalse(BoardArrow(0, 420, 0, 6, 4, 4, 4).isDrawable)
        assertFalse(BoardArrow(0, 420, 1080, 6, 4, 6, 4).isDrawable)
        assertFalse(BoardArrow(0, 420, 1080, 8, 4, 4, 4).isDrawable)
        assertFalse(BoardArrow(0, 420, 1080, 6, 4, 4, -1).isDrawable)
    }

    @Test
    fun `evaluation tone`() {
        assertEquals(EvalTone.GOOD, EvalTone.of("Winning"))
        assertEquals(EvalTone.GOOD, EvalTone.of(" better for White"))
        assertEquals(EvalTone.BAD, EvalTone.of("LOSING"))
        assertEquals(EvalTone.BAD, EvalTone.of("worse"))
        assertEquals(EvalTone.NEUTRAL, EvalTone.of("Equal"))
        assertEquals(EvalTone.NEUTRAL, EvalTone.of("Unclear"))
        assertEquals(EvalTone.NEUTRAL, EvalTone.of(""))
        assertEquals(EvalTone.NEUTRAL, EvalTone.of("+1.4"))
    }
}
