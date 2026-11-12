package com.example.stismartlib.data

import com.example.stismartlib.data.models.CatalogViewer
import com.example.stismartlib.ui.auth.UserType

/**
 * How many books the cart may hold, or null for no limit. Uses the borrowing policy the catalog
 * reported; before the catalog loads it falls back to the server's default (students 2, faculty
 * unlimited). Like the web, this caps the cart only; the server checks loans and requests on submit.
 */
fun cartLimitFor(userType: UserType, viewer: CatalogViewer?): Int? =
    if (viewer != null) viewer.bookLimit else if (userType == UserType.STUDENT) 2 else null

/** The cart header line, matching the web's "Active books + requests: n / limit". */
fun cartLimitDescription(limit: Int?, viewer: CatalogViewer?): String {
    if (limit == null) return "No borrowing limit for your account"
    val base = "Cart limit: $limit books"
    val active = viewer?.activeBookCount ?: return base
    val note = if (viewer.atLimit) " — return a book or cancel a request before borrowing more" else ""
    return "$base · Active books + requests: $active / $limit$note"
}
