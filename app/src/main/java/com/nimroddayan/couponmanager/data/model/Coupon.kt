package com.nimroddayan.couponmanager.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
        foreignKeys =
                [
                        ForeignKey(
                                entity = Category::class,
                                parentColumns = ["id"],
                                childColumns = ["categoryId"],
                                onDelete = ForeignKey.SET_NULL
                        )],
        indices = [Index(value = ["categoryId"])]
)
data class Coupon(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val name: String,
        val currentValue: Double,
        val initialValue: Double,
        val expirationDate: Long,
        val categoryId: Long?,
        val redeemCode: String? = null,
        val isArchived: Boolean = false,
        val creationMessage: String? = null,
        val isOneTime: Boolean = false,
)
