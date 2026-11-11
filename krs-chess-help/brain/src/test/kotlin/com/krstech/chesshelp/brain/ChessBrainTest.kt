package com.krstech.chesshelp.brain

import com.krstech.chesshelp.chess.BoardView
import com.krstech.chesshelp.chess.Position
import com.krstech.chesshelp.claude.AnalysisResult
import com.krstech.chesshelp.claude.ClaudeException
import com.krstech.chesshelp.claude.Evaluation
import com.krstech.chesshelp.claude.MoveAdvice
import com.krstech.chesshelp.claude.MoveVerdict
import com.krstech.chesshelp.vision.CellState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The analysis flow with auto-advise off: every cycle is started by a user action. */
@OptIn(ExperimentalCoroutinesApi::class)
class ChessBrainTest {
    private val start = Position.START

    private fun TestScope.harness(configure: Harness.() -> Unit = {}): Harness {
        val h = Harness(this)
        h.configure()
        h.brain.start()
        runCurrent()
        return h
    }

    /** Lets everything that is queued or delayed run; with auto off nothing is periodic. */
    private fun TestScope.settle() {
        advanceTimeBy(10_000)
        runCurrent()
    }

    private fun TestScope.tap(h: Harness) {
        h.brain.onAnalyze()
        settle()
    }

    /** Start position on screen, read by Claude, which recommends [move]. */
    private fun TestScope.synced(move: String = "e4", configure: Harness.() -> Unit = {}): Harness {
        val h = harness(configure)
        h.show(start)
        h.claude.vision = reads(start, move = move)
        tap(h)
        return h
    }

    private fun captureRestored(h: Harness) {
        assertEquals(0, h.overlay.captureModes.size % 2, h.overlay.captureModes.toString())
        h.overlay.captureModes.forEachIndexed { index, hidden -> assertEquals(index % 2 == 0, hidden) }
        assertFalse(h.overlay.hidden)
    }

    @Test
    fun startRendersIdleAndReportsTheAutoSetting() = runTest {
        val h = harness()
        assertEquals(UiState.Idle(BrainUi.IDLE_HINT), h.overlay.state)
        assertEquals(false, h.overlay.auto)
        assertEquals(0, h.overlay.captures)

        val noKey = harness { settings.apiKey = " " }
        assertEquals(UiState.Idle(BrainUi.IDLE_HINT_NO_KEY), noKey.overlay.state)
    }

    @Test
    fun visionSyncRendersAdviceAndArrow() = runTest {
        val h = synced()

        val request = h.claude.visionRequests.single()
        assertNotNull(request.boardImage)
        assertNotNull(request.screenImage)
        assertEquals(listOf(1024), h.encoder.screens)
        assertEquals(listOf(BOARD_RECT), h.encoder.boards)
        assertEquals(snapshotOf(BoardView.toRows(start, true)).toAscii(), request.occupancyHint)
        assertNull(request.previousFen)
        assertNull(request.forcedSideToMove)
        assertEquals("sk-test", h.claude.configs.single().apiKey)
        assertEquals("claude-opus-5-5", h.claude.configs.single().model)

        val advice = h.advice
        assertEquals("White to move", advice.title)
        assertEquals("e4", advice.moveSan)
        assertEquals("Better", advice.evaluation)
        assertEquals("Because it is good.", advice.explanation)
        assertEquals("A threat", advice.threat)
        assertEquals("Claude Opus 5.5", advice.source)
        assertEquals("1.2k in / 0.3k out", advice.footer)
        assertEquals(BoardUi(BoardView.toRows(start, true), 6, 4, 4, 4), advice.board)
        assertEquals(ArrowSpec(40, 600, 1000, 6, 4, 4, 4), h.overlay.arrow)

        assertEquals(UiState.Working(BrainUi.LOOKING, null), h.overlay.states[1])
        assertTrue(UiState.Working(BrainUi.READING, null) in h.overlay.states)
        assertEquals(listOf(true, false), h.overlay.captureModes)
        assertEquals(1, h.frames.freshCalls)
    }

