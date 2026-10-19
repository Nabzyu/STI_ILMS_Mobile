package com.example.stismartlib.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.stismartlib.data.LibraryRepository
import com.example.stismartlib.data.LocalLibraryRepository
import com.example.stismartlib.data.models.AvatarApprovalStatus
import com.example.stismartlib.data.models.Book
import com.example.stismartlib.data.models.BorrowStatus
import com.example.stismartlib.data.models.BorrowingRecord
import com.example.stismartlib.data.DayHours
import com.example.stismartlib.data.LibraryClosure
import com.example.stismartlib.data.LibrarySchedule
import com.example.stismartlib.data.campusToday
import com.example.stismartlib.data.models.FineReceipt
import com.example.stismartlib.data.models.FineRecord
import com.example.stismartlib.data.models.FineStatus
import com.example.stismartlib.data.models.NotificationCategory
import com.example.stismartlib.data.models.NotificationItem
import com.example.stismartlib.data.models.PrintReceipt
import com.example.stismartlib.data.models.PrintRequest
import com.example.stismartlib.data.models.PrintStatus
import com.example.stismartlib.data.models.ProfileAvatar
import com.example.stismartlib.data.models.Research
import com.example.stismartlib.data.models.Reservation
import com.example.stismartlib.data.models.UserProfile
import com.example.stismartlib.session.SessionStore
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.theme.LocalThemeController
import com.example.stismartlib.ui.theme.STIOrmocLibraryTheme
import com.example.stismartlib.ui.theme.ThemeController
import com.example.stismartlib.ui.theme.ThemeMode
import java.time.DayOfWeek

/**
 * Wraps screen previews with theme, theme controller, and an in-memory repository.
 * Safe for Android Studio Compose Preview (no STIOrmocLibraryApp cast).
 */
@Composable
fun PreviewApp(
    userType: UserType = UserType.STUDENT,
    seedSampleData: Boolean = true,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val themeController = remember { ThemeController(if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT) }
    val repository = remember(userType, seedSampleData) {
        LibraryRepository(SessionStore(context.applicationContext)).also { repo ->
            repo.setUserType(userType)
            if (seedSampleData) {
                seedPreviewLibraryData(repo)
            }
        }
    }
    CompositionLocalProvider(
        LocalThemeController provides themeController,
        LocalLibraryRepository provides repository,
    ) {
        STIOrmocLibraryTheme(darkTheme = darkTheme) {
            content()
        }
    }
}

