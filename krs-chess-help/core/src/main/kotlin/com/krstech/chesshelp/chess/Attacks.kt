package com.krstech.chesshelp.chess

/**
 * Precomputed attack sets as bitboards (bit `n` = square index `n`, a1 = bit 0 … h8 = bit 63).
 *
 * Leaper tables are indexed by square; sliding attacks use fixed-shift magic bitboards whose
 * multipliers were found offline and are verified exhaustively against [slowRook]/[slowBishop] in
 * the unit tests. Everything here is immutable after class initialisation, so it is safe to share
 * between threads.
 */
internal object Attacks {
    const val FILE_A = 0x0101010101010101L
    const val FILE_H = FILE_A shl 7
    const val RANK_1 = 0xFFL
    const val RANK_3 = RANK_1 shl 16
    const val RANK_6 = RANK_1 shl 40
    const val RANK_8 = RANK_1 shl 56

    /** Squares a knight on `sq` attacks. */
    @JvmField val KNIGHT = LongArray(64)

    /** Squares a king on `sq` attacks. */
    @JvmField val KING = LongArray(64)

    /** `[side * 64 + sq]`: squares attacked by a pawn of `side` (0 = white, 1 = black) standing on `sq`. */
    @JvmField val PAWN = LongArray(128)

    /** `[a * 64 + b]`: squares strictly between `a` and `b` when they share a rank, file or diagonal, else 0. */
    @JvmField val BETWEEN = LongArray(64 * 64)

    /** `[a * 64 + b]`: the whole line (edge to edge, including `a` and `b`) through two aligned squares, else 0. */
    @JvmField val LINE = LongArray(64 * 64)

    private val ROOK_DIRS = intArrayOf(1, 0, -1, 0, 0, 1, 0, -1)
    private val BISHOP_DIRS = intArrayOf(1, 1, 1, -1, -1, 1, -1, -1)

    private val ROOK_MAGIC = longArrayOf(
        0x2080108000204000L, 0x024002401000E000L, 0x2080100008200082L, 0x1880080006801000L,
        0x24800800801C0007L, 0x0200048110080200L, 0x1080288002004100L, 0x40801020C1000480L,
        0x0001800080400021L, 0x0000402010004001L, 0x0800808010002000L, 0x0800800800801001L,
        0x0288800800040080L, 0x0001000400090002L, 0x20A30003000A0084L, 0x0D01800EE0800100L,
        0x0000820020420100L, 0x7310044000402000L, 0x0400888020021000L, 0x0088008008801004L,
        0x0000808008000400L, 0x0010080104102040L, 0x0000040082085021L, 0x4902420000810044L,
        0x0000209080004009L, 0x2004220200410480L, 0x4040100080802000L, 0x0488080080100481L,
        0x0201000500100800L, 0x3006000200100804L, 0x0444620400080190L, 0x0226008600004D24L,
        0x6C80804010800020L, 0x1940201000400040L, 0x4000200584801000L, 0x0008008008801000L,
        0x0200040080800800L, 0x0032000401010008L, 0x00A2015004000288L, 0x0000088842002401L,
        0x1240400080E2800DL, 0x1860004000810100L, 0x0820010060430030L, 0x0400080010008080L,
        0x0181000800250050L, 0x0004001008020200L, 0x15082110CA040018L, 0x0000090143820024L,
        0x0000800042B10300L, 0x4001002890420200L, 0x4840100080200080L, 0x0030080080100080L,
        0x0004800402080080L, 0x4001000400080300L, 0x0080420188104400L, 0x408B010080440200L,
        0x2A03020018204282L, 0x0000182041020082L, 0x0800220008421082L, 0x0049200805100101L,
        0x4B01000210040801L, 0x0002008801500402L, 0x10801808D0010204L, 0x40110008220582C1L,
    )

    private val BISHOP_MAGIC = longArrayOf(
        0x0160020420408200L, 0x0221240400404001L, 0x2989220602088800L, 0x0402209201000A08L,
        0x0004042202100014L, 0x1040821040200080L, 0x0860440220104000L, 0x008A004C0E08020CL,
        0x0000400881040084L, 0x000814480A024201L, 0x3002880204182089L, 0x4000840702008800L,
        0x08900510C0800036L, 0x080002012460020AL, 0x00A0008090082008L, 0x2004010088010801L,
        0x1010000405080801L, 0x00048C0801240410L, 0x0102010104040280L, 0x0088008A20821080L,
        0x202A0084020A0000L, 0x0041008A0080C408L, 0x0014000088845000L, 0x04020001004202A2L,
        0x1008082420204100L, 0x1010022010020248L, 0x080C020401180100L, 0x0024040020410200L,
        0x020200201A008040L, 0x1408020008208402L, 0x0988008000640400L, 0x40008200688218A0L,
        0x2001042100102022L, 0x0002222001900100L, 0x1004060100080044L, 0x000A020084680080L,
        0x1404010010040040L, 0x4110044882011001L, 0x00180A5088004800L, 0x40B8408881010041L,
        0x0004020840C04428L, 0x0004161104001000L, 0x680010149000C801L, 0x0080204010450201L,
        0x0080B810140020C1L, 0x0004048802000941L, 0x1C20012C49080080L, 0x0010020084220100L,
        0x06110410340501C0L, 0x4001510410028001L, 0x0004002422080214L, 0x0804807442020010L,
        0x0080020410440003L, 0x4000442024011001L, 0x0410052104040920L, 0x2002020222020800L,
        0x0208202210100800L, 0x000013804410B400L, 0x20B4040104411060L, 0x20004040420A0A02L,
        0x2000208010620A00L, 0x2020108431020A01L, 0x000440038E040108L, 0x010244180E044204L,
    )

