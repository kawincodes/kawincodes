package com.krstech.chesshelp.brain

import com.krstech.chesshelp.chess.BoardView
import com.krstech.chesshelp.chess.Move
import com.krstech.chesshelp.chess.Position
import com.krstech.chesshelp.chess.Side
import com.krstech.chesshelp.claude.AnalysisResult
import com.krstech.chesshelp.claude.ClaudeModels
import com.krstech.chesshelp.claude.Evaluation
import com.krstech.chesshelp.claude.MoveAdvice
import com.krstech.chesshelp.claude.TokenUsage
import com.krstech.chesshelp.vision.BoardRect
import java.util.Locale

/** Everything the user reads, and the mapping from analysis results to [UiState]. */
internal object BrainUi {
    const val LOCAL_ENGINE = "Local engine"

    const val LOOKING = "Looking at the board…"
    const val THINKING = "Thinking…"
    const val ASKING = "Asking Claude…"
    const val READING = "Reading the board with Claude…"

    const val IDLE_HINT = "Open a chess board and tap the bubble."
    const val IDLE_HINT_NO_KEY =
        "No API key yet. Add one in KRS Chess Help so Claude can read the board, then tap the bubble."

    const val NOT_VISIBLE_TITLE = "Board not visible"
    const val NOT_VISIBLE_BODY =
        "The chess board is not on screen right now. The position is kept: tap the bubble when the board is back."

    const val NOT_FOUND_TITLE = "Board not found"
    const val NOT_FOUND_BODY =
        "No chess board was found on this screen. Open the game and tap the bubble again. " +
            "(Sending a screenshot for context is off, so Claude cannot look for the board either.)"

    const val NO_FRAME_TITLE = "No screen image yet"
    const val NO_FRAME_BODY = "Screen capture has not delivered a picture yet. Try again in a moment."

    const val NO_KEY_TITLE = "API key needed"
    const val NO_KEY_BODY =
        "Reading the board needs Claude, and no API key is set. Add one in KRS Chess Help. " +
            "The local engine can only advise once a position is known, and only Claude or move tracking can supply one."
    const val NO_KEY_LOST_BODY =
        "The app lost track of the game, and reading the board again needs Claude, but no API key is set. " +
            "Add one in KRS Chess Help. The last known position is kept in case the board matches it again."
    const val NO_KEY_NOTE = "No API key is set, so this is the local engine's move (a shallow search)."

    const val NO_POSITION_TITLE = "No position read"
    const val NO_POSITION_BODY = "Claude did not find a chess position on this screen."

    const val CLAUDE_FAILED_TITLE = "Claude is unavailable"
    const val CLAUDE_FAILED_SUFFIX =
        " The local engine cannot help here: it needs a known position, which only Claude or move tracking can supply."
    const val ENGINE_INSTEAD = " Showing the local engine's move instead."
    const val NO_MOVE_FROM_CLAUDE = "Claude did not settle on a move."
    const val NO_RECOMMENDATION_TITLE = "No recommendation"
    const val ENGINE_EMPTY = " The local engine found no move either. Tap the bubble to try again."

    const val PAUSED_TITLE = "Auto-advise paused"
    const val NOTHING_TRACKED_TITLE = "No position yet"
    const val NOTHING_TRACKED_BODY = "Tap the bubble to read the board first."
    const val CANNOT_SWITCH_TITLE = "Can't switch the side to move"
    const val CANNOT_FLIP_TITLE = "Can't flip the board"
    const val FAILURE_TITLE = "Something went wrong"

    fun pausedBody(calls: Int): String =
        "$calls automatic requests to Claude in a row without you touching the app. " +
            "Tap the bubble to carry on."

    fun sideName(side: Side): String = Briefings.sideName(side)

    fun toMove(side: Side): String = "${sideName(side)} to move"

    fun board(position: Position, whiteAtBottom: Boolean, move: Move? = null): BoardUi {
        val rows = BoardView.toRows(position, whiteAtBottom)
        if (move == null) return BoardUi(rows)
        return BoardUi(
            rows = rows,
            fromRow = BoardView.rowOf(move.from, whiteAtBottom),
            fromCol = BoardView.colOf(move.from, whiteAtBottom),
            toRow = BoardView.rowOf(move.to, whiteAtBottom),
            toCol = BoardView.colOf(move.to, whiteAtBottom),
        )
    }

    fun arrow(rect: BoardRect, move: Move, whiteAtBottom: Boolean): ArrowSpec = ArrowSpec(
        boardLeft = rect.left,
        boardTop = rect.top,
        boardSize = rect.size,
        fromRow = BoardView.rowOf(move.from, whiteAtBottom),
        fromCol = BoardView.colOf(move.from, whiteAtBottom),
        toRow = BoardView.rowOf(move.to, whiteAtBottom),
        toCol = BoardView.colOf(move.to, whiteAtBottom),
    )

