package com.krstech.chesshelp.chess

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** Tests of the internal board API that the engine builds on. */
class BoardTest {
    private fun board(fen: String): Board = Position.fromFen(fen).toBoard()

    private fun Board.playUci(vararg moves: String) {
        for (uci in moves) make(find(uci))
    }

    private fun Board.find(uci: String): Int {
        val buf = IntArray(Board.MAX_MOVES)
        val n = generateMoves(buf)
        return buf.take(n).single { Moves.uci(it) == uci }
    }

    @Test
    fun internalNumberingMatchesThePublicEnums() {
        // Board and Position convert between the two by ordinal.
        assertEquals(Board.WHITE, Side.WHITE.ordinal)
        assertEquals(Board.BLACK, Side.BLACK.ordinal)
        assertEquals(Board.PAWN, PieceType.PAWN.ordinal)
        assertEquals(Board.KNIGHT, PieceType.KNIGHT.ordinal)
        assertEquals(Board.BISHOP, PieceType.BISHOP.ordinal)
        assertEquals(Board.ROOK, PieceType.ROOK.ordinal)
        assertEquals(Board.QUEEN, PieceType.QUEEN.ordinal)
        assertEquals(Board.KING, PieceType.KING.ordinal)
        val board = board(Position.START_FEN)
        for (sq in 0..63) {
            val piece = Position.START.pieceAt(sq)
            val code = board.pieceAt(sq)
            if (piece == null) {
                assertEquals(Board.NO_PIECE, code)
            } else {
                assertEquals(piece.side.ordinal, Board.sideOf(code))
                assertEquals(piece.type.ordinal, Board.typeOf(code))
                assertEquals(code, Board.pieceCode(piece.side.ordinal, piece.type.ordinal))
            }
        }
    }

    @Test
    fun moveEncodingRoundTrips() {
        val m = Moves.encode(52, 61, Board.PAWN, Board.ROOK, Board.QUEEN, Moves.KIND_NORMAL)
        assertEquals(52, Moves.from(m))
        assertEquals(61, Moves.to(m))
        assertEquals(Board.PAWN, Moves.piece(m))
        assertEquals(Board.ROOK, Moves.captured(m))
        assertEquals(Board.QUEEN, Moves.promotion(m))
        assertEquals(Moves.KIND_NORMAL, Moves.kind(m))
        assertTrue(Moves.isCapture(m))
        assertTrue(Moves.isPromotion(m))
        assertEquals("e7f8q", Moves.uci(m))
        assertEquals(Move(52, 61, PieceType.QUEEN), Moves.toMove(m))

        val quiet = Moves.encode(6, 21, Board.KNIGHT, -1, 0, Moves.KIND_NORMAL)
        assertEquals(-1, Moves.captured(quiet))
        assertEquals(0, Moves.promotion(quiet))
        assertFalse(Moves.isCapture(quiet))
        assertFalse(Moves.isPromotion(quiet))
        assertNotEquals(Moves.NONE, quiet)

        val ep = Moves.encode(36, 43, Board.PAWN, Board.PAWN, 0, Moves.KIND_EN_PASSANT)
        assertTrue(Moves.isEnPassant(ep) && Moves.isCapture(ep) && !Moves.isCastle(ep))
        val castle = Moves.encode(4, 6, Board.KING, -1, 0, Moves.KIND_CASTLE)
        assertTrue(Moves.isCastle(castle) && !Moves.isCapture(castle))
        // Every square pair survives the encoding, and a real move is never NONE.
        for (from in 0..63) for (to in 0..63) {
            if (from == to) continue
            val e = Moves.encode(from, to, Board.KING, -1, 0, Moves.KIND_NORMAL)
            assertEquals(from, Moves.from(e))
            assertEquals(to, Moves.to(e))
            assertNotEquals(Moves.NONE, e)
        }
    }

