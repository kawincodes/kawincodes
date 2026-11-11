package com.krstech.chesshelp.overlay

import android.content.Context
import android.content.ContextWrapper
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.inspector.WindowInspector
import android.view.WindowManager
import android.widget.ScrollView
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.krstech.chesshelp.R
import com.krstech.chesshelp.settings.AppSettings
import com.krstech.chesshelp.settings.FractionPoint
import com.krstech.chesshelp.snapshotAsIs
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowSettings

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-port-xxhdpi")
class OverlayControllerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val events = ArrayList<String>()
    private val listener = object : OverlayController.Listener {
        override fun onAnalyze() { events += "analyze" }
        override fun onRescan() { events += "rescan" }
        override fun onToggleSideToMove() { events += "side" }
        override fun onAutoChanged(enabled: Boolean) { events += "auto=$enabled" }
        override fun onFlipBoard() { events += "flip" }
        override fun onStop() { events += "stop" }
    }
    private lateinit var controller: OverlayController

    private val start = listOf("rnbqkbnr", "pppppppp", "........", "........", "....P...", "........", "PPPP.PPP", "RNBQKBNR")
    private val advice = OverlayState.Advice(
        title = "Black to move",
        moveSan = "Nf6",
        evaluation = "Equal",
        explanation = "Develops the knight and attacks the e4 pawn, the most flexible reply.",
        alternatives = listOf("e5" to "claims the centre", "c5" to "Sicilian, sharper", "" to "ignored"),
        threat = "White is ready to push e5.",
        source = "Claude Opus 5.5",
        board = MiniBoard(start, 0, 6, 2, 5),
        footer = "Tracked locally · move 1",
    )

    @BeforeTest
    fun setUp() {
        ShadowSettings.setCanDrawOverlays(true)
        controller = OverlayController(context, listener)
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun roots(): List<View> = WindowInspector.getGlobalWindowViews()

    private fun params(view: View) = view.layoutParams as WindowManager.LayoutParams

    private fun bubble(): View = roots().first { it.id == R.id.bubbleRoot }
    private fun panel(): View = roots().first { it.id == R.id.panelRoot }
    private fun arrowView(): BoardArrowView = roots().filterIsInstance<BoardArrowView>().first()
    private fun text(root: View, id: Int): String = root.findViewById<TextView>(id).text.toString()

    private fun attach() {
        controller.attach()
        idle()
    }

    private fun touch(view: View, action: Int, x: Float, y: Float, downTime: Long) {
        val event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x, y, 0)
        view.dispatchTouchEvent(event)
        event.recycle()
    }

    private fun drag(view: View, fromX: Float, fromY: Float, toX: Float, toY: Float) {
        val down = SystemClock.uptimeMillis()
        touch(view, MotionEvent.ACTION_DOWN, fromX, fromY, down)
        for (i in 1..5) touch(view, MotionEvent.ACTION_MOVE, fromX + (toX - fromX) * i / 5, fromY + (toY - fromY) * i / 5, down)
        touch(view, MotionEvent.ACTION_UP, toX, toY, down)
        idle()
    }

    @Test
    fun `attach shows only the bubble, in overlay windows that cannot take focus`() {
        assertFalse(controller.isAttached)
        attach()
        assertTrue(controller.isAttached)
        assertEquals(3, roots().size)
        assertEquals(View.VISIBLE, bubble().visibility)
        assertEquals(View.GONE, panel().visibility)
        assertEquals(View.GONE, arrowView().visibility)
        for (root in roots()) {
            val lp = params(root)
            assertEquals(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, lp.type)
            assertTrue(lp.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE != 0)
            assertTrue(lp.flags and WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN != 0)
            assertEquals(0, lp.windowAnimations)
        }
        val arrow = params(arrowView())
        assertTrue(arrow.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE != 0)
        assertTrue(arrow.flags and WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS != 0)
        assertEquals(WindowManager.LayoutParams.MATCH_PARENT, arrow.width)
        assertEquals(WindowManager.LayoutParams.MATCH_PARENT, arrow.height)
        assertEquals(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS, arrow.layoutInDisplayCutoutMode)
        assertEquals(0, params(bubble()).flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        // Attaching twice adds nothing.
        attach()
        assertEquals(3, roots().size)
    }

    @Test
    fun `bubble starts at its default spot inside the screen`() {
        attach()
        val lp = params(bubble())
        val size = context.resources.getDimensionPixelSize(R.dimen.overlay_bubble_size)
        val dm = context.resources.displayMetrics
        assertEquals(size, lp.width)
        assertEquals(dm.widthPixels - size, lp.x)
        assertTrue(lp.y > 0 && lp.y + size < dm.heightPixels, "y=${lp.y}")
    }

    @Test
    fun `tapping the bubble analyses and opens the panel, which can be hidden again`() {
        attach()
        controller.render(OverlayState.Idle("Open a chess board, then tap the knight."))
        bubble().performClick()
        idle()
        assertEquals(listOf("analyze"), events)
        assertEquals(View.VISIBLE, panel().visibility)
        assertEquals("Ready", text(panel(), R.id.panelTitle))
        assertEquals("Open a chess board, then tap the knight.", text(panel(), R.id.bodyText))
        assertEquals(View.GONE, panel().findViewById<View>(R.id.summaryRow).visibility)
        snapshotAsIs(panel(), "panel-idle")

        panel().findViewById<View>(R.id.hideButton).performClick()
        idle()
        assertEquals(View.GONE, panel().visibility)
        assertEquals(listOf("analyze"), events)
    }

    @Test
    fun `a touch that stays within the slop is a tap, a longer one drags and is remembered`() {
        attach()
        val bubble = bubble()
        val before = params(bubble).x to params(bubble).y
        drag(bubble, 30f, 30f, 33f, 34f)
        assertEquals(listOf("analyze"), events)
        assertEquals(before, params(bubble).x to params(bubble).y)

        events.clear()
        drag(bubble, 30f, 30f, -470f, 430f)
        assertTrue(events.isEmpty(), "a drag must not analyse: $events")
        assertEquals(before.first - 500, params(bubble).x)
        assertEquals(before.second + 400, params(bubble).y)
        val stored = AppSettings(context).bubblePosition
        assertTrue(stored.x in 0.4f..0.7f && stored.y > AppSettings.DEFAULT_BUBBLE_POSITION.y, "$stored")

        // Dragging far outside keeps it on screen.
        drag(bubble, 30f, 30f, -5000f, 9000f)
        val size = params(bubble).width
        val dm = context.resources.displayMetrics
        assertEquals(0, params(bubble).x)
        assertTrue(params(bubble).y + size <= dm.heightPixels, "y=${params(bubble).y}")
        assertEquals(0f, AppSettings(context).bubblePosition.x)
        assertEquals(1f, AppSettings(context).bubblePosition.y)

        // A new controller (next session) puts it back where it was left.
        controller.detach()
        idle()
        assertTrue(roots().isEmpty())
        controller = OverlayController(context, listener)
        attach()
        assertEquals(0, params(bubble()).x)
    }

    @Test
    fun `advice fills the panel and the bubble`() {
        attach()
        bubble().performClick()
        controller.render(advice)
        idle()
        val panel = panel()
        assertEquals("Black to move", text(panel, R.id.panelTitle))
        assertEquals("Nf6", text(panel, R.id.moveText))
        assertEquals("Equal", text(panel, R.id.evalChip))
        assertEquals("Claude Opus 5.5", text(panel, R.id.sourceText))
        assertEquals(advice.explanation, text(panel, R.id.bodyText))
        assertEquals("Also good\ne5 — claims the centre\nc5 — Sicilian, sharper", text(panel, R.id.alternativesText))
        assertEquals("Watch out: White is ready to push e5.", text(panel, R.id.threatText))
        assertEquals("Tracked locally · move 1", text(panel, R.id.footerText))
        assertEquals(View.GONE, panel.findViewById<View>(R.id.panelProgress).visibility)
        val board = panel.findViewById<MiniBoardView>(R.id.miniBoard)
        assertEquals(View.VISIBLE, board.visibility)
        assertEquals(advice.board, board.board)
        assertTrue(board.width > 0 && board.width == board.height)
        assertEquals("Nf6", text(bubble(), R.id.bubbleLabel))
        assertEquals(View.VISIBLE, bubble().findViewById<View>(R.id.bubbleLabel).visibility)
        assertEquals(View.GONE, bubble().findViewById<View>(R.id.bubbleIcon).visibility)
        snapshotAsIs(panel, "panel-advice")
        snapshotAsIs(bubble(), "bubble-advice")
        snapshotAsIs(board, "miniboard")
    }

    @Test
    fun `optional advice parts disappear when empty`() {
        attach()
        bubble().performClick()
        controller.render(advice.copy(evaluation = " ", alternatives = emptyList(), threat = null, footer = "", board = null))
        idle()
        val panel = panel()
        for (id in listOf(R.id.evalChip, R.id.alternativesText, R.id.threatText, R.id.footerText, R.id.miniBoard)) {
            assertEquals(View.GONE, panel.findViewById<View>(id).visibility)
        }
        assertEquals(View.VISIBLE, panel.findViewById<View>(R.id.moveText).visibility)
        snapshotAsIs(panel, "panel-advice-minimal")
    }

    @Test
    fun `working and message states`() {
        attach()
        bubble().performClick()
        controller.render(OverlayState.Working("Reading the board…", MiniBoard(start, null, null, null, null)))
        idle()
        val panel = panel()
        assertEquals("Working…", text(panel, R.id.panelTitle))
        assertEquals(View.VISIBLE, panel.findViewById<View>(R.id.panelProgress).visibility)
        assertEquals(View.VISIBLE, bubble().findViewById<View>(R.id.bubbleSpinner).visibility)
        assertEquals(View.GONE, panel.findViewById<View>(R.id.moveColumn).visibility)
        assertEquals(View.VISIBLE, panel.findViewById<View>(R.id.miniBoard).visibility)
        snapshotAsIs(panel, "panel-working")

        controller.render(OverlayState.Message("Invalid API key", "Check the key in the app settings.", isError = true))
        idle()
        assertEquals("Invalid API key", text(panel, R.id.panelTitle))
        assertEquals(context.getColor(R.color.overlay_error), panel.findViewById<TextView>(R.id.panelTitle).currentTextColor)
        assertEquals(View.GONE, bubble().findViewById<View>(R.id.bubbleSpinner).visibility)
        assertEquals("!", text(bubble(), R.id.bubbleLabel))
        snapshotAsIs(panel, "panel-error")
        snapshotAsIs(bubble(), "bubble-error")

        controller.render(OverlayState.Message("No board found", "Open a chess app.", isError = false))
        idle()
        assertEquals(context.getColor(R.color.overlay_text), panel.findViewById<TextView>(R.id.panelTitle).currentTextColor)
        assertEquals(View.VISIBLE, bubble().findViewById<View>(R.id.bubbleIcon).visibility)
        assertEquals(View.GONE, bubble().findViewById<View>(R.id.bubbleLabel).visibility)
    }

    @Test
    fun `state rendered before attach, or while the panel is hidden, is shown later`() {
        controller.render(advice)
        attach()
        assertEquals("Nf6", text(bubble(), R.id.bubbleLabel))
        assertEquals(View.GONE, panel().visibility)
        bubble().performClick()
        idle()
        assertEquals("Nf6", text(panel(), R.id.moveText))
    }

    @Test
    fun `panel buttons reach the listener`() {
        attach()
        bubble().performClick()
        events.clear()
        val panel = panel()
        panel.findViewById<View>(R.id.rescanButton).performClick()
        panel.findViewById<View>(R.id.switchTurnButton).performClick()
        panel.findViewById<View>(R.id.flipButton).performClick()
        panel.findViewById<View>(R.id.stopButton).performClick()
        assertEquals(listOf("rescan", "side", "flip", "stop"), events)
    }

    @Test
    fun `auto button toggles and reports, setAutoEnabled only displays`() {
        AppSettings(context).autoAdvise = false
        controller = OverlayController(context, listener)
        attach()
        val auto = panel().findViewById<TextView>(R.id.autoButton)
        assertEquals("Auto: off", auto.text.toString())
        assertFalse(auto.isActivated)
        controller.setAutoEnabled(true)
        assertEquals("Auto: on", auto.text.toString())
        assertTrue(auto.isActivated)
        assertTrue(events.isEmpty())
        auto.performClick()
        assertEquals(listOf("auto=false"), events)
        assertEquals("Auto: off", auto.text.toString())
        auto.performClick()
        assertEquals(listOf("auto=false", "auto=true"), events)
    }

    @Test
    fun `capture mode makes every window transparent and back`() {
        attach()
        bubble().performClick()
        controller.render(advice)
        controller.showBoardArrow(BoardArrow(0, 700, 1232, 0, 6, 2, 5))
        idle()
        controller.setCaptureMode(true)
        assertEquals(3, roots().size)
        for (root in roots()) assertEquals(0f, root.alpha, "$root")
        // Updates while hidden do not reveal anything.
        controller.render(OverlayState.Working("x"))
        controller.showBoardArrow(BoardArrow(0, 700, 1232, 1, 4, 3, 4))
        for (root in roots()) assertEquals(0f, root.alpha, "$root")
        controller.setCaptureMode(false)
        for (root in roots()) assertEquals(1f, root.alpha, "$root")
        assertEquals(View.VISIBLE, arrowView().visibility)
    }

    @Test
    fun `capture mode set before attach applies on attach`() {
        controller.setCaptureMode(true)
        attach()
        for (root in roots()) assertEquals(0f, root.alpha)
    }

    @Test
    fun `board arrow window lets touches through and hides when cleared`() {
        attach()
        val arrow = BoardArrow(0, 700, 1232, 6, 4, 4, 4)
        controller.showBoardArrow(arrow)
        idle()
        val view = arrowView()
        assertEquals(View.VISIBLE, view.visibility)
        assertEquals(arrow, view.arrow)
        val alpha = params(view).alpha
        assertTrue(alpha > 0.5f && alpha < 0.8f, "window alpha $alpha must stay below the 0.8 pass-through limit")
        snapshotAsIs(view, "board-arrow")

        controller.showBoardArrow(null)
        assertEquals(View.GONE, view.visibility)
        assertNull(view.arrow)
        // Nonsense input is treated as "no arrow".
        controller.showBoardArrow(BoardArrow(0, 0, 0, 1, 1, 2, 2))
        assertEquals(View.GONE, view.visibility)
        controller.showBoardArrow(BoardArrow(0, 0, 800, 1, 1, 9, 2))
        assertEquals(View.GONE, view.visibility)
    }

    @Test
    fun `panel is at most 300dp wide and sixty percent of the screen high, and scrolls`() {
        attach()
        bubble().performClick()
        val longText = (1..60).joinToString(" ") { "This explanation is deliberately far too long." }
        controller.render(advice.copy(explanation = longText))
        idle()
        val panel = panel()
        val dm = context.resources.displayMetrics
        assertTrue(panel.width <= (300 * dm.density).toInt() + 1, "width ${panel.width}")
        assertTrue(panel.height <= (dm.heightPixels * 0.6f).toInt(), "height ${panel.height} of ${dm.heightPixels}")
        assertTrue(panel.height > dm.heightPixels / 2)
        val scroll = panel.findViewById<ScrollView>(R.id.panelScroll)
        assertTrue(scroll.getChildAt(0).height > scroll.height, "content must scroll")
        // The action buttons stay visible below the scrolling part.
        val stop = panel.findViewById<View>(R.id.stopButton)
        assertTrue(stop.height > 0 && stop.isShown)
        val lp = params(panel)
        assertTrue(lp.x >= 0 && lp.x + panel.width <= dm.widthPixels, "x ${lp.x}")
        assertTrue(lp.y >= 0 && lp.y + panel.height <= dm.heightPixels, "y ${lp.y}")
        snapshotAsIs(panel, "panel-long")
    }

    @Test
    fun `panel can be dragged by its header and stays where it was put`() {
        attach()
        bubble().performClick()
        controller.render(advice)
        idle()
        val panel = panel()
        val startX = params(panel).x
        val startY = params(panel).y
        drag(panel.findViewById(R.id.panelHeader), 40f, 20f, 40f - 100f, 20f + 300f)
        assertEquals(startX - 100, params(panel).x)
        assertEquals(startY + 300, params(panel).y)
        assertNotNull(AppSettings(context).panelPosition)
        // Moving the bubble no longer drags the panel along.
        val placed = params(panel).x to params(panel).y
        drag(bubble(), 30f, 30f, -300f, 30f)
        assertEquals(placed, params(panel).x to params(panel).y)
        // Far off-screen drags are clamped.
        drag(panel.findViewById(R.id.panelHeader), 40f, 20f, 9000f, 9000f)
        val dm = context.resources.displayMetrics
        assertTrue(params(panel).x + panel.width <= dm.widthPixels)
        assertTrue(params(panel).y + panel.height <= dm.heightPixels)
    }

    @Test
    fun `an unplaced panel follows the bubble`() {
        attach()
        bubble().performClick()
        controller.render(advice)
        idle()
        val before = params(panel()).x to params(panel()).y
        drag(bubble(), 30f, 30f, -600f, 630f)
        assertTrue(before != params(panel()).x to params(panel()).y)
        assertNull(AppSettings(context).panelPosition)
    }

    @Test
    fun `rotation keeps bubble and panel on screen and drops the stale arrow`() {
        AppSettings(context).bubblePosition = FractionPoint(1f, 1f)
        controller = OverlayController(context, listener)
        attach()
        bubble().performClick()
        controller.render(advice)
        controller.showBoardArrow(BoardArrow(0, 700, 1232, 6, 4, 4, 4))
        idle()
        RuntimeEnvironment.setQualifiers("+land")
        controller.onConfigurationChanged()
        idle()
        val dm = context.resources.displayMetrics
        assertTrue(dm.widthPixels > dm.heightPixels)
        val bubble = params(bubble())
        assertEquals(dm.widthPixels - bubble.width, bubble.x)
        assertTrue(bubble.y + bubble.height <= dm.heightPixels, "bubble y ${bubble.y}")
        val panel = panel()
        assertTrue(params(panel).x >= 0 && params(panel).x + panel.width <= dm.widthPixels)
        assertTrue(params(panel).y >= 0 && params(panel).y + panel.height <= dm.heightPixels, "panel y ${params(panel).y} h ${panel.height}")
        assertTrue(panel.height <= (dm.heightPixels * 0.6f).toInt(), "panel height ${panel.height}")
        assertEquals(View.GONE, arrowView().visibility)
        snapshotAsIs(panel, "panel-landscape")
    }

    @Test
    fun `detach removes everything and attach works again`() {
        attach()
        bubble().performClick()
        controller.showBoardArrow(BoardArrow(0, 700, 1232, 6, 4, 4, 4))
        idle()
        controller.detach()
        idle()
        assertTrue(roots().isEmpty())
        assertFalse(controller.isAttached)
        controller.detach()
        // Calls while detached are harmless.
        controller.render(advice)
        controller.setCaptureMode(true)
        controller.setCaptureMode(false)
        controller.setAutoEnabled(true)
        controller.onConfigurationChanged()
        attach()
        assertEquals(3, roots().size)
        assertEquals(View.VISIBLE, panel().visibility)
        assertEquals(View.VISIBLE, arrowView().visibility)
        assertEquals("Nf6", text(panel(), R.id.moveText))
    }

    @Test
    fun `calls from a background thread are moved to the main thread`() {
        val worker = Thread {
            controller.attach()
            controller.render(advice)
            controller.setCaptureMode(true)
        }
        worker.start()
        worker.join()
        assertTrue(roots().isEmpty())
        idle()
        assertEquals(3, roots().size)
        assertEquals("Nf6", text(bubble(), R.id.bubbleLabel))
        assertEquals(0f, bubble().alpha)
    }

    // ---- A WindowManager that refuses everything: revoked permission / dead windows ---------------

    private class RefusingContext(base: Context, private val failure: () -> RuntimeException) : ContextWrapper(base) {
        var calls = 0
        private val windowManager = Proxy.newProxyInstance(
            WindowManager::class.java.classLoader,
            arrayOf(WindowManager::class.java),
        ) { _, method, args ->
            when (method.name) {
                "addView", "updateViewLayout", "removeView", "removeViewImmediate" -> {
                    calls++
                    throw failure()
                }
                else -> try {
                    method.invoke(base.getSystemService(WINDOW_SERVICE), *(args ?: emptyArray()))
                } catch (e: InvocationTargetException) {
                    throw e.targetException
                }
            }
        }

        override fun getApplicationContext(): Context = this
        override fun getSystemService(name: String): Any? =
            if (name == WINDOW_SERVICE) windowManager else super.getSystemService(name)
    }

    @Test
    fun `a revoked overlay permission never crashes`() {
        val refusing = RefusingContext(context) {
            WindowManager.BadTokenException("Unable to add window -- permission denied for window type 2038")
        }
        val guarded = OverlayController(refusing, listener)
        guarded.attach()
        idle()
        assertFalse(guarded.isAttached)
        assertTrue(refusing.calls > 0)
        assertTrue(roots().isEmpty())
        guarded.render(advice)
        guarded.showBoardArrow(BoardArrow(0, 700, 1232, 6, 4, 4, 4))
        guarded.setCaptureMode(true)
        guarded.setAutoEnabled(false)
        guarded.onConfigurationChanged()
        guarded.attach()
        guarded.detach()
        idle()
    }

    @Test
    fun `window wrapper survives add, update and remove failures`() {
        var mode = "ok"
        val log = ArrayList<String>()
        val wm = Proxy.newProxyInstance(WindowManager::class.java.classLoader, arrayOf(WindowManager::class.java)) { _, method, _ ->
            log += method.name
            if (mode == "dead" && method.name != "addView") throw IllegalArgumentException("View not attached to window manager")
            if (mode == "denied") throw SecurityException("denied")
            null
        } as WindowManager
        val window = OverlayWindow(wm, View(context), WindowManager.LayoutParams(), "test")
        window.update()
        window.remove()
        assertTrue(log.isEmpty(), "nothing to do before the window is added")
        mode = "denied"
        assertFalse(window.add())
        assertFalse(window.isAdded)
        mode = "ok"
        assertTrue(window.show())
        assertTrue(window.isAdded)
        assertTrue(window.add())
        assertEquals(2, log.count { it == "addView" })
        mode = "dead"
        window.update()
        assertFalse(window.isAdded, "a dead window is forgotten so that it can be added again")
        assertTrue(window.show())
        window.remove()
        assertFalse(window.isAdded)
        window.remove()
        assertEquals(1, log.count { it == "removeViewImmediate" })
    }
}
