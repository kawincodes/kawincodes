package com.krstech.chesshelp.claude

import com.anthropic.core.JsonValue
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChessToolsTest {

    private fun reading(input: Map<String, Any?>): Parsed<BoardReading> = ChessTools.parseReading(JsonValue.from(input))

    private fun recommendation(input: Map<String, Any?>): Parsed<Recommendation> =
        ChessTools.parseRecommendation(JsonValue.from(input))

    private fun problems(parsed: Parsed<*>): String = assertIs<Parsed.Bad>(parsed).problems.joinToString("\n")

    @Test
    fun `a well-formed reading is taken as it is`() {
        val rows = listOf("r...k..r", "ppp..ppp", "..n.....", "...qp...", "........", "..N..N..", "PPP..PPP", "R..QK..R")
        val parsed = reading(submitPositionInput(rows = rows, whiteAtBottom = false, sideToMove = "b", notes = " r4c4 could be a bishop "))

        assertEquals(
            BoardReading(rows, whiteAtBottom = false, sideToMove = 'b', notes = "r4c4 could be a bishop"),
            assertIs<Parsed.Ok<BoardReading>>(parsed).value,
        )
    }

    @Test
    fun `spaces and fen digits in rows are normalised`() {
        val rows = listOf("r n b q k b n r", "pppppppp", "8", "8", "4P3", "8", "PPPP1PPP", "RNBQKBNR")
        val parsed = assertIs<Parsed.Ok<BoardReading>>(reading(submitPositionInput(rows = rows))).value

        assertEquals(
            listOf("rnbqkbnr", "pppppppp", "........", "........", "....P...", "........", "PPPP.PPP", "RNBQKBNR"),
            parsed.rows,
        )
    }

    @Test
    fun `wrong row count and row length are named by image row`() {
        assertContains(problems(reading(submitPositionInput(rows = START_ROWS + "........"))), "9 entries")

        val longRow = START_ROWS.toMutableList().also { it[2] = "........." }
        val message = problems(reading(submitPositionInput(rows = longRow)))
        assertContains(message, "Row r3")
        assertContains(message, "9 squares")
    }

    @Test
    fun `unknown characters are named`() {
        val bad = START_ROWS.toMutableList().also { it[4] = "...x...." }
        val message = problems(reading(submitPositionInput(rows = bad)))

        assertContains(message, "Row r5")
        assertContains(message, "'x'")
    }

    @Test
    fun `missing or mistyped fields are reported together`() {
        val message = problems(reading(linkedMapOf("rows" to "rnbqkbnr", "side_to_move" to "white to move")))

        assertContains(message, "\"rows\" is missing")
        assertContains(message, "\"white_at_bottom\" is missing")
        assertContains(message, "\"side_to_move\" must be")
        assertIs<Parsed.Bad>(ChessTools.parseReading(JsonValue.from("not an object")))
    }

    @Test
    fun `side to move accepts words and any case`() {
        assertEquals('w', assertIs<Parsed.Ok<BoardReading>>(reading(submitPositionInput(sideToMove = "White"))).value.sideToMove)
        assertEquals('b', assertIs<Parsed.Ok<BoardReading>>(reading(submitPositionInput(sideToMove = "B"))).value.sideToMove)
    }

    @Test
    fun `recommendation fields are read and tidied`() {
        val parsed = assertIs<Parsed.Ok<Recommendation>>(
            recommendation(
                recommendMoveInput(
                    move = " Nf3 ",
                    explanation = " Develops with tempo. ",
                    evaluation = "Winning",
                    alternatives = listOf(linkedMapOf("move" to "d4", "note" to " solid "), linkedMapOf("move" to "", "note" to "empty")),
                    opponentThreat = " ...Qxh2# ",
                )
            )
        ).value

        assertEquals("Nf3", parsed.move)
        assertEquals("Develops with tempo.", parsed.explanation)
        assertEquals(Evaluation.WINNING, parsed.evaluation)
        assertEquals(listOf(Alternative("d4", "solid")), parsed.alternatives)
        assertEquals("...Qxh2#", parsed.opponentThreat)
    }

    @Test
    fun `every evaluation value of the schema maps to the enum`() {
        for (evaluation in Evaluation.entries) {
            val parsed = assertIs<Parsed.Ok<Recommendation>>(recommendation(recommendMoveInput(evaluation = evaluation.name.lowercase())))
            assertEquals(evaluation, parsed.value.evaluation)
        }
        val unknown = assertIs<Parsed.Ok<Recommendation>>(recommendation(recommendMoveInput(evaluation = "crushing")))
        assertEquals(Evaluation.UNCLEAR, unknown.value.evaluation)
    }

    @Test
    fun `no threat is null and optional fields may be absent`() {
        for (none in listOf("", "  ", "None", "none.", "No threat", "N/A")) {
            assertNull(assertIs<Parsed.Ok<Recommendation>>(recommendation(recommendMoveInput(opponentThreat = none))).value.opponentThreat, none)
        }
        val minimal = assertIs<Parsed.Ok<Recommendation>>(recommendation(linkedMapOf("move" to "e4"))).value
        assertEquals("", minimal.explanation)
        assertEquals(Evaluation.UNCLEAR, minimal.evaluation)
        assertTrue(minimal.alternatives.isEmpty())
        assertNull(minimal.opponentThreat)
    }

    @Test
    fun `a recommendation without a move is rejected`() {
        assertContains(problems(recommendation(recommendMoveInput(move = "  "))), "\"move\" is missing")
        assertIs<Parsed.Bad>(ChessTools.parseRecommendation(JsonValue.from(listOf("e4"))))
    }

    @Test
    fun `tool definitions are the same objects on every use`() {
        assertEquals(listOf("submit_position", "recommend_move"), ChessTools.all.map { it.name() })
        assertTrue(ChessTools.all === ChessTools.all)
        assertTrue(ChessTools.all.all { it.strict().orElse(false) })
    }
}
