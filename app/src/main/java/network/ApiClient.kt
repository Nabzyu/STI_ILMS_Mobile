package com.example.stismartlib.network

import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.session.SessionStore
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
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
        val rawMessage = errorEnvelope?.message ?: body?.message
        val message = sanitizeUserMessage(rawMessage)
            ?: when (response.code()) {
                401 -> "Invalid student ID or password."
                403 -> "Your account is currently deactivated. Please coordinate with the campus librarian."
                429 -> "Too many login attempts. Please wait 15 minutes and try again."
                else -> "The request could not be completed."
            }
        val code = errorEnvelope?.code
            ?: body?.code
            ?: when (response.code()) {
                429 -> "TOO_MANY_LOGIN_ATTEMPTS"
                401 -> "INVALID_CREDENTIALS"
                else -> "REQUEST_FAILED"
            }
        val fieldErrors = errorEnvelope?.details?.errors ?: body?.details?.errors ?: emptyMap()
        if (::sessionStore.isInitialized && code != "INVALID_CREDENTIALS") {
            val shouldClear = code in setOf(
                "ACCOUNT_DEACTIVATED",
                "ACCOUNT_ACCESS_REVOKED",
                "JWT_REQUIRED",
                "JWT_INVALID",
            )
            if (shouldClear) sessionStore.clear()
        }
        throw ApiException(message, code, fieldErrors, response.code())
    }

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

    /** Drop messages that look like stack traces, HTML, or internal server dump. */
    private fun sanitizeUserMessage(raw: String?): String? {
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
