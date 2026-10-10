package com.nimroddayan.clipit.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.nimroddayan.clipit.data.model.CouponHistory
import com.nimroddayan.clipit.data.model.MonthlySpending
import kotlinx.coroutines.flow.Flow

@Dao
interface CouponHistoryDao {
    @Insert suspend fun insert(couponHistory: CouponHistory)

    @Query("SELECT * FROM coupon_history WHERE couponId = :couponId ORDER BY timestamp DESC")
    fun getHistoryForCoupon(couponId: Long): Flow<List<CouponHistory>>

    @Query("SELECT * FROM coupon_history") fun getAll(): Flow<List<CouponHistory>>

    @Query(
            "SELECT strftime('%Y-%m', ch.timestamp / 1000, 'unixepoch') as month, SUM(CAST(ch.changeSummary AS REAL)) as totalSpent FROM coupon_history ch JOIN Coupon c ON ch.couponId = c.id WHERE ch.action = 'Coupon Used' AND c.isOneTime = 0 GROUP BY month ORDER BY month DESC"
    )
    fun getSpendingByMonth(): Flow<List<MonthlySpending>>

    @Query("DELETE FROM coupon_history WHERE couponId = :couponId AND timestamp >= :since")
    suspend fun deleteSince(couponId: Long, since: Long)

    @Delete suspend fun delete(couponHistory: CouponHistory)
}


