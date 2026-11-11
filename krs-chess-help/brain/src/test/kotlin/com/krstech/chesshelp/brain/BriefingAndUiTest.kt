package com.krstech.chesshelp.brain

import com.krstech.chesshelp.chess.Move
import com.krstech.chesshelp.chess.Position
import com.krstech.chesshelp.claude.Evaluation
import com.krstech.chesshelp.claude.TokenUsage
import com.krstech.chesshelp.vision.CellState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BriefingAndUiTest {
    private fun hint(position: Position, san: String, cp: Int, mate: Int? = null, vararg rest: String): EngineHint =
        EngineHint(position.parseMove(san)!!, san, cp, mate, listOf(san) + rest)

    @Test
    fun briefingListsLegalMovesAndNumberedEngineLines() {
        val start = Position.START
        val briefing = Briefings.build(
            start,
            listOf(
                hint(start, "e4", 35, null, "e5", "Nf3"),
                hint(start, "d4", 0),
                hint(start, "a3", -120, null, "e5"),
            ),
        )
        val lines = briefing.text.lines()
        assertEquals("FEN: ${start.toFen()}", lines[0])
        assertEquals("Side to move: White", lines[1])
        assertEquals("Check: no", lines[2])
        val legal = lines.single { it.startsWith("Legal moves:") }.removePrefix("Legal moves:").trim().split(" ")
        assertEquals(20, legal.size)
        assertTrue(legal.containsAll(listOf("e4", "Nf3", "Na3", "h4")))
        assertTrue(lines.any { it.contains("shallow") })
        assertTrue("1. e4 (+0.35) e5 Nf3" in lines, briefing.text)
        assertTrue("2. d4 (0.00)" in lines, briefing.text)
        assertTrue("3. a3 (-1.20) e5" in lines, briefing.text)
        assertEquals(3, briefing.lines.size)
    }

    @Test
    fun briefingSaysCheckAndMate() {
        val check = Position.START.after("e4", "f5", "Qh5+")
        val briefing = Briefings.build(check, listOf(hint(check, "g6", -250, -3)), notes = listOf("something to know."))
        assertTrue(briefing.text.contains("Side to move: Black"))
        assertTrue(briefing.text.contains("Check: yes, Black's king is in check"))
        assertTrue(briefing.text.contains("Note: something to know."))
        assertTrue(briefing.text.lines().single { it.startsWith("Legal moves:") }.trim() == "Legal moves: g6")
        assertTrue(briefing.text.contains("1. g6 (mated in 3)"))
        assertTrue(Briefings.build(Position.START, listOf(hint(Position.START, "e4", 31990, 2))).text.contains("1. e4 (mate in 2)"))
    }

    @Test
    fun briefingWithoutEngineLinesSaysSo() {
        val text = Briefings.build(Position.START, emptyList()).text
        assertTrue(text.contains("Local engine lines: none available"))
        assertTrue(text.lines().any { it.startsWith("Legal moves: ") })
    }

    @Test
    fun tokenFooter() {
        assertEquals("1.2k in / 0.3k out", BrainUi.usageFooter(TokenUsage(1234, 321)))
        assertEquals("45 in / 0.9k out", BrainUi.usageFooter(TokenUsage(45, 870)))
        assertEquals("123k in / 1.5M out", BrainUi.usageFooter(TokenUsage(123_400, 1_500_000)))
        assertNull(BrainUi.usageFooter(TokenUsage(0, 0)))
    }

    @Test
    fun evaluationLabels() {
        assertEquals(
            listOf("Winning", "Better", "Equal", "Worse", "Losing", "Unclear"),
            Evaluation.entries.map { BrainUi.label(it) },
        )
        val any = Move(12, 28)
        fun label(cp: Int, mate: Int? = null) = BrainUi.label(EngineHint(any, "e4", cp, mate, listOf("e4")))
        assertEquals("Winning", label(450))
        assertEquals("Better", label(120))
        assertEquals("Equal", label(-30))
        assertEquals("Worse", label(-150))
        assertEquals("Losing", label(-500))
        assertEquals("Winning", label(31000, 4))
        assertEquals("Losing", label(-31000, -2))
    }

    @Test
    fun sourceLabelFollowsTheServingModel() {
        assertEquals("My label", BrainUi.sourceLabel("claude-opus-5-5", "claude-opus-5-5", "My label"))
        assertEquals("My label", BrainUi.sourceLabel("", "claude-opus-5-5", "My label"))
        assertEquals("Claude Sonnet 5.5", BrainUi.sourceLabel("claude-sonnet-5-5", "claude-opus-5-5", "My label"))
    }

    @Test
    fun boardAndArrowUseViewCoordinates() {
        val move = Position.START.parseMove("e4")!!
        val white = BrainUi.board(Position.START, true, move)
        assertEquals("rnbqkbnr", white.rows[0])
        assertEquals(listOf(6, 4, 4, 4), listOf(white.fromRow, white.fromCol, white.toRow, white.toCol))
        val black = BrainUi.board(Position.START, false, move)
        assertEquals("RNBKQBNR", black.rows[0])
        assertEquals(listOf(1, 3, 3, 3), listOf(black.fromRow, black.fromCol, black.toRow, black.toCol))
        assertEquals(ArrowSpec(40, 600, 1000, 1, 3, 3, 3), BrainUi.arrow(BOARD_RECT, move, false))
    }

    @Test
    fun playedSentenceNamesTheSides() {
        assertEquals("White played e4.", BrainUi.playedSentence(listOf("e4"), Position.START.after("e4")))
        assertEquals(
            "White played e4, Black played e5.",
            BrainUi.playedSentence(listOf("e4", "e5"), Position.START.after("e4", "e5")),
        )
    }

    @Test
    fun observedOccupancyTrustsThePositionWhereThePixelsDidNotChange() {
        val start = Position.START
        // At sync time the sampler misread c1 (cell 58) as a dark piece; Claude's reading won.
        val reference = snapshotOf(com.krstech.chesshelp.chess.BoardView.toRows(start, true), misread = mapOf(58 to CellState.DARK_PIECE))
        val tracked = Tracked(start, true, emptyList(), reference, sideForced = false, orientationForced = false)

        val after = start.after("e4")
        val now = snapshotOf(com.krstech.chesshelp.chess.BoardView.toRows(after, true), misread = mapOf(58 to CellState.DARK_PIECE))
        val observed = Tracking.observedOccupancy(tracked, now)
        assertTrue(observed.contentEquals(com.krstech.chesshelp.chess.MoveTracker.occupancyOf(after)))

        // Without the reference the misread cell shows through.
        val unanchored = Tracked(start, true, emptyList(), null, sideForced = false, orientationForced = false)
        assertFalse(Tracking.observedOccupancy(unanchored, now).contentEquals(com.krstech.chesshelp.chess.MoveTracker.occupancyOf(after)))
    }

    @Test
    fun flippingKeepsEveryPieceInItsViewCell() {
        val position = Position.START.after("e4")
        val tracked = Tracked(position, true, listOf("e4"), null, sideForced = true, orientationForced = false)
        val flipped = Tracking.flipped(tracked).tracked!!
        assertFalse(flipped.whiteAtBottom)
        assertTrue(flipped.orientationForced && flipped.sideForced)
        assertTrue(flipped.history.isEmpty())
        assertEquals(position.sideToMove, flipped.position.sideToMove)
        assertEquals(
            com.krstech.chesshelp.chess.BoardView.toRows(position, true),
            com.krstech.chesshelp.chess.BoardView.toRows(flipped.position, false),
        )
    }
}

