package com.krstech.chesshelp.chess

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BoardViewTest {
    private val startRowsWhiteAtBottom = listOf(
        "rnbqkbnr", "pppppppp", "........", "........", "........", "........", "PPPPPPPP", "RNBQKBNR",
    )
    private val startRowsBlackAtBottom = listOf(
        "RNBKQBNR", "PPPPPPPP", "........", "........", "........", "........", "pppppppp", "rnbkqbnr",
    )

    @Test
    fun cornersFollowTheOrientationConvention() {
        // White at bottom: top-left is a8, bottom-left a1, bottom-right h1.
        assertEquals(Square.parse("a8"), BoardView.squareOf(0, 0, true))
        assertEquals(Square.parse("h8"), BoardView.squareOf(0, 7, true))
        assertEquals(Square.parse("a1"), BoardView.squareOf(7, 0, true))
        assertEquals(Square.parse("h1"), BoardView.squareOf(7, 7, true))
        assertEquals(Square.parse("e4"), BoardView.squareOf(4, 4, true))
        // Black at bottom: top-left is h1, bottom-left h8, bottom-right a8.
        assertEquals(Square.parse("h1"), BoardView.squareOf(0, 0, false))
        assertEquals(Square.parse("a1"), BoardView.squareOf(0, 7, false))
        assertEquals(Square.parse("h8"), BoardView.squareOf(7, 0, false))
        assertEquals(Square.parse("a8"), BoardView.squareOf(7, 7, false))
        assertEquals(Square.parse("e4"), BoardView.squareOf(3, 3, false))
    }

    @Test
    fun squareRowAndColumnAreConsistent() {
        for (whiteAtBottom in listOf(true, false)) {
            val seen = HashSet<Int>()
            for (row in 0..7) {
                for (col in 0..7) {
                    val sq = BoardView.squareOf(row, col, whiteAtBottom)
                    assertTrue(seen.add(sq))
                    assertEquals(row, BoardView.rowOf(sq, whiteAtBottom))
                    assertEquals(col, BoardView.colOf(sq, whiteAtBottom))
                    // The convention of the design: file = col, rank = 7 - row (or mirrored).
                    if (whiteAtBottom) {
                        assertEquals(col, Square.file(sq))
                        assertEquals(7 - row, Square.rank(sq))
                    } else {
                        assertEquals(7 - col, Square.file(sq))
                        assertEquals(row, Square.rank(sq))
                    }
                }
            }
            assertEquals(64, seen.size)
            for (sq in 0..63) {
                assertEquals(sq, BoardView.squareOf(BoardView.rowOf(sq, whiteAtBottom), BoardView.colOf(sq, whiteAtBottom), whiteAtBottom))
            }
        }
        // Flipping the board is a 180 degree rotation.
        for (sq in 0..63) {
            assertEquals(7 - BoardView.rowOf(sq, true), BoardView.rowOf(sq, false))
            assertEquals(7 - BoardView.colOf(sq, true), BoardView.colOf(sq, false))
        }
    }

    @Test
    fun toRowsInBothOrientations() {
        assertEquals(startRowsWhiteAtBottom, BoardView.toRows(Position.START, true))
        assertEquals(startRowsBlackAtBottom, BoardView.toRows(Position.START, false))
        val position = Position.fromFen("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1")
        assertEquals(
            listOf("........", "..p.....", "...p....", "KP.....r", ".R...p.k", "........", "....P.P.", "........"),
            BoardView.toRows(position, true),
        )
        assertEquals(
            listOf("........", ".P.P....", "........", "k.p...R.", "r.....PK", "....p...", ".....p..", "........"),
            BoardView.toRows(position, false),
        )
    }

    @Test
    fun parseRowsInBothOrientations() {
        val white = BoardView.parseRows(startRowsWhiteAtBottom, true)
        val black = BoardView.parseRows(startRowsBlackAtBottom, false)
        assertContentEquals(white, black)
        assertEquals(Position.START, Position.fromBoard(white, Side.WHITE))
        assertEquals(Piece(Side.WHITE, PieceType.KING), white[Square.parse("e1")])
        assertEquals(Piece(Side.BLACK, PieceType.QUEEN), white[Square.parse("d8")])
        assertNull(white[Square.parse("e4")])
        // Reading the same rows with the wrong orientation gives a different (rotated) board.
        val wrong = BoardView.parseRows(startRowsWhiteAtBottom, false)
        assertEquals(Piece(Side.BLACK, PieceType.KING), wrong[Square.parse("d1")])
    }

    @Test
    fun rowsRoundTrip() {
        val fens = listOf(
            Position.START_FEN,
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
            "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
            "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
            "8/8/8/8/8/8/8/8 w - - 0 1",
        )
        for (fen in fens) {
            val position = Position.fromFen(fen)
            for (whiteAtBottom in listOf(true, false)) {
                val rows = BoardView.toRows(position, whiteAtBottom)
                assertEquals(8, rows.size)
                assertTrue(rows.all { it.length == 8 })
                val pieces = BoardView.parseRows(rows, whiteAtBottom)
                for (sq in 0..63) assertEquals(position.pieceAt(sq), pieces[sq], "${Square.name(sq)} of $fen")
                assertEquals(position.placement(), Position.fromBoard(pieces, position.sideToMove).placement())
                assertEquals(rows, BoardView.toRows(Position.fromBoard(pieces, Side.WHITE), whiteAtBottom))
            }
        }
    }

    @Test
    fun parseRowsToleratesOtherSpellingsOfEmptySquares() {
        val expected = BoardView.parseRows(startRowsWhiteAtBottom, true)
        val variants = listOf(
            listOf("rnbqkbnr", "pppppppp", "8", "8", "8", "8", "PPPPPPPP", "RNBQKBNR"),
            listOf("rnbqkbnr", "pppppppp", "        ", "--------", "________", "4....", "PPPPPPPP", "RNBQKBNR"),
        )
        for (rows in variants) assertContentEquals(expected, BoardView.parseRows(rows, true))
        val mixed = BoardView.parseRows(listOf("r3k2r", "8", "8", "8", "8", "8", "8", "R3K2R"), true)
        assertEquals("r3k2r/8/8/8/8/8/8/R3K2R", Position.fromBoard(mixed, Side.WHITE).placement())
    }

    @Test
    fun malformedRowsAreRejectedWithAClearMessage() {
        fun messageFor(rows: List<String>): String =
            assertFailsWith<IllegalArgumentException> { BoardView.parseRows(rows, true) }.message!!

        assertTrue(messageFor(startRowsWhiteAtBottom.drop(1)).contains("Expected 8 rows, got 7"))
        assertTrue(messageFor(startRowsWhiteAtBottom + "........").contains("got 9"))
        assertTrue(messageFor(emptyList()).contains("got 0"))

        val short = startRowsWhiteAtBottom.toMutableList().also { it[2] = "......." }
        assertTrue(messageFor(short).contains("Row 3"), messageFor(short))
        assertTrue(messageFor(short).contains("7 squares"), messageFor(short))

        val long = startRowsWhiteAtBottom.toMutableList().also { it[7] = "RNBQKBNRR" }
        assertTrue(messageFor(long).contains("Row 8"), messageFor(long))
        assertTrue(messageFor(long).contains("9 squares"), messageFor(long))

        val badChar = startRowsWhiteAtBottom.toMutableList().also { it[0] = "rnbqkbnx" }
        assertTrue(messageFor(badChar).contains("Row 1"), messageFor(badChar))
        assertTrue(messageFor(badChar).contains("'x'"), messageFor(badChar))

        val empty = startRowsWhiteAtBottom.toMutableList().also { it[4] = "" }
        assertTrue(messageFor(empty).contains("Row 5"), messageFor(empty))

        assertFailsWith<IllegalArgumentException> { BoardView.squareOf(8, 0, true) }
        assertFailsWith<IllegalArgumentException> { BoardView.rowOf(64, true) }
        assertFailsWith<IllegalArgumentException> { BoardView.colOf(-1, false) }
    }
}
