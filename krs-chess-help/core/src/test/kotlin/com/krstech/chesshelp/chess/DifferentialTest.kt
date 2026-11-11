package com.krstech.chesshelp.chess

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Plays random games and compares, position by position, the production rules with the naive
 * oracle in [ReferencePosition]: the set of legal moves, check status, and the placement, side to
 * move, castling rights and en-passant square after every move.
 */
class DifferentialTest {
    private val starts = listOf(
        Position.START_FEN,
        "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
        "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
        "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
        "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8",
        "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10",
        "n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1",
        "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1",
        "4k3/pppppppp/8/8/8/8/PPPPPPPP/4K3 w - - 0 1",
        "rnbqkbnr/1pp1pppp/p7/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3",
        "8/8/8/K2pP2r/8/8/8/4k3 w - d6 0 1",
        "3k4/8/8/2Pp4/8/8/B7/4K3 w - d6 0 1",
        "8/5k2/8/2Pp4/8/8/8/R3K2R w KQ d6 0 1",
    )

    @Test
    fun randomGamesAgreeWithTheNaiveOracle() {
        val random = Random(42)
        var positions = 0
        var enPassants = 0
        var castlings = 0
        var promotions = 0
        var checks = 0
        for (fen in starts) {
            repeat(25) {
                var position = Position.fromFen(fen)
                var reference = ReferencePosition.fromFen(fen)
                var plies = 0
                while (plies < 100) {
                    val context = "after $plies plies from $fen: ${position.toFen()}"
                    val expected = reference.legalMoves()
                    assertEquals(
                        expected.map { it.uci() }.sorted(),
                        position.legalMoves().map { it.uci() }.sorted(),
                        "legal moves $context",
                    )
                    assertEquals(reference.inCheck(), position.isInCheck(), "check $context")
                    assertEquals(expected.isEmpty() && reference.inCheck(), position.isCheckmate(), "mate $context")
                    assertEquals(expected.isEmpty() && !reference.inCheck(), position.isStalemate(), "stalemate $context")
                    assertEquals(position, Position.fromFen(position.toFen()), "FEN round trip $context")
                    assertEquals(position, position.toBoard().toPosition(), "board round trip $context")
                    positions++
                    if (reference.inCheck()) checks++
                    if (expected.isEmpty()) break

                    // Random play rarely castles, promotes or takes en passant: nudge it.
                    val special = expected.filter { candidate ->
                        val type = position.pieceAt(candidate.from)!!.type
                        candidate.promotion != null ||
                            (type == PieceType.PAWN && candidate.to == position.enPassantSquare) ||
                            (type == PieceType.KING && kotlin.math.abs(candidate.to - candidate.from) == 2)
                    }
                    val pool = if (special.isNotEmpty() && random.nextInt(3) == 0) special else expected
                    val choice = pool[random.nextInt(pool.size)]
                    val move = Move.fromUci(choice.uci())!!
                    val mover = position.pieceAt(move.from)!!
                    if (mover.type == PieceType.PAWN && move.to == position.enPassantSquare) enPassants++
                    if (mover.type == PieceType.KING && kotlin.math.abs(move.to - move.from) == 2) castlings++
                    if (move.promotion != null) promotions++

                    position = position.play(move)
                    reference = reference.play(choice)
                    assertEquals(
                        reference.fenPrefix(),
                        position.toFen().split(' ').take(4).joinToString(" "),
                        "state after ${choice.uci()} $context",
                    )
                    plies++
                }
            }
        }
        // Make sure the walk really exercised the special rules.
        assertTrue(positions > 15_000, "positions: $positions")
        assertTrue(enPassants >= 10, "en passant captures: $enPassants")
        assertTrue(castlings >= 20, "castlings: $castlings")
        assertTrue(promotions >= 50, "promotions: $promotions")
        assertTrue(checks >= 500, "checks: $checks")
        println("differential test: $positions positions, $enPassants e.p., $castlings castlings, $promotions promotions, $checks checks")
    }
}

/**
 * The same comparison on wild, mostly unreachable placements (one king each, pawns off the back
 * ranks, anything else goes): several checkers at once, kings that can be captured, pins
 * everywhere. The generator must not rely on the position having arisen from a real game.
 */
class RandomPlacementDifferentialTest {
    @Test
    fun wildPlacementsAgreeWithTheNaiveOracle() {
        val random = Random(1234)
        var compared = 0
        var withEnPassant = 0
        var inCheck = 0
        repeat(4000) {
            val position = randomPosition(random, kingsAndPawnsValid = true)
            val fen = position.toFen()
            val reference = ReferencePosition.fromFen(fen)
            val expected = reference.legalMoves()
            assertEquals(
                expected.map { it.uci() }.sorted(),
                position.legalMoves().map { it.uci() }.sorted(),
                "legal moves of $fen",
            )
            assertEquals(reference.inCheck(), position.isInCheck(), "check in $fen")
            for (move in expected) {
                val next = position.play(Move.fromUci(move.uci())!!)
                assertEquals(
                    reference.play(move).fenPrefix(),
                    next.toFen().split(' ').take(4).joinToString(" "),
                    "state after ${move.uci()} in $fen",
                )
                position.san(Move.fromUci(move.uci())!!)
            }
            compared++
            if (position.enPassantSquare >= 0) withEnPassant++
            if (position.isInCheck()) inCheck++
        }
        assertTrue(withEnPassant >= 200, "positions with an en-passant square: $withEnPassant")
        assertTrue(inCheck >= 500, "positions in check: $inCheck")
        println("wild placements: $compared compared, $withEnPassant with e.p. square, $inCheck in check")
    }

