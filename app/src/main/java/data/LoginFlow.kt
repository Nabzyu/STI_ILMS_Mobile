package com.example.stismartlib.data

import com.example.stismartlib.network.ApiException
import com.example.stismartlib.network.dto.LoginData

/** Emailed-code challenge returned by `/auth/login` for Student and Faculty accounts. */
data class LoginChallenge(
    val challengeId: String,
    val emailHint: String,
    val expiresInSeconds: Int,
    val resendAfterSeconds: Int,
)

/** What a sign-in step produced: either a code to enter, or a usable session. */
sealed interface LoginStep {
    data class CodeRequired(val challenge: LoginChallenge) : LoginStep
    data class Authenticated(
        val token: String,
        val accountId: Int,
        val schoolId: String,
        val fullName: String,
        val role: String,
    ) : LoginStep
}

/** Result the login screen acts on. */
sealed interface LoginOutcome {
    data object SignedIn : LoginOutcome
    data class CodeRequired(val challenge: LoginChallenge) : LoginOutcome
}

/**
 * Turns a successful `/auth/login` or `/auth/login/verify` payload into the next step.
 * Throws [ApiException] when the payload cannot be used.
 */
fun interpretLoginData(data: LoginData, requestedSchoolId: String): LoginStep {
    if (data.otpRequired == true) {
        val challengeId = data.challengeId?.takeIf { it.isNotBlank() }
            ?: throw ApiException("The server returned an incomplete login response.", "INVALID_LOGIN_RESPONSE")
        return LoginStep.CodeRequired(
            LoginChallenge(
                challengeId = challengeId,
                emailHint = data.emailHint?.takeIf { it.isNotBlank() } ?: "your school email",
                expiresInSeconds = data.expiresIn ?: 600,
                resendAfterSeconds = data.resendAfter ?: 60,
            ),
        )
    }
    if (data.deviceSwitchRequired == true) {
        throw ApiException(
            "This account is signed in on another device. Sign out there, or switch devices from the library website.",
            "DEVICE_SWITCH_REQUIRED",
        )
    }
    val token = data.token?.takeIf { it.isNotBlank() }
        ?: throw ApiException("The server returned an invalid access token.", "INVALID_TOKEN_RESPONSE")
    val user = data.user ?: throw ApiException("The server returned an incomplete login response.", "INVALID_LOGIN_RESPONSE")
    val accountId = user.accountId ?: user.id
        ?: throw ApiException("The server returned an incomplete login response.", "INVALID_LOGIN_RESPONSE")
    val schoolId = user.schoolId?.takeIf { it.isNotBlank() } ?: requestedSchoolId
    return LoginStep.Authenticated(
        token = token,
        accountId = accountId,
        schoolId = schoolId,
        fullName = user.fullName?.takeIf { it.isNotBlank() } ?: schoolId,
        role = user.role.orEmpty(),
    )
}

/** Password recovery in progress; `message` is the server's neutral "if an account matches…" text. */
data class PasswordResetChallenge(
    val challengeId: String,
    val message: String?,
)