    @Test
    fun progressTextsBecomeWorkingStates() = runTest {
        val h = harness()
        h.show(start)
        val script = reads(start, move = "e4")
        h.claude.vision = { request, oracle, progress ->
            progress("Reading the board…")
            progress("Choosing a move…")
            script(request, oracle, progress)
        }
        tap(h)
        val messages = h.overlay.states.filterIsInstance<UiState.Working>().map { it.message }
        assertEquals(listOf(BrainUi.LOOKING, BrainUi.READING, "Reading the board…", "Choosing a move…"), messages)
        assertIs<UiState.Advice>(h.overlay.state)
    }

    @Test
    fun screenContextCanBeSwitchedOff() = runTest {
        val h = synced { settings.sendScreenContext = false }
        assertNull(h.claude.visionRequests.single().screenImage)
        assertNotNull(h.claude.visionRequests.single().boardImage)
        assertTrue(h.encoder.screens.isEmpty())
    }

    @Test
    fun noArrowWhenDrawingIsOffOrFramesAreNotScreenPixels() = runTest {
        val off = synced { settings.drawOnBoard = false }
        assertIs<UiState.Advice>(off.overlay.state)
        assertTrue(off.overlay.arrows.all { it == null })

        val scaled = synced { frames.framesMatchScreen = false }
        assertIs<UiState.Advice>(scaled.overlay.state)
        assertTrue(scaled.overlay.arrows.all { it == null })
    }

    @Test
    fun boardNotLocatedSendsTheScreenshotAloneWhenAllowed() = runTest {
        val h = harness()
        h.frames.scene = Scene(null)
        h.claude.vision = reads(start, move = "e4")
        tap(h)

        val request = h.claude.visionRequests.single()
        assertNull(request.boardImage)
        assertNotNull(request.screenImage)
        assertNull(request.occupancyHint)
        assertEquals(listOf(1568), h.encoder.screens)
        assertEquals("e4", h.advice.moveSan)
        // No rectangle is known, so nothing can be drawn over the board.
        assertNull(h.overlay.arrow)

        // The position cannot be followed locally, so the next tap asks Claude again.
        tap(h)
        assertEquals(2, h.claude.visionRequests.size)
    }

    @Test
    fun boardNotLocatedWithoutScreenContextTellsTheUser() = runTest {
        val h = harness { settings.sendScreenContext = false }
        h.frames.scene = Scene(null)
        tap(h)
        assertEquals(BrainUi.NOT_FOUND_TITLE, h.message.title)
        assertEquals(0, h.claude.calls)
        captureRestored(h)
    }

    @Test
    fun noFrameYetTellsTheUser() = runTest {
        val h = harness()
        tap(h)
        assertEquals(BrainUi.NO_FRAME_TITLE, h.message.title)
        captureRestored(h)
    }

    @Test
    fun claudeSeeingNoBoardShowsItsCommentary() = runTest {
        val h = harness()
        h.show(start)
        h.claude.vision = { _, _, _ -> AnalysisResult(null, null, null, "I can't see a chess board on this screen.", USAGE, "claude-opus-5-5", 1) }
        tap(h)
        assertEquals("I can't see a chess board on this screen.", h.message.body)
        assertFalse(h.message.isError)
        assertNull(h.overlay.arrow)
    }

    @Test
    fun acceptedPositionWithoutARecommendationFallsBackToTheEngine() = runTest {
        val h = harness()
        h.show(start)
        h.claude.vision = reads(start, move = null)
        tap(h)
        val advice = h.advice
        assertEquals(BrainUi.LOCAL_ENGINE, advice.source)
        assertEquals(start.san(start.legalMoves().first()), advice.moveSan)
        assertTrue(advice.explanation.startsWith("No move this time." + BrainUi.ENGINE_INSTEAD), advice.explanation)
        assertNotNull(h.overlay.arrow)
    }

