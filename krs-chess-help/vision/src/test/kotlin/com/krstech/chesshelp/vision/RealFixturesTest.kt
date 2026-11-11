package com.krstech.chesshelp.vision

import org.junit.Assume
import org.junit.BeforeClass
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Re-checks the module against real chess-app screenshots that are not part of the repository (the piece
 * artwork in them is third-party licensed).
 *
 * Point the environment variable `KRS_VISION_FIXTURES` at a directory holding PNG screenshots and a
 * `truth.json` (an array of entries: `file`, `kind` = "board" | "negative", for boards `rect` {left, top,
 * size} in screenshot pixels and `cells` = 64 view-order characters '.', 'L', 'D'; optional `strict`,
 * `knownIssue`, `sequence`/`step`). Without the variable every test here is skipped.
 *
 * Fixtures carrying a `knownIssue` are evaluated and listed in the report but not asserted, so the suite
 * stays a regression gate for everything that works. The full per-screenshot report is written to
 * `build/reports/vision-real-fixtures.txt`.
 */
class RealFixturesTest {

    private class Result(
        val fixture: Fixture,
        val rect: BoardRect?,
        val errorPx: Double,
        val cellsRight: Int,
        val reliable: Boolean,
        val hintAgrees: Boolean,
        val locateMs: Double,
        val fingerprint: IntArray?,
    ) {
        val locateOk: Boolean get() = if (fixture.isBoard) rect != null && errorPx <= TOLERANCE_PX else rect == null
        val cellsOk: Boolean get() = cellsRight == 64
    }

    companion object {
        private const val TOLERANCE_PX = 2.0
        private var results: List<Result> = emptyList()
        private val report = StringBuilder()

        @JvmStatic
        @BeforeClass
        fun evaluate() {
            val path = System.getenv("KRS_VISION_FIXTURES")
            Assume.assumeTrue("KRS_VISION_FIXTURES is not set; real-screenshot checks skipped", !path.isNullOrBlank())
            val dir = File(path!!)
            assertTrue(File(dir, "truth.json").isFile, "KRS_VISION_FIXTURES=$path has no truth.json")
            val fixtures = Fixture.loadAll(dir)
            assertTrue(fixtures.isNotEmpty(), "truth.json lists no fixtures")

            // The fingerprint of a sequence is always taken with the rectangle found in its first frame,
            // the way the app keeps using one rectangle while it polls.
            val sequenceRect = HashMap<String, BoardRect>()
            val out = ArrayList<Result>()
            run {
                val warm = loadPng(fixtures.first().file)
                repeat(5) { BoardLocator.locate(warm) }
            }
            for (fx in fixtures.sortedWith(compareBy({ it.sequence ?: "" }, { it.step }, { it.name }))) {
                val frame = loadPng(fx.file)
                val t0 = System.nanoTime()
                val rect = BoardLocator.locate(frame)
                val ms = (System.nanoTime() - t0) / 1e6
                var error = Double.NaN
                var right = 0
                var reliable = false
                var hintAgrees = true
                var fingerprint: IntArray? = null
                if (rect != null && fx.isBoard) {
                    error = max(
                        max(abs(rect.left - fx.left), abs(rect.top - fx.top)),
                        max(abs(rect.right - (fx.left + fx.size)), abs(rect.bottom - (fx.top + fx.size))),
                    )
                    val snapshot = BoardSampler.sample(frame, rect)
                    val got = cellsToString(snapshot.cells)
                    right = (0 until 64).count { got[it] == fx.cells!![it] }
                    reliable = snapshot.reliable
                    val hinted = BoardLocator.locate(frame, rect)
                    hintAgrees = hinted != null && hinted.left == rect.left && hinted.top == rect.top && hinted.size == rect.size
                    if (fx.sequence != null) {
                        fingerprint = BoardFingerprint.of(frame, sequenceRect.getOrPut(fx.sequence) { rect })
                    }
                    report.append(
                        String.format(
                            "%-46s locate err %4.1f px  conf %.2f  %5.1f ms  cells %2d/64  reliable=%-5s %s%s%n",
                            fx.name, error, rect.confidence, ms, right, reliable,
                            if (fx.strict) "" else "[cells not asserted] ",
                            fx.knownIssue?.let { "[known issue: $it]" } ?: "",
                        )
                    )
                } else if (fx.isBoard) {
                    report.append(String.format("%-46s NOT LOCATED %s%n", fx.name, fx.knownIssue?.let { "[known issue: $it]" } ?: ""))
                } else {
                    report.append(String.format("%-46s negative: %s  %5.1f ms%n", fx.name, if (rect == null) "rejected" else "FALSE POSITIVE $rect", ms))
                }
                out.add(Result(fx, rect, error, right, reliable, hintAgrees, ms, fingerprint))
            }
            results = out

            val boards = out.filter { it.fixture.isBoard }
            val located = boards.filter { it.rect != null }
            val strict = located.filter { it.fixture.strict }
            val negatives = out.filter { !it.fixture.isBoard }
            report.append(
                String.format(
                    "%nboards %d | located %d | within %.0f px %d | max error of located %.1f px%n" +
                        "cells (strict boards that were located): %d / %d correct | boards 64/64: %d of %d%n" +
                        "negatives %d | false positives %d%n",
                    boards.size, located.size, TOLERANCE_PX, boards.count { it.locateOk },
                    located.maxOfOrNull { it.errorPx } ?: 0.0,
                    strict.sumOf { it.cellsRight }, strict.size * 64, strict.count { it.cellsOk }, strict.size,
                    negatives.size, negatives.count { it.rect != null },
                )
            )
            report.append(timings(fixtures))
            println(report)
            File("build/reports").mkdirs()
            File("build/reports/vision-real-fixtures.txt").writeText(report.toString())
        }

        private fun timings(fixtures: List<Fixture>): String {
            val fx = fixtures.firstOrNull { it.isBoard && it.knownIssue == null } ?: return ""
            val frame = loadPng(fx.file)
            val rect = BoardLocator.locate(frame) ?: return ""
            repeat(20) {
                BoardLocator.locate(frame); BoardLocator.locate(frame, rect)
                BoardSampler.sample(frame, rect); BoardFingerprint.of(frame, rect)
            }
            fun median(n: Int, block: () -> Unit): Double {
                val t = DoubleArray(n) { val s = System.nanoTime(); block(); (System.nanoTime() - s) / 1e6 }
                t.sort()
                return t[n / 2]
            }
            val fp = BoardFingerprint.of(frame, rect)
            return String.format(
                "timings on %s (%dx%d), median: locate %.1f ms | locate with hint %.2f ms | sample %.1f ms | " +
                    "fingerprint %.3f ms | fingerprint distance %.3f ms%n",
                fx.name, frame.width, frame.height,
                median(25) { BoardLocator.locate(frame) }, median(25) { BoardLocator.locate(frame, rect) },
                median(25) { BoardSampler.sample(frame, rect) }, median(100) { BoardFingerprint.of(frame, rect) },
                median(100) { BoardFingerprint.distance(fp, fp) },
            )
        }
    }

