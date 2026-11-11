package com.krstech.chesshelp.brain

import com.krstech.chesshelp.chess.Position
import com.krstech.chesshelp.claude.AnalysisResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * One analysis at a time. The Claude calls here run on real threads (as they do in the app) and block
 * until they are interrupted or released, while everything else stays on the test scheduler.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CancellationTest {
    private val start = Position.START
    private val executor = Executors.newFixedThreadPool(2)
    private val io = executor.asCoroutineDispatcher()

    /** Counts calls and how many run at the same time. */
    private val calls = AtomicInteger()
    private val active = AtomicInteger()
    private val mostActive = AtomicInteger()

    @After
    fun tearDown() {
        executor.shutdownNow()
    }

    private fun <T> counted(block: (Int) -> T): T {
        val now = active.incrementAndGet()
        mostActive.accumulateAndGet(now, ::maxOf)
        try {
            return block(calls.incrementAndGet())
        } finally {
            active.decrementAndGet()
        }
    }

    /** Advances virtual time in small steps while giving the real threads time to do their part. */
    private fun TestScope.pump(what: String, condition: () -> Boolean) {
        val deadline = System.nanoTime() + 10_000_000_000L
        while (!condition()) {
            if (System.nanoTime() > deadline) fail("timed out waiting for $what")
            advanceTimeBy(20)
            runCurrent()
            Thread.sleep(2)
        }
    }

    private fun TestScope.pumpFor(steps: Int) {
        repeat(steps) {
            advanceTimeBy(20)
            runCurrent()
            Thread.sleep(2)
        }
    }

    private fun blockUntilInterrupted(interrupted: AtomicBoolean): Nothing {
        try {
            CountDownLatch(1).await()
        } catch (e: InterruptedException) {
            interrupted.set(true)
            throw e
        }
        error("unreachable")
    }

    @Test
    fun aTapCancelsARunningAutomaticAnalysis() = runTest {
        val h = Harness(this, io = io)
        h.settings.autoAdvise = true
        h.settings.adviseFor = AdviseFor.BOTH
        h.brain.start()
        h.show(start)
        h.claude.vision = reads(start, move = "e4")
        h.brain.onAnalyze()
        pump("the first advice") { h.overlay.state is UiState.Advice }

        val entered = CountDownLatch(1)
        val interrupted = AtomicBoolean()
        h.claude.advice = { request, oracle, progress ->
            counted { call ->
                if (call == 1) {
                    entered.countDown()
                    blockUntilInterrupted(interrupted)
                }
                recommends("e5")(request, oracle, progress)
            }
        }
        // Black's turn after White's move: with "advise both" the auto loop asks Claude by itself.
        h.show(start.after("e4"))
        pump("the automatic Claude call") { entered.count == 0L }
        assertEquals(UiState.Working(BrainUi.ASKING, BrainUi.board(start.after("e4"), true)), h.overlay.state)
        assertFalse(interrupted.get())

        h.brain.onAnalyze()
        pump("the interrupt") { interrupted.get() }
        pump("the user's advice") { calls.get() == 2 && h.overlay.state is UiState.Advice }

        assertEquals("e5", h.advice.moveSan)
        assertEquals(1, mostActive.get())
        // The cancelled call left no trace on screen.
        assertTrue(h.overlay.states.none { it is UiState.Message && it.isError }, h.overlay.states.toString())
        assertEquals(0, h.overlay.captureModes.size % 2)
        assertFalse(h.overlay.hidden)
        h.brain.stop()
    }

    @Test
    fun aSecondTapWhileTheUsersAnalysisRunsIsIgnored() = runTest {
        val h = Harness(this, io = io)
        h.brain.start()
        h.show(start)
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val script = reads(start, move = "e4")
        h.claude.vision = { request, oracle, progress ->
            counted {
                entered.countDown()
                release.await()
                script(request, oracle, progress)
            }
        }
        h.brain.onAnalyze()
        pump("the Claude call") { entered.count == 0L }
        h.brain.onAnalyze()
        h.brain.onAnalyze()
        pumpFor(50)
        assertEquals(1, calls.get())
        assertEquals(1, h.overlay.captures)

        release.countDown()
        pump("the advice") { h.overlay.state is UiState.Advice }
        assertEquals("e4", h.advice.moveSan)
        assertEquals(1, calls.get())
        h.brain.stop()
    }

    @Test
    fun rescanRestartsARunningAnalysisAfterItHasLetGo() = runTest {
        val h = Harness(this, io = io)
        h.brain.start()
        h.show(start)
        val entered = CountDownLatch(1)
        val interrupted = AtomicBoolean()
        val script = reads(start, move = "e4")
        h.claude.vision = { request, oracle, progress ->
            counted { call ->
                if (call == 1) {
                    entered.countDown()
                    blockUntilInterrupted(interrupted)
                }
                script(request, oracle, progress)
            }
        }
        h.brain.onAnalyze()
        pump("the Claude call") { entered.count == 0L }
        h.brain.onRescan()
        pump("the advice") { calls.get() == 2 && h.overlay.state is UiState.Advice }
        assertTrue(interrupted.get())
        assertEquals(1, mostActive.get())
        assertEquals("e4", h.advice.moveSan)
        assertEquals(listOf(true, false, true, false), h.overlay.captureModes)
        h.brain.stop()
    }

    @Test
    fun stopInterruptsTheCallAndShowsNothingMore() = runTest {
        val h = Harness(this, io = io)
        h.brain.start()
        h.show(start)
        val entered = CountDownLatch(1)
        val interrupted = AtomicBoolean()
        h.claude.vision = { _, _, _ ->
            entered.countDown()
            blockUntilInterrupted(interrupted)
        }
        h.brain.onAnalyze()
        pump("the Claude call") { entered.count == 0L }
        val shown = h.overlay.states.size

        h.brain.stop()
        pump("the interrupt") { interrupted.get() }
        pumpFor(20)
        assertEquals(1, h.claude.closed)
        assertEquals(shown, h.overlay.states.size)
        assertFalse(h.overlay.hidden)
    }

    @Test
    fun aCallThatIgnoresItsInterruptDoesNotBlockTheUser() = runTest {
        val h = Harness(this, io = io)
        h.brain.start()
        h.show(start)
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val stuckReturned = AtomicBoolean()
        val script = reads(start, move = "e4")
        h.claude.vision = { request, oracle, progress ->
            if (calls.incrementAndGet() == 1) {
                entered.countDown()
                // A socket read that does not react to Thread.interrupt().
                var done = false
                while (!done) {
                    try {
                        release.await()
                        done = true
                    } catch (_: InterruptedException) {
                        // keep waiting
                    }
                }
                stuckReturned.set(true)
                AnalysisResult(null, null, null, "Too late.", USAGE, "claude-opus-5-5", 1)
            } else {
                script(request, oracle, progress)
            }
        }
        h.brain.onAnalyze()
        pump("the stuck Claude call") { entered.count == 0L }

        h.brain.onRescan()
        pump("the advice of the second analysis") { h.overlay.state is UiState.Advice }
        assertEquals("e4", h.advice.moveSan)
        assertFalse(stuckReturned.get())
        val shown = h.overlay.states.size

        // When the abandoned call finally returns, its result is dropped.
        release.countDown()
        pump("the stuck call to return") { stuckReturned.get() }
        pumpFor(20)
        assertEquals(shown, h.overlay.states.size)
        assertIs<UiState.Advice>(h.overlay.state)
        h.brain.stop()
    }
}
