package com.nimroddayan.clipit.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [CouponHistory] data class. */
class CouponHistoryTest {

    @Test
    fun `default values are set correctly`() {
        val history =
                CouponHistory(couponId = 1L, action = "Test Action", changeSummary = "Test Summary")

        assertEquals(0L, history.id)
        assertTrue(history.timestamp > 0) // Should be set to current time
        assertEquals(null, history.couponState)
    }

    @Test
    fun `couponId is set correctly`() {
        val history = CouponHistory(couponId = 42L, action = "Action", changeSummary = "Summary")

        assertEquals(42L, history.couponId)
    }

    @Test
    fun `optional couponState can be set`() {
        val stateJson = """{"name":"Test","value":100}"""
        val history =
                CouponHistory(
                        couponId = 1L,
                        action = "Update",
                        changeSummary = "Changed value",
                        couponState = stateJson
                )

        assertEquals(stateJson, history.couponState)
    }

    @Test
    fun `custom timestamp can be set`() {
        val customTime = 1234567890L
        val history =
                CouponHistory(
                        couponId = 1L,
                        action = "Action",
                        changeSummary = "Summary",
                        timestamp = customTime
                )

        assertEquals(customTime, history.timestamp)
    }

    @Test
    fun `copy preserves unmodified fields`() {
        val original =
                CouponHistory(
                        couponId = 5L,
                        action = "Original Action",
                        changeSummary = "Original Summary",
                        timestamp = 1000L
                )

        val modified = original.copy(action = "Modified Action")

        assertEquals(5L, modified.couponId)
        assertEquals("Modified Action", modified.action)
        assertEquals("Original Summary", modified.changeSummary)
        assertEquals(1000L, modified.timestamp)
    }
}
