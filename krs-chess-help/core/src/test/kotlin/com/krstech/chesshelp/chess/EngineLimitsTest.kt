package com.krstech.chesshelp.chess

import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Time budget, stop callback and the "always at least one line" guarantee. */
class EngineLimitsTest {
    private val start = Position.START
    private val middlegame = Position.fromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1")
    private val tactical = Position.fromFen("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10")
    private val endgame = Position.fromFen("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1")

    private fun timed(block: () -> EngineResult): Pair<EngineResult, Long> {
        val begin = System.nanoTime()
        val result = block()
        return result to (System.nanoTime() - begin) / 1_000_000
    }

    @Test
    fun timeBudgetIsRespected() {
        // The very first search of a JVM also pays for class loading; do that outside the measurement.
        Engine().analyze(start, SearchLimits(maxDepth = 2))
        for (position in listOf(start, middlegame, tactical, endgame)) {
            val limits = SearchLimits(maxDepth = 64, timeMs = 300, multiPv = 3)
            val (result, wallMs) = timed { Engine().analyze(position, limits) }
            assertSoundResult(position, result, limits)
            assertTrue(wallMs < 800, "timeMs = 300 took $wallMs ms in ${position.toFen()}")
            assertTrue(result.elapsedMs <= wallMs)
            assertTrue(result.depth >= 3, "only depth ${result.depth} in 300 ms")
            println("timeMs=300: returned after $wallMs ms at depth ${result.depth}, ${result.nodes} nodes :: ${position.toFen()}")
        }
    }

    @Test
    fun tinyOrNonsensicalBudgetsStillGiveASearchedLine() {
        for (position in listOf(start, middlegame, tactical, endgame)) {
            for (timeMs in longArrayOf(1, 0, -50, Long.MIN_VALUE)) {
                val limits = SearchLimits(maxDepth = 32, timeMs = timeMs, multiPv = 3)
                val (result, wallMs) = timed { Engine().analyze(position, limits) }
                assertSoundResult(position, result, limits)
                assertEquals(3, result.lines.size)
                assertTrue(result.depth >= 1, "timeMs = $timeMs gave depth ${result.depth}")
                assertTrue(wallMs < 500, "timeMs = $timeMs took $wallMs ms")
            }
        }
        // Even a one-ply look takes a hanging queen rather than a random move.
        val hanging = Position.fromFen("rnb1kbnr/pppp1ppp/8/4p1q1/4P3/3P4/PPP2PPP/RNBQKBNR w KQkq - 1 3")
        assertEquals("c1g5", Engine().analyze(hanging, SearchLimits(timeMs = 0)).lines[0].move.uci())
    }

    @Test
    fun depthLimitsAreClamped() {
        for (depth in intArrayOf(0, -7, Int.MIN_VALUE)) {
            val result = Engine().analyze(tactical, SearchLimits(maxDepth = depth, timeMs = 60_000))
            assertEquals(1, result.depth)
            assertEquals(3, result.lines.size)
        }
        // A huge depth limit is cut to something the search stack can hold and the clock still rules.
        val (result, wallMs) = timed { Engine().analyze(tactical, SearchLimits(maxDepth = Int.MAX_VALUE, timeMs = 60)) }
        assertTrue(result.depth in 1..64)
        assertTrue(wallMs < 500, "took $wallMs ms")
        // A finished position searches to the depth cap almost instantly instead of using up the budget.
        val (solved, solvedMs) = timed {
            Engine().analyze(Position.fromFen("6k1/8/8/8/8/1r6/r7/7K w - - 0 1"), SearchLimits(maxDepth = 500, timeMs = 60_000))
        }
        assertEquals(64, solved.depth)
        assertEquals(-1, solved.lines[0].mateIn)
        assertTrue(solvedMs < 2_000, "took $solvedMs ms")
    }

    @Test
    fun shouldStopAbortsPromptly() {
        Engine().analyze(start, SearchLimits(maxDepth = 2))
        for (position in listOf(start, middlegame, tactical)) {
            val limits = SearchLimits(maxDepth = 64, timeMs = 60_000, multiPv = 3)
            val polls = AtomicInteger()
            val begin = System.nanoTime()
            val result = Engine().analyze(position, limits) {
                polls.incrementAndGet()
                System.nanoTime() - begin > 150_000_000L
            }
            val wallMs = (System.nanoTime() - begin) / 1_000_000
            assertSoundResult(position, result, limits)
            assertTrue(wallMs in 150..600, "stop requested after 150 ms, returned after $wallMs ms")
            assertTrue(polls.get() > 20, "shouldStop was polled only ${polls.get()} times")
            assertTrue(result.depth >= 3)
            println("stop after 150 ms: returned after $wallMs ms, ${polls.get()} polls, depth ${result.depth}")
        }
    }

