package com.krstech.chesshelp

import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** The seam between the settings screen and the capture/analysis service. */
interface SessionController {
    /** True while the capture service is running. */
    val running: StateFlow<Boolean>

    /** Starts the service with the screen-capture consent result ([resultCode] is RESULT_OK). */
    fun start(context: Context, resultCode: Int, data: Intent)

    fun stop(context: Context)

    /**
     * Validates the key/model/base URL with one tiny request. The success value is shown to the user
     * after "Connection OK:" (the serving model id is a good value); a failure's message is shown as is.
     */
    suspend fun testConnection(apiKey: String, model: String, baseUrl: String?): Result<String>
}

/** Thrown/returned by the placeholder controller that is in place until the service is wired in. */
class SessionUnavailableException : IllegalStateException("The capture service is not connected in this build")

object AppGraph {
    @Volatile
    var session: SessionController = UnwiredSessionController
}

internal object UnwiredSessionController : SessionController {
    private val state = MutableStateFlow(false)

    override val running: StateFlow<Boolean> = state

    override fun start(context: Context, resultCode: Int, data: Intent) {
        Toast.makeText(context.applicationContext, R.string.session_not_wired, Toast.LENGTH_LONG).show()
    }

    override fun stop(context: Context) = Unit

    override suspend fun testConnection(apiKey: String, model: String, baseUrl: String?): Result<String> =
        Result.failure(SessionUnavailableException())
}
