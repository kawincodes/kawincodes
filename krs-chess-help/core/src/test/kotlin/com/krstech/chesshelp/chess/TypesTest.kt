package com.krstech.chesshelp.chess

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class TypesTest {
    @Test
    fun sideBasics() {
        assertEquals(Side.BLACK, Side.WHITE.opposite)
        assertEquals(Side.WHITE, Side.BLACK.opposite)
        assertEquals('w', Side.WHITE.fenChar)
        assertEquals('b', Side.BLACK.fenChar)
    }

    @Test
    fun pieceFenChars() {
        assertEquals('N', Piece(Side.WHITE, PieceType.KNIGHT).fenChar)
        assertEquals('n', Piece(Side.BLACK, PieceType.KNIGHT).fenChar)
        for (c in "PNBRQKpnbrqk") {
            val piece = Piece.fromFenChar(c)!!
            assertEquals(c, piece.fenChar)
            assertEquals(c.isUpperCase(), piece.side == Side.WHITE)
        }
        assertEquals(Piece(Side.BLACK, PieceType.QUEEN), Piece.fromFenChar('q'))
        for (c in ".x1 -Zz") assertNull(Piece.fromFenChar(c), "'$c'")
    }

    @Test
    fun squareIndexConvention() {
        assertEquals(0, Square.parse("a1"))
        assertEquals(1, Square.parse("b1"))
        assertEquals(7, Square.parse("h1"))
        assertEquals(8, Square.parse("a2"))
        assertEquals(28, Square.parse("e4"))
        assertEquals(63, Square.parse("h8"))
        assertEquals(28, Square.of(4, 3))
        assertEquals(4, Square.file(28))
        assertEquals(3, Square.rank(28))
        assertEquals("e4", Square.name(28))
        for (sq in 0..63) {
            assertEquals(sq, Square.parse(Square.name(sq)))
            assertEquals(sq, Square.of(Square.file(sq), Square.rank(sq)))
        }
    }

    @Test
    fun squareParseRejectsGarbage() {
        for (text in listOf("", "e", "e9", "i1", "e0", "e44", "4e", " e4", "ee")) {
            assertEquals(-1, Square.parse(text), "\"$text\"")
        }
        assertFailsWith<IllegalArgumentException> { Square.name(64) }
        assertFailsWith<IllegalArgumentException> { Square.name(-1) }
        assertFailsWith<IllegalArgumentException> { Square.of(8, 0) }
    }

    @Test
    fun moveUci() {
        assertEquals("e2e4", Move(12, 28).uci())
        assertEquals("e7e8q", Move(52, 60, PieceType.QUEEN).uci())
        assertEquals("a2a1n", Move(8, 0, PieceType.KNIGHT).uci())
        assertEquals(Move(12, 28), Move.fromUci("e2e4"))
        assertEquals(Move(52, 60, PieceType.QUEEN), Move.fromUci("e7e8q"))
        assertEquals(Move(52, 60, PieceType.ROOK), Move.fromUci("E7E8R"))
        assertEquals(Move(6, 21), Move.fromUci(" g1f3 "))
        for (text in listOf("", "e2", "e2e", "e2e9", "e2e4x", "e7e8k", "e7e8p", "e2e2", "0000", "e2e4qq", "O-O")) {
            assertNull(Move.fromUci(text), "\"$text\"")
        }
    }
}
