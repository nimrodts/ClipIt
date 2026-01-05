package com.nimroddayan.couponmanager.data.repo

import com.nimroddayan.couponmanager.data.db.CouponHistoryDao
import com.nimroddayan.couponmanager.data.model.CouponHistory
import kotlinx.coroutines.flow.Flow

class CouponHistoryRepository(private val couponHistoryDao: CouponHistoryDao) {
    fun getHistoryForCoupon(couponId: Long): Flow<List<CouponHistory>> {
        return couponHistoryDao.getHistoryForCoupon(couponId)
    }

    suspend fun addHistoryEntry(couponHistory: CouponHistory) {
        couponHistoryDao.insert(couponHistory)
    }
}
