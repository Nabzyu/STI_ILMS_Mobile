package com.example.stismartlib.data

import com.example.stismartlib.network.dto.LibraryScheduleDto
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeParseException

/** Library opening hours and posted closures, in campus time (Asia/Manila). */
data class LibrarySchedule(
    val weekly: List<DayHours>,
    val closures: List<LibraryClosure>,
) {
    fun hoursFor(day: DayOfWeek): DayHours? = weekly.firstOrNull { it.day == day }
}

data class DayHours(val day: DayOfWeek, val isOpen: Boolean, val opensAt: String?, val closesAt: String?) {
    /** "08:00 – 17:00", or "Closed". */
    val label: String
        get() = if (isOpen && opensAt != null && closesAt != null) "${opensAt.take(5)} – ${closesAt.take(5)}" else "Closed"
}

data class LibraryClosure(val date: LocalDate, val reason: String)

val CampusZone: ZoneId = ZoneId.of("Asia/Manila")

/** Today's date on campus, which is what the server's schedule is keyed to. */
fun campusToday(): LocalDate = LocalDate.now(CampusZone)

fun LibraryScheduleDto.toSchedule(): LibrarySchedule = LibrarySchedule(
    weekly = weekly.orEmpty().mapNotNull { row ->
        val day = row.dayOfWeek?.takeIf { it in 1..7 }?.let(DayOfWeek::of) ?: return@mapNotNull null
        DayHours(day, row.isOpen == true, row.opensAt, row.closesAt)
    }.sortedBy { it.day },
    closures = upcomingClosures.orEmpty().mapNotNull { row ->
        val date = closureDate(row.date) ?: return@mapNotNull null
        LibraryClosure(date, row.reason?.trim().orEmpty().ifBlank { "Library closed" })
    }.sortedBy { it.date },
)

/**
 * Closed dates arrive as "2026-10-21" or as a timestamp of that day's midnight in the
 * server's zone (e.g. "2026-10-20T16:00:00.000Z"); reading it in campus time gives the day.
 */
internal fun closureDate(raw: String?): LocalDate? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    return try {
        if (value.length == 10) LocalDate.parse(value) else Instant.parse(value).atZone(CampusZone).toLocalDate()
    } catch (_: DateTimeParseException) {
        null
    }
}
