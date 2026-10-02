package com.nimroddayan.clipit.data.gemini

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nimroddayan.clipit.data.ai.AiSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class GeminiApiKeyRepository(private val context: Context) {
    private val geminiApiKey = stringPreferencesKey("gemini_api_key")
    private val geminiModel = stringPreferencesKey("gemini_model")
    private val geminiTemperature = floatPreferencesKey("gemini_temperature")
    private val detectionMode = stringPreferencesKey("detection_mode")

    val getApiKey: Flow<String> =
            context.dataStore.data.map { preferences -> preferences[geminiApiKey] ?: "" }

    val getModel: Flow<String> =
            context.dataStore.data.map { preferences -> GeminiModel.toModelId(preferences[geminiModel]) }

    val getTemperature: Flow<Float> =
            context.dataStore.data.map { preferences ->
                preferences[geminiTemperature] ?: DEFAULT_TEMPERATURE
            }

    val getDetectionMode: Flow<DetectionMode> =
            context.dataStore.data.map { preferences -> DetectionMode.fromStored(preferences[detectionMode]) }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { settings -> settings[geminiApiKey] = apiKey }
    }

    suspend fun saveModel(modelId: String) {
        context.dataStore.edit { settings -> settings[geminiModel] = modelId }
    }

    suspend fun saveTemperature(temperature: Float) {
        context.dataStore.edit { settings -> settings[geminiTemperature] = temperature }
    }

    suspend fun saveDetectionMode(mode: DetectionMode) {
        context.dataStore.edit { settings -> settings[detectionMode] = mode.name }
    }

    suspend fun currentSettings(): AiSettings {
        val preferences = context.dataStore.data.first()
        return AiSettings(
                apiKey = preferences[geminiApiKey] ?: "",
                modelId = GeminiModel.toModelId(preferences[geminiModel]),
                temperature = preferences[geminiTemperature] ?: DEFAULT_TEMPERATURE,
                detectionMode = DetectionMode.fromStored(preferences[detectionMode]),
        )
    }

    companion object {
        const val DEFAULT_TEMPERATURE = 0.15f
    }
}

enum class DetectionMode(val displayName: String) {
    LOCAL_HEURISTIC("On-device rules (recommended)"),
    REGEX_ONLY("Offline only, no AI"),
    ALWAYS_EXTRACT("Always extract (sends every SMS to AI)");

    companion object {
        fun fromStored(name: String?): DetectionMode =
                entries.firstOrNull { it.name == name } ?: LOCAL_HEURISTIC
    }
}
