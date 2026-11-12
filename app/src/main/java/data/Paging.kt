package com.example.stismartlib.data

/** Where a paged list stands; `page = 0` means nothing is loaded yet. */
data class PageCursor(
    val page: Int = 0,
    val totalPages: Int = 0,
    val query: String? = null,
    val categoryId: Int? = null,
) {
    val hasMore: Boolean get() = page in 1 until totalPages

    /** The next page of the same query. */
    fun next(): PageCursor = copy(page = page + 1)
}

/** The items of [next] not already in [existing] (and not repeated within [next]). */
fun <T, K> newItems(existing: List<T>, next: List<T>, key: (T) -> K): List<T> {
    val seen = existing.mapTo(HashSet(), key)
    return next.filter { seen.add(key(it)) }
}

/**
 * Fetches pages 1..[upTo] into one list, stopping early at the server's last page.
 * [fetch] returns a page's items and the server's total page count.
 */
suspend fun <T, K> fetchPages(
    upTo: Int,
    key: (T) -> K,
    fetch: suspend (page: Int) -> Pair<List<T>, Int>,
): Pair<List<T>, PageCursor> {
    val rows = mutableListOf<T>()
    var cursor = PageCursor()
    do {
        val page = cursor.page + 1
        val (items, totalPages) = fetch(page)
        rows += newItems(rows, items, key)
        cursor = PageCursor(page, totalPages)
    } while (cursor.page < upTo && cursor.hasMore)
    return rows to cursor
}

/** True when this cursor already holds results for [query] and [categoryId], so a screen can skip reloading page 1. */
fun PageCursor.isLoadedFor(query: String?, categoryId: Int? = null): Boolean =
    page > 0 && this.query == query && this.categoryId == categoryId

/** Offer "Load more" while more pages exist, even if local filters hide everything loaded so far. */
fun shouldOfferLoadMore(loadedCount: Int, cursor: PageCursor): Boolean = loadedCount > 0 && cursor.hasMore
