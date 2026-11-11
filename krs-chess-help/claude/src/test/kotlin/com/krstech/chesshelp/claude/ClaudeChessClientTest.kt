package com.krstech.chesshelp.claude

import com.fasterxml.jackson.databind.JsonNode
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Base64
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClaudeChessClientTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // ------------------------------------------------------------ helpers

    private fun client(config: ClaudeConfig = server.config()) = ClaudeChessClient(config)

    private fun enqueue(
        content: List<Map<String, Any?>>,
        stopReason: String = "tool_use",
        model: String = "claude-opus-5-5",
        inputTokens: Long = 2095,
        outputTokens: Long = 503,
        cacheCreationInputTokens: Long = 0,
        cacheReadInputTokens: Long = 0,
    ) {
        server.enqueue(
            okResponse(
                messageJson(
                    content = content,
                    stopReason = stopReason,
                    model = model,
                    inputTokens = inputTokens,
                    outputTokens = outputTokens,
                    cacheCreationInputTokens = cacheCreationInputTokens,
                    cacheReadInputTokens = cacheReadInputTokens,
                )
            )
        )
    }

    private val submitTurn = listOf(
        thinkingBlock(SIGNATURE_1),
        toolUseBlock("toolu_01SubmitPosition0001", "submit_position", submitPositionInput()),
    )
    private val recommendTurn = listOf(
        thinkingBlock(SIGNATURE_2),
        toolUseBlock("toolu_01RecommendMove00002", "recommend_move", recommendMoveInput()),
    )

    /**
     * Splits a request body into the raw text of the `messages` array (without its brackets) and
     * everything else, so both can be compared byte for byte between requests.
     */
    private class RawBody(raw: String) {
        val messages: String
        val rest: String

        init {
            val open = "\"messages\":["
            val start = raw.indexOf(open)
            val end = raw.lastIndexOf("],\"model\":")
            check(start >= 0 && end > start) { "Unexpected request layout: ${raw.take(200)}" }
            messages = raw.substring(start + open.length, end)
            rest = raw.substring(0, start) + raw.substring(end + 1)
        }
    }

    private fun RecordedRequest.raw(): RawBody = RawBody(body.clone().readUtf8())

    private fun JsonNode.message(index: Int): JsonNode = get("messages").get(index)

    private fun JsonNode.lastMessage(): JsonNode = get("messages").let { it.get(it.size() - 1) }

    /** Every field the response block carried must come back with the same value. */
    private fun assertEchoed(expectedContent: List<Map<String, Any?>>, assistantMessage: JsonNode) {
        assertEquals("assistant", assistantMessage.get("role").asText())
        val expected = json(toJson(expectedContent))
        val actual = assistantMessage.get("content")
        assertEquals(expected.size(), actual.size(), "number of echoed blocks")
        for (i in 0 until expected.size()) {
            val want = expected.get(i)
            val got = actual.get(i)
            for (field in want.fieldNames()) {
                if (want.get(field).isNull) {
                    assertTrue(got.get(field) == null || got.get(field).isNull, "block $i field $field should stay null")
                } else {
                    assertEquals(want.get(field), got.get(field), "block $i field $field")
                }
            }
            for (field in got.fieldNames()) {
                assertTrue(want.has(field), "block $i gained a field the response did not have: $field")
            }
        }
    }

    private fun assertToolResult(message: JsonNode, toolUseId: String, isError: Boolean): String {
        assertEquals("user", message.get("role").asText())
        val content = message.get("content")
        assertEquals(1, content.size())
        val block = content.get(0)
        assertEquals("tool_result", block.get("type").asText())
        assertEquals(toolUseId, block.get("tool_use_id").asText())
        if (isError) {
            assertEquals(true, block.get("is_error")?.asBoolean())
        } else {
            assertTrue(block.get("is_error") == null || !block.get("is_error").asBoolean(), "is_error must not be set")
        }
        return block.get("content").asText()
    }

    private fun assertEverySchemaObjectIsClosed(schema: JsonNode, path: String) {
        if (schema.isObject) {
            if (schema.get("type")?.asText() == "object") {
                assertEquals(false, schema.get("additionalProperties")?.booleanValue(), "$path.additionalProperties")
                val properties = schema.get("properties").fieldNames().asSequence().toList()
                val required = schema.get("required").map { it.asText() }
                assertEquals(properties, required, "$path: strict tools need every property listed as required")
            }
            schema.fields().forEach { (name, child) -> assertEverySchemaObjectIsClosed(child, "$path.$name") }
        } else if (schema.isArray) {
            schema.forEachIndexed { index, child -> assertEverySchemaObjectIsClosed(child, "$path[$index]") }
        }
    }

    // ------------------------------------------------------------ happy path

    @Test
    fun `happy path - position accepted then legal move in two requests`() {
        enqueue(submitTurn, inputTokens = 2095, outputTokens = 503, cacheCreationInputTokens = 1900)
        enqueue(recommendTurn, inputTokens = 310, outputTokens = 620, cacheReadInputTokens = 3995)
        val oracle = FakeOracle()
        val progress = ArrayList<String>()

        val result = client().use { it.analyzeBoard(visionRequest(), oracle) { step -> progress += step } }

        assertEquals(2, server.requestCount)
        assertEquals(2, result.roundTrips)
        assertEquals(START_FEN, result.fen)
        assertEquals(BoardReading(START_ROWS, whiteAtBottom = true, sideToMove = 'w', notes = ""), result.reading)
        assertEquals(listOf(result.reading), oracle.readings)
        assertEquals("claude-opus-5-5", result.servedBy)
        // Input counts every prompt token: uncached + cache writes + cache reads, over both requests.
        assertEquals(TokenUsage(inputTokens = 2095 + 1900 + 310 + 3995, outputTokens = 503 + 620), result.usage)

        val advice = assertNotNull(result.advice)
        assertEquals("e4", advice.san)
        assertEquals("e2e4", advice.uci)
        assertEquals("Takes the centre and opens lines for the queen and bishop.", advice.explanation)
        assertEquals(Evaluation.EQUAL, advice.evaluation)
        assertEquals(listOf(Alternative("d4", "Equally principled"), Alternative("Nf3", "Flexible")), advice.alternatives)
        assertNull(advice.opponentThreat)
        assertEquals(START_FEN to "e4", oracle.moveChecks.first())

        assertEquals("Reading the board…", progress.first())
        assertContains(progress, "Choosing a move…")

        // ---- request 1
        val first = server.take()
        assertEquals("POST", first.method)
        assertEquals("/v1/messages", first.requestUrl!!.encodedPath)
        assertEquals("server-side-fallback-2026-07-01", first.getHeader("anthropic-beta"))
        // A custom base URL may be an older gateway, so the key goes out in both header forms, once each.
        assertEquals(listOf("sk-ant-test-key"), first.headers.values("X-Api-Key"))
        assertEquals(listOf("Bearer sk-ant-test-key"), first.headers.values("Authorization"))
        assertEquals("2026-10-21", first.getHeader("anthropic-version"))
        val body1 = first.json()
        assertEquals("claude-opus-5-5", body1.get("model").asText())
        assertEquals(16000, body1.get("max_tokens").asInt())
        assertEquals("medium", body1.get("output_config").get("effort").asText())
        assertEquals(json("""{"effort":"medium"}"""), body1.get("output_config"))
        assertEquals("default", body1.get("fallbacks").asText())
        for (forbidden in listOf("thinking", "temperature", "top_p", "top_k", "stop_sequences", "stream", "budget_tokens")) {
            assertFalse(body1.has(forbidden), "request must not send $forbidden")
        }
        assertEquals(json("""{"type":"auto","disable_parallel_tool_use":true}"""), body1.get("tool_choice"))

        val system = body1.get("system")
        assertEquals(1, system.size())
        assertEquals("text", system.get(0).get("type").asText())
        assertEquals(Prompts.SYSTEM, system.get(0).get("text").asText())

        val tools = body1.get("tools")
        assertEquals(listOf("submit_position", "recommend_move"), tools.map { it.get("name").asText() })
        for (tool in tools) {
            val name = tool.get("name").asText()
            assertEquals(true, tool.get("strict")?.booleanValue(), "$name.strict")
            assertTrue(tool.get("description").asText().length > 80, "$name needs a real description")
            assertEquals("object", tool.get("input_schema").get("type").asText())
            assertEverySchemaObjectIsClosed(tool.get("input_schema"), name)
        }
        assertEquals(
            listOf("rows", "white_at_bottom", "side_to_move", "notes"),
            tools.get(0).get("input_schema").get("required").map { it.asText() },
        )
        assertEquals(json("""["w","b"]"""), tools.get(0).get("input_schema").get("properties").get("side_to_move").get("enum"))
        assertEquals(
            listOf("move", "explanation", "evaluation", "alternatives", "opponent_threat"),
            tools.get(1).get("input_schema").get("required").map { it.asText() },
        )
        assertEquals(
            json("""["winning","better","equal","worse","losing","unclear"]"""),
            tools.get(1).get("input_schema").get("properties").get("evaluation").get("enum"),
        )
        val alternativeItems = tools.get(1).get("input_schema").get("properties").get("alternatives").get("items")
        assertEquals(false, alternativeItems.get("additionalProperties").booleanValue())

        assertEquals(1, body1.get("messages").size())
        val user = body1.message(0)
        assertEquals("user", user.get("role").asText())
        val blocks = user.get("content")
        assertEquals(listOf("image", "image", "text"), blocks.map { it.get("type").asText() })
        assertEquals(
            json("""{"type":"base64","media_type":"image/png","data":"${Base64.getEncoder().encodeToString(PNG_BYTES)}"}"""),
            blocks.get(0).get("source"),
        )
        assertEquals(
            json("""{"type":"base64","media_type":"image/jpeg","data":"${Base64.getEncoder().encodeToString(JPEG_BYTES)}"}"""),
            blocks.get(1).get("source"),
        )
        val prompt = blocks.get(2).get("text").asText()
        assertContains(prompt, "Image 1 is the board crop")
        assertContains(prompt, "Image 2 is the full screenshot")
        assertContains(prompt, "r1 DDDDDDDD")
        assertContains(prompt, "r8 LLLLLLLL")
        assertContains(prompt, "call submit_position")

        // ---- request 2: same everything, history appended
        val second = server.take()
        assertEquals("server-side-fallback-2026-07-01", second.getHeader("anthropic-beta"))
        val raw1 = first.raw()
        val raw2 = second.raw()
        assertEquals(raw1.rest, raw2.rest, "model, system, tools, tool_choice and output_config must be byte-identical")
        assertTrue(raw2.messages.startsWith(raw1.messages + ","), "earlier messages must be resent verbatim")

        val body2 = second.json()
        assertEquals(3, body2.get("messages").size())
        assertEquals(body1.message(0), body2.message(0))
        assertEchoed(submitTurn, body2.message(1))
        assertEquals(SIGNATURE_1, body2.message(1).get("content").get(0).get("signature").asText())
        val accepted = assertToolResult(body2.message(2), "toolu_01SubmitPosition0001", isError = false)
        assertContains(accepted, START_FEN)
        assertContains(accepted, START_BRIEFING)
        assertContains(accepted, "recommend_move")
    }

    @Test
    fun `effort is sent as configured`() {
        for ((effort, wire) in listOf(Effort.LOW to "low", Effort.MEDIUM to "medium", Effort.HIGH to "high")) {
            enqueue(listOf(textBlock("I can't see a chess board on this screen.")), stopReason = "end_turn")
            client(server.config(effort = effort)).use { it.analyzeBoard(visionRequest(), FakeOracle()) }
            assertEquals(wire, server.take().json().get("output_config").get("effort").asText())
        }
    }

    // ------------------------------------------------------------ corrections

    @Test
    fun `rejected position is reported as an error and the corrected reading is accepted`() {
        val wrongRows = listOf("rnbqkbkr", "pppppppp", "........", "........", "........", "........", "PPPPPPPP", "RNBQKBNR")
        val wrongTurn = listOf(
            thinkingBlock(SIGNATURE_1),
            toolUseBlock("toolu_01Wrong", "submit_position", submitPositionInput(rows = wrongRows, notes = "r1c7 hard to read")),
        )
        val correctedTurn = listOf(
            thinkingBlock(SIGNATURE_2),
            toolUseBlock("toolu_01Corrected", "submit_position", submitPositionInput()),
        )
        enqueue(wrongTurn)
        enqueue(correctedTurn)
        enqueue(recommendTurn)
        val oracle = FakeOracle(
            positions = mutableListOf(
                PositionVerdict.Rejected(listOf("Black has 2 kings (e8, g8)", "Black has only 1 knight but 16 pieces are expected")),
                PositionVerdict.Accepted(START_FEN, START_BRIEFING),
            )
        )

        val result = client().use { it.analyzeBoard(visionRequest(), oracle) }

        assertEquals(3, server.requestCount)
        assertEquals(3, result.roundTrips)
        assertEquals("e4", result.advice?.san)
        assertEquals(START_ROWS, result.reading?.rows)
        assertEquals(listOf(wrongRows, START_ROWS), oracle.readings.map { it.rows })
        assertEquals("r1c7 hard to read", oracle.readings.first().notes)

        val first = server.take()
        val second = server.take()
        val third = server.take()

        val body2 = second.json()
        assertEquals(3, body2.get("messages").size())
        assertEchoed(wrongTurn, body2.message(1))
        val rejection = assertToolResult(body2.message(2), "toolu_01Wrong", isError = true)
        assertContains(rejection, "Black has 2 kings (e8, g8)")
        assertContains(rejection, "Black has only 1 knight")
        assertContains(rejection, "submit_position")

        val body3 = third.json()
        assertEquals(5, body3.get("messages").size())
        assertEchoed(correctedTurn, body3.message(3))
        assertToolResult(body3.message(4), "toolu_01Corrected", isError = false)

        assertEquals(first.raw().rest, second.raw().rest)
        assertEquals(second.raw().rest, third.raw().rest)
        assertTrue(second.raw().messages.startsWith(first.raw().messages + ","))
        assertTrue(third.raw().messages.startsWith(second.raw().messages + ","))
    }

    @Test
    fun `illegal move is reported as an error and the corrected move is returned`() {
        val illegalTurn = listOf(
            thinkingBlock(SIGNATURE_2),
            textBlock("Going for the quick attack."),
            toolUseBlock("toolu_01Illegal", "recommend_move", recommendMoveInput(move = "Qh5", alternatives = emptyList())),
        )
        val legalTurn = listOf(
            thinkingBlock(SIGNATURE_1, thinking = "The queen is blocked by the e-pawn."),
            toolUseBlock("toolu_01Legal", "recommend_move", recommendMoveInput(move = "Nf3", evaluation = "better", opponentThreat = "None")),
        )
        enqueue(submitTurn)
        enqueue(illegalTurn)
        enqueue(legalTurn)
        val oracle = FakeOracle()

        val result = client().use { it.analyzeBoard(visionRequest(), oracle) }

        assertEquals(3, server.requestCount)
        assertEquals(3, result.roundTrips)
        val advice = assertNotNull(result.advice)
        assertEquals("Nf3", advice.san)
        assertEquals("g1f3", advice.uci)
        assertEquals(Evaluation.BETTER, advice.evaluation)
        assertNull(advice.opponentThreat)
        assertEquals(listOf(START_FEN to "Qh5", START_FEN to "Nf3"), oracle.moveChecks.take(2))

        server.take()
        val second = server.take()
        val third = server.take()
        val body3 = third.json()
        assertEquals(5, body3.get("messages").size())
        assertEchoed(illegalTurn, body3.message(3))
        val reason = assertToolResult(body3.message(4), "toolu_01Illegal", isError = true)
        assertContains(reason, "Qh5")
        assertContains(reason, "Legal moves: e4 d4 Nf3 c4")
        assertContains(reason, "recommend_move")
        assertEquals(second.raw().rest, third.raw().rest)
        assertTrue(third.raw().messages.startsWith(second.raw().messages + ","))
    }

    @Test
    fun `malformed reading is answered without bothering the oracle`() {
        val sevenRows = START_ROWS.dropLast(1)
        enqueue(listOf(thinkingBlock(SIGNATURE_1), toolUseBlock("toolu_01Short", "submit_position", submitPositionInput(rows = sevenRows))))
        enqueue(submitTurn)
        enqueue(recommendTurn)
        val oracle = FakeOracle()

        val result = client().use { it.analyzeBoard(visionRequest(), oracle) }

        assertEquals("e4", result.advice?.san)
        assertEquals(1, oracle.readings.size)
        server.take()
        val complaint = assertToolResult(server.take().json().lastMessage(), "toolu_01Short", isError = true)
        assertContains(complaint, "7 entries")
    }

    @Test
    fun `recommend_move before any accepted position is refused`() {
        enqueue(listOf(thinkingBlock(SIGNATURE_1), toolUseBlock("toolu_01Early", "recommend_move", recommendMoveInput())))
        enqueue(submitTurn)
        enqueue(recommendTurn)
        val oracle = FakeOracle()

        val result = client().use { it.analyzeBoard(visionRequest(), oracle) }

        assertEquals(3, result.roundTrips)
        assertEquals("e4", result.advice?.san)
        server.take()
        val complaint = assertToolResult(server.take().json().lastMessage(), "toolu_01Early", isError = true)
        assertContains(complaint, "submit_position")
        // The early move was never checked against a position.
        assertEquals(START_FEN, oracle.moveChecks.first().first)
    }

    @Test
    fun `unknown tool and several tool calls in one turn each get their own result`() {
        enqueue(
            listOf(
                thinkingBlock(SIGNATURE_1),
                toolUseBlock("toolu_01Bogus", "look_closer", linkedMapOf("square" to "e4")),
                toolUseBlock("toolu_01Submit", "submit_position", submitPositionInput()),
            )
        )
        enqueue(recommendTurn)

        val result = client().use { it.analyzeBoard(visionRequest(), FakeOracle()) }

        assertEquals("e4", result.advice?.san)
        server.take()
        val results = server.take().json().lastMessage().get("content")
        assertEquals(listOf("toolu_01Bogus", "toolu_01Submit"), results.map { it.get("tool_use_id").asText() })
        assertEquals(true, results.get(0).get("is_error").asBoolean())
        assertContains(results.get(0).get("content").asText(), "look_closer")
        assertTrue(results.get(1).get("is_error") == null)
    }

    @Test
    fun `illegal and duplicate alternatives are dropped and the list is capped at two`() {
        enqueue(submitTurn)
        enqueue(
            listOf(
                thinkingBlock(SIGNATURE_2),
                toolUseBlock(
                    "toolu_01Alts", "recommend_move",
                    recommendMoveInput(
                        move = "e4",
                        alternatives = listOf(
                            linkedMapOf("move" to "Qh5", "note" to "illegal here"),
                            linkedMapOf("move" to "e4", "note" to "same as the main move"),
                            linkedMapOf("move" to "d4", "note" to "first"),
                            linkedMapOf("move" to "c4", "note" to "second"),
                        ),
                        opponentThreat = "Nothing immediate, but ...e5 next",
                    ),
                ),
            )
        )

        val advice = client().use { it.analyzeBoard(visionRequest(), FakeOracle()) }.advice

        assertEquals(listOf(Alternative("d4", "first"), Alternative("c4", "second")), advice?.alternatives)
        assertEquals("Nothing immediate, but ...e5 next", advice?.opponentThreat)
    }

    // ------------------------------------------------------------ no tool call

    @Test
    fun `text-only reply means no board and ends after one request`() {
        enqueue(listOf(thinkingBlock(SIGNATURE_1), textBlock("I can't see a chess board on this screen.")), stopReason = "end_turn", outputTokens = 41)
        val oracle = FakeOracle()

        val result = client().use { it.analyzeBoard(visionRequest(), oracle) }

        assertEquals(1, server.requestCount)
        assertEquals(1, result.roundTrips)
        assertNull(result.advice)
        assertNull(result.reading)
        assertNull(result.fen)
        assertEquals("I can't see a chess board on this screen.", result.commentary)
        assertEquals(TokenUsage(2095, 41), result.usage)
        assertTrue(oracle.readings.isEmpty() && oracle.moveChecks.isEmpty())
    }

    @Test
    fun `text instead of recommend_move is nudged once`() {
        val chatty = listOf(thinkingBlock(SIGNATURE_2), textBlock("I would play e4 here."))
        enqueue(submitTurn)
        enqueue(chatty, stopReason = "end_turn")
        enqueue(recommendTurn)

        val result = client().use { it.analyzeBoard(visionRequest(), FakeOracle()) }

        assertEquals(3, result.roundTrips)
        assertEquals("e4", result.advice?.san)
        server.take()
        val second = server.take()
        val third = server.take()
        val body3 = third.json()
        assertEquals(5, body3.get("messages").size())
        assertEchoed(chatty, body3.message(3))
        val nudge = body3.message(4)
        assertEquals("user", nudge.get("role").asText())
        assertEquals("text", nudge.get("content").get(0).get("type").asText())
        assertContains(nudge.get("content").get(0).get("text").asText(), "recommend_move")
        assertTrue(third.raw().messages.startsWith(second.raw().messages + ","))
    }

    @Test
    fun `second text-only reply after the nudge ends the analysis with the position kept`() {
        enqueue(submitTurn)
        enqueue(listOf(textBlock("I would play e4 here.")), stopReason = "end_turn")
        enqueue(listOf(textBlock("As I said, e4.")), stopReason = "end_turn")

        val result = client().use { it.analyzeBoard(visionRequest(), FakeOracle()) }

        assertEquals(3, server.requestCount)
        assertNull(result.advice)
        assertEquals(START_FEN, result.fen)
        assertEquals("As I said, e4.", result.commentary)
    }

    // ------------------------------------------------------------ limits and stop reasons

    @Test
    fun `stops at the round trip cap`() {
        repeat(3) { index ->
            enqueue(listOf(thinkingBlock(SIGNATURE_1), toolUseBlock("toolu_0$index", "submit_position", submitPositionInput())))
        }
        val oracle = FakeOracle(positions = mutableListOf(PositionVerdict.Rejected(listOf("White has no king"))))

        val result = client(server.config(maxRoundTrips = 3)).use { it.analyzeBoard(visionRequest(), oracle) }

        assertEquals(3, server.requestCount)
        assertEquals(3, result.roundTrips)
        assertNull(result.advice)
        assertNull(result.fen)
        assertEquals(START_ROWS, result.reading?.rows)
        assertEquals(3, oracle.readings.size)
        assertEquals("Claude could not produce a valid reading of the board.", result.commentary)
        assertEquals(TokenUsage(3 * 2095, 3 * 503), result.usage)
    }

    @Test
    fun `cap reached after an accepted position keeps the position`() {
        enqueue(submitTurn)
        enqueue(listOf(thinkingBlock(SIGNATURE_2), toolUseBlock("toolu_01Bad", "recommend_move", recommendMoveInput(move = "Qh5"))))

        val result = client(server.config(maxRoundTrips = 2)).use { it.analyzeBoard(visionRequest(), FakeOracle()) }

        assertEquals(2, server.requestCount)
        assertNull(result.advice)
        assertEquals(START_FEN, result.fen)
        assertEquals("Claude did not settle on a legal move.", result.commentary)
    }

    @Test
    fun `refusal is raised before content is read`() {
        server.enqueue(
            okResponse(
                messageJson(
                    content = emptyList(),
                    stopReason = "refusal",
                    stopDetails = linkedMapOf("type" to "refusal", "category" to "cyber", "explanation" to "Declined by a safety classifier."),
                )
            )
        )

        val error = assertFailsWith<ClaudeException.Refused> { client().use { it.analyzeBoard(visionRequest(), FakeOracle()) } }

        assertEquals("cyber", error.category)
        assertTrue(error.userMessage.isNotBlank())
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `refusal without stop details and with partial content is still a refusal`() {
        server.enqueue(
            okResponse(
                messageJson(
                    content = listOf(toolUseBlock("toolu_01Partial", "submit_position", linkedMapOf("rows" to START_ROWS.take(2)))),
                    stopReason = "refusal",
                )
            )
        )
        val oracle = FakeOracle()

        val error = assertFailsWith<ClaudeException.Refused> { client().use { it.analyzeBoard(visionRequest(), oracle) } }

        assertNull(error.category)
        assertTrue(oracle.readings.isEmpty(), "a refused turn's tool call must not be executed")
    }

    @Test
    fun `max_tokens is reported as truncated and the cut-off tool call is not executed`() {
        enqueue(
            listOf(thinkingBlock(SIGNATURE_1), toolUseBlock("toolu_01Cut", "submit_position", linkedMapOf("rows" to START_ROWS))),
            stopReason = "max_tokens",
            outputTokens = 16000,
        )
        val oracle = FakeOracle()

        val error = assertFailsWith<ClaudeException.Truncated> { client().use { it.analyzeBoard(visionRequest(), oracle) } }

        assertTrue(error.userMessage.isNotBlank())
        assertTrue(oracle.readings.isEmpty())
        assertEquals(1, server.requestCount)
    }

    // ------------------------------------------------------------ request shape variants

    @Test
    fun `only the models with server-side fallback get the beta header and the fallbacks field`() {
        val expectations = listOf(
            "claude-opus-5-5" to true,
            "claude-sonnet-5-5" to true,
            "claude-fable-5-2" to true,
            "claude-haiku-5-5" to false,
        )
        for ((model, optIn) in expectations) {
            assertEquals(optIn, ClaudeModels.supportsServerFallback(model), model)
            enqueue(listOf(textBlock("I can't see a chess board on this screen.")), stopReason = "end_turn", model = model)
            val result = client(server.config(model = model)).use { it.analyzeBoard(visionRequest(), FakeOracle()) }
            assertEquals(model, result.servedBy)

            val request = server.take()
            val body = request.json()
            assertEquals(model, body.get("model").asText())
            if (optIn) {
                assertEquals("server-side-fallback-2026-07-01", request.getHeader("anthropic-beta"), model)
                assertEquals("default", body.get("fallbacks").asText(), model)
            } else {
                assertNull(request.getHeader("anthropic-beta"), "$model must not get a beta header")
                assertFalse(body.has("fallbacks"), "$model must not get the fallbacks field")
            }
            for (forbidden in listOf("thinking", "temperature", "top_p", "top_k")) {
                assertFalse(body.has(forbidden), "$model: request must not send $forbidden")
            }
            assertEquals("auto", body.get("tool_choice").get("type").asText())
        }
    }

    @Test
    fun `a fallback block is echoed back where it was and the serving model is reported`() {
        val fallbackTurn = listOf(
            linkedMapOf("type" to "fallback", "from" to linkedMapOf("model" to "claude-opus-5-5"), "to" to linkedMapOf("model" to "claude-opus-4-8")),
            linkedMapOf("type" to "redacted_thinking", "data" to "EmwKAhgBEgy3va3pzix7Lq0mUO4aDFk2"),
            thinkingBlock(SIGNATURE_1, thinking = "Summarised reasoning."),
            textBlock("Reading the board."),
            toolUseBlock("toolu_01AfterFallback", "submit_position", submitPositionInput()),
        )
        enqueue(fallbackTurn, model = "claude-opus-4-8")
        enqueue(recommendTurn, model = "claude-opus-4-8")

        val result = client().use { it.analyzeBoard(visionRequest(), FakeOracle()) }

        assertEquals("claude-opus-4-8", result.servedBy)
        assertEquals("e4", result.advice?.san)
        server.take()
        val body2 = server.take().json()
        assertEquals("claude-opus-5-5", body2.get("model").asText(), "the requested model does not change")
        assertEchoed(fallbackTurn, body2.message(1))
        assertEquals(
            listOf("fallback", "redacted_thinking", "thinking", "text", "tool_use"),
            body2.message(1).get("content").map { it.get("type").asText() },
        )
    }

    @Test
    fun `a rejected fallback opt-in is dropped and the request repeated without it`() {
        server.enqueue(
            errorResponse(400, "invalid_request_error", "Unexpected value(s) `server-side-fallback-2026-07-01` for the `anthropic-beta` header.")
        )
        enqueue(submitTurn)
        enqueue(recommendTurn)

        val result = client().use { it.analyzeBoard(visionRequest(), FakeOracle()) }

        assertEquals("e4", result.advice?.san)
        assertEquals(2, result.roundTrips)
        assertEquals(3, server.requestCount)
        val rejected = server.take()
        assertEquals("server-side-fallback-2026-07-01", rejected.getHeader("anthropic-beta"))
        for (retry in listOf(server.take(), server.take())) {
            assertNull(retry.getHeader("anthropic-beta"))
            assertFalse(retry.json().has("fallbacks"))
        }
    }

    @Test
    fun `an unrelated 400 is not retried`() {
        server.enqueue(errorResponse(400, "invalid_request_error", "messages.0.content.0.image.source.base64: invalid base64 data"))

        val error = assertFailsWith<ClaudeException.BadRequest> { client().use { it.analyzeBoard(visionRequest(), FakeOracle()) } }

        assertEquals("messages.0.content.0.image.source.base64: invalid base64 data", error.apiMessage)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `a single image is described for what it is`() {
        enqueue(listOf(textBlock("No board.")), stopReason = "end_turn")
        enqueue(listOf(textBlock("No board.")), stopReason = "end_turn")
        client().use {
            it.analyzeBoard(visionRequest(board = true, screen = false), FakeOracle())
            it.analyzeBoard(visionRequest(board = false, screen = true, occupancyHint = null), FakeOracle())
        }

        val boardOnly = server.take().json().message(0).get("content")
        assertEquals(listOf("image", "text"), boardOnly.map { it.get("type").asText() })
        assertEquals("image/png", boardOnly.get(0).get("source").get("media_type").asText())
        assertContains(boardOnly.get(1).get("text").asText(), "The image is the board crop")

        val screenOnly = server.take().json().message(0).get("content")
        assertEquals(listOf("image", "text"), screenOnly.map { it.get("type").asText() })
        assertEquals("image/jpeg", screenOnly.get(0).get("source").get("media_type").asText())
        assertContains(screenOnly.get(1).get("text").asText(), "could not locate the board")
        assertFalse(screenOnly.get(1).get("text").asText().contains("Occupancy hint"))
    }

    @Test
    fun `previous position and forced side to move are passed on and the override wins`() {
        val blackToMoveFen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"
        enqueue(listOf(thinkingBlock(SIGNATURE_1), toolUseBlock("toolu_01Side", "submit_position", submitPositionInput(sideToMove = "w"))))
        enqueue(recommendTurn)
        val oracle = FakeOracle(positions = mutableListOf(PositionVerdict.Accepted(blackToMoveFen, "Legal moves: e5 c5")))

        val result = client().use {
            it.analyzeBoard(visionRequest(previousFen = START_FEN, forcedSideToMove = 'B'), oracle)
        }

        assertEquals('b', oracle.readings.single().sideToMove)
        assertEquals('b', result.reading?.sideToMove)
        val prompt = server.take().json().message(0).get("content").get(2).get("text").asText()
        assertContains(prompt, "Previous position known to the app (FEN): $START_FEN")
        assertContains(prompt, "Black is to move")
        assertContains(prompt, "side_to_move = \"b\"")
        val accepted = assertToolResult(server.take().json().lastMessage(), "toolu_01Side", isError = false)
        assertContains(accepted, "The user has set Black to move")
    }

    @Test
    fun `a trailing v1 in the base url is tolerated`() {
        enqueue(listOf(textBlock("No board.")), stopReason = "end_turn")
        val config = server.config().copy(baseUrl = server.url("/v1/").toString())

        client(config).use { it.analyzeBoard(visionRequest(), FakeOracle()) }

        assertEquals("/v1/messages", server.take().requestUrl!!.encodedPath)
    }

    // ------------------------------------------------------------ advise

    @Test
    fun `advise sends the position as text and returns the move from one request`() {
        enqueue(recommendTurn, inputTokens = 900, outputTokens = 250)
        val oracle = FakeOracle()
        val request = AdviceRequest(START_FEN, START_BRIEFING, history = listOf("e4", "e5", "Nf3"))

        val result = client().use { it.advise(request, oracle) }

        assertEquals(1, server.requestCount)
        assertEquals(1, result.roundTrips)
        assertEquals("e4", result.advice?.san)
        assertEquals(START_FEN, result.fen)
        assertNull(result.reading)
        assertEquals(TokenUsage(900, 250), result.usage)
        assertTrue(oracle.readings.isEmpty())

        val body = server.take().json()
        assertEquals(1, body.get("messages").size())
        val content = body.message(0).get("content")
        assertEquals(listOf("text"), content.map { it.get("type").asText() })
        val text = content.get(0).get("text").asText()
        assertContains(text, "FEN: $START_FEN")
        assertContains(text, START_BRIEFING)
        assertContains(text, "Moves played so far (oldest first): e4 e5 Nf3")
        assertContains(text, "recommend_move")
        assertEquals(Prompts.SYSTEM, body.get("system").get(0).get("text").asText())
        assertEquals(listOf("submit_position", "recommend_move"), body.get("tools").map { it.get("name").asText() })
        assertEquals("medium", body.get("output_config").get("effort").asText())
        assertEquals("default", body.get("fallbacks").asText())
        assertFalse(body.has("thinking"))
    }

    @Test
    fun `advise and analyzeBoard share one system prompt and tool list`() {
        enqueue(recommendTurn)
        enqueue(listOf(textBlock("No board.")), stopReason = "end_turn")
        client().use {
            it.advise(AdviceRequest(START_FEN, START_BRIEFING, emptyList()), FakeOracle())
            it.analyzeBoard(visionRequest(), FakeOracle())
        }

        assertEquals(server.take().raw().rest, server.take().raw().rest)
    }

    @Test
    fun `advise corrects an illegal move and a stray submit_position`() {
        enqueue(listOf(thinkingBlock(SIGNATURE_1), toolUseBlock("toolu_01Stray", "submit_position", submitPositionInput())))
        enqueue(listOf(thinkingBlock(SIGNATURE_2), toolUseBlock("toolu_01Illegal", "recommend_move", recommendMoveInput(move = "Ke2"))))
        enqueue(recommendTurn)
        val oracle = FakeOracle()

        val result = client().use { it.advise(AdviceRequest(START_FEN, START_BRIEFING, emptyList()), oracle) }

        assertEquals(3, result.roundTrips)
        assertEquals("e4", result.advice?.san)
        assertTrue(oracle.readings.isEmpty(), "the tracked position must not be replaced by a reading")
        server.take()
        val stray = assertToolResult(server.take().json().lastMessage(), "toolu_01Stray", isError = true)
        assertContains(stray, "already known")
        val illegal = assertToolResult(server.take().json().lastMessage(), "toolu_01Illegal", isError = true)
        assertContains(illegal, "Ke2")
    }

    @Test
    fun `advise nudges once when Claude answers in prose`() {
        enqueue(listOf(textBlock("Play e4.")), stopReason = "end_turn")
        enqueue(recommendTurn)

        val result = client().use { it.advise(AdviceRequest(START_FEN, START_BRIEFING, emptyList()), FakeOracle()) }

        assertEquals(2, result.roundTrips)
        assertEquals("e4", result.advice?.san)
        server.take()
        val body2 = server.take().json()
        assertEquals(listOf("user", "assistant", "user"), body2.get("messages").map { it.get("role").asText() })
    }

    // ------------------------------------------------------------ ping

    @Test
    fun `ping asks the models api and spends no tokens`() {
        server.enqueue(
            okResponse(
                """{"type":"model","id":"claude-opus-5-5","display_name":"Claude Opus 5.5","created_at":"2026-09-30T00:00:00Z","max_input_tokens":1000000,"max_tokens":128000}"""
            )
        )

        val served = client().use { it.ping() }

        assertEquals("claude-opus-5-5", served)
        assertEquals(1, server.requestCount)
        val request = server.take()
        assertEquals("GET", request.method)
        assertEquals("/v1/models/claude-opus-5-5", request.requestUrl!!.encodedPath)
        assertEquals("Bearer sk-ant-test-key", request.getHeader("Authorization"))
    }

    @Test
    fun `ping falls back to a minimal message when the server has no models endpoint`() {
        server.enqueue(okResponse("<html>Not Found</html>").setResponseCode(404).setHeader("content-type", "text/html"))
        enqueue(listOf(thinkingBlock(SIGNATURE_1)), stopReason = "max_tokens", model = "claude-haiku-5-5", inputTokens = 14, outputTokens = 16)

        val served = client(server.config(model = "claude-haiku-5-5")).use { it.ping() }

        assertEquals("claude-haiku-5-5", served)
        assertEquals("GET", server.take().method)
        val message = server.take()
        assertEquals("POST", message.method)
        assertEquals("/v1/messages", message.requestUrl!!.encodedPath)
        val body = message.json()
        assertEquals("claude-haiku-5-5", body.get("model").asText())
        assertTrue(body.get("max_tokens").asInt() in 1..64, "the fallback ping must stay tiny")
        assertEquals("low", body.get("output_config").get("effort").asText())
        assertEquals(1, body.get("messages").size())
        for (absent in listOf("thinking", "temperature", "tools", "tool_choice", "system", "fallbacks")) {
            assertFalse(body.has(absent), "ping message must not send $absent")
        }
    }

    @Test
    fun `ping reports an unknown model`() {
        repeat(2) { server.enqueue(errorResponse(404, "not_found_error", "model: claude-opus-9")) }

        val error = assertFailsWith<ClaudeException.ModelNotFound> {
            client(server.config(model = "claude-opus-9")).use { it.ping() }
        }

        assertEquals("claude-opus-9", error.model)
        assertContains(error.userMessage, "claude-opus-9")
    }

    @Test
    fun `ping reports a bad key from the first request`() {
        server.enqueue(errorResponse(401, "authentication_error", "invalid x-api-key"))

        assertFailsWith<ClaudeException.InvalidApiKey> { client().use { it.ping() } }
        assertEquals(1, server.requestCount)
    }

    // ------------------------------------------------------------ argument checks

    @Test
    fun `blank or unsendable api key fails without a request`() {
        for (key in listOf("   ", "", "sk-ant-api03-abc\ndef", "sk-ant api03")) {
            client(server.config().copy(apiKey = key)).use { client ->
                assertFailsWith<ClaudeException.InvalidApiKey> { client.analyzeBoard(visionRequest(), FakeOracle()) }
                assertFailsWith<ClaudeException.InvalidApiKey> { client.advise(AdviceRequest(START_FEN, START_BRIEFING, emptyList()), FakeOracle()) }
                assertFailsWith<ClaudeException.InvalidApiKey> { client.ping() }
            }
        }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `surrounding whitespace in the api key is ignored`() {
        enqueue(listOf(textBlock("No board.")), stopReason = "end_turn")

        client(server.config().copy(apiKey = "  sk-ant-test-key\n")).use { it.analyzeBoard(visionRequest(), FakeOracle()) }

        assertEquals("Bearer sk-ant-test-key", server.take().getHeader("Authorization"))
    }

    @Test
    fun `invalid arguments are rejected before any request`() {
        client().use { client ->
            assertFailsWith<IllegalArgumentException> {
                client.analyzeBoard(visionRequest(board = false, screen = false), FakeOracle())
            }
            assertFailsWith<IllegalArgumentException> {
                client.analyzeBoard(visionRequest(forcedSideToMove = 'x'), FakeOracle())
            }
            assertFailsWith<IllegalArgumentException> {
                client.analyzeBoard(
                    VisionRequest(EncodedImage(PNG_BYTES, "image/webp"), null, null, null, null),
                    FakeOracle(),
                )
            }
            assertFailsWith<IllegalArgumentException> { client.advise(AdviceRequest(" ", START_BRIEFING, emptyList()), FakeOracle()) }
        }
        for (badUrl in listOf("api.example.com", "https://", "http://exa mple.com")) {
            assertFailsWith<ClaudeException.BadRequest>(badUrl) {
                client(server.config().copy(baseUrl = badUrl)).use { it.ping() }
            }
        }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `a closed client refuses further calls`() {
        val client = client()
        client.close()
        client.close()

        assertFailsWith<IllegalStateException> { client.ping() }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `oracle exceptions pass through unchanged`() {
        enqueue(submitTurn)
        val oracle = object : PositionOracle {
            override fun submitPosition(reading: BoardReading): PositionVerdict = throw IllegalStateException("oracle broke")
            override fun checkMove(fen: String, move: String): MoveVerdict = throw AssertionError("not reached")
        }

        val error = assertFailsWith<IllegalStateException> { client().use { it.analyzeBoard(visionRequest(), oracle) } }

        assertEquals("oracle broke", error.message)
    }

    @Test
    fun `config toString does not leak the key`() {
        assertFalse(server.config().toString().contains("sk-ant-test-key"))
    }
}
