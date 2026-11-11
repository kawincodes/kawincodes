package com.krstech.chesshelp.chess

/**
 * Limits of one [Engine.analyze] call; the search ends at whichever is reached first.
 *
 * @property maxDepth deepest iteration in plies (clamped to 1..64).
 * @property timeMs time budget. The first iteration is always completed, so a result is returned
 *   even for a budget of 0; after that the search never runs past the budget by more than the
 *   few milliseconds between two clock checks, and it returns early when another iteration
 *   would not fit.
 * @property multiPv number of best moves to report (at least 1, at most the number of legal moves).
 */
data class SearchLimits(val maxDepth: Int = 32, val timeMs: Long = 800, val multiPv: Int = 3)

/**
 * One candidate move and the line the engine expects after it.
 *
 * @property scoreCp centipawns from the point of view of the side to move in the analysed
 *   position. For a forced mate it is `±(32000 - plies until mate)`; read [mateIn] instead.
 * @property mateIn `N > 0`: the side to move mates in N moves; `N < 0`: it is mated in -N moves;
 *   `null`: no forced mate found.
 * @property pv the line in SAN, starting with [san]. Every move in it is legal.
 * @property depth the iteration this line comes from (0 only for the static fallback, see [Engine.analyze]).
 */
data class EngineLine(
    val move: Move,
    val san: String,
    val scoreCp: Int,
    val mateIn: Int?,
    val pv: List<String>,
    val depth: Int,
)

/**
 * @property lines best first; empty when the position has no legal move (checkmate or stalemate).
 * @property depth the last fully completed iteration, which all [lines] come from.
 * @property nodes positions visited, including those of an unfinished last iteration.
 */
data class EngineResult(val lines: List<EngineLine>, val depth: Int, val nodes: Long, val elapsedMs: Long)

/**
 * A small alpha-beta chess engine. Instances are independent of each other and hold no state
 * between calls that could change a result; an instance keeps about 8.5 MB of search tables
 * (allocated on the first call) for reuse. Calls on one instance are serialised.
 */
class Engine {
    private var searcher: Searcher? = null

    /**
     * Searches [position] on the calling thread and returns the best [SearchLimits.multiPv] moves
     * of the last iteration that was completed.
     *
     * [shouldStop] is polled every few thousand positions; once it returns true the search ends
     * and the result of the last completed iteration is returned. At least one line is returned
     * whenever a legal move exists: the first iteration is not interrupted (it takes a
     * millisecond or so), except in freak placements where it runs away, for which the moves are
     * ranked by static evaluation and reported with depth 0.
     *
     * Any [position] is accepted, including placements that could not occur in a game.
     */
    @Synchronized
    fun analyze(
        position: Position,
        limits: SearchLimits = SearchLimits(),
        shouldStop: () -> Boolean = { false },
    ): EngineResult {
        val active = searcher ?: Searcher().also { searcher = it }
        return active.analyze(position, limits, shouldStop)
    }
}
