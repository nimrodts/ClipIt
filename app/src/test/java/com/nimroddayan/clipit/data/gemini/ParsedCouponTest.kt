package com.nimroddayan.clipit.data.gemini

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for [ParsedCoupon] data class. Tests edge cases and error handling scenarios. */
class ParsedCouponTest {

    @Test
    fun `default error is null`() {
        val coupon =
                ParsedCoupon(
                        storeName = "Store",
                        initialValue = 100.0,
                        expirationDate = "2025-01-01",
                        description = "Test",
                        redeemCode = "CODE",
                        redemptionUrl = null
                )

        assertNull(coupon.error)
    }

    @Test
    fun `all nullable fields can be null`() {
        val coupon =
                ParsedCoupon(
                        storeName = null,
                        initialValue = null,
                        expirationDate = null,
                        description = null,
                        redeemCode = null,
                        redemptionUrl = null,
                        error = null
                )

        assertNull(coupon.storeName)
        assertNull(coupon.initialValue)
        assertNull(coupon.expirationDate)
        assertNull(coupon.description)
        assertNull(coupon.redeemCode)
        assertNull(coupon.redemptionUrl)
        assertNull(coupon.error)
    }

    @Test
    fun `error field can be set`() {
        val coupon =
                ParsedCoupon(
                        storeName = "Store",
                        initialValue = 0.0,
                        expirationDate = null,
                        description = null,
                        redeemCode = null,
                        redemptionUrl = null,
                        error = "Quota Limit Reached"
                )

        assertEquals("Quota Limit Reached", coupon.error)
    }

    @Test
    fun `copy preserves all fields`() {
        val original =
                ParsedCoupon(
                        storeName = "Original Store",
                        initialValue = 50.0,
                        expirationDate = "2025-06-01",
                        description = "Description",
                        redeemCode = "CODE123",
                        redemptionUrl = "https://example.com",
                        error = null
                )

        val modified = original.copy(error = "New Error")

        assertEquals("Original Store", modified.storeName)
        assertEquals(50.0, modified.initialValue!!, 0.01)
        assertEquals("CODE123", modified.redeemCode)
        assertEquals("https://example.com", modified.redemptionUrl)
        assertEquals("New Error", modified.error)
    }

    @Test
    fun `zero value is valid`() {
        val coupon =
                ParsedCoupon(
                        storeName = "Store",
                        initialValue = 0.0,
                        expirationDate = null,
                        description = null,
                        redeemCode = null,
                        redemptionUrl = null
                )

        assertEquals(0.0, coupon.initialValue!!, 0.01)
    }

    @Test
    fun `negative value is technically allowed`() {
        // While not logically valid, the data class doesn't enforce this
        val coupon =
                ParsedCoupon(
                        storeName = "Store",
                        initialValue = -50.0,
                        expirationDate = null,
                        description = null,
                        redeemCode = null,
                        redemptionUrl = null
                )

        assertEquals(-50.0, coupon.initialValue!!, 0.01)
    }
}
