package com.example.stismartlib

import com.example.stismartlib.ui.dashboard.components.CatalogViewMode
import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogViewModeTest {
    @Test
    fun fromStorage_list() {
        assertEquals(CatalogViewMode.LIST, CatalogViewMode.fromStorage("list"))
    }

    @Test
    fun fromStorage_grid2() {
        assertEquals(CatalogViewMode.GRID_2, CatalogViewMode.fromStorage("grid_2"))
    }

    @Test
    fun fromStorage_nullOrBlankOrGarbage_defaultsToList() {
        assertEquals(CatalogViewMode.LIST, CatalogViewMode.fromStorage(null))
        assertEquals(CatalogViewMode.LIST, CatalogViewMode.fromStorage(""))
        assertEquals(CatalogViewMode.LIST, CatalogViewMode.fromStorage("grid_3"))
    }

    @Test
    fun toStorage_roundTrip() {
        assertEquals("list", CatalogViewMode.LIST.toStorage())
        assertEquals("grid_2", CatalogViewMode.GRID_2.toStorage())
        assertEquals(
            CatalogViewMode.GRID_2,
            CatalogViewMode.fromStorage(CatalogViewMode.GRID_2.toStorage())
        )
    }
}
