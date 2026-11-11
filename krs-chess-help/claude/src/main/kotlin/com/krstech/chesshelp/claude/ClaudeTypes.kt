package com.krstech.chesshelp.claude

/** An encoded image. [mediaType] is `"image/png"` or `"image/jpeg"`. */
class EncodedImage(val bytes: ByteArray, val mediaType: String)

/**
 * A board as Claude read it from the image.
 *
 * [rows] are 8 strings of 8 characters, top image row first, left to right; FEN letters, `.` = empty.
 * [sideToMove] is `'w'` or `'b'`.
 */
data class BoardReading(val rows: List<String>, val whiteAtBottom: Boolean, val sideToMove: Char, val notes: String)

sealed interface PositionVerdict {
    /** [briefing]: legal moves and local engine lines as plain text; it is shown to Claude as is. */
    data class Accepted(val fen: String, val briefing: String) : PositionVerdict
    data class Rejected(val problems: List<String>) : PositionVerdict
}

sealed interface MoveVerdict {
    data class Legal(val san: String, val uci: String) : MoveVerdict
    /** [reason] is shown to Claude and should include the legal move list. */
    data class Illegal(val reason: String) : MoveVerdict
}

/** Implemented by the app (with :core and :vision). Called synchronously, on the caller's thread, from inside the tool loop. */
interface PositionOracle {
    fun submitPosition(reading: BoardReading): PositionVerdict
    fun checkMove(fen: String, move: String): MoveVerdict
}

/** Assessment of the position from the point of view of the side to move. */
enum class Evaluation { WINNING, BETTER, EQUAL, WORSE, LOSING, UNCLEAR }

data class Alternative(val san: String, val note: String)

data class MoveAdvice(
    val san: String,
    val uci: String,
    val explanation: String,
    val evaluation: Evaluation,
    val alternatives: List<Alternative>,
    val opponentThreat: String?,
)

/** [inputTokens] counts every prompt token, including those written to or read from the prompt cache. */
data class TokenUsage(val inputTokens: Long, val outputTokens: Long)

data class AnalysisResult(
    /** The board as Claude last reported it; `null` on the text-only path or when Claude saw no board. Trustworthy only when [fen] is set. */
    val reading: BoardReading?,
    /** The accepted position, or `null` if no reading was accepted. */
    val fen: String?,
    /** `null` when Claude gave no (legal) recommendation. */
    val advice: MoveAdvice?,
    /** Plain text Claude wrote (e.g. "no chess board visible"), or a short note when it wrote none and there is no advice. */
    val commentary: String,
    /** Summed over all round trips. */
    val usage: TokenUsage,
    /** Model id that produced the last response (differs from the configured model after a server-side fallback). */
    val servedBy: String,
    val roundTrips: Int,
)

data class VisionRequest(
    /** Board crop with r1..r8 / c1..c8 margin labels, or `null` if the board was not located locally. */
    val boardImage: EncodedImage?,
    /** Downscaled full screenshot for context (clocks, move list), or `null`. At least one image must be non-null. */
    val screenImage: EncodedImage?,
    /** `BoardSnapshot.toAscii()` from local pixel analysis, or `null`. */
    val occupancyHint: String?,
    /** Last known position, if any. */
    val previousFen: String?,
    /** User override: `'w'`, `'b'` or `null`. */
    val forcedSideToMove: Char?,
)

/** [history]: SAN moves played so far, oldest first. */
data class AdviceRequest(val fen: String, val briefing: String, val history: List<String>)
