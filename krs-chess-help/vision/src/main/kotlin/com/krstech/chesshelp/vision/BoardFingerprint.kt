package com.krstech.chesshelp.vision

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Cheap change detector for the board area: a [GRID] x [GRID] grid of mean luma values (4 x 4 per square).
 *
 * [distance] looks at each square on its own and takes the square that changed most (the mean of its
 * [TOP] most changed sub-cells), so a single piece appearing or disappearing lands far above
 * [CHANGE_THRESHOLD] no matter how much of the board stayed the same - even a pale piece on a pale square,
 * which only alters a few sub-cells. Pixels outside the board rectangle have no influence at all.
 */
object BoardFingerprint {

    /** Sub-cells per board side. */
    internal const val GRID = 32

    /** Distance above this = "the board changed". */
    const val CHANGE_THRESHOLD: Float = 0.02f

    /** Coarse luminance grid over the board region ([GRID] * [GRID] values of 0..255, row-major). */
    fun of(frame: Frame, rect: BoardRect): IntArray {
        val out = IntArray(GRID * GRID)
        if (rect.size < GRID) return out
        val px = frame.pixels
        val fw = frame.width
        val fh = frame.height
        val sub = rect.size.toFloat() / GRID
        // At most ~6 sample points per axis in each sub-cell keeps this far below a millisecond.
        val stride = max(1, (sub / 6f).toInt())
        for (gy in 0 until GRID) {
            val y0 = rect.top + (gy * sub).toInt()
            val y1 = max(y0 + 1, rect.top + ((gy + 1) * sub).toInt())
            for (gx in 0 until GRID) {
                val x0 = rect.left + (gx * sub).toInt()
                val x1 = max(x0 + 1, rect.left + ((gx + 1) * sub).toInt())
                var sum = 0
                var count = 0
                var y = y0
                while (y < y1) {
                    val yy = min(fh - 1, max(0, y))
                    var x = x0
                    while (x < x1) {
                        val xx = min(fw - 1, max(0, x))
                        sum += lumaOf(px[yy * fw + xx])
                        count++
                        x += stride
                    }
                    y += stride
                }
                out[gy * GRID + gx] = sum / count
            }
        }
        return out
    }

    /** 0..1; 0 for identical fingerprints, 1 for fingerprints that cannot be compared. */
    fun distance(a: IntArray, b: IntArray): Float {
        if (a.size != GRID * GRID || b.size != GRID * GRID) return 1f
        val per = GRID / 8
        val top = IntArray(TOP)
        var worst = 0
        for (row in 0..7) for (col in 0..7) {
            java.util.Arrays.fill(top, 0)
            for (dy in 0 until per) {
                val base = (row * per + dy) * GRID + col * per
                for (dx in 0 until per) {
                    // keep the TOP largest differences of this square, smallest first
                    var d = abs(a[base + dx] - b[base + dx])
                    for (k in TOP - 1 downTo 0) {
                        if (d > top[k]) { val t = top[k]; top[k] = d; d = t }
                    }
                }
            }
            var sum = 0
            for (v in top) sum += v
            if (sum > worst) worst = sum
        }
        return min(1f, worst / (TOP * 255f))
    }

    private const val TOP = 4
}
