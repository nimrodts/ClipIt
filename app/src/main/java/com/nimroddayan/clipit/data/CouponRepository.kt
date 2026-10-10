package com.nimroddayan.clipit.data

import com.nimroddayan.clipit.data.db.CouponDao
import com.nimroddayan.clipit.data.db.CouponHistoryDao
import com.nimroddayan.clipit.data.model.Coupon
import com.nimroddayan.clipit.data.model.CouponHistory
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

    suspend fun findByRedeemCode(redeemCode: String): Coupon? {
        return couponDao.getCouponByRedeemCode(redeemCode)
    }

    /** Returns the new coupon's id. */
    suspend fun insert(coupon: Coupon): Long {
        coupon.redeemCode?.takeIf { it.isNotBlank() }?.let {
            if (couponDao.getCouponByRedeemCode(it) != null) {
                throw DuplicateRedeemCodeException("A coupon with this redeem code already exists.")
            }
        }

        val ids = couponDao.insertAll(coupon)
        val id = ids.first()
        val couponState = Json.encodeToString(coupon.copy(id = id))
        val history =
                CouponHistory(
                        couponId = id,
                        action = "Coupon Created",
                        changeSummary =
                                "Coupon created with initial value of ${coupon.initialValue}",
                        couponState = couponState,
                )
        couponHistoryDao.insert(history)
        return id
    }

    suspend fun update(coupon: Coupon) {
        val oldCoupon = couponDao.getCouponById(coupon.id)
        if (oldCoupon != null) {
            val changeSummary = generateChangeSummary(oldCoupon, coupon)
            val couponState = Json.encodeToString(oldCoupon)
            val history =
                    CouponHistory(
                            couponId = coupon.id,
                            action = "Coupon Edited",
                            changeSummary = changeSummary,
                            couponState = couponState,
                    )
            couponHistoryDao.insert(history)

            // If the value was reduced, add a separate history entry for analytics
            if (oldCoupon.currentValue > coupon.currentValue) {
                val amountSpent = oldCoupon.currentValue - coupon.currentValue
                val useHistory =
                        CouponHistory(
                                couponId = coupon.id,
                                action = "Coupon Used",
                                changeSummary = amountSpent.toString(),
                                couponState = couponState
                        )
                couponHistoryDao.insert(useHistory)
            }
        }
        couponDao.update(coupon)
    }

    suspend fun delete(coupon: Coupon) {
        couponDao.delete(coupon)
    }

    suspend fun archive(coupon: Coupon) {
        val couponState = Json.encodeToString(coupon)
        val history =
                CouponHistory(
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
        val history =
                CouponHistory(
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
        val newBalance = coupon.currentValue - amount
        val isFullyUsed = newBalance <= 0

        val updatedCoupon =
                coupon.copy(
                        currentValue = newBalance,
                        isArchived = if (isFullyUsed) true else coupon.isArchived
                )

        val couponState = Json.encodeToString(coupon)
        val useHistory =
                CouponHistory(
                        couponId = coupon.id,
                        action = "Coupon Used",
                        changeSummary = amount.toString(),
                        couponState = couponState,
                )
        couponHistoryDao.insert(useHistory)

        if (isFullyUsed) {
            val archiveState = Json.encodeToString(updatedCoupon.copy(isArchived = false))
            val archiveHistory =
                    CouponHistory(
                            couponId = coupon.id,
                            action = "Coupon Archived",
                            changeSummary = "Coupon automatically archived after being fully used.",
                            couponState = archiveState,
                    )
            couponHistoryDao.insert(archiveHistory)
        }

        couponDao.update(updatedCoupon)
    }

    /**
     * Puts [snapshot] back exactly as it was and drops the history written since [since], so an
     * immediate Undo (archive, redeem, use) leaves no trace.
     */
    suspend fun restore(snapshot: Coupon, since: Long) {
        couponDao.update(snapshot)
        couponHistoryDao.deleteSince(snapshot.id, since)
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
            operation.couponState?.let { couponState ->
                val coupon = Json.decodeFromString<Coupon>(couponState)
                couponDao.update(coupon)
            }
        }
        couponHistoryDao.delete(operation)
    }

    private fun generateChangeSummary(oldCoupon: Coupon, newCoupon: Coupon): String {
        val changes = mutableListOf<String>()
        if (oldCoupon.name != newCoupon.name) {
            changes.add("Name changed from '${oldCoupon.name}' to '${newCoupon.name}'")
        }
        if (oldCoupon.currentValue != newCoupon.currentValue) {
            changes.add(
                    "Balance changed from ${oldCoupon.currentValue} to ${newCoupon.currentValue}"
            )
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

    val pendingCoupons: Flow<List<Coupon>> = couponDao.getPendingCoupons()

    suspend fun approveCoupon(coupon: Coupon) {
        val approvedCoupon = coupon.copy(isPending = false)
        update(approvedCoupon)
    }

    /**
     * Overwrites a pending coupon with a new AI result. Skips [update]'s history, which would log a
     * changed value as money spent.
     */
    suspend fun replacePending(coupon: Coupon) {
        couponDao.update(coupon)
    }

    suspend fun deletePendingCoupon(coupon: Coupon) {
        // Hard delete for pending coupons that are rejected
        couponDao.delete(coupon)
    }
}
