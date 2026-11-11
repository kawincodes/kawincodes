package com.krstech.chesshelp.vision

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object FrameOps {

    /**
     * Copies the given region. The region is clipped to the frame; an [IllegalArgumentException] is thrown
     * when nothing of it lies inside the frame.
     */
    fun crop(frame: Frame, left: Int, top: Int, width: Int, height: Int): Frame {
        val x0 = max(0, left)
        val y0 = max(0, top)
        val x1 = min(frame.width.toLong(), left.toLong() + width).toInt()
        val y1 = min(frame.height.toLong(), top.toLong() + height).toInt()
        require(x1 > x0 && y1 > y0) {
            "Crop region ($left,$top ${width}x$height) is outside the ${frame.width}x${frame.height} frame"
        }
        val w = x1 - x0
        val h = y1 - y0
        val out = IntArray(w * h)
        for (y in 0 until h) {
            System.arraycopy(frame.pixels, (y0 + y) * frame.width + x0, out, y * w, w)
        }
        return Frame(w, h, out)
    }

    /** The board's squares area resampled (area-averaged) to `outSize x outSize`. */
    fun cropBoard(frame: Frame, rect: BoardRect, outSize: Int): Frame {
        require(outSize > 0) { "outSize must be positive: $outSize" }
        val x0 = max(0, rect.left)
        val y0 = max(0, rect.top)
        val x1 = min(frame.width, rect.right)
        val y1 = min(frame.height, rect.bottom)
        require(x1 > x0 && y1 > y0) { "Board rect $rect is outside the ${frame.width}x${frame.height} frame" }
        return resample(frame, x0, y0, x1 - x0, y1 - y0, outSize, outSize)
    }

    /**
     * Shrinks the frame so that its longer side is at most [maxDimension], keeping the aspect ratio.
     * A frame that already fits is returned as is (never upscaled).
     */
    fun downscale(frame: Frame, maxDimension: Int): Frame {
        require(maxDimension > 0) { "maxDimension must be positive: $maxDimension" }
        val longer = max(frame.width, frame.height)
        if (longer <= maxDimension) return frame
        val scale = maxDimension.toDouble() / longer
        val outW = min(maxDimension, max(1, (frame.width * scale).roundToInt()))
        val outH = min(maxDimension, max(1, (frame.height * scale).roundToInt()))
        return resample(frame, 0, 0, frame.width, frame.height, outW, outH)
    }

    /**
     * Box-filter resample of a source region: every output pixel is the coverage-weighted mean of the source
     * pixels under it, so downscaling averages areas and upscaling degenerates to (blended) nearest pixels.
     */
    internal fun resample(frame: Frame, srcLeft: Int, srcTop: Int, srcW: Int, srcH: Int, outW: Int, outH: Int): Frame {
        val out = IntArray(outW * outH)
        if (srcW == outW && srcH == outH) {
            for (y in 0 until outH) {
                System.arraycopy(frame.pixels, (srcTop + y) * frame.width + srcLeft, out, y * outW, outW)
            }
            return Frame(outW, outH, out)
        }
        val sx = srcW.toDouble() / outW
        val sy = srcH.toDouble() / outH

        // Horizontal taps: output x covers source [xa, xb) with fractional weights at both ends.
        val xFirst = IntArray(outW)
        val xLast = IntArray(outW)
        val wFirst = FloatArray(outW)
        val wLast = FloatArray(outW)
        for (ox in 0 until outW) {
            val a = ox * sx
            val b = min(srcW.toDouble(), (ox + 1) * sx)
            val first = min(srcW - 1, a.toInt())
            var last = min(srcW - 1, b.toInt())
            if (last > first && b - last <= 1e-9) last--
            xFirst[ox] = first
            xLast[ox] = last
            if (first == last) {
                wFirst[ox] = (b - a).toFloat()
                wLast[ox] = 0f
            } else {
                wFirst[ox] = (first + 1 - a).toFloat()
                wLast[ox] = (b - last).toFloat()
            }
        }

        val row = FloatArray(outW * 4)      // one source row, horizontally resampled (sum, not mean)
        val acc = FloatArray(outW * 4)
        var cachedRow = -1
        val norm = (1.0 / (sx * sy)).toFloat()
        val pixels = frame.pixels
        val stride = frame.width

        for (oy in 0 until outH) {
            val a = oy * sy
            val b = min(srcH.toDouble(), (oy + 1) * sy)
            val first = min(srcH - 1, a.toInt())
            var last = min(srcH - 1, b.toInt())
            if (last > first && b - last <= 1e-9) last--
            java.util.Arrays.fill(acc, 0f)
            for (syi in first..last) {
                val wy = when {
                    first == last -> (b - a).toFloat()
                    syi == first -> (first + 1 - a).toFloat()
                    syi == last -> (b - last).toFloat()
                    else -> 1f
                }
                if (syi != cachedRow) {
                    val base = (srcTop + syi) * stride + srcLeft
                    for (ox in 0 until outW) {
                        val f = xFirst[ox]
                        val l = xLast[ox]
                        var p = pixels[base + f]
                        var w = wFirst[ox]
                        var sa = w * (p ushr 24)
                        var sr = w * ((p ushr 16) and 0xFF)
                        var sg = w * ((p ushr 8) and 0xFF)
                        var sb = w * (p and 0xFF)
                        for (xi in f + 1 until l) {
                            p = pixels[base + xi]
                            sa += (p ushr 24)
                            sr += ((p ushr 16) and 0xFF)
                            sg += ((p ushr 8) and 0xFF)
                            sb += (p and 0xFF)
                        }
                        if (l > f) {
                            p = pixels[base + l]
                            w = wLast[ox]
                            sa += w * (p ushr 24)
                            sr += w * ((p ushr 16) and 0xFF)
                            sg += w * ((p ushr 8) and 0xFF)
                            sb += w * (p and 0xFF)
                        }
                        val o = ox * 4
                        row[o] = sa
                        row[o + 1] = sr
                        row[o + 2] = sg
                        row[o + 3] = sb
                    }
                    cachedRow = syi
                }
                for (i in acc.indices) acc[i] += wy * row[i]
            }
            val outBase = oy * outW
            for (ox in 0 until outW) {
                val o = ox * 4
                val ca = clamp255(acc[o] * norm)
                val cr = clamp255(acc[o + 1] * norm)
                val cg = clamp255(acc[o + 2] * norm)
                val cb = clamp255(acc[o + 3] * norm)
                out[outBase + ox] = (ca shl 24) or (cr shl 16) or (cg shl 8) or cb
            }
        }
        return Frame(outW, outH, out)
    }

    private fun clamp255(v: Float): Int {
        val i = (v + 0.5f).toInt()
        return if (i < 0) 0 else if (i > 255) 255 else i
    }
}
