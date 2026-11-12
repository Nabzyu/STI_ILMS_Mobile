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
    /** "user" for Student/Faculty, "staff" for Librarian/Admin. */
    val portal: String? = null,
    @SerializedName("login_as") val loginAs: String? = null,
)

data class LoginData(
    val token: String? = null,
    val tokenType: String? = null,
    val expiresIn: Int? = null,
    val redirect: String? = null,
    val user: LoginUser? = null,
)

data class LoginUser(
    val id: Int? = null,
    val accountId: Int? = null,
    val schoolId: String? = null,
    val fullName: String? = null,
    val email: String? = null,
    val role: String? = null,
)

/** Step 1 of roster sign-up: the server emails a code to the school email on the enrolment roster. */
data class RosterBeginRequest(
    @SerializedName("school_id") val schoolId: String,
)

/** Always returned, even when the ID is not eligible, so anonymous callers learn nothing about the roster. */
data class RosterBeginData(
    val challengeId: String? = null,
)

data class RosterVerifyRequest(
    @SerializedName("challenge_id") val challengeId: String,
    val code: String,
)

data class RosterVerifyData(
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
