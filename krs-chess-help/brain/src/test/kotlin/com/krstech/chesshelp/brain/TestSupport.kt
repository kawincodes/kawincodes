package com.krstech.chesshelp.brain

import com.krstech.chesshelp.chess.BoardView
import com.krstech.chesshelp.chess.Position
import com.krstech.chesshelp.claude.AdviceRequest
import com.krstech.chesshelp.claude.AnalysisResult
import com.krstech.chesshelp.claude.BoardReading
import com.krstech.chesshelp.claude.ClaudeConfig
import com.krstech.chesshelp.claude.Effort
import com.krstech.chesshelp.claude.EncodedImage
import com.krstech.chesshelp.claude.Evaluation
import com.krstech.chesshelp.claude.MoveAdvice
import com.krstech.chesshelp.claude.MoveVerdict
import com.krstech.chesshelp.claude.PositionOracle
import com.krstech.chesshelp.claude.PositionVerdict
import com.krstech.chesshelp.claude.TokenUsage
import com.krstech.chesshelp.claude.VisionRequest
import com.krstech.chesshelp.vision.BoardRect
import com.krstech.chesshelp.vision.BoardSnapshot
import com.krstech.chesshelp.vision.CellState
import com.krstech.chesshelp.vision.Frame
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope

/** Small on purpose: the fake frames are FRAME_SIDE x FRAME_SIDE and only carry two numbers. */
val BOARD_RECT = BoardRect(4, 6, 16, 0.9f)
const val FRAME_SIDE = 32

/** What is on the (fake) screen: a board shown as view rows, or no board at all. */
class Scene(
    val rows: List<String>?,
    val rect: BoardRect = BOARD_RECT,
    val reliable: Boolean = true,
    /** Cells the fake sampler misreads. */
    val misread: Map<Int, CellState> = emptyMap(),
)

fun sceneOf(position: Position, whiteAtBottom: Boolean = true, misread: Map<Int, CellState> = emptyMap()): Scene =
    Scene(BoardView.toRows(position, whiteAtBottom), misread = misread)

fun Position.after(vararg sans: String): Position {
    var position = this
    for (san in sans) position = position.play(position.parseMove(san) ?: error("illegal $san in $position"))
    return position
}

fun cellsOf(rows: List<String>): Array<CellState> = Array(64) { cell ->
    val c = rows[cell / 8][cell % 8]
    when {
        c == '.' -> CellState.EMPTY
        c.isUpperCase() -> CellState.LIGHT_PIECE
        else -> CellState.DARK_PIECE
    }
}

fun snapshotOf(rows: List<String>, reliable: Boolean = true, misread: Map<Int, CellState> = emptyMap()): BoardSnapshot {
    val cells = cellsOf(rows)
    for ((cell, state) in misread) cells[cell] = state
    return BoardSnapshot(BOARD_RECT, cells, FloatArray(64) { 1f }, reliable)
}

class FakeOverlay : OverlayPort {
    val states = ArrayList<UiState>()
    val arrows = ArrayList<ArrowSpec?>()
    val captureModes = ArrayList<Boolean>()
    var hidden = false
    var auto: Boolean? = null

    /** Changes whenever the app draws something, like the pixels of a real overlay would. */
    var stamp = 1

    val state: UiState? get() = states.lastOrNull()
    val arrow: ArrowSpec? get() = arrows.lastOrNull()
    val captures: Int get() = captureModes.count { it }

    override fun render(state: UiState) {
        states += state
        stamp++
    }

    override fun showArrow(arrow: ArrowSpec?) {
        arrows += arrow
        stamp++
    }

    override fun setCaptureMode(hidden: Boolean) {
        captureModes += hidden
        this.hidden = hidden
    }

    override fun setAutoEnabled(enabled: Boolean) {
        auto = enabled
    }
}

/** Frames carry the scene index and the overlay's stamp (0 while the overlay is hidden) instead of pixels. */
class FakeFrames(private val overlay: FakeOverlay) : FrameSource {
    val scenes = ArrayList<Scene>()
    private var index = -1
    var scene: Scene?
        get() = scenes.getOrNull(index)
        set(value) {
            if (value == null) {
                index = -1
            } else {
                scenes += value
                index = scenes.size - 1
            }
        }

    override var framesMatchScreen = true
    var freshDelayMs = 0L
    var freshFailure: Exception? = null
    var freshCalls = 0

