package com.nimroddayan.clipit.data.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CouponDetectorTest {

    private val detector = CouponDetector()

    // ========== Coupons (must be detected) ==========

    @Test
    fun `english coupon keywords are detected`() {
        assertTrue(detector.isLikelyCoupon("Your coupon is ready"))
        assertTrue(detector.isLikelyCoupon("Your voucher ABC123 is ready"))
        assertTrue(detector.isLikelyCoupon("Here is your gift card worth ₪100"))
        assertTrue(detector.isLikelyCoupon("Use promo code SAVE20 at checkout"))
    }

    @Test
    fun `hebrew coupon keywords are detected`() {
        assertTrue(detector.isLikelyCoupon("קופון הנחה בשווי 50 שקל"))
        assertTrue(detector.isLikelyCoupon("שובר מתנה לרכישה"))
        assertTrue(detector.isLikelyCoupon("כרטיס מתנה בשבילך"))
        assertTrue(detector.isLikelyCoupon("ניתן למימוש בחנות"))
        assertTrue(detector.isLikelyCoupon("תו קנייה בשווי 200 ש\"ח"))
    }

    @Test
    fun `code plus value without keyword is detected`() {
        assertTrue(detector.isLikelyCoupon("Your Shufersal card: 1234-5678, value ₪200"))
    }

    @Test
    fun `keywords are case insensitive`() {
        assertTrue(detector.isLikelyCoupon("COUPON"))
        assertTrue(detector.isLikelyCoupon("Coupon"))
    }

    @Test
    fun `gift card that says do not share is still detected`() {
        assertTrue(
            detector.isLikelyCoupon("Your gift card code 1234-5678 worth ₪100. Do not share this code.")
        )
    }

    @Test
    fun `single use coupons are not mistaken for one time passwords`() {
        assertTrue(detector.isLikelyCoupon("Your one-time use coupon: SAVE20"))
        assertTrue(detector.isLikelyCoupon("שובר חד פעמי לרכישה"))
    }

    // ========== Not coupons (must stay on the phone) ==========

    @Test
    fun `one time passwords are rejected`() {
        assertFalse(detector.isLikelyCoupon("Your verification code is 482913. Do not share it."))
        assertFalse(detector.isLikelyCoupon("קוד האימות שלך הוא 482913"))
        assertFalse(detector.isLikelyCoupon("Your OTP is 482913"))
        assertFalse(detector.isLikelyCoupon("Your login code: 123456"))
        assertFalse(detector.isLikelyCoupon("הקוד החד פעמי שלך: 482913"))
    }

    @Test
    fun `otp rejection wins over coupon keywords`() {
        assertFalse(detector.isLikelyCoupon("Your verification code for the Coupon app is 482913"))
    }

    @Test
    fun `bank alerts are rejected`() {
        assertFalse(detector.isLikelyCoupon("Transaction on your card ending 1234: ₪250 at SHUFERSAL"))
        assertFalse(detector.isLikelyCoupon("בוצעה עסקה בכרטיס 1234 בסך 250 ש\"ח"))
    }

    @Test
    fun `delivery messages with code and value are rejected`() {
        assertFalse(detector.isLikelyCoupon("Your parcel tracking number RR123456789IL, delivery fee \$5"))
    }

    @Test
    fun `plain and marketing text is rejected`() {
        assertFalse(detector.isLikelyCoupon(""))
        assertFalse(detector.isLikelyCoupon("   "))
        assertFalse(detector.isLikelyCoupon("Get 20% Off everything this weekend"))
        assertFalse(detector.isLikelyCoupon("office hours changed"))
        assertFalse(detector.isLikelyCoupon("Meeting at 3pm, call 555-1234"))
        assertFalse(detector.isLikelyCoupon("Date: 2024-01-15"))
        assertFalse(detector.isLikelyCoupon("Your package ABC123456 is on its way"))
    }
}
