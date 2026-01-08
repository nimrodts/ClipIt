package com.nimroddayan.couponmanager.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nimroddayan.couponmanager.data.model.CategorySpending
import com.nimroddayan.couponmanager.data.model.Coupon
import kotlinx.coroutines.flow.Flow

@Dao
interface CouponDao {
    @Query("SELECT * FROM coupon WHERE isArchived = 0") fun getAll(): Flow<List<Coupon>>

    @Query("SELECT * FROM coupon WHERE id = :couponId")
    suspend fun getCouponById(couponId: Long): Coupon?

    @Query("SELECT * FROM coupon WHERE redeemCode = :redeemCode")
    suspend fun getCouponByRedeemCode(redeemCode: String): Coupon?

    @Query("SELECT * FROM coupon WHERE id = :couponId")
    fun getCouponByIdFlow(couponId: Long): Flow<Coupon?>

    @Query("SELECT * FROM coupon WHERE isArchived = 1") fun getArchived(): Flow<List<Coupon>>

    @Query("SELECT SUM(currentValue) FROM coupon WHERE isArchived = 0 AND isOneTime = 0")
    fun getTotalBalance(): Flow<Double>

    @Query("SELECT SUM(initialValue - currentValue) FROM coupon WHERE isOneTime = 0")
    fun getTotalSpent(): Flow<Double>

    @Query(
            "SELECT IFNULL(c.name, 'Uncategorized') as name, IFNULL(c.colorHex, '#808080') as colorHex, SUM(co.initialValue - co.currentValue) as totalSpent FROM coupon co LEFT JOIN category c ON co.categoryId = c.id WHERE co.isOneTime = 0 GROUP BY c.name, c.colorHex"
    )
    fun getSpendingByCategory(): Flow<List<CategorySpending>>

    @Query("UPDATE coupon SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun updateCategoryForCoupons(oldCategoryId: Long, newCategoryId: Long?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg coupons: Coupon): List<Long>

    @Delete suspend fun delete(coupon: Coupon)

    @Query("DELETE FROM coupon") suspend fun clearAll()

    @Update suspend fun update(coupon: Coupon)
}