    @Test
    fun checkmateOnScreenIsShownAsAMessage() = runTest {
        val mated = start.after("f3", "e5", "g4", "Qh4#")
        val h = harness()
        h.show(mated)
        h.claude.vision = reads(mated, move = null)
        tap(h)
        assertEquals("Checkmate", h.message.title)
        assertTrue(h.message.body.contains("Black has won"), h.message.body)
        assertEquals(BoardView.toRows(mated, true), h.message.board!!.rows)
        assertNull(h.overlay.arrow)
    }

    @Test
    fun trackingNoChangeKeepsWhatIsShown() = runTest {
        val h = synced()
        val shown = h.overlay.state
        val arrow = h.overlay.arrow
        tap(h)
        assertEquals(1, h.claude.calls)
        assertEquals(shown, h.overlay.state)
        assertEquals(arrow, h.overlay.arrow)
        assertEquals(2, h.overlay.captures)
        captureRestored(h)
    }

    @Test
    fun ownMoveIsFollowedWithoutCallingClaude() = runTest {
        val h = synced()
        h.show(start.after("e4"))
        tap(h)
        assertEquals(1, h.claude.calls)
        val message = h.message
        assertEquals("Black to move", message.title)
        assertEquals("White played e4. Waiting for Black…", message.body)
        assertEquals(BoardUi(BoardView.toRows(start.after("e4"), true), 6, 4, 4, 4), message.board)
        assertNull(h.overlay.arrow)
    }

    @Test
    fun opponentMoveAsksClaudeForAdviceOnTheTextPath() = runTest {
        val h = synced()
        h.show(start.after("e4"))
        tap(h)
        h.show(start.after("e4", "e5"))
        h.claude.advice = recommends("Nf3")
        tap(h)

        assertEquals(1, h.claude.visionRequests.size)
        val request = h.claude.adviceRequests.single()
        assertEquals(start.after("e4", "e5").toFen(), request.fen)
        assertEquals(listOf("e4", "e5"), request.history)
        assertTrue(request.briefing.contains("Legal moves:") && request.briefing.lines().any { it.startsWith("1. ") })
        val advice = h.advice
        assertEquals("Nf3", advice.moveSan)
        assertEquals("Equal", advice.evaluation)
        assertNull(advice.threat)
        assertEquals(ArrowSpec(40, 600, 1000, 7, 6, 5, 5), h.overlay.arrow)
        assertTrue(UiState.Working(BrainUi.ASKING, BrainUi.board(start.after("e4", "e5"), true)) in h.overlay.states)
    }

    @Test
    fun twoPliesSinceTheLastLookAreFollowedTogether() = runTest {
        val h = synced()
        h.show(start.after("e4", "c5"))
        h.claude.advice = recommends("Nf3")
        tap(h)
        assertEquals(listOf("e4", "c5"), h.claude.adviceRequests.single().history)
        assertEquals(1, h.claude.visionRequests.size)
    }

    @Test
    fun aPositionThatCannotBeExplainedIsReadAgain() = runTest {
        val h = synced()
        val later = start.after("e4", "e5", "Nf3", "Nc6", "Bb5", "a6")
        h.show(later)
        h.claude.vision = reads(later, move = "Ba4")
        tap(h)
        assertEquals(2, h.claude.visionRequests.size)
        assertEquals(start.toFen(), h.claude.visionRequests[1].previousFen)
        assertEquals("Ba4", h.advice.moveSan)
        assertTrue(h.claude.adviceRequests.isEmpty())
    }

    @Test
    fun anUnreliableSnapshotIsReadAgainWithoutHintOrCrossCheck() = runTest {
        val h = synced()
        val next = start.after("e4")
        h.frames.scene = Scene(BoardView.toRows(next, true), reliable = false)
        h.claude.vision = reads(next, move = "e5")
        tap(h)
        assertEquals(2, h.claude.visionRequests.size)
        assertNull(h.claude.visionRequests[1].occupancyHint)
        assertNotNull(h.claude.visionRequests[1].boardImage)
        assertEquals("e5", h.advice.moveSan)
    }

