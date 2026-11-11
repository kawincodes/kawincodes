package com.krstech.chesshelp.claude

import com.anthropic.core.JsonValue
import com.anthropic.models.beta.messages.BetaTool

/** What Claude put into a `recommend_move` call, before the move has been checked. */
internal data class Recommendation(
    val move: String,
    val explanation: String,
    val evaluation: Evaluation,
    val alternatives: List<Alternative>,
    val opponentThreat: String?,
)

internal sealed interface Parsed<out T> {
    data class Ok<T>(val value: T) : Parsed<T>
    data class Bad(val problems: List<String>) : Parsed<Nothing>
}

/**
 * The two client tools and the parsing of their inputs.
 *
 * Schemas are written by hand and inputs are read through [JsonValue] accessors rather than
 * converted to classes: class-based conversion relies on reflection over our own types, which R8
 * would break in the release build of the app.
 */
internal object ChessTools {
    const val SUBMIT_POSITION = "submit_position"
    const val RECOMMEND_MOVE = "recommend_move"

    private const val PIECE_LETTERS = "PNBRQKpnbrqk"
    private const val MAX_ALTERNATIVES = 2

    private val submitPosition: BetaTool = tool(
        name = SUBMIT_POSITION,
        description = Prompts.SUBMIT_POSITION_DESCRIPTION,
        properties = linkedMapOf(
            "rows" to linkedMapOf(
                "type" to "array",
                "description" to Prompts.ROWS_DESCRIPTION,
                "items" to linkedMapOf("type" to "string"),
            ),
            "white_at_bottom" to linkedMapOf("type" to "boolean", "description" to Prompts.WHITE_AT_BOTTOM_DESCRIPTION),
            "side_to_move" to linkedMapOf(
                "type" to "string",
                "enum" to listOf("w", "b"),
                "description" to Prompts.SIDE_TO_MOVE_DESCRIPTION,
            ),
            "notes" to linkedMapOf("type" to "string", "description" to Prompts.NOTES_DESCRIPTION),
        ),
    )

    private val recommendMove: BetaTool = tool(
        name = RECOMMEND_MOVE,
        description = Prompts.RECOMMEND_MOVE_DESCRIPTION,
        properties = linkedMapOf(
            "move" to linkedMapOf("type" to "string", "description" to Prompts.MOVE_DESCRIPTION),
            "explanation" to linkedMapOf("type" to "string", "description" to Prompts.EXPLANATION_DESCRIPTION),
            "evaluation" to linkedMapOf(
                "type" to "string",
                "enum" to Evaluation.entries.map { it.name.lowercase() },
                "description" to Prompts.EVALUATION_DESCRIPTION,
            ),
            "alternatives" to linkedMapOf(
                "type" to "array",
                "description" to Prompts.ALTERNATIVES_DESCRIPTION,
                "items" to linkedMapOf(
                    "type" to "object",
                    "properties" to linkedMapOf(
                        "move" to linkedMapOf("type" to "string", "description" to Prompts.ALTERNATIVE_MOVE_DESCRIPTION),
                        "note" to linkedMapOf("type" to "string", "description" to Prompts.ALTERNATIVE_NOTE_DESCRIPTION),
                    ),
                    "required" to listOf("move", "note"),
                    "additionalProperties" to false,
                ),
            ),
            "opponent_threat" to linkedMapOf("type" to "string", "description" to Prompts.OPPONENT_THREAT_DESCRIPTION),
        ),
    )

    /** Same instances and same order on every request, so the serialized tool list never changes. */
    val all: List<BetaTool> = listOf(submitPosition, recommendMove)

    /** Strict tools require every property to be listed as required and every object to be closed. */
    private fun tool(name: String, description: String, properties: Map<String, Map<String, Any>>): BetaTool {
        val schemaProperties = BetaTool.InputSchema.Properties.builder()
        properties.forEach { (key, schema) -> schemaProperties.putAdditionalProperty(key, JsonValue.from(schema)) }
        return BetaTool.builder()
            .name(name)
            .description(description)
            .inputSchema(
                BetaTool.InputSchema.builder()
                    .properties(schemaProperties.build())
                    .required(properties.keys.toList())
                    .putAdditionalProperty("additionalProperties", JsonValue.from(false))
                    .build()
            )
            .strict(true)
            .build()
    }

