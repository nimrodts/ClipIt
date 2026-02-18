package com.nimroddayan.clipit.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for [CategorySpending] data class. */
class CategorySpendingTest {

    @Test
    fun `properties are set correctly`() {
        val spending = CategorySpending(name = "Food", colorHex = "#FF5733", totalSpent = 250.75)

        assertEquals("Food", spending.name)
        assertEquals("#FF5733", spending.colorHex)
        assertEquals(250.75, spending.totalSpent, 0.01)
    }

    @Test
    fun `uncategorized spending is valid`() {
        val spending =
                CategorySpending(name = "Uncategorized", colorHex = "#808080", totalSpent = 100.0)

        assertEquals("Uncategorized", spending.name)
    }

    @Test
    fun `zero spending is valid`() {
        val spending = CategorySpending(name = "Empty", colorHex = "#000000", totalSpent = 0.0)

        assertEquals(0.0, spending.totalSpent, 0.01)
    }

    @Test
    fun `copy modifies specified fields`() {
        val original = CategorySpending(name = "A", colorHex = "#FFF", totalSpent = 10.0)
        val modified = original.copy(name = "B", totalSpent = 20.0)

        assertEquals("B", modified.name)
        assertEquals("#FFF", modified.colorHex)
        assertEquals(20.0, modified.totalSpent, 0.01)
    }

    @Test
    fun `equality works correctly`() {
        val spending1 = CategorySpending(name = "X", colorHex = "#123", totalSpent = 50.0)
        val spending2 = CategorySpending(name = "X", colorHex = "#123", totalSpent = 50.0)

        assertEquals(spending1, spending2)
    }
}
