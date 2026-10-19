package com.example.stismartlib.network.dto

import com.google.gson.annotations.SerializedName

data class ClearanceRecordDto(
    val student: ClearanceStudentDto? = null,
    val status: String? = null,
    @SerializedName(value = "computed_status", alternate = ["computedStatus"])
    val computedStatus: String? = null,
    val reason: String? = null,
    @SerializedName(value = "checked_at", alternate = ["checkedAt"])
    val checkedAt: String? = null,
    val summary: ClearanceSummaryDto? = null,
    val loans: List<ClearanceLoanDto>? = null,
    val fines: List<ClearanceFineDto>? = null,
    @SerializedName(value = "lost_books", alternate = ["lostBooks"])
    val lostBooks: List<ClearanceLostBookDto>? = null,
    @SerializedName(value = "active_override", alternate = ["activeOverride"])
    val activeOverride: ClearanceOverrideDto? = null,
)

data class ClearanceStudentDto(
    @SerializedName(value = "user_id", alternate = ["userId"])
    val userId: Int? = null,
    @SerializedName(value = "school_id", alternate = ["schoolId"])
    val schoolId: String? = null,
    val name: String? = null,
    val program: String? = null,
    val section: String? = null,
    @SerializedName(value = "account_status", alternate = ["accountStatus"])
    val accountStatus: String? = null,
)

data class ClearanceSummaryDto(
    @SerializedName(value = "active_loans", alternate = ["activeLoans"])
    val activeLoans: Int? = null,
    @SerializedName(value = "unpaid_overdue_fines", alternate = ["unpaidOverdueFines"])
    val unpaidOverdueFines: Int? = null,
    @SerializedName(value = "unpaid_replacement_charges", alternate = ["unpaidReplacementCharges"])
    val unpaidReplacementCharges: Double? = null,
    @SerializedName(value = "total_outstanding", alternate = ["totalOutstanding"])
    val totalOutstanding: Double? = null,
    @SerializedName(value = "block_count", alternate = ["blockCount"])
    val blockCount: Int? = null,
)

data class ClearanceLoanDto(
    @SerializedName(value = "transaction_id", alternate = ["transactionId"])
    val transactionId: Int? = null,
    val title: String? = null,
    @SerializedName(value = "accession_number", alternate = ["accessionNumber"])
    val accessionNumber: String? = null,
    val status: String? = null,
    @SerializedName(value = "borrowed_at", alternate = ["borrowedAt"])
    val borrowedAt: String? = null,
    @SerializedName(value = "due_at", alternate = ["dueAt"])
    val dueAt: String? = null,
    @SerializedName(value = "overdue_hours", alternate = ["overdueHours"])
    val overdueHours: Int? = null,
    @SerializedName(value = "current_fine", alternate = ["currentFine"])
    val currentFine: Double? = null,
)

data class ClearanceFineDto(
    @SerializedName(value = "fine_id", alternate = ["fineId"])
    val fineId: Int? = null,
    @SerializedName(value = "transaction_id", alternate = ["transactionId"])
    val transactionId: Int? = null,
    val title: String? = null,
    val amount: Double? = null,
    val basis: String? = null,
    @SerializedName(value = "applied_at", alternate = ["appliedAt"])
    val appliedAt: String? = null,
    val notes: String? = null,
)

data class ClearanceLostBookDto(
    @SerializedName(value = "lost_book_report_id", alternate = ["lostBookReportId"])
    val lostBookReportId: Int? = null,
    @SerializedName(value = "transaction_id", alternate = ["transactionId"])
    val transactionId: Int? = null,
    val title: String? = null,
    val status: String? = null,
    @SerializedName(value = "replacement_charge", alternate = ["replacementCharge"])
    val replacementCharge: Double? = null,
    @SerializedName(value = "payment_status", alternate = ["paymentStatus"])
    val paymentStatus: String? = null,
    @SerializedName(value = "reported_at", alternate = ["reportedAt"])
    val reportedAt: String? = null,
)

data class ClearanceOverrideDto(
    @SerializedName(value = "override_id", alternate = ["overrideId"])
    val overrideId: Int? = null,
    val status: String? = null,
    val reason: String? = null,
    @SerializedName(value = "applied_at", alternate = ["appliedAt"])
    val appliedAt: String? = null,
    @SerializedName(value = "expires_at", alternate = ["expiresAt"])
    val expiresAt: String? = null,
    @SerializedName(value = "applied_by", alternate = ["appliedBy"])
    val appliedBy: String? = null,
)
