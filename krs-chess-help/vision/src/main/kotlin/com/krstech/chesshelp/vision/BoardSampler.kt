package com.krstech.chesshelp.vision

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class CellState { EMPTY, LIGHT_PIECE, DARK_PIECE }

/** Occupancy of the 64 squares in view order (`row * 8 + col`, row 0 = top of the board image). */
class BoardSnapshot(
    val rect: BoardRect,
    val cells: Array<CellState>,
    /** 0..1 certainty of each cell's state. */
    val confidence: FloatArray,
    /** False when the reading is implausible as a chess position (too many pieces, low certainty ...). */
    val reliable: Boolean,
) {
    init {
        require(cells.size == 64 && confidence.size == 64) { "a snapshot has 64 cells" }
    }

    /** 8 lines of 8 characters, top row first: '.' empty, 'L' light piece, 'D' dark piece. */
    fun toAscii(): String = buildString(72) {
        for (row in 0..7) {
            if (row > 0) append('\n')
            for (col in 0..7) {
                append(
                    when (cells[row * 8 + col]) {
                        CellState.EMPTY -> '.'
                        CellState.LIGHT_PIECE -> 'L'
                        CellState.DARK_PIECE -> 'D'
                    }
                )
            }
        }
    }
}

/**
 * Reads piece occupancy from pixels.
 *
 * Per square: estimate the (possibly highlighted) background colour from the band just inside the square's
 * edges; mark everything that differs from it; flood the background in from the edges, so that whatever it
 * cannot reach is the piece - outline plus enclosed fill, even when the fill has the square's own colour.
 * A square is occupied when that region contains a solid blob tall enough to be a piece (move dots, labels,
 * capture rings and texture are too small or too thin). The tone comes from what is solid inside the piece:
 * a light piece keeps its bright fill under erosion, a dark piece only its dark body.
 *
 * Known limit: an analysis arrow drawn across an empty square is a solid blob too and reads as a piece.
 */
object BoardSampler {

    /**
     * Occupancy of the 64 squares of the board at [rect] (normally the result of [BoardLocator.locate]).
     * Never throws for odd rectangles; a rectangle that is too small or not on a board yields
     * `reliable = false`.
     */
    fun sample(frame: Frame, rect: BoardRect): BoardSnapshot = classify(rect, analyze(frame, rect))

    /** Per-cell measurements. */
    internal class CellFeatures {
        /** Luma of the square's own (possibly highlighted) background colour. */
        var bgLuma = 0
        /**
         * Share of the cell left after eroding the piece region (everything the background cannot reach from
         * the cell border): solid blobs survive, thin lines, labels and texture speckle do not.
         */
        var coreArea = 0f
        /** Share of the cell covered by the biggest solid blob alone. */
        var coreMain = 0f
        /** Vertical extent of the solid blobs (erosion added back) as a fraction of the cell side. */
        var boxHeight = 0f
        /** Luma histogram ([TONE_BINS] bins) of the piece region shrunk by one pixel. */
        val toneHist = IntArray(TONE_BINS)
        var toneCount = 0
        /** 0..1 tone of the piece: near 1 for a light piece, near 0 for a dark one (see [classify]). */
        var lightness = 0f
        /** Colour distance that counted as "not background" in this cell; high on strongly textured squares. */
        var threshold = 0
    }

    /** Per-cell features plus the working-resolution luma and piece-interior mask of every cell. */
    internal class Analysis(val n: Int) {
        val features = Array(64) { CellFeatures() }
        val luma = ByteArray(64 * n * n)
        val interior = BooleanArray(64 * n * n)
    }

    internal fun analyze(frame: Frame, rect: BoardRect): Analysis {
        val cellPx = rect.size / 8
        if (cellPx < MIN_CELL_PX) return Analysis(0)
        val step = max(1, cellPx / WORK_CELL)
        val n = cellPx / step
        val out = Analysis(n)
        val work = Work(n)
        for (row in 0..7) for (col in 0..7) {
            val x0 = rect.cellLeft(col) + (rect.cellRight(col) - rect.cellLeft(col) - n * step) / 2
            val y0 = rect.cellTop(row) + (rect.cellBottom(row) - rect.cellTop(row) - n * step) / 2
            work.load(frame, x0, y0, step)
            work.measure(out, row * 8 + col)
        }
        return out
    }

