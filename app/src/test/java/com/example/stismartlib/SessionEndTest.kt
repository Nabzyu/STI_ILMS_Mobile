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
}
