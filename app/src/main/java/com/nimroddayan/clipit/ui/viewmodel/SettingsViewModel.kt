package com.nimroddayan.clipit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nimroddayan.clipit.data.UserPreferencesRepository
import com.nimroddayan.clipit.data.gemini.DetectionMode
import com.nimroddayan.clipit.data.gemini.GeminiApiKeyRepository
import com.nimroddayan.clipit.data.gemini.GeminiModel
import com.nimroddayan.clipit.data.gemini.GeminiRestClient
import com.nimroddayan.clipit.data.gemini.ModelOption
import com.nimroddayan.clipit.data.gemini.ModelOptions
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
        private val geminiApiKeyRepository: GeminiApiKeyRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val geminiRestClient: GeminiRestClient
) : ViewModel() {

        private val _modelListState = MutableStateFlow(ModelListState())
        val modelListState: StateFlow<ModelListState> = _modelListState.asStateFlow()

        /** Loads the models the saved key can use; falls back to the built-in list on any failure. */
        fun refreshModels() {
                viewModelScope.launch {
                        _modelListState.update { it.copy(isLoading = true, error = null) }
                        val settings = geminiApiKeyRepository.currentSettings()
                        var error: String? = null
                        val fetched =
                                if (settings.apiKey.isBlank()) {
                                        null
                                } else {
                                        try {
                                                geminiRestClient.listModels(settings.apiKey)
                                        } catch (e: CancellationException) {
                                                throw e
                                        } catch (e: Exception) {
                                                error = e.message ?: "Couldn't load models"
                                                null
                                        }
                                }
                        _modelListState.value =
                                ModelListState(
                                        options = ModelOptions.build(fetched, settings.modelId),
                                        isLoading = false,
                                        error = error
                                )
                }
        }

        val selectedCurrency: StateFlow<String> =
                userPreferencesRepository.selectedCurrency.stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = "ILS"
                )

        val geminiApiKey: StateFlow<String> =
                geminiApiKeyRepository.getApiKey.stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = ""
                )

        val geminiModel: StateFlow<String> =
                geminiApiKeyRepository.getModel.stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = GeminiModel.DEFAULT_MODEL_ID
                )

        val geminiTemperature: StateFlow<Float> =
                geminiApiKeyRepository.getTemperature.stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = GeminiApiKeyRepository.DEFAULT_TEMPERATURE
                )

        suspend fun saveGeminiApiKey(apiKey: String) {
                geminiApiKeyRepository.saveApiKey(apiKey)
        }

        suspend fun saveGeminiModel(modelId: String) {
                geminiApiKeyRepository.saveModel(modelId)
        }

        suspend fun saveGeminiTemperature(temperature: Float) {
                geminiApiKeyRepository.saveTemperature(temperature)
        }

        val detectionMode: StateFlow<DetectionMode> =
                geminiApiKeyRepository.getDetectionMode.stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = DetectionMode.LOCAL_HEURISTIC
                )

        suspend fun saveDetectionMode(mode: DetectionMode) {
                geminiApiKeyRepository.saveDetectionMode(mode)
        }

        fun saveSelectedCurrency(currencyCode: String) {
                viewModelScope.launch {
                        userPreferencesRepository.saveSelectedCurrency(currencyCode)
                }
        }

        val smsSenderWhitelist: StateFlow<Set<String>> =
                userPreferencesRepository.smsSenderWhitelist.stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = emptySet()
                )

        fun addSenderToWhitelist(sender: String) {
                viewModelScope.launch { userPreferencesRepository.addSenderToWhitelist(sender) }
        }

        fun removeSenderFromWhitelist(sender: String) {
                viewModelScope.launch {
                        userPreferencesRepository.removeSenderFromWhitelist(sender)
                }
        }
}

data class ModelListState(
        val options: List<ModelOption> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
)
