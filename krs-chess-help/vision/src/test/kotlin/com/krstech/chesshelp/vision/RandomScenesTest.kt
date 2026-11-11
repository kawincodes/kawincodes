package com.krstech.chesshelp.vision

import java.awt.Color
import java.util.Random
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Seeded random boards: arbitrary two-tone colours, sizes from 36 % to 100 % of the shorter screen side, any
 * offset, random positions and decorations. Deterministic (fixed seeds); set the environment variable
 * `KRS_VISION_RANDOM_SCENES` to run more than the default number.
 */
class RandomScenesTest {

    private val screens = listOf(720 to 1560, 1080 to 2400, 720 to 1280, 1080 to 2340, 1600 to 960, 1200 to 2000, 2000 to 1200)

    private fun luma(c: Color) = (c.red * 77 + c.green * 150 + c.blue * 29) shr 8

    private fun randomTheme(random: Random): Theme {
        while (true) {
            val light = Color(165 + random.nextInt(91), 165 + random.nextInt(91), 165 + random.nextInt(91))
            val dark = Color(
                (light.red * (0.4 + random.nextDouble() * 0.5)).toInt(),
                (light.green * (0.4 + random.nextDouble() * 0.5)).toInt(),
                (light.blue * (0.4 + random.nextDouble() * 0.5)).toInt(),
            )
            // squares must differ in luminance, and the dark squares must not swallow the dark pieces
            if (luma(light) - luma(dark) >= 24 && maxOf(dark.red, dark.green, dark.blue) >= 100) return Theme("random", light, dark)
        }
    }

    private fun randomScene(seed: Long): Scene {
        val random = Random(seed)
        val (w, h) = screens[random.nextInt(screens.size)]
        val shortSide = minOf(w, h)
        val size = (shortSide * (0.36 + random.nextDouble() * 0.64)).toInt().coerceAtMost(shortSide)
        val left = if (random.nextInt(4) == 0) 0 else random.nextInt(w - size + 1)
        val top = random.nextInt(h - size + 1)
        val cells = CharArray(64) { '.' }
        var light = 0
        var dark = 0
        for (i in 0 until 64) {
            if (random.nextInt(100) < 30) {
                if (random.nextBoolean()) { if (light < 16) { cells[i] = 'L'; light++ } } else if (dark < 16) { cells[i] = 'D'; dark++ }
            }
        }
        val empty = (0 until 64).filter { cells[it] == '.' }.shuffled(random)
        val occupied = (0 until 64).filter { cells[it] != '.' }.shuffled(random)
        val lastMove = if (occupied.isNotEmpty() && random.nextBoolean()) intArrayOf(empty[0], occupied[0]) else intArrayOf()
        val dots = if (random.nextInt(3) == 0) empty.drop(1).take(1 + random.nextInt(6)).toIntArray() else intArrayOf()
        val rings = if (occupied.size > 2 && random.nextInt(3) == 0) intArrayOf(occupied[1]) else intArrayOf()
        val corner = if (occupied.size > 3 && random.nextInt(4) == 0) intArrayOf(occupied[2]) else intArrayOf()
        val check = if (occupied.size > 4 && random.nextInt(4) == 0) occupied[3] else -1
        val selected = if (occupied.size > 5 && random.nextInt(3) == 0) occupied[4] else -1
        val page = when (random.nextInt(4)) {
            0 -> Color(255, 255, 255)
            1 -> Color(18, 18, 18)
            2 -> Color(48, 46, 43)
            else -> Color(random.nextInt(256), random.nextInt(256), random.nextInt(256))
        }
        return Scene(
            w, h, left, top, size, randomTheme(random), String(cells), pageColor = page, clutter = random.nextInt(4) != 0,
            frameWidth = if (random.nextInt(3) == 0) 1 + random.nextInt(24) else 0, coords = random.nextBoolean(),
            lastMove = lastMove, selected = selected, check = check, dots = dots, rings = rings, cornerHints = corner,
            texture = if (random.nextInt(4) == 0) 1 + random.nextInt(6) else 0, seed = seed, clutterSeed = seed * 31 + 7,
            name = "random #$seed",
        )
    }

    @Test
    fun `random boards are located and read`() {
        val count = System.getenv("KRS_VISION_RANDOM_SCENES")?.toIntOrNull() ?: 60
        val problems = ArrayList<String>()
        for (seed in 1L..count) {
            val scene = randomScene(seed)
            val frame = scene.render()
            val rect = BoardLocator.locate(frame)
            if (rect == null) { problems.add("$scene: not located"); continue }
            val worst = maxOf(
                abs(rect.left - scene.left), abs(rect.top - scene.top),
                abs(rect.right - (scene.left + scene.size)), abs(rect.bottom - (scene.top + scene.size)),
            )
            if (worst > 2) { problems.add("$scene: located $rect"); continue }
            val snapshot = BoardSampler.sample(frame, rect)
            val got = cellsToString(snapshot.cells)
            if (got != scene.cells) {
                val wrong = (0 until 64).filter { got[it] != scene.cells[it] }
                problems.add("$scene: cells " + wrong.joinToString { "(${it / 8},${it % 8}) ${scene.cells[it]}>${got[it]}" })
            } else if (!snapshot.reliable) problems.add("$scene: flagged unreliable")
        }
        assertTrue(problems.isEmpty(), "${problems.size} of $count random scenes failed:\n  " + problems.joinToString("\n  "))
    }

    @Test
    fun `random scenes really vary`() {
        // guards the generator itself: sizes span the supported range and every decoration occurs
        val scenes = (1L..60L).map { randomScene(it) }
        assertTrue(scenes.any { it.size <= minOf(it.width, it.height) * 0.45 })
        assertTrue(scenes.any { it.size >= minOf(it.width, it.height) * 0.9 })
        assertTrue(scenes.any { it.dots.isNotEmpty() } && scenes.any { it.check >= 0 } && scenes.any { it.texture > 0 })
        assertTrue(scenes.any { it.left == 0 } && scenes.any { it.frameWidth > 0 } && scenes.any { it.width > it.height })
        assertNotNull(scenes.firstOrNull { it.cells.count { c -> c != '.' } >= 20 })
    }
}
