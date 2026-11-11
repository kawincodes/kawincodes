package com.krstech.chesshelp.capture

import android.content.Context
import android.graphics.Color
import android.graphics.HardwareRenderer
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RenderNode
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Display
import android.view.Surface
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The capturer with the real platform classes of the Android 15 framework (Robolectric, native graphics):
 * a real HandlerThread, a real ImageReader fed through its Surface, real display metrics. Only the
 * MediaProjection and its VirtualDisplay are mocks, the test plays the part of the screen compositor.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenCapturerPipelineTest {

    private val app: Context get() = RuntimeEnvironment.getApplication()

    private val projection: MediaProjection = mock(MediaProjection::class.java)
    private val virtualDisplay: VirtualDisplay = mock(VirtualDisplay::class.java)

    private fun startCapturer(onStopped: () -> Unit = {}): Pair<ScreenCapturer, Surface> {
        `when`(
            projection.createVirtualDisplay(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), any(), any(), any()),
        ).thenReturn(virtualDisplay)
        val capturer = ScreenCapturer(app, projection, onStopped)
        capturer.start()
        val surface = ArgumentCaptor.forClass(Surface::class.java)
        verify(projection).createVirtualDisplay(
            anyString(), anyInt(), anyInt(), anyInt(), anyInt(), surface.capture(), any(), any(),
        )
        return capturer to surface.value
    }

    /** Draws one frame into the capture surface: four quadrants plus a white pixel in the top-left corner. */
    private fun compose(surface: Surface, width: Int, height: Int, topLeft: Int) {
        val node = RenderNode("screen")
        node.setPosition(0, 0, width, height)
        val canvas = node.beginRecording()
        val paint = Paint()
        fun quadrant(left: Int, top: Int, color: Int) {
            paint.color = color
            canvas.drawRect(
                left.toFloat(), top.toFloat(), (left + width / 2 + 1).toFloat(), (top + height / 2 + 1).toFloat(), paint,
            )
        }
        quadrant(0, 0, topLeft)
        quadrant(width / 2, 0, GREEN)
        quadrant(0, height / 2, BLUE)
        quadrant(width / 2, height / 2, ORANGE)
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, 1f, 1f, paint)
        node.endRecording()
        val renderer = HardwareRenderer()
        try {
            renderer.setSurface(surface)
            renderer.setContentRoot(node)
            renderer.createRenderRequest().setWaitForPresent(true).syncAndDraw()
        } finally {
            renderer.destroy()
        }
    }

    @Test
    fun realDisplayMetricsAreRead() {
        val spec = ScreenMetrics.read(app)
        val legacy = DisplayMetrics()
        @Suppress("DEPRECATION")
        app.getSystemService(DisplayManager::class.java).getDisplay(Display.DEFAULT_DISPLAY).getRealMetrics(legacy)
        println("ScreenMetrics.read: ${spec.size} @ ${spec.densityDpi} dpi; getRealMetrics: ${legacy.widthPixels}x${legacy.heightPixels}")
        assertEquals(PixelSize(720, 1280), spec.size)
        assertEquals(PixelSize(legacy.widthPixels, legacy.heightPixels), spec.size)
        assertEquals(320, spec.densityDpi)
    }

    @Test
    fun theImageReaderIsRgba8888WithTwoImages() {
        val reader = CapturePlatform().newImageReader(PixelSize(37, 23))
        try {
            assertEquals(37, reader.width)
            assertEquals(23, reader.height)
            assertEquals(PixelFormat.RGBA_8888, reader.imageFormat)
            assertEquals(2, reader.maxImages)
            assertNotNull(reader.surface)
        } finally {
            reader.close()
        }
    }

    @Test
    fun framesDrawnIntoTheSurfaceComeOutAsArgbPixels() = runBlocking {
        val (capturer, surface) = startCapturer()
        try {
            verify(projection).createVirtualDisplay(
                anyString(), org.mockito.ArgumentMatchers.eq(720), org.mockito.ArgumentMatchers.eq(1280),
                org.mockito.ArgumentMatchers.eq(320),
                org.mockito.ArgumentMatchers.eq(DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR), any(), any(), any(),
            )
            assertTrue(capturer.framesMatchScreen)
            assertNull(capturer.latestFrame())

            compose(surface, 720, 1280, topLeft = RED)
            val first = assertNotNull(capturer.freshFrame(10_000), "no frame arrived from the ImageReader")
            assertEquals(720, first.width)
            assertEquals(1280, first.height)
            assertEquals(Color.WHITE, first.pixel(0, 0))
            assertEquals(RED, first.pixel(100, 100))
            assertEquals(GREEN, first.pixel(600, 100))
            assertEquals(BLUE, first.pixel(100, 1100))
            assertEquals(ORANGE, first.pixel(719, 1279))
            assertEquals(RED, first.pixel(1, 0))
            assertEquals(RED, first.pixel(0, 1))
            assertEquals(GREEN, first.pixel(719, 0), "row padding must not shift the rows")
            assertEquals(BLUE, first.pixel(0, 1279))

            // A static screen: no new frame, the same one again.
            assertTrue(capturer.freshFrame(100) === first)

            // The next frame replaces it and does not disturb the one already handed out.
            compose(surface, 720, 1280, topLeft = PURPLE)
            var second = capturer.latestFrame()
            val deadline = System.nanoTime() + 10_000_000_000
            while (second === first && System.nanoTime() < deadline) {
                Thread.sleep(10)
                second = capturer.latestFrame()
            }
            assertEquals(PURPLE, assertNotNull(second).pixel(100, 100))
            assertEquals(RED, first.pixel(100, 100))
        } finally {
            capturer.stop()
        }
        assertNull(capturer.latestFrame())
        verify(virtualDisplay).release()
        verify(projection).stop()
    }

    @Test
    fun rotationResizesTheCaptureThroughTheRealDisplayCallbacks() = runBlocking {
        var sizeChanges = 0
        val (capturer, firstSurface) = startCapturer()
        capturer.onSizeChanged = { sizeChanges++ }
        try {
            compose(firstSurface, 720, 1280, topLeft = RED)
            assertNotNull(capturer.freshFrame(10_000))

            RuntimeEnvironment.setQualifiers("+land")

            verify(virtualDisplay, timeout(10_000)).resize(1280, 720, 320)
            val newSurface = ArgumentCaptor.forClass(Surface::class.java)
            verify(virtualDisplay, timeout(10_000)).surface = newSurface.capture()
            assertTrue(newSurface.value !== firstSurface)
            waitUntil { capturer.framesMatchScreen }
            shadowOf(Looper.getMainLooper()).idle()
            assertEquals(1, sizeChanges)
            assertNull(capturer.latestFrame(), "the portrait frame must be gone")

            compose(newSurface.value, 1280, 720, topLeft = PURPLE)
            val frame = assertNotNull(capturer.freshFrame(10_000))
            assertEquals(1280, frame.width)
            assertEquals(720, frame.height)
            assertEquals(PURPLE, frame.pixel(100, 100))
            assertEquals(ORANGE, frame.pixel(1279, 719))
        } finally {
            capturer.stop()
        }
    }

    @Test
    fun theSystemStoppingTheProjectionIsReportedOnTheMainThread() {
        var stoppedOn: Thread? = null
        val (capturer, _) = startCapturer(onStopped = { stoppedOn = Thread.currentThread() })
        val callback = ArgumentCaptor.forClass(MediaProjection.Callback::class.java)
        val handler = ArgumentCaptor.forClass(android.os.Handler::class.java)
        verify(projection).registerCallback(callback.capture(), handler.capture())
        assertTrue(handler.value.looper !== Looper.getMainLooper(), "projection callbacks run on the capture thread")

        handler.value.post { callback.value.onStop() } // as the system would deliver it
        verify(virtualDisplay, timeout(10_000)).release()
        verify(projection, timeout(10_000)).stop()
        waitUntil { !handler.value.looper.thread.isAlive }
        assertNull(stoppedOn)
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(stoppedOn === Looper.getMainLooper().thread)
        capturer.stop()
    }

    private fun waitUntil(condition: () -> Boolean) {
        val deadline = System.nanoTime() + 10_000_000_000
        while (!condition()) {
            check(System.nanoTime() < deadline) { "condition not reached in 10 s" }
            Thread.sleep(10)
        }
    }

    private companion object {
        const val RED = 0xFFE01020.toInt()
        const val GREEN = 0xFF10C040.toInt()
        const val BLUE = 0xFF2030F0.toInt()
        const val ORANGE = 0xFFF0A010.toInt()
        const val PURPLE = 0xFF8020C0.toInt()
    }
}
