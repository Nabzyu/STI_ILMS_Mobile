package com.example.stismartlib.ui.dashboard.search

import com.example.stismartlib.data.models.Book
import com.example.stismartlib.data.models.Research

enum class SearchResultKind { Book, Research }

data class SearchPrediction(
    val id: String,
    val kind: SearchResultKind,
    val title: String,
    val subtitle: String,
)

fun buildSearchPredictions(
    query: String,
    books: List<Book>,
    research: List<Research>,
    limit: Int = 8,
): List<SearchPrediction> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isEmpty()) return emptyList()

    val bookHits = books.asSequence()
        .filter { book ->
            "${book.title} ${book.author} ${book.isbn}".contains(normalizedQuery, ignoreCase = true)
        }
        .map { book ->
            SearchPrediction(
                id = book.id,
                kind = SearchResultKind.Book,
                title = book.title,
                subtitle = book.author.ifBlank { "Book" },
            )
        }

    val researchHits = research.asSequence()
        .filter { item ->
            "${item.title} ${item.author} ${item.category}".contains(normalizedQuery, ignoreCase = true)
        }
        .map { item ->
            SearchPrediction(
                id = item.id,
                kind = SearchResultKind.Research,
                title = item.title,
                subtitle = item.author.ifBlank { "Research" },
            )
        }

    return (bookHits + researchHits).take(limit).toList()
}
