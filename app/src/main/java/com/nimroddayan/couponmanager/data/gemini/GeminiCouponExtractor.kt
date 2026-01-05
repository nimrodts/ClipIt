package com.nimroddayan.couponmanager.data.gemini

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.nimroddayan.couponmanager.data.gemini.InvalidApiKeyException
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate

class GeminiCouponExtractor(private val geminiApiKeyRepository: GeminiApiKeyRepository) {

    suspend fun extractCoupon(text: String): ParsedCoupon {
        val apiKey = geminiApiKeyRepository.getApiKey.first()
        if (apiKey.isEmpty()) {
            throw InvalidApiKeyException("API Key is empty")
        }

        val model = geminiApiKeyRepository.getModel.first()
        val temperature = geminiApiKeyRepository.getTemperature.first()

        val generativeModel = GenerativeModel(
            modelName = model.modelName,
            apiKey = apiKey,
            // Force the model to return strict JSON
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                this.temperature = temperature
            }
        )
        
        val today = LocalDate.now().toString()
        val prompt = """
        role: Expert Data Extractor.
        task: Extract coupon details from the text below.
        current_date: $today
        
        STRICT EXTRACTION RULES:
        1. Store Name: Identify the brand (e.g., "KSP", "Super-Pharm", "Nike").
        
        2. Code: Aggressively search for the redemption code.
           - PATTERN PRIORITY: Look specifically for multi-part numeric codes separated by a dash, such as "12345-67890" or "1111-2222-3333". 
           - Extract the ENTIRE string including the dashes. Do not return only the first part.
           - Also look for standard alphanumeric codes (e.g., "AB-1234", "GIFT100").
           - Look after labels: "Code", "Ref", "Barcode", "Number", "קוד", "אסמכתא", "מספר".
           
        3. Value: The monetary value (e.g. 100, 50.5).
        4. Date: Output format YYYY-MM-DD. Handle "Valid for X years" relative to $today.
        
        JSON OUTPUT STRUCTURE:
        {
          "storeName": "string",
          "redeemCode": "string (MUST include hyphens if present, e.g. '1234-5678')",
          "initialValue": double,
          "expirationDate": "YYYY-MM-DD",
          "description": "string"
        }

        INPUT TEXT:
        "$text"
    """.trimIndent()

        val response = generativeModel.generateContent(prompt)

        val json = response.text?.trim()?.replace("```json", "")?.replace("```", "")

        if (json.isNullOrBlank()) {
            throw IOException("Failed to extract coupon details. Empty or blank response from Gemini model.")
        }

        val jsonObject: JSONObject
        val sanitizedJson = json.trim()
        if (sanitizedJson.startsWith("[")) {
            val jsonArray = JSONArray(sanitizedJson)
            if (jsonArray.length() > 0) {
                jsonObject = jsonArray.getJSONObject(0)
            } else {
                throw IOException("Failed to extract coupon details. Empty JSON array response from Gemini model.")
            }
        } else {
            jsonObject = JSONObject(sanitizedJson)
        }

        val expirationDateString = jsonObject.optString("expirationDate")
        val expirationDate = if (expirationDateString.isNullOrBlank() || expirationDateString.equals("null", ignoreCase = true)) {
            LocalDate.now().plusYears(1).toString()
        } else {
            expirationDateString
        }

        val rawInitialValue = jsonObject.optDouble("initialValue")
        val initialValue = if(rawInitialValue.isNaN()) null else rawInitialValue

        return ParsedCoupon(
            storeName = jsonObject.optString("storeName"),
            redeemCode = jsonObject.optString("redeemCode"),
            initialValue = initialValue,
            expirationDate = expirationDate,
            description = jsonObject.optString("description")
        )
    }
}
