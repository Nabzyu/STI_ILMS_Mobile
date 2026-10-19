package com.example.stismartlib.data.models

import android.graphics.Bitmap
import com.example.stismartlib.ui.auth.UserType

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val isbn: String,
    val category: String,
    val yearPublished: Int,
    val publisher: String,
    val shelfLocation: String,
    val availableCopies: Int,
    val totalCopies: Int,
    val description: String,
    val coverColor: Long = 0xFF0B5EA2,
    val callNumber: String = "",
)

data class PrintReceipt(
    val id: String,
    val receiptNumber: String,
    val verificationCode: String,
    val fileName: String,
    val amountReceived: Double,
    val printType: String,
    val paperSize: String,
    val copies: Int,
    val pageCount: Int,
    val receivedAt: String,
    val receivedBy: String,
    val status: String,
)

data class Research(
    val id: String,
    val title: String,
    val author: String,
    val category: String,
    val yearPublished: Int,
    val abstract: String,
    val shelfLocation: String,
    val schoolName: String = "STI College",
    val coverColor: Long = 0xFF2B8A3E,
)

data class BorrowingRecord(
    val id: String,
    val book: Book,
    val borrowDate: String,
    val dueDate: String,
    val returnDate: String?,
    val borrowStatus: BorrowStatus,
    val fineStatus: FineStatus,
)

enum class BorrowStatus(val label: String) {
    PENDING("Pending"),
    BORROWED("Borrowed"),
    RETURNED("Returned"),
    OVERDUE("Overdue"),
    CANCELLED("Cancelled"),
}

enum class FineStatus(val label: String) {
    NONE("No Fine"),
    UNPAID("Unpaid"),
    PAID("Paid"),
}

data class FineRecord(
    val id: String,
    val bookTitle: String,
    val amount: Double,
    val reason: String,
    val dateIssued: String,
    val paymentStatus: FineStatus,
    val paymentDate: String?,
)

data class Reservation(
    val id: String,
    val book: Book,
    val queuePosition: Int,
    val dateReserved: String,
    val status: String = "pending",
    val pickupDeadline: String? = null,
) {
    val isCancellable: Boolean
        get() {
            val normalized = status.lowercase().replace(' ', '_')
            return normalized in setOf("pending", "approved", "ready_for_pickup", "queued", "")
        }

    val statusBadgeLabel: String
        get() = when (status.lowercase().replace(' ', '_')) {
            "ready_for_pickup" -> "Ready to claim"
            "approved" -> "Approved"
            "cancelled" -> "Cancelled"
            "expired" -> "Expired"
            "claimed" -> "Claimed"
            else -> "Queued"
        }

    val isReadyForPickup: Boolean
        get() = status.lowercase().replace(' ', '_') == "ready_for_pickup"
}

enum class NotificationCategory(val label: String) {
    BOOKS("Books"),
    PRINTING("Printing"),
    REMINDERS("Reminders"),
    GENERAL("General"),
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val category: NotificationCategory = NotificationCategory.GENERAL,
    val isRead: Boolean = false,
)

data class PrintRequest(
    val id: String,
    val fileName: String,
    val copies: Int,
    val printType: String,
    val isColored: Boolean,
    val note: String,
    val totalCost: Double,
    val amountPaid: Double,
    val status: PrintStatus,
    val dateSubmitted: String,
    val paymentStatus: String = "",
    val pageCount: Int = 0,
) {
    val canCancel: Boolean
        get() = status == PrintStatus.PENDING &&
            paymentStatus.equals("Unpaid", ignoreCase = true)
}

data class PrintQuote(
    val pageCount: Int,
    val totalSheets: Int,
    val calculatedCost: Double,
    val documentSha256: String,
    val printableFileName: String?,
)

data class ClearanceStanding(
    val isCleared: Boolean,
    val statusLabel: String,
    val reason: String,
    val checkedAt: String?,
    val activeLoans: Int,
    val unpaidOverdueFines: Int,
    val unpaidReplacementCharges: Double,
    val totalOutstanding: Double,
    val loans: List<ClearanceLoanItem> = emptyList(),
    val fines: List<ClearanceFineItem> = emptyList(),
    val lostBooks: List<ClearanceLostBookItem> = emptyList(),
    val activeOverrideReason: String? = null,
)

data class ClearanceLoanItem(
    val title: String,
    val status: String,
    val dueAt: String?,
    val currentFine: Double,
)

data class ClearanceFineItem(
    val title: String,
    val amount: Double,
    val basis: String?,
)

data class ClearanceLostBookItem(
    val title: String,
    val status: String,
    val replacementCharge: Double,
    val paymentStatus: String,
)

enum class PrintStatus(val label: String) {
    PENDING("Pending"),
    PRINTING("Printing"),
    READY("Ready for Pickup"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
}

data class CartItem(
    val book: Book,
    val isAvailable: Boolean,
)

data class LibrarySession(
    val userType: UserType = UserType.STUDENT,
    val userName: String = "Student User",
    val schoolId: String = "",
    val accountId: Int = 0,
    val isAuthenticated: Boolean = false,
)

enum class AvatarApprovalStatus(val label: String) {
    NONE("No photo"),
    PENDING("Pending approval"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    UNKNOWN("Unknown"),
}

data class UserProfile(
    val accountId: Int = 0,
    val schoolId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "",
    val programStrand: String = "",
    val yearGradeLevel: String = "",
    val status: String = "",
)

data class ProfileAvatar(
    val status: AvatarApprovalStatus = AvatarApprovalStatus.NONE,
    val imageUrl: String? = null,
    val rejectionReason: String? = null,
    val note: String? = null,
)

data class AttendancePass(
    val schoolId: String,
    val fullName: String,
    val role: String,
    val visitsThisMonth: Int = 0,
    val todayCheckIn: String? = null,
    val commonPurpose: String? = null,
    val qrBitmap: Bitmap? = null,
)

fun Book.apaCitation(): String {
    return "$author ($yearPublished). $title. $publisher."
}

fun Research.apaCitation(): String {
    return "$author ($yearPublished). $title. $schoolName."
}

fun Book.availabilityLabel(): String =
    if (availableCopies > 0) "$availableCopies of $totalCopies available" else "Unavailable"
