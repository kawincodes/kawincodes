package com.krstech.chesshelp.brain

import com.krstech.chesshelp.chess.Position
import com.krstech.chesshelp.claude.AdviceRequest
import com.krstech.chesshelp.claude.AnalysisResult
import com.krstech.chesshelp.claude.ClaudeException
import com.krstech.chesshelp.claude.PositionOracle
import com.krstech.chesshelp.vision.BoardRect
import com.krstech.chesshelp.vision.CellState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Auto-advise: the fingerprint watch, its rate limits, and the second look after an analysis. */
@OptIn(ExperimentalCoroutinesApi::class)
class AutoLoopTest {
    private val start = Position.START

    /** Recommends the first legal move of whatever position it is asked about. */
    private val anyMove: (AdviceRequest, PositionOracle, (String) -> Unit) -> AnalysisResult = { request, oracle, progress ->
        val position = Position.fromFen(request.fen)
        recommends(position.san(position.legalMoves().first()))(request, oracle, progress)
    }

    private fun TestScope.harness(tuning: BrainTuning = BrainTuning(), configure: Harness.() -> Unit = {}): Harness {
        val h = Harness(this, tuning)
        h.settings.autoAdvise = true
        h.configure()
        h.brain.start()
        runCurrent()
        return h
    }

    private fun TestScope.run(ms: Long) {
        advanceTimeBy(ms)
        runCurrent()
    }

    /** Start position on screen and read by Claude after a tap. */
    private fun TestScope.synced(tuning: BrainTuning = BrainTuning(), configure: Harness.() -> Unit = {}): Harness {
        val h = harness(tuning, configure)
        h.show(start)
        h.claude.vision = reads(start, move = "e4")
        h.brain.onAnalyze()
        run(2_000)
        assertEquals("e4", h.advice.moveSan)
        assertEquals(1, h.overlay.captures)
        return h
    }

    @Test
    fun doesNothingUntilTheUserHasTappedOnce() = runTest {
        val h = harness()
        assertEquals(true, h.overlay.auto)
        h.show(start)
        run(5_000)
        h.show(start.after("e4"))
        run(10_000)
        assertEquals(0, h.overlay.captures)
        assertEquals(0, h.frames.freshCalls)
        assertEquals(0, h.vision.locateCalls)
        assertEquals(0, h.claude.calls)
    }

    @Test
    fun runsOneCyclePerSettledChangeAndNoneForItsOwnDrawing() = runTest {
        val h = synced()
        // The panel and the arrow the app drew changed the picture, but that is not a board change.
        run(10_000)
        assertEquals(1, h.overlay.captures)

        h.show(start.after("e4"))
        run(3_000)
        assertEquals(2, h.overlay.captures)
        assertEquals("White played e4. Waiting for Black…", h.message.body)
        run(10_000)
        assertEquals(2, h.overlay.captures)

        h.claude.advice = recommends("Nf3")
        h.show(start.after("e4", "e5"))
        run(5_000)
        assertEquals(3, h.overlay.captures)
        assertEquals("Nf3", h.advice.moveSan)
        run(10_000)
        assertEquals(3, h.overlay.captures)
        assertEquals(2, h.claude.calls)
        assertEquals(0, h.overlay.captureModes.size % 2)
    }

    @Test
    fun waitsForTheBoardToHoldStill() = runTest {
        val h = synced()
        run(2_000)
        // A piece being dragged: a different picture at every poll.
        repeat(12) {
            h.show(start.after("e4"))
            run(250)
        }
        assertEquals(1, h.overlay.captures)
        run(300)
        assertEquals(1, h.overlay.captures)
        run(1_000)
        assertEquals(2, h.overlay.captures)
        assertIs<UiState.Message>(h.overlay.state)
    }

    @Test
    fun keepsTheMinimumGapBetweenClaudeCalls() = runTest {
        val h = harness { settings.adviseFor = AdviseFor.BOTH }
        h.show(start)
        h.claude.vision = reads(start, move = "d4")
        h.claude.advice = anyMove
        h.brain.onAnalyze()
        run(300)
        assertEquals(1, h.claude.calls)

        h.show(start.after("e4"))
        run(2_500)
        // The cycle has run and is waiting at the gate, not calling.
        assertEquals(2, h.overlay.captures)
        assertEquals(1, h.claude.calls)
        assertEquals(UiState.Working(BrainUi.ASKING, BrainUi.board(start.after("e4"), true)), h.overlay.state)
        run(1_500)
        assertEquals(2, h.claude.calls)

        h.show(start.after("e4", "e5"))
        run(8_000)
        assertEquals(3, h.claude.calls)
        val times = h.claude.callTimes
        assertTrue(times[1] - times[0] >= 3_000 && times[2] - times[1] >= 3_000, times.toString())
        assertTrue(times[1] - times[0] < 3_500 && times[2] - times[1] < 3_500, times.toString())
    }

