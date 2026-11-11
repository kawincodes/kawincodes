package com.krstech.chesshelp.service

import android.content.Context
import android.content.Intent
import com.krstech.chesshelp.SessionController
import com.krstech.chesshelp.claude.ClaudeChessClient
import com.krstech.chesshelp.claude.ClaudeConfig
import com.krstech.chesshelp.claude.ClaudeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runInterruptible

/** The real [SessionController]: backed by [ChessHelpService] and the Claude client. */
object ServiceSessionController : SessionController {

    override val running: StateFlow<Boolean> get() = ChessHelpService.running

    override fun start(context: Context, resultCode: Int, data: Intent) =
        ChessHelpService.start(context, resultCode, data)

    override fun stop(context: Context) = ChessHelpService.stop(context)

    override suspend fun testConnection(apiKey: String, model: String, baseUrl: String?): Result<String> =
        runInterruptible(Dispatchers.IO) {
            try {
                val config = ClaudeConfig(apiKey = apiKey, model = model, baseUrl = baseUrl, requestTimeoutSeconds = 30)
                ClaudeChessClient(config).use { Result.success(it.ping()) }
            } catch (e: ClaudeException) {
                Result.failure(IllegalStateException(e.userMessage, e))
            }
        }
}