    private class Work(val n: Int) {
        val r = IntArray(n * n)
        val g = IntArray(n * n)
        val b = IntArray(n * n)
        val fg = BooleanArray(n * n)
        val outside = BooleanArray(n * n)
        val core = BooleanArray(n * n)
        val scratch = BooleanArray(n * n)
        val queue = IntArray(n * n)
        val bandIndex = IntArray(4 * n * 8)
        val bandLuma = IntArray(4 * n * 8)
        val bandSide = IntArray(4 * n * 8)
        val order = IntArray(4 * n * 8)
        val hist = IntArray(257)
        val sideHist = IntArray(4 * 256)
        val blobArea = IntArray(64)
        val blobTop = IntArray(64)
        val blobBottom = IntArray(64)

        /** Fills the n x n working grid with `step x step` block means; coordinates are clamped to the frame. */
        fun load(frame: Frame, x0: Int, y0: Int, step: Int) {
            val px = frame.pixels
            val fw = frame.width
            val fh = frame.height
            val area = step * step
            for (wy in 0 until n) {
                for (wx in 0 until n) {
                    var sr = 0; var sg = 0; var sb = 0
                    for (dy in 0 until step) {
                        val yy = min(fh - 1, max(0, y0 + wy * step + dy))
                        val base = yy * fw
                        for (dx in 0 until step) {
                            val xx = min(fw - 1, max(0, x0 + wx * step + dx))
                            val p = px[base + xx]
                            sr += (p ushr 16) and 0xFF
                            sg += (p ushr 8) and 0xFF
                            sb += p and 0xFF
                        }
                    }
                    val i = wy * n + wx
                    r[i] = sr / area
                    g[i] = sg / area
                    b[i] = sb / area
                }
            }
        }

        private fun lumaAt(i: Int): Int = (r[i] * 77 + g[i] * 150 + b[i] * 29) shr 8