class ChangeWatcherTest {
    private val watcher = ChangeWatcher(stableMs = 600, settleMs = 350) { a, b -> !a.contentEquals(b) }
    private fun fp(value: Int) = intArrayOf(value)
    private val none = ChangeWatcher.Signal.NONE
    private val settled = ChangeWatcher.Signal.SETTLED
    private val changed = ChangeWatcher.Signal.CHANGED

    @Test
    fun settlesOnTheFirstRepeatedFrameAfterTheSettleTime() {
        watcher.reset(1000)
        assertEquals(none, watcher.onFrame(fp(1), 1100))
        assertEquals(none, watcher.onFrame(fp(1), 1300))      // too early
        assertEquals(none, watcher.onFrame(fp(2), 1400))      // late enough, but the picture just changed
        assertEquals(settled, watcher.onFrame(fp(2), 1650))
        assertEquals(none, watcher.onFrame(fp(2), 1900))
    }

    @Test
    fun reportsAChangeOnceItHasHeldStill() {
        watcher.adopt(fp(1))
        assertEquals(none, watcher.onFrame(fp(2), 0))
        assertEquals(none, watcher.onFrame(fp(2), 250))
        assertEquals(none, watcher.onFrame(fp(2), 500))
        assertEquals(changed, watcher.onFrame(fp(2), 750))
    }

    @Test
    fun anAnimationKeepsRestartingTheWait() {
        watcher.adopt(fp(1))
        var time = 0L
        for (frame in 2..10) {
            assertEquals(none, watcher.onFrame(fp(frame), time))
            time += 250
        }
        assertEquals(none, watcher.onFrame(fp(10), time))
        assertEquals(none, watcher.onFrame(fp(10), time + 250))
        assertEquals(changed, watcher.onFrame(fp(10), time + 500))
    }

    @Test
    fun aFlickerBackToTheBaselineIsNotAChange() {
        watcher.adopt(fp(1))
        assertEquals(none, watcher.onFrame(fp(2), 0))
        assertEquals(none, watcher.onFrame(fp(2), 250))
        assertEquals(none, watcher.onFrame(fp(1), 500))
        assertEquals(none, watcher.onFrame(fp(1), 2000))
        assertEquals(none, watcher.onFrame(fp(2), 2250))
        assertEquals(none, watcher.onFrame(fp(2), 2500))
    }

    @Test
    fun adoptMakesTheChangedPictureTheNewBaseline() {
        watcher.adopt(fp(1))
        watcher.onFrame(fp(2), 0)
        assertEquals(changed, watcher.onFrame(fp(2), 600))
        watcher.adopt(fp(2))
        assertEquals(none, watcher.onFrame(fp(2), 850))
        assertEquals(none, watcher.onFrame(fp(2), 5000))
    }
}
