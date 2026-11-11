package com.krstech.chesshelp.capture

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Pixel conversion for `ImageReader` buffers in `PixelFormat.RGBA_8888`.
 *
 * In memory every pixel is the four bytes R, G, B, A. Read as one little-endian word that is `0xAABBGGRR`,
 * whereas a `Frame` pixel is `0xAARRGGBB`: the two differ by swapping the R and B bytes.
 */
internal object RgbaPixels {

    /** Converts a whole buffer to `0xAARRGGBB` pixels, row-major, without the row padding. */
    fun toArgb(buffer: ByteBuffer, width: Int, height: Int, rowStride: Int, pixelStride: Int): IntArray {
        val pixels = IntArray(pixelCount(width, height))
        readWords(buffer, width, height, rowStride, pixelStride, pixels)
        wordsToArgb(pixels, pixels.size)
        return pixels
    }

    /**
     * Copies `width * height` pixels out of [buffer] into [out] as little-endian words (`0xAABBGGRR`),
     * skipping the padding at the end of each row. The data starts at the buffer's current position; the
     * buffer's own position, limit and byte order are left untouched.
     *
     * This is the cheap half of the conversion (a bulk copy per row), done for every captured frame;
     * [wordsToArgb] finishes the job only for the frames that are actually used.
     */
    fun readWords(buffer: ByteBuffer, width: Int, height: Int, rowStride: Int, pixelStride: Int, out: IntArray) {
        val count = pixelCount(width, height)
        require(pixelStride >= BYTES_PER_PIXEL) { "RGBA_8888 needs a pixel stride of at least 4, got $pixelStride" }
        val rowBytes = (width - 1).toLong() * pixelStride + BYTES_PER_PIXEL
        require(rowStride >= rowBytes) { "Row stride $rowStride is too small for $width pixels of stride $pixelStride" }
        require(out.size >= count) { "Output holds ${out.size} pixels, ${width}x$height needs $count" }

        // The last row is not padded in every implementation, so only its pixels are required to be there.
        val needed = (height - 1).toLong() * rowStride + rowBytes
        val source = buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN)
        require(source.remaining() >= needed) {
            "Buffer has ${source.remaining()} bytes, ${width}x$height with row stride $rowStride needs $needed"
        }

        if (pixelStride == BYTES_PER_PIXEL && rowStride % BYTES_PER_PIXEL == 0) {
            val words = source.asIntBuffer()
            val rowWords = rowStride / BYTES_PER_PIXEL
            if (rowWords == width) {
                words.get(out, 0, count)
            } else {
                for (y in 0 until height) {
                    words.position(y * rowWords)
                    words.get(out, y * width, width)
                }
            }
            return
        }

        val start = source.position()
        val row = ByteArray(rowBytes.toInt())
        for (y in 0 until height) {
            source.position(start + y * rowStride)
            source.get(row, 0, row.size)
            var o = y * width
            var i = 0
            repeat(width) {
                out[o++] = (row[i].toInt() and 0xFF) or
                    ((row[i + 1].toInt() and 0xFF) shl 8) or
                    ((row[i + 2].toInt() and 0xFF) shl 16) or
                    ((row[i + 3].toInt() and 0xFF) shl 24)
                i += pixelStride
            }
        }
    }

    /** Turns the first [count] little-endian RGBA words (`0xAABBGGRR`) into `0xAARRGGBB`, in place. */
    fun wordsToArgb(words: IntArray, count: Int) {
        require(count in 0..words.size) { "count $count is outside 0..${words.size}" }
        for (i in 0 until count) {
            val w = words[i]
            words[i] = (w and GREEN_ALPHA_MASK) or ((w and 0xFF) shl 16) or ((w ushr 16) and 0xFF)
        }
    }

    private fun pixelCount(width: Int, height: Int): Int {
        require(width > 0 && height > 0) { "Image dimensions must be positive: ${width}x$height" }
        val count = width.toLong() * height
        require(count <= Int.MAX_VALUE) { "Image ${width}x$height is too large" }
        return count.toInt()
    }

    private const val BYTES_PER_PIXEL = 4
    private const val GREEN_ALPHA_MASK = 0xFF00FF00.toInt()
}
