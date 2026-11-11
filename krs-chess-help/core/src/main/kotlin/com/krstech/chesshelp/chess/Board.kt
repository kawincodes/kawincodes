package com.krstech.chesshelp.chess

/**
 * Int-encoded moves used by [Board] (no allocation in the generator or the search).
 *
 * Layout: bits 0-5 from, 6-11 to, 12-14 moving piece type, 15-17 captured piece type + 1 (0 = no
 * capture; an en-passant capture stores the pawn), 18-20 promotion piece type (0 = none, else
 * [Board.KNIGHT]..[Board.QUEEN]), 21-22 kind. A move therefore carries everything needed for
 * ordering (MVV/LVA etc.) without looking at the board. [NONE] (0) is never a real move.
 */
internal object Moves {
    const val NONE = 0

    const val KIND_NORMAL = 0
    const val KIND_DOUBLE_PUSH = 1
    const val KIND_EN_PASSANT = 2
    const val KIND_CASTLE = 3

    private const val CAPTURE_MASK = 7 shl 15
    private const val PROMOTION_MASK = 7 shl 18

    /** [captured] is a piece type or -1, [promotion] a piece type or 0. */
    fun encode(from: Int, to: Int, piece: Int, captured: Int, promotion: Int, kind: Int): Int =
        from or (to shl 6) or (piece shl 12) or ((captured + 1) shl 15) or (promotion shl 18) or (kind shl 21)

    fun from(m: Int): Int = m and 63
    fun to(m: Int): Int = (m ushr 6) and 63

    /** Type of the moving piece (the pawn for promotions, the king for castling). */
    fun piece(m: Int): Int = (m ushr 12) and 7

    /** Type of the captured piece, or -1. */
    fun captured(m: Int): Int = ((m ushr 15) and 7) - 1

    /** Promotion piece type, or 0 when the move is not a promotion. */
    fun promotion(m: Int): Int = (m ushr 18) and 7

    fun kind(m: Int): Int = (m ushr 21) and 3
    fun isCapture(m: Int): Boolean = m and CAPTURE_MASK != 0
    fun isPromotion(m: Int): Boolean = m and PROMOTION_MASK != 0
    fun isEnPassant(m: Int): Boolean = kind(m) == KIND_EN_PASSANT
    fun isCastle(m: Int): Boolean = kind(m) == KIND_CASTLE

    fun toMove(m: Int): Move {
        val promotion = promotion(m)
        return Move(from(m), to(m), if (promotion == 0) null else PieceType.ofOrdinal(promotion))
    }

    fun uci(m: Int): String = toMove(m).uci()
}

/**
 * The one mutable chess board of this module: rules, move generation and make/unmake. [Position]
 * (immutable, public) and the search engine are both built on it, so there is a single move
 * generator to validate (see the perft tests).
 *
 * Representation: bitboards per piece type and per side plus a 64-entry mailbox. Sides are
 * [WHITE] = 0 / [BLACK] = 1, piece types [PAWN]..[KING] = 0..5 (the same numbers as
 * `Side.ordinal` / `PieceType.ordinal`), a piece code is `(side shl 3) or type`, [NO_PIECE] = -1.
 *
 * Typical use:
 * ```
 * val board = position.toBoard()
 * val moves = IntArray(Board.MAX_MOVES)
 * val n = board.generateMoves(moves)          // strictly legal moves, see Moves for the encoding
 * for (i in 0 until n) { board.make(moves[i]); …; board.unmake() }
 * ```
 *
 * - [generateMoves] / [generateCaptures] produce **strictly legal** moves (pins, check evasions,
 *   castling through check and the en-passant rank pin are all resolved in the generator), so no
 *   legality test is needed after [make]. "No moves" therefore means mate ([inCheck]) or stalemate.
 * - [make] only accepts moves produced by this board's generator for the current position.
 * - [key] is a Zobrist hash maintained incrementally; [isRepetition] scans the make-history.
 * - The board never throws on implausible placements (missing kings, pawns on the back ranks,
 *   60 queens …): it then simply follows the piece-movement rules it can still apply.
 *
 * Not thread-safe; use one instance per thread ([copy] is cheap).
 */
internal class Board {
    private val byType = LongArray(6)
    private val bySide = LongArray(2)
    private val squares = IntArray(64) { NO_PIECE }

