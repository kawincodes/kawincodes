package com.krstech.chesshelp.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.Typeface
import android.hardware.input.InputManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.krstech.chesshelp.R
import com.krstech.chesshelp.databinding.OverlayBubbleBinding
import com.krstech.chesshelp.databinding.OverlayPanelBinding
import com.krstech.chesshelp.settings.AppSettings
import com.krstech.chesshelp.settings.FractionPoint
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * The floating UI shown over other apps: a draggable bubble, the advice panel and a full-screen
 * pass-through window that draws the move on the real board.
 *
 * Every method may be called from any thread; work is done on the main thread (immediately when the
 * caller is already on it). No call throws when the overlay permission was revoked or a window died:
 * the failure is logged and the controller keeps working with whatever windows remain.
 */
class OverlayController(context: Context, private val listener: Listener) {

    /** Callbacks arrive on the main thread. */
    interface Listener {
        fun onAnalyze()
        fun onRescan()
        fun onToggleSideToMove()
        fun onAutoChanged(enabled: Boolean)
        fun onFlipBoard()
        fun onStop()
    }

    private val appContext: Context = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val settings = AppSettings(appContext)
    private val windowManager: WindowManager? = appContext.getSystemService(WindowManager::class.java)

    private var ui: Ui? = null
    private var attached = false
    private var panelWanted = false
    private var captureMode = false
    private var autoEnabled = settings.autoAdvise
    private var state: OverlayState = OverlayState.Idle("")
    private var arrow: BoardArrow? = null

    private var screenWidth = 0
    private var screenHeight = 0
    private var area = Area(0, 0, 0, 0)
    private var bubbleFraction: FractionPoint = settings.bubblePosition
    private var panelFraction: FractionPoint? = settings.panelPosition

    /** True while the bubble window is on screen (false when the overlay permission is missing). */
    val isAttached: Boolean
        get() = attached

    /** Shows the bubble. Does nothing (and logs) when the overlay permission is missing. */
    fun attach() = onMain {
        if (attached) return@onMain
        val wm = windowManager
        if (wm == null) {
            Log.w(TAG, "No WindowManager: the overlay cannot be shown")
            return@onMain
        }
        val views = ui ?: createUi(wm).also { ui = it }
        refreshMetrics(views)
        // Added bottom-up: arrow below the panel, panel below the bubble.
        views.arrowWindow.hide()
        views.panelWindow.hide()
        sizePanel(views)
        placeBubble(views)
        if (!(views.arrowWindow.add() && views.panelWindow.add() && views.bubbleWindow.show())) {
            removeWindows(views)
            return@onMain
        }
        attached = true
        bindState(views)
        bindAutoButton(views)
        applyCaptureMode(views)
        if (panelWanted) showPanel(views)
        applyArrow(views)
    }

    /** Removes every window. [attach] may be called again afterwards. */
    fun detach() = onMain {
        attached = false
        ui?.let { removeWindows(it) }
    }

    fun render(state: OverlayState) = onMain {
        this.state = state
        ui?.let { bindState(it) }
    }

    /** Updates the Auto button without reporting a change to the listener. */
    fun setAutoEnabled(enabled: Boolean) = onMain {
        autoEnabled = enabled
        ui?.let { bindAutoButton(it) }
    }

    /** Draws the move over the real board; null hides it. */
    fun showBoardArrow(arrow: BoardArrow?) = onMain {
        this.arrow = arrow?.takeIf { it.isDrawable }
        ui?.let { applyArrow(it) }
    }

    /**
     * true makes every overlay window fully transparent so a clean screenshot can be taken; false
     * restores them. Takes effect with the next frame each window draws (the windows and their
     * surfaces stay in place, so nothing has to be re-created on the way back).
     */
    fun setCaptureMode(enabled: Boolean) = onMain {
        captureMode = enabled
        ui?.let { applyCaptureMode(it) }
    }

