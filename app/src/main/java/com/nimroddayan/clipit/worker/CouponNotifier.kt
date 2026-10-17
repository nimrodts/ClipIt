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
import com.nimroddayan.clipit.MainActivity
import com.nimroddayan.clipit.R

/** Posts the coupon notifications shared by the extraction and AI retry workers. */
object CouponNotifier {

    private const val CHANNEL_ID = "coupon_channel"
    private const val TAG = "CouponNotifier"

    fun show(context: Context, title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                    NotificationChannel(
                                    CHANNEL_ID,
                                    "Coupon Notifications",
                                    NotificationManager.IMPORTANCE_DEFAULT
                            )
                            .apply { description = "Notifications for extracted coupons" }
            val notificationManager =
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
                NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_stat_clipit)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)

        val permission =
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            android.util.Log.d(TAG, "Posting notification: $title")
            NotificationManagerCompat.from(context)
                    .notify(System.currentTimeMillis().toInt(), builder.build())
        } else {
            android.util.Log.w(TAG, "Notification permission missing")
        }
    }


    /** "Fox (from BuyMe)", or just the sender when the store is unknown or is the sender. */
    fun storeAndSender(store: String?, sender: String?): String =
            when {
                store.isNullOrBlank() -> sender.orEmpty()
                sender.isNullOrBlank() || store == sender -> store
                else -> "$store (from $sender)"
            }
}
