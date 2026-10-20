package com.example.stismartlib.data

import com.example.stismartlib.network.dto.ClearanceScheduleDto
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val DeadlineFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a", Locale.US).withZone(CampusZone)

// OffsetDateTime accepts both "…Z" and "…+08:00"; Instant.parse rejects offsets on older Android.
private fun ClearanceScheduleDto.deadline(): Instant? = try {
    deadlineAt?.let { OffsetDateTime.parse(it).toInstant() }
} catch (_: DateTimeParseException) {
    null
}

/** The web's clearance schedule line; no schedule (or an old server without the field) reads as requirements only. */
fun clearanceScheduleLine(schedule: ClearanceScheduleDto?): String {
    if (schedule == null) return "Your current library requirements are listed below."
    val deadline = schedule.deadline() ?: return "No schedule yet for clearance day."
    return "Clearance deadline: ${DeadlineFormat.format(deadline)} (Philippine time)"
}

fun isPastClearanceDeadline(schedule: ClearanceScheduleDto?, now: Instant): Boolean {
    val deadline = schedule?.deadline() ?: return false
    return now.isAfter(deadline)
}
