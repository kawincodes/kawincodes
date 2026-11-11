package com.krstech.chesshelp.chess

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PositionValidatorTest {
    /** Pieces of a FEN placement, without going through Position (which normalises nothing here anyway). */
    private fun pieces(placement: String): Array<Piece?> {
        val result = arrayOfNulls<Piece>(64)
        for ((i, rankText) in placement.split('/').withIndex()) {
            var file = 0
            for (c in rankText) {
                if (c.isDigit()) {
                    file += c - '0'
                } else {
                    result[(7 - i) * 8 + file] = Piece.fromFenChar(c)!!
                    file++
                }
            }
            check(file == 8) { "bad test placement $placement" }
        }
        return result
    }

    private fun problems(placement: String, side: Side = Side.WHITE) = PositionValidator.problems(pieces(placement), side)

    /** Exactly one problem, containing all [fragments]. */
    private fun assertSingleProblem(placement: String, side: Side, vararg fragments: String) {
        val found = problems(placement, side)
        assertEquals(1, found.size, "expected exactly one problem, got $found")
        for (fragment in fragments) {
            assertTrue(found[0].contains(fragment), "\"${found[0]}\" should mention \"$fragment\"")
        }
    }

    @Test
    fun plausiblePositionsHaveNoProblems() {
        val clean = listOf(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR" to Side.WHITE,
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR" to Side.BLACK,
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R" to Side.WHITE,
            "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8" to Side.WHITE,
            "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1" to Side.WHITE,
            "8/8/8/8/8/8/8/K6k" to Side.BLACK,
            // The side to move may be in check (here even checkmated).
            "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR" to Side.WHITE,
            // Two queens are fine once a pawn is missing; so are three knights.
            "4k3/8/8/8/8/8/PPPPPPP1/QQ2K3" to Side.WHITE,
            "4k3/pppppp2/8/8/8/8/8/nnn1K2n" to Side.WHITE,
            // Nine queens need all eight pawns to be gone.
            "QQQQQQQQ/Q7/8/8/8/8/k7/7K" to Side.BLACK,
        )
        for ((placement, side) in clean) {
            assertEquals(emptyList(), problems(placement, side), "$placement ($side to move)")
        }
    }

    @Test
    fun missingKing() {
        assertSingleProblem("4k3/8/8/8/8/8/8/8", Side.WHITE, "White has no king")
        assertSingleProblem("8/8/8/8/8/8/8/4K3", Side.WHITE, "Black has no king")
        assertEquals(listOf("White has no king", "Black has no king"), problems("8/8/8/8/8/8/8/8"))
    }

    @Test
    fun severalKings() {
        assertSingleProblem("4k1k1/8/8/8/8/8/8/4K3", Side.WHITE, "Black has 2 kings (e8, g8)")
        assertSingleProblem("4k3/8/8/8/8/8/8/K1K1K3", Side.BLACK, "White has 3 kings (a1, c1, e1)")
    }

    @Test
    fun pawnsOnTheBackRanks() {
        assertSingleProblem("P3k3/8/8/8/8/8/8/4K3", Side.WHITE, "rank 1 or rank 8", "white pawn on a8")
        assertSingleProblem("4k3/8/8/8/8/8/8/4K2p", Side.WHITE, "black pawn on h1")
        // Own back rank is just as impossible.
        assertSingleProblem("4k2p/8/8/8/8/8/8/P3K3", Side.WHITE, "white pawn on a1", "black pawn on h8")
    }

    @Test
    fun tooManyPawns() {
        assertSingleProblem("4k3/8/8/8/8/P7/PPPPPPPP/4K3", Side.WHITE, "White has 9 pawns", "a2", "a3", "at most 8")
        assertSingleProblem("4k3/pppppppp/pp6/8/8/8/8/4K3", Side.WHITE, "Black has 10 pawns")
    }

    @Test
    fun tooManyPieces() {
        // 17 white pieces cannot be reached without also breaking the pawn/promotion limits,
        // so the other rules fire as well; the count itself must be reported too.
        val found = problems("4k3/8/8/8/8/P7/PPPPPPPP/RNBQKBNR")
        assertTrue(found.any { it.contains("White has 17 pieces") && it.contains("at most 16") }, found.toString())
        val black = problems("rnbqkbnr/pppppppp/n7/8/8/8/8/4K3")
        assertTrue(black.any { it.contains("Black has 17 pieces") }, black.toString())
        assertTrue(black.any { it.contains("3 knights") }, black.toString())
    }

    @Test
    fun adjacentKings() {
        assertSingleProblem("8/8/8/8/8/8/4k3/4K3", Side.WHITE, "adjacent", "white king on e1", "black king on e2")
        assertSingleProblem("8/8/8/3k4/4K3/8/8/8", Side.BLACK, "adjacent", "e4", "d5")
        assertEquals(emptyList(), problems("8/8/8/2k5/4K3/8/8/8"))
    }

    @Test
    fun sideNotToMoveInCheck() {
        // White to move while Black's king is attacked: White would simply take the king.
        assertSingleProblem(
            "4k3/8/8/8/8/8/8/4R1K1", Side.WHITE,
            "Black's king on e8", "white rook on e1", "White to move",
        )
        // The same placement with Black to move is an ordinary check.
        assertEquals(emptyList(), problems("4k3/8/8/8/8/8/8/4R1K1", Side.BLACK))
        // Black to move while White is in check, by a knight and a pawn at once.
        assertSingleProblem(
            "4k3/8/8/8/8/3n4/3p4/4K3", Side.BLACK,
            "White's king on e1", "black", "pawn on d2", "knight on d3", "Black to move",
        )
        // A blocked line is no check.
        assertEquals(emptyList(), problems("4k3/4p3/8/8/8/8/8/4R1K1", Side.WHITE))
        // Every piece type is recognised as an attacker.
        for (placement in listOf(
            "4k3/8/8/8/8/8/8/4Q1K1", "4k3/8/8/1B6/8/8/8/6K1", "4k3/8/3N4/8/8/8/8/6K1", "4k3/3P4/8/8/8/8/8/6K1",
        )) {
            assertEquals(1, problems(placement, Side.WHITE).size, placement)
            assertEquals(emptyList(), problems(placement, Side.BLACK), placement)
        }
    }

    @Test
    fun promotedPiecesMustFitTheMissingPawns() {
        // Two queens with all eight pawns still on the board.
        assertSingleProblem(
            "4k3/8/8/8/8/8/PPPPPPPP/QQ2K3", Side.WHITE,
            "White has 2 queens", "at least 1 promoted pawn", "8 pawns", "at most 0",
        )
        // Three rooks and three knights with seven pawns: two promotions needed, one possible.
        assertSingleProblem(
            "rrr1knnn/ppppppp1/8/8/8/8/8/4K3", Side.WHITE,
            "Black has 3 rooks, 3 knights", "at least 2 promoted pawns", "7 pawns", "at most 1",
        )
        // Two bishops on squares of the same colour: one of them is a promoted pawn.
        assertSingleProblem(
            "4k3/8/8/8/8/8/PPPPPPPP/1B1BK3", Side.WHITE,
            "White has 2 light-squared bishops",
        )
        assertSingleProblem(
            "3bkb2/pppppppp/8/8/8/8/8/4K3", Side.BLACK,
            "Black has 2 dark-squared bishops",
        )
        // ... which is fine with a pawn missing, and a normal bishop pair is fine anyway.
        assertEquals(emptyList(), problems("4k3/8/8/8/8/8/PPPPPPP1/1B1BK3"))
        assertEquals(emptyList(), problems("4k3/8/8/8/8/8/PPPPPPPP/2B1KB2"))
    }

    @Test
    fun severalProblemsAreAllReported() {
        val found = problems("P3k1k1/8/8/8/8/8/8/8", Side.WHITE)
        assertEquals(3, found.size, found.toString())
        assertTrue(found.any { it.contains("White has no king") })
        assertTrue(found.any { it.contains("Black has 2 kings (e8, g8)") })
        assertTrue(found.any { it.contains("white pawn on a8") })
    }

    @Test
    fun wrongArraySizeIsRejected() {
        assertFailsWith<IllegalArgumentException> { PositionValidator.problems(arrayOfNulls(60), Side.WHITE) }
    }
}
