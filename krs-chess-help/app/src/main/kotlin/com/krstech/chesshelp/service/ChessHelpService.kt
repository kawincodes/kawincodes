package com.krstech.chesshelp.service

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.krstech.chesshelp.R
import com.krstech.chesshelp.brain.BrainDispatchers
import com.krstech.chesshelp.brain.ChessBrain
import com.krstech.chesshelp.brain.ClaudeAdviceService
import com.krstech.chesshelp.capture.ScreenCapturer
import com.krstech.chesshelp.overlay.OverlayController
import com.krstech.chesshelp.settings.AppSettings
import com.krstech.chesshelp.settings.SecretStore
import com.krstech.chesshelp.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Foreground service (type mediaProjection) that owns one capture session: the MediaProjection, the
 * floating overlay and the analysis brain. It lives from the user's screen-capture consent until Stop.
 */
class ChessHelpService : LifecycleService(), OverlayController.Listener {

    private var capturer: ScreenCapturer? = null
    private var overlay: OverlayController? = null
    private var brain: ChessBrain? = null
    private var released = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> startSession(intent)
            // ACTION_STOP, or a restart without the consent Intent: a projection cannot be recovered.
            else -> shutdown()
        }
        return START_NOT_STICKY
    }

    private fun startSession(intent: Intent) {
        if (brain != null) return

        // Android requires the service to be in the foreground with the mediaProjection type before
        // getMediaProjection(); and startForegroundService() must always be answered by startForeground().
        try {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            } else {
                0
            }
            ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(), type)
        } catch (e: RuntimeException) {
            Log.w(TAG, "Could not enter the foreground", e)
            fail(R.string.service_error_start)
            return
        }

        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
        val data = IntentCompat.getParcelableExtra(intent, EXTRA_RESULT_DATA, Intent::class.java)
        if (resultCode != Activity.RESULT_OK || data == null) {
            fail(R.string.service_error_start)
            return
        }
        if (!Settings.canDrawOverlays(this)) {
            fail(R.string.service_error_overlay)
            return
        }

        val projection = try {
            getSystemService(MediaProjectionManager::class.java)?.getMediaProjection(resultCode, data)
        } catch (e: RuntimeException) {
            Log.w(TAG, "getMediaProjection failed", e)
            null
        }
        if (projection == null) {
            fail(R.string.service_error_start)
            return
        }

        val capturer = ScreenCapturer(this, projection) { shutdown() }
        this.capturer = capturer
        val overlay = OverlayController(this, this)
        this.overlay = overlay
        val brain = ChessBrain(
            frames = CapturerFrames(capturer),
            overlay = OverlayAdapter(overlay),
            encoder = RendererEncoder,
            settings = SettingsAdapter(AppSettings(this), SecretStore(this)),
            claude = ClaudeAdviceService.Factory,
            scope = lifecycleScope,
            dispatchers = BrainDispatchers(Dispatchers.Main.immediate, Dispatchers.Default, Dispatchers.IO),
        )
        this.brain = brain
        capturer.onSizeChanged = {
            overlay.onConfigurationChanged()
            brain.onScreenChanged()
        }

        try {
            capturer.start()
        } catch (e: RuntimeException) {
            Log.w(TAG, "Screen capture could not start", e)
            fail(R.string.service_error_start)
            return
        }
        overlay.attach()
        brain.start()
        runningState.value = true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        overlay?.onConfigurationChanged()
        brain?.onScreenChanged()
    }

    override fun onDestroy() {
        release()
        super.onDestroy()
    }

    private fun fail(messageRes: Int) {
        Toast.makeText(applicationContext, messageRes, Toast.LENGTH_LONG).show()
        shutdown()
    }

    private fun shutdown() {
        release()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun release() {
        if (released) return
        released = true
        runningState.value = false
        brain?.stop()
        brain = null
        overlay?.detach()
        overlay = null
        capturer?.stop()
        capturer = null
    }

    // ---- OverlayController.Listener (main thread) ----

    override fun onAnalyze() { brain?.onAnalyze() }
    override fun onRescan() { brain?.onRescan() }
    override fun onToggleSideToMove() { brain?.onToggleSideToMove() }
    override fun onAutoChanged(enabled: Boolean) { brain?.onAutoChanged(enabled) }
    override fun onFlipBoard() { brain?.onFlipBoard() }
    override fun onStop() { shutdown() }

    private fun buildNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.service_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            )
            channel.setShowBadge(false)
            manager.createNotificationChannel(channel)
        }
        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stop = PendingIntent.getService(
            this, 1,
            Intent(this, ChessHelpService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_chess)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_text))
            .setContentIntent(open)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(0, getString(R.string.service_notification_stop), stop)
            .build()
    }

    companion object {
        private const val TAG = "ChessHelpService"
        private const val ACTION_START = "com.krstech.chesshelp.action.START"
        private const val ACTION_STOP = "com.krstech.chesshelp.action.STOP"
        private const val EXTRA_RESULT_CODE = "result_code"
        private const val EXTRA_RESULT_DATA = "result_data"
        private const val CHANNEL_ID = "capture_session"
        private const val NOTIFICATION_ID = 1

        private val runningState = MutableStateFlow(false)

        /** True while a capture session is active. */
        val running: StateFlow<Boolean> get() = runningState

        /** Starts a session with the screen-capture consent result. Call right after the user consents. */
        fun start(context: Context, resultCode: Int, data: Intent) {
            val intent = Intent(context, ChessHelpService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_RESULT_CODE, resultCode)
                .putExtra(EXTRA_RESULT_DATA, data)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ChessHelpService::class.java))
        }
    }
}
