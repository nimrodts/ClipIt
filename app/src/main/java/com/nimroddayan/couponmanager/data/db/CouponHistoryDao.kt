package com.nimroddayan.couponmanager.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.nimroddayan.couponmanager.data.model.CouponHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface CouponHistoryDao {
    @Insert
    suspend fun insert(couponHistory: CouponHistory)

    @Query("SELECT * FROM CouponHistory WHERE couponId = :couponId ORDER BY timestamp DESC")
    fun getHistoryForCoupon(couponId: Long): Flow<List<CouponHistory>>

    @Query("SELECT * FROM CouponHistory")
    fun getAll(): Flow<List<CouponHistory>>

    @Delete
    suspend fun delete(couponHistory: CouponHistory)
}
