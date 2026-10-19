package com.example.stismartlib

import com.example.stismartlib.network.ApiClient
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionEndTest {
    @Test
    fun sessionEndCodes() {
        listOf(
            "SESSION_EXPIRED",
            "SESSION_UPGRADE_REQUIRED",
            "JWT_REQUIRED",
            "JWT_INVALID",
            "ACCOUNT_ACCESS_REVOKED",
            "ACCOUNT_DEACTIVATED",
        ).forEach { assertTrue(it, ApiClient.isSessionEndCode(it)) }
        listOf("INVALID_CREDENTIALS", "REQUEST_FAILED").forEach {
            assertFalse(it, ApiClient.isSessionEndCode(it))
        }
    }

    @Test
    fun onlyTheCurrentTokenEndsTheSession() {
        assertTrue(ApiClient.shouldEndSession("JWT_REQUIRED", "Bearer fresh", "fresh"))
        assertFalse("stale token", ApiClient.shouldEndSession("JWT_REQUIRED", "Bearer old", "fresh"))
        assertFalse("no token sent", ApiClient.shouldEndSession("JWT_REQUIRED", null, "fresh"))
        assertFalse("already signed out", ApiClient.shouldEndSession("JWT_REQUIRED", "Bearer old", null))
        assertFalse("not a session code", ApiClient.shouldEndSession("REQUEST_FAILED", "Bearer fresh", "fresh"))
    }
}
