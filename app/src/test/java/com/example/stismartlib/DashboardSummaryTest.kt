package com.example.stismartlib

import com.example.stismartlib.data.LibraryAnnouncement
import com.example.stismartlib.data.occupancyPercent
import com.example.stismartlib.data.toSummary
import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.network.dto.UserDashboardDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DashboardSummaryTest {
    private fun parse(json: String) = Gson().fromJson<ApiEnvelope<UserDashboardDto>>(
        json, object : TypeToken<ApiEnvelope<UserDashboardDto>>() {}.type,
    ).data!!

    @Test
    fun mapsOccupancyAndAnnouncement() {
        val s = parse("""{"success":true,"data":{"occupancy":{"current":12,"capacity":80},
            "announcement":{"id":4,"title":"Inventory week","message":"Closed Friday.","priority":"Important","publishedAt":"2026-10-20 09:15 AM"},
            "summary":{"activeLoans":1}}}""").toSummary()!!
        assertEquals(12, s.occupancyCurrent)
        assertEquals(80, s.occupancyCapacity)
        assertEquals(LibraryAnnouncement("Inventory week", "Closed Friday.", "Important", "2026-10-20 09:15 AM"), s.announcement)
    }

    @Test
    fun nullAnnouncement() {
        assertNull(parse("""{"success":true,"data":{"occupancy":{"current":0,"capacity":80},"announcement":null}}""").toSummary()!!.announcement)
    }

    @Test
    fun missingOccupancyMeansNoSummary() = assertNull(UserDashboardDto().toSummary())

    @Test
    fun percentIsClamped() {
        assertEquals(15, occupancyPercent(12, 80))
        assertEquals(100, occupancyPercent(120, 80))
        assertEquals(0, occupancyPercent(-3, 80))
        assertEquals(0, occupancyPercent(5, 0))
    }
}