    @Test
    fun aPromotionIsReadAgainBecausePixelsCannotTellThePiece() = runTest {
        val before = Position.fromFen("7k/P7/8/8/8/8/8/K7 w - - 0 1")
        val after = before.after("a8=N")
        val h = harness { settings.adviseFor = AdviseFor.BOTH }
        h.show(before)
        h.claude.vision = reads(before, move = "a8=Q+")
        tap(h)
        h.show(after)
        h.claude.vision = reads(after, move = "Kg7")
        tap(h)
        assertEquals(2, h.claude.visionRequests.size)
        assertEquals(before.toFen(), h.claude.visionRequests[1].previousFen)
        assertEquals("Kg7", h.advice.moveSan)
    }

    @Test
    fun boardNotVisibleKeepsTheTrackedState() = runTest {
        val h = synced()
        val advice = h.advice
        h.frames.scene = Scene(null)
        tap(h)
        assertEquals(BrainUi.NOT_VISIBLE_TITLE, h.message.title)
        assertEquals(BoardView.toRows(start, true), h.message.board!!.rows)
        assertNull(h.overlay.arrow)
        assertEquals(1, h.claude.calls)

        // The board comes back unchanged: the advice returns without anyone being asked.
        h.show(start)
        tap(h)
        assertEquals(advice, h.overlay.state)
        assertEquals(ArrowSpec(40, 600, 1000, 6, 4, 4, 4), h.overlay.arrow)
        assertEquals(1, h.claude.calls)
    }

    @Test
    fun trackingSurvivesASquareWhereClaudeOverruledThePixels() = runTest {
        // The sampler takes White's c1 bishop (cell 58) for a dark piece, now and later.
        val misread = mapOf(58 to CellState.DARK_PIECE)
        val h = harness()
        h.frames.scene = sceneOf(start, misread = misread)
        h.claude.vision = reads(start, move = "e4")
        tap(h)
        assertEquals("e4", h.advice.moveSan)

        h.frames.scene = sceneOf(start.after("e4", "e5"), misread = misread)
        h.claude.advice = recommends("Nf3")
        tap(h)
        assertEquals(1, h.claude.visionRequests.size)
        assertEquals(start.after("e4", "e5").toFen(), h.claude.adviceRequests.single().fen)
    }

    @Test
    fun adviseForBottomFollowsTheOrientation() = runTest {
        // White at the bottom: White's turn is advised, Black's is not.
        val white = synced()
        white.show(start.after("e4"))
        tap(white)
        assertIs<UiState.Message>(white.overlay.state)
        assertTrue(white.claude.adviceRequests.isEmpty())

        // Black at the bottom: after White's move it is the user's turn.
        val black = harness()
        black.show(start, whiteAtBottom = false)
        black.claude.vision = reads(start, whiteAtBottom = false, move = "e4")
        tap(black)
        assertEquals(ArrowSpec(40, 600, 1000, 1, 3, 3, 3), black.overlay.arrow)
        black.show(start.after("e4"), whiteAtBottom = false)
        black.claude.advice = recommends("c5")
        tap(black)
        assertEquals("c5", black.advice.moveSan)
        assertEquals("Black to move", black.advice.title)
        black.show(start.after("e4", "c5"), whiteAtBottom = false)
        tap(black)
        assertEquals("Black played c5. Waiting for White…", black.message.body)
        assertEquals(1, black.claude.adviceRequests.size)
    }

