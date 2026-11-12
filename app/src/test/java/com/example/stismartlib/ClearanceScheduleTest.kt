package com.example.stismartlib

import com.example.stismartlib.data.clearanceScheduleLine
import com.example.stismartlib.data.isPastClearanceDeadline
import com.example.stismartlib.network.dto.ClearanceScheduleDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ClearanceScheduleTest {
    @Test
    fun noSchedule() = assertEquals("Your current library requirements are listed below.", clearanceScheduleLine(null))

    @Test
    fun deadlineInPhilippineTime() = assertEquals(
        "Clearance deadline: October 30, 2026 at 5:00 PM (Philippine time)",
        clearanceScheduleLine(ClearanceScheduleDto(termId = 3, deadlineAt = "2026-10-30T09:00:00.000Z")),
    )

    @Test
    fun scheduleWithoutDeadline() =
        assertEquals("No schedule yet for clearance day.", clearanceScheduleLine(ClearanceScheduleDto(termId = 3)))

    @Test
    fun pastDeadline() {
        val schedule = ClearanceScheduleDto(deadlineAt = "2026-10-30T09:00:00.000Z")
        assertTrue(isPastClearanceDeadline(schedule, Instant.parse("2026-10-30T09:00:01Z")))
        assertFalse(isPastClearanceDeadline(schedule, Instant.parse("2026-10-30T08:59:59Z")))
        assertFalse(isPastClearanceDeadline(null, Instant.now()))
    }
}
