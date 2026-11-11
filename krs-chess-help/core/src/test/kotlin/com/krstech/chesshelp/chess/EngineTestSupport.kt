package com.krstech.chesshelp.chess

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** The same position with the colours swapped and the board flipped top to bottom (side to move swapped too). */
internal fun mirrorFen(fen: String): String {
    val fields = fen.trim().split(" ")
    val placement = fields[0].split("/").reversed().joinToString("/") { rank ->
        rank.map { c -> if (c.isUpperCase()) c.lowercaseChar() else c.uppercaseChar() }.joinToString("")
    }
    val side = if (fields[1] == "w") "b" else "w"
    val castling = if (fields[2] == "-") "-" else fields[2].map { c -> if (c.isUpperCase()) c.lowercaseChar() else c.uppercaseChar() }.joinToString("")
    val enPassant = if (fields[3] == "-") "-" else "${fields[3][0]}${'1' + ('8' - fields[3][1])}"
    return "$placement $side $castling $enPassant ${fields[4]} ${fields[5]}"
}

/** A move in UCI text with the ranks flipped, matching [mirrorFen]. */
internal fun mirrorUci(uci: String): String {
    val flipped = StringBuilder(uci)
    flipped[1] = '1' + ('8' - uci[1])
    flipped[3] = '1' + ('8' - uci[3])
    return flipped.toString()
}

/**
 * Brute-force forced-mate solver, independent of the engine's search: it only uses the move
 * generator. Used to prove what the puzzle positions in the tests really are.
 */
internal object MateOracle {
    /** Can the side to move force checkmate with at most [moves] of its own moves? */
    fun canMate(board: Board, moves: Int): Boolean {
        val buf = IntArray(Board.MAX_MOVES)
        val n = board.generateMoves(buf)
        for (i in 0 until n) {
            board.make(buf[i])
            val mated = isMatedWithin(board, moves - 1)
            board.unmake()
            if (mated) return true
        }
        return false
    }

    /** Is the side to move checkmated now, or unable to avoid mate within [moves] more enemy moves? */
    fun isMatedWithin(board: Board, moves: Int): Boolean {
        val buf = IntArray(Board.MAX_MOVES)
        val n = board.generateMoves(buf)
        if (n == 0) return board.inCheck()
        if (moves == 0) return false
        for (i in 0 until n) {
            board.make(buf[i])
            val forced = canMate(board, moves)
            board.unmake()
            if (!forced) return false
        }
        return true
    }

    /** The engine's `mateIn` convention: N > 0 mates in N, N < 0 is mated in -N, null if neither within [max]. */
    fun mateIn(fen: String, max: Int): Int? {
        val board = Position.fromFen(fen).toBoard()
        for (n in 1..max) {
            if (canMate(board, n)) return n
            if (isMatedWithin(board, n)) return -n
        }
        return null
    }
}

/**
 * Checks everything that must hold for any result: the right number of distinct legal lines, best
 * first, and every principal variation replayable move by move through the public [Position] API
 * with exactly the SAN the engine reported.
 */
internal fun assertSoundResult(position: Position, result: EngineResult, limits: SearchLimits) {
    val legal = position.legalMoves()
    val context = "in ${position.toFen()}"
    assertEquals(minOf(maxOf(limits.multiPv, 1), legal.size), result.lines.size, "number of lines $context")
    assertEquals(result.lines.size, result.lines.map { it.move }.toSet().size, "lines must be distinct root moves $context")
    assertTrue(result.depth <= maxOf(limits.maxDepth, 1), "depth ${result.depth} beyond the limit $context")
    assertTrue(result.nodes >= 0 && result.elapsedMs >= 0)

    var previousScore = Int.MAX_VALUE
    for (line in result.lines) {
        assertTrue(line.scoreCp <= previousScore, "lines must be sorted best first $context")
        previousScore = line.scoreCp
        assertEquals(result.depth, line.depth, "line depth $context")
        assertTrue(position.isLegal(line.move), "illegal root move ${line.move} $context")
        assertEquals(position.san(line.move), line.san, "SAN of the root move $context")
        assertTrue(line.pv.isNotEmpty(), "empty pv $context")
        assertEquals(line.san, line.pv[0], "pv must start with the line's own move $context")

        var current = position
        for (san in line.pv) {
            val move = current.parseMove(san)
            assertNotNull(move, "pv move $san of ${line.pv} is not legal in ${current.toFen()} (root ${position.toFen()})")
            assertTrue(current.isLegal(move))
            assertEquals(current.san(move), san, "pv SAN in ${current.toFen()}")
            current = current.play(move)
        }

        val mate = line.mateIn
        if (mate == null) {
            assertTrue(kotlin.math.abs(line.scoreCp) <= Evaluation.LIMIT, "score ${line.scoreCp} without mateIn $context")
        } else {
            assertTrue(mate != 0, "mateIn 0 $context")
            val plies = if (mate > 0) 2 * mate - 1 else -2 * mate
            assertEquals(if (mate > 0) Searcher.MATE - plies else -(Searcher.MATE - plies), line.scoreCp, "mate score $context")
            assertTrue(line.pv.size <= plies, "pv ${line.pv} longer than the announced mate in $mate $context")
            if (line.pv.size == plies) assertTrue(current.isCheckmate(), "pv ${line.pv} must end in mate $context")
        }
    }
}
