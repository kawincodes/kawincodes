package com.krstech.chesshelp.chess

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * What the engine finds. Every puzzle is run as given and colour-mirrored, so both sides are
 * covered, and every forced mate is first proven by [MateOracle], so the expected values do not
 * rest on anyone's memory of the puzzle.
 */
class EngineTest {
    /** Depth-limited (and therefore deterministic) analysis; the time limit is only a safety net. */
    private fun analyze(fen: String, depth: Int, multiPv: Int = 3): EngineResult {
        val position = Position.fromFen(fen)
        val limits = SearchLimits(maxDepth = depth, timeMs = 60_000, multiPv = multiPv)
        val result = Engine().analyze(position, limits)
        assertSoundResult(position, result, limits)
        assertEquals(depth, result.depth, "all iterations must have completed for $fen")
        return result
    }

    private fun bothColours(fen: String): List<String> = listOf(fen, mirrorFen(fen))

    private fun uciFor(fen: String, original: String, uci: String): String = if (fen == original) uci else mirrorUci(uci)

    private class Mate(val fen: String, val mateIn: Int, val firstMove: String?)

    private fun checkMates(puzzles: List<Mate>, depth: Int, multiPv: Int) {
        for (puzzle in puzzles) {
            for (fen in bothColours(puzzle.fen)) {
                assertEquals(puzzle.mateIn, MateOracle.mateIn(fen, 3), "the puzzle itself: $fen")
                val best = analyze(fen, depth, multiPv).lines[0]
                assertEquals(puzzle.mateIn, best.mateIn, "mateIn for $fen (pv ${best.pv})")
                val plies = if (puzzle.mateIn > 0) 2 * puzzle.mateIn - 1 else -2 * puzzle.mateIn
                // assertSoundResult has already replayed the line and seen it end in checkmate.
                assertEquals(plies, best.pv.size, "the line must run to the mate: ${best.pv} in $fen")
                assertTrue(best.pv.last().endsWith("#"), "last move of ${best.pv}")
                if (puzzle.firstMove != null) {
                    assertEquals(uciFor(fen, puzzle.fen, puzzle.firstMove), best.move.uci(), "key move in $fen")
                }
            }
        }
    }

    @Test
    fun findsMateInOne() {
        checkMates(
            listOf(
                Mate("6k1/5ppp/8/8/8/8/5PPP/R5K1 w - - 0 1", 1, "a1a8"),
                Mate("r1bqkb1r/pppp1ppp/2n2n2/4p2Q/2B1P3/8/PPPP1PPP/RNB1K1NR w KQkq - 4 4", 1, "h5f7"),
                Mate("rnbqkbnr/pppp1ppp/8/4p3/6P1/5P2/PPPPP2P/RNBQKBNR b KQkq g3 0 2", 1, "d8h4"),
                Mate("6rk/6pp/8/6N1/8/8/8/7K w - - 0 1", 1, "g5f7"),
                Mate("7k/8/6K1/8/8/8/8/5Q2 w - - 0 1", 1, null),
                Mate("2r3k1/5ppp/8/8/8/8/2R2PPP/6K1 w - - 0 1", 1, "c2c8"),
            ),
            depth = 5,
            multiPv = 3,
        )
    }

    @Test
    fun mateInOneIsFoundAtDepthOne() {
        for (fen in bothColours("r1bqkb1r/pppp1ppp/2n2n2/4p2Q/2B1P3/8/PPPP1PPP/RNB1K1NR w KQkq - 4 4")) {
            val best = analyze(fen, 1).lines[0]
            assertEquals(1, best.mateIn)
            assertEquals(listOf("Qxf7#"), best.pv)
            assertEquals(Searcher.MATE - 1, best.scoreCp)
        }
    }

    @Test
    fun findsMateInTwo() {
        checkMates(
            listOf(
                Mate("7k/8/8/8/8/8/R7/1R4K1 w - - 0 1", 2, null),
                Mate("5r1k/6pp/7N/8/2Q5/8/8/7K w - - 0 1", 2, "c4g8"),
                Mate("r1bq2r1/b4pk1/p1pp1p2/1p2pP2/1P2P1PB/3P4/1PPQ2P1/R3K2R w - - 0 1", 2, "d2h6"),
                Mate("2bqkbn1/2pppp2/np2N3/r3P1p1/p2N2B1/5Q2/PPPPKPP1/RNB2r2 w - - 0 1", 2, "f3f7"),
                Mate("r2qk2r/pb4pp/1n2Pb2/2B2Q2/p1p5/2P5/2B2PPP/RN2R1K1 w - - 1 1", 2, "f5g6"),
                Mate("6k1/pp4p1/2p5/2bp4/8/P5Pb/1P3rrP/2BRRN1K b - - 0 1", 2, "g2g1"),
            ),
            depth = 6,
            multiPv = 2,
        )
    }

