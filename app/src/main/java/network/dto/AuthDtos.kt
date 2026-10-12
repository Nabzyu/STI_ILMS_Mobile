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

data class RegisterRequest(
    @SerializedName("school_id") val schoolId: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("contact_number") val contactNumber: String,
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
    val schoolId: String? = null,
    val role: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
)

data class MeUser(
    val id: Int? = null,
    val accountId: Int? = null,
    val schoolId: String? = null,
    val fullName: String? = null,
    val email: String? = null,
    val role: String? = null,
)
