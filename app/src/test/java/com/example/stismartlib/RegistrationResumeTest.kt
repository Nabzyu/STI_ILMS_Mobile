package com.example.stismartlib

import com.example.stismartlib.navigation.Screen
import com.example.stismartlib.network.ApiException
import com.example.stismartlib.ui.auth.registrationResumeRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegistrationResumeTest {
    @Test
    fun unverifiedRegistrationOpensCodeStep() {
        val error = ApiException("Verify the code sent to your school email before signing in.", "EMAIL_VERIFICATION_REQUIRED")
        assertEquals(
            "student_register?verifySchoolId=02000123456",
            registrationResumeRoute(error, "02000123456"),
        )
    }

    @Test
    fun otherLoginErrorsStayOnLogin() {
        listOf("INVALID_CREDENTIALS", "ACCOUNT_APPROVAL_PENDING", "LOGIN_PAUSED").forEach {
            assertNull(it, registrationResumeRoute(ApiException("x", it), "02000123456"))
        }
    }

    @Test
    fun registerRouteKeepsSchoolIdCharacters() {
        assertEquals("student_register", Screen.Register.create())
        // Server school IDs are letters, digits, dots, underscores, and hyphens.
        assertEquals("student_register?verifySchoolId=STI-2026.1_A", Screen.Register.create("STI-2026.1_A"))
    }
}
