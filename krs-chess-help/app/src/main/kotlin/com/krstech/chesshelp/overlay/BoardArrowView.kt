package com.krstech.chesshelp.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import androidx.core.content.ContextCompat
import com.krstech.chesshelp.R

/**
 * Content of the full-screen pass-through window: the recommended move drawn over the real board.
 * [BoardArrow] is in screen pixels, so drawing is shifted by this view's own position on screen and
 * stays correct wherever the system actually placed the window.
 */
internal class BoardArrowView(context: Context) : View(context) {

    var arrow: BoardArrow? = null
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.arrow_fill)
    }
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        color = ContextCompat.getColor(context, R.color.arrow_outline)
    }
    private val targetFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.arrow_target_fill)
    }
    private val targetStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        color = ContextCompat.getColor(context, R.color.arrow_fill)
    }
    private val path = Path()
    private val location = IntArray(2)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val current = arrow ?: return
        if (!current.isDrawable) return
        val cell = current.boardSize / 8f
        getLocationOnScreen(location)
        val save = canvas.save()
        canvas.translate(-location[0].toFloat(), -location[1].toFloat())

        val frame = cell * TARGET_STROKE
        targetStrokePaint.strokeWidth = frame
        val left = current.boardLeft + current.toCol * cell + frame / 2f
        val top = current.boardTop + current.toRow * cell + frame / 2f
        val side = cell - frame
        val radius = cell * TARGET_RADIUS
        canvas.drawRoundRect(left, top, left + side, top + side, radius, radius, targetFillPaint)
        canvas.drawRoundRect(left, top, left + side, top + side, radius, radius, targetStrokePaint)

        val points = ArrowGeometry.polygon(
            ArrowGeometry.cellCenter(current.boardLeft.toFloat(), cell, current.fromCol),
            ArrowGeometry.cellCenter(current.boardTop.toFloat(), cell, current.fromRow),
            ArrowGeometry.cellCenter(current.boardLeft.toFloat(), cell, current.toCol),
            ArrowGeometry.cellCenter(current.boardTop.toFloat(), cell, current.toRow),
            cell,
        )
        if (points != null) {
            path.setPolygon(points)
            outlinePaint.strokeWidth = cell * ARROW_OUTLINE
            canvas.drawPath(path, fillPaint)
            canvas.drawPath(path, outlinePaint)
        }
        canvas.restoreToCount(save)
    }

    private companion object {
        const val TARGET_STROKE = 0.07f
        const val TARGET_RADIUS = 0.12f
        const val ARROW_OUTLINE = 0.03f
    }
}
