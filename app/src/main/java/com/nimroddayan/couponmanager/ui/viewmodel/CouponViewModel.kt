package com.nimroddayan.couponmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nimroddayan.couponmanager.data.CouponRepository
import com.nimroddayan.couponmanager.data.DuplicateRedeemCodeException
import com.nimroddayan.couponmanager.data.UserPreferencesRepository
import com.nimroddayan.couponmanager.data.gemini.GeminiCouponExtractor
import com.nimroddayan.couponmanager.data.gemini.ParsedCoupon
import com.nimroddayan.couponmanager.data.model.Coupon
import com.nimroddayan.couponmanager.data.model.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CouponViewModel(
        private val couponRepository: CouponRepository,
        private val geminiCouponExtractor: GeminiCouponExtractor,
        private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val sortOption: StateFlow<SortOption> =
            userPreferencesRepository
                    .sortOption
                    .map { optionName ->
                        try {
                            if (optionName != null) SortOption.valueOf(optionName)
                            else SortOption.DateAddedDesc
                        } catch (e: IllegalArgumentException) {
                            SortOption.DateAddedDesc
                        }
                    }
                    .stateIn(
                            viewModelScope,
                            SharingStarted.WhileSubscribed(5000),
                            SortOption.DateAddedDesc
                    )

    fun saveSortOption(option: SortOption) {
        viewModelScope.launch { userPreferencesRepository.saveSortOption(option.name) }
    }

    val allCoupons = couponRepository.allCoupons
    val archivedCoupons = couponRepository.archivedCoupons

    private val _parsedCoupon = MutableStateFlow<ParsedCoupon?>(null)
    val parsedCoupon: StateFlow<ParsedCoupon?> = _parsedCoupon.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun insert(coupon: Coupon, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                couponRepository.insert(coupon)
                onSuccess()
            } catch (e: DuplicateRedeemCodeException) {
                _error.value = e.message
            } catch (e: Exception) {
                _error.value = "An unexpected error occurred."
            }
        }
    }

    fun update(coupon: Coupon) {
        viewModelScope.launch { couponRepository.update(coupon) }
    }

    fun delete(coupon: Coupon) {
        viewModelScope.launch { couponRepository.delete(coupon) }
    }

    fun archive(coupon: Coupon) {
        viewModelScope.launch { couponRepository.archive(coupon) }
    }

    fun unarchive(coupon: Coupon) {
        viewModelScope.launch { couponRepository.unarchive(coupon) }
    }

    fun use(coupon: Coupon, amount: Double) {
        viewModelScope.launch { couponRepository.use(coupon, amount) }
    }

    fun redeemOneTime(coupon: Coupon) {
        viewModelScope.launch {
            couponRepository.use(coupon, coupon.currentValue)
            couponRepository.archive(coupon)
        }
    }

    fun autofillFromClipboard(text: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _parsedCoupon.value = geminiCouponExtractor.extractCoupon(text)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearParsedCoupon() {
        _parsedCoupon.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