    @Test
    fun findsMateInThree() {
        checkMates(
            listOf(
                Mate("5rk1/5Npp/8/8/2Q5/8/8/7K w - - 0 1", 3, "f7h6"),
                Mate("r5rk/5p1p/5R2/4B3/8/8/7P/7K w - - 0 1", 3, "f6a6"),
                Mate("r1b1kb1r/pppp1ppp/5q2/4n3/3KP3/2N3PN/PPP4P/R1BQ1B1R b kq - 0 1", 3, "f8c5"),
                Mate("8/7k/8/8/8/8/R7/1R4K1 w - - 0 1", 3, null),
                Mate("3r4/pR2N3/2pkb3/5p2/8/2B5/qP3PPP/4R1K1 w - - 1 1", 3, "c3e5"),
                Mate("1k5r/pP3ppp/3p2b1/1BN1n3/1Q2P3/P1B5/KP3P1P/7q w - - 1 1", 3, "c5a6"),
            ),
            depth = 8,
            multiPv = 1,
        )
    }

    @Test
    fun reportsNegativeMateInWhenGettingMated() {
        checkMates(
            listOf(
                Mate("6k1/8/8/8/8/1r6/r7/7K w - - 0 1", -1, "h1g1"),
                Mate("5rQk/6pp/7N/8/8/8/8/7K b - - 0 1", -1, "f8g8"),
                Mate("7k/5K2/8/6Q1/8/8/8/8 b - - 0 1", -1, "h8h7"),
                Mate("k7/8/1K6/8/8/8/8/7R b - - 0 1", -1, "a8b8"),
                Mate("5rk1/6pp/7N/8/2Q5/8/8/7K b - - 0 1", -2, "g8h8"),
                Mate("4k3/8/4K3/8/8/8/8/R7 b - - 0 1", -2, null),
                Mate("8/8/8/8/8/2k5/8/K2r4 w - - 0 1", -2, "a1a2"),
                Mate("7k/8/8/8/8/8/R7/1R4K1 b - - 0 1", -3, null),
            ),
            depth = 8,
            multiPv = 3,
        )
    }

    @Test
    fun shorterMateRanksAboveLongerMateAndLosingSlowlyAboveLosingFast() {
        // Three ways to mate: in one, and twice in two.
        val winning = analyze("5k2/8/5K2/5Q2/8/8/8/8 w - - 0 1", 6).lines
        assertEquals(listOf(1, 2, 2), winning.map { it.mateIn })
        assertEquals(listOf(31_999, 31_997, 31_997), winning.map { it.scoreCp })

        // Being mated: the defences that last three moves come before the one that lasts two.
        val losing = analyze("7k/8/8/8/8/8/R7/1R4K1 b - - 0 1", 8).lines
        assertEquals(listOf(-3, -3, -2), losing.map { it.mateIn })
        assertEquals(listOf(-31_994, -31_994, -31_996), losing.map { it.scoreCp })
    }

    @Test
    fun winsAHangingPiece() {
        val cases = listOf(
            // The queen on g5 is attacked by the bishop on c1.
            Triple("rnb1kbnr/pppp1ppp/8/4p1q1/4P3/3P4/PPP2PPP/RNBQKBNR w KQkq - 1 3", "c1g5", 800),
            // An undefended rook.
            Triple("4k3/8/8/3r4/8/8/3R4/4K3 w - - 0 1", "d2d5", 400),
            // A knight fork of king and queen.
            Triple("q3k3/8/8/3N4/8/8/5PPP/6K1 w - - 0 1", "d5c7", 300),
        )
        for ((original, uci, minimumScore) in cases) {
            for (fen in bothColours(original)) {
                for (depth in intArrayOf(1, 2, 4, 6)) {
                    val result = analyze(fen, depth)
                    val best = result.lines[0]
                    assertEquals(uciFor(fen, original, uci), best.move.uci(), "depth $depth in $fen")
                    assertNull(best.mateIn)
                    assertTrue(best.scoreCp >= minimumScore, "score ${best.scoreCp} at depth $depth in $fen")
                    assertTrue(result.lines[1].scoreCp < best.scoreCp - 200, "the win must stand out: ${result.lines.map { it.scoreCp }}")
                }
            }
        }
    }

