package com.krstech.chesshelp.chess

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MoveTrackerTest {
    private fun pos(fen: String) = Position.fromFen(fen)

    private fun Position.playUci(vararg moves: String): Position =
        moves.fold(this) { position, uci -> position.play(Move.fromUci(uci)!!) }

    /** What the camera would see after the given moves. */
    private fun Position.occupancyAfter(vararg moves: String): Array<Occupant> = MoveTracker.occupancyOf(playUci(*moves))

    private fun played(position: Position, observed: Array<Occupant>, maxPlies: Int = 2): MoveInference.Played =
        assertIs<MoveInference.Played>(MoveTracker.infer(position, observed, maxPlies))

    @Test
    fun occupancyOfStartPosition() {
        val occupancy = MoveTracker.occupancyOf(Position.START)
        assertEquals(64, occupancy.size)
        for (sq in 0..63) {
            val expected = when (Square.rank(sq)) {
                0, 1 -> Occupant.WHITE
                6, 7 -> Occupant.BLACK
                else -> Occupant.EMPTY
            }
            assertEquals(expected, occupancy[sq], Square.name(sq))
        }
    }

    @Test
    fun noChange() {
        assertEquals(MoveInference.NoChange, MoveTracker.infer(Position.START, MoveTracker.occupancyOf(Position.START)))
        val position = pos("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1")
        assertEquals(MoveInference.NoChange, MoveTracker.infer(position, MoveTracker.occupancyOf(position)))
        assertEquals(MoveInference.NoChange, MoveTracker.infer(position, MoveTracker.occupancyOf(position), maxPlies = 0))
    }

    @Test
    fun quietMove() {
        val result = played(Position.START, Position.START.occupancyAfter("g1f3"))
        assertEquals(listOf(Move.fromUci("g1f3")), result.moves)
        assertEquals(listOf("Nf3"), result.sans)
        assertEquals(Position.START.playUci("g1f3"), result.position)
        assertFalse(result.promotionAssumed)

        val pawn = played(Position.START, Position.START.occupancyAfter("e2e4"))
        assertEquals(listOf("e4"), pawn.sans)
        assertEquals("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", pawn.position.toFen())
    }

    @Test
    fun capture() {
        val position = pos("r1bqkbnr/ppp2ppp/2n5/3pp3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 0 4")
        val result = played(position, position.occupancyAfter("f3e5"))
        assertEquals(listOf("Nxe5"), result.sans)
        assertEquals(listOf(Move.fromUci("f3e5")), result.moves)
        assertEquals(position.playUci("f3e5"), result.position)
        // The pawn capture exd5 is told apart from it by the squares involved.
        assertEquals(listOf("exd5"), played(position, position.occupancyAfter("e4d5")).sans)
    }

    @Test
    fun castlingBothSides() {
        val position = pos("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1")
        val short = played(position, position.occupancyAfter("e1g1"))
        assertEquals(listOf("O-O"), short.sans)
        assertEquals(listOf(Move.fromUci("e1g1")), short.moves)
        assertEquals("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R4RK1 b kq - 1 1", short.position.toFen())

        val long = played(position, position.occupancyAfter("e1c1"))
        assertEquals(listOf("O-O-O"), long.sans)
        assertEquals("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/2KR3R b kq - 1 1", long.position.toFen())

        val black = position.withSideToMove(Side.BLACK)
        assertEquals(listOf("O-O"), played(black, black.occupancyAfter("e8g8")).sans)
        assertEquals(listOf("O-O-O"), played(black, black.occupancyAfter("e8c8")).sans)

        // Castling is not confused with the rook or king moves that touch some of the same squares.
        assertEquals(listOf("Rf1"), played(position, position.occupancyAfter("h1f1")).sans)
        assertEquals(listOf("Kf1"), played(position, position.occupancyAfter("e1f1")).sans)
        assertEquals(listOf("Kd1"), played(position, position.occupancyAfter("e1d1")).sans)
    }

    @Test
    fun enPassant() {
        val position = pos("rnbqkbnr/1pp1pppp/p7/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3")
        val result = played(position, position.occupancyAfter("e5d6"))
        assertEquals(listOf("exd6"), result.sans)
        assertEquals("rnbqkbnr/1pp1pppp/p2P4/8/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 3", result.position.toFen())
        assertFalse(result.promotionAssumed)
        // The plain push next to it is a different observation.
        assertEquals(listOf("e6"), played(position, position.occupancyAfter("e5e6")).sans)
    }

    @Test
    fun promotionIsAssumedToBeAQueen() {
        val position = pos("1n5k/P7/8/8/8/8/8/K7 w - - 0 1")
        for (uci in listOf("a7a8q", "a7a8r", "a7a8b", "a7a8n")) {
            val result = played(position, position.occupancyAfter(uci))
            assertEquals(listOf(Move.fromUci("a7a8q")), result.moves, "observed after $uci")
            assertEquals(listOf("a8=Q"), result.sans)
            assertTrue(result.promotionAssumed)
            assertEquals("Qn5k/8/8/8/8/8/8/K7 b - - 0 1", result.position.toFen())
        }
        val capture = played(position, position.occupancyAfter("a7b8n"))
        assertEquals(listOf("axb8=Q+"), capture.sans)
        assertTrue(capture.promotionAssumed)

        // Black promotions too.
        val black = pos("k7/8/8/8/8/8/p7/1N5K b - - 0 1")
        val result = played(black, black.occupancyAfter("a2a1r"))
        assertEquals(listOf(Move.fromUci("a2a1q")), result.moves)
        assertTrue(result.promotionAssumed)
    }

    @Test
    fun twoPlySequence() {
        // The watcher missed a frame: our move and the opponent's reply are both on the board.
        val result = played(Position.START, Position.START.occupancyAfter("e2e4", "e7e5"))
        assertEquals(listOf(Move.fromUci("e2e4"), Move.fromUci("e7e5")), result.moves)
        assertEquals(listOf("e4", "e5"), result.sans)
        assertEquals("rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2", result.position.toFen())
        assertFalse(result.promotionAssumed)

        // Move and recapture on the same square.
        val position = pos("r1bqkbnr/ppp2ppp/2n5/3pp3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 0 4")
        val exchange = played(position, position.occupancyAfter("f3e5", "c6e5"))
        assertEquals(listOf("Nxe5", "Nxe5"), exchange.sans)
        assertEquals(position.playUci("f3e5", "c6e5"), exchange.position)

        // With only one ply allowed the same observation cannot be explained.
        assertEquals(
            MoveInference.NoMatch,
            MoveTracker.infer(Position.START, Position.START.occupancyAfter("e2e4", "e7e5"), maxPlies = 1),
        )
    }

    @Test
    fun onePlyIsPreferredOverTwo() {
        // After 1.e4 the occupancy is explained by one move; no reply is invented.
        val result = played(Position.START, Position.START.occupancyAfter("e2e4"), maxPlies = 3)
        assertEquals(1, result.moves.size)
    }

    @Test
    fun twoPlyPromotionKeepsTheVariantThatFitsTheReply() {
        // After a8=Q+ Black would be in check and Kg7 legal; all four promotions allow ...Kg7,
        // so the queen is assumed.
        val position = pos("7k/P7/8/8/8/8/8/K7 w - - 0 1")
        val assumed = played(position, position.occupancyAfter("a7a8r", "h8g7"))
        assertEquals(listOf("a8=Q+", "Kg7"), assumed.sans)
        assertTrue(assumed.promotionAssumed)

        // Here Black answers ...h6: illegal after a8=Q+ or a8=R+ (check along the rank), so only
        // the bishop or knight promotion fits and the "bigger" one of those is taken.
        val quiet = pos("7k/P6p/8/8/8/8/8/K7 w - - 0 1")
        val deduced = played(quiet, quiet.occupancyAfter("a7a8n", "h7h6"))
        assertEquals(listOf(Move.fromUci("a7a8b"), Move.fromUci("h7h6")), deduced.moves)
        assertTrue(deduced.promotionAssumed)
    }

    @Test
    fun differentMoveOrdersWithTheSameOutcomeAreNotAmbiguous() {
        // e2-e3 dxe3 and e2-e4 dxe3 e.p. leave exactly the same placement.
        val position = pos("4k3/8/8/8/3p4/8/4P3/4K3 w - - 0 1")
        val viaEnPassant = position.playUci("e2e4", "d4e3")
        val viaPlainCapture = position.playUci("e2e3", "d4e3")
        assertEquals(viaEnPassant.placement(), viaPlainCapture.placement())
        val result = played(position, MoveTracker.occupancyOf(viaEnPassant))
        assertEquals(viaEnPassant.placement(), result.position.placement())
        assertEquals(2, result.moves.size)
        assertEquals("d4e3", result.moves[1].uci())
    }

    @Test
    fun ambiguousCaptureAndRecapture() {
        // The f3 knight takes on d4 or e5 and the c6 knight takes back on the same square: both
        // origins are empty afterwards and both targets were black before and still are.
        val position = pos("4k3/8/2n5/4p3/3p4/5N2/8/4K3 w - - 0 1")
        val onE5 = position.playUci("f3e5", "c6e5")
        val onD4 = position.playUci("f3d4", "c6d4")
        assertContentEquals(MoveTracker.occupancyOf(onE5), MoveTracker.occupancyOf(onD4))
        assertEquals(MoveInference.Ambiguous(2), MoveTracker.infer(position, MoveTracker.occupancyOf(onE5)))
        assertEquals(MoveInference.Ambiguous(2), MoveTracker.infer(position, MoveTracker.occupancyOf(onD4)))
        // Each half on its own is perfectly clear.
        assertEquals(listOf("Nxe5"), played(position, position.occupancyAfter("f3e5")).sans)
        val afterCapture = position.playUci("f3e5")
        assertEquals(listOf("Nxe5"), played(afterCapture, MoveTracker.occupancyOf(onE5)).sans)
    }

    @Test
    fun ambiguousObservationOverThreePlies() {
        // Three plies (we, they, we again): king and queen swap targets, d2/e2 end up white either
        // way, but which piece stands where cannot be known from occupancy.
        val position = pos("7k/8/8/8/8/8/8/3QK3 w - - 0 1")
        val oneWay = position.playUci("e1d2", "h8g8", "d1e2")
        val otherWay = position.playUci("e1e2", "h8g8", "d1d2")
        assertContentEquals(MoveTracker.occupancyOf(oneWay), MoveTracker.occupancyOf(otherWay))
        assertEquals(MoveInference.Ambiguous(2), MoveTracker.infer(position, MoveTracker.occupancyOf(oneWay), maxPlies = 3))
        // Within the default two plies there is no explanation at all.
        assertEquals(MoveInference.NoMatch, MoveTracker.infer(position, MoveTracker.occupancyOf(oneWay)))
    }

    @Test
    fun noMatchForGarbage() {
        val random = Random(99)
        repeat(50) {
            val garbage = Array(64) { Occupant.entries[random.nextInt(3)] }
            assertEquals(MoveInference.NoMatch, MoveTracker.infer(Position.START, garbage))
        }
        // An empty board, a board with one piece teleported, and an illegal move.
        assertEquals(MoveInference.NoMatch, MoveTracker.infer(Position.START, Array(64) { Occupant.EMPTY }))
        val teleported = MoveTracker.occupancyOf(Position.START)
        teleported[Square.parse("a2")] = Occupant.EMPTY
        teleported[Square.parse("h5")] = Occupant.WHITE
        assertEquals(MoveInference.NoMatch, MoveTracker.infer(Position.START, teleported))
        val illegal = MoveTracker.occupancyOf(Position.START) // "Ke2" with the pawn vanishing
        illegal[Square.parse("e1")] = Occupant.EMPTY
        assertEquals(MoveInference.NoMatch, MoveTracker.infer(Position.START, illegal))
        // A move by the side that is not to move is not explained by a single ply ...
        val blackMoved = Position.START.withSideToMove(Side.BLACK).occupancyAfter("e7e5")
        assertEquals(MoveInference.NoMatch, MoveTracker.infer(Position.START, blackMoved, maxPlies = 1))
        // ... nor by two plies, since White would have had to move as well.
        assertEquals(MoveInference.NoMatch, MoveTracker.infer(Position.START, blackMoved))
    }

    @Test
    fun wrongArraySizeIsRejected() {
        assertFailsWith<IllegalArgumentException> { MoveTracker.infer(Position.START, Array(63) { Occupant.EMPTY }) }
    }

    @Test
    fun everySingleMoveOfRandomGamesIsRecovered() {
        val random = Random(5)
        val starts = listOf(
            Position.START_FEN,
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
            "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8",
            "n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1",
        )
        var checked = 0
        for (fen in starts) {
            repeat(6) {
                var position = pos(fen)
                repeat(40) {
                    val moves = position.legalMoves()
                    if (moves.isEmpty()) return@repeat
                    for (move in moves) {
                        val next = position.play(move)
                        val result = played(position, MoveTracker.occupancyOf(next))
                        assertEquals(1, result.moves.size)
                        if (move.promotion == null) {
                            assertEquals(move, result.moves[0])
                            assertEquals(next, result.position)
                            assertFalse(result.promotionAssumed)
                        } else {
                            assertEquals(Move(move.from, move.to, PieceType.QUEEN), result.moves[0])
                            assertTrue(result.promotionAssumed)
                        }
                        assertEquals(listOf(position.san(result.moves[0])), result.sans)
                        checked++
                    }
                    position = position.play(moves[random.nextInt(moves.size)])
                }
            }
        }
        assertTrue(checked > 5_000, "only $checked moves checked")
    }

    /**
     * Differential test of the two-ply search against a brute force over the public API: every
     * (move, reply) pair is played out and the placements whose occupancy matches are collected.
     */
    @Test
    fun randomTwoPlySequencesAgreeWithBruteForce() {
        val random = Random(11)
        val starts = listOf(
            Position.START_FEN,
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
            "n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1",
        )
        var checked = 0
        var ambiguous = 0
        for (fen in starts) {
            repeat(15) {
                var position = pos(fen)
                repeat(25) {
                    val first = position.legalMoves()
                    if (first.isEmpty()) return@repeat
                    val afterFirst = position.play(first[random.nextInt(first.size)])
                    val second = afterFirst.legalMoves()
                    if (second.isEmpty()) return@repeat
                    val afterSecond = afterFirst.play(second[random.nextInt(second.size)])
                    val observed = MoveTracker.occupancyOf(afterSecond)

                    val placements = HashSet<String>()
                    var promotionInvolved = false
                    for (m1 in position.legalMoves()) {
                        val p1 = position.play(m1)
                        for (m2 in p1.legalMoves()) {
                            val p2 = p1.play(m2)
                            if (MoveTracker.occupancyOf(p2).contentEquals(observed)) {
                                placements += p2.placement()
                                if (m1.promotion != null || m2.promotion != null) promotionInvolved = true
                            }
                        }
                    }
                    assertTrue(afterSecond.placement() in placements)

                    when (val result = MoveTracker.infer(position, observed)) {
                        is MoveInference.Played -> {
                            assertEquals(2, result.moves.size)
                            assertTrue(result.position.placement() in placements)
                            assertEquals(position.play(result.moves[0]).play(result.moves[1]), result.position)
                            assertEquals(promotionInvolved, result.promotionAssumed || result.moves.any { it.promotion != null })
                            if (!promotionInvolved) {
                                assertEquals(1, placements.size, "a unique outcome was reported for ${position.toFen()}")
                                assertEquals(afterSecond.placement(), result.position.placement())
                            }
                        }
                        is MoveInference.Ambiguous -> {
                            ambiguous++
                            assertTrue(placements.size >= 2, "reported ambiguous for ${position.toFen()}")
                            assertTrue(result.candidates in 2..placements.size)
                            if (!promotionInvolved) assertEquals(placements.size, result.candidates)
                        }
                        else -> throw AssertionError("unexpected $result for ${position.toFen()}")
                    }
                    checked++
                    position = afterSecond
                }
            }
        }
        assertTrue(checked > 500, "only $checked sequences checked")
        println("two-ply inference: $checked sequences checked, $ambiguous genuinely ambiguous")
    }
}
