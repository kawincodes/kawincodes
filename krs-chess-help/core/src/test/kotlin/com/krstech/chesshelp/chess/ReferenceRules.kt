package com.krstech.chesshelp.chess

/**
 * A deliberately naive, independent implementation of the rules of chess (8x8 char array, ray
 * walking, "make the move on a copy and look whether the king can be taken"). It shares no code
 * with the production board and exists only as an oracle for differential tests.
 */
internal class ReferencePosition(
    val board: CharArray,
    var whiteToMove: Boolean,
    val castling: BooleanArray, // K, Q, k, q
    var enPassant: Int,
) {
    data class RefMove(val from: Int, val to: Int, val promotion: Char? = null) {
        fun uci(): String = Square.name(from) + Square.name(to) + (promotion?.lowercaseChar()?.toString() ?: "")
    }

    fun copy() = ReferencePosition(board.copyOf(), whiteToMove, castling.copyOf(), enPassant)

    private fun isWhite(c: Char) = c in 'A'..'Z'
    private fun isOwn(c: Char, white: Boolean) = c != '.' && isWhite(c) == white
    private fun isEnemy(c: Char, white: Boolean) = c != '.' && isWhite(c) != white

    fun isAttacked(sq: Int, byWhite: Boolean): Boolean {
        val f = sq % 8
        val r = sq / 8
        fun at(df: Int, dr: Int): Char {
            val nf = f + df
            val nr = r + dr
            return if (nf in 0..7 && nr in 0..7) board[nr * 8 + nf] else '#'
        }
        // A white pawn attacks upwards, so it sits one rank below the attacked square.
        val pawn = if (byWhite) 'P' else 'p'
        val pawnRank = if (byWhite) -1 else 1
        if (at(-1, pawnRank) == pawn || at(1, pawnRank) == pawn) return true
        val knight = if (byWhite) 'N' else 'n'
        for ((df, dr) in KNIGHT_STEPS) if (at(df, dr) == knight) return true
        val king = if (byWhite) 'K' else 'k'
        for ((df, dr) in KING_STEPS) if (at(df, dr) == king) return true
        val queen = if (byWhite) 'Q' else 'q'
        for ((df, dr) in KING_STEPS) {
            val diagonal = df != 0 && dr != 0
            val slider = if (diagonal) (if (byWhite) 'B' else 'b') else (if (byWhite) 'R' else 'r')
            var n = 1
            while (true) {
                val c = at(df * n, dr * n)
                if (c == '#') break
                if (c != '.') {
                    if (c == slider || c == queen) return true
                    break
                }
                n++
            }
        }
        return false
    }

    private fun kingSquare(white: Boolean): Int = board.indexOf(if (white) 'K' else 'k')

    fun inCheck(): Boolean {
        val k = kingSquare(whiteToMove)
        return k >= 0 && isAttacked(k, !whiteToMove)
    }

    private fun pseudoLegal(): List<RefMove> {
        val moves = ArrayList<RefMove>()
        val white = whiteToMove
        for (from in 0 until 64) {
            val c = board[from]
            if (!isOwn(c, white)) continue
            val f = from % 8
            val r = from / 8
            fun target(df: Int, dr: Int): Int {
                val nf = f + df
                val nr = r + dr
                return if (nf in 0..7 && nr in 0..7) nr * 8 + nf else -1
            }
            when (c.uppercaseChar()) {
                'P' -> {
                    val dir = if (white) 1 else -1
                    val startRank = if (white) 1 else 6
                    val lastRank = if (white) 7 else 0
                    fun add(to: Int) {
                        if (to / 8 == lastRank) {
                            for (p in "QRBN") moves += RefMove(from, to, p)
                        } else {
                            moves += RefMove(from, to)
                        }
                    }
                    val one = target(0, dir)
                    if (one >= 0 && board[one] == '.') {
                        add(one)
                        val two = target(0, 2 * dir)
                        if (r == startRank && board[two] == '.') add(two)
                    }
                    for (df in intArrayOf(-1, 1)) {
                        val to = target(df, dir)
                        if (to < 0) continue
                        if (isEnemy(board[to], white) || to == enPassant) add(to)
                    }
                }
                'N' -> for ((df, dr) in KNIGHT_STEPS) {
                    val to = target(df, dr)
                    if (to >= 0 && !isOwn(board[to], white)) moves += RefMove(from, to)
                }
                'K' -> {
                    for ((df, dr) in KING_STEPS) {
                        val to = target(df, dr)
                        if (to >= 0 && !isOwn(board[to], white)) moves += RefMove(from, to)
                    }
                    val home = if (white) 4 else 60
                    if (from == home && !isAttacked(home, !white)) {
                        val rook = if (white) 'R' else 'r'
                        val kingSide = castling[if (white) 0 else 2]
                        val queenSide = castling[if (white) 1 else 3]
                        if (kingSide && board[home + 3] == rook && board[home + 1] == '.' && board[home + 2] == '.' &&
                            !isAttacked(home + 1, !white) && !isAttacked(home + 2, !white)
                        ) {
                            moves += RefMove(home, home + 2)
                        }
                        if (queenSide && board[home - 4] == rook && board[home - 1] == '.' && board[home - 2] == '.' &&
                            board[home - 3] == '.' && !isAttacked(home - 1, !white) && !isAttacked(home - 2, !white)
                        ) {
                            moves += RefMove(home, home - 2)
                        }
                    }
                }
                else -> for ((df, dr) in KING_STEPS) {
                    val diagonal = df != 0 && dr != 0
                    if (c.uppercaseChar() == 'B' && !diagonal) continue
                    if (c.uppercaseChar() == 'R' && diagonal) continue
                    var n = 1
                    while (true) {
                        val to = target(df * n, dr * n)
                        if (to < 0 || isOwn(board[to], white)) break
                        moves += RefMove(from, to)
                        if (board[to] != '.') break
                        n++
                    }
                }
            }
        }
        return moves
    }

    fun play(move: RefMove): ReferencePosition {
        val next = copy()
        val b = next.board
        val piece = b[move.from]
        val white = whiteToMove
        val isPawn = piece.uppercaseChar() == 'P'
        if (isPawn && move.to == enPassant) b[move.to + (if (white) -8 else 8)] = '.'
        if (piece.uppercaseChar() == 'K' && kotlin.math.abs(move.to - move.from) == 2) {
            if (move.to > move.from) {
                b[move.to - 1] = b[move.to + 1]
                b[move.to + 1] = '.'
            } else {
                b[move.to + 1] = b[move.to - 2]
                b[move.to - 2] = '.'
            }
        }
        b[move.to] = when {
            move.promotion == null -> piece
            white -> move.promotion
            else -> move.promotion.lowercaseChar()
        }
        b[move.from] = '.'
        for (sq in intArrayOf(move.from, move.to)) {
            when (sq) {
                4 -> { next.castling[0] = false; next.castling[1] = false }
                7 -> next.castling[0] = false
                0 -> next.castling[1] = false
                60 -> { next.castling[2] = false; next.castling[3] = false }
                63 -> next.castling[2] = false
                56 -> next.castling[3] = false
            }
        }
        next.enPassant = if (isPawn && kotlin.math.abs(move.to - move.from) == 16) (move.from + move.to) / 2 else -1
        next.whiteToMove = !white
        return next
    }

    fun legalMoves(): List<RefMove> = pseudoLegal().filter { move ->
        val next = play(move)
        val k = next.kingSquare(whiteToMove)
        k < 0 || !next.isAttacked(k, !whiteToMove)
    }

    /** Placement, side, castling and en-passant fields of the FEN (no clocks). */
    fun fenPrefix(): String {
        val sb = StringBuilder()
        for (rank in 7 downTo 0) {
            var empty = 0
            for (file in 0..7) {
                val c = board[rank * 8 + file]
                if (c == '.') {
                    empty++
                } else {
                    if (empty > 0) sb.append(empty)
                    empty = 0
                    sb.append(c)
                }
            }
            if (empty > 0) sb.append(empty)
            if (rank > 0) sb.append('/')
        }
        sb.append(if (whiteToMove) " w " else " b ")
        val rights = "KQkq".filterIndexed { i, _ -> castling[i] }
        sb.append(rights.ifEmpty { "-" })
        sb.append(' ').append(if (enPassant < 0) "-" else Square.name(enPassant))
        return sb.toString()
    }

    companion object {
        private val KNIGHT_STEPS = listOf(1 to 2, 2 to 1, 2 to -1, 1 to -2, -1 to -2, -2 to -1, -2 to 1, -1 to 2)
        private val KING_STEPS = listOf(1 to 0, 1 to 1, 0 to 1, -1 to 1, -1 to 0, -1 to -1, 0 to -1, 1 to -1)

        /** Only for well-formed, consistent six-field FENs (the oracle does no normalisation). */
        fun fromFen(fen: String): ReferencePosition {
            val fields = fen.split(' ')
            val board = CharArray(64) { '.' }
            for ((i, rankText) in fields[0].split('/').withIndex()) {
                var file = 0
                for (c in rankText) {
                    if (c.isDigit()) {
                        file += c - '0'
                    } else {
                        board[(7 - i) * 8 + file] = c
                        file++
                    }
                }
            }
            val castling = BooleanArray(4) { "KQkq"[it] in fields[2] }
            val ep = if (fields[3] == "-") -1 else Square.parse(fields[3])
            return ReferencePosition(board, fields[1] == "w", castling, ep)
        }
    }
}
