package com.example.stismartlib.network.dto

import com.google.gson.annotations.SerializedName

data class ApiEnvelope<T>(
    val success: Boolean? = null,
    val message: String? = null,
    val code: String? = null,
    val data: T? = null,
    val details: ApiErrorDetails? = null,
    val meta: Any? = null,
)

data class ApiErrorDetails(
    val errors: Map<String, String>? = null,
)

data class LoginRequest(
    @SerializedName("school_id") val schoolId: String,
    val password: String,
    /** "user" for Student/Faculty, "staff" for Librarian/Admin. The server resolves the role. */
    val portal: String,
)

data class LoginVerifyRequest(
    @SerializedName("challenge_id") val challengeId: String,
    val code: String,
)

data class LoginResendRequest(
    @SerializedName("challenge_id") val challengeId: String,
)

/** `POST auth/activity`: a fresh token for the same session plus the idle window. */
data class SessionActivityDto(
    val token: String? = null,
    @SerializedName(value = "idle_seconds", alternate = ["idleSeconds"])
    val idleSeconds: Int? = null,
)

data class LoginResendData(
    val message: String? = null,
    @SerializedName(value = "resend_after", alternate = ["resendAfter"])
    val resendAfter: Int? = null,
)

data class LoginData(
    val token: String? = null,
    @SerializedName(value = "token_type", alternate = ["tokenType"])
    val tokenType: String? = null,
    @SerializedName(value = "expires_in", alternate = ["expiresIn"])
    val expiresIn: Int? = null,
    val redirect: String? = null,
    val user: LoginUser? = null,
    /** Student/Faculty sign-in returns an email code challenge instead of a token. */
    @SerializedName(value = "otp_required", alternate = ["otpRequired"])
    val otpRequired: Boolean? = null,
    @SerializedName(value = "challenge_id", alternate = ["challengeId"])
    val challengeId: String? = null,
    @SerializedName(value = "email_hint", alternate = ["emailHint"])
    val emailHint: String? = null,
    @SerializedName(value = "resend_after", alternate = ["resendAfter"])
    val resendAfter: Int? = null,
)

data class LoginUser(
    val id: Int? = null,
    @SerializedName(value = "account_id", alternate = ["accountId"])
    val accountId: Int? = null,
    @SerializedName(value = "school_id", alternate = ["schoolId"])
    val schoolId: String? = null,
    @SerializedName(value = "full_name", alternate = ["fullName"])
    val fullName: String? = null,
    @SerializedName(value = "first_name", alternate = ["firstName"])
    val firstName: String? = null,
    @SerializedName(value = "last_name", alternate = ["lastName"])
    val lastName: String? = null,
    val email: String? = null,
    val role: String? = null,
)

data class RegisterRequest(
    @SerializedName("school_id") val schoolId: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("school_email") val schoolEmail: String,
    @SerializedName("program_strand") val programStrand: String,
    @SerializedName("year_grade_level") val yearGradeLevel: String,
    val password: String,
    @SerializedName("confirm_password") val confirmPassword: String,
)

/** Registration creates a pending request; the account exists only after email code + librarian approval. */
data class RegisterData(
    @SerializedName(value = "request_id", alternate = ["requestId"])
    val requestId: Int? = null,
    @SerializedName(value = "school_id", alternate = ["schoolId"])
    val schoolId: String? = null,
    val email: String? = null,
    /** "PendingEmail" after register; "PendingApproval" after verify. */
    val status: String? = null,
    val role: String? = null,
)

data class RegisterVerifyRequest(
    @SerializedName("school_id") val schoolId: String,
    val code: String,
)

data class RegisterResendRequest(
    @SerializedName("school_id") val schoolId: String,
)

/** Password recovery: `identifier` is a School ID or school email. */
data class RecoveryRequest(
    val identifier: String,
)

/** Always neutral: returned even when no account matches. */
data class RecoveryStartDto(
    @SerializedName(value = "challenge_id", alternate = ["challengeId"])
    val challengeId: String? = null,
    val message: String? = null,
)

data class RecoveryVerifyRequest(
    @SerializedName("challenge_id") val challengeId: String,
    val code: String,
)

data class RecoveryVerifyDto(
    @SerializedName(value = "reset_token", alternate = ["resetToken"])
    val resetToken: String? = null,
)

data class RecoveryResetRequest(
    @SerializedName("challenge_id") val challengeId: String,
    @SerializedName("reset_token") val resetToken: String,
    val password: String,
    @SerializedName("confirm_password") val confirmPassword: String,
)

data class RecoveryDoneDto(
    val message: String? = null,
)

data class MeUser(
    val id: Int? = null,
    @SerializedName(value = "account_id", alternate = ["accountId"])
    val accountId: Int? = null,
    @SerializedName(value = "school_id", alternate = ["schoolId"])
    val schoolId: String? = null,
    @SerializedName(value = "full_name", alternate = ["fullName"])
    val fullName: String? = null,
    val email: String? = null,
    val role: String? = null,
)
