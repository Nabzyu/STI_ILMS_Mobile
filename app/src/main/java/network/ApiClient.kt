package com.example.stismartlib.network

import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.session.SessionStore
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private lateinit var sessionStore: SessionStore
    private val gson = Gson()

    val api: LibraryApi by lazy { retrofit.create(LibraryApi::class.java) }

    fun initialize(store: SessionStore) {
        sessionStore = store
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val path = original.url.encodedPath
        val isAuthHandshake = path.endsWith("/auth/login") || path.endsWith("/auth/register")
        val token = if (!isAuthHandshake && ::sessionStore.isInitialized) sessionStore.getToken() else null
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build()
        } else {
            original.newBuilder()
                .header("Accept", "application/json")
                .removeHeader("Authorization")
                .build()
        }
        chain.proceed(request)
    }

    private val okHttp: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.baseUrl)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun <T> unwrap(response: Response<ApiEnvelope<T>>): T {
        val body = response.body()
        if (response.isSuccessful && body?.success == true && body.data != null) {
            return body.data
        }
        val errorEnvelope = parseError(response)
        val serverCode = errorEnvelope?.code ?: body?.code
        val rawMessage = errorEnvelope?.message ?: body?.message
        // Strip dumps/HTML only; meaning rewrites happen in userFacingMessage.
        val lightMessage = sanitizeTransportMessage(rawMessage)
        val code = serverCode
            ?: when (response.code()) {
                429 -> "TOO_MANY_LOGIN_ATTEMPTS"
                401 -> "JWT_REQUIRED"
                403 -> "JWT_ROLE_FORBIDDEN"
                else -> "REQUEST_FAILED"
            }
        val message = when {
            serverCode == "INVALID_CREDENTIALS" ->
                lightMessage ?: "Invalid school ID or password."
            serverCode == "TOO_MANY_LOGIN_ATTEMPTS" ->
                lightMessage ?: "Too many login attempts. Please wait 15 minutes and try again."
            !lightMessage.isNullOrBlank() -> lightMessage
            else -> when (response.code()) {
                401 -> "Sign-in is required."
                403 -> "You do not have access to this action."
                404 -> "This feature is not available right now."
                429 -> "Too many requests. Please wait and try again."
                else -> "The request could not be completed."
            }
        }
        val fieldErrors = errorEnvelope?.details?.errors ?: body?.details?.errors ?: emptyMap()
        // Only a request that carried a token can end a session; login/code steps send none.
        val sentToken = response.raw().request.header("Authorization") != null
        if (::sessionStore.isInitialized && sentToken && isSessionEndCode(code)) {
            sessionStore.clear()
            _sessionEnded.value = true
        }
        throw ApiException(message, code, fieldErrors, response.code())
    }

    private val _sessionEnded = MutableStateFlow(false)

    /** True after a signed-in request was rejected because the session is over. */
    val sessionEnded: StateFlow<Boolean> = _sessionEnded

    fun acknowledgeSessionEnded() {
        _sessionEnded.value = false
    }

    fun isSessionEndCode(code: String): Boolean = code in SESSION_END_CODES

    private val SESSION_END_CODES = setOf(
        "SESSION_EXPIRED",
        "SESSION_UPGRADE_REQUIRED",
        "JWT_REQUIRED",
        "JWT_INVALID",
        "ACCOUNT_ACCESS_REVOKED",
        "ACCOUNT_DEACTIVATED",
    )

    private fun parseError(response: Response<*>): ApiEnvelope<Any?>? {
        val raw = response.errorBody()?.string().orEmpty()
        if (raw.isBlank()) return null
        return try {
            val type = object : TypeToken<ApiEnvelope<Any?>>() {}.type
            gson.fromJson<ApiEnvelope<Any?>>(raw, type)
        } catch (_: JsonSyntaxException) {
            null
        }
    }

    /** Drop stack traces, HTML, and JSON dumps. Keep auth wording for display mapping. */
    private fun sanitizeTransportMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val lower = raw.lowercase()
        val looksInternal =
            lower.contains("exception") ||
                lower.contains("stack") ||
                lower.contains("sql") ||
                lower.contains("at com.") ||
                lower.contains("traceback") ||
                lower.contains("{") ||
                lower.contains("</")
        return if (looksInternal) null else raw.trim()
    }
}