        fun measure(out: Analysis, cell: Int) {
            val f = out.features[cell]
            val m = max(1, (MARGIN * n).roundToInt())
            val lo = m
            val hi = n - m                       // inner region is [lo, hi) on both axes
            val inner = (hi - lo) * (hi - lo)

            // --- background colour: densest luma cluster of the band along the middle of each edge
            val bandWidth = max(2, (BAND * n).roundToInt())
            val a0 = (0.18f * n).roundToInt()
            val a1 = n - a0
            var count = 0
            for (k in 0 until bandWidth) {
                val near = lo + k
                val far = hi - 1 - k
                for (t in a0 until a1) {
                    for (side in 0..3) {
                        val i = when (side) {
                            0 -> near * n + t
                            1 -> far * n + t
                            2 -> t * n + near
                            else -> t * n + far
                        }
                        bandIndex[count] = i
                        bandLuma[count] = lumaAt(i)
                        bandSide[count] = side
                        count++
                    }
                }
            }
            java.util.Arrays.fill(hist, 0)
            for (k in 0 until count) hist[bandLuma[k] + 1]++
            for (v in 1..256) hist[v] += hist[v - 1]
            for (k in 0 until count) order[hist[bandLuma[k]]++] = k
            var bestStart = 0
            var bestLen = 0
            var end = 0
            for (start in 0 until count) {
                val base = bandLuma[order[start]]
                while (end < count && bandLuma[order[end]] - base <= BG_LUMA_WINDOW) end++
                if (end - start > bestLen) { bestLen = end - start; bestStart = start }
            }
            var sr = 0L; var sg = 0L; var sb = 0L
            for (k in bestStart until bestStart + bestLen) {
                val i = bandIndex[order[k]]
                sr += r[i]; sg += g[i]; sb += b[i]
            }
            var bgR = (sr / bestLen).toInt()
            var bgG = (sg / bestLen).toInt()
            var bgB = (sb / bestLen).toInt()
            // refine with the samples close to that colour on every channel
            sr = 0; sg = 0; sb = 0
            var kept = 0
            for (k in 0 until count) {
                val i = bandIndex[k]
                if (abs(r[i] - bgR) <= BG_REFINE && abs(g[i] - bgG) <= BG_REFINE && abs(b[i] - bgB) <= BG_REFINE) {
                    sr += r[i]; sg += g[i]; sb += b[i]; kept++
                }
            }
            if (kept > 0) {
                bgR = (sr / kept).toInt(); bgG = (sg / kept).toInt(); bgB = (sb / kept).toInt()
            }

            // --- threshold: how far the background texture itself strays from that colour. Measured as the
            // median deviation of each side's band; the second cleanest side decides, so that artwork
            // reaching into the band (wide crowns, piece bases, arrows) does not inflate it.
            java.util.Arrays.fill(sideHist, 0)
            val perSide = count / 4
            for (k in 0 until count) {
                val i = bandIndex[k]
                val d = max(abs(r[i] - bgR), max(abs(g[i] - bgG), abs(b[i] - bgB)))
                sideHist[bandSide[k] * 256 + d]++
            }
            var low1 = 255
            var low2 = 255
            for (side in 0..3) {
                var acc = 0
                var median = 255
                val target = (perSide + 1) / 2
                for (d in 0..255) { acc += sideHist[side * 256 + d]; if (acc >= target) { median = d; break } }
                if (median < low1) { low2 = low1; low1 = median } else if (median < low2) low2 = median
            }
            // The band can sit under an overlay that the corners escape - a capture ring running along the
            // middle of every edge, the radial "king in check" glow - or the corners carry a tint the band
            // escapes (corner-style capture hints). When the two disagree both colours count as background,
            // so the overlay never becomes part of a piece. A glow (band darker than the corners, and not
            // by plain darkening) is remembered and taken out of the piece's interior further down.
            val cornerR = cornerMedian(r, lo, hi)
            val cornerG = cornerMedian(g, lo, hi)
            val cornerB = cornerMedian(b, lo, hi)
            val tintR = bgR - cornerR
            val tintG = bgG - cornerG
            val tintB = bgB - cornerB
            val tint = max(abs(tintR), max(abs(tintG), abs(tintB)))
            val twoTone = tint >= SECOND_BG_MIN_TINT
            val bandDarker = tintR * 77 + tintG * 150 + tintB * 29 < 0
            val ring = twoTone && bandDarker && isUniformDarkening(bgR, bgG, bgB, cornerR, cornerG, cornerB)
            val glow = bandDarker && !ring && tint >= GLOW_MIN_TINT

            // A flat background needs a step proportional to its brightness (a 10 % darkening overlay such
            // as a capture ring must stay background on any square, yet dark artwork on a dark square
            // must not); a textured one needs to clear its own texture.
            val flat = (TAU_RELATIVE * max(bgR, max(bgG, bgB))).roundToInt().coerceIn(TAU_MIN, TAU_FLAT_MAX)
            val tau = min(TAU_MAX, max(flat, (TAU_TEXTURE_GAIN * low2).roundToInt() + TAU_TEXTURE_BASE))
            f.threshold = tau
            // under a ring the corners show the square's real colour
            f.bgLuma = if (ring) (cornerR * 77 + cornerG * 150 + cornerB * 29) shr 8 else (bgR * 77 + bgG * 150 + bgB * 29) shr 8

            // --- foreground mask
            for (y in lo until hi) for (x in lo until hi) {
                val i = y * n + x
                var isFg = abs(r[i] - bgR) > tau || abs(g[i] - bgG) > tau || abs(b[i] - bgB) > tau
                if (isFg && twoTone) {
                    isFg = abs(r[i] - cornerR) > tau || abs(g[i] - cornerG) > tau || abs(b[i] - cornerB) > tau
                }
                fg[i] = isFg
                outside[i] = false
            }

            // --- flood the background in from the inner border
            var head = 0
            var tail = 0
            for (t in lo until hi) {
                for (side in 0..3) {
                    val i = when (side) {
                        0 -> lo * n + t
                        1 -> (hi - 1) * n + t
                        2 -> t * n + lo
                        else -> t * n + hi - 1
                    }
                    if (!fg[i] && !outside[i]) { outside[i] = true; queue[tail++] = i }
                }
            }
            while (head < tail) {
                val i = queue[head++]
                val x = i % n
                val y = i / n
                if (x > lo) { val j = i - 1; if (!fg[j] && !outside[j]) { outside[j] = true; queue[tail++] = j } }
                if (x < hi - 1) { val j = i + 1; if (!fg[j] && !outside[j]) { outside[j] = true; queue[tail++] = j } }
                if (y > lo) { val j = i - n; if (!fg[j] && !outside[j]) { outside[j] = true; queue[tail++] = j } }
                if (y < hi - 1) { val j = i + n; if (!fg[j] && !outside[j]) { outside[j] = true; queue[tail++] = j } }
            }

            // --- piece region: whatever the background could not reach (outline plus enclosed fill)
            for (y in lo until hi) for (x in lo until hi) {
                val i = y * n + x
                core[i] = !outside[i]
            }

            // --- interior tone: the region shrunk by one pixel drops the anti-aliased rim
            erodeCore(lo, hi)
            val store = cell * n * n
            for (i in 0 until n * n) {
                out.luma[store + i] = lumaAt(i).toByte()
                out.interior[store + i] = false
            }
            var interiorCount = 0
            for (y in lo until hi) for (x in lo until hi) {
                val i = y * n + x
                if (core[i]) { out.interior[store + i] = true; interiorCount++ }
            }
            // A glow under the piece (the red "king in check" gradient) is part of the region but not of the
            // piece; left in, its dark red would outweigh a light piece's fill. Its pixels continue the tint
            // that separates the band from the corners: they deviate from the background colour in that
            // very direction. Piece material does not (white goes the other way, black darkens all channels).
            if (glow) {
                val vv = (tintR * tintR + tintG * tintG + tintB * tintB).toLong()
                var glowCount = 0
                for (y in lo until hi) for (x in lo until hi) {
                    val i = y * n + x
                    scratch[i] = false
                    if (!core[i] || !fg[i]) continue
                    val dr = r[i] - bgR; val dg = g[i] - bgG; val db = b[i] - bgB
                    val dot = (dr * tintR + dg * tintG + db * tintB).toLong()
                    // cos(angle between deviation and tint) >= 0.92
                    if (dot > 0 && dot * dot * 10000 >= 8464 * vv * (dr * dr + dg * dg + db * db)) { scratch[i] = true; glowCount++ }
                }
                // only when a real piece remains; otherwise this was no glow after all
                if (glowCount > 0 && (interiorCount - glowCount) * 5 >= interiorCount) {
                    for (y in lo until hi) for (x in lo until hi) {
                        val i = y * n + x
                        if (scratch[i]) out.interior[store + i] = false
                    }
                }
            }
            java.util.Arrays.fill(f.toneHist, 0)
            var toneCount = 0
            for (y in lo until hi) for (x in lo until hi) {
                val i = y * n + x
                if (out.interior[store + i]) { f.toneHist[lumaAt(i) * TONE_BINS / 256]++; toneCount++ }
            }
            f.toneCount = toneCount

            // --- solid blob: keep eroding; texture speckle, hatching, labels and thin logos vanish
            val erosions = max(1, (ERODE * n).roundToInt())
            for (k in 1 until erosions) erodeCore(lo, hi)
            // Connected blobs of what is left. Only blobs comparable to the biggest one count, so that the
            // remains of a coordinate label next to a move dot do not stretch the dot's extent.
            var blobs = 0
            for (y in lo until hi) for (x in lo until hi) {
                val start = y * n + x
                if (!core[start]) continue
                if (blobs == blobArea.size) break
                core[start] = false
                var head2 = 0
                var tail2 = 0
                queue[tail2++] = start
                var top = y; var bottom = y
                while (head2 < tail2) {
                    val i = queue[head2++]
                    val px = i % n
                    val py = i / n
                    if (py < top) top = py
                    if (py > bottom) bottom = py
                    if (px > lo && core[i - 1]) { core[i - 1] = false; queue[tail2++] = i - 1 }
                    if (px < hi - 1 && core[i + 1]) { core[i + 1] = false; queue[tail2++] = i + 1 }
                    if (py > lo && core[i - n]) { core[i - n] = false; queue[tail2++] = i - n }
                    if (py < hi - 1 && core[i + n]) { core[i + n] = false; queue[tail2++] = i + n }
                }
                blobArea[blobs] = tail2
                blobTop[blobs] = top; blobBottom[blobs] = bottom
                blobs++
            }
            var biggest = 0
            for (k in 0 until blobs) if (blobArea[k] > biggest) biggest = blobArea[k]
            var coreCount = 0
            var top = n; var bottom = -1
            for (k in 0 until blobs) {
                if (blobArea[k] * 8 < biggest) continue
                coreCount += blobArea[k]
                if (blobTop[k] < top) top = blobTop[k]
                if (blobBottom[k] > bottom) bottom = blobBottom[k]
            }
            f.coreArea = coreCount.toFloat() / inner
            f.coreMain = biggest.toFloat() / inner
            f.boxHeight = if (bottom < 0) 0f else (bottom - top + 1 + 2 * erosions).toFloat() / n
        }

