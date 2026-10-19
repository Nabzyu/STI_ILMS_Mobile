package com.example.stismartlib

import com.example.stismartlib.session.shouldRenew
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
