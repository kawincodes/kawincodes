package com.krstech.chesshelp.vision

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Finds the 8x8 board in a screenshot.
 *
 * 1. Proposal: on a downscaled luma image a signed checkerboard-corner response is summed with alternating
 *    signs over every 7x7 lattice `(x0, y0, cell)`; the best lattices become candidates.
 * 2. Refinement: at full resolution the 7 interior grid lines per axis are located to sub-pixel precision
 *    and a regular grid is fitted through them.
 * 3. Verification: the background tone of every lattice cell around the candidate is measured; the 8x8 window
 *    must be a clean two-tone checker whose pattern stops at its border.
 */
object BoardLocator {

    /** Finds the on-screen 8x8 chessboard (squares area only, excluding borders/coordinate margins), or null. */
    fun locate(frame: Frame, hint: BoardRect? = null): BoardRect? = locate(frame, hint, null)

    internal fun locate(frame: Frame, hint: BoardRect?, trace: ((String) -> Unit)?): BoardRect? {
        if (min(frame.width, frame.height) < MIN_FRAME_SIDE) return null
        if (hint != null) {
            val viaHint = checkHint(frame, hint, trace)
            if (viaHint != null) return viaHint
        }
        return search(frame, trace)
    }

    // ------------------------------------------------------------------------------------------------
    // Hint fast path
    // ------------------------------------------------------------------------------------------------

    private fun checkHint(frame: Frame, hint: BoardRect, trace: ((String) -> Unit)?): BoardRect? {
        if (hint.size < MIN_BOARD_PX || hint.left < -2 || hint.top < -2 ||
            hint.right > frame.width + 2 || hint.bottom > frame.height + 2
        ) return null
        val cell = hint.size / 8f
        val lattice = refine(frame, hint.left.toFloat(), hint.top.toFloat(), cell, margin = 3, cellSlack = 0.4f, trace = trace)
            ?: return null
        return verify(frame, lattice, maxShift = 0, trace = trace)
    }

    // ------------------------------------------------------------------------------------------------
    // Full search
    // ------------------------------------------------------------------------------------------------

    private class Proposal(val x0: Int, val y0: Int, val cell: Float, val score: Int)

    private fun search(frame: Frame, trace: ((String) -> Unit)?): BoardRect? {
        val shortSide = min(frame.width, frame.height)
        val factor = max(1, (shortSide + COARSE_SHORT_SIDE / 2) / COARSE_SHORT_SIDE)
        val proposals = propose(frame, factor)
        var best: BoardRect? = null
        var bestRank = 0f
        for (p in proposals) {
            trace?.invoke("proposal x0=${p.x0 * factor} y0=${p.y0 * factor} cell=${p.cell * factor} score=${p.score / (49f * FIXED_ONE)}")
            val lattice = refine(
                frame, (p.x0 * factor).toFloat(), (p.y0 * factor).toFloat(), p.cell * factor,
                margin = 2 * factor + 4, cellSlack = 0.3f * factor + 0.5f, trace = trace,
            ) ?: continue
            val rect = verify(frame, lattice, maxShift = MAX_SHIFT, trace = trace) ?: continue
            // Several boards on screen (thumbnails next to the main board): the biggest convincing one wins.
            val rank = rect.confidence * rect.size
            if (best == null || rank > bestRank) {
                best = rect
                bestRank = rank
            }
        }
        return best
    }

