package com.krstech.chesshelp.capture

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.createBitmap
import com.krstech.chesshelp.vision.BoardRect
import com.krstech.chesshelp.vision.Frame
import com.krstech.chesshelp.vision.FrameOps
import java.io.ByteArrayOutputStream

/** Builds the images sent to Claude. Safe to call from any thread; every bitmap is recycled before returning. */
object BoardImageRenderer {

    /**
     * The board's squares area resampled to 768 px and framed by a margin that names the view rows `r1..r8`
     * (top to bottom, on the left) and the view columns `c1..c8` (left to right, on top). PNG, opaque.
     *
     * @throws IllegalArgumentException when [rect] lies completely outside [frame].
     */
    fun boardPng(frame: Frame, rect: BoardRect): ByteArray {
        val board = FrameOps.cropBoard(frame, rect, BoardImageLayout.BOARD_SIZE)
        val bitmap = createBitmap(BoardImageLayout.IMAGE_SIZE, BoardImageLayout.IMAGE_SIZE) // ARGB_8888, mutable
        try {
            val canvas = Canvas(bitmap)
            canvas.drawColor(MARGIN_COLOR)
            bitmap.setPixels(
                opaque(board.pixels), 0, board.width,
                BoardImageLayout.BOARD_ORIGIN, BoardImageLayout.BOARD_ORIGIN, board.width, board.height,
            )
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = LABEL_COLOR
                textSize = BoardImageLayout.LABEL_TEXT_SIZE
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            val metrics = paint.fontMetrics
            for (label in BoardImageLayout.rowLabels() + BoardImageLayout.columnLabels()) {
                val baseline = BoardImageLayout.baseline(label.centerY, metrics.ascent, metrics.descent)
                canvas.drawText(label.text, label.centerX, baseline, paint)
            }
            bitmap.setHasAlpha(false)
            return encode(bitmap, Bitmap.CompressFormat.PNG, 100)
        } finally {
            bitmap.recycle()
        }
    }

    /** The whole [frame] scaled down so that its long edge is at most [maxLongEdge] px, as JPEG. */
    fun screenJpeg(frame: Frame, maxLongEdge: Int, quality: Int = 80): ByteArray {
        val scaled = FrameOps.downscale(frame, maxLongEdge)
        val bitmap = Bitmap.createBitmap(opaque(scaled.pixels), scaled.width, scaled.height, Bitmap.Config.ARGB_8888)
        try {
            return encode(bitmap, Bitmap.CompressFormat.JPEG, quality.coerceIn(1, 100))
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * A copy with every pixel fully opaque. A screen has no transparency, but a capture whose alpha bytes
     * are not 255 would otherwise come out see-through (PNG) or darkened (premultiplied bitmap).
     */
    private fun opaque(pixels: IntArray): IntArray = IntArray(pixels.size) { pixels[it] or OPAQUE }

    private fun encode(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int): ByteArray {
        val out = ByteArrayOutputStream(INITIAL_BUFFER_BYTES)
        check(bitmap.compress(format, quality, out)) { "Could not encode the image as $format" }
        return out.toByteArray()
    }

    private const val OPAQUE = 0xFF000000.toInt()
    private const val MARGIN_COLOR = 0xFF17181C.toInt()
    private const val LABEL_COLOR = 0xFFFFFFFF.toInt()
    private const val INITIAL_BUFFER_BYTES = 256 * 1024
}
