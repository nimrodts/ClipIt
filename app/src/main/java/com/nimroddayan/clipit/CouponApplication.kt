package com.nimroddayan.clipit

import android.app.Application
import com.nimroddayan.clipit.data.CouponRepository
import com.nimroddayan.clipit.data.db.AppDatabase
import com.nimroddayan.clipit.data.gemini.GeminiRestClient

class CouponApplication : Application() {
    val geminiRestClient: GeminiRestClient by lazy { GeminiRestClient() }

    private var _database: AppDatabase? = null
    val database: AppDatabase
        get() {
            if (_database == null) {
                _database = AppDatabase.getDatabase(this)
            }
            return _database!!
        }

    private var _couponRepository: CouponRepository? = null
    val couponRepository: CouponRepository
        get() {
            if (_couponRepository == null) {
                _couponRepository =
                        CouponRepository(database.couponDao(), database.couponHistoryDao())
            }
            return _couponRepository!!
        }

    fun resetDependencies() {
        AppDatabase.enableAccess()
        _database = null
        _couponRepository = null
    }
}