    /** When false the overlay never shows up in frames (it does not cover the board). */
    var overlayInFrames = true

    override suspend fun latestFrame(): Frame? {
        if (index < 0) return null
        val stamp = if (overlay.hidden || !overlayInFrames) 0 else overlay.stamp
        val pixels = IntArray(FRAME_SIDE * FRAME_SIDE)
        pixels[0] = index
        pixels[1] = stamp
        return Frame(FRAME_SIDE, FRAME_SIDE, pixels)
    }

    override suspend fun freshFrame(timeoutMs: Long): Frame? {
        freshCalls++
        if (freshDelayMs > 0) delay(freshDelayMs)
        freshFailure?.let { throw it }
        return latestFrame()
    }
}

class FakeVision(private val frames: FakeFrames) : BoardVision {
    var locateCalls = 0
    var sampleCalls = 0

    /** Cells read differently in frames that contain the overlay (a panel covering part of the board). */
    var covered: Map<Int, CellState> = emptyMap()

    private fun sceneOf(frame: Frame): Scene = frames.scenes[frame.pixels[0]]

    override fun locate(frame: Frame, hint: BoardRect?): BoardRect? {
        locateCalls++
        val scene = sceneOf(frame)
        return if (scene.rows == null) null else scene.rect
    }

    override fun sample(frame: Frame, rect: BoardRect): BoardSnapshot {
        sampleCalls++
        val scene = sceneOf(frame)
        val cells = cellsOf(scene.rows!!)
        for ((cell, state) in scene.misread) cells[cell] = state
        if (frame.pixels[1] != 0) for ((cell, state) in covered) cells[cell] = state
        return BoardSnapshot(rect, cells, FloatArray(64) { 1f }, scene.reliable)
    }

    override fun fingerprint(frame: Frame, rect: BoardRect): IntArray = frame.pixels.copyOf(2)

    override fun differs(a: IntArray, b: IntArray): Boolean = !a.contentEquals(b)
}

class FakeEncoder : BoardImageEncoder {
    val boards = ArrayList<BoardRect>()
    val screens = ArrayList<Int>()

    override fun encodeBoard(frame: Frame, rect: BoardRect): EncodedImage {
        boards += rect
        return EncodedImage(byteArrayOf(1, 2, 3), "image/png")
    }

    override fun encodeScreen(frame: Frame, maxLongEdge: Int): EncodedImage {
        screens += maxLongEdge
        return EncodedImage(byteArrayOf(4, 5, 6), "image/jpeg")
    }
}

class FakeSettings : BrainSettings {
    override var apiKey: String? = "sk-test"
    override var model: String = "claude-opus-5-5"
    override var modelLabel: String = "Claude Opus 5.5"
    override var effort: Effort = Effort.MEDIUM
    override var baseUrl: String? = null
    override var adviseFor: AdviseFor = AdviseFor.BOTTOM
    override var autoAdvise: Boolean = false
    override var drawOnBoard: Boolean = true
    override var sendScreenContext: Boolean = true
}

/** Deterministic "engine": the first legal moves in generator order. */
class FakeAnalyzer : PositionAnalyzer {
    var calls = 0
    var failure: Exception? = null

    override fun analyze(position: Position, timeMs: Long): List<EngineHint> {
        calls++
        failure?.let { throw it }
        return position.legalMoves().take(3).mapIndexed { index, move ->
            val san = position.san(move)
            EngineHint(move, san, 30 - 10 * index, null, listOf(san))
        }
    }
}

class FakeClaude(private val now: () -> Long = { 0L }) : AdviceServiceFactory {
    var vision: (VisionRequest, PositionOracle, (String) -> Unit) -> AnalysisResult = { _, _, _ -> error("unexpected analyzeBoard") }
    var advice: (AdviceRequest, PositionOracle, (String) -> Unit) -> AnalysisResult = { _, _, _ -> error("unexpected advise") }

    val visionRequests = ArrayList<VisionRequest>()
    val adviceRequests = ArrayList<AdviceRequest>()
    val callTimes = ArrayList<Long>()
    val configs = ArrayList<ClaudeConfig>()
    var closed = 0
    val calls: Int get() = visionRequests.size + adviceRequests.size

