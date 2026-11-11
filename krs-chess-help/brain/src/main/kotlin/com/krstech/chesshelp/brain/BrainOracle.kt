package com.krstech.chesshelp.brain

import com.krstech.chesshelp.chess.BoardView
import com.krstech.chesshelp.chess.MoveInference
import com.krstech.chesshelp.chess.MoveTracker
import com.krstech.chesshelp.chess.Piece
import com.krstech.chesshelp.chess.PieceType
import com.krstech.chesshelp.chess.Position
import com.krstech.chesshelp.chess.PositionValidator
import com.krstech.chesshelp.chess.Side
import com.krstech.chesshelp.claude.BoardReading
import com.krstech.chesshelp.claude.MoveVerdict
import com.krstech.chesshelp.claude.PositionOracle
import com.krstech.chesshelp.claude.PositionVerdict
import com.krstech.chesshelp.vision.BoardSnapshot
import com.krstech.chesshelp.vision.CellState

/** The game the app was following before a fresh reading of the board. */
internal class PreviousGame(val position: Position, val whiteAtBottom: Boolean, val history: List<String>)

/** A position the oracle has accepted, with everything the brain needs to start tracking it. */
internal class AcceptedPosition(
    val position: Position,
    val whiteAtBottom: Boolean,
    /** SAN moves known to have led here (carried over from [PreviousGame] when the reading continues it). */
    val history: List<String>,
    val briefing: Briefing,
    /** The user's side-to-move override was applied. */
    val sideForced: Boolean,
    /** The user's orientation override was applied. */
    val orientationForced: Boolean,
) {
    val fen: String = position.toFen()
}

/**
 * Validates what Claude reads from the screen and checks the moves it recommends. One instance serves one
 * analysis; it is called synchronously on the thread that runs the Claude call.
 *
 * @param snapshot the local pixel reading of the same frame when it is reliable, else `null`. Claude's
 *   reading is compared with it once; a second submission is taken as Claude's considered answer.
 * @param forcedSide the user's side-to-move override, if it applies to this frame.
 * @param forcedWhiteAtBottom the user's orientation override, if it applies to this frame.
 * @param previous the game followed so far, to keep castling rights and the move list when the new
 *   reading is the same game.
 */
