package com.example.stismartlib.ui.feedback

import com.example.stismartlib.network.ApiException
import com.example.stismartlib.network.SESSION_IDLE_CODE
import com.example.stismartlib.network.SessionEndingCodes
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
                "Invalid school ID or password." + remainingAttemptsSuffix(error.message)
            "LOGIN_PAUSED" ->
                pausedMessage(error.message)
                    ?: "Too many wrong attempts. Sign-in is paused for 10 minutes."
            "TOO_MANY_LOGIN_ATTEMPTS" ->
                "Too many requests. Please wait a few minutes and try again."
            "ACCOUNT_DEACTIVATED" ->
                "Your account is currently deactivated. Please coordinate with the campus librarian."
            "STAFF_ACCOUNT_NOT_SUPPORTED" ->
                "Librarian and library staff accounts can't use the app yet. Please use the library website."
            "LOGIN_EMAIL_MISSING" ->
                "Your school email is not linked to this account. Please contact the librarian."
            "LOGIN_CHANGED" ->
                "Your account access changed. Please start sign-in again."
            "LOGIN_CODE_WAIT" ->
                "Please wait a minute before requesting another login code."
            "LOGIN_CODE_INVALID" ->
                "The code is incorrect, expired or already used. Use the newest code or start sign-in again."
            "LOGIN_CODE_PAUSED" ->
                "Three incorrect codes were entered. Wait 10 minutes before signing in again."
            "RECOVERY_CODE_INVALID" ->
                "The code is incorrect, expired or already used. Use the newest code or request a new one."
            "RECOVERY_LOCKED" ->
                "Three incorrect codes were entered. Wait 15 minutes before trying again. Your current password still works."
            "RECOVERY_TOKEN_INVALID" ->
                "This password reset has expired or was already used. Please start again."
            "PASSWORD_UNCHANGED" ->
                "Choose a password different from your current one."
            "RECOVERY_RATE_LIMITED" ->
                "Too many password reset attempts. Please wait 15 minutes and try again."
            "DEVICE_ORIGIN" ->
                "Sign-in from the app is not available right now. Please use the library website."
            in SessionEndingCodes, SESSION_IDLE_CODE ->
                sessionEndedMessage(error.code)
            else -> sanitizeApiMessage(error.message) ?: fallback
        }
        is IOException ->
            "Unable to reach the library service. Check your internet connection and try again."
        else -> fallback
    }
}

/** Copy for the login screen after a session ended without the user signing out. */
fun sessionEndedMessage(code: String): String = when (code) {
    SESSION_IDLE_CODE ->
        "You were signed out after 10 minutes of inactivity. Please sign in again."
    "SESSION_EXPIRED" ->
        "Your session has ended. Please sign in again."
    "ACCOUNT_ACCESS_REVOKED" ->
        "Your account access changed or you signed in on another device. Please sign in again."
    "ACCOUNT_DEACTIVATED" ->
        "Your account is currently deactivated. Please coordinate with the campus librarian."
    else ->
        "Please sign in again to continue."
}

/** "Wrong credentials. 2 attempts remaining." → " 2 attempts remaining." */
private fun remainingAttemptsSuffix(raw: String?): String {
    val count = raw?.let { Regex("""(\d+) attempts? remaining""").find(it) }?.groupValues?.get(1)
        ?.toIntOrNull() ?: return ""
    return if (count == 1) " 1 attempt remaining." else " $count attempts remaining."
}

/** Keeps the server's "Try again in N minutes." wording, which is the only part that varies. */
private fun pausedMessage(raw: String?): String? {
    val minutes = raw?.let { Regex("""(\d+) minutes?""").find(it) }?.groupValues?.get(1)?.toIntOrNull()
        ?: return null
    val unit = if (minutes == 1) "minute" else "minutes"
    return "Too many wrong attempts. Sign-in is paused. Try again in $minutes $unit."
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
