package com.krstech.chesshelp.capture

internal data class PixelSize(val width: Int, val height: Int) {
    val isValid: Boolean get() = width > 0 && height > 0

    override fun toString(): String = "${width}x$height"
}

/**
 * Decides how large the capture has to be from the two signals the platform gives: the real display size
 * (every API level) and the size of the captured content (`MediaProjection.Callback.onCapturedContentResize`,
 * API 34+, which is the only correct source when the user shares a single app instead of the whole screen).
 */
internal class CaptureSizeTracker(initialDisplay: PixelSize) {

    var display: PixelSize = initialDisplay
        private set

    private var content: PixelSize? = null

    /** The size the virtual display and its ImageReader should have. */
    val target: PixelSize get() = content ?: display

    fun onDisplaySize(size: PixelSize) {
        if (!size.isValid || size == display) return
        // While the content fills the display it follows the display, so a rotation is handled as soon as
        // the display reports it; the content callback that follows then merely confirms the size.
        if (content == display) content = size
        display = size
    }

    fun onContentSize(size: PixelSize) {
        if (size.isValid) content = size
    }
}
