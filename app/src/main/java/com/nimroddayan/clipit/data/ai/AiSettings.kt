package com.nimroddayan.clipit.data.ai

import com.nimroddayan.clipit.data.gemini.DetectionMode

/** Snapshot of the AI settings, read once per detection or extraction. */
data class AiSettings(
    val apiKey: String,
    val modelId: String,
    val temperature: Float,
    val detectionMode: DetectionMode,
)
