package com.example.stismartlib

import com.example.stismartlib.data.PageCursor
import com.example.stismartlib.data.isLoadedFor
import com.example.stismartlib.data.orNullOnFailure
import com.example.stismartlib.data.shouldOfferLoadMore
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewFixesTest {
    @Test
    fun returningToTheSameSearchKeepsLoadedPages() {
        val cursor = PageCursor(page = 3, totalPages = 5, query = "java")
        assertTrue(cursor.isLoadedFor("java"))
        assertFalse(cursor.isLoadedFor(null))
        assertFalse(PageCursor().isLoadedFor(null))
    }

    @Test
    fun loadMoreStaysOfferedWhenFiltersHidePageOne() {
        assertTrue(shouldOfferLoadMore(loadedCount = 50, cursor = PageCursor(1, 3)))
        assertFalse(shouldOfferLoadMore(loadedCount = 0, cursor = PageCursor(1, 3)))
        assertFalse(shouldOfferLoadMore(loadedCount = 50, cursor = PageCursor(3, 3)))
    }

    @Test
    fun malformedCartStateReplyDoesNotCrash() = runBlocking {
        assertNull(orNullOnFailure<List<Long>> { throw JsonSyntaxException("bad body") })
        assertEquals(listOf(7L), orNullOnFailure { listOf(7L) })
    }

    @Test(expected = CancellationException::class)
    fun cancellationStillPropagates(): Unit = runBlocking {
        orNullOnFailure<List<Long>> { throw CancellationException("left screen") }
        Unit
    }
}
