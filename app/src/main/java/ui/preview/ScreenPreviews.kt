package com.example.stismartlib.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.stismartlib.ui.auth.LoginScreen
import com.example.stismartlib.ui.auth.RegisterScreen
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.components.PortalScreen
import com.example.stismartlib.ui.dashboard.DashboardScreen
import com.example.stismartlib.ui.dashboard.account.ClearanceScreen
import com.example.stismartlib.ui.dashboard.account.FinesScreen
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
import com.example.stismartlib.ui.dashboard.returnbook.ReturnBookScreen
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

@Preview(name = "Overview · Student", showBackground = true, showSystemUi = true)
@Composable
private fun DashboardOverviewStudentPreview() {
    PreviewApp(userType = UserType.STUDENT) {
        NestedPreview {
            DashboardOverviewScreen(
                onNavigateToBookCatalog = NoOp,
                onNavigateToResearchCatalog = NoOp,
                onNavigateToBorrowingHistory = NoOp,
                onNavigateToBorrowProcess = NoOp,
                onNavigateToReturnBook = NoOp,
                onNavigateToReserveBook = NoOp,
                onNavigateToPrintingService = NoOp,
                onNavigateToQrLogin = NoOp,
                onNavigateToNotifications = NoOp,
                onLogout = NoOp,
            )
        }
    }
}

@Preview(name = "Overview · Student · Dark", showBackground = true, showSystemUi = true)
@Composable
private fun DashboardOverviewStudentDarkPreview() {
    PreviewApp(userType = UserType.STUDENT, darkTheme = true) {
        NestedPreview {
            DashboardOverviewScreen(
                onNavigateToBookCatalog = NoOp,
                onNavigateToResearchCatalog = NoOp,
                onNavigateToBorrowingHistory = NoOp,
                onNavigateToBorrowProcess = NoOp,
                onNavigateToReturnBook = NoOp,
                onNavigateToReserveBook = NoOp,
                onNavigateToPrintingService = NoOp,
                onNavigateToQrLogin = NoOp,
                onNavigateToNotifications = NoOp,
                onLogout = NoOp,
            )
        }
    }
}

@Preview(name = "Overview · Faculty", showBackground = true, showSystemUi = true)
@Composable
private fun DashboardOverviewFacultyPreview() {
    PreviewApp(userType = UserType.FACULTY) {
        NestedPreview {
            DashboardOverviewScreen(
                onNavigateToBookCatalog = NoOp,
                onNavigateToResearchCatalog = NoOp,
                onNavigateToBorrowingHistory = NoOp,
                onNavigateToBorrowProcess = NoOp,
                onNavigateToReturnBook = NoOp,
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

@Preview(name = "Return book", showBackground = true, showSystemUi = true)
@Composable
private fun ReturnBookScreenPreview() {
    PreviewApp {
        NestedPreview {
            ReturnBookScreen(onBack = NoOp)
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