    /** Side to move: [WHITE] or [BLACK]. */
    var side: Int = WHITE
        private set

    /** Castling rights as a bit set of [WK], [WQ], [BK], [BQ]. */
    var castling: Int = 0
        private set

    /** En-passant target square (set after every double pawn push, as in FEN), or -1. */
    var epSquare: Int = -1
        private set

    var halfmoveClock: Int = 0
        private set

    var fullmoveNumber: Int = 1
        private set

    /** Zobrist key of the current position (pieces, side to move, castling rights, capturable en-passant file). */
    var key: Long = 0L
        private set

    /** Number of moves made (and not yet unmade) since the last [setup]. */
    var ply: Int = 0
        private set

    private var undoMove = IntArray(INITIAL_HISTORY)
    private var undoState = IntArray(INITIAL_HISTORY)
    private var undoHalfmove = IntArray(INITIAL_HISTORY)
    private var undoKey = LongArray(INITIAL_HISTORY)
    private val oneMove = IntArray(1)

    // ------------------------------------------------------------------ setup

    /**
     * Replaces the whole state and clears the history. [pieceCodes] holds a piece code or
     * [NO_PIECE] per square. Castling rights without king and rook on their home squares and an
     * en-passant square that cannot result from a double pawn push are silently dropped, so the
     * state is always internally consistent.
     */
    fun setup(pieceCodes: ByteArray, side: Int, castling: Int, epSquare: Int, halfmoveClock: Int, fullmoveNumber: Int) {
        require(pieceCodes.size == 64) { "expected 64 squares, got ${pieceCodes.size}" }
        byType.fill(0L)
        bySide.fill(0L)
        for (sq in 0 until 64) {
            val code = pieceCodes[sq].toInt()
            if (code < 0) {
                squares[sq] = NO_PIECE
            } else {
                require(code and 7 <= KING && code ushr 3 <= BLACK) { "invalid piece code $code on square $sq" }
                addPiece(code ushr 3, code and 7, sq)
            }
        }
        this.side = if (side == BLACK) BLACK else WHITE
        this.castling = sanitizeCastling(castling)
        this.epSquare = if (isPlausibleEnPassant(epSquare)) epSquare else -1
        this.halfmoveClock = maxOf(0, halfmoveClock)
        this.fullmoveNumber = maxOf(1, fullmoveNumber)
        ply = 0
        key = computeKey()
    }

    /** An independent board with the same position and an empty make-history. */
    fun copy(): Board {
        val other = Board()
        byType.copyInto(other.byType)
        bySide.copyInto(other.bySide)
        squares.copyInto(other.squares)
        other.side = side
        other.castling = castling
        other.epSquare = epSquare
        other.halfmoveClock = halfmoveClock
        other.fullmoveNumber = fullmoveNumber
        other.key = key
        return other
    }

    /** Immutable snapshot of the current state (the make-history is not carried over). */
    fun toPosition(): Position {
        val codes = ByteArray(64) { squares[it].toByte() }
        return Position.fromState(codes, side, castling, epSquare, halfmoveClock, fullmoveNumber)
    }

    private fun sanitizeCastling(requested: Int): Int {
        var rights = requested and 15
        if (squares[E1] != W_KING) rights = rights and (WK or WQ).inv()
        if (squares[H1] != W_ROOK) rights = rights and WK.inv()
        if (squares[A1] != W_ROOK) rights = rights and WQ.inv()
        if (squares[E8] != B_KING) rights = rights and (BK or BQ).inv()
        if (squares[H8] != B_ROOK) rights = rights and BK.inv()
        if (squares[A8] != B_ROOK) rights = rights and BQ.inv()
        return rights
    }

    private fun isPlausibleEnPassant(ep: Int): Boolean {
        if (ep !in 0..63 || squares[ep] != NO_PIECE) return false
        return if (side == WHITE) {
            ep shr 3 == 5 && squares[ep - 8] == pieceCode(BLACK, PAWN) && squares[ep + 8] == NO_PIECE
        } else {
            ep shr 3 == 2 && squares[ep + 8] == pieceCode(WHITE, PAWN) && squares[ep - 8] == NO_PIECE
        }
    }

    // ------------------------------------------------------------------ queries

    /** Piece code on [sq] (`(side shl 3) or type`) or [NO_PIECE]. */
    fun pieceAt(sq: Int): Int = squares[sq]

