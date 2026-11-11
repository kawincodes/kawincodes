package com.krstech.chesshelp.claude

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.util.concurrent.TimeUnit

internal val MAPPER = ObjectMapper()

internal const val START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
internal val START_ROWS = listOf(
    "rnbqkbnr", "pppppppp", "........", "........", "........", "........", "PPPPPPPP", "RNBQKBNR",
)
internal const val START_BRIEFING =
    "Side to move: White\n" +
        "Legal moves (20): a3 a4 b3 b4 c3 c4 d3 d4 e3 e4 f3 f4 g3 g4 h3 h4 Na3 Nc3 Nf3 Nh3\n" +
        "Local engine (depth 6): 1. e4 (+0.25) e5 Nf3; 2. d4 (+0.22) d5 c4; 3. Nf3 (+0.18) d5 d4"

/** A signature the way the API returns one: opaque base64 that must come back byte for byte. */
internal const val SIGNATURE_1 = "EqQBCkgIBxABGAIiQL2j4VvE7k0yQmXw3r1cS9aZf0n8uT+G5hJ/d6LpWq1oYb7xN2eKcR4sTtUuVvWwXxYyZz0011223344556677889900AABBCCDD=="
internal const val SIGNATURE_2 = "ErUBCkYIBxABGAIqQJ9mN8bV7cX6zL5kK4jH3gF2dS1aP0oI9uY8tR7eW6qZzYyXxWwVvUuTtSsRrQqPpOoNnMmLlKkJjIiHhGgFfEeDdCcBbAa=="

internal fun json(text: String): JsonNode = MAPPER.readTree(text)

internal fun toJson(value: Any?): String = MAPPER.writeValueAsString(value)

internal fun thinkingBlock(signature: String, thinking: String = ""): Map<String, Any?> =
    linkedMapOf("type" to "thinking", "thinking" to thinking, "signature" to signature)

internal fun textBlock(text: String): Map<String, Any?> =
    linkedMapOf("type" to "text", "text" to text, "citations" to null)

internal fun toolUseBlock(id: String, name: String, input: Map<String, Any?>): Map<String, Any?> =
    linkedMapOf(
        "type" to "tool_use",
        "id" to id,
        "name" to name,
        "input" to input,
        "caller" to linkedMapOf("type" to "direct"),
    )

internal fun submitPositionInput(
    rows: List<String> = START_ROWS,
    whiteAtBottom: Boolean = true,
    sideToMove: String = "w",
    notes: String = "",
): Map<String, Any?> = linkedMapOf(
    "rows" to rows,
    "white_at_bottom" to whiteAtBottom,
    "side_to_move" to sideToMove,
    "notes" to notes,
)

internal fun recommendMoveInput(
    move: String = "e4",
    explanation: String = "Takes the centre and opens lines for the queen and bishop.",
    evaluation: String = "equal",
    alternatives: List<Map<String, Any?>> = listOf(
        linkedMapOf("move" to "d4", "note" to "Equally principled"),
        linkedMapOf("move" to "Nf3", "note" to "Flexible"),
    ),
    opponentThreat: String = "",
): Map<String, Any?> = linkedMapOf(
    "move" to move,
    "explanation" to explanation,
    "evaluation" to evaluation,
    "alternatives" to alternatives,
    "opponent_threat" to opponentThreat,
)

/** A Messages API response body with every field the real API sends. */
internal fun messageJson(
    content: List<Map<String, Any?>>,
    stopReason: String = "tool_use",
    model: String = "claude-opus-5-5",
    id: String = "msg_01XFDUDYJgAACzvnptvVoYEL",
    inputTokens: Long = 2095,
    outputTokens: Long = 503,
    cacheCreationInputTokens: Long = 0,
    cacheReadInputTokens: Long = 0,
    stopDetails: Map<String, Any?>? = null,
): String = toJson(
    linkedMapOf(
        "id" to id,
        "type" to "message",
        "role" to "assistant",
        "model" to model,
        "content" to content,
        "stop_reason" to stopReason,
        "stop_sequence" to null,
        "stop_details" to stopDetails,
        "usage" to linkedMapOf(
            "input_tokens" to inputTokens,
            "cache_creation_input_tokens" to cacheCreationInputTokens,
            "cache_read_input_tokens" to cacheReadInputTokens,
            "cache_creation" to linkedMapOf("ephemeral_5m_input_tokens" to cacheCreationInputTokens, "ephemeral_1h_input_tokens" to 0),
            "output_tokens" to outputTokens,
            "service_tier" to "standard",
        ),
    )
)

