package com.example.stismartlib.data

import com.example.stismartlib.network.dto.UserDashboardDto
import kotlin.math.roundToInt

/** The web dashboard's live library facts: who is inside now, and the current announcement. */
data class DashboardSummary(
    val occupancyCurrent: Int,
    val occupancyCapacity: Int,
    val announcement: LibraryAnnouncement?,
)

data class LibraryAnnouncement(
    val title: String,
    val message: String,
    val priority: String,
    /** Preformatted by the server, e.g. "2026-10-20 09:15 AM". */
    val publishedAt: String,
)

fun UserDashboardDto.toSummary(): DashboardSummary? {
    val occupancy = occupancy ?: return null
    val notice = announcement?.takeIf { !it.title.isNullOrBlank() || !it.message.isNullOrBlank() }
    return DashboardSummary(
        occupancyCurrent = occupancy.current ?: 0,
        occupancyCapacity = (occupancy.capacity ?: 1).coerceAtLeast(1),
        announcement = notice?.let {
            LibraryAnnouncement(
                title = it.title.orEmpty(),
                message = it.message.orEmpty(),
                priority = it.priority?.takeIf(String::isNotBlank) ?: "Normal",
                publishedAt = it.publishedAt.orEmpty(),
            )
        },
    )
}

/** Share of seats in use, 0..100; safe for bad data such as more people than seats. */
fun occupancyPercent(current: Int, capacity: Int): Int {
    if (capacity <= 0) return 0
    return (current * 100f / capacity).roundToInt().coerceIn(0, 100)
}
