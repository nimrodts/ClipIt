package com.nimroddayan.clipit.data.gemini

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

data class GeminiModelInfo(val id: String, val displayName: String, val description: String?)

/**
 * Minimal client for the Gemini Developer API (generativelanguage.googleapis.com) using the user's
 * own API key. The key is sent only in the x-goog-api-key header.
 */
class GeminiRestClient(
    private val httpClient: OkHttpClient = defaultHttpClient(),
    baseUrl: String = DEFAULT_BASE_URL,
) {
    private val baseUrl: HttpUrl = baseUrl.toHttpUrl()

    suspend fun listModels(apiKey: String): List<GeminiModelInfo> = withContext(Dispatchers.IO) {
        val models = mutableListOf<GeminiModelInfo>()
        var pageToken: String? = null
        do {
            val url = baseUrl.newBuilder()
                .addPathSegment("models")
                .addQueryParameter("pageSize", "1000")
                .apply { pageToken?.let { addQueryParameter("pageToken", it) } }
                .build()
            val page = json.decodeFromString<ListModelsResponse>(execute(Request.Builder().url(url).get(), apiKey))
            page.models
                .filter { GENERATE_CONTENT in it.supportedGenerationMethods }
                .mapTo(models) {
                    val id = it.name.removePrefix("models/")
                    GeminiModelInfo(id = id, displayName = it.displayName ?: id, description = it.description)
                }
            pageToken = page.nextPageToken?.takeIf { it.isNotBlank() }
        } while (pageToken != null)
        models
    }

    suspend fun generateJson(
        apiKey: String,
        modelId: String,
        systemInstruction: String,
        userText: String,
        temperature: Float,
        responseSchema: JsonObject? = null,
    ): String = withContext(Dispatchers.IO) {
        val url = baseUrl.newBuilder()
            .addPathSegment("models")
            .addPathSegment("$modelId:$GENERATE_CONTENT")
            .build()
        val body = buildJsonObject {
            putJsonObject("systemInstruction") {
                putJsonArray("parts") { addJsonObject { put("text", systemInstruction) } }
            }
            putJsonArray("contents") {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("parts") { addJsonObject { put("text", userText) } }
                }
            }
            putJsonObject("generationConfig") {
                put("temperature", temperature)
                put("responseMimeType", "application/json")
                if (responseSchema != null) put("responseSchema", responseSchema)
            }
        }
        val request = Request.Builder().url(url).post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
        val response = json.decodeFromString<GenerateContentResponse>(execute(request, apiKey))

        val candidate = response.candidates.firstOrNull()
        val text = candidate?.content?.parts?.mapNotNull { it.text }?.joinToString("")
        if (text.isNullOrBlank()) {
            val reason = response.promptFeedback?.blockReason ?: candidate?.finishReason ?: "no candidates"
            throw GeminiEmptyResponseException("Gemini returned no content ($reason)")
        }
        text
    }


    private fun execute(builder: Request.Builder, apiKey: String): String {
        val request = builder.header(API_KEY_HEADER, apiKey).build()
        httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw toApiException(response.code, body)
            return body
        }
    }


    private fun toApiException(code: Int, body: String): GeminiApiException {
        val error = runCatching { json.decodeFromString<ErrorResponse>(body).error }.getOrNull()
        val message = error?.message ?: "HTTP $code"
        val keyInvalid = error?.details.orEmpty().any { it.reason == "API_KEY_INVALID" }
        return when {
            code == 429 -> GeminiQuotaException(message)
            keyInvalid || code == 401 || code == 403 -> InvalidApiKeyException(message, code)
            else -> GeminiApiException(code, message)
        }
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"
        private const val API_KEY_HEADER = "x-goog-api-key"
        private const val GENERATE_CONTENT = "generateContent"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private val json = Json { ignoreUnknownKeys = true }

        private fun defaultHttpClient(): OkHttpClient =
            OkHttpClient.Builder().callTimeout(60, TimeUnit.SECONDS).build()
    }
}

@Serializable
private data class ListModelsResponse(val models: List<ModelDto> = emptyList(), val nextPageToken: String? = null)

@Serializable
private data class ModelDto(
    val name: String,
    val displayName: String? = null,
    val description: String? = null,
    val supportedGenerationMethods: List<String> = emptyList(),
)

@Serializable
private data class GenerateContentResponse(
    val candidates: List<CandidateDto> = emptyList(),
    val promptFeedback: PromptFeedbackDto? = null,
)

@Serializable
private data class CandidateDto(val content: ContentDto? = null, val finishReason: String? = null)

@Serializable
private data class ContentDto(val parts: List<PartDto> = emptyList())

@Serializable
private data class PartDto(val text: String? = null)

@Serializable
private data class PromptFeedbackDto(val blockReason: String? = null)

@Serializable
private data class ErrorResponse(val error: ErrorDto)

@Serializable
private data class ErrorDto(val message: String? = null, val details: List<ErrorDetailDto> = emptyList())

@Serializable
private data class ErrorDetailDto(val reason: String? = null)
