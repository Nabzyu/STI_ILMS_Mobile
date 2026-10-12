package com.example.stismartlib.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.stismartlib.data.LibraryRepository
import com.example.stismartlib.data.LocalLibraryRepository
import com.example.stismartlib.data.models.Book
import com.example.stismartlib.data.models.BorrowStatus
import com.example.stismartlib.data.models.BorrowingRecord
import com.example.stismartlib.data.models.FineRecord
import com.example.stismartlib.data.models.FineStatus
import com.example.stismartlib.data.models.NotificationCategory
import com.example.stismartlib.data.models.NotificationItem
import com.example.stismartlib.data.models.PrintRequest
import com.example.stismartlib.data.models.PrintStatus
import com.example.stismartlib.data.models.Research
import com.example.stismartlib.data.models.Reservation
import com.example.stismartlib.session.SessionStore
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.theme.LocalThemeController
import com.example.stismartlib.ui.theme.STIOrmocLibraryTheme
import com.example.stismartlib.ui.theme.ThemeController
import com.example.stismartlib.ui.theme.ThemeMode

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
    if (repository.books.none { it.id == sampleBook.id }) {
        repository.books.add(sampleBook)
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
    }
    if (repository.reservations.isEmpty()) {
        repository.reservations.add(
            Reservation(
                id = "preview-res-1",
                book = sampleBook,
                queuePosition = 1,
                dateReserved = "2026-10-01",
                status = "pending",
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
            ),
        )
    }
}
