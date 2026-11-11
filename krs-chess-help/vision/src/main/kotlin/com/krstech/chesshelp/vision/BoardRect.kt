package com.krstech.chesshelp.vision

/**
 * The squares area of an on-screen board in frame pixels: `[left, left + size) x [top, top + size)`.
 * Borders and coordinate margins around the squares are not part of it.
 */
data class BoardRect(val left: Int, val top: Int, val size: Int, val confidence: Float) {

    /** Side of one square in pixels (may be fractional). */
    val cell: Float get() = size / 8f

    /** Exclusive right edge. */
    val right: Int get() = left + size

    /** Exclusive bottom edge. */
    val bottom: Int get() = top + size

    /** Left pixel of view column [col] (0..7); `cellLeft(8) == right`. */
    fun cellLeft(col: Int): Int = left + (col * size + 4) / 8

    /** Top pixel of view row [row] (0..7); `cellTop(8) == bottom`. */
    fun cellTop(row: Int): Int = top + (row * size + 4) / 8

    /** Exclusive right pixel of view column [col]. */
    fun cellRight(col: Int): Int = cellLeft(col + 1)

    /** Exclusive bottom pixel of view row [row]. */
    fun cellBottom(row: Int): Int = cellTop(row + 1)

    fun cellCenterX(col: Int): Float = left + (col + 0.5f) * cell

    fun cellCenterY(row: Int): Float = top + (row + 0.5f) * cell

    fun contains(x: Int, y: Int): Boolean = x >= left && x < right && y >= top && y < bottom

    /** View column (0..7) containing frame x, or -1 when x is outside the board. */
    fun colAt(x: Int): Int = if (x < left || x >= right || size <= 0) -1 else minOf(7, (x - left) * 8 / size)

    /** View row (0..7) containing frame y, or -1 when y is outside the board. */
    fun rowAt(y: Int): Int = if (y < top || y >= bottom || size <= 0) -1 else minOf(7, (y - top) * 8 / size)

    /** True when the whole rect lies inside a frame of the given dimensions. */
    fun fitsIn(frameWidth: Int, frameHeight: Int): Boolean =
        size >= 8 && left >= 0 && top >= 0 && right <= frameWidth && bottom <= frameHeight
}
