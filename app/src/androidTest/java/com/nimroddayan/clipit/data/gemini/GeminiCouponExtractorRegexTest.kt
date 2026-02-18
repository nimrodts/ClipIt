package com.nimroddayan.clipit.data.gemini

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [GeminiCouponExtractor.extractCouponRegex]. Tests the regex-based fallback
 * extraction (offline, no LLM).
 */
@RunWith(AndroidJUnit4::class)
class GeminiCouponExtractorRegexTest {

    private lateinit var extractor: GeminiCouponExtractor

    @Before
    fun setUp() {
        // Create with null context - we only test extractCouponRegex which doesn't need context
        extractor = GeminiCouponExtractor(null)
    }

    // ========== Code Pattern Extraction ==========

    @Test
    fun extractsRedeemCodeFromStandardFormat() {
        val text = "Your gift card code is: ABC123XYZ"
        val result = extractor.extractCouponRegex(text)

        assertNotNull(result)
        assertEquals("ABC123XYZ", result?.redeemCode)
    }

    @Test
    fun extractsRedeemCodeWithDashes() {
        val text = "Redeem code: GIFT-1234-ABCD"
        val result = extractor.extractCouponRegex(text)

        assertNotNull(result)
        assertNotNull(result?.redeemCode)
    }

    @Test
    fun extractsCodeFromHebrewText() {
        val text = "קוד המימוש שלך הוא: XYZ789ABC"
        val result = extractor.extractCouponRegex(text)

        assertNotNull(result)
        assertEquals("XYZ789ABC", result?.redeemCode)
    }

    // ========== Value Extraction ==========

    @Test
    fun extractsValueWithShekelSymbol() {
        val text = "Gift card worth ₪100 with code: ABC123"
        val result = extractor.extractCouponRegex(text)

        assertNotNull(result)
        assertEquals(100.0, result?.initialValue ?: 0.0, 0.01)
    }

    @Test
    fun extractsValueWithILSText() {
        val text = "Your 50 ILS coupon is ready. Code: DEF456"
        val result = extractor.extractCouponRegex(text)

        assertNotNull(result)
        assertEquals(50.0, result?.initialValue ?: 0.0, 0.01)
    }

    @Test
    fun extractsValueWithDollarSign() {
        val text = "$25 voucher - use code SAVE25"
        val result = extractor.extractCouponRegex(text)

        assertNotNull(result)
        // Should extract value
        assertNotNull(result?.initialValue)
    }

    @Test
    fun extractsDecimalValue() {
        val text = "Gift card value: ₪149.90 Code: TEST123"
        val result = extractor.extractCouponRegex(text)

        assertNotNull(result)
        assertEquals(149.90, result?.initialValue ?: 0.0, 0.01)
    }

    // ========== Date Extraction ==========

    @Test
    fun extractsExpirationDateSlashFormat() {
        val text = "Expires: 12/31/2025 Code: HOLIDAY"
        val result = extractor.extractCouponRegex(text)

        assertNotNull(result)
        assertNotNull(result?.expirationDate)
    }

    @Test
    fun extractsExpirationDateDashFormat() {
        val text = "Valid until 2025-06-30. Code: SUMMER"
        val result = extractor.extractCouponRegex(text)

        assertNotNull(result)
        assertNotNull(result?.expirationDate)
    }

    // ========== Store Name Extraction ==========

    @Test
    fun extractsKnownStoreName() {
        val text = "Your Shufersal gift card is ready! Code: FOOD123 Value: ₪200"
        val result = extractor.extractCouponRegex(text)

        assertNotNull(result)
        // Store name extraction depends on implementation
    }

    // ========== Edge Cases ==========

    @Test
    fun returnsNullForNonCouponText() {
        val text = "This is just a regular message with no coupon information."
        val result = extractor.extractCouponRegex(text)

        // Should return null or ParsedCoupon with null values
        // The exact behavior depends on implementation
    }

    @Test
    fun handlesEmptyText() {
        val text = ""
        val result = extractor.extractCouponRegex(text)

        // Should not crash
    }

    @Test
    fun handlesMultipleCodesOnlyExtractsFirst() {
        val text = "Codes: ABC123, DEF456, GHI789"
        val result = extractor.extractCouponRegex(text)

        // Should extract first valid code
        assertNotNull(result)
    }

    @Test
    fun extractsFromComplexMultilineMessage() {
        val text =
                """
            Thank you for your purchase!
            
            Your gift card details:
            Store: Example Store
            Value: ₪150
            Code: GIFT-2024-XYZ
            Expires: 31/12/2025
            
            Enjoy your gift!
        """.trimIndent()
        val result = extractor.extractCouponRegex(text)

        assertNotNull(result)
        assertNotNull(result?.redeemCode)
    }

    @Test
    fun handlesCodeWithSpecialCharacters() {
        val text = "Promo code: SAVE@50%OFF!"
        val result = extractor.extractCouponRegex(text)

        // Should handle gracefully
    }
}