    @Test
    fun doesNotTakeADefendedPawnWithTheQueen() {
        for (fen in bothColours("4k3/8/2p5/3p4/8/8/3Q4/4K3 w - - 0 1")) {
            val queenTakes = uciFor(fen, "4k3/8/2p5/3p4/8/8/3Q4/4K3 w - - 0 1", "d2d5")
            for (depth in 1..6) {
                val result = analyze(fen, depth, multiPv = 5)
                assertTrue(result.lines.none { it.move.uci() == queenTakes }, "Qxd5 among the best moves at depth $depth")
                assertTrue(result.lines[0].scoreCp > 600, "still a queen up at depth $depth")
            }
        }
    }

    @Test
    fun doesNotStepIntoAKnightForkAtDepthFour() {
        // Qxd7 wins a pawn but gives up the guard of c2: ...Nc2+ then forks king and rook.
        val original = "5r1k/3p2pp/8/8/1n6/8/P4PPP/R2QK3 w Q - 0 1"
        for (fen in bothColours(original)) {
            val greedy = uciFor(fen, original, "d1d7")
            val fork = uciFor(fen, original, "b4c2")

            // The premise: the capture is what a one-ply look prefers, and the fork really is the refutation.
            assertEquals(greedy, analyze(fen, 1).lines[0].move.uci(), "the pawn must look tempting at depth 1")
            val afterCapture = Position.fromFen(fen).play(Move.fromUci(greedy)!!)
            assertEquals(fork, analyze(afterCapture.toFen(), 4).lines[0].move.uci(), "the refutation")

            for (depth in intArrayOf(4, 5, 6)) {
                val lines = analyze(fen, depth).lines
                assertTrue(lines.none { it.move.uci() == greedy }, "walked into the fork at depth $depth: ${lines.map { it.san }}")
            }
        }
    }

    @Test
    fun doesNotStepIntoARookSkewerAtDepthFour() {
        // In check from the e5 pawn. Kxe5 walks onto the e-file in front of the queen (…Re8+ wins
        // it); Qxe5+ takes the same pawn safely.
        val original = "r7/6k1/8/4p3/3K4/8/8/4Q3 w - - 0 1"
        for (fen in bothColours(original)) {
            val kingTakes = uciFor(fen, original, "d4e5")
            val queenTakes = uciFor(fen, original, "e1e5")
            val afterKingTakes = Position.fromFen(fen).play(Move.fromUci(kingTakes)!!)
            val punished = analyze(afterKingTakes.toFen(), 4).lines[0]
            assertEquals(uciFor(fen, original, "a8e8"), punished.move.uci(), "the skewer")
            assertTrue(punished.scoreCp > 300, "the skewer wins the queen: ${punished.scoreCp}")

            for (depth in intArrayOf(2, 3, 4, 6)) {
                val lines = analyze(fen, depth).lines
                assertEquals(queenTakes, lines[0].move.uci(), "depth $depth")
                assertTrue(lines.none { it.move.uci() == kingTakes }, "stepped into the skewer at depth $depth")
                assertTrue(lines[0].scoreCp > 400)
            }
        }
    }

    @Test
    fun avoidsStalemateWhenWinning() {
        // King and queen against a bare king that is one careless move away from stalemate.
        val positions = listOf(
            "k7/2Q5/8/8/8/8/8/7K w - - 0 1", // every king move stalemates, so do Qb6 and others
            "7k/5Q2/8/8/8/8/8/K7 w - - 0 1",
            "8/8/8/8/8/5k2/5q2/7K b - - 0 1",
        )
        for (original in positions) {
            for (fen in bothColours(original)) {
                val position = Position.fromFen(fen)
                val stalemating = position.legalMoves().filter { position.play(it).isStalemate() }
                assertTrue(stalemating.isNotEmpty(), "the position must offer a stalemating move: $fen")
                for (depth in 1..6) {
                    val result = analyze(fen, depth, multiPv = 3)
                    for (line in result.lines) {
                        assertTrue(line.move !in stalemating, "${line.san} stalemates (depth $depth, $fen)")
                        assertTrue(line.scoreCp > 800, "${line.san} scored ${line.scoreCp} (depth $depth, $fen)")
                    }
                    // Asked for every move, the stalemating ones are there and are worth exactly a draw.
                    val all = analyze(fen, depth, multiPv = 100).lines
                    assertEquals(position.legalMoves().size, all.size)
                    for (line in all.filter { it.move in stalemating }) {
                        assertEquals(0, line.scoreCp, "${line.san} is stalemate")
                        assertEquals(1, line.pv.size)
                    }
                }
            }
        }
    }

