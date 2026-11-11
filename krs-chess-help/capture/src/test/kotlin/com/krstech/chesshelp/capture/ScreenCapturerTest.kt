package com.krstech.chesshelp.capture

import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.HandlerThread
import android.view.Display
import android.view.Surface
import kotlinx.coroutines.runBlocking
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.isNull
import org.mockito.ArgumentMatchers.same
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Runs the capture lifecycle on the JVM against mocked platform objects. It proves the call sequence and
 * the state handling, not the behaviour of the real MediaProjection stack.
 */
class ScreenCapturerTest {

    /** A Handler whose posted work is run by the test, standing in for a looper thread. */
    private class FakeHandler {
        private val queue = ArrayList<Pair<Runnable, Long>>()
        val handler: Handler = mock(Handler::class.java)

        init {
            `when`(handler.post(any())).thenAnswer { add(it.getArgument(0), 0L) }
            `when`(handler.postDelayed(any(), anyLong())).thenAnswer { add(it.getArgument(0), it.getArgument(1)) }
            doAnswer { inv ->
                synchronized(queue) { queue.removeAll { it.first === inv.getArgument<Runnable>(0) } }
                null
            }.`when`(handler).removeCallbacks(any())
            doAnswer {
                synchronized(queue) { queue.clear() }
                null
            }.`when`(handler).removeCallbacksAndMessages(any())
        }

        private fun add(runnable: Runnable, delay: Long): Boolean {
            synchronized(queue) { queue += runnable to delay }
            return true
        }

        val delays: List<Long> get() = synchronized(queue) { queue.map { it.second } }

        fun runAll(): Int {
            var ran = 0
            while (true) {
                val next = synchronized(queue) { if (queue.isEmpty()) null else queue.removeAt(0) } ?: return ran
                next.first.run()
                ran++
            }
        }
    }

    private class FakeReader(val size: PixelSize) {
        val surface: Surface = mock(Surface::class.java)
        val reader: ImageReader = mock(ImageReader::class.java)

        init {
            `when`(reader.surface).thenReturn(surface)
            `when`(reader.width).thenReturn(size.width)
            `when`(reader.height).thenReturn(size.height)
        }

        fun listener(): ImageReader.OnImageAvailableListener {
            val captor = ArgumentCaptor.forClass(ImageReader.OnImageAvailableListener::class.java)
            verify(reader).setOnImageAvailableListener(captor.capture(), any())
            return captor.value
        }

        /** Queues an image of this reader's size, every pixel R=0x11 G=0x22 B=0x33 A=0xFF, rows padded. */
        fun offer(buffer: ByteBuffer = solidRgba(size, padPixels = 3), rowStride: Int = (size.width + 3) * 4): Image {
            val plane = mock(Image.Plane::class.java)
            `when`(plane.buffer).thenReturn(buffer)
            `when`(plane.rowStride).thenReturn(rowStride)
            `when`(plane.pixelStride).thenReturn(4)
            val image = mock(Image::class.java)
            `when`(image.width).thenReturn(size.width)
            `when`(image.height).thenReturn(size.height)
            `when`(image.planes).thenReturn(arrayOf(plane))
            // doReturn: also replaces an earlier stubbing that throws.
            doReturn(image, null).`when`(reader).acquireLatestImage()
            return image
        }

        fun deliver(buffer: ByteBuffer = solidRgba(size, padPixels = 3), rowStride: Int = (size.width + 3) * 4): Image {
            val image = offer(buffer, rowStride)
            listener().onImageAvailable(reader)
            return image
        }
    }

    private inner class Fixture(var displaySize: PixelSize = PORTRAIT) {
        val context: Context = mock(Context::class.java)
        val appContext: Context = mock(Context::class.java)
        val displayManager: DisplayManager = mock(DisplayManager::class.java)
        val projection: MediaProjection = mock(MediaProjection::class.java)
        val virtualDisplay: VirtualDisplay = mock(VirtualDisplay::class.java)
        val thread: HandlerThread = mock(HandlerThread::class.java)
        val capture = FakeHandler()
        val main = FakeHandler()
        val readers = ArrayList<FakeReader>()
        var failNextReader: RuntimeException? = null
        var stoppedCalls = 0
        var sizeChangedCalls = 0

        val platform = object : CapturePlatform() {
            override fun startCaptureThread(): HandlerThread = thread
            override fun handlerFor(thread: HandlerThread): Handler = capture.handler
            override fun mainHandler(): Handler = main.handler
            override fun screenSpec(context: Context): ScreenSpec = ScreenSpec(displaySize, DPI)
            override fun newImageReader(size: PixelSize): ImageReader {
                failNextReader?.let { failNextReader = null; throw it }
                return FakeReader(size).also { readers += it }.reader
            }
        }

        val capturer: ScreenCapturer

        init {
            `when`(context.applicationContext).thenReturn(appContext)
            `when`(appContext.getSystemService(DisplayManager::class.java)).thenReturn(displayManager)
            `when`(
                projection.createVirtualDisplay(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), any(), any(), any()),
            ).thenReturn(virtualDisplay)
            capturer = ScreenCapturer(context, projection, { stoppedCalls++ }, platform)
            capturer.onSizeChanged = { sizeChangedCalls++ }
        }

