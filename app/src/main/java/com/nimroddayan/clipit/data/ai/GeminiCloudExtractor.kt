package com.nimroddayan.clipit.data.ai

import com.nimroddayan.clipit.data.gemini.GeminiRestClient
import com.nimroddayan.clipit.data.gemini.ParsedCoupon
import com.nimroddayan.clipit.data.model.Category
import java.io.IOException
import java.time.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Extracts coupon fields with Gemini, using the user's key and model from [settings]. When [categories] are
 * given, the model picks one of them and the result carries its id.
 */
class GeminiCloudExtractor(
    private val client: GeminiRestClient,
    private val settings: AiSettings,
    private val today: LocalDate,
    private val categories: List<Category> = emptyList(),
) : CouponExtractor {

    override suspend fun extract(text: String): ParsedCoupon {
        val names = categories.map { it.name.trim() }.filter { it.isNotEmpty() }.distinct()
        val raw = client.generateJson(
            apiKey = settings.apiKey,
            modelId = settings.modelId,
            systemInstruction = CouponPrompt.systemInstruction(today, names),
            userText = CouponPrompt.userMessage(text),
            temperature = settings.temperature,
            responseSchema = CouponPrompt.responseSchema(names),
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
            categoryId = matchCategory(dto.category, categories),
        )
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /** The id of the category named [name], ignoring case and surrounding spaces; null if there's none. */
        internal fun matchCategory(name: String?, categories: List<Category>): Long? {
            val wanted = name?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            return categories.firstOrNull { it.name.trim().equals(wanted, ignoreCase = true) }?.id
        }
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
    val category: String? = null,
)