    @Test
    fun generatedMovesCarryPieceAndCaptureInformation() {
        val board = board("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1")
        val buf = IntArray(Board.MAX_MOVES)
        val n = board.generateMoves(buf)
        assertEquals(48, n)
        for (i in 0 until n) {
            val m = buf[i]
            val mover = board.pieceAt(Moves.from(m))
            assertEquals(Board.WHITE, Board.sideOf(mover))
            assertEquals(Board.typeOf(mover), Moves.piece(m))
            val target = board.pieceAt(Moves.to(m))
            if (target == Board.NO_PIECE) {
                assertEquals(-1, Moves.captured(m))
            } else {
                assertEquals(Board.BLACK, Board.sideOf(target))
                assertEquals(Board.typeOf(target), Moves.captured(m))
            }
        }
        val names = buf.take(n).map { Moves.uci(it) }.toSet()
        assertTrue(names.containsAll(listOf("e1g1", "e1c1", "e5f7", "f3f6", "d5e6", "g2h3")))
        assertEquals(Moves.KIND_CASTLE, Moves.kind(board.find("e1g1")))
        assertEquals(Moves.KIND_DOUBLE_PUSH, Moves.kind(board.find("a2a4")))
        assertEquals(Moves.KIND_NORMAL, Moves.kind(board.find("a2a3")))
        assertEquals(Board.KNIGHT, Moves.captured(board.find("f3f6")))

        val epBoard = board("rnbqkbnr/1pp1pppp/p7/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3")
        val ep = epBoard.find("e5d6")
        assertTrue(Moves.isEnPassant(ep))
        assertEquals(Board.PAWN, Moves.captured(ep))
    }

    @Test
    fun capturesOnlyGeneration() {
        val board = board("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1")
        val buf = IntArray(Board.MAX_MOVES)
        val n = board.generateCaptures(buf)
        assertEquals(
            setOf("g2h3", "d5e6", "e5d7", "e5f7", "e5g6", "e2a6", "f3f6", "f3h3"),
            buf.take(n).map { Moves.uci(it) }.toSet(),
        )
        // Promotions count as noisy even without a capture; quiet pawn pushes do not.
        val promo = board("1n5k/P7/8/8/8/8/1P6/K7 w - - 0 1")
        val m = promo.generateCaptures(buf)
        assertEquals(
            setOf("a7a8q", "a7a8r", "a7a8b", "a7a8n", "a7b8q", "a7b8r", "a7b8b", "a7b8n"),
            buf.take(m).map { Moves.uci(it) }.toSet(),
        )
        // In check, only captures that actually answer the check are produced: Rxe8 does,
        // Qxh2 and Bxh2 do not.
        val check = board("R3r1k1/8/8/8/8/6B1/7p/4K2Q w - - 0 1")
        assertTrue(check.inCheck())
        val k = check.generateCaptures(buf)
        assertEquals(setOf("a8e8"), buf.take(k).map { Moves.uci(it) }.toSet())
        val notInCheck = board("R3r1k1/8/8/8/8/6B1/4P2p/4K2Q w - - 0 1")
        assertFalse(notInCheck.inCheck())
        val j = notInCheck.generateCaptures(buf)
        assertEquals(setOf("a8e8", "h1h2", "g3h2"), buf.take(j).map { Moves.uci(it) }.toSet())
    }

    @Test
    fun offsetAndCapacityOfMoveBuffers() {
        val board = board(Position.START_FEN)
        val buf = IntArray(100)
        assertEquals(20, board.generateMoves(buf))
        val end = board.generateMoves(buf, 20)
        assertEquals(40, end)
        assertEquals(buf.take(20), buf.slice(20 until 40))
        assertEquals(30, board.generateCaptures(buf, 30)) // nothing to capture: end == offset
        // Too small a buffer truncates instead of throwing.
        assertEquals(5, board.generateMoves(IntArray(5)))
        assertEquals(0, board.generateMoves(IntArray(0)))
    }