        /** Per-channel median over the four corner patches of the inner region (each patch averaged). */
        private fun cornerMedian(channel: IntArray, lo: Int, hi: Int): Int {
            val patch = max(2, (CORNER_PATCH * n).roundToInt())
            val means = IntArray(4)
            for (k in 0..3) {
                val x0 = if (k and 1 == 0) lo else hi - patch
                val y0 = if (k and 2 == 0) lo else hi - patch
                var sum = 0
                for (y in y0 until y0 + patch) for (x in x0 until x0 + patch) sum += channel[y * n + x]
                means[k] = sum / (patch * patch)
            }
            means.sort()
            return (means[1] + means[2]) / 2
        }

        /** True when colour 1 is colour 2 darkened by one common factor (a translucent black overlay). */
        private fun isUniformDarkening(r1: Int, g1: Int, b1: Int, r2: Int, g2: Int, b2: Int): Boolean {
            var lowest = 2f
            var highest = 0f
            var used = 0
            if (r2 >= 40) { val q = r1.toFloat() / r2; lowest = min(lowest, q); highest = max(highest, q); used++ }
            if (g2 >= 40) { val q = g1.toFloat() / g2; lowest = min(lowest, q); highest = max(highest, q); used++ }
            if (b2 >= 40) { val q = b1.toFloat() / b2; lowest = min(lowest, q); highest = max(highest, q); used++ }
            return used >= 2 && lowest >= 0.72f && highest <= 0.97f && highest - lowest <= 0.05f
        }

