package com.nimroddayan.clipit.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nimroddayan.clipit.CouponApplication
import com.nimroddayan.clipit.data.UserPreferencesRepository
import com.nimroddayan.clipit.data.ai.AiStatus
import com.nimroddayan.clipit.data.ai.CouponParser
import com.nimroddayan.clipit.data.ai.PendingCouponBuilder
import kotlinx.coroutines.flow.first

class CouponExtractionWorker(context: Context, params: WorkerParameters) :
        CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val sender = inputData.getString("sender") ?: return Result.failure()
        val smsBody = inputData.getString("body") ?: return Result.failure()

        android.util.Log.d("CouponWorker", "Starting work for sender=$sender")

        val context = applicationContext
        val userPreferencesRepository = UserPreferencesRepository(context)

        // 1. Whitelist Check
        val whitelist = userPreferencesRepository.smsSenderWhitelist.first()
        if (whitelist.isNotEmpty() && !whitelist.contains(sender)) {
            android.util.Log.d(
                    "CouponWorker",
                    "Sender not in whitelist. Whitelist size: ${whitelist.size}"
            )
            // Sender not in whitelist, ignore
            return Result.success()
        }

        // 2. Coupon Content Check (on-device, nothing leaves the phone here)
        val parser = CouponParser.create(context)

        if (!parser.shouldExtract(smsBody)) {
            android.util.Log.d("CouponWorker", "No coupon detected in message")
            return Result.success()
        }

        android.util.Log.d("CouponWorker", "Coupon detected! Extracting...")

        // 3. Extract, and persist as Pending even when the AI failed, so the SMS is never lost
        try {
            val parsedCoupon = parser.parse(smsBody)
            val coupon =
                    PendingCouponBuilder.build(
                            parsedCoupon,
                            sender,
                            smsBody,
                            System.currentTimeMillis()
                    )
            if (parsedCoupon.error != null) {
                android.util.Log.w(
                        "CouponWorker",
                        "Extraction warning: ${parsedCoupon.error}. Saving with ${coupon.aiStatus}."
                )
            }

            val repository = (context as CouponApplication).couponRepository
            val couponId = repository.insert(coupon)
            val from = CouponNotifier.storeAndSender(parsedCoupon.storeName, sender)

            when {
                coupon.aiStatus == AiStatus.FAILED_RETRYING -> {
                    AiRetryWorker.schedule(context, couponId)
                    CouponNotifier.show(
                            context,
                            "Coupon saved — AI unavailable",
                            "From $sender. Will retry automatically."
                    )
                }
                coupon.aiStatus == AiStatus.FAILED ->
                        CouponNotifier.show(
                                context,
                                "Coupon saved — AI failed",
                                "From $sender. Open Pending Coupons to retry or edit."
                        )
                coupon.name == PendingCouponBuilder.REVIEW_REQUIRED_NAME ->
                        CouponNotifier.show(
                                context,
                                "Coupon Review Needed",
                                "Extraction failed. Tap to edit manually."
                        )
                else -> CouponNotifier.show(context, "New Coupon Found", "Pending review: $from")
            }

            return Result.success()
        } catch (e: com.nimroddayan.clipit.data.DuplicateRedeemCodeException) {
            android.util.Log.w("CouponWorker", "Duplicate coupon detected, ignoring.")
            return Result.success()
        } catch (e: Exception) {
            android.util.Log.e("CouponWorker", "Extraction failed", e)
            return Result.failure()
        }
    }
}
