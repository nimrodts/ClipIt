package com.nimroddayan.clipit.data.ai

import android.content.Context
import android.util.Log
import com.nimroddayan.clipit.CouponApplication
import com.nimroddayan.clipit.data.gemini.DetectionMode
import com.nimroddayan.clipit.data.gemini.GeminiApiKeyRepository
import com.nimroddayan.clipit.data.gemini.GeminiQuotaException
import com.nimroddayan.clipit.data.gemini.InvalidApiKeyException
import com.nimroddayan.clipit.data.gemini.ParsedCoupon
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

/**
 * Entry point for coupon AI: decides whether a message should be extracted, then extracts it with
 * Gemini (falling back to regex) and normalizes the result. Never throws except on cancellation.
 */
class CouponParser(
    private val settingsProvider: suspend () -> AiSettings,
    private val cloudExtractorFactory: (AiSettings, LocalDate) -> CouponExtractor,
    private val regexExtractor: CouponExtractor = RegexCouponExtractor(),
    private val detector: CouponDetector = CouponDetector(),
    private val today: () -> LocalDate = LocalDate::now,
) {

    suspend fun shouldExtract(text: String): Boolean =
        settingsProvider().detectionMode == DetectionMode.ALWAYS_EXTRACT || detector.isLikelyCoupon(text)


    suspend fun parse(text: String): ParsedCoupon {
        val settings = settingsProvider()
        val date = today()
        val useCloud = settings.apiKey.isNotBlank() && settings.detectionMode != DetectionMode.REGEX_ONLY

        val raw = if (!useCloud) {
            regexExtractor.extract(text)
        } else {
            try {
                cloudExtractorFactory(settings, date).extract(text)
            } catch (e: CancellationException) {
                throw e
            } catch (e: GeminiQuotaException) {
                Log.w(TAG, "Quota exceeded, falling back to regex")
                regexExtractor.extract(text).copy(error = ParsedCoupon.ERROR_QUOTA)
            } catch (e: InvalidApiKeyException) {
                Log.w(TAG, "Gemini rejected the API key, falling back to regex")
                regexExtractor.extract(text).copy(error = ParsedCoupon.ERROR_INVALID_KEY)
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
                cloudExtractorFactory = { settings, date ->
                    GeminiCloudExtractor(app.geminiRestClient, settings, date)
                },
            )
        }
    }
}
