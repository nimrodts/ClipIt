package com.nimroddayan.couponmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nimroddayan.couponmanager.data.db.CouponDao
import com.nimroddayan.couponmanager.data.model.CategorySpending
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(couponDao: CouponDao) : ViewModel() {
    val totalBalance: StateFlow<Double> = couponDao.getTotalBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val totalSpent: StateFlow<Double> = couponDao.getTotalSpent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val spendingByCategory: StateFlow<List<CategorySpending>> = couponDao.getSpendingByCategory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
}
