package com.krstech.chesshelp.claude

import com.anthropic.errors.AnthropicException
import com.anthropic.errors.AnthropicInvalidDataException
import com.anthropic.errors.AnthropicIoException
import com.anthropic.errors.AnthropicRetryableException
import com.anthropic.errors.AnthropicServiceException
import com.anthropic.errors.BadRequestException
import com.anthropic.errors.ContentTooLargeException
import com.anthropic.errors.CredentialResolutionException
import com.anthropic.errors.InternalServerException
import com.anthropic.errors.NoCredentialsException
import com.anthropic.errors.NotFoundException
import com.anthropic.errors.OverloadedException
import com.anthropic.errors.PermissionDeniedException
import com.anthropic.errors.RateLimitException
import com.anthropic.errors.RequestTimeoutException
import com.anthropic.errors.ServiceUnavailableException
import com.anthropic.errors.UnauthorizedException
import com.anthropic.errors.UnprocessableEntityException
import com.anthropic.models.ErrorType
import java.io.IOException
import java.io.InterruptedIOException
import java.nio.channels.ClosedByInterruptException

/** Turns whatever the SDK throws into a [ClaudeException], or into [InterruptedException] for a cancelled call. */
internal object ErrorMapping {

    fun map(error: Throwable, model: String): Exception = when (error) {
        is ClaudeException -> error
        is InterruptedException -> error

        // HTTP errors, most specific class first.
        is UnauthorizedException -> ClaudeException.InvalidApiKey(detail(error), error)
        is PermissionDeniedException -> ClaudeException.PermissionDenied(detail(error), error)
        is NotFoundException -> ClaudeException.ModelNotFound(model, detail(error), error)
        is RateLimitException -> ClaudeException.RateLimited(detail(error), outOfCredit = false, cause = error)
        is OverloadedException, is ServiceUnavailableException -> ClaudeException.Overloaded(detail(error), error)
        is InternalServerException ->
            if (typeOf(error) == ErrorType.OVERLOADED_ERROR) ClaudeException.Overloaded(detail(error), error)
            else ClaudeException.ServerError(error.statusCode(), detail(error), error)
        is RequestTimeoutException -> ClaudeException.Network(detail(error), timedOut = true, cause = error)
        is ContentTooLargeException ->
            ClaudeException.BadRequest("The request is too large: ${apiMessage(error)}", error)
        is BadRequestException, is UnprocessableEntityException ->
            if (isBilling(error)) ClaudeException.RateLimited(detail(error), outOfCredit = true, cause = error)
            else ClaudeException.BadRequest(apiMessage(error), error)
        is AnthropicServiceException -> mapByType(error, model)

        // No HTTP response at all.
        is NoCredentialsException, is CredentialResolutionException ->
            ClaudeException.InvalidApiKey("No usable API key: ${error.message}", error)
        is AnthropicIoException, is AnthropicRetryableException, is IOException -> mapTransport(error)

        is AnthropicInvalidDataException ->
            ClaudeException.Protocol("The response could not be read: ${error.message}", error)
        is AnthropicException -> ClaudeException.Protocol("Claude SDK error: ${error.message}", error)
        else -> ClaudeException.Protocol("Unexpected ${error.javaClass.simpleName}: ${error.message}", error)
    }

    /**
     * True when [error] exists because a thread was interrupted. The transport reports that as an
     * I/O failure, which must not reach the user as a network problem.
     */
    fun isInterruption(error: Throwable): Boolean = causes(error).any { cause ->
        cause is InterruptedException ||
            cause is ClosedByInterruptException ||
            // Exactly this class: its subclass SocketTimeoutException is a genuine timeout, and so is
            // OkHttp's call timeout, which uses this class with the message "timeout".
            (cause.javaClass == InterruptedIOException::class.java && cause.message != "timeout")
    }

    /** Statuses that have no exception class of their own (402 among them), classified by the API's error type. */
    private fun mapByType(error: AnthropicServiceException, model: String): ClaudeException {
        val status = error.statusCode()
        return when (typeOf(error)) {
            ErrorType.BILLING_ERROR -> ClaudeException.RateLimited(detail(error), outOfCredit = true, cause = error)
            ErrorType.RATE_LIMIT_ERROR -> ClaudeException.RateLimited(detail(error), outOfCredit = false, cause = error)
            ErrorType.OVERLOADED_ERROR -> ClaudeException.Overloaded(detail(error), error)
            ErrorType.AUTHENTICATION_ERROR -> ClaudeException.InvalidApiKey(detail(error), error)
            ErrorType.PERMISSION_ERROR -> ClaudeException.PermissionDenied(detail(error), error)
            ErrorType.NOT_FOUND_ERROR -> ClaudeException.ModelNotFound(model, detail(error), error)
            ErrorType.TIMEOUT_ERROR -> ClaudeException.Network(detail(error), timedOut = true, cause = error)
            else -> when {
                status == 402 -> ClaudeException.RateLimited(detail(error), outOfCredit = true, cause = error)
                status >= 500 -> ClaudeException.ServerError(status, detail(error), error)
                else -> ClaudeException.BadRequest(apiMessage(error), error)
            }
        }
    }

    private fun mapTransport(error: Throwable): Exception {
        if (isInterruption(error)) {
            return InterruptedException("The Claude request was interrupted").apply { initCause(error) }
        }
        // What is left of InterruptedIOException after the interruption check is timeouts.
        val timedOut = causes(error).any { it is InterruptedIOException }
        val root = causes(error).last()
        val detail = (if (timedOut) "Timed out" else "Connection failed") +
            ": ${root.javaClass.simpleName}" + (root.message?.let { " ($it)" } ?: "")
        return ClaudeException.Network(detail, timedOut = timedOut, cause = error)
    }

    /** Before 402 `billing_error` existed, an empty balance came back as a 400 with this wording. */
    private fun isBilling(error: AnthropicServiceException): Boolean =
        typeOf(error) == ErrorType.BILLING_ERROR ||
            apiMessage(error).contains("credit balance", ignoreCase = true)

    private fun typeOf(error: AnthropicServiceException): ErrorType? =
        try {
            error.errorType().orElse(null)
        } catch (_: RuntimeException) {
            null
        }

    /** The API's own message, or the SDK's rendering when the body was not an API error object. */
    private fun apiMessage(error: AnthropicServiceException): String {
        val fromApi = try {
            error.apiMessage().orElse(null)
        } catch (_: RuntimeException) {
            null
        }
        return fromApi?.takeIf { it.isNotBlank() } ?: error.message ?: "HTTP ${error.statusCode()}"
    }

    private fun detail(error: AnthropicServiceException): String = "HTTP ${error.statusCode()}: ${apiMessage(error)}"

    private fun causes(error: Throwable): List<Throwable> {
        val chain = ArrayList<Throwable>()
        var current: Throwable? = error
        while (current != null && chain.size < 12 && current !in chain) {
            chain += current
            current = current.cause
        }
        return chain
    }
}