    private val ROOK_MASK = LongArray(64)
    private val ROOK_SHIFT = IntArray(64)
    private val ROOK_OFFSET = IntArray(64)
    private val ROOK_TABLE: LongArray

    private val BISHOP_MASK = LongArray(64)
    private val BISHOP_SHIFT = IntArray(64)
    private val BISHOP_OFFSET = IntArray(64)
    private val BISHOP_TABLE: LongArray

    init {
        val knightSteps = intArrayOf(1, 2, 2, 1, 2, -1, 1, -2, -1, -2, -2, -1, -2, 1, -1, 2)
        val kingSteps = intArrayOf(1, 0, 1, 1, 0, 1, -1, 1, -1, 0, -1, -1, 0, -1, 1, -1)
        for (sq in 0 until 64) {
            KNIGHT[sq] = leaper(sq, knightSteps)
            KING[sq] = leaper(sq, kingSteps)
            PAWN[sq] = leaper(sq, intArrayOf(-1, 1, 1, 1))
            PAWN[64 + sq] = leaper(sq, intArrayOf(-1, -1, 1, -1))
        }

        ROOK_TABLE = buildMagicTable(ROOK_DIRS, ROOK_MAGIC, ROOK_MASK, ROOK_SHIFT, ROOK_OFFSET)
        BISHOP_TABLE = buildMagicTable(BISHOP_DIRS, BISHOP_MAGIC, BISHOP_MASK, BISHOP_SHIFT, BISHOP_OFFSET)

        for (a in 0 until 64) {
            val rookRays = slide(a, 0L, ROOK_DIRS)
            val bishopRays = slide(a, 0L, BISHOP_DIRS)
            for (b in 0 until 64) {
                if (a == b) continue
                val bit = 1L shl b
                val dirs = when {
                    rookRays and bit != 0L -> ROOK_DIRS
                    bishopRays and bit != 0L -> BISHOP_DIRS
                    else -> continue
                }
                val aBit = 1L shl a
                BETWEEN[a * 64 + b] = slide(a, bit, dirs) and slide(b, aBit, dirs)
                LINE[a * 64 + b] = (slide(a, 0L, dirs) and slide(b, 0L, dirs)) or aBit or bit
            }
        }
    }

    /** Rook attacks from `sq` given the occupancy `occ` (blockers are included in the result). */
    fun rook(sq: Int, occ: Long): Long =
        ROOK_TABLE[ROOK_OFFSET[sq] + (((occ and ROOK_MASK[sq]) * ROOK_MAGIC[sq]) ushr ROOK_SHIFT[sq]).toInt()]

    /** Bishop attacks from `sq` given the occupancy `occ` (blockers are included in the result). */
    fun bishop(sq: Int, occ: Long): Long =
        BISHOP_TABLE[BISHOP_OFFSET[sq] + (((occ and BISHOP_MASK[sq]) * BISHOP_MAGIC[sq]) ushr BISHOP_SHIFT[sq]).toInt()]

    fun queen(sq: Int, occ: Long): Long = rook(sq, occ) or bishop(sq, occ)

    /** Reference implementation of [rook] by ray walking; used to build and to test the tables. */
    fun slowRook(sq: Int, occ: Long): Long = slide(sq, occ, ROOK_DIRS)

    /** Reference implementation of [bishop] by ray walking; used to build and to test the tables. */
    fun slowBishop(sq: Int, occ: Long): Long = slide(sq, occ, BISHOP_DIRS)

    private fun leaper(sq: Int, steps: IntArray): Long {
        var result = 0L
        val file = sq and 7
        val rank = sq shr 3
        var i = 0
        while (i < steps.size) {
            val f = file + steps[i]
            val r = rank + steps[i + 1]
            if (f in 0..7 && r in 0..7) result = result or (1L shl (r * 8 + f))
            i += 2
        }
        return result
    }

    private fun slide(sq: Int, occ: Long, dirs: IntArray): Long {
        var result = 0L
        var i = 0
        while (i < dirs.size) {
            var f = (sq and 7) + dirs[i]
            var r = (sq shr 3) + dirs[i + 1]
            while (f in 0..7 && r in 0..7) {
                val bit = 1L shl (r * 8 + f)
                result = result or bit
                if (occ and bit != 0L) break
                f += dirs[i]
                r += dirs[i + 1]
            }
            i += 2
        }
        return result
    }

    /** Relevant-occupancy mask: the rays from `sq` without the last square of each ray. */
    private fun relevantMask(sq: Int, dirs: IntArray): Long {
        var result = 0L
        var i = 0
        while (i < dirs.size) {
            var f = (sq and 7) + dirs[i]
            var r = (sq shr 3) + dirs[i + 1]
            while (f + dirs[i] in 0..7 && r + dirs[i + 1] in 0..7) {
                result = result or (1L shl (r * 8 + f))
                f += dirs[i]
                r += dirs[i + 1]
            }
            i += 2
        }
        return result
    }

    private fun buildMagicTable(
        dirs: IntArray,
        magics: LongArray,
        masks: LongArray,
        shifts: IntArray,
        offsets: IntArray,
    ): LongArray {
        var total = 0
        for (sq in 0 until 64) {
            masks[sq] = relevantMask(sq, dirs)
            val bits = java.lang.Long.bitCount(masks[sq])
            shifts[sq] = 64 - bits
            offsets[sq] = total
            total += 1 shl bits
        }
        val table = LongArray(total)
        for (sq in 0 until 64) {
            val mask = masks[sq]
            var subset = 0L
            do {
                val index = offsets[sq] + ((subset * magics[sq]) ushr shifts[sq]).toInt()
                table[index] = slide(sq, subset, dirs)
                subset = (subset - mask) and mask
            } while (subset != 0L)
        }
        return table
    }
}
