package com.nimroddayan.couponmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nimroddayan.couponmanager.data.CouponRepository
import com.nimroddayan.couponmanager.data.UserPreferencesRepository
import com.nimroddayan.couponmanager.data.gemini.GeminiCouponExtractor

class CouponViewModelFactory(
        private val couponRepository: CouponRepository,
        private val geminiCouponExtractor: GeminiCouponExtractor,
        private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CouponViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CouponViewModel(
                    couponRepository,
                    geminiCouponExtractor,
                    userPreferencesRepository
            ) as
                    T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
