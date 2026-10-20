package com.example.stismartlib

import com.example.stismartlib.data.PageCursor
import com.example.stismartlib.network.ApiClient
import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.network.dto.BookCatalogItemDto
import com.example.stismartlib.network.dto.BorrowingHistoryDataDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class PagingTest {
    @Test
    fun hasMoreOnlyBeforeLastPage() {
        assertTrue(PageCursor(page = 1, totalPages = 3).hasMore)
        assertFalse(PageCursor(page = 3, totalPages = 3).hasMore)
        assertFalse(PageCursor().hasMore)
    }

    @Test
    fun nextKeepsQuery() {
        assertEquals(PageCursor(page = 2, totalPages = 3, query = "java"), PageCursor(1, 3, "java").next())
    }

    @Test
    fun catalogMetaParses() {
        val json = """{"success":true,"data":[],"meta":{"pagination":{"page":1,"limit":50,"total":120,"totalPages":3}}}"""
        val env = Gson().fromJson<ApiEnvelope<List<BookCatalogItemDto>>>(json, object : TypeToken<ApiEnvelope<List<BookCatalogItemDto>>>() {}.type)
        assertEquals(3, ApiClient.unwrapPage(Response.success(env)).second?.totalPages)
    }

    @Test
    fun historyPaginationParses() {
        val json = """{"success":true,"data":{"items":[],"pagination":{"page":2,"limit":50,"total":51,"totalPages":2}}}"""
        val env = Gson().fromJson<ApiEnvelope<BorrowingHistoryDataDto>>(json, object : TypeToken<ApiEnvelope<BorrowingHistoryDataDto>>() {}.type)
        assertEquals(2, env.data?.pagination?.page)
    }
}
