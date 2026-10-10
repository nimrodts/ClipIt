package com.nimroddayan.clipit.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nimroddayan.clipit.data.ai.CouponAiRetrier
import com.nimroddayan.clipit.data.ai.RetryOutcome
import java.util.concurrent.TimeUnit

/**
 * Retries the AI on a pending coupon whose extraction failed temporarily. Runs 15 minutes after the
 * failure, then backs off linearly (1h, 2h, 3h, ... capped at 5h by WorkManager) for about a day.
 */
class AiRetryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val couponId = inputData.getLong(KEY_COUPON_ID, -1L)
        if (couponId < 0) return Result.failure()

        val isLastAttempt = runAttemptCount >= MAX_RUNS - 1
        val outcome =
                CouponAiRetrier.create(applicationContext).retryInBackground(couponId, isLastAttempt)
        android.util.Log.d(TAG, "Retry #${runAttemptCount + 1} for coupon $couponId: $outcome")

        return when (outcome) {
            is RetryOutcome.Updated -> {
                val coupon = outcome.coupon
                CouponNotifier.show(
                        applicationContext,
                        "Coupon updated by AI",
                        CouponNotifier.storeAndSender(coupon.name, coupon.sourceSender)
                )
                Result.success()
            }
            is RetryOutcome.StillFailing -> Result.retry()
            is RetryOutcome.Failed, RetryOutcome.Skipped -> Result.success()
        }
    }

    companion object {
        private const val TAG = "AiRetryWorker"
        private const val KEY_COUPON_ID = "coupon_id"
        private const val MAX_RUNS = 8

        fun schedule(context: Context, couponId: Long) {
            val request =
                    OneTimeWorkRequestBuilder<AiRetryWorker>()
                            .setInputData(workDataOf(KEY_COUPON_ID to couponId))
                            .setInitialDelay(15, TimeUnit.MINUTES)
                            .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
                            .setConstraints(
                                    Constraints.Builder()
                                            .setRequiredNetworkType(NetworkType.CONNECTED)
                                            .build()
                            )
                            .build()
            WorkManager.getInstance(context)
                    .enqueueUniqueWork("ai-retry-$couponId", ExistingWorkPolicy.KEEP, request)
        }
    }
}