    /** Bitboard of all pieces of [type], both sides. */
    fun pieces(type: Int): Long = byType[type]

    /** Bitboard of the pieces of [type] belonging to [side]. */
    fun pieces(side: Int, type: Int): Long = byType[type] and bySide[side]

    /** Bitboard of all pieces of [side]. */
    fun occupancy(side: Int): Long = bySide[side]

    /** Bitboard of all pieces. */
    val occupied: Long get() = bySide[WHITE] or bySide[BLACK]

    /** Square of [side]'s king, or -1 when there is none (lowest square when there are several). */
    fun kingSquare(side: Int): Int {
        val kings = byType[KING] and bySide[side]
        return if (kings == 0L) -1 else java.lang.Long.numberOfTrailingZeros(kings)
    }

    /** True when any piece of side [by] attacks [sq] (the piece standing on [sq] is irrelevant). */
    fun isSquareAttacked(sq: Int, by: Int): Boolean = isAttacked(sq, by, occupied)

    /** Bitboard of the pieces of side [by] that attack [sq]. */
    fun attackers(sq: Int, by: Int): Long = attackersBy(by, sq, occupied)

    /** Bitboard of the enemy pieces giving check to the side to move (0 when not in check). */
    fun checkers(): Long {
        val ksq = kingSquare(side)
        return if (ksq < 0) 0L else attackersBy(side xor 1, ksq, occupied)
    }

    /** True when the side to move is in check. */
    fun inCheck(): Boolean {
        val ksq = kingSquare(side)
        return ksq >= 0 && isAttacked(ksq, side xor 1, occupied)
    }

    /** True when the side to move has at least one legal move. */
    fun hasLegalMoves(): Boolean = generate(oneMove, 0, false) > 0

    /**
     * True when the current position occurred before on this board's make-history (same [key], same
     * side to move), looking back no further than the last irreversible move. Positions from before
     * [setup] are unknown to the board.
     */
    fun isRepetition(): Boolean {
        val limit = minOf(halfmoveClock, ply)
        var back = 4
        while (back <= limit) {
            if (undoKey[ply - back] == key) return true
            back += 2
        }
        return false
    }

    /** K v K, K + one minor v K, or only bishops that all stand on squares of one colour. */
    fun isInsufficientMaterial(): Boolean {
        if (byType[PAWN] or byType[ROOK] or byType[QUEEN] != 0L) return false
        val knights = byType[KNIGHT]
        val bishops = byType[BISHOP]
        if (knights == 0L) {
            return bishops and LIGHT_SQUARES == 0L || bishops and LIGHT_SQUARES.inv() == 0L
        }
        return bishops == 0L && knights and (knights - 1) == 0L
    }

    private fun isAttacked(sq: Int, by: Int, occ: Long): Boolean {
        val attackers = bySide[by]
        if (Attacks.PAWN[(by xor 1) * 64 + sq] and byType[PAWN] and attackers != 0L) return true
        if (Attacks.KNIGHT[sq] and byType[KNIGHT] and attackers != 0L) return true
        if (Attacks.KING[sq] and byType[KING] and attackers != 0L) return true
        if (Attacks.bishop(sq, occ) and (byType[BISHOP] or byType[QUEEN]) and attackers != 0L) return true
        return Attacks.rook(sq, occ) and (byType[ROOK] or byType[QUEEN]) and attackers != 0L
    }

    private fun attackersBy(by: Int, sq: Int, occ: Long): Long =
        (
            (Attacks.PAWN[(by xor 1) * 64 + sq] and byType[PAWN]) or
                (Attacks.KNIGHT[sq] and byType[KNIGHT]) or
                (Attacks.KING[sq] and byType[KING]) or
                (Attacks.bishop(sq, occ) and (byType[BISHOP] or byType[QUEEN])) or
                (Attacks.rook(sq, occ) and (byType[ROOK] or byType[QUEEN]))
            ) and bySide[by]

    /** Pieces of [us] that are the only piece between their king on [ksq] and an enemy slider aiming at it. */
    private fun pinnedTo(ksq: Int, us: Int, occ: Long): Long {
        var snipers = (
            (Attacks.rook(ksq, 0L) and (byType[ROOK] or byType[QUEEN])) or
                (Attacks.bishop(ksq, 0L) and (byType[BISHOP] or byType[QUEEN]))
            ) and bySide[us xor 1]
        var pinned = 0L
        while (snipers != 0L) {
            val sniper = java.lang.Long.numberOfTrailingZeros(snipers)
            snipers = snipers and (snipers - 1)
            val blockers = Attacks.BETWEEN[ksq * 64 + sniper] and occ
            if (blockers != 0L && blockers and (blockers - 1) == 0L) pinned = pinned or (blockers and bySide[us])
        }
        return pinned
    }