    override fun create(config: ClaudeConfig): AdviceService {
        configs += config
        return object : AdviceService {
            override fun analyzeBoard(request: VisionRequest, oracle: PositionOracle, onProgress: (String) -> Unit): AnalysisResult {
                synchronized(this@FakeClaude) {
                    visionRequests += request
                    callTimes += now()
                }
                return vision(request, oracle, onProgress)
            }

            override fun advise(request: AdviceRequest, oracle: PositionOracle, onProgress: (String) -> Unit): AnalysisResult {
                synchronized(this@FakeClaude) {
                    adviceRequests += request
                    callTimes += now()
                }
                return advice(request, oracle, onProgress)
            }

            override fun close() {
                closed++
            }
        }
    }
}

val USAGE = TokenUsage(1234, 321)

/**
 * Plays Claude's part of the vision tool loop: submits [rows], resubmits the same reading once when it
 * is rejected (as Claude does when it stands by what it sees), then recommends [move].
 */
fun reads(
    rows: List<String>,
    whiteAtBottom: Boolean = true,
    side: Char = 'w',
    move: String?,
): (VisionRequest, PositionOracle, (String) -> Unit) -> AnalysisResult = { request, oracle, _ ->
    val reading = BoardReading(rows, whiteAtBottom, request.forcedSideToMove ?: side, "")
    var verdict = oracle.submitPosition(reading)
    var roundTrips = 1
    if (verdict is PositionVerdict.Rejected) {
        verdict = oracle.submitPosition(reading)
        roundTrips++
    }
    when (verdict) {
        is PositionVerdict.Rejected -> AnalysisResult(reading, null, null, "Claude could not produce a valid reading of the board.", USAGE, "claude-opus-5-5", roundTrips)
        is PositionVerdict.Accepted -> {
            val legal = move?.let { oracle.checkMove(verdict.fen, it) as? MoveVerdict.Legal }
            AnalysisResult(
                reading, verdict.fen,
                legal?.let { MoveAdvice(it.san, it.uci, "Because it is good.", Evaluation.BETTER, emptyList(), "A threat") },
                if (legal == null) "No move this time." else "",
                USAGE, "claude-opus-5-5", roundTrips + 1,
            )
        }
    }
}

fun reads(position: Position, whiteAtBottom: Boolean = true, move: String?) =
    reads(BoardView.toRows(position, whiteAtBottom), whiteAtBottom, position.sideToMove.fenChar, move)

/** Claude's part of the text-only path: recommends [move] for the given FEN. */
fun recommends(move: String): (AdviceRequest, PositionOracle, (String) -> Unit) -> AnalysisResult = { request, oracle, _ ->
    val legal = oracle.checkMove(request.fen, move) as? MoveVerdict.Legal
    AnalysisResult(
        null, request.fen,
        legal?.let { MoveAdvice(it.san, it.uci, "Develops.", Evaluation.EQUAL, emptyList(), null) },
        if (legal == null) "Claude did not settle on a legal move." else "",
        USAGE, "claude-opus-5-5", 1,
    )
}

/** A brain wired to fakes on the test scheduler. */
@OptIn(ExperimentalCoroutinesApi::class)
class Harness(
    scope: TestScope,
    tuning: BrainTuning = BrainTuning(),
    io: CoroutineDispatcher? = null,
) {
    val scheduler = scope.testScheduler
    val overlay = FakeOverlay()
    val frames = FakeFrames(overlay)
    val vision = FakeVision(frames)
    val encoder = FakeEncoder()
    val settings = FakeSettings()
    val analyzer = FakeAnalyzer()
    val claude = FakeClaude { scheduler.currentTime }
    val brain = ChessBrain(
        frames, overlay, encoder, settings, claude, scope.backgroundScope,
        BrainDispatchers(StandardTestDispatcher(scheduler), StandardTestDispatcher(scheduler), io ?: StandardTestDispatcher(scheduler)),
        clock = { scheduler.currentTime },
        analyzer = analyzer,
        vision = vision,
        tuning = tuning,
    )

    val advice: UiState.Advice get() = overlay.state as? UiState.Advice ?: error("expected Advice, got ${overlay.state}")
    val message: UiState.Message get() = overlay.state as? UiState.Message ?: error("expected Message, got ${overlay.state}")

    fun show(position: Position, whiteAtBottom: Boolean = true) {
        frames.scene = sceneOf(position, whiteAtBottom)
    }
}
