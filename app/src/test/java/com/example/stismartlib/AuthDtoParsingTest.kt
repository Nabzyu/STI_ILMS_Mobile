package com.example.stismartlib

import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.network.dto.LoginData
import com.example.stismartlib.network.dto.RegisterData
import com.example.stismartlib.network.dto.RegisterRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthDtoParsingTest {
    private val gson = Gson()

    @Test
    fun loginUserAcceptsCamelCaseFullName() {
        val json = """
            {
              "success": true,
              "data": {
                "token": "abc",
                "tokenType": "Bearer",
                "expiresIn": 3600,
                "user": {
                  "id": 12,
                  "accountId": 12,
                  "schoolId": "02000123456",
                  "fullName": "Ana Santos",
                  "role": "Student"
                }
              }
            }
        """.trimIndent()

        val type = object : TypeToken<ApiEnvelope<LoginData>>() {}.type
        val envelope = gson.fromJson<ApiEnvelope<LoginData>>(json, type)
        val user = envelope.data?.user

        assertNotNull(user)
        assertEquals(12, user!!.accountId)
        assertEquals("02000123456", user.schoolId)
        assertEquals("Ana Santos", user.fullName)
        assertEquals("Student", user.role)
    }

    @Test
    fun loginUserStillAcceptsSnakeCaseFullName() {
        val json = """
            {
              "success": true,
              "data": {
                "token": "abc",
                "user": {
                  "id": 12,
                  "account_id": 12,
                  "school_id": "02000123456",
                  "full_name": "Ana Santos",
                  "role": "Student"
                }
              }
            }
        """.trimIndent()

        val type = object : TypeToken<ApiEnvelope<LoginData>>() {}.type
        val envelope = gson.fromJson<ApiEnvelope<LoginData>>(json, type)

        assertEquals("Ana Santos", envelope.data?.user?.fullName)
    }

    @Test
    fun loginOtpChallengeParses() {
        val json = """
            {"success":true,"data":{"otpRequired":true,"challengeId":"00000000-0000-4000-8000-000000000001",
             "emailHint":"te***@ormoc.sti.edu.ph","expiresIn":600,"resendAfter":60}}
        """.trimIndent()

        val type = object : TypeToken<ApiEnvelope<LoginData>>() {}.type
        val data = gson.fromJson<ApiEnvelope<LoginData>>(json, type).data!!

        assertEquals(true, data.otpRequired)
        assertEquals("00000000-0000-4000-8000-000000000001", data.challengeId)
        assertEquals("te***@ormoc.sti.edu.ph", data.emailHint)
        assertEquals(60, data.resendAfter)
        assertNull(data.token)
    }

    @Test
    fun registerRequestSendsSchoolEmail() {
        val body = gson.toJson(
            RegisterRequest("02000123456", "Ana", "Santos", "ana@ormoc.sti.edu.ph", "BSIT", "1st Year", "secret123", "secret123"),
        )
        assertTrue(body.contains("\"school_email\":\"ana@ormoc.sti.edu.ph\""))
    }

    @Test
    fun registerPendingStatusParses() {
        val json = """{"success":true,"data":{"requestId":7,"schoolId":"02000123456","email":"ana@ormoc.sti.edu.ph","status":"PendingEmail"}}"""
        val type = object : TypeToken<ApiEnvelope<RegisterData>>() {}.type
        val data = gson.fromJson<ApiEnvelope<RegisterData>>(json, type).data!!

        assertEquals(7, data.requestId)
        assertEquals("PendingEmail", data.status)
        assertEquals("ana@ormoc.sti.edu.ph", data.email)
    }
}
