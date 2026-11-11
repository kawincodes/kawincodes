package com.krstech.chesshelp.chess

import java.util.Collections

/**
 * An immutable chess position (the six FEN fields). Equality and hash code cover the full state.
 *
 * Any placement is accepted, including ones that cannot occur in a real game (use
 * [PositionValidator] to judge plausibility); the rules are then applied as far as they make sense.
 * Castling rights and the en-passant square are normalised on construction: a right is only kept
 * while king and rook stand on their home squares, and the en-passant square only when a pawn of
 * the side not to move stands in front of it as after a double push.
 */
class Position private constructor(
    private val codes: ByteArray,
    val sideToMove: Side,
    private val castling: Int,
    /** Square "behind" a pawn that has just advanced two squares, or -1. */
    val enPassantSquare: Int,
    val halfmoveClock: Int,
    val fullmoveNumber: Int,
) {
    private val legal: IntArray by lazy {
        val board = toBoard()
        var buf = IntArray(Board.MAX_MOVES)
        var n = board.generateMoves(buf)
        if (n == buf.size) {
            // Only absurd placements (dozens of queens) get here; 64 pieces x 27 targets is the ceiling.
            buf = IntArray(2048)
            n = board.generateMoves(buf)
        }
        buf.copyOf(n)
    }

    private val legalList: List<Move> by lazy {
        Collections.unmodifiableList(legal.map { Moves.toMove(it) })
    }

    private val inCheck: Boolean by lazy { toBoard().inCheck() }

    fun pieceAt(sq: Int): Piece? {
        require(sq in 0..63) { "square index must be 0..63 (got $sq)" }
        val code = codes[sq].toInt()
        return if (code < 0) null else PIECES[code]
    }

    fun canCastle(side: Side, kingSide: Boolean): Boolean {
        val bit = when {
            side == Side.WHITE && kingSide -> Board.WK
            side == Side.WHITE -> Board.WQ
            kingSide -> Board.BK
            else -> Board.BQ
        }
        return castling and bit != 0
    }

    fun toFen(): String {
        val sb = StringBuilder(90)
        appendPlacement(sb)
        sb.append(' ').append(sideToMove.fenChar).append(' ')
        if (castling == 0) {
            sb.append('-')
        } else {
            if (castling and Board.WK != 0) sb.append('K')
            if (castling and Board.WQ != 0) sb.append('Q')
            if (castling and Board.BK != 0) sb.append('k')
            if (castling and Board.BQ != 0) sb.append('q')
        }
        sb.append(' ').append(if (enPassantSquare < 0) "-" else Square.name(enPassantSquare))
        sb.append(' ').append(halfmoveClock).append(' ').append(fullmoveNumber)
        return sb.toString()
    }

    /** The first FEN field, e.g. `rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR`. */
    fun placement(): String = StringBuilder(72).also { appendPlacement(it) }.toString()

    private fun appendPlacement(sb: StringBuilder) {
        for (rank in 7 downTo 0) {
            var empty = 0
            for (file in 0..7) {
                val code = codes[rank * 8 + file].toInt()
                if (code < 0) {
                    empty++
                } else {
                    if (empty > 0) sb.append(empty)
                    empty = 0
                    sb.append(PIECES[code]!!.fenChar)
                }
            }
            if (empty > 0) sb.append(empty)
            if (rank > 0) sb.append('/')
        }
    }

    /** All legal moves of the side to move (a pawn reaching the last rank yields four moves). */
    fun legalMoves(): List<Move> = legalList

    /** A promotion must name its piece: `Move(e7, e8)` without one is not legal. */
    fun isLegal(move: Move): Boolean = encoded(move) != Moves.NONE

    fun play(move: Move): Position {
        val m = encoded(move)
        require(m != Moves.NONE) { "Illegal move ${describe(move)} in position ${toFen()}" }
        val board = toBoard()
        board.make(m)
        return board.toPosition()
    }

    /** True when the side to move is in check. */
    fun isInCheck(): Boolean = inCheck

    fun isCheckmate(): Boolean = inCheck && legal.isEmpty()

    fun isStalemate(): Boolean = !inCheck && legal.isEmpty()

    /**
     * True when neither side can possibly mate: K v K, K + one minor piece v K, or kings with
     * bishops that all stand on squares of the same colour (e.g. K+B v K+B with like bishops).
     */
    fun isInsufficientMaterial(): Boolean = toBoard().isInsufficientMaterial()

    /** Standard algebraic notation of a legal [move]; `IllegalArgumentException` if it is not legal. */
    fun san(move: Move): String {
        val m = encoded(move)
        require(m != Moves.NONE) { "Illegal move ${describe(move)} in position ${toFen()}" }
        return San.format(toBoard(), m, legal, legal.size)
    }

    /**
     * Parses SAN or UCI text into a legal move of this position, or `null` unless exactly one
     * legal move matches. Lenient about `0-0`, missing or extra `x` / `+` / `#` / `=`, annotations
     * (`!?`), move numbers (`12...`), `e.p.`, long algebraic (`Ng1-f3`), over-specified origins
     * and lower-case piece letters where that is unambiguous.
     */
    fun parseMove(text: String): Move? {
        val m = San.parse(text, legal, legal.size)
        return if (m == Moves.NONE) null else Moves.toMove(m)
    }

    /**
     * The same placement with [side] to move. Changing the side clears the en-passant square (it
     * would belong to the wrong player); asking for the current side returns this position unchanged.
     */
    fun withSideToMove(side: Side): Position =
        if (side == sideToMove) this else Position(codes, side, castling, -1, halfmoveClock, fullmoveNumber)

    /** A fresh mutable [Board] holding this position (empty make-history). */
    internal fun toBoard(): Board {
        val board = Board()
        board.setup(codes, sideToMove.ordinal, castling, enPassantSquare, halfmoveClock, fullmoveNumber)
        return board
    }

    private fun encoded(move: Move): Int {
        if (move.promotion == PieceType.PAWN || move.promotion == PieceType.KING) return Moves.NONE
        val promotion = move.promotion?.ordinal ?: 0
        for (m in legal) {
            if (Moves.from(m) == move.from && Moves.to(m) == move.to && Moves.promotion(m) == promotion) return m
        }
        return Moves.NONE
    }

    private fun describe(move: Move): String =
        if (move.from in 0..63 && move.to in 0..63) move.uci() else "(${move.from} -> ${move.to})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Position) return false
        return sideToMove == other.sideToMove && castling == other.castling &&
            enPassantSquare == other.enPassantSquare && halfmoveClock == other.halfmoveClock &&
            fullmoveNumber == other.fullmoveNumber && codes.contentEquals(other.codes)
    }

    override fun hashCode(): Int {
        var h = codes.contentHashCode()
        h = h * 31 + sideToMove.ordinal
        h = h * 31 + castling
        h = h * 31 + enPassantSquare
        h = h * 31 + halfmoveClock
        h = h * 31 + fullmoveNumber
        return h
    }

    override fun toString(): String = toFen()

    companion object {
        const val START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

        private val PIECES: Array<Piece?> = arrayOfNulls<Piece>(14).also { table ->
            for (side in Side.entries) {
                for (type in PieceType.entries) table[Board.pieceCode(side.ordinal, type.ordinal)] = Piece(side, type)
            }
        }

        private val WHITESPACE = Regex("\\s+")

        /** The initial position of a game. */
        val START: Position = fromFen(START_FEN)

        /**
         * Parses a FEN string. Trailing fields may be missing: side to move defaults to White,
         * castling rights to "whatever king and rook on their home squares allow", en passant to
         * none, the clocks to `0 1` — so a bare placement is accepted.
         *
         * @throws IllegalArgumentException with a message naming the offending field.
         */
        fun fromFen(fen: String): Position {
            val fields = fen.trim().split(WHITESPACE).filter { it.isNotEmpty() }
            require(fields.isNotEmpty()) { "FEN is empty" }
            require(fields.size <= 6) { "FEN has ${fields.size} fields, expected at most 6: \"$fen\"" }
            val codes = parsePlacement(fields[0])
            val side = if (fields.size > 1) parseSide(fields[1]) else Board.WHITE
            val castling = if (fields.size > 2) parseCastling(fields[2]) else 15
            val enPassant = if (fields.size > 3) parseEnPassant(fields[3]) else -1
            val halfmove = if (fields.size > 4) parseCounter(fields[4], "halfmove clock") else 0
            val fullmove = if (fields.size > 5) parseCounter(fields[5], "fullmove number") else 1
            return normalised(codes, side, castling, enPassant, halfmove, fullmove)
        }

        /**
         * Builds a position from 64 pieces indexed by square. With [inferCastling] every castling
         * right whose king and rook stand on their home squares is assumed to be available;
         * otherwise there are none. An [enPassantSquare] that does not fit the placement is ignored.
         */
        fun fromBoard(
            pieces: Array<Piece?>,
            sideToMove: Side,
            inferCastling: Boolean = true,
            enPassantSquare: Int = -1,
        ): Position {
            require(pieces.size == 64) { "expected 64 squares, got ${pieces.size}" }
            require(enPassantSquare in -1..63) { "en passant square must be -1 or 0..63 (got $enPassantSquare)" }
            return normalised(codesOf(pieces), sideToMove.ordinal, if (inferCastling) 15 else 0, enPassantSquare, 0, 1)
        }

        /** [codes] must not be modified afterwards and the state must already be normalised (as [Board] keeps it). */
        internal fun fromState(
            codes: ByteArray,
            side: Int,
            castling: Int,
            enPassantSquare: Int,
            halfmoveClock: Int,
            fullmoveNumber: Int,
        ): Position = Position(
            codes,
            if (side == Board.WHITE) Side.WHITE else Side.BLACK,
            castling,
            enPassantSquare,
            halfmoveClock,
            fullmoveNumber,
        )

        internal fun codesOf(pieces: Array<Piece?>): ByteArray = ByteArray(pieces.size) { sq ->
            val piece = pieces[sq]
            if (piece == null) Board.NO_PIECE.toByte() else Board.pieceCode(piece.side.ordinal, piece.type.ordinal).toByte()
        }

        private fun normalised(codes: ByteArray, side: Int, castling: Int, ep: Int, halfmove: Int, fullmove: Int): Position {
            val board = Board()
            board.setup(codes, side, castling, ep, halfmove, fullmove)
            return board.toPosition()
        }

        private fun parsePlacement(placement: String): ByteArray {
            val ranks = placement.split('/')
            require(ranks.size == 8) {
                "FEN placement must have 8 ranks separated by '/', found ${ranks.size} in \"$placement\""
            }
            val codes = ByteArray(64) { Board.NO_PIECE.toByte() }
            for (i in 0 until 8) {
                val rank = 7 - i
                val text = ranks[i]
                var file = 0
                for (c in text) {
                    if (c in '1'..'8') {
                        file += c - '0'
                    } else {
                        val piece = Piece.fromFenChar(c)
                            ?: throw IllegalArgumentException(
                                "FEN placement: unexpected character '$c' in rank ${rank + 1} (\"$text\")",
                            )
                        if (file < 8) codes[rank * 8 + file] = Board.pieceCode(piece.side.ordinal, piece.type.ordinal).toByte()
                        file++
                    }
                    require(file <= 8) { "FEN placement: rank ${rank + 1} (\"$text\") describes more than 8 squares" }
                }
                require(file == 8) { "FEN placement: rank ${rank + 1} (\"$text\") describes $file squares, expected 8" }
            }
            return codes
        }

        private fun parseSide(text: String): Int = when (text) {
            "w", "W" -> Board.WHITE
            "b", "B" -> Board.BLACK
            else -> throw IllegalArgumentException("FEN side to move must be 'w' or 'b', got \"$text\"")
        }

        private fun parseCastling(text: String): Int {
            if (text == "-") return 0
            var rights = 0
            for (c in text) {
                rights = rights or when (c) {
                    'K' -> Board.WK
                    'Q' -> Board.WQ
                    'k' -> Board.BK
                    'q' -> Board.BQ
                    else -> throw IllegalArgumentException(
                        "FEN castling field must be '-' or a combination of K, Q, k, q; got \"$text\"",
                    )
                }
            }
            return rights
        }

        private fun parseEnPassant(text: String): Int {
            if (text == "-") return -1
            val sq = Square.parse(text)
            require(sq >= 0 && (Square.rank(sq) == 2 || Square.rank(sq) == 5)) {
                "FEN en passant field must be '-' or a square on rank 3 or 6, got \"$text\""
            }
            return sq
        }

        private fun parseCounter(text: String, name: String): Int {
            val value = text.toIntOrNull()
            require(value != null && value >= 0) { "FEN $name must be a non-negative number, got \"$text\"" }
            return value
        }
    }
}

