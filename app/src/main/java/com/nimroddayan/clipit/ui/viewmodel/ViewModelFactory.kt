package com.nimroddayan.clipit.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nimroddayan.clipit.data.CouponRepository
import com.nimroddayan.clipit.data.UserPreferencesRepository
import com.nimroddayan.clipit.data.gemini.GeminiApiKeyRepository
import com.nimroddayan.clipit.data.ai.CouponParser

class ViewModelFactory(
        private val context: Context,
        private val couponRepository: CouponRepository,
        private val geminiApiKeyRepository: GeminiApiKeyRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CouponViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CouponViewModel(
                    couponRepository,
                    CouponParser.create(context),
                    userPreferencesRepository
            ) as
                    T
        }
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(geminiApiKeyRepository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
