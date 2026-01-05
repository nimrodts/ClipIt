package com.nimroddayan.couponmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nimroddayan.couponmanager.data.CouponRepository
import com.nimroddayan.couponmanager.data.db.CouponHistoryDao

class HistoryViewModelFactory(
    private val couponHistoryDao: CouponHistoryDao,
    private val couponRepository: CouponRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(couponHistoryDao, couponRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
