package com.nimroddayan.clipit.data.ai

import com.nimroddayan.clipit.data.gemini.GeminiRestClient
import com.nimroddayan.clipit.data.gemini.ParsedCoupon
import java.io.IOException
import java.time.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Extracts coupon fields with Gemini, using the user's key and model from [settings]. */
class GeminiCloudExtractor(
    private val client: GeminiRestClient,
    private val settings: AiSettings,
    private val today: LocalDate,
) : CouponExtractor {

    override suspend fun extract(text: String): ParsedCoupon {
        val raw = client.generateJson(
            apiKey = settings.apiKey,
            modelId = settings.modelId,
            systemInstruction = CouponPrompt.systemInstruction(today),
            userText = CouponPrompt.userMessage(text),
            temperature = settings.temperature,
            responseSchema = CouponPrompt.RESPONSE_SCHEMA,
        )
        val dto = try {
            json.decodeFromString<ExtractedCouponDto>(raw)
        } catch (e: SerializationException) {
            throw IOException("Unexpected AI response: ${e.message}", e)
        }
        return ParsedCoupon(
            storeName = dto.storeName,
            initialValue = dto.initialValue,
            expirationDate = dto.expirationDate,
            description = dto.description,
            redeemCode = dto.redeemCode,
            redemptionUrl = dto.redemptionUrl,
        )
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}

@Serializable
private data class ExtractedCouponDto(
    val storeName: String? = null,
    val redeemCode: String? = null,
    val initialValue: Double? = null,
    val expirationDate: String? = null,
    val description: String? = null,
    val redemptionUrl: String? = null,
)