    fun parseReading(input: JsonValue): Parsed<BoardReading> {
        val fields = input.asObject().orElse(null)
            ?: return Parsed.Bad(listOf("The tool input must be a JSON object."))
        val problems = ArrayList<String>()

        val rawRows = fields["rows"]?.asArray()?.orElse(null)
        val rows = ArrayList<String>()
        if (rawRows == null) {
            problems += "\"rows\" is missing; it must be an array of 8 strings."
        } else {
            if (rawRows.size != 8) problems += "\"rows\" has ${rawRows.size} entries; it needs exactly 8, one per board row."
            rawRows.forEachIndexed { index, value ->
                val label = "Row r${index + 1}"
                val text = value.asString().orElse(null)
                if (text == null) {
                    problems += "$label is not a string."
                    return@forEachIndexed
                }
                val row = normalizeRow(text)
                val bad = row.firstOrNull { it != '.' && it !in PIECE_LETTERS }
                when {
                    bad != null -> problems += "$label (\"$text\") contains '$bad', which is neither a piece letter nor '.'."
                    row.length != 8 -> problems += "$label (\"$text\") has ${row.length} squares; every row needs exactly 8."
                    else -> rows += row
                }
            }
        }

        val whiteAtBottom = fields["white_at_bottom"]?.asBoolean()?.orElse(null)
        if (whiteAtBottom == null) problems += "\"white_at_bottom\" is missing; it must be true or false."

        val side = when (fields["side_to_move"]?.asString()?.orElse(null)?.trim()?.lowercase()) {
            "w", "white" -> 'w'
            "b", "black" -> 'b'
            else -> null
        }
        if (side == null) problems += "\"side_to_move\" must be \"w\" or \"b\"."

        if (problems.isNotEmpty() || whiteAtBottom == null || side == null) return Parsed.Bad(problems)
        val notes = fields["notes"]?.asString()?.orElse(null)?.trim().orEmpty()
        return Parsed.Ok(BoardReading(rows, whiteAtBottom, side, notes))
    }

    fun parseRecommendation(input: JsonValue): Parsed<Recommendation> {
        val fields = input.asObject().orElse(null)
            ?: return Parsed.Bad(listOf("The tool input must be a JSON object."))
        val move = fields["move"]?.asString()?.orElse(null)?.trim().orEmpty()
        if (move.isEmpty()) return Parsed.Bad(listOf("\"move\" is missing; it must be a move in SAN."))

        val evaluation = fields["evaluation"]?.asString()?.orElse(null)?.trim()?.uppercase()
            ?.let { name -> Evaluation.entries.firstOrNull { it.name == name } }
            ?: Evaluation.UNCLEAR

        val alternatives = fields["alternatives"]?.asArray()?.orElse(null).orEmpty().mapNotNull { entry ->
            val alternative = entry.asObject().orElse(null) ?: return@mapNotNull null
            val san = alternative["move"]?.asString()?.orElse(null)?.trim().orEmpty()
            if (san.isEmpty()) null
            else Alternative(san, alternative["note"]?.asString()?.orElse(null)?.trim().orEmpty())
        }

        val threat = fields["opponent_threat"]?.asString()?.orElse(null)?.trim().orEmpty()
        return Parsed.Ok(
            Recommendation(
                move = move,
                explanation = fields["explanation"]?.asString()?.orElse(null)?.trim().orEmpty(),
                evaluation = evaluation,
                alternatives = alternatives.take(MAX_ALTERNATIVES * 2),
                opponentThreat = threat.takeUnless { it.isEmpty() || it.lowercase().trimEnd('.') in NO_THREAT },
            )
        )
    }

    const val ALTERNATIVE_LIMIT: Int = MAX_ALTERNATIVES

    private val NO_THREAT = setOf("none", "no", "n/a", "na", "-", "nothing", "no threat", "no real threat")

    /**
     * Tolerates the two harmless deviations that would otherwise cost a round trip: spaces between
     * the squares, and FEN-style digits for runs of empty squares.
     */
    private fun normalizeRow(text: String): String {
        val row = StringBuilder(8)
        for (c in text) {
            when {
                c.isWhitespace() -> Unit
                c in '1'..'8' -> repeat(c - '0') { row.append('.') }
                else -> row.append(c)
            }
        }
        return row.toString()
    }
}