    /** Call on rotation / screen-size changes: keeps bubble and panel on screen. */
    fun onConfigurationChanged() = onMain { relayout() }

    // ---------------------------------------------------------------------------------------------

    private class Ui(
        val bubble: OverlayBubbleBinding,
        val panel: OverlayPanelBinding,
        val arrowView: BoardArrowView,
        val bubbleWindow: OverlayWindow,
        val panelWindow: OverlayWindow,
        val arrowWindow: OverlayWindow,
        val bubbleSize: Int,
        val touchSlop: Int,
    )

    private inline fun onMain(crossinline block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else mainHandler.post { block() }
    }

    @SuppressLint("ClickableViewAccessibility") // The drag listeners report taps through performClick().
    private fun createUi(wm: WindowManager): Ui {
        // A service has no theme; the widgets need one for ripples, progress bars and text defaults.
        val themed = ContextThemeWrapper(appContext, R.style.Theme_KrsChessHelp)
        val inflater = LayoutInflater.from(themed)
        val bubble = OverlayBubbleBinding.inflate(inflater)
        val panel = OverlayPanelBinding.inflate(inflater)
        val arrowView = BoardArrowView(themed)
        val resources = themed.resources
        val bubbleSize = resources.getDimensionPixelSize(R.dimen.overlay_bubble_size)

        val baseFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        val arrowFlags = baseFlags or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        val views = Ui(
            bubble = bubble,
            panel = panel,
            arrowView = arrowView,
            bubbleWindow = OverlayWindow(wm, bubble.root, newParams(bubbleSize, bubbleSize, baseFlags, "KRS bubble"), "bubble"),
            panelWindow = OverlayWindow(
                wm,
                panel.root,
                newParams(bubbleSize, WindowManager.LayoutParams.WRAP_CONTENT, baseFlags, "KRS panel"),
                "panel",
            ),
            arrowWindow = OverlayWindow(
                wm,
                arrowView,
                newParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    arrowFlags,
                    "KRS board arrow",
                ),
                "board arrow",
            ),
            bubbleSize = bubbleSize,
            touchSlop = ViewConfiguration.get(themed).scaledTouchSlop,
        )

        panel.panelCard.clipToOutline = true

        bubble.root.setOnClickListener {
            showPanel(views)
            listener.onAnalyze()
        }
        bubble.root.setOnTouchListener(
            DragListener(
                slop = views.touchSlop,
                params = views.bubbleWindow.params,
                onDrag = { x, y -> moveBubble(views, x, y) },
                onDragEnd = { saveBubblePosition(views) },
            ),
        )
        bubble.root.onConfigurationChangedListener = { mainHandler.post { relayout() } }

