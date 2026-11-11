package com.krstech.chesshelp.brain

import com.krstech.chesshelp.chess.BoardView
import com.krstech.chesshelp.chess.MoveTracker
import com.krstech.chesshelp.chess.Occupant
import com.krstech.chesshelp.chess.Piece
import com.krstech.chesshelp.chess.Position
import com.krstech.chesshelp.chess.PositionValidator
import com.krstech.chesshelp.chess.Side
import com.krstech.chesshelp.vision.BoardSnapshot
import com.krstech.chesshelp.vision.CellState

/** The game as the app follows it. Immutable; every change makes a new instance. */
internal class Tracked(
    val position: Position,
    val whiteAtBottom: Boolean,
    /** SAN moves seen so far, oldest first. */
    val history: List<String>,
    /**
     * The local pixel reading taken when the screen showed [position], or `null` when the position has
     * never been matched to pixels (read from a whole screenshot, or the pixels were unreliable).
     */
    val snapshot: BoardSnapshot?,
    /** The user set the side to move, and every move since then has been followed. */
    val sideForced: Boolean,
    /** The user chose the orientation with "Flip board". */
    val orientationForced: Boolean,
) {
    fun with(
        position: Position = this.position,
        history: List<String> = this.history,
        snapshot: BoardSnapshot? = this.snapshot,
        sideForced: Boolean = this.sideForced,
    ): Tracked = Tracked(position, whiteAtBottom, history, snapshot, sideForced, orientationForced)

    fun previousGame(): PreviousGame = PreviousGame(position, whiteAtBottom, history)
}

internal object Tracking {
    /** Moves kept in the history; the Claude prompt uses the most recent ones only. */
    const val MAX_HISTORY = 200

    private fun occupant(state: CellState): Occupant = when (state) {
        CellState.EMPTY -> Occupant.EMPTY
        CellState.LIGHT_PIECE -> Occupant.WHITE
        CellState.DARK_PIECE -> Occupant.BLACK
    }

    /**
     * What [now] says about the squares, indexed by square, for [MoveTracker.infer].
     *
     * The tracked position can differ from its own pixel reading on a few squares (Claude overruled the
     * pixels there, or the sampler misjudges one piece). Such a square would otherwise break every later
     * inference, so a cell that reads the same as it did when the position was established keeps the
     * occupancy the position has; only cells whose reading changed are taken from [now].
     */
    fun observedOccupancy(tracked: Tracked, now: BoardSnapshot): Array<Occupant> {
        val known = MoveTracker.occupancyOf(tracked.position)
        val reference = tracked.snapshot
        return Array(64) { sq ->
            val cell = BoardView.rowOf(sq, tracked.whiteAtBottom) * 8 + BoardView.colOf(sq, tracked.whiteAtBottom)
            if (reference != null && reference.cells[cell] == now.cells[cell]) known[sq] else occupant(now.cells[cell])
        }
    }

    fun piecesOf(position: Position): Array<Piece?> = Array(64) { position.pieceAt(it) }

    /** Why [side] cannot be the side to move in [position], or an empty list when it can. */
    fun sideProblems(position: Position, side: Side): List<String> =
        PositionValidator.problems(piecesOf(position), side)

    /**
     * The same picture read from the other side of the board: every piece stays in its view cell, so it
     * lands on the square rotated by 180 degrees. Returns the problems instead when that is not a
     * possible position.
     */
    fun flipped(tracked: Tracked): FlipResult {
        val rows = BoardView.toRows(tracked.position, tracked.whiteAtBottom)
        val pieces = BoardView.parseRows(rows, !tracked.whiteAtBottom)
        val side = tracked.position.sideToMove
        val problems = PositionValidator.problems(pieces, side)
        if (problems.isNotEmpty()) return FlipResult(null, problems)
        return FlipResult(
            Tracked(
                position = Position.fromBoard(pieces, side),
                whiteAtBottom = !tracked.whiteAtBottom,
                // The moves were named for the other orientation and mean nothing now.
                history = emptyList(),
                snapshot = tracked.snapshot,
                sideForced = tracked.sideForced,
                orientationForced = true,
            ),
            emptyList(),
        )
    }

    class FlipResult(val tracked: Tracked?, val problems: List<String>)
}

/**
 * Decides when the board on screen has changed, from fingerprints taken a few times per second.
 *
 * After [reset] (the app drew something itself, or an analysis just ended) it waits [settleMs], then
 * takes the first fingerprint that repeats as the baseline and reports [Signal.SETTLED]. From then on a
 * fingerprint that differs from the baseline and then holds still for [stableMs] (the move animation
 * is over) is reported once as [Signal.CHANGED]; the caller is expected to [reset] or [adopt] next.
 */
internal class ChangeWatcher(
    private val stableMs: Long,
    private val settleMs: Long,
    private val differs: (IntArray, IntArray) -> Boolean,
) {
    enum class Signal { NONE, SETTLED, CHANGED }

    private var baseline: IntArray? = null
    private var settleAfter = 0L
    private var previous: IntArray? = null
    private var candidate: IntArray? = null
    private var candidateSince = 0L

    fun reset(now: Long) {
        baseline = null
        previous = null
        candidate = null
        settleAfter = now + settleMs
    }

    /** Takes [fingerprint] as the baseline right away. */
    fun adopt(fingerprint: IntArray) {
        baseline = fingerprint
        previous = null
        candidate = null
    }

    fun onFrame(fingerprint: IntArray, now: Long): Signal {
        val settled = baseline
        if (settled == null) {
            val before = previous
            previous = fingerprint
            if (now < settleAfter || before == null || differs(before, fingerprint)) return Signal.NONE
            adopt(fingerprint)
            return Signal.SETTLED
        }
        if (!differs(settled, fingerprint)) {
            candidate = null
            return Signal.NONE
        }
        val pending = candidate
        if (pending == null || differs(pending, fingerprint)) {
            candidate = fingerprint
            candidateSince = now
            return Signal.NONE
        }
        if (now - candidateSince < stableMs) return Signal.NONE
        candidate = null
        return Signal.CHANGED
    }
}
