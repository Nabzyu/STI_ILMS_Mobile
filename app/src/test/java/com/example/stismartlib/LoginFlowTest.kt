package com.example.stismartlib

import com.example.stismartlib.data.LoginStep
import com.example.stismartlib.data.interpretLoginData
import com.example.stismartlib.network.ApiException
import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.network.dto.LoginData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginFlowTest {
    private val gson = Gson()

    private fun parse(json: String): LoginData {
        val type = object : TypeToken<ApiEnvelope<LoginData>>() {}.type
        return gson.fromJson<ApiEnvelope<LoginData>>(json, type).data!!
    }

    @Test
    fun studentLoginRequiresEmailedCode() {
        // Shape from login-security.service.ts `login()` for Student/Faculty.
        val data = parse(
            """{"success":true,"message":"Login successful.","data":{"otpRequired":true,
            "challengeId":"6f1c2a8e-1b2c-4d3e-8f90-123456789abc","emailHint":"ju***@ormoc.sti.edu.ph",
            "expiresIn":600,"resendAfter":60}}""",
        )
        val step = interpretLoginData(data, "02000123456")
        assertTrue(step is LoginStep.CodeRequired)
        val challenge = (step as LoginStep.CodeRequired).challenge
        assertEquals("6f1c2a8e-1b2c-4d3e-8f90-123456789abc", challenge.challengeId)
        assertEquals("ju***@ormoc.sti.edu.ph", challenge.emailHint)
        assertEquals(600, challenge.expiresInSeconds)
        assertEquals(60, challenge.resendAfterSeconds)
    }

    @Test
    fun staffLoginReturnsTokenDirectly() {
        // Shape from login-security.service.ts `loginResult()`, also returned by /auth/login/verify.
        val data = parse(
            """{"success":true,"data":{"token":"header.payload.sig","tokenType":"Bearer","expiresIn":28800,
            "redirect":"/staff/dashboard","user":{"id":7,"accountId":7,"schoolId":"DESK-001",
            "fullName":"Desk Staff","role":"Staff"}}}""",
        )
        val step = interpretLoginData(data, "DESK-001") as LoginStep.Authenticated
        assertEquals("header.payload.sig", step.token)
        assertEquals(7, step.accountId)
        assertEquals("DESK-001", step.schoolId)
        assertEquals("Desk Staff", step.fullName)
        assertEquals("Staff", step.role)
    }

    @Test
    fun missingNameFallsBackToSchoolId() {
        val data = parse("""{"data":{"token":"t","user":{"id":3,"role":"Faculty"}}}""")
        val step = interpretLoginData(data, "FAC-01") as LoginStep.Authenticated
        assertEquals("FAC-01", step.schoolId)
        assertEquals("FAC-01", step.fullName)
    }

    @Test
    fun codeRequiredWithoutChallengeIsRejected() {
        val data = parse("""{"data":{"otpRequired":true}}""")
        val error = assertThrows(ApiException::class.java) { interpretLoginData(data, "X01") }
        assertEquals("INVALID_LOGIN_RESPONSE", error.code)
    }

    @Test
    fun missingTokenIsRejected() {
        val data = parse("""{"data":{"user":{"id":3,"role":"Student"}}}""")
        val error = assertThrows(ApiException::class.java) { interpretLoginData(data, "X01") }
        assertEquals("INVALID_TOKEN_RESPONSE", error.code)
    }

    @Test
    fun deviceSwitchIsReportedNotIgnored() {
        val data = parse("""{"data":{"deviceSwitchRequired":true,"switchToken":"abc"}}""")
        val error = assertThrows(ApiException::class.java) { interpretLoginData(data, "X01") }
        assertEquals("DEVICE_SWITCH_REQUIRED", error.code)
    }
}