    /** Anything at all on the board: no kings, five kings, pawns on the back ranks. Nothing may throw. */
    @Test
    fun arbitraryGarbageNeverThrowsAndKeepsTheBoardConsistent() {
        val random = Random(77)
        val buf = IntArray(2048)
        repeat(3000) {
            val position = randomPosition(random, kingsAndPawnsValid = false)
            val moves = position.legalMoves()
            position.isCheckmate()
            position.isStalemate()
            position.isInsufficientMaterial()
            assertEquals(position, Position.fromFen(position.toFen()))
            PositionValidator.problems(Array(64) { position.pieceAt(it) }, position.sideToMove)
            MoveTracker.occupancyOf(position)

            val board = position.toBoard()
            assertEquals(board.computeKey(), board.key)
            val n = board.generateMoves(buf)
            assertEquals(moves.size, n)
            val fen = position.toFen()
            val noisy = IntArray(2048)
            val noisyCount = board.generateCaptures(noisy)
            assertEquals(
                buf.take(n).filter { Moves.isCapture(it) || Moves.isPromotion(it) }.sorted(),
                noisy.take(noisyCount).sorted(),
                "captures-only generation in $fen",
            )
            assertEquals(n > 0, board.hasLegalMoves())
            for (i in 0 until n) {
                board.make(buf[i])
                assertEquals(board.computeKey(), board.key, "key after ${Moves.uci(buf[i])} in $fen")
                assertEquals(position.play(Moves.toMove(buf[i])), board.toPosition())
                board.unmake()
            }
            assertEquals(fen, board.toPosition().toFen())
            for (move in moves.take(5)) {
                assertEquals(move, position.parseMove(position.san(move)), "SAN round trip in $fen")
                val next = position.play(move)
                val inferred = MoveTracker.infer(position, MoveTracker.occupancyOf(next), maxPlies = 1)
                assertTrue(inferred is MoveInference.Played, "$inferred for ${move.uci()} in $fen")
            }
        }
    }

    private fun randomPosition(random: Random, kingsAndPawnsValid: Boolean): Position {
        val pieces = arrayOfNulls<Piece>(64)
        val letters = if (kingsAndPawnsValid) "PNBRQpnbrq" else "PNBRQKpnbrqk"
        if (kingsAndPawnsValid) {
            val whiteKing = random.nextInt(64)
            var blackKing = random.nextInt(64)
            while (blackKing == whiteKing) blackKing = random.nextInt(64)
            pieces[whiteKing] = Piece(Side.WHITE, PieceType.KING)
            pieces[blackKing] = Piece(Side.BLACK, PieceType.KING)
        }
        repeat(random.nextInt(1, 24)) {
            val sq = random.nextInt(64)
            val piece = Piece.fromFenChar(letters[random.nextInt(letters.length)])!!
            val backRank = Square.rank(sq) == 0 || Square.rank(sq) == 7
            if (pieces[sq] == null && !(kingsAndPawnsValid && piece.type == PieceType.PAWN && backRank)) pieces[sq] = piece
        }
        // Kings and rooks on their home squares now and then, so that castling gets exercised.
        if (random.nextInt(4) == 0 && kingsAndPawnsValid) {
            for (sq in 0 until 64) if (pieces[sq]?.type == PieceType.KING) pieces[sq] = null
            pieces[4] = Piece(Side.WHITE, PieceType.KING)
            pieces[60] = Piece(Side.BLACK, PieceType.KING)
            for (corner in intArrayOf(0, 7, 56, 63)) {
                if (random.nextBoolean()) pieces[corner] = Piece(if (corner < 8) Side.WHITE else Side.BLACK, PieceType.ROOK)
            }
        }
        val side = if (random.nextBoolean()) Side.WHITE else Side.BLACK
        // Often set the stage for en passant: a pawn that "just" advanced two squares, with a
        // possible capturer next to it.
        var enPassant = -1
        if (random.nextInt(3) == 0) {
            val file = random.nextInt(8)
            val pawnRank = if (side == Side.WHITE) 4 else 3
            val behind = if (side == Side.WHITE) 5 else 2
            val origin = if (side == Side.WHITE) 6 else 1
            val squares = intArrayOf(Square.of(file, pawnRank), Square.of(file, behind), Square.of(file, origin))
            if (squares.none { pieces[it]?.type == PieceType.KING }) {
                pieces[squares[0]] = Piece(side.opposite, PieceType.PAWN)
                pieces[squares[1]] = null
                pieces[squares[2]] = null
                enPassant = squares[1]
                val neighbourFile = if (file == 0) 1 else if (file == 7) 6 else file + (if (random.nextBoolean()) 1 else -1)
                val neighbour = Square.of(neighbourFile, pawnRank)
                if (pieces[neighbour]?.type != PieceType.KING && random.nextInt(4) != 0) {
                    pieces[neighbour] = Piece(side, PieceType.PAWN)
                }
            }
        }
        return Position.fromBoard(pieces, side, inferCastling = random.nextInt(3) != 0, enPassantSquare = enPassant)
    }
}
