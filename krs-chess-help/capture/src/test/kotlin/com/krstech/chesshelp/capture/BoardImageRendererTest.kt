package com.krstech.chesshelp.capture

import com.krstech.chesshelp.vision.BoardRect
import com.krstech.chesshelp.vision.Frame
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Renders with the real Android graphics code (Robolectric's native graphics mode) and inspects the
 * encoded images pixel by pixel. The images are also written to `build/render-preview` for a look.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BoardImageRendererTest {

    private val rect = BoardRect(left = 60, top = 700, size = 960, confidence = 1f)

    /** A 1080x2400 "screenshot": grey background and a two-tone board with a coloured marker in each cell. */
    private fun screenshot(alpha: Int = 0xFF): Frame {
        val width = 1080
        val height = 2400
        val pixels = IntArray(width * height) { BACKGROUND }
        val cell = rect.size / 8
        for (row in 0 until 8) for (col in 0 until 8) {
            val square = if ((row + col) % 2 == 0) LIGHT_SQUARE else DARK_SQUARE
            for (y in 0 until cell) for (x in 0 until cell) {
                val inMarker = x in 40 until 80 && y in 40 until 80
                pixels[(rect.top + row * cell + y) * width + rect.left + col * cell + x] =
                    if (inMarker) marker(row, col) else square
            }
        }
        if (alpha != 0xFF) for (i in pixels.indices) pixels[i] = (pixels[i] and 0xFFFFFF) or (alpha shl 24)
        return Frame(width, height, pixels)
    }

    private fun marker(row: Int, col: Int): Int = 0xFF000000.toInt() or ((40 + row * 25) shl 16) or ((40 + col * 25) shl 8) or 200

    private fun decode(bytes: ByteArray): BufferedImage = assertNotNull(ImageIO.read(ByteArrayInputStream(bytes)))

    private fun save(name: String, bytes: ByteArray) {
        File("build/render-preview").apply { mkdirs() }.resolve(name).writeBytes(bytes)
    }

    private fun BufferedImage.rgb(x: Int, y: Int): Int = getRGB(x, y) and 0xFFFFFF

    private fun isText(rgb: Int): Boolean =
        ((rgb shr 16) and 0xFF) > 170 && ((rgb shr 8) and 0xFF) > 170 && (rgb and 0xFF) > 170

    /** Bounding box (minX, minY, maxX, maxY) of the label pixels inside a region of the margin. */
    private fun BufferedImage.textBounds(x0: Int, y0: Int, x1: Int, y1: Int): IntArray? {
        var minX = Int.MAX_VALUE
        var minY = Int.MAX_VALUE
        var maxX = -1
        var maxY = -1
        for (y in y0 until y1) for (x in x0 until x1) {
            if (isText(rgb(x, y))) {
                minX = minOf(minX, x); maxX = maxOf(maxX, x)
                minY = minOf(minY, y); maxY = maxOf(maxY, y)
            }
        }
        return if (maxX < 0) null else intArrayOf(minX, minY, maxX, maxY)
    }

    @Test
    fun boardPngIsAnOpaquePngWithTheBoardInTheBottomRightCorner() {
        val bytes = BoardImageRenderer.boardPng(screenshot(), rect)
        save("board.png", bytes)
        assertEquals(listOf(0x89, 'P'.code, 'N'.code, 'G'.code), bytes.take(4).map { it.toInt() and 0xFF })
        val image = decode(bytes)
        assertEquals(812, image.width)
        assertEquals(812, image.height)
        for (y in 0 until image.height step 7) for (x in 0 until image.width step 7) {
            assertEquals(0xFF, image.getRGB(x, y) ushr 24, "pixel ($x,$y) must be opaque")
        }

        for (row in 0 until 8) for (col in 0 until 8) {
            val left = 44 + col * 96
            val top = 44 + row * 96
            val square = (if ((row + col) % 2 == 0) LIGHT_SQUARE else DARK_SQUARE) and 0xFFFFFF
            assertEquals(marker(row, col) and 0xFFFFFF, image.rgb(left + 48, top + 48), "marker of view cell r${row + 1} c${col + 1}")
            assertEquals(square, image.rgb(left + 3, top + 3), "square colour, top-left of r${row + 1} c${col + 1}")
            assertEquals(square, image.rgb(left + 92, top + 92), "square colour, bottom-right of r${row + 1} c${col + 1}")
        }
        // The margin is dark right up to the board, and the board starts exactly at the margin.
        assertFalse(isText(image.rgb(2, 2)))
        assertFalse(isText(image.rgb(43, 400)))
        assertFalse(isText(image.rgb(400, 43)))
        assertEquals(LIGHT_SQUARE and 0xFFFFFF, image.rgb(44, 44))
        assertEquals(DARK_SQUARE and 0xFFFFFF, image.rgb(811, 44 + 3))
    }

    @Test
    fun everyRowAndColumnHasALegibleLabelCentredInTheMargin() {
        val image = decode(BoardImageRenderer.boardPng(screenshot(), rect))
        val rowShapes = HashSet<String>()
        val columnShapes = HashSet<String>()
        for (i in 0 until 8) {
            val bandStart = 44 + i * 96
            val row = assertNotNull(image.textBounds(0, bandStart, 44, bandStart + 96), "row label r${i + 1}")
            val col = assertNotNull(image.textBounds(bandStart, 0, bandStart + 96, 44), "column label c${i + 1}")
            val (rowCx, rowCy) = (row[0] + row[2]) / 2f to (row[1] + row[3]) / 2f
            val (colCx, colCy) = (col[0] + col[2]) / 2f to (col[1] + col[3]) / 2f
            println("r${i + 1}: bounds ${row.toList()} centre ($rowCx,$rowCy)   c${i + 1}: bounds ${col.toList()} centre ($colCx,$colCy)")

            assertTrue(abs(rowCx - 22f) <= 2.5f, "r${i + 1} horizontally centred in the margin, was $rowCx")
            assertTrue(abs(rowCy - (bandStart + 48f)) <= 4f, "r${i + 1} vertically centred on its row, was $rowCy")
            assertTrue(abs(colCx - (bandStart + 48f)) <= 2.5f, "c${i + 1} horizontally centred on its column, was $colCx")
            assertTrue(abs(colCy - 22f) <= 4f, "c${i + 1} vertically centred in the margin, was $colCy")

            // Not clipped by the image edge or the board, and large enough to read.
            assertTrue(row[0] >= 2 && row[2] <= 41, "r${i + 1} fits the margin: ${row.toList()}")
            assertTrue(col[1] >= 2 && col[3] <= 41, "c${i + 1} fits the margin: ${col.toList()}")
            assertTrue(row[3] - row[1] + 1 >= 15 && col[3] - col[1] + 1 >= 15, "labels are at least 15 px tall")
            assertTrue(row[2] - row[0] + 1 >= 18 && col[2] - col[0] + 1 >= 18, "two characters wide")

            // Each label is a different text: compare the glyph shapes.
            rowShapes += shape(image, row)
            columnShapes += shape(image, col)
        }
        assertEquals(8, rowShapes.size, "r1..r8 must all look different")
        assertEquals(8, columnShapes.size, "c1..c8 must all look different")
    }

    private fun shape(image: BufferedImage, bounds: IntArray): String = buildString {
        for (y in bounds[1]..bounds[3]) {
            for (x in bounds[0]..bounds[2]) append(if (isText(image.rgb(x, y))) '#' else '.')
            append('\n')
        }
    }

    @Test
    fun aCaptureWithoutAlphaStillRendersTheBoard() {
        val image = decode(BoardImageRenderer.boardPng(screenshot(alpha = 0x00), rect))
        assertEquals(marker(3, 5) and 0xFFFFFF, image.rgb(44 + 5 * 96 + 48, 44 + 3 * 96 + 48))
        assertEquals(0xFF, image.getRGB(400, 400) ushr 24)
        val jpeg = decode(BoardImageRenderer.screenJpeg(screenshot(alpha = 0x00), 600))
        assertTrue(colorDistance(BACKGROUND, jpeg.rgb(10, 10)) < 12, "background survives: %06x".format(jpeg.rgb(10, 10)))
    }

    @Test
    fun boardOutsideTheFrameIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            BoardImageRenderer.boardPng(screenshot(), BoardRect(5000, 5000, 400, 1f))
        }
    }

    @Test
    fun screenJpegIsScaledToTheLongEdge() {
        val bytes = BoardImageRenderer.screenJpeg(screenshot(), 1200)
        save("screen.jpg", bytes)
        assertEquals(listOf(0xFF, 0xD8), bytes.take(2).map { it.toInt() and 0xFF })
        val image = decode(bytes)
        assertEquals(540, image.width)
        assertEquals(1200, image.height)
        // Half scale: the centre of view cell (2, 6) of the board, and a point of the background.
        val x = (rect.left + 6 * 120 + 60) / 2
        val y = (rect.top + 2 * 120 + 60) / 2
        assertTrue(colorDistance(marker(2, 6), image.rgb(x, y)) < 24, "marker colour: %06x".format(image.rgb(x, y)))
        assertTrue(colorDistance(BACKGROUND, image.rgb(270, 100)) < 12)
    }

    @Test
    fun screenJpegNeverUpscalesAndHonoursQuality() {
        val small = Frame(300, 200, IntArray(300 * 200) { if ((it % 300 / 10 + it / 300 / 10) % 2 == 0) LIGHT_SQUARE else DARK_SQUARE })
        val high = BoardImageRenderer.screenJpeg(small, 1568, quality = 95)
        val low = BoardImageRenderer.screenJpeg(small, 1568, quality = 20)
        val image = decode(high)
        assertEquals(300, image.width)
        assertEquals(200, image.height)
        assertTrue(low.size < high.size, "quality 20 (${low.size} B) must be smaller than quality 95 (${high.size} B)")
        // Out-of-range qualities are clamped rather than rejected.
        assertEquals(300, decode(BoardImageRenderer.screenJpeg(small, 1568, quality = 500)).width)
        assertEquals(300, decode(BoardImageRenderer.screenJpeg(small, 1568, quality = -3)).width)
    }

    private fun colorDistance(a: Int, b: Int): Int =
        maxOf(abs(((a shr 16) and 0xFF) - ((b shr 16) and 0xFF)), abs(((a shr 8) and 0xFF) - ((b shr 8) and 0xFF)), abs((a and 0xFF) - (b and 0xFF)))

    private companion object {
        const val BACKGROUND = 0xFF303030.toInt()
        const val LIGHT_SQUARE = 0xFFEEEED2.toInt()
        const val DARK_SQUARE = 0xFF769656.toInt()
    }
}
