package com.krstech.chesshelp.overlay

import com.krstech.chesshelp.settings.FractionPoint
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** The part of the screen floating windows may occupy (system bars and cutouts already removed). */
internal data class Area(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
}

internal data class IntPoint(val x: Int, val y: Int)

/** Framework-free placement maths for the floating windows. All values are pixels. */
internal object OverlayGeometry {

    /** Largest window alpha that is still safely below the system's pass-through limit. */
    private const val MAX_ARROW_ALPHA = 0.8f

    /** Kept below the limit because the compositor stores alpha with less precision than a float. */
    private const val ARROW_ALPHA_MARGIN = 0.02f

    private const val PANEL_MAX_SCREEN_FRACTION = 0.6f

    /** The screen minus the given insets; never inverted even for nonsensical input. */
    fun insetArea(screenWidth: Int, screenHeight: Int, left: Int, top: Int, right: Int, bottom: Int): Area {
        val l = left.coerceIn(0, max(0, screenWidth))
        val t = top.coerceIn(0, max(0, screenHeight))
        val r = (screenWidth - max(0, right)).coerceAtLeast(l)
        val b = (screenHeight - max(0, bottom)).coerceAtLeast(t)
        return Area(l, t, r, b)
    }

    /** Moves a window fully inside [area]; one that is larger than the area is pinned to its left/top. */
    fun clamp(x: Int, y: Int, width: Int, height: Int, area: Area): IntPoint = IntPoint(
        x.coerceIn(area.left, max(area.left, area.right - width)),
        y.coerceIn(area.top, max(area.top, area.bottom - height)),
    )

    fun fromFraction(fraction: FractionPoint, width: Int, height: Int, area: Area): IntPoint {
        val travelX = max(0, area.width - width)
        val travelY = max(0, area.height - height)
        return IntPoint(
            area.left + (finite(fraction.x) * travelX).roundToInt(),
            area.top + (finite(fraction.y) * travelY).roundToInt(),
        )
    }

    fun toFraction(x: Int, y: Int, width: Int, height: Int, area: Area): FractionPoint {
        val travelX = area.width - width
        val travelY = area.height - height
        return FractionPoint(
            if (travelX <= 0) 0f else ((x - area.left).toFloat() / travelX).coerceIn(0f, 1f),
            if (travelY <= 0) 0f else ((y - area.top).toFloat() / travelY).coerceIn(0f, 1f),
        )
    }

    private fun finite(value: Float): Float = if (value.isNaN()) 0f else value.coerceIn(0f, 1f)

    /** True once a touch has travelled far enough to be a drag rather than a tap. */
    fun isDrag(dx: Float, dy: Float, touchSlop: Int): Boolean = hypot(dx, dy) > touchSlop

    /** At most [maxWidth], leaving [margin] free on both sides of a narrow screen. */
    fun panelWidth(areaWidth: Int, maxWidth: Int, margin: Int): Int =
        min(maxWidth, areaWidth - 2 * margin).coerceAtLeast(1)

    /** The panel may use at most 60% of the screen height, and never more than the usable area. */
    fun panelMaxHeight(screenHeight: Int, areaHeight: Int): Int =
        min((screenHeight * PANEL_MAX_SCREEN_FRACTION).toInt(), areaHeight).coerceAtLeast(1)

    /**
     * Where the panel goes while it still follows the bubble: beside the bubble when the screen is wide
     * enough, otherwise below it (or above when there is no room below), always inside [area].
     */
    fun panelNear(
        bubbleX: Int,
        bubbleY: Int,
        bubbleSize: Int,
        panelWidth: Int,
        panelHeight: Int,
        area: Area,
        gap: Int,
    ): IntPoint {
        val bubbleOnRight = bubbleX + bubbleSize / 2 >= (area.left + area.right) / 2
        val roomLeft = bubbleX - gap - area.left
        val roomRight = area.right - (bubbleX + bubbleSize + gap)
        val x: Int
        val y: Int
        if (bubbleOnRight && roomLeft >= panelWidth) {
            x = bubbleX - gap - panelWidth
            y = bubbleY
        } else if (!bubbleOnRight && roomRight >= panelWidth) {
            x = bubbleX + bubbleSize + gap
            y = bubbleY
        } else {
            x = if (bubbleOnRight) bubbleX + bubbleSize - panelWidth else bubbleX
            val below = bubbleY + bubbleSize + gap
            val above = bubbleY - gap - panelHeight
            val roomBelow = area.bottom - below
            val roomAbove = bubbleY - gap - area.top
            y = when {
                roomBelow >= panelHeight -> below
                roomAbove >= panelHeight -> above
                roomBelow >= roomAbove -> below
                else -> above
            }
        }
        return clamp(x, y, panelWidth, panelHeight, area)
    }