    // ------------------------------------------------------------------ move generation

    /**
     * Writes every legal move of the side to move into [buf] starting at [offset] and returns the
     * end index (exclusive), i.e. the number of moves when [offset] is 0. Moves that do not fit
     * into [buf] are dropped rather than throwing; [MAX_MOVES] entries are enough for any position
     * reachable in a real game (the known maximum is 218).
     */
    fun generateMoves(buf: IntArray, offset: Int = 0): Int = generate(buf, offset, false)

    /**
     * Like [generateMoves] but restricted to "noisy" moves: exactly the legal moves that capture
     * (including en passant) and/or promote (all four promotion pieces, capturing or not).
     */
    fun generateCaptures(buf: IntArray, offset: Int = 0): Int = generate(buf, offset, true)

    private fun generate(buf: IntArray, start: Int, noisyOnly: Boolean): Int {
        var n = start
        val us = side
        val them = us xor 1
        val own = bySide[us]
        val opp = bySide[them]
        val occ = own or opp
        val ksq = kingSquare(us)

        var checkers = 0L
        var pinned = 0L
        // Squares a non-king piece may move to: everything, or (in single check) the checker and
        // the squares between it and the king, or nothing (double check).
        var evasion = -1L
        if (ksq >= 0) {
            checkers = attackersBy(them, ksq, occ)
            pinned = pinnedTo(ksq, us, occ)
            if (checkers != 0L) {
                evasion = if (checkers and (checkers - 1) != 0L) {
                    0L
                } else {
                    Attacks.BETWEEN[ksq * 64 + java.lang.Long.numberOfTrailingZeros(checkers)] or checkers
                }
            }
        }
        val lineBase = ksq * 64 // only read when `pinned` is non-zero, which implies a king

        if (evasion != 0L) {
            val target = (if (noisyOnly) opp else own.inv()) and evasion

            // Pawns.
            val pawns = byType[PAWN] and own
            val empty = occ.inv()
            if (us == WHITE) {
                val single = (pawns shl 8) and empty
                val pushTargets = if (noisyOnly) single and Attacks.RANK_8 else single
                n = addPawnMoves(buf, n, pushTargets and evasion, 8, false, Moves.KIND_NORMAL, pinned, lineBase)
                if (!noisyOnly) {
                    val double = ((single and Attacks.RANK_3) shl 8) and empty and evasion
                    n = addPawnMoves(buf, n, double, 16, false, Moves.KIND_DOUBLE_PUSH, pinned, lineBase)
                }
                val left = ((pawns and Attacks.FILE_A.inv()) shl 7) and opp and evasion
                n = addPawnMoves(buf, n, left, 7, true, Moves.KIND_NORMAL, pinned, lineBase)
                val right = ((pawns and Attacks.FILE_H.inv()) shl 9) and opp and evasion
                n = addPawnMoves(buf, n, right, 9, true, Moves.KIND_NORMAL, pinned, lineBase)
            } else {
                val single = (pawns ushr 8) and empty
                val pushTargets = if (noisyOnly) single and Attacks.RANK_1 else single
                n = addPawnMoves(buf, n, pushTargets and evasion, -8, false, Moves.KIND_NORMAL, pinned, lineBase)
                if (!noisyOnly) {
                    val double = ((single and Attacks.RANK_6) ushr 8) and empty and evasion
                    n = addPawnMoves(buf, n, double, -16, false, Moves.KIND_DOUBLE_PUSH, pinned, lineBase)
                }
                val left = ((pawns and Attacks.FILE_A.inv()) ushr 9) and opp and evasion
                n = addPawnMoves(buf, n, left, -9, true, Moves.KIND_NORMAL, pinned, lineBase)
                val right = ((pawns and Attacks.FILE_H.inv()) ushr 7) and opp and evasion
                n = addPawnMoves(buf, n, right, -7, true, Moves.KIND_NORMAL, pinned, lineBase)
            }

            // En passant: rare, so it gets a full "is my king safe afterwards" test instead of the
            // pin/evasion masks (it removes two pieces from one rank and may capture a checking pawn).
            val ep = epSquare
            if (ep >= 0) {
                var candidates = Attacks.PAWN[them * 64 + ep] and pawns
                while (candidates != 0L) {
                    val from = java.lang.Long.numberOfTrailingZeros(candidates)
                    candidates = candidates and (candidates - 1)
                    if (isEnPassantLegal(from, ep, ksq, us, occ, checkers)) {
                        n = push(buf, n, Moves.encode(from, ep, PAWN, PAWN, 0, Moves.KIND_EN_PASSANT))
                    }
                }
            }

            // Knights (a pinned knight can never move).
            var bb = byType[KNIGHT] and own and pinned.inv()
            while (bb != 0L) {
                val from = java.lang.Long.numberOfTrailingZeros(bb)
                bb = bb and (bb - 1)
                n = addPieceMoves(buf, n, from, KNIGHT, Attacks.KNIGHT[from] and target)
            }

            // Sliders (a pinned slider may only move along the pin line).
            bb = byType[BISHOP] and own
            while (bb != 0L) {
                val from = java.lang.Long.numberOfTrailingZeros(bb)
                bb = bb and (bb - 1)
                var targets = Attacks.bishop(from, occ) and target
                if (pinned and (1L shl from) != 0L) targets = targets and Attacks.LINE[lineBase + from]
                n = addPieceMoves(buf, n, from, BISHOP, targets)
            }
            bb = byType[ROOK] and own
            while (bb != 0L) {
                val from = java.lang.Long.numberOfTrailingZeros(bb)
                bb = bb and (bb - 1)
                var targets = Attacks.rook(from, occ) and target
                if (pinned and (1L shl from) != 0L) targets = targets and Attacks.LINE[lineBase + from]
                n = addPieceMoves(buf, n, from, ROOK, targets)
            }
            bb = byType[QUEEN] and own
            while (bb != 0L) {
                val from = java.lang.Long.numberOfTrailingZeros(bb)
                bb = bb and (bb - 1)
                var targets = Attacks.queen(from, occ) and target
                if (pinned and (1L shl from) != 0L) targets = targets and Attacks.LINE[lineBase + from]
                n = addPieceMoves(buf, n, from, QUEEN, targets)
            }
        }

        if (ksq >= 0) {
            // King steps: the destination must not be attacked once the king has left its square
            // (so a slider giving check also covers the square "behind" the king).
            var targets = Attacks.KING[ksq] and (if (noisyOnly) opp else own.inv())
            val occWithoutKing = occ xor (1L shl ksq)
            var safe = 0L
            while (targets != 0L) {
                val to = java.lang.Long.numberOfTrailingZeros(targets)
                targets = targets and (targets - 1)
                if (!isAttacked(to, them, occWithoutKing)) safe = safe or (1L shl to)
            }
            n = addPieceMoves(buf, n, ksq, KING, safe)

            // Castling: rights imply king and rook are on their home squares; the squares between
            // must be empty and the king may not start on, cross or land on an attacked square.
            if (!noisyOnly && checkers == 0L && castling != 0) {
                if (us == WHITE) {
                    if (castling and WK != 0 && occ and WK_EMPTY == 0L &&
                        !isAttacked(F1, them, occ) && !isAttacked(G1, them, occ)
                    ) {
                        n = push(buf, n, Moves.encode(E1, G1, KING, -1, 0, Moves.KIND_CASTLE))
                    }
                    if (castling and WQ != 0 && occ and WQ_EMPTY == 0L &&
                        !isAttacked(D1, them, occ) && !isAttacked(C1, them, occ)
                    ) {
                        n = push(buf, n, Moves.encode(E1, C1, KING, -1, 0, Moves.KIND_CASTLE))
                    }
                } else {
                    if (castling and BK != 0 && occ and BK_EMPTY == 0L &&
                        !isAttacked(F8, them, occ) && !isAttacked(G8, them, occ)
                    ) {
                        n = push(buf, n, Moves.encode(E8, G8, KING, -1, 0, Moves.KIND_CASTLE))
                    }
                    if (castling and BQ != 0 && occ and BQ_EMPTY == 0L &&
                        !isAttacked(D8, them, occ) && !isAttacked(C8, them, occ)
                    ) {
                        n = push(buf, n, Moves.encode(E8, C8, KING, -1, 0, Moves.KIND_CASTLE))
                    }
                }
            }
        }
        return n
    }

