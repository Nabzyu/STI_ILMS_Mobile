package com.example.stismartlib

import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.network.dto.ClearanceRecordDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClearanceDtoParsingTest {
    private val gson = Gson()

    @Test
    fun clearanceAcceptsCamelCaseNotCleared() {
        val json = """
            {
              "success": true,
              "data": {
                "status": "Not Cleared",
                "computedStatus": "Not Cleared",
                "reason": "1 active loan",
                "checkedAt": "2026-10-14T01:00:00.000Z",
                "summary": {
                  "activeLoans": 1,
                  "unpaidOverdueFines": 0,
                  "unpaidReplacementCharges": 0,
                  "totalOutstanding": 25.5,
                  "blockCount": 1
                },
                "loans": [
                  {
                    "transactionId": 11,
                    "title": "Clean Code",
                    "status": "Overdue",
                    "dueAt": "2026-10-01T00:00:00.000Z",
                    "currentFine": 25.5
                  }
                ],
                "fines": [],
                "lostBooks": [],
                "activeOverride": null
              }
            }
        """.trimIndent()
        val type = object : TypeToken<ApiEnvelope<ClearanceRecordDto>>() {}.type
        val data = gson.fromJson<ApiEnvelope<ClearanceRecordDto>>(json, type).data!!
        assertEquals("Not Cleared", data.status)
        assertEquals(1, data.summary?.activeLoans)
        assertEquals(25.5, data.summary?.totalOutstanding!!, 0.001)
        assertEquals("Clean Code", data.loans?.first()?.title)
        assertFalse(data.status.equals("Cleared", ignoreCase = true))
    }

    @Test
    fun clearanceAcceptsSnakeCaseCleared() {
        val json = """
            {
              "success": true,
              "data": {
                "status": "Cleared",
                "computed_status": "Cleared",
                "reason": "No blockers",
                "summary": {
                  "active_loans": 0,
                  "unpaid_overdue_fines": 0,
                  "unpaid_replacement_charges": 0,
                  "total_outstanding": 0,
                  "block_count": 0
                },
                "loans": [],
                "fines": [],
                "lost_books": []
              }
            }
        """.trimIndent()
        val type = object : TypeToken<ApiEnvelope<ClearanceRecordDto>>() {}.type
        val data = gson.fromJson<ApiEnvelope<ClearanceRecordDto>>(json, type).data!!
        assertTrue(data.status.equals("Cleared", ignoreCase = true))
        assertEquals(0, data.summary?.activeLoans)
    }
}
