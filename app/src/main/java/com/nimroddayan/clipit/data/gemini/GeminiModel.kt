package com.nimroddayan.clipit.data.gemini

/** Built-in model list, used when the live list can't be fetched and to read old saved settings. */
enum class GeminiModel(val modelName: String) {
    GEMINI_2_5_FLASH("gemini-2.5-flash"),
    GEMINI_2_5_PRO("gemini-2.5-pro"),
    GEMINI_FLASH_LATEST("gemini-flash-latest"),
    GEMINI_PRO_LATEST("gemini-pro-latest"),
    GEMINI_3_PRO_PREVIEW("gemini-3-pro-preview"),
    GEMINI_3_FLASH_PREVIEW("gemini-3-flash-preview");

    companion object {
        const val DEFAULT_MODEL_ID = "gemini-flash-latest"

        /** Older versions stored the enum name (e.g. GEMINI_FLASH_LATEST); newer ones store the model id. */
        fun toModelId(stored: String?): String {
            if (stored.isNullOrBlank()) return DEFAULT_MODEL_ID
            return entries.firstOrNull { it.name == stored }?.modelName ?: stored
        }
    }
}
