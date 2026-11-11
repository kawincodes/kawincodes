package com.krstech.chesshelp.capture

import android.graphics.Color
import android.graphics.HardwareRenderer
import android.graphics.RenderNode
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLog
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TmpProbeTest {
    @Test
    fun probe() {
        ShadowLog.stream = System.out
        val thread = HandlerThread("probe").apply { start() }
        val handler = Handler(thread.looper)
        val reader = CapturePlatform().newImageReader(PixelSize(37, 23))
        var calls = 0
        reader.setOnImageAvailableListener({ calls++; println("listener called on ${Thread.currentThread().name}") }, handler)
        val node = RenderNode("screen")
        node.setPosition(0, 0, 37, 23)
        val canvas = node.beginRecording()
        canvas.drawColor(Color.rgb(0x11, 0x22, 0x33))
        node.endRecording()
        val renderer = HardwareRenderer()
        renderer.setSurface(reader.surface)
        renderer.setContentRoot(node)
        val result = renderer.createRenderRequest().setWaitForPresent(true).syncAndDraw()
        println("syncAndDraw result=$result")
        Thread.sleep(1000)
        println("listener calls after 1s: $calls")
        val image = reader.acquireLatestImage()
        println("image=$image")
        if (image != null) {
            val plane = image.planes[0]
            println("size=${image.width}x${image.height} format=${image.format} rowStride=${plane.rowStride} pixelStride=${plane.pixelStride} buffer=${plane.buffer} order=${plane.buffer.order()}")
            val b = plane.buffer
            println("first bytes: " + (0 until 8).joinToString(" ") { "%02x".format(b.get(it)) })
            val px = RgbaPixels.toArgb(b, image.width, image.height, plane.rowStride, plane.pixelStride)
            println("pixel0=%08x last=%08x".format(px[0], px[px.size - 1]))
            image.close()
        }
        reader.close()
        thread.quitSafely()
    }
}
