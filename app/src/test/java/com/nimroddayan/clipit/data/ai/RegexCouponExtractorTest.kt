package com.nimroddayan.clipit.data.ai

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegexCouponExtractorTest {

    private val extractor = RegexCouponExtractor()

    @Test
    fun `extracts plain alphanumeric code`() = runTest {
        assertEquals("ABC123XYZ", extractor.extract("Your gift card code is: ABC123XYZ").redeemCode)
    }

    @Test
    fun `extracts hyphenated code in full`() = runTest {
        assertEquals("GIFT-1234-ABCD", extractor.extract("Redeem code: GIFT-1234-ABCD").redeemCode)
    }

    @Test
    fun `extracts code from hebrew text`() = runTest {
        assertEquals("XYZ789ABC", extractor.extract("קוד המימוש שלך הוא: XYZ789ABC").redeemCode)
    }

    @Test
    fun `extracts first code when several are present`() = runTest {
        assertEquals("ABC123", extractor.extract("Codes: ABC123, DEF456").redeemCode)
    }

    @Test
    fun `extracts values in shekel, ILS, dollar and decimal formats`() = runTest {
        assertEquals(100.0, extractor.extract("Gift card worth ₪100 with code: ABC123").initialValue!!, 0.01)
        assertEquals(50.0, extractor.extract("Your 50 ILS coupon is ready. Code: DEF456").initialValue!!, 0.01)
        assertEquals(25.0, extractor.extract("\$25 voucher - use code SAVE25").initialValue!!, 0.01)
        assertEquals(149.90, extractor.extract("Gift card value: ₪149.90 Code: TEST123").initialValue!!, 0.01)
    }

    @Test
    fun `finds known store name`() = runTest {
        val result = extractor.extract("Your Nike gift card is ready! Code: FOOD123 Value: ₪200")
        assertEquals("Nike", result.storeName)
        assertEquals("FOOD123", result.redeemCode)
        assertEquals(200.0, result.initialValue!!, 0.01)
    }

    @Test
    fun `non coupon text yields defaults and keeps original text`() = runTest {
        val text = "This is just a regular message with no coupon information."
        val result = extractor.extract(text)
        assertEquals("New Coupon", result.storeName)
        assertNull(result.redeemCode)
        assertNull(result.initialValue)
        assertNull(result.expirationDate)
        assertEquals(text, result.description)
    }

    @Test
    fun `empty text does not crash`() = runTest {
        assertNull(extractor.extract("").redeemCode)
    }
}
