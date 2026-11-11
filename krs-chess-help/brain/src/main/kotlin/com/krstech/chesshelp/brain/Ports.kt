package com.krstech.chesshelp.brain

import com.krstech.chesshelp.claude.AdviceRequest
import com.krstech.chesshelp.claude.AnalysisResult
import com.krstech.chesshelp.claude.ClaudeConfig
import com.krstech.chesshelp.claude.Effort
import com.krstech.chesshelp.claude.EncodedImage
import com.krstech.chesshelp.claude.PositionOracle
import com.krstech.chesshelp.claude.VisionRequest
import com.krstech.chesshelp.vision.BoardRect
import com.krstech.chesshelp.vision.Frame
import kotlinx.coroutines.CoroutineDispatcher

/*
 * The seams between the analysis logic in this module (pure Kotlin, unit-tested on the JVM) and the
 * Android pieces in :app / :capture that implement them.
 */

/** Screen frames. Implemented by the MediaProjection capturer. */
interface FrameSource {
    /** The most recent captured frame, or null if nothing has been captured yet. */
    suspend fun latestFrame(): Frame?

    /**
     * Waits for a frame captured after this call started, up to [timeoutMs]; a static screen produces no
     * new frames, so on timeout this returns the latest frame instead (null only if there is none at all).
     */
    suspend fun freshFrame(timeoutMs: Long): Frame?

    /** True when frame pixel coordinates are physical screen coordinates (whole-display capture). */
    val framesMatchScreen: Boolean
}

/** The floating overlay. Always called on [BrainDispatchers.main]. */
interface OverlayPort {
    fun render(state: UiState)
    fun showArrow(arrow: ArrowSpec?)

    /** true = make every overlay window invisible so a clean screenshot can be taken. */
    fun setCaptureMode(hidden: Boolean)
    fun setAutoEnabled(enabled: Boolean)
}

/** Turns frames into the images sent to Claude. Called on [BrainDispatchers.cpu]. */
interface BoardImageEncoder {
    /** Board crop with the r1..r8 / c1..c8 margin labels the Claude prompt refers to (PNG). */
    fun encodeBoard(frame: Frame, rect: BoardRect): EncodedImage

    /** Whole screenshot scaled so its long edge is at most [maxLongEdge] (JPEG). */
    fun encodeScreen(frame: Frame, maxLongEdge: Int): EncodedImage
}

enum class AdviseFor { BOTTOM, WHITE, BLACK, BOTH }

/** Live view of the user's settings. Read on any thread. */
interface BrainSettings {
    val apiKey: String?
    val model: String
    val modelLabel: String
    val effort: Effort
    val baseUrl: String?
    val adviseFor: AdviseFor
    var autoAdvise: Boolean
    val drawOnBoard: Boolean
    val sendScreenContext: Boolean
}

/** The two Claude calls the brain needs; ClaudeChessClient satisfies it through a thin adapter. Blocking. */
interface AdviceService : AutoCloseable {
    fun analyzeBoard(request: VisionRequest, oracle: PositionOracle, onProgress: (String) -> Unit): AnalysisResult
    fun advise(request: AdviceRequest, oracle: PositionOracle, onProgress: (String) -> Unit): AnalysisResult
}

fun interface AdviceServiceFactory {
    fun create(config: ClaudeConfig): AdviceService
}

class BrainDispatchers(
    val main: CoroutineDispatcher,
    val cpu: CoroutineDispatcher,
    val io: CoroutineDispatcher,
)

// ---- What the overlay shows (mirrors the app's overlay model one-to-one) ----

/** 8 view rows top-to-bottom (FEN letters, '.' empty) plus the highlighted move in view coordinates. */
data class BoardUi(
    val rows: List<String>,
    val fromRow: Int? = null,
    val fromCol: Int? = null,
    val toRow: Int? = null,
    val toCol: Int? = null,
)

sealed interface UiState {
    data class Idle(val hint: String) : UiState
    data class Working(val message: String, val board: BoardUi? = null) : UiState
    data class Advice(
        val title: String,
        val moveSan: String,
        val evaluation: String,
        val explanation: String,
        val alternatives: List<Pair<String, String>>,
        val threat: String?,
        val source: String,
        val board: BoardUi?,
        val footer: String?,
    ) : UiState
    data class Message(val title: String, val body: String, val isError: Boolean, val board: BoardUi? = null) : UiState
}

/** Where to draw the move arrow over the real board, in screen pixels and view cells. */
data class ArrowSpec(
    val boardLeft: Int,
    val boardTop: Int,
    val boardSize: Int,
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
)
