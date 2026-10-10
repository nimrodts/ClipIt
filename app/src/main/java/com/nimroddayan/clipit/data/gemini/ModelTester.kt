package com.nimroddayan.clipit.data.gemini

import java.security.MessageDigest
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class ModelTestStatus {
    WORKS,
    NO_FREE_QUOTA,
    DAILY_LIMIT,
    RATE_LIMITED,
    INVALID_KEY,
    ERROR,
}


/** Outcome of a test request; [detail] is the retry delay for RATE_LIMITED and the message for ERROR. */
@Serializable
data class ModelTestResult(val status: ModelTestStatus, val detail: String? = null, val testedAt: Long) {

    /** Short tag for the model dropdown. */
    val badge: String
        get() = when (status) {
            ModelTestStatus.WORKS -> "✅ works"
            ModelTestStatus.NO_FREE_QUOTA -> "❌ no free quota"
            ModelTestStatus.DAILY_LIMIT -> "⏳ daily limit reached"
            ModelTestStatus.RATE_LIMITED -> "⏳ busy"
            ModelTestStatus.INVALID_KEY -> "❌ key rejected"
            ModelTestStatus.ERROR -> "❌ error"
        }

    val description: String
        get() = when (status) {
            ModelTestStatus.WORKS -> "✅ Works with your key"
            ModelTestStatus.NO_FREE_QUOTA -> "❌ No free quota for this model on your key"
            ModelTestStatus.DAILY_LIMIT -> "⏳ Daily limit reached: the model is free, but today's requests are used up"
            ModelTestStatus.RATE_LIMITED ->
                "⏳ Too many requests right now" + (detail?.let { ", try again in $it seconds" } ?: ", try again soon")
            ModelTestStatus.INVALID_KEY -> "❌ API key rejected"
            ModelTestStatus.ERROR -> "❌ Error: ${detail ?: "unknown"}"
        }

    val isFailure: Boolean
        get() = status != ModelTestStatus.WORKS
}


/** Checks which Gemini models answer for an API key, one short request per model. */
class ModelTester(
    private val testCall: suspend (apiKey: String, modelId: String) -> Unit,
    private val now: () -> Long = System::currentTimeMillis,
) {

    suspend fun test(apiKey: String, modelId: String): ModelTestResult =
        try {
            testCall(apiKey, modelId)
            ModelTestResult(ModelTestStatus.WORKS, null, now())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            resultFor(e)
        }


    /** Tests [modelIds] a few at a time, stopping once the key is rejected since every test would fail. */
    suspend fun testAll(
        apiKey: String,
        modelIds: List<String>,
        onResult: suspend (modelId: String, result: ModelTestResult) -> Unit,
    ) = coroutineScope {
        val permits = Semaphore(CONCURRENCY)
        var keyRejected = false
        for (modelId in modelIds) {
            launch {
                permits.withPermit {
                    if (keyRejected) return@withPermit
                    val result = test(apiKey, modelId)
                    if (result.status == ModelTestStatus.INVALID_KEY) keyRejected = true
                    onResult(modelId, result)
                }
            }
        }
    }


    private fun resultFor(e: Exception): ModelTestResult {
        val (status, detail) = when (e) {
            is GeminiQuotaException -> when (e.limit) {
                QuotaLimit.NO_FREE_QUOTA -> ModelTestStatus.NO_FREE_QUOTA to null
                QuotaLimit.PER_DAY -> ModelTestStatus.DAILY_LIMIT to null
                QuotaLimit.PER_MINUTE, QuotaLimit.UNKNOWN ->
                    ModelTestStatus.RATE_LIMITED to e.retryAfterSeconds?.toString()
            }
            is InvalidApiKeyException -> ModelTestStatus.INVALID_KEY to null
            else -> ModelTestStatus.ERROR to (e.message ?: e.javaClass.simpleName)
        }
        return ModelTestResult(status, detail, now())
    }

    companion object {
        const val CONCURRENCY = 3
    }
}


/**
 * Stored test results, kept as JSON in settings. They belong to one API key, identified by a hash so
 * the key itself is never stored twice; results for any other key read as empty.
 */
object ModelTestStore {

    @Serializable
    private data class Stored(val keyHash: String, val results: Map<String, ModelTestResult>)

    private val json = Json { ignoreUnknownKeys = true }

    fun read(stored: String?, apiKey: String): Map<String, ModelTestResult> =
        decode(stored)?.takeIf { it.keyHash == hash(apiKey) }?.results.orEmpty()


    fun put(stored: String?, apiKey: String, modelId: String, result: ModelTestResult): String =
        json.encodeToString(Stored(hash(apiKey), read(stored, apiKey) + (modelId to result)))


    private fun decode(stored: String?): Stored? =
        stored?.let { runCatching { json.decodeFromString<Stored>(it) }.getOrNull() }


    private fun hash(apiKey: String): String =
        MessageDigest.getInstance("SHA-256").digest(apiKey.toByteArray())
            .take(16).joinToString("") { "%02x".format(it) }
}
