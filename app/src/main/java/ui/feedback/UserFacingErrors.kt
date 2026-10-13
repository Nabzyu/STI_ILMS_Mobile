package com.example.stismartlib.ui.feedback

import com.example.stismartlib.network.ApiException
import java.io.IOException

private const val SESSION_EXPIRED = "Your session expired. Please sign in again."
private const val SIGN_IN_INCOMPLETE = "Sign-in could not be completed. Please try again."

/**
 * Maps exceptions to user-safe copy. Never exposes stack traces, raw HTTP bodies,
 * SQL, internal paths, or server codes.
 */
fun userFacingMessage(
    error: Throwable,
    fallback: String = "Something went wrong. Please try again.",
): String {
    return when (error) {
        is ApiException -> mapApiException(error, fallback)
        is IOException ->
            "Unable to reach the library service. Check your internet connection and try again."
        else -> fallback
    }
}

private fun mapApiException(error: ApiException, fallback: String): String {
    when (error.code) {
        "INVALID_CREDENTIALS" -> return "Invalid school ID or password."
        "ACCOUNT_DEACTIVATED" ->
            return "Your account is currently deactivated. Please coordinate with the campus librarian."
        "TOO_MANY_LOGIN_ATTEMPTS" ->
            return "Too many login attempts. Please wait 15 minutes and try again."
        "ADMIN_ROLE_REQUIRED" ->
            return "This sign-in is for Librarian / Admin accounts only."
        "JWT_REQUIRED", "JWT_INVALID", "ACCOUNT_ACCESS_REVOKED" -> return SESSION_EXPIRED
        "JWT_ROLE_FORBIDDEN" -> return "You do not have access to this action."
        "AUTH_VALIDATION_FAILED" -> return "Please check the highlighted fields."
        "INVALID_TOKEN_RESPONSE" -> return SIGN_IN_INCOMPLETE
    }

    val raw = error.message.orEmpty()
    val lower = raw.lowercase()
    if (lower.contains("route not found")) {
        return "This feature is not available right now."
    }
    if (
        lower.contains("bearer") ||
        lower.contains("authorization") ||
        (lower.contains("token") && (lower.contains("required") || lower.contains("invalid") || lower.contains("expired")))
    ) {
        return SESSION_EXPIRED
    }
    if (
        lower.contains("missing account") ||
        lower.contains("incomplete login") ||
        lower.contains("invalid access token")
    ) {
        return SIGN_IN_INCOMPLETE
    }
    if (lower.contains("invalid book id")) {
        return "This book could not be reserved."
    }
    if (lower.contains("invalid reservation id")) {
        return "This reservation could not be updated."
    }

    return sanitizeApiMessage(error.message) ?: fallback
}

/** Light filter for developer / dump-like strings. Returns null when unsafe. */
fun sanitizeApiMessage(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val lower = raw.lowercase()
    val looksInternal =
        lower.contains("exception") ||
            lower.contains("stack") ||
            lower.contains("sql") ||
            lower.contains("at com.") ||
            lower.contains("traceback") ||
            lower.contains("bearer") ||
            lower.contains("unauthorized") ||
            lower.contains("authorization") ||
            lower.contains("{") ||
            lower.contains("</") ||
            Regex("""\b(500|502|503|504)\b""").containsMatchIn(raw)
    return if (looksInternal) null else raw.trim()
}