/** SAN formatting and lenient SAN/UCI parsing on top of [Board]'s int-encoded legal moves. */
internal object San {
    private const val AMBIGUOUS = -1
    private const val PIECE_LETTERS = "PNBRQK"

    /** SAN of the legal move [m] in [board]'s current position. The board is left unchanged. */
    fun format(board: Board, m: Int): String {
        val legal = IntArray(Board.MAX_MOVES)
        return format(board, m, legal, board.generateMoves(legal))
    }

    /** As [format], given the already generated legal moves `legal[0 until count]` of the position. */
    fun format(board: Board, m: Int, legal: IntArray, count: Int): String {
        val sb = StringBuilder(8)
        val from = Moves.from(m)
        val to = Moves.to(m)
        val piece = Moves.piece(m)
        if (Moves.isCastle(m)) {
            sb.append(if (to > from) "O-O" else "O-O-O")
        } else {
            if (piece != Board.PAWN) {
                sb.append(PIECE_LETTERS[piece])
                // Other pieces of the same type that can also legally reach the target square.
                var rivals = false
                var sameFile = false
                var sameRank = false
                for (i in 0 until count) {
                    val other = legal[i]
                    val otherFrom = Moves.from(other)
                    if (otherFrom == from || Moves.piece(other) != piece || Moves.to(other) != to) continue
                    rivals = true
                    if (otherFrom and 7 == from and 7) sameFile = true
                    if (otherFrom shr 3 == from shr 3) sameRank = true
                }
                if (rivals) {
                    when {
                        !sameFile -> sb.append('a' + (from and 7))
                        !sameRank -> sb.append('1' + (from shr 3))
                        else -> sb.append(Square.name(from))
                    }
                }
            } else if (Moves.isCapture(m)) {
                sb.append('a' + (from and 7))
            }
            if (Moves.isCapture(m)) sb.append('x')
            sb.append(Square.name(to))
            val promotion = Moves.promotion(m)
            if (promotion != 0) sb.append('=').append(PIECE_LETTERS[promotion])
        }
        board.make(m)
        if (board.inCheck()) sb.append(if (board.hasLegalMoves()) '+' else '#')
        board.unmake()
        return sb.toString()
    }

