package com.krstech.chesshelp.chess

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Static evaluation in centipawns: tapered material and piece-square tables (the public-domain
 * PeSTO values) plus a few cheap terms — mobility, passed / isolated / doubled pawns, the bishop
 * pair, rooks on open files, a pawn shield, and two endgame corrections (material that cannot
 * win is scaled down; a bare king is driven to the edge).
 *
 * Every term is computed for White minus Black from colour-mirrored tables, so swapping the
 * colours and flipping the board negates [evaluateWhite] exactly (checked by the tests).
 */
internal object Evaluation {
    /** Scores never leave `-LIMIT..LIMIT`, so they can not be mistaken for mate scores. */
    const val LIMIT = 20_000

    private const val MAX_PHASE = 24

    private val MG_VALUE = intArrayOf(82, 337, 365, 477, 1025, 0)
    private val EG_VALUE = intArrayOf(94, 281, 297, 512, 936, 0)
    private val PHASE = intArrayOf(0, 1, 1, 2, 4, 0)

    // Piece-square tables as seen by White with a8 first, i.e. the way a diagram is printed.
    private val MG_PST = arrayOf(
        intArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0,
            98, 134, 61, 95, 68, 126, 34, -11,
            -6, 7, 26, 31, 65, 56, 25, -20,
            -14, 13, 6, 21, 23, 12, 17, -23,
            -27, -2, -5, 12, 17, 6, 10, -25,
            -26, -4, -4, -10, 3, 3, 33, -12,
            -35, -1, -20, -23, -15, 24, 38, -22,
            0, 0, 0, 0, 0, 0, 0, 0,
        ),
        intArrayOf(
            -167, -89, -34, -49, 61, -97, -15, -107,
            -73, -41, 72, 36, 23, 62, 7, -17,
            -47, 60, 37, 65, 84, 129, 73, 44,
            -9, 17, 19, 53, 37, 69, 18, 22,
            -13, 4, 16, 13, 28, 19, 21, -8,
            -23, -9, 12, 10, 19, 17, 25, -16,
            -29, -53, -12, -3, -1, 18, -14, -19,
            -105, -21, -58, -33, -17, -28, -19, -23,
        ),
        intArrayOf(
            -29, 4, -82, -37, -25, -42, 7, -8,
            -26, 16, -18, -13, 30, 59, 18, -47,
            -16, 37, 43, 40, 35, 50, 37, -2,
            -4, 5, 19, 50, 37, 37, 7, -2,
            -6, 13, 13, 26, 34, 12, 10, 4,
            0, 15, 15, 15, 14, 27, 18, 10,
            4, 15, 16, 0, 7, 21, 33, 1,
            -33, -3, -14, -21, -13, -12, -39, -21,
        ),
        intArrayOf(
            32, 42, 32, 51, 63, 9, 31, 43,
            27, 32, 58, 62, 80, 67, 26, 44,
            -5, 19, 26, 36, 17, 45, 61, 16,
            -24, -11, 7, 26, 24, 35, -8, -20,
            -36, -26, -12, -1, 9, -7, 6, -23,
            -45, -25, -16, -17, 3, 0, -5, -33,
            -44, -16, -20, -9, -1, 11, -6, -71,
            -19, -13, 1, 17, 16, 7, -37, -26,
        ),
        intArrayOf(
            -28, 0, 29, 12, 59, 44, 43, 45,
            -24, -39, -5, 1, -16, 57, 28, 54,
            -13, -17, 7, 8, 29, 56, 47, 57,
            -27, -27, -16, -16, -1, 17, -2, 1,
            -9, -26, -9, -10, -2, -4, 3, -3,
            -14, 2, -11, -2, -5, 2, 14, 5,
            -35, -8, 11, 2, 8, 15, -3, 1,
            -1, -18, -9, 10, -15, -25, -31, -50,
        ),
        intArrayOf(
            -65, 23, 16, -15, -56, -34, 2, 13,
            29, -1, -20, -7, -8, -4, -38, -29,
            -9, 24, 2, -16, -20, 6, 22, -22,
            -17, -20, -12, -27, -30, -25, -14, -36,
            -49, -1, -27, -39, -46, -44, -33, -51,
            -14, -14, -22, -46, -44, -30, -15, -27,
            1, 7, -8, -64, -43, -16, 9, 8,
            -15, 36, 12, -54, 8, -28, 24, 14,
        ),
    )

    private val EG_PST = arrayOf(
        intArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0,
            178, 173, 158, 134, 147, 132, 165, 187,
            94, 100, 85, 67, 56, 53, 82, 84,
            32, 24, 13, 5, -2, 4, 17, 17,
            13, 9, -3, -7, -7, -8, 3, -1,
            4, 7, -6, 1, 0, -5, -1, -8,
            13, 8, 8, 10, 13, 0, 2, -7,
            0, 0, 0, 0, 0, 0, 0, 0,
        ),
        intArrayOf(
            -58, -38, -13, -28, -31, -27, -63, -99,
            -25, -8, -25, -2, -9, -25, -24, -52,
            -24, -20, 10, 9, -1, -9, -19, -41,
            -17, 3, 22, 22, 22, 11, 8, -18,
            -18, -6, 16, 25, 16, 17, 4, -18,
            -23, -3, -1, 15, 10, -3, -20, -22,
            -42, -20, -10, -5, -2, -20, -23, -44,
            -29, -51, -23, -15, -22, -18, -50, -64,
        ),
        intArrayOf(
            -14, -21, -11, -8, -7, -9, -17, -24,
            -8, -4, 7, -12, -3, -13, -4, -14,
            2, -8, 0, -1, -2, 6, 0, 4,
            -3, 9, 12, 9, 14, 10, 3, 2,
            -6, 3, 13, 19, 7, 10, -3, -9,
            -12, -3, 8, 10, 13, 3, -7, -15,
            -14, -18, -7, -1, 4, -9, -15, -27,
            -23, -9, -23, -5, -9, -16, -5, -17,
        ),
        intArrayOf(
            13, 10, 18, 15, 12, 12, 8, 5,
            11, 13, 13, 11, -3, 3, 8, 3,
            7, 7, 7, 5, 4, -3, -5, -3,
            4, 3, 13, 1, 2, 1, -1, 2,
            3, 5, 8, 4, -5, -6, -8, -11,
            -4, 0, -5, -1, -7, -12, -8, -16,
            -6, -6, 0, 2, -9, -9, -11, -3,
            -9, 2, 3, -1, -5, -13, 4, -20,
        ),
        intArrayOf(
            -9, 22, 22, 27, 27, 19, 10, 20,
            -17, 20, 32, 41, 58, 25, 30, 0,
            -20, 6, 9, 49, 47, 35, 19, 9,
            3, 22, 24, 45, 57, 40, 57, 36,
            -18, 28, 19, 47, 31, 34, 39, 23,
            -16, -27, 15, 6, 9, 17, 10, 5,
            -22, -23, -30, -16, -16, -23, -36, -32,
            -33, -28, -22, -43, -5, -32, -20, -41,
        ),
        intArrayOf(
            -74, -35, -18, -18, -11, 15, 4, -17,
            -12, 17, 14, 17, 17, 38, 23, 11,
            10, 17, 23, 15, 20, 45, 44, 13,
            -8, 22, 24, 27, 26, 33, 26, 3,
            -18, -4, 21, 24, 27, 23, 9, -11,
            -19, -3, 11, 21, 23, 16, 7, -9,
            -27, -11, 4, 13, 14, 4, -5, -17,
            -53, -34, -21, -11, -28, -14, -24, -43,
        ),
    )

    /** `[(pieceCode shl 6) or square]`: material plus square value, negated for Black. */
    private val MG = IntArray(14 * 64)
    private val EG = IntArray(14 * 64)

    private val FILE_MASK = LongArray(8) { Attacks.FILE_A shl it }
    private val ADJACENT_FILES = LongArray(8) { file ->
        (if (file > 0) FILE_MASK[file - 1] else 0L) or (if (file < 7) FILE_MASK[file + 1] else 0L)
    }

    // All indexed `[side * 64 + square]`, "ahead" meaning towards the enemy back rank.
    /** Squares ahead of the square on its own file. */
    private val FRONT = LongArray(128)

    /** Squares ahead on the own and the adjacent files: no enemy pawn there = passed pawn. */
    private val PASSED = LongArray(128)

    /** The up to six squares one and two ranks ahead of a king, on its own and the adjacent files. */
    private val SHIELD = LongArray(128)

    // Indexed by the pawn's rank counted from its own side (1 = start rank, 6 = one step from promoting).
    private val PASSED_MG = intArrayOf(0, 5, 5, 10, 20, 35, 55, 0)
    private val PASSED_EG = intArrayOf(0, 10, 15, 25, 45, 75, 110, 0)

    private const val ISOLATED_MG = 10
    private const val ISOLATED_EG = 12
    private const val DOUBLED_MG = 8
    private const val DOUBLED_EG = 15
    private const val BISHOP_PAIR_MG = 25
    private const val BISHOP_PAIR_EG = 40
    private const val OPEN_FILE_MG = 20
    private const val OPEN_FILE_EG = 10
    private const val HALF_OPEN_FILE_MG = 10
    private const val HALF_OPEN_FILE_EG = 5
    private const val SHIELD_MG = 6

    init {
        for (type in Board.PAWN..Board.KING) {
            for (sq in 0 until 64) {
                val white = (Board.pieceCode(Board.WHITE, type) shl 6) or sq
                val black = (Board.pieceCode(Board.BLACK, type) shl 6) or sq
                MG[white] = MG_VALUE[type] + MG_PST[type][sq xor 56]
                EG[white] = EG_VALUE[type] + EG_PST[type][sq xor 56]
                MG[black] = -(MG_VALUE[type] + MG_PST[type][sq])
                EG[black] = -(EG_VALUE[type] + EG_PST[type][sq])
            }
        }
        for (sq in 0 until 64) {
            val file = sq and 7
            val rank = sq shr 3
            for (f in max(0, file - 1)..min(7, file + 1)) {
                for (r in 0..7) {
                    if (r == rank) continue
                    val bit = 1L shl (r * 8 + f)
                    val index = if (r > rank) sq else 64 + sq
                    PASSED[index] = PASSED[index] or bit
                    if (f == file) FRONT[index] = FRONT[index] or bit
                    if (abs(r - rank) <= 2) SHIELD[index] = SHIELD[index] or bit
                }
            }
        }
    }

    /** Evaluation from the point of view of the side to move. */
    fun evaluate(board: Board): Int {
        val white = evaluateWhite(board)
        return if (board.side == Board.WHITE) white else -white
    }

    /** Evaluation from White's point of view (independent of the side to move). */
    fun evaluateWhite(board: Board): Int {
        val whitePieces = board.occupancy(Board.WHITE)
        val blackPieces = board.occupancy(Board.BLACK)
        val occupied = whitePieces or blackPieces
        val pawns = board.pieces(Board.PAWN)
        val whitePawns = pawns and whitePieces
        val blackPawns = pawns and blackPieces
        val whitePawnAttacks =
            ((whitePawns and Attacks.FILE_A.inv()) shl 7) or ((whitePawns and Attacks.FILE_H.inv()) shl 9)
        val blackPawnAttacks =
            ((blackPawns and Attacks.FILE_A.inv()) ushr 9) or ((blackPawns and Attacks.FILE_H.inv()) ushr 7)
        // Mobility counts squares that are neither blocked by an own piece nor guarded by an enemy pawn.
        val whiteArea = (whitePieces or blackPawnAttacks).inv()
        val blackArea = (blackPieces or whitePawnAttacks).inv()

        var mg = 0
        var eg = 0
        var phase = 0
        var bb = occupied
        while (bb != 0L) {
            val sq = java.lang.Long.numberOfTrailingZeros(bb)
            bb = bb and (bb - 1)
            val code = board.pieceAt(sq)
            val index = (code shl 6) or sq
            mg += MG[index]
            eg += EG[index]
            val type = code and 7
            phase += PHASE[type]

            val white = code < 8
            val sign = if (white) 1 else -1
            val side = if (white) 0 else 64
            val ownPawns = if (white) whitePawns else blackPawns
            val area = if (white) whiteArea else blackArea
            val file = sq and 7
            val relativeRank = if (white) sq shr 3 else 7 - (sq shr 3)
            when (type) {
                Board.PAWN -> {
                    val enemyPawns = if (white) blackPawns else whitePawns
                    if (PASSED[side + sq] and enemyPawns == 0L) {
                        mg += sign * PASSED_MG[relativeRank]
                        eg += sign * PASSED_EG[relativeRank]
                    }
                    if (ADJACENT_FILES[file] and ownPawns == 0L) {
                        mg -= sign * ISOLATED_MG
                        eg -= sign * ISOLATED_EG
                    }
                    if (FRONT[side + sq] and ownPawns != 0L) {
                        mg -= sign * DOUBLED_MG
                        eg -= sign * DOUBLED_EG
                    }
                }
                Board.KNIGHT -> {
                    val mobility = java.lang.Long.bitCount(Attacks.KNIGHT[sq] and area) - 4
                    mg += sign * 4 * mobility
                    eg += sign * 4 * mobility
                }
                Board.BISHOP -> {
                    val mobility = java.lang.Long.bitCount(Attacks.bishop(sq, occupied) and area) - 6
                    mg += sign * 4 * mobility
                    eg += sign * 5 * mobility
                }
                Board.ROOK -> {
                    val mobility = java.lang.Long.bitCount(Attacks.rook(sq, occupied) and area) - 7
                    mg += sign * 2 * mobility
                    eg += sign * 4 * mobility
                    if (FILE_MASK[file] and pawns == 0L) {
                        mg += sign * OPEN_FILE_MG
                        eg += sign * OPEN_FILE_EG
                    } else if (FILE_MASK[file] and ownPawns == 0L) {
                        mg += sign * HALF_OPEN_FILE_MG
                        eg += sign * HALF_OPEN_FILE_EG
                    }
                }
                Board.QUEEN -> {
                    val mobility = java.lang.Long.bitCount(Attacks.queen(sq, occupied) and area) - 14
                    mg += sign * mobility
                    eg += sign * 2 * mobility
                }
                else -> {
                    if (relativeRank <= 1) {
                        mg += sign * SHIELD_MG * java.lang.Long.bitCount(SHIELD[side + sq] and ownPawns)
                    }
                }
            }
        }

        val bishops = board.pieces(Board.BISHOP)
        if (hasBishopPair(bishops and whitePieces)) {
            mg += BISHOP_PAIR_MG
            eg += BISHOP_PAIR_EG
        }
        if (hasBishopPair(bishops and blackPieces)) {
            mg -= BISHOP_PAIR_MG
            eg -= BISHOP_PAIR_EG
        }

        val p = min(phase, MAX_PHASE)
        val tapered = (mg * p + eg * (MAX_PHASE - p)) / MAX_PHASE
        val score = endgameCorrection(board, tapered, whitePawns, blackPawns)
        return max(-LIMIT, min(LIMIT, score))
    }

    private fun hasBishopPair(bishops: Long): Boolean =
        bishops and Board.LIGHT_SQUARES != 0L && bishops and Board.LIGHT_SQUARES.inv() != 0L

    /**
     * Applied to the side that is ahead: without pawns, an advantage of no more than a minor
     * piece (or knights only) can normally not be converted, so the score is scaled towards a
     * draw; against a bare king, mating material gets a bonus for cornering that king.
     */
    private fun endgameCorrection(board: Board, score: Int, whitePawns: Long, blackPawns: Long): Int {
        if (score == 0) return 0
        val strong = if (score > 0) Board.WHITE else Board.BLACK
        val weak = strong xor 1
        val strongHasPawns = (if (strong == Board.WHITE) whitePawns else blackPawns) != 0L
        val weakPieces = board.occupancy(weak)
        val weakKing = board.pieces(weak, Board.KING)
        val weakIsBare = weakKing != 0L && weakPieces == weakKing && weakKing and (weakKing - 1) == 0L
        if (strongHasPawns && !weakIsBare) return score

        val knights = java.lang.Long.bitCount(board.pieces(strong, Board.KNIGHT))
        val bishops = java.lang.Long.bitCount(board.pieces(strong, Board.BISHOP))
        val rooks = java.lang.Long.bitCount(board.pieces(strong, Board.ROOK))
        val queens = java.lang.Long.bitCount(board.pieces(strong, Board.QUEEN))
        var magnitude = abs(score)

        if (!strongHasPawns) {
            if (bishops + rooks + queens == 0 && knights <= 2) {
                magnitude /= 16
            } else {
                val strongMaterial = 3 * (knights + bishops) + 5 * rooks + 9 * queens
                val weakMaterial = 3 * java.lang.Long.bitCount(board.pieces(weak, Board.KNIGHT)) +
                    3 * java.lang.Long.bitCount(board.pieces(weak, Board.BISHOP)) +
                    5 * java.lang.Long.bitCount(board.pieces(weak, Board.ROOK)) +
                    9 * java.lang.Long.bitCount(board.pieces(weak, Board.QUEEN))
                if (strongMaterial - weakMaterial <= 3) magnitude /= 8
            }
        }

        val canMate = rooks + queens > 0 || bishops >= 2 || (bishops >= 1 && knights >= 1)
        if (weakIsBare && canMate) {
            val weakSq = board.kingSquare(weak)
            val strongSq = board.kingSquare(strong)
            if (strongSq >= 0) {
                val file = weakSq and 7
                val rank = weakSq shr 3
                val fromCentre = max(3 - file, file - 4) + max(3 - rank, rank - 4)
                val kingDistance = abs(file - (strongSq and 7)) + abs(rank - (strongSq shr 3))
                magnitude += 10 * fromCentre + 4 * (14 - kingDistance)
            }
        }
        return if (score > 0) magnitude else -magnitude
    }
}
