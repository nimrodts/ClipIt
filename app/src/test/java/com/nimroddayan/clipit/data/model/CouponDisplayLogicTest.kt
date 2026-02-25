package com.nimroddayan.clipit.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class CouponDisplayLogicTest {

    @Test
    fun `display code when redeemCode is available`() {
        val coupon = Coupon(
            name = "Test",
            currentValue = 100.0,
            initialValue = 100.0,
            expirationDate = 1700000000000L,
            categoryId = null,
            redeemCode = "ABC-1234",
            redemptionUrl = null
        )

        val displayCode = getDisplayCode(coupon.redeemCode, coupon.redemptionUrl)
        assertEquals("ABC-1234", displayCode)
    }

    @Test
    fun `display URL when redeemCode is null but URL is available`() {
        val coupon = Coupon(
            name = "Test",
            currentValue = 100.0,
            initialValue = 100.0,
            expirationDate = 1700000000000L,
            categoryId = null,
            redeemCode = null,
            redemptionUrl = "https://example.com/redeem/abc123"
        )

        val displayCode = getDisplayCode(coupon.redeemCode, coupon.redemptionUrl)
        assertEquals("https://example.com/redeem/abc123", displayCode)
    }

    @Test
    fun `prioritize redeemCode over URL when both are available`() {
        val coupon = Coupon(
            name = "Test",
            currentValue = 100.0,
            initialValue = 100.0,
            expirationDate = 1700000000000L,
            categoryId = null,
            redeemCode = "CODE-999",
            redemptionUrl = "https://example.com/redeem"
        )

        val displayCode = getDisplayCode(coupon.redeemCode, coupon.redemptionUrl)
        assertEquals("CODE-999", displayCode)
    }

    @Test
    fun `return empty string when both redeemCode and URL are null`() {
        val coupon = Coupon(
            name = "Test",
            currentValue = 100.0,
            initialValue = 100.0,
            expirationDate = 1700000000000L,
            categoryId = null,
            redeemCode = null,
            redemptionUrl = null
        )

        val displayCode = getDisplayCode(coupon.redeemCode, coupon.redemptionUrl)
        assertEquals("", displayCode)
    }

    @Test
    fun `return empty string when redeemCode is blank`() {
        val coupon = Coupon(
            name = "Test",
            currentValue = 100.0,
            initialValue = 100.0,
            expirationDate = 1700000000000L,
            categoryId = null,
            redeemCode = "   ",
            redemptionUrl = "https://example.com/redeem"
        )

        val displayCode = getDisplayCode(coupon.redeemCode, coupon.redemptionUrl)
        assertEquals("https://example.com/redeem", displayCode)
    }

    @Test
    fun `isUrl returns true for URL`() {
        assertTrue(isUrl("https://example.com"))
        assertTrue(isUrl("http://example.com"))
        assertTrue(isUrl("https://example.com/redeem?id=123"))
    }

    @Test
    fun `isUrl returns false for code`() {
        assertFalse(isUrl("ABC-1234"))
        assertFalse(isUrl("CODE123"))
        assertFalse(isUrl(""))
        assertFalse(isUrl("   "))
    }

    private fun getDisplayCode(redeemCode: String?, redemptionUrl: String?): String {
        return if (!redeemCode.isNullOrBlank()) redeemCode
        else if (!redemptionUrl.isNullOrBlank()) redemptionUrl else ""
    }

    private fun isUrl(value: String): Boolean {
        return value.startsWith("http://") || value.startsWith("https://")
    }
}
