package com.nimroddayan.couponmanager.data

import com.nimroddayan.couponmanager.data.db.CouponDao
import com.nimroddayan.couponmanager.data.db.CouponHistoryDao
import com.nimroddayan.couponmanager.data.model.Coupon
import com.nimroddayan.couponmanager.data.model.CouponHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CouponRepository(
    private val couponDao: CouponDao,
    private val couponHistoryDao: CouponHistoryDao,
) {
    val allCoupons: Flow<List<Coupon>> = couponDao.getAll()
    val archivedCoupons: Flow<List<Coupon>> = couponDao.getArchived()

    suspend fun getCouponById(couponId: Long): Coupon? {
        return couponDao.getCouponById(couponId)
    }

    fun getCouponByIdFlow(couponId: Long): Flow<Coupon?> {
        return couponDao.getCouponByIdFlow(couponId)
    }

    suspend fun insert(coupon: Coupon) {
        coupon.redeemCode?.takeIf { it.isNotBlank() }?.let {
            if (couponDao.getCouponByRedeemCode(it) != null) {
                throw Exception("A coupon with this redeem code already exists.")
            }
        }

        val ids = couponDao.insertAll(coupon)
        val id = ids.first()
        val couponState = Json.encodeToString(coupon.copy(id = id))
        val history = CouponHistory(
            couponId = id,
            action = "Coupon Created",
            changeSummary = "Coupon created with initial value of ${coupon.initialValue}",
            couponState = couponState,
        )
        couponHistoryDao.insert(history)
    }

    suspend fun update(coupon: Coupon) {
        val oldCoupon = couponDao.getCouponById(coupon.id)
        if (oldCoupon != null) {
            val changeSummary = generateChangeSummary(oldCoupon, coupon)
            val couponState = Json.encodeToString(oldCoupon)
            val history = CouponHistory(
                couponId = coupon.id,
                action = "Coupon Edited",
                changeSummary = changeSummary,
                couponState = couponState,
            )
            couponHistoryDao.insert(history)
        }
        couponDao.update(coupon)
    }

    suspend fun delete(coupon: Coupon) {
        couponDao.delete(coupon)
    }

    suspend fun archive(coupon: Coupon) {
        val couponState = Json.encodeToString(coupon)
        val history = CouponHistory(
            couponId = coupon.id,
            action = "Coupon Archived",
            changeSummary = "Coupon has been archived",
            couponState = couponState,
        )
        couponHistoryDao.insert(history)
        val updatedCoupon = coupon.copy(isArchived = true)
        couponDao.update(updatedCoupon)
    }

    suspend fun unarchive(coupon: Coupon) {
        val couponState = Json.encodeToString(coupon)
        val history = CouponHistory(
            couponId = coupon.id,
            action = "Coupon Unarchived",
            changeSummary = "Coupon has been unarchived",
            couponState = couponState,
        )
        couponHistoryDao.insert(history)
        val updatedCoupon = coupon.copy(isArchived = false)
        couponDao.update(updatedCoupon)
    }

    suspend fun use(coupon: Coupon, amount: Double) {
        val updatedCoupon = coupon.copy(currentValue = coupon.currentValue - amount)
        val couponState = Json.encodeToString(coupon)
        val history = CouponHistory(
            couponId = coupon.id,
            action = "Coupon Used",
            changeSummary = amount.toString(),
            couponState = couponState,
        )
        couponHistoryDao.insert(history)
        couponDao.update(updatedCoupon)
    }

    suspend fun undo(operation: CouponHistory) {
        if (operation.action == "Coupon Used") {
            val usedAmount = operation.changeSummary.toDoubleOrNull() ?: 0.0
            val coupon = couponDao.getCouponById(operation.couponId)
            if (coupon != null) {
                val restoredCoupon = coupon.copy(currentValue = coupon.currentValue + usedAmount)
                couponDao.update(restoredCoupon)
            }
        } else {
            val coupon = Json.decodeFromString<Coupon>(operation.couponState)
            couponDao.update(coupon)
        }
        couponHistoryDao.delete(operation)
    }

    private fun generateChangeSummary(oldCoupon: Coupon, newCoupon: Coupon): String {
        val changes = mutableListOf<String>()
        if (oldCoupon.name != newCoupon.name) {
            changes.add("Name changed from '${oldCoupon.name}' to '${newCoupon.name}'")
        }
        if (oldCoupon.currentValue != newCoupon.currentValue) {
            changes.add("Balance changed from ${oldCoupon.currentValue} to ${newCoupon.currentValue}")
        }
        if (oldCoupon.expirationDate != newCoupon.expirationDate) {
            changes.add("Expiration date changed")
        }
        if (oldCoupon.categoryId != newCoupon.categoryId) {
            changes.add("Category changed")
        }
        if (oldCoupon.redeemCode != newCoupon.redeemCode) {
            changes.add("Redeem code changed")
        }
        return if (changes.isEmpty()) "No changes" else changes.joinToString(", ")
    }
}
