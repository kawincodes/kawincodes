package com.krstech.chesshelp.capture

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class FrameMailboxTest {

    /** A raw word 0xAABBGGRR and the Frame pixel 0xAARRGGBB it must become. */
    private val word = 0x44332211
    private val argb = 0x44112233

    private fun FrameMailbox.publishFilled(width: Int, height: Int, value: Int = word): IntArray {
        val buffer = obtainBuffer(width * height)
        buffer.fill(value)
        publish(buffer, width, height)
        return buffer
    }

    @Test
    fun emptyMailboxHasNoFrame() {
        val mailbox = FrameMailbox()
        assertNull(mailbox.latest())
        assertNull(mailbox.convertedOrNull())
    }

    @Test
    fun publishedWordsBecomeArgbPixelsOnDemand() {
        val mailbox = FrameMailbox()
        mailbox.publishFilled(3, 2)
        assertNull(mailbox.convertedOrNull(), "conversion is deferred until the frame is asked for")
        val frame = assertNotNull(mailbox.latest())
        assertEquals(3, frame.width)
        assertEquals(2, frame.height)
        assertContentEquals(IntArray(6) { argb }, frame.pixels)
        assertSame(frame, mailbox.latest(), "a static screen keeps returning the same frame")
        assertSame(frame, mailbox.convertedOrNull())
    }

    @Test
    fun unreadFramesAreOverwrittenAndTheirBufferReused() {
        val mailbox = FrameMailbox()
        val first = mailbox.publishFilled(2, 2, value = 1)
        val second = mailbox.publishFilled(2, 2, value = 2)
        assertNotSame(first, second)
        val third = mailbox.obtainBuffer(4)
        assertSame(first, third, "the buffer of the unread frame is recycled, not reallocated")
        third.fill(word)
        mailbox.publish(third, 2, 2)
        assertContentEquals(IntArray(4) { argb }, assertNotNull(mailbox.latest()).pixels)
    }

    @Test
    fun aBufferHandedOutInAFrameIsNeverReused() {
        val mailbox = FrameMailbox()
        mailbox.publishFilled(2, 2)
        val frame = assertNotNull(mailbox.latest())
        repeat(5) { round ->
            val buffer = mailbox.obtainBuffer(4)
            assertNotSame(frame.pixels, buffer, "round $round")
            buffer.fill(round)
            mailbox.publish(buffer, 2, 2)
        }
        assertContentEquals(IntArray(4) { argb }, frame.pixels, "a frame already handed out must not change")
    }

    @Test
    fun aBufferOfTheWrongSizeIsNotReused() {
        val mailbox = FrameMailbox()
        mailbox.publishFilled(2, 2)
        mailbox.publishFilled(2, 2) // leaves a 4-pixel spare behind
        assertEquals(6, mailbox.obtainBuffer(6).size)
        assertEquals(4, mailbox.obtainBuffer(4).size)
    }

    @Test
    fun recycledBufferComesBack() {
        val mailbox = FrameMailbox()
        val buffer = mailbox.obtainBuffer(9)
        mailbox.recycle(buffer)
        assertSame(buffer, mailbox.obtainBuffer(9))
    }

    @Test
    fun publishRejectsAMismatchedBuffer() {
        val mailbox = FrameMailbox()
        assertFailsWith<IllegalArgumentException> { mailbox.publish(IntArray(5), 2, 2) }
        assertFailsWith<IllegalArgumentException> { mailbox.publish(IntArray(0), 0, 0) }
        assertNull(mailbox.latest())
    }

    @Test
    fun clearForgetsEveryFrame() {
        val mailbox = FrameMailbox()
        mailbox.publishFilled(2, 2)
        mailbox.latest()
        mailbox.publishFilled(2, 2)
        mailbox.clear()
        assertNull(mailbox.latest())
        assertNull(mailbox.convertedOrNull())
        mailbox.publishFilled(4, 1)
        assertEquals(4, assertNotNull(mailbox.latest()).width, "still usable after clear")
    }

    @Test
    fun closeForgetsEverythingAndRefusesNewFrames() {
        val mailbox = FrameMailbox()
        mailbox.publishFilled(2, 2)
        mailbox.close()
        assertNull(mailbox.latest())
        mailbox.publishFilled(2, 2)
        assertNull(mailbox.latest())
        val buffer = IntArray(4)
        mailbox.recycle(buffer)
        assertNotSame(buffer, mailbox.obtainBuffer(4), "nothing is retained after close")
    }

    @Test
    fun awaitArrivalTimesOutOnAStaticScreen() = runBlocking {
        val mailbox = FrameMailbox()
        mailbox.publishFilled(2, 2) // arrived before the call: does not count
        val start = System.nanoTime()
        assertFalse(mailbox.awaitArrival(60))
        assertTrue((System.nanoTime() - start) / 1_000_000 >= 50, "must wait for the timeout")
        assertFalse(mailbox.awaitArrival(0))
        assertFalse(mailbox.awaitArrival(-5))
        assertNotNull(mailbox.latest())
        Unit
    }

    @Test
    fun awaitArrivalReturnsAsSoonAsAFrameIsPublished() = runBlocking {
        val mailbox = FrameMailbox()
        val waiter = async(Dispatchers.Default, start = CoroutineStart.UNDISPATCHED) { mailbox.awaitArrival(30_000) }
        thread {
            Thread.sleep(50)
            mailbox.publishFilled(2, 2)
        }
        assertTrue(withTimeout(10_000) { waiter.await() })
        assertContentEquals(IntArray(4) { argb }, assertNotNull(mailbox.latest()).pixels)
    }

    @Test
    fun closeReleasesWaiters() = runBlocking {
        val mailbox = FrameMailbox()
        val waiter = async(Dispatchers.Default, start = CoroutineStart.UNDISPATCHED) { mailbox.awaitArrival(30_000) }
        thread {
            Thread.sleep(50)
            mailbox.close()
        }
        withTimeout(10_000) { waiter.await() }
        assertNull(mailbox.latest())
    }

    @Test
    fun concurrentPublishAndReadNeverTearsAFrame() {
        val mailbox = FrameMailbox()
        val width = 64
        val height = 48
        val stop = AtomicBoolean(false)
        val failure = AtomicReference<Throwable?>(null)
        val publisher = thread {
            var n = 0
            while (!stop.get()) {
                // Every pixel of frame n carries n in its green byte, which the R/B swap leaves alone.
                val buffer = mailbox.obtainBuffer(width * height)
                buffer.fill(((n++ and 0xFF) shl 8) or 0xFF0000AA.toInt())
                mailbox.publish(buffer, width, height)
            }
        }
        val readers = List(3) {
            thread {
                try {
                    val kept = ArrayList<Pair<IntArray, Int>>()
                    while (!stop.get()) {
                        val frame = mailbox.latest() ?: continue
                        val first = frame.pixels[0]
                        check(first and 0xFFFF00FF.toInt() == 0xFFAA0000.toInt()) { "bad pixel %08x".format(first) }
                        check(frame.pixels.all { it == first }) { "torn frame" }
                        if (kept.size < 200) kept += frame.pixels to first
                    }
                    // Frames handed out earlier must not have been overwritten by later captures.
                    for ((pixels, value) in kept) check(pixels.all { it == value }) { "a handed-out frame changed" }
                } catch (t: Throwable) {
                    failure.compareAndSet(null, t)
                }
            }
        }
        Thread.sleep(400)
        stop.set(true)
        publisher.join()
        readers.forEach { it.join() }
        failure.get()?.let { throw it }
    }
}
