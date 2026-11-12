package com.example.stismartlib

import com.example.stismartlib.network.LibraryApi
import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.network.dto.RosterBeginData
import com.example.stismartlib.network.dto.RosterBeginRequest
import com.example.stismartlib.network.dto.RosterCompleteData
import com.example.stismartlib.network.dto.RosterCompleteRequest
import com.example.stismartlib.network.dto.RosterVerifyData
import com.example.stismartlib.network.dto.RosterVerifyRequest
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import retrofit2.http.POST

/** Pins the app to the server's roster sign-up contract (apps/api jwt-auth.routes.ts + roster-registration.service.ts). */
class RosterRegistrationContractTest {
    private val gson = Gson()

    private fun json(value: Any) = JsonParser.parseString(gson.toJson(value)).asJsonObject

    private inline fun <reified T> envelope(raw: String): ApiEnvelope<T> =
        gson.fromJson(raw, object : TypeToken<ApiEnvelope<T>>() {}.type)

    private fun postPath(method: String) =
        LibraryApi::class.java.methods.first { it.name == method }.getAnnotation(POST::class.java)!!.value

    @Test
    fun endpointsUseRosterRegistrationFlow() {
        assertEquals("api/v1/auth/roster-registration/begin", postPath("rosterRegistrationBegin"))
        assertEquals("api/v1/auth/roster-registration/verify", postPath("rosterRegistrationVerify"))
        assertEquals("api/v1/auth/roster-registration/complete", postPath("rosterRegistrationComplete"))
        assertFalse(
            "Retired /auth/register (410 REGISTRATION_FLOW_REPLACED) must not be called",
            LibraryApi::class.java.methods.any { it.getAnnotation(POST::class.java)?.value == "api/v1/auth/register" },
        )
    }

    @Test
    fun requestBodiesUseServerFieldNames() {
        assertEquals(setOf("school_id"), json(RosterBeginRequest("02000123456")).keySet())
        assertEquals(setOf("challenge_id", "code"), json(RosterVerifyRequest("id", "123456")).keySet())
        assertEquals(
            setOf("challenge_id", "grant", "password", "confirm_password"),
            json(RosterCompleteRequest("id", "g", "secret123", "secret123")).keySet(),
        )
    }

    @Test
    fun responseBodiesParse() {
        val id = "00000000-0000-4000-8000-000000000001"
        assertEquals(id, envelope<RosterBeginData>("""{"success":true,"data":{"challengeId":"$id"}}""").data?.challengeId)
        assertEquals("a".repeat(64), envelope<RosterVerifyData>("""{"success":true,"data":{"grant":"${"a".repeat(64)}"}}""").data?.grant)
        val done = envelope<RosterCompleteData>("""{"success":true,"data":{"status":"Active","schoolId":"02000123456"}}""").data
        assertEquals("Active", done?.status)
        assertEquals("02000123456", done?.schoolId)
    }
}
