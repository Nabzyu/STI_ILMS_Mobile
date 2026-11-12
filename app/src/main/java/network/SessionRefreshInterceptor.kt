package com.example.stismartlib.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/** Where the interceptor reads and writes the phone's tokens (implemented by `SessionStore`). */
interface SessionTokens {
    fun accessToken(): String?
    fun refreshToken(): String?
    fun saveTokens(accessToken: String, refreshToken: String)
}

data class RefreshedTokens(val accessToken: String, val refreshToken: String)

/** 401 codes that a fresh access token can fix. Anything else (revoked, deactivated…) still signs out. */
val RefreshableCodes: Set<String> = setOf("SESSION_EXPIRED", "JWT_INVALID")

/**
 * Keeps a Student/Faculty phone signed in for the 30-day refresh window. When a request fails because
 * the access token expired or the 10-minute idle limit passed, swap the refresh key for a new access
 * token (`/auth/refresh`) and retry the request once. Must run after the interceptor that adds the
 * `Authorization` header.
 *
 * [refresh] returns null when the server rejects the key (the session really ended) and throws
 * [IOException] when it cannot be reached (the session is kept; the request fails as offline).
 */
class SessionRefreshInterceptor(
    private val tokens: SessionTokens,
    private val refresh: (refreshToken: String) -> RefreshedTokens?,
    private val gson: Gson = Gson(),
) : Interceptor {
    private val lock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (isAuthHandshakePath(request.url.encodedPath)) return chain.proceed(request)
        val response = chain.proceed(request)
        if (response.code != 401 || errorCode(response) !in RefreshableCodes) return response

        val sentToken = request.header("Authorization")?.removePrefix("Bearer ")?.trim()
        // One refresh at a time: the key rotates, so a second refresh with the old key would look stolen.
        val newToken = synchronized(lock) {
            val current = tokens.accessToken()
            if (!current.isNullOrBlank() && current != sentToken) {
                current // another request already refreshed while this one waited
            } else {
                val refreshToken = tokens.refreshToken() ?: return response
                val refreshed = refresh(refreshToken) ?: return response
                tokens.saveTokens(refreshed.accessToken, refreshed.refreshToken)
                refreshed.accessToken
            }
        }
        response.close()
        return chain.proceed(request.newBuilder().header("Authorization", "Bearer $newToken").build())
    }

    private fun errorCode(response: Response): String? = try {
        gson.fromJson(response.peekBody(16_384).string(), JsonObject::class.java)
            ?.get("code")?.takeIf { it.isJsonPrimitive }?.asString
    } catch (_: Exception) {
        null
    }
}