    @Test
    fun prefersTheDrawWhenLosingOtherwise() {
        // White is a rook down but can take the rook: K v K is a draw, everything else loses.
        val result = analyze("8/8/8/8/8/8/3r1k2/3K4 w - - 0 1", 6)
        assertEquals("d1d2", result.lines[0].move.uci())
        assertEquals(0, result.lines[0].scoreCp)

        // Black to move must not allow the capture of its last winning material.
        val defended = analyze("8/8/8/8/8/5k2/3r4/3K4 b - - 0 1", 4)
        assertTrue(defended.lines[0].scoreCp > 300, "black keeps the rook: ${defended.lines.map { it.san + " " + it.scoreCp }}")
    }

    @Test
    fun promotesToAQueen() {
        for (fen in bothColours("8/P7/8/8/8/8/8/k6K w - - 0 1")) {
            for (depth in 1..6) {
                val best = analyze(fen, depth).lines[0]
                assertEquals(PieceType.QUEEN, best.move.promotion, "depth $depth in $fen")
                assertTrue(best.scoreCp > 800)
            }
        }
    }

    @Test
    fun returnsNoLinesWithoutLegalMoves() {
        val finished = listOf(
            "8/8/8/8/8/5k2/5p2/5K2 w - - 0 1", // stalemate
            "7k/5Q2/6K1/8/8/8/8/8 b - - 0 1", // stalemate
            "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3", // checkmate
            "R5k1/5ppp/8/8/8/8/5PPP/6K1 b - - 0 1", // checkmate
        )
        for (fen in finished) {
            val position = Position.fromFen(fen)
            assertTrue(position.legalMoves().isEmpty())
            val result = Engine().analyze(position)
            assertEquals(emptyList(), result.lines, fen)
            assertEquals(0, result.depth)
            assertEquals(0L, result.nodes)
        }
    }

    @Test
    fun multiPvIsCappedByTheNumberOfLegalMoves() {
        // One legal move.
        val forced = analyze("6k1/8/8/8/8/1r6/r7/7K w - - 0 1", 4, multiPv = 3)
        assertEquals(1, forced.lines.size)
        // Two legal moves.
        val two = analyze("4k3/8/4K3/8/8/8/8/R7 b - - 0 1", 4, multiPv = 5)
        assertEquals(2, two.lines.size)
        // Values below one mean one line.
        val position = Position.START
        for (multiPv in intArrayOf(0, -3)) {
            val result = Engine().analyze(position, SearchLimits(maxDepth = 3, timeMs = 60_000, multiPv = multiPv))
            assertEquals(1, result.lines.size)
        }
        // All twenty moves of the start position, each exactly once.
        val all = analyze(Position.START_FEN, 4, multiPv = 50)
        assertEquals(position.legalMoves().toSet(), all.lines.map { it.move }.toSet())
    }

    @Test
    fun sameInputGivesTheSameResultOnAFreshAndOnAUsedEngine() {
        val fen = "r1bq1rk1/pp2ppbp/2np1np1/8/3NP3/2N1BP2/PPPQ2PP/R3KB1R w KQ - 3 9"
        val limits = SearchLimits(maxDepth = 6, timeMs = 60_000, multiPv = 3)
        val used = Engine()
        used.analyze(Position.START, limits)
        used.analyze(Position.fromFen("5rk1/5Npp/8/8/2Q5/8/8/7K w - - 0 1"), limits)
        val a = used.analyze(Position.fromFen(fen), limits)
        val b = Engine().analyze(Position.fromFen(fen), limits)
        assertEquals(b.lines, a.lines)
        assertEquals(b.nodes, a.nodes)
        assertEquals(b.depth, a.depth)
    }

    @Test
    fun startPositionIsRoughlyBalancedAndDevelopsAPiece() {
        val result = analyze(Position.START_FEN, 7)
        val best = result.lines[0]
        assertTrue(kotlin.math.abs(best.scoreCp) < 80, "start position score ${best.scoreCp}")
        assertTrue(best.san in setOf("e4", "d4", "Nf3", "Nc3", "c4", "e3", "d3"), "odd first move ${best.san}")
        assertTrue(best.pv.size >= 5, "a full-length line is expected in a quiet position: ${best.pv}")
    }