    private fun push(buf: IntArray, n: Int, move: Int): Int {
        if (n >= buf.size) return n
        buf[n] = move
        return n + 1
    }

    /** Emits pawn moves for every target square; the origin is `to - delta`. */
    private fun addPawnMoves(
        buf: IntArray,
        start: Int,
        targets: Long,
        delta: Int,
        capture: Boolean,
        kind: Int,
        pinned: Long,
        lineBase: Int,
    ): Int {
        var n = start
        var bb = targets
        while (bb != 0L) {
            val to = java.lang.Long.numberOfTrailingZeros(bb)
            bb = bb and (bb - 1)
            val from = to - delta
            if (pinned and (1L shl from) != 0L && Attacks.LINE[lineBase + from] and (1L shl to) == 0L) continue
            val captured = if (capture) squares[to] and 7 else -1
            if (to >= 56 || to < 8) {
                n = push(buf, n, Moves.encode(from, to, PAWN, captured, QUEEN, Moves.KIND_NORMAL))
                n = push(buf, n, Moves.encode(from, to, PAWN, captured, ROOK, Moves.KIND_NORMAL))
                n = push(buf, n, Moves.encode(from, to, PAWN, captured, BISHOP, Moves.KIND_NORMAL))
                n = push(buf, n, Moves.encode(from, to, PAWN, captured, KNIGHT, Moves.KIND_NORMAL))
            } else {
                n = push(buf, n, Moves.encode(from, to, PAWN, captured, 0, kind))
            }
        }
        return n
    }

