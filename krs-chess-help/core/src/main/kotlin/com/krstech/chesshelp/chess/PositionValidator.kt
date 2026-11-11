package com.krstech.chesshelp.chess

object PositionValidator {
    /**
     * Human-readable problems, empty when the placement is a plausible legal chess position.
     *
     * Checked: exactly one king per side; no pawns on rank 1/8; at most 8 pawns and 16 pieces per
     * side; kings not adjacent; the side not to move must not be in check; the number of pieces
     * that can only exist through promotion must fit the number of missing pawns. Every message
     * names the side and the squares involved so that a reader can re-inspect the board.
     */
    fun problems(pieces: Array<Piece?>, sideToMove: Side): List<String> {
        require(pieces.size == 64) { "expected 64 squares, got ${pieces.size}" }
        val problems = ArrayList<String>()

        for (side in Side.entries) {
            val kings = squaresOf(pieces, side, PieceType.KING)
            when (kings.size) {
                1 -> Unit
                0 -> problems += "${name(side)} has no king"
                else -> problems += "${name(side)} has ${kings.size} kings (${names(kings)})"
            }
        }

        val strayPawns = ArrayList<String>()
        for (sq in 0 until 64) {
            val piece = pieces[sq] ?: continue
            val rank = Square.rank(sq)
            if (piece.type == PieceType.PAWN && (rank == 0 || rank == 7)) {
                strayPawns += "${name(piece.side).lowercase()} pawn on ${Square.name(sq)}"
            }
        }
        if (strayPawns.isNotEmpty()) {
            problems += "Pawns cannot stand on rank 1 or rank 8: ${strayPawns.joinToString(", ")}"
        }

        for (side in Side.entries) {
            val pawns = squaresOf(pieces, side, PieceType.PAWN)
            if (pawns.size > 8) {
                problems += "${name(side)} has ${pawns.size} pawns (${names(pawns)}); at most 8 are possible"
            }
            val total = pieces.count { it?.side == side }
            if (total > 16) {
                problems += "${name(side)} has $total pieces; at most 16 are possible"
            }
            promotionProblem(pieces, side, pawns.size)?.let { problems += it }
        }

        val whiteKings = squaresOf(pieces, Side.WHITE, PieceType.KING)
        val blackKings = squaresOf(pieces, Side.BLACK, PieceType.KING)
        if (whiteKings.size == 1 && blackKings.size == 1) {
            val wk = whiteKings[0]
            val bk = blackKings[0]
            if (Attacks.KING[wk] and (1L shl bk) != 0L) {
                problems += "The kings stand on adjacent squares (white king on ${Square.name(wk)}, " +
                    "black king on ${Square.name(bk)})"
            }
        }

        val waiting = sideToMove.opposite
        val waitingKings = if (waiting == Side.WHITE) whiteKings else blackKings
        if (waitingKings.size == 1) {
            val board = Board()
            board.setup(Position.codesOf(pieces), sideToMove.ordinal, 0, -1, 0, 1)
            val kingSq = waitingKings[0]
            // An attack by the enemy king is the "adjacent kings" problem reported above.
            var attackers = board.attackers(kingSq, sideToMove.ordinal) and board.pieces(Board.KING).inv()
            if (attackers != 0L) {
                val described = ArrayList<String>()
                while (attackers != 0L) {
                    val sq = java.lang.Long.numberOfTrailingZeros(attackers)
                    attackers = attackers and (attackers - 1)
                    val type = PieceType.ofOrdinal(Board.typeOf(board.pieceAt(sq)))
                    described += "${type.name.lowercase()} on ${Square.name(sq)}"
                }
                problems += "${name(waiting)}'s king on ${Square.name(kingSq)} is attacked by the " +
                    "${name(sideToMove).lowercase()} ${described.joinToString(" and the ")}, yet it is " +
                    "${name(sideToMove)} to move - the side that has just moved cannot be in check"
            }
        }

        return problems
    }

    /**
     * Each side starts with one queen, two rooks, two knights and one bishop per square colour;
     * anything beyond that is a promoted pawn, and only missing pawns can have promoted.
     */
    private fun promotionProblem(pieces: Array<Piece?>, side: Side, pawnCount: Int): String? {
        val queens = squaresOf(pieces, side, PieceType.QUEEN).size
        val rooks = squaresOf(pieces, side, PieceType.ROOK).size
        val knights = squaresOf(pieces, side, PieceType.KNIGHT).size
        val bishops = squaresOf(pieces, side, PieceType.BISHOP)
        val lightBishops = bishops.count { Board.LIGHT_SQUARES and (1L shl it) != 0L }
        val darkBishops = bishops.size - lightBishops

        val surplus = ArrayList<String>()
        var promoted = 0
        fun count(have: Int, original: Int, label: String) {
            if (have > original) {
                promoted += have - original
                surplus += "$have $label"
            }
        }
        count(queens, 1, "queens")
        count(rooks, 2, "rooks")
        count(knights, 2, "knights")
        count(lightBishops, 1, "light-squared bishops")
        count(darkBishops, 1, "dark-squared bishops")

        val missingPawns = maxOf(0, 8 - pawnCount)
        if (promoted <= missingPawns) return null
        return "${name(side)} has ${surplus.joinToString(", ")}, which takes at least $promoted promoted " +
            "pawn${if (promoted == 1) "" else "s"}, but with $pawnCount pawns still on the board at most " +
            "$missingPawns can have promoted"
    }

    private fun squaresOf(pieces: Array<Piece?>, side: Side, type: PieceType): List<Int> {
        val result = ArrayList<Int>()
        for (sq in 0 until 64) {
            val piece = pieces[sq]
            if (piece != null && piece.side == side && piece.type == type) result += sq
        }
        return result
    }

    private fun names(squares: List<Int>): String = squares.joinToString(", ") { Square.name(it) }

    private fun name(side: Side): String = if (side == Side.WHITE) "White" else "Black"
}
