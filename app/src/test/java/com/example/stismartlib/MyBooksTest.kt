package com.example.stismartlib

import com.example.stismartlib.data.filterMyBooks
import com.example.stismartlib.data.myBooksCollection
import com.example.stismartlib.network.dto.BorrowingItemDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MyBooksTest {
    private fun row(titleId: Int?, title: String, borrow: String?, status: String) =
        BorrowingItemDto(titleId = titleId, title = title, author = "A", borrowDate = borrow, status = status)

    @Test
    fun oneEntryPerTitleWithLatestBorrow() {
        val books = myBooksCollection(listOf(
            row(7, "Clean Code", "2026-09-01T02:00:00.000Z", "Returned"),
            row(7, "Clean Code", "2026-10-05T02:00:00.000Z", "Borrowed"),
        ))
        assertEquals(1, books.size)
        assertEquals("Borrowed", books[0].status)
        assertEquals("2026-10-05", books[0].borrowedOn)
    }

    @Test
    fun skipsPendingCancelledAndUnborrowed() {
        assertTrue(myBooksCollection(listOf(
            row(1, "X", null, "Borrowed"),
            row(2, "Y", "2026-10-01", "Pending"),
            row(3, "Z", "2026-10-01", "Cancelled"),
        )).isEmpty())
    }

    @Test
    fun missingTitleIdKeysByTitle() {
        assertEquals(2, myBooksCollection(listOf(row(null, "A", "2026-10-01", "Lost"), row(null, "B", "2026-10-02", "Returned"))).size)
    }

    @Test
    fun newestFirstAndSearch() {
        val books = myBooksCollection(listOf(row(1, "Algorithms", "2026-10-01", "Returned"), row(2, "Databases", "2026-10-03", "Borrowed")))
        assertEquals(listOf("Databases", "Algorithms"), books.map { it.title })
        assertEquals(listOf("Algorithms"), filterMyBooks(books, "ALGO").map { it.title })
        assertEquals(2, filterMyBooks(books, " ").size)
    }
}
