package com.example.stismartlib.ui.format

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val IsoDate: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val ShortMonthDay: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d", Locale.US)
private val MediumMonthDayYear: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)

/** Compact date for tight cards (e.g. "Sep 15"). Falls back to the raw string. */
fun formatDateShort(raw: String): String = formatOrRaw(raw, ShortMonthDay)

/** Readable date with year (e.g. "Sep 15, 2026"). Falls back to the raw string. */
fun formatDateMedium(raw: String): String = formatOrRaw(raw, MediumMonthDayYear)

private fun formatOrRaw(raw: String, formatter: DateTimeFormatter): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty() || trimmed.equals("None", ignoreCase = true)) {
        return trimmed.ifEmpty { "None" }
    }
    return try {
        LocalDate.parse(trimmed, IsoDate).format(formatter)
    } catch (_: DateTimeParseException) {
        trimmed
    }
}