    private fun addPieceMoves(buf: IntArray, start: Int, from: Int, type: Int, targets: Long): Int {
        var n = start
        var bb = targets
        while (bb != 0L) {
            val to = java.lang.Long.numberOfTrailingZeros(bb)
            bb = bb and (bb - 1)
            val code = squares[to]
            n = push(buf, n, Moves.encode(from, to, type, if (code < 0) -1 else code and 7, 0, Moves.KIND_NORMAL))
        }
        return n
    }

    private fun isEnPassantLegal(from: Int, ep: Int, ksq: Int, us: Int, occ: Long, checkers: Long): Boolean {
        if (ksq < 0) return true
        val capturedSq = if (us == WHITE) ep - 8 else ep + 8
        // A checking knight, or a checking pawn other than the one being captured, stays a check
        // (as does an adjacent enemy king, which only an implausible placement can produce).
        val leapers = byType[PAWN] or byType[KNIGHT] or byType[KING]
        if (checkers and (1L shl capturedSq).inv() and leapers != 0L) return false
        val occAfter = (occ xor (1L shl from) xor (1L shl capturedSq)) or (1L shl ep)
        val enemy = bySide[us xor 1]
        if (Attacks.rook(ksq, occAfter) and (byType[ROOK] or byType[QUEEN]) and enemy != 0L) return false
        return Attacks.bishop(ksq, occAfter) and (byType[BISHOP] or byType[QUEEN]) and enemy == 0L
    }

    // ------------------------------------------------------------------ make / unmake

