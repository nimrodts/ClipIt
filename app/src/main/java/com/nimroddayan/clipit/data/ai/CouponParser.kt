package com.nimroddayan.clipit.data.ai

import android.content.Context
import android.util.Log
import com.nimroddayan.clipit.CouponApplication
import com.nimroddayan.clipit.data.gemini.DetectionMode
import com.nimroddayan.clipit.data.gemini.GeminiApiException
import com.nimroddayan.clipit.data.gemini.GeminiApiKeyRepository
import com.nimroddayan.clipit.data.gemini.GeminiQuotaException
import com.nimroddayan.clipit.data.gemini.InvalidApiKeyException
import com.nimroddayan.clipit.data.gemini.ParsedCoupon
import com.nimroddayan.clipit.data.gemini.QuotaLimit
import com.nimroddayan.clipit.data.model.Category
import java.io.IOException
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.first

/**
 * Entry point for coupon AI: decides whether a message should be extracted, then extracts it with
 * Gemini (falling back to regex) and normalizes the result. Never throws except on cancellation.
 */
class CouponParser(
    private val settingsProvider: suspend () -> AiSettings,
    private val cloudExtractorFactory: (AiSettings, LocalDate, List<Category>) -> CouponExtractor,
    private val categoriesProvider: suspend () -> List<Category> = { emptyList() },
    private val regexExtractor: CouponExtractor = RegexCouponExtractor(),
    private val detector: CouponDetector = CouponDetector(),
    private val today: () -> LocalDate = LocalDate::now,
) {

    suspend fun shouldExtract(text: String): Boolean {
        if (detector.isSensitive(text)) return false
        return settingsProvider().detectionMode == DetectionMode.ALWAYS_EXTRACT || detector.isLikelyCoupon(text)
    }


    suspend fun parse(text: String): ParsedCoupon {
        val settings = settingsProvider()
        val date = today()
        val useCloud = settings.apiKey.isNotBlank() && settings.detectionMode != DetectionMode.REGEX_ONLY

        val raw = if (!useCloud) {
            regexExtractor.extract(text)
        } else {
            try {
                cloudExtractorFactory(settings, date, categoriesProvider()).extract(text)
            } catch (e: CancellationException) {
                throw e
            } catch (e: GeminiQuotaException) {
                Log.w(TAG, "Quota exceeded (${e.limit}), falling back to regex")
                // A quota of 0 never recovers by waiting, so it must not be retried like a used-up one.
                val error = if (e.limit == QuotaLimit.NO_FREE_QUOTA) {
                    ParsedCoupon.ERROR_NO_FREE_QUOTA
                } else {
                    ParsedCoupon.ERROR_QUOTA
                }
                regexExtractor.extract(text).copy(error = error)
            } catch (e: InvalidApiKeyException) {
                Log.w(TAG, "Gemini rejected the API key, falling back to regex")
                regexExtractor.extract(text).copy(error = ParsedCoupon.ERROR_INVALID_KEY)
            } catch (e: IOException) {
                Log.w(TAG, "AI unreachable, falling back to regex", e)
                regexExtractor.extract(text).copy(error = ParsedCoupon.ERROR_TRANSIENT)
            } catch (e: GeminiApiException) {
                Log.e(TAG, "Gemini returned HTTP ${e.httpCode}, falling back to regex", e)
                val error = if (e.httpCode >= 500 || e.httpCode == 408) {
                    ParsedCoupon.ERROR_TRANSIENT
                } else {
                    "AI Extraction Failed: ${e.message}"
                }
                regexExtractor.extract(text).copy(error = error)
            } catch (e: Exception) {
                Log.e(TAG, "AI extraction failed, falling back to regex", e)
                regexExtractor.extract(text).copy(error = "AI Extraction Failed: ${e.message}")
            }
        }
        return CouponNormalizer.normalize(raw, date)
    }

    companion object {
        private const val TAG = "CouponParser"

        fun create(context: Context): CouponParser {
            val app = context.applicationContext as CouponApplication
            val repository = GeminiApiKeyRepository(app)
            return CouponParser(
                settingsProvider = repository::currentSettings,
                cloudExtractorFactory = { settings, date, categories ->
                    GeminiCloudExtractor(app.geminiRestClient, settings, date, categories)
                },
                categoriesProvider = { app.database.categoryDao().getAll().first() },
            )
        }
    }
}
