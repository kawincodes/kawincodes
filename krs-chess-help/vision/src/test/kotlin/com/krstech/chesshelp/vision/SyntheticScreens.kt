package com.krstech.chesshelp.vision

import java.awt.BasicStroke
import java.awt.Color
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.RadialGradientPaint
import java.awt.RenderingHints
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.awt.geom.Line2D
import java.awt.geom.Path2D
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.util.Random

/** Two-tone board colours. */
class Theme(val name: String, val light: Color, val dark: Color)

object Themes {
    val GREEN = Theme("green", Color(235, 236, 208), Color(115, 149, 82))
    val BROWN = Theme("brown", Color(240, 217, 181), Color(181, 136, 99))
    val BLUE = Theme("blue", Color(222, 227, 230), Color(140, 162, 173))
    val GREY = Theme("grey", Color(205, 205, 205), Color(134, 134, 134))
    val PURPLE = Theme("purple", Color(239, 239, 239), Color(125, 74, 141))
    val NIGHT = Theme("night", Color(128, 138, 158), Color(72, 82, 104))
    val PALE = Theme("pale", Color(242, 242, 242), Color(212, 212, 212))
    val ALL = listOf(GREEN, BROWN, BLUE, GREY, PURPLE, NIGHT, PALE)
}

object Positions {
    const val EMPTY = "................................................................"
    const val START = "DDDDDDDD" + "DDDDDDDD" + "........" + "........" + "........" + "........" + "LLLLLLLL" + "LLLLLLLL"
    const val MIDDLE = "D..D.DD." + "DD..DDDD" + "..D.DD.." + "..DD...." + "...L...." + "..L.LL.." + "LLL.LLLL" + "L.L..LL."
    const val MIDDLE_FLIPPED = ".LL..L.L" + "LLLL.LLL" + "..LL.L.." + "....L..." + "....DD.." + "..DD.D.." + "DDDD..DD" + ".DD.D..D"
    const val SPARSE = "........" + ".....D.." + "........" + "........" + "...L...." + "........" + "....L..." + "........"
    const val PAWN_ENDING = "........" + "DD...D.." + "..D....." + "....L..." + "L.L....." + ".L......" + "........" + "........"
    /** 40 pieces: more than a chess position can hold. */
    const val CROWDED = "DDDDDDDD" + "DDDDDDDD" + "DDDD...." + "........" + "........" + "....LLLL" + "LLLLLLLL" + "LLLLLLLL"
}

/**
 * Description of one synthetic phone screenshot: a board at a known rectangle, drawn the way chess apps draw
 * it, on top of unrelated UI clutter. Indices are view indices (`row * 8 + col`).
 */
