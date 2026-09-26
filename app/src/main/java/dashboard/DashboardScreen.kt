package com.example.stismartlib.ui.dashboard
import com.composables.icons.lucide.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.components.LibraryMark
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
import com.example.stismartlib.ui.dashboard.reserve.ReserveBookScreen
import com.example.stismartlib.ui.dashboard.research.ResearchCatalogScreen
import com.example.stismartlib.ui.dashboard.research.ResearchDetailScreen
import com.example.stismartlib.ui.dashboard.returnbook.ReturnBookScreen
import androidx.compose.material3.MaterialTheme
import com.example.stismartlib.ui.theme.AccentYellow
import com.example.stismartlib.ui.theme.ThemeMode
import com.example.stismartlib.ui.theme.LocalThemeController
import com.example.stismartlib.ui.theme.PrimaryBlue
import com.example.stismartlib.ui.theme.isAppDarkTheme
import kotlinx.coroutines.launch

private data class DrawerItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val section: String? = null
)

@Composable
fun DashboardScreen(
    userType: UserType,
    onLogout: () -> Unit
) {
    val repository = rememberLibraryRepository()
    repository.setUserType(userType)
    val dashboardNavController = rememberNavController()
    val navBackStackEntry by dashboardNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val items = drawerItems(userType)
    val current = items.find { item ->
        currentRoute == item.route ||
            (item.route == DashboardRoute.BookCatalog.route && currentRoute?.startsWith("book_detail") == true) ||
            (item.route == DashboardRoute.ResearchCatalog.route && currentRoute?.startsWith("research_detail") == true) ||
            (item.route == DashboardRoute.PrintingService.route && currentRoute == DashboardRoute.PrintingHistory.route)
    }
    val unread = repository.notifications.any { !it.isRead }
    val themeController = LocalThemeController.current
    val darkTheme = isAppDarkTheme()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = PrimaryBlue,
                modifier = Modifier.width(280.dp)
            ) {
                Column(modifier = Modifier.fillMaxHeight()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LibraryMark(onDark = true)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.10f))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 16.dp)
                    ) {
                        items.forEach { item ->
                            if (item.section != null) {
                                Text(
                                    text = item.section.uppercase(),
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.6.sp,
                                    modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 8.dp)
                                )
                            }
                            val selected = current?.route == item.route
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) Color.White else Color.Transparent)
                                    .clickable {
                                        dashboardNavController.navigate(item.route) {
                                            popUpTo(dashboardNavController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    item.icon,
                                    contentDescription = null,
                                    tint = if (selected) PrimaryBlue else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    item.label,
                                    color = if (selected) PrimaryBlue else Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.10f))
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    themeController.themeMode = if (darkTheme) ThemeMode.LIGHT else ThemeMode.DARK
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (darkTheme) Lucide.Sun else Lucide.Moon,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (darkTheme) "Light mode" else "Dark mode",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AccentYellow),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    userType.displayName.take(2).uppercase(),
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("STI account", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(userType.displayName, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Lucide.Menu, contentDescription = "Open navigation", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (userType == UserType.FACULTY) "FACULTY PORTAL" else "STUDENT PORTAL",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = current?.label ?: "Smart Library",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    IconButton(onClick = {
                        dashboardNavController.navigate(DashboardRoute.Notifications.route)
                    }) {
                        Box {
                            Icon(Lucide.Bell, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                            if (unread) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(AccentYellow)
                                )
                            }
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Lucide.LogOut, contentDescription = "Sign out", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = dashboardNavController,
                startDestination = DashboardRoute.Overview.route,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                composable(DashboardRoute.Overview.route) {
                    DashboardOverviewScreen(
                        onNavigateToBookCatalog = { dashboardNavController.navigate(DashboardRoute.BookCatalog.route) },
                        onNavigateToResearchCatalog = { dashboardNavController.navigate(DashboardRoute.ResearchCatalog.route) },
                        onNavigateToBorrowingHistory = { dashboardNavController.navigate(DashboardRoute.BorrowingHistory.route) },
                        onNavigateToBorrowProcess = { dashboardNavController.navigate(DashboardRoute.BorrowProcess.route) },
                        onNavigateToReturnBook = { dashboardNavController.navigate(DashboardRoute.ReturnBook.route) },
                        onNavigateToReserveBook = { dashboardNavController.navigate(DashboardRoute.ReserveBook.route) },
                        onNavigateToPrintingService = { dashboardNavController.navigate(DashboardRoute.PrintingService.route) },
                        onNavigateToQrLogin = { dashboardNavController.navigate(DashboardRoute.QrLogin.route) },
                        onNavigateToNotifications = { dashboardNavController.navigate(DashboardRoute.Notifications.route) },
                        onLogout = onLogout
                    )
                }
                composable(DashboardRoute.BookCatalog.route) {
                    BookCatalogScreen(
                        onBookClick = { dashboardNavController.navigate(DashboardRoute.BookDetail.create(it)) },
                        onCartClick = { dashboardNavController.navigate(DashboardRoute.BookCart.route) }
                    )
                }
                composable(
                    route = DashboardRoute.BookDetail.route,
                    arguments = listOf(navArgument("bookId") { type = NavType.StringType })
                ) { entry ->
                    BookDetailScreen(
                        bookId = entry.arguments?.getString("bookId").orEmpty(),
                        onBack = { dashboardNavController.popBackStack() },
                        onCartClick = { dashboardNavController.navigate(DashboardRoute.BookCart.route) }
                    )
                }
                composable(DashboardRoute.BookCart.route) {
                    BookCartScreen(onBack = { dashboardNavController.popBackStack() })
                }
                composable(DashboardRoute.BorrowProcess.route) {
                    BorrowProcessScreen(
                        onBack = { dashboardNavController.popBackStack() },
                        onBrowseCatalog = { dashboardNavController.navigate(DashboardRoute.BookCatalog.route) },
                        onOpenCart = { dashboardNavController.navigate(DashboardRoute.BookCart.route) }
                    )
                }
                composable(DashboardRoute.ReturnBook.route) {
                    ReturnBookScreen(onBack = { dashboardNavController.popBackStack() })
                }
                composable(DashboardRoute.ReserveBook.route) {
                    ReserveBookScreen(
                        onBack = { dashboardNavController.popBackStack() },
                        onBrowseCatalog = { dashboardNavController.navigate(DashboardRoute.BookCatalog.route) }
                    )
                }
                composable(DashboardRoute.Notifications.route) {
                    NotificationsScreen(onBack = { dashboardNavController.popBackStack() })
                }
                composable(DashboardRoute.QrLogin.route) {
                    AttendanceScreen()
                }
                composable(DashboardRoute.ResearchCatalog.route) {
                    ResearchCatalogScreen(
                        onResearchClick = { dashboardNavController.navigate(DashboardRoute.ResearchDetail.create(it)) }
                    )
                }
                composable(
                    route = DashboardRoute.ResearchDetail.route,
                    arguments = listOf(navArgument("researchId") { type = NavType.StringType })
                ) { entry ->
                    ResearchDetailScreen(
                        researchId = entry.arguments?.getString("researchId").orEmpty(),
                        onBack = { dashboardNavController.popBackStack() }
                    )
                }
                composable(DashboardRoute.BorrowingHistory.route) {
                    BorrowingHistoryScreen()
                }
                composable(DashboardRoute.PrintingService.route) {
                    PrintingServiceScreen(
                        onViewHistory = { dashboardNavController.navigate(DashboardRoute.PrintingHistory.route) }
                    )
                }
                composable(DashboardRoute.PrintingHistory.route) {
                    PrintingHistoryScreen(onBack = { dashboardNavController.popBackStack() })
                }
                composable(DashboardRoute.Fines.route) {
                    FinesScreen()
                }
                composable(DashboardRoute.Clearance.route) {
                    ClearanceScreen()
                }
            }
        }
    }
}

