package com.krstech.chesshelp.chess

import kotlin.math.max
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Tests of the search internals: exactness against a reference minimax, exchange evaluation, hash table. */
class SearchTest {
    // ------------------------------------------------------------------ exactness

    /**
     * Plain minimax without any window, ordering, hashing or pruning. It follows the same rules as
     * the engine (check extension, draws, mate scores) and shares only the quiescence search at
     * the leaves, so it defines the value the alpha-beta machinery has to reproduce.
     */
    private fun reference(searcher: Searcher, depth: Int, ply: Int, inCheck: Boolean): Int {
        if (depth <= 0) return searcher.quiesce(-Searcher.INFINITE, Searcher.INFINITE, ply, inCheck)
        val board = searcher.board
        if (board.isRepetition() || board.isInsufficientMaterial()) return 0
        if (board.halfmoveClock >= 100) return if (inCheck && !board.hasLegalMoves()) -Searcher.MATE + ply else 0
        val moves = IntArray(Board.MAX_MOVES)
        val count = board.generateMoves(moves)
        if (count == 0) return if (inCheck) -Searcher.MATE + ply else 0
        var best = -Searcher.INFINITE
        for (i in 0 until count) {
            board.make(moves[i])
            val givesCheck = board.inCheck()
            best = max(best, -reference(searcher, if (givesCheck) depth else depth - 1, ply + 1, givesCheck))
            board.unmake()
        }
        return best
    }

    private fun referenceRootScores(position: Position, depth: Int): Map<Move, Int> {
        val searcher = Searcher()
        searcher.prepare(position)
        val board = searcher.board
        val moves = IntArray(Board.MAX_MOVES)
        val count = board.generateMoves(moves)
        val scores = LinkedHashMap<Move, Int>()
        for (i in 0 until count) {
            board.make(moves[i])
            val givesCheck = board.inCheck()
            scores[Moves.toMove(moves[i])] = -reference(searcher, if (givesCheck) depth else depth - 1, 1, givesCheck)
            board.unmake()
        }
        return scores
    }

    /**
     * With the selective parts off, principal-variation search, aspiration windows, mate-distance
     * pruning, move ordering and MultiPV must not change a single score.
     */
    private fun assertExact(fen: String, depth: Int) {
        val position = Position.fromFen(fen)
        val t0 = System.nanoTime()
        val expected = referenceRootScores(position, depth)
        val t1 = System.nanoTime()
        val ranked = expected.values.sortedDescending()

        for (multiPv in intArrayOf(1, 3, 1000)) {
            val searcher = Searcher()
            searcher.selective = false
            val limits = SearchLimits(maxDepth = depth, timeMs = 600_000, multiPv = multiPv)
            val result = searcher.analyze(position, limits) { false }
            assertSoundResult(position, result, limits)
            assertEquals(depth, result.depth)
            for ((index, line) in result.lines.withIndex()) {
                assertEquals(expected[line.move], line.scoreCp, "score of ${line.san} at depth $depth, multiPv $multiPv in $fen")
                assertEquals(ranked[index], line.scoreCp, "line ${index + 1} must be the ${index + 1}. best move (depth $depth, multiPv $multiPv) in $fen")
            }
        }
        println("TIMING ref ${(t1 - t0) / 1_000_000} ms engine ${(System.nanoTime() - t1) / 1_000_000} ms depth $depth scores ${ranked.take(4)} :: $fen")
    }