        /** One round of 4-neighbour erosion of [core] within the inner region. */
        private fun erodeCore(lo: Int, hi: Int) {
            for (y in lo until hi) for (x in lo until hi) {
                val i = y * n + x
                scratch[i] = core[i] && x > lo && x < hi - 1 && y > lo && y < hi - 1 &&
                    core[i - 1] && core[i + 1] && core[i - n] && core[i + n]
            }
            for (y in lo until hi) for (x in lo until hi) { val i = y * n + x; core[i] = scratch[i] }
        }
    }

    internal fun classify(rect: BoardRect, analysis: Analysis): BoardSnapshot {
        val features = analysis.features
        val cells = Array(64) { CellState.EMPTY }
        val confidence = FloatArray(64)
        val occupied = BooleanArray(64)
        val global = IntArray(TONE_BINS)
        var any = false
        for (i in 0 until 64) {
            val f = features[i]
            occupied[i] = f.coreArea >= MIN_CORE_AREA && f.coreMain >= MIN_CORE_MAIN && f.boxHeight >= MIN_BOX && f.toneCount > 0
            if (occupied[i]) {
                // every piece gets the same say, however big its artwork is
                for (k in 0 until TONE_BINS) global[k] += f.toneHist[k] * 1024 / f.toneCount
                any = true
            }
        }
        // Board-wide luma that separates "light piece material" from "dark piece material". Clamped so that
        // a board showing pieces of one colour only does not split that colour's own shading.
        val binWidth = 256 / TONE_BINS
        val split = if (any) (otsu(global) * binWidth).coerceIn(TONE_SPLIT_MIN, TONE_SPLIT_MAX) else 128
        val n = analysis.n
        val a = BooleanArray(n * n)
        val b = BooleanArray(n * n)
        val scratch = BooleanArray(n * n)
        val rounds = max(1, (TONE_ERODE * n).roundToInt())
        var light = 0
        var dark = 0
        for (i in 0 until 64) {
            val f = features[i]
            if (!occupied[i]) {
                cells[i] = CellState.EMPTY
                // certain when nothing solid is there at all, or when what is there is clearly too small
                val byArea = 1f - f.coreArea / MIN_CORE_AREA
                val byBox = (MIN_BOX - f.boxHeight) / BOX_MARGIN
                // on a strongly textured square a low-contrast piece can hide below the raised threshold
                val byTexture = (1.3f - f.threshold / 100f).coerceIn(0.3f, 1f)
                confidence[i] = (max(byArea, byBox) * byTexture).coerceIn(MIN_CONFIDENCE, 1f)
                continue
            }
            // Solid (erosion-surviving) bright and dark parts of the interior: a light piece keeps its fill
            // and loses its outline, a dark piece keeps its body and loses highlights and detail lines.
            val store = i * n * n
            for (k in 0 until n * n) {
                val inside = analysis.interior[store + k]
                val bright = (analysis.luma[store + k].toInt() and 0xFF) >= split
                a[k] = inside && bright
                b[k] = inside && !bright
            }
            val thickBright = erodedCount(a, scratch, n, rounds)
            val thickDark = erodedCount(b, scratch, n, rounds)
            val solid = thickBright + thickDark
            f.lightness = if (solid >= THIN_PIECE_SOLID * f.toneCount) {
                thickBright.toFloat() / solid
            } else {
                // Nothing solid either way: a piece drawn with thin strokes only (the spiky, striped queens
                // of some sets). A dark piece always has a solid dark body, so this is a light piece unless
                // hardly anything in it is bright; scaled so that LIGHT_SHARE is the decision point again.
                var above = 0
                for (k in split / binWidth until TONE_BINS) above += f.toneHist[k]
                min(1f, above.toFloat() / f.toneCount * (LIGHT_SHARE / THIN_PIECE_BRIGHT))
            }
            val isLight = f.lightness >= LIGHT_SHARE
            cells[i] = if (isLight) CellState.LIGHT_PIECE else CellState.DARK_PIECE
            if (isLight) light++ else dark++
            val bySize = min((f.boxHeight - MIN_BOX) / BOX_MARGIN, (f.coreArea - MIN_CORE_AREA) / AREA_MARGIN)
            val byTone = abs(f.lightness - LIGHT_SHARE) / TONE_MARGIN
            confidence[i] = min(bySize, byTone).coerceIn(MIN_CONFIDENCE, 1f)
        }
        var mean = 0f
        for (c in confidence) mean += c
        mean /= 64f
        var shaky = 0
        for (c in confidence) if (c < SHAKY_CONFIDENCE) shaky++
        val reliable = analysis.n > 0 && light + dark <= 32 && light <= 16 && dark <= 16 &&
            mean >= MIN_MEAN_CONFIDENCE && shaky <= MAX_SHAKY_CELLS && checkerPairs(features) >= MIN_CHECKER_PAIRS
        return BoardSnapshot(rect, cells, confidence, reliable)
    }

