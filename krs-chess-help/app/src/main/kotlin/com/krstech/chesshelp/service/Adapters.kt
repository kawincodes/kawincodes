package com.krstech.chesshelp.service

import com.krstech.chesshelp.brain.AdviseFor
import com.krstech.chesshelp.brain.ArrowSpec
import com.krstech.chesshelp.brain.BoardImageEncoder
import com.krstech.chesshelp.brain.BoardUi
import com.krstech.chesshelp.brain.BrainSettings
import com.krstech.chesshelp.brain.FrameSource
import com.krstech.chesshelp.brain.OverlayPort
import com.krstech.chesshelp.brain.UiState
import com.krstech.chesshelp.capture.BoardImageRenderer
import com.krstech.chesshelp.capture.ScreenCapturer
import com.krstech.chesshelp.claude.ClaudeModels
import com.krstech.chesshelp.claude.Effort
import com.krstech.chesshelp.claude.EncodedImage
import com.krstech.chesshelp.overlay.BoardArrow
import com.krstech.chesshelp.overlay.MiniBoard
import com.krstech.chesshelp.overlay.OverlayController
import com.krstech.chesshelp.overlay.OverlayState
import com.krstech.chesshelp.settings.AdviseForSetting
import com.krstech.chesshelp.settings.AppSettings
import com.krstech.chesshelp.settings.EffortSetting
import com.krstech.chesshelp.settings.SecretStore
import com.krstech.chesshelp.vision.BoardRect
import com.krstech.chesshelp.vision.Frame

/* Thin adapters from the Android pieces to the ports the analysis brain is written against. */

internal class CapturerFrames(private val capturer: ScreenCapturer) : FrameSource {
    override suspend fun latestFrame(): Frame? = capturer.latestFrame()
    override suspend fun freshFrame(timeoutMs: Long): Frame? = capturer.freshFrame(timeoutMs)
    override val framesMatchScreen: Boolean get() = capturer.framesMatchScreen
}

internal object RendererEncoder : BoardImageEncoder {
    override fun encodeBoard(frame: Frame, rect: BoardRect): EncodedImage =
        EncodedImage(BoardImageRenderer.boardPng(frame, rect), "image/png")

    override fun encodeScreen(frame: Frame, maxLongEdge: Int): EncodedImage =
        EncodedImage(BoardImageRenderer.screenJpeg(frame, maxLongEdge), "image/jpeg")
}

internal class OverlayAdapter(private val overlay: OverlayController) : OverlayPort {
    override fun render(state: UiState) {
        overlay.render(state.toOverlayState())
    }

    override fun showArrow(arrow: ArrowSpec?) {
        overlay.showBoardArrow(
            arrow?.let { BoardArrow(it.boardLeft, it.boardTop, it.boardSize, it.fromRow, it.fromCol, it.toRow, it.toCol) },
        )
    }

    override fun setCaptureMode(hidden: Boolean) {
        overlay.setCaptureMode(hidden)
    }

    override fun setAutoEnabled(enabled: Boolean) {
        overlay.setAutoEnabled(enabled)
    }
}

internal fun BoardUi.toMiniBoard(): MiniBoard = MiniBoard(rows, fromRow, fromCol, toRow, toCol)

internal fun UiState.toOverlayState(): OverlayState = when (this) {
    is UiState.Idle -> OverlayState.Idle(hint)
    is UiState.Working -> OverlayState.Working(message, board?.toMiniBoard())
    is UiState.Advice -> OverlayState.Advice(
        title = title,
        moveSan = moveSan,
        evaluation = evaluation,
        explanation = explanation,
        alternatives = alternatives,
        threat = threat,
        source = source,
        board = board?.toMiniBoard(),
        footer = footer,
    )
    is UiState.Message -> OverlayState.Message(title, body, isError, board?.toMiniBoard())
}

internal class SettingsAdapter(
    private val settings: AppSettings,
    private val secrets: SecretStore,
) : BrainSettings {
    override val apiKey: String? get() = secrets.loadApiKey()?.takeIf { it.isNotBlank() }
    override val model: String get() = settings.modelId
    override val modelLabel: String get() = ClaudeModels.labelOf(settings.modelId)
    override val effort: Effort
        get() = when (settings.effort) {
            EffortSetting.LOW -> Effort.LOW
            EffortSetting.MEDIUM -> Effort.MEDIUM
            EffortSetting.HIGH -> Effort.HIGH
        }
    override val baseUrl: String? get() = settings.baseUrlOrNull
    override val adviseFor: AdviseFor
        get() = when (settings.adviseFor) {
            AdviseForSetting.BOTTOM -> AdviseFor.BOTTOM
            AdviseForSetting.WHITE -> AdviseFor.WHITE
            AdviseForSetting.BLACK -> AdviseFor.BLACK
            AdviseForSetting.BOTH -> AdviseFor.BOTH
        }
    override var autoAdvise: Boolean
        get() = settings.autoAdvise
        set(value) {
            settings.autoAdvise = value
        }
    override val drawOnBoard: Boolean get() = settings.drawOnBoard
    override val sendScreenContext: Boolean get() = settings.sendScreenContext
}