    @Test
    fun attackQueries() {
        val board = board("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1")
        fun sq(name: String) = Square.parse(name)
        assertEquals(sq("e1"), board.kingSquare(Board.WHITE))
        assertEquals(sq("e8"), board.kingSquare(Board.BLACK))
        assertFalse(board.inCheck())
        assertEquals(0L, board.checkers())
        assertTrue(board.isSquareAttacked(sq("f7"), Board.WHITE)) // knight e5
        assertTrue(board.isSquareAttacked(sq("c3"), Board.BLACK)) // pawn b4
        assertTrue(board.isSquareAttacked(sq("g2"), Board.BLACK)) // pawn h3
        assertTrue(board.isSquareAttacked(sq("a6"), Board.WHITE)) // bishop e2
        assertFalse(board.isSquareAttacked(sq("a5"), Board.WHITE))
        assertFalse(board.isSquareAttacked(sq("h8"), Board.WHITE))
        assertEquals((1L shl sq("e5")), board.attackers(sq("f7"), Board.WHITE))
        assertEquals((1L shl sq("e7")) or (1L shl sq("e8")), board.attackers(sq("f7"), Board.BLACK))
        assertEquals((1L shl sq("e5")) or (1L shl sq("f3")), board.attackers(sq("g4"), Board.WHITE))
        assertEquals(Board.pieceCode(Board.WHITE, Board.QUEEN), board.pieceAt(sq("f3")))
        assertEquals(Board.NO_PIECE, board.pieceAt(sq("e3")))
        assertEquals(8, java.lang.Long.bitCount(board.pieces(Board.WHITE, Board.PAWN)))
        assertEquals(16, java.lang.Long.bitCount(board.pieces(Board.PAWN)))
        assertEquals(16, java.lang.Long.bitCount(board.occupancy(Board.BLACK)))
        assertEquals(32, java.lang.Long.bitCount(board.occupied))

        val check = board("4r1k1/8/8/8/8/5n2/3P4/3QK3 w - - 0 1")
        assertTrue(check.inCheck())
        assertEquals((1L shl sq("e8")) or (1L shl sq("f3")), check.checkers())
        assertTrue(check.hasLegalMoves())
        assertFalse(board("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1").hasLegalMoves())
        assertFalse(board("3R2k1/5ppp/8/8/8/8/8/6K1 b - - 0 1").hasLegalMoves())
    }

    @Test
    fun zobristKeyDependsOnEveryPartOfTheState() {
        val base = board("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")
        val keys = listOf(
            "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1",
            "r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1",
            "r3k2r/8/8/8/8/8/8/R3K2R w KQk - 0 1",
            "r3k2r/8/8/8/8/8/8/R3K2R w Qkq - 0 1",
            "r3k2r/8/8/8/8/8/8/R3K2R w - - 0 1",
            "r3k2r/8/8/8/8/8/7P/R3K2R w KQkq - 0 1",
            "r3k2r/8/8/8/8/8/8/R3K1R1 w Qkq - 0 1",
        ).map { board(it).key }
        assertEquals(keys.size, keys.toSet().size)
        assertEquals(base.key, keys[0])
        // The clocks are not part of the key.
        assertEquals(base.key, board("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 42 99").key)
    }

    @Test
    fun enPassantSquareIsHashedOnlyWhenACaptureIsPossible() {
        // After 1.e4 no black pawn can take on e3: same key as without the en-passant square.
        val unusable = board("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1")
        val none = board("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1")
        assertEquals(Square.parse("e3"), unusable.epSquare)
        assertEquals(none.key, unusable.key)
        // With a black pawn on d4 the capture exists and the positions differ.
        val usable = board("rnbqkbnr/ppp1pppp/8/8/3pP3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 2")
        val without = board("rnbqkbnr/ppp1pppp/8/8/3pP3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 2")
        assertNotEquals(without.key, usable.key)
        assertEquals(usable.computeKey(), usable.key)
    }

    @Test
    fun transpositionsShareAKey() {
        val a = board(Position.START_FEN).also { it.playUci("e2e4", "e7e5", "g1f3", "b8c6") }
        val b = board(Position.START_FEN).also { it.playUci("g1f3", "b8c6", "e2e4", "e7e5") }
        assertEquals(a.key, b.key)
        assertEquals(a.computeKey(), a.key)
        val c = board(Position.START_FEN).also { it.playUci("g1f3", "b8c6", "e2e3", "e7e5") }
        assertNotEquals(a.key, c.key)
    }