    /**
     * Window alpha for the pass-through arrow window. Since Android 12 a touch only passes through
     * another app's overlay when the overlay's opacity is at most
     * `InputManager.getMaximumObscuringOpacityForTouch()` (0.8 unless the device changed it).
     */
    fun arrowWindowAlpha(maximumObscuringOpacity: Float): Float {
        val limit = if (maximumObscuringOpacity.isNaN()) MAX_ARROW_ALPHA else maximumObscuringOpacity
        return (min(limit, MAX_ARROW_ALPHA) - ARROW_ALPHA_MARGIN).coerceIn(0f, 1f)
    }
}

/** The outline of a move arrow, shared by the mini board and the on-screen board arrow. */
internal object ArrowGeometry {
    const val POINT_COUNT = 7

    // Proportions relative to one board cell.
    private const val START_INSET = 0.28f
    private const val TIP_OVERSHOOT = 0.14f
    private const val HEAD_LENGTH = 0.44f
    private const val HEAD_HALF_WIDTH = 0.27f
    private const val SHAFT_HALF_WIDTH = 0.095f

    fun cellCenter(origin: Float, cell: Float, index: Int): Float = origin + (index + 0.5f) * cell

    /**
     * Seven polygon points as x0,y0,x1,y1…: shaft start (left), head base (left), head barb (left), tip,
     * head barb (right), head base (right), shaft start (right). The arrow starts a little outside the
     * centre of the from-cell and its tip lands just past the centre of the to-cell.
     * Returns null when both centres coincide or [cell] is not positive.
     */
    fun polygon(fromX: Float, fromY: Float, toX: Float, toY: Float, cell: Float): FloatArray? {
        val dx = toX - fromX
        val dy = toY - fromY
        val length = hypot(dx, dy)
        if (cell <= 0f || length < 1e-3f) return null
        val ux = dx / length
        val uy = dy / length
        val nx = -uy
        val ny = ux
        val startX = fromX + ux * START_INSET * cell
        val startY = fromY + uy * START_INSET * cell
        val tipX = toX + ux * TIP_OVERSHOOT * cell
        val tipY = toY + uy * TIP_OVERSHOOT * cell
        val baseX = tipX - ux * HEAD_LENGTH * cell
        val baseY = tipY - uy * HEAD_LENGTH * cell
        val shaft = SHAFT_HALF_WIDTH * cell
        val head = HEAD_HALF_WIDTH * cell
        return floatArrayOf(
            startX + nx * shaft, startY + ny * shaft,
            baseX + nx * shaft, baseY + ny * shaft,
            baseX + nx * head, baseY + ny * head,
            tipX, tipY,
            baseX - nx * head, baseY - ny * head,
            baseX - nx * shaft, baseY - ny * shaft,
            startX - nx * shaft, startY - ny * shaft,
        )
    }
}

/** Maps FEN letters to the glyphs the mini board draws. */
internal object PieceGlyphs {
    // U+FE0E asks for the text (not emoji) presentation; without it some devices draw the pawn as an emoji.
    private const val TEXT_PRESENTATION = "︎"

    /**
     * The solid chess glyph for a FEN letter of either colour (colour is applied when drawing: a filled
     * glyph plus a contrasting outline reads on light and dark squares alike). Null for anything else.
     */
    fun glyph(fenChar: Char): String? = when (fenChar.uppercaseChar()) {
        'K' -> "♚"
        'Q' -> "♛"
        'R' -> "♜"
        'B' -> "♝"
        'N' -> "♞"
        'P' -> "♟"
        else -> null
    }?.plus(TEXT_PRESENTATION)

    /** Upper-case piece letter, used when the device font has no chess glyphs. */
    fun letter(fenChar: Char): String? = if (glyph(fenChar) != null) fenChar.uppercaseChar().toString() else null

    fun isPiece(fenChar: Char): Boolean = glyph(fenChar) != null

    /** FEN convention: upper case = white. */
    fun isWhite(fenChar: Char): Boolean = fenChar.isUpperCase()
}

/** How the evaluation chip is tinted. */
internal enum class EvalTone {
    GOOD, NEUTRAL, BAD;

    companion object {
        fun of(evaluation: String): EvalTone {
            val text = evaluation.trim().lowercase()
            return when {
                text.startsWith("winning") || text.startsWith("better") -> GOOD
                text.startsWith("losing") || text.startsWith("worse") -> BAD
                else -> NEUTRAL
            }
        }
    }
}