    /** The one move of `legal[0 until count]` that [text] denotes, or [Moves.NONE]. */
    fun parse(text: String, legal: IntArray, count: Int): Int {
        val s = stripDecorations(text)
        if (s.isEmpty() || s.any { it.isWhitespace() }) return Moves.NONE

        val castle = s.filter { it != '-' }.uppercase().replace('0', 'O')
        if (castle == "OO" || castle == "OOO") {
            val targetFile = if (castle == "OO") 6 else 2
            for (i in 0 until count) {
                if (Moves.isCastle(legal[i]) && Moves.to(legal[i]) and 7 == targetFile) return legal[i]
            }
            return Moves.NONE
        }

        // Case matters here: "b1c3" is UCI, but "B1c3" is a bishop from the first rank.
        if (isUci(s, strictCase = true)) {
            val m = matchUci(s, legal, count)
            if (m != Moves.NONE) return m
        }

        val core = s.filter { it !in "xX×:-=()/" }
        var m = matchSan(core, legal, count)
        if (m == Moves.NONE && core.isNotEmpty() && core[0] in "nbrqk") {
            m = matchSan(core[0].uppercaseChar() + core.substring(1), legal, count)
        }
        if (m == Moves.NONE && isUci(s, strictCase = false)) m = matchUci(s, legal, count)
        return if (m == AMBIGUOUS) Moves.NONE else m
    }

