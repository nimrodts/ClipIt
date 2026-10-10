package com.nimroddayan.clipit.data.ai

import com.nimroddayan.clipit.data.gemini.ParsedCoupon
import java.time.LocalDate

/** Cleans extractor output so every source (AI or regex) hands the UI the same shape of data. */
object CouponNormalizer {

    private val URL_PREFIX = Regex("^https?://", RegexOption.IGNORE_CASE)

    fun normalize(coupon: ParsedCoupon, today: LocalDate): ParsedCoupon {
        val code = coupon.redeemCode.cleaned()
        val url = coupon.redemptionUrl.cleaned()
        val codeIsUrl = code != null && URL_PREFIX.containsMatchIn(code)

        return coupon.copy(
            storeName = coupon.storeName.cleaned(),
            redeemCode = if (codeIsUrl) null else code,
            redemptionUrl = url ?: if (codeIsUrl) code else null,
            initialValue = coupon.initialValue?.takeIf { !it.isNaN() && it > 0.0 },
            expirationDate = coupon.expirationDate.cleaned()?.takeIf { isIsoDate(it) }
                ?: today.plusYears(1).toString(),
            description = coupon.description.cleaned(),
        )
    }


    private fun String?.cleaned(): String? =
        this?.trim()?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }


    private fun isIsoDate(value: String): Boolean = runCatching { LocalDate.parse(value) }.isSuccess
}
