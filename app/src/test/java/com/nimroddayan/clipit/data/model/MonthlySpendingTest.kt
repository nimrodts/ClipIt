package com.nimroddayan.clipit.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for [MonthlySpending] data class. */
class MonthlySpendingTest {

    @Test
    fun `properties are set correctly`() {
        val spending = MonthlySpending(month = "2024-01", totalSpent = 150.50)

        assertEquals("2024-01", spending.month)
        assertEquals(150.50, spending.totalSpent, 0.01)
    }

    @Test
    fun `zero spending is valid`() {
        val spending = MonthlySpending(month = "2024-02", totalSpent = 0.0)

        assertEquals(0.0, spending.totalSpent, 0.01)
    }

    @Test
    fun `copy modifies specified fields`() {
        val original = MonthlySpending(month = "2024-01", totalSpent = 100.0)
        val modified = original.copy(totalSpent = 200.0)

        assertEquals("2024-01", modified.month)
        assertEquals(200.0, modified.totalSpent, 0.01)
    }

    @Test
    fun `equality works correctly`() {
        val spending1 = MonthlySpending(month = "2024-03", totalSpent = 500.0)
        val spending2 = MonthlySpending(month = "2024-03", totalSpent = 500.0)

        assertEquals(spending1, spending2)
    }

    @Test
    fun `empty month string is valid`() {
        val spending = MonthlySpending(month = "", totalSpent = 50.0)

        assertEquals("", spending.month)
    }
}
