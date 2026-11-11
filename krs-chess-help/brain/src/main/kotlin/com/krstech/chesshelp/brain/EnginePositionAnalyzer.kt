package com.krstech.chesshelp.brain

import com.krstech.chesshelp.chess.Engine
import com.krstech.chesshelp.chess.Position
import com.krstech.chesshelp.chess.SearchLimits

/**
 * [PositionAnalyzer] on top of the :core engine. One instance keeps one engine (and its search tables,
 * about 8.5 MB once used); calls are serialised by the engine itself.
 */
class EnginePositionAnalyzer(private val multiPv: Int = 3) : PositionAnalyzer {
    private val engine = Engine()

    override fun analyze(position: Position, timeMs: Long): List<EngineHint> {
        val caller = Thread.currentThread()
        // A cancelled analysis interrupts its thread; the flag is left set for whoever blocks next.
        val result = engine.analyze(position, SearchLimits(timeMs = timeMs, multiPv = multiPv)) { caller.isInterrupted }
        return result.lines.map { EngineHint(it.move, it.san, it.scoreCp, it.mateIn, it.pv) }
    }
}
