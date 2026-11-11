package com.krstech.chesshelp.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import androidx.core.content.ContextCompat
import com.krstech.chesshelp.R
import kotlin.math.min

/** Replaces the path with the closed polygon x0,y0,x1,y1… */
internal fun Path.setPolygon(points: FloatArray) {
    rewind()
    if (points.size < 6) return
    moveTo(points[0], points[1])
    var i = 2
    while (i + 1 < points.size) {
        lineTo(points[i], points[i + 1])
        i += 2
    }
    close()
}

/** Draws a [MiniBoard]: squares, pieces, the highlighted from/to cells and the move arrow. */
internal class MiniBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var board: MiniBoard? = null
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    private val lightPaint = fillPaint(R.color.board_light)
    private val darkPaint = fillPaint(R.color.board_dark)
    private val highlightPaint = fillPaint(R.color.board_move_highlight)
    private val arrowFillPaint = fillPaint(R.color.arrow_fill).apply { alpha = ARROW_ALPHA }
    private val arrowOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        color = ContextCompat.getColor(context, R.color.arrow_outline)
        alpha = ARROW_ALPHA
    }
    private val pieceFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val pieceOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
    }
    private val whiteFill = ContextCompat.getColor(context, R.color.piece_white_fill)
    private val whiteOutline = ContextCompat.getColor(context, R.color.piece_white_outline)
    private val blackFill = ContextCompat.getColor(context, R.color.piece_black_fill)
    private val blackOutline = ContextCompat.getColor(context, R.color.piece_black_outline)

    /** False on the rare device whose fonts lack the chess symbols; letters are drawn instead. */
    private val hasChessGlyphs: Boolean = pieceFillPaint.hasGlyph("♚")

    private val arrowPath = Path()
    private val textBounds = Rect()

    /** Text -> (x, y) offset from the cell centre to the drawText origin, for the current size. */
    private val glyphOffsets = HashMap<String, FloatArray>()

    init {
        val radius = resources.displayMetrics.density * CORNER_RADIUS_DP
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }
        clipToOutline = true
    }

    private fun fillPaint(colorRes: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, colorRes)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desired = (resources.displayMetrics.density * DEFAULT_SIZE_DP).toInt()
        val size = min(resolveSize(desired, widthMeasureSpec), resolveSize(desired, heightMeasureSpec))
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        glyphOffsets.clear()
        val cell = min(w, h) / 8f
        val textSize = cell * (if (hasChessGlyphs) GLYPH_SIZE else LETTER_SIZE)
        pieceFillPaint.textSize = textSize
        pieceOutlinePaint.textSize = textSize
        pieceOutlinePaint.strokeWidth = cell * PIECE_OUTLINE
        arrowOutlinePaint.strokeWidth = cell * ARROW_OUTLINE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cell = min(width, height) / 8f
        if (cell <= 0f) return
        for (row in 0..7) {
            for (col in 0..7) {
                val paint = if ((row + col) % 2 == 0) lightPaint else darkPaint
                canvas.drawRect(col * cell, row * cell, (col + 1) * cell, (row + 1) * cell, paint)
            }
        }
        val current = board ?: return
        val move = current.hasMove
        if (move) {
            highlightCell(canvas, current.fromRow!!, current.fromCol!!, cell)
            highlightCell(canvas, current.toRow!!, current.toCol!!, cell)
        }
        for (row in 0..7) {
            for (col in 0..7) {
                drawPiece(canvas, current.pieceAt(row, col), (col + 0.5f) * cell, (row + 0.5f) * cell)
            }
        }
        if (move) {
            val points = ArrowGeometry.polygon(
                ArrowGeometry.cellCenter(0f, cell, current.fromCol!!),
                ArrowGeometry.cellCenter(0f, cell, current.fromRow!!),
                ArrowGeometry.cellCenter(0f, cell, current.toCol!!),
                ArrowGeometry.cellCenter(0f, cell, current.toRow!!),
                cell,
            ) ?: return
            arrowPath.setPolygon(points)
            canvas.drawPath(arrowPath, arrowFillPaint)
            canvas.drawPath(arrowPath, arrowOutlinePaint)
        }
    }

    private fun highlightCell(canvas: Canvas, row: Int, col: Int, cell: Float) {
        canvas.drawRect(col * cell, row * cell, (col + 1) * cell, (row + 1) * cell, highlightPaint)
    }

    private fun drawPiece(canvas: Canvas, fenChar: Char, centerX: Float, centerY: Float) {
        val text = (if (hasChessGlyphs) PieceGlyphs.glyph(fenChar) else PieceGlyphs.letter(fenChar)) ?: return
        val white = PieceGlyphs.isWhite(fenChar)
        pieceFillPaint.color = if (white) whiteFill else blackFill
        pieceOutlinePaint.color = if (white) whiteOutline else blackOutline
        val offset = glyphOffsets.getOrPut(text) {
            // Centre on the glyph's own ink box: the symbols come from a fallback font whose metrics
            // differ from the default typeface, so font metrics would place them off-centre.
            pieceFillPaint.getTextBounds(text, 0, text.length, textBounds)
            floatArrayOf(-textBounds.exactCenterX(), -textBounds.exactCenterY())
        }
        val x = centerX + offset[0]
        val y = centerY + offset[1]
        canvas.drawText(text, x, y, pieceOutlinePaint)
        canvas.drawText(text, x, y, pieceFillPaint)
    }

    private companion object {
        const val DEFAULT_SIZE_DP = 132
        const val CORNER_RADIUS_DP = 6
        const val GLYPH_SIZE = 0.82f
        const val LETTER_SIZE = 0.62f
        const val PIECE_OUTLINE = 0.09f
        const val ARROW_OUTLINE = 0.04f
        const val ARROW_ALPHA = 225
    }
}