    private fun failures(lines: List<String>, what: String) {
        assertTrue(lines.isEmpty(), "$what:\n  " + lines.joinToString("\n  "))
    }

    @Test
    fun `boards are located within two pixels`() {
        failures(
            results.filter { it.fixture.isBoard && !it.locateOk && it.fixture.knownIssue?.startsWith("locate") != true }
                .map { "${it.fixture.name}: ${it.rect ?: "not found"} vs truth (${it.fixture.left}, ${it.fixture.top}, ${it.fixture.size})" },
            "boards not located within $TOLERANCE_PX px",
        )
    }

    @Test
    fun `screens without a board give null`() {
        failures(results.filter { !it.fixture.isBoard && it.rect != null }.map { "${it.fixture.name}: ${it.rect}" }, "false positives")
    }

    @Test
    fun `every square is read correctly`() {
        failures(
            results.filter { it.fixture.isBoard && it.rect != null && it.fixture.strict && it.fixture.knownIssue == null && !it.cellsOk }
                .map { "${it.fixture.name}: ${it.cellsRight}/64" },
            "boards with misread squares",
        )
        failures(
            results.filter { it.fixture.isBoard && it.rect != null && it.fixture.strict && it.fixture.knownIssue == null && !it.reliable }
                .map { it.fixture.name },
            "correctly read boards flagged unreliable",
        )
    }

    @Test
    fun `overall accuracy stays above the recorded level`() {
        val boards = results.filter { it.fixture.isBoard }
        val strict = boards.filter { it.rect != null && it.fixture.strict }
        assertTrue(boards.count { it.locateOk } >= boards.size * 0.97, "fewer than 97 % of all boards located")
        val right = strict.sumOf { it.cellsRight }
        assertTrue(right >= strict.size * 64 * 0.995, "cell accuracy $right / ${strict.size * 64} below 99.5 %")
    }

    @Test
    fun `locating again with the result as hint returns the same rectangle`() {
        failures(results.filter { it.fixture.isBoard && it.rect != null && !it.hintAgrees }.map { it.fixture.name }, "hint disagreed")
    }

    @Test
    fun `fingerprint tells moved from unchanged in consecutive screenshots`() {
        val problems = ArrayList<String>()
        var compared = 0
        for ((_, steps) in results.filter { it.fixture.sequence != null && it.fingerprint != null }.groupBy { it.fixture.sequence }) {
            val ordered = steps.sortedBy { it.fixture.step }
            for (k in 1 until ordered.size) {
                val a = ordered[k - 1]
                val b = ordered[k]
                val d = BoardFingerprint.distance(a.fingerprint!!, b.fingerprint!!)
                val moved = a.fixture.cells != b.fixture.cells
                compared++
                if (moved != (d > BoardFingerprint.CHANGE_THRESHOLD)) {
                    problems.add("${a.fixture.name} -> ${b.fixture.name}: distance $d, position ${if (moved) "changed" else "unchanged"}")
                }
            }
        }
        failures(problems, "fingerprint misjudged")
        if (results.any { it.fixture.sequence != null }) assertTrue(compared > 0)
    }

    @Test
    fun `a full search stays within two hundred milliseconds`() {
        val slow = results.filter { it.locateMs > 200.0 }
        // single timings on a shared build machine jitter; a systematic overrun would hit most frames
        assertTrue(slow.size <= results.size / 10, "slow frames: " + slow.joinToString { "${it.fixture.name} ${it.locateMs.toInt()} ms" })
        assertEquals(results.size, results.count { it.locateMs > 0.0 })
    }
}