        panel.panelHeader.setOnTouchListener(
            DragListener(
                slop = views.touchSlop,
                params = views.panelWindow.params,
                onDrag = { x, y -> movePanel(views, x, y) },
                onDragEnd = { savePanelPosition(views) },
            ),
        )
        panel.root.addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            // The panel's height follows its content; keep it on screen (and next to the bubble) when it
            // changes. Posted: window parameters must not be changed in the middle of a layout pass.
            if (right - left != oldRight - oldLeft || bottom - top != oldBottom - oldTop) {
                mainHandler.post { if (attached && panelWanted) placePanel(views) }
            }
        }
        panel.hideButton.setOnClickListener { hidePanel(views) }
        panel.rescanButton.setOnClickListener { listener.onRescan() }
        panel.switchTurnButton.setOnClickListener { listener.onToggleSideToMove() }
        panel.flipButton.setOnClickListener { listener.onFlipBoard() }
        panel.autoButton.setOnClickListener {
            autoEnabled = !autoEnabled
            bindAutoButton(views)
            listener.onAutoChanged(autoEnabled)
        }
        panel.stopButton.setOnClickListener { listener.onStop() }
        return views
    }

    @SuppressLint("RtlHardcoded") // x/y are physical screen coordinates and must not mirror in RTL locales.
    private fun newParams(width: Int, height: Int, flags: Int, title: String): WindowManager.LayoutParams =
        WindowManager.LayoutParams(
            width,
            height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            windowAnimations = 0
            this.title = title
            // Lay out against the whole display on every Android version, so that x/y are screen
            // coordinates; keeping clear of bars and cutouts is done here, with `area`.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                fitInsetsTypes = 0
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

    private fun removeWindows(views: Ui) {
        views.bubbleWindow.remove()
        views.panelWindow.remove()
        views.arrowWindow.remove()
    }

    // ---- Screen metrics -------------------------------------------------------------------------

    private fun refreshMetrics(views: Ui) {
        val wm = windowManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val metrics = wm.maximumWindowMetrics
                val insets = metrics.windowInsets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout(),
                )
                screenWidth = metrics.bounds.width()
                screenHeight = metrics.bounds.height()
                area = OverlayGeometry.insetArea(
                    screenWidth, screenHeight, insets.left, insets.top, insets.right, insets.bottom,
                )
            } else {
                val size = Point()
                @Suppress("DEPRECATION")
                wm.defaultDisplay.getRealSize(size)
                screenWidth = size.x
                screenHeight = size.y
                // The navigation bar needs no inset here: before Android 11 the system itself keeps
                // these windows out of it. Only the status bar has to be avoided by hand.
                @Suppress("DEPRECATION")
                val reportedTop = views.bubble.root.rootWindowInsets?.stableInsetTop ?: 0
                val fallbackTop = views.bubble.root.resources.getDimensionPixelSize(R.dimen.overlay_legacy_status_bar)
                area = OverlayGeometry.insetArea(screenWidth, screenHeight, 0, max(reportedTop, fallbackTop), 0, 0)
            }
        } catch (e: RuntimeException) {
            Log.w(TAG, "Could not read the screen size", e)
        }
        if (screenWidth <= 0 || screenHeight <= 0 || area.width <= 0 || area.height <= 0) {
            val dm = views.bubble.root.resources.displayMetrics
            screenWidth = dm.widthPixels
            screenHeight = dm.heightPixels
            area = Area(0, 0, screenWidth, screenHeight)
        }
    }

    private fun relayout() {
        val views = ui ?: return
        val oldWidth = screenWidth
        val oldHeight = screenHeight
        refreshMetrics(views)
        if (oldWidth != 0 && (oldWidth != screenWidth || oldHeight != screenHeight)) {
            // The arrow's coordinates belong to the old screen geometry; a stale arrow in the wrong
            // place is worse than none. The next analysis draws a new one.
            arrow = null
            applyArrow(views)
        }
        placeBubble(views)
        sizePanel(views)
        if (panelWanted) placePanel(views)
    }

    // ---- Bubble ---------------------------------------------------------------------------------

    private fun placeBubble(views: Ui) {
        val position = OverlayGeometry.fromFraction(bubbleFraction, views.bubbleSize, views.bubbleSize, area)
        val params = views.bubbleWindow.params
        if (params.x != position.x || params.y != position.y) {
            params.x = position.x
            params.y = position.y
            views.bubbleWindow.update()
        }
    }

    private fun moveBubble(views: Ui, x: Int, y: Int) {
        val position = OverlayGeometry.clamp(x, y, views.bubbleSize, views.bubbleSize, area)
        val params = views.bubbleWindow.params
        params.x = position.x
        params.y = position.y
        views.bubbleWindow.update()
        if (panelWanted && panelFraction == null) placePanel(views)
    }

    private fun saveBubblePosition(views: Ui) {
        val params = views.bubbleWindow.params
        val fraction = OverlayGeometry.toFraction(params.x, params.y, views.bubbleSize, views.bubbleSize, area)
        bubbleFraction = fraction
        settings.bubblePosition = fraction
    }

    private fun bindBubble(bubble: OverlayBubbleBinding, current: OverlayState) {
        val showMove = current is OverlayState.Advice && current.moveSan.isNotBlank()
        val showAlert = current is OverlayState.Message && current.isError
        val context = bubble.root.context
        bubble.bubbleSpinner.isVisible = current is OverlayState.Working
        bubble.bubbleIcon.isVisible = !showMove && !showAlert
        bubble.bubbleIcon.alpha = if (current is OverlayState.Working) 0.55f else 1f
        bubble.bubbleLabel.isVisible = showMove || showAlert
        if (showMove) {
            bubble.bubbleLabel.text = (current as OverlayState.Advice).moveSan
            bubble.bubbleLabel.setTextColor(ContextCompat.getColor(context, R.color.overlay_accent))
        } else if (showAlert) {
            bubble.bubbleLabel.setText(R.string.overlay_bubble_alert)
            bubble.bubbleLabel.setTextColor(ContextCompat.getColor(context, R.color.overlay_error))
        }
        bubble.root.setBackgroundResource(
            if (showAlert) R.drawable.overlay_bubble_bg_error else R.drawable.overlay_bubble_bg,
        )
    }

    // ---- Panel ----------------------------------------------------------------------------------

    private fun showPanel(views: Ui) {
        panelWanted = true
        if (!attached) return
        sizePanel(views)
        placePanel(views)
        views.panelWindow.show()
    }

    private fun hidePanel(views: Ui) {
        panelWanted = false
        views.panelWindow.hide()
    }

    private fun sizePanel(views: Ui) {
        val resources = views.panel.root.resources
        val width = OverlayGeometry.panelWidth(
            area.width,
            resources.getDimensionPixelSize(R.dimen.overlay_panel_max_width),
            resources.getDimensionPixelSize(R.dimen.overlay_panel_margin),
        )
        views.panel.panelCard.maxHeightPx = OverlayGeometry.panelMaxHeight(screenHeight, area.height)
        val params = views.panelWindow.params
        if (params.width != width) {
            params.width = width
            views.panelWindow.update()
        }
    }

    /** The panel's height: what is on screen when laid out, otherwise what it would measure to. */
    private fun panelHeight(views: Ui): Int {
        val root = views.panel.root
        val width = views.panelWindow.params.width
        if (root.isLaidOut && !root.isLayoutRequested && root.width == width && root.height > 0) return root.height
        root.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(max(1, area.height), View.MeasureSpec.AT_MOST),
        )
        return root.measuredHeight
    }

    private fun placePanel(views: Ui) {
        val params = views.panelWindow.params
        val height = panelHeight(views)
        val fraction = panelFraction
        val position = if (fraction != null) {
            OverlayGeometry.fromFraction(fraction, params.width, height, area)
        } else {
            val bubble = views.bubbleWindow.params
            OverlayGeometry.panelNear(
                bubble.x, bubble.y, views.bubbleSize, params.width, height, area,
                views.panel.root.resources.getDimensionPixelSize(R.dimen.overlay_panel_gap),
            )
        }
        if (params.x != position.x || params.y != position.y) {
            params.x = position.x
            params.y = position.y
            views.panelWindow.update()
        }
    }

    private fun movePanel(views: Ui, x: Int, y: Int) {
        val params = views.panelWindow.params
        val position = OverlayGeometry.clamp(x, y, params.width, panelHeight(views), area)
        params.x = position.x
        params.y = position.y
        views.panelWindow.update()
    }

    private fun savePanelPosition(views: Ui) {
        val params = views.panelWindow.params
        val fraction = OverlayGeometry.toFraction(params.x, params.y, params.width, panelHeight(views), area)
        panelFraction = fraction
        settings.panelPosition = fraction
    }

    private fun bindAutoButton(views: Ui) {
        views.panel.autoButton.setText(if (autoEnabled) R.string.overlay_auto_on else R.string.overlay_auto_off)
        views.panel.autoButton.isActivated = autoEnabled
    }

    private fun bindState(views: Ui) {
        bindBubble(views.bubble, state)
        bindPanel(views.panel, state)
    }

    private fun bindPanel(panel: OverlayPanelBinding, current: OverlayState) {
        val context = panel.root.context
        val advice = current as? OverlayState.Advice
        val board = when (current) {
            is OverlayState.Idle -> null
            is OverlayState.Working -> current.board
            is OverlayState.Advice -> current.board
            is OverlayState.Message -> current.board
        }
        val title: CharSequence = when (current) {
            is OverlayState.Idle -> context.getString(R.string.overlay_title_ready)
            is OverlayState.Working -> context.getString(R.string.overlay_title_working)
            is OverlayState.Advice -> current.title
            is OverlayState.Message -> current.title
        }
        val body = when (current) {
            is OverlayState.Idle -> current.hint
            is OverlayState.Working -> current.message
            is OverlayState.Advice -> current.explanation
            is OverlayState.Message -> current.body
        }
        val isError = current is OverlayState.Message && current.isError

        panel.panelTitle.text = title
        panel.panelTitle.setTextColor(
            ContextCompat.getColor(context, if (isError) R.color.overlay_error else R.color.overlay_text),
        )
        panel.panelProgress.isVisible = current is OverlayState.Working

        panel.summaryRow.isVisible = board != null || advice != null
        panel.summaryRow.gravity = if (advice != null) Gravity.CENTER_VERTICAL else Gravity.CENTER
        panel.miniBoard.isVisible = board != null
        panel.miniBoard.board = board
        panel.miniBoard.contentDescription = if (advice != null && advice.moveSan.isNotBlank()) {
            context.getString(R.string.overlay_mini_board_move_description, advice.moveSan)
        } else {
            context.getString(R.string.overlay_mini_board_description)
        }

        panel.moveColumn.isVisible = advice != null
        if (advice != null) {
            panel.moveText.text = advice.moveSan
            setTextOrGone(panel.evalChip, advice.evaluation)
            val chipColor = when (EvalTone.of(advice.evaluation)) {
                EvalTone.GOOD -> R.color.overlay_chip_good
                EvalTone.NEUTRAL -> R.color.overlay_chip_neutral
                EvalTone.BAD -> R.color.overlay_chip_bad
            }
            panel.evalChip.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, chipColor))
            setTextOrGone(panel.sourceText, advice.source)
        }

        setTextOrGone(panel.bodyText, body)
        setTextOrGone(panel.alternativesText, advice?.let { alternativesText(context, it.alternatives) })
        setTextOrGone(
            panel.threatText,
            advice?.threat?.takeIf { it.isNotBlank() }?.let { context.getString(R.string.overlay_threat, it.trim()) },
        )
        setTextOrGone(panel.footerText, advice?.footer)
        panel.panelScroll.scrollTo(0, 0)
    }

    private fun setTextOrGone(view: TextView, text: CharSequence?) {
        val shown = !text.isNullOrBlank()
        view.isVisible = shown
        view.text = if (shown) text else null
    }

    private fun alternativesText(context: Context, alternatives: List<Pair<String, String>>): CharSequence? {
        val usable = alternatives.filter { it.first.isNotBlank() }
        if (usable.isEmpty()) return null
        val text = SpannableStringBuilder(context.getString(R.string.overlay_alternatives_label))
        for ((san, note) in usable) {
            text.append('\n')
            val start = text.length
            text.append(san.trim())
            text.setSpan(StyleSpan(Typeface.BOLD), start, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (note.isNotBlank()) text.append(NOTE_SEPARATOR).append(note.trim())
        }
        return text
    }

    // ---- Board arrow and capture mode -----------------------------------------------------------

    private fun applyArrow(views: Ui) {
        val current = arrow
        views.arrowView.arrow = current
        if (current == null || !attached) {
            views.arrowWindow.hide()
            return
        }
        val alpha = arrowWindowAlpha()
        val params = views.arrowWindow.params
        if (params.alpha != alpha) {
            params.alpha = alpha
            views.arrowWindow.update()
        }
        views.arrowWindow.show()
    }

    private fun arrowWindowAlpha(): Float {
        val limit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                appContext.getSystemService(InputManager::class.java)?.maximumObscuringOpacityForTouch
            } catch (e: RuntimeException) {
                Log.w(TAG, "Could not read the obscuring-opacity limit", e)
                null
            }
        } else {
            null
        }
        return OverlayGeometry.arrowWindowAlpha(limit ?: Float.NaN)
    }

    private fun applyCaptureMode(views: Ui) {
        val alpha = if (captureMode) 0f else 1f
        views.bubble.root.alpha = alpha
        views.panel.root.alpha = alpha
        views.arrowView.alpha = alpha
    }

    // ---- Dragging -------------------------------------------------------------------------------

    /** Moves a window with the finger once the touch has travelled past the slop; otherwise it is a tap. */
    private class DragListener(
        private val slop: Int,
        private val params: WindowManager.LayoutParams,
        private val onDrag: (x: Int, y: Int) -> Unit,
        private val onDragEnd: () -> Unit,
    ) : View.OnTouchListener {
        private var downRawX = 0f
        private var downRawY = 0f
        private var startX = 0
        private var startY = 0
        private var dragging = false

        @SuppressLint("ClickableViewAccessibility") // performClick() is called for taps, below.
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX
                    downRawY = event.rawY
                    startX = params.x
                    startY = params.y
                    dragging = false
                    view.isPressed = true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downRawX
                    val dy = event.rawY - downRawY
                    if (!dragging && OverlayGeometry.isDrag(dx, dy, slop)) {
                        dragging = true
                        view.isPressed = false
                    }
                    if (dragging) onDrag(startX + dx.roundToInt(), startY + dy.roundToInt())
                }
                MotionEvent.ACTION_UP -> {
                    view.isPressed = false
                    if (dragging) onDragEnd() else view.performClick()
                    dragging = false
                }
                MotionEvent.ACTION_CANCEL -> {
                    view.isPressed = false
                    if (dragging) onDragEnd()
                    dragging = false
                }
                else -> return false
            }
            return true
        }
    }

    private companion object {
        const val TAG = "KrsOverlay"
        const val NOTE_SEPARATOR = " — "
    }
}

