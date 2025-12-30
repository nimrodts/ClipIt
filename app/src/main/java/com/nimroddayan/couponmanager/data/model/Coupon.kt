package com.nimroddayan.couponmanager.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Coupon(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val currentValue: Double,
    val initialValue: Double,
    val expirationDate: Long,
    val categoryId: Long,
    val isArchived: Boolean = false
)
