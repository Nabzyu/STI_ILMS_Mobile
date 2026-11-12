package com.example.stismartlib

import com.example.stismartlib.network.ApiException
import com.example.stismartlib.ui.feedback.userFacingMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.IOException

class UserFacingErrorsTest {
    @Test
    fun invalidCredentials() {
        val msg = userFacingMessage(ApiException("x", "INVALID_CREDENTIALS"))
        assertEquals("Invalid school ID or password.", msg)
    }

    @Test
    fun jwtRequired() {
        val msg = userFacingMessage(
            ApiException("A valid Bearer token is required.", "JWT_REQUIRED"),
            fallback = "Unable to load.",
        )
        assertFalse(msg.contains("Bearer", ignoreCase = true))
        // Sign-in copy follows PR #1 (`sessionEndedMessage`).
        assertEquals("Please sign in again to continue.", msg)
    }

    @Test
    fun routeNotFound() {
        val msg = userFacingMessage(
            ApiException("Route not found.", "REQUEST_FAILED"),
            fallback = "Unable to load.",
        )
        assertEquals("This feature is not available right now.", msg)
    }

    @Test
    fun ioException() {
        assertEquals(
            "Unable to reach the library service. Check your internet connection and try again.",
            userFacingMessage(IOException("failed to connect")),
        )
    }
}