    @Test
    fun theAdvisedMovePlayedBeforeTheNewBaselineIsStillNoticed() = runTest {
        val h = harness()
        h.show(start)
        h.claude.vision = reads(start, move = "e4")
        h.brain.onAnalyze()
        run(300)
        assertEquals("e4", h.advice.moveSan)
        // Played at once, under the arrow, while the watch is still settling on the app's own drawing.
        h.show(start.after("e4"))
        run(3_000)
        assertEquals(2, h.overlay.captures)
        assertEquals("White played e4. Waiting for Black…", h.message.body)
        assertEquals(1, h.claude.calls)
    }

    @Test
    fun pausesAfterTheCapOnAutomaticCallsUntilTheUserActs() = runTest {
        val h = synced(BrainTuning(maxAutoClaudeCalls = 2, minClaudeGapMs = 0)) { settings.adviseFor = AdviseFor.BOTH }
        h.claude.advice = anyMove
        val moves = listOf("e4", "e5", "Nf3", "Nc6", "Bb5")
        var position = start
        for (san in moves.take(2)) {
            position = position.after(san)
            h.show(position)
            run(3_000)
        }
        assertEquals(3, h.claude.calls)
        assertIs<UiState.Advice>(h.overlay.state)

        // The third automatic call in a row is refused.
        position = position.after("Nf3")
        h.show(position)
        run(3_000)
        assertEquals(3, h.claude.calls)
        assertEquals(BrainUi.PAUSED_TITLE, h.message.title)
        assertEquals(BrainUi.pausedBody(2), h.message.body)
        val captures = h.overlay.captures

        // Paused means paused: further moves are not even looked at.
        position = position.after("Nc6")
        h.show(position)
        run(10_000)
        assertEquals(captures, h.overlay.captures)
        assertEquals(3, h.claude.calls)

        // A tap is a user action: it is served, and auto-advise runs again afterwards.
        h.brain.onAnalyze()
        run(3_000)
        assertEquals(4, h.claude.calls)
        assertEquals(position.toFen(), h.claude.adviceRequests.last().fen)
        assertIs<UiState.Advice>(h.overlay.state)
        position = position.after("Bb5")
        h.show(position)
        run(3_000)
        assertEquals(5, h.claude.calls)
    }

    @Test
    fun aMoveMadeWhileClaudeWasAnsweringIsNoticedAfterwards() = runTest {
        val h = harness()
        h.show(start)
        val script = reads(start, move = "e4")
        h.claude.vision = { request, oracle, progress ->
            // The user did not wait for the advice.
            h.show(start.after("Nf3"))
            script(request, oracle, progress)
        }
        h.brain.onAnalyze()
        run(300)
        assertEquals("e4", h.advice.moveSan)
        assertEquals(1, h.overlay.captures)

        run(3_000)
        assertEquals(2, h.overlay.captures)
        assertEquals("White played Nf3. Waiting for Black…", h.message.body)
        assertNull(h.overlay.arrow)
        assertEquals(1, h.claude.calls)
        run(10_000)
        assertEquals(2, h.overlay.captures)
    }

    @Test
    fun theSecondLookNeverSpendsAClaudeCall() = runTest {
        val h = harness()
        h.show(start)
        val script = reads(start, move = "e4")
        val elsewhere = start.after("d4", "d5", "c4", "e6", "Nc3", "Nf6")
        h.claude.vision = { request, oracle, progress ->
            h.show(elsewhere)
            script(request, oracle, progress)
        }
        h.brain.onAnalyze()
        run(10_000)
        // The screen no longer shows what Claude read, and two moves cannot explain it: the second look
        // leaves it at that.
        assertEquals(2, h.overlay.captures)
        assertEquals(1, h.claude.calls)
        assertEquals("e4", h.advice.moveSan)

        // The next change on the board is a real trigger and gets a fresh reading.
        val next = elsewhere.after("Nf3")
        h.claude.vision = reads(next, move = "Be7")
        h.show(next)
        run(5_000)
        assertEquals(3, h.overlay.captures)
        assertEquals(2, h.claude.visionRequests.size)
        assertEquals("Be7", h.advice.moveSan)
    }

