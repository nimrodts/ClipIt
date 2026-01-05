package com.nimroddayan.couponmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nimroddayan.couponmanager.data.CouponRepository
import com.nimroddayan.couponmanager.data.db.CouponHistoryDao
import com.nimroddayan.couponmanager.data.model.Coupon
import com.nimroddayan.couponmanager.data.model.CouponHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val couponHistoryDao: CouponHistoryDao,
    private val couponRepository: CouponRepository,
) : ViewModel() {
    private val couponId = MutableStateFlow(-1L)

    val history: StateFlow<List<CouponHistory>> =
        couponId.flatMapLatest { couponId ->
            couponHistoryDao.getHistoryForCoupon(couponId)
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCouponId(couponId: Long) {
        this.couponId.value = couponId
    }

    fun undo(operation: CouponHistory) {
        viewModelScope.launch {
            couponRepository.undo(operation)
        }
    }

    suspend fun getCoupon(couponId: Long): Coupon? {
        return couponRepository.getCouponById(couponId)
    }
}
