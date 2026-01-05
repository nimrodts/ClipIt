package com.nimroddayan.couponmanager.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Coupon::class,
            parentColumns = ["id"],
            childColumns = ["couponId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CouponHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val couponId: Long,
    val action: String,
    val changeSummary: String,
    val timestamp: Long = System.currentTimeMillis(),
    val couponState: String,
)
