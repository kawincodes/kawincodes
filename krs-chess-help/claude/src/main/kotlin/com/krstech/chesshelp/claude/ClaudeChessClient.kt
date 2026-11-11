package com.krstech.chesshelp.claude

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.core.JsonValue
import com.anthropic.core.Timeout
import com.anthropic.models.beta.AnthropicBeta
import com.anthropic.models.beta.messages.BetaBase64ImageSource
import com.anthropic.models.beta.messages.BetaCacheControlEphemeral
import com.anthropic.models.beta.messages.BetaContentBlockParam
import com.anthropic.models.beta.messages.BetaImageBlockParam
import com.anthropic.models.beta.messages.BetaMessage
import com.anthropic.models.beta.messages.BetaMessageParam
import com.anthropic.models.beta.messages.BetaOutputConfig
import com.anthropic.models.beta.messages.BetaStopReason
import com.anthropic.models.beta.messages.BetaTextBlockParam
import com.anthropic.models.beta.messages.BetaToolChoiceAuto
import com.anthropic.models.beta.messages.BetaToolResultBlockParam
import com.anthropic.models.beta.messages.MessageCreateParams
import java.time.Duration
import java.util.Base64
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Blocking Claude client for the chess assistant. Thread-safe; one instance can serve several calls.
 *
 * Every method throws [ClaudeException] for API and transport failures and [InterruptedException]
 * when the calling thread is interrupted (that is how the app cancels a running analysis).
 * Exceptions thrown by the [PositionOracle] or by `onProgress` pass through unchanged.
 */
class ClaudeChessClient(private val config: ClaudeConfig) : AutoCloseable {

    private val closed = AtomicBoolean(false)

    /**
     * Requests run on these threads while the caller waits on a [Future]. Blocking socket reads do
     * not react to [Thread.interrupt], so a caller blocked inside the HTTP call itself could not be
     * cancelled until the response arrived; waiting on a future can be.
     */
    private val executor: ExecutorService = Executors.newCachedThreadPool { task ->
        Thread(task, "claude-request-${THREAD_COUNTER.incrementAndGet()}").apply { isDaemon = true }
    }

    /** Requests being waited for, so that [close] can release their callers. */
    private val inFlight: MutableSet<Future<*>> = ConcurrentHashMap.newKeySet()

    private val sdkLazy = lazy(::createSdkClient)
    private val sdk: AnthropicClient by sdkLazy

    /**
     * Cleared when the API turns the fallback opt-in down (see [isFallbackOptInRejection]), so the
     * rest of this client's requests go out without it instead of failing the same way.
     */
    private val fallbackOptIn = AtomicBoolean(ClaudeModels.supportsServerFallback(config.model))

    private val effort: BetaOutputConfig.Effort = when (config.effort) {
        Effort.LOW -> BetaOutputConfig.Effort.LOW
        Effort.MEDIUM -> BetaOutputConfig.Effort.MEDIUM
        Effort.HIGH -> BetaOutputConfig.Effort.HIGH
    }

    /**
     * Reads the position from the screenshot(s), has [oracle] validate it, and returns Claude's
     * recommendation. Returns as soon as a legal `recommend_move` arrives.
     *
     * `advice` is `null` when Claude saw no board (its sentence is in `commentary`), when no reading
     * was accepted, or when no legal move arrived within [ClaudeConfig.maxRoundTrips] requests.
     */
    @Throws(ClaudeException::class, InterruptedException::class)
    fun analyzeBoard(request: VisionRequest, oracle: PositionOracle, onProgress: (String) -> Unit = {}): AnalysisResult {
        require(request.boardImage != null || request.screenImage != null) {
            "VisionRequest needs a board image, a screen image, or both"
        }
        val forcedSide = request.forcedSideToMove?.lowercaseChar()
        require(forcedSide == null || forcedSide == 'w' || forcedSide == 'b') {
            "forcedSideToMove must be 'w', 'b' or null"
        }
        ensureUsable()

        val content = ArrayList<BetaContentBlockParam>(3)
        request.boardImage?.let { content += imageBlock(it) }
        request.screenImage?.let { content += imageBlock(it) }
        // The second round trip resends this message with the images in it; the cache breakpoint
        // lets that request read them back instead of paying for them again.
        content += textBlock(Prompts.visionRequest(request, forcedSide), cacheBreakpoint = true)

        val loop = ToolLoop(vision = true, knownFen = null, forcedSide = forcedSide, oracle = oracle, onProgress = onProgress)
        return loop.run(BetaMessageParam.ofUserOfContentBlockParams(content))
    }

