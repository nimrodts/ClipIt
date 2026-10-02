package com.nimroddayan.clipit.data.gemini

data class ParsedCoupon(
        val storeName: String?,
        val initialValue: Double?,
        val expirationDate: String?,
        val description: String?,
        val redeemCode: String?,
        val redemptionUrl: String?,
        val error: String? = null
) {
    companion object {
        const val ERROR_QUOTA = "Quota Limit Reached"
        const val ERROR_INVALID_KEY = "Invalid Gemini API key"
    }
}
