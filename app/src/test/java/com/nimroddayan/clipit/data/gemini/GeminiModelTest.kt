package com.nimroddayan.clipit.data.gemini

import org.junit.Assert.assertEquals
import org.junit.Test

class GeminiModelTest {

    @Test
    fun `missing value uses default model id`() {
        assertEquals("gemini-flash-latest", GeminiModel.toModelId(null))
        assertEquals("gemini-flash-latest", GeminiModel.toModelId(""))
    }

    @Test
    fun `legacy enum name converts to model id`() {
        assertEquals("gemini-flash-latest", GeminiModel.toModelId("GEMINI_FLASH_LATEST"))
        assertEquals("gemini-2.5-pro", GeminiModel.toModelId("GEMINI_2_5_PRO"))
    }

    @Test
    fun `model ids pass through unchanged, including ones not in the enum`() {
        assertEquals("gemini-2.5-flash", GeminiModel.toModelId("gemini-2.5-flash"))
        assertEquals("gemini-9-ultra", GeminiModel.toModelId("gemini-9-ultra"))
    }
}
