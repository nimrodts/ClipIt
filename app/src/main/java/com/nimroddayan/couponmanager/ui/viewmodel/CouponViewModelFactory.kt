package com.nimroddayan.couponmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nimroddayan.couponmanager.data.db.CouponDao

class CouponViewModelFactory(private val couponDao: CouponDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CouponViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CouponViewModel(couponDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
