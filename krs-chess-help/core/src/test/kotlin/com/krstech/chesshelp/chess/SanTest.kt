package com.krstech.chesshelp.chess

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SanTest {
    private fun pos(fen: String) = Position.fromFen(fen)

    private fun Position.sanOf(uci: String): String = san(Move.fromUci(uci)!!)

    private fun Position.parsedUci(text: String): String? = parseMove(text)?.uci()

    // ------------------------------------------------------------------ generation

    @Test
    fun simpleMoves() {
        val start = Position.START
        assertEquals("e4", start.sanOf("e2e4"))
        assertEquals("e3", start.sanOf("e2e3"))
        assertEquals("Nf3", start.sanOf("g1f3"))
        assertEquals("Na3", start.sanOf("b1a3"))
        val middle = pos("r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 3")
        assertEquals("Bb5", middle.sanOf("f1b5"))
        assertEquals("Ke2", middle.sanOf("e1e2"))
        assertEquals("Rg1", middle.sanOf("h1g1"))
        assertEquals("Qe2", middle.sanOf("d1e2"))
    }

    @Test
    fun captures() {
        val position = pos("r1bqkbnr/ppp2ppp/2n5/3pp3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq d6 0 4")
        assertEquals("exd5", position.sanOf("e4d5"))
        assertEquals("Nxe5", position.sanOf("f3e5"))
        val black = pos("r1bqkbnr/ppp2ppp/2n5/3pp3/4P3/3P1N2/PPP2PPP/RNBQKB1R b KQkq - 0 4")
        assertEquals("dxe4", black.sanOf("d5e4"))
    }

    @Test
    fun enPassantIsWrittenAsAPlainPawnCapture() {
        val position = pos("rnbqkbnr/1pp1pppp/p7/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3")
        assertEquals("exd6", position.sanOf("e5d6"))
        val black = pos("4k3/8/8/8/2pPp3/8/8/4K3 b - d3 0 1")
        assertEquals("cxd3", black.sanOf("c4d3"))
        assertEquals("exd3", black.sanOf("e4d3"))
    }

    @Test
    fun fileDisambiguation() {
        val position = pos("4k3/8/8/8/8/8/8/1N2KN2 w - - 0 1")
        assertEquals("Nbd2", position.sanOf("b1d2"))
        assertEquals("Nfd2", position.sanOf("f1d2"))
        assertEquals("Na3", position.sanOf("b1a3")) // only one knight reaches a3
        val rooks = pos("4k3/8/8/8/8/8/4K3/R6R w - - 0 1")
        assertEquals("Rad1", rooks.sanOf("a1d1"))
        assertEquals("Rhd1", rooks.sanOf("h1d1"))
        // With the king in between, each rook has its own side of the rank: no disambiguation.
        val separated = pos("4k3/8/8/8/8/8/8/R3K2R w - - 0 1")
        assertEquals("Rd1", separated.sanOf("a1d1"))
        assertEquals("Rf1", separated.sanOf("h1f1"))
    }

    @Test
    fun rankDisambiguation() {
        val position = pos("4k3/R7/8/8/8/8/8/R3K3 w - - 0 1")
        assertEquals("R1a4", position.sanOf("a1a4"))
        assertEquals("R7a4", position.sanOf("a7a4"))
        val knights = pos("4k3/8/8/N7/8/8/8/N3K3 w - - 0 1")
        assertEquals("N1b3", knights.sanOf("a1b3"))
        assertEquals("N5b3", knights.sanOf("a5b3"))
    }

    @Test
    fun fileAndRankDisambiguation() {
        val position = pos("1k6/8/8/8/4Q2Q/8/8/K6Q w - - 0 1")
        assertEquals("Qh4e1", position.sanOf("h4e1"))
        assertEquals("Qee1", position.sanOf("e4e1"))
        assertEquals("Q1e1", position.sanOf("h1e1"))
        // File is preferred over rank when either would do.
        val knights = pos("4k3/8/8/8/8/2N5/8/4K1N1 w - - 0 1")
        assertEquals("Nce2", knights.sanOf("c3e2"))
        assertEquals("Nge2", knights.sanOf("g1e2"))
    }

    @Test
    fun pinnedPiecesDoNotCauseDisambiguation() {
        // The e2 knight is pinned by the rook, so only the b1 knight can go to c3.
        val position = pos("4r1k1/8/8/8/8/8/4N3/1N2K3 w - - 0 1")
        assertEquals("Nc3", position.sanOf("b1c3"))
    }

    @Test
    fun promotions() {
        val position = pos("1n5k/P7/8/8/8/8/8/K7 w - - 0 1")
        assertEquals("a8=Q", position.sanOf("a7a8q")) // the b8 knight shields the king
        assertEquals("a8=N", position.sanOf("a7a8n"))
        assertEquals("axb8=Q+", position.sanOf("a7b8q"))
        assertEquals("axb8=R+", position.sanOf("a7b8r"))
        assertEquals("axb8=B", position.sanOf("a7b8b"))
        assertEquals("axb8=N", position.sanOf("a7b8n"))
        val black = pos("k7/8/8/8/8/8/p7/1N5K b - - 0 1")
        assertEquals("a1=Q", black.sanOf("a2a1q"))
        assertEquals("axb1=Q+", black.sanOf("a2b1q"))
        assertEquals("axb1=N", black.sanOf("a2b1n"))
    }

    @Test
    fun promotionWithMate() {
        val position = pos("6k1/3P1ppp/8/8/8/8/8/6K1 w - - 0 1")
        assertEquals("d8=Q#", position.sanOf("d7d8q"))
        assertEquals("d8=R#", position.sanOf("d7d8r"))
        assertEquals("d8=B", position.sanOf("d7d8b"))
        assertEquals("d8=N", position.sanOf("d7d8n"))
    }

    @Test
    fun castling() {
        val position = pos("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")
        assertEquals("O-O", position.sanOf("e1g1"))
        assertEquals("O-O-O", position.sanOf("e1c1"))
        val black = position.withSideToMove(Side.BLACK)
        assertEquals("O-O", black.sanOf("e8g8"))
        assertEquals("O-O-O", black.sanOf("e8c8"))
        // Castling can give check (and mate) with the rook.
        assertEquals("O-O+", pos("5k2/8/8/8/8/8/8/4K2R w K - 0 1").sanOf("e1g1"))
        assertEquals("O-O-O+", pos("3k4/8/8/8/8/8/8/R3K3 w Q - 0 1").sanOf("e1c1"))
        assertEquals("O-O#", pos("4rkr1/4p1p1/8/8/8/8/8/4K2R w K - 0 1").sanOf("e1g1"))
    }

    @Test
    fun checkAndMateSuffixes() {
        assertEquals("Qh4#", pos("rnbqkbnr/pppp1ppp/8/4p3/6P1/5P2/PPPPP2P/RNBQKBNR b KQkq g3 0 2").sanOf("d8h4"))
        assertEquals("Bb5+", pos("rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 2").sanOf("f1b5"))
        // Discovered check: the moving piece is not the one giving check.
        assertEquals("Nc3+", pos("4k3/8/8/8/8/8/4N3/4RK2 w - - 0 1").sanOf("e2c3"))
        // Double check.
        assertEquals("Nf6+", pos("4k3/8/8/8/4N3/8/8/4RK2 w - - 0 1").sanOf("e4f6"))
        assertEquals("Rd8#", pos("6k1/5ppp/8/8/8/8/8/3R2K1 w - - 0 1").sanOf("d1d8"))
    }

    @Test
    fun sanOfAnIllegalMoveIsRejected() {
        assertFailsWith<IllegalArgumentException> { Position.START.sanOf("e2e5") }
        assertFailsWith<IllegalArgumentException> { Position.START.san(Move(12, 28, PieceType.QUEEN)) }
    }

    /** Morphy v Duke of Brunswick and Count Isouard, Paris 1858: the score exactly as published. */
    @Test
    fun operaGameIsReproducedMoveByMove() {
        val score = (
            "e4 e5 Nf3 d6 d4 Bg4 dxe5 Bxf3 Qxf3 dxe5 Bc4 Nf6 Qb3 Qe7 Nc3 c6 Bg5 b5 Nxb5 cxb5 " +
                "Bxb5+ Nbd7 O-O-O Rd8 Rxd7 Rxd7 Rd1 Qe6 Bxd7+ Nxd7 Qb8+ Nxb8 Rd8#"
            ).split(' ')
        var position = Position.START
        for (san in score) {
            val move = assertNotNull(position.parseMove(san), "cannot parse $san in ${position.toFen()}")
            assertEquals(san, position.san(move), "SAN in ${position.toFen()}")
            position = position.play(move)
        }
        assertEquals("1n1Rkb1r/p4ppp/4q3/4p1B1/4P3/8/PPP2PPP/2K5 b k - 1 17", position.toFen())
        assertTrue(position.isCheckmate())
    }

    /** A line with en passant, under-promotion and both castlings, checked against hand-written SAN. */
    @Test
    fun scoreWithSpecialMoves() {
        val score = (
            "e4 Nf6 e5 d5 exd6 Nc6 dxc7 Bg4 cxd8=N Rxd8 Nf3 e6 Be2 Be7 O-O O-O"
            ).split(' ')
        var position = Position.START
        for (san in score) {
            val move = assertNotNull(position.parseMove(san), "cannot parse $san in ${position.toFen()}")
            assertEquals(san, position.san(move), "SAN in ${position.toFen()}")
            position = position.play(move)
        }
        assertEquals("3r1rk1/pp2bppp/2n1pn2/8/6b1/5N2/PPPPBPPP/RNBQ1RK1 w - - 4 9", position.toFen())
    }

    // ------------------------------------------------------------------ parsing

    @Test
    fun parsesStandardSan() {
        val start = Position.START
        assertEquals("e2e4", start.parsedUci("e4"))
        assertEquals("g1f3", start.parsedUci("Nf3"))
        assertEquals("b1c3", start.parsedUci("Nc3"))
        val position = pos("r1bqkbnr/ppp2ppp/2n5/3pp3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq d6 0 4")
        assertEquals("e4d5", position.parsedUci("exd5"))
        assertEquals("f3e5", position.parsedUci("Nxe5"))
        assertEquals("f1b5", position.parsedUci("Bb5"))
    }

    @Test
    fun parsesLenientForms() {
        val position = pos("r1bqkbnr/ppp2ppp/2n5/3pp3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq d6 0 4")
        // Missing or superfluous capture / check marks and annotations.
        assertEquals("e4d5", position.parsedUci("ed5"))
        assertEquals("e4d5", position.parsedUci("e4xd5"))
        assertEquals("e4d5", position.parsedUci("exd5!"))
        assertEquals("e4d5", position.parsedUci("exd5+"))
        assertEquals("f3e5", position.parsedUci("Ne5"))
        assertEquals("f3e5", position.parsedUci("Nxe5!?"))
        assertEquals("f3e5", position.parsedUci("Nxe5#"))
        assertEquals("f3e5", position.parsedUci(" Nxe5 "))
        assertEquals("f3e5", position.parsedUci("N:e5"))
        assertEquals("f1b5", position.parsedUci("Bb5+"))
        assertEquals("f1b5", position.parsedUci("Bb5")) // the check mark is optional
        // Over-specified origin and long algebraic notation.
        assertEquals("f3e5", position.parsedUci("Nfxe5"))
        assertEquals("f3e5", position.parsedUci("Nf3xe5"))
        assertEquals("f3e5", position.parsedUci("Nf3-e5"))
        assertEquals("f3e5", position.parsedUci("N3e5"))
        assertEquals("d2d4", position.parsedUci("d2-d4"))
        // Move numbers and trailing punctuation.
        assertEquals("d2d4", position.parsedUci("4.d4"))
        assertEquals("d2d4", position.parsedUci("4. d4"))
        assertEquals("d2d4", position.parsedUci("d4."))
        assertEquals("e7e5", Position.START.play(Move.fromUci("e2e4")!!).parsedUci("1...e5"))
        assertEquals("e7e5", Position.START.play(Move.fromUci("e2e4")!!).parsedUci("1... e5"))
        // Lower-case piece letters where that cannot be mistaken for a pawn move.
        assertEquals("f3e5", position.parsedUci("nxe5"))
        assertEquals("d1e2", position.parsedUci("qe2"))
        assertEquals("e1e2", position.parsedUci("ke2"))
        assertEquals("f1b5", position.parsedUci("bb5"))
        assertEquals("b2b4", position.parsedUci("b4")) // ... but a pawn move stays a pawn move
    }

    @Test
    fun parsesCastlingInAllSpellings() {
        val position = pos("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")
        for (text in listOf("O-O", "0-0", "o-o", "OO", "00", "O-O+", "O-O!", "e1g1", "Kg1")) {
            assertEquals("e1g1", position.parsedUci(text), text)
        }
        for (text in listOf("O-O-O", "0-0-0", "o-o-o", "OOO", "000", "O-O-O#", "e1c1", "Kc1")) {
            assertEquals("e1c1", position.parsedUci(text), text)
        }
        val black = position.withSideToMove(Side.BLACK)
        assertEquals("e8g8", black.parsedUci("O-O"))
        assertEquals("e8c8", black.parsedUci("0-0-0"))
        // No castling available: null rather than a guess.
        assertNull(Position.START.parseMove("O-O"))
        assertNull(pos("r3k2r/8/8/8/8/8/8/R3K2R w kq - 0 1").parseMove("O-O-O"))
    }

    @Test
    fun parsesPromotionsInAllSpellings() {
        val position = pos("1n5k/P7/8/8/8/8/8/K7 w - - 0 1")
        for (text in listOf("a8=Q", "a8Q", "a8q", "a8=Q+", "a8(Q)", "a8/Q", "a7a8q", "a7a8Q", "a7-a8=Q", "a8=Q!")) {
            assertEquals("a7a8q", position.parsedUci(text), text)
        }
        assertEquals("a7a8n", position.parsedUci("a8=N"))
        assertEquals("a7a8r", position.parsedUci("a8R"))
        assertEquals("a7a8b", position.parsedUci("a8=B"))
        assertEquals("a7a8b", position.parsedUci("a8b"))
        for (text in listOf("axb8=Q", "axb8Q", "ab8=Q", "ab8Q", "a7b8q", "a7xb8=Q", "axb8=Q+")) {
            assertEquals("a7b8q", position.parsedUci(text), text)
        }
        assertEquals("a7b8n", position.parsedUci("axb8=N"))
        // The promotion piece is required, and kings / pawns are not valid choices.
        assertNull(position.parseMove("a8"))
        assertNull(position.parseMove("a7a8"))
        assertNull(position.parseMove("axb8"))
        assertNull(position.parseMove("a8=K"))
        assertNull(position.parseMove("a8=P"))
        assertNull(position.parseMove("a7a8k"))
    }

    @Test
    fun parsesEnPassant() {
        val position = pos("rnbqkbnr/1pp1pppp/p7/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3")
        for (text in listOf("exd6", "ed6", "exd6 e.p.", "exd6e.p.", "exd6 ep", "e5d6", "e5xd6", "exd6!")) {
            assertEquals("e5d6", position.parsedUci(text), text)
        }
        // "d6" alone is not the capture (and no pawn can push there).
        assertNull(position.parseMove("d6"))
    }

    @Test
    fun parsesUci() {
        val start = Position.START
        assertEquals("e2e4", start.parsedUci("e2e4"))
        assertEquals("g1f3", start.parsedUci("g1f3"))
        assertEquals("e2e4", start.parsedUci("E2E4"))
        assertNull(start.parseMove("e2e5"))
        assertNull(start.parseMove("e7e5"))
        assertNull(start.parseMove("e1g1"))
    }

    @Test
    fun upperCaseBishopIsNotConfusedWithUciFromTheBFile() {
        // Bishops on d1 and f1 can both go to e2; on b1 stands a queen that could also reach c2.
        val position = pos("4k3/8/8/8/8/8/8/1Q1BKB2 w - - 0 1")
        assertEquals("b1c2", position.parsedUci("b1c2"))
        assertEquals("d1c2", position.parsedUci("B1c2")) // "bishop on rank 1 to c2" is unique here (f1 cannot reach c2)
        assertEquals("d1e2", position.parsedUci("Bde2"))
        assertEquals("f1e2", position.parsedUci("Bfe2"))
        assertNull(position.parseMove("Be2"))
    }

    @Test
    fun ambiguousOrIllegalOrGarbageInputGivesNull() {
        val knights = pos("4k3/8/8/8/8/8/8/1N2KN2 w - - 0 1")
        assertNull(knights.parseMove("Nd2")) // two knights can go there
        assertEquals("b1d2", knights.parsedUci("Nbd2"))
        assertEquals("f1d2", knights.parsedUci("Nfd2"))
        assertNull(knights.parseMove("Ncd2")) // no knight on the c-file
        val rooks = pos("4k3/R7/8/8/8/8/8/R3K3 w - - 0 1")
        assertNull(rooks.parseMove("Ra4"))
        assertNull(rooks.parseMove("Raa4")) // the file does not help
        assertEquals("a1a4", rooks.parsedUci("R1a4"))

        val start = Position.START
        for (text in listOf(
            "", " ", "hello", "x", "e5", "Ke3", "Nf6", "Qd4", "e9", "i4", "Nf3 Nc6", "e4 e5", "O-O-O-O",
            "--", "1.", "...", "e2e4e5", "Pe4x", "=Q", "e8=Q", "Zf3", "42",
        )) {
            assertNull(start.parseMove(text), "\"$text\"")
        }
        // A pinned piece's move is illegal, so it is not offered even if it is the only candidate.
        assertNull(pos("4r1k1/8/8/8/8/8/4N3/4K3 w - - 0 1").parseMove("Nc3"))
    }

    @Test
    fun pawnPushVersusPawnCapture() {
        // d5 can be reached by the d-pawn (push) and by the e-pawn (capture): "d5" means the push.
        val position = pos("4k3/8/8/3p4/3PP3/8/8/4K3 w - - 0 1")
        assertEquals("e4d5", position.parsedUci("exd5"))
        assertEquals("e4e5", position.parsedUci("e5"))
        assertNull(position.parseMove("d5")) // the d4 pawn is blocked; the capture must name its file
        val black = pos("4k3/8/8/3pp3/4P3/8/8/4K3 b - - 0 1")
        assertEquals("d5d4", black.parsedUci("d4"))
        assertEquals("d5e4", black.parsedUci("dxe4"))
        assertEquals("d5e4", black.parsedUci("de4"))
    }

    // ------------------------------------------------------------------ properties

    @Test
    fun sanRoundTripsForEveryLegalMoveAlongRandomGames() {
        val random = Random(2024)
        val starts = listOf(
            Position.START_FEN,
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
            "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
            "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8",
            "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
            "1k6/8/8/8/4Q2Q/8/8/K6Q w - - 0 1",
            "n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1",
        )
        var checkedMoves = 0
        for (fen in starts) {
            repeat(12) {
                var position = pos(fen)
                var plies = 0
                while (plies < 60) {
                    val moves = position.legalMoves()
                    if (moves.isEmpty()) break
                    val sans = HashSet<String>()
                    for (move in moves) {
                        val san = position.san(move)
                        assertTrue(sans.add(san), "duplicate SAN $san in ${position.toFen()}")
                        assertEquals(move, position.parseMove(san), "$san in ${position.toFen()}")
                        assertEquals(move, position.parseMove(move.uci()), "${move.uci()} in ${position.toFen()}")
                        // Check marks agree with the resulting position.
                        val next = position.play(move)
                        assertEquals(next.isCheckmate(), san.endsWith("#"), "$san in ${position.toFen()}")
                        assertEquals(next.isInCheck() && !next.isCheckmate(), san.endsWith("+"), "$san in ${position.toFen()}")
                        checkedMoves++
                    }
                    position = position.play(moves[random.nextInt(moves.size)])
                    plies++
                }
            }
        }
        assertTrue(checkedMoves > 20_000, "only $checkedMoves moves checked")
    }
}