    /**
     * How many of the 112 pairs of neighbouring squares show the light/dark step of a checkerboard in their
     * background tones. On a real board nearly all do (highlighted squares may not); a rectangle that is not
     * sitting on a board fails this however plausible the "pieces" found in it look.
     */
    internal fun checkerPairs(features: Array<CellFeatures>): Int {
        val diffs = IntArray(112)
        var n = 0
        var sum = 0
        for (row in 0..7) for (col in 0..7) {
            val t = features[row * 8 + col].bgLuma
            val sign = if ((row + col) and 1 == 0) 1 else -1
            if (col < 7) { val d = sign * (t - features[row * 8 + col + 1].bgLuma); diffs[n++] = d; sum += d }
            if (row < 7) { val d = sign * (t - features[(row + 1) * 8 + col].bgLuma); diffs[n++] = d; sum += d }
        }
        val polarity = if (sum >= 0) 1 else -1
        val sorted = IntArray(n) { diffs[it] * polarity }
        sorted.sort()
        val contrast = sorted[n / 2]
        if (contrast < MIN_CHECKER_CONTRAST) return 0
        var pairs = 0
        for (k in 0 until n) if (diffs[k] * polarity * 2 >= contrast) pairs++
        return pairs
    }

    /** Erodes [mask] in place [rounds] times (4-neighbourhood, image border counts as outside); returns what is left. */
    private fun erodedCount(mask: BooleanArray, scratch: BooleanArray, n: Int, rounds: Int): Int {
        repeat(rounds) {
            for (y in 0 until n) for (x in 0 until n) {
                val i = y * n + x
                scratch[i] = mask[i] && x > 0 && x < n - 1 && y > 0 && y < n - 1 &&
                    mask[i - 1] && mask[i + 1] && mask[i - n] && mask[i + n]
            }
            System.arraycopy(scratch, 0, mask, 0, n * n)
        }
        var count = 0
        for (v in mask) if (v) count++
        return count
    }

