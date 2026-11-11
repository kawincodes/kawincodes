package com.krstech.chesshelp.ui

import android.Manifest
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjectionConfig
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.krstech.chesshelp.AppGraph
import com.krstech.chesshelp.R
import com.krstech.chesshelp.SessionUnavailableException
import com.krstech.chesshelp.databinding.ActivityMainBinding
import com.krstech.chesshelp.settings.AdviseForSetting
import com.krstech.chesshelp.settings.AppSettings
import com.krstech.chesshelp.settings.BaseUrlValidator
import com.krstech.chesshelp.settings.EffortSetting
import com.krstech.chesshelp.settings.ModelCatalog
import com.krstech.chesshelp.settings.SecretStore
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** The single settings screen: API key, options, permissions and the Start/Stop button. */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var settings: AppSettings
    private lateinit var secrets: SecretStore

    private var dialog: Dialog? = null
    private var testJob: Job? = null
    private var running = false

    /** The key as stored; null = none. Loaded off the main thread (Keystore calls can be slow). */
    private var savedKey: String? = null
    private var keyLoaded = false

    /** The user's explicit, per-start choice to run without an API key. Deliberately not persisted. */
    private var localEngineOnly = false

    /** Set once the system stopped showing the notification prompt, so the button opens settings instead. */
    /** Why the last start attempt ended without a session; shown until the next attempt. */
    @StringRes
    private var startNotice: Int? = null

    private var notificationPromptExhausted = false
    private var notificationRationaleBeforeRequest = false

    private val captureConsent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { onCaptureConsent(it) }

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted && !notificationRationaleBeforeRequest && !shouldShowNotificationRationale()) {
                // Denied without the system offering to ask again: only the settings page can change it now.
                notificationPromptExhausted = true
            }
            refreshPermissions()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        // Keeps this screen (and the API key on it) out of screenshots, including this app's own capture.
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        settings = AppSettings(this)
        secrets = SecretStore(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        bindApiKeySection()
        bindPickers()
        bindSwitches()
        bindBaseUrl()
        bindPermissions()
        binding.startButton.setOnClickListener { onStartStopClicked() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppGraph.session.running.collect { isRunning ->
                    running = isRunning
                    refreshSessionUi()
                }
            }
        }
        loadSavedKey()
    }

    override fun onResume() {
        super.onResume()
        refreshPermissions()
        refreshSessionUi()
    }

    override fun onDestroy() {
        dialog?.dismiss()
        dialog = null
        super.onDestroy()
    }

    // ---- Insets ---------------------------------------------------------------------------------

    private fun applyWindowInsets() {
        val bottomBarPadding = binding.bottomBar.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            root.updatePadding(left = bars.left, top = bars.top, right = bars.right)
            // The bottom bar's background runs to the screen edge; its content stays above the
            // navigation bar and rides on top of the keyboard.
            binding.bottomBar.updatePadding(bottom = bottomBarPadding + max(bars.bottom, keyboard.bottom))
            WindowInsetsCompat.CONSUMED
        }
    }

    // ---- API key --------------------------------------------------------------------------------

    private fun bindApiKeySection() {
        binding.saveKeyButton.setOnClickListener { saveKeyFromInput(announce = true) }
        binding.testButton.setOnClickListener { testConnection() }
        binding.openConsoleButton.setOnClickListener {
            launchSafely(Intent(Intent.ACTION_VIEW, getString(R.string.api_key_console_url).toUri()))
        }
        binding.apiKeyInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) saveKeyFromInput(announce = true)
            false
        }
        setKeyControlsEnabled(false)
    }

    private fun loadSavedKey() {
        lifecycleScope.launch {
            val key = withContext(Dispatchers.IO) { secrets.loadApiKey() }
            savedKey = key
            keyLoaded = true
            if (binding.apiKeyInput.text.isNullOrEmpty() && key != null) binding.apiKeyInput.setText(key)
            setKeyControlsEnabled(true)
            binding.apiKeyStatus.setText(if (key != null) R.string.api_key_status_saved else R.string.api_key_status_none)
            refreshSessionUi()
        }
    }

    private fun setKeyControlsEnabled(enabled: Boolean) {
        binding.saveKeyButton.isEnabled = enabled
        binding.testButton.isEnabled = enabled && testJob?.isActive != true
    }

    private fun typedKey(): String = binding.apiKeyInput.text?.toString()?.trim().orEmpty()

    /**
     * Stores what is in the field (an empty field removes the key). [onDone] runs afterwards on the main
     * thread with true when the stored state now matches the field.
     */
    private fun saveKeyFromInput(announce: Boolean, onDone: (Boolean) -> Unit = {}) {
        if (!keyLoaded) return
        val typed = typedKey()
        if (typed == savedKey.orEmpty()) {
            if (announce) {
                binding.apiKeyStatus.setText(
                    if (typed.isEmpty()) R.string.api_key_status_none else R.string.api_key_status_saved,
                )
            }
            onDone(true)
            return
        }
        setKeyControlsEnabled(false)
        lifecycleScope.launch {
            val stored = withContext(Dispatchers.IO) { secrets.saveApiKey(typed) }
            if (stored) savedKey = typed.ifEmpty { null }
            setKeyControlsEnabled(true)
            binding.apiKeyStatus.setText(
                when {
                    !stored -> R.string.api_key_status_save_failed
                    typed.isEmpty() -> R.string.api_key_status_removed
                    else -> R.string.api_key_status_saved
                },
            )
            refreshSessionUi()
            onDone(stored)
        }
    }

    private fun testConnection() {
        val key = typedKey().ifEmpty { savedKey.orEmpty() }
        if (key.isEmpty()) {
            binding.apiKeyStatus.setText(R.string.api_key_status_empty)
            return
        }
        testJob?.cancel()
        testJob = lifecycleScope.launch {
            binding.testButton.isEnabled = false
            binding.apiKeyStatus.setText(R.string.api_key_status_testing)
            val result = try {
                AppGraph.session.testConnection(key, settings.modelId, settings.baseUrlOrNull)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                binding.testButton.isEnabled = true
            }
            binding.apiKeyStatus.text = result.fold(
                onSuccess = { getString(R.string.api_key_status_test_ok, redact(it, key)) },
                onFailure = { getString(R.string.api_key_status_test_failed, describe(it, key)) },
            )
        }
    }

    private fun describe(error: Throwable, key: String): String = when {
        error is SessionUnavailableException -> getString(R.string.session_not_wired)
        error.message.isNullOrBlank() -> getString(R.string.error_unknown)
        else -> redact(error.message.orEmpty(), key)
    }

    /** A message from lower layers must never put the key on screen. */
    private fun redact(text: String, key: String): String =
        if (key.isEmpty()) text else text.replace(key, "…")

    // ---- Pickers --------------------------------------------------------------------------------

    private fun bindPickers() {
        binding.modelRow.setOnClickListener {
            val labels = ModelCatalog.ALL.map { getString(R.string.model_choice, it.label, getString(it.noteRes)) }
            showChoices(R.string.model_title, labels, ModelCatalog.indexOf(settings.modelId)) { index ->
                settings.modelId = ModelCatalog.ALL[index].id
                refreshPickers()
            }
        }
        binding.effortRow.setOnClickListener {
            val options = EffortSetting.entries
            showChoices(R.string.effort_title, options.map { getString(effortLabel(it)) }, options.indexOf(settings.effort)) { index ->
                settings.effort = options[index]
                refreshPickers()
            }
        }
        binding.adviseForRow.setOnClickListener {
            val options = AdviseForSetting.entries
            showChoices(
                R.string.advise_for_title,
                options.map { getString(adviseForLabel(it)) },
                options.indexOf(settings.adviseFor),
            ) { index ->
                settings.adviseFor = options[index]
                refreshPickers()
            }
        }
        refreshPickers()
    }

    private fun refreshPickers() {
        val model = ModelCatalog.byId(settings.modelId)
        binding.modelValue.text = getString(R.string.model_choice, model.label, getString(model.noteRes))
        binding.effortValue.setText(effortLabel(settings.effort))
        binding.adviseForValue.setText(adviseForLabel(settings.adviseFor))
    }

    @StringRes
    private fun effortLabel(effort: EffortSetting): Int = when (effort) {
        EffortSetting.LOW -> R.string.effort_low
        EffortSetting.MEDIUM -> R.string.effort_medium
        EffortSetting.HIGH -> R.string.effort_high
    }

    @StringRes
    private fun adviseForLabel(adviseFor: AdviseForSetting): Int = when (adviseFor) {
        AdviseForSetting.BOTTOM -> R.string.advise_for_bottom
        AdviseForSetting.WHITE -> R.string.advise_for_white
        AdviseForSetting.BLACK -> R.string.advise_for_black
        AdviseForSetting.BOTH -> R.string.advise_for_both
    }

    private fun showChoices(@StringRes title: Int, labels: List<String>, selected: Int, onChosen: (Int) -> Unit) {
        showDialog(
            MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setSingleChoiceItems(labels.toTypedArray(), selected) { choice, index ->
                    onChosen(index)
                    choice.dismiss()
                }
                .setNegativeButton(R.string.dialog_cancel, null)
                .create(),
        )
    }

    private fun showDialog(next: Dialog) {
        dialog?.dismiss()
        dialog = next
        next.show()
    }

    // ---- Switches and advanced ------------------------------------------------------------------

    private fun bindSwitches() {
        binding.autoAdviseSwitch.isChecked = settings.autoAdvise
        binding.drawOnBoardSwitch.isChecked = settings.drawOnBoard
        binding.sendScreenSwitch.isChecked = settings.sendScreenContext
        binding.autoAdviseSwitch.setOnCheckedChangeListener { _, checked -> settings.autoAdvise = checked }
        binding.drawOnBoardSwitch.setOnCheckedChangeListener { _, checked -> settings.drawOnBoard = checked }
        binding.sendScreenSwitch.setOnCheckedChangeListener { _, checked -> settings.sendScreenContext = checked }
    }

    private fun bindBaseUrl() {
        binding.baseUrlInput.setText(settings.customBaseUrl)
        binding.baseUrlInput.doAfterTextChanged { text ->
            val normalized = BaseUrlValidator.normalize(text?.toString())
            if (normalized == null) {
                binding.baseUrlLayout.error = getString(R.string.base_url_error)
            } else {
                binding.baseUrlLayout.error = null
                settings.customBaseUrl = normalized
            }
        }
    }

    // ---- Permissions ----------------------------------------------------------------------------

    private fun bindPermissions() {
        binding.overlayButton.setOnClickListener { openOverlaySettings() }
        binding.notificationButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPromptExhausted) {
                notificationRationaleBeforeRequest = shouldShowNotificationRationale()
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                launchSafely(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName),
                )
            }
        }
    }

    private fun shouldShowNotificationRationale(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)

    private fun refreshPermissions() {
        val overlay = Settings.canDrawOverlays(this)
        binding.overlayStatus.setText(if (overlay) R.string.perm_overlay_granted else R.string.perm_overlay_missing)
        binding.overlayButton.isVisible = !overlay

        val notifications = NotificationManagerCompat.from(this).areNotificationsEnabled()
        binding.notificationStatus.setText(
            if (notifications) R.string.perm_notifications_granted else R.string.perm_notifications_missing,
        )
        binding.notificationButton.isVisible = !notifications
        val canPrompt = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPromptExhausted
        binding.notificationButton.setText(if (canPrompt) R.string.perm_grant else R.string.perm_open_settings)
    }

    private fun openOverlaySettings() {
        val forThisApp = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:$packageName".toUri())
        if (launchSafely(forThisApp, quiet = true)) return
        launchSafely(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri()))
    }

    private fun launchSafely(intent: Intent, quiet: Boolean = false): Boolean = try {
        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        if (!quiet) Toast.makeText(this, R.string.error_no_app_for_link, Toast.LENGTH_LONG).show()
        false
    } catch (e: SecurityException) {
        if (!quiet) Toast.makeText(this, R.string.error_no_app_for_link, Toast.LENGTH_LONG).show()
        false
    }

    // ---- Start / stop ---------------------------------------------------------------------------

    private fun refreshSessionUi() {
        binding.startButton.setText(if (running) R.string.stop else R.string.start)
        binding.sessionStatus.setText(
            when {
                running -> R.string.status_running
                startNotice != null -> startNotice ?: R.string.status_ready
                !Settings.canDrawOverlays(this) -> R.string.status_need_overlay
                keyLoaded && savedKey == null && typedKey().isEmpty() -> R.string.status_need_key
                else -> R.string.status_ready
            },
        )
    }

    private fun onStartStopClicked() {
        if (running) {
            AppGraph.session.stop(applicationContext)
            return
        }
        if (!keyLoaded) return
        localEngineOnly = false
        startNotice = null
        // A key that was typed but not saved yet is what the user expects to be used.
        saveKeyFromInput(announce = false) { continueStart() }
    }

    /** Walks through the start requirements in order; each dialog calls back in here when it is satisfied. */
    private fun continueStart() {
        if (running || isFinishing || isDestroyed) return
        when {
            !Settings.canDrawOverlays(this) -> showOverlayNeededDialog()
            savedKey == null && !localEngineOnly -> showNoKeyDialog()
            !settings.disclosureAccepted -> showDisclosureDialog()
            else -> requestScreenCapture()
        }
    }

    private fun showOverlayNeededDialog() {
        showDialog(
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_overlay_title)
                .setMessage(R.string.dialog_overlay_message)
                .setPositiveButton(R.string.dialog_overlay_open) { _, _ -> openOverlaySettings() }
                .setNegativeButton(R.string.dialog_cancel, null)
                .create(),
        )
    }

    private fun showNoKeyDialog() {
        showDialog(
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_no_key_title)
                .setMessage(R.string.dialog_no_key_message)
                .setPositiveButton(R.string.dialog_no_key_add) { _, _ ->
                    binding.scroll.smoothScrollTo(0, 0)
                    binding.apiKeyInput.requestFocus()
                }
                .setNegativeButton(R.string.dialog_no_key_local) { _, _ ->
                    localEngineOnly = true
                    continueStart()
                }
                .setNeutralButton(R.string.dialog_cancel, null)
                .create(),
        )
    }

    private fun showDisclosureDialog() {
        showDialog(
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.disclosure_title)
                .setMessage(R.string.disclosure_message)
                .setPositiveButton(R.string.disclosure_accept) { _, _ ->
                    settings.disclosureAccepted = true
                    continueStart()
                }
                .setNegativeButton(R.string.dialog_cancel, null)
                .create(),
        )
    }

    private fun requestScreenCapture() {
        val manager = getSystemService(MediaProjectionManager::class.java)
        if (manager == null) {
            showStartNotice(R.string.status_capture_unavailable)
            return
        }
        // Android 14+ lets the user share a single app instead of the screen; the arrow drawn over the
        // board needs whole-display capture, where frame pixels are screen pixels.
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            manager.createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay())
        } else {
            manager.createScreenCaptureIntent()
        }
        try {
            captureConsent.launch(intent)
        } catch (e: ActivityNotFoundException) {
            showStartNotice(R.string.status_capture_unavailable)
        }
    }

    private fun showStartNotice(@StringRes message: Int) {
        startNotice = message
        refreshSessionUi()
    }

    private fun onCaptureConsent(result: ActivityResult) {
        val data = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            startNotice = null
            AppGraph.session.start(applicationContext, result.resultCode, data)
        } else {
            showStartNotice(R.string.status_capture_denied)
        }
    }
}
