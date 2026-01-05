package com.nimroddayan.couponmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "coupon_operation")
data class CouponOperation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val couponId: Long,
    val operationType: String, // "used", "edited", "archived"
    val couponJson: String, // JSON representation of the coupon state before the operation
    val couponJsonAfter: String? = null, // JSON representation of the coupon state after the operation
    val timestamp: Date = Date()
)