    /** Text-only path: the app already tracks the position, Claude only chooses the move. */
    @Throws(ClaudeException::class, InterruptedException::class)
    fun advise(request: AdviceRequest, oracle: PositionOracle, onProgress: (String) -> Unit = {}): AnalysisResult {
        require(request.fen.isNotBlank()) { "AdviceRequest.fen must not be blank" }
        ensureUsable()

        val content = listOf(textBlock(Prompts.adviceRequest(request), cacheBreakpoint = false))
        val loop = ToolLoop(vision = false, knownFen = request.fen.trim(), forcedSide = null, oracle = oracle, onProgress = onProgress)
        return loop.run(BetaMessageParam.ofUserOfContentBlockParams(content))
    }

    /**
     * Checks that the key works and the model is available to it, and returns the serving model id.
     * Uses the Models API, which costs no tokens; when the server has no such endpoint (a gateway
     * behind a custom base URL) it sends a minimal message instead.
     */
    @Throws(ClaudeException::class, InterruptedException::class)
    fun ping(): String {
        ensureUsable()
        try {
            return http { sdk.models().retrieve(config.model).id() }
        } catch (_: ClaudeException.ModelNotFound) {
            // A 404 here is either an unknown model or a server without the endpoint. The message
            // request below tells the two apart, and for an unknown model it fails without spending tokens.
        }
        val params = MessageCreateParams.builder()
            .model(config.model)
            .maxTokens(PING_MAX_TOKENS)
            .outputConfig(BetaOutputConfig.builder().effort(BetaOutputConfig.Effort.LOW).build())
            .addUserMessage("Reply with the single word: ok")
            .build()
        return http { sdk.beta().messages().create(params).model().asString() }
    }

