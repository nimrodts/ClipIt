package com.nimroddayan.couponmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nimroddayan.couponmanager.data.gemini.GeminiApiKeyRepository
import com.nimroddayan.couponmanager.data.gemini.GeminiModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val geminiApiKeyRepository: GeminiApiKeyRepository) : ViewModel() {

    val geminiApiKey: StateFlow<String> = geminiApiKeyRepository.getApiKey
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val geminiModel: StateFlow<GeminiModel> = geminiApiKeyRepository.getModel
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GeminiModel.GEMINI_FLASH_LATEST
        )

    val geminiTemperature: StateFlow<Float> = geminiApiKeyRepository.getTemperature
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.15f
        )

    fun saveGeminiApiKey(apiKey: String) {
        viewModelScope.launch {
            geminiApiKeyRepository.saveApiKey(apiKey)
        }
    }

    fun saveGeminiModel(model: GeminiModel) {
        viewModelScope.launch {
            geminiApiKeyRepository.saveModel(model)
        }
    }

    fun saveGeminiTemperature(temperature: Float) {
        viewModelScope.launch {
            geminiApiKeyRepository.saveTemperature(temperature)
        }
    }
}
