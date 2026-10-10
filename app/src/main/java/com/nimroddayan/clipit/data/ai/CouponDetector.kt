package com.nimroddayan.clipit.data.ai

/**
 * Decides on the phone whether a message looks like a coupon, before anything is sent to a cloud
 * model. One-time passwords are always rejected; bank and delivery messages are rejected unless they
 * contain an explicit coupon keyword.
 */
class CouponDetector {

    fun isLikelyCoupon(text: String): Boolean {
        if (text.isBlank() || isSensitive(text)) return false
        if (COUPON_KEYWORDS.any { text.lowercase().contains(it) }) return true

        return CODE_PATTERN.containsMatchIn(text) && VALUE_PATTERN.containsMatchIn(text)
    }


    /**
     * Messages that must never leave the phone, in any detection mode: one-time passwords always, and
     * bank, delivery or "do not share" messages unless they explicitly mention a coupon.
     */
    fun isSensitive(text: String): Boolean {
        val lower = text.lowercase()
        if (OTP_PHRASES.any { lower.contains(it) } || OTP_WORDS.containsMatchIn(text)) return true
        return CONTEXT_REJECT_PHRASES.any { lower.contains(it) } && COUPON_KEYWORDS.none { lower.contains(it) }
    }


    private companion object {
        val OTP_PHRASES = listOf(
            "verification code", "one-time password", "one-time code", "one time password",
            "one time code", "login code", "passcode",
            "אימות", "קוד חד פעמי", "קוד חד-פעמי", "הקוד החד פעמי", "הקוד החד-פעמי",
            "סיסמה חד פעמית", "סיסמה חד-פעמית", "סיסמא חד פעמית", "קוד הזדהות",
        )

        // ASCII-only word boundaries; Hebrew phrases above use plain contains().
        val OTP_WORDS = Regex("""\b(otp|2fa)\b""", RegexOption.IGNORE_CASE)

        val COUPON_KEYWORDS = listOf(
            "coupon", "voucher", "gift", "promo", "redeem",
            "קופון", "שובר", "תו קנייה", "תו שי", "כרטיס מתנה", "גיפט קארד", "גיפטקארד", "מימוש", "הטבה",
        )

        val CONTEXT_REJECT_PHRASES = listOf(
            "do not share", "don't share", "אין למסור", "אל תמסור",
            "transaction on your card", "was charged", "your card ending",
            "חויב", "בוצעה עסקה", "עסקה בכרטיס",
            "tracking number", "your package", "your parcel",
            "מספר מעקב", "החבילה שלך", "המשלוח שלך",
        )

        val CODE_PATTERN = Regex(
            """(?<!\d)\d{4,}-\d{4,}(?!\d)|\b[A-Z0-9]{4,}-[A-Z0-9]{4,}\b|\b(?=[A-Z0-9]*\d)(?=[A-Z0-9]*[A-Z])[A-Z0-9]{6,}\b|(?<![\d-])\d{6,}(?![\d-])"""
        )

        val VALUE_PATTERN = Regex(
            """(₪|\$|€)\s*\d+|\d+\s*(₪|\$|€|nis|ils|ש"ח|שקל)""",
            RegexOption.IGNORE_CASE
        )
    }
}
