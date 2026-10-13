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
    @SerializedName("account_status") val accountStatus: String? = null,
)

data class ProfileUpdateRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("program_strand") val programStrand: String? = null,
    @SerializedName("year_grade_level") val yearGradeLevel: String? = null,
)

data class ProfileAvatarPendingDto(
    val id: Int? = null,
    @SerializedName("submittedAt") val submittedAt: String? = null,
)

data class ProfileAvatarRejectionDto(
    @SerializedName("reviewedAt") val reviewedAt: String? = null,
    val reason: String? = null,
)

data class ProfileAvatarDto(
    val status: String? = null,
    val url: String? = null,
    @SerializedName("currentUrl") val currentUrl: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("signed_url") val signedUrl: String? = null,
    @SerializedName("rejection_reason") val rejectionReason: String? = null,
    val pending: ProfileAvatarPendingDto? = null,
    val lastRejection: ProfileAvatarRejectionDto? = null,
    val message: String? = null,
) {
    fun resolvedUrl(): String? =
        currentUrl?.takeIf { it.isNotBlank() }
            ?: url?.takeIf { it.isNotBlank() }
            ?: avatarUrl?.takeIf { it.isNotBlank() }
            ?: signedUrl?.takeIf { it.isNotBlank() }

    fun resolvedStatus(): String? = when {
        !status.isNullOrBlank() -> status
        pending != null -> "Pending"
        !lastRejection?.reason.isNullOrBlank() && resolvedUrl().isNullOrBlank() -> "Rejected"
        !resolvedUrl().isNullOrBlank() -> "Approved"
        else -> null
    }

    fun resolvedRejectionReason(): String? =
        rejectionReason?.takeIf { it.isNotBlank() }
            ?: lastRejection?.reason?.takeIf { it.isNotBlank() }
}