    @Test
    fun adviseForAFixedColourOrBoth() = runTest {
        fun adviceCallsAfterWhitesMove(setting: AdviseFor, whiteAtBottom: Boolean): Int {
            val h = harness { settings.adviseFor = setting }
            h.show(start, whiteAtBottom)
            h.claude.vision = reads(start, whiteAtBottom, move = "e4")
            tap(h)
            h.show(start.after("e4"), whiteAtBottom)
            h.claude.advice = recommends("e5")
            tap(h)
            return h.claude.adviceRequests.size
        }
        for (whiteAtBottom in listOf(true, false)) {
            assertEquals(0, adviceCallsAfterWhitesMove(AdviseFor.WHITE, whiteAtBottom))
            assertEquals(1, adviceCallsAfterWhitesMove(AdviseFor.BLACK, whiteAtBottom))
            assertEquals(1, adviceCallsAfterWhitesMove(AdviseFor.BOTH, whiteAtBottom))
        }
    }

    @Test
    fun claudeFailureOnTheTextPathShowsTheEngineMoveAndATapRetries() = runTest {
        val h = synced()
        val position = start.after("e4", "e5")
        h.show(position)
        h.claude.advice = { _, _, _ -> throw ClaudeException.Overloaded("529") }
        tap(h)

        val fallback = h.advice
        assertEquals(BrainUi.LOCAL_ENGINE, fallback.source)
        assertEquals(position.san(position.legalMoves().first()), fallback.moveSan)
        assertTrue(fallback.explanation.startsWith("Claude is overloaded right now. Try again shortly." + BrainUi.ENGINE_INSTEAD), fallback.explanation)
        assertNotNull(h.overlay.arrow)

        h.claude.advice = recommends("Nf3")
        tap(h)
        assertEquals(2, h.claude.adviceRequests.size)
        assertEquals("Nf3", h.advice.moveSan)
        assertEquals("Claude Opus 5.5", h.advice.source)
    }

    @Test
    fun claudeFailureWhileReadingLeavesNoPositionForTheEngine() = runTest {
        val h = harness()
        h.show(start)
        h.claude.vision = { _, _, _ -> throw ClaudeException.Network("down") }
        tap(h)
        val message = h.message
        assertTrue(message.isError)
        assertEquals(BrainUi.CLAUDE_FAILED_TITLE, message.title)
        assertTrue(message.body.startsWith("Can't reach Claude. Check your internet connection."), message.body)
        assertTrue(message.body.contains("needs a known position"))
        captureRestored(h)

        // Nothing is tracked, so the next tap reads the board again.
        h.claude.vision = reads(start, move = "e4")
        tap(h)
        assertEquals(2, h.claude.visionRequests.size)
        assertEquals("e4", h.advice.moveSan)
    }

    @Test
    fun claudeFailureAfterTheReadingWasAcceptedKeepsThePosition() = runTest {
        val h = harness()
        h.show(start)
        val script = reads(start, move = "e4")
        h.claude.vision = { request, oracle, progress ->
            script(request, oracle, progress)
            throw ClaudeException.RateLimited("429")
        }
        tap(h)
        assertEquals(BrainUi.LOCAL_ENGINE, h.advice.source)
        assertTrue(h.advice.explanation.startsWith("Rate limit reached."), h.advice.explanation)

        // The position is tracked: the retry goes down the cheap text path.
        h.claude.advice = recommends("d4")
        tap(h)
        assertEquals(1, h.claude.visionRequests.size)
        assertEquals("d4", h.advice.moveSan)
    }

    @Test
    fun failedRescanOfAnUnchangedBoardStillShowsTheEngineMove() = runTest {
        val h = synced()
        h.claude.vision = { _, _, _ -> throw ClaudeException.Truncated() }
        h.brain.onRescan()
        settle()
        assertEquals(2, h.claude.visionRequests.size)
        assertEquals(BrainUi.LOCAL_ENGINE, h.advice.source)
        assertEquals("White to move", h.advice.title)
    }

    @Test
    fun unexpectedFailuresAreShownNotThrown() = runTest {
        val h = harness()
        h.show(start)
        h.claude.vision = { _, _, _ -> throw IllegalStateException("ClaudeChessClient is closed") }
        tap(h)
        assertEquals(BrainUi.FAILURE_TITLE, h.message.title)
        assertEquals("ClaudeChessClient is closed", h.message.body)
        captureRestored(h)
    }