internal fun okResponse(body: String): MockResponse =
    MockResponse()
        .setResponseCode(200)
        .setHeader("content-type", "application/json")
        .setHeader("request-id", "req_011CSHoEeqs5C35K2UUqR7Fy")
        .setBody(body)

internal fun errorResponse(status: Int, type: String, message: String): MockResponse =
    MockResponse()
        .setResponseCode(status)
        .setHeader("content-type", "application/json")
        .setHeader("request-id", "req_011CSHoEeqs5C35K2UUqR7Fy")
        .setBody(
            toJson(
                linkedMapOf(
                    "type" to "error",
                    "error" to linkedMapOf("type" to type, "message" to message),
                    "request_id" to "req_011CSHoEeqs5C35K2UUqR7Fy",
                )
            )
        )

internal fun MockWebServer.take(): RecordedRequest =
    takeRequest(5, TimeUnit.SECONDS) ?: throw AssertionError("Expected another request to have been sent")

internal fun RecordedRequest.json(): JsonNode = MAPPER.readTree(body.clone().readUtf8())

internal fun MockWebServer.config(
    model: String = ClaudeModels.DEFAULT,
    effort: Effort = Effort.MEDIUM,
    maxRoundTrips: Int = 6,
    maxRetries: Int = 0,
    requestTimeoutSeconds: Long = 20,
): ClaudeConfig = ClaudeConfig(
    apiKey = "sk-ant-test-key",
    model = model,
    effort = effort,
    baseUrl = url("/").toString(),
    requestTimeoutSeconds = requestTimeoutSeconds,
    maxRoundTrips = maxRoundTrips,
    maxRetries = maxRetries,
)

/** Scripted stand-in for the app's oracle; records what it was asked. */
internal class FakeOracle(
    private val positions: MutableList<PositionVerdict> = mutableListOf(PositionVerdict.Accepted(START_FEN, START_BRIEFING)),
    private val legalMoves: Map<String, String> = mapOf("e4" to "e2e4", "d4" to "d2d4", "Nf3" to "g1f3", "c4" to "c2c4"),
) : PositionOracle {
    val readings = ArrayList<BoardReading>()
    val moveChecks = ArrayList<Pair<String, String>>()

    override fun submitPosition(reading: BoardReading): PositionVerdict {
        readings += reading
        return if (positions.size > 1) positions.removeAt(0) else positions.first()
    }

    override fun checkMove(fen: String, move: String): MoveVerdict {
        moveChecks += fen to move
        val uci = legalMoves[move]
        return if (uci != null) MoveVerdict.Legal(move, uci)
        else MoveVerdict.Illegal("Legal moves: ${legalMoves.keys.joinToString(" ")}")
    }
}

internal val PNG_BYTES = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3, 4)
internal val JPEG_BYTES = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 9, 8, 7)

internal fun visionRequest(
    board: Boolean = true,
    screen: Boolean = true,
    occupancyHint: String? = "DDDDDDDD\nDDDDDDDD\n........\n........\n........\n........\nLLLLLLLL\nLLLLLLLL",
    previousFen: String? = null,
    forcedSideToMove: Char? = null,
): VisionRequest = VisionRequest(
    boardImage = if (board) EncodedImage(PNG_BYTES, "image/png") else null,
    screenImage = if (screen) EncodedImage(JPEG_BYTES, "image/jpeg") else null,
    occupancyHint = occupancyHint,
    previousFen = previousFen,
    forcedSideToMove = forcedSideToMove,
)
