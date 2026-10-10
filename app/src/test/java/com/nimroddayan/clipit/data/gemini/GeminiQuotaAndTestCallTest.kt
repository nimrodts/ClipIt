package com.nimroddayan.clipit.data.gemini

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GeminiQuotaAndTestCallTest {

    private lateinit var server: MockWebServer
    private lateinit var client: GeminiRestClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = GeminiRestClient(OkHttpClient(), server.url("/v1beta/").toString())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun quota429(violations: String, retryDelay: String? = null, message: String = "Quota exceeded") =
        MockResponse().setResponseCode(429).setBody(
            """{"error":{"code":429,"message":"$message","status":"RESOURCE_EXHAUSTED","details":[
                 {"@type":"type.googleapis.com/google.rpc.QuotaFailure","violations":[$violations]}
                 ${retryDelay?.let { """,{"@type":"type.googleapis.com/google.rpc.RetryInfo","retryDelay":"$it"}""" } ?: ""}
               ]}}"""
        )

    private suspend fun quotaError(): GeminiQuotaException {
        val error = runCatching { client.testModel("k", "gemini-x") }.exceptionOrNull()
        assertTrue("got $error", error is GeminiQuotaException)
        return error as GeminiQuotaException
    }

    // ========== 429 details ==========

    @Test
    fun `quota value of zero means no free quota`() = runTest {
        server.enqueue(
            quota429(
                """{"quotaMetric":"generativelanguage.googleapis.com/generate_content_free_tier_requests",
                    "quotaId":"GenerateRequestsPerDayPerProjectPerModel-FreeTier","quotaValue":"0"}"""
            )
        )
        assertEquals(QuotaLimit.NO_FREE_QUOTA, quotaError().limit)
    }

    @Test
    fun `limit 0 in the message also means no free quota`() = runTest {
        server.enqueue(quota429("", message = "Quota exceeded for metric: x, limit: 0, model: gemini-x"))
        assertEquals(QuotaLimit.NO_FREE_QUOTA, quotaError().limit)
    }

    @Test
    fun `per day quota is a daily limit`() = runTest {
        server.enqueue(
            quota429("""{"quotaId":"GenerateRequestsPerDayPerProjectPerModel-FreeTier","quotaValue":"250"}""")
        )
        assertEquals(QuotaLimit.PER_DAY, quotaError().limit)
    }

    @Test
    fun `per minute quota carries the retry delay`() = runTest {
        server.enqueue(
            quota429(
                """{"quotaId":"GenerateRequestsPerMinutePerProjectPerModel-FreeTier","quotaValue":"10"}""",
                retryDelay = "38.5s"
            )
        )
        val error = quotaError()
        assertEquals(QuotaLimit.PER_MINUTE, error.limit)
        assertEquals(39L, error.retryAfterSeconds)
    }

    @Test
    fun `429 without details is an unknown limit`() = runTest {
        server.enqueue(MockResponse().setResponseCode(429).setBody("""{"error":{"message":"Resource exhausted"}}"""))
        val error = quotaError()
        assertEquals(QuotaLimit.UNKNOWN, error.limit)
        assertNull(error.retryAfterSeconds)
    }

    // ========== testModel ==========

    @Test
    fun `testModel sends one tiny generateContent request for the model`() = runTest {
        server.enqueue(MockResponse().setBody("""{"candidates":[]}"""))

        client.testModel("secret-key", "gemini-2.5-flash")

        val request = server.takeRequest()
        assertEquals("/v1beta/models/gemini-2.5-flash:generateContent", request.path)
        assertEquals("secret-key", request.getHeader("x-goog-api-key"))
        val body = request.body.readUtf8()
        assertTrue(body, body.length < 200)
        assertFalse(body.contains("secret-key"))
    }

    @Test
    fun `testModel succeeds even when the model returns no text`() = runTest {
        server.enqueue(MockResponse().setBody("""{"candidates":[{"finishReason":"MAX_TOKENS"}]}"""))
        client.testModel("k", "gemini-2.5-pro")
    }

    @Test
    fun `testModel surfaces api errors`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"error":{"message":"model not found"}}"""))
        val error = runCatching { client.testModel("k", "gone") }.exceptionOrNull()
        assertEquals(404, (error as GeminiApiException).httpCode)
        assertEquals("model not found", error.message)
    }
}
