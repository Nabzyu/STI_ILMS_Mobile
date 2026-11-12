package com.example.stismartlib.session

/**
 * When the fingerprint lock screen is shown. Only for phones that stay signed in with a 30-day key
 * and whose user turned the lock on. Locks once when the app starts with a saved session, and again
 * when it returns after [relockAfterMillis] or more in the background. A fresh password sign-in is
 * never locked. Times are monotonic milliseconds.
 */
class AppLock(private val relockAfterMillis: Long = RELOCK_AFTER_MILLIS) {
    var isLocked: Boolean = false
        private set

    private var backgroundedAt: Long? = null
    private var startHandled = false

    /** A saved session was restored. Locks only the first time in this process (not on rotation). */
    fun onSessionRestored(enabled: Boolean) {
        if (startHandled) return
        startHandled = true
        if (enabled) isLocked = true
    }

    /** The user just signed in with their password. */
    fun onSignedIn() {
        startHandled = true
        isLocked = false
        backgroundedAt = null
    }

    /**
     * The app left the screen. Ignored while locked, so leaving for the system PIN screen
     * during unlock does not count as time away.
     */
    fun onBackground(now: Long) {
        if (!isLocked) backgroundedAt = now
    }

    fun onForeground(now: Long, enabled: Boolean) {
        val since = backgroundedAt ?: return
        backgroundedAt = null
        if (enabled && now - since >= relockAfterMillis) isLocked = true
    }

    fun unlock() {
        isLocked = false
        backgroundedAt = null
    }

    /** Signed out: nothing to protect. */
    fun reset() {
        isLocked = false
        backgroundedAt = null
    }

    companion object {
        const val RELOCK_AFTER_MILLIS = 60_000L

        /**
         * Ask "Use fingerprint to unlock?" once per account, only when the phone holds a 30-day key
         * and can check a fingerprint or screen lock.
         */
        fun shouldOffer(hasRefreshKey: Boolean, storedChoice: Boolean?, deviceCanAuthenticate: Boolean): Boolean =
            hasRefreshKey && storedChoice == null && deviceCanAuthenticate
    }
}
