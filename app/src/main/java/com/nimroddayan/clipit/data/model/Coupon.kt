package com.nimroddayan.clipit.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nimroddayan.clipit.data.ai.AiStatus
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
        val isPending: Boolean = false,
        val redemptionUrl: String? = null,
        // Set for coupons created from an SMS; aiStatus stays null unless the AI failed on it.
        val sourceSms: String? = null,
        val sourceSender: String? = null,
        val aiStatus: AiStatus? = null,
        val aiError: String? = null,
)
