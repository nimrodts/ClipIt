package com.nimroddayan.clipit.data.gemini

class InvalidApiKeyException(message: String, httpCode: Int = 400) : GeminiApiException(httpCode, message)