private fun seedPreviewLibraryData(repository: LibraryRepository) {
    val sampleBook = Book(
        id = "preview-book-1",
        title = "Introduction to Algorithms",
        author = "Cormen et al.",
        isbn = "9780262033848",
        category = "Computing",
        yearPublished = 2009,
        publisher = "MIT Press",
        shelfLocation = "QA76.6 .C662",
        availableCopies = 2,
        totalCopies = 4,
        description = "Sample catalog entry for Compose previews.",
        callNumber = "QA76.6 .C662 2009",
    )
    val sampleResearch = Research(
        id = "preview-research-1",
        title = "Campus Library Usage Patterns",
        author = "Dela Cruz, J.",
        category = "Information Science",
        yearPublished = 2024,
        abstract = "Sample research abstract for Compose previews.",
        shelfLocation = "RES-AI-014",
    )
    val unavailableBook = sampleBook.copy(
        id = "preview-book-unavailable",
        title = "Design Patterns",
        availableCopies = 0,
        totalCopies = 3,
    )
    if (repository.books.none { it.id == sampleBook.id }) {
        repository.books.add(sampleBook)
    }
    if (repository.books.none { it.id == unavailableBook.id }) {
        repository.books.add(unavailableBook)
    }
    if (repository.research.none { it.id == sampleResearch.id }) {
        repository.research.add(sampleResearch)
    }
    if (repository.borrowingHistory.isEmpty()) {
        repository.borrowingHistory.add(
            BorrowingRecord(
                id = "preview-loan-1",
                book = sampleBook,
                borrowDate = "2026-09-01",
                dueDate = "2026-09-15",
                returnDate = null,
                borrowStatus = BorrowStatus.BORROWED,
                fineStatus = FineStatus.NONE,
            ),
        )
        repository.borrowingHistory.add(
            BorrowingRecord(
                id = "42",
                book = sampleBook.copy(id = "preview-book-2", title = "Clean Code"),
                borrowDate = "2026-10-12",
                dueDate = "2026-10-26",
                returnDate = null,
                borrowStatus = BorrowStatus.PENDING,
                fineStatus = FineStatus.NONE,
            ),
        )
    }
    if (repository.reservations.isEmpty()) {
        repository.reservations.add(
            Reservation(
                id = "preview-res-1",
                book = sampleBook,
                queuePosition = 1,
                dateReserved = "2026-10-01",
                status = "pending",
                pickupDeadline = null,
            ),
        )
        repository.reservations.add(
            Reservation(
                id = "preview-res-2",
                book = sampleBook.copy(id = "preview-book-ready", title = "Clean Code"),
                queuePosition = 1,
                dateReserved = "2026-10-05",
                status = "ready_for_pickup",
                pickupDeadline = "2026-10-16",
            ),
        )
    }
    if (repository.fines.isEmpty()) {
        repository.fines.add(
            FineRecord(
                id = "preview-fine-1",
                bookTitle = sampleBook.title,
                amount = 25.0,
                reason = "Overdue",
                dateIssued = "2026-09-20",
                paymentStatus = FineStatus.UNPAID,
                paymentDate = null,
            ),
        )
    }
    repository.applyPreviewPaging()
    if (repository.librarySchedule == null) {
        repository.applyPreviewSchedule(
            LibrarySchedule(
                weekly = DayOfWeek.entries.map { day ->
                    val weekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
                    DayHours(day, isOpen = !weekend, opensAt = "08:00:00", closesAt = "17:00:00")
                },
                closures = listOf(LibraryClosure(campusToday().plusDays(12), "All Saints' Day")),
            )
        )
    }
    if (repository.fineReceipts.isEmpty()) {
        repository.fineReceipts.add(
            FineReceipt(
                id = "12",
                number = "OR-20261010-000012",
                amount = 45.5,
                method = "Cash",
                receivedOn = "2026-10-10",
                status = "Issued",
            ),
        )
    }
    if (repository.notifications.isEmpty()) {
        repository.notifications.add(
            NotificationItem(
                id = "preview-note-1",
                title = "Book due soon",
                message = "Return Introduction to Algorithms by Sep 15.",
                time = "2h ago",
                category = NotificationCategory.REMINDERS,
                isRead = false,
            ),
        )
    }
    if (repository.printHistory.isEmpty()) {
        repository.printHistory.add(
            PrintRequest(
                id = "preview-print-1",
                fileName = "thesis-chapter-1.pdf",
                copies = 1,
                printType = "Black & white",
                isColored = false,
                note = "",
                totalCost = 12.0,
                amountPaid = 0.0,
                status = PrintStatus.PENDING,
                dateSubmitted = "2026-10-10",
                paymentStatus = "Unpaid",
            ),
        )
    }
    if (repository.printReceipts.isEmpty()) {
        repository.printReceipts.add(
            PrintReceipt(
                id = "7",
                receiptNumber = "PR-2026-0007",
                verificationCode = "AB12CD",
                fileName = "lab-report.pdf",
                amountReceived = 15.0,
                printType = "Monochrome",
                paperSize = "A4",
                copies = 2,
                pageCount = 6,
                receivedAt = "2026-10-11",
                receivedBy = "Desk Staff",
                status = "Issued",
            ),
        )
    }
    if (repository.profile == null) {
        repository.applyPreviewProfile(
            userProfile = UserProfile(
                accountId = 1,
                schoolId = "02000XXXXXX",
                firstName = "Ana",
                lastName = "Santos",
                fullName = "Ana Santos",
                email = "ana.santos@example.com",
                role = "Student",
                programStrand = "BSIT",
                yearGradeLevel = "3rd Year",
                status = "Active",
            ),
            avatar = ProfileAvatar(
                status = AvatarApprovalStatus.PENDING,
                imageUrl = null,
                note = "Photo submitted. It stays private until a librarian approves it.",
            ),
        )
    }
}
