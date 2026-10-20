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
    @SerializedName(value = "callNumber", alternate = ["call_number"])
    val callNumber: String? = null,
    val coverImagePath: String? = null,
    @SerializedName(value = "shelfLocation", alternate = ["shelf_location"])
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
    @SerializedName(value = "shelfLocation", alternate = ["shelf_location"])
    val shelfLocation: String? = null,
    @SerializedName("abstract") val abstractText: String? = null,
    val keywords: String? = null,
    val accessStatus: String? = null,
)

data class BorrowingHistoryDataDto(
    val summary: BorrowingSummaryDto? = null,
    val items: List<BorrowingItemDto>? = null,
    val pagination: PaginationDto? = null,
)

data class BorrowingSummaryDto(
    val activeLoans: Int? = null,
    val activeReservations: Int? = null,
)

/** `GET catalog/copies/{barcode}`: one physical copy; `barcodeImageData` is an SVG data URI. */
data class CatalogCopyDto(
    val accessionNumber: String? = null,
    val barcode: String? = null,
    val shelfLocation: String? = null,
    val conditionStatus: String? = null,
    val barcodeImageData: String? = null,
)

/** `GET dashboard` (Student/Faculty); only the fields the app shows. */
data class UserDashboardDto(
    val occupancy: OccupancyDto? = null,
    val announcement: AnnouncementDto? = null,
)

data class OccupancyDto(val current: Int? = null, val capacity: Int? = null)

data class AnnouncementDto(
    val id: Long? = null,
    val title: String? = null,
    val message: String? = null,
    val priority: String? = null,
    val publishedAt: String? = null,
)

/** `GET borrow/cart-state`: titles with an active loan or request. */
data class CartStateDto(val activeTitleIds: List<Long>? = null)

/** `GET borrowing/my-books`: the same row shape as borrowing history. */
data class MyBooksDto(val items: List<BorrowingItemDto>? = null)

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

data class ReserveRequestDto(
    @SerializedName("bookTitleId") val bookTitleId: Int,
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

/** `GET fines/receipts` row: a cash payment recorded at the library counter. */
data class FineReceiptDto(
    val receiptId: Long? = null,
    val receiptNumber: String? = null,
    val amountReceived: Double? = null,
    val paymentMethod: String? = null,
    val receivedAt: String? = null,
    val status: String? = null,
)

/** `GET notifications/schedule`. Days are 1 = Monday … 7 = Sunday; times look like "08:00:00". */
data class LibraryScheduleDto(
    val timezone: String? = null,
    val weekly: List<WeeklyHoursDto>? = null,
    val upcomingClosures: List<LibraryClosureDto>? = null,
)

data class WeeklyHoursDto(
    val dayOfWeek: Int? = null,
    val isOpen: Boolean? = null,
    val opensAt: String? = null,
    val closesAt: String? = null,
)

data class LibraryClosureDto(
    val date: String? = null,
    val reason: String? = null,
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
    @SerializedName(value = "request_id", alternate = ["requestId"])
    val requestId: Int? = null,
    @SerializedName(value = "file_name", alternate = ["fileName"])
    val fileName: String? = null,
    @SerializedName(value = "number_of_copies", alternate = ["numberOfCopies"])
    val numberOfCopies: Int? = null,
    @SerializedName(value = "print_type", alternate = ["printType"])
    val printType: String? = null,
    @SerializedName(value = "paper_size", alternate = ["paperSize"])
    val paperSize: String? = null,
    @SerializedName(value = "page_count", alternate = ["pageCount"])
    val pageCount: Int? = null,
    @SerializedName(value = "calculated_cost", alternate = ["calculatedCost"])
    val calculatedCost: Double? = null,
    @SerializedName(value = "payment_status", alternate = ["paymentStatus"])
    val paymentStatus: String? = null,
    @SerializedName(value = "job_status", alternate = ["jobStatus"])
    val jobStatus: String? = null,
    @SerializedName(value = "created_at", alternate = ["createdAt"])
    val createdAt: String? = null,
)

data class BorrowSubmitRequest(
    @SerializedName("title_ids") val titleIds: List<Int>,
)

data class CancelBorrowRequestDto(
    val reason: String? = null,
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
