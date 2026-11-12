package com.example.stismartlib

import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.network.dto.PrintQuoteDto
import com.example.stismartlib.network.dto.PrintRequestDto
import com.example.stismartlib.network.dto.PrintServiceStatusDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrintingDtoParsingTest {
    private val gson = Gson()

    @Test
    fun serviceStatusAcceptsSnakeCase() {
        val json = """
            {"success":true,"data":{"accepting_requests":1,"unavailable_reason":null,"docx_auto_count_available":true}}
        """.trimIndent()
        val type = object : TypeToken<ApiEnvelope<PrintServiceStatusDto>>() {}.type
        val data = gson.fromJson<ApiEnvelope<PrintServiceStatusDto>>(json, type).data!!
        assertTrue(data.isAccepting())
        assertTrue(data.docxAutoCountAvailable == true)
    }

    @Test
    fun serviceStatusAcceptsCamelCaseClosed() {
        val json = """
            {"success":true,"data":{"acceptingRequests":false,"unavailableReason":"Ink low"}}
        """.trimIndent()
        val type = object : TypeToken<ApiEnvelope<PrintServiceStatusDto>>() {}.type
        val data = gson.fromJson<ApiEnvelope<PrintServiceStatusDto>>(json, type).data!!
        assertFalse(data.isAccepting())
        assertEquals("Ink low", data.unavailableReason)
    }

    @Test
    fun quoteAcceptsSnakeCase() {
        val json = """
            {"success":true,"data":{"page_count":3,"total_sheets":6,"calculated_cost":12.5,
            "document_sha256":"abc","printable_file_name":"doc.pdf"}}
        """.trimIndent()
        val type = object : TypeToken<ApiEnvelope<PrintQuoteDto>>() {}.type
        val data = gson.fromJson<ApiEnvelope<PrintQuoteDto>>(json, type).data!!
        assertEquals(3, data.pageCount)
        assertEquals(6, data.totalSheets)
        assertEquals(12.5, data.calculatedCost!!, 0.001)
        assertEquals("abc", data.documentSha256)
    }

    @Test
    fun printRequestAcceptsCamelCase() {
        val json = """
            {"requestId":9,"fileName":"a.pdf","numberOfCopies":2,"printType":"Monochrome",
            "paperSize":"A4","pageCount":4,"calculatedCost":8.0,"paymentStatus":"Unpaid",
            "jobStatus":"Pending","createdAt":"2026-10-14T01:00:00.000Z"}
        """.trimIndent()
        val data = gson.fromJson(json, PrintRequestDto::class.java)
        assertEquals(9, data.requestId)
        assertEquals("Unpaid", data.paymentStatus)
        assertEquals("Pending", data.jobStatus)
    }
}
