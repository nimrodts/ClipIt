package com.nimroddayan.clipit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nimroddayan.clipit.data.CouponRepository
import com.nimroddayan.clipit.data.DuplicateRedeemCodeException
import com.nimroddayan.clipit.data.UserPreferencesRepository
import com.nimroddayan.clipit.data.ai.CouponAiRetrier
import com.nimroddayan.clipit.data.ai.CouponParser
import com.nimroddayan.clipit.data.gemini.ParsedCoupon
import com.nimroddayan.clipit.data.model.Coupon
import com.nimroddayan.clipit.data.model.SortOption
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CouponViewModel(
        private val couponRepository: CouponRepository,
        private val couponParser: CouponParser,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val couponAiRetrier: CouponAiRetrier,
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

    val currencySymbol: StateFlow<String> =
            userPreferencesRepository
                    .selectedCurrency
                    .map { code ->
                        com.nimroddayan.clipit.data.model.Currency.fromCode(code).symbol
                    }
                    .stateIn(
                            scope = viewModelScope,
                            started = SharingStarted.WhileSubscribed(5000),
                            initialValue = "₪"
                    )

    val allCoupons = couponRepository.allCoupons
    val archivedCoupons = couponRepository.archivedCoupons
    val pendingCoupons = couponRepository.pendingCoupons

    fun approve(coupon: Coupon) {
        viewModelScope.launch { couponRepository.approveCoupon(coupon) }
    }

    fun reject(coupon: Coupon) {
        viewModelScope.launch { couponRepository.deletePendingCoupon(coupon) }
    }

    private val _aiRetryingIds = MutableStateFlow<Set<Long>>(emptySet())
    /** Pending coupons with a "Retry with AI" request in flight. */
    val aiRetryingIds: StateFlow<Set<Long>> = _aiRetryingIds.asStateFlow()

    /** Re-runs the AI on a pending coupon's SMS; a failure shows up as the coupon's new aiError. */
    fun retryAi(coupon: Coupon) {
        if (coupon.id in _aiRetryingIds.value) return
        viewModelScope.launch {
            _aiRetryingIds.update { it + coupon.id }
            try {
                couponAiRetrier.retryNow(coupon.id)
            } finally {
                _aiRetryingIds.update { it - coupon.id }
            }
        }
    }

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

    private val _undoableChanges = MutableSharedFlow<UndoableChange>(extraBufferCapacity = 4)
    /** Changes the Home screen offers to take back from its snackbar. */
    val undoableChanges: SharedFlow<UndoableChange> = _undoableChanges.asSharedFlow()

    fun archiveUndoable(coupon: Coupon) =
            changeUndoable(coupon, "Archived ${coupon.name}") { couponRepository.archive(coupon) }

    fun redeemOneTimeUndoable(coupon: Coupon) =
            changeUndoable(coupon, "Redeemed ${coupon.name}") {
                couponRepository.use(coupon, coupon.currentValue)
                couponRepository.archive(coupon.copy(currentValue = 0.0))
            }

    fun useUndoable(coupon: Coupon, amount: Double, message: String) =
            changeUndoable(coupon, message) { couponRepository.use(coupon, amount) }

    fun undo(change: UndoableChange) {
        viewModelScope.launch { couponRepository.restore(change.snapshot, change.since) }
    }

    private fun changeUndoable(coupon: Coupon, message: String, block: suspend () -> Unit) {
        val since = System.currentTimeMillis()
        viewModelScope.launch {
            block()
            _undoableChanges.emit(UndoableChange(message, coupon, since))
        }
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
                val coupon = couponParser.parse(text)
                _parsedCoupon.value = coupon
                if (coupon.error != null) {
                    _error.value = coupon.error
                }
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

/** A change that can be reverted by writing [snapshot] back and dropping history since [since]. */
data class UndoableChange(val message: String, val snapshot: Coupon, val since: Long)