    private fun stripDecorations(text: String): String {
        var s = text.trim()

        // Leading move number: "12.", "12...", "12. ", "…".
        var i = 0
        while (i < s.length && s[i].isDigit()) i++
        var dots = 0
        while (i < s.length && (s[i] == '.' || s[i] == '…' || s[i].isWhitespace())) {
            if (!s[i].isWhitespace()) dots++
            i++
        }
        if (dots > 0) s = s.substring(i)

        while (true) {
            val length = s.length
            s = s.trimEnd { it.isWhitespace() || it in "+#!?.,;" }
            for (suffix in EN_PASSANT_SUFFIXES) {
                if (s.endsWith(suffix, ignoreCase = true)) {
                    s = s.dropLast(suffix.length)
                    break
                }
            }
            if (s.length == length) return s
        }
    }

    private val EN_PASSANT_SUFFIXES = arrayOf("e.p.", "e.p", "ep")

    private fun isUci(s: String, strictCase: Boolean): Boolean {
        if (s.length != 4 && s.length != 5) return false
        val t = if (strictCase) s else s.lowercase()
        if (t[0] !in 'a'..'h' || t[1] !in '1'..'8' || t[2] !in 'a'..'h' || t[3] !in '1'..'8') return false
        return s.length == 4 || s[4].lowercaseChar() in "qrbn"
    }

