package com.krstech.chesshelp.brain

import com.krstech.chesshelp.vision.BoardFingerprint
import com.krstech.chesshelp.vision.BoardLocator
import com.krstech.chesshelp.vision.BoardRect
import com.krstech.chesshelp.vision.BoardSampler
import com.krstech.chesshelp.vision.BoardSnapshot
import com.krstech.chesshelp.vision.Frame

/** The pixel analysis the brain needs. Blocking; called on [BrainDispatchers.cpu]. */
interface BoardVision {
    fun locate(frame: Frame, hint: BoardRect?): BoardRect?
    fun sample(frame: Frame, rect: BoardRect): BoardSnapshot
    fun fingerprint(frame: Frame, rect: BoardRect): IntArray

    /** True when two fingerprints differ by more than the "the board changed" threshold. */
    fun differs(a: IntArray, b: IntArray): Boolean
}

/** [BoardVision] on top of the :vision module. */
object PixelBoardVision : BoardVision {
    override fun locate(frame: Frame, hint: BoardRect?): BoardRect? = BoardLocator.locate(frame, hint)

    override fun sample(frame: Frame, rect: BoardRect): BoardSnapshot = BoardSampler.sample(frame, rect)

    override fun fingerprint(frame: Frame, rect: BoardRect): IntArray = BoardFingerprint.of(frame, rect)

    override fun differs(a: IntArray, b: IntArray): Boolean =
        BoardFingerprint.distance(a, b) > BoardFingerprint.CHANGE_THRESHOLD
}
