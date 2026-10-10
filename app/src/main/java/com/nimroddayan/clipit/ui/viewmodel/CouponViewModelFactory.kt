package com.nimroddayan.clipit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nimroddayan.clipit.data.CouponRepository
import com.nimroddayan.clipit.data.UserPreferencesRepository
import com.nimroddayan.clipit.data.ai.CouponAiRetrier
import com.nimroddayan.clipit.data.ai.CouponParser

class CouponViewModelFactory(
        private val couponRepository: CouponRepository,
        private val couponParser: CouponParser,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val couponAiRetrier: CouponAiRetrier
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CouponViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CouponViewModel(
                    couponRepository,
                    couponParser,
                    userPreferencesRepository,
                    couponAiRetrier
            ) as
                    T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


