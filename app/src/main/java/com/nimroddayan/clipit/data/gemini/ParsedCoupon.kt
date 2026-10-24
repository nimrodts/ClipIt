package com.nimroddayan.clipit.data.gemini

data class ParsedCoupon(
        val storeName: String?,
        val initialValue: Double?,
        val expirationDate: String?,
        val description: String?,
        val redeemCode: String?,
        val redemptionUrl: String?,
        val error: String? = null,
        val categoryId: Long? = null
) {
    companion object {
        const val ERROR_QUOTA = "Quota Limit Reached"
        const val ERROR_NO_FREE_QUOTA =
                "This model has no free quota on your API key. Pick another model in AI Settings."
        const val ERROR_INVALID_KEY = "Invalid Gemini API key"
        const val ERROR_TRANSIENT = "AI is temporarily unavailable. Please try again."
    }
}