    /** Plays [m], which must come from [generateMoves]/[generateCaptures] for the current position. */
    fun make(m: Int) {
        pushHistory(m)
        val us = side
        val them = us xor 1
        val from = Moves.from(m)
        val to = Moves.to(m)
        val piece = Moves.piece(m)
        val captured = Moves.captured(m)
        val kind = Moves.kind(m)
        var k = key xor enPassantKey()

        epSquare = -1
        halfmoveClock++

        if (captured >= 0) {
            val capturedSq = if (kind == Moves.KIND_EN_PASSANT) (if (us == WHITE) to - 8 else to + 8) else to
            removePiece(them, captured, capturedSq)
            k = k xor Zobrist.PIECE[(pieceCode(them, captured) shl 6) or capturedSq]
            halfmoveClock = 0
        }

        val code = pieceCode(us, piece)
        movePiece(us, piece, from, to)
        k = k xor Zobrist.PIECE[(code shl 6) or from] xor Zobrist.PIECE[(code shl 6) or to]

        if (piece == PAWN) {
            halfmoveClock = 0
            val promotion = Moves.promotion(m)
            if (promotion != 0) {
                removePiece(us, PAWN, to)
                addPiece(us, promotion, to)
                k = k xor Zobrist.PIECE[(code shl 6) or to] xor Zobrist.PIECE[(pieceCode(us, promotion) shl 6) or to]
            } else if (kind == Moves.KIND_DOUBLE_PUSH) {
                epSquare = (from + to) shr 1
            }
        } else if (kind == Moves.KIND_CASTLE) {
            val rookFrom = if (to > from) to + 1 else to - 2
            val rookTo = if (to > from) to - 1 else to + 1
            val rook = pieceCode(us, ROOK)
            movePiece(us, ROOK, rookFrom, rookTo)
            k = k xor Zobrist.PIECE[(rook shl 6) or rookFrom] xor Zobrist.PIECE[(rook shl 6) or rookTo]
        }

        // One table handles "king moved", "rook moved" and "rook captured on its home square".
        val rights = castling and CASTLE_KEEP[from] and CASTLE_KEEP[to]
        if (rights != castling) {
            k = k xor Zobrist.CASTLING[castling] xor Zobrist.CASTLING[rights]
            castling = rights
        }

        if (us == BLACK) fullmoveNumber++
        side = them
        key = k xor Zobrist.SIDE
        key = key xor enPassantKey()
    }

    /** Takes back the most recent [make] or [makeNull]. */
    fun unmake() {
        check(ply > 0) { "no move to unmake" }
        ply--
        val m = undoMove[ply]
        val state = undoState[ply]
        castling = state and 15
        epSquare = (state ushr 4) - 1
        halfmoveClock = undoHalfmove[ply]
        key = undoKey[ply]
        side = side xor 1
        val us = side
        if (us == BLACK) fullmoveNumber--
        if (m == Moves.NONE) return

        val from = Moves.from(m)
        val to = Moves.to(m)
        val piece = Moves.piece(m)
        val captured = Moves.captured(m)
        val kind = Moves.kind(m)

        val promotion = Moves.promotion(m)
        if (promotion != 0) {
            removePiece(us, promotion, to)
            addPiece(us, PAWN, from)
        } else {
            movePiece(us, piece, to, from)
        }
        if (kind == Moves.KIND_CASTLE) {
            val rookFrom = if (to > from) to + 1 else to - 2
            val rookTo = if (to > from) to - 1 else to + 1
            movePiece(us, ROOK, rookTo, rookFrom)
        }
        if (captured >= 0) {
            val capturedSq = if (kind == Moves.KIND_EN_PASSANT) (if (us == WHITE) to - 8 else to + 8) else to
            addPiece(us xor 1, captured, capturedSq)
        }
    }

    /**
     * Passes the turn (null move, for search pruning); undo with [unmake]. The caller must not do
     * this while in check. It resets [halfmoveClock] so that [isRepetition] never looks across it.
     */
    fun makeNull() {
        pushHistory(Moves.NONE)
        key = key xor enPassantKey() xor Zobrist.SIDE
        epSquare = -1
        halfmoveClock = 0
        if (side == BLACK) fullmoveNumber++
        side = side xor 1
    }

    private fun pushHistory(m: Int) {
        if (ply == undoMove.size) {
            undoMove = undoMove.copyOf(ply * 2)
            undoState = undoState.copyOf(ply * 2)
            undoHalfmove = undoHalfmove.copyOf(ply * 2)
            undoKey = undoKey.copyOf(ply * 2)
        }
        undoMove[ply] = m
        undoState[ply] = castling or ((epSquare + 1) shl 4)
        undoHalfmove[ply] = halfmoveClock
        undoKey[ply] = key
        ply++
    }

    private fun addPiece(side: Int, type: Int, sq: Int) {
        val bit = 1L shl sq
        byType[type] = byType[type] or bit
        bySide[side] = bySide[side] or bit
        squares[sq] = pieceCode(side, type)
    }

    private fun removePiece(side: Int, type: Int, sq: Int) {
        val keep = (1L shl sq).inv()
        byType[type] = byType[type] and keep
        bySide[side] = bySide[side] and keep
        squares[sq] = NO_PIECE
    }

    /** [to] must be empty. */
    private fun movePiece(side: Int, type: Int, from: Int, to: Int) {
        val bits = (1L shl from) or (1L shl to)
        byType[type] = byType[type] xor bits
        bySide[side] = bySide[side] xor bits
        squares[from] = NO_PIECE
        squares[to] = pieceCode(side, type)
    }

