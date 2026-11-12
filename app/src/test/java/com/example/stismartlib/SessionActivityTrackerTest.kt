package com.example.stismartlib

import com.example.stismartlib.session.SessionActivityTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionActivityTrackerTest {
    private val minute = 60_000L

    @Test
    fun inactiveUntilStarted() {
        val tracker = SessionActivityTracker()
        assertFalse(tracker.isActive)
        tracker.recordInteraction(20 * minute)
        assertFalse(tracker.isIdleExpired(30 * minute))
    }

    @Test
    fun expiresAfterTenIdleMinutes() {
        val tracker = SessionActivityTracker()
        tracker.start(0)
        assertFalse(tracker.isIdleExpired(10 * minute - 1))
        assertTrue(tracker.isIdleExpired(10 * minute))
    }

    @Test
    fun interactionResetsIdleWindow() {
        val tracker = SessionActivityTracker()
        tracker.start(0)
        tracker.recordInteraction(9 * minute)
        assertFalse(tracker.isIdleExpired(18 * minute))
        assertTrue(tracker.isIdleExpired(19 * minute))
    }

    @Test
    fun pingsAreThrottledToEveryThirtySeconds() {
        val tracker = SessionActivityTracker()
        tracker.start(0)
        assertEquals(30_000L, tracker.delayUntilNextPing(0))
        assertEquals(20_000L, tracker.delayUntilNextPing(10_000))
        assertEquals(0L, tracker.delayUntilNextPing(45_000))
        tracker.markPinged(45_000)
        assertEquals(30_000L, tracker.delayUntilNextPing(45_000))
    }

    @Test
    fun stopEndsTracking() {
        val tracker = SessionActivityTracker()
        tracker.start(0)
        tracker.stop()
        assertFalse(tracker.isActive)
        assertFalse(tracker.isIdleExpired(60 * minute))
    }
}
