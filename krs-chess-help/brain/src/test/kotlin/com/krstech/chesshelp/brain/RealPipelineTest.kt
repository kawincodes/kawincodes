package com.krstech.chesshelp.brain

import com.krstech.chesshelp.chess.BoardView
import com.krstech.chesshelp.chess.Position
import com.krstech.chesshelp.vision.BoardRect
import com.krstech.chesshelp.vision.Frame
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The brain with its default collaborators: the real locator, sampler and fingerprint of :vision on
 * drawn screenshots, and the real engine of :core. Only the overlay, the encoder and Claude are fakes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RealPipelineTest {
    private val start = Position.START

    private class DrawnFrames : FrameSource {
        var frame: Frame? = null
        override val framesMatchScreen = true
        override suspend fun latestFrame(): Frame? = frame
        override suspend fun freshFrame(timeoutMs: Long): Frame? = frame
    }

    /** A phone screenshot: dark page, green board, pieces as outlined discs on a short base. */
    private fun draw(position: Position, whiteAtBottom: Boolean = true): Frame {
        val width = 1080
        val height = 2400
        val pixels = IntArray(width * height) { 0xFF161512.toInt() }
        val cell = BOARD.size / 8
        for (row in 0..7) for (col in 0..7) {
            val square = if ((row + col) % 2 == 0) 0xFFEEEED2.toInt() else 0xFF769656.toInt()
            val x0 = BOARD.left + col * cell
            val y0 = BOARD.top + row * cell
            for (y in y0 until y0 + cell) for (x in x0 until x0 + cell) pixels[y * width + x] = square
            val piece = position.pieceAt(BoardView.squareOf(row, col, whiteAtBottom)) ?: continue
            val white = piece.fenChar.isUpperCase()
            val fill = if (white) 0xFFF8F8F8.toInt() else 0xFF2B2B2B.toInt()
            val outline = if (white) 0xFF303030.toInt() else 0xFF000000.toInt()
            val cx = x0 + cell / 2
            val cy = y0 + cell / 2 - cell / 12
            val radius = cell * 0.30f
            for (y in y0 + 6 until y0 + cell - 6) for (x in x0 + 6 until x0 + cell - 6) {
                val dx = (x - cx).toFloat()
                val dy = (y - cy).toFloat()
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                val inBase = y in (cy + (radius * 0.6f).toInt())..(y0 + cell - 14) && abs(x - cx) <= cell * 0.30f
                val inDisc = distance <= radius
                if (!inDisc && !inBase) continue
                val edge = (inDisc && distance > radius - 5) && !inBase ||
                    inBase && !inDisc && (abs(x - cx) > cell * 0.30f - 5 || y > y0 + cell - 19)
                pixels[y * width + x] = if (edge) outline else fill
            }
        }
        return Frame(width, height, pixels)
    }

    @Test
    fun readsTracksAndWatchesDrawnBoardsWithTheRealVisionAndEngine() = runTest {
        val overlay = FakeOverlay()
        val frames = DrawnFrames()
        val settings = FakeSettings().apply { autoAdvise = true }
        val claude = FakeClaude { testScheduler.currentTime }
        val dispatcher = StandardTestDispatcher(testScheduler)
        // Default analyzer and vision: EnginePositionAnalyzer and PixelBoardVision.
        val brain = ChessBrain(
            frames, overlay, FakeEncoder(), settings, claude, backgroundScope,
            BrainDispatchers(dispatcher, dispatcher, dispatcher),
            clock = { testScheduler.currentTime },
        )
        fun run(ms: Long) {
            advanceTimeBy(ms)
            runCurrent()
        }

        brain.start()
        frames.frame = draw(start)
        claude.vision = reads(start, move = "e4")
        brain.onAnalyze()
        run(2_000)

        // The pixels agreed with the reading: accepted at the first submission, no cross-check rejection.
        val vision = claude.visionRequests.single()
        assertEquals(snapshotOf(BoardView.toRows(start, true)).toAscii(), vision.occupancyHint)
        val advice = overlay.state as? UiState.Advice ?: error("expected advice, got ${overlay.state}")
        assertEquals("e4", advice.moveSan)
        val arrow = assertNotNull(overlay.arrow)
        assertTrue(abs(arrow.boardLeft - BOARD.left) <= 2 && abs(arrow.boardTop - BOARD.top) <= 2 && abs(arrow.boardSize - BOARD.size) <= 3, arrow.toString())
        assertEquals(listOf(6, 4, 4, 4), listOf(arrow.fromRow, arrow.fromCol, arrow.toRow, arrow.toCol))
        run(5_000)
        assertEquals(1, overlay.captures)

        // White moves: the fingerprint notices, the sampler and the move tracker follow, nobody is asked.
        frames.frame = draw(start.after("e4"))
        run(3_000)
        assertEquals(2, overlay.captures)
        assertEquals("White played e4. Waiting for Black…", (overlay.state as UiState.Message).body)
        assertEquals(1, claude.calls)

        // Black answers: Claude gets the position with the real engine's lines in the briefing.
        claude.advice = recommends("Nf3")
        frames.frame = draw(start.after("e4", "e5"))
        run(5_000)
        assertEquals(3, overlay.captures)
        val request = claude.adviceRequests.single()
        val position = start.after("e4", "e5")
        assertEquals(position.toFen(), request.fen)
        assertEquals(listOf("e4", "e5"), request.history)
        val engineLines = request.briefing.lines().filter { it.matches(Regex("""\d\. .*""")) }
        assertTrue(engineLines.isNotEmpty(), request.briefing)
        for (line in engineLines) {
            val san = line.substringAfter(". ").substringBefore(" ")
            assertNotNull(position.parseMove(san), line)
        }
        assertEquals("Nf3", (overlay.state as UiState.Advice).moveSan)

        // Nothing else changes on screen, so nothing else happens.
        run(10_000)
        assertEquals(3, overlay.captures)
        assertEquals(2, claude.calls)
        brain.stop()
    }

    @Test
    fun aBoardSeenFromBlacksSideWithoutAKeyGetsTheRealEnginesMove() = runTest {
        val overlay = FakeOverlay()
        val frames = DrawnFrames()
        val settings = FakeSettings()
        val claude = FakeClaude { testScheduler.currentTime }
        val dispatcher = StandardTestDispatcher(testScheduler)
        val brain = ChessBrain(
            frames, overlay, FakeEncoder(), settings, claude, backgroundScope,
            BrainDispatchers(dispatcher, dispatcher, dispatcher),
            clock = { testScheduler.currentTime },
        )
        fun run(ms: Long) {
            advanceTimeBy(ms)
            runCurrent()
        }
        brain.start()
        // Scholar's mate is on: after 1.e4 e5 2.Bc4 Nc6 3.Qh5 Nf6?? the engine must find Qxf7#.
        val before = start.after("e4", "e5", "Bc4", "Nc6", "Qh5")
        frames.frame = draw(before, whiteAtBottom = false)
        claude.vision = reads(before, whiteAtBottom = false, move = "g6")
        brain.onAnalyze()
        run(2_000)
        assertEquals("g6", (overlay.state as UiState.Advice).moveSan)

        settings.apiKey = null
        frames.frame = draw(before.after("Nf6"), whiteAtBottom = false)
        settings.adviseFor = AdviseFor.BOTH
        brain.onAnalyze()
        run(2_000)
        val advice = overlay.state as? UiState.Advice ?: error("expected advice, got ${overlay.state}")
        assertEquals(BrainUi.LOCAL_ENGINE, advice.source)
        assertEquals("Qxf7#", advice.moveSan)
        assertEquals("Winning", advice.evaluation)
        assertEquals("White to move", advice.title)
        assertEquals(1, claude.calls)
        brain.stop()
    }

    private companion object {
        val BOARD = BoardRect(0, 420, 1080, 1f)
    }
}
