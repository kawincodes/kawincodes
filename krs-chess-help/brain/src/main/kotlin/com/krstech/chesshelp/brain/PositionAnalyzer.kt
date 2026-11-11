package com.krstech.chesshelp.brain

import com.krstech.chesshelp.chess.Move
import com.krstech.chesshelp.chess.Position

/**
 * One line of the local engine. [scoreCp] is in centipawns from the point of view of the side to move;
 * [mateIn] > 0 means the side to move mates in that many moves, < 0 that it gets mated.
 * [pv] is in SAN and starts with the line's own move.
 */
data class EngineHint(
    val move: Move,
    val san: String,
    val scoreCp: Int,
    val mateIn: Int?,
    val pv: List<String>,
)

/**
 * The local engine as the brain sees it. Blocking; called off the main thread. The real engine sits
 * behind [EnginePositionAnalyzer].
 */
fun interface PositionAnalyzer {
    /**
     * Best-first lines for [position] found within about [timeMs]; empty when there is no legal move.
     * Implementations should give up early when the calling thread is interrupted.
     */
    fun analyze(position: Position, timeMs: Long): List<EngineHint>
}

/**
 * Runs [analyzer] defensively: the engine is a hint, so a failure inside it must not take the analysis
 * down, and a line whose move is not legal in [position] is dropped.
 */
internal fun PositionAnalyzer.safeLines(position: Position, timeMs: Long): List<EngineHint> {
    if (position.legalMoves().isEmpty()) return emptyList()
    val lines = try {
        analyze(position, timeMs)
    } catch (interrupted: InterruptedException) {
        throw interrupted
    } catch (_: Exception) {
        emptyList()
    }
    return lines.filter { position.isLegal(it.move) }
}