    /** Coarse lattice search; returns up to [MAX_PROPOSALS] distinct candidates, best first (coarse pixel units). */
    private fun propose(frame: Frame, factor: Int): List<Proposal> {
        val w = frame.width / factor
        val h = frame.height / factor
        val shortSide = min(w, h)
        if (shortSide < 48) return emptyList()
        val luma = coarseLuma(frame, factor, w, h)
        val stride = w + 1
        val integral = IntArray(stride * (h + 1))
        for (y in 0 until h) {
            var rowSum = 0
            val o = (y + 1) * stride
            for (x in 0 until w) {
                rowSum += luma[y * w + x]
                integral[o + x + 1] = integral[o - stride + x + 1] + rowSum
            }
        }

        val cellMin = max(6f, MIN_BOARD_FRACTION * shortSide / 8f)
        val cellMax = shortSide / 8f + 0.25f
        val found = ArrayList<Proposal>()
        val response = IntArray(stride * (h + 1))
        val rowSums = IntArray(stride * (h + 1))
        val offsets = IntArray(8)

        // The quadrant size grows with the cell so that the corner peak stays wide enough for the scale step.
        var cell = cellMin
        var quadrant = 0
        while (cell <= cellMax) {
            val q = if (cell < 16f) 2 else if (cell < 24f) 3 else 4
            if (q != quadrant) {
                quadrant = q
                cornerResponse(integral, w, h, q, response)
            }
            for (i in 0..7) offsets[i] = (i * cell).roundToInt()
            val span = (8 * cell).roundToInt()
            val xMin = max(-1, q - offsets[1])
            val xMax = min(w - span + 1, w - q - offsets[7])
            val yMin = max(-1, q - offsets[1])
            val yMax = min(h - span + 1, h - q - offsets[7])
            if (xMax >= xMin && yMax >= yMin) {
                scanScale(response, rowSums, stride, h, q, offsets, xMin, xMax, yMin, yMax, cell, found)
            }
            cell += q / 8f
        }

        found.sortByDescending { it.score }
        val kept = ArrayList<Proposal>()
        val minScore = (MIN_PROPOSAL_SCORE * 49 * FIXED_ONE).toInt()
        for (p in found) {
            if (p.score < minScore || kept.size >= MAX_PROPOSALS) break
            if (kept.none { overlap(it, p) > 0.45f }) kept.add(p)
        }
        return kept
    }

    private fun overlap(a: Proposal, b: Proposal): Float {
        val sa = a.cell * 8
        val sb = b.cell * 8
        val ix = min(a.x0 + sa, b.x0 + sb) - max(a.x0, b.x0)
        val iy = min(a.y0 + sa, b.y0 + sb) - max(a.y0, b.y0)
        if (ix <= 0 || iy <= 0) return 0f
        val inter = ix * iy
        return inter / (sa * sa + sb * sb - inter)
    }

    private fun coarseLuma(frame: Frame, factor: Int, w: Int, h: Int): IntArray {
        val out = IntArray(w * h)
        val px = frame.pixels
        val area = factor * factor
        val sums = IntArray(w)
        for (cy in 0 until h) {
            java.util.Arrays.fill(sums, 0)
            for (dy in 0 until factor) {
                var idx = (cy * factor + dy) * frame.width
                for (cx in 0 until w) {
                    var s = 0
                    for (dx in 0 until factor) {
                        val p = px[idx++]
                        s += ((p ushr 16) and 0xFF) * 77 + ((p ushr 8) and 0xFF) * 150 + (p and 0xFF) * 29
                    }
                    sums[cx] += s shr 8
                }
            }
            val o = cy * w
            for (cx in 0 until w) out[o + cx] = sums[cx] / area
        }
        return out
    }

    /**
     * `(TL + BR) - (TR + BL)` of the four `q x q` boxes around every pixel corner, squashed to
     * `r / (|r| + k)` so that the lattice sum counts consistent corners instead of rewarding raw contrast.
     */
    private fun cornerResponse(integral: IntArray, w: Int, h: Int, q: Int, out: IntArray) {
        java.util.Arrays.fill(out, 0)
        val stride = w + 1
        val k = CORNER_SOFT_K * q * q
        for (y in q..h - q) {
            val top = (y - q) * stride
            val mid = y * stride
            val bot = (y + q) * stride
            for (x in q..w - q) {
                val a = integral[top + x - q]
                val b = integral[top + x]
                val c = integral[top + x + q]
                val d = integral[mid + x - q]
                val e = integral[mid + x]
                val f = integral[mid + x + q]
                val g = integral[bot + x - q]
                val hh = integral[bot + x]
                val i = integral[bot + x + q]
                val tl = e - b - d + a
                val tr = f - c - e + b
                val bl = hh - e - g + d
                val br = i - f - hh + e
                val r = tl + br - tr - bl
                out[mid + x] = r * FIXED_ONE / (abs(r) + k)
            }
        }
    }

