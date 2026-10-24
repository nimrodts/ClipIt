package com.nimroddayan.clipit.data.ai

import com.nimroddayan.clipit.data.gemini.DetectionMode
import com.nimroddayan.clipit.data.gemini.GeminiApiException
import com.nimroddayan.clipit.data.gemini.GeminiQuotaException
import com.nimroddayan.clipit.data.gemini.InvalidApiKeyException
import com.nimroddayan.clipit.data.gemini.ParsedCoupon
import com.nimroddayan.clipit.data.gemini.QuotaLimit
import com.nimroddayan.clipit.data.model.Category
import java.io.IOException
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CouponParserTest {

    private val today = LocalDate.of(2026, 10, 2)
    private val cloudResult = ParsedCoupon("KSP", 100.0, "2027-01-31", "d", "https://bit.ly/x", null)
    private val regexResult = ParsedCoupon("New Coupon", null, null, "text", "ABC123", null)

    private class FakeExtractor(private val behavior: () -> ParsedCoupon) : CouponExtractor {
        var calls = 0
        override suspend fun extract(text: String): ParsedCoupon {
            calls++
            return behavior()
        }
    }

    private val regex = FakeExtractor { regexResult }

    private fun parser(
        apiKey: String = "key",
        mode: DetectionMode = DetectionMode.LOCAL_HEURISTIC,
        cloud: FakeExtractor = FakeExtractor { cloudResult },
        categories: List<Category> = emptyList(),
        onFactory: (AiSettings, LocalDate, List<Category>) -> Unit = { _, _, _ -> },
    ) = CouponParser(
        settingsProvider = { AiSettings(apiKey, "gemini-2.5-flash", 0.15f, mode) },
        cloudExtractorFactory = { settings, date, cats -> onFactory(settings, date, cats); cloud },
        categoriesProvider = { categories },
        regexExtractor = regex,
        today = { today },
    )

    @Test
    fun `no api key uses regex and never calls cloud`() = runTest {
        val cloud = FakeExtractor { cloudResult }
        val result = parser(apiKey = " ", cloud = cloud).parse("text")
        assertEquals(0, cloud.calls)
        assertEquals("ABC123", result.redeemCode)
        assertNull(result.error)
    }

    @Test
    fun `regex only mode never calls cloud even with a key`() = runTest {
        val cloud = FakeExtractor { cloudResult }
        parser(mode = DetectionMode.REGEX_ONLY, cloud = cloud).parse("text")
        assertEquals(0, cloud.calls)
        assertEquals(1, regex.calls)
    }

    @Test
    fun `cloud result is normalized`() = runTest {
        val result = parser().parse("text")
        assertNull(result.redeemCode)
        assertEquals("https://bit.ly/x", result.redemptionUrl)
        assertEquals(0, regex.calls)
    }

    @Test
    fun `cloud factory gets settings and today`() = runTest {
        var seen: Pair<AiSettings, LocalDate>? = null
        parser(onFactory = { s, d, _ -> seen = s to d }).parse("text")
        assertEquals("gemini-2.5-flash", seen!!.first.modelId)
        assertEquals(today, seen!!.second)
    }


    @Test
    fun `cloud factory gets the categories and the category survives normalizing`() = runTest {
        val food = Category(id = 7, name = "Food", colorHex = "#FF0000", iconName = "food")
        var seen: List<Category>? = null
        val cloud = FakeExtractor { cloudResult.copy(categoryId = 7) }

        val result = parser(cloud = cloud, categories = listOf(food), onFactory = { _, _, c -> seen = c }).parse("text")

        assertEquals(listOf(food), seen)
        assertEquals(7L, result.categoryId)
    }

    @Test
    fun `quota error falls back to regex with quota marker`() = runTest {
        val result = parser(cloud = FakeExtractor { throw GeminiQuotaException("quota") }).parse("text")
        assertEquals(ParsedCoupon.ERROR_QUOTA, result.error)
    }

    @Test
    fun `model without free quota is reported as a non retryable error`() = runTest {
        val error = GeminiQuotaException("limit: 0", QuotaLimit.NO_FREE_QUOTA)
        val result = parser(cloud = FakeExtractor { throw error }).parse("text")
        assertEquals(ParsedCoupon.ERROR_NO_FREE_QUOTA, result.error)
        assertEquals(AiStatus.FAILED, PendingCouponBuilder.statusFor(result.error))
        assertEquals("ABC123", result.redeemCode)
        assertEquals("2027-10-02", result.expirationDate)
    }

    @Test
    fun `invalid key falls back to regex with key marker`() = runTest {
        val result = parser(cloud = FakeExtractor { throw InvalidApiKeyException("bad") }).parse("text")
        assertEquals(ParsedCoupon.ERROR_INVALID_KEY, result.error)
    }

    @Test
    fun `network error falls back to regex with transient marker`() = runTest {
        val result = parser(cloud = FakeExtractor { throw IOException("offline") }).parse("text")
        assertEquals(ParsedCoupon.ERROR_TRANSIENT, result.error)
        assertEquals("ABC123", result.redeemCode)
    }

    @Test
    fun `server overload is transient`() = runTest {
        val result = parser(cloud = FakeExtractor { throw GeminiApiException(503, "high demand") }).parse("text")
        assertEquals(ParsedCoupon.ERROR_TRANSIENT, result.error)
    }

    @Test
    fun `client errors are not transient and keep the message`() = runTest {
        val result = parser(cloud = FakeExtractor { throw GeminiApiException(400, "bad model") }).parse("text")
        assertEquals("AI Extraction Failed: bad model", result.error)
    }

    @Test
    fun `always extract mode still keeps OTP and bank SMS on the phone`() = runTest {
        val always = parser(mode = DetectionMode.ALWAYS_EXTRACT)
        assertFalse(always.shouldExtract("Your verification code is 482913"))
        assertFalse(always.shouldExtract("בוצעה עסקה בכרטיס 1234 בסך 250 ש\"ח"))
        assertTrue(always.shouldExtract("hello there"))
    }

    @Test
    fun `cancellation is not swallowed`() = runTest {
        val error = runCatching {
            parser(cloud = FakeExtractor { throw CancellationException("left screen") }).parse("text")
        }.exceptionOrNull()
        assertTrue("got $error", error is CancellationException)
    }

    @Test
    fun `shouldExtract respects detection mode`() = runTest {
        assertTrue(parser(mode = DetectionMode.ALWAYS_EXTRACT).shouldExtract("hello there"))
        assertFalse(parser().shouldExtract("Your verification code is 482913"))
        assertTrue(parser().shouldExtract("Your coupon code SAVE20"))
        assertTrue(parser(mode = DetectionMode.REGEX_ONLY).shouldExtract("Your coupon code SAVE20"))
    }
}
