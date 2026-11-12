package com.example.stismartlib

import com.example.stismartlib.data.PageCursor
import com.example.stismartlib.data.clearanceScheduleLine
import com.example.stismartlib.data.fetchPages
import com.example.stismartlib.data.newItems
import com.example.stismartlib.network.dto.ClearanceScheduleDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DeferredFixesTest {
    @Test
    fun newItemsSkipsKnownAndRepeated() {
        assertEquals(listOf(3, 4), newItems(listOf(1, 2), listOf(2, 3, 3, 4)) { it })
    }

    @Test
    fun refreshReloadsEveryPageTheUserHadOpen() = runBlocking {
        val asked = mutableListOf<Int>()
        val (rows, cursor) = fetchPages(upTo = 3, key = { it }) { page ->
            asked += page
            listOf(page * 10, page * 10 + 1) to 5
        }
        assertEquals(listOf(1, 2, 3), asked)
        assertEquals(listOf(10, 11, 20, 21, 30, 31), rows)
        assertEquals(PageCursor(page = 3, totalPages = 5), cursor)
    }

    @Test
    fun refreshStopsAtTheLastServerPage() = runBlocking {
        val asked = mutableListOf<Int>()
        val (_, cursor) = fetchPages(upTo = 4, key = { it }) { page -> asked += page; listOf(page) to 2 }
        assertEquals(listOf(1, 2), asked)
        assertEquals(PageCursor(page = 2, totalPages = 2), cursor)
    }

    @Test
    fun deadlineWithOffsetIsReadToo() = assertEquals(
        "Clearance deadline: October 30, 2026 at 5:00 PM (Philippine time)",
        clearanceScheduleLine(ClearanceScheduleDto(deadlineAt = "2026-10-30T17:00:00+08:00")),
    )
}