    private fun scanScale(
        response: IntArray, rowSums: IntArray, stride: Int, h: Int, q: Int, offsets: IntArray,
        xMin: Int, xMax: Int, yMin: Int, yMax: Int, cell: Float, found: MutableList<Proposal>,
    ) {
        val o1 = offsets[1]; val o2 = offsets[2]; val o3 = offsets[3]; val o4 = offsets[4]
        val o5 = offsets[5]; val o6 = offsets[6]; val o7 = offsets[7]
        // rowSums[y][x0] = alternating sum of the 7 interior corners of lattice row y.
        for (y in max(q, yMin + o1)..min(h - q, yMax + o7)) {
            val base = y * stride
            for (x0 in xMin..xMax) {
                val b = base + x0
                rowSums[base + x0 - xMin] =
                    -response[b + o1] + response[b + o2] - response[b + o3] + response[b + o4] -
                        response[b + o5] + response[b + o6] - response[b + o7]
            }
        }
        var best1 = 0; var best1x = 0; var best1y = 0
        var best2 = 0; var best2x = 0; var best2y = 0
        val far = (2 * cell).toInt()
        for (y0 in yMin..yMax) {
            val b1 = (y0 + o1) * stride - xMin
            val b2 = (y0 + o2) * stride - xMin
            val b3 = (y0 + o3) * stride - xMin
            val b4 = (y0 + o4) * stride - xMin
            val b5 = (y0 + o5) * stride - xMin
            val b6 = (y0 + o6) * stride - xMin
            val b7 = (y0 + o7) * stride - xMin
            for (x0 in xMin..xMax) {
                val v = abs(
                    -rowSums[b1 + x0] + rowSums[b2 + x0] - rowSums[b3 + x0] + rowSums[b4 + x0] -
                        rowSums[b5 + x0] + rowSums[b6 + x0] - rowSums[b7 + x0]
                )
                if (v > best2) {
                    if (v > best1) {
                        if (abs(x0 - best1x) > far || abs(y0 - best1y) > far) {
                            best2 = best1; best2x = best1x; best2y = best1y
                        }
                        best1 = v; best1x = x0; best1y = y0
                    } else if (abs(x0 - best1x) > far || abs(y0 - best1y) > far) {
                        best2 = v; best2x = x0; best2y = y0
                    }
                }
            }
        }
        if (best1 > 0) found.add(Proposal(best1x, best1y, cell, best1))
        if (best2 > 0) found.add(Proposal(best2x, best2y, cell, best2))
    }

    // ------------------------------------------------------------------------------------------------
    // Full-resolution grid refinement
    // ------------------------------------------------------------------------------------------------

    /** A regular grid in frame pixels: vertical lines at `x0 + i*sx`, horizontal lines at `y0 + j*sy`. */
    internal class Lattice(val x0: Float, val y0: Float, val sx: Float, val sy: Float)

    private class AxisFit(val origin: Float, val cell: Float, val sign: Int, val strength: Float)

    private fun refine(
        frame: Frame, x0: Float, y0: Float, cell: Float, margin: Int, cellSlack: Float,
        trace: ((String) -> Unit)?,
    ): Lattice? {
        // Pass 1: rows/columns through the middle of the cells tolerate the still inaccurate other axis.
        val fx = fitAxis(frame, true, x0, cell, y0, cell, margin, cellSlack, wide = true) ?: run {
            trace?.invoke("  refine: no x grid"); return null
        }
        val fy = fitAxis(frame, false, y0, cell, fx.origin, fx.cell, margin, cellSlack, wide = true) ?: run {
            trace?.invoke("  refine: no y grid"); return null
        }
        if (fx.sign != fy.sign) {
            trace?.invoke("  refine: polarity mismatch"); return null
        }
        // Pass 2: rows/columns hugging the grid lines stay clear of the pieces.
        val px = fitAxis(frame, true, fx.origin, fx.cell, fy.origin, fy.cell, 3, 0.3f, wide = false) ?: run {
            trace?.invoke("  refine: no precise x grid"); return null
        }
        val py = fitAxis(frame, false, fy.origin, fy.cell, px.origin, px.cell, 3, 0.3f, wide = false) ?: run {
            trace?.invoke("  refine: no precise y grid"); return null
        }
        if (px.sign != py.sign) return null
        val meanCell = (px.cell + py.cell) / 2
        if (abs(px.cell - py.cell) > max(0.6f, 0.015f * meanCell)) {
            trace?.invoke("  refine: not square ${px.cell} vs ${py.cell}"); return null
        }
        if (meanCell * 8 < MIN_BOARD_PX) return null
        trace?.invoke("  refined x0=${px.origin} y0=${py.origin} sx=${px.cell} sy=${py.cell} strength=${px.strength}/${py.strength}")
        return Lattice(px.origin, py.origin, px.cell, py.cell)
    }

