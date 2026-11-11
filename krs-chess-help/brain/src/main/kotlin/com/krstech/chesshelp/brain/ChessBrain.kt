package com.krstech.chesshelp.brain

import com.krstech.chesshelp.chess.Move
import com.krstech.chesshelp.chess.MoveInference
import com.krstech.chesshelp.chess.MoveTracker
import com.krstech.chesshelp.chess.Position
import com.krstech.chesshelp.chess.Side
import com.krstech.chesshelp.claude.AdviceRequest
import com.krstech.chesshelp.claude.AnalysisResult
import com.krstech.chesshelp.claude.ClaudeConfig
import com.krstech.chesshelp.claude.ClaudeException
import com.krstech.chesshelp.claude.VisionRequest
import com.krstech.chesshelp.vision.BoardRect
import com.krstech.chesshelp.vision.BoardSnapshot
import com.krstech.chesshelp.vision.CellState
import com.krstech.chesshelp.vision.Frame
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/** Timings and limits of [ChessBrain]. The defaults are the shipping values; tests shorten some. */
class BrainTuning(
    /** How long to wait for a frame captured after the overlay was hidden. */
    val freshFrameMs: Long = 300,
    /** Extra time for the compositor to really drop the overlay from the picture. */
    val compositorMs: Long = 80,
    /** Auto mode: how often the board fingerprint is taken. */
    val pollMs: Long = 250,
    /** Auto mode: how long a changed board must hold still before it is analysed. */
    val stableMs: Long = 600,
    /** Auto mode: how long after the app's own drawing a new baseline may be taken. */
    val settleMs: Long = 350,
    /** Minimum time between the starts of two Claude calls. */
    val minClaudeGapMs: Long = 3_000,
    /** Automatic Claude calls allowed in a row without any user action. */
    val maxAutoClaudeCalls: Int = 60,
    /** Time budget of one local engine search. */
    val engineMs: Long = 700,
    /** How long a new analysis waits for a cancelled one to wind down before it starts anyway. */
    val cancelGraceMs: Long = 2_000,
    /** Auto mode: delay before looking for the board again after a rotation or resize. */
    val screenChangeDelayMs: Long = 700,
)

/**
 * The analysis coordinator: capture, locate, read, track, advise, and the auto loop that watches the
 * board for moves.
 *
 * All state lives on [BrainDispatchers.main]. The public methods may be called from any thread; each
 * one only queues its work there. Only one analysis runs at a time.
 *
 * @param clock milliseconds used for the rate limits and the auto loop's timing. The default is
 *   monotonic; a wall clock works too (a jump only shortens or restarts a wait, never extends it).
 * @param analyzer the local engine; replaceable for tests.
 * @param vision the pixel analysis; replaceable for tests.
 */
