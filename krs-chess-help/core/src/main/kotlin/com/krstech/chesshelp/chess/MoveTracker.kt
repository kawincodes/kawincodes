package com.krstech.chesshelp.chess

/** What pixel analysis can tell about a square: empty, or holding a piece of a given colour. */
enum class Occupant { EMPTY, WHITE, BLACK }

sealed interface MoveInference {
    /** The observed occupancy is that of the known position. */
    data object NoChange : MoveInference

    /**
     * The move(s) that explain the observed occupancy, oldest first, with their SAN and the
     * resulting [position]. [promotionAssumed] is set when a pawn promoted and the piece could not
     * be told from occupancy alone, in which case a queen was assumed.
     */
    data class Played(
        val moves: List<Move>,
        val sans: List<String>,
        val position: Position,
        val promotionAssumed: Boolean,
    ) : MoveInference

    /** Several move sequences leading to [candidates] different placements fit the observation. */
    data class Ambiguous(val candidates: Int) : MoveInference

    /** No legal move sequence within the ply limit produces the observed occupancy. */
    data object NoMatch : MoveInference
}

object MoveTracker {
    /** Occupancy by square index. */
    fun occupancyOf(position: Position): Array<Occupant> = Array(64) { sq ->
        when (position.pieceAt(sq)?.side) {
            null -> Occupant.EMPTY
            Side.WHITE -> Occupant.WHITE
            Side.BLACK -> Occupant.BLACK
        }
    }

    /**
     * Finds the legal move sequence from [position] (1 ply, else 2 … up to [maxPlies]) whose
     * resulting occupancy equals [observed] (indexed by square). Shorter sequences win. The
     * promotions of one pawn move look identical and collapse to the queen promotion; if the
     * remaining sequences still lead to more than one placement the result is
     * [MoveInference.Ambiguous].
     */
    fun infer(position: Position, observed: Array<Occupant>, maxPlies: Int = 2): MoveInference {
        require(observed.size == 64) { "expected 64 squares, got ${observed.size}" }
        var white = 0L
        var black = 0L
        for (sq in 0 until 64) {
            when (observed[sq]) {
                Occupant.WHITE -> white = white or (1L shl sq)
                Occupant.BLACK -> black = black or (1L shl sq)
                Occupant.EMPTY -> Unit
            }
        }

        val board = position.toBoard()
        if (board.occupancy(Board.WHITE) == white && board.occupancy(Board.BLACK) == black) {
            return MoveInference.NoChange
        }

        val plyLimit = minOf(maxPlies, MAX_PLIES)
        val buffers = Array(maxOf(plyLimit, 0)) { IntArray(Board.MAX_MOVES) }
        for (plies in 1..plyLimit) {
            val matches = ArrayList<IntArray>()
            search(board, plies, IntArray(plies), 0, white, black, buffers, matches)
            if (matches.isNotEmpty()) return resolve(position, matches)
        }
        return MoveInference.NoMatch
    }

    private fun search(
        board: Board,
        plies: Int,
        path: IntArray,
        depth: Int,
        white: Long,
        black: Long,
        buffers: Array<IntArray>,
        matches: MutableList<IntArray>,
    ) {
        val differing = (board.occupancy(Board.WHITE) xor white) or (board.occupancy(Board.BLACK) xor black)
        if (depth == plies) {
            if (differing == 0L) matches += path.copyOf()
            return
        }
        // A single move changes the occupancy of at most four squares (castling).
        if (java.lang.Long.bitCount(differing) > 4 * (plies - depth)) return
        val moves = buffers[depth]
        val count = board.generateMoves(moves)
        for (i in 0 until count) {
            path[depth] = moves[i]
            board.make(moves[i])
            search(board, plies, path, depth + 1, white, black, buffers, matches)
            board.unmake()
        }
    }

    private fun resolve(start: Position, matches: List<IntArray>): MoveInference {
        // Sequences that differ only in the promotion piece are one candidate: keep the "biggest"
        // promotion (queen first) and remember that it was a guess.
        val bySquares = LinkedHashMap<List<Int>, MutableList<IntArray>>()
        for (sequence in matches) {
            bySquares.getOrPut(sequence.map { it and FROM_TO_MASK }) { ArrayList() } += sequence
        }

        // Different sequences can still end in the very same placement (e2-e3 dxe3 versus
        // e2-e4 dxe3 e.p.); only genuinely different outcomes make the observation ambiguous.
        val byPlacement = LinkedHashMap<String, Candidate>()
        for (group in bySquares.values) {
            val best = group.maxWith { a, b -> comparePromotions(a, b) }
            val moves = best.map { Moves.toMove(it) }
            val sans = ArrayList<String>(moves.size)
            var position = start
            for (move in moves) {
                sans += position.san(move)
                position = position.play(move)
            }
            val candidate = Candidate(moves, sans, position, promotionAssumed = group.size > 1)
            byPlacement.getOrPut(position.placement()) { candidate }
        }

        if (byPlacement.size > 1) return MoveInference.Ambiguous(byPlacement.size)
        val only = byPlacement.values.first()
        return MoveInference.Played(only.moves, only.sans, only.position, only.promotionAssumed)
    }

    private fun comparePromotions(a: IntArray, b: IntArray): Int {
        for (i in a.indices) {
            val difference = Moves.promotion(a[i]) - Moves.promotion(b[i])
            if (difference != 0) return difference
        }
        return 0
    }

    private class Candidate(
        val moves: List<Move>,
        val sans: List<String>,
        val position: Position,
        val promotionAssumed: Boolean,
    )

    private const val FROM_TO_MASK = 0xFFF

    /** Search cost grows ~35x per ply; beyond this the answer would be guesswork anyway. */
    private const val MAX_PLIES = 4
}