    /**
     * Fits the 7 interior grid lines of one axis. For the x axis (`horizontal = true`) luma is summed along
     * sample rows with the sign of their board row, which turns the checker into a square wave whose
     * alternating edges are the grid lines; pieces average out because they do not alternate.
     */
    private fun fitAxis(
        frame: Frame, horizontal: Boolean, origin: Float, cell: Float, otherOrigin: Float, otherCell: Float,
        margin: Int, cellSlack: Float, wide: Boolean,
    ): AxisFit? {
        val length = if (horizontal) frame.width else frame.height
        val otherLength = if (horizontal) frame.height else frame.width
        val lo = max(0, floor(origin - margin).toInt() - 4)
        val hi = min(length, ceil(origin + 8 * cell + margin).toInt() + 4)
        if (hi - lo < 16) return null

        // Sample lines across the other axis.
        val positions = IntArray(64)
        val signs = IntArray(64)
        var n = 0
        if (wide) {
            for (band in 0..7) for (k in 0..6) {
                val p = (otherOrigin + (band + 0.26f + 0.08f * k) * otherCell).roundToInt()
                if (p in 0 until otherLength) { positions[n] = p; signs[n] = if (band and 1 == 0) 1 else -1; n++ }
            }
        } else {
            for (band in 0..7) for (k in 0..5) {
                val t = NEAR_LINE_OFFSETS[k]
                val off = if (t < 0.5f) max(3f, t * otherCell) else otherCell - max(3f, (1 - t) * otherCell)
                val p = (otherOrigin + band * otherCell + off).roundToInt()
                if (p in 0 until otherLength) { positions[n] = p; signs[n] = if (band and 1 == 0) 1 else -1; n++ }
            }
        }
        if (n < 16) return null

        val profile = IntArray(hi - lo)
        val px = frame.pixels
        val fw = frame.width
        if (horizontal) {
            for (k in 0 until n) {
                val base = positions[k] * fw
                if (signs[k] > 0) for (x in lo until hi) profile[x - lo] += lumaOf(px[base + x])
                else for (x in lo until hi) profile[x - lo] -= lumaOf(px[base + x])
            }
        } else {
            for (k in 0 until n) {
                val col = positions[k]
                if (signs[k] > 0) for (y in lo until hi) profile[y - lo] += lumaOf(px[y * fw + col])
                else for (y in lo until hi) profile[y - lo] -= lumaOf(px[y * fw + col])
            }
        }

        return if (wide) fitWide(profile, lo, origin, cell, margin, cellSlack, n)
        else fitPrecise(profile, lo, origin, cell, n)
    }

    private fun fitWide(profile: IntArray, lo: Int, origin: Float, cell: Float, margin: Int, cellSlack: Float, rows: Int): AxisFit? {
        val len = profile.size
        // Smoothed edge strength at pixel boundary e (between e-1 and e), index relative to lo.
        val edge = IntArray(len + 1)
        for (e in 2..len - 2) edge[e] = profile[e] + profile[e + 1] - profile[e - 1] - profile[e - 2]
        var best = 0
        var bestOrigin = origin
        var bestCell = cell
        val steps = ceil(cellSlack / 0.125f).toInt()
        for (ci in -steps..steps) {
            val c = cell + ci * 0.125f
            var oi = -2 * margin
            while (oi <= 2 * margin) {
                val o = origin + oi * 0.5f - lo
                var f = 0
                for (i in 1..7) {
                    val e = (o + i * c).roundToInt()
                    if (e in 2..len - 2) f += if (i and 1 == 0) edge[e] else -edge[e]
                }
                if (abs(f) > abs(best)) { best = f; bestOrigin = origin + oi * 0.5f; bestCell = c }
                oi++
            }
        }
        // Mean luma step per line: 7 lines, `rows` sample lines, the kernel sums 2 pixels per side.
        val strength = abs(best) / (7f * rows * 2f)
        if (strength < MIN_LINE_CONTRAST) return null
        return AxisFit(bestOrigin, bestCell, if (best > 0) 1 else -1, strength)
    }

