package com.krstech.chesshelp.chess

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Perft = number of leaf nodes of the legal move tree at a given depth. The reference values are
 * the published ones (chessprogramming.org "Perft Results"); any bug in castling, en passant,
 * promotion, pin or check-evasion handling shows up as a wrong count.
 */
class PerftTest {
    private class Case(val fen: String, vararg val counts: Long)

    private val cases = listOf(
        Case("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 20, 400, 8902, 197281),
        Case("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 48, 2039, 97862),
        Case("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1", 14, 191, 2812, 43238, 674624),
        Case("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 6, 264, 9467, 422333),
        Case("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 44, 1486, 62379),
        Case("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", 46, 2079, 89890),
    )

    @Test
    fun perftOnInternalBoardMatchesReferenceValues() {
        for (case in cases) {
            val board = Position.fromFen(case.fen).toBoard()
            for ((index, expected) in case.counts.withIndex()) {
                assertEquals(expected, perft(board, index + 1), "perft(${index + 1}) of ${case.fen}")
            }
            assertEquals(case.fen, board.toPosition().toFen(), "board must be restored after perft")
        }
    }

    /** One ply deeper than the required table, to exercise far more positions (about 40 million leaves). */
    @Test
    fun deeperPerftOnInternalBoardMatchesReferenceValues() {
        val deeper = listOf(
            Triple(cases[0].fen, 5, 4865609L),
            Triple(cases[1].fen, 4, 4085603L),
            Triple(cases[2].fen, 6, 11030083L),
            Triple(cases[3].fen, 5, 15833292L),
            Triple(cases[4].fen, 4, 2103487L),
            Triple(cases[5].fen, 4, 3894594L),
        )
        for ((fen, depth, expected) in deeper) {
            assertEquals(expected, perft(Position.fromFen(fen).toBoard(), depth), "perft($depth) of $fen")
        }
    }

    /** The mirrored position 4 (black to move) must give the same counts as the original. */
    @Test
    fun perftIsColourSymmetric() {
        val mirrored = "r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1"
        val board = Position.fromFen(mirrored).toBoard()
        val expected = longArrayOf(6, 264, 9467, 422333)
        for (depth in 1..4) assertEquals(expected[depth - 1], perft(board, depth), "perft($depth)")
    }

    @Test
    fun perftThroughPublicPositionApiMatchesReferenceValues() {
        // Same tree walked with the immutable API (legalMoves + play), one or two plies shallower.
        val depths = intArrayOf(3, 3, 4, 3, 3, 3)
        for ((i, case) in cases.withIndex()) {
            val position = Position.fromFen(case.fen)
            for (depth in 1..depths[i]) {
                assertEquals(case.counts[depth - 1], perft(position, depth), "Position perft($depth) of ${case.fen}")
            }
        }
    }

    /**
     * Walks the same trees while checking the board's invariants at every node: incremental
     * Zobrist key, make/unmake symmetry, captures-only generation and check detection.
     */
    @Test
    fun boardInvariantsHoldThroughoutTheTree() {
        val depths = intArrayOf(3, 3, 4, 3, 3, 3)
        for ((i, case) in cases.withIndex()) {
            val board = Position.fromFen(case.fen).toBoard()
            val nodes = checkedPerft(board, depths[i])
            assertEquals(case.counts[depths[i] - 1], nodes, "checked perft(${depths[i]}) of ${case.fen}")
        }
    }

    private fun perft(board: Board, depth: Int): Long = perft(board, depth, Array(depth + 1) { IntArray(Board.MAX_MOVES) })

    private fun perft(board: Board, depth: Int, buffers: Array<IntArray>): Long {
        val moves = buffers[depth]
        val count = board.generateMoves(moves)
        if (depth == 1) return count.toLong()
        var nodes = 0L
        for (i in 0 until count) {
            board.make(moves[i])
            nodes += perft(board, depth - 1, buffers)
            board.unmake()
        }
        return nodes
    }

    private fun perft(position: Position, depth: Int): Long {
        val moves = position.legalMoves()
        if (depth == 1) return moves.size.toLong()
        return moves.sumOf { perft(position.play(it), depth - 1) }
    }

    private fun checkedPerft(board: Board, depth: Int): Long {
        assertEquals(board.computeKey(), board.key, "incremental key")
        val moves = IntArray(Board.MAX_MOVES)
        val count = board.generateMoves(moves)
        val all = moves.copyOf(count)

        val noisy = IntArray(Board.MAX_MOVES)
        val noisyCount = board.generateCaptures(noisy)
        assertEquals(
            all.filter { Moves.isCapture(it) || Moves.isPromotion(it) }.sorted(),
            noisy.copyOf(noisyCount).sorted(),
            "captures-only generation",
        )
        assertEquals(board.checkers() != 0L, board.inCheck())
        assertEquals(count > 0, board.hasLegalMoves())
        val kingSq = board.kingSquare(board.side)
        assertEquals(board.inCheck(), board.isSquareAttacked(kingSq, board.side xor 1))
        // The side that just moved can never be left in check by a legal move.
        assertTrue(!board.isSquareAttacked(board.kingSquare(board.side xor 1), board.side))

        if (depth == 0) return 1
        val fenBefore = board.toPosition().toFen()
        val keyBefore = board.key
        val plyBefore = board.ply
        var nodes = 0L
        for (m in all) {
            board.make(m)
            nodes += checkedPerft(board, depth - 1)
            board.unmake()
            assertEquals(keyBefore, board.key, "key after unmake of ${Moves.uci(m)}")
            assertEquals(plyBefore, board.ply)
        }
        assertEquals(fenBefore, board.toPosition().toFen(), "state after unmake")
        return nodes
    }
}
