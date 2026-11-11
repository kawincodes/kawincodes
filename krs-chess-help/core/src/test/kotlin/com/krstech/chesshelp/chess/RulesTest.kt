package com.krstech.chesshelp.chess

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RulesTest {
    private fun pos(fen: String) = Position.fromFen(fen)

    private fun Position.uciMoves(): Set<String> = legalMoves().map { it.uci() }.toSet()

    private fun Position.playUci(vararg moves: String): Position =
        moves.fold(this) { position, uci -> position.play(Move.fromUci(uci)!!) }

    // ------------------------------------------------------------------ castling

    @Test
    fun castlingMovesKingAndRook() {
        val position = pos("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")
        assertTrue(position.uciMoves().containsAll(listOf("e1g1", "e1c1")))
        assertEquals("r3k2r/8/8/8/8/8/8/R4RK1 b kq - 1 1", position.playUci("e1g1").toFen())
        assertEquals("r3k2r/8/8/8/8/8/8/2KR3R b kq - 1 1", position.playUci("e1c1").toFen())

        val black = position.withSideToMove(Side.BLACK)
        assertTrue(black.uciMoves().containsAll(listOf("e8g8", "e8c8")))
        assertEquals("r4rk1/8/8/8/8/8/8/R3K2R w KQ - 1 2", black.playUci("e8g8").toFen())
        assertEquals("2kr3r/8/8/8/8/8/8/R3K2R w KQ - 1 2", black.playUci("e8c8").toFen())
    }

    @Test
    fun castlingRightsAreLostWhenKingMoves() {
        val position = pos("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")
        assertEquals("r3k2r/8/8/8/8/8/4K3/R6R b kq - 1 1", position.playUci("e1e2").toFen())
        // Moving back does not restore them.
        val back = position.playUci("e1e2", "e8e7", "e2e1", "e7e8")
        assertEquals("r3k2r/8/8/8/8/8/8/R3K2R w - - 4 3", back.toFen())
        assertFalse(back.uciMoves().any { it == "e1g1" || it == "e1c1" })
    }

    @Test
    fun castlingRightsAreLostWhenRookMoves() {
        val position = pos("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")
        assertEquals("r3k2r/8/8/8/8/8/8/1R2K2R b Kkq - 1 1", position.playUci("a1b1").toFen())
        assertEquals("r3k2r/8/8/8/8/8/8/R3K1R1 b Qkq - 1 1", position.playUci("h1g1").toFen())
        assertEquals("r3k1r1/8/8/8/8/8/8/R3K2R w KQq - 1 2", position.withSideToMove(Side.BLACK).playUci("h8g8").toFen())
        assertEquals("1r2k2r/8/8/8/8/8/8/R3K2R w KQk - 1 2", position.withSideToMove(Side.BLACK).playUci("a8b8").toFen())
    }

    @Test
    fun castlingRightIsLostWhenRookIsCapturedOnItsHomeSquare() {
        // Bishop takes the a8 rook: Black loses queen-side castling only.
        val position = pos("r3k2r/8/8/8/8/8/6B1/R3K2R w KQkq - 0 1")
        val afterCapture = position.playUci("g2a8")
        assertEquals("B3k2r/8/8/8/8/8/8/R3K2R b KQk - 0 1", afterCapture.toFen())
        assertFalse("e8c8" in afterCapture.uciMoves())
        assertTrue("e8g8" in afterCapture.uciMoves())

        // Rook takes rook h1xh8: both sides lose the king-side right (white's rook left h1).
        val rooks = pos("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1").playUci("h1h8")
        assertEquals("r3k2R/8/8/8/8/8/8/R3K3 b Qq - 0 1", rooks.toFen())

        // A new rook arriving on h8 later does not bring the right back.
        val refilled = pos("r3k3/7r/8/8/8/8/8/4K3 b q - 0 1").playUci("h7h8")
        assertEquals("r3k2r/8/8/8/8/8/8/4K3 w q - 1 2", refilled.toFen())
    }

    @Test
    fun cannotCastleOutOfCheck() {
        val position = pos("4k3/8/8/8/8/8/4r3/R3K2R w KQ - 0 1")
        assertTrue(position.isInCheck())
        assertFalse("e1g1" in position.uciMoves())
        assertFalse("e1c1" in position.uciMoves())
    }

    @Test
    fun cannotCastleThroughCheck() {
        val kingSideBlocked = pos("4k3/8/8/8/8/8/5r2/R3K2R w KQ - 0 1") // f1 attacked
        assertFalse("e1g1" in kingSideBlocked.uciMoves())
        assertTrue("e1c1" in kingSideBlocked.uciMoves())

        val queenSideBlocked = pos("4k3/8/8/8/8/8/3r4/R3K2R w KQ - 0 1") // d1 attacked
        assertTrue("e1g1" in queenSideBlocked.uciMoves())
        assertFalse("e1c1" in queenSideBlocked.uciMoves())

        val black = pos("r3k2r/8/8/8/8/8/8/3RKR2 b kq - 0 1") // d8 and f8 attacked
        assertFalse("e8g8" in black.uciMoves())
        assertFalse("e8c8" in black.uciMoves())
    }

    @Test
    fun cannotCastleIntoCheck() {
        val kingSide = pos("4k3/8/8/8/8/8/6r1/R3K2R w KQ - 0 1") // g1 attacked
        assertFalse("e1g1" in kingSide.uciMoves())
        assertTrue("e1c1" in kingSide.uciMoves())

        val queenSide = pos("4k3/8/8/8/8/8/2r5/R3K2R w KQ - 0 1") // c1 attacked
        assertTrue("e1g1" in queenSide.uciMoves())
        assertFalse("e1c1" in queenSide.uciMoves())
    }

    @Test
    fun queenSideCastlingOnlyNeedsTheKingPathToBeSafe() {
        // b1 is attacked, but the king never touches it; the rook may pass an attacked square.
        val position = pos("4k3/8/8/8/8/8/1r6/R3K2R w KQ - 0 1")
        assertTrue("e1c1" in position.uciMoves())
        // An attacked rook does not prevent castling either.
        val attackedRook = pos("r3k3/8/8/8/8/8/8/R3K3 w Q - 0 1")
        assertTrue("e1c1" in attackedRook.uciMoves())
    }

    @Test
    fun squaresBetweenKingAndRookMustBeEmpty() {
        assertFalse("e1c1" in pos("4k3/8/8/8/8/8/8/RN2K2R w KQ - 0 1").uciMoves()) // b1
        assertFalse("e1c1" in pos("4k3/8/8/8/8/8/8/R1B1K2R w KQ - 0 1").uciMoves()) // c1
        assertFalse("e1c1" in pos("4k3/8/8/8/8/8/8/R2QK2R w KQ - 0 1").uciMoves()) // d1
        assertFalse("e1g1" in pos("4k3/8/8/8/8/8/8/R3KB1R w KQ - 0 1").uciMoves()) // f1
        assertFalse("e1g1" in pos("4k3/8/8/8/8/8/8/R3K1NR w KQ - 0 1").uciMoves()) // g1
        assertTrue("e1g1" in pos("4k3/8/8/8/8/8/8/RN2K2R w KQ - 0 1").uciMoves())
        // An enemy piece in between blocks just the same.
        assertFalse("e8c8" in pos("rn2k2r/8/8/8/8/8/8/4K3 b kq - 0 1").uciMoves())
        assertFalse("e8g8" in pos("r3k1Nr/8/8/8/8/8/8/4K3 b kq - 0 1").uciMoves())
    }

    @Test
    fun noCastlingWithoutTheRight() {
        val position = pos("r3k2r/8/8/8/8/8/8/R3K2R w - - 0 1")
        assertFalse(position.uciMoves().any { it == "e1g1" || it == "e1c1" })
        assertFalse(position.canCastle(Side.WHITE, kingSide = true))
    }

    // ------------------------------------------------------------------ en passant

    @Test
    fun enPassantSquareIsSetAfterEveryDoublePush() {
        // Even when no enemy pawn could capture, as the FEN standard prescribes.
        assertEquals(
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
            Position.START.playUci("e2e4").toFen(),
        )
        assertEquals(
            "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2",
            Position.START.playUci("e2e4", "c7c5").toFen(),
        )
        // A single push sets none.
        assertEquals(-1, Position.START.playUci("e2e3").enPassantSquare)
    }

    @Test
    fun enPassantCapture() {
        val position = Position.START.playUci("e2e4", "a7a6", "e4e5", "d7d5")
        assertEquals("rnbqkbnr/1pp1pppp/p7/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3", position.toFen())
        assertTrue("e5d6" in position.uciMoves())
        val after = position.playUci("e5d6")
        assertEquals("rnbqkbnr/1pp1pppp/p2P4/8/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 3", after.toFen())
        assertNull(after.pieceAt(Square.parse("d5")))

        // The right lasts for one move only.
        val later = position.playUci("g1f3", "a6a5")
        assertEquals(-1, later.enPassantSquare)
        assertFalse("e5d6" in later.uciMoves())
    }

    @Test
    fun enPassantCaptureByBlackFromBothSides() {
        val position = pos("4k3/8/8/8/2pPp3/8/8/4K3 b - d3 0 1")
        assertTrue(position.uciMoves().containsAll(listOf("c4d3", "e4d3")))
        assertEquals("4k3/8/8/8/4p3/3p4/8/4K3 w - - 0 2", position.playUci("c4d3").toFen())
        assertEquals("4k3/8/8/8/2p5/3p4/8/4K3 w - - 0 2", position.playUci("e4d3").toFen())
    }

    @Test
    fun enPassantThatExposesOwnKingOnTheRankIsIllegal() {
        // Both pawns leave the fifth rank at once, opening the line from the h5 rook to the king.
        val position = pos("8/8/8/K2pP2r/8/8/8/4k3 w - d6 0 1")
        assertFalse("e5d6" in position.uciMoves())
        assertTrue("e5e6" in position.uciMoves())
        // Same idea for Black on the fourth rank.
        val black = pos("4K3/8/8/8/k2Pp2R/8/8/8 b - d3 0 1")
        assertFalse("e4d3" in black.uciMoves())
        // With a third piece on the rank the capture is fine.
        val shielded = pos("8/8/8/K2pP1nr/8/8/8/4k3 w - d6 0 1")
        assertTrue("e5d6" in shielded.uciMoves())
    }

    @Test
    fun enPassantByADiagonallyPinnedPawn() {
        // The e5 pawn is pinned on the a1-h8 diagonal... capturing towards d6 leaves it: illegal.
        val pinned = pos("7b/8/8/3pP3/8/8/1K6/4k3 w - d6 0 1")
        assertFalse("e5d6" in pinned.uciMoves())
        // Pinned on the diagonal that the capture follows: legal.
        val along = pos("4k3/2b5/8/3pP3/5K2/8/8/8 w - d6 0 1")
        assertTrue("e5d6" in along.uciMoves())
    }

    @Test
    fun enPassantCanCaptureACheckingPawn() {
        // Black has just played d7-d5+, the pawn checks the king on e4.
        val position = pos("7k/8/8/3pP3/4K3/8/8/8 w - d6 0 2")
        assertTrue(position.isInCheck())
        assertTrue("e5d6" in position.uciMoves())
        assertFalse("e5e6" in position.uciMoves())
    }

    @Test
    fun enPassantDoesNotHelpAgainstAnotherCheck() {
        // The knight on f2 gives check; taking en passant does nothing about it.
        val knight = pos("7k/8/8/3pP3/8/8/5n2/7K w - d6 0 2")
        assertTrue(knight.isInCheck())
        assertFalse("e5d6" in knight.uciMoves())
        // A rook check along the first rank.
        val rook = pos("7k/8/8/3pP3/8/8/8/r3K3 w - d6 0 2")
        assertTrue(rook.isInCheck())
        assertFalse("e5d6" in rook.uciMoves())
        // Pawn and knight both give check: only the king may move.
        val double = pos("7k/8/8/3pP3/4K3/8/5n2/8 w - d6 0 2")
        assertTrue(double.uciMoves().all { it.startsWith("e4") })
        // Without any check the capture is available.
        assertTrue("e5d6" in pos("7k/8/8/3pP3/8/8/8/4K3 w - d6 0 2").uciMoves())
    }

    @Test
    fun enPassantCanInterposeAgainstACheck() {
        // Cannot arise in a real game, but the generator must stay consistent on any position:
        // the rook checks along the sixth rank and the capturing pawn lands on d6, in between.
        val position = pos("8/8/K6r/3pP3/8/8/8/7k w - d6 0 2")
        assertTrue(position.isInCheck())
        assertTrue("e5d6" in position.uciMoves())
        assertTrue("e5e6" in position.uciMoves())
    }

    // ------------------------------------------------------------------ promotion

    @Test
    fun promotionsWithAndWithoutCapture() {
        val position = pos("1n5k/P7/8/8/8/8/8/K7 w - - 0 1")
        val promotions = position.uciMoves().filter { it.startsWith("a7") }.toSet()
        assertEquals(setOf("a7a8q", "a7a8r", "a7a8b", "a7a8n", "a7b8q", "a7b8r", "a7b8b", "a7b8n"), promotions)
        assertEquals("Qn5k/8/8/8/8/8/8/K7 b - - 0 1", position.playUci("a7a8q").toFen())
        assertEquals("Rn5k/8/8/8/8/8/8/K7 b - - 0 1", position.playUci("a7a8r").toFen())
        assertEquals("Bn5k/8/8/8/8/8/8/K7 b - - 0 1", position.playUci("a7a8b").toFen())
        assertEquals("Nn5k/8/8/8/8/8/8/K7 b - - 0 1", position.playUci("a7a8n").toFen())
        assertEquals("1Q5k/8/8/8/8/8/8/K7 b - - 0 1", position.playUci("a7b8q").toFen())
        assertEquals("1N5k/8/8/8/8/8/8/K7 b - - 0 1", position.playUci("a7b8n").toFen())

        // The promotion piece is mandatory, and only on the last rank.
        assertFalse(position.isLegal(Move(Square.parse("a7"), Square.parse("a8"))))
        assertFalse(position.isLegal(Move(Square.parse("a7"), Square.parse("a8"), PieceType.KING)))
        assertFalse(position.isLegal(Move(Square.parse("a7"), Square.parse("a8"), PieceType.PAWN)))
        assertFalse(Position.START.isLegal(Move(Square.parse("e2"), Square.parse("e4"), PieceType.QUEEN)))
        assertFalse(Position.START.isLegal(Move(Square.parse("e2"), Square.parse("e4"), PieceType.PAWN)))
        assertTrue(Position.START.isLegal(Move(Square.parse("e2"), Square.parse("e4"))))
    }

    @Test
    fun blackPromotions() {
        val position = pos("k7/8/8/8/8/8/p7/1N5K b - - 0 1")
        val promotions = position.uciMoves().filter { it.startsWith("a2") }.toSet()
        assertEquals(setOf("a2a1q", "a2a1r", "a2a1b", "a2a1n", "a2b1q", "a2b1r", "a2b1b", "a2b1n"), promotions)
        assertEquals("k7/8/8/8/8/8/8/1q5K w - - 0 2", position.playUci("a2b1q").toFen())
        assertEquals(Piece(Side.BLACK, PieceType.ROOK), position.playUci("a2a1r").pieceAt(0))
    }

    @Test
    fun promotionCapturingARookOnItsHomeSquareRemovesTheCastlingRight() {
        val position = pos("r3k2r/1P6/8/8/8/8/8/4K3 w kq - 0 1")
        assertEquals("Q3k2r/8/8/8/8/8/8/4K3 b k - 0 1", position.playUci("b7a8q").toFen())
    }

    // ------------------------------------------------------------------ pins and checks

    @Test
    fun pinnedKnightCannotMove() {
        val position = pos("4r1k1/8/8/8/8/8/4N3/4K3 w - - 0 1")
        assertTrue(position.uciMoves().none { it.startsWith("e2") })
    }

    @Test
    fun pinnedSlidersMoveOnlyAlongThePin() {
        val rook = pos("4r1k1/8/8/8/8/8/4R3/4K3 w - - 0 1")
        assertEquals(
            setOf("e2e3", "e2e4", "e2e5", "e2e6", "e2e7", "e2e8"),
            rook.uciMoves().filter { it.startsWith("e2") }.toSet(),
        )
        val bishop = pos("6k1/8/8/7b/8/5B2/8/3K4 w - - 0 1")
        assertEquals(
            setOf("f3e2", "f3g4", "f3h5"),
            bishop.uciMoves().filter { it.startsWith("f3") }.toSet(),
        )
        // A bishop pinned on a file cannot move at all.
        val stuck = pos("4r1k1/8/8/8/8/8/4B3/4K3 w - - 0 1")
        assertTrue(stuck.uciMoves().none { it.startsWith("e2") })
    }

    @Test
    fun pinnedPawn() {
        // Pinned on the file: may advance, may not capture.
        val file = pos("4r1k1/8/8/8/8/3p1p2/4P3/4K3 w - - 0 1")
        assertEquals(setOf("e2e3", "e2e4"), file.uciMoves().filter { it.startsWith("e2") }.toSet())
        // Pinned on a diagonal: may only capture the pinning piece.
        val diagonal = pos("6k1/8/8/8/8/2b5/3P4/4K3 w - - 0 1")
        assertEquals(setOf("d2c3"), diagonal.uciMoves().filter { it.startsWith("d2") }.toSet())
        // Pinned on the rank: cannot move.
        val rank = pos("6k1/8/8/8/8/8/r2PK3/8 w - - 0 1")
        assertTrue(rank.uciMoves().none { it.startsWith("d2") })
    }

    @Test
    fun singleCheckMustBeAnswered() {
        // Rook e8 checks the king on e1: capture it, block on the e-file, or step aside.
        val position = pos("4r1k1/8/8/8/8/6B1/8/R3K2Q w Q - 0 1")
        assertTrue(position.isInCheck())
        assertEquals(
            setOf("e1d1", "e1d2", "e1f1", "e1f2", "g3e5", "h1e4"),
            position.uciMoves(),
        )
    }

    @Test
    fun doubleCheckAllowsOnlyKingMoves() {
        val position = pos("4r1k1/8/8/8/8/5n2/3P4/3QK3 w - - 0 1")
        assertTrue(position.isInCheck())
        // Qxf3 would remove one checker but not the other.
        assertEquals(setOf("e1f1", "e1f2"), position.uciMoves())
    }

    @Test
    fun kingCannotStepBackAlongTheCheckingLine() {
        // Rook a1 checks along the first rank: f1 is still on that line "behind" the king.
        val position = pos("6k1/8/8/8/8/8/8/r3K3 w - - 0 1")
        assertEquals(setOf("e1d2", "e1e2", "e1f2"), position.uciMoves())
    }

    @Test
    fun kingCannotCaptureAProtectedPiece() {
        val protectedQueen = pos("6k1/8/8/8/8/2b5/3q4/4K3 w - - 0 1")
        assertTrue(protectedQueen.isCheckmate().not())
        assertFalse("e1d2" in protectedQueen.uciMoves())
        val loose = pos("6k1/8/8/8/8/8/3q4/4K3 w - - 0 1")
        assertTrue("e1d2" in loose.uciMoves())
    }

    // ------------------------------------------------------------------ clocks

    @Test
    fun halfmoveClockAndFullmoveNumber() {
        var position = Position.START
        position = position.playUci("g1f3")
        assertEquals(1 to 1, position.halfmoveClock to position.fullmoveNumber)
        position = position.playUci("g8f6")
        assertEquals(2 to 2, position.halfmoveClock to position.fullmoveNumber)
        position = position.playUci("e2e4") // pawn move resets
        assertEquals(0 to 2, position.halfmoveClock to position.fullmoveNumber)
        position = position.playUci("f6e4") // capture resets
        assertEquals(0 to 3, position.halfmoveClock to position.fullmoveNumber)
        position = position.playUci("f1c4", "e4f6", "e1g1") // castling counts as a quiet move
        assertEquals(3 to 4, position.halfmoveClock to position.fullmoveNumber)
        assertEquals("rnbqkb1r/pppppppp/5n2/8/2B5/5N2/PPPP1PPP/RNBQ1RK1 b kq - 3 4", position.toFen())

        val late = pos("4k3/8/8/8/8/8/8/4K2R b K - 99 80").playUci("e8d8")
        assertEquals("3k4/8/8/8/8/8/8/4K2R w K - 100 81", late.toFen())
    }

    // ------------------------------------------------------------------ game end

    @Test
    fun checkmate() {
        val foolsMate = Position.START.playUci("f2f3", "e7e5", "g2g4", "d8h4")
        assertEquals("rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3", foolsMate.toFen())
        assertTrue(foolsMate.isInCheck())
        assertTrue(foolsMate.isCheckmate())
        assertFalse(foolsMate.isStalemate())
        assertTrue(foolsMate.legalMoves().isEmpty())

        val backRank = pos("3R2k1/5ppp/8/8/8/8/8/6K1 b - - 0 1")
        assertTrue(backRank.isCheckmate())
        val smothered = pos("6rk/5Npp/8/8/8/8/8/6K1 b - - 0 1")
        assertTrue(smothered.isCheckmate())
    }

    @Test
    fun stalemate() {
        val position = pos("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1")
        assertFalse(position.isInCheck())
        assertTrue(position.isStalemate())
        assertFalse(position.isCheckmate())
        assertTrue(position.legalMoves().isEmpty())

        // The classic king-and-pawn stalemate.
        val busy = pos("k7/P7/K7/8/8/8/8/8 b - - 0 1")
        assertTrue(busy.isStalemate())
    }

    @Test
    fun checkIsNeitherMateNorStalemate() {
        val position = pos("4k3/8/8/8/8/8/4r3/4K3 w - - 0 1")
        assertTrue(position.isInCheck())
        assertFalse(position.isCheckmate())
        assertFalse(position.isStalemate())
        assertFalse(Position.START.isInCheck())
        assertFalse(Position.START.isCheckmate())
        assertFalse(Position.START.isStalemate())
    }

    @Test
    fun insufficientMaterial() {
        val drawn = listOf(
            "8/8/8/8/8/8/8/K6k w - - 0 1", // K v K
            "8/8/8/8/8/8/8/KN5k w - - 0 1", // K+N v K
            "8/8/8/8/8/8/8/K5nk w - - 0 1", // K v K+N
            "8/8/8/8/8/8/8/KB5k w - - 0 1", // K+B v K
            "8/8/8/8/8/8/8/K4b1k b - - 0 1", // K v K+B
            "8/8/8/8/8/8/8/KB3b1k w - - 0 1", // K+B v K+B, b1 and f1 are both light squares
            "8/8/8/8/8/8/8/KB1B3k w - - 0 1", // two bishops of one side on the same colour (one promoted)
        )
        for (fen in drawn) assertTrue(pos(fen).isInsufficientMaterial(), fen)
        // b1 is a light square and f8 a dark one: with opposite-coloured bishops a mate can be constructed.
        assertFalse(pos("5b2/8/8/8/8/8/8/KB5k w - - 0 1").isInsufficientMaterial())

        val playable = listOf(
            "8/8/8/8/8/8/P7/K6k w - - 0 1", // pawn
            "8/8/8/8/8/8/8/KR5k w - - 0 1", // rook
            "8/8/8/8/8/8/8/KQ5k w - - 0 1", // queen
            "8/8/8/8/8/8/8/KNN4k w - - 0 1", // two knights (mate is possible, if not forced)
            "8/8/8/8/8/8/8/KN4nk w - - 0 1", // knight v knight
            "8/8/8/8/8/8/8/KB4nk w - - 0 1", // bishop v knight
            "8/8/8/8/8/8/8/KBN4k w - - 0 1", // bishop and knight
            "8/8/8/8/8/8/8/KBB4k w - - 0 1", // b1 light, c1 dark
            Position.START_FEN,
        )
        for (fen in playable) assertFalse(pos(fen).isInsufficientMaterial(), fen)
    }

    // ------------------------------------------------------------------ API contracts

    @Test
    fun playRejectsIllegalMoves() {
        val error = assertFailsWith<IllegalArgumentException> { Position.START.play(Move.fromUci("e2e5")!!) }
        assertTrue(error.message!!.contains("e2e5"), error.message)
        assertTrue(error.message!!.contains(Position.START_FEN), error.message)
        assertFailsWith<IllegalArgumentException> { Position.START.play(Move(-1, 99)) }
        assertFailsWith<IllegalArgumentException> { Position.START.play(Move.fromUci("e7e5")!!) } // wrong side
        assertFalse(Position.START.isLegal(Move.fromUci("e1g1")!!))
        assertTrue(Position.START.isLegal(Move.fromUci("g1f3")!!))
    }

    @Test
    fun positionsAreImmutable() {
        val start = Position.START
        val moves = start.legalMoves()
        assertEquals(20, moves.size)
        start.play(moves[0])
        assertEquals(Position.START_FEN, start.toFen())
        assertFailsWith<UnsupportedOperationException> { (moves as MutableList<Move>).clear() }
        assertEquals(moves, start.legalMoves())
    }

    @Test
    fun implausiblePlacementsDoNotCrash() {
        // No kings, pawns on the back ranks, far too many queens: rules are applied as far as possible.
        val noKings = pos("8/8/8/3q4/8/8/8/R7 w - - 0 1")
        assertEquals(14, noKings.legalMoves().size)
        assertFalse(noKings.isInCheck())
        assertFalse(noKings.isCheckmate())
        noKings.legalMoves().forEach { noKings.play(it); noKings.san(it) }

        val backRankPawns = pos("P3k2p/8/8/8/8/8/8/p3K2P w - - 0 1")
        backRankPawns.legalMoves().forEach { backRankPawns.play(it) }
        val blackToo = backRankPawns.withSideToMove(Side.BLACK)
        blackToo.legalMoves().forEach { blackToo.play(it) }

        val queens = pos("QQQQQQQQ/QQQQQQQQ/8/8/8/8/qqqqqqqq/qqqqqqqq w - - 0 1")
        assertTrue(queens.legalMoves().size > 16)
        queens.legalMoves().forEach { queens.play(it) }

        val everything = pos("QQQQQQQQ/Q6Q/Q6Q/Q6Q/Q6Q/Q6Q/Q6Q/QQQQQQQk w - - 0 1")
        assertTrue(everything.legalMoves().size > Board.MAX_MOVES, "all moves are kept even beyond the usual buffer size")
        everything.legalMoves().forEach { everything.play(it) }

        // Capturing a king is "legal" when the side not to move was left in check.
        val hanging = pos("4k3/8/8/8/8/8/8/4R1K1 w - - 0 1")
        assertTrue(hanging.isLegal(Move.fromUci("e1e8")!!))
        val captured = hanging.playUci("e1e8")
        assertEquals("4R3/8/8/8/8/8/8/6K1 b - - 0 1", captured.toFen())
        assertTrue(captured.legalMoves().isEmpty())

        val twoKings = pos("4k3/8/8/8/8/8/8/K3K3 w - - 0 1")
        twoKings.legalMoves().forEach { twoKings.play(it) }
    }
}