internal class BrainOracle private constructor(
    private val analyzer: PositionAnalyzer,
    private val engineMs: Long,
    private val snapshot: BoardSnapshot?,
    private val forcedSide: Side?,
    private val forcedWhiteAtBottom: Boolean?,
    private val previous: PreviousGame?,
    known: AcceptedPosition?,
) : PositionOracle {

    constructor(
        analyzer: PositionAnalyzer,
        engineMs: Long,
        snapshot: BoardSnapshot?,
        forcedSide: Side? = null,
        forcedWhiteAtBottom: Boolean? = null,
        previous: PreviousGame? = null,
    ) : this(analyzer, engineMs, snapshot, forcedSide, forcedWhiteAtBottom, previous, null)

    /** The last accepted position; read by the brain once the Claude call has returned. */
    @Volatile
    var accepted: AcceptedPosition? = known
        private set

    /** On the text-only path the position is the tracked one and cannot be replaced by a reading. */
    private val positionIsFixed = known != null
    private var crossChecked = false
    private var forcedSideQuestioned = false

    override fun submitPosition(reading: BoardReading): PositionVerdict {
        if (positionIsFixed) {
            return PositionVerdict.Rejected(
                listOf("The position is already known (FEN: ${accepted?.fen}). Call recommend_move with a move from the legal move list.")
            )
        }
        val readSide = when (reading.sideToMove.lowercaseChar()) {
            'w' -> Side.WHITE
            'b' -> Side.BLACK
            else -> return PositionVerdict.Rejected(
                listOf("side_to_move must be \"w\" or \"b\" (got \"${reading.sideToMove}\")")
            )
        }
        val readPieces = try {
            BoardView.parseRows(reading.rows, reading.whiteAtBottom)
        } catch (malformed: IllegalArgumentException) {
            return PositionVerdict.Rejected(listOf(malformed.message ?: "The rows are malformed"))
        }
        // The board as displayed, independent of which way round it is.
        val view = Array(64) { cell -> readPieces[BoardView.squareOf(cell / 8, cell % 8, reading.whiteAtBottom)] }

        val side = forcedSide ?: readSide
        var whiteAtBottom = forcedWhiteAtBottom ?: reading.whiteAtBottom
        var pieces = piecesOf(view, whiteAtBottom)
        var problems = PositionValidator.problems(pieces, side)
        if (problems.isNotEmpty() && whiteAtBottom != reading.whiteAtBottom &&
            PositionValidator.problems(readPieces, side).isEmpty()
        ) {
            // The user's orientation no longer fits what is on screen (e.g. a new game from the other side).
            whiteAtBottom = reading.whiteAtBottom
            pieces = readPieces
            problems = emptyList()
        }

        var sideToMove = side
        val notes = ArrayList<String>()
        if (problems.isNotEmpty()) {
            val other = side.opposite
            val fitsOtherSide = forcedSide != null && PositionValidator.problems(pieces, other).isEmpty()
            if (!fitsOtherSide) return PositionVerdict.Rejected(problems)
            if (!forcedSideQuestioned) {
                // Either the reading or the user's override is wrong; give the reading one more look first.
                forcedSideQuestioned = true
                return PositionVerdict.Rejected(
                    problems + "The user has set ${Briefings.sideName(side)} to move. Check the squares named above " +
                        "once more; if the board really is as you reported it, submit the same reading again."
                )
            }
            sideToMove = other
            notes += "the user's setting (${Briefings.sideName(side)} to move) is impossible in this position, " +
                "so ${Briefings.sideName(other)} is to move."
        }

        val local = snapshot
        if (local != null && !crossChecked) {
            crossChecked = true
            val disagreements = disagreements(view, local)
            if (disagreements.isNotEmpty()) return PositionVerdict.Rejected(crossCheckProblems(disagreements))
        }

        val fresh = Position.fromBoard(pieces, sideToMove)
        val continued = continuationOf(fresh, whiteAtBottom)
        val position = continued?.first ?: fresh
        val lines = analyzer.safeLines(position, engineMs)
        val result = AcceptedPosition(
            position = position,
            whiteAtBottom = whiteAtBottom,
            history = continued?.second ?: emptyList(),
            briefing = Briefings.build(position, lines, notes),
            sideForced = forcedSide != null && sideToMove == forcedSide,
            orientationForced = forcedWhiteAtBottom != null && whiteAtBottom == forcedWhiteAtBottom,
        )
        accepted = result
        return PositionVerdict.Accepted(result.fen, result.briefing.text)
    }

    override fun checkMove(fen: String, move: String): MoveVerdict {
        val position = accepted?.position
            ?: try {
                Position.fromFen(fen)
            } catch (_: IllegalArgumentException) {
                return MoveVerdict.Illegal("No position has been accepted yet, so the move cannot be checked. Call submit_position first.")
            }
        val legal = position.legalMoves()
        if (legal.isEmpty()) {
            return MoveVerdict.Illegal(
                "The game is over: ${Briefings.gameOverSentence(position)} There is no legal move, " +
                    "so do not call recommend_move again; reply with one short sentence instead."
            )
        }
        val parsed = position.parseMove(move.trim())
        if (parsed != null) return MoveVerdict.Legal(position.san(parsed), parsed.uci())
        return MoveVerdict.Illegal(
            "It is not a legal move for ${Briefings.sideName(position.sideToMove)} here, or the notation fits more than one move. " +
                "${Briefings.LEGAL_MOVES_PREFIX} ${legal.joinToString(" ") { position.san(it) }}"
        )
    }

    /**
     * When the reading shows the game followed so far (same placement, or one or two moves later), the
     * tracked position knows more than a picture can: castling rights lost by earlier king or rook
     * moves, the en-passant square, the moves played. Returns that position and its move list.
     */
    private fun continuationOf(fresh: Position, whiteAtBottom: Boolean): Pair<Position, List<String>>? {
        val before = previous ?: return null
        if (before.whiteAtBottom != whiteAtBottom) return null
        if (before.position.placement() == fresh.placement()) {
            return before.position.withSideToMove(fresh.sideToMove) to before.history
        }
        val played = MoveTracker.infer(before.position, MoveTracker.occupancyOf(fresh)) as? MoveInference.Played
            ?: return null
        if (played.position.placement() != fresh.placement() || played.position.sideToMove != fresh.sideToMove) return null
        return played.position to (before.history + played.sans)
    }

    companion object {
        /** Most disagreeing squares listed one by one; beyond this the reading is misaligned rather than mistaken. */
        private const val MAX_LISTED = 16

        /** Oracle for the text-only path: the position is already tracked, only moves are checked. */
        fun forKnownPosition(position: Position, whiteAtBottom: Boolean, history: List<String>, briefing: Briefing): BrainOracle =
            BrainOracle(
                analyzer = { _, _ -> emptyList() },
                engineMs = 0,
                snapshot = null,
                forcedSide = null,
                forcedWhiteAtBottom = null,
                previous = null,
                known = AcceptedPosition(position, whiteAtBottom, history, briefing, sideForced = false, orientationForced = false),
            )

        private fun piecesOf(view: Array<Piece?>, whiteAtBottom: Boolean): Array<Piece?> {
            val pieces = arrayOfNulls<Piece>(64)
            for (cell in 0 until 64) pieces[BoardView.squareOf(cell / 8, cell % 8, whiteAtBottom)] = view[cell]
            return pieces
        }

        /** One line per square where the reading and the pixels differ in occupancy or piece colour. */
        internal fun disagreements(view: Array<Piece?>, snapshot: BoardSnapshot): List<String> {
            val lines = ArrayList<String>()
            for (cell in 0 until 64) {
                val piece = view[cell]
                val read = when (piece?.side) {
                    null -> CellState.EMPTY
                    Side.WHITE -> CellState.LIGHT_PIECE
                    Side.BLACK -> CellState.DARK_PIECE
                }
                val seen = snapshot.cells[cell]
                if (read == seen) continue
                lines += "r${cell / 8 + 1}c${cell % 8 + 1}: you reported ${describe(piece)}, " +
                    "but the pixels there look like ${describe(seen)}."
            }
            return lines
        }

        private fun crossCheckProblems(disagreements: List<String>): List<String> {
            val problems = ArrayList<String>(minOf(disagreements.size, MAX_LISTED) + 2)
            problems += "The app's pixel analysis disagrees with this reading on ${disagreements.size} " +
                "square(s), named below by the image labels (r1-r8 from the top, c1-c8 from the left). " +
                "Look at each of them again and correct what you misread. Where the image clearly shows " +
                "that you were right, keep your reading: the next submission is accepted as it is."
            problems += disagreements.take(MAX_LISTED)
            if (disagreements.size > MAX_LISTED) {
                problems += "... and ${disagreements.size - MAX_LISTED} more squares. With this many differences, " +
                    "check that the rows are not shifted by one or the board mirrored."
            }
            return problems
        }

        private fun describe(piece: Piece?): String {
            if (piece == null) return "an empty square"
            val colour = if (piece.side == Side.WHITE) "white" else "black"
            val name = when (piece.type) {
                PieceType.PAWN -> "pawn"
                PieceType.KNIGHT -> "knight"
                PieceType.BISHOP -> "bishop"
                PieceType.ROOK -> "rook"
                PieceType.QUEEN -> "queen"
                PieceType.KING -> "king"
            }
            return "a $colour $name (${piece.fenChar})"
        }

        private fun describe(state: CellState): String = when (state) {
            CellState.EMPTY -> "an empty square"
            CellState.LIGHT_PIECE -> "a light-coloured (White) piece"
            CellState.DARK_PIECE -> "a dark-coloured (Black) piece"
        }
    }
}