    @Test
    fun withoutAnApiKeyTheBoardCannotBeRead() = runTest {
        val h = harness { settings.apiKey = null }
        h.show(start)
        tap(h)
        assertEquals(BrainUi.NO_KEY_TITLE, h.message.title)
        assertEquals(BrainUi.NO_KEY_BODY, h.message.body)
        assertTrue(h.message.isError)
        assertTrue(h.claude.configs.isEmpty())
        captureRestored(h)
    }

    @Test
    fun withoutAnApiKeyATrackedPositionGetsTheLocalEngine() = runTest {
        val h = synced()
        h.settings.apiKey = ""
        val position = start.after("e4", "e5")
        h.show(position)
        tap(h)
        assertEquals(1, h.claude.calls)
        val advice = h.advice
        assertEquals(BrainUi.LOCAL_ENGINE, advice.source)
        assertEquals(position.san(position.legalMoves().first()), advice.moveSan)
        assertTrue(advice.explanation.startsWith(BrainUi.NO_KEY_NOTE), advice.explanation)
        assertNotNull(h.overlay.arrow)

        // Losing the thread without a key cannot be repaired; the position is kept.
        h.show(start.after("d4", "d5", "c4", "c6", "Nc3", "Nf6"))
        tap(h)
        assertEquals(BrainUi.NO_KEY_LOST_BODY, h.message.body)
        assertEquals(1, h.claude.calls)
        h.show(position)
        tap(h)
        assertEquals(BrainUi.LOCAL_ENGINE, h.advice.source)

        // With a key again, a tap asks Claude.
        h.settings.apiKey = "sk-test"
        h.claude.advice = recommends("Nf3")
        tap(h)
        assertEquals("Nf3", h.advice.moveSan)
    }

    @Test
    fun rescanForcesAFreshReadingAndKeepsUserCorrections() = runTest {
        val h = synced()
        h.brain.onToggleSideToMove()
        h.claude.advice = recommends("e5")
        settle()
        assertEquals("Black to move", h.advice.title)

        h.claude.vision = reads(start, move = "e5")
        h.brain.onRescan()
        settle()
        assertEquals(2, h.claude.visionRequests.size)
        val request = h.claude.visionRequests[1]
        assertEquals('b', request.forcedSideToMove)
        assertEquals(start.withSideToMove(com.krstech.chesshelp.chess.Side.BLACK).toFen(), request.previousFen)
        assertEquals("Black to move", h.advice.title)
        assertEquals("e5", h.advice.moveSan)
    }

    @Test
    fun toggleSideToMoveReAdvisesOnTheTextPath() = runTest {
        val h = synced()
        h.claude.advice = recommends("e5")
        h.brain.onToggleSideToMove()
        settle()
        val request = h.claude.adviceRequests.single()
        assertTrue(request.fen.contains(" b "), request.fen)
        assertEquals("Black to move", h.advice.title)
        assertEquals("e5", h.advice.moveSan)
        // Explicitly asked for, so no capture was needed.
        assertEquals(1, h.overlay.captures)
    }

    @Test
    fun toggleSideToMoveRefusesAnImpossibleSide() = runTest {
        val check = start.after("e4", "f5", "Qh5+")
        val h = harness { settings.adviseFor = AdviseFor.BOTH }
        h.show(check)
        h.claude.vision = reads(check, move = "g6")
        tap(h)
        h.brain.onToggleSideToMove()
        settle()
        assertEquals(BrainUi.CANNOT_SWITCH_TITLE, h.message.title)
        assertTrue(h.message.isError)
        assertTrue(h.claude.adviceRequests.isEmpty())

        // The tracked position is untouched: a tap brings the advice back.
        tap(h)
        assertEquals("g6", h.advice.moveSan)
        assertEquals(1, h.claude.calls)
    }