    /**
     * Releases connections and threads. Calls still in flight end with [InterruptedException].
     * Safe to call from any thread, the Android main thread included.
     */
    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        inFlight.forEach { it.cancel(true) }
        if (sdkLazy.isInitialized()) {
            // Closing pooled TLS connections writes to the network, which Android forbids on the
            // main thread, so it is handed to a request thread.
            try {
                executor.execute {
                    try {
                        sdk.close()
                    } catch (_: RuntimeException) {
                        // Nothing useful can be done about a failure to release connections.
                    }
                }
            } catch (_: RejectedExecutionException) {
                // Only possible if the executor was shut down already; nothing left to release then.
            }
        }
        executor.shutdown()
    }

    // ---------------------------------------------------------------- tool loop

    private class ToolCall(val id: String, val name: String, val input: JsonValue)

    /** One assistant turn, reduced to what the loop needs; [echo] is the turn as it must be sent back. */
    private class Turn(
        val model: String,
        val inputTokens: Long,
        val outputTokens: Long,
        val toolCalls: List<ToolCall>,
        val text: String,
        val echo: BetaMessageParam,
    )

    private inner class ToolLoop(
        private val vision: Boolean,
        knownFen: String?,
        private val forcedSide: Char?,
        private val oracle: PositionOracle,
        private val onProgress: (String) -> Unit,
    ) {
        /** Append-only: earlier entries are never rebuilt, trimmed or reordered. */
        private val messages = ArrayList<BetaMessageParam>()
        private var reading: BoardReading? = null
        private var fen: String? = knownFen
        private var commentary = ""
        private var inputTokens = 0L
        private var outputTokens = 0L
        private var servedBy = config.model
        private var roundTrips = 0
        private var nudged = false
        private var progress = if (vision) "Reading the board…" else "Choosing a move…"

        fun run(firstMessage: BetaMessageParam): AnalysisResult {
            messages += firstMessage
            val limit = config.maxRoundTrips.coerceAtLeast(1)
            while (roundTrips < limit) {
                onProgress(progress)
                val turn = send(messages)
                roundTrips++
                inputTokens += turn.inputTokens
                outputTokens += turn.outputTokens
                servedBy = turn.model
                if (turn.text.isNotEmpty()) commentary = turn.text

                if (turn.toolCalls.isEmpty()) {
                    // tool_choice "auto" does not guarantee a call. Before a position exists, a
                    // sentence is the expected way to say "no board here". After that, or when the
                    // reply is empty, ask once more for the tool call.
                    val expectedText = fen == null && turn.text.isNotEmpty()
                    if (expectedText || nudged) return result(advice = null)
                    nudged = true
                    messages += turn.echo
                    messages += BetaMessageParam.ofUserOfContentBlockParams(
                        listOf(textBlock(if (fen == null) Prompts.NUDGE_EMPTY_REPLY else Prompts.NUDGE_RECOMMEND, cacheBreakpoint = false))
                    )
                    continue
                }

                val results = ArrayList<BetaContentBlockParam>(turn.toolCalls.size)
                for (call in turn.toolCalls) {
                    val advice = handle(call, results)
                    if (advice != null) return result(advice)
                }
                messages += turn.echo
                messages += BetaMessageParam.ofUserOfContentBlockParams(results)
            }
            return result(advice = null)
        }

        /** Answers one tool call by appending its result to [results]; returns the advice when the call settles the analysis. */
        private fun handle(call: ToolCall, results: MutableList<BetaContentBlockParam>): MoveAdvice? {
            when (call.name) {
                ChessTools.SUBMIT_POSITION -> results += submitPosition(call)
                ChessTools.RECOMMEND_MOVE -> {
                    val position = fen
                    if (position == null) {
                        results += toolResult(call, Prompts.POSITION_NOT_ACCEPTED_YET, isError = true)
                        return null
                    }
                    when (val parsed = ChessTools.parseRecommendation(call.input)) {
                        is Parsed.Bad ->
                            results += toolResult(call, Prompts.recommendationMalformed(parsed.problems.joinToString(" ")), isError = true)
                        is Parsed.Ok -> {
                            onProgress("Checking the move…")
                            when (val verdict = oracle.checkMove(position, parsed.value.move)) {
                                is MoveVerdict.Legal -> return advice(position, parsed.value, verdict)
                                is MoveVerdict.Illegal -> {
                                    results += toolResult(call, Prompts.moveIllegal(parsed.value.move, verdict.reason), isError = true)
                                    progress = "Choosing another move…"
                                }
                            }
                        }
                    }
                }
                else -> results += toolResult(call, Prompts.unknownTool(call.name), isError = true)
            }
            return null
        }

        private fun submitPosition(call: ToolCall): BetaContentBlockParam {
            if (!vision) return toolResult(call, Prompts.POSITION_ALREADY_KNOWN, isError = true)
            val submitted = when (val parsed = ChessTools.parseReading(call.input)) {
                is Parsed.Ok -> parsed.value
                is Parsed.Bad -> {
                    progress = "Reading the board again…"
                    return toolResult(call, Prompts.readingMalformed(parsed.problems), isError = true)
                }
            }
            // The user's override is authoritative even if Claude reported the other side.
            val overridden = forcedSide != null && forcedSide != submitted.sideToMove
            val candidate = if (forcedSide != null && overridden) submitted.copy(sideToMove = forcedSide) else submitted
            reading = candidate

            onProgress("Checking the position…")
            return when (val verdict = oracle.submitPosition(candidate)) {
                is PositionVerdict.Accepted -> {
                    fen = verdict.fen
                    progress = "Choosing a move…"
                    val note =
                        if (overridden) "The user has set ${Prompts.sideName(candidate.sideToMove)} to move, so that is used." else null
                    toolResult(call, Prompts.positionAccepted(verdict.fen, verdict.briefing, note), isError = false)
                }
                is PositionVerdict.Rejected -> {
                    // A reading that was accepted earlier in this exchange stays the tracked position.
                    progress = "Reading the board again…"
                    toolResult(call, Prompts.positionRejected(verdict.problems), isError = true)
                }
            }
        }

        private fun advice(position: String, recommendation: Recommendation, verdict: MoveVerdict.Legal): MoveAdvice {
            val alternatives = ArrayList<Alternative>(ChessTools.ALTERNATIVE_LIMIT)
            for (candidate in recommendation.alternatives) {
                if (alternatives.size == ChessTools.ALTERNATIVE_LIMIT) break
                val legal = oracle.checkMove(position, candidate.san) as? MoveVerdict.Legal ?: continue
                if (legal.uci == verdict.uci || alternatives.any { it.san == legal.san }) continue
                alternatives += Alternative(legal.san, candidate.note)
            }
            return MoveAdvice(
                san = verdict.san,
                uci = verdict.uci,
                explanation = recommendation.explanation,
                evaluation = recommendation.evaluation,
                alternatives = alternatives,
                opponentThreat = recommendation.opponentThreat,
            )
        }

        private fun result(advice: MoveAdvice?): AnalysisResult {
            val note = when {
                advice != null || commentary.isNotEmpty() -> commentary
                fen != null -> Prompts.NOTE_NO_LEGAL_MOVE
                reading != null -> Prompts.NOTE_NO_VALID_READING
                else -> Prompts.NOTE_NO_POSITION
            }
            return AnalysisResult(
                reading = reading,
                fen = fen,
                advice = advice,
                commentary = note,
                usage = TokenUsage(inputTokens, outputTokens),
                servedBy = servedBy,
                roundTrips = roundTrips,
            )
        }
    }

    // ---------------------------------------------------------------- requests

    private fun send(messages: List<BetaMessageParam>): Turn {
        val withFallback = fallbackOptIn.get()
        return try {
            http { read(sdk.beta().messages().create(messageParams(messages, withFallback))) }
        } catch (rejected: ClaudeException.BadRequest) {
            if (!withFallback || !isFallbackOptInRejection(rejected)) throw rejected
            fallbackOptIn.set(false)
            http { read(sdk.beta().messages().create(messageParams(messages, withFallback = false))) }
        }
    }

    /**
     * Everything except [messages] is identical on every request of this client: thinking blocks are
     * only valid while the system prompt, the tool list and the earlier messages stay as they were.
     */
    private fun messageParams(messages: List<BetaMessageParam>, withFallback: Boolean): MessageCreateParams {
        val builder = MessageCreateParams.builder()
            .model(config.model)
            // Thinking is adaptive and always on for these models and counts towards max_tokens, so
            // the limit has to leave room for it on top of the tool call. No `thinking` parameter,
            // no sampling parameters and no prefill: each of those is a 400 on at least one model.
            .maxTokens(MAX_TOKENS)
            .systemOfBetaTextBlockParams(SYSTEM_BLOCKS)
            .outputConfig(BetaOutputConfig.builder().effort(effort).build())
            // Forced tool choice ("any" / "tool") is a 400 on three of the four models.
            .toolChoice(TOOL_CHOICE)
            .messages(ArrayList(messages))
        ChessTools.all.forEach { builder.addTool(it) }
        if (withFallback) {
            // Lets the API re-run a request that a safety classifier declined on the fallback model
            // Anthropic recommends, instead of returning the refusal.
            builder.addBeta(AnthropicBeta.SERVER_SIDE_FALLBACK_2026_07_01).fallbacksDefault()
        }
        return builder.build()
    }

    /** Runs on a request thread. Looks at `stop_reason` before touching `content`. */
    private fun read(response: BetaMessage): Turn {
        when (response.stopReason().orElse(null)?.value()) {
            // A refusal carries no usable content: empty, or cut off mid-way.
            BetaStopReason.Value.REFUSAL ->
                throw ClaudeException.Refused(
                    response.stopDetails().flatMap { it.category() }.map { it.asString() }.orElse(null)
                )
            // A tool call cut off by the limit can still parse as a smaller, wrong object.
            BetaStopReason.Value.MAX_TOKENS, BetaStopReason.Value.MODEL_CONTEXT_WINDOW_EXCEEDED ->
                throw ClaudeException.Truncated()
            else -> Unit
        }

        val toolCalls = ArrayList<ToolCall>()
        val text = StringBuilder()
        for (block in response.content()) {
            if (block.isToolUse()) {
                val use = block.asToolUse()
                toolCalls += ToolCall(use.id(), use.name(), use._input())
            } else if (block.isText()) {
                val part = block.asText().text().trim()
                if (part.isNotEmpty()) {
                    if (text.isNotEmpty()) text.append('\n')
                    text.append(part)
                }
            }
        }
        val usage = response.usage()
        return Turn(
            model = response.model().asString(),
            inputTokens = usage.inputTokens() +
                usage.cacheCreationInputTokens().orElse(0L) +
                usage.cacheReadInputTokens().orElse(0L),
            outputTokens = usage.outputTokens(),
            toolCalls = toolCalls,
            text = text.toString(),
            // The SDK's own conversion, so thinking blocks (with their signatures), tool calls and
            // fallback blocks go back exactly as they came.
            echo = response.toParam(),
        )
    }

    /**
     * Runs [call] on a request thread and waits for it. Interrupting the caller abandons the wait at
     * once and interrupts the request thread, which aborts an HTTP/2 exchange immediately.
     */
    private fun <T> http(call: () -> T): T {
        if (Thread.interrupted()) throw InterruptedException("Interrupted before the Claude request was sent")
        val future: Future<T> = try {
            executor.submit(Callable { call() })
        } catch (shutDown: RejectedExecutionException) {
            throw IllegalStateException("ClaudeChessClient is closed", shutDown)
        }
        inFlight += future
        try {
            // close() may have run between the submit and the line above and missed this future.
            if (closed.get()) future.cancel(true)
            return future.get()
        } catch (interrupted: InterruptedException) {
            future.cancel(true)
            throw interrupted
        } catch (cancelled: CancellationException) {
            throw InterruptedException("The Claude request was cancelled")
        } catch (failed: ExecutionException) {
            throw ErrorMapping.map(failed.cause ?: failed, config.model)
        } finally {
            inFlight -= future
        }
    }

    /** Catches the configuration mistakes that would otherwise surface as obscure transport errors. */
    private fun ensureUsable() {
        check(!closed.get()) { "ClaudeChessClient is closed" }
        val key = config.apiKey.trim()
        if (key.isEmpty()) throw ClaudeException.InvalidApiKey("No API key is configured")
        // Anything else cannot be sent in an HTTP header; typically a line break from a bad paste.
        if (key.any { it < '!' || it > '~' }) {
            throw ClaudeException.InvalidApiKey("The API key contains spaces or other characters that cannot be part of a key")
        }
        val baseUrl = normalizeBaseUrl(config.baseUrl) ?: return
        val host = when {
            baseUrl.startsWith("https://", ignoreCase = true) -> baseUrl.substring("https://".length)
            baseUrl.startsWith("http://", ignoreCase = true) -> baseUrl.substring("http://".length)
            else -> throw ClaudeException.BadRequest("The custom base URL must start with http:// or https://")
        }
        if (host.isEmpty() || host.any { it.isWhitespace() }) {
            throw ClaudeException.BadRequest("The custom base URL is not a valid address")
        }
    }

    private fun createSdkClient(): AnthropicClient {
        val requestTimeout = Duration.ofSeconds(config.requestTimeoutSeconds.coerceAtLeast(1))
        val builder = AnthropicOkHttpClient.builder()
            .apiKey(config.apiKey.trim())
            .maxRetries(config.maxRetries.coerceAtLeast(0))
            .timeout(
                Timeout.builder()
                    .connect(minOf(requestTimeout, CONNECT_TIMEOUT))
                    .request(requestTimeout)
                    .build()
            )
        normalizeBaseUrl(config.baseUrl)?.let { baseUrl ->
            builder.baseUrl(baseUrl)
            // The SDK sends the key as "Authorization: Bearer", which api.anthropic.com expects from
            // it. Gateways written against earlier SDK versions read "X-Api-Key" instead, and there
            // is no telling which kind sits behind a custom URL, so that one gets both.
            val key = config.apiKey.trim()
            builder.putHeader("X-Api-Key", key).putHeader("Authorization", "Bearer $key")
        }
        return builder.build()
    }

    private fun imageBlock(image: EncodedImage): BetaContentBlockParam {
        val mediaType = when (image.mediaType.trim().lowercase()) {
            "image/png" -> BetaBase64ImageSource.MediaType.IMAGE_PNG
            "image/jpeg", "image/jpg" -> BetaBase64ImageSource.MediaType.IMAGE_JPEG
            else -> throw IllegalArgumentException("Unsupported image media type: ${image.mediaType}")
        }
        require(image.bytes.isNotEmpty()) { "The image is empty" }
        return BetaContentBlockParam.ofImage(
            BetaImageBlockParam.builder()
                .source(
                    BetaBase64ImageSource.builder()
                        .data(Base64.getEncoder().encodeToString(image.bytes))
                        .mediaType(mediaType)
                        .build()
                )
                .build()
        )
    }

    private fun textBlock(text: String, cacheBreakpoint: Boolean): BetaContentBlockParam {
        val block = BetaTextBlockParam.builder().text(text)
        if (cacheBreakpoint) block.cacheControl(CACHE_BREAKPOINT)
        return BetaContentBlockParam.ofText(block.build())
    }

    private fun toolResult(call: ToolCall, text: String, isError: Boolean): BetaContentBlockParam {
        val block = BetaToolResultBlockParam.builder().toolUseId(call.id).content(text)
        if (isError) block.isError(true)
        return BetaContentBlockParam.ofToolResult(block.build())
    }

    private companion object {
        /** Room for adaptive thinking plus the tool call; small enough to stay a non-streaming request. */
        const val MAX_TOKENS = 16_000L
        const val PING_MAX_TOKENS = 16L
        val CONNECT_TIMEOUT: Duration = Duration.ofSeconds(30)
        val THREAD_COUNTER = AtomicInteger()

        val CACHE_BREAKPOINT: BetaCacheControlEphemeral = BetaCacheControlEphemeral.builder().build()

        /** The breakpoint caches the tool list and the system prompt, which every request of the app shares. */
        val SYSTEM_BLOCKS: List<BetaTextBlockParam> = listOf(
            BetaTextBlockParam.builder().text(Prompts.SYSTEM).cacheControl(CACHE_BREAKPOINT).build()
        )

        val TOOL_CHOICE: BetaToolChoiceAuto = BetaToolChoiceAuto.builder().disableParallelToolUse(true).build()

        /**
         * A 400 that complains about the fallback opt-in itself: an account or gateway on which the
         * `server-side-fallback` beta header or the `fallbacks` field is not accepted. The API offers
         * no error code for this, only the message, which names the header value or the field.
         */
        fun isFallbackOptInRejection(error: ClaudeException.BadRequest): Boolean =
            error.apiMessage.contains("fallback", ignoreCase = true)

        /** Accepts what people paste: trailing slashes and a trailing `/v1`, which the SDK adds itself. */
        fun normalizeBaseUrl(raw: String?): String? {
            var url = raw?.trim().orEmpty().trimEnd('/')
            if (url.endsWith("/v1", ignoreCase = true)) url = url.dropLast(3).trimEnd('/')
            return url.ifEmpty { null }
        }
    }
}
