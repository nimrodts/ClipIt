package com.nimroddayan.couponmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nimroddayan.couponmanager.data.db.CouponDao
import com.nimroddayan.couponmanager.data.model.Coupon
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CouponViewModel(private val couponDao: CouponDao) : ViewModel() {
    val allCoupons: StateFlow<List<Coupon>> = couponDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedCoupons: StateFlow<List<Coupon>> = couponDao.getArchived()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insert(coupon: Coupon) = viewModelScope.launch {
        couponDao.insertAll(coupon)
    }

    fun delete(coupon: Coupon) = viewModelScope.launch {
        couponDao.delete(coupon)
    }

    fun clearAll() = viewModelScope.launch {
        couponDao.clearAll()
    }

    fun update(coupon: Coupon) = viewModelScope.launch {
        couponDao.update(coupon)
    }
}