    private fun fitPrecise(profile: IntArray, lo: Int, origin: Float, cell: Float, rows: Int): AxisFit? {
        val len = profile.size
        val edge = IntArray(len + 1)
        for (e in 1 until len) edge[e] = profile[e] - profile[e - 1]
        // Polarity: sign that makes the strongest edges near the predicted lines positive.
        var pol = 0L
        val peak = IntArray(8)
        for (i in 1..7) {
            val e0 = (origin + i * cell).roundToInt() - lo
            var bestE = -1
            var bestV = 0
            for (e in e0 - 4..e0 + 4) {
                if (e < 1 || e >= len) continue
                if (abs(edge[e]) > abs(bestV)) { bestV = edge[e]; bestE = e }
            }
            peak[i] = bestE
            pol += if (i and 1 == 0) bestV else -bestV
        }
        val sign = if (pol >= 0) 1 else -1

        val pos = FloatArray(8)
        val weight = FloatArray(8)
        for (i in 1..7) {
            val lineSign = if (i and 1 == 0) sign else -sign
            val e0 = (origin + i * cell).roundToInt() - lo
            var bestE = -1
            var bestV = 0
            for (e in e0 - 4..e0 + 4) {
                if (e < 1 || e >= len) continue
                val v = edge[e] * lineSign
                if (v > bestV) { bestV = v; bestE = e }
            }
            if (bestE < 0) continue
            var sw = 0f
            var swe = 0f
            val floorV = bestV * 0.12f
            for (e in bestE - 2..bestE + 2) {
                if (e < 1 || e >= len) continue
                val v = edge[e] * lineSign - floorV
                if (v > 0) { sw += v; swe += v * e }
            }
            if (sw <= 0f) continue
            pos[i] = swe / sw + lo
            weight[i] = bestV.toFloat()
        }
        val sorted = weight.copyOfRange(1, 8).also { it.sort() }
        val median = sorted[3]
        if (median <= 0f) return null
        val use = BooleanArray(8)
        var count = 0
        for (i in 1..7) if (weight[i] >= 0.3f * median) { use[i] = true; count++ }
        if (count < 5) return null

        var fit = lineFit(pos, use) ?: return null
        var dropped = false
        for (i in 1..7) if (use[i] && abs(pos[i] - (fit[0] + i * fit[1])) > 1.25f) { use[i] = false; dropped = true; count-- }
        if (dropped) {
            if (count < 5) return null
            fit = lineFit(pos, use) ?: return null
        }
        var strength = 0f
        for (i in 1..7) if (use[i]) strength += weight[i]
        strength /= count * rows
        if (strength < MIN_LINE_CONTRAST) return null
        return AxisFit(fit[0], fit[1], sign, strength)
    }

    /** Least squares `pos[i] = a + b*i` over the used lines; returns `[a, b]`. */
    private fun lineFit(pos: FloatArray, use: BooleanArray): FloatArray? {
        var n = 0
        var si = 0.0; var sp = 0.0; var sii = 0.0; var sip = 0.0
        for (i in 1..7) if (use[i]) {
            n++; si += i; sp += pos[i]; sii += i * i; sip += i * pos[i]
        }
        val den = n * sii - si * si
        if (n < 2 || den == 0.0) return null
        val b = (n * sip - si * sp) / den
        val a = (sp - b * si) / n
        return floatArrayOf(a.toFloat(), b.toFloat())
    }

    // ------------------------------------------------------------------------------------------------
    // Verification and extent
    // ------------------------------------------------------------------------------------------------

