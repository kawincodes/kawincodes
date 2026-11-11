package com.krstech.chesshelp.capture

import android.annotation.SuppressLint
import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import com.krstech.chesshelp.vision.Frame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Turns a granted [MediaProjection] into [Frame]s of the whole screen.
 *
 * The capturer owns the projection: [stop] (or the system ending the capture) stops it, and a stopped
 * capturer cannot be started again. [start] and [stop] may be called from any thread.
 *
 * [onStopped] and [onSizeChanged] are invoked on the main thread. [onStopped] is invoked at most once, and
 * only when the capture ended without [stop] being called: the user ended it from the system UI, or the
 * system revoked it (e.g. the device was locked on Android 15+).
 */
class ScreenCapturer internal constructor(
    context: Context,
    private val projection: MediaProjection,
    private val onStopped: () -> Unit,
    private val platform: CapturePlatform,
) {

    constructor(context: Context, projection: MediaProjection, onStopped: () -> Unit) :
        this(context, projection, onStopped, CapturePlatform())

    private enum class State { NEW, RUNNING, STOPPED }

    // Never the Service/Activity itself: the capture thread and system listeners would keep it alive.
    private val appContext: Context = context.applicationContext ?: context
    private val mainHandler: Handler = platform.mainHandler()
    private val mailbox = FrameMailbox()

    /**
     * Guards the fields below and, just as importantly, the lifetime of the ImageReader: a frame is copied
     * out of an Image while holding it, so a [stop] on another thread cannot free the buffer mid-copy.
     * No app callback is ever invoked while it is held.
     */
    private val lock = Any()

    private var state = State.NEW
    private var captureThread: HandlerThread? = null
    private var captureHandler: Handler? = null
    private var displayManager: DisplayManager? = null
    private var reader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var sizes: CaptureSizeTracker? = null
    private var captureSize = PixelSize(0, 0)
    private var densityDpi = DisplayMetrics.DENSITY_DEFAULT
    private var droppedFrames = 0

    @Volatile
    private var matchesScreen = false

    /** Rotation or a captured-content resize: frames from now on have a different size (or screen mapping). */
    @Volatile
    var onSizeChanged: (() -> Unit)? = null

    /**
     * True when frames have exactly the real display size, so that frame pixel coordinates are screen pixel
     * coordinates. False before [start], after the capture stopped and while a rotation is being applied.
     */
    val framesMatchScreen: Boolean get() = matchesScreen

    private val notifyStopped = Runnable { onStopped() }
    private val notifySizeChanged = Runnable { onSizeChanged?.invoke() }
    private val checkDisplay = Runnable { refreshGeometry(null) }

    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() = onProjectionStopped()

        // API 34+. The size of what is actually being captured: the display, or one app's window.
        override fun onCapturedContentResize(width: Int, height: Int) = refreshGeometry(PixelSize(width, height))
    }

    private val imageListener = ImageReader.OnImageAvailableListener { source -> onImageAvailable(source) }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit

        override fun onDisplayChanged(displayId: Int) {
            if (displayId != Display.DEFAULT_DISPLAY) return
            refreshGeometry(null)
            // From API 31 the display size is read from the app's Configuration, which may be updated only
            // after this event. The configuration callback below covers that; this is the safety net.
            scheduleDisplayCheck(DISPLAY_RECHECK_DELAY_MS)
        }
    }

    private val configurationCallbacks = object : ComponentCallbacks {
        override fun onConfigurationChanged(newConfig: Configuration) = scheduleDisplayCheck(0)

        @Deprecated("Deprecated in Java")
        override fun onLowMemory() = Unit
    }

    /**
     * Registers the projection callback, then creates the virtual display (exactly once per projection).
     * Calling it again while running does nothing.
     *
     * @throws IllegalStateException when the capturer was already stopped, or no virtual display could be created.
     * @throws SecurityException when the system rejects the projection (expired or reused consent token, or
     *   no foreground service of type mediaProjection is running). Everything is released before rethrowing.
     */
    fun start() {
        synchronized(lock) {
            when (state) {
                State.RUNNING -> return
                State.STOPPED -> throw IllegalStateException("This ScreenCapturer was stopped; a MediaProjection cannot be restarted")
                State.NEW -> Unit
            }
            state = State.RUNNING
            try {
                startLocked()
            } catch (e: RuntimeException) {
                releaseLocked()
                throw e
            }
        }
    }

    /**
     * Releases the virtual display, the ImageReader and the capture thread, unregisters every callback and
     * stops the projection. Idempotent; safe from any thread, including from [onStopped] / [onSizeChanged].
     * No callback is delivered once it has returned.
     */
    fun stop() {
        synchronized(lock) {
            if (state != State.STOPPED) releaseLocked()
        }
        mainHandler.removeCallbacks(notifyStopped)
        mainHandler.removeCallbacks(notifySizeChanged)
    }

    /** The newest frame, or null when none has been captured (yet, since the last size change, or after stop). */
    suspend fun latestFrame(): Frame? =
        mailbox.convertedOrNull() ?: withContext(Dispatchers.Default) { mailbox.latest() }

    /**
     * Suspends until a frame arrives that was captured after this call, for at most [timeoutMs]. A static
     * screen produces no frames, so on timeout this returns the same as [latestFrame].
     */
    suspend fun freshFrame(timeoutMs: Long): Frame? {
        mailbox.awaitArrival(timeoutMs)
        return latestFrame()
    }

    private fun startLocked() {
        val thread = platform.startCaptureThread()
        captureThread = thread
        val handler = platform.handlerFor(thread)
        captureHandler = handler

        // Must precede createVirtualDisplay: mandatory from API 34, and the only way to learn that the
        // user or the system ended the capture.
        projection.registerCallback(projectionCallback, handler)

        val screen = platform.screenSpec(appContext)
        check(screen.size.isValid) { "The display reports an unusable size: ${screen.size}" }
        densityDpi = screen.densityDpi
        val tracker = CaptureSizeTracker(screen.size)
        sizes = tracker
        val size = tracker.target

        val firstReader = openReader(size, handler)
        reader = firstReader
        virtualDisplay = projection.createVirtualDisplay(
            DISPLAY_NAME, size.width, size.height, densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, firstReader.surface, null, null,
        ) ?: throw IllegalStateException("The system did not create a virtual display for the screen capture")
        captureSize = size
        matchesScreen = true

        displayManager = appContext.getSystemService(DisplayManager::class.java)
        displayManager?.registerDisplayListener(displayListener, handler)
        appContext.registerComponentCallbacks(configurationCallbacks)
    }

    private fun openReader(size: PixelSize, handler: Handler): ImageReader {
        val opened = platform.newImageReader(size)
        opened.setOnImageAvailableListener(imageListener, handler)
        return opened
    }

    /** Capture thread. */
    private fun onImageAvailable(source: ImageReader) {
        synchronized(lock) {
            // A callback queued before a resize or a stop can still arrive for a reader that is closed by now.
            if (state != State.RUNNING || source !== reader) return
            try {
                copyLatestImage(source)
            } catch (e: RuntimeException) {
                noteDroppedFrame(e)
            } catch (e: OutOfMemoryError) {
                mailbox.trim()
                noteDroppedFrame(e)
            }
        }
    }

    private fun copyLatestImage(source: ImageReader) {
        // Latest, not next: older queued frames are discarded, so the producer is never held up by us.
        val image = source.acquireLatestImage() ?: return
        try {
            val width = image.width
            val height = image.height
            val plane = image.planes[0]
            val words = mailbox.obtainBuffer(width * height)
            try {
                RgbaPixels.readWords(plane.buffer, width, height, plane.rowStride, plane.pixelStride, words)
            } catch (e: RuntimeException) {
                mailbox.recycle(words)
                throw e
            }
            mailbox.publish(words, width, height)
        } finally {
            image.close()
        }
    }

    private fun noteDroppedFrame(cause: Throwable) {
        if (droppedFrames++ % DROPPED_FRAME_LOG_INTERVAL == 0) {
            Log.w(TAG, "Dropped a captured frame (${droppedFrames} so far)", cause)
        }
    }

    /** Capture thread: the user ended the capture from the system UI, or the system revoked it. */
    private fun onProjectionStopped() {
        synchronized(lock) {
            if (state != State.RUNNING) return
            releaseLocked()
            // Posted while holding the lock so that a concurrent stop() reliably cancels it.
            mainHandler.post(notifyStopped)
        }
    }

    private fun scheduleDisplayCheck(delayMs: Long) {
        synchronized(lock) {
            val handler = captureHandler
            if (state != State.RUNNING || handler == null) return
            handler.removeCallbacks(checkDisplay)
            handler.postDelayed(checkDisplay, delayMs)
        }
    }

    /**
     * Capture thread. Re-reads the display size, takes in a new captured-content size if there is one, and
     * resizes the capture when the two call for it.
     */
    private fun refreshGeometry(content: PixelSize?) {
        val screen = try {
            platform.screenSpec(appContext)
        } catch (e: RuntimeException) {
            null
        }
        synchronized(lock) {
            val tracker = sizes
            if (state != State.RUNNING || tracker == null) return
            val displayBefore = tracker.display
            val captureBefore = captureSize
            if (screen != null) {
                tracker.onDisplaySize(screen.size)
                densityDpi = screen.densityDpi
            }
            if (content != null) tracker.onContentSize(content)
            resizeLocked(tracker.target)
            matchesScreen = captureSize == tracker.display
            if (captureSize != captureBefore || tracker.display != displayBefore) {
                mainHandler.removeCallbacks(notifySizeChanged)
                mainHandler.post(notifySizeChanged)
            }
        }
    }

    /**
     * A projection allows a single createVirtualDisplay call (a second one throws from API 34), so a new
     * size is applied to the existing display: resize it, then give it the surface of a new reader.
     */
    private fun resizeLocked(target: PixelSize) {
        if (target == captureSize) return
        val display = virtualDisplay ?: return
        val handler = captureHandler ?: return
        val previous = reader
        val replacement = try {
            openReader(target, handler)
        } catch (e: RuntimeException) {
            Log.w(TAG, "Could not create an ImageReader of $target; capture stays at $captureSize", e)
            return
        }
        try {
            display.resize(target.width, target.height, densityDpi)
            display.surface = replacement.surface
        } catch (e: RuntimeException) {
            Log.w(TAG, "Could not resize the capture to $target; capture stays at $captureSize", e)
            closeQuietly(replacement)
            return
        }
        reader = replacement
        captureSize = target
        previous?.let(::closeQuietly)
        // Frames of the old size no longer describe the screen.
        mailbox.clear()
    }

    private fun releaseLocked() {
        state = State.STOPPED
        matchesScreen = false
        captureHandler?.removeCallbacksAndMessages(null)
        quietly { displayManager?.unregisterDisplayListener(displayListener) }
        quietly { appContext.unregisterComponentCallbacks(configurationCallbacks) }
        virtualDisplay?.let { display -> quietly { display.release() } }
        virtualDisplay = null
        reader?.let(::closeQuietly)
        reader = null
        quietly { projection.unregisterCallback(projectionCallback) }
        quietly { projection.stop() }
        // Not joined: this may be running on the capture thread itself.
        captureThread?.quitSafely()
        captureThread = null
        captureHandler = null
        displayManager = null
        sizes = null
        mailbox.close()
    }

    private fun closeQuietly(target: ImageReader) = quietly { target.close() }

    private inline fun quietly(action: () -> Unit) {
        try {
            action()
        } catch (e: RuntimeException) {
            Log.w(TAG, "Ignored a failure while releasing the screen capture", e)
        }
    }

    private companion object {
        const val TAG = "ScreenCapturer"
        const val DISPLAY_NAME = "krs-chess-help-capture"
        const val DISPLAY_RECHECK_DELAY_MS = 500L
        const val DROPPED_FRAME_LOG_INTERVAL = 120
    }
}

/** The platform objects [ScreenCapturer] creates, behind a seam so its lifecycle can be exercised on the JVM. */
internal open class CapturePlatform {

    open fun startCaptureThread(): HandlerThread = HandlerThread("krs-screen-capture").apply { start() }

    open fun handlerFor(thread: HandlerThread): Handler = Handler(thread.looper)

    open fun mainHandler(): Handler = Handler(Looper.getMainLooper())

    /**
     * Two images: `acquireLatestImage` needs one of headroom to drop older frames, and no Image is ever
     * held across callbacks.
     */
    // ImageReader takes ImageFormat constants, but RGBA_8888 is the documented format for screen capture.
    @SuppressLint("WrongConstant")
    open fun newImageReader(size: PixelSize): ImageReader =
        ImageReader.newInstance(size.width, size.height, PixelFormat.RGBA_8888, MAX_IMAGES)

    open fun screenSpec(context: Context): ScreenSpec = ScreenMetrics.read(context)

    private companion object {
        const val MAX_IMAGES = 2
    }
}
