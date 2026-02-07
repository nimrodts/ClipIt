package com.nimroddayan.clipit.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for [Category] data class. */
class CategoryTest {

    @Test
    fun `default id is 0`() {
        val category = Category(name = "Test", colorHex = "#FF0000", iconName = "Grocery")

        assertEquals(0L, category.id)
    }

    @Test
    fun `properties are set correctly`() {
        val category =
                Category(id = 5L, name = "Food", colorHex = "#00FF00", iconName = "Restaurant")

        assertEquals(5L, category.id)
        assertEquals("Food", category.name)
        assertEquals("#00FF00", category.colorHex)
        assertEquals("Restaurant", category.iconName)
    }

    @Test
    fun `copy modifies specified fields`() {
        val original =
                Category(id = 1L, name = "Original", colorHex = "#111111", iconName = "Grocery")

        val modified = original.copy(name = "Modified", colorHex = "#222222")

        assertEquals(1L, modified.id)
        assertEquals("Modified", modified.name)
        assertEquals("#222222", modified.colorHex)
        assertEquals("Grocery", modified.iconName)
    }

    @Test
    fun `categories with same data are equal`() {
        val cat1 = Category(id = 1L, name = "Test", colorHex = "#FFF", iconName = "Icon")
        val cat2 = Category(id = 1L, name = "Test", colorHex = "#FFF", iconName = "Icon")

        assertEquals(cat1, cat2)
    }
}
