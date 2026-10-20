package com.example.stismartlib.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.stismartlib.data.LoginChallenge
import com.example.stismartlib.data.MyBook
import com.example.stismartlib.network.dto.BookLocationDto
import com.example.stismartlib.network.dto.FloorPlanImageDto
import com.example.stismartlib.ui.dashboard.catalog.CopyDetailsContent
import com.example.stismartlib.network.dto.CatalogCopyDto
import com.example.stismartlib.ui.dashboard.floorplan.FloorPlanContent
import com.example.stismartlib.ui.dashboard.mybooks.MyBooksContent
import com.example.stismartlib.ui.auth.ForgotPasswordScreen
import com.example.stismartlib.ui.auth.LoginScreen
import com.example.stismartlib.ui.auth.RecoveryStep
import com.example.stismartlib.ui.auth.RegisterScreen
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.components.PortalScreen
import com.example.stismartlib.ui.dashboard.DashboardScreen
import com.example.stismartlib.ui.dashboard.account.ClearanceScreen
import com.example.stismartlib.ui.dashboard.account.FinesScreen
import com.example.stismartlib.ui.dashboard.account.ProfileScreen
import com.example.stismartlib.ui.dashboard.borrow.BorrowProcessScreen
import com.example.stismartlib.ui.dashboard.catalog.BookCartScreen
import com.example.stismartlib.ui.dashboard.catalog.BookCatalogScreen
import com.example.stismartlib.ui.dashboard.catalog.BookDetailScreen
import com.example.stismartlib.ui.dashboard.history.BorrowingHistoryScreen
import com.example.stismartlib.ui.dashboard.notifications.NotificationsScreen
import com.example.stismartlib.ui.dashboard.overview.DashboardOverviewScreen
import com.example.stismartlib.ui.dashboard.printing.PrintingHistoryScreen
import com.example.stismartlib.ui.dashboard.printing.PrintingServiceScreen
import com.example.stismartlib.ui.dashboard.qr.AttendanceScreen
import com.example.stismartlib.ui.dashboard.qr.QrLoginScreen
import com.example.stismartlib.ui.dashboard.research.ResearchCatalogScreen
import com.example.stismartlib.ui.dashboard.research.ResearchDetailScreen
import com.example.stismartlib.ui.dashboard.reserve.ReserveBookScreen
import com.example.stismartlib.ui.dashboard.search.GlobalSearchScreen
import com.example.stismartlib.ui.dashboard.staff.StaffHomeScreen
import com.example.stismartlib.ui.splash.SplashScreen

private val NoOp: () -> Unit = {}

/** Wraps dashboard destinations that rely on Scaffold chrome when shown standalone in Preview. */
@Composable
private fun NestedPreview(content: @Composable () -> Unit) {
    PortalScreen(content = content)
}

@Preview(name = "Splash", showBackground = true, showSystemUi = true)
@Composable
private fun SplashScreenPreview() {
    PreviewApp(seedSampleData = false) {
        SplashScreen()
    }
}

@Preview(name = "Login", showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    PreviewApp(seedSampleData = false) {
        LoginScreen(navController = rememberNavController())
    }
}

@Preview(name = "Login · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenDarkPreview() {
    PreviewApp(seedSampleData = false, darkTheme = true) {
        LoginScreen(navController = rememberNavController())
    }
}

