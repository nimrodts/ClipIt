package com.nimroddayan.clipit.data.ai

import kotlinx.serialization.Serializable

/** Why a pending coupon still needs the AI. A coupon with no AI problem has no status. */
@Serializable
enum class AiStatus {
    /** The AI was temporarily unavailable; a background job keeps retrying. */
    FAILED_RETRYING,

    /** The AI failed and won't be retried automatically; the user can retry by hand. */
    FAILED,
}
