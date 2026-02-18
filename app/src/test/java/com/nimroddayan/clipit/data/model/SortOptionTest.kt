package com.nimroddayan.clipit.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** Unit tests for [SortOption] enum. */
class SortOptionTest {

    @Test
    fun `valueOf returns correct enum for valid name`() {
        assertEquals(SortOption.NameAsc, SortOption.valueOf("NameAsc"))
        assertEquals(SortOption.NameDesc, SortOption.valueOf("NameDesc"))
        assertEquals(SortOption.DateAsc, SortOption.valueOf("DateAsc"))
        assertEquals(SortOption.DateDesc, SortOption.valueOf("DateDesc"))
        assertEquals(SortOption.DateAddedAsc, SortOption.valueOf("DateAddedAsc"))
        assertEquals(SortOption.DateAddedDesc, SortOption.valueOf("DateAddedDesc"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `valueOf throws for invalid name`() {
        SortOption.valueOf("InvalidOption")
    }

    @Test
    fun `entries contains all 6 options`() {
        assertEquals(6, SortOption.entries.size)
    }

    @Test
    fun `name property returns correct string`() {
        assertEquals("NameAsc", SortOption.NameAsc.name)
        assertEquals("DateAddedDesc", SortOption.DateAddedDesc.name)
    }

    @Test
    fun `ordinal values are sequential`() {
        assertEquals(0, SortOption.NameAsc.ordinal)
        assertEquals(1, SortOption.NameDesc.ordinal)
        assertEquals(2, SortOption.DateAsc.ordinal)
        assertEquals(3, SortOption.DateDesc.ordinal)
        assertEquals(4, SortOption.DateAddedAsc.ordinal)
        assertEquals(5, SortOption.DateAddedDesc.ordinal)
    }

    @Test
    fun `different options are not equal`() {
        assertNotEquals(SortOption.NameAsc, SortOption.NameDesc)
        assertNotEquals(SortOption.DateAsc, SortOption.DateDesc)
    }
}