    @Test
    fun toggleAndFlipWithoutAPositionExplain() = runTest {
        val h = harness()
        h.brain.onToggleSideToMove()
        settle()
        assertEquals(BrainUi.NOTHING_TRACKED_TITLE, h.message.title)
        h.brain.onFlipBoard()
        settle()
        assertEquals(BrainUi.NOTHING_TRACKED_TITLE, h.message.title)
        assertEquals(0, h.overlay.captures)
    }

    @Test
    fun flipBoardRebuildsThePositionFromTheSamePicture() = runTest {
        // Black is at the bottom, but the first reading said White.
        val real = start.after("e4")
        val picture = BoardView.toRows(real, false)
        val h = harness()
        h.frames.scene = Scene(picture)
        h.claude.vision = reads(picture, whiteAtBottom = true, side = 'b', move = null)
        tap(h)

        h.claude.advice = recommends("c5")
        h.brain.onFlipBoard()
        settle()
        val request = h.claude.adviceRequests.single()
        assertEquals(real.placement(), request.fen.substringBefore(' '))
        assertTrue(request.history.isEmpty())
        val advice = h.advice
        assertEquals("c5", advice.moveSan)
        assertEquals(picture, advice.board!!.rows)
        // c7 -> c5 seen from Black's side: file c is column 5, rank 7 is row 6.
        assertEquals(ArrowSpec(40, 600, 1000, 6, 5, 4, 5), h.overlay.arrow)

        // Tracking continues in the flipped orientation, and a rescan keeps it.
        h.frames.scene = Scene(BoardView.toRows(real.after("c5"), false))
        tap(h)
        assertEquals("Black played c5. Waiting for White…", h.message.body)
        h.claude.vision = reads(BoardView.toRows(real.after("c5"), false), whiteAtBottom = true, side = 'w', move = "Nf3")
        h.brain.onRescan()
        settle()
        assertEquals("Nf3", h.advice.moveSan)
        assertEquals(BoardView.toRows(real.after("c5"), false), h.advice.board!!.rows)
        assertEquals(real.after("c5").placement(), h.claude.visionRequests.last().previousFen!!.substringBefore(' '))
    }

    @Test
    fun captureModeIsRestoredWhenTheCaptureFails() = runTest {
        val h = harness()
        h.show(start)
        h.frames.freshFailure = IllegalStateException("projection stopped")
        tap(h)
        assertEquals(listOf(true, false), h.overlay.captureModes)
        assertEquals(BrainUi.FAILURE_TITLE, h.message.title)
        assertEquals("projection stopped", h.message.body)
    }

    @Test
    fun captureModeIsRestoredWhenTheAnalysisIsCancelledMidCapture() = runTest {
        val h = harness()
        h.show(start)
        h.frames.freshDelayMs = 200
        h.claude.vision = reads(start, move = "e4")

        h.brain.onAnalyze()
        advanceTimeBy(100)
        runCurrent()
        assertTrue(h.overlay.hidden)
        h.brain.onRescan()                       // cancels the capture in flight and starts over
        runCurrent()
        assertEquals(listOf(true, false, true), h.overlay.captureModes)
        settle()
        assertEquals(listOf(true, false, true, false), h.overlay.captureModes)
        assertEquals(1, h.claude.calls)
        assertEquals("e4", h.advice.moveSan)

        h.brain.onRescan()
        advanceTimeBy(100)
        runCurrent()
        assertTrue(h.overlay.hidden)
        h.brain.stop()
        runCurrent()
        assertFalse(h.overlay.hidden)
        captureRestored(h)
        val states = h.overlay.states.size
        settle()
        assertEquals(states, h.overlay.states.size)
        assertEquals(1, h.claude.calls)
    }

