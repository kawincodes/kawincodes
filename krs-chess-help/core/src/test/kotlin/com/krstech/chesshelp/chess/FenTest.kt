package com.krstech.chesshelp.chess

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FenTest {
    @Test
    fun startPosition() {
        val start = Position.START
        assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", start.toFen())
        assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR", start.placement())
        assertEquals(Side.WHITE, start.sideToMove)
        assertEquals(-1, start.enPassantSquare)
        assertEquals(0, start.halfmoveClock)
        assertEquals(1, start.fullmoveNumber)
        assertEquals(Piece(Side.WHITE, PieceType.ROOK), start.pieceAt(Square.parse("a1")))
        assertEquals(Piece(Side.WHITE, PieceType.KING), start.pieceAt(Square.parse("e1")))
        assertEquals(Piece(Side.WHITE, PieceType.QUEEN), start.pieceAt(Square.parse("d1")))
        assertEquals(Piece(Side.BLACK, PieceType.KING), start.pieceAt(Square.parse("e8")))
        assertEquals(Piece(Side.BLACK, PieceType.PAWN), start.pieceAt(Square.parse("h7")))
        assertNull(start.pieceAt(Square.parse("e4")))
        for (side in Side.entries) {
            assertTrue(start.canCastle(side, kingSide = true))
            assertTrue(start.canCastle(side, kingSide = false))
        }
        assertFailsWith<IllegalArgumentException> { start.pieceAt(64) }
    }

    @Test
    fun roundTrips() {
        val fens = listOf(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
            "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2",
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
            "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
            "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
            "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8",
            "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10",
            "4k3/8/8/8/8/8/8/R3K3 w Q - 37 91",
            "4k2r/8/8/8/8/8/8/4K3 b k - 99 150",
            "8/8/8/8/8/8/8/8 w - - 0 1",
        )
        for (fen in fens) {
            val position = Position.fromFen(fen)
            assertEquals(fen, position.toFen())
            assertEquals(fen, position.toString())
            assertEquals(fen.substringBefore(' '), position.placement())
            assertEquals(position, Position.fromFen(position.toFen()))
        }
    }

    @Test
    fun fieldsAreExposed() {
        val position = Position.fromFen("rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w Kq c6 4 2")
        assertEquals(Side.WHITE, position.sideToMove)
        assertEquals(Square.parse("c6"), position.enPassantSquare)
        assertEquals(4, position.halfmoveClock)
        assertEquals(2, position.fullmoveNumber)
        assertTrue(position.canCastle(Side.WHITE, kingSide = true))
        assertFalse(position.canCastle(Side.WHITE, kingSide = false))
        assertFalse(position.canCastle(Side.BLACK, kingSide = true))
        assertTrue(position.canCastle(Side.BLACK, kingSide = false))
    }

    @Test
    fun barePlacementIsAccepted() {
        assertEquals(Position.START, Position.fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"))
        assertEquals(Position.START, Position.fromFen("  rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR  \n"))
        // Castling rights are inferred from king and rook home squares when the field is missing.
        assertEquals(
            "r3k3/8/8/8/8/8/8/4K2R w Kq - 0 1",
            Position.fromFen("r3k3/8/8/8/8/8/8/4K2R").toFen(),
        )
    }

    @Test
    fun missingTrailingFieldsGetDefaults() {
        val placement = "r3k2r/8/8/8/8/8/8/R3K2R"
        assertEquals("$placement b KQkq - 0 1", Position.fromFen("$placement b").toFen())
        assertEquals("$placement b Kq - 0 1", Position.fromFen("$placement b Kq").toFen())
        assertEquals("$placement w - - 0 1", Position.fromFen("$placement w -").toFen())
        assertEquals("$placement w KQkq - 0 1", Position.fromFen("$placement w KQkq -").toFen())
        assertEquals("$placement w KQkq - 7 1", Position.fromFen("$placement w KQkq - 7").toFen())
        assertEquals("$placement w KQkq - 7 30", Position.fromFen("$placement   w  KQkq\t-  7  30").toFen())
    }

    @Test
    fun castlingRightsWithoutKingAndRookAtHomeAreDropped() {
        assertEquals("4k3/8/8/8/8/8/8/4K3 w - - 0 1", Position.fromFen("4k3/8/8/8/8/8/8/4K3 w KQkq - 0 1").toFen())
        assertEquals("r3k2r/8/8/8/8/8/8/R4K1R w kq - 0 1", Position.fromFen("r3k2r/8/8/8/8/8/8/R4K1R w KQkq - 0 1").toFen())
        assertEquals("4k2r/8/8/8/8/8/8/R3K3 w Qk - 0 1", Position.fromFen("4k2r/8/8/8/8/8/8/R3K3 w KQkq - 0 1").toFen())
        // A black rook on a1 does not give White the right to castle.
        assertEquals("4k3/8/8/8/8/8/8/r3K3 w - - 0 1", Position.fromFen("4k3/8/8/8/8/8/8/r3K3 w Q - 0 1").toFen())
    }

    @Test
    fun implausibleEnPassantSquareIsDropped() {
        // No pawn that could have just double-pushed.
        assertEquals(-1, Position.fromFen("4k3/8/8/8/8/8/8/4K3 w - e6 0 1").enPassantSquare)
        // Wrong rank for the side to move (e3 would be a white double push, but White is to move).
        assertEquals(-1, Position.fromFen("4k3/8/8/8/4P3/8/8/4K3 w - e3 0 1").enPassantSquare)
        // Origin square of the double push is still occupied.
        assertEquals(-1, Position.fromFen("4k3/4p3/8/4p3/8/8/8/4K3 w - e6 0 1").enPassantSquare)
        // The plausible ones are kept, whether or not a capture is actually possible.
        assertEquals(Square.parse("e6"), Position.fromFen("4k3/8/8/4p3/8/8/8/4K3 w - e6 0 1").enPassantSquare)
        assertEquals(Square.parse("e3"), Position.fromFen("4k3/8/8/8/4P3/8/8/4K3 b - e3 0 1").enPassantSquare)
    }

    @Test
    fun garbageIsRejectedWithAClearMessage() {
        val bad = mapOf(
            "" to "empty",
            "   " to "empty",
            "hello world" to "8 ranks",
            "8/8/8/8/8/8/8 w - - 0 1" to "8 ranks",
            "8/8/8/8/8/8/8/8/8 w - - 0 1" to "8 ranks",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNX w KQkq - 0 1" to "'X'",
            "rnbqkbnr/pppppppp/9/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1" to "'9'",
            "rnbqkbnr/ppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1" to "rank 7",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPPP/RNBQKBNR w KQkq - 0 1" to "rank 2",
            "rnbqkbnr/pppppppp/44p/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1" to "rank 6",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR x KQkq - 0 1" to "side to move",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQxq - 0 1" to "castling",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq e9 0 1" to "en passant",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq e4 0 1" to "en passant",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - x 1" to "halfmove clock",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - -3 1" to "halfmove clock",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 one" to "fullmove number",
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 extra" to "7 fields",
        )
        for ((fen, expectedFragment) in bad) {
            val error = assertFailsWith<IllegalArgumentException>("\"$fen\" must be rejected") { Position.fromFen(fen) }
            assertTrue(
                error.message!!.contains(expectedFragment),
                "message for \"$fen\" should mention \"$expectedFragment\" but was: ${error.message}",
            )
        }
    }

    @Test
    fun equalityCoversTheFullState() {
        val base = Position.fromFen("r3k2r/8/8/8/4P3/8/8/R3K2R b KQkq e3 3 20")
        assertEquals(base, Position.fromFen(base.toFen()))
        assertEquals(base.hashCode(), Position.fromFen(base.toFen()).hashCode())
        val variants = listOf(
            "r3k2r/8/8/8/4P3/8/7P/R3K2R b KQkq e3 3 20", // placement
            "r3k2r/8/8/8/4P3/8/8/R3K2R w KQkq - 3 20", // side (and en passant)
            "r3k2r/8/8/8/4P3/8/8/R3K2R b KQk e3 3 20", // castling
            "r3k2r/8/8/8/4P3/8/8/R3K2R b KQkq - 3 20", // en passant
            "r3k2r/8/8/8/4P3/8/8/R3K2R b KQkq e3 4 20", // halfmove clock
            "r3k2r/8/8/8/4P3/8/8/R3K2R b KQkq e3 3 21", // fullmove number
        )
        for (fen in variants) assertNotEquals(base, Position.fromFen(fen), fen)
        assertFalse(base.equals("not a position"))
    }

    @Test
    fun fromBoardInfersCastlingAndKeepsPlausibleEnPassant() {
        val pieces = arrayOfNulls<Piece>(64)
        fun put(square: String, c: Char) {
            pieces[Square.parse(square)] = Piece.fromFenChar(c)
        }
        put("e1", 'K'); put("h1", 'R'); put("a1", 'R')
        put("e8", 'k'); put("a8", 'r'); put("h7", 'r')
        put("d5", 'p'); put("e5", 'P')

        val inferred = Position.fromBoard(pieces, Side.WHITE)
        assertEquals("r3k3/7r/8/3pP3/8/8/8/R3K2R w KQq - 0 1", inferred.toFen())

        val none = Position.fromBoard(pieces, Side.BLACK, inferCastling = false)
        assertEquals("r3k3/7r/8/3pP3/8/8/8/R3K2R b - - 0 1", none.toFen())

        val withEp = Position.fromBoard(pieces, Side.WHITE, enPassantSquare = Square.parse("d6"))
        assertEquals("r3k3/7r/8/3pP3/8/8/8/R3K2R w KQq d6 0 1", withEp.toFen())
        assertTrue(withEp.isLegal(Move.fromUci("e5d6")!!))

        // An en-passant square that does not fit the placement is ignored rather than trusted.
        assertEquals(-1, Position.fromBoard(pieces, Side.WHITE, enPassantSquare = Square.parse("c6")).enPassantSquare)
        assertEquals(-1, Position.fromBoard(pieces, Side.BLACK, enPassantSquare = Square.parse("d6")).enPassantSquare)

        assertFailsWith<IllegalArgumentException> { Position.fromBoard(arrayOfNulls(63), Side.WHITE) }
        assertFailsWith<IllegalArgumentException> { Position.fromBoard(pieces, Side.WHITE, enPassantSquare = 64) }

        // The input array is copied: later changes to it do not leak into the position.
        pieces[Square.parse("e5")] = null
        assertEquals("r3k3/7r/8/3pP3/8/8/8/R3K2R w KQq - 0 1", inferred.toFen())
    }

    @Test
    fun withSideToMove() {
        val position = Position.fromFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1")
        val flipped = position.withSideToMove(Side.WHITE)
        assertEquals("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1", flipped.toFen())
        assertEquals(position.placement(), flipped.placement())
        assertEquals(position, position.withSideToMove(Side.BLACK))
        assertEquals(Side.BLACK, flipped.withSideToMove(Side.BLACK).sideToMove)
        assertEquals(30, flipped.legalMoves().size)
    }
}