    @Test
    fun repetitionDetection() {
        val board = board(Position.START_FEN)
        val startKey = board.key
        assertFalse(board.isRepetition())
        board.playUci("g1f3", "g8f6", "f3g1")
        assertFalse(board.isRepetition())
        board.playUci("f6g8")
        assertEquals(startKey, board.key)
        assertTrue(board.isRepetition())
        board.playUci("g1f3")
        assertTrue(board.isRepetition()) // the position after 1.Nf3 occurs for the second time
        board.playUci("e7e5") // irreversible: nothing before it can repeat
        assertFalse(board.isRepetition())
        board.playUci("f3g1", "f8e7", "g1f3", "e7f8")
        assertTrue(board.isRepetition())
        repeat(6) { board.unmake() }
        assertEquals(startKey, board.key)
        assertTrue(board.isRepetition())
        repeat(4) { board.unmake() }
        assertEquals(0, board.ply)
        assertFalse(board.isRepetition())
        assertEquals(Position.START_FEN, board.toPosition().toFen())
    }

    @Test
    fun nullMove() {
        val board = board("rnbqkbnr/ppp1pppp/8/8/3pP3/8/PPPP1PPP/RNBQKBNR b KQkq e3 5 2")
        val fen = board.toPosition().toFen()
        val key = board.key
        board.makeNull()
        assertEquals(Board.WHITE, board.side)
        assertEquals(-1, board.epSquare)
        assertEquals(board.computeKey(), board.key)
        assertNotEquals(key, board.key)
        assertEquals(1, board.ply)
        assertEquals("rnbqkbnr/ppp1pppp/8/8/3pP3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 3", board.toPosition().toFen())
        // Moves can be made and unmade on top of it.
        board.playUci("g1f3")
        board.unmake()
        board.unmake()
        assertEquals(fen, board.toPosition().toFen())
        assertEquals(key, board.key)
        assertEquals(0, board.ply)
    }

    @Test
    fun longGamesGrowTheHistoryAndUnwindCleanly() {
        val board = board("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1")
        val fen = board.toPosition().toFen()
        val key = board.key
        val random = Random(3)
        val buf = IntArray(Board.MAX_MOVES)
        var made = 0
        while (made < 400) {
            val n = board.generateMoves(buf)
            if (n == 0) break
            board.make(buf[random.nextInt(n)])
            made++
            assertEquals(board.computeKey(), board.key)
        }
        assertTrue(made > 100, "the random game ended after $made plies")
        assertEquals(made, board.ply)
        repeat(made) { board.unmake() }
        assertEquals(fen, board.toPosition().toFen())
        assertEquals(key, board.key)
        assertFailsWith<IllegalStateException> { board.unmake() }
    }

    @Test
    fun copyIsIndependent() {
        val board = board(Position.START_FEN)
        board.playUci("e2e4")
        val copy = board.copy()
        assertEquals(board.key, copy.key)
        assertEquals(board.toPosition(), copy.toPosition())
        assertEquals(0, copy.ply)
        copy.playUci("e7e5")
        assertEquals("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", board.toPosition().toFen())
        board.unmake()
        assertEquals(Position.START_FEN, board.toPosition().toFen())
        assertEquals("rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2", copy.toPosition().toFen())
    }

    @Test
    fun sanDirectlyOnTheBoard() {
        val board = board("r1bqkbnr/ppp2ppp/2n5/3pp3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 0 4")
        val key = board.key
        assertEquals("Nxe5", San.format(board, board.find("f3e5")))
        assertEquals("Bb5", San.format(board, board.find("f1b5")))
        assertEquals(key, board.key)
        assertEquals(0, board.ply)
    }

    @Test
    fun insufficientMaterialOnTheBoard() {
        assertTrue(board("8/8/8/8/8/8/8/KB5k w - - 0 1").isInsufficientMaterial())
        assertFalse(board("8/8/8/8/8/8/8/KR5k w - - 0 1").isInsufficientMaterial())
    }
}