class Scene(
    val width: Int,
    val height: Int,
    val left: Int,
    val top: Int,
    val size: Int,
    val theme: Theme,
    val cells: String = Positions.START,
    val pageColor: Color = Color(48, 46, 43),
    val clutter: Boolean = true,
    val frameWidth: Int = 0,
    val coords: Boolean = true,
    val lastMove: IntArray = intArrayOf(),
    val selected: Int = -1,
    val check: Int = -1,
    val dots: IntArray = intArrayOf(),
    val rings: IntArray = intArrayOf(),
    val cornerHints: IntArray = intArrayOf(),
    val arrow: IntArray? = null,
    val texture: Int = 0,
    val seed: Long = 7,
    val clutterSeed: Long = 11,
    val name: String = "",
) {
    val rect: BoardRect get() = BoardRect(left, top, size, 1f)

    fun cellX(col: Int): Int = left + (col * size + 4) / 8
    fun cellY(row: Int): Int = top + (row * size + 4) / 8

    fun copy(
        cells: String = this.cells, lastMove: IntArray = this.lastMove, clutterSeed: Long = this.clutterSeed,
        left: Int = this.left, top: Int = this.top,
    ) = Scene(
        width, height, left, top, size, theme, cells, pageColor, clutter, frameWidth, coords, lastMove, selected,
        check, dots, rings, cornerHints, arrow, texture, seed, clutterSeed, name,
    )

    override fun toString(): String = "$name ${width}x$height board=($left,$top,$size) ${theme.name}"

    fun render(): Frame = renderImage().toFrame()

    fun renderImage(): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.color = pageColor
        g.fillRect(0, 0, width, height)
        if (clutter) Clutter.appScreen(g, width, height, pageColor, Random(clutterSeed))

        if (frameWidth > 0) {
            g.color = Color(60, 45, 35)
            g.fillRect(left - frameWidth, top - frameWidth, size + 2 * frameWidth, size + 2 * frameWidth)
        }
        for (row in 0..7) for (col in 0..7) {
            g.color = if ((row + col) % 2 == 0) theme.light else theme.dark
            g.fillRect(cellX(col), cellY(row), cellX(col + 1) - cellX(col), cellY(row + 1) - cellY(row))
        }
        if (texture > 0) addNoise(image, left, top, size, size, texture, Random(seed))

        val cell = size / 8.0
        for (i in lastMove) overlay(g, i, Color(255, 255, 51, 128))
        if (selected >= 0) overlay(g, selected, Color(20, 85, 30, 128))
        if (check >= 0) {
            val cx = cellX(check % 8) + cell / 2
            val cy = cellY(check / 8) + cell / 2
            g.paint = RadialGradientPaint(
                cx.toFloat(), cy.toFloat(), (cell * 0.7071).toFloat(), floatArrayOf(0f, 0.25f, 0.89f, 1f),
                arrayOf(Color(255, 0, 0, 255), Color(231, 0, 0, 255), Color(169, 0, 0, 0), Color(158, 0, 0, 0)),
            )
            g.fill(cellRect(check))
        }
        for (i in cornerHints) {
            // lichess-style capture hint: everything outside a centred circle is tinted
            val area = Area(cellRect(i))
            val r = cell * 0.566
            area.subtract(Area(Ellipse2D.Double(cellX(i % 8) + cell / 2 - r, cellY(i / 8) + cell / 2 - r, 2 * r, 2 * r)))
            g.color = Color(20, 85, 0, 77)
            g.fill(area)
        }
        if (coords) drawCoordinates(g, cell)

        for (i in 0 until 64) {
            val c = cells[i]
            if (c == '.') continue
            Pieces.draw(g, cellX(i % 8).toDouble(), cellY(i / 8).toDouble(), cell, kindOf(i), c == 'L')
        }
        for (i in dots) {
            // alternate between the two common styles: small opaque-ish green and larger faint black
            val lichess = i % 2 == 0
            val r = cell * (if (lichess) 0.134 else 0.165)
            g.color = if (lichess) Color(20, 85, 30, 128) else Color(0, 0, 0, 36)
            g.fill(Ellipse2D.Double(cellX(i % 8) + cell / 2 - r, cellY(i / 8) + cell / 2 - r, 2 * r, 2 * r))
        }
        for (i in rings) {
            val r = cell * 0.44
            g.color = Color(0, 0, 0, 28)
            g.stroke = BasicStroke((cell * 0.085).toFloat())
            g.draw(Ellipse2D.Double(cellX(i % 8) + cell / 2 - r, cellY(i / 8) + cell / 2 - r, 2 * r, 2 * r))
        }
        arrow?.let { drawArrow(g, it[0], it[1], cell) }
        g.dispose()
        return image
    }

    private fun kindOf(i: Int): Int = (i * 5 + i / 8) % 6

    private fun cellRect(i: Int): Rectangle2D {
        val col = i % 8
        val row = i / 8
        return Rectangle2D.Double(
            cellX(col).toDouble(), cellY(row).toDouble(),
            (cellX(col + 1) - cellX(col)).toDouble(), (cellY(row + 1) - cellY(row)).toDouble(),
        )
    }

    private fun overlay(g: Graphics2D, i: Int, color: Color) {
        g.color = color
        g.fill(cellRect(i))
    }

    /** Font-free stand-ins for the rank/file labels: small stroked marks in the cell corners. */
    private fun drawCoordinates(g: Graphics2D, cell: Double) {
        g.stroke = BasicStroke(maxOf(1.5f, (cell * 0.022).toFloat()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        val s = cell * 0.13
        for (row in 0..7) {
            g.color = if (row % 2 == 0) theme.dark else theme.light
            val x = cellX(0) + cell * 0.05
            val y = cellY(row) + cell * 0.05
            val p = Path2D.Double()
            p.moveTo(x, y); p.lineTo(x + s * 0.7, y); p.lineTo(x + s * 0.25, y + s)
            if (row % 3 == 0) { p.moveTo(x, y + s * 0.5); p.lineTo(x + s * 0.7, y + s * 0.5) }
            g.draw(p)
        }
        for (col in 0..7) {
            g.color = if ((7 + col) % 2 == 0) theme.dark else theme.light
            val x = cellX(col + 1) - cell * 0.05 - s * 0.7
            val y = cellY(8) - cell * 0.05 - s
            g.draw(Ellipse2D.Double(x, y + s * 0.3, s * 0.6, s * 0.7))
            g.draw(Line2D.Double(x + s * 0.6, y, x + s * 0.6, y + s))
        }
    }

    private fun drawArrow(g: Graphics2D, from: Int, to: Int, cell: Double) {
        val x1 = cellX(from % 8) + cell / 2
        val y1 = cellY(from / 8) + cell / 2
        val x2 = cellX(to % 8) + cell / 2
        val y2 = cellY(to / 8) + cell / 2
        val len = Math.hypot(x2 - x1, y2 - y1)
        val ux = (x2 - x1) / len
        val uy = (y2 - y1) / len
        val head = cell * 0.42
        val shaft = Path2D.Double()
        val w = cell * 0.078
        val ex = x2 - ux * head
        val ey = y2 - uy * head
        shaft.moveTo(x1 - uy * w, y1 + ux * w)
        shaft.lineTo(ex - uy * w, ey + ux * w)
        shaft.lineTo(ex - uy * head * 0.55, ey + ux * head * 0.55)
        shaft.lineTo(x2, y2)
        shaft.lineTo(ex + uy * head * 0.55, ey - ux * head * 0.55)
        shaft.lineTo(ex + uy * w, ey - ux * w)
        shaft.lineTo(x1 + uy * w, y1 - ux * w)
        shaft.closePath()
        g.color = Color(21, 120, 27, 150)
        g.fill(shaft)
    }

    companion object {
        fun addNoise(image: BufferedImage, x0: Int, y0: Int, w: Int, h: Int, amplitude: Int, random: Random) {
            for (y in maxOf(0, y0) until minOf(image.height, y0 + h)) {
                for (x in maxOf(0, x0) until minOf(image.width, x0 + w)) {
                    val p = image.getRGB(x, y)
                    val d = random.nextInt(2 * amplitude + 1) - amplitude
                    val r = (((p shr 16) and 0xFF) + d).coerceIn(0, 255)
                    val gg = (((p shr 8) and 0xFF) + d).coerceIn(0, 255)
                    val b = ((p and 0xFF) + d).coerceIn(0, 255)
                    image.setRGB(x, y, (0xFF shl 24) or (r shl 16) or (gg shl 8) or b)
                }
            }
        }
    }
}

/** Filled-and-outlined figurines built from primitive shapes (no fonts involved). */
object Pieces {
    private val LIGHT_FILL = Color(248, 248, 248)
    private val LIGHT_LINE = Color(40, 40, 40)
    private val DARK_FILL = Color(52, 50, 48)
    private val DARK_LINE = Color(12, 12, 12)
    private val DARK_DETAIL = Color(205, 205, 205)

    /** kind: 0 pawn, 1 rook, 2 knight, 3 bishop, 4 queen, 5 king. (x, y) is the cell's top-left corner. */
    fun draw(g: Graphics2D, x: Double, y: Double, cell: Double, kind: Int, light: Boolean) {
        val shape = Area()
        fun rect(x0: Double, y0: Double, x1: Double, y1: Double) =
            shape.add(Area(Rectangle2D.Double(x + x0 * cell, y + y0 * cell, (x1 - x0) * cell, (y1 - y0) * cell)))
        fun oval(cx: Double, cy: Double, rx: Double, ry: Double) =
            shape.add(Area(Ellipse2D.Double(x + (cx - rx) * cell, y + (cy - ry) * cell, 2 * rx * cell, 2 * ry * cell)))
        fun poly(vararg pts: Double) {
            val p = Path2D.Double()
            p.moveTo(x + pts[0] * cell, y + pts[1] * cell)
            for (k in 2 until pts.size step 2) p.lineTo(x + pts[k] * cell, y + pts[k + 1] * cell)
            p.closePath()
            shape.add(Area(p))
        }
        when (kind) {
            0 -> {
                oval(0.5, 0.34, 0.115, 0.115)
                poly(0.44, 0.42, 0.56, 0.42, 0.68, 0.78, 0.32, 0.78)
                rect(0.27, 0.77, 0.73, 0.86)
            }
            1 -> {
                rect(0.27, 0.20, 0.37, 0.36); rect(0.45, 0.20, 0.55, 0.36); rect(0.63, 0.20, 0.73, 0.36)
                rect(0.27, 0.28, 0.73, 0.38)
                rect(0.33, 0.36, 0.67, 0.76)
                rect(0.24, 0.74, 0.76, 0.86)
            }
            2 -> {
                poly(0.30, 0.86, 0.74, 0.86, 0.72, 0.54, 0.62, 0.30, 0.48, 0.17, 0.44, 0.26, 0.30, 0.34, 0.23, 0.50, 0.31, 0.57, 0.45, 0.48, 0.40, 0.66)
            }
            3 -> {
                oval(0.5, 0.17, 0.045, 0.045)
                oval(0.5, 0.42, 0.14, 0.21)
                poly(0.36, 0.64, 0.64, 0.64, 0.74, 0.86, 0.26, 0.86)
            }
            4 -> {
                poly(0.20, 0.30, 0.33, 0.58, 0.39, 0.22, 0.50, 0.56, 0.61, 0.22, 0.67, 0.58, 0.80, 0.30, 0.71, 0.86, 0.29, 0.86)
                oval(0.20, 0.27, 0.045, 0.045); oval(0.39, 0.19, 0.045, 0.045)
                oval(0.61, 0.19, 0.045, 0.045); oval(0.80, 0.27, 0.045, 0.045)
            }
            else -> {
                rect(0.47, 0.11, 0.53, 0.32); rect(0.40, 0.17, 0.60, 0.23)
                oval(0.5, 0.48, 0.23, 0.17)
                poly(0.32, 0.56, 0.68, 0.56, 0.73, 0.86, 0.27, 0.86)
            }
        }
        g.color = if (light) LIGHT_FILL else DARK_FILL
        g.fill(shape)
        g.color = if (light) LIGHT_LINE else DARK_LINE
        g.stroke = BasicStroke(maxOf(1.5f, (cell * 0.035).toFloat()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        g.draw(shape)
        // a thin detail line, as most real sets have (light on dark pieces, dark on light ones)
        g.color = if (light) LIGHT_LINE else DARK_DETAIL
        g.stroke = BasicStroke(maxOf(1f, (cell * 0.018).toFloat()))
        g.draw(Line2D.Double(x + 0.40 * cell, y + 0.72 * cell, x + 0.60 * cell, y + 0.72 * cell))
    }
}

/** Board-free UI: the stuff around a board in an app, and whole screens that contain no board. */
object Clutter {

    private fun shade(c: Color, d: Int) =
        Color((c.red + d).coerceIn(0, 255), (c.green + d).coerceIn(0, 255), (c.blue + d).coerceIn(0, 255))

    private fun ink(page: Color): Color = if (page.red + page.green + page.blue > 380) Color(40, 40, 46) else Color(222, 222, 216)

    /** One line of word-like rounded bars standing in for text. */
    fun textLine(g: Graphics2D, x: Int, y: Int, maxWidth: Int, lineHeight: Int, color: Color, random: Random) {
        var cx = x
        g.color = color
        while (true) {
            val w = lineHeight * (1 + random.nextInt(5))
            if (cx + w > x + maxWidth) break
            g.fill(RoundRectangle2D.Double(cx.toDouble(), y.toDouble(), w.toDouble(), lineHeight.toDouble(), lineHeight * 0.4, lineHeight * 0.4))
            // ascender/descender ticks so that the bars are not perfectly regular
            if (random.nextInt(3) == 0) g.fillRect(cx + w / 3, y - lineHeight / 3, maxOf(2, lineHeight / 4), lineHeight / 3)
            cx += w + lineHeight / 2 + random.nextInt(lineHeight)
        }
    }

    fun paragraph(g: Graphics2D, x: Int, y: Int, width: Int, lines: Int, lineHeight: Int, color: Color, random: Random): Int {
        var cy = y
        repeat(lines) {
            textLine(g, x, cy, if (it == lines - 1) width * 2 / 3 else width, lineHeight, color, random)
            cy += lineHeight * 2
        }
        return cy
    }

    /** Status bar, app bar, player strips, move list, buttons, cards and a bottom navigation bar. */
    fun appScreen(g: Graphics2D, w: Int, h: Int, page: Color, random: Random) {
        val u = maxOf(8, minOf(w, h) / 45)
        val text = ink(page)
        // status bar
        g.color = shade(page, -14)
        g.fillRect(0, 0, w, 3 * u)
        g.color = text
        for (k in 0..3) g.fillRect(2 * u + k * u, u, u * 2 / 3, u)
        for (k in 0..2) g.fill(Ellipse2D.Double((w - 3 * u - k * 2 * u).toDouble(), (u * 0.8), u * 1.3, u * 1.3))
        // app bar
        g.color = shade(page, 12)
        g.fillRect(0, 3 * u, w, 5 * u)
        g.color = text
        for (k in 0..2) g.fillRect(2 * u, 4 * u + k * u + u / 3, 3 * u, u / 3)
        textLine(g, 7 * u, 5 * u, w / 3, u, text, random)
        g.color = Color(96, 160, 60)
        g.fill(RoundRectangle2D.Double((w - 14 * u).toDouble(), 3.8 * u, 11.0 * u, 3.4 * u, u.toDouble(), u.toDouble()))

        // widgets in a column down the whole screen (the board is painted over them later)
        var y = 9 * u
        while (y < h - 9 * u) {
            when (random.nextInt(6)) {
                0 -> { // player strip
                    g.color = shade(page, 40)
                    g.fillRect(2 * u, y, 4 * u, 4 * u)
                    g.color = shade(page, 90)
                    g.fill(Ellipse2D.Double(3.0 * u, y + 0.6 * u, 2.0 * u, 2.0 * u))
                    g.fillRect(2 * u + u / 2, y + 3 * u - u / 3, 3 * u, u)
                    textLine(g, 7 * u, y + u / 2, w / 2, u, text, random)
                    g.color = shade(page, 60)
                    for (k in 0..5) g.fill(Ellipse2D.Double(7.0 * u + k * 1.4 * u, y + 2.4 * u, 1.0 * u, 1.3 * u))
                    g.color = Color(250, 250, 250)
                    g.fill(RoundRectangle2D.Double(w - 12.0 * u, y + 0.3 * u, 10.0 * u, 3.4 * u, 0.6 * u, 0.6 * u))
                    g.color = Color(30, 30, 30)
                    for (k in 0..3) g.fillRect(w - 10 * u + k * 2 * u, y + u, u, 2 * u)
                    y += 5 * u
                }
                1 -> y = paragraph(g, 2 * u, y, w - 4 * u, 2 + random.nextInt(4), u, text, random) + u
                2 -> { // row of buttons
                    val n = 3 + random.nextInt(3)
                    val bw = (w - 2 * u) / n
                    for (k in 0 until n) {
                        g.color = shade(page, 22)
                        g.fill(RoundRectangle2D.Double(u + k * bw + u / 2.0, y.toDouble(), bw - u.toDouble(), 5.0 * u, u.toDouble(), u.toDouble()))
                        g.color = text
                        val cx = u + k * bw + bw / 2
                        val tri = Path2D.Double()
                        tri.moveTo(cx - u.toDouble(), y + 1.5 * u); tri.lineTo(cx + u.toDouble(), y + 2.5 * u); tri.lineTo(cx - u.toDouble(), y + 3.5 * u)
                        tri.closePath()
                        g.fill(tri)
                    }
                    y += 6 * u
                }
                3 -> { // rounded card with text
                    val ch = (6 + random.nextInt(8)) * u
                    g.color = shade(page, if (page.red > 127) -18 else 18)
                    g.fill(RoundRectangle2D.Double(u.toDouble(), y.toDouble(), w - 2.0 * u, ch.toDouble(), 2.0 * u, 2.0 * u))
                    paragraph(g, 3 * u, y + 2 * u, w - 6 * u, (ch / u - 3) / 2, u, text, random)
                    y += ch + u
                }
                4 -> { // move list chips
                    var x = 2 * u
                    while (x < w - 8 * u) {
                        val cw = (3 + random.nextInt(4)) * u
                        g.color = if (random.nextInt(5) == 0) Color(70, 130, 200) else shade(page, 28)
                        g.fill(RoundRectangle2D.Double(x.toDouble(), y.toDouble(), cw.toDouble(), 2.4 * u, 0.5 * u, 0.5 * u))
                        g.color = text
                        g.fillRect(x + u / 2, y + u * 3 / 4, cw - u, u * 3 / 4)
                        x += cw + u
                    }
                    y += 4 * u
                }
                else -> { // thin separator and a labelled value row
                    g.color = shade(page, 30)
                    g.fillRect(0, y, w, maxOf(1, u / 6))
                    textLine(g, 2 * u, y + u, w / 3, u, text, random)
                    textLine(g, w * 2 / 3, y + u, w / 4, u, shade(text, -60), random)
                    y += 4 * u
                }
            }
        }
        // bottom navigation
        g.color = shade(page, -10)
        g.fillRect(0, h - 8 * u, w, 8 * u)
        for (k in 0..4) {
            val cx = w * (2 * k + 1) / 10
            g.color = if (k == 1) Color(129, 182, 76) else shade(page, 110)
            if (k % 2 == 0) g.fill(Ellipse2D.Double(cx - 1.5 * u, h - 6.5 * u, 3.0 * u, 3.0 * u))
            else g.fill(RoundRectangle2D.Double(cx - 1.5 * u, h - 6.5 * u, 3.0 * u, 3.0 * u, 0.8 * u, 0.8 * u))
            g.fillRect(cx - 2 * u, h - 2 * u - u / 2, 4 * u, u * 2 / 3)
        }
    }

    private fun canvas(w: Int, h: Int, page: Color): Pair<BufferedImage, Graphics2D> {
        val image = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.color = page
        g.fillRect(0, 0, w, h)
        return image to g
    }

    fun appWithoutBoard(w: Int, h: Int, page: Color, seed: Long): Frame {
        val (image, g) = canvas(w, h, page)
        appScreen(g, w, h, page, Random(seed))
        g.dispose()
        return image.toFrame()
    }

    fun textPage(w: Int, h: Int, seed: Long): Frame {
        val page = Color(252, 252, 250)
        val (image, g) = canvas(w, h, page)
        val random = Random(seed)
        val u = maxOf(8, minOf(w, h) / 50)
        var y = 4 * u
        while (y < h - 6 * u) {
            textLine(g, 2 * u, y, w / 2, u * 3 / 2, Color(20, 20, 20), random)
            y = paragraph(g, 2 * u, y + 4 * u, w - 4 * u, 4 + random.nextInt(7), u, Color(60, 60, 64), random) + 2 * u
        }
        g.dispose()
        return image.toFrame()
    }

    fun gradient(w: Int, h: Int): Frame {
        val (image, g) = canvas(w, h, Color.BLACK)
        g.paint = GradientPaint(0f, 0f, Color(20, 40, 120), w.toFloat(), h.toFloat(), Color(240, 180, 90))
        g.fillRect(0, 0, w, h)
        g.paint = GradientPaint(0f, h / 2f, Color(255, 255, 255, 0), 0f, h.toFloat(), Color(255, 255, 255, 160))
        g.fillRect(0, 0, w, h)
        g.dispose()
        return image.toFrame()
    }

    /** Soft overlapping blobs plus sensor-like noise: has contrast everywhere but no structure. */
    fun photoNoise(w: Int, h: Int, seed: Long): Frame {
        val (image, g) = canvas(w, h, Color(90, 110, 90))
        val random = Random(seed)
        repeat(260) {
            val rw = w / 12 + random.nextInt(w / 3)
            val rh = h / 20 + random.nextInt(h / 5)
            g.color = Color(random.nextInt(256), random.nextInt(256), random.nextInt(256), 70 + random.nextInt(120))
            g.fill(Ellipse2D.Double(random.nextInt(w) - rw / 2.0, random.nextInt(h) - rh / 2.0, rw.toDouble(), rh.toDouble()))
        }
        g.dispose()
        Scene.addNoise(image, 0, 0, w, h, 14, random)
        return image.toFrame()
    }

    /** Launcher-like grid of rounded icons with labels. */
    fun iconGrid(w: Int, h: Int, columns: Int, seed: Long): Frame {
        val (image, g) = canvas(w, h, Color(18, 24, 48))
        g.paint = GradientPaint(0f, 0f, Color(40, 20, 90), w.toFloat(), h.toFloat(), Color(10, 90, 120))
        g.fillRect(0, 0, w, h)
        val random = Random(seed)
        val pitch = w / columns
        val icon = pitch * 62 / 100
        var y = pitch / 2
        while (y + pitch < h) {
            for (c in 0 until columns) {
                val x = c * pitch + (pitch - icon) / 2
                g.color = Color(60 + random.nextInt(196), 60 + random.nextInt(196), 60 + random.nextInt(196))
                g.fill(RoundRectangle2D.Double(x.toDouble(), y.toDouble(), icon.toDouble(), icon.toDouble(), icon * 0.3, icon * 0.3))
                g.color = Color(255, 255, 255, 200)
                g.fill(Ellipse2D.Double(x + icon * 0.3, y + icon * 0.3, icon * 0.4, icon * 0.4))
                g.color = Color(235, 235, 235)
                g.fillRect(x + icon / 6, y + icon + icon / 8, icon * 2 / 3, maxOf(3, icon / 10))
            }
            y += pitch * 125 / 100
        }
        g.dispose()
        return image.toFrame()
    }

    /** A chat-like screen with an on-screen keyboard: a regular grid of identical light keys. */
    fun keyboard(w: Int, h: Int, seed: Long): Frame {
        val page = Color(245, 245, 247)
        val (image, g) = canvas(w, h, page)
        val random = Random(seed)
        val u = maxOf(8, minOf(w, h) / 45)
        paragraph(g, 2 * u, 6 * u, w - 4 * u, 8, u, Color(50, 50, 55), random)
        val kbTop = h * 58 / 100
        g.color = Color(206, 210, 217)
        g.fillRect(0, kbTop, w, h - kbTop)
        val rows = 4
        val keyH = (h - kbTop - 2 * u) / (rows + 1)
        for (row in 0 until rows) {
            val keys = intArrayOf(10, 9, 9, 5)[row]
            val keyW = w / 10
            val x0 = (w - keys * keyW) / 2
            for (k in 0 until keys) {
                val wide = row == 3 && k == 2
                val kw = if (wide) keyW * 5 else keyW
                val x = if (row == 3) (if (k < 2) k * keyW else if (k == 2) 2 * keyW else (k + 4) * keyW) + (w - 10 * keyW) / 2 else x0 + k * keyW
                g.color = if (row == 3 && k != 2 || (row == 2 && (k == 0 || k == 8))) Color(172, 177, 187) else Color(252, 252, 254)
                g.fill(RoundRectangle2D.Double(x + keyW * 0.07, kbTop + u + row * keyH + keyH * 0.08, kw - keyW * 0.14, keyH * 0.84, u * 0.7, u * 0.7))
                if (!wide) {
                    g.color = Color(30, 30, 30)
                    g.stroke = BasicStroke(maxOf(2f, u / 5f))
                    val cx = x + keyW / 2.0
                    val cy = kbTop + u + row * keyH + keyH / 2.0
                    if ((k + row) % 2 == 0) g.draw(Ellipse2D.Double(cx - u * 0.6, cy - u * 0.8, u * 1.2, u * 1.6))
                    else { g.draw(Line2D.Double(cx - u * 0.6, cy - u * 0.8, cx + u * 0.6, cy + u * 0.8)); g.draw(Line2D.Double(cx - u * 0.6, cy + u * 0.8, cx + u * 0.6, cy - u * 0.8)) }
                }
            }
        }
        g.dispose()
        return image.toFrame()
    }

    /** Alternating bands (one direction only): strong regular edges, but no checker crossings. */
    fun stripes(w: Int, h: Int, period: Int, horizontal: Boolean): Frame {
        val (image, g) = canvas(w, h, Color(250, 250, 250))
        g.color = Color(70, 90, 160)
        var p = 0
        while (p < (if (horizontal) h else w)) {
            if (horizontal) g.fillRect(0, p, w, period / 2) else g.fillRect(p, 0, period / 2, h)
            p += period
        }
        g.dispose()
        return image.toFrame()
    }

    /** A `columns x rows` two-tone checker (not necessarily 8x8) with its top-left corner at (x, y). */
    fun checker(w: Int, h: Int, x: Int, y: Int, cell: Int, columns: Int, rows: Int, theme: Theme, page: Color, seed: Long): Frame {
        val (image, g) = canvas(w, h, page)
        appScreen(g, w, h, page, Random(seed))
        for (r in 0 until rows) for (c in 0 until columns) {
            g.color = if ((r + c) % 2 == 0) theme.light else theme.dark
            g.fillRect(x + c * cell, y + r * cell, cell, cell)
        }
        g.dispose()
        return image.toFrame()
    }

    /** An 8x8 grid of identical coloured tiles separated by gaps (a tile game, a colour picker). */
    fun tileGrid(w: Int, h: Int, seed: Long): Frame {
        val page = Color(250, 248, 239)
        val (image, g) = canvas(w, h, page)
        val random = Random(seed)
        val size = w * 9 / 10
        val x0 = (w - size) / 2
        val y0 = (h - size) / 2
        g.color = Color(187, 173, 160)
        g.fill(RoundRectangle2D.Double(x0.toDouble(), y0.toDouble(), size.toDouble(), size.toDouble(), 20.0, 20.0))
        val pitch = size / 8
        val palette = arrayOf(Color(238, 228, 218), Color(237, 224, 200), Color(242, 177, 121), Color(245, 149, 99), Color(246, 124, 95), Color(237, 207, 114))
        for (r in 0..7) for (c in 0..7) {
            g.color = palette[random.nextInt(palette.size)]
            g.fill(RoundRectangle2D.Double(x0 + c * pitch + pitch * 0.07, y0 + r * pitch + pitch * 0.07, pitch * 0.86, pitch * 0.86, 10.0, 10.0))
        }
        g.dispose()
        return image.toFrame()
    }
}
