package com.nimroddayan.clipit.data.gemini

/** An error response (or unusable success response) from the Gemini REST API. */
open class GeminiApiException(val httpCode: Int, message: String) : Exception(message)

class GeminiQuotaException(message: String) : GeminiApiException(429, message)

class GeminiEmptyResponseException(message: String) : GeminiApiException(200, message)