    /** Claude's recommendation. [move] is the recommended move, already checked against [position]. */
    fun claudeAdvice(
        position: Position,
        whiteAtBottom: Boolean,
        move: Move,
        advice: MoveAdvice,
        result: AnalysisResult,
        configuredModel: String,
        modelLabel: String,
    ): UiState.Advice = UiState.Advice(
        title = toMove(position.sideToMove),
        moveSan = position.san(move),
        evaluation = label(advice.evaluation),
        explanation = advice.explanation.trim(),
        alternatives = advice.alternatives.mapNotNull { alternative ->
            // An "alternative" that cannot be played would be misinformation.
            val legal = position.parseMove(alternative.san) ?: return@mapNotNull null
            if (legal == move) null else position.san(legal) to alternative.note.trim()
        },
        threat = advice.opponentThreat?.trim()?.takeIf { it.isNotEmpty() },
        source = sourceLabel(result.servedBy, configuredModel, modelLabel),
        board = board(position, whiteAtBottom, move),
        footer = usageFooter(result.usage),
    )

    /** The local engine's best line, with [note] (why Claude is not the source) leading the explanation. */
    fun engineAdvice(position: Position, whiteAtBottom: Boolean, lines: List<EngineHint>, note: String): UiState.Advice {
        val best = lines.first()
        val continuation = Briefings.continuationOf(best)
        val line = if (continuation.isEmpty()) "" else " Expected line: ${best.san} ${continuation.joinToString(" ")}."
        return UiState.Advice(
            title = toMove(position.sideToMove),
            moveSan = position.san(best.move),
            evaluation = label(best),
            explanation = (note.trim() + line).trim(),
            alternatives = lines.drop(1).take(2).map { position.san(it.move) to Briefings.scoreText(it) },
            threat = null,
            source = LOCAL_ENGINE,
            board = board(position, whiteAtBottom, best.move),
            footer = "Score ${Briefings.scoreText(best)}",
        )
    }

    fun gameOver(position: Position, whiteAtBottom: Boolean, lastMoves: String?): UiState.Message {
        val sentence = Briefings.gameOverSentence(position).replaceFirstChar { it.uppercase() }
        return UiState.Message(
            title = if (position.isInCheck()) "Checkmate" else "Stalemate",
            body = if (lastMoves == null) sentence else "$lastMoves $sentence",
            isError = false,
            board = board(position, whiteAtBottom),
        )
    }

    /** "White played Nf3. Waiting for Black…" */
    fun waiting(position: Position, whiteAtBottom: Boolean, lastMoves: String?, lastMove: Move?): UiState.Message {
        val waitingFor = "Waiting for ${sideName(position.sideToMove)}…"
        return UiState.Message(
            title = toMove(position.sideToMove),
            body = if (lastMoves == null) waitingFor else "$lastMoves $waitingFor",
            isError = false,
            board = board(position, whiteAtBottom, lastMove),
        )
    }

    /** "White played Nf3." / "White played Nf3, Black played Nf6." for the moves leading to [after]. */
    fun playedSentence(sans: List<String>, after: Position): String {
        // The last move was made by the side that is not to move now; earlier ones alternate backwards.
        var side = if (sans.size % 2 == 1) after.sideToMove.opposite else after.sideToMove
        val parts = ArrayList<String>(sans.size)
        for (san in sans) {
            parts += "${sideName(side)} played $san"
            side = side.opposite
        }
        return parts.joinToString(", ") + "."
    }

    fun label(evaluation: Evaluation): String = when (evaluation) {
        Evaluation.WINNING -> "Winning"
        Evaluation.BETTER -> "Better"
        Evaluation.EQUAL -> "Equal"
        Evaluation.WORSE -> "Worse"
        Evaluation.LOSING -> "Losing"
        Evaluation.UNCLEAR -> "Unclear"
    }

    fun label(line: EngineHint): String {
        val mate = line.mateIn
        if (mate != null) return if (mate >= 0) "Winning" else "Losing"
        return when {
            line.scoreCp >= 300 -> "Winning"
            line.scoreCp >= 80 -> "Better"
            line.scoreCp > -80 -> "Equal"
            line.scoreCp > -300 -> "Worse"
            else -> "Losing"
        }
    }

    /** The model that actually answered: after a server-side fallback it is not the configured one. */
    fun sourceLabel(servedBy: String, configuredModel: String, modelLabel: String): String {
        val served = servedBy.trim()
        return if (served.isEmpty() || served == configuredModel) modelLabel else ClaudeModels.labelOf(served)
    }

    /** "1.2k in / 0.3k out" */
    fun usageFooter(usage: TokenUsage): String? {
        if (usage.inputTokens <= 0 && usage.outputTokens <= 0) return null
        return "${tokens(usage.inputTokens)} in / ${tokens(usage.outputTokens)} out"
    }

    fun tokens(count: Long): String = when {
        count < 100 -> count.coerceAtLeast(0).toString()
        count < 100_000 -> String.format(Locale.ROOT, "%.1fk", count / 1000.0)
        count < 1_000_000 -> String.format(Locale.ROOT, "%.0fk", count / 1000.0)
        else -> String.format(Locale.ROOT, "%.1fM", count / 1_000_000.0)
    }
}
