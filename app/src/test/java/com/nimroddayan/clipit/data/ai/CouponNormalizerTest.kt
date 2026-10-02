package com.nimroddayan.clipit.data.ai

import com.nimroddayan.clipit.data.gemini.ParsedCoupon
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CouponNormalizerTest {

    private val today = LocalDate.of(2026, 10, 2)

    private fun coupon(
        storeName: String? = "KSP",
        initialValue: Double? = 100.0,
        expirationDate: String? = "2027-01-31",
        description: String? = "desc",
        redeemCode: String? = "ABC-123",
        redemptionUrl: String? = null,
    ) = ParsedCoupon(storeName, initialValue, expirationDate, description, redeemCode, redemptionUrl)

    @Test
    fun `valid coupon is unchanged`() {
        assertEquals(coupon(), CouponNormalizer.normalize(coupon(), today))
    }

    @Test
    fun `blank and literal null strings become null and others are trimmed`() {
        val result = CouponNormalizer.normalize(
            coupon(storeName = "  KSP  ", description = "null", redeemCode = "   ", redemptionUrl = "NULL"),
            today
        )
        assertEquals("KSP", result.storeName)
        assertNull(result.description)
        assertNull(result.redeemCode)
        assertNull(result.redemptionUrl)
    }

    @Test
    fun `url in redeem code moves to redemption url`() {
        val result = CouponNormalizer.normalize(coupon(redeemCode = "https://bit.ly/abc"), today)
        assertNull(result.redeemCode)
        assertEquals("https://bit.ly/abc", result.redemptionUrl)
    }

    @Test
    fun `url in redeem code is dropped when a redemption url already exists`() {
        val result = CouponNormalizer.normalize(
            coupon(redeemCode = "http://a.com/x", redemptionUrl = "https://b.com/y"),
            today
        )
        assertNull(result.redeemCode)
        assertEquals("https://b.com/y", result.redemptionUrl)
    }

    @Test
    fun `non positive and NaN values become null`() {
        assertNull(CouponNormalizer.normalize(coupon(initialValue = 0.0), today).initialValue)
        assertNull(CouponNormalizer.normalize(coupon(initialValue = -5.0), today).initialValue)
        assertNull(CouponNormalizer.normalize(coupon(initialValue = Double.NaN), today).initialValue)
    }

    @Test
    fun `missing or invalid date defaults to one year from today`() {
        assertEquals("2027-10-02", CouponNormalizer.normalize(coupon(expirationDate = null), today).expirationDate)
        assertEquals("2027-10-02", CouponNormalizer.normalize(coupon(expirationDate = "31/12/2026"), today).expirationDate)
    }
}
