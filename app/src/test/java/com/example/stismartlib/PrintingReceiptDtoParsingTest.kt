package com.example.stismartlib

import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.network.dto.PrintReceiptDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Test

class PrintingReceiptDtoParsingTest {
    private val gson = Gson()

    @Test
    fun receiptAcceptsSnakeCase() {
        val json = """
            {"success":true,"data":[{
              "print_receipt_id":12,
              "request_id":9,
              "receipt_number":"PR-2026-0012",
              "verification_code":"AB12CD",
              "receipt_status":"Issued",
              "file_name":"essay.pdf",
              "page_count":4,
              "number_of_copies":1,
              "print_type":"Monochrome",
              "paper_size":"A4",
              "amount_received":8.0,
              "received_by":"Desk Staff",
              "received_at":"2026-10-14T02:00:00.000Z"
            }]}
        """.trimIndent()
        val type = object : TypeToken<ApiEnvelope<List<PrintReceiptDto>>>() {}.type
        val data = gson.fromJson<ApiEnvelope<List<PrintReceiptDto>>>(json, type).data!!
        assertEquals(1, data.size)
        assertEquals(12, data[0].printReceiptId)
        assertEquals("PR-2026-0012", data[0].receiptNumber)
        assertEquals(8.0, data[0].amountReceived!!, 0.001)
    }

    @Test
    fun receiptAcceptsCamelCase() {
        val json = """
            {"printReceiptId":3,"receiptNumber":"PR-3","verificationCode":"ZZ99",
            "fileName":"lab.pdf","amountReceived":5.5,"receivedAt":"2026-10-01"}
        """.trimIndent()
        val data = gson.fromJson(json, PrintReceiptDto::class.java)
        assertEquals(3, data.printReceiptId)
        assertEquals("PR-3", data.receiptNumber)
        assertEquals("ZZ99", data.verificationCode)
        assertEquals(5.5, data.amountReceived!!, 0.001)
    }
}
