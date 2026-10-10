package com.nimroddayan.clipit.data.ai

import com.nimroddayan.clipit.data.gemini.ParsedCoupon
import com.nimroddayan.clipit.data.model.Coupon
import java.time.LocalDate
import java.time.ZoneId

/** Turns the parse result of an SMS into a pending coupon awaiting the user's review. */
object PendingCouponBuilder {

    const val REVIEW_REQUIRED_NAME = "Review Required"
    private const val ONE_YEAR_MILLIS = 31_536_000_000L

    fun build(
        parsed: ParsedCoupon,
        sender: String,
        sms: String,
        now: Long,
        zone: ZoneId = ZoneId.systemDefault(),
    ): Coupon {
        val value = parsed.initialValue ?: 0.0
        val status = statusFor(parsed.error)
        val coupon = Coupon(
            name = parsed.storeName ?: sender,
            currentValue = value,
            initialValue = value,
            expirationDate = parseExpiration(parsed.expirationDate, zone) ?: (now + ONE_YEAR_MILLIS),
            categoryId = null,
            redeemCode = parsed.redeemCode,
            creationMessage = parsed.description,
            isPending = true,
            redemptionUrl = parsed.redemptionUrl,
            sourceSms = sms,
            sourceSender = sender,
            aiStatus = status,
            aiError = parsed.error.takeIf { status != null },
        )

        // Nothing useful was found: keep a draft holding the SMS so the user can fill it in.
        // The unique code lets it past the duplicate redeem code check.
        if (value == 0.0 && coupon.redeemCode.isNullOrBlank()) {
            return coupon.copy(name = REVIEW_REQUIRED_NAME, redeemCode = "REVIEW-$now", creationMessage = sms)
        }
        return coupon
    }


    /** Transient and quota errors are worth retrying later; anything else needs the user. */
    fun statusFor(error: String?): AiStatus? = when (error) {
        null -> null
        ParsedCoupon.ERROR_TRANSIENT, ParsedCoupon.ERROR_QUOTA -> AiStatus.FAILED_RETRYING
        else -> AiStatus.FAILED
    }


    private fun parseExpiration(date: String?, zone: ZoneId): Long? =
        try {
            date?.let { LocalDate.parse(it).atStartOfDay(zone).toInstant().toEpochMilli() }
        } catch (e: Exception) {
            null
        }
}