    private fun matchUci(s: String, legal: IntArray, count: Int): Int {
        val move = Move.fromUci(s) ?: return Moves.NONE
        val promotion = move.promotion?.ordinal ?: 0
        for (i in 0 until count) {
            val m = legal[i]
            if (Moves.from(m) == move.from && Moves.to(m) == move.to && Moves.promotion(m) == promotion) return m
        }
        return Moves.NONE
    }

    /** [core] is SAN without capture/check/promotion punctuation, e.g. `Nbd7`, `exd5`, `e8Q`, `Ng1f3`. */
    private fun matchSan(core: String, legal: IntArray, count: Int): Int {
        var t = core
        var promotion = 0
        if (t.length >= 3 && (t[t.length - 2] == '1' || t[t.length - 2] == '8')) {
            val index = "NBRQ".indexOf(t[t.length - 1].uppercaseChar())
            if (index >= 0) {
                promotion = index + 1
                t = t.dropLast(1)
            }
        }

        var piece = Board.PAWN
        if (t.isNotEmpty() && t[0] in "NBRQK") {
            piece = PIECE_LETTERS.indexOf(t[0])
            t = t.substring(1)
        }
        if (t.length !in 2..4) return Moves.NONE

        val toFile = t[t.length - 2] - 'a'
        val toRank = t[t.length - 1] - '1'
        if (toFile !in 0..7 || toRank !in 0..7) return Moves.NONE
        var fromFile = -1
        var fromRank = -1
        if (t.length == 3) {
            when (val c = t[0]) {
                in 'a'..'h' -> fromFile = c - 'a'
                in '1'..'8' -> fromRank = c - '1'
                else -> return Moves.NONE
            }
        } else if (t.length == 4) {
            fromFile = t[0] - 'a'
            fromRank = t[1] - '1'
            if (fromFile !in 0..7 || fromRank !in 0..7) return Moves.NONE
        }

        val to = toRank * 8 + toFile
        var found = Moves.NONE
        for (i in 0 until count) {
            val m = legal[i]
            if (Moves.piece(m) != piece || Moves.to(m) != to || Moves.promotion(m) != promotion) continue
            val from = Moves.from(m)
            if (fromFile >= 0 && from and 7 != fromFile) continue
            if (fromRank >= 0 && from shr 3 != fromRank) continue
            // "d5" is the pawn push; a pawn capture always names its file ("exd5").
            if (piece == Board.PAWN && fromFile < 0 && from and 7 != toFile) continue
            if (found != Moves.NONE) return AMBIGUOUS
            found = m
        }
        return found
    }
}
