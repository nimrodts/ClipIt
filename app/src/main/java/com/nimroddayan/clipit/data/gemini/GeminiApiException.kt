package com.nimroddayan.clipit.data.gemini

/** An error response (or unusable success response) from the Gemini REST API. */
open class GeminiApiException(val httpCode: Int, message: String) : Exception(message)

/** Which Gemini quota a 429 hit, as far as the error details tell. */
enum class QuotaLimit {
    /** The quota is 0: this model isn't available on the key's tier at all. */
    NO_FREE_QUOTA,
    PER_DAY,
    PER_MINUTE,
    UNKNOWN,
}

class GeminiQuotaException(
    message: String,
    val limit: QuotaLimit = QuotaLimit.UNKNOWN,
    val retryAfterSeconds: Long? = null,
) : GeminiApiException(429, message)

class GeminiEmptyResponseException(message: String) : GeminiApiException(200, message)
