package com.example.stismartlib.data

import com.example.stismartlib.network.dto.BorrowingItemDto

/** A title in the user's borrowed collection, with its most recent loan. Dates are yyyy-MM-dd. */
data class MyBook(
    val key: String,
    val title: String,
    val author: String,
    val status: String,
    val borrowedOn: String,
    val dueOn: String?,
    val returnedOn: String?,
)

private val CollectionStatuses = setOf("Borrowed", "Overdue", "Returned", "Lost")

/** Same rules as the web's My books: borrowed titles only, one per title, newest first. */
fun myBooksCollection(items: List<BorrowingItemDto>): List<MyBook> =
    items.asSequence()
        .filter { !it.borrowDate.isNullOrBlank() && it.status in CollectionStatuses }
        .groupBy { it.titleId?.toString() ?: "title:${it.title.orEmpty()}" }
        .map { (key, rows) ->
            // ISO timestamps sort correctly as text.
            val latest = rows.maxBy { it.borrowDate.orEmpty() }
            MyBook(
                key = key,
                title = latest.title.orEmpty(),
                author = latest.author.orEmpty(),
                status = latest.status.orEmpty(),
                borrowedOn = latest.borrowDate.orEmpty().take(10),
                dueOn = latest.dueDate?.take(10),
                returnedOn = latest.returnDate?.take(10),
            )
        }
        .sortedByDescending { it.borrowedOn }

fun filterMyBooks(books: List<MyBook>, query: String): List<MyBook> {
    val needle = query.trim()
    if (needle.isEmpty()) return books
    return books.filter { "${it.title} ${it.author}".contains(needle, ignoreCase = true) }
}