    @Test
    fun stopRequestedFromTheStartStillReturnsTheFirstIteration() {
        for (position in listOf(start, middlegame, tactical, endgame)) {
            val limits = SearchLimits(maxDepth = 64, timeMs = 60_000, multiPv = 3)
            val (result, wallMs) = timed { Engine().analyze(position, limits) { true } }
            assertSoundResult(position, result, limits)
            assertEquals(1, result.depth)
            assertEquals(3, result.lines.size)
            assertTrue(wallMs < 500, "took $wallMs ms")
        }
    }

    @Test
    fun runawayFirstIterationFallsBackToStaticLines() {
        // Thirty queens: capture sequences explode, so the first iteration becomes interruptible
        // and the stop request ends it. The answer is then the static ranking, flagged as depth 0.
        val position = Position.fromFen("qqqqkqqq/qqqqqqqq/8/8/8/8/QQQQQQQQ/QQQQKQQQ w - - 0 1")
        val limits = SearchLimits(maxDepth = 8, timeMs = 60_000, multiPv = 3)
        val (result, wallMs) = timed { Engine().analyze(position, limits) { true } }
        assertSoundResult(position, result, limits)
        assertEquals(3, result.lines.size)
        assertTrue(wallMs < 1_000, "took $wallMs ms")
        println("thirty queens: depth ${result.depth} after $wallMs ms and ${result.nodes} nodes, ${result.lines.map { it.san + " " + it.scoreCp }}")
        if (result.depth == 0) {
            assertTrue(result.lines.all { it.pv.size == 1 && it.depth == 0 })
        }

        // The same fallback ranks a mate in one first.
        val mating = Position.fromFen("qqqqqqqk/qqqqqqpp/8/8/8/8/QQQQQQQQ/QQQQKQQR w - - 0 1")
        val forced = Engine().analyze(mating, SearchLimits(timeMs = 0)) { true }
        assertSoundResult(mating, forced, SearchLimits(timeMs = 0))
    }

    @Test
    fun exceptionFromShouldStopPropagatesAndLeavesTheEngineUsable() {
        val engine = Engine()
        val calls = AtomicInteger()
        assertFailsWith<IllegalStateException> {
            engine.analyze(tactical, SearchLimits(maxDepth = 64, timeMs = 60_000)) {
                if (calls.incrementAndGet() > 5) throw IllegalStateException("cancelled")
                false
            }
        }
        val limits = SearchLimits(maxDepth = 5, timeMs = 60_000, multiPv = 3)
        val after = engine.analyze(tactical, limits)
        assertSoundResult(tactical, after, limits)
        assertEquals(Engine().analyze(tactical, limits).lines, after.lines)
    }

    @Test
    fun enginesAreIndependentAndOneEngineCanBeSharedBetweenThreads() {
        val limits = SearchLimits(maxDepth = 5, timeMs = 60_000, multiPv = 3)
        val positions = listOf(start, middlegame, tactical, endgame)
        val expected = positions.map { Engine().analyze(it, limits).lines }

        val shared = Engine()
        val failures = java.util.Collections.synchronizedList(ArrayList<String>())
        val threads = (0 until 4).map { t ->
            thread {
                try {
                    val own = Engine()
                    repeat(3) { round ->
                        val index = (t + round) % positions.size
                        if (shared.analyze(positions[index], limits).lines != expected[index]) failures.add("shared engine, thread $t")
                        if (own.analyze(positions[index], limits).lines != expected[index]) failures.add("own engine, thread $t")
                    }
                } catch (e: Throwable) {
                    failures.add("thread $t: $e")
                }
            }
        }
        threads.forEach { it.join() }
        assertEquals(emptyList(), failures.toList())
    }

    @Test
    fun reachesDepthSixFromTheStartPositionInTheDefaultBudget() {
        Engine().analyze(start, SearchLimits(maxDepth = 2))
        var best = 0
        for (round in 1..2) {
            for ((name, position) in listOf("start" to start, "kiwipete" to middlegame, "middlegame" to tactical)) {
                val (result, wallMs) = timed { Engine().analyze(position, SearchLimits()) }
                println(
                    "800 ms, multiPv 3, $name, round $round: depth ${result.depth}, ${result.nodes} nodes in ${result.elapsedMs} ms " +
                        "(${result.nodes * 1000 / maxOf(1, result.elapsedMs)} nodes/s), best ${result.lines[0].san} ${result.lines[0].scoreCp}",
                )
                assertTrue(wallMs < 1_500, "default limits took $wallMs ms")
                if (position === start) best = maxOf(best, result.depth)
            }
        }
        assertTrue(best >= 6, "start position reached only depth $best in 800 ms")
    }
}