class ChessBrain(
    private val frames: FrameSource,
    private val overlay: OverlayPort,
    private val encoder: BoardImageEncoder,
    private val settings: BrainSettings,
    private val claude: AdviceServiceFactory,
    private val scope: CoroutineScope,
    private val dispatchers: BrainDispatchers,
    private val clock: () -> Long = MONOTONIC_MILLIS,
    private val analyzer: PositionAnalyzer = EnginePositionAnalyzer(),
    private val vision: BoardVision = PixelBoardVision,
    private val tuning: BrainTuning = BrainTuning(),
) {
    private enum class Trigger(val user: Boolean) {
        /** Bubble tap. */
        TAP(true),

        /** "Rescan": read the board with Claude even though a position is tracked. */
        RESCAN(true),

        /** The auto loop saw the board change. */
        AUTO(false),

        /** The auto loop suspects that a move slipped in while the last analysis was running. */
        VERIFY(false),
    }

    private class Shot(val frame: Frame, val rect: BoardRect?, val snapshot: BoardSnapshot?)

    /** What was last worked out for a position, so it can be shown again without asking anyone. */
    private class Presented(
        val position: Position,
        val whiteAtBottom: Boolean,
        val state: UiState,
        val move: Move?,
        /** Claude should have answered but did not (failure, no key): a tap asks again. */
        val retryable: Boolean,
    )

    private class Running(val job: Job, val user: Boolean)

    // ---- guarded by [lock]: touched by stop() and by the Claude call thread ----
    private val lock = Any()
    private var session: CoroutineScope? = null
    private var service: AdviceService? = null
    private var serviceConfig: ClaudeConfig? = null

    // ---- confined to dispatchers.main ----
    private var tracked: Tracked? = null
    private var presented: Presented? = null

    /** Where the board was last found; forgotten when the screen geometry changes. */
    private var lastRect: BoardRect? = null

    /** The last clean capture showed no board. */
    private var boardLost = false
    private var screenEpoch = 0

    private var lastState: UiState? = null
    private var lastArrow: ArrowSpec? = null

    private var running: Running? = null

    /** Cancelled analyses that may still be unwinding (a blocking call that has not returned yet). */
    private val winding = ArrayList<Job>()
    private var autoJob: Job? = null
    private var screenJob: Job? = null

    private var autoClaudeCalls = 0
    private var autoPaused = false
    private var lastClaudeCallAt: Long? = null

    private val watcher = ChangeWatcher(tuning.stableMs, tuning.settleMs, vision::differs)

    /** The last analysis took time or changed the position: check that the screen still shows it. */
    private var verifyPending = false

    /** Cells that differed from the tracked reading when the running verification was started. */
    private var verifyDiff: BooleanArray? = null

    /** Cells the app's own windows cover: a verification found them different while the board was unchanged. */
    private val overlayMask = BooleanArray(64)

    // ------------------------------------------------------------------ public API

    /** Renders the idle state and starts the auto loop when the user has auto-advise on. */
    fun start() {
        val created = synchronized(lock) {
            if (session != null) return
            CoroutineScope(scope.coroutineContext + SupervisorJob(scope.coroutineContext[Job])).also { session = it }
        }
        created.launch(dispatchers.main) { begin() }
    }

    /** Cancels everything and closes the Claude client. [start] may be called again afterwards. */
    fun stop() {
        val ended: CoroutineScope?
        val client: AdviceService?
        synchronized(lock) {
            ended = session
            session = null
            client = service
            service = null
            serviceConfig = null
        }
        ended?.cancel()
        if (client != null) closeQuietly(client)
    }

    /** Bubble tap: analyse what is on screen now. Ignored while a user-started analysis is running. */
    fun onAnalyze() = post { startCycle(Trigger.TAP) }

    /** Forces a fresh reading of the board by Claude; restarts a running analysis. */
    fun onRescan() = post { startCycle(Trigger.RESCAN) }

    fun onToggleSideToMove() = post { toggleSideToMove() }

    fun onAutoChanged(enabled: Boolean) = post { autoChanged(enabled) }

    fun onFlipBoard() = post { flipBoard() }

    /** Rotation or a capture size change: the cached board rectangle no longer means anything. */
    fun onScreenChanged() = post { screenChanged() }

    // ------------------------------------------------------------------ commands (main)

    private fun currentSession(): CoroutineScope? = synchronized(lock) { session }

    private fun post(action: () -> Unit) {
        val current = currentSession() ?: return
        current.launch(dispatchers.main) {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                showFailure(failure)
            }
        }
    }

    private fun begin() {
        tracked = null
        presented = null
        lastRect = null
        boardLost = false
        autoClaudeCalls = 0
        autoPaused = false
        verifyPending = false
        overlayMask.fill(false)
        lastState = null
        lastArrow = null
        overlay.showArrow(null)
        overlay.setAutoEnabled(settings.autoAdvise)
        render(UiState.Idle(if (claudeConfig() == null) BrainUi.IDLE_HINT_NO_KEY else BrainUi.IDLE_HINT))
        if (settings.autoAdvise) startAutoLoop()
    }

    private fun userActed() {
        autoClaudeCalls = 0
        autoPaused = false
    }

    private fun startCycle(trigger: Trigger) {
        userActed()
        val current = running
        if (trigger == Trigger.TAP && current != null && current.user && current.job.isActive) return
        launchAnalysis(user = true) { runCycle(trigger) }
    }

    private fun toggleSideToMove() {
        userActed()
        val current = tracked
        if (current == null) {
            render(UiState.Message(BrainUi.NOTHING_TRACKED_TITLE, BrainUi.NOTHING_TRACKED_BODY, isError = false))
            return
        }
        val side = current.position.sideToMove.opposite
        val problems = Tracking.sideProblems(current.position, side)
        if (problems.isNotEmpty()) {
            render(
                UiState.Message(
                    BrainUi.CANNOT_SWITCH_TITLE,
                    "${BrainUi.sideName(side)} cannot be the side to move here: ${problems.first()}",
                    isError = true,
                    board = BrainUi.board(current.position, current.whiteAtBottom),
                )
            )
            return
        }
        val next = current.with(position = current.position.withSideToMove(side), sideForced = true)
        retarget(next)
    }

    private fun flipBoard() {
        userActed()
        val current = tracked
        if (current == null) {
            render(UiState.Message(BrainUi.NOTHING_TRACKED_TITLE, BrainUi.NOTHING_TRACKED_BODY, isError = false))
            return
        }
        val flipped = Tracking.flipped(current)
        val next = flipped.tracked
        if (next == null) {
            render(
                UiState.Message(
                    BrainUi.CANNOT_FLIP_TITLE,
                    "Seen from the other side this is not a possible position: ${flipped.problems.first()}",
                    isError = true,
                    board = BrainUi.board(current.position, current.whiteAtBottom),
                )
            )
            return
        }
        retarget(next)
    }

    /** The user corrected the tracked position: drop what was shown for the old one and advise again. */
    private fun retarget(next: Tracked) {
        // Nothing of the running analysis can interfere: it only runs on this thread, and launchAnalysis
        // cancels it before it gets another turn.
        tracked = next
        presented = null
        launchAnalysis(user = true) { advise(next, Trigger.TAP) }
    }

    private fun autoChanged(enabled: Boolean) {
        userActed()
        settings.autoAdvise = enabled
        overlay.setAutoEnabled(enabled)
        if (enabled) {
            // Nobody watched while auto was off: compare the screen with the tracked reading once.
            verifyPending = true
            startAutoLoop()
        } else {
            autoJob?.cancel()
            autoJob = null
            screenJob?.cancel()
            screenJob = null
            running?.let { if (!it.user) it.job.cancel() }
        }
    }

    private fun screenChanged() {
        screenEpoch++
        lastRect = null
        overlayMask.fill(false)
        setArrow(null)
        watcher.reset(clock())
        screenJob?.cancel()
        screenJob = null
        val current = currentSession() ?: return
        if (autoJob?.isActive != true || tracked == null) return
        // The fingerprint watch needs a rectangle, so look for the board once on the app's own initiative.
        screenJob = current.launch(dispatchers.main) {
            delay(tuning.screenChangeDelayMs)
            while (analysisActive()) delay(tuning.pollMs)
            if (lastRect == null && !autoPaused && tracked != null) launchAnalysis(user = false) { runCycle(Trigger.AUTO) }
        }
    }

    // ------------------------------------------------------------------ one analysis at a time

    private fun analysisActive(): Boolean = running?.job?.isActive == true

    /**
     * Cancels the running analysis and starts [block] once that one has let go. [block] returns whether
     * the screen should be verified afterwards (see [verifyPending]).
     */
    private fun launchAnalysis(user: Boolean, block: suspend () -> Boolean) {
        val current = currentSession() ?: return
        running?.job?.let {
            if (!it.isCompleted) {
                it.cancel()
                winding += it
            }
        }
        winding.removeAll { it.isCompleted }
        val predecessors = winding.toList()
        // LAZY so that [running] names this job before any of its code runs, whatever the dispatcher.
        val job = current.launch(dispatchers.main, start = CoroutineStart.LAZY) {
            val self = coroutineContext[Job]
            var verify = false
            try {
                if (predecessors.isNotEmpty()) {
                    // Normally immediate. The limit only matters if a blocking call ignores its interrupt:
                    // the user must not wait for a request nobody wants any more.
                    withTimeoutOrNull(tuning.cancelGraceMs) { predecessors.joinAll() }
                }
                verify = block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                coroutineContext.ensureActive()
                showFailure(failure)
            } finally {
                // The overlay was hidden and shown again and probably redrawn: the old baseline is void.
                watcher.reset(clock())
                if (running?.job === self) verifyPending = verify
            }
        }
        running = Running(job, user)
        job.start()
    }

    private fun showFailure(failure: Exception) {
        setArrow(null)
        val detail = failure.message?.trim().orEmpty().ifEmpty { failure.javaClass.simpleName }
        render(UiState.Message(BrainUi.FAILURE_TITLE, detail, isError = true, board = trackedBoard()))
    }

    // ------------------------------------------------------------------ the cycle

    private suspend fun runCycle(trigger: Trigger): Boolean {
        if (trigger.user) showWorking(BrainUi.LOOKING)
        val epoch = screenEpoch
        val shot = capture()
        if (shot == null) {
            if (trigger.user) {
                render(UiState.Message(BrainUi.NO_FRAME_TITLE, BrainUi.NO_FRAME_BODY, isError = true, board = trackedBoard()))
            }
            return false
        }
        // A frame taken across a rotation still shows a readable board, but not where the screen has it now.
        if (shot.rect != null && epoch == screenEpoch) lastRect = shot.rect
        boardLost = shot.rect == null

        val current = tracked
        if (current != null && trigger != Trigger.RESCAN) return track(shot, trigger, current)
        if (trigger == Trigger.VERIFY) return false
        val snapshot = shot.snapshot
        val inference = if (current != null && snapshot != null && snapshot.reliable) infer(current, snapshot) else null
        return visionSync(shot, trigger, current, inference)
    }

    /**
     * Hides the overlay, takes a frame without it, and always shows the overlay again; then finds and
     * samples the board. Returns `null` when no frame has been captured yet.
     */
    private suspend fun capture(): Shot? {
        overlay.setCaptureMode(true)
        val captured = try {
            frames.freshFrame(tuning.freshFrameMs)
            delay(tuning.compositorMs)
            frames.latestFrame().also { overlay.setCaptureMode(false) }
        } finally {
        }
        val frame = captured ?: return null
        val hint = lastRect?.takeIf { it.fitsIn(frame.width, frame.height) }
        return withContext(dispatchers.cpu) {
            val rect = vision.locate(frame, hint)
            Shot(frame, rect, rect?.let { vision.sample(frame, it) })
        }
    }

    private suspend fun infer(current: Tracked, snapshot: BoardSnapshot): MoveInference =
        withContext(dispatchers.cpu) {
            MoveTracker.infer(current.position, Tracking.observedOccupancy(current, snapshot))
        }

    // ------------------------------------------------------------------ tracking

    private suspend fun track(shot: Shot, trigger: Trigger, current: Tracked): Boolean {
        val snapshot = shot.snapshot
        if (snapshot == null) {
            // A position that was never matched to pixels (Claude found the board in a screenshot the
            // locator could not handle) cannot be followed locally: a tap has to ask Claude again.
            if (trigger.user && current.snapshot == null) return visionSync(shot, trigger, current, null)
            setArrow(null)
            render(
                UiState.Message(
                    BrainUi.NOT_VISIBLE_TITLE,
                    BrainUi.NOT_VISIBLE_BODY,
                    isError = false,
                    board = BrainUi.board(current.position, current.whiteAtBottom),
                )
            )
            return false
        }
        if (!snapshot.reliable) return lostTrack(shot, trigger, current, null)

        when (val inference = infer(current, snapshot)) {
            is MoveInference.NoChange -> {
                val anchored = current.with(snapshot = snapshot)
                tracked = anchored
                if (trigger == Trigger.VERIFY) verifyDiff?.copyInto(overlayMask)
                return unchanged(anchored, trigger)
            }
            is MoveInference.Played -> {
                // Pixels cannot tell which piece a pawn became.
                if (inference.promotionAssumed) return lostTrack(shot, trigger, current, inference)
                val next = current.with(
                    position = inference.position,
                    history = (current.history + inference.sans).takeLast(Tracking.MAX_HISTORY),
                    snapshot = snapshot,
                )
                tracked = next
                return moved(next, inference, trigger)
            }
            is MoveInference.Ambiguous, MoveInference.NoMatch -> return lostTrack(shot, trigger, current, inference)
        }
    }

    private suspend fun lostTrack(shot: Shot, trigger: Trigger, current: Tracked, inference: MoveInference?): Boolean {
        // A verification is the app's own second look; it must never spend a Claude call on a reading
        // it cannot explain, or an unreadable board would feed itself. The next real change resyncs.
        return visionSync(shot, trigger, current, inference)
    }

    private suspend fun unchanged(current: Tracked, trigger: Trigger): Boolean {
        val shown = presented
        if (shown != null && shown.position == current.position && shown.whiteAtBottom == current.whiteAtBottom) {
            if (trigger == Trigger.TAP && shown.retryable) return advise(current, trigger)
            show(shown)
            return false
        }
        if (isAdvised(current) || current.position.legalMoves().isEmpty()) return advise(current, trigger)
        present(current, BrainUi.waiting(current.position, current.whiteAtBottom, null, null), null, retryable = false)
        return false
    }

    private suspend fun moved(current: Tracked, played: MoveInference.Played, trigger: Trigger): Boolean {
        val position = current.position
        val sentence = BrainUi.playedSentence(played.sans, position)
        if (position.legalMoves().isEmpty()) {
            present(current, BrainUi.gameOver(position, current.whiteAtBottom, sentence), null, retryable = false)
            return true
        }
        if (isAdvised(current)) return advise(current, trigger)
        present(
            current,
            BrainUi.waiting(position, current.whiteAtBottom, sentence, played.moves.lastOrNull()),
            null,
            retryable = false,
        )
        return true
    }

    private fun isAdvised(current: Tracked): Boolean {
        val side = current.position.sideToMove
        return when (settings.adviseFor) {
            AdviseFor.BOTH -> true
            AdviseFor.WHITE -> side == Side.WHITE
            AdviseFor.BLACK -> side == Side.BLACK
            AdviseFor.BOTTOM -> side == if (current.whiteAtBottom) Side.WHITE else Side.BLACK
        }
    }

    // ------------------------------------------------------------------ advice for a tracked position

    private suspend fun advise(current: Tracked, trigger: Trigger): Boolean {
        val position = current.position
        if (position.legalMoves().isEmpty()) {
            present(current, BrainUi.gameOver(position, current.whiteAtBottom, null), null, retryable = false)
            return true
        }
        showWorking(BrainUi.THINKING)
        val lines = engineLines(position)
        val config = claudeConfig()
        if (config == null) {
            showEngineAdvice(current, lines, BrainUi.NO_KEY_NOTE, suffix = "")
            return true
        }
        if (!admitClaudeCall(trigger, BrainUi.ASKING)) return false
        val briefing = Briefings.build(position, lines)
        val oracle = BrainOracle.forKnownPosition(position, current.whiteAtBottom, current.history, briefing)
        val request = AdviceRequest(position.toFen(), briefing.text, current.history)
        val result = try {
            callClaude(config) { client, progress -> client.advise(request, oracle, progress) }
        } catch (failure: ClaudeException) {
            currentCoroutineContext().ensureActive()
            showEngineAdvice(current, lines, failure.userMessage)
            return true
        }
        showClaudeResult(current, lines, result)
        return true
    }

    private suspend fun engineLines(position: Position): List<EngineHint> =
        withContext(dispatchers.cpu) {
            runInterruptible { analyzer.safeLines(position, tuning.engineMs) }
        }

    private fun showClaudeResult(current: Tracked, lines: List<EngineHint>, result: AnalysisResult) {
        val position = current.position
        val advice = result.advice
        val move = advice?.let { position.parseMove(it.uci) ?: position.parseMove(it.san) }
        if (advice != null && move != null) {
            val state = BrainUi.claudeAdvice(
                position, current.whiteAtBottom, move, advice, result, settings.model, settings.modelLabel,
            )
            present(current, state, move, retryable = false)
        } else {
            showEngineAdvice(current, lines, result.commentary.trim().ifEmpty { BrainUi.NO_MOVE_FROM_CLAUDE })
        }
    }

    /** Claude is not the source this time: say why ([reason]) and show the local engine's best move. */
    private fun showEngineAdvice(
        current: Tracked,
        lines: List<EngineHint>,
        reason: String,
        suffix: String = BrainUi.ENGINE_INSTEAD,
    ) {
        if (lines.isEmpty()) {
            val state = UiState.Message(
                BrainUi.NO_RECOMMENDATION_TITLE,
                reason + BrainUi.ENGINE_EMPTY,
                isError = true,
                board = BrainUi.board(current.position, current.whiteAtBottom),
            )
            present(current, state, null, retryable = true)
            return
        }
        val state = BrainUi.engineAdvice(current.position, current.whiteAtBottom, lines, reason + suffix)
        present(current, state, lines.first().move, retryable = true)
    }

    // ------------------------------------------------------------------ reading the board with Claude

    private suspend fun visionSync(shot: Shot, trigger: Trigger, current: Tracked?, inference: MoveInference?): Boolean {
        val config = claudeConfig()
        if (config == null) {
            setArrow(null)
            val lost = current != null && trigger != Trigger.RESCAN
            render(
                UiState.Message(
                    BrainUi.NO_KEY_TITLE,
                    if (lost) BrainUi.NO_KEY_LOST_BODY else BrainUi.NO_KEY_BODY,
                    isError = true,
                    board = trackedBoard(),
                )
            )
            return false
        }
        val rect = shot.rect
        val screenContext = settings.sendScreenContext
        if (rect == null && !(trigger.user && screenContext)) {
            // Without a board there is nothing to send, and the app never sends a bare screenshot unasked.
            if (trigger.user) {
                setArrow(null)
                render(UiState.Message(BrainUi.NOT_FOUND_TITLE, BrainUi.NOT_FOUND_BODY, isError = true, board = trackedBoard()))
            }
            return false
        }
        if (!admitClaudeCall(trigger, BrainUi.READING)) return false

        val samePosition = inference is MoveInference.NoChange
        val sameGame = inference != null && inference !is MoveInference.NoMatch
        val forcedSide = if (current != null && current.sideForced && samePosition) current.position.sideToMove else null
        val forcedOrientation = if (current != null && current.orientationForced && sameGame) current.whiteAtBottom else null
        val pixels = shot.snapshot?.takeIf { it.reliable }

        val request = withContext(dispatchers.cpu) {
            VisionRequest(
                boardImage = rect?.let { encoder.encodeBoard(shot.frame, it) },
                screenImage = when {
                    rect == null -> encoder.encodeScreen(shot.frame, SCREEN_ONLY_EDGE)
                    screenContext -> encoder.encodeScreen(shot.frame, SCREEN_CONTEXT_EDGE)
                    else -> null
                },
                occupancyHint = pixels?.toAscii(),
                previousFen = current?.position?.toFen(),
                forcedSideToMove = forcedSide?.fenChar,
            )
        }
        val oracle = BrainOracle(analyzer, tuning.engineMs, pixels, forcedSide, forcedOrientation, current?.previousGame())

        val result = try {
            callClaude(config) { client, progress -> client.analyzeBoard(request, oracle, progress) }
        } catch (failure: ClaudeException) {
            currentCoroutineContext().ensureActive()
            val accepted = oracle.accepted
            if (accepted != null) {
                // The reading got through before the failure: keep it and let the engine answer.
                showEngineAdvice(adopt(accepted, shot), accepted.briefing.lines, failure.userMessage)
                return true
            }
            if (current != null && samePosition) {
                showEngineAdvice(current, engineLines(current.position), failure.userMessage)
                return true
            }
            setArrow(null)
            render(
                UiState.Message(
                    BrainUi.CLAUDE_FAILED_TITLE,
                    failure.userMessage + BrainUi.CLAUDE_FAILED_SUFFIX,
                    isError = true,
                    board = trackedBoard(),
                )
            )
            return false
        }

        val accepted = oracle.accepted
        if (accepted == null) {
            setArrow(null)
            render(
                UiState.Message(
                    BrainUi.NO_POSITION_TITLE,
                    result.commentary.trim().ifEmpty { BrainUi.NO_POSITION_BODY },
                    isError = false,
                )
            )
            return false
        }
        val next = adopt(accepted, shot)
        if (next.position.legalMoves().isEmpty()) {
            present(next, BrainUi.gameOver(next.position, next.whiteAtBottom, null), null, retryable = false)
        } else {
            showClaudeResult(next, accepted.briefing.lines, result)
        }
        return true
    }

    private fun adopt(accepted: AcceptedPosition, shot: Shot): Tracked {
        val next = Tracked(
            position = accepted.position,
            whiteAtBottom = accepted.whiteAtBottom,
            history = accepted.history.takeLast(Tracking.MAX_HISTORY),
            snapshot = shot.snapshot?.takeIf { it.reliable },
            sideForced = accepted.sideForced,
            orientationForced = accepted.orientationForced,
        )
        tracked = next
        presented = null
        return next
    }

    // ------------------------------------------------------------------ Claude plumbing

    private fun claudeConfig(): ClaudeConfig? {
        val key = settings.apiKey?.trim().orEmpty()
        if (key.isEmpty()) return null
        return ClaudeConfig(
            apiKey = key,
            model = settings.model,
            effort = settings.effort,
            baseUrl = settings.baseUrl?.trim()?.takeIf { it.isNotEmpty() },
        )
    }

    /**
     * The gate every Claude call passes: the cap on automatic calls, then the minimum gap. Returns
     * false when auto-advise was paused instead.
     */
    private suspend fun admitClaudeCall(trigger: Trigger, message: String): Boolean {
        if (!trigger.user) {
            if (autoClaudeCalls >= tuning.maxAutoClaudeCalls) {
                autoPaused = true
                setArrow(null)
                render(
                    UiState.Message(
                        BrainUi.PAUSED_TITLE,
                        BrainUi.pausedBody(autoClaudeCalls),
                        isError = false,
                        board = trackedBoard(),
                    )
                )
                return false
            }
            autoClaudeCalls++
        }
        showWorking(message)
        val last = lastClaudeCallAt
        if (last != null) {
            // Capped so that a clock set back while the app runs cannot stretch the wait.
            val wait = minOf(last + tuning.minClaudeGapMs - clock(), tuning.minClaudeGapMs)
            if (wait > 0) delay(wait)
        }
        lastClaudeCallAt = clock()
        return true
    }

    /**
     * Runs a blocking Claude call on the io dispatcher. Cancelling the analysis interrupts the call;
     * progress texts are shown as working states and are all delivered before this returns.
     */
    private suspend fun <T> callClaude(config: ClaudeConfig, call: (AdviceService, (String) -> Unit) -> T): T =
        coroutineScope {
            val progress: (String) -> Unit = { text ->
                if (text.isNotBlank()) launch(dispatchers.main) { showWorking(text.trim()) }
            }
            withContext(dispatchers.io) {
                val client = serviceFor(config)
                runInterruptible { call(client, progress) }
            }
        }

    /** The client for [config]; a changed setting (key, model, effort, URL) replaces the old client. */
    private fun serviceFor(config: ClaudeConfig): AdviceService {
        var stale: AdviceService? = null
        val client = synchronized(lock) {
            if (session == null) throw CancellationException("The brain was stopped")
            val existing = service
            if (existing != null && serviceConfig == config) {
                existing
            } else {
                stale = existing
                claude.create(config).also {
                    service = it
                    serviceConfig = config
                }
            }
        }
        stale?.let { closeQuietly(it) }
        return client
    }

    private fun closeQuietly(client: AdviceService) {
        try {
            client.close()
        } catch (_: Exception) {
            // Nothing useful can be done about a client that fails to close.
        }
    }

    // ------------------------------------------------------------------ overlay

    private fun trackedBoard(): BoardUi? = tracked?.let { BrainUi.board(it.position, it.whiteAtBottom) }

    private fun render(state: UiState) {
        if (state == lastState) return
        lastState = state
        overlay.render(state)
        watcher.reset(clock())
    }

    private fun setArrow(arrow: ArrowSpec?) {
        if (arrow == lastArrow) return
        lastArrow = arrow
        overlay.showArrow(arrow)
        watcher.reset(clock())
    }

    private fun showWorking(message: String) {
        setArrow(null)
        render(UiState.Working(message, trackedBoard()))
    }

    private fun present(current: Tracked, state: UiState, move: Move?, retryable: Boolean) {
        val shown = Presented(current.position, current.whiteAtBottom, state, move, retryable)
        presented = shown
        show(shown)
    }

    private fun show(shown: Presented) {
        render(shown.state)
        setArrow(shown.move?.let { arrowFor(it, shown.whiteAtBottom) })
    }

    private fun arrowFor(move: Move, whiteAtBottom: Boolean): ArrowSpec? {
        val rect = lastRect ?: return null
        if (boardLost || !settings.drawOnBoard || !frames.framesMatchScreen) return null
        return BrainUi.arrow(rect, move, whiteAtBottom)
    }

    // ------------------------------------------------------------------ auto loop

    private fun startAutoLoop() {
        if (autoJob?.isActive == true) return
        val current = currentSession() ?: return
        watcher.reset(clock())
        autoJob = current.launch(dispatchers.main) {
            while (true) {
                delay(tuning.pollMs)
                try {
                    poll()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    // A frame that could not be read is not worth stopping the watch for.
                }
            }
        }
    }

    private suspend fun poll() {
        if (autoPaused || analysisActive()) return
        // Before a board has ever been found there is nothing to watch: the user starts with a tap.
        val rect = lastRect ?: return
        val frame = frames.latestFrame() ?: return
        if (!rect.fitsIn(frame.width, frame.height)) return
        val fingerprint = withContext(dispatchers.cpu) { vision.fingerprint(frame, rect) }
        if (autoPaused || analysisActive() || lastRect != rect) return
        when (watcher.onFrame(fingerprint, clock())) {
            ChangeWatcher.Signal.NONE -> Unit
            ChangeWatcher.Signal.SETTLED -> if (verifyPending) {
                verifyPending = false
                verify(frame, rect)
            }
            ChangeWatcher.Signal.CHANGED -> boardChanged(frame, rect, fingerprint)
        }
    }

    private suspend fun boardChanged(frame: Frame, rect: BoardRect, fingerprint: IntArray) {
        if (tracked == null && claudeConfig() == null) {
            // No position and no key: a cycle could only repeat that the board cannot be read.
            watcher.adopt(fingerprint)
            return
        }
        if (boardLost) {
            // While the board is away (another app is in front) every change on screen would otherwise
            // blink the overlay for a clean capture. Look at the frame as it is first.
            val found = withContext(dispatchers.cpu) { vision.locate(frame, rect) }
            if (analysisActive()) return
            if (found == null) {
                watcher.adopt(fingerprint)
                return
            }
        }
        launchAnalysis(user = false) { runCycle(Trigger.AUTO) }
    }

    /**
     * Closes the gap between the clean capture an analysis worked on and the new baseline taken after
     * the app drew its result: a move made in between (a bot answering within a second, or anything
     * played during a long Claude call) would otherwise be part of the baseline and go unnoticed.
     * The settled frame is sampled as it is, overlay included; if it reads differently from the
     * tracked reading on squares the app's own windows are not known to cover, one clean look follows.
     */
    private suspend fun verify(frame: Frame, rect: BoardRect) {
        val current = tracked ?: return
        val reference = current.snapshot ?: return
        if (boardLost || reference.rect != rect) return
        val seen = withContext(dispatchers.cpu) { vision.sample(frame, rect) }
        if (autoPaused || analysisActive() || tracked !== current) return
        val arrow = lastArrow
        val diff = BooleanArray(64)
        var suspicious = false
        for (cell in 0 until 64) {
            if (seen.cells[cell] == reference.cells[cell]) continue
            // The arrow can make an empty square look occupied or change a piece's apparent colour, but it
            // cannot make a piece vanish: a square under it that now reads empty was moved away from.
            if (underArrow(arrow, cell) && seen.cells[cell] != CellState.EMPTY) continue
            diff[cell] = true
            if (!overlayMask[cell]) suspicious = true
        }
        if (!suspicious) return
        verifyDiff = diff
        launchAnalysis(user = false) { runCycle(Trigger.VERIFY) }
    }

    /** The arrow may be drawn straight or as an L, so every cell of the box it spans counts as covered. */
    private fun underArrow(arrow: ArrowSpec?, cell: Int): Boolean {
        if (arrow == null) return false
        val row = cell / 8
        val col = cell % 8
        return row in minOf(arrow.fromRow, arrow.toRow)..maxOf(arrow.fromRow, arrow.toRow) &&
            col in minOf(arrow.fromCol, arrow.toCol)..maxOf(arrow.fromCol, arrow.toCol)
    }

    private companion object {
        val MONOTONIC_MILLIS: () -> Long = { System.nanoTime() / 1_000_000 }

        /** Long edge of the context screenshot sent next to a board crop. */
        const val SCREEN_CONTEXT_EDGE = 1024

        /** Long edge of the screenshot when it is the only image and Claude has to find the board in it. */
        const val SCREEN_ONLY_EDGE = 1568
    }
}
