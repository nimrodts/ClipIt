package com.nimroddayan.couponmanager

import android.app.Application
import com.nimroddayan.couponmanager.data.CouponRepository
import com.nimroddayan.couponmanager.data.db.AppDatabase

class CouponApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val couponRepository: CouponRepository by lazy { CouponRepository(database.couponDao(), database.couponHistoryDao()) }
}
