package com.example.stismartlib

import com.example.stismartlib.data.toFineReceipt
import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.network.dto.FineReceiptDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FineReceiptTest {
    @Test
    fun receiptsParseAndMap() {
        val json = """{"success":true,"data":[{"receiptId":12,"receiptNumber":"OR-20261010-000012",
            "amountReceived":"45.50","paymentMethod":"Cash","receivedAt":"2026-10-10T03:20:00.000Z","status":"Issued"}]}"""
        val type = object : TypeToken<ApiEnvelope<List<FineReceiptDto>>>() {}.type
        val receipt = Gson().fromJson<ApiEnvelope<List<FineReceiptDto>>>(json, type).data!!.single().toFineReceipt()!!
        assertEquals("12", receipt.id)
        assertEquals("OR-20261010-000012", receipt.number)
        assertEquals(45.5, receipt.amount, 0.001)
        assertEquals("2026-10-10", receipt.receivedOn)
        assertFalse(receipt.isReversed)
    }

    @Test
    fun missingFieldsFallBack() {
        val receipt = FineReceiptDto(receiptId = 7, status = "Reversed").toFineReceipt()!!
        assertEquals("Receipt 7", receipt.number)
        assertEquals("Cash", receipt.method)
        assertEquals(0.0, receipt.amount, 0.0)
        assertTrue(receipt.isReversed)
    }

    @Test
    fun rowWithoutIdIsDropped() {
        assertNull(FineReceiptDto(receiptNumber = "OR-1").toFineReceipt())
    }
}