        fun projectionCallback(): MediaProjection.Callback {
            val captor = ArgumentCaptor.forClass(MediaProjection.Callback::class.java)
            verify(projection).registerCallback(captor.capture(), same(capture.handler))
            return captor.value
        }

        fun displayListener(): DisplayManager.DisplayListener {
            val captor = ArgumentCaptor.forClass(DisplayManager.DisplayListener::class.java)
            verify(displayManager).registerDisplayListener(captor.capture(), same(capture.handler))
            return captor.value
        }

        fun configurationCallbacks(): ComponentCallbacks {
            val captor = ArgumentCaptor.forClass(ComponentCallbacks::class.java)
            verify(appContext).registerComponentCallbacks(captor.capture())
            return captor.value
        }

        fun rotateTo(size: PixelSize) {
            displaySize = size
            displayListener().onDisplayChanged(Display.DEFAULT_DISPLAY)
        }

        fun verifyReleasedOnce() {
            // Captured first: a verify() cannot be nested inside the arguments of another one.
            val callback = projectionCallback()
            val listener = displayListener()
            val callbacks = configurationCallbacks()
            verify(virtualDisplay, times(1)).release()
            readers.forEach { verify(it.reader, times(1)).close() }
            verify(projection, times(1)).unregisterCallback(callback)
            verify(projection, times(1)).stop()
            verify(thread, times(1)).quitSafely()
            verify(displayManager, times(1)).unregisterDisplayListener(listener)
            verify(appContext, times(1)).unregisterComponentCallbacks(callbacks)
        }
    }

    // ---- start ----

    @Test
    fun startRegistersTheProjectionCallbackBeforeCreatingTheVirtualDisplay() {
        val f = Fixture()
        f.capturer.start()

        val order = inOrder(f.projection)
        order.verify(f.projection).registerCallback(any(), same(f.capture.handler))
        order.verify(f.projection).createVirtualDisplay(
            anyString(), eq(1080), eq(2400), eq(DPI), eq(DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR),
            same(f.readers.single().surface), isNull(), isNull(),
        )
        assertEquals(PORTRAIT, f.readers.single().size)
        verify(f.readers.single().reader).setOnImageAvailableListener(any(), same(f.capture.handler))
        f.displayListener()
        f.configurationCallbacks()
        assertTrue(f.capturer.framesMatchScreen)
        assertEquals(0, f.stoppedCalls)
    }

    @Test
    fun startingTwiceCreatesOneVirtualDisplay() {
        val f = Fixture()
        f.capturer.start()
        f.capturer.start()
        verify(f.projection, times(1)).createVirtualDisplay(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), any(), any(), any())
        verify(f.projection, times(1)).registerCallback(any(), any())
        assertEquals(1, f.readers.size)
    }

    @Test
    fun onlyTheApplicationContextIsKept() {
        val f = Fixture()
        f.capturer.start()
        f.rotateTo(LANDSCAPE)
        f.capturer.stop()
        verify(f.context).applicationContext
        verifyNoMoreInteractions(f.context)
    }

    @Test
    fun nothingIsCapturedBeforeStart() = runBlocking {
        val f = Fixture()
        assertFalse(f.capturer.framesMatchScreen)
        assertNull(f.capturer.latestFrame())
        assertNull(f.capturer.freshFrame(10))
    }

    @Test
    fun aRejectedProjectionIsReleasedAndRethrown() {
        val f = Fixture()
        `when`(
            f.projection.createVirtualDisplay(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), any(), any(), any()),
        ).thenThrow(SecurityException("token reused"))

        assertFailsWith<SecurityException> { f.capturer.start() }

        val callback = f.projectionCallback()
        verify(f.readers.single().reader).close()
        verify(f.projection).unregisterCallback(callback)
        verify(f.projection).stop()
        verify(f.thread).quitSafely()
        assertFalse(f.capturer.framesMatchScreen)
        assertEquals(0, f.main.runAll())
        assertEquals(0, f.stoppedCalls, "a failed start is reported by the exception, not by onStopped")
        assertFailsWith<IllegalStateException> { f.capturer.start() }
    }

    @Test
    fun aMissingVirtualDisplayIsAnError() {
        val f = Fixture()
        `when`(
            f.projection.createVirtualDisplay(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), any(), any(), any()),
        ).thenReturn(null)
        assertFailsWith<IllegalStateException> { f.capturer.start() }
        verify(f.readers.single().reader).close()
        verify(f.projection).stop()
    }

    @Test
    fun cannotStartAfterStop() {
        val f = Fixture()
        f.capturer.stop()
        verify(f.projection).stop()
        assertFailsWith<IllegalStateException> { f.capturer.start() }
        verify(f.projection, never()).createVirtualDisplay(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), any(), any(), any())
    }

    // ---- frames ----

    @Test
    fun framesAreConvertedAndEveryImageIsClosed() = runBlocking {
        val f = Fixture(displaySize = PixelSize(5, 3))
        f.capturer.start()
        assertNull(f.capturer.latestFrame())

        val image = f.readers.single().deliver()

        val frame = assertNotNull(f.capturer.latestFrame())
        assertEquals(5, frame.width)
        assertEquals(3, frame.height)
        assertContentEquals(IntArray(15) { 0xFF112233.toInt() }, frame.pixels)
        verify(image).close()
        assertSame(frame, f.capturer.latestFrame())
        assertSame(frame, f.capturer.freshFrame(20), "a static screen: the latest frame after the timeout")
    }

    @Test
    fun freshFrameWaitsForAFrameThatArrivesAfterTheCall() = runBlocking {
        val f = Fixture(displaySize = PixelSize(5, 3))
        f.capturer.start()
        val reader = f.readers.single()
        reader.deliver()
        val before = assertNotNull(f.capturer.latestFrame())

        val producer = thread {
            Thread.sleep(80)
            reader.offer()
            reader.listener().onImageAvailable(reader.reader)
        }
        val fresh = assertNotNull(f.capturer.freshFrame(30_000))
        producer.join()
        assertTrue(fresh !== before)
        assertContentEquals(before.pixels, fresh.pixels)
    }

    @Test
    fun aBrokenImageIsClosedAndDropped() = runBlocking {
        val f = Fixture(displaySize = PixelSize(5, 3))
        f.capturer.start()
        val reader = f.readers.single()

        val tooSmall = reader.deliver(buffer = ByteBuffer.allocate(8), rowStride = 20)
        verify(tooSmall).close()
        assertNull(f.capturer.latestFrame())

        `when`(reader.reader.acquireLatestImage()).thenThrow(IllegalStateException("maxImages"))
        reader.listener().onImageAvailable(reader.reader)
        assertNull(f.capturer.latestFrame())

        val good = reader.deliver()
        verify(good).close()
        assertNotNull(f.capturer.latestFrame(), "capture carries on after a bad frame")
        Unit
    }

    @Test
    fun anEmptyReaderIsNotAnError() = runBlocking {
        val f = Fixture()
        f.capturer.start()
        val reader = f.readers.single()
        reader.listener().onImageAvailable(reader.reader) // acquireLatestImage() == null
        assertNull(f.capturer.latestFrame())
    }

    // ---- rotation and resize ----

    @Test
    fun rotationResizesTheExistingDisplayAndSwapsTheReader() = runBlocking {
        val f = Fixture()
        f.capturer.start()
        val first = f.readers.single()
        first.deliver()
        assertNotNull(f.capturer.latestFrame())

        f.rotateTo(LANDSCAPE)

        assertEquals(2, f.readers.size)
        val second = f.readers[1]
        assertEquals(LANDSCAPE, second.size)
        val order = inOrder(f.virtualDisplay, first.reader)
        order.verify(f.virtualDisplay).resize(2400, 1080, DPI)
        order.verify(f.virtualDisplay).surface = second.surface
        order.verify(first.reader).close()
        verify(second.reader).setOnImageAvailableListener(any(), same(f.capture.handler))
        verify(second.reader, never()).close()
        verify(f.projection, times(1)).createVirtualDisplay(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), any(), any(), any())
        verify(f.virtualDisplay, never()).release()

        assertNull(f.capturer.latestFrame(), "frames of the old orientation are gone")
        assertTrue(f.capturer.framesMatchScreen)
        assertEquals(0, f.sizeChangedCalls, "delivered on the main thread, not inline")
        f.main.runAll()
        assertEquals(1, f.sizeChangedCalls)

        second.deliver()
        val frame = assertNotNull(f.capturer.latestFrame())
        assertEquals(2400, frame.width)
        assertEquals(1080, frame.height)
    }

    @Test
    fun displayEventsThatDoNotChangeTheSizeDoNothing() {
        val f = Fixture()
        f.capturer.start()
        f.displayListener().onDisplayChanged(Display.DEFAULT_DISPLAY)
        f.displayListener().onDisplayChanged(7) // e.g. our own virtual display
        f.displayListener().onDisplayAdded(7)
        f.displayListener().onDisplayRemoved(7)
        f.capture.runAll()
        f.main.runAll()
        assertEquals(1, f.readers.size)
        verify(f.virtualDisplay, never()).resize(anyInt(), anyInt(), anyInt())
        assertEquals(0, f.sizeChangedCalls)
    }

    @Test
    fun aStaleDisplayReadIsCorrectedByTheConfigurationCallback() {
        val f = Fixture()
        f.capturer.start()
        // The display event arrives while the app's configuration still reports the old size (API 31+).
        f.displayListener().onDisplayChanged(Display.DEFAULT_DISPLAY)
        verify(f.virtualDisplay, never()).resize(anyInt(), anyInt(), anyInt())
        assertEquals(listOf(500L), f.capture.delays, "a re-check is scheduled as a safety net")

        f.displaySize = LANDSCAPE
        f.configurationCallbacks().onConfigurationChanged(Configuration())
        assertEquals(listOf(0L), f.capture.delays, "re-checks are coalesced")
        verify(f.virtualDisplay, never()).resize(anyInt(), anyInt(), anyInt())
        assertEquals(1, f.capture.runAll())

        verify(f.virtualDisplay).resize(2400, 1080, DPI)
        assertTrue(f.capturer.framesMatchScreen)
    }

    @Test
    fun theDelayedRecheckAloneAlsoPicksUpTheNewSize() {
        val f = Fixture()
        f.capturer.start()
        f.displayListener().onDisplayChanged(Display.DEFAULT_DISPLAY)
        f.displaySize = LANDSCAPE
        f.capture.runAll()
        verify(f.virtualDisplay).resize(2400, 1080, DPI)
    }

    @Test
    fun capturedContentResizeIsFollowed() {
        val f = Fixture()
        f.capturer.start()
        val callback = f.projectionCallback()

        callback.onCapturedContentResize(1080, 2400) // right after capture begins: the whole display
        verify(f.virtualDisplay, never()).resize(anyInt(), anyInt(), anyInt())
        assertTrue(f.capturer.framesMatchScreen)
        assertEquals(0, f.main.runAll())

        callback.onCapturedContentResize(1080, 1170) // a single app, now in split-screen
        verify(f.virtualDisplay).resize(1080, 1170, DPI)
        assertEquals(PixelSize(1080, 1170), f.readers[1].size)
        assertFalse(f.capturer.framesMatchScreen, "frame pixels are no longer screen pixels")
        f.main.runAll()
        assertEquals(1, f.sizeChangedCalls)

        callback.onCapturedContentResize(0, 0) // nonsense is ignored
        assertEquals(2, f.readers.size)
    }

    @Test
    fun rotationReportedByBothTheDisplayAndTheContentCallbackResizesOnce() {
        val f = Fixture()
        f.capturer.start()
        val callback = f.projectionCallback()
        callback.onCapturedContentResize(1080, 2400)
        f.rotateTo(LANDSCAPE)
        callback.onCapturedContentResize(2400, 1080)
        f.capture.runAll()
        verify(f.virtualDisplay, times(1)).resize(anyInt(), anyInt(), anyInt())
        assertEquals(2, f.readers.size)
        assertTrue(f.capturer.framesMatchScreen)
        f.main.runAll()
        assertEquals(1, f.sizeChangedCalls)
    }

    @Test
    fun aLateCallbackFromTheReplacedReaderIsIgnored() = runBlocking {
        val f = Fixture()
        f.capturer.start()
        val first = f.readers.single()
        val listener = first.listener()
        f.rotateTo(LANDSCAPE)

        first.offer()
        listener.onImageAvailable(first.reader)

        verify(first.reader, never()).acquireLatestImage()
        assertNull(f.capturer.latestFrame())
    }

    @Test
    fun aFailedResizeKeepsTheCaptureRunningAndIsRetried() = runBlocking {
        val f = Fixture()
        f.capturer.start()
        val first = f.readers.single()

        f.failNextReader = UnsupportedOperationException("no buffers")
        f.rotateTo(LANDSCAPE)
        verify(f.virtualDisplay, never()).resize(anyInt(), anyInt(), anyInt())
        verify(first.reader, never()).close()
        assertFalse(f.capturer.framesMatchScreen, "still capturing at the old size")
        first.deliver()
        assertEquals(1080, assertNotNull(f.capturer.latestFrame()).width)

        f.capture.runAll() // the scheduled re-check tries again
        verify(f.virtualDisplay).resize(2400, 1080, DPI)
        verify(first.reader).close()
        assertTrue(f.capturer.framesMatchScreen)
    }

    @Test
    fun aDisplayThatRefusesToResizeLeavesTheOldReaderInPlace() {
        val f = Fixture()
        f.capturer.start()
        val first = f.readers.single()
        doThrow(IllegalStateException("dead")).`when`(f.virtualDisplay).resize(anyInt(), anyInt(), anyInt())

        f.rotateTo(LANDSCAPE)

        verify(first.reader, never()).close()
        verify(f.readers[1].reader).close()
        assertFalse(f.capturer.framesMatchScreen)
    }

    // ---- stopping ----

    @Test
    fun theSystemEndingTheCaptureReleasesEverythingAndReportsItOnce() = runBlocking {
        val f = Fixture()
        f.capturer.start()
        val reader = f.readers.single()
        reader.deliver()
        val callback = f.projectionCallback()

        callback.onStop()

        f.verifyReleasedOnce()
        val order = inOrder(f.virtualDisplay, reader.reader, f.projection)
        order.verify(f.virtualDisplay).release()
        order.verify(reader.reader).close()
        order.verify(f.projection).unregisterCallback(callback)
        assertEquals(0, f.stoppedCalls, "delivered on the main thread, not inline")
        assertEquals(1, f.main.runAll())
        assertEquals(1, f.stoppedCalls)
        assertNull(f.capturer.latestFrame())
        assertFalse(f.capturer.framesMatchScreen)

        callback.onStop()
        f.capturer.stop()
        f.capturer.stop()
        f.main.runAll()
        assertEquals(1, f.stoppedCalls)
        f.verifyReleasedOnce()
    }

    @Test
    fun stopReleasesEverythingOnceAndDoesNotReportStopped() = runBlocking {
        val f = Fixture()
        f.capturer.start()
        val reader = f.readers.single()
        reader.deliver()
        val callback = f.projectionCallback()
        val listener = reader.listener()
        f.displayListener().onDisplayChanged(Display.DEFAULT_DISPLAY) // leaves a delayed re-check pending

        f.capturer.stop()

        f.verifyReleasedOnce()
        assertEquals(emptyList(), f.capture.delays, "nothing left queued on the capture thread")
        assertNull(f.capturer.latestFrame())
        assertNull(f.capturer.freshFrame(10))
        assertFalse(f.capturer.framesMatchScreen)

        // Everything that may still trickle in afterwards is ignored.
        f.capturer.stop()
        callback.onStop() // the echo of our own projection.stop()
        callback.onCapturedContentResize(2400, 1080)
        f.displaySize = LANDSCAPE
        f.displayListener().onDisplayChanged(Display.DEFAULT_DISPLAY)
        f.configurationCallbacks().onConfigurationChanged(Configuration())
        reader.offer()
        listener.onImageAvailable(reader.reader)
        f.capture.runAll()
        f.main.runAll()

        f.verifyReleasedOnce()
        verify(reader.reader, times(1)).acquireLatestImage()
        verify(f.virtualDisplay, never()).resize(anyInt(), anyInt(), anyInt())
        assertEquals(1, f.readers.size)
        assertEquals(0, f.stoppedCalls)
        assertEquals(0, f.sizeChangedCalls)
    }

    @Test
    fun stopCancelsNotificationsThatWereNotDeliveredYet() {
        val f = Fixture()
        f.capturer.start()
        f.rotateTo(LANDSCAPE)        // onSizeChanged queued for the main thread
        f.projectionCallback().onStop() // onStopped queued for the main thread
        f.capturer.stop()
        assertEquals(0, f.main.runAll())
        assertEquals(0, f.stoppedCalls)
        assertEquals(0, f.sizeChangedCalls)
    }

    @Test
    fun stopMayBeCalledFromTheCallbacks() {
        val f = Fixture()
        lateinit var capturer: ScreenCapturer
        var stopped = 0
        capturer = ScreenCapturer(f.context, f.projection, { stopped++; capturer.stop() }, f.platform)
        capturer.onSizeChanged = { capturer.stop() }
        capturer.start()

        f.rotateTo(LANDSCAPE)
        f.main.runAll() // onSizeChanged -> stop()
        verify(f.virtualDisplay).release()
        verify(f.projection).stop()

        val other = Fixture()
        lateinit var second: ScreenCapturer
        second = ScreenCapturer(other.context, other.projection, { stopped++; second.stop() }, other.platform)
        second.start()
        other.projectionCallback().onStop()
        other.main.runAll() // onStopped -> stop()
        assertEquals(1, stopped)
        verify(other.projection, times(1)).stop()
    }

    @Test
    fun aFailureWhileReleasingDoesNotStopTheRestFromBeingReleased() {
        val f = Fixture()
        f.capturer.start()
        doThrow(IllegalStateException("already released")).`when`(f.virtualDisplay).release()
        doThrow(IllegalStateException("gone")).`when`(f.displayManager).unregisterDisplayListener(any())
        f.capturer.stop()
        f.verifyReleasedOnce()
    }

    @Test
    fun stopFromAnotherThreadWaitsForTheFrameBeingCopied() {
        val f = Fixture(displaySize = PixelSize(5, 3))
        f.capturer.start()
        val reader = f.readers.single()
        val copying = CountDownLatch(1)
        val finishCopy = CountDownLatch(1)

        // The capture thread is inside an Image's buffer when stop() arrives on another thread.
        val plane = mock(Image.Plane::class.java)
        `when`(plane.rowStride).thenReturn(32)
        `when`(plane.pixelStride).thenReturn(4)
        `when`(plane.buffer).thenAnswer {
            copying.countDown()
            check(finishCopy.await(20, TimeUnit.SECONDS))
            solidRgba(PixelSize(5, 3), padPixels = 3)
        }
        val image = mock(Image::class.java)
        `when`(image.width).thenReturn(5)
        `when`(image.height).thenReturn(3)
        `when`(image.planes).thenReturn(arrayOf(plane))
        `when`(reader.reader.acquireLatestImage()).thenReturn(image)
        val listener = reader.listener()

        val captureThread = thread { listener.onImageAvailable(reader.reader) }
        assertTrue(copying.await(20, TimeUnit.SECONDS))
        val stopper = thread { f.capturer.stop() }
        stopper.join(300)
        assertTrue(stopper.isAlive, "stop() must not close the reader under a frame that is being read")
        verify(reader.reader, never()).close()

        finishCopy.countDown()
        captureThread.join(20_000)
        stopper.join(20_000)
        assertFalse(captureThread.isAlive || stopper.isAlive)
        val order = inOrder(image, reader.reader)
        order.verify(image).close()
        order.verify(reader.reader).close()
        f.verifyReleasedOnce()
    }

    private companion object {
        const val DPI = 420
        val PORTRAIT = PixelSize(1080, 2400)
        val LANDSCAPE = PixelSize(2400, 1080)

        /** RGBA bytes 11 22 33 FF for every pixel, each row followed by [padPixels] pixels of junk. */
        fun solidRgba(size: PixelSize, padPixels: Int): ByteBuffer {
            val stride = (size.width + padPixels) * 4
            val bytes = ByteArray((size.height - 1) * stride + size.width * 4) { 0x7E }
            for (y in 0 until size.height) for (x in 0 until size.width) {
                val i = y * stride + x * 4
                bytes[i] = 0x11
                bytes[i + 1] = 0x22
                bytes[i + 2] = 0x33
                bytes[i + 3] = 0xFF.toByte()
            }
            return ByteBuffer.wrap(bytes)
        }
    }
}
