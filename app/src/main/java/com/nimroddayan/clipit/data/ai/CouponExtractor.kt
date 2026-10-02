package com.nimroddayan.clipit.data.ai

import com.nimroddayan.clipit.data.gemini.ParsedCoupon

/** Turns a message that is believed to contain a coupon into coupon fields. */
interface CouponExtractor {
    suspend fun extract(text: String): ParsedCoupon
}
