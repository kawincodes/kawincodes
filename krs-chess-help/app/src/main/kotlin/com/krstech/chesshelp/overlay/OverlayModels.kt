package com.krstech.chesshelp.overlay

/**
 * A position as seen on screen: [rows] are the 8 view rows top to bottom, 8 chars each, FEN letters
 * (`PNBRQK` white, `pnbrqk` black) and `.` for an empty square. The optional move is in view cells.
 */
data class MiniBoard(
    val rows: List<String>,
    val fromRow: Int?,
    val fromCol: Int?,
    val toRow: Int?,
    val toCol: Int?,
) {
    /** The FEN letter at a view cell; '.' for empty, out-of-range or malformed input. */
    fun pieceAt(row: Int, col: Int): Char = rows.getOrNull(row)?.getOrNull(col) ?: EMPTY

    /** True when a complete, in-range move (to a different cell) is attached. */
    val hasMove: Boolean
        get() {
            val fr = fromRow ?: return false
            val fc = fromCol ?: return false
            val tr = toRow ?: return false
            val tc = toCol ?: return false
            return fr in 0..7 && fc in 0..7 && tr in 0..7 && tc in 0..7 && (fr != tr || fc != tc)
        }

    companion object {
        const val EMPTY = '.'
    }
}

sealed interface OverlayState {
    data class Idle(val hint: String) : OverlayState

    data class Working(val message: String, val board: MiniBoard? = null) : OverlayState

    data class Advice(
        /** e.g. "White to move". */
        val title: String,
        val moveSan: String,
        val evaluation: String,
        val explanation: String,
        /** SAN to short note. */
        val alternatives: List<Pair<String, String>>,
        val threat: String?,
        /** e.g. "Claude Opus 5.5" or "Local engine". */
        val source: String,
        val board: MiniBoard?,
        val footer: String?,
    ) : OverlayState

    data class Message(
        val title: String,
        val body: String,
        val isError: Boolean,
        val board: MiniBoard? = null,
    ) : OverlayState
}

/** A move to draw over the real board: the squares area in screen pixels plus the move in view cells. */
data class BoardArrow(
    val boardLeft: Int,
    val boardTop: Int,
    val boardSize: Int,
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
) {
    val isDrawable: Boolean
        get() = boardSize >= 8 && fromRow in 0..7 && fromCol in 0..7 && toRow in 0..7 && toCol in 0..7 &&
            (fromRow != toRow || fromCol != toCol)
}