    // ------------------------------------------------------------------ hashing

    /**
     * The en-passant part of the key. The file is only hashed when a pawn of the side to move
     * could actually take, so that positions differing only by an unusable en-passant square
     * (every double push sets one) still count as repetitions.
     */
    private fun enPassantKey(): Long {
        val ep = epSquare
        if (ep < 0) return 0L
        val capturers = Attacks.PAWN[(side xor 1) * 64 + ep] and byType[PAWN] and bySide[side]
        return if (capturers != 0L) Zobrist.EN_PASSANT[ep and 7] else 0L
    }

    /** Recomputes the Zobrist key from scratch; always equal to [key] (checked by the tests). */
    fun computeKey(): Long {
        var k = 0L
        for (sq in 0 until 64) {
            val code = squares[sq]
            if (code >= 0) k = k xor Zobrist.PIECE[(code shl 6) or sq]
        }
        if (side == BLACK) k = k xor Zobrist.SIDE
        return k xor Zobrist.CASTLING[castling] xor enPassantKey()
    }

    companion object {
        const val WHITE = 0
        const val BLACK = 1

        const val PAWN = 0
        const val KNIGHT = 1
        const val BISHOP = 2
        const val ROOK = 3
        const val QUEEN = 4
        const val KING = 5

        const val NO_PIECE = -1

        /** Castling right bits: white/black, king/queen side. */
        const val WK = 1
        const val WQ = 2
        const val BK = 4
        const val BQ = 8

        /** Buffer size that holds the legal moves of any position reachable in a real game. */
        const val MAX_MOVES = 256

        /** Bitboard of the light squares (b1, d1 … g8). */
        const val LIGHT_SQUARES = 0x55AA55AA55AA55AAL

        private const val INITIAL_HISTORY = 32

        private const val A1 = 0
        private const val C1 = 2
        private const val D1 = 3
        private const val E1 = 4
        private const val F1 = 5
        private const val G1 = 6
        private const val H1 = 7
        private const val A8 = 56
        private const val C8 = 58
        private const val D8 = 59
        private const val E8 = 60
        private const val F8 = 61
        private const val G8 = 62
        private const val H8 = 63

        private const val W_KING = KING
        private const val W_ROOK = ROOK
        private const val B_KING = (BLACK shl 3) or KING
        private const val B_ROOK = (BLACK shl 3) or ROOK

        // Squares that must be empty for each castling move.
        private const val WK_EMPTY = 0x60L
        private const val WQ_EMPTY = 0x0EL
        private const val BK_EMPTY = WK_EMPTY shl 56
        private const val BQ_EMPTY = WQ_EMPTY shl 56

        /** Rights that survive a move touching the square (as origin or destination). */
        private val CASTLE_KEEP = IntArray(64) { 15 }.also {
            it[A1] = 15 and WQ.inv()
            it[E1] = 15 and (WK or WQ).inv()
            it[H1] = 15 and WK.inv()
            it[A8] = 15 and BQ.inv()
            it[E8] = 15 and (BK or BQ).inv()
            it[H8] = 15 and BK.inv()
        }

        fun pieceCode(side: Int, type: Int): Int = (side shl 3) or type
        fun typeOf(code: Int): Int = code and 7
        fun sideOf(code: Int): Int = code ushr 3
    }
}

/** Zobrist random numbers (fixed seed, so keys are stable across runs and devices). */
internal object Zobrist {
    /** `[(pieceCode shl 6) or square]`. */
    @JvmField val PIECE = LongArray(14 * 64)

    /** `[castling rights bit set]`. */
    @JvmField val CASTLING = LongArray(16)

    /** `[file of the en-passant square]`. */
    @JvmField val EN_PASSANT = LongArray(8)

    @JvmField val SIDE: Long

    init {
        var state = 0x4B52535F43484553L
        fun next(): Long {
            // SplitMix64
            state += -0x61c8864680b583ebL
            var z = state
            z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L
            z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L
            return z xor (z ushr 31)
        }
        for (i in PIECE.indices) PIECE[i] = next()
        for (i in CASTLING.indices) CASTLING[i] = next()
        for (i in EN_PASSANT.indices) EN_PASSANT[i] = next()
        SIDE = next()
    }
}