    @Test
    fun exactSearchEqualsMinimaxInMiddlegames() {
        assertExact(Position.START_FEN, 4)
        assertExact("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 3)
        assertExact("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 3)
        assertExact("r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1", 3)
        assertExact("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 3)
        assertExact("r1b1kb1r/pppp1ppp/5q2/4n3/3KP3/2N3PN/PPP4P/R1BQ1B1R b kq - 0 1", 3)
    }

    @Test
    fun exactSearchEqualsMinimaxInEndgamesDeepEnoughForAspirationWindows() {
        assertExact("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1", 5)
        assertExact("8/8/4k3/3p4/3P4/4K3/8/8 w - - 0 1", 7)
        assertExact("8/5k2/8/8/8/8/1P6/K7 b - - 0 1", 7)
        assertExact("k7/2Q5/8/8/8/8/8/7K w - - 0 1", 6)
        assertExact("8/8/8/8/8/5k2/3r4/3K4 b - - 0 1", 6)
        assertExact("8/P7/8/8/8/8/6p1/k6K w - - 0 1", 5)
    }

    @Test
    fun exactSearchEqualsMinimaxForMatesAndDraws() {
        // Mates for and against the side to move.
        assertExact("5rk1/5Npp/8/8/2Q5/8/8/7K w - - 0 1", 5)
        assertExact("7k/8/8/8/8/8/R7/1R4K1 b - - 0 1", 6)
        assertExact("7k/8/8/8/8/8/R7/1R4K1 w - - 0 1", 6)
        assertExact("r5rk/5p1p/5R2/4B3/8/8/7P/7K w - - 0 1", 4)
        // Repetition (perpetual check), the fifty-move rule, stalemate and insufficient material.
        assertExact("6k1/5pp1/8/7Q/8/7P/rr4PK/8 w - - 0 1", 4)
        assertExact("4k3/8/8/8/8/8/4P3/R3K3 w - - 96 60", 5)
        assertExact("7k/5Q2/8/8/8/8/8/K7 w - - 0 1", 5)
        assertExact("8/8/8/8/8/8/3r1k2/3K4 w - - 0 1", 5)
        assertExact("8/8/8/2b5/8/2k5/8/2K1N3 w - - 0 1", 5)
    }

    // ------------------------------------------------------------------ static exchange evaluation

    private fun see(fen: String, uci: String): Int {
        val searcher = Searcher()
        searcher.prepare(Position.fromFen(fen))
        val moves = IntArray(Board.MAX_MOVES)
        val count = searcher.board.generateMoves(moves)
        val move = moves.take(count).single { Moves.uci(it) == uci }
        return searcher.see(move)
    }

    @Test
    fun staticExchangeEvaluation() {
        // The two classic examples from the chess programming literature.
        assertEquals(100, see("1k1r4/1pp4p/p7/4p3/8/P5P1/1PP4P/2K1R3 w - - 0 1", "e1e5"))
        assertEquals(-225, see("1k1r3q/1ppn3p/p4b2/4p3/8/P2N2P1/1PP1R1BP/2K1Q3 w - - 0 1", "d3e5"))
        // Queen takes a pawn that a pawn defends.
        assertEquals(-900, see("4k3/8/2p5/3p4/8/8/3Q4/4K3 w - - 0 1", "d2d5"))
        // Pawn takes queen; whatever recaptures, the queen is won for a pawn at worst.
        assertEquals(900, see("4k3/2p5/3q4/4P3/8/8/8/4K3 w - - 0 1", "e5d6"))
        // Rooks doubled behind each other: R x R, R x R, R x R leaves White a rook up.
        assertEquals(500, see("4k3/3r4/3r4/8/8/8/3R4/3RK3 w - - 0 1", "d2d6"))
        // A rook takes a pawn defended only by the king: lost unless a second rook backs it up,
        // because a king can not recapture on an attacked square.
        assertEquals(-400, see("4k3/3p4/8/8/8/8/8/3RK3 w - - 0 1", "d1d7"))
        assertEquals(100, see("4k3/3p4/8/8/8/8/3R4/3RK3 w - - 0 1", "d2d7"))
        // En passant removes the pawn beside the target square; nothing recaptures here.
        assertEquals(100, see("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1", "e5d6"))
        assertEquals(0, see("4k3/2p5/8/3pP3/8/8/8/4K3 w - d6 0 1", "e5d6"))
        // Promotion with capture: rook plus the queen-for-pawn upgrade, minus the queen if it is retaken.
        assertEquals(1400, see("3r1k2/4P3/8/8/8/8/8/4K3 w - - 0 1", "e7d8q"))
        assertEquals(400, see("3rk3/4P3/8/8/8/8/8/4K3 w - - 0 1", "e7d8q"))
        // Black to move, with a bishop x-raying through its own queen.
        assertEquals(100, see("4k3/8/5b2/4q3/3P4/8/8/4K3 b - - 0 1", "e5d4"))
        assertEquals(-900, see("4k3/8/8/4q3/3P4/2P5/8/4K3 b - - 0 1", "e5d4"))
        // A quiet move to a square where the piece simply hangs.
        assertEquals(-325, see("4k3/8/2p5/8/8/4N3/8/4K3 w - - 0 1", "e3d5"))
        assertEquals(0, see("4k3/8/8/8/8/4N3/8/4K3 w - - 0 1", "e3d5"))
    }

    // ------------------------------------------------------------------ transposition table

    @Test
    fun transpositionEntriesRoundTrip() {
        val move = Moves.encode(63, 0, Board.KING, Board.QUEEN, Board.QUEEN, Moves.KIND_CASTLE)
        for (score in intArrayOf(0, 1, -1, 137, -20_000, 20_000, Searcher.MATE + Searcher.MAX_PLY, -Searcher.MATE - Searcher.MAX_PLY)) {
            for (depth in intArrayOf(0, 1, 64, 255)) {
                for (bound in intArrayOf(TranspositionTable.EXACT, TranspositionTable.LOWER, TranspositionTable.UPPER)) {
                    for (m in intArrayOf(Moves.NONE, move, Moves.encode(12, 28, Board.PAWN, -1, 0, Moves.KIND_DOUBLE_PUSH))) {
                        val entry = TranspositionTable.pack(m, score, depth, bound)
                        assertTrue(entry != 0L)
                        assertEquals(m, TranspositionTable.move(entry))
                        assertEquals(score, TranspositionTable.score(entry))
                        assertEquals(depth, TranspositionTable.depth(entry))
                        assertEquals(bound, TranspositionTable.bound(entry))
                    }
                }
            }
        }
    }

    @Test
    fun transpositionTableKeepsDeepEntriesAndReplacesShallowOnes() {
        val table = TranspositionTable(bucketBits = 4)
        assertEquals(16L * 4 * 8, table.sizeBytes)
        val exact = TranspositionTable.EXACT
        // Three keys falling into the same bucket (same low bits).
        val a = 0x1111_0000_0000_0003L
        val b = 0x2222_0000_0000_0003L
        val c = 0x3333_0000_0000_0003L
        assertEquals(0L, table.probe(a))

        table.store(a, 11, 50, 9, exact)
        table.store(b, 22, -60, 2, exact)
        assertEquals(9, TranspositionTable.depth(table.probe(a)))
        assertEquals(-60, TranspositionTable.score(table.probe(b)))

        // A shallow newcomer takes the always-replace slot; the deep entry survives.
        table.store(c, 33, 70, 1, exact)
        assertEquals(50, TranspositionTable.score(table.probe(a)))
        assertEquals(70, TranspositionTable.score(table.probe(c)))
        assertEquals(0L, table.probe(b))

        // A deeper newcomer takes the first slot and pushes the old deep entry into the second.
        table.store(b, 22, -61, 12, exact)
        assertEquals(12, TranspositionTable.depth(table.probe(b)))
        assertEquals(9, TranspositionTable.depth(table.probe(a)))
        assertEquals(0L, table.probe(c))

        // Same position again: updated in place; a missing move keeps the known one, and a much
        // shallower bound does not overwrite a deep entry.
        table.store(b, Moves.NONE, -65, 13, TranspositionTable.UPPER)
        assertEquals(22, TranspositionTable.move(table.probe(b)))
        assertEquals(-65, TranspositionTable.score(table.probe(b)))
        table.store(b, 44, 5, 3, TranspositionTable.LOWER)
        assertEquals(13, TranspositionTable.depth(table.probe(b)))
        assertEquals(22, TranspositionTable.move(table.probe(b)))

        table.clear()
        assertEquals(0L, table.probe(a))
        assertEquals(0L, table.probe(b))
    }

    @Test
    fun defaultTableStaysWithinThePhoneBudget() {
        val bytes = TranspositionTable().sizeBytes
        assertEquals(8L * 1024 * 1024, bytes)
        assertTrue(bytes <= 16L * 1024 * 1024)
    }

    @Test
    fun mateDistanceConversion() {
        assertEquals(1, Searcher.mateIn(Searcher.MATE - 1))
        assertEquals(2, Searcher.mateIn(Searcher.MATE - 3))
        assertEquals(3, Searcher.mateIn(Searcher.MATE - 5))
        assertEquals(-1, Searcher.mateIn(-Searcher.MATE + 2))
        assertEquals(-2, Searcher.mateIn(-Searcher.MATE + 4))
        assertEquals(-3, Searcher.mateIn(-Searcher.MATE + 6))
        assertNull(Searcher.mateIn(0))
        assertNull(Searcher.mateIn(Evaluation.LIMIT))
        assertNull(Searcher.mateIn(-Evaluation.LIMIT))
        // No evaluation can reach the mate range, and every mate score fits the table's 16 bits.
        assertTrue(Evaluation.LIMIT < Searcher.MATE_BOUND)
        assertTrue(Searcher.MATE + Searcher.MAX_PLY <= Short.MAX_VALUE)
    }
}
