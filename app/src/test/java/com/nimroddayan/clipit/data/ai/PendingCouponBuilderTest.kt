package com.nimroddayan.clipit.data.ai

import com.nimroddayan.clipit.data.gemini.ParsedCoupon
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PendingCouponBuilderTest {

    private val now = 1_790_000_000_000L
    private val sms = "Your Fox gift card: 1234-5678, 100 NIS"
    private val good = ParsedCoupon("Fox", 100.0, "2027-01-31", "Fox gift card", "1234-5678", null)

    private fun build(parsed: ParsedCoupon) =
        PendingCouponBuilder.build(parsed, sender = "BuyMe", sms = sms, now = now, zone = ZoneOffset.UTC)

    @Test
    fun `successful parse becomes a pending coupon with no ai status`() {
        val coupon = build(good)
        assertEquals("Fox", coupon.name)
        assertEquals(100.0, coupon.initialValue, 0.0)
        assertEquals(100.0, coupon.currentValue, 0.0)
        assertEquals(LocalDate.of(2027, 1, 31).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(), coupon.expirationDate)
        assertEquals("1234-5678", coupon.redeemCode)
        assertEquals("Fox gift card", coupon.creationMessage)
        assertTrue(coupon.isPending)
        assertNull(coupon.aiStatus)
        assertNull(coupon.aiError)
    }

    @Test
    fun `ai picked category is applied`() {
        assertEquals(4L, build(good.copy(categoryId = 4)).categoryId)
        assertNull(build(good).categoryId)
    }

    @Test
    fun `original sms and sender are always kept`() {
        val coupon = build(good)
        assertEquals(sms, coupon.sourceSms)
        assertEquals("BuyMe", coupon.sourceSender)
    }

    @Test
    fun `missing store name falls back to the sender`() {
        assertEquals("BuyMe", build(good.copy(storeName = null)).name)
    }

    @Test
    fun `unparseable expiration defaults to one year from now`() {
        assertEquals(now + 31_536_000_000L, build(good.copy(expirationDate = "soon")).expirationDate)
        assertEquals(now + 31_536_000_000L, build(good.copy(expirationDate = null)).expirationDate)
    }

    @Test
    fun `no value and no code becomes a review required draft holding the sms`() {
        val coupon = build(good.copy(initialValue = null, redeemCode = null))
        assertEquals("Review Required", coupon.name)
        assertEquals("REVIEW-$now", coupon.redeemCode)
        assertEquals(sms, coupon.creationMessage)
    }

    @Test
    fun `transient and quota errors keep retrying`() {
        assertEquals(AiStatus.FAILED_RETRYING, PendingCouponBuilder.statusFor(ParsedCoupon.ERROR_TRANSIENT))
        assertEquals(AiStatus.FAILED_RETRYING, PendingCouponBuilder.statusFor(ParsedCoupon.ERROR_QUOTA))
    }

    @Test
    fun `invalid key and other errors fail without automatic retry`() {
        assertEquals(AiStatus.FAILED, PendingCouponBuilder.statusFor(ParsedCoupon.ERROR_INVALID_KEY))
        assertEquals(AiStatus.FAILED, PendingCouponBuilder.statusFor("AI Extraction Failed: boom"))
        assertNull(PendingCouponBuilder.statusFor(null))
    }

    @Test
    fun `failed parse records status and error on the coupon`() {
        val coupon = build(good.copy(error = ParsedCoupon.ERROR_TRANSIENT))
        assertEquals(AiStatus.FAILED_RETRYING, coupon.aiStatus)
        assertEquals(ParsedCoupon.ERROR_TRANSIENT, coupon.aiError)
    }
}
