package com.krstech.chesshelp.chess

enum class Side {
    WHITE, BLACK;

    val opposite: Side get() = if (this == WHITE) BLACK else WHITE

    /** `'w'` or `'b'`, as used in the second FEN field. */
    val fenChar: Char get() = if (this == WHITE) 'w' else 'b'
}

enum class PieceType(
    /** Upper-case letter used in FEN and SAN (`'P'` for the pawn, which SAN omits). */
    val letter: Char,
) {
    PAWN('P'), KNIGHT('N'), BISHOP('B'), ROOK('R'), QUEEN('Q'), KING('K');

    companion object {
        private val VALUES = values()

        internal fun ofOrdinal(ordinal: Int): PieceType = VALUES[ordinal]

        /** Accepts either case; `null` for anything that is not a piece letter. */
        fun fromLetter(c: Char): PieceType? = when (c.uppercaseChar()) {
            'P' -> PAWN
            'N' -> KNIGHT
            'B' -> BISHOP
            'R' -> ROOK
            'Q' -> QUEEN
            'K' -> KING
            else -> null
        }
    }
}

data class Piece(val side: Side, val type: PieceType) {
    /** FEN letter: upper case for White (`'N'`), lower case for Black (`'n'`). */
    val fenChar: Char get() = if (side == Side.WHITE) type.letter else type.letter.lowercaseChar()

    companion object {
        fun fromFenChar(c: Char): Piece? {
            val type = PieceType.fromLetter(c) ?: return null
            return Piece(if (c.isUpperCase()) Side.WHITE else Side.BLACK, type)
        }
    }
}

/** Square indices: `a1 = 0, b1 = 1 … h1 = 7, a2 = 8 … h8 = 63`. */
object Square {
    fun of(file: Int, rank: Int): Int {
        require(file in 0..7 && rank in 0..7) { "file and rank must be 0..7 (got file=$file, rank=$rank)" }
        return rank * 8 + file
    }

    fun file(sq: Int): Int = sq and 7

    fun rank(sq: Int): Int = sq shr 3

    fun name(sq: Int): String {
        require(sq in 0..63) { "square index must be 0..63 (got $sq)" }
        return String(charArrayOf('a' + (sq and 7), '1' + (sq shr 3)))
    }

    /** Parses `"e4"` (either case). Returns -1 when [name] is not a square. */
    fun parse(name: String): Int {
        if (name.length != 2) return -1
        val file = name[0].lowercaseChar() - 'a'
        val rank = name[1] - '1'
        return if (file in 0..7 && rank in 0..7) rank * 8 + file else -1
    }
}

data class Move(val from: Int, val to: Int, val promotion: PieceType? = null) {
    /** UCI long algebraic form: `"e2e4"`, `"e7e8q"`. Castling is the king's move (`"e1g1"`). */
    fun uci(): String {
        val base = Square.name(from) + Square.name(to)
        return if (promotion == null) base else base + promotion.letter.lowercaseChar()
    }

    override fun toString(): String = uci()

    companion object {
        /** Parses UCI text (either case). `null` when malformed; legality is not checked here. */
        fun fromUci(text: String): Move? {
            val s = text.trim()
            if (s.length != 4 && s.length != 5) return null
            val from = Square.parse(s.substring(0, 2))
            val to = Square.parse(s.substring(2, 4))
            if (from < 0 || to < 0 || from == to) return null
            var promotion: PieceType? = null
            if (s.length == 5) {
                promotion = PieceType.fromLetter(s[4])
                if (promotion == null || promotion == PieceType.PAWN || promotion == PieceType.KING) return null
            }
            return Move(from, to, promotion)
        }
    }
}
