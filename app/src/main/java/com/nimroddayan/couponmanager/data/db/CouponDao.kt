package com.nimroddayan.couponmanager.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nimroddayan.couponmanager.data.model.Coupon
import kotlinx.coroutines.flow.Flow

@Dao
interface CouponDao {
    @Query("SELECT * FROM coupon WHERE isArchived = 0")
    fun getAll(): Flow<List<Coupon>>

    @Query("SELECT * FROM coupon WHERE isArchived = 1")
    fun getArchived(): Flow<List<Coupon>>

    @Insert
    suspend fun insertAll(vararg coupons: Coupon)

    @Delete
    suspend fun delete(coupon: Coupon)

    @Query("DELETE FROM coupon")
    suspend fun clearAll()

    @Update
    suspend fun update(coupon: Coupon)
}
