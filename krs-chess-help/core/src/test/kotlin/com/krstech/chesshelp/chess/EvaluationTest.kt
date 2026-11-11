package com.krstech.chesshelp.chess

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvaluationTest {
    private fun white(fen: String): Int = Evaluation.evaluateWhite(Position.fromFen(fen).toBoard())

    private fun toMove(fen: String): Int = Evaluation.evaluate(Position.fromFen(fen).toBoard())

    /** Mirroring swaps the colours, so White's score must flip sign and the mover's score must not change. */
    private fun assertSymmetric(fen: String) {
        val mirrored = mirrorFen(fen)
        assertEquals(white(fen), -white(mirrored), "White's view of $fen and of its mirror $mirrored")
        assertEquals(toMove(fen), toMove(mirrored), "side to move's view of $fen and of its mirror $mirrored")
        // The side to move does not enter the evaluation itself.
        val position = Position.fromFen(fen)
        val other = position.withSideToMove(position.sideToMove.opposite)
        assertEquals(white(fen), Evaluation.evaluateWhite(other.toBoard()))
        assertEquals(toMove(fen), -Evaluation.evaluate(other.toBoard()))
    }

    @Test
    fun mirrorFenHelperMirrorsEveryField() {
        assertEquals(
            "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w kqKQ c6 0 2",
            mirrorFen("rnbqkbnr/pppp1ppp/8/4p3/2P5/8/PP1PPPPP/RNBQKBNR b KQkq c3 0 2"),
        )
        assertEquals("r3k3/8/8/8/8/8/8/4K2R b qK - 3 9", mirrorFen("4k2r/8/8/8/8/8/8/R3K3 w Qk - 3 9"))
        val fen = "r1bq1rk1/pp2ppbp/2np1np1/8/3NP3/2N1BP2/PPPQ2PP/R3KB1R w KQ - 3 9"
        assertEquals(fen, mirrorFen(mirrorFen(fen)))
        assertEquals("e7e5", mirrorUci("e2e4"))
        assertEquals("a2a1q", mirrorUci("a7a8q"))
    }

    @Test
    fun startPositionIsEqual() {
        assertEquals(0, white(Position.START_FEN))
        assertEquals(0, toMove(Position.START_FEN))
    }

    @Test
    fun evaluationIsColourSymmetricOnHandPickedPositions() {
        val fens = listOf(
            Position.START_FEN,
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
            "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10",
            "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
            "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8",
            // Pawn structure: passed, doubled, isolated pawns on both sides.
            "4k3/pp3p2/5p2/P7/3P4/3P4/6PP/4K3 w - - 0 1",
            "4k3/8/1p6/1P4p1/6P1/8/P6p/4K3 b - - 0 1",
            // Bishop pair, rooks on open and half-open files, king shields.
            "2rr2k1/5ppp/8/8/8/8/PPP2BB1/1K1R3R w - - 0 1",
            "r4rk1/ppp2ppp/8/8/8/2N2N2/PPP5/2KR3R b - - 0 1",
            // Endgame corrections: material that cannot win, and mating material against a bare king.
            "8/8/8/4k3/8/8/2NN4/4K3 w - - 0 1",
            "8/8/8/4k3/8/8/2B5/4K3 w - - 0 1",
            "8/8/8/4k3/8/3b4/2R5/4K3 w - - 0 1",
            "8/8/8/4k3/8/8/2BN4/4K3 w - - 0 1",
            "k7/8/8/8/8/8/8/3QK3 w - - 0 1",
            "7k/8/8/8/8/8/1p6/RK6 b - - 0 1",
            "8/8/8/4k3/8/8/2Rr4/4K3 w - - 0 1",
            // Placements that can not occur in a game must still be handled symmetrically.
            "8/8/8/3q4/8/3Q4/8/8 w - - 0 1",
            "qqqqkqqq/qqqqqqqq/8/8/8/8/8/4K3 w - - 0 1",
            "4k3/8/8/8/8/8/8/4KK2 w - - 0 1",
        )
        for (fen in fens) assertSymmetric(fen)
    }

    @Test
    fun evaluationIsColourSymmetricAlongRandomGames() {
        val random = Random(4242)
        var checked = 0
        var endgames = 0
        repeat(40) {
            var position = Position.START
            var ply = 0
            while (ply < 300) {
                val moves = position.legalMoves()
                if (moves.isEmpty()) break
                // Prefer captures now and then so that the games also reach thin endgames.
                val captures = moves.filter { position.pieceAt(it.to) != null }
                position = if (captures.isNotEmpty() && random.nextInt(3) == 0) {
                    position.play(captures[random.nextInt(captures.size)])
                } else {
                    position.play(moves[random.nextInt(moves.size)])
                }
                ply++
                assertSymmetric(position.toFen())
                checked++
                if ((0..63).count { position.pieceAt(it) != null } <= 6) endgames++
            }
        }
        assertTrue(checked > 5000, "only $checked positions checked")
        assertTrue(endgames > 200, "only $endgames endgame positions checked")
        println("evaluation symmetry: $checked positions, $endgames of them with at most 6 pieces")
    }

    @Test
    fun materialDominatesTheScore() {
        // Start position without one piece of Black's.
        assertTrue(white("rnb1kbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1") > 800)
        assertTrue(white("1nbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQk - 0 1") > 400)
        assertTrue(white("r1bqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1") > 250)
        assertTrue(white("rnbqkbnr/pppp1ppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1") in 40..200)
        // And the mover's view has the opposite sign for Black.
        assertTrue(toMove("rnb1kbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1") < -800)
        // Queen > rook > bishop ~ knight > pawn.
        val queen = white("4k3/8/8/8/8/8/PPP5/3QK3 w - - 0 1")
        val rook = white("4k3/8/8/8/8/8/PPP5/3RK3 w - - 0 1")
        val bishop = white("4k3/8/8/8/8/8/PPP5/3BK3 w - - 0 1")
        val knight = white("4k3/8/8/8/8/8/PPP5/3NK3 w - - 0 1")
        val nothing = white("4k3/8/8/8/8/8/PPP5/4K3 w - - 0 1")
        assertTrue(queen > rook + 200 && rook > bishop + 100 && bishop > nothing + 200 && knight > nothing + 200)
        assertTrue(kotlin.math.abs(bishop - knight) < 120)
    }

    @Test
    fun positionalTermsPointTheRightWay() {
        // A passed pawn is worth more the further it has advanced.
        val third = white("4k3/8/8/8/8/P7/8/4K3 w - - 0 1")
        val fifth = white("4k3/8/8/P7/8/8/8/4K3 w - - 0 1")
        val seventh = white("4k3/P7/8/8/8/8/8/4K3 w - - 0 1")
        assertTrue(third < fifth && fifth < seventh, "$third $fifth $seventh")
        // The same pawn is worth less when an enemy pawn stands in its way.
        assertTrue(white("4k3/p7/8/P7/8/8/8/4K3 w - - 0 1") + 80 < fifth)
        // Doubled and isolated pawns are worse than a healthy pair.
        val healthy = white("4k3/8/8/8/8/8/3PP3/4K3 w - - 0 1")
        val doubled = white("4k3/8/8/8/8/4P3/4P3/4K3 w - - 0 1")
        val isolated = white("4k3/8/8/8/8/8/2P1P3/4K3 w - - 0 1")
        assertTrue(doubled < healthy && isolated < healthy, "$healthy $doubled $isolated")
        // Two bishops beat bishop and knight; a rook prefers the open file; a knight prefers the centre.
        assertTrue(white("4k3/pppp4/8/8/8/8/PPPP4/2B1KB2 w - - 0 1") > white("4k3/pppp4/8/8/8/8/PPPP4/2B1KN2 w - - 0 1"))
        assertTrue(white("4k3/pppp4/8/8/8/8/PPPP4/4K2R w - - 0 1") > white("4k3/pppp4/8/8/8/8/PPPP4/3RK3 w - - 0 1"))
        assertTrue(white("4k3/8/8/8/3N4/8/8/4K3 w - - 0 1") > white("4k3/8/8/8/8/8/8/N3K3 w - - 0 1"))
        // With the heavy pieces still on, a castled king behind its pawns beats one in the open.
        assertTrue(
            white("r2q1rk1/ppp2ppp/2n1bn2/8/8/2N1BN2/PPP2PPP/R2Q1RK1 w - - 0 1") >
                white("r2q1rk1/ppp2ppp/2n1bn2/8/8/2NKBN2/PPP2PPP/R2Q1R2 w - - 0 1") + 50,
        )
        // In a pawn ending it is the other way round: the king belongs in the centre.
        assertTrue(white("4k3/ppp5/8/8/3K4/8/PPP5/8 w - - 0 1") > white("4k3/ppp5/8/8/8/8/PPP5/6K1 w - - 0 1"))
    }

    @Test
    fun materialThatCanNotWinIsScoredNearADraw() {
        assertTrue(white("8/8/8/4k3/8/8/2NN4/4K3 w - - 0 1") in 0..60, "two knights")
        assertTrue(white("8/8/8/4k3/8/8/2B5/4K3 w - - 0 1") in 0..60, "lone bishop")
        assertTrue(white("8/8/8/4k3/8/3b4/2R5/4K3 w - - 0 1") in 0..60, "rook against bishop")
        assertTrue(white("8/8/8/4k3/8/8/2Rr4/4K3 w - - 0 1") in -30..30, "rook against rook")
        // …while real winning material is not scaled down.
        assertTrue(white("8/8/8/4k3/8/8/2BN4/4K3 w - - 0 1") > 500, "bishop and knight")
        assertTrue(white("8/8/8/4k3/8/8/2R5/4K3 w - - 0 1") > 400, "rook")
        assertTrue(white("8/8/8/4k3/8/8/2P5/2B1K3 w - - 0 1") > 250, "bishop with a pawn")
        assertTrue(white("8/8/8/4k3/8/3p4/2B5/4K3 b - - 0 1") in -10..60, "bishop against a pawn")
    }

    @Test
    fun bareKingIsDrivenToTheEdgeAndTowardsTheAttackingKing() {
        val centre = white("8/8/8/4k3/8/8/8/Q3K3 w - - 0 1")
        val edge = white("4k3/8/8/8/8/8/8/Q3K3 w - - 0 1")
        val corner = white("7k/8/8/8/8/8/8/Q3K3 w - - 0 1")
        assertTrue(centre < edge && edge < corner, "$centre $edge $corner")
        val far = white("7k/8/8/8/8/8/8/Q3K3 w - - 0 1")
        val near = white("7k/8/6K1/8/8/8/8/Q7 w - - 0 1")
        assertTrue(far < near, "$far $near")
    }

    @Test
    fun scoreIsClampedBelowTheMateRange() {
        assertEquals(-Evaluation.LIMIT, white("qqqqkqqq/qqqqqqqq/qqqqqqqq/8/8/8/8/4K3 w - - 0 1"))
        assertEquals(Evaluation.LIMIT, white("4k3/8/8/8/8/QQQQQQQQ/QQQQQQQQ/QQQQKQQQ w - - 0 1"))
        assertEquals(0, white("8/8/8/8/8/8/8/8 w - - 0 1"))
        assertTrue(kotlin.math.abs(white("4k3/8/8/8/8/8/8/4K3 w - - 0 1")) < 50)
    }
}
