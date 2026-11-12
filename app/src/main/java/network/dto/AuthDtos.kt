package com.example.stismartlib.network.dto

import com.google.gson.annotations.SerializedName

data class ApiEnvelope<T>(
    val success: Boolean? = null,
    val message: String? = null,
    val code: String? = null,
    val data: T? = null,
    val details: ApiErrorDetails? = null,
    val meta: PageMetaDto? = null,
)

/** `meta` on list endpoints (catalog, research). */
data class PageMetaDto(
    val pagination: PaginationDto? = null,
    val viewer: CatalogViewerDto? = null,
)

data class PaginationDto(
    val page: Int? = null,
    val limit: Int? = null,
    val total: Int? = null,
    val totalPages: Int? = null,
)

data class ApiErrorDetails(
    val errors: Map<String, String>? = null,
)

data class LoginRequest(
    @SerializedName("school_id") val schoolId: String,
    val password: String,
    /** "user" for Student/Faculty, "staff" for Librarian/Admin. */
    val portal: String? = null,
    @SerializedName("login_as") val loginAs: String? = null,
)

/**
 * Response of `/auth/login` and `/auth/login/verify`. Staff roles get a token straight away;
 * Student/Faculty get `otpRequired` + `challengeId` and must verify the emailed code first.
 */
data class LoginData(
    val token: String? = null,
    val tokenType: String? = null,
    val expiresIn: Int? = null,
    val redirect: String? = null,
    val user: LoginUser? = null,
    val otpRequired: Boolean? = null,
    val challengeId: String? = null,
    val emailHint: String? = null,
    val resendAfter: Int? = null,
    /** Only sent when the server's device sign-in feature is on; the app cannot complete that flow. */
    val deviceSwitchRequired: Boolean? = null,
)

data class LoginVerifyRequest(
    @SerializedName("challenge_id") val challengeId: String,
    val code: String,
)

data class LoginResendRequest(
    @SerializedName("challenge_id") val challengeId: String,
)

data class LoginResendData(
    val message: String? = null,
    val resendAfter: Int? = null,
)

/** Response of `/auth/activity`: a renewed token for the same session. */
data class SessionActivityData(
    val token: String? = null,
    val idleSeconds: Int? = null,
)

data class LoginUser(
    val id: Int? = null,
    val accountId: Int? = null,
    val schoolId: String? = null,
    val fullName: String? = null,
    val email: String? = null,
    val role: String? = null,
)

/** `GET /auth/roster-registration`: whether Student account activation is open this semester. */
data class RosterStatusData(
    val enabled: Boolean? = null,
    val required: Boolean? = null,
    val open: Boolean? = null,
)

data class RosterBeginRequest(
    @SerializedName("school_id") val schoolId: String,
)

/** Always returned, even when the ID isn't on the roster (the server doesn't reveal it). */
data class RosterChallengeData(
    val challengeId: String? = null,
)

data class RosterVerifyRequest(
    @SerializedName("challenge_id") val challengeId: String,
    val code: String,
)

data class RosterGrantData(
    val grant: String? = null,
)

data class RosterCompleteRequest(
    @SerializedName("challenge_id") val challengeId: String,
    val grant: String,
    val password: String,
    @SerializedName("confirm_password") val confirmPassword: String,
)

data class RosterCompleteData(
    val status: String? = null,
    val schoolId: String? = null,
)

data class MeUser(
    val id: Int? = null,
    val accountId: Int? = null,
    val schoolId: String? = null,
    val fullName: String? = null,
    val email: String? = null,
    val role: String? = null,
)

/** `/auth/password-recovery/request`. `portal: "staff"` (desk staff) is not used by the app yet. */
data class PasswordRecoveryRequest(
    val identifier: String,
    val portal: String? = null,
)

/** Always returned, whether or not an account matched (the server does not reveal accounts). */
data class PasswordRecoveryChallengeData(
    val challengeId: String? = null,
    val message: String? = null,
)

data class PasswordRecoveryVerifyRequest(
    @SerializedName("challenge_id") val challengeId: String,
    val code: String,
)

data class PasswordRecoveryTokenData(
    val resetToken: String? = null,
    val expiresIn: Int? = null,
)

data class PasswordResetRequest(
    @SerializedName("challenge_id") val challengeId: String,
    @SerializedName("reset_token") val resetToken: String,
    val password: String,
    @SerializedName("confirm_password") val confirmPassword: String,
)

data class PasswordResetData(
    val message: String? = null,
)
