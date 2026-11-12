package com.example.stismartlib.network

/** Server codes meaning the stored session can no longer be used; the user must sign in again. */
val SessionEndingCodes: Set<String> = setOf(
    "SESSION_EXPIRED",
    "SESSION_UPGRADE_REQUIRED",
    "JWT_REQUIRED",
    "JWT_INVALID",
    "ACCOUNT_ACCESS_REVOKED",
    "ACCOUNT_DEACTIVATED",
)

/** Sign-in endpoints: sent without a bearer token, and their errors never end a session. */
fun isAuthHandshakePath(encodedPath: String): Boolean =
    encodedPath.endsWith("/auth/login") ||
        encodedPath.endsWith("/auth/login/verify") ||
        encodedPath.endsWith("/auth/login/resend") ||
        encodedPath.endsWith("/auth/register") ||
        encodedPath.contains("/auth/password-recovery/")

/**
 * Picks the error code for a failed response. A 401 without a code means a wrong password on
 * sign-in, but an ended session anywhere else (`/auth/activity` sends a bare 401 when it is too late).
 */
fun resolveErrorCode(serverCode: String?, httpStatus: Int, isAuthHandshake: Boolean): String =
    serverCode?.takeIf { it.isNotBlank() } ?: when (httpStatus) {
        429 -> "TOO_MANY_LOGIN_ATTEMPTS"
        401 -> if (isAuthHandshake) "INVALID_CREDENTIALS" else "SESSION_EXPIRED"
        else -> "REQUEST_FAILED"
    }

fun endsSession(code: String, isAuthHandshake: Boolean): Boolean =
    !isAuthHandshake && code in SessionEndingCodes

/** Client-side reason: the app saw no interaction for 10 minutes and signed the user out itself. */
const val SESSION_IDLE_CODE = "SESSION_IDLE"
