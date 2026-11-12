package com.example.stismartlib.session

/**
 * Client side of the server's 10-minute idle rule, modelled on the web app's `SessionActivity.tsx`:
 * user interaction counts as activity, `/auth/activity` is called at most once per [pingIntervalMillis],
 * and a session idle for [idleMillis] is treated as ended. Times are monotonic milliseconds.
 */
class SessionActivityTracker(
    private val idleMillis: Long = IDLE_MILLIS,
    private val pingIntervalMillis: Long = PING_INTERVAL_MILLIS,
) {
    private var lastInteractionAt: Long? = null
    private var lastPingAt: Long = 0L

    /** Call when a session starts or is restored; the server has just seen activity. */
    fun start(now: Long) {
        lastInteractionAt = now
        lastPingAt = now
    }

    fun stop() {
        lastInteractionAt = null
    }

    val isActive: Boolean get() = lastInteractionAt != null

    fun isIdleExpired(now: Long): Boolean {
        val last = lastInteractionAt ?: return false
        return now - last >= idleMillis
    }

    fun recordInteraction(now: Long) {
        if (lastInteractionAt != null) lastInteractionAt = now
    }

    /** How long to wait before the next `/auth/activity` call (0 = now). */
    fun delayUntilNextPing(now: Long): Long = (lastPingAt + pingIntervalMillis - now).coerceAtLeast(0L)

    fun markPinged(now: Long) {
        lastPingAt = now
    }

    companion object {
        const val IDLE_MILLIS = 10 * 60_000L
        const val PING_INTERVAL_MILLIS = 30_000L
    }
}
