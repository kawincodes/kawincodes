package com.krstech.chesshelp.claude

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** HTTP errors, transport failures and cancellation. */
class ClaudeFailureTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private inline fun <reified T : ClaudeException> failure(config: ClaudeConfig = server.config()): T =
        assertFailsWith<T> { ClaudeChessClient(config).use { it.analyzeBoard(visionRequest(), FakeOracle()) } }

    private fun assertPresentable(error: ClaudeException) {
        assertTrue(error.userMessage.isNotBlank())
        assertTrue(error.userMessage.length <= 180, "too long for the overlay: ${error.userMessage}")
        assertFalse(error.userMessage.contains("sk-ant"), "the key must never be shown")
        assertFalse(error.userMessage.contains('\n'))
    }

    @Test
    fun `401 is an invalid api key`() {
        server.enqueue(errorResponse(401, "authentication_error", "invalid x-api-key"))

        val error = failure<ClaudeException.InvalidApiKey>()

        assertPresentable(error)
        assertContains(error.message!!, "invalid x-api-key")
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `403 is permission denied`() {
        server.enqueue(errorResponse(403, "permission_error", "Your API key does not have permission to use the specified resource."))

        assertPresentable(failure<ClaudeException.PermissionDenied>())
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `404 is an unknown model`() {
        server.enqueue(errorResponse(404, "not_found_error", "model: claude-opus-9"))

        val error = failure<ClaudeException.ModelNotFound>(server.config(model = "claude-opus-9"))

        assertEquals("claude-opus-9", error.model)
        assertContains(error.userMessage, "claude-opus-9")
        assertPresentable(error)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `429 is a rate limit`() {
        server.enqueue(errorResponse(429, "rate_limit_error", "This request would exceed your organization's rate limit of 30,000 input tokens per minute."))

        val error = failure<ClaudeException.RateLimited>()

        assertFalse(error.outOfCredit)
        assertContains(error.userMessage, "Rate limit")
        assertPresentable(error)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `billing errors are reported as out of credit`() {
        server.enqueue(errorResponse(402, "billing_error", "Your credit balance is too low to access the Anthropic API."))
        server.enqueue(
            errorResponse(400, "invalid_request_error", "Your credit balance is too low to access the Anthropic API. Please go to Plans & Billing to upgrade or purchase credits.")
        )

        repeat(2) {
            val error = failure<ClaudeException.RateLimited>()
            assertTrue(error.outOfCredit)
            assertContains(error.userMessage, "credit")
            assertPresentable(error)
        }
    }

    @Test
    fun `529 is overloaded`() {
        server.enqueue(errorResponse(529, "overloaded_error", "Overloaded"))

        assertPresentable(failure<ClaudeException.Overloaded>())
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `500 is a server error`() {
        server.enqueue(errorResponse(500, "api_error", "Internal server error"))

        val error = failure<ClaudeException.ServerError>()

        assertEquals(500, error.statusCode)
        assertPresentable(error)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `400 carries the api message`() {
        server.enqueue(errorResponse(400, "invalid_request_error", "messages.0.content.0.image.source.base64.data: Image does not match the provided media type image/png"))

        val error = failure<ClaudeException.BadRequest>()

        assertEquals("messages.0.content.0.image.source.base64.data: Image does not match the provided media type image/png", error.apiMessage)
        assertContains(error.userMessage, "Image does not match")
        assertPresentable(error)
    }

    @Test
    fun `413 is a bad request about size`() {
        server.enqueue(errorResponse(413, "request_too_large", "Request exceeds the maximum allowed number of bytes."))

        assertPresentable(failure<ClaudeException.BadRequest>())
    }

    @Test
    fun `an error body that is not json still maps by status`() {
        server.enqueue(MockResponse().setResponseCode(502).setHeader("content-type", "text/html").setBody("<html>Bad Gateway</html>"))

        val error = failure<ClaudeException.ServerError>()

        assertEquals(502, error.statusCode)
        assertPresentable(error)
    }

    @Test
    fun `a 200 that is not a message is a protocol error`() {
        server.enqueue(okResponse("""{"unexpected":true}"""))

        assertPresentable(failure<ClaudeException.Protocol>())
    }

    @Test
    fun `transient errors are retried by the sdk`() {
        server.enqueue(errorResponse(529, "overloaded_error", "Overloaded").setHeader("retry-after-ms", "5"))
        server.enqueue(okResponse(messageJson(listOf(textBlock("I can't see a chess board on this screen.")), stopReason = "end_turn")))

        val result = ClaudeChessClient(server.config(maxRetries = 2)).use { it.analyzeBoard(visionRequest(), FakeOracle()) }

        assertEquals(1, result.roundTrips)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `retries stop at the configured limit`() {
        repeat(3) { server.enqueue(errorResponse(500, "api_error", "Internal server error").setHeader("retry-after-ms", "5")) }

        failure<ClaudeException.ServerError>(server.config(maxRetries = 1))

        assertEquals(2, server.requestCount)
    }

    @Test
    fun `connection failure is a network error`() {
        val config = server.config()
        server.shutdown()

        val error = failure<ClaudeException.Network>(config)

        assertFalse(error.timedOut)
        assertContains(error.userMessage, "internet connection")
        assertPresentable(error)
    }

    @Test
    fun `a dropped connection is a network error`() {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))

        assertFalse(failure<ClaudeException.Network>().timedOut)
    }

    @Test
    fun `no answer within the timeout is a network timeout`() {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        val started = System.nanoTime()

        val error = failure<ClaudeException.Network>(server.config(requestTimeoutSeconds = 1))

        val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)
        assertTrue(error.timedOut, "expected a timeout, got: ${error.message}")
        assertContains(error.userMessage, "too long")
        assertTrue(elapsedMs < 10_000, "the configured timeout was not applied (took $elapsedMs ms)")
    }

    // ------------------------------------------------------------ cancellation

    /**
     * Makes the server sit on every request until the returned latch is released, like an API call
     * that is still thinking. Returns a second latch that opens when a request has arrived.
     */
    private fun holdResponses(): Pair<CountDownLatch, CountDownLatch> {
        val release = CountDownLatch(1)
        val arrived = CountDownLatch(1)
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                arrived.countDown()
                release.await(20, TimeUnit.SECONDS)
                return okResponse(messageJson(listOf(textBlock("late")), stopReason = "end_turn"))
            }
        }
        return release to arrived
    }

    @Test
    fun `interrupting the caller cancels promptly with InterruptedException`() {
        val (release, arrived) = holdResponses()
        val outcome = AtomicReference<Throwable>()
        val finished = CountDownLatch(1)
        val client = ClaudeChessClient(server.config(maxRetries = 2))
        val caller = Thread {
            try {
                client.analyzeBoard(visionRequest(), FakeOracle())
                outcome.set(AssertionError("analyzeBoard returned although it was interrupted"))
            } catch (error: Throwable) {
                outcome.set(error)
            } finally {
                finished.countDown()
            }
        }
        caller.start()
        try {
            assertTrue(arrived.await(5, TimeUnit.SECONDS), "the request should have been sent")

            val interruptedAt = System.nanoTime()
            caller.interrupt()

            assertTrue(finished.await(3, TimeUnit.SECONDS), "the call did not return after the interrupt")
            val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - interruptedAt)
            assertIs<InterruptedException>(outcome.get(), "cancellation must not look like a network error: ${outcome.get()}")
            assertTrue(elapsedMs < 2_000, "cancellation took $elapsedMs ms")
        } finally {
            release.countDown()
        }
        // The abandoned request thread now gets its (late) answer. It must give up there rather
        // than retry: a retry would be a second, paid request for an analysis nobody waits for.
        Thread.sleep(2_500)
        assertEquals(1, server.requestCount, "a cancelled request must not be retried")
        client.close()
    }

    @Test
    fun `a thread that is already interrupted sends nothing`() {
        val outcome = AtomicReference<Throwable>()
        val caller = Thread {
            Thread.currentThread().interrupt()
            try {
                ClaudeChessClient(server.config()).use { it.analyzeBoard(visionRequest(), FakeOracle()) }
            } catch (error: Throwable) {
                outcome.set(error)
            }
        }
        caller.start()
        caller.join(5_000)

        assertIs<InterruptedException>(outcome.get())
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `interrupt between round trips stops before the next request`() {
        server.enqueue(
            okResponse(
                messageJson(listOf(thinkingBlock(SIGNATURE_1), toolUseBlock("toolu_01Submit", "submit_position", submitPositionInput())))
            )
        )
        val outcome = AtomicReference<Throwable>()
        val caller = Thread {
            val oracle = object : PositionOracle {
                override fun submitPosition(reading: BoardReading): PositionVerdict {
                    Thread.currentThread().interrupt()
                    return PositionVerdict.Accepted(START_FEN, START_BRIEFING)
                }

                override fun checkMove(fen: String, move: String): MoveVerdict = throw AssertionError("not reached")
            }
            try {
                ClaudeChessClient(server.config()).use { it.analyzeBoard(visionRequest(), oracle) }
            } catch (error: Throwable) {
                outcome.set(error)
            }
        }
        caller.start()
        caller.join(5_000)

        assertIs<InterruptedException>(outcome.get())
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `closing the client cancels a call in flight`() {
        val (release, arrived) = holdResponses()
        val outcome = AtomicReference<Throwable>()
        val finished = CountDownLatch(1)
        val client = ClaudeChessClient(server.config())
        val caller = Thread {
            try {
                client.analyzeBoard(visionRequest(), FakeOracle())
            } catch (error: Throwable) {
                outcome.set(error)
            } finally {
                finished.countDown()
            }
        }
        caller.start()
        try {
            assertTrue(arrived.await(5, TimeUnit.SECONDS), "the request should have been sent")

            client.close()

            assertTrue(finished.await(3, TimeUnit.SECONDS), "the caller was not released by close()")
            assertIs<InterruptedException>(outcome.get(), "got ${outcome.get()}")
        } finally {
            release.countDown()
        }
    }

    // ------------------------------------------------------------ mapping details

    @Test
    fun `interruption is told apart from timeouts`() {
        assertTrue(ErrorMapping.isInterruption(java.io.InterruptedIOException("interrupted")))
        assertTrue(ErrorMapping.isInterruption(java.io.InterruptedIOException()))
        assertTrue(ErrorMapping.isInterruption(RuntimeException("wrapped", InterruptedException())))
        assertFalse(ErrorMapping.isInterruption(java.io.InterruptedIOException("timeout")))
        assertFalse(ErrorMapping.isInterruption(java.net.SocketTimeoutException("Read timed out")))
        assertFalse(ErrorMapping.isInterruption(java.net.ConnectException("Connection refused")))

        val timeout = ErrorMapping.map(java.net.SocketTimeoutException("timeout"), "claude-opus-5-5")
        assertTrue(assertIs<ClaudeException.Network>(timeout).timedOut)
        val offline = ErrorMapping.map(java.net.UnknownHostException("api.anthropic.com"), "claude-opus-5-5")
        assertFalse(assertIs<ClaudeException.Network>(offline).timedOut)
        assertIs<InterruptedException>(ErrorMapping.map(java.io.InterruptedIOException("interrupted"), "claude-opus-5-5"))
        assertIs<ClaudeException.Protocol>(ErrorMapping.map(IllegalStateException("boom"), "claude-opus-5-5"))
    }
}
