package com.nimroddayan.clipit.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nimroddayan.clipit.CouponApplication
import com.nimroddayan.clipit.MainActivity
import com.nimroddayan.clipit.R
import com.nimroddayan.clipit.data.UserPreferencesRepository
import com.nimroddayan.clipit.data.ai.CouponParser
import com.nimroddayan.clipit.data.gemini.ParsedCoupon
import com.nimroddayan.clipit.data.model.Coupon
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

        // 3. Extract
        try {
            val parsedCoupon = parser.parse(smsBody)
            if (parsedCoupon.error == ParsedCoupon.ERROR_QUOTA) {
                showNotification(
                        "Quota Exceeded",
                        "Coupons extraction paused. Will retry automatically."
                )
                return Result.retry()
            } else if (parsedCoupon.error == ParsedCoupon.ERROR_TRANSIENT &&
                            runAttemptCount < MAX_TRANSIENT_RETRIES
            ) {
                android.util.Log.w("CouponWorker", "AI temporarily unavailable, retrying later")
                return Result.retry()
            } else if (parsedCoupon.error != null) {
                android.util.Log.w(
                        "CouponWorker",
                        "Extraction warning: ${parsedCoupon.error}. Using fallback data."
                )
            }

            // 4. Persist as Pending
            val repository = (context as CouponApplication).couponRepository
            val newCoupon =
                    Coupon(
                            name = parsedCoupon.storeName ?: sender,
                            currentValue = parsedCoupon.initialValue ?: 0.0,
                            initialValue = parsedCoupon.initialValue ?: 0.0,
                            expirationDate =
                                    try {
                                        parsedCoupon.expirationDate?.toLongOrNull()
                                                ?: System.currentTimeMillis() // Fallback handling
                                        // needed for date
                                        // string parsing if
                                        // it's not Long
                                    } catch (e: Exception) {
                                        System.currentTimeMillis()
                                    },
                            categoryId = null,
                            redeemCode = parsedCoupon.redeemCode,
                            creationMessage = parsedCoupon.description,
                            isPending = true,
                            redemptionUrl = parsedCoupon.redemptionUrl
                    )
            // Note: expirationDate in Coupon is Long, but ParsedCoupon returns String (YYYY-MM-DD).
            // We need to parse it or change Coupon model. Coupon model uses Long.
            // Let's do a simple parsing or fallback.
            // For now, I will use System.currentTimeMillis() if parsing fails, but I should improve
            // this.
            // Actually, existing AddCouponDialog logic parses it? No, AddCouponDialog takes Long.
            // Let's try to parse "YYYY-MM-DD" to Epoch Long.

            // Correction: Coupon.expirationDate is Long. ParsedCoupon.expirationDate is String.
            val expirationLong =
                    try {
                        java.time.LocalDate.parse(parsedCoupon.expirationDate)
                                .atStartOfDay(java.time.ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli()
                    } catch (e: Exception) {
                        System.currentTimeMillis() + 31536000000L // +1 year default
                    }

            var finalCoupon = newCoupon.copy(expirationDate = expirationLong)

            // Junk Filter / Draft Handler
            // If we have no useful info (extraction failed completely), save as "Review Required"
            if (finalCoupon.currentValue == 0.0 && finalCoupon.redeemCode.isNullOrBlank()) {
                android.util.Log.w(
                        "CouponWorker",
                        "Extraction yielded no data. Saving as Draft/Review."
                )
                finalCoupon =
                        finalCoupon.copy(
                                name = "Review Required",
                                redeemCode =
                                        "REVIEW-${System.currentTimeMillis()}", // Unique code to
                                // allow insertion
                                creationMessage =
                                        smsBody, // Preserve original SMS for user reference
                                redemptionUrl = finalCoupon.redemptionUrl
                        )
                showNotification("Coupon Review Needed", "Extraction failed. Tap to edit manually.")
            } else {
                showNotification(
                        "New Coupon Found",
                        "Pending review: ${parsedCoupon.storeName ?: sender}"
                )
            }

            repository.insert(finalCoupon)

            return Result.success()
        } catch (e: com.nimroddayan.clipit.data.DuplicateRedeemCodeException) {
            android.util.Log.w("CouponWorker", "Duplicate coupon detected, ignoring.")
            return Result.success()
        } catch (e: Exception) {
            android.util.Log.e("CouponWorker", "Extraction failed", e)
            return Result.failure()
        }
    }

    private companion object {
        // After this many tries the regex result is saved instead, so the coupon isn't lost.
        const val MAX_TRANSIENT_RETRIES = 3
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "coupon_channel"
        val context = applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Coupon Notifications"
            val descriptionText = "Notifications for extracted coupons"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                    NotificationChannel(channelId, name, importance).apply {
                        description = descriptionText
                    }
            val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
        val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder =
                NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher_round) // Replace with valid icon
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)

        val permission =
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        android.util.Log.d(
                "CouponWorker",
                "Checking notification permission: $permission (Granted=${PackageManager.PERMISSION_GRANTED})"
        )

        if (permission == PackageManager.PERMISSION_GRANTED) {
            android.util.Log.d("CouponWorker", "Posting notification: $title")
            NotificationManagerCompat.from(context)
                    .notify(System.currentTimeMillis().toInt(), builder.build())
        } else {
            android.util.Log.w("CouponWorker", "Notification permission missing")
        }
    }
}
