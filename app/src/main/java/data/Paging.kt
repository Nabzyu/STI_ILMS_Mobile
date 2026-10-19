package com.example.stismartlib.data

/** Where a paged list stands; `page = 0` means nothing is loaded yet. */
data class PageCursor(val page: Int = 0, val totalPages: Int = 0, val query: String? = null) {
    val hasMore: Boolean get() = page in 1 until totalPages

    /** The next page of the same query. */
    fun next(): PageCursor = copy(page = page + 1)
}

/** Appends [next] to [existing], skipping items already present (rows can shift between pages). */
fun <T, K> mergePage(existing: List<T>, next: List<T>, key: (T) -> K): List<T> {
    val seen = existing.mapTo(HashSet(), key)
    return existing + next.filter { seen.add(key(it)) }
}
