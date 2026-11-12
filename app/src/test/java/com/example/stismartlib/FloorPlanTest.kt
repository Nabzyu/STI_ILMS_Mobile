package com.example.stismartlib

import com.example.stismartlib.data.detailLine
import com.example.stismartlib.network.ApiClient
import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.network.dto.BookLocationDto
import com.example.stismartlib.network.dto.FloorPlanImageDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Response

class FloorPlanTest {
    private val gson = Gson()

    @Test
    fun imageParsesServerShape() {
        val json = """{"success":true,"data":{"id":3,"originalName":"floor.png",
            "uploadedAt":"2026-10-10T08:15:00.000Z","uploadedBy":"LIB-001",
            "imageUrl":"https://x.supabase.co/storage/v1/object/public/floor-plan-images/a.png"}}"""
        val type = object : TypeToken<ApiEnvelope<FloorPlanImageDto>>() {}.type
        val image = gson.fromJson<ApiEnvelope<FloorPlanImageDto>>(json, type).data!!
        assertEquals("https://x.supabase.co/storage/v1/object/public/floor-plan-images/a.png", image.imageUrl)
        assertEquals("LIB-001", image.uploadedBy)
        assertEquals("2026-10-10T08:15:00.000Z", image.uploadedAt)
    }

    @Test
    fun locationAcceptsNumericAccession() {
        val json = """{"success":true,"data":{"title":"Clean Code","shelfLabel":"A-3",
            "accession":10042,"callNumber":"QA76.76 .M37"}}"""
        val type = object : TypeToken<ApiEnvelope<BookLocationDto>>() {}.type
        val location = gson.fromJson<ApiEnvelope<BookLocationDto>>(json, type).data!!
        assertEquals("A-3", location.shelfLabel)
        assertEquals("Accession 10042 · Call number QA76.76 .M37", location.detailLine())
    }

    @Test
    fun detailLineNeedsAnAccession() {
        assertEquals("Accession 7", BookLocationDto(accession = "7", callNumber = " ").detailLine())
        assertNull(BookLocationDto(accession = "", callNumber = "QA1").detailLine())
    }

    @Test
    fun nullDataMeansNothingPublished() {
        val response = Response.success(ApiEnvelope<FloorPlanImageDto>(success = true, data = null))
        assertNull(ApiClient.unwrapOrNull(response))
    }

    @Test
    fun presentDataIsReturned() {
        val image = FloorPlanImageDto(imageUrl = "https://example.test/a.png")
        val response = Response.success(ApiEnvelope(success = true, data = image))
        assertEquals(image, ApiClient.unwrapOrNull(response))
    }
}
