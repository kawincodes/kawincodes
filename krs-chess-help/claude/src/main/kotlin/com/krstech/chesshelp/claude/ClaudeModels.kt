package com.krstech.chesshelp.claude

/** How hard Claude thinks before answering. Sent as `output_config.effort`. */
enum class Effort { LOW, MEDIUM, HIGH }

data class ModelInfo(val id: String, val label: String, val note: String)

object ClaudeModels {
    const val DEFAULT = "claude-opus-5-5"

    private const val SONNET = "claude-sonnet-5-5"
    private const val HAIKU = "claude-haiku-5-5"
    private const val FABLE = "claude-fable-5-2"

    val ALL: List<ModelInfo> = listOf(
        ModelInfo(DEFAULT, "Claude Opus 5.5", "Recommended: accurate, at a moderate price."),
        ModelInfo(SONNET, "Claude Sonnet 5.5", "Balanced: faster and cheaper than Opus."),
        ModelInfo(HAIKU, "Claude Haiku 5.5", "Fastest and cheapest."),
        ModelInfo(FABLE, "Claude Fable 5.2", "Most capable; slowest and most expensive."),
    )

    /**
     * Whether the model accepts the server-side refusal fallback opt-in (`fallbacks: "default"`).
     * Claude Haiku 5.5 has no server-side fallback, and unknown ids are treated the same way so a
     * model the app does not know never gets a parameter it might reject.
     */
    fun supportsServerFallback(model: String): Boolean =
        model == DEFAULT || model == SONNET || model == FABLE

    /** Display label for a model id; ids the app does not list are shown as they are. */
    fun labelOf(model: String): String = ALL.firstOrNull { it.id == model }?.label ?: model
}

data class ClaudeConfig(
    val apiKey: String,
    val model: String = ClaudeModels.DEFAULT,
    val effort: Effort = Effort.MEDIUM,
    /** `null` = api.anthropic.com. */
    val baseUrl: String? = null,
    val requestTimeoutSeconds: Long = 180,
    /** Upper bound on API requests made for one `analyzeBoard` / `advise` call. */
    val maxRoundTrips: Int = 6,
    /** Automatic retries (by the SDK) of rate-limit, overload, server and connection errors, per request. */
    val maxRetries: Int = 2,
) {
    // The key must never end up in logs through a data-class toString().
    override fun toString(): String =
        "ClaudeConfig(model=$model, effort=$effort, baseUrl=$baseUrl, requestTimeoutSeconds=$requestTimeoutSeconds, " +
            "maxRoundTrips=$maxRoundTrips, maxRetries=$maxRetries)"
}
