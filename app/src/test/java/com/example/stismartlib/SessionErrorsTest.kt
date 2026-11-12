package com.example.stismartlib

import com.example.stismartlib.network.ApiException
import com.example.stismartlib.network.SESSION_IDLE_CODE
import com.example.stismartlib.network.endsSession
import com.example.stismartlib.network.isAuthHandshakePath
import com.example.stismartlib.network.resolveErrorCode
import com.example.stismartlib.ui.feedback.sessionEndedMessage
import com.example.stismartlib.ui.feedback.userFacingMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionErrorsTest {
    @Test
    fun signInPathsAreHandshakes() {
        assertTrue(isAuthHandshakePath("/api/v1/auth/login"))
        assertTrue(isAuthHandshakePath("/api/v1/auth/login/verify"))
        assertTrue(isAuthHandshakePath("/api/v1/auth/login/resend"))
        assertTrue(isAuthHandshakePath("/api/v1/auth/password-recovery/request"))
        assertTrue(isAuthHandshakePath("/api/v1/auth/password-recovery/verify"))
        assertTrue(isAuthHandshakePath("/api/v1/auth/password-recovery/reset"))
        assertTrue(isAuthHandshakePath("/api/v1/auth/roster-registration"))
        assertTrue(isAuthHandshakePath("/api/v1/auth/roster-registration/begin"))
        assertTrue(isAuthHandshakePath("/api/v1/auth/roster-registration/complete"))
        assertFalse(isAuthHandshakePath("/api/v1/auth/activity"))
        assertFalse(isAuthHandshakePath("/api/v1/auth/me"))
    }

    @Test
    fun bare401MeansWrongPasswordOnlyOnSignIn() {
        assertEquals("INVALID_CREDENTIALS", resolveErrorCode(null, 401, isAuthHandshake = true))
        assertEquals("SESSION_EXPIRED", resolveErrorCode(null, 401, isAuthHandshake = false))
        assertEquals("TOO_MANY_LOGIN_ATTEMPTS", resolveErrorCode(null, 429, isAuthHandshake = true))
        assertEquals("REQUEST_FAILED", resolveErrorCode("", 500, isAuthHandshake = false))
        assertEquals("LOGIN_PAUSED", resolveErrorCode("LOGIN_PAUSED", 429, isAuthHandshake = true))
    }

    @Test
    fun sessionCodesSignTheUserOut() {
        listOf(
            "SESSION_EXPIRED", "SESSION_UPGRADE_REQUIRED", "JWT_REQUIRED", "JWT_INVALID",
            "ACCOUNT_ACCESS_REVOKED", "ACCOUNT_DEACTIVATED",
        ).forEach { assertTrue(it, endsSession(it, isAuthHandshake = false)) }
        assertFalse(endsSession("JWT_ROLE_FORBIDDEN", isAuthHandshake = false))
        assertFalse(endsSession("INVALID_CREDENTIALS", isAuthHandshake = false))
        // A deactivated account found while signing in has no session to end.
        assertFalse(endsSession("ACCOUNT_DEACTIVATED", isAuthHandshake = true))
    }

    @Test
    fun wrongPasswordKeepsRemainingAttempts() {
        val few = ApiException("Wrong credentials. 2 attempts remaining.", "INVALID_CREDENTIALS")
        assertEquals("Invalid Student ID / Employee ID or password. 2 attempts remaining.", userFacingMessage(few))
        val one = ApiException("Wrong credentials. 1 attempts remaining.", "INVALID_CREDENTIALS")
        assertEquals("Invalid Student ID / Employee ID or password. 1 attempt remaining.", userFacingMessage(one))
        val many = ApiException("Wrong credentials.", "INVALID_CREDENTIALS")
        assertEquals("Invalid Student ID / Employee ID or password.", userFacingMessage(many))
    }

    @Test
    fun pausedSignInKeepsMinutes() {
        val paused = ApiException("Sign-in is paused. Try again in 7 minutes.", "LOGIN_PAUSED")
        assertEquals("Too many wrong attempts. Sign-in is paused. Try again in 7 minutes.", userFacingMessage(paused))
        val first = ApiException("Too many wrong attempts. Try again in 10 minutes.", "LOGIN_PAUSED")
        assertEquals("Too many wrong attempts. Sign-in is paused. Try again in 10 minutes.", userFacingMessage(first))
        val noMinutes = ApiException("Paused.", "LOGIN_PAUSED")
        assertEquals("Too many wrong attempts. Sign-in is paused for 10 minutes.", userFacingMessage(noMinutes))
    }

    @Test
    fun signInCodesHaveFixedCopy() {
        listOf(
            "LOGIN_CODE_WAIT", "LOGIN_EMAIL_MISSING", "LOGIN_CHANGED", "LOGIN_CODE_INVALID",
            "LOGIN_CODE_PAUSED", "DEVICE_ORIGIN", "STAFF_ACCOUNT_NOT_SUPPORTED", "SESSION_EXPIRED", "SESSION_UPGRADE_REQUIRED",
            "RECOVERY_CODE_INVALID", "RECOVERY_LOCKED", "RECOVERY_TOKEN_INVALID", "PASSWORD_UNCHANGED",
            "RECOVERY_RATE_LIMITED", "ROSTER_CODE_INVALID", "ROSTER_REGISTRATION_CLOSED",
            "STUDENT_ID_INVALID", "ROSTER_PASSWORD_INVALID",
        ).forEach { code ->
            val message = userFacingMessage(ApiException("internal {detail}", code), "fallback")
            assertFalse(code, message == "fallback")
            assertFalse(code, message.contains("{"))
        }
    }

    @Test
    fun sessionEndedMessages() {
        assertTrue(sessionEndedMessage(SESSION_IDLE_CODE).contains("10 minutes"))
        assertEquals("Your session has ended. Please sign in again.", sessionEndedMessage("SESSION_EXPIRED"))
        assertEquals("Please sign in again to continue.", sessionEndedMessage("JWT_INVALID"))
    }
}
