package com.nimroddayan.clipit.data.gemini

import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelTesterTest {

    private fun tester(call: suspend (String, String) -> Unit) = ModelTester(call, now = { 42L })

    private suspend fun resultOf(error: Exception) = tester { _, _ -> throw error }.test("k", "m")

    @Test
    fun `successful call means the model works`() = runTest {
        var called: Pair<String, String>? = null
        val result = tester { key, model -> called = key to model }.test("key", "gemini-a")
        assertEquals("key" to "gemini-a", called)
        assertEquals(ModelTestResult(ModelTestStatus.WORKS, null, 42L), result)
    }

    @Test
    fun `quota errors map to their own statuses`() = runTest {
        assertEquals(ModelTestStatus.NO_FREE_QUOTA, resultOf(GeminiQuotaException("q", QuotaLimit.NO_FREE_QUOTA)).status)
        assertEquals(ModelTestStatus.DAILY_LIMIT, resultOf(GeminiQuotaException("q", QuotaLimit.PER_DAY)).status)
        val rate = resultOf(GeminiQuotaException("q", QuotaLimit.PER_MINUTE, retryAfterSeconds = 30))
        assertEquals(ModelTestStatus.RATE_LIMITED, rate.status)
        assertEquals("30", rate.detail)
        assertEquals(ModelTestStatus.RATE_LIMITED, resultOf(GeminiQuotaException("q")).status)
    }

    @Test
    fun `other errors keep the message`() = runTest {
        assertEquals(ModelTestStatus.INVALID_KEY, resultOf(InvalidApiKeyException("bad key", 400)).status)
        val error = resultOf(GeminiApiException(404, "model not found"))
        assertEquals(ModelTestResult(ModelTestStatus.ERROR, "model not found", 42L), error)
        assertEquals(ModelTestStatus.ERROR, resultOf(IOException("offline")).status)
    }

    @Test
    fun `testAll reports every model`() = runTest {
        val results = mutableMapOf<String, ModelTestStatus>()
        tester { _, model -> if (model == "b") throw GeminiQuotaException("q", QuotaLimit.NO_FREE_QUOTA) }
            .testAll("k", listOf("a", "b", "c")) { model, result -> results[model] = result.status }

        assertEquals(
            mapOf("a" to ModelTestStatus.WORKS, "b" to ModelTestStatus.NO_FREE_QUOTA, "c" to ModelTestStatus.WORKS),
            results
        )
    }

    @Test
    fun `testAll stops after the key is rejected`() = runTest {
        val calls = AtomicInteger()
        val results = mutableListOf<ModelTestStatus>()
        tester { _, _ -> calls.incrementAndGet(); throw InvalidApiKeyException("bad key", 400) }
            .testAll("k", (1..10).map { "m$it" }) { _, result -> results += result.status }

        assertTrue("calls=${calls.get()}", calls.get() <= ModelTester.CONCURRENCY)
        assertTrue(results.all { it == ModelTestStatus.INVALID_KEY })
    }

    @Test
    fun `testAll runs at most three tests at a time`() = runTest {
        val running = AtomicInteger()
        var maxRunning = 0
        tester { _, _ ->
            maxRunning = maxOf(maxRunning, running.incrementAndGet())
            delay(100)
            running.decrementAndGet()
        }.testAll("k", (1..10).map { "m$it" }) { _, _ -> }

        assertEquals(ModelTester.CONCURRENCY, maxRunning)
    }

    // ========== stored results ==========

    @Test
    fun `stored results round trip for the same key`() {
        val works = ModelTestResult(ModelTestStatus.WORKS, null, 1L)
        val json = ModelTestStore.put(null, "key-1", "gemini-a", works)
        assertEquals(mapOf("gemini-a" to works), ModelTestStore.read(json, "key-1"))
    }

    @Test
    fun `results of another api key are not shown and get replaced`() {
        val works = ModelTestResult(ModelTestStatus.WORKS, null, 1L)
        val old = ModelTestStore.put(null, "key-1", "gemini-a", works)
        assertEquals(emptyMap<String, ModelTestResult>(), ModelTestStore.read(old, "key-2"))

        val replaced = ModelTestStore.put(old, "key-2", "gemini-b", works)
        assertEquals(mapOf("gemini-b" to works), ModelTestStore.read(replaced, "key-2"))
    }

    @Test
    fun `the api key itself is never stored`() {
        val json = ModelTestStore.put(null, "AIzaSecret", "gemini-a", ModelTestResult(ModelTestStatus.WORKS, null, 1L))
        assertTrue(json, !json.contains("AIzaSecret"))
    }

    @Test
    fun `corrupt stored data reads as empty`() {
        assertEquals(emptyMap<String, ModelTestResult>(), ModelTestStore.read("not json", "k"))
    }
}
