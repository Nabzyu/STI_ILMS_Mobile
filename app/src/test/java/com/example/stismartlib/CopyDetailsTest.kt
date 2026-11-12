package com.example.stismartlib

import com.example.stismartlib.data.displayValue
import com.example.stismartlib.data.svgBytesFromDataUri
import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.network.dto.CatalogCopyDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Base64

class CopyDetailsTest {
    private val svg = "<svg xmlns='http://www.w3.org/2000/svg'/>"
    private val uri = "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.toByteArray())

    @Test
    fun decodesSvgDataUri() = assertEquals(svg, String(svgBytesFromDataUri(uri)!!))

    @Test
    fun rejectsOtherUris() {
        assertNull(svgBytesFromDataUri("data:image/png;base64,AAAA"))
        assertNull(svgBytesFromDataUri("data:image/svg+xml;base64,%%%"))
        assertNull(svgBytesFromDataUri(null))
    }

    @Test
    fun blankValuesReadNotRecorded() {
        assertEquals("Not recorded", displayValue(" "))
        assertEquals("Not recorded", displayValue(null))
        assertEquals("Good", displayValue("Good"))
    }

    @Test
    fun copyReplyParses() {
        val json = """{"success":true,"data":{"physicalCopyId":3,"titleId":7,"accessionNumber":"10042","barcode":"STI-000123",
            "shelfLocation":"Circulation A-3","conditionStatus":null,"barcodeImageData":"$uri"}}"""
        val dto = Gson().fromJson<ApiEnvelope<CatalogCopyDto>>(json, object : TypeToken<ApiEnvelope<CatalogCopyDto>>() {}.type).data!!
        assertEquals("STI-000123", dto.barcode)
        assertNull(dto.conditionStatus)
    }
}
