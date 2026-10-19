package com.example.stismartlib.data

/**
 * Cart titles to drop because the server already has an active loan or request for them.
 * [activeTitleIds] is null when that check was unavailable; then the cart stays as it is.
 */
fun cartAfterSync(cartTitleIds: List<String>, activeTitleIds: List<Long>?): Set<String> {
    val active = activeTitleIds?.toSet() ?: return emptySet()
    return cartTitleIds.filterTo(HashSet()) { it.toLongOrNull() in active }
}

/** Runs [block]; any failure except cancellation (bad reply, missing route, offline) becomes null. */
suspend fun <T> orNullOnFailure(block: suspend () -> T): T? = try {
    block()
} catch (cancelled: kotlinx.coroutines.CancellationException) {
    throw cancelled
} catch (_: Exception) {
    null
}
