package com.krstech.chesshelp.claude

/**
 * Everything that can go wrong while talking to Claude, except cancellation: a call whose thread is
 * interrupted throws [InterruptedException] instead.
 *
 * [message] is the technical detail for logs; [userMessage] is short enough for the overlay.
 */
sealed class ClaudeException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    abstract val userMessage: String

    /** 401, or no key configured. */
    class InvalidApiKey(detail: String = "The API key was rejected", cause: Throwable? = null) :
        ClaudeException(detail, cause) {
        override val userMessage: String get() = "Invalid API key. Check it in Settings."
    }

    /** 403. */
    class PermissionDenied(detail: String, cause: Throwable? = null) : ClaudeException(detail, cause) {
        override val userMessage: String get() = "This API key is not allowed to do that."
    }

    /** 404: the model id is unknown or not available to this account. */
    class ModelNotFound(val model: String, detail: String, cause: Throwable? = null) : ClaudeException(detail, cause) {
        override val userMessage: String get() = "Model \"$model\" is not available for this API key."
    }

    /** 429, or ([outOfCredit]) a billing error: the account has no credit left. */
    class RateLimited(detail: String, val outOfCredit: Boolean = false, cause: Throwable? = null) :
        ClaudeException(detail, cause) {
        override val userMessage: String
            get() = if (outOfCredit) "Out of API credit. Add credit in the Anthropic Console."
            else "Rate limit reached. Wait a moment and try again."
    }

    /** 529 / 503: the API is temporarily over capacity. */
    class Overloaded(detail: String, cause: Throwable? = null) : ClaudeException(detail, cause) {
        override val userMessage: String get() = "Claude is overloaded right now. Try again shortly."
    }

    /** No response: connection failure or ([timedOut]) a timeout. */
    class Network(detail: String, val timedOut: Boolean = false, cause: Throwable? = null) :
        ClaudeException(detail, cause) {
        override val userMessage: String
            get() = if (timedOut) "Claude took too long to answer. Try again."
            else "Can't reach Claude. Check your internet connection."
    }

    /** 400 and other request errors. [apiMessage] is the API's own explanation. */
    class BadRequest(val apiMessage: String, cause: Throwable? = null) : ClaudeException(apiMessage, cause) {
        override val userMessage: String get() = "Claude rejected the request: ${shorten(apiMessage)}"
    }

    /** 5xx other than overload. */
    class ServerError(val statusCode: Int, detail: String, cause: Throwable? = null) : ClaudeException(detail, cause) {
        override val userMessage: String get() = "Claude had a server error ($statusCode). Try again shortly."
    }

    /** `stop_reason: "refusal"`. [category] is the policy category when the API names one. */
    class Refused(val category: String?) :
        ClaudeException("Claude declined the request" + (category?.let { " (category: $it)" } ?: "")) {
        override val userMessage: String get() = "Claude declined to analyse this screen."
    }

    /** `stop_reason: "max_tokens"`: the answer was cut off before it was complete. */
    class Truncated :
        ClaudeException("The response hit max_tokens before the answer was complete") {
        override val userMessage: String get() = "Claude's answer was cut off. Try again or lower the effort."
    }

    /** The response did not have the expected shape. */
    class Protocol(val detail: String, cause: Throwable? = null) : ClaudeException(detail, cause) {
        override val userMessage: String get() = "Unexpected response from Claude. Try again."
    }

    protected fun shorten(text: String, max: Int = 140): String {
        val oneLine = text.replace(Regex("\\s+"), " ").trim()
        return if (oneLine.length <= max) oneLine else oneLine.take(max - 1).trimEnd() + "…"
    }
}