    /** Otsu split of a histogram: returns the first bin of the upper class. */
    private fun otsu(hist: IntArray): Int {
        var total = 0L
        var sumAll = 0L
        for (k in hist.indices) { total += hist[k]; sumAll += k.toLong() * hist[k] }
        if (total == 0L) return hist.size / 2
        var best = hist.size / 2
        var bestVar = -1.0
        var w0 = 0L
        var sum0 = 0L
        for (k in 0 until hist.size - 1) {
            w0 += hist[k]
            sum0 += k.toLong() * hist[k]
            val w1 = total - w0
            if (w0 == 0L || w1 == 0L) continue
            val m0 = sum0.toDouble() / w0
            val m1 = (sumAll - sum0).toDouble() / w1
            val between = w0.toDouble() * w1 * (m0 - m1) * (m0 - m1)
            if (between > bestVar) { bestVar = between; best = k + 1 }
        }
        return best
    }

    private const val MIN_CELL_PX = 12
    private const val WORK_CELL = 40
    private const val MARGIN = 0.045f
    private const val BAND = 0.07f
    private const val BG_LUMA_WINDOW = 14
    private const val BG_REFINE = 20
    private const val TAU_MIN = 12
    private const val TAU_FLAT_MAX = 30
    private const val TAU_RELATIVE = 0.118f
    private const val TAU_MAX = 120
    private const val TAU_TEXTURE_GAIN = 4f
    private const val TAU_TEXTURE_BASE = 10
    private const val CORNER_PATCH = 0.08f
    private const val SECOND_BG_MIN_TINT = 8
    private const val GLOW_MIN_TINT = 20
    private const val ERODE = 0.045f
    private const val TONE_BINS = 64
    private const val MIN_CORE_AREA = 0.012f
    private const val MIN_CORE_MAIN = 0.008f
    private const val MIN_BOX = 0.36f
    private const val TONE_SPLIT_MIN = 90
    private const val TONE_SPLIT_MAX = 170
    private const val TONE_ERODE = 0.022f
    private const val LIGHT_SHARE = 0.37f
    private const val THIN_PIECE_SOLID = 0.10f
    private const val THIN_PIECE_BRIGHT = 0.25f
    private const val BOX_MARGIN = 0.12f
    private const val AREA_MARGIN = 0.03f
    private const val TONE_MARGIN = 0.2f
    private const val MIN_CONFIDENCE = 0.05f
    private const val SHAKY_CONFIDENCE = 0.25f
    private const val MAX_SHAKY_CELLS = 8
    private const val MIN_MEAN_CONFIDENCE = 0.6f
    private const val MIN_CHECKER_PAIRS = 84
    private const val MIN_CHECKER_CONTRAST = 6
}
