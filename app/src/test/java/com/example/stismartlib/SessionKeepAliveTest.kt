package com.example.stismartlib

import com.example.stismartlib.network.ApiException
import com.example.stismartlib.session.NEVER_RENEWED
import com.example.stismartlib.session.renewSafely
import com.example.stismartlib.session.shouldRenew
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SessionKeepAliveTest {
    @Test
    fun renewsWhenActiveAndNeverRenewed() {
        assertTrue(shouldRenew(nowMs = 200_000, lastInteractionMs = 190_000, lastRenewMs = 0))
    }

    @Test
    fun skipsWhenRenewedRecently() {
        assertFalse(shouldRenew(nowMs = 200_000, lastInteractionMs = 190_000, lastRenewMs = 150_000))
    }

    @Test
    fun skipsWhenUserIdle() {
        assertFalse(shouldRenew(nowMs = 200_000, lastInteractionMs = 100_000, lastRenewMs = 0))
    }

    @Test
    fun firstTouchAfterLaunchRenewsImmediately() {
        // A restored token may be ~10 min old, so the loop must not assume a fresh renewal.
        val now = System.currentTimeMillis()
        assertTrue(shouldRenew(nowMs = now, lastInteractionMs = now - 1_000, lastRenewMs = NEVER_RENEWED))
    }

    @Test
    fun apiFailureDoesNotEscape() = runBlocking {
        assertFalse(renewSafely { throw ApiException("Your session expired. Sign in again.", "JWT_REQUIRED") })
    }

    @Test
    fun offlineDoesNotEscape() = runBlocking {
        assertFalse(renewSafely { throw IOException("offline") })
    }

    @Test(expected = CancellationException::class)
    fun cancellationStillPropagates() {
        runBlocking { renewSafely { throw CancellationException("left composition") } }
    }
}
