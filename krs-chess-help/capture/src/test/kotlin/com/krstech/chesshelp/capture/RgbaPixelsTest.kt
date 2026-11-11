package com.krstech.chesshelp.capture

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RgbaPixelsTest {

    /** Reference pixel (x, y): distinct R, G, B, A so that any byte mix-up shows. */
    private fun argbAt(x: Int, y: Int): Int {
        val r = (x * 7 + y * 3 + 1) and 0xFF
        val g = (x * 5 + y * 11 + 2) and 0xFF
        val b = (x * 13 + y * 17 + 3) and 0xFF
        val a = (x * 19 + y * 23 + 4) and 0xFF
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    /**
     * Builds an ImageReader-style buffer: bytes R,G,B,A per pixel, [pixelStride] bytes apart, rows
     * [rowStride] bytes apart, the last row without trailing padding, everything else filled with junk.
     */
    private fun rgbaBuffer(
        width: Int,
        height: Int,
        rowStride: Int,
        pixelStride: Int = 4,
        direct: Boolean = false,
        leading: Int = 0,
    ): ByteBuffer {
        val size = leading + (height - 1) * rowStride + (width - 1) * pixelStride + 4
        val bytes = ByteArray(size) { 0x5A }
        for (y in 0 until height) {
            for (x in 0 until width) {
                val p = argbAt(x, y)
                val i = leading + y * rowStride + x * pixelStride
                bytes[i] = (p ushr 16).toByte()
                bytes[i + 1] = (p ushr 8).toByte()
                bytes[i + 2] = p.toByte()
                bytes[i + 3] = (p ushr 24).toByte()
            }
        }
        val buffer = if (direct) ByteBuffer.allocateDirect(size).put(bytes) else ByteBuffer.wrap(bytes)
        buffer.position(leading)
        return buffer
    }

    private fun expected(width: Int, height: Int) = IntArray(width * height) { argbAt(it % width, it / width) }

    @Test
    fun singlePixelByteOrder() {
        val buffer = ByteBuffer.wrap(byteArrayOf(0x11, 0x22, 0x33, 0x44)) // R, G, B, A
        assertContentEquals(intArrayOf(0x44112233), RgbaPixels.toArgb(buffer, 1, 1, 4, 4))
    }

    @Test
    fun tightlyPackedRows() {
        val pixels = RgbaPixels.toArgb(rgbaBuffer(6, 4, rowStride = 24), 6, 4, 24, 4)
        assertContentEquals(expected(6, 4), pixels)
    }

    @Test
    fun rowPaddingIsSkipped() {
        for (direct in listOf(false, true)) {
            val pixels = RgbaPixels.toArgb(rgbaBuffer(5, 7, rowStride = 32, direct = direct), 5, 7, 32, 4)
            assertContentEquals(expected(5, 7), pixels, "direct=$direct")
        }
    }

    @Test
    fun oddSizes() {
        for ((w, h) in listOf(1 to 1, 1 to 9, 9 to 1, 3 to 5, 7 to 3, 13 to 11, 101 to 53)) {
            for (padPixels in listOf(0, 1, 3, 16)) {
                val stride = (w + padPixels) * 4
                for (direct in listOf(false, true)) {
                    val pixels = RgbaPixels.toArgb(rgbaBuffer(w, h, stride, direct = direct), w, h, stride, 4)
                    assertContentEquals(expected(w, h), pixels, "${w}x$h stride=$stride direct=$direct")
                }
            }
        }
    }

    @Test
    fun rowStrideThatIsNotAMultipleOfFour() {
        val pixels = RgbaPixels.toArgb(rgbaBuffer(5, 6, rowStride = 23), 5, 6, 23, 4)
        assertContentEquals(expected(5, 6), pixels)
    }

    @Test
    fun widerPixelStride() {
        for (direct in listOf(false, true)) {
            val pixels = RgbaPixels.toArgb(rgbaBuffer(5, 4, rowStride = 48, pixelStride = 8, direct = direct), 5, 4, 48, 8)
            assertContentEquals(expected(5, 4), pixels, "direct=$direct")
        }
    }

    @Test
    fun dataStartsAtTheBufferPositionAndTheBufferIsLeftUntouched() {
        for (direct in listOf(false, true)) {
            for (leading in listOf(4, 3)) { // 3: the words are not 4-byte aligned in the buffer
                val buffer = rgbaBuffer(6, 5, rowStride = 40, direct = direct, leading = leading)
                buffer.order(ByteOrder.BIG_ENDIAN)
                val limit = buffer.limit()
                val pixels = RgbaPixels.toArgb(buffer, 6, 5, 40, 4)
                assertContentEquals(expected(6, 5), pixels, "direct=$direct leading=$leading")
                assertEquals(leading, buffer.position())
                assertEquals(limit, buffer.limit())
                assertEquals(ByteOrder.BIG_ENDIAN, buffer.order())
            }
        }
    }

    @Test
    fun byteOrderOfTheSourceBufferDoesNotMatter() {
        val little = rgbaBuffer(4, 3, rowStride = 20).order(ByteOrder.LITTLE_ENDIAN)
        val big = rgbaBuffer(4, 3, rowStride = 20).order(ByteOrder.BIG_ENDIAN)
        assertContentEquals(RgbaPixels.toArgb(little, 4, 3, 20, 4), RgbaPixels.toArgb(big, 4, 3, 20, 4))
    }

    @Test
    fun readWordsThenWordsToArgbEqualsToArgb() {
        val out = IntArray(5 * 7 + 3) { -1 }
        RgbaPixels.readWords(rgbaBuffer(5, 7, rowStride = 28), 5, 7, 28, 4, out)
        // Raw words keep the memory order: 0xAABBGGRR.
        val first = argbAt(0, 0)
        val word = (first and 0xFF00FF00.toInt()) or ((first and 0xFF) shl 16) or ((first ushr 16) and 0xFF)
        assertEquals(word, out[0])
        RgbaPixels.wordsToArgb(out, 35)
        assertContentEquals(expected(5, 7), out.copyOf(35))
        assertContentEquals(intArrayOf(-1, -1, -1), out.copyOfRange(35, 38), "pixels beyond the image are untouched")
    }

    @Test
    fun fastAndGenericPathsAgreeOnRandomData() {
        val random = Random(42)
        repeat(50) {
            val w = random.nextInt(1, 40)
            val h = random.nextInt(1, 40)
            val stride = (w + random.nextInt(0, 9)) * 4
            val bytes = ByteArray((h - 1) * stride + w * 4).also(random::nextBytes)
            val fast = RgbaPixels.toArgb(ByteBuffer.wrap(bytes), w, h, stride, 4)
            for (y in 0 until h) for (x in 0 until w) {
                val i = y * stride + x * 4
                val want = ((bytes[i + 3].toInt() and 0xFF) shl 24) or ((bytes[i].toInt() and 0xFF) shl 16) or
                    ((bytes[i + 1].toInt() and 0xFF) shl 8) or (bytes[i + 2].toInt() and 0xFF)
                assertEquals(want, fast[y * w + x], "${w}x$h stride=$stride at ($x,$y)")
            }
        }
    }

    @Test
    fun rejectsBuffersThatAreTooSmallOrBadGeometry() {
        val buffer = rgbaBuffer(4, 4, rowStride = 16)
        assertFailsWith<IllegalArgumentException> { RgbaPixels.toArgb(buffer, 4, 5, 16, 4) }
        assertFailsWith<IllegalArgumentException> { RgbaPixels.toArgb(buffer, 4, 4, 12, 4) }
        assertFailsWith<IllegalArgumentException> { RgbaPixels.toArgb(buffer, 4, 4, 16, 3) }
        assertFailsWith<IllegalArgumentException> { RgbaPixels.toArgb(buffer, 0, 4, 16, 4) }
        assertFailsWith<IllegalArgumentException> { RgbaPixels.toArgb(buffer, 4, -1, 16, 4) }
        assertFailsWith<IllegalArgumentException> { RgbaPixels.readWords(buffer, 4, 4, 16, 4, IntArray(15)) }
        val shortLastRow = ByteBuffer.wrap(ByteArray(3 * 32 + 15))
        assertFailsWith<IllegalArgumentException> { RgbaPixels.toArgb(shortLastRow, 4, 4, 32, 4) }
    }

    @Test
    fun lastRowNeedsNoPadding() {
        val exact = ByteBuffer.wrap(ByteArray(3 * 32 + 16))
        assertEquals(16, RgbaPixels.toArgb(exact, 4, 4, 32, 4).size)
    }

    @Test
    fun fullScreenFrameConvertsQuickly() {
        val w = 1080
        val h = 2400
        val stride = 1088 * 4 // padded, like a real gralloc buffer
        val buffer = ByteBuffer.allocateDirect((h - 1) * stride + w * 4)
        val random = Random(7)
        val chunk = ByteArray(1 shl 16)
        while (buffer.remaining() > 0) {
            random.nextBytes(chunk)
            buffer.put(chunk, 0, minOf(chunk.size, buffer.remaining()))
        }
        buffer.position(0)
        repeat(5) { RgbaPixels.toArgb(buffer, w, h, stride, 4) } // warm-up
        var best = Long.MAX_VALUE
        var pixels = IntArray(0)
        repeat(10) {
            val start = System.nanoTime()
            pixels = RgbaPixels.toArgb(buffer, w, h, stride, 4)
            best = minOf(best, System.nanoTime() - start)
        }
        println("RgbaPixels.toArgb 1080x2400 (padded rows): best of 10 = ${best / 1_000} us")
        // Spot-check against the bytes, then a generous bound that only a per-pixel implementation misses.
        for ((x, y) in listOf(0 to 0, 1079 to 0, 0 to 2399, 1079 to 2399, 517 to 1203)) {
            val i = y * stride + x * 4
            val want = ((buffer.get(i + 3).toInt() and 0xFF) shl 24) or ((buffer.get(i).toInt() and 0xFF) shl 16) or
                ((buffer.get(i + 1).toInt() and 0xFF) shl 8) or (buffer.get(i + 2).toInt() and 0xFF)
            assertEquals(want, pixels[y * w + x])
        }
        assertTrue(best < 100_000_000, "conversion took ${best / 1_000_000} ms")
    }
}
