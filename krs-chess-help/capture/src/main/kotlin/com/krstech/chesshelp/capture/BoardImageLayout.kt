package com.krstech.chesshelp.capture

/**
 * Geometry of the board image sent to Claude: a dark margin on the left and on the top, the board crop in
 * the bottom-right corner, `r1..r8` down the left margin and `c1..c8` along the top one. The labels name
 * view rows and columns (not ranks and files), which is what the Claude prompt refers to.
 */
internal object BoardImageLayout {

    const val BOARD_SIZE = 768
    const val MARGIN = 44
    const val IMAGE_SIZE = MARGIN + BOARD_SIZE
    const val CELL_SIZE = BOARD_SIZE / 8
    const val LABEL_TEXT_SIZE = 26f

    /** Where the board's top-left pixel goes, on both axes. */
    const val BOARD_ORIGIN = MARGIN

    class Label(val text: String, val centerX: Float, val centerY: Float)

    /** `r1` (top row) to `r8` (bottom row), each centred in the left margin beside its row. */
    fun rowLabels(): List<Label> =
        List(8) { row -> Label("r${row + 1}", MARGIN / 2f, cellCenter(row)) }

    /** `c1` (left column) to `c8` (right column), each centred in the top margin above its column. */
    fun columnLabels(): List<Label> =
        List(8) { col -> Label("c${col + 1}", cellCenter(col), MARGIN / 2f) }

    /**
     * Baseline that puts a line of text vertically centred on [centerY], given the font's [ascent]
     * (negative, above the baseline) and [descent] (positive, below it).
     */
    fun baseline(centerY: Float, ascent: Float, descent: Float): Float = centerY - (ascent + descent) / 2f

    private fun cellCenter(index: Int): Float = BOARD_ORIGIN + (index + 0.5f) * CELL_SIZE
}
