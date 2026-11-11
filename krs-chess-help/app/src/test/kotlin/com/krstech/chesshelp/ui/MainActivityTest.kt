package com.krstech.chesshelp.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ApplicationProvider
import com.krstech.chesshelp.AppGraph
import com.krstech.chesshelp.InMemoryCipher
import com.krstech.chesshelp.R
import com.krstech.chesshelp.RecordingSession
import com.krstech.chesshelp.UnwiredSessionController
import com.krstech.chesshelp.databinding.ActivityMainBinding
import com.krstech.chesshelp.settings.AdviseForSetting
import com.krstech.chesshelp.settings.AppSettings
import com.krstech.chesshelp.settings.EffortSetting
import com.krstech.chesshelp.settings.SecretStore
import com.krstech.chesshelp.snapshotAsIs
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDialog
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowSettings
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class MainActivityTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val session = RecordingSession()
    private val cipher = InMemoryCipher()
    private val key = "sk-ant-api03-TEST-ONLY-abcdefghijklmnop"
    private lateinit var controller: ActivityController<MainActivity>

    @BeforeTest
    fun setUp() {
        AppGraph.session = session
        SecretStore.cipherFactory = { cipher }
        ShadowSettings.setCanDrawOverlays(true)
    }

    @AfterTest
    fun tearDown() {
        AppGraph.session = UnwiredSessionController
    }

    private fun launch(): MainActivity {
        controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        idle()
        return controller.get()
    }

    /** Lets Dispatchers.IO work finish and runs what it posted back to the main thread. */
    private fun idle() {
        repeat(20) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun binding(activity: MainActivity) = ActivityMainBinding.bind(activity.findViewById(R.id.root))

    private fun latestDialog(): AlertDialog = assertNotNull(ShadowDialog.getLatestDialog() as? AlertDialog)

    private fun dialogTitle(dialog: AlertDialog): String =
        dialog.findViewById<android.widget.TextView>(androidx.appcompat.R.id.alertTitle)?.text.toString()

    @Test
    fun `first launch shows defaults and is secure`() {
        val activity = launch()
        val b = binding(activity)
        assertEquals("", b.apiKeyInput.text.toString())
        assertEquals(context.getString(R.string.api_key_status_none), b.apiKeyStatus.text.toString())
        assertTrue(b.modelValue.text.startsWith("Claude Opus 5.5"))
        assertEquals("Medium", b.effortValue.text.toString())
        assertEquals("Side at the bottom", b.adviseForValue.text.toString())
        assertTrue(b.autoAdviseSwitch.isChecked && b.drawOnBoardSwitch.isChecked && b.sendScreenSwitch.isChecked)
        assertEquals("Start", b.startButton.text.toString())
        assertEquals(context.getString(R.string.status_need_key), b.sessionStatus.text.toString())
        assertTrue(activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE != 0)
        assertEquals(View.GONE, b.overlayButton.visibility)
        snapshotAsIs(b.root, "main-first-launch")
    }

    @Test
    fun `saving a key encrypts it and survives a relaunch`() {
        val b = binding(launch())
        b.apiKeyInput.setText("  $key ")
        b.saveKeyButton.performClick()
        idle()
        assertEquals(context.getString(R.string.api_key_status_saved), b.apiKeyStatus.text.toString())
        assertEquals(key, SecretStore(context).loadApiKey())
        val raw = context.getSharedPreferences(SecretStore.PREFS_NAME, Context.MODE_PRIVATE).all.values.joinToString()
        assertFalse(raw.contains("sk-ant"), raw)
        assertEquals(context.getString(R.string.status_ready), b.sessionStatus.text.toString())

        val relaunched = binding(launch())
        assertEquals(key, relaunched.apiKeyInput.text.toString())
        assertEquals(context.getString(R.string.api_key_status_saved), relaunched.apiKeyStatus.text.toString())

        relaunched.apiKeyInput.setText("")
        relaunched.saveKeyButton.performClick()
        idle()
        assertEquals(context.getString(R.string.api_key_status_removed), relaunched.apiKeyStatus.text.toString())
        assertNull(SecretStore(context).loadApiKey())
    }

    @Test
    fun `a device that cannot encrypt reports it and stores nothing`() {
        SecretStore.cipherFactory = {
            object : com.krstech.chesshelp.settings.SecretCipher {
                override fun encrypt(plain: ByteArray) = throw java.security.KeyStoreException("no keystore")
                override fun decrypt(sealed: com.krstech.chesshelp.settings.SealedBytes) = throw java.security.KeyStoreException("no keystore")
                override fun reset() = Unit
            }
        }
        val b = binding(launch())
        b.apiKeyInput.setText(key)
        b.saveKeyButton.performClick()
        idle()
        assertEquals(context.getString(R.string.api_key_status_save_failed), b.apiKeyStatus.text.toString())
        assertTrue(context.getSharedPreferences(SecretStore.PREFS_NAME, Context.MODE_PRIVATE).all.isEmpty())
    }

    @Test
    fun `test connection uses the typed key, model and base url and shows the result`() {
        val settings = AppSettings(context)
        settings.modelId = "claude-sonnet-5-5"
        val b = binding(launch())
        b.baseUrlInput.setText("https://gateway.example.com/")
        b.apiKeyInput.setText(key)
        b.testButton.performClick()
        idle()
        assertEquals(listOf(Triple(key, "claude-sonnet-5-5", "https://gateway.example.com")), session.testCalls.toList())
        assertEquals("Connection OK: claude-opus-5-5", b.apiKeyStatus.text.toString())
        assertTrue(b.testButton.isEnabled)

        session.testResult = Result.failure(IllegalStateException("401 invalid x-api-key $key"))
        b.testButton.performClick()
        idle()
        val shown = b.apiKeyStatus.text.toString()
        assertTrue(shown.startsWith("Connection failed: 401 invalid x-api-key"), shown)
        assertFalse(shown.contains(key), "the key must never be echoed")
    }

    @Test
    fun `test connection without any key asks for one`() {
        val b = binding(launch())
        b.testButton.performClick()
        idle()
        assertTrue(session.testCalls.isEmpty())
        assertEquals(context.getString(R.string.api_key_status_empty), b.apiKeyStatus.text.toString())
    }

    @Test
    fun `the placeholder controller explains itself`() {
        AppGraph.session = UnwiredSessionController
        val b = binding(launch())
        b.apiKeyInput.setText(key)
        b.testButton.performClick()
        idle()
        assertEquals(
            context.getString(R.string.api_key_status_test_failed, context.getString(R.string.session_not_wired)),
            b.apiKeyStatus.text.toString(),
        )
    }

    @Test
    fun `pickers store the choice`() {
        val b = binding(launch())
        b.modelRow.performClick()
        var dialog = latestDialog()
        assertEquals(4, dialog.listView.count)
        assertTrue(dialog.listView.adapter.getItem(3).toString().startsWith("Claude Fable 5.2"))
        shadowOf(dialog.listView).performItemClick(2)
        idle()
        assertEquals("claude-haiku-5-5", AppSettings(context).modelId)
        assertTrue(b.modelValue.text.startsWith("Claude Haiku 5.5"))

        b.effortRow.performClick()
        dialog = latestDialog()
        assertEquals(listOf("Low", "Medium", "High"), (0 until 3).map { dialog.listView.adapter.getItem(it).toString() })
        shadowOf(dialog.listView).performItemClick(2)
        idle()
        assertEquals(EffortSetting.HIGH, AppSettings(context).effort)
        assertEquals("High", b.effortValue.text.toString())

        b.adviseForRow.performClick()
        dialog = latestDialog()
        assertEquals(
            listOf("Side at the bottom", "White", "Black", "Both"),
            (0 until 4).map { dialog.listView.adapter.getItem(it).toString() },
        )
        shadowOf(dialog.listView).performItemClick(3)
        idle()
        assertEquals(AdviseForSetting.BOTH, AppSettings(context).adviseFor)
        assertEquals("Both", b.adviseForValue.text.toString())
    }

    @Test
    fun `switches and base url are stored`() {
        val b = binding(launch())
        b.autoAdviseSwitch.performClick()
        b.drawOnBoardSwitch.performClick()
        b.sendScreenSwitch.performClick()
        b.baseUrlInput.setText("http://not-secure.example.com")
        val settings = AppSettings(context)
        assertFalse(settings.autoAdvise)
        assertFalse(settings.drawOnBoard)
        assertFalse(settings.sendScreenContext)
        assertEquals("", settings.customBaseUrl)
        assertEquals(context.getString(R.string.base_url_error), b.baseUrlLayout.error.toString())
        b.baseUrlInput.setText("https://ok.example.com")
        assertNull(b.baseUrlLayout.error)
        assertEquals("https://ok.example.com", settings.customBaseUrl)
        b.baseUrlInput.setText("")
        assertEquals("", settings.customBaseUrl)
    }

    @Test
    fun `start without overlay permission explains and opens the settings page`() {
        ShadowSettings.setCanDrawOverlays(false)
        val activity = launch()
        val b = binding(activity)
        assertEquals(context.getString(R.string.status_need_overlay), b.sessionStatus.text.toString())
        assertEquals(View.VISIBLE, b.overlayButton.visibility)
        b.startButton.performClick()
        idle()
        val dialog = latestDialog()
        assertEquals(context.getString(R.string.dialog_overlay_title), dialogTitle(dialog))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        idle()
        val intent = shadowOf(activity).nextStartedActivity
        assertEquals(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, intent.action)
        assertEquals("package:com.krstech.chesshelp", intent.dataString)
        assertNull(session.started)

        // Granted while away: the row updates when the user comes back.
        ShadowSettings.setCanDrawOverlays(true)
        controller.pause().resume()
        assertEquals(View.GONE, b.overlayButton.visibility)
        assertEquals(context.getString(R.string.perm_overlay_granted), b.overlayStatus.text.toString())
    }

    @Test
    fun `full first start - local engine choice, disclosure, capture consent, running state, stop`() {
        val activity = launch()
        val b = binding(activity)
        b.startButton.performClick()
        idle()

        var dialog = latestDialog()
        assertEquals(context.getString(R.string.dialog_no_key_title), dialogTitle(dialog))
        assertEquals("Use local engine only", dialog.getButton(AlertDialog.BUTTON_NEGATIVE).text.toString())
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
        idle()

        dialog = latestDialog()
        assertEquals(context.getString(R.string.disclosure_title), dialogTitle(dialog))
        val message = dialog.findViewById<android.widget.TextView>(android.R.id.message)!!.text.toString()
        for (needle in listOf("captures your screen", "Anthropic", "your own API key", "costs money", "Fair play", "account closed")) {
            assertTrue(message.contains(needle), "disclosure must mention: $needle")
        }
        assertFalse(AppSettings(context).disclosureAccepted)
        assertNull(shadowOf(activity).peekNextStartedActivityForResult())
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        idle()
        assertTrue(AppSettings(context).disclosureAccepted)

        val request = assertNotNull(shadowOf(activity).nextStartedActivityForResult)
        assertNull(session.started)
        val consent = Intent().putExtra("token", 42)
        shadowOf(activity).receiveResult(request.intent, Activity.RESULT_OK, consent)
        idle()
        val started = assertNotNull(session.started)
        assertEquals(Activity.RESULT_OK, started.second)
        assertEquals(42, started.third.getIntExtra("token", 0))
        assertEquals("Stop", b.startButton.text.toString())
        assertEquals(context.getString(R.string.status_running), b.sessionStatus.text.toString())
        snapshotAsIs(b.root, "main-running")

        b.startButton.performClick()
        idle()
        assertEquals(1, session.stops)
        assertEquals("Start", b.startButton.text.toString())
    }

    @Test
    fun `second start with a saved key goes straight to capture consent, and a refusal starts nothing`() {
        SecretStore(context).saveApiKey(key)
        AppSettings(context).disclosureAccepted = true
        val activity = launch()
        val b = binding(activity)
        assertEquals(context.getString(R.string.status_ready), b.sessionStatus.text.toString())
        b.startButton.performClick()
        idle()
        assertNull(ShadowDialog.getLatestDialog())
        val request = assertNotNull(shadowOf(activity).nextStartedActivityForResult)
        shadowOf(activity).receiveResult(request.intent, Activity.RESULT_CANCELED, null)
        idle()
        assertNull(session.started)
        assertEquals(context.getString(R.string.status_capture_denied), b.sessionStatus.text.toString())
        controller.pause().resume()
        assertEquals(context.getString(R.string.status_capture_denied), b.sessionStatus.text.toString())
    }

    @Test
    fun `a key typed but not saved is saved by Start`() {
        AppSettings(context).disclosureAccepted = true
        val activity = launch()
        val b = binding(activity)
        b.apiKeyInput.setText(key)
        b.startButton.performClick()
        idle()
        assertEquals(key, SecretStore(context).loadApiKey())
        assertNull(ShadowDialog.getLatestDialog())
        assertNotNull(shadowOf(activity).nextStartedActivityForResult)
    }

    @Test
    fun `cancelling the disclosure starts nothing and asks again next time`() {
        SecretStore(context).saveApiKey(key)
        val activity = launch()
        val b = binding(activity)
        b.startButton.performClick()
        idle()
        latestDialog().getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
        idle()
        assertFalse(AppSettings(context).disclosureAccepted)
        assertNull(shadowOf(activity).peekNextStartedActivityForResult())
        b.startButton.performClick()
        idle()
        assertEquals(context.getString(R.string.disclosure_title), dialogTitle(latestDialog()))
    }

    @Test
    fun `console link and notification settings do not crash without a handler`() {
        val activity = launch()
        val b = binding(activity)
        b.openConsoleButton.performClick()
        assertEquals("https://console.anthropic.com/", shadowOf(activity).nextStartedActivity.dataString)
        shadowOf(activity.application).checkActivities(true)
        b.openConsoleButton.performClick()
        assertEquals(context.getString(R.string.error_no_app_for_link), ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `survives recreation and destroys cleanly with a dialog open`() {
        val activity = launch()
        binding(activity).modelRow.performClick()
        controller.recreate()
        idle()
        val recreated = binding(controller.get())
        assertTrue(recreated.modelValue.text.isNotEmpty())
        recreated.effortRow.performClick()
        controller.pause().stop().destroy()
        ShadowLooper.idleMainLooper()
    }
}
