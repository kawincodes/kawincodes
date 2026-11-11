package com.krstech.chesshelp.vision

import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

// TEMPORARY development harness (not part of the shipped tests).
fun main(args: Array<String>) {
    val dir = File(args[0])
    val mode = args.getOrElse(1) { "all" }
    if (mode == "render") {
        val out = File(args[2]); out.mkdirs()
        for ((i, s) in SyntheticScenes.all.withIndex()) javax.imageio.ImageIO.write(s.renderImage(), "png", File(out, "scene_$i.png"))
        val neg = mapOf("kb" to Clutter.keyboard(1080, 2400, 13), "icons" to Clutter.iconGrid(1080, 2400, 4, 10), "text" to Clutter.textPage(1080, 2400, 6),
            "photo" to Clutter.photoNoise(1080, 2400, 8), "app" to Clutter.appWithoutBoard(1080, 2400, java.awt.Color(48, 46, 43), 3), "tiles" to Clutter.tileGrid(1080, 2400, 15),
            "c12" to Clutter.checker(1080, 2400, 30, 500, 85, 12, 12, Themes.PURPLE, java.awt.Color(40, 40, 44), 8))
        for ((k, f) in neg) javax.imageio.ImageIO.write(f.toImage(), "png", File(out, "neg_$k.png"))
        return
    }
    val only = args.getOrNull(2)?.let { Regex(it) }
    val fixtures = Fixture.loadAll(dir).filter { only == null || only.containsMatchIn(it.file.name) }
    when (mode) {
        "trace" -> for (fx in fixtures) {
            val frame = loadPng(fx.file)
            println("== ${fx.file.name} truth=(${fx.left}, ${fx.top}, ${fx.size})")
            val r = BoardLocator.locate(frame, null) { println(it) }
            println("-> $r")
        }
        "dump" -> {
            println("file,cell,truth,area,coreArea,boxW,boxH,touch,bright,bgLuma,tau,thickBright,thickDark,coreMain")
            for (fx in fixtures) {
                if (!fx.isBoard) continue
                val frame = loadPng(fx.file)
                val rect = BoardRect(fx.left.roundToInt(), fx.top.roundToInt(), fx.size.roundToInt(), 1f)
                val analysis = BoardSampler.analyze(frame, rect)
                BoardSampler.classify(rect, analysis)
                val feats = analysis.features
                for (i in 0 until 64) {
                    val f = feats[i]
                    println("${fx.file.name},$i,${fx.cells!![i]},${f.area},${f.coreArea},${f.boxWidth},${f.boxHeight},${f.borderTouch},${f.lightness},${f.bgLuma},${f.threshold},${f.thickBright},${f.thickDark},${f.coreMain}")
                }
            }
        }
        "seq" -> {
            var prev: IntArray? = null
            var prevCells = ""
            var prevName = ""
            for (fx in fixtures.filter { it.file.name.startsWith("s") }.sortedBy { it.file.name }) {
                val frame = loadPng(fx.file)
                val rect = BoardLocator.locate(frame)!!
                val hinted = BoardLocator.locate(frame, rect)
                val fp = BoardFingerprint.of(frame, rect)
                val snap = BoardSampler.sample(frame, rect)
                val got = cellsToString(snap.cells)
                if (prev != null && prevName.substring(0, 2) == fx.file.name.substring(0, 2)) {
                    val d = BoardFingerprint.distance(prev, fp)
                    val changed = prevCells != fx.cells
                    println(String.format("%s -> %s dist=%.4f truthChanged=%s cellsOk=%s hintSame=%s", prevName, fx.file.name, d, changed, got == fx.cells, hinted == rect))
                }
                prev = fp; prevCells = fx.cells!!; prevName = fx.file.name
            }
        }
        "time" -> {
            val fx = fixtures.first { it.isBoard }
            val frame = loadPng(fx.file)
            val rect = BoardLocator.locate(frame)!!
            repeat(30) { BoardLocator.locate(frame); BoardLocator.locate(frame, rect); BoardSampler.sample(frame, rect); BoardFingerprint.of(frame, rect) }
            fun bench(name: String, n: Int, block: () -> Unit) {
                val times = DoubleArray(n)
                for (i in 0 until n) { val t = System.nanoTime(); block(); times[i] = (System.nanoTime() - t) / 1e6 }
                times.sort()
                println(String.format("%-22s median %.2f ms  p90 %.2f ms  max %.2f ms (%dx%d)", name, times[n / 2], times[n * 9 / 10], times[n - 1], frame.width, frame.height))
            }
            bench("locate (full search)", 40) { BoardLocator.locate(frame) }
            bench("locate (hint)", 40) { BoardLocator.locate(frame, rect) }
            bench("sample", 40) { BoardSampler.sample(frame, rect) }
            bench("fingerprint.of", 200) { BoardFingerprint.of(frame, rect) }
            val a = BoardFingerprint.of(frame, rect)
            bench("fingerprint.distance", 200) { BoardFingerprint.distance(a, a) }
            bench("cropBoard 512", 20) { FrameOps.cropBoard(frame, rect, 512) }
            bench("downscale 768", 20) { FrameOps.downscale(frame, 768) }
        }
        else -> {
            // warm-up
            run {
                val frame = loadPng(fixtures.first().file)
                repeat(6) { BoardLocator.locate(frame) }
            }
            var boards = 0; var located = 0; var within2 = 0; var perfect = 0; var cellsOk = 0; var cellsTotal = 0
            var strictBoards = 0
            var falsePos = 0; var negatives = 0
            for (fx in fixtures) {
                val frame = loadPng(fx.file)
                val t0 = System.nanoTime()
                val rect = BoardLocator.locate(frame)
                val t1 = System.nanoTime()
                val ms = (t1 - t0) / 1e6
                if (!fx.isBoard) {
                    negatives++
                    if (rect != null) { falsePos++; println("FALSE POSITIVE ${fx.file.name}: $rect") }
                    else println(String.format("neg ok  %-44s %5.1f ms", fx.file.name, ms))
                    continue
                }
                boards++
                if (rect == null) { println("NOT FOUND ${fx.file.name} ${fx.label}"); continue }
                located++
                val err = max(
                    max(abs(rect.left - fx.left), abs(rect.top - fx.top)),
                    max(abs(rect.right - (fx.left + fx.size)), abs(rect.bottom - (fx.top + fx.size))),
                )
                if (err <= 2.0) within2++
                val snap = BoardSampler.sample(frame, rect)
                val got = cellsToString(snap.cells)
                var ok = 0
                val wrong = StringBuilder()
                for (i in 0 until 64) if (got[i] == fx.cells!![i]) ok++ else wrong.append(" ${i / 8},${i % 8}:${fx.cells[i]}>${got[i]}")
                if (fx.strict) { strictBoards++; cellsOk += ok; cellsTotal += 64; if (ok == 64) perfect++ }
                val meanConf = snap.confidence.average()
                val shaky = snap.confidence.count { it < 0.25f }
                var wrongConf = 0f
                for (i in 0 until 64) if (got[i] != fx.cells!![i]) wrongConf = maxOf(wrongConf, snap.confidence[i])
                val pairs = BoardSampler.checkerPairs(BoardSampler.analyze(frame, rect).features)
                println(String.format("%s %-44s err=%4.1f conf=%.2f %5.1f ms cells=%d/64 rel=%s mean=%.2f shaky=%d wrongConfMax=%.2f pairs=%d %s%s",
                    if (err <= 2.0) "ok " else "BAD", fx.file.name, err, rect.confidence, ms, ok, snap.reliable, meanConf, shaky, wrongConf, pairs,
                    if (fx.strict) "" else "(non-strict) ", wrong))
            }
            println("boards=$boards located=$located within2px=$within2 strictBoards=$strictBoards perfect=$perfect cells=$cellsOk/$cellsTotal negatives=$negatives falsePositives=$falsePos")
        }
    }
}
