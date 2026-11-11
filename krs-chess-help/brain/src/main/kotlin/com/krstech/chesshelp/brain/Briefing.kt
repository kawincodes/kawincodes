package com.krstech.chesshelp.brain

import com.krstech.chesshelp.chess.Position
import com.krstech.chesshelp.chess.Side
import java.util.Locale

/** What Claude is told about an accepted position, plus the engine lines the text was built from. */
internal class Briefing(val text: String, val lines: List<EngineHint>)

internal object Briefings {
    const val LEGAL_MOVES_PREFIX = "Legal moves:"

    /** Longest continuation printed per engine line, in plies after the line's own move. */
    private const val MAX_CONTINUATION = 7

    /**
     * Plain text: FEN, side to move, check, every legal move in SAN and the local engine's lines.
     * [notes] are extra remarks for Claude (e.g. a user override that could not be honoured).
     */
    fun build(position: Position, lines: List<EngineHint>, notes: List<String> = emptyList()): Briefing {
        val mover = sideName(position.sideToMove)
        val legal = position.legalMoves()
        val text = StringBuilder(512)
        text.append("FEN: ").append(position.toFen()).append('\n')
        text.append("Side to move: ").append(mover).append('\n')
        text.append("Check: ").append(if (position.isInCheck()) "yes, $mover's king is in check" else "no").append('\n')
        for (note in notes) text.append("Note: ").append(note).append('\n')

        if (legal.isEmpty()) {
            text.append(LEGAL_MOVES_PREFIX).append(" none\n")
            text.append("Game over: ").append(gameOverSentence(position)).append(' ')
            text.append("There is no move to recommend, so do not call recommend_move; ")
            text.append("reply with one short sentence saying that the game is over.")
            return Briefing(text.toString(), emptyList())
        }

        text.append(LEGAL_MOVES_PREFIX).append(' ')
        legal.joinTo(text, " ") { position.san(it) }
        text.append('\n')
        if (position.isInsufficientMaterial()) {
            text.append("Note: neither side has enough material left to give checkmate, so the game is a draw.\n")
        }

        if (lines.isEmpty()) {
            text.append("Local engine lines: none available for this position.")
        } else {
            text.append("Local engine lines (a shallow search on the phone, so treat them as a tactical sanity check ")
            text.append("and not as the answer). Each line: move (score for ").append(mover)
            text.append(" in pawns, or a forced mate) and the continuation it expects:")
            lines.forEachIndexed { index, line ->
                text.append('\n').append(index + 1).append(". ").append(line.san)
                text.append(" (").append(scoreText(line)).append(')')
                val continuation = continuationOf(line)
                if (continuation.isNotEmpty()) text.append(' ').append(continuation.joinToString(" "))
            }
        }
        return Briefing(text.toString(), lines)
    }

    /** "+0.35", "-1.20", "0.00", "mate in 3", "mated in 2". */
    fun scoreText(line: EngineHint): String {
        val mate = line.mateIn
        if (mate != null) return if (mate >= 0) "mate in ${maxOf(mate, 1)}" else "mated in ${-mate}"
        if (line.scoreCp == 0) return "0.00"
        return String.format(Locale.ROOT, "%+.2f", line.scoreCp / 100.0)
    }

    /** The moves the engine expects after the line's own move. */
    fun continuationOf(line: EngineHint): List<String> {
        val rest = if (line.pv.firstOrNull() == line.san) line.pv.drop(1) else line.pv
        return rest.take(MAX_CONTINUATION)
    }

    /** For a position without legal moves: who won, or that it is a draw. */
    fun gameOverSentence(position: Position): String {
        val mover = sideName(position.sideToMove)
        return if (position.isInCheck()) {
            "checkmate. $mover has no legal move and is in check, so ${sideName(position.sideToMove.opposite)} has won."
        } else {
            "stalemate. $mover has no legal move and is not in check, so the game is a draw."
        }
    }

    fun sideName(side: Side): String = if (side == Side.WHITE) "White" else "Black"
}
