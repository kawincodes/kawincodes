package com.krstech.chesshelp.vision

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun BufferedImage.toFrame(): Frame {
    val pixels = IntArray(width * height)
    getRGB(0, 0, width, height, pixels, 0, width)
    return Frame(width, height, pixels)
}

fun Frame.toImage(): BufferedImage {
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    image.setRGB(0, 0, width, height, pixels, 0, width)
    return image
}

fun loadPng(file: File): Frame = ImageIO.read(file).toFrame()

/** Expected view-order cell states from a 64-character string of '.', 'L', 'D'. */
fun cellsOf(text: String): Array<CellState> {
    require(text.length == 64) { "expected 64 cells, got ${text.length}" }
    return Array(64) {
        when (text[it]) {
            '.' -> CellState.EMPTY
            'L' -> CellState.LIGHT_PIECE
            'D' -> CellState.DARK_PIECE
            else -> throw IllegalArgumentException("bad cell '${text[it]}'")
        }
    }
}

fun cellsToString(cells: Array<CellState>): String = buildString {
    for (c in cells) append(
        when (c) {
            CellState.EMPTY -> '.'
            CellState.LIGHT_PIECE -> 'L'
            CellState.DARK_PIECE -> 'D'
        }
    )
}

/** Minimal JSON reader (objects -> Map, arrays -> List, numbers -> Double) for the fixtures' truth.json. */
object MiniJson {
    fun parse(text: String): Any? {
        val p = Parser(text)
        val v = p.value()
        p.skipWs()
        require(p.pos == text.length) { "trailing JSON at ${p.pos}" }
        return v
    }

    private class Parser(val s: String) {
        var pos = 0

        fun skipWs() {
            while (pos < s.length && s[pos].isWhitespace()) pos++
        }

        fun value(): Any? {
            skipWs()
            require(pos < s.length) { "unexpected end of JSON" }
            return when (val c = s[pos]) {
                '{' -> obj()
                '[' -> arr()
                '"' -> str()
                't' -> lit("true", true)
                'f' -> lit("false", false)
                'n' -> lit("null", null)
                else -> if (c == '-' || c.isDigit()) num() else throw IllegalArgumentException("bad JSON at $pos")
            }
        }

        private fun lit(word: String, v: Any?): Any? {
            require(s.startsWith(word, pos)) { "bad JSON literal at $pos" }
            pos += word.length
            return v
        }

        private fun num(): Double {
            val start = pos
            while (pos < s.length && (s[pos].isDigit() || s[pos] in "+-.eE")) pos++
            return s.substring(start, pos).toDouble()
        }

        private fun str(): String {
            pos++
            val sb = StringBuilder()
            while (s[pos] != '"') {
                var c = s[pos++]
                if (c == '\\') {
                    c = s[pos++]
                    when (c) {
                        'n' -> sb.append('\n')
                        't' -> sb.append('\t')
                        'r' -> sb.append('\r')
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000C')
                        'u' -> { sb.append(s.substring(pos, pos + 4).toInt(16).toChar()); pos += 4 }
                        else -> sb.append(c)
                    }
                } else sb.append(c)
            }
            pos++
            return sb.toString()
        }

        private fun arr(): List<Any?> {
            pos++
            val out = ArrayList<Any?>()
            skipWs()
            if (s[pos] == ']') { pos++; return out }
            while (true) {
                out.add(value())
                skipWs()
                if (s[pos] == ',') { pos++; continue }
                require(s[pos] == ']') { "expected ] at $pos" }
                pos++
                return out
            }
        }

        private fun obj(): Map<String, Any?> {
            pos++
            val out = LinkedHashMap<String, Any?>()
            skipWs()
            if (s[pos] == '}') { pos++; return out }
            while (true) {
                skipWs()
                val k = str()
                skipWs()
                require(s[pos] == ':') { "expected : at $pos" }
                pos++
                out[k] = value()
                skipWs()
                if (s[pos] == ',') { pos++; continue }
                require(s[pos] == '}') { "expected } at $pos" }
                pos++
                return out
            }
        }
    }
}

/** One entry of a fixtures directory's truth.json. */
class Fixture(
    val file: File,
    val isBoard: Boolean,
    /** True squares rectangle in screenshot pixels (DOM bounding box x device scale factor). */
    val left: Double,
    val top: Double,
    val size: Double,
    /** 64 view-order characters ('.', 'L', 'D'), boards only. */
    val cells: String?,
    /** False when the piece set makes the two colours (or pawns and dots) indistinguishable by design. */
    val strict: Boolean,
    /** Why this fixture is currently expected to fail; such fixtures are reported but not asserted. */
    val knownIssue: String?,
    val label: String,
    val features: List<String>,
    /** Screenshots of one board taken one after the other share a sequence id; [step] orders them. */
    val sequence: String?,
    val step: Int,
) {
    val name: String get() = file.name

    companion object {
        fun loadAll(dir: File): List<Fixture> {
            val truth = File(dir, "truth.json")
            require(truth.isFile) { "missing ${truth.path}" }
            @Suppress("UNCHECKED_CAST")
            val entries = MiniJson.parse(truth.readText()) as List<Map<String, Any?>>
            return entries.map { e ->
                val rect = e["rect"] as? Map<*, *>
                Fixture(
                    file = File(dir, e["file"] as String),
                    isBoard = e["kind"] == "board",
                    left = (rect?.get("left") as? Double) ?: 0.0,
                    top = (rect?.get("top") as? Double) ?: 0.0,
                    size = (rect?.get("size") as? Double) ?: 0.0,
                    cells = e["cells"] as? String,
                    strict = (e["strict"] as? Boolean) ?: true,
                    knownIssue = e["knownIssue"] as? String,
                    label = listOfNotNull(e["site"], e["theme"], e["pieceSet"]).joinToString("/"),
                    features = (e["features"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                    sequence = e["sequence"] as? String,
                    step = (e["step"] as? Double)?.toInt() ?: 0,
                )
            }
        }
    }
}
