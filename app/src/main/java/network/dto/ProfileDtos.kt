package com.example.stismartlib.network.dto

import com.google.gson.annotations.SerializedName

data class ProfileMeDto(
    val id: Int? = null,
    @SerializedName("account_id") val accountId: Int? = null,
    @SerializedName("school_id") val schoolId: String? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    val email: String? = null,
    val role: String? = null,
    @SerializedName("program_strand") val programStrand: String? = null,
    @SerializedName("year_grade_level") val yearGradeLevel: String? = null,
    val status: String? = null,
)

data class ProfileUpdateRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("program_strand") val programStrand: String? = null,
    @SerializedName("year_grade_level") val yearGradeLevel: String? = null,
)

data class ProfileAvatarDto(
    val status: String? = null,
    val url: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("signed_url") val signedUrl: String? = null,
    @SerializedName("rejection_reason") val rejectionReason: String? = null,
    val message: String? = null,
) {
    fun resolvedUrl(): String? =
        url?.takeIf { it.isNotBlank() }
            ?: avatarUrl?.takeIf { it.isNotBlank() }
            ?: signedUrl?.takeIf { it.isNotBlank() }
}
