package com.krstech.chesshelp.capture

import kotlin.test.Test
import kotlin.test.assertEquals

class CaptureSizeTrackerTest {

    private val portrait = PixelSize(1080, 2400)
    private val landscape = PixelSize(2400, 1080)

    @Test
    fun withoutContentCallbacksTheDisplayDecides() {
        val tracker = CaptureSizeTracker(portrait)
        assertEquals(portrait, tracker.target)
        tracker.onDisplaySize(landscape)
        assertEquals(landscape, tracker.target)
        assertEquals(landscape, tracker.display)
    }

    @Test
    fun wholeDisplayCaptureFollowsARotationBeforeTheContentCallbackArrives() {
        val tracker = CaptureSizeTracker(portrait)
        tracker.onContentSize(portrait) // API 34+: reported right after capture begins
        tracker.onDisplaySize(landscape)
        assertEquals(landscape, tracker.target)
        tracker.onContentSize(landscape)
        assertEquals(landscape, tracker.target)
    }

    @Test
    fun contentCallbackFirstThenDisplay() {
        val tracker = CaptureSizeTracker(portrait)
        tracker.onContentSize(portrait)
        tracker.onContentSize(landscape) // the display read was still stale at this point
        assertEquals(landscape, tracker.target)
        assertEquals(portrait, tracker.display)
        tracker.onDisplaySize(landscape)
        assertEquals(landscape, tracker.target)
        assertEquals(landscape, tracker.display)
    }

    @Test
    fun singleAppCaptureKeepsItsOwnSize() {
        val tracker = CaptureSizeTracker(portrait)
        val splitScreen = PixelSize(1080, 1170)
        tracker.onContentSize(splitScreen)
        assertEquals(splitScreen, tracker.target)
        tracker.onDisplaySize(landscape) // the display rotates; the app window reports its own size later
        assertEquals(splitScreen, tracker.target)
        assertEquals(landscape, tracker.display)
        tracker.onContentSize(PixelSize(1200, 1080))
        assertEquals(PixelSize(1200, 1080), tracker.target)
    }

    @Test
    fun invalidSizesAreIgnored() {
        val tracker = CaptureSizeTracker(portrait)
        tracker.onDisplaySize(PixelSize(0, 2400))
        tracker.onDisplaySize(PixelSize(1080, -1))
        tracker.onContentSize(PixelSize(0, 0))
        assertEquals(portrait, tracker.target)
        assertEquals(portrait, tracker.display)
    }
}