private fun drawerItems(userType: UserType): List<DrawerItem> {
    val items = mutableListOf(
        DrawerItem(DashboardRoute.Overview.route, "Overview", Lucide.LayoutDashboard, "My library"),
        DrawerItem(DashboardRoute.BookCatalog.route, "Book catalog", Lucide.BookOpen),
        DrawerItem(DashboardRoute.BookCart.route, "Book cart", Lucide.ShoppingCart),
        DrawerItem(DashboardRoute.ResearchCatalog.route, "Research & thesis", Lucide.FileText),
        DrawerItem(DashboardRoute.BorrowingHistory.route, "Borrowing history", Lucide.CalendarClock, "My activity"),
        DrawerItem(DashboardRoute.ReserveBook.route, "Reservations", Lucide.BookMarked)
    )
    if (userType == UserType.STUDENT) {
        items += DrawerItem(DashboardRoute.PrintingService.route, "Printing service", Lucide.Printer)
        items += DrawerItem(DashboardRoute.QrLogin.route, "QR attendance", Lucide.QrCode)
    }
    items += DrawerItem(DashboardRoute.Notifications.route, "Notifications", Lucide.Bell, "My account")
    items += DrawerItem(DashboardRoute.Fines.route, "Fines", Lucide.CircleDollarSign)
    items += DrawerItem(DashboardRoute.Clearance.route, "Clearance status", Lucide.BadgeCheck)
    return items
}