    private fun verify(frame: Frame, lat: Lattice, maxShift: Int, trace: ((String) -> Unit)?): BoardRect? {
        val cell = (lat.sx + lat.sy) / 2
        // Background tone of every lattice cell in the neighbourhood (-1 = outside the frame).
        val tones = IntArray(EXT * EXT)
        for (j in 0 until EXT) for (i in 0 until EXT) {
            tones[j * EXT + i] = cellTone(frame, lat, i - OFF, j - OFF)
        }

        var bestScore = Int.MIN_VALUE
        var bestDi = 0; var bestDj = 0
        var bestPairs = 0; var bestCorners = 0; var bestRing = 0; var bestContrast = 0
        val diffs = IntArray(PAIRS)
        val sorted = IntArray(PAIRS)
        for (dj in -maxShift..maxShift) for (di in -maxShift..maxShift) {
            val base = (dj + OFF) * EXT + di + OFF
            var valid = true
            for (b in 0..7) {
                for (a in 0..7) if (tones[base + b * EXT + a] < 0) { valid = false; break }
                if (!valid) break
            }
            if (!valid) continue
            // Tone step across every pair of adjacent cells, signed so that "even cell is lighter" is positive.
            var n = 0
            var sum = 0
            for (b in 0..7) for (a in 0..7) {
                val t = tones[base + b * EXT + a]
                val sign = if ((a + b) and 1 == 0) 1 else -1
                if (a < 7) { val d = sign * (t - tones[base + b * EXT + a + 1]); diffs[n++] = d; sum += d }
                if (b < 7) { val d = sign * (t - tones[base + (b + 1) * EXT + a]); diffs[n++] = d; sum += d }
            }
            val polarity = if (sum >= 0) 1 else -1
            for (k in 0 until n) sorted[k] = diffs[k] * polarity
            sorted.sort()
            val contrast = sorted[n / 2]
            if (contrast < MIN_TONE_CONTRAST) continue
            val need = (contrast + 1) / 2
            var pairs = 0
            for (k in 0 until n) if (diffs[k] * polarity >= need) pairs++

            // Cells just outside each side that would continue the checker: the step to their inner
            // neighbour fits AND they look like the same-coloured cell two steps further in.
            val similar = max(4, (contrast * 2) / 5)
            var ring = 0
            var worstSide = 0
            for (side in 0..3) {
                var c = 0
                for (k in 0..7) {
                    val a = when (side) { 0 -> -1; 1 -> 8; else -> k }
                    val b = when (side) { 2 -> -1; 3 -> 8; else -> k }
                    val ia = when (side) { 0 -> 0; 1 -> 7; else -> k }
                    val ib = when (side) { 2 -> 0; 3 -> 7; else -> k }
                    val ja = when (side) { 0 -> 1; 1 -> 6; else -> k }
                    val jb = when (side) { 2 -> 1; 3 -> 6; else -> k }
                    val t = tones[base + b * EXT + a]
                    if (t < 0) continue
                    val sign = if ((a + b) and 1 == 0) polarity else -polarity
                    val step = sign * (t - tones[base + ib * EXT + ia])
                    if (step >= need && abs(t - tones[base + jb * EXT + ja]) <= similar) c++
                }
                ring += max(0, c - 4)
                worstSide = max(worstSide, c)
            }
            val corners = fullCorners(frame, lat, di, dj, contrast * polarity)
            val score = pairs + corners - 6 * ring
            if (score > bestScore) {
                bestScore = score; bestDi = di; bestDj = dj
                bestPairs = pairs; bestCorners = corners; bestRing = worstSide; bestContrast = contrast
            }
        }
        trace?.invoke("  verify shift=($bestDi,$bestDj) pairs=$bestPairs corners=$bestCorners ringWorst=$bestRing contrast=$bestContrast")
        if (bestScore == Int.MIN_VALUE) return null
        if (bestPairs < MIN_PAIR_FITS || bestCorners < MIN_FULL_CORNERS || bestRing > MAX_RING_FITS) return null

        var size = (4 * (lat.sx + lat.sy)).roundToInt()
        var left = (lat.x0 + bestDi * lat.sx).roundToInt()
        var top = (lat.y0 + bestDj * lat.sy).roundToInt()
        // Sub-pixel fits of a board flush with the screen edge may round one pixel out.
        if (left < 0) left = 0
        if (top < 0) top = 0
        size = min(size, min(frame.width - left, frame.height - top))
        if (size < MIN_BOARD_PX || cell < 4f) return null
        val confidence = 0.6f * bestPairs / PAIRS + 0.4f * bestCorners / 49f
        return BoardRect(left, top, size, confidence)
    }

    /**
     * Background luma of lattice cell (i, j): the densest cluster of small block means taken along four
     * segments just inside the cell edges (their middle 60 %, clear of corner labels and of most piece
     * artwork). Block means rather than single pixels so that textured or hatched squares read as their tone.
     */
    private fun cellTone(frame: Frame, lat: Lattice, i: Int, j: Int): Int {
        val l = lat.x0 + i * lat.sx
        val t = lat.y0 + j * lat.sy
        val r = l + lat.sx
        val b = t + lat.sy
        if (l < -1.5f || t < -1.5f || r > frame.width + 1.5f || b > frame.height + 1.5f) return -1
        val cell = min(lat.sx, lat.sy)
        val inset = max(2f, 0.09f * cell)
        val half = max(1f, 0.04f * cell)
        val samples = IntArray(4 * TONE_SAMPLES)
        var n = 0
        for (k in 0 until TONE_SAMPLES) {
            val f = 0.2f + 0.6f * k / (TONE_SAMPLES - 1)
            val xa = l + f * lat.sx
            val ya = t + f * lat.sy
            samples[n++] = blockLuma(frame, xa, t + inset, half)
            samples[n++] = blockLuma(frame, xa, b - inset, half)
            samples[n++] = blockLuma(frame, l + inset, ya, half)
            samples[n++] = blockLuma(frame, r - inset, ya, half)
        }
        samples.sort()
        var bestStart = 0
        var bestCount = 0
        var end = 0
        for (start in 0 until n) {
            while (end < n && samples[end] - samples[start] <= TONE_WINDOW) end++
            if (end - start > bestCount) { bestCount = end - start; bestStart = start }
        }
        return samples[bestStart + bestCount / 2]
    }

