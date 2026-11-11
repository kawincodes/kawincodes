package com.krstech.chesshelp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.krstech.chesshelp.databinding.ActivityMainBinding
import com.krstech.chesshelp.overlay.BoardArrow
import com.krstech.chesshelp.overlay.MiniBoard
import com.krstech.chesshelp.overlay.OverlayController
import com.krstech.chesshelp.overlay.OverlayState
import com.krstech.chesshelp.settings.AppSettings
import com.krstech.chesshelp.settings.SecretStore
import com.krstech.chesshelp.ui.MainActivity
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowSettings
import org.robolectric.shadows.ShadowView

/**
 * The same flows on the oldest supported Android releases (minSdk 26 and Android 9), which take the
 * pre-Android-11 window layout path, have no notification runtime permission and use the plain
 * screen-capture intent.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [26, 28], qualifiers = "w360dp-h640dp-port-xhdpi")
class OldAndroidTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val session = RecordingSession()

    @BeforeTest
    fun setUp() {
        AppGraph.session = session
        val cipher = InMemoryCipher()
        SecretStore.cipherFactory = { cipher }
        ShadowSettings.setCanDrawOverlays(true)
    }

    @AfterTest
    fun tearDown() {
        AppGraph.session = UnwiredSessionController
    }

    private fun idle() {
        repeat(20) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `settings screen starts a session`() {
        SecretStore(context).saveApiKey("sk-ant-api03-TEST-ONLY")
        AppSettings(context).disclosureAccepted = true
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        idle()
        val activity = controller.get()
        val b = ActivityMainBinding.bind(activity.findViewById(R.id.root))
        assertEquals("sk-ant-api03-TEST-ONLY", b.apiKeyInput.text.toString())
        assertEquals(context.getString(R.string.status_ready), b.sessionStatus.text.toString())

        // No runtime notification permission here: the button leads to the notification settings.
        b.notificationButton.performClick()
        val settingsIntent = shadowOf(activity).nextStartedActivity
        assertEquals(Settings.ACTION_APP_NOTIFICATION_SETTINGS, settingsIntent.action)
        assertEquals("com.krstech.chesshelp", settingsIntent.getStringExtra(Settings.EXTRA_APP_PACKAGE))

        b.startButton.performClick()
        idle()
        val request = assertNotNull(shadowOf(activity).nextStartedActivityForResult)
        shadowOf(activity).receiveResult(request.intent, Activity.RESULT_OK, Intent())
        idle()
        assertNotNull(session.started)
        assertEquals("Stop", b.startButton.text.toString())
        snapshotAsIs(b.root, "main-small-old-android")
        controller.pause().stop().destroy()
    }

    @Test
    fun `overlay attaches, renders, drags and detaches`() {
        val events = ArrayList<String>()
        val overlay = OverlayController(
            context,
            object : OverlayController.Listener {
                override fun onAnalyze() { events += "analyze" }
                override fun onRescan() { events += "rescan" }
                override fun onToggleSideToMove() { events += "side" }
                override fun onAutoChanged(enabled: Boolean) { events += "auto=$enabled" }
                override fun onFlipBoard() { events += "flip" }
                override fun onStop() { events += "stop" }
            },
        )
        val start = listOf("rnbqkbnr", "pppppppp", "........", "........", "........", "........", "PPPPPPPP", "RNBQKBNR")
        overlay.render(
            OverlayState.Advice(
                "White to move", "e4", "Equal", "Takes the centre.", listOf("d4" to "also principled"), null,
                "Local engine", MiniBoard(start, 6, 4, 4, 4), null,
            ),
        )
        overlay.attach()
        idle()
        assertTrue(overlay.isAttached)

        val roots = windowRoots()
        assertEquals(3, roots.size)
        val bubble = roots.first { it.id == R.id.bubbleRoot }
        val panel = roots.first { it.id == R.id.panelRoot }
        val dm = context.resources.displayMetrics
        val bubbleParams = bubble.layoutParams as WindowManager.LayoutParams
        assertEquals(dm.widthPixels - bubbleParams.width, bubbleParams.x)
        // Kept below the status bar even though its height cannot be queried on these releases.
        assertTrue(bubbleParams.y >= context.resources.getDimensionPixelSize(R.dimen.overlay_legacy_status_bar))

        bubble.performClick()
        idle()
        assertEquals(listOf("analyze"), events)
        assertEquals(View.VISIBLE, panel.visibility)
        assertEquals("e4", panel.findViewById<TextView>(R.id.moveText).text.toString())
        val panelParams = panel.layoutParams as WindowManager.LayoutParams
        // 360dp wide: the 300dp panel does not fit beside the bubble, so it sits below it.
        assertTrue(panelParams.y >= bubbleParams.y + bubbleParams.height, "panel y ${panelParams.y}")
        assertTrue(panelParams.x >= 0 && panelParams.x + panelParams.width <= dm.widthPixels)
        assertTrue(panel.height <= (dm.heightPixels * 0.6f).toInt(), "panel height ${panel.height}")
        snapshotAsIs(panel, "panel-small-old-android")

        overlay.showBoardArrow(BoardArrow(0, 300, 720, 6, 4, 4, 4))
        overlay.setCaptureMode(true)
        roots.forEach { assertEquals(0f, it.alpha) }
        overlay.setCaptureMode(false)
        overlay.onConfigurationChanged()
        idle()

        ShadowView.clickOn(panel.findViewById(R.id.stopButton))
        assertEquals(listOf("analyze", "stop"), events)
        overlay.detach()
        idle()
        assertTrue(windowRoots().isEmpty())
    }
}
