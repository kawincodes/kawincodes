package com.krstech.chesshelp.chess

import kotlin.random.Random
import kotlin.test.Test

/** TEMPORARY exploration, deleted before the task is finished. */
class ScratchExploreTest {
    @Test
    fun explore() {
        val out = StringBuilder()
        val fen = "4q2r/1k1b2p1/2r1pn2/2PP1N1p/1p2N1RP/p1QPb3/PBK1PP2/1R3B2 w - - 0 34"
        for (pv in intArrayOf(1, 3, 4)) for (d in 1..3) {
            val r = Engine().analyze(Position.fromFen(fen), SearchLimits(maxDepth = d, timeMs = 100000, multiPv = pv))
            out.append("multiPv=$pv d=$d nodes=${r.nodes} ms=${r.elapsedMs} ${r.lines.map { it.san + " " + it.scoreCp }}\n")
        }
        // distribution of depth-1 node counts over random-play positions
        val counts = ArrayList<Long>()
        val random = Random(99)
        repeat(60) {
            var position = Position.START
            var ply = 0
            while (ply < 150) {
                val moves = position.legalMoves()
                if (moves.isEmpty()) break
                position = position.play(moves[random.nextInt(moves.size)])
                ply++
                if (ply % 10 == 5 && position.legalMoves().isNotEmpty()) {
                    counts.add(Engine().analyze(position, SearchLimits(maxDepth = 1, timeMs = 100000, multiPv = 4)).nodes)
                }
            }
        }
        counts.sort()
        out.append("depth-1 nodes over ${counts.size} random positions: median ${counts[counts.size / 2]}, p90 ${counts[counts.size * 9 / 10]}, p99 ${counts[counts.size * 99 / 100]}, max ${counts.last()}\n")
        java.io.File("/tmp/claude-0/-workspace-kawincodes/21b00ec2-ead7-52ce-b155-2d140ef991ec/scratchpad/explore.txt").writeText(out.toString())
    }
}