    /** Mean luma of a 3x3 sample grid spanning `[x - half, x + half] x [y - half, y + half]`. */
    private fun blockLuma(frame: Frame, x: Float, y: Float, half: Float): Int {
        var sum = 0
        val w = frame.width
        val h = frame.height
        for (v in -1..1) {
            val yy = (y + v * half).toInt().coerceIn(0, h - 1)
            for (u in -1..1) {
                val xx = (x + u * half).toInt().coerceIn(0, w - 1)
                sum += lumaOf(frame.pixels[yy * w + xx])
            }
        }
        return sum / 9
    }

    /** Number of the window's 49 interior corners that show a full checker crossing of the right sign. */
    private fun fullCorners(frame: Frame, lat: Lattice, di: Int, dj: Int, signedContrast: Int): Int {
        val cell = min(lat.sx, lat.sy)
        val box = (0.10f * cell).coerceIn(2f, 14f)
        val gap = max(1f, 0.03f * cell)
        val need = 0.7f * 2f * abs(signedContrast)
        var count = 0
        for (b in 1..7) for (a in 1..7) {
            val cx = lat.x0 + (di + a) * lat.sx
            val cy = lat.y0 + (dj + b) * lat.sy
            val tl = boxLuma(frame, cx - gap - box, cy - gap - box, box)
            val tr = boxLuma(frame, cx + gap, cy - gap - box, box)
            val bl = boxLuma(frame, cx - gap - box, cy + gap, box)
            val br = boxLuma(frame, cx + gap, cy + gap, box)
            var r = (tl + br) - (tr + bl)
            if ((a + b) and 1 != 0) r = -r
            if (signedContrast < 0) r = -r
            if (r >= need) count++
        }
        return count
    }

    /** Mean luma of a 3x3 sample grid inside the square box at (x, y). */
    private fun boxLuma(frame: Frame, x: Float, y: Float, box: Float): Float {
        var sum = 0
        for (v in 0..2) {
            val yy = (y + box * (0.17f + 0.33f * v)).toInt().coerceIn(0, frame.height - 1)
            for (u in 0..2) {
                val xx = (x + box * (0.17f + 0.33f * u)).toInt().coerceIn(0, frame.width - 1)
                sum += lumaOf(frame.pixels[yy * frame.width + xx])
            }
        }
        return sum / 9f
    }

    private const val MIN_FRAME_SIDE = 96
    private const val MIN_BOARD_PX = 64
    private const val COARSE_SHORT_SIDE = 270
    private const val MIN_BOARD_FRACTION = 0.30f
    private const val FIXED_ONE = 1024
    private const val CORNER_SOFT_K = 30
    private const val MIN_PROPOSAL_SCORE = 0.16f
    private const val MAX_PROPOSALS = 6
    private const val MIN_LINE_CONTRAST = 3.5f
    private const val MIN_TONE_CONTRAST = 9
    private const val TONE_SAMPLES = 10
    private const val TONE_WINDOW = 14
    private const val MAX_SHIFT = 2
    private const val OFF = MAX_SHIFT + 1          // window shift plus the ring of cells around it
    private const val EXT = 8 + 2 * OFF
    private const val PAIRS = 112                  // adjacent cell pairs inside an 8x8 window
    private const val MIN_PAIR_FITS = 92
    private const val MIN_FULL_CORNERS = 34
    private const val MAX_RING_FITS = 6
    private val NEAR_LINE_OFFSETS = floatArrayOf(0.07f, 0.12f, 0.17f, 0.83f, 0.88f, 0.93f)
}