    @Test
    fun everyPrincipalVariationIsLegalInRandomPositions() {
        val random = Random(20261111)
        var analysed = 0
        var mates = 0
        var game = 0
        while (analysed < 50) {
            game++
            var position = Position.START
            var ply = 0
            while (analysed < 50) {
                val moves = position.legalMoves()
                if (moves.isEmpty() || position.isInsufficientMaterial() || ply >= 120) break
                position = position.play(moves[random.nextInt(moves.size)])
                ply++
                if (ply % 12 != 6) continue
                if (position.legalMoves().isEmpty()) break

                // Alternate between a fixed depth and a tiny time budget.
                val limits = if (analysed % 2 == 0) {
                    SearchLimits(maxDepth = 5, timeMs = 60_000, multiPv = 3)
                } else {
                    SearchLimits(maxDepth = 32, timeMs = 25, multiPv = 4)
                }
                val result = Engine().analyze(position, limits)
                assertSoundResult(position, result, limits)
                assertTrue(result.depth >= 1, "no completed iteration in ${position.toFen()}")
                if (limits.timeMs > 1000) assertEquals(5, result.depth)
                if (result.lines[0].mateIn != null) mates++
                analysed++
            }
        }
        assertEquals(50, analysed)
        assertTrue(game >= 5, "positions must come from several games (was $game)")
        println("random positions: $analysed analysed from $game games, $mates with a forced mate")
    }

    @Test
    fun implausiblePlacementsAreAnalysedWithoutIllegalMoves() {
        val odd = listOf(
            "8/8/8/3q4/8/3Q4/8/8 w - - 0 1", // no kings
            "k6K/8/8/8/8/8/8/8 w - - 0 1", // bare kings
            "4k3/8/8/8/8/8/8/4KK2 w - - 0 1", // two white kings
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 150 80", // far past the fifty-move rule
            "qqqqkqqq/qqqqqqqq/8/8/8/8/QQQQQQQQ/QQQQKQQQ w - - 0 1", // thirty queens
            "4k3/8/8/8/8/8/8/R3K2R w KQ - 99 60", // one half-move from a fifty-move draw
            "r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1",
            "4k3/4R3/8/8/8/8/8/4K3 w - - 0 1", // the side not to move is in check
        )
        for (fen in odd) {
            val position = Position.fromFen(fen)
            for (limits in listOf(SearchLimits(maxDepth = 3, timeMs = 1_000, multiPv = 3), SearchLimits(timeMs = 30, multiPv = 2))) {
                val result = Engine().analyze(position, limits)
                assertSoundResult(position, result, limits)
            }
        }
    }

    @Test
    fun fiftyMoveRuleAndRepetitionAreDrawsInsideTheSearch() {
        // A rook up, but any quiet move completes the hundredth half-move: only a draw is left
        // unless a pawn moves or something is captured. Here nothing can be, so the score is 0.
        val drawn = analyze("4k3/8/8/8/8/8/8/R3K3 w - - 99 60", 4)
        assertEquals(0, drawn.lines[0].scoreCp)
        // The same position with a fresh counter is simply winning.
        val winning = analyze("4k3/8/8/8/8/8/8/R3K3 w - - 0 60", 4)
        assertTrue(winning.lines[0].scoreCp > 400)
        // With the counter at 99 a pawn move resets it and keeps the advantage.
        val pawn = analyze("4k3/8/8/8/8/8/4P3/R3K3 w - - 99 60", 4)
        assertEquals(PieceType.PAWN, Position.fromFen("4k3/8/8/8/8/8/4P3/R3K3 w - - 99 60").pieceAt(pawn.lines[0].move.from)?.type)
        assertTrue(pawn.lines[0].scoreCp > 400)

        // Perpetual check: two rooks down, White has nothing better than Qe8+ Kh7 Qh5+ Kg8 for ever.
        val perpetual = analyze("6k1/5pp1/8/7Q/8/7P/rr4PK/8 w - - 0 1", 6)
        assertEquals("h5e8", perpetual.lines[0].move.uci())
        assertEquals(0, perpetual.lines[0].scoreCp)
        assertTrue(perpetual.lines[1].scoreCp < -500, "everything else loses: ${perpetual.lines.map { it.san + " " + it.scoreCp }}")
    }
}
