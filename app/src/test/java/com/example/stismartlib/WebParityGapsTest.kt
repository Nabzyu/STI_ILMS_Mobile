package com.example.stismartlib

import com.example.stismartlib.data.PageCursor
import com.example.stismartlib.data.cartLimitDescription
import com.example.stismartlib.data.cartLimitFor
import com.example.stismartlib.data.isLoadedFor
import com.example.stismartlib.data.models.CatalogViewer
import com.example.stismartlib.data.models.InLibraryUse
import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.network.dto.BookCatalogItemDto
import com.example.stismartlib.network.dto.InLibraryUseDto
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.format.formatDateTime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WebParityGapsTest {
    private val gson = Gson()

    @Test
    fun cartLimitUsesServerPolicyOnceKnown() {
        assertEquals(3, cartLimitFor(UserType.STUDENT, CatalogViewer(activeBookCount = 1, bookLimit = 3)))
        assertNull(cartLimitFor(UserType.FACULTY, CatalogViewer(activeBookCount = 4, bookLimit = null)))
        assertEquals(5, cartLimitFor(UserType.FACULTY, CatalogViewer(activeBookCount = 0, bookLimit = 5)))
    }

    @Test
    fun cartLimitFallsBackToServerDefaultBeforeCatalogLoads() {
        assertEquals(2, cartLimitFor(UserType.STUDENT, null))
        assertNull(cartLimitFor(UserType.FACULTY, null))
    }

    @Test
    fun cartDescriptionShowsActiveCountAndLimitNote() {
        assertEquals("No borrowing limit for your account", cartLimitDescription(null, null))
        assertEquals("Cart limit: 2 books", cartLimitDescription(2, null))
        assertEquals(
            "Cart limit: 2 books · Active books + requests: 1 / 2",
            cartLimitDescription(2, CatalogViewer(1, 2)),
        )
        assertTrue(cartLimitDescription(2, CatalogViewer(2, 2)).endsWith("before borrowing more"))
    }

    @Test
    fun catalogMetaViewerParses() {
        val json = """
            {"success":true,"data":[{"titleId":7,"title":"Networks"}],
             "meta":{"pagination":{"page":1,"limit":50,"total":1,"totalPages":1},
                     "viewer":{"role":"Student","activeBookCount":2,"bookLimit":2}}}
        """.trimIndent()
        val type = object : TypeToken<ApiEnvelope<List<BookCatalogItemDto>>>() {}.type
        val envelope: ApiEnvelope<List<BookCatalogItemDto>> = gson.fromJson(json, type)
        assertEquals(2, envelope.meta?.viewer?.activeBookCount)
        assertEquals(2, envelope.meta?.viewer?.bookLimit)
        assertEquals(1, envelope.meta?.pagination?.totalPages)
    }

    @Test
    fun cursorIsOnlyReusedForTheSameCategory() {
        val cursor = PageCursor(page = 1, totalPages = 3, query = "java", categoryId = 4)
        assertTrue(cursor.isLoadedFor("java", 4))
        assertFalse(cursor.isLoadedFor("java", null))
        assertFalse(cursor.isLoadedFor("java", 5))
        assertEquals(4, cursor.next().categoryId)
    }

    @Test
    fun inLibraryUseParsesSnakeCase() {
        val json = """
            {"success":true,"data":[{"use_id":31,"status":"Active","started_at":"2026-11-12T01:00:00.000Z",
              "hand_back_by":"2026-11-12T09:00:00.000Z","ended_at":null,"condition_out":"Good",
              "condition_in":null,"title":"Data Structures","barcode":"STI-000123"}]}
        """.trimIndent()
        val type = object : TypeToken<ApiEnvelope<List<InLibraryUseDto>>>() {}.type
        val row = gson.fromJson<ApiEnvelope<List<InLibraryUseDto>>>(json, type).data!!.single()
        assertEquals(31L, row.useId)
        assertEquals("2026-11-12T09:00:00.000Z", row.handBackBy)
        assertEquals("STI-000123", row.barcode)
    }

    @Test
    fun reviewStatusReadsAsMissingHandBack() {
        val use = InLibraryUse("1", "Data Structures", "STI-1", "Review", "")
        assertEquals("Hand-back missing — contact the library", use.statusLabel)
        assertEquals("Active", use.copy(status = "Active").statusLabel)
    }

    @Test
    fun dateTimeShowsLibraryTime() {
        assertEquals("Nov 12, 2026 5:00 PM", formatDateTime("2026-11-12T09:00:00.000Z"))
        assertEquals("not a date", formatDateTime("not a date"))
    }
}
