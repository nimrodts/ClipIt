package com.nimroddayan.clipit.data.gemini

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelOptionsTest {

    private val fetched = listOf(
        GeminiModelInfo("gemini-2.5-pro", "Gemini 2.5 Pro", null),
        GeminiModelInfo("gemini-2.5-flash", "Gemini 2.5 Flash", null),
        GeminiModelInfo("gemini-2.5-flash", "Gemini 2.5 Flash", null),
    )

    @Test
    fun `fetched models are deduplicated, sorted and labelled with id`() {
        val options = ModelOptions.build(fetched, "gemini-2.5-flash")
        assertEquals(listOf("gemini-2.5-flash", "gemini-2.5-pro"), options.map { it.id })
        assertEquals("Gemini 2.5 Flash (gemini-2.5-flash)", options[0].label)
        assertTrue(options.all { it.available })
    }

    @Test
    fun `selected model missing from fetched list is kept and marked unavailable`() {
        val options = ModelOptions.build(fetched, "gemini-1.5-flash")
        assertEquals("gemini-1.5-flash", options[0].id)
        assertEquals("gemini-1.5-flash (unavailable)", options[0].label)
        assertFalse(options[0].available)
    }

    @Test
    fun `without fetched list the built-in models are used`() {
        val options = ModelOptions.build(null, GeminiModel.DEFAULT_MODEL_ID)
        assertEquals(GeminiModel.entries.size, options.size)
        assertTrue(options.any { it.id == "gemini-flash-latest" })
    }

    @Test
    fun `without fetched list a custom selected model is kept as available`() {
        val options = ModelOptions.build(null, "gemini-9-ultra")
        assertEquals("gemini-9-ultra", options[0].id)
        assertTrue(options[0].available)
    }
}