    @Test
    fun aSecondTapDuringCaptureIsIgnored() = runTest {
        val h = harness()
        h.show(start)
        h.frames.freshDelayMs = 200
        h.claude.vision = reads(start, move = "e4")
        h.brain.onAnalyze()
        advanceTimeBy(100)
        runCurrent()
        h.brain.onAnalyze()
        settle()
        assertEquals(1, h.overlay.captures)
        assertEquals(1, h.claude.calls)
    }

    @Test
    fun userCallsKeepTheMinimumGapToo() = runTest {
        val h = harness()
        h.show(start)
        h.claude.vision = reads(start, move = "e4")
        h.brain.onAnalyze()
        advanceTimeBy(500)
        runCurrent()
        assertEquals(1, h.claude.calls)
        h.brain.onRescan()
        advanceTimeBy(500)
        runCurrent()
        assertEquals(1, h.claude.calls)
        assertEquals(UiState.Working(BrainUi.READING, BrainUi.board(start, true)), h.overlay.state)
        settle()
        assertEquals(2, h.claude.calls)
        assertTrue(h.claude.callTimes[1] - h.claude.callTimes[0] >= 3000, h.claude.callTimes.toString())
    }

    @Test
    fun aChangedSettingReplacesTheClientAndStopClosesIt() = runTest {
        val h = synced()
        h.settings.model = "claude-haiku-5-5"
        h.settings.baseUrl = " https://gateway.example/ "
        h.claude.vision = reads(start, move = "d4")
        h.brain.onRescan()
        settle()
        assertEquals(2, h.claude.configs.size)
        assertEquals("claude-haiku-5-5", h.claude.configs[1].model)
        assertEquals("https://gateway.example/", h.claude.configs[1].baseUrl)
        assertEquals(1, h.claude.closed)

        h.brain.stop()
        assertEquals(2, h.claude.closed)
        h.brain.onAnalyze()
        settle()
        assertEquals(2, h.claude.calls)
    }

    @Test
    fun onScreenChangedForgetsTheRectangleAndTheArrow() = runTest {
        val h = synced()
        assertNotNull(h.overlay.arrow)
        h.brain.onScreenChanged()
        settle()
        assertNull(h.overlay.arrow)
        assertEquals(1, h.overlay.captures)

        // The next look finds the board where it is now.
        val moved = com.krstech.chesshelp.vision.BoardRect(500, 100, 640, 0.9f)
        h.frames.scene = Scene(BoardView.toRows(start, true), rect = moved)
        tap(h)
        assertEquals(ArrowSpec(500, 100, 640, 6, 4, 4, 4), h.overlay.arrow)
        assertEquals(1, h.claude.calls)
    }

    @Test
    fun illegalAdviceFromTheClientIsNotShown() = runTest {
        val h = harness()
        h.show(start)
        val script = reads(start, move = "e4")
        h.claude.vision = { request, oracle, progress ->
            script(request, oracle, progress).copy(
                advice = MoveAdvice("Qh5", "d1h5", "Nonsense.", Evaluation.WINNING, emptyList(), null),
            )
        }
        tap(h)
        assertEquals(BrainUi.LOCAL_ENGINE, h.advice.source)
        assertTrue((h.overlay.state as UiState.Advice).moveSan != "Qh5")
    }

    @Test
    fun alternativesAreNormalisedAndIllegalOnesDropped() = runTest {
        val h = harness()
        h.show(start)
        h.claude.vision = { request, oracle, progress ->
            val result = reads(start, move = "e4")(request, oracle, progress)
            val legal = oracle.checkMove(result.fen!!, "g1f3") as MoveVerdict.Legal
            result.copy(
                advice = result.advice!!.copy(
                    alternatives = listOf(
                        com.krstech.chesshelp.claude.Alternative(legal.uci, "Flexible."),
                        com.krstech.chesshelp.claude.Alternative("Qh5", "Not possible."),
                    ),
                ),
            )
        }
        tap(h)
        assertEquals(listOf("Nf3" to "Flexible."), h.advice.alternatives)
    }
}
