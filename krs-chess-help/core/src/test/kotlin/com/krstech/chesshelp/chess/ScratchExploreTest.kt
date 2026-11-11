package com.krstech.chesshelp.chess

import kotlin.test.Test

/** TEMPORARY exploration, deleted before the task is finished. */
class ScratchExploreTest {
    private val out = StringBuilder()
    private fun log(s: String) { out.append(s).append('\n') }

    @Test
    fun explore() {
        val candidates = listOf(
            "5r1k/3p2pp/8/8/1n6/8/P4PPP/R2QK3 w Q - 0 1",
            "r7/6k1/8/4p3/3K4/8/8/4Q3 w - - 0 1",
            "q3k3/8/8/3N4/8/8/5PPP/6K1 w - - 0 1",
            "k7/2Q5/8/8/8/8/8/7K w - - 0 1",
            "rnb1kbnr/pppp1ppp/8/4p1q1/4P3/3P4/PPP2PPP/RNBQKBNR w KQkq - 1 3",
            "4k3/8/2p5/3p4/8/8/3Q4/4K3 w - - 0 1",
            "4k3/8/8/3r4/8/8/3R4/4K3 w - - 0 1",
            "8/P7/8/8/8/8/8/k6K w - - 0 1",
            "8/8/8/8/8/5k2/5p2/5K2 w - - 0 1",
            "7k/5Q2/8/8/8/8/8/K7 w - - 0 1",
            "5k2/8/5K2/5Q2/8/8/8/8 w - - 0 1",
            "r1b1k2r/ppppnppp/2n2q2/2b5/3NP3/2P1B3/PP3PPP/RN1QKB1R w KQkq - 0 1",
            "2r3k1/5ppp/8/8/8/8/2R2PPP/6K1 w - - 0 1",
        )
        for (fen in candidates) {
            for (d in intArrayOf(1, 2, 3, 4, 6, 8)) {
                val r = Engine().analyze(Position.fromFen(fen), SearchLimits(maxDepth = d, timeMs = 20000, multiPv = 3))
                log("d=$d nodes=${r.nodes} ms=${r.elapsedMs} ${r.lines.map { "${it.san}(${it.move}) ${it.scoreCp} m=${it.mateIn} ${it.pv}" }}  :: $fen")
            }
        }
        java.io.File("/tmp/claude-0/-workspace-kawincodes/21b00ec2-ead7-52ce-b155-2d140ef991ec/scratchpad/explore.txt").writeText(out.toString())
    }
}
