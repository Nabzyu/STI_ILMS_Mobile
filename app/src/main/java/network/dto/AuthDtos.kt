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
    /** API role: "Student", "Faculty", or "Admin". */
    @SerializedName("login_as") val loginAs: String,
    /** Legacy portal hint; "user" for Student/Faculty, "staff" for Admin. */
    val portal: String? = null,
)

data class LoginData(
    val token: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
    @SerializedName("expires_in") val expiresIn: Int? = null,
    val redirect: String? = null,
    val user: LoginUser? = null,
)

data class LoginUser(
    val id: Int? = null,
    @SerializedName("account_id") val accountId: Int? = null,
    @SerializedName("school_id") val schoolId: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    val email: String? = null,
    val role: String? = null,
)

data class RegisterRequest(
    @SerializedName("school_id") val schoolId: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val email: String,
    @SerializedName("program_strand") val programStrand: String,
    @SerializedName("year_grade_level") val yearGradeLevel: String,
    val password: String,
    @SerializedName("confirm_password") val confirmPassword: String,
)

data class RegisterData(
    val account: RegisterAccount? = null,
)

data class RegisterAccount(
    val id: Int? = null,
    @SerializedName("school_id") val schoolId: String? = null,
    val role: String? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
)

data class MeUser(
    val id: Int? = null,
    @SerializedName("account_id") val accountId: Int? = null,
    @SerializedName("school_id") val schoolId: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    val email: String? = null,
    val role: String? = null,
)
