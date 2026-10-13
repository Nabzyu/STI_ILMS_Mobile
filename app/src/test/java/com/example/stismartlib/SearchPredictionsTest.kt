package com.example.stismartlib

import com.example.stismartlib.data.models.Book
import com.example.stismartlib.data.models.Research
import com.example.stismartlib.ui.dashboard.search.SearchResultKind
import com.example.stismartlib.ui.dashboard.search.buildSearchPredictions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchPredictionsTest {
    private val sampleBook = Book(
        id = "1",
        title = "Introduction to Algorithms",
        author = "Cormen",
        isbn = "9780262033848",
        category = "Computing",
        yearPublished = 2009,
        publisher = "MIT Press",
        shelfLocation = "QA76",
        availableCopies = 2,
        totalCopies = 4,
        description = "",
    )

    private val sampleResearch = Research(
        id = "9",
        title = "Campus Library Usage Patterns",
        author = "Dela Cruz",
        category = "Information Science",
        yearPublished = 2024,
        abstract = "",
        shelfLocation = "RES-1",
    )

    @Test
    fun blankQueryReturnsEmpty() {
        val result = buildSearchPredictions(
            query = "   ",
            books = listOf(sampleBook),
            research = listOf(sampleResearch),
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun booksAppearBeforeResearch() {
        val result = buildSearchPredictions(
            query = "library",
            books = listOf(sampleBook.copy(title = "Library Handbook")),
            research = listOf(sampleResearch),
        )
        assertEquals(SearchResultKind.Book, result.first().kind)
        assertEquals(SearchResultKind.Research, result.last().kind)
    }

    @Test
    fun respectsLimit() {
        val books = (1..10).map { index ->
            sampleBook.copy(id = index.toString(), title = "Algorithms Vol $index")
        }
        val result = buildSearchPredictions(
            query = "Algorithms",
            books = books,
            research = emptyList(),
            limit = 8,
        )
        assertEquals(8, result.size)
    }
}
