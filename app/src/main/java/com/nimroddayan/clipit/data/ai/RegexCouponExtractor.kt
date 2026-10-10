package com.nimroddayan.clipit.data.ai

import com.nimroddayan.clipit.data.gemini.ParsedCoupon

/** Offline fallback: pulls a value, a code and a known store name out of a message with regexes. */
class RegexCouponExtractor : CouponExtractor {

    override suspend fun extract(text: String): ParsedCoupon {
        val valueMatch = VALUE_REGEX.find(text)
        // Group 2 is the value when the symbol comes first (₪100), group 4 when it comes last (100₪).
        val initialValue = valueMatch?.let { (it.groups[2]?.value ?: it.groups[4]?.value)?.toDoubleOrNull() }

        return ParsedCoupon(
            storeName = KNOWN_STORES.find { text.contains(it, ignoreCase = true) } ?: DEFAULT_STORE_NAME,
            initialValue = initialValue,
            expirationDate = null,
            description = text,
            redeemCode = CODE_REGEX.find(text)?.value,
            redemptionUrl = null,
        )
    }

    private companion object {
        const val DEFAULT_STORE_NAME = "New Coupon"

        val VALUE_REGEX = Regex(
            """(₪|\$|€)\s*(\d+(\.\d{1,2})?)|(\d+(\.\d{1,2})?)\s*(₪|\$|€|NIS|nis|ILS|ils|ש"ח|שקלים)"""
        )

        val CODE_REGEX = Regex(
            """(?<!\d)\d{4,}-\d{4,}(?!\d)|[A-Z0-9]{4,}-[A-Z0-9]{4,}(-[A-Z0-9]{4,})?|\b[A-Z0-9]{5,}\b"""
        )

        val KNOWN_STORES = listOf(
            "KSP", "Super-Pharm", "Wolt", "Dominos", "Mcdonalds", "Nike", "Adidas", "Fox", "Castro",
            "Renuar", "Terminal X", "AliExpress", "Amazon", "Next", "Shein", "Zara", "H&M",
        )
    }
}
