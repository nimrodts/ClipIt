package com.nimroddayan.clipit.data.gemini

data class ModelOption(val id: String, val label: String, val available: Boolean)

/** Builds the model dropdown entries from the live list (or the built-in list when it's unavailable). */
object ModelOptions {

    fun build(fetched: List<GeminiModelInfo>?, selectedId: String): List<ModelOption> {
        val options = (fetched?.map { ModelOption(it.id, labelFor(it), available = true) }
            ?: GeminiModel.entries.map { ModelOption(it.modelName, it.modelName, available = true) })
            .distinctBy { it.id }
            .sortedBy { it.label.lowercase() }

        if (options.any { it.id == selectedId }) return options
        val selected = if (fetched == null) {
            ModelOption(selectedId, selectedId, available = true)
        } else {
            ModelOption(selectedId, "$selectedId (unavailable)", available = false)
        }
        return listOf(selected) + options
    }


    private fun labelFor(model: GeminiModelInfo): String =
        if (model.displayName == model.id) model.id else "${model.displayName} (${model.id})"
}
