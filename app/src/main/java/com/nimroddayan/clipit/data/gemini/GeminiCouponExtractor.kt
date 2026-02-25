package com.nimroddayan.clipit.data.gemini

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

class GeminiCouponExtractor(
        private val context: Context,
        private val geminiApiKeyRepository: GeminiApiKeyRepository
) {

    suspend fun extractCoupon(text: String): ParsedCoupon {
        val apiKey = geminiApiKeyRepository.getApiKey.first()
        if (apiKey.isEmpty()) {
            android.util.Log.w("GeminiExtractor", "API Key is empty, using Regex fallback")
            return extractCouponRegex(text)
        }

        val model = geminiApiKeyRepository.getModel.first()
        val temperature = geminiApiKeyRepository.getTemperature.first()

        val generativeModel =
                GenerativeModel(
                        modelName = model.modelName,
                        apiKey = apiKey,
                        generationConfig =
                                generationConfig {
                                    responseMimeType = "application/json"
                                    this.temperature = temperature
                                }
                )

        val today = LocalDate.now().toString()
        val prompt =
                """
        role: Expert Data Extractor.
        task: Extract coupon details from the text below.
        current_date: $today
        
        STRICT EXTRACTION RULES:
        1. Store Name: Identify the brand (e.g., "KSP", "Super-Pharm", "Nike").
        
        2. Redemption Data (The "Key" to value):
            - OBJECTIVE: Find the string OR link needed to redeem.
            
            - CRITICAL URL RULE (HIGHEST PRIORITY): 
                - ANY value that starts with "http://" or "https://" MUST go to 'redemptionUrl', NEVER to 'redeemCode'.
                - This applies EVEN IF the URL follows a label like "קוד השובר שלך:" or "Your code:".
                - Examples of URLs (go to redemptionUrl): "https://bit.ly/abc", "http://voucher.com/123", "https://redeem.store.com?code=XYZ"
                - Examples of codes (go to redeemCode): "12345", "ABC-DEF-123", "GIFT2024"

            - PRIORITY 1: Explicitly Labeled Data.
                - Look for labels: "Code", "Ref", "Barcode", "Coupon", "קוד", "אסמכתא", "מספר שובר", "קוד השובר שלך".
                - Extract exactly what follows the label.
                - IF the value is a URL -> put in 'redemptionUrl' (never in redeemCode).
                - IF the value is alphanumeric text (NOT a URL) -> put in 'redeemCode'.

            - PRIORITY 2: Dynamic Links (No Label).
                - Look for URLs in context of redemption actions ("Click to redeem", "לחץ למימוש").
                - Map to 'redemptionUrl'.

            - PRIORITY 3: Pattern Matching (Fallback).
                - Look for standalone alphanumeric patterns (e.g., "AA-BB-CC", "SALE20").
                - Map to 'redeemCode' ONLY if they don't start with http/https.

            - SAFETY FILTERS (What to IGNORE):
                - DO NOT extract "Order Numbers" (מספר הזמנה) even if they look like codes.
                - DO NOT extract generic links (e.g., "Terms of Service", "Unsubscribe").
           
        3. Value: Calculate the TOTAL effective balance of the code.
            - Step A: Identify the unit value (e.g., "100 ₪", "$50").
            - Step B: Scan immediately nearby for a quantity multiplier (keywords: "Quantity", "Qty", "כמות", "units", "x3").
            - Step C: CALCULATION REQUIRED. 
                - If a quantity is found (e.g., "Quantity: 3"), multiply Unit Value * Quantity (e.g., 100 * 3 = 300).
                - If text says "Code for total amount" (קוד אחד... בסכום שהוזמן), ensure you output the aggregate sum.
         
        4. Date: Output format YYYY-MM-DD. Handle "Valid for X years" relative to $today.
        
        JSON OUTPUT STRUCTURE:
        {
          "storeName": "string",
          "redeemCode": "string (MUST include hyphens if present, e.g. '1234-5678')",
          "initialValue": double,
          "expirationDate": "YYYY-MM-DD",
          "description": "string",
          "redemptionUrl": "string (Optional: URL for redemption)"
        }

        INPUT TEXT:
        "$text"
    """.trimIndent()

        try {
            val response = generativeModel.generateContent(prompt)
            val json = response.text?.trim()?.replace("```json", "")?.replace("```", "")

            if (json.isNullOrBlank()) {
                throw IOException("Empty response from AI")
            }

            val jsonObject: JSONObject
            val sanitizedJson = json.trim()
            if (sanitizedJson.startsWith("[")) {
                val jsonArray = JSONArray(sanitizedJson)
                if (jsonArray.length() > 0) {
                    jsonObject = jsonArray.getJSONObject(0)
                } else {
                    throw IOException("Empty JSON array")
                }
            } else {
                jsonObject = JSONObject(sanitizedJson)
            }

            val expirationDateString = jsonObject.optString("expirationDate")
            val expirationDate =
                    if (expirationDateString.isNullOrBlank() ||
                                    expirationDateString.equals("null", ignoreCase = true)
                    ) {
                        LocalDate.now().plusYears(1).toString()
                    } else {
                        expirationDateString
                    }

            val rawInitialValue = jsonObject.optDouble("initialValue")
            // Check if NaN
            val initialValue = if (rawInitialValue.isNaN()) null else rawInitialValue

            return ParsedCoupon(
                    storeName =
                            jsonObject.optString("storeName").takeIf {
                                it.isNotBlank() && it != "null"
                            },
                    redeemCode =
                            jsonObject.optString("redeemCode").takeIf {
                                it.isNotBlank() && it != "null"
                            },
                    initialValue = initialValue,
                    expirationDate = expirationDate,
                    description =
                            jsonObject.optString("description").takeIf {
                                it.isNotBlank() && it != "null"
                            },
                    redemptionUrl =
                            jsonObject.optString("redemptionUrl").takeIf {
                                it.isNotBlank() && it != "null"
                            }
            )
        } catch (e: com.google.ai.client.generativeai.type.QuotaExceededException) {
            android.util.Log.w("GeminiExtractor", "Quota exceeded. Falling back to regex.")
            val fallback = extractCouponRegex(text)
            return fallback.copy(error = "Quota Limit Reached")
        } catch (e: IOException) {
            android.util.Log.e("GeminiExtractor", "IO/Parsing error. Falling back to regex.", e)
            val fallback = extractCouponRegex(text)
            return fallback.copy(error = "AI Parsing Failed")
        } catch (e: Exception) {
            android.util.Log.e(
                    "GeminiExtractor",
                    "AI Extraction failed (Unknown). Falling back to regex.",
                    e
            )
            val fallback = extractCouponRegex(text)
            return fallback.copy(error = "AI Extraction Failed: ${e.message}")
        }
    }

    private fun extractCouponRegex(text: String): ParsedCoupon {
        // 1. Value Extraction (Enhanced)
        // Matches: ₪100, $50, 100₪, 100 NIS, 100 ש"ח, 100.50
        val valueRegex =
                Regex(
                        """(₪|\$|€)\s*(\d+(\.\d{1,2})?)|(\d+(\.\d{1,2})?)\s*(₪|\$|€|NIS|nis|ש"ח|שקלים)"""
                )
        val valueMatch = valueRegex.find(text)

        var initialValue: Double? = null
        if (valueMatch != null) {
            // Group 2 is value if symbol first (₪100)
            // Group 4 is value if symbol last (100₪)
            val valGroup1 = valueMatch.groups[2]?.value
            val valGroup2 = valueMatch.groups[4]?.value
            initialValue = (valGroup1 ?: valGroup2)?.toDoubleOrNull()
        }

        // 2. Code Extraction (Alphanumeric with dashes, length > 4)
        // Matches: 1234-5678, ABCD-1234, 12345678, GIFT100
        val codeRegex =
                Regex(
                        """(?<!\d)\d{4,}-\d{4,}(?!\d)|[A-Z0-9]{4,}-[A-Z0-9]{4,}(-[A-Z0-9]{4,})?|\b[A-Z0-9]{5,}\b"""
                )
        val codeMatch = codeRegex.find(text)
        val redeemCode = codeMatch?.value ?: ""

        // 3. Store Name - Naive guess: First word if it looks like a name, or specific keywords
        var storeName = "Unknown Store"
        val knownStores =
                listOf(
                        "KSP",
                        "Super-Pharm",
                        "Wolt",
                        "Dominos",
                        "Mcdonalds",
                        "Nike",
                        "Adidas",
                        "Fox",
                        "Castro",
                        "Renuar",
                        "Terminal X",
                        "AliExpress",
                        "Amazon",
                        "Next",
                        "Shein",
                        "Zara",
                        "H&M"
                )
        val foundStore = knownStores.find { text.contains(it, ignoreCase = true) }
        if (foundStore != null) {
            storeName = foundStore
        } else {
            // Fallback: take first non-common word or just "New Coupon"
            storeName = "New Coupon"
        }

        return ParsedCoupon(
                storeName = storeName,
                redeemCode = redeemCode,
                initialValue = initialValue ?: 0.0,
                expirationDate = LocalDate.now().plusYears(1).toString(),
                description = text,
                redemptionUrl = null
        )
    }
    suspend fun hasCoupon(text: String): Boolean {
        // 1. Quick local check (Optimization & Fallback)
        val keywords =
                listOf(
                        "coupon",
                        "code",
                        "voucher",
                        "gift card",
                        "promo",
                        "קופון",
                        "קוד",
                        "שובר",
                        "מתנה",
                        "הטבה",
                        "מימוש",
                        "אסמכתא",
                        "כרטיס",
                        "Gift"
                )
        if (keywords.any { text.contains(it, ignoreCase = true) }) {
            android.util.Log.d("GeminiExtractor", "Local keyword match found. Skipping AI check.")
            return true
        }

        // Use Local LLM (AICore/Gemini Nano)
        val localClassifier = LocalCouponClassifier(context)
        return localClassifier.isCoupon(text)
    }
}
