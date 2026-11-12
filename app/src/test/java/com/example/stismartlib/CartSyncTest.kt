package com.example.stismartlib

import com.example.stismartlib.data.cartAfterSync
import org.junit.Assert.assertEquals
import org.junit.Test

class CartSyncTest {
    @Test
    fun removesTitlesAlreadyActive() = assertEquals(setOf("7"), cartAfterSync(listOf("7", "9"), listOf(7L, 12L)))

    @Test
    fun keepsCartWhenCheckUnavailable() = assertEquals(emptySet<String>(), cartAfterSync(listOf("7"), null))

    @Test
    fun ignoresNonNumericIds() = assertEquals(emptySet<String>(), cartAfterSync(listOf("preview-1"), listOf(1L)))
}
