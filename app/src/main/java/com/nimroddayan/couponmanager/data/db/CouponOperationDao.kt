package com.nimroddayan.couponmanager.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nimroddayan.couponmanager.data.model.CouponOperation
import kotlinx.coroutines.flow.Flow

@Dao
interface CouponOperationDao {
    @Insert
    suspend fun insert(couponOperation: CouponOperation)

    @Query("SELECT * FROM coupon_operation ORDER BY timestamp DESC")
    fun getAllOperations(): Flow<List<CouponOperation>>

    @Query("DELETE FROM coupon_operation WHERE id = :operationId")
    suspend fun deleteOperation(operationId: Long)
}
