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
)

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
