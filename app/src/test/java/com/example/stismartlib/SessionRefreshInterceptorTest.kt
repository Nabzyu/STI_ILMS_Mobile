package com.example.stismartlib

import com.example.stismartlib.network.RefreshedTokens
import com.example.stismartlib.network.SessionRefreshInterceptor
import com.example.stismartlib.network.SessionTokens
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SessionRefreshInterceptorTest {
    private class FakeTokens(var access: String?, var refresh: String?) : SessionTokens {
        override fun accessToken() = access
        override fun refreshToken() = refresh
        override fun saveTokens(accessToken: String, refreshToken: String) {
            access = accessToken
            refresh = refreshToken
        }
    }

    /** Fake server: 401 with [code] unless the request carries the token "fresh". */
    private class FakeServer(private val code: String) : Interceptor {
        val seenTokens = mutableListOf<String?>()
        override fun intercept(chain: Interceptor.Chain): Response {
            val token = chain.request().header("Authorization")?.removePrefix("Bearer ")
            seenTokens += token
            val ok = token == "fresh"
            return Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(if (ok) 200 else 401)
                .message("")
                .body((if (ok) """{"success":true,"data":{}}""" else """{"success":false,"code":"$code"}""")
                    .toResponseBody("application/json".toMediaType()))
                .build()
        }
    }

    private fun call(
        tokens: FakeTokens,
        server: FakeServer,
        path: String = "/api/v1/notifications",
        refresh: (String) -> RefreshedTokens?,
    ): Int {
        val addAuth = Interceptor { chain ->
            chain.proceed(chain.request().newBuilder().header("Authorization", "Bearer ${tokens.access}").build())
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(addAuth)
            .addInterceptor(SessionRefreshInterceptor(tokens, refresh))
            .addInterceptor(server)
            .build()
        return client.newCall(Request.Builder().url("https://example.invalid$path").build()).execute().use { it.code }
    }

    @Test
    fun expiredSessionIsRefreshedAndRetriedOnce() {
        val tokens = FakeTokens("old", "key1")
        val server = FakeServer("SESSION_EXPIRED")
        var refreshCalls = 0
        val code = call(tokens, server) { key ->
            refreshCalls++
            assertEquals("key1", key)
            RefreshedTokens("fresh", "key2")
        }
        assertEquals(200, code)
        assertEquals(1, refreshCalls)
        assertEquals(listOf<String?>("old", "fresh"), server.seenTokens)
        assertEquals("fresh", tokens.access)
        assertEquals("key2", tokens.refresh)
    }

    @Test
    fun expiredAccessTokenIsAlsoRefreshed() {
        val tokens = FakeTokens("old", "key1")
        assertEquals(200, call(tokens, FakeServer("JWT_INVALID")) { RefreshedTokens("fresh", "key2") })
    }

    @Test
    fun rejectedKeyPassesTheOriginal401Through() {
        val tokens = FakeTokens("old", "key1")
        assertEquals(401, call(tokens, FakeServer("SESSION_EXPIRED")) { null })
        assertEquals("old", tokens.access)
    }

    @Test
    fun noKeyMeansNoRefresh() {
        var refreshCalls = 0
        assertEquals(401, call(FakeTokens("old", null), FakeServer("SESSION_EXPIRED")) { refreshCalls++; null })
        assertEquals(0, refreshCalls)
    }

    @Test
    fun revokedAccountsAreNotRefreshed() {
        var refreshCalls = 0
        val code = call(FakeTokens("old", "key1"), FakeServer("ACCOUNT_ACCESS_REVOKED")) {
            refreshCalls++
            RefreshedTokens("fresh", "key2")
        }
        assertEquals(401, code)
        assertEquals(0, refreshCalls)
    }

    @Test
    fun signInRequestsAreNeverRefreshed() {
        var refreshCalls = 0
        call(FakeTokens("old", "key1"), FakeServer("SESSION_EXPIRED"), "/api/v1/auth/login") {
            refreshCalls++
            null
        }
        assertEquals(0, refreshCalls)
    }

    @Test
    fun offlineRefreshFailsTheRequestButKeepsTheSession() {
        val tokens = FakeTokens("old", "key1")
        val error = runCatching {
            call(tokens, FakeServer("SESSION_EXPIRED")) { throw IOException("offline") }
        }.exceptionOrNull()
        assertTrue(error is IOException)
        assertEquals("key1", tokens.refresh)
    }

    @Test
    fun aTokenRenewedByAnotherRequestIsReusedWithoutRefreshing() {
        val tokens = FakeTokens("old", "key1")
        var refreshCalls = 0
        // Another request refreshed after this one was sent with "old".
        val server = object : Interceptor {
            var first = true
            override fun intercept(chain: Interceptor.Chain): Response {
                if (first) { first = false; tokens.access = "fresh" }
                return FakeServer("SESSION_EXPIRED").intercept(chain)
            }
        }
        val client = OkHttpClient.Builder()
            .addInterceptor { it.proceed(it.request().newBuilder().header("Authorization", "Bearer ${tokens.access}").build()) }
            .addInterceptor(SessionRefreshInterceptor(tokens, { refreshCalls++; null }))
            .addInterceptor(server)
            .build()
        val code = client.newCall(Request.Builder().url("https://example.invalid/api/v1/fines/me").build()).execute().use { it.code }
        assertEquals(200, code)
        assertEquals(0, refreshCalls)
    }
}
