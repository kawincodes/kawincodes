package com.krstech.chesshelp.chess

/**
 * Mapping between the board as seen on screen (view grid: row 0 = top row of the image, col 0 =
 * left column) and square indices. With `whiteAtBottom` the top-left cell is a8, otherwise h1.
 */
object BoardView {
    fun squareOf(row: Int, col: Int, whiteAtBottom: Boolean): Int {
        require(row in 0..7 && col in 0..7) { "row and col must be 0..7 (got row=$row, col=$col)" }
        return if (whiteAtBottom) (7 - row) * 8 + col else row * 8 + (7 - col)
    }

    fun rowOf(sq: Int, whiteAtBottom: Boolean): Int {
        require(sq in 0..63) { "square index must be 0..63 (got $sq)" }
        return if (whiteAtBottom) 7 - (sq shr 3) else sq shr 3
    }

    fun colOf(sq: Int, whiteAtBottom: Boolean): Int {
        require(sq in 0..63) { "square index must be 0..63 (got $sq)" }
        return if (whiteAtBottom) sq and 7 else 7 - (sq and 7)
    }

    /**
     * Parses 8 rows of 8 cells (top view row first) into pieces indexed by square. Cells are FEN
     * letters (`PNBRQK` white, `pnbrqk` black) or `.` for an empty square; a space, `-`, `_` or a
     * FEN-style digit run (`3` = three empty squares) is tolerated as empty too.
     *
     * @throws IllegalArgumentException naming the offending row/column when the input is malformed.
     */
    fun parseRows(rows: List<String>, whiteAtBottom: Boolean): Array<Piece?> {
        require(rows.size == 8) { "Expected 8 rows, got ${rows.size}" }
        val pieces = arrayOfNulls<Piece>(64)
        for (row in 0 until 8) {
            val text = rows[row]
            var col = 0
            for (c in text) {
                when {
                    c == '.' || c == ' ' || c == '-' || c == '_' -> col++
                    c in '1'..'8' -> col += c - '0'
                    else -> {
                        val piece = Piece.fromFenChar(c)
                            ?: throw IllegalArgumentException(
                                "Row ${row + 1} (\"$text\"): unexpected character '$c' at position ${col + 1}; " +
                                    "use PNBRQK for white pieces, pnbrqk for black pieces and '.' for an empty square",
                            )
                        if (col < 8) pieces[squareOf(row, col, whiteAtBottom)] = piece
                        col++
                    }
                }
            }
            require(col == 8) { "Row ${row + 1} (\"$text\") describes $col squares, expected exactly 8" }
        }
        return pieces
    }

    /** The position as 8 strings of 8 chars, top view row first, `.` for empty squares. */
    fun toRows(position: Position, whiteAtBottom: Boolean): List<String> = List(8) { row ->
        String(CharArray(8) { col -> position.pieceAt(squareOf(row, col, whiteAtBottom))?.fenChar ?: '.' })
    }
}
