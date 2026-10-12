package com.example.stismartlib.ui.feedback

import com.example.stismartlib.network.ApiException
import java.io.IOException

/**
 * Maps exceptions to user-safe copy. Never exposes stack traces, raw HTTP bodies,
 * SQL, internal paths, or server codes.
 */
fun userFacingMessage(
    error: Throwable,
    fallback: String = "Something went wrong. Please try again."
): String {
    return when (error) {
        is ApiException -> when (error.code) {
            "INVALID_CREDENTIALS" ->
                "Invalid student ID or password."
            "ACCOUNT_DEACTIVATED" ->
                "Your account is currently deactivated. Please coordinate with the campus librarian."
            "TOO_MANY_LOGIN_ATTEMPTS" ->
                "Too many login attempts. Please wait 15 minutes and try again."
            "ADMIN_ROLE_REQUIRED" ->
                "This sign-in is for Librarian / Admin accounts only."
            else -> sanitizeApiMessage(error.message) ?: fallback
        }
        is IOException ->
            "Unable to reach the library service. Check your internet connection and try again."
        else -> fallback
    }
}

private fun sanitizeApiMessage(raw: String?): String? {
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
