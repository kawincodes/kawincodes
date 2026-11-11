package com.krstech.chesshelp.chess

import kotlin.random.Random
import kotlin.test.Test

/** TEMPORARY exploration, deleted before the task is finished. */
class ScratchExploreTest {
    private val out = StringBuilder()
    private fun log(s: String) { out.append(s).append('\n') }

    private fun play(fen: String, attacker: SearchLimits, defender: SearchLimits?, seed: Int, maxPlies: Int, useHistory: Boolean = false): String {
        val history = ArrayList<Position>()
        var position = Position.fromFen(fen)
        val attackerSide = position.sideToMove
        val random = Random(seed)
        val engine = Engine()
        var plies = 0
        val seen = HashMap<String, Int>()
        var maxRep = 1
        while (plies < maxPlies) {
            val moves = position.legalMoves()
            if (moves.isEmpty()) return (if (position.isCheckmate()) "MATE" else "STALEMATE") + " after $plies plies (maxRep $maxRep)"
            if (position.isInsufficientMaterial()) return "INSUFFICIENT after $plies plies"
            val key = position.placement() + position.sideToMove
            val rep = (seen[key] ?: 0) + 1
            seen[key] = rep
            if (rep > maxRep) maxRep = rep
            val past = if (useHistory) history.toList() else emptyList()
            val move = if (position.sideToMove == attackerSide) {
                engine.analyze(position, past, attacker).lines[0].move
            } else if (defender != null) {
                engine.analyze(position, past, defender).lines[0].move
            } else {
                moves[random.nextInt(moves.size)]
            }
            history.add(position)
            position = position.play(move)
            plies++
        }
        return "UNFINISHED after $plies plies (maxRep $maxRep) ${position.toFen()}"
    }

    @Test
    fun explore() {
        val d4 = SearchLimits(maxDepth = 4, timeMs = 60000, multiPv = 1)
        val d6 = SearchLimits(maxDepth = 6, timeMs = 60000, multiPv = 1)
        val d8 = SearchLimits(maxDepth = 8, timeMs = 60000, multiPv = 1)
        val t100 = SearchLimits(timeMs = 100, multiPv = 3)
        val endings = listOf(
            "8/8/8/4k3/8/8/8/Q3K3 w - - 0 1", "8/8/8/4k3/8/8/8/R3K3 w - - 0 1", "8/8/3k4/8/8/8/8/2B1KB2 w - - 0 1",
            "8/8/3k4/8/8/8/8/1NB1K3 w - - 0 1", "8/8/3k4/8/8/8/4P3/4K3 w - - 0 1", "4k3/8/8/8/8/8/PPP5/2K5 w - - 0 1",
            "8/5k2/8/8/8/8/1q6/6K1 b - - 0 1", "8/8/8/3k4/8/8/r7/4K3 b - - 0 1", "8/8/3k4/8/8/8/5PPP/6K1 w - - 0 1",
            "8/pp3k2/8/8/8/8/5PPP/3R2K1 w - - 0 1", "5k2/8/8/8/8/8/8/3QK3 b - - 0 1",
        )
        for (fen in endings) {
            for (h in listOf(false, true)) {
                log("history=$h d4 vs d4: " + play(fen, d4, d4, 0, 250, h) + " :: $fen")
                log("history=$h d6 vs d6: " + play(fen, d6, d6, 0, 250, h) + " :: $fen")
                log("history=$h d8 vs d6: " + play(fen, d8, d6, 0, 250, h) + " :: $fen")
                log("history=$h t100 vs d6: " + play(fen, t100, d6, 0, 250, h) + " :: $fen")
            }
        }
        java.io.File("/tmp/claude-0/-workspace-kawincodes/21b00ec2-ead7-52ce-b155-2d140ef991ec/scratchpad/explore.txt").writeText(out.toString())
    }
}