    @Test
    fun aWindowOverTheBoardCostsOneExtraLookAndIsThenKnown() = runTest {
        val h = harness()
        // With the overlay on screen, the top-left square reads as empty (the bubble sits there).
        h.vision.covered = mapOf(0 to CellState.EMPTY)
        h.show(start)
        h.claude.vision = reads(start, move = "e4")
        h.brain.onAnalyze()
        run(5_000)
        assertEquals(2, h.overlay.captures)
        assertEquals("e4", h.advice.moveSan)
        assertEquals(1, h.claude.calls)
        run(10_000)
        assertEquals(2, h.overlay.captures)

        h.show(start.after("e4"))
        run(10_000)
        assertEquals(3, h.overlay.captures)
        assertIs<UiState.Message>(h.overlay.state)
        assertEquals(1, h.claude.calls)
    }

    @Test
    fun whileTheBoardIsAwayTheOverlayIsNotHiddenAgain() = runTest {
        val h = synced()
        val advice = h.advice
        run(2_000)
        h.frames.scene = Scene(null)
        run(3_000)
        assertEquals(2, h.overlay.captures)
        assertEquals(BrainUi.NOT_VISIBLE_TITLE, h.message.title)
        assertNull(h.overlay.arrow)

        // Scrolling around in another app.
        val locates = h.vision.locateCalls
        repeat(4) {
            h.frames.scene = Scene(null)
            run(2_000)
        }
        assertEquals(2, h.overlay.captures)
        assertTrue(h.vision.locateCalls >= locates + 4)

        // Back in the game: the advice returns without a Claude call.
        h.show(start)
        run(3_000)
        assertEquals(3, h.overlay.captures)
        assertEquals(advice, h.overlay.state)
        assertEquals(ArrowSpec(4, 6, 16, 6, 4, 4, 4), h.overlay.arrow)
        assertEquals(1, h.claude.calls)
    }

    @Test
    fun switchingAutoOffStopsTheWatchAndSwitchingItOnCatchesUp() = runTest {
        val h = synced()
        h.brain.onAutoChanged(false)
        run(1_000)
        assertEquals(false, h.settings.autoAdvise)
        assertEquals(false, h.overlay.auto)
        h.show(start.after("e4"))
        run(10_000)
        assertEquals(1, h.overlay.captures)

        h.brain.onAutoChanged(true)
        run(3_000)
        assertEquals(true, h.settings.autoAdvise)
        assertEquals(true, h.overlay.auto)
        assertEquals(2, h.overlay.captures)
        assertEquals("White played e4. Waiting for Black…", h.message.body)
        run(10_000)
        assertEquals(2, h.overlay.captures)
    }

    @Test
    fun afterAScreenChangeTheBoardIsLookedForOnce() = runTest {
        val h = synced()
        run(2_000)
        val moved = BoardRect(10, 2, 20, 0.9f)
        h.frames.scene = Scene(com.krstech.chesshelp.chess.BoardView.toRows(start, true), rect = moved)
        h.brain.onScreenChanged()
        run(200)
        assertNull(h.overlay.arrow)
        run(3_000)
        assertEquals(2, h.overlay.captures)
        assertEquals(ArrowSpec(10, 2, 20, 6, 4, 4, 4), h.overlay.arrow)
        assertEquals(1, h.claude.calls)
        run(10_000)
        assertEquals(2, h.overlay.captures)
    }

    @Test
    fun withoutAKeyAndWithoutAPositionThereIsNothingToWatchFor() = runTest {
        val h = harness { settings.apiKey = null }
        h.show(start)
        h.brain.onAnalyze()
        run(2_000)
        assertEquals(BrainUi.NO_KEY_TITLE, h.message.title)
        h.show(start.after("e4"))
        run(10_000)
        assertEquals(1, h.overlay.captures)
    }

    @Test
    fun aKnownBoardWithoutAPositionIsReadWhenItChanges() = runTest {
        val h = harness()
        h.show(start)
        h.claude.vision = { _, _, _ -> throw ClaudeException.Network("down") }
        h.brain.onAnalyze()
        run(2_000)
        assertEquals(BrainUi.CLAUDE_FAILED_TITLE, h.message.title)

        val next = start.after("e4")
        h.claude.vision = reads(next, move = "e5")
        h.show(next)
        run(5_000)
        assertEquals(2, h.claude.visionRequests.size)
        assertEquals("e5", h.advice.moveSan)
        assertEquals(2, h.overlay.captures)
    }

    @Test
    fun automaticCyclesNeverSendABareScreenshot() = runTest {
        val h = harness()
        h.show(start)
        h.claude.vision = { _, _, _ -> throw ClaudeException.Network("down") }
        h.brain.onAnalyze()
        run(2_000)
        // The board disappears before anything was read: nothing is tracked, nothing is sent.
        h.frames.scene = Scene(null)
        run(10_000)
        assertEquals(1, h.claude.calls)
        assertEquals(2, h.overlay.captures)
        assertTrue(h.encoder.screens.all { it == 1024 })
    }
}
