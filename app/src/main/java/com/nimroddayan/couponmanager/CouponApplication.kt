package com.nimroddayan.couponmanager

import android.app.Application
import com.nimroddayan.couponmanager.data.db.AppDatabase

class CouponApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