@Preview(name = "Register", showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenPreview() {
    PreviewApp(seedSampleData = false) {
        RegisterScreen(navController = rememberNavController())
    }
}

@Preview(name = "Register · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenDarkPreview() {
    PreviewApp(seedSampleData = false, darkTheme = true) {
        RegisterScreen(navController = rememberNavController())
    }
}

private val PreviewLoginChallenge = LoginChallenge(
    challengeId = "00000000-0000-4000-8000-000000000001",
    schoolId = "02000123456",
    portal = "user",
    emailHint = "an***@ormoc.sti.edu.ph",
    resendAfterSeconds = 60,
)

@Preview(name = "Login · Code", showBackground = true, showSystemUi = true)
@Composable
private fun LoginCodeStepPreview() {
    PreviewApp(seedSampleData = false) {
        LoginScreen(navController = rememberNavController(), initialChallenge = PreviewLoginChallenge)
    }
}

@Preview(name = "Login · Code · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun LoginCodeStepDarkPreview() {
    PreviewApp(seedSampleData = false, darkTheme = true) {
        LoginScreen(navController = rememberNavController(), initialChallenge = PreviewLoginChallenge)
    }
}

@Preview(name = "Register · Verify", showBackground = true, showSystemUi = true)
@Composable
private fun RegisterVerifyStepPreview() {
    PreviewApp(seedSampleData = false) {
        RegisterScreen(
            navController = rememberNavController(),
            initialVerifySchoolId = "02000123456",
            initialPendingEmail = "ana@ormoc.sti.edu.ph",
        )
    }
}

@Preview(name = "Register · Verify · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun RegisterVerifyStepDarkPreview() {
    PreviewApp(seedSampleData = false, darkTheme = true) {
        RegisterScreen(
            navController = rememberNavController(),
            initialVerifySchoolId = "02000123456",
            initialPendingEmail = "ana@ormoc.sti.edu.ph",
        )
    }
}

@Preview(name = "Register · Resume from login", showBackground = true, showSystemUi = true)
@Composable
private fun RegisterResumeStepPreview() {
    PreviewApp(seedSampleData = false) {
        RegisterScreen(navController = rememberNavController(), initialVerifySchoolId = "02000123456")
    }
}

@Preview(name = "Register · Resume from login · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun RegisterResumeStepDarkPreview() {
    PreviewApp(seedSampleData = false, darkTheme = true) {
        RegisterScreen(navController = rememberNavController(), initialVerifySchoolId = "02000123456")
    }
}

@Preview(name = "Forgot password", showBackground = true, showSystemUi = true)
@Composable
private fun ForgotPasswordPreview() {
    PreviewApp(seedSampleData = false) {
        ForgotPasswordScreen(navController = rememberNavController())
    }
}

@Preview(name = "Forgot password · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun ForgotPasswordDarkPreview() {
    PreviewApp(seedSampleData = false, darkTheme = true) {
        ForgotPasswordScreen(navController = rememberNavController())
    }
}

private const val PreviewRecoveryNotice =
    "If an active student account matches, a six-digit code will be sent to its school email. Check your inbox and junk folder."

@Preview(name = "Forgot password · Code", showBackground = true, showSystemUi = true)
@Composable
private fun ForgotPasswordCodePreview() {
    PreviewApp(seedSampleData = false) {
        ForgotPasswordScreen(rememberNavController(), RecoveryStep.Code, PreviewRecoveryNotice)
    }
}

@Preview(name = "Forgot password · Code · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun ForgotPasswordCodeDarkPreview() {
    PreviewApp(seedSampleData = false, darkTheme = true) {
        ForgotPasswordScreen(rememberNavController(), RecoveryStep.Code, PreviewRecoveryNotice)
    }
}

@Preview(name = "Forgot password · New", showBackground = true, showSystemUi = true)
@Composable
private fun ForgotPasswordNewPreview() {
    PreviewApp(seedSampleData = false) {
        ForgotPasswordScreen(rememberNavController(), RecoveryStep.NewPassword)
    }
}

@Preview(name = "Forgot password · New · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun ForgotPasswordNewDarkPreview() {
    PreviewApp(seedSampleData = false, darkTheme = true) {
        ForgotPasswordScreen(rememberNavController(), RecoveryStep.NewPassword)
    }
}

@Preview(name = "Forgot password · Done", showBackground = true, showSystemUi = true)
@Composable
private fun ForgotPasswordDonePreview() {
    PreviewApp(seedSampleData = false) {
        ForgotPasswordScreen(rememberNavController(), RecoveryStep.Done, "Password updated. Sign in with your new password.")
    }
}

@Preview(name = "Staff home", showBackground = true, showSystemUi = true)
@Composable
private fun StaffHomeScreenPreview() {
    PreviewApp(userType = UserType.ADMIN, seedSampleData = false) {
        StaffHomeScreen(onLogout = NoOp)
    }
}

@Preview(name = "Dashboard shell", showBackground = true, showSystemUi = true)
@Composable
private fun DashboardScreenPreview() {
    PreviewApp(userType = UserType.STUDENT) {
        DashboardScreen(userType = UserType.STUDENT, onLogout = NoOp)
    }
}

@Preview(name = "Dashboard shell · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun DashboardScreenDarkPreview() {
    PreviewApp(userType = UserType.STUDENT, darkTheme = true) {
        DashboardScreen(userType = UserType.STUDENT, onLogout = NoOp)
    }
}

@Preview(name = "Dashboard · Student", showBackground = true, showSystemUi = true)
@Composable
private fun DashboardOverviewStudentPreview() {
    PreviewApp(userType = UserType.STUDENT) {
        NestedPreview {
            DashboardOverviewScreen(
                onNavigateToBookCatalog = NoOp,
                onNavigateToResearchCatalog = NoOp,
                onNavigateToBorrowingHistory = NoOp,
                onNavigateToBorrowProcess = NoOp,
                onNavigateToReserveBook = NoOp,
                onNavigateToPrintingService = NoOp,
                onNavigateToQrLogin = NoOp,
                onNavigateToNotifications = NoOp,
                onLogout = NoOp,
            )
        }
    }
}

@Preview(name = "Dashboard · Student · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun DashboardOverviewStudentDarkPreview() {
    PreviewApp(userType = UserType.STUDENT, darkTheme = true) {
        NestedPreview {
            DashboardOverviewScreen(
                onNavigateToBookCatalog = NoOp,
                onNavigateToResearchCatalog = NoOp,
                onNavigateToBorrowingHistory = NoOp,
                onNavigateToBorrowProcess = NoOp,
                onNavigateToReserveBook = NoOp,
                onNavigateToPrintingService = NoOp,
                onNavigateToQrLogin = NoOp,
                onNavigateToNotifications = NoOp,
                onLogout = NoOp,
            )
        }
    }
}

@Preview(name = "Dashboard · Faculty", showBackground = true, showSystemUi = true)
@Composable
private fun DashboardOverviewFacultyPreview() {
    PreviewApp(userType = UserType.FACULTY) {
        NestedPreview {
            DashboardOverviewScreen(
                onNavigateToBookCatalog = NoOp,
                onNavigateToResearchCatalog = NoOp,
                onNavigateToBorrowingHistory = NoOp,
                onNavigateToBorrowProcess = NoOp,
                onNavigateToReserveBook = NoOp,
                onNavigateToPrintingService = NoOp,
                onNavigateToQrLogin = NoOp,
                onNavigateToNotifications = NoOp,
                onLogout = NoOp,
            )
        }
    }
}

@Preview(name = "Book catalog", showBackground = true, showSystemUi = true)
@Composable
private fun BookCatalogScreenPreview() {
    PreviewApp {
        NestedPreview {
            BookCatalogScreen(onBookClick = {}, onCartClick = NoOp)
        }
    }
}

@Preview(name = "Book catalog · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun BookCatalogScreenDarkPreview() {
    PreviewApp(darkTheme = true) {
        NestedPreview {
            BookCatalogScreen(onBookClick = {}, onCartClick = NoOp)
        }
    }
}

@Preview(name = "Book catalog empty", showBackground = true, showSystemUi = true)
@Composable
private fun BookCatalogEmptyPreview() {
    PreviewApp(seedSampleData = false) {
        NestedPreview {
            BookCatalogScreen(onBookClick = {}, onCartClick = NoOp)
        }
    }
}

@Preview(name = "Book detail", showBackground = true, showSystemUi = true)
@Composable
private fun BookDetailScreenPreview() {
    PreviewApp {
        NestedPreview {
            BookDetailScreen(
                bookId = "preview-book-1",
                onBack = NoOp,
                onCartClick = NoOp,
            )
        }
    }
}

@Preview(name = "Book detail unavailable", showBackground = true, showSystemUi = true)
@Composable
private fun BookDetailUnavailablePreview() {
    PreviewApp {
        NestedPreview {
            BookDetailScreen(
                bookId = "preview-book-unavailable",
                onBack = NoOp,
                onCartClick = NoOp,
            )
        }
    }
}

@Preview(name = "Book cart", showBackground = true, showSystemUi = true)
@Composable
private fun BookCartScreenPreview() {
    PreviewApp {
        NestedPreview {
            BookCartScreen(onBack = NoOp)
        }
    }
}

@Preview(name = "Book cart empty", showBackground = true, showSystemUi = true)
@Composable
private fun BookCartEmptyPreview() {
    PreviewApp(seedSampleData = false) {
        NestedPreview {
            BookCartScreen(onBack = NoOp)
        }
    }
}

@Preview(name = "Research catalog", showBackground = true, showSystemUi = true)
@Composable
private fun ResearchCatalogScreenPreview() {
    PreviewApp {
        NestedPreview {
            ResearchCatalogScreen(onResearchClick = {})
        }
    }
}

@Preview(name = "Research detail", showBackground = true, showSystemUi = true)
@Composable
private fun ResearchDetailScreenPreview() {
    PreviewApp {
        NestedPreview {
            ResearchDetailScreen(researchId = "preview-research-1", onBack = NoOp)
        }
    }
}

@Preview(name = "Borrow process", showBackground = true, showSystemUi = true)
@Composable
private fun BorrowProcessScreenPreview() {
    PreviewApp {
        NestedPreview {
            BorrowProcessScreen(
                onBack = NoOp,
                onBrowseCatalog = NoOp,
                onOpenCart = NoOp,
            )
        }
    }
}

@Preview(name = "Reservations", showBackground = true, showSystemUi = true)
@Composable
private fun ReserveBookScreenPreview() {
    PreviewApp {
        NestedPreview {
            ReserveBookScreen(onBack = NoOp, onBrowseCatalog = NoOp)
        }
    }
}

@Preview(name = "Reservations empty", showBackground = true, showSystemUi = true)
@Composable
private fun ReserveBookEmptyPreview() {
    PreviewApp(seedSampleData = false) {
        NestedPreview {
            ReserveBookScreen(onBack = NoOp, onBrowseCatalog = NoOp)
        }
    }
}

@Preview(name = "Borrowing history", showBackground = true, showSystemUi = true)
@Composable
private fun BorrowingHistoryScreenPreview() {
    PreviewApp {
        NestedPreview {
            BorrowingHistoryScreen()
        }
    }
}

@Preview(name = "Borrowing history empty", showBackground = true, showSystemUi = true)
@Composable
private fun BorrowingHistoryEmptyPreview() {
    PreviewApp(seedSampleData = false) {
        NestedPreview {
            BorrowingHistoryScreen()
        }
    }
}

@Preview(name = "Notifications", showBackground = true, showSystemUi = true)
@Composable
private fun NotificationsScreenPreview() {
    PreviewApp {
        NestedPreview {
            NotificationsScreen(onBack = NoOp)
        }
    }
}

@Preview(name = "Global search", showBackground = true, showSystemUi = true)
@Composable
private fun GlobalSearchScreenPreview() {
    PreviewApp {
        NestedPreview {
            GlobalSearchScreen(
                onBack = NoOp,
                onBookClick = {},
                onResearchClick = {},
                onBrowseCatalog = NoOp,
            )
        }
    }
}

@Preview(name = "QR check-in", showBackground = true, showSystemUi = true)
@Composable
private fun QrLoginScreenPreview() {
    PreviewApp(seedSampleData = false) {
        NestedPreview {
            QrLoginScreen(onBack = NoOp, isAuthFlow = false)
        }
    }
}

@Preview(name = "Attendance pass", showBackground = true, showSystemUi = true)
@Composable
private fun AttendanceScreenPreview() {
    PreviewApp {
        NestedPreview {
            AttendanceScreen()
        }
    }
}

@Preview(name = "Printing service", showBackground = true, showSystemUi = true)
@Composable
private fun PrintingServiceScreenPreview() {
    PreviewApp {
        NestedPreview {
            PrintingServiceScreen(onViewHistory = NoOp)
        }
    }
}

@Preview(name = "Printing history", showBackground = true, showSystemUi = true)
@Composable
private fun PrintingHistoryScreenPreview() {
    PreviewApp {
        NestedPreview {
            PrintingHistoryScreen(onBack = NoOp)
        }
    }
}

@Preview(name = "Printing history empty", showBackground = true, showSystemUi = true)
@Composable
private fun PrintingHistoryEmptyPreview() {
    PreviewApp(seedSampleData = false) {
        NestedPreview {
            PrintingHistoryScreen(onBack = NoOp)
        }
    }
}

@Preview(name = "Fines", showBackground = true, showSystemUi = true)
@Composable
private fun FinesScreenPreview() {
    PreviewApp {
        NestedPreview {
            FinesScreen()
        }
    }
}

@Preview(name = "Fines · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun FinesScreenDarkPreview() {
    PreviewApp(darkTheme = true) {
        NestedPreview {
            FinesScreen()
        }
    }
}

@Preview(name = "Fines empty", showBackground = true, showSystemUi = true)
@Composable
private fun FinesEmptyPreview() {
    PreviewApp(seedSampleData = false) {
        NestedPreview {
            FinesScreen()
        }
    }
}

@Preview(name = "Clearance", showBackground = true, showSystemUi = true)
@Composable
private fun ClearanceScreenPreview() {
    PreviewApp {
        NestedPreview {
            ClearanceScreen()
        }
    }
}

@Preview(name = "Clearance · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun ClearanceScreenDarkPreview() {
    PreviewApp(darkTheme = true) {
        NestedPreview {
            ClearanceScreen()
        }
    }
}

@Preview(name = "My profile", showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    PreviewApp {
        NestedPreview {
            ProfileScreen()
        }
    }
}

@Preview(name = "My profile · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenDarkPreview() {
    PreviewApp(darkTheme = true) {
        NestedPreview {
            ProfileScreen()
        }
    }
}

private val PreviewFloorPlanImage = FloorPlanImageDto(
    imageUrl = "android.resource://com.example.stismartlib/drawable/sti_logo",
    uploadedAt = "2026-10-10T08:15:00.000Z",
    uploadedBy = "LIB-001",
)

private val PreviewBookLocation = BookLocationDto(
    title = "Clean Code",
    shelfLabel = "Circulation · Shelf A-3",
    accession = "10042",
    callNumber = "QA76.76 .M37",
)

@Preview(name = "Floor plan · Book location", showBackground = true, showSystemUi = true)
@Composable
private fun FloorPlanLocationPreview() {
    PreviewApp(seedSampleData = false) {
        NestedPreview {
            FloorPlanContent(true, PreviewFloorPlanImage, PreviewBookLocation, loading = false, error = null, onRefresh = {})
        }
    }
}

@Preview(name = "Floor plan · Book location · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun FloorPlanLocationDarkPreview() {
    PreviewApp(seedSampleData = false, darkTheme = true) {
        NestedPreview {
            FloorPlanContent(true, PreviewFloorPlanImage, PreviewBookLocation, loading = false, error = null, onRefresh = {})
        }
    }
}

@Preview(name = "Floor plan · Empty", showBackground = true, showSystemUi = true)
@Composable
private fun FloorPlanEmptyPreview() {
    PreviewApp(seedSampleData = false) {
        NestedPreview {
            FloorPlanContent(false, image = null, location = null, loading = false, error = null, onRefresh = {})
        }
    }
}

@Preview(name = "Notifications · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun NotificationsScreenDarkPreview() {
    PreviewApp(darkTheme = true) {
        NestedPreview {
            NotificationsScreen(onBack = NoOp)
        }
    }
}

@Preview(name = "Research catalog · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun ResearchCatalogScreenDarkPreview() {
    PreviewApp(darkTheme = true) {
        NestedPreview {
            ResearchCatalogScreen(onResearchClick = {})
        }
    }
}

@Preview(name = "Borrowing history · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun BorrowingHistoryScreenDarkPreview() {
    PreviewApp(darkTheme = true) {
        NestedPreview {
            BorrowingHistoryScreen()
        }
    }
}

private val PreviewMyBooks = listOf(
    MyBook("7", "Clean Code", "Robert C. Martin", "Borrowed", "2026-10-05", "2026-10-19", null),
    MyBook("2", "Introduction to Algorithms", "Cormen et al.", "Overdue", "2026-09-20", "2026-10-04", null),
    MyBook("4", "Design Patterns", "Gamma et al.", "Returned", "2026-08-12", "2026-08-26", "2026-08-25"),
)

@Preview(name = "My books", showBackground = true, showSystemUi = true)
@Composable
private fun MyBooksScreenPreview() {
    PreviewApp(seedSampleData = false) {
        NestedPreview {
            MyBooksContent(PreviewMyBooks, loading = false, error = null, query = "", onQueryChange = {}, onRetry = {})
        }
    }
}

@Preview(name = "My books · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun MyBooksScreenDarkPreview() {
    PreviewApp(seedSampleData = false, darkTheme = true) {
        NestedPreview {
            MyBooksContent(PreviewMyBooks, loading = false, error = null, query = "", onQueryChange = {}, onRetry = {})
        }
    }
}

@Preview(name = "My books · Unavailable", showBackground = true, showSystemUi = true)
@Composable
private fun MyBooksUnavailablePreview() {
    PreviewApp(seedSampleData = false) {
        NestedPreview {
            MyBooksContent(emptyList(), loading = false, error = "This feature is not available right now.", query = "", onQueryChange = {}, onRetry = {})
        }
    }
}

private const val PreviewCartSyncNotice =
    "Removed books already requested or checked out. You can find them in My books or Borrowing history."

@Preview(name = "Book cart · Sync notice", showBackground = true, showSystemUi = true)
@Composable
private fun BookCartSyncNoticePreview() {
    PreviewApp {
        NestedPreview {
            BookCartScreen(onBack = NoOp, initialSyncNotice = PreviewCartSyncNotice)
        }
    }
}

@Preview(name = "Book cart · Sync notice · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun BookCartSyncNoticeDarkPreview() {
    PreviewApp(darkTheme = true) {
        NestedPreview {
            BookCartScreen(onBack = NoOp, initialSyncNotice = PreviewCartSyncNotice)
        }
    }
}

private val PreviewCopy = CatalogCopyDto(
    accessionNumber = "10042",
    barcode = "STI-000123",
    shelfLocation = "Circulation · Shelf A-3",
    conditionStatus = "Good",
    barcodeImageData = "data:image/svg+xml;base64,PHN2ZyB4bWxucz0naHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmcnIHdpZHRoPScyMjAnIGhlaWdodD0nNjAnPjxyZWN0IHg9JzQnIHk9JzAnIHdpZHRoPSczJyBoZWlnaHQ9JzYwJyBmaWxsPSdibGFjaycvPjxyZWN0IHg9JzEwJyB5PScwJyB3aWR0aD0nMicgaGVpZ2h0PSc2MCcgZmlsbD0nYmxhY2snLz48cmVjdCB4PScxNScgeT0nMCcgd2lkdGg9JzQnIGhlaWdodD0nNjAnIGZpbGw9J2JsYWNrJy8+PHJlY3QgeD0nMjInIHk9JzAnIHdpZHRoPScyJyBoZWlnaHQ9JzYwJyBmaWxsPSdibGFjaycvPjxyZWN0IHg9JzI3JyB5PScwJyB3aWR0aD0nMycgaGVpZ2h0PSc2MCcgZmlsbD0nYmxhY2snLz48cmVjdCB4PSczMycgeT0nMCcgd2lkdGg9JzUnIGhlaWdodD0nNjAnIGZpbGw9J2JsYWNrJy8+PHJlY3QgeD0nNDEnIHk9JzAnIHdpZHRoPScyJyBoZWlnaHQ9JzYwJyBmaWxsPSdibGFjaycvPjxyZWN0IHg9JzQ2JyB5PScwJyB3aWR0aD0nMycgaGVpZ2h0PSc2MCcgZmlsbD0nYmxhY2snLz48cmVjdCB4PSc1MicgeT0nMCcgd2lkdGg9JzInIGhlaWdodD0nNjAnIGZpbGw9J2JsYWNrJy8+PHJlY3QgeD0nNTcnIHk9JzAnIHdpZHRoPSc0JyBoZWlnaHQ9JzYwJyBmaWxsPSdibGFjaycvPjxyZWN0IHg9JzY0JyB5PScwJyB3aWR0aD0nMycgaGVpZ2h0PSc2MCcgZmlsbD0nYmxhY2snLz48cmVjdCB4PSc3MCcgeT0nMCcgd2lkdGg9JzInIGhlaWdodD0nNjAnIGZpbGw9J2JsYWNrJy8+PHJlY3QgeD0nNzUnIHk9JzAnIHdpZHRoPSc1JyBoZWlnaHQ9JzYwJyBmaWxsPSdibGFjaycvPjxyZWN0IHg9JzgzJyB5PScwJyB3aWR0aD0nMicgaGVpZ2h0PSc2MCcgZmlsbD0nYmxhY2snLz48cmVjdCB4PSc4OCcgeT0nMCcgd2lkdGg9JzMnIGhlaWdodD0nNjAnIGZpbGw9J2JsYWNrJy8+PHJlY3QgeD0nOTQnIHk9JzAnIHdpZHRoPScyJyBoZWlnaHQ9JzYwJyBmaWxsPSdibGFjaycvPjxyZWN0IHg9Jzk5JyB5PScwJyB3aWR0aD0nNCcgaGVpZ2h0PSc2MCcgZmlsbD0nYmxhY2snLz48cmVjdCB4PScxMDYnIHk9JzAnIHdpZHRoPSczJyBoZWlnaHQ9JzYwJyBmaWxsPSdibGFjaycvPjxyZWN0IHg9JzExMicgeT0nMCcgd2lkdGg9JzInIGhlaWdodD0nNjAnIGZpbGw9J2JsYWNrJy8+PHJlY3QgeD0nMTE3JyB5PScwJyB3aWR0aD0nNScgaGVpZ2h0PSc2MCcgZmlsbD0nYmxhY2snLz48cmVjdCB4PScxMjUnIHk9JzAnIHdpZHRoPScyJyBoZWlnaHQ9JzYwJyBmaWxsPSdibGFjaycvPjxyZWN0IHg9JzEzMCcgeT0nMCcgd2lkdGg9JzMnIGhlaWdodD0nNjAnIGZpbGw9J2JsYWNrJy8+PHJlY3QgeD0nMTM2JyB5PScwJyB3aWR0aD0nMicgaGVpZ2h0PSc2MCcgZmlsbD0nYmxhY2snLz48cmVjdCB4PScxNDEnIHk9JzAnIHdpZHRoPSc0JyBoZWlnaHQ9JzYwJyBmaWxsPSdibGFjaycvPjxyZWN0IHg9JzE0OCcgeT0nMCcgd2lkdGg9JzMnIGhlaWdodD0nNjAnIGZpbGw9J2JsYWNrJy8+PHJlY3QgeD0nMTU0JyB5PScwJyB3aWR0aD0nMicgaGVpZ2h0PSc2MCcgZmlsbD0nYmxhY2snLz48cmVjdCB4PScxNTknIHk9JzAnIHdpZHRoPSc1JyBoZWlnaHQ9JzYwJyBmaWxsPSdibGFjaycvPjxyZWN0IHg9JzE2NycgeT0nMCcgd2lkdGg9JzInIGhlaWdodD0nNjAnIGZpbGw9J2JsYWNrJy8+PHJlY3QgeD0nMTcyJyB5PScwJyB3aWR0aD0nMycgaGVpZ2h0PSc2MCcgZmlsbD0nYmxhY2snLz48cmVjdCB4PScxNzgnIHk9JzAnIHdpZHRoPScyJyBoZWlnaHQ9JzYwJyBmaWxsPSdibGFjaycvPjxyZWN0IHg9JzE4MycgeT0nMCcgd2lkdGg9JzQnIGhlaWdodD0nNjAnIGZpbGw9J2JsYWNrJy8+PHJlY3QgeD0nMTkwJyB5PScwJyB3aWR0aD0nMycgaGVpZ2h0PSc2MCcgZmlsbD0nYmxhY2snLz48cmVjdCB4PScxOTYnIHk9JzAnIHdpZHRoPScyJyBoZWlnaHQ9JzYwJyBmaWxsPSdibGFjaycvPjxyZWN0IHg9JzIwMScgeT0nMCcgd2lkdGg9JzUnIGhlaWdodD0nNjAnIGZpbGw9J2JsYWNrJy8+PHJlY3QgeD0nMjA5JyB5PScwJyB3aWR0aD0nMicgaGVpZ2h0PSc2MCcgZmlsbD0nYmxhY2snLz48cmVjdCB4PScyMTQnIHk9JzAnIHdpZHRoPSczJyBoZWlnaHQ9JzYwJyBmaWxsPSdibGFjaycvPjwvc3ZnPg==",
)

@Preview(name = "Copy details", showBackground = true, showSystemUi = true)
@Composable
private fun CopyDetailsPreview() {
    PreviewApp(seedSampleData = false) {
        NestedPreview { CopyDetailsContent(PreviewCopy, loading = false, failed = false) }
    }
}

@Preview(name = "Copy details · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun CopyDetailsDarkPreview() {
    PreviewApp(seedSampleData = false, darkTheme = true) {
        NestedPreview { CopyDetailsContent(PreviewCopy, loading = false, failed = false) }
    }
}

@Preview(name = "Copy details · Unavailable", showBackground = true, showSystemUi = true)
@Composable
private fun CopyDetailsUnavailablePreview() {
    PreviewApp(seedSampleData = false) {
        NestedPreview { CopyDetailsContent(null, loading = false, failed = true) }
    }
}
