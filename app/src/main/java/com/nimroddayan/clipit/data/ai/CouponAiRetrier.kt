package com.nimroddayan.clipit.data.ai

import android.content.Context
import com.nimroddayan.clipit.CouponApplication
import com.nimroddayan.clipit.data.CouponRepository
import com.nimroddayan.clipit.data.gemini.ParsedCoupon
import com.nimroddayan.clipit.data.model.Coupon

/** The coupon storage the retrier needs; lets it be tested without Room. */
interface PendingCouponStore {
    suspend fun get(id: Long): Coupon?
    suspend fun findByRedeemCode(code: String): Coupon?
    suspend fun save(coupon: Coupon)
}


sealed interface RetryOutcome {
    data class Updated(val coupon: Coupon) : RetryOutcome

    /** Still failing; background retries continue. */
    data class StillFailing(val error: String) : RetryOutcome

    /** Failed and no automatic retries remain. */
    data class Failed(val error: String) : RetryOutcome

    /** Nothing to do: the coupon is gone, approved, already fixed, or changed meanwhile. */
    data object Skipped : RetryOutcome
}


/** Re-runs the AI on the saved SMS of a pending coupon whose first extraction failed. */
class CouponAiRetrier(
    private val parse: suspend (String) -> ParsedCoupon,
    private val store: PendingCouponStore,
    private val now: () -> Long = System::currentTimeMillis,
) {

    suspend fun retryInBackground(couponId: Long, isLastAttempt: Boolean): RetryOutcome =
        retry(couponId, manual = false, isLastAttempt = isLastAttempt)


    suspend fun retryNow(couponId: Long): RetryOutcome = retry(couponId, manual = true, isLastAttempt = false)


    private suspend fun retry(couponId: Long, manual: Boolean, isLastAttempt: Boolean): RetryOutcome {
        val coupon = store.get(couponId) ?: return RetryOutcome.Skipped
        val sms = coupon.sourceSms
        if (!coupon.isPending || sms == null) return RetryOutcome.Skipped
        if (!manual && coupon.aiStatus != AiStatus.FAILED_RETRYING) return RetryOutcome.Skipped

        val parsed = parse(sms)
        val error = parsed.error
        if (error == null) {
            val updated = PendingCouponBuilder.build(parsed, coupon.sourceSender ?: coupon.name, sms, now())
                .copy(id = coupon.id, categoryId = coupon.categoryId)
            val owner = updated.redeemCode?.takeIf { it.isNotBlank() }?.let { store.findByRedeemCode(it) }
            if (owner != null && owner.id != coupon.id) {
                return saveFailure(coupon, ERROR_DUPLICATE, AiStatus.FAILED)
            }
            if (store.get(couponId) != coupon) return RetryOutcome.Skipped
            store.save(updated)
            return RetryOutcome.Updated(updated)
        }

        // A manual attempt never ends background retries that are still running.
        val keepRetrying = coupon.aiStatus == AiStatus.FAILED_RETRYING &&
            PendingCouponBuilder.statusFor(error) == AiStatus.FAILED_RETRYING &&
            (manual || !isLastAttempt)
        return saveFailure(coupon, error, if (keepRetrying) AiStatus.FAILED_RETRYING else AiStatus.FAILED)
    }


    private suspend fun saveFailure(coupon: Coupon, error: String, status: AiStatus): RetryOutcome {
        if (store.get(coupon.id) != coupon) return RetryOutcome.Skipped
        store.save(coupon.copy(aiStatus = status, aiError = error))
        return if (status == AiStatus.FAILED_RETRYING) RetryOutcome.StillFailing(error) else RetryOutcome.Failed(error)
    }

    companion object {
        const val ERROR_DUPLICATE = "Looks like a duplicate: another coupon already has this code."

        fun create(context: Context): CouponAiRetrier {
            val app = context.applicationContext as CouponApplication
            val parser = CouponParser.create(app)
            return CouponAiRetrier(parse = parser::parse, store = RepositoryPendingCouponStore(app.couponRepository))
        }
    }
}


private class RepositoryPendingCouponStore(private val repository: CouponRepository) : PendingCouponStore {
    override suspend fun get(id: Long): Coupon? = repository.getCouponById(id)

    override suspend fun findByRedeemCode(code: String): Coupon? = repository.findByRedeemCode(code)

    override suspend fun save(coupon: Coupon) = repository.replacePending(coupon)
}
