package com.example.stismartlib

import com.example.stismartlib.data.closureDate
import com.example.stismartlib.data.toSchedule
import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.network.dto.LibraryScheduleDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class LibraryScheduleTest {
    private val json = """{"success":true,"data":{"timezone":"Asia/Manila",
        "weekly":[{"dayOfWeek":2,"isOpen":true,"opensAt":"08:00:00","closesAt":"17:00:00"},
                  {"dayOfWeek":7,"isOpen":false,"opensAt":null,"closesAt":null},
                  {"dayOfWeek":9,"isOpen":true,"opensAt":"08:00:00","closesAt":"17:00:00"}],
        "upcomingClosures":[{"date":"2026-11-01T00:00:00.000Z","reason":"All Saints' Day"},
                            {"date":"2026-10-30","reason":" "}]}}"""

    private fun schedule() = Gson().fromJson<ApiEnvelope<LibraryScheduleDto>>(
        json, object : TypeToken<ApiEnvelope<LibraryScheduleDto>>() {}.type,
    ).data!!.toSchedule()

    @Test
    fun weeklyHoursMapToDays() {
        val schedule = schedule()
        assertEquals(listOf(DayOfWeek.TUESDAY, DayOfWeek.SUNDAY), schedule.weekly.map { it.day })
        assertEquals("08:00 – 17:00", schedule.hoursFor(DayOfWeek.TUESDAY)?.label)
        assertEquals("Closed", schedule.hoursFor(DayOfWeek.SUNDAY)?.label)
        assertNull(schedule.hoursFor(DayOfWeek.MONDAY))
    }

    @Test
    fun closuresAreSortedWithDefaultReason() {
        val closures = schedule().closures
        assertEquals(listOf(LocalDate.of(2026, 10, 30), LocalDate.of(2026, 11, 1)), closures.map { it.date })
        assertEquals("Library closed", closures.first().reason)
    }

    @Test
    fun closureDateReadsCampusDay() {
        assertEquals(LocalDate.of(2026, 10, 21), closureDate("2026-10-21"))
        // Midnight in Manila serialized as UTC is still the 21st on campus.
        assertEquals(LocalDate.of(2026, 10, 21), closureDate("2026-10-20T16:00:00.000Z"))
        assertEquals(LocalDate.of(2026, 10, 21), closureDate("2026-10-21T00:00:00.000Z"))
        assertNull(closureDate("not a date"))
        assertNull(closureDate(null))
    }
}
