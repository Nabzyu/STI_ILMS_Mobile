package com.example.stismartlib

import com.example.stismartlib.session.AppLock
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLockTest {
    private val second = 1_000L
    private val minute = 60_000L

    @Test
    fun locksAtStartWhenTurnedOn() {
        val lock = AppLock()
        lock.onSessionRestored(enabled = true)
        assertTrue(lock.isLocked)
    }

    @Test
    fun staysOpenAtStartWhenTurnedOff() {
        val lock = AppLock()
        lock.onSessionRestored(enabled = false)
        assertFalse(lock.isLocked)
    }

    @Test
    fun restoreAgainInSameProcessDoesNotRelock() {
        // MainActivity re-runs restoreSession when it is recreated (e.g. rotation).
        val lock = AppLock()
        lock.onSessionRestored(enabled = true)
        lock.unlock()
        lock.onSessionRestored(enabled = true)
        assertFalse(lock.isLocked)
    }

    @Test
    fun freshPasswordSignInIsNotLocked() {
        val lock = AppLock()
        lock.onSignedIn()
        lock.onSessionRestored(enabled = true)
        assertFalse(lock.isLocked)
    }

    @Test
    fun relocksAfterOneMinuteAway() {
        val lock = unlocked()
        lock.onBackground(10 * minute)
        lock.onForeground(11 * minute, enabled = true)
        assertTrue(lock.isLocked)
    }

    @Test
    fun shortTripAwayDoesNotRelock() {
        val lock = unlocked()
        lock.onBackground(10 * minute)
        lock.onForeground(10 * minute + 59 * second, enabled = true)
        assertFalse(lock.isLocked)
    }

    @Test
    fun doesNotRelockWhenTurnedOff() {
        val lock = unlocked()
        lock.onBackground(0)
        lock.onForeground(30 * minute, enabled = false)
        assertFalse(lock.isLocked)
    }

    @Test
    fun foregroundWithoutBackgroundDoesNothing() {
        // First onStart after launch: there was no onStop before it.
        val lock = AppLock()
        lock.onForeground(30 * minute, enabled = true)
        assertFalse(lock.isLocked)
    }

    @Test
    fun timeSpentOnSystemPinScreenWhileLockedDoesNotRelockAfterUnlock() {
        val lock = AppLock()
        lock.onSessionRestored(enabled = true)
        lock.onBackground(0) // system PIN screen covers the app while still locked
        lock.unlock()
        lock.onForeground(5 * minute, enabled = true)
        assertFalse(lock.isLocked)
    }

    @Test
    fun backgroundTimeIsUsedOnce() {
        val lock = unlocked()
        lock.onBackground(0)
        lock.onForeground(10 * second, enabled = true)
        lock.onForeground(10 * minute, enabled = true)
        assertFalse(lock.isLocked)
    }

    @Test
    fun resetClearsLockOnSignOut() {
        val lock = AppLock()
        lock.onSessionRestored(enabled = true)
        lock.reset()
        assertFalse(lock.isLocked)
    }

    @Test
    fun offeredOnlyOnceAndOnlyWithKeyAndDeviceLock() {
        assertTrue(AppLock.shouldOffer(hasRefreshKey = true, storedChoice = null, deviceCanAuthenticate = true))
        assertFalse(AppLock.shouldOffer(hasRefreshKey = false, storedChoice = null, deviceCanAuthenticate = true))
        assertFalse(AppLock.shouldOffer(hasRefreshKey = true, storedChoice = false, deviceCanAuthenticate = true))
        assertFalse(AppLock.shouldOffer(hasRefreshKey = true, storedChoice = true, deviceCanAuthenticate = true))
        assertFalse(AppLock.shouldOffer(hasRefreshKey = true, storedChoice = null, deviceCanAuthenticate = false))
    }

    private fun unlocked() = AppLock().also {
        it.onSessionRestored(enabled = true)
        it.unlock()
    }
}
