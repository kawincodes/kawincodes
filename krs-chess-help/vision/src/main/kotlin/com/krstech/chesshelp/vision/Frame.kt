package com.krstech.chesshelp.vision

/** A raw screenshot: [pixels] are 0xAARRGGBB, row-major, `pixels.size == width * height`. */
class Frame(val width: Int, val height: Int, val pixels: IntArray) {
    init {
        require(width > 0 && height > 0) { "Frame dimensions must be positive: ${width}x$height" }
        require(pixels.size == width * height) {
            "Frame pixel count ${pixels.size} does not match ${width}x$height"
        }
    }

    fun pixel(x: Int, y: Int): Int = pixels[y * width + x]
}

/** Integer luma (0..255) of an 0xAARRGGBB pixel, BT.601 weights. */
internal fun lumaOf(p: Int): Int =
    (((p ushr 16) and 0xFF) * 77 + ((p ushr 8) and 0xFF) * 150 + (p and 0xFF) * 29) shr 8