/** One window added through WindowManager. Add/update/remove never throw. */
internal class OverlayWindow(
    private val windowManager: WindowManager,
    val view: View,
    val params: WindowManager.LayoutParams,
    private val name: String,
) {
    var isAdded = false
        private set

    /** Adds the window in its current visibility. False when the system refused (e.g. no permission). */
    fun add(): Boolean {
        if (isAdded) return true
        return try {
            windowManager.addView(view, params)
            isAdded = true
            true
        } catch (e: RuntimeException) {
            // BadTokenException when "Display over other apps" was revoked, among others.
            Log.w(TAG, "Could not add the $name window", e)
            false
        }
    }

    /** Makes the window visible, adding it first when necessary. */
    fun show(): Boolean {
        view.visibility = View.VISIBLE
        return add()
    }

    /** Hides the window without removing it (a hidden window has no surface and blocks nothing). */
    fun hide() {
        view.visibility = View.GONE
    }

    /** Pushes changed [params] to the system. */
    fun update() {
        if (!isAdded) return
        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: RuntimeException) {
            // The window is gone; let the next show() add it again.
            Log.w(TAG, "Could not update the $name window", e)
            isAdded = false
        }
    }

    fun remove() {
        if (!isAdded) return
        isAdded = false
        try {
            windowManager.removeViewImmediate(view)
        } catch (e: RuntimeException) {
            Log.w(TAG, "Could not remove the $name window", e)
        }
    }

    private companion object {
        const val TAG = "KrsOverlay"
    }
}
