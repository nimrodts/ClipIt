package com.nimroddayan.clipit.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the [Coupon] data class. */
class CouponTest {

    @Test
    fun `default values are set correctly`() {
        val coupon =
                Coupon(
                        name = "Test Coupon",
                        currentValue = 100.0,
                        initialValue = 100.0,
                        expirationDate = 1700000000000L,
                        categoryId = null
                )

        assertFalse(coupon.isArchived)
        assertFalse(coupon.isPending)
        assertFalse(coupon.isOneTime)
        assertNull(coupon.redeemCode)
        assertNull(coupon.creationMessage)
        assertNull(coupon.redemptionUrl)
    }

    @Test
    fun `id is 0 by default`() {
        val coupon =
                Coupon(
                        name = "Test",
                        currentValue = 50.0,
                        initialValue = 50.0,
                        expirationDate = 1700000000000L,
                        categoryId = null
                )

        assertEquals(0L, coupon.id)
    }

    @Test
    fun `copy modifies specified fields only`() {
        val original =
                Coupon(
                        id = 1L,
                        name = "Original",
                        currentValue = 100.0,
                        initialValue = 100.0,
                        expirationDate = 1700000000000L,
                        categoryId = 1L
                )

        val modified = original.copy(currentValue = 50.0)

        assertEquals(1L, modified.id)
        assertEquals("Original", modified.name)
        assertEquals(50.0, modified.currentValue, 0.01)
        assertEquals(100.0, modified.initialValue, 0.01)
        assertEquals(1L, modified.categoryId)
    }

    @Test
    fun `copy can archive coupon`() {
        val coupon =
                Coupon(
                        name = "Test",
                        currentValue = 100.0,
                        initialValue = 100.0,
                        expirationDate = 1700000000000L,
                        categoryId = null
                )

        val archived = coupon.copy(isArchived = true)

        assertTrue(archived.isArchived)
    }

    @Test
    fun `coupons with same data are equal`() {
        val coupon1 =
                Coupon(
                        id = 1L,
                        name = "Test",
                        currentValue = 100.0,
                        initialValue = 100.0,
                        expirationDate = 1700000000000L,
                        categoryId = null
                )

        val coupon2 =
                Coupon(
                        id = 1L,
                        name = "Test",
                        currentValue = 100.0,
                        initialValue = 100.0,
                        expirationDate = 1700000000000L,
                        categoryId = null
                )

        assertEquals(coupon1, coupon2)
    }

    @Test
    fun `coupon with redeemCode is set correctly`() {
        val coupon =
                Coupon(
                        name = "Gift Card",
                        currentValue = 50.0,
                        initialValue = 50.0,
                        expirationDate = 1700000000000L,
                        categoryId = null,
                        redeemCode = "ABCD-1234"
                )

        assertEquals("ABCD-1234", coupon.redeemCode)
    }

    @Test
    fun `one-time coupon flag is set correctly`() {
        val coupon =
                Coupon(
                        name = "One Time Use",
                        currentValue = 20.0,
                        initialValue = 20.0,
                        expirationDate = 1700000000000L,
                        categoryId = null,
                        isOneTime = true
                )

        assertTrue(coupon.isOneTime)
    }

    @Test
    fun `pending coupon flag is set correctly`() {
        val coupon =
                Coupon(
                        name = "Pending Approval",
                        currentValue = 30.0,
                        initialValue = 30.0,
                        expirationDate = 1700000000000L,
                        categoryId = null,
                        isPending = true
                )

        assertTrue(coupon.isPending)
    }

    @Test
    fun `coupon with only redemptionUrl is set correctly`() {
        val coupon =
                Coupon(
                        name = "Online Coupon",
                        currentValue = 25.0,
                        initialValue = 25.0,
                        expirationDate = 1700000000000L,
                        categoryId = null,
                        redeemCode = null,
                        redemptionUrl = "https://example.com/redeem/abc123"
                )

        assertNull(coupon.redeemCode)
        assertEquals("https://example.com/redeem/abc123", coupon.redemptionUrl)
    }

    @Test
    fun `coupon with both redeemCode and redemptionUrl prioritizes code`() {
        val coupon =
                Coupon(
                        name = "Hybrid Coupon",
                        currentValue = 50.0,
                        initialValue = 50.0,
                        expirationDate = 1700000000000L,
                        categoryId = null,
                        redeemCode = "CODE-123",
                        redemptionUrl = "https://example.com/redeem"
                )

        assertEquals("CODE-123", coupon.redeemCode)
        assertEquals("https://example.com/redeem", coupon.redemptionUrl)
    }
}
