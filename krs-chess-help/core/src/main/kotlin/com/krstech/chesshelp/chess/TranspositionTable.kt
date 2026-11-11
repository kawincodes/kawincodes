package com.krstech.chesshelp.chess

/**
 * Fixed-size hash table of search results, keyed by the full 64-bit Zobrist key.
 *
 * One primitive `LongArray` of `2^bucketBits` buckets, each holding two entries of two longs
 * (key, packed data): the first entry is kept while it is the deeper one, the second is always
 * replaced. The default of 2^18 buckets is 8 MB.
 *
 * Packed data: bits 0-22 move, 23-38 score + 32768, 39-46 depth, 47-48 bound. A stored entry is
 * never 0 because its bound is never 0.
 */
internal class TranspositionTable(bucketBits: Int = DEFAULT_BUCKET_BITS) {
    private val mask = (1 shl bucketBits) - 1
    private val table = LongArray((1 shl bucketBits) * 4)

    val sizeBytes: Long get() = table.size * 8L

    fun clear() {
        table.fill(0L)
    }

    /** The packed entry stored for [key], or 0 when there is none. */
    fun probe(key: Long): Long {
        val i = (key.toInt() and mask) shl 2
        if (table[i] == key) return table[i + 1]
        if (table[i + 2] == key) return table[i + 3]
        return 0L
    }

    /** [score] must fit 16 bits, [depth] 8 bits; [move] may be [Moves.NONE]. */
    fun store(key: Long, move: Int, score: Int, depth: Int, bound: Int) {
        val i = (key.toInt() and mask) shl 2
        val same = if (table[i] == key) i else if (table[i + 2] == key) i + 2 else -1
        if (same >= 0) {
            val old = table[same + 1]
            // A much shallower result must not wipe out a deep one for the same position.
            if (bound != EXACT && depth + 2 < depth(old)) return
            table[same + 1] = pack(if (move == Moves.NONE) move(old) else move, score, depth, bound)
            return
        }
        if (depth >= depth(table[i + 1])) {
            table[i + 2] = table[i]
            table[i + 3] = table[i + 1]
            table[i] = key
            table[i + 1] = pack(move, score, depth, bound)
        } else {
            table[i + 2] = key
            table[i + 3] = pack(move, score, depth, bound)
        }
    }

    companion object {
        const val DEFAULT_BUCKET_BITS = 18

        /** The stored score is the exact value. */
        const val EXACT = 1

        /** The true value is at least the stored score (the node failed high). */
        const val LOWER = 2

        /** The true value is at most the stored score (the node failed low). */
        const val UPPER = 3

        fun pack(move: Int, score: Int, depth: Int, bound: Int): Long =
            move.toLong() or ((score + 32768).toLong() shl 23) or (depth.toLong() shl 39) or (bound.toLong() shl 47)

        fun move(entry: Long): Int = (entry and 0x7FFFFFL).toInt()
        fun score(entry: Long): Int = ((entry ushr 23) and 0xFFFFL).toInt() - 32768
        fun depth(entry: Long): Int = ((entry ushr 39) and 0xFFL).toInt()
        fun bound(entry: Long): Int = ((entry ushr 47) and 3L).toInt()
    }
}
