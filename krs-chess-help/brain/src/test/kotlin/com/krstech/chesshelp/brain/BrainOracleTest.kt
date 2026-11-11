package com.krstech.chesshelp.brain

import com.krstech.chesshelp.chess.BoardView
import com.krstech.chesshelp.chess.Position
import com.krstech.chesshelp.chess.Side
import com.krstech.chesshelp.claude.BoardReading
import com.krstech.chesshelp.claude.MoveVerdict
import com.krstech.chesshelp.claude.PositionVerdict
import com.krstech.chesshelp.vision.CellState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BrainOracleTest {
    private val analyzer = FakeAnalyzer()
    private val startRows = BoardView.toRows(Position.START, true)

    private fun oracle(
        snapshotRows: List<String>? = null,
        forcedSide: Side? = null,
        forcedWhiteAtBottom: Boolean? = null,
        previous: PreviousGame? = null,
        misread: Map<Int, CellState> = emptyMap(),
    ) = BrainOracle(analyzer, 700, snapshotRows?.let { snapshotOf(it, misread = misread) }, forcedSide, forcedWhiteAtBottom, previous)

    private fun reading(rows: List<String>, whiteAtBottom: Boolean = true, side: Char = 'w') =
        BoardReading(rows, whiteAtBottom, side, "")

    private fun List<String>.with(row: Int, text: String): List<String> = toMutableList().also { it[row] = text }

    @Test
    fun acceptsAValidReadingAndBriefsClaude() {
        val oracle = oracle(snapshotRows = startRows)
        val verdict = assertIs<PositionVerdict.Accepted>(oracle.submitPosition(reading(startRows)))
        assertEquals(Position.START.toFen(), verdict.fen)
        assertTrue(verdict.briefing.contains("FEN: ${Position.START.toFen()}"))
        assertTrue(verdict.briefing.contains("Side to move: White"))
        assertTrue(verdict.briefing.contains("Check: no"))
        val legalLine = verdict.briefing.lines().single { it.startsWith("Legal moves:") }
        assertEquals(
            Position.START.legalMoves().map { Position.START.san(it) }.toSet(),
            legalLine.removePrefix("Legal moves:").trim().split(" ").toSet(),
        )
        assertTrue(verdict.briefing.lines().any { it.startsWith("1. ") && it.contains("(+0.30)") })
        assertTrue(verdict.briefing.contains("shallow"))

        val accepted = assertNotNull(oracle.accepted)
        assertEquals(Position.START, accepted.position)
        assertTrue(accepted.whiteAtBottom)
        assertEquals(1, analyzer.calls)
    }

    @Test
    fun readsABoardShownFromBlacksSide() {
        val position = Position.START.after("e4")
        val rows = BoardView.toRows(position, false)
        val oracle = oracle(snapshotRows = rows)
        assertIs<PositionVerdict.Accepted>(oracle.submitPosition(reading(rows, whiteAtBottom = false, side = 'b')))
        val accepted = assertNotNull(oracle.accepted)
        assertEquals(position.placement(), accepted.position.placement())
        assertEquals(Side.BLACK, accepted.position.sideToMove)
        assertFalse(accepted.whiteAtBottom)
    }

    @Test
    fun rejectsMalformedRows() {
        val oracle = oracle()
        val short = assertIs<PositionVerdict.Rejected>(oracle.submitPosition(reading(startRows.dropLast(1))))
        assertTrue(short.problems.single().contains("8 rows"), short.problems.toString())
        val badChar = assertIs<PositionVerdict.Rejected>(oracle.submitPosition(reading(startRows.with(3, "...x...."))))
        assertTrue(badChar.problems.single().contains("Row 4"), badChar.problems.toString())
        val badSide = assertIs<PositionVerdict.Rejected>(oracle.submitPosition(reading(startRows, side = 'x')))
        assertTrue(badSide.problems.single().contains("side_to_move"))
        assertNull(oracle.accepted)
        assertEquals(0, analyzer.calls)
    }

    @Test
    fun rejectsAnImpossiblePositionWithTheValidatorsMessages() {
        val oracle = oracle()
        val twoKings = startRows.with(4, "....K...")
        val verdict = assertIs<PositionVerdict.Rejected>(oracle.submitPosition(reading(twoKings)))
        assertTrue(verdict.problems.any { it.contains("White has 2 kings") }, verdict.problems.toString())
        assertNull(oracle.accepted)
    }

    @Test
    fun crossChecksAgainstThePixelsExactlyOnce() {
        // Claude reads the e-pawn on e4; the pixels show it still on e2.
        val misreadRows = startRows.with(4, "....P...").with(6, "PPPP.PPP")
        val oracle = oracle(snapshotRows = startRows)

        val first = assertIs<PositionVerdict.Rejected>(oracle.submitPosition(reading(misreadRows)))
        val cellLines = first.problems.filter { it.startsWith("r") }
        assertEquals(2, cellLines.size, first.problems.toString())
        assertTrue(cellLines.any { it.startsWith("r5c5:") && it.contains("white pawn (P)") && it.contains("an empty square") })
        assertTrue(cellLines.any { it.startsWith("r7c5:") && it.contains("you reported an empty square") && it.contains("light-coloured (White) piece") })
        assertNull(oracle.accepted)

        // Claude stands by its reading: accepted this time.
        assertIs<PositionVerdict.Accepted>(oracle.submitPosition(reading(misreadRows)))
        // And a later submission is not compared again either.
        val other = startRows.with(5, "....P...").with(6, "PPPP.PPP")
        assertIs<PositionVerdict.Accepted>(oracle.submitPosition(reading(other)))
        assertEquals("rnbqkbnr/pppppppp/8/8/8/4P3/PPPP1PPP/RNBQKBNR", oracle.accepted!!.position.placement())
    }

    @Test
    fun crossCheckReportsPieceColourDisagreements() {
        // The sampler sees a dark piece on c1 where Claude reads White's bishop.
        val oracle = oracle(snapshotRows = startRows, misread = mapOf(58 to CellState.DARK_PIECE))
        val verdict = assertIs<PositionVerdict.Rejected>(oracle.submitPosition(reading(startRows)))
        val line = verdict.problems.single { it.startsWith("r") }
        assertTrue(line.startsWith("r8c3:") && line.contains("white bishop (B)") && line.contains("dark-coloured (Black) piece"), line)
    }

    @Test
    fun anEarlierRejectionDoesNotUseUpTheCrossCheck() {
        val misreadRows = startRows.with(4, "....P...").with(6, "PPPP.PPP")
        val oracle = oracle(snapshotRows = startRows)
        assertIs<PositionVerdict.Rejected>(oracle.submitPosition(reading(startRows.dropLast(1))))
        assertIs<PositionVerdict.Rejected>(oracle.submitPosition(reading(startRows.with(4, "....K..."))))
        val crossCheck = assertIs<PositionVerdict.Rejected>(oracle.submitPosition(reading(misreadRows)))
        assertTrue(crossCheck.problems.any { it.startsWith("r5c5:") })
        assertIs<PositionVerdict.Accepted>(oracle.submitPosition(reading(misreadRows)))
    }

    @Test
    fun aReadingThatMatchesThePixelsPassesTheCrossCheckAndIsNotCheckedAgain() {
        val oracle = oracle(snapshotRows = startRows)
        assertIs<PositionVerdict.Accepted>(oracle.submitPosition(reading(startRows)))
        val other = startRows.with(4, "....P...").with(6, "PPPP.PPP")
        assertIs<PositionVerdict.Accepted>(oracle.submitPosition(reading(other)))
    }

    @Test
    fun withoutAReliableSnapshotThereIsNoCrossCheck() {
        val oracle = oracle(snapshotRows = null)
        assertIs<PositionVerdict.Accepted>(oracle.submitPosition(reading(startRows.with(4, "....P...").with(6, "PPPP.PPP"))))
    }

    @Test
    fun manyDisagreementsAreSummarised() {
        val empty = List(8) { "........" }
        val kingsOnly = empty.with(0, "....k...").with(7, "....K...")
        val oracle = oracle(snapshotRows = startRows)
        val verdict = assertIs<PositionVerdict.Rejected>(oracle.submitPosition(reading(kingsOnly)))
        assertEquals(16, verdict.problems.count { it.startsWith("r") })
        assertTrue(verdict.problems.last().contains("14 more squares"), verdict.problems.last())
    }

    @Test
    fun checksMoves() {
        val oracle = oracle()
        val fen = assertIs<PositionVerdict.Accepted>(oracle.submitPosition(reading(startRows))).fen
        assertEquals(MoveVerdict.Legal("e4", "e2e4"), oracle.checkMove(fen, "e4"))
        assertEquals(MoveVerdict.Legal("Nf3", "g1f3"), oracle.checkMove(fen, "g1f3"))
        assertEquals(MoveVerdict.Legal("Nf3", "g1f3"), oracle.checkMove(fen, " Nf3! "))
        val illegal = assertIs<MoveVerdict.Illegal>(oracle.checkMove(fen, "e5"))
        assertTrue(illegal.reason.contains("Legal moves:") && illegal.reason.contains("Nf3") && illegal.reason.contains("e4"), illegal.reason)
        assertIs<MoveVerdict.Illegal>(oracle.checkMove(fen, "Ke2"))
    }

    @Test
    fun checkMoveBeforeAnyPositionFallsBackToTheFen() {
        val oracle = oracle()
        assertEquals(MoveVerdict.Legal("e4", "e2e4"), oracle.checkMove(Position.START.toFen(), "e4"))
        assertIs<MoveVerdict.Illegal>(oracle.checkMove("not a fen", "e4"))
    }

    @Test
    fun checkmateIsAcceptedAndAsksForNoMove() {
        val mated = Position.START.after("f3", "e5", "g4", "Qh4#")
        val oracle = oracle()
        val verdict = assertIs<PositionVerdict.Accepted>(oracle.submitPosition(reading(BoardView.toRows(mated, true), side = 'w')))
        assertTrue(verdict.briefing.contains("Legal moves: none"))
        assertTrue(verdict.briefing.contains("checkmate") && verdict.briefing.contains("Black has won"), verdict.briefing)
        assertTrue(verdict.briefing.contains("do not call recommend_move"))
        assertEquals(0, analyzer.calls)
        val move = assertIs<MoveVerdict.Illegal>(oracle.checkMove(verdict.fen, "Kf2"))
        assertTrue(move.reason.contains("game is over"), move.reason)
    }

    @Test
    fun stalemateIsAcceptedAndAsksForNoMove() {
        val stalemate = Position.fromFen("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1")
        val oracle = oracle()
        val verdict = assertIs<PositionVerdict.Accepted>(oracle.submitPosition(reading(BoardView.toRows(stalemate, true), side = 'b')))
        assertTrue(verdict.briefing.contains("stalemate") && verdict.briefing.contains("draw"), verdict.briefing)
        assertTrue(verdict.briefing.contains("Legal moves: none"))
    }

    @Test
    fun honoursTheUsersSideToMove() {
        val oracle = oracle(forcedSide = Side.BLACK)
        // The Claude client already reports the forced side; the oracle must not depend on that.
        assertIs<PositionVerdict.Accepted>(oracle.submitPosition(reading(startRows, side = 'w')))
        val accepted = oracle.accepted!!
        assertEquals(Side.BLACK, accepted.position.sideToMove)
        assertTrue(accepted.sideForced)
    }

    @Test
    fun anImpossibleForcedSideIsQuestionedOnceThenDropped() {
        // Black is in check from the queen, so it cannot be White's move.
        val position = Position.fromFen("4k3/8/8/8/8/8/4Q3/4K3 b - - 0 1")
        val rows = BoardView.toRows(position, true)
        val oracle = oracle(forcedSide = Side.WHITE)
        val first = assertIs<PositionVerdict.Rejected>(oracle.submitPosition(reading(rows, side = 'w')))
        assertTrue(first.problems.any { it.contains("e8") }, first.problems.toString())
        assertTrue(first.problems.last().contains("The user has set White to move"))

        val second = assertIs<PositionVerdict.Accepted>(oracle.submitPosition(reading(rows, side = 'w')))
        val accepted = oracle.accepted!!
        assertEquals(Side.BLACK, accepted.position.sideToMove)
        assertFalse(accepted.sideForced)
        assertTrue(second.briefing.contains("Note: the user's setting (White to move) is impossible"), second.briefing)
    }

    @Test
    fun honoursTheUsersOrientationWhileItFits() {
        val oracle = oracle(forcedWhiteAtBottom = false)
        assertIs<PositionVerdict.Accepted>(oracle.submitPosition(reading(startRows, whiteAtBottom = true)))
        val accepted = oracle.accepted!!
        assertFalse(accepted.whiteAtBottom)
        assertTrue(accepted.orientationForced)
        // Same picture, other way round: the piece in the top-left corner now stands on h1.
        assertEquals('r', accepted.position.pieceAt(7)!!.fenChar)
    }

    @Test
    fun dropsTheUsersOrientationWhenOnlyClaudesFits() {
        // White at the bottom: pawn e5, black king d4 (behind the pawn, not attacked). Seen the other way
        // round the pawn would stand on d4 and check a king on e5 with White to move.
        val rows = listOf("........", "........", "........", "....P...", "...k....", "........", "........", "K.......")
        val oracle = oracle(forcedWhiteAtBottom = false)
        assertIs<PositionVerdict.Accepted>(oracle.submitPosition(reading(rows, whiteAtBottom = true)))
        val accepted = oracle.accepted!!
        assertTrue(accepted.whiteAtBottom)
        assertFalse(accepted.orientationForced)
    }

    @Test
    fun keepsCastlingRightsAndHistoryWhenTheReadingContinuesTheTrackedGame() {
        // Both kings have moved and returned: the picture alone would grant castling rights again.
        val before = Position.START.after("e4", "e5", "Ke2", "Ke7", "Ke1", "Ke8")
        val history = listOf("e4", "e5", "Ke2", "Ke7", "Ke1", "Ke8")
        val same = oracle(previous = PreviousGame(before, true, history))
        assertIs<PositionVerdict.Accepted>(same.submitPosition(reading(BoardView.toRows(before, true))))
        assertEquals(before, same.accepted!!.position)
        assertEquals(history, same.accepted!!.history)

        val later = before.after("Nf3")
        val moved = oracle(previous = PreviousGame(before, true, history))
        assertIs<PositionVerdict.Accepted>(moved.submitPosition(reading(BoardView.toRows(later, true), side = 'b')))
        assertEquals(later, moved.accepted!!.position)
        assertEquals(history + "Nf3", moved.accepted!!.history)
        assertFalse(moved.accepted!!.position.canCastle(Side.WHITE, true))

        // An unrelated position starts from scratch.
        val fresh = oracle(previous = PreviousGame(before, true, history))
        assertIs<PositionVerdict.Accepted>(fresh.submitPosition(reading(startRows)))
        assertEquals(Position.START, fresh.accepted!!.position)
        assertTrue(fresh.accepted!!.history.isEmpty())
    }

    @Test
    fun aBrokenEngineDoesNotBlockTheReading() {
        analyzer.failure = IllegalStateException("boom")
        val oracle = oracle()
        val verdict = assertIs<PositionVerdict.Accepted>(oracle.submitPosition(reading(startRows)))
        assertTrue(verdict.briefing.contains("Local engine lines: none available"))
        assertTrue(oracle.accepted!!.briefing.lines.isEmpty())
    }

    @Test
    fun theKnownPositionOracleOnlyChecksMoves() {
        val position = Position.START.after("e4")
        val oracle = BrainOracle.forKnownPosition(position, true, listOf("e4"), Briefings.build(position, emptyList()))
        val rejected = assertIs<PositionVerdict.Rejected>(oracle.submitPosition(reading(startRows)))
        assertTrue(rejected.problems.single().contains("already known"))
        assertEquals(position, oracle.accepted!!.position)
        assertEquals(MoveVerdict.Legal("e5", "e7e5"), oracle.checkMove(position.toFen(), "e5"))
        assertIs<MoveVerdict.Illegal>(oracle.checkMove(position.toFen(), "e4"))
    }
}
