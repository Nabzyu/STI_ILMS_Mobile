package com.example.stismartlib.network.dto

import com.google.gson.annotations.SerializedName

data class BookCatalogItemDto(
    val titleId: Int? = null,
    val title: String? = null,
    val author: String? = null,
    val isbn: String? = null,
    val publisher: String? = null,
    val publicationYear: Int? = null,
    val categoryId: Int? = null,
    val categoryName: String? = null,
    val callNumber: String? = null,
    val coverImagePath: String? = null,
    val shelfLocation: String? = null,
    val currentAvailabilityStatus: String? = null,
    val totalCopiesCount: Int? = null,
    val availableCopiesCount: Int? = null,
    val reservableMaterialId: Int? = null,
    val previewBarcode: String? = null,
)

data class ResearchCatalogItemDto(
    val titleId: Int? = null,
    val researchRecordId: Int? = null,
    val researchCode: String? = null,
    val title: String? = null,
    val authors: String? = null,
    val adviser: String? = null,
    val department: String? = null,
    val publicationYear: Int? = null,
    val shelfLocation: String? = null,
    @SerializedName("abstract") val abstractText: String? = null,
    val keywords: String? = null,
    val accessStatus: String? = null,
)

data class BorrowingHistoryDataDto(
    val summary: BorrowingSummaryDto? = null,
    val items: List<BorrowingItemDto>? = null,
)

data class BorrowingSummaryDto(
    val activeLoans: Int? = null,
    val activeReservations: Int? = null,
)

data class BorrowingItemDto(
    val transactionId: Int? = null,
    val titleId: Int? = null,
    val title: String? = null,
    val author: String? = null,
    val borrowDate: String? = null,
    val dueDate: String? = null,
    val returnDate: String? = null,
    val status: String? = null,
)

data class ReservationItemDto(
    val reservationId: Int? = null,
    val titleId: Int? = null,
    val title: String? = null,
    val author: String? = null,
    val queuePosition: Int? = null,
    val status: String? = null,
    val reservedAt: String? = null,
    val pickupDeadline: String? = null,
)

data class ReserveResultDto(
    val reservationId: Int? = null,
    val queuePosition: Int? = null,
    val status: String? = null,
)

data class FineListDto(
    val items: List<FineObligationDto>? = null,
)

data class FineObligationDto(
    val id: String? = null,
    val fineId: Int? = null,
    val type: String? = null,
    val title: String? = null,
    val reason: String? = null,
    val assessed: Double? = null,
    val balance: Double? = null,
    val status: String? = null,
    val occurredAt: String? = null,
)

data class NotificationListDto(
    val items: List<NotificationItemDto>? = null,
    val unreadCount: Int? = null,
)

data class NotificationItemDto(
    val notificationId: Int? = null,
    val title: String? = null,
    val body: String? = null,
    val type: String? = null,
    val priority: String? = null,
    val isRead: Boolean? = null,
    val createdAt: String? = null,
)

data class PrintRequestDto(
    @SerializedName("request_id") val requestId: Int? = null,
    @SerializedName("file_name") val fileName: String? = null,
    @SerializedName("number_of_copies") val numberOfCopies: Int? = null,
    @SerializedName("print_type") val printType: String? = null,
    @SerializedName("paper_size") val paperSize: String? = null,
    @SerializedName("calculated_cost") val calculatedCost: Double? = null,
    @SerializedName("payment_status") val paymentStatus: String? = null,
    @SerializedName("job_status") val jobStatus: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
)

data class BorrowSubmitRequest(
    @SerializedName("title_ids") val titleIds: List<Int>,
)

data class BorrowRequestReceiptDto(
    val requestGroupId: String? = null,
    val status: String? = null,
    val instructions: String? = null,
)

data class AttendancePassDto(
    val profile: AttendanceProfileDto? = null,
    val summary: AttendanceSummaryDto? = null,
)

data class AttendanceProfileDto(
    val userId: Int? = null,
    val schoolId: String? = null,
    val name: String? = null,
    val role: String? = null,
    val program: String? = null,
    val section: String? = null,
)

data class AttendanceSummaryDto(
    val visitsThisMonth: Int? = null,
    val todayCheckIn: String? = null,
    val commonPurpose: String? = null,
)
