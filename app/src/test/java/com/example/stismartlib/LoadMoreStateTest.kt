package com.example.stismartlib

import com.example.stismartlib.ui.components.LoadMoreState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadMoreStateTest {
    private val state = LoadMoreState(CoroutineScope(Dispatchers.Unconfined))

    @Test
    fun secondTapWhileLoadingIsIgnored() {
        val gate = CompletableDeferred<Unit>()
        var calls = 0
        state.load { calls++; gate.await() }
        state.load { calls++ }
        assertTrue(state.loading)
        gate.complete(Unit)
        assertFalse(state.loading)
        assertEquals(1, calls)
    }

    @Test
    fun failureShowsMessageAndClearsOnRetry() {
        state.load { throw java.io.IOException("offline") }
        assertFalse(state.loading)
        assertTrue(state.error!!.isNotBlank())
        state.load { }
        assertNull(state.error)
    }
}
