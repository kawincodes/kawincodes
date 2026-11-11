package com.krstech.chesshelp.capture

import com.krstech.chesshelp.vision.Frame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Holds the newest captured frame and nothing else.
 *
 * The capture thread publishes every frame as raw little-endian RGBA words; the R/B swap that makes it a
 * [Frame] happens in place, and only when somebody asks for the frame. The screen can produce 60+ frames a
 * second while the app looks at a few, so an unread frame costs one bulk copy and no allocation: its buffer
 * is reused for the next one. A buffer handed out inside a [Frame] is never reused.
 */
internal class FrameMailbox {

    private val lock = Any()

    // Guarded by [lock].
    private var spare: IntArray? = null
    private var pending: IntArray? = null
    private var pendingWidth = 0
    private var pendingHeight = 0
    private var frame: Frame? = null
    private var closed = false

    /** Bumped for every published frame (and on close), so waiters can tell that something new arrived. */
    private val arrivals = MutableStateFlow(0L)

    /** A buffer of exactly [pixelCount] ints for the next frame; give it back with [publish] or [recycle]. */
    fun obtainBuffer(pixelCount: Int): IntArray {
        synchronized(lock) {
            val reusable = spare
            spare = null
            if (reusable != null && reusable.size == pixelCount) return reusable
        }
        return IntArray(pixelCount)
    }

    /** Returns a buffer from [obtainBuffer] that ended up not holding a frame. */
    fun recycle(buffer: IntArray) {
        synchronized(lock) { if (!closed) spare = buffer }
    }

    /** Makes [words] (little-endian RGBA, `width * height` of them) the newest frame. */
    fun publish(words: IntArray, width: Int, height: Int) {
        require(width > 0 && height > 0 && words.size == width * height) {
            "Buffer of ${words.size} pixels does not match ${width}x$height"
        }
        synchronized(lock) {
            if (closed) return
            pending?.let { spare = it }
            pending = words
            pendingWidth = width
            pendingHeight = height
            frame = null
        }
        arrivals.update { it + 1 }
    }

    /** The newest frame if it needs no conversion work, else null (also when there is no frame at all). */
    fun convertedOrNull(): Frame? = synchronized(lock) { if (pending == null) frame else null }

    /** The newest frame, converting it first if necessary (a few milliseconds for a full screen). */
    fun latest(): Frame? {
        synchronized(lock) {
            val words = pending ?: return frame
            RgbaPixels.wordsToArgb(words, words.size)
            pending = null
            return Frame(pendingWidth, pendingHeight, words).also { frame = it }
        }
    }

    /** Suspends until a frame is published after this call; false if [timeoutMs] passed without one. */
    suspend fun awaitArrival(timeoutMs: Long): Boolean {
        val seen = arrivals.value
        return withTimeoutOrNull(timeoutMs) { arrivals.first { it != seen } } != null
    }

    /** Forgets every frame, e.g. because the capture size changed and they no longer describe the screen. */
    fun clear() {
        synchronized(lock) { dropAll() }
    }

    /** Drops the buffer kept for reuse (after a failed allocation). */
    fun trim() {
        synchronized(lock) { spare = null }
    }

    /** Forgets everything, refuses further frames and releases anyone waiting in [awaitArrival]. */
    fun close() {
        synchronized(lock) {
            closed = true
            dropAll()
        }
        arrivals.update { it + 1 }
    }

    private fun dropAll() {
        spare = null
        pending = null
        frame = null
    }
}
