package com.nimroddayan.clipit.data.gemini

import org.junit.Assert.assertEquals
import org.junit.Test

class DetectionModeTest {

    @Test
    fun `missing or removed modes fall back to local heuristic`() {
        assertEquals(DetectionMode.LOCAL_HEURISTIC, DetectionMode.fromStored(null))
        assertEquals(DetectionMode.LOCAL_HEURISTIC, DetectionMode.fromStored("ONLINE_GEMINI"))
        assertEquals(DetectionMode.LOCAL_HEURISTIC, DetectionMode.fromStored("garbage"))
    }

    @Test
    fun `known modes round trip`() {
        DetectionMode.entries.forEach { assertEquals(it, DetectionMode.fromStored(it.name)) }
    }
}
