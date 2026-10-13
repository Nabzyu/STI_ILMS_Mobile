package com.example.stismartlib.ui.dashboard

import com.composables.icons.lucide.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stismartlib.R
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.components.ElmsCountBadge
import com.example.stismartlib.ui.components.ShapeXl
import com.example.stismartlib.ui.components.StiMarkTile
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
import com.example.stismartlib.ui.dashboard.reserve.ReserveBookScreen
import com.example.stismartlib.ui.dashboard.research.ResearchCatalogScreen
import com.example.stismartlib.ui.dashboard.research.ResearchDetailScreen
import com.example.stismartlib.ui.dashboard.returnbook.ReturnBookScreen
import com.example.stismartlib.ui.dashboard.search.GlobalSearchScreen
import com.example.stismartlib.ui.theme.ElmsCyan
import com.example.stismartlib.ui.theme.LocalThemeController
import com.example.stismartlib.ui.theme.StiMarkBlue
import com.example.stismartlib.ui.theme.StiYellow
import com.example.stismartlib.ui.theme.ThemeMode
import com.example.stismartlib.ui.theme.isAppDarkTheme
import kotlinx.coroutines.launch

private data class DrawerItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val section: String? = null
)

private data class BottomDest(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val opensDrawer: Boolean = false
)

/**
 * Bottom-bar / drawer navigation that does not get stuck on stacked screens
 * (e.g. Notifications on top of Overview). Prefer popping back to an existing
 * destination before using restoreState navigate.
 */
private fun NavController.navigateDashboardRoute(route: String) {
    if (currentDestination?.route == route) return
    if (popBackStack(route, inclusive = false)) return
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavController.openStackedRoute(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) {
        launchSingleTop = true
    }
}

@Composable
fun DashboardScreen(
    userType: UserType,
    onLogout: () -> Unit
) {
    val repository = rememberLibraryRepository()
    repository.setUserType(userType)
    LaunchedEffect(repository.session.accountId) {
        if (repository.session.isAuthenticated) {
            runCatching { repository.refreshStudentData() }
        }
    }
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
    val unreadCount = repository.notifications.count { !it.isRead }
    val themeController = LocalThemeController.current
    val darkTheme = isAppDarkTheme()
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val bottomItems = listOf(
        BottomDest(DashboardRoute.Overview.route, "Home", Lucide.LayoutDashboard),
        BottomDest(DashboardRoute.BookCatalog.route, "Books", Lucide.BookOpen),
        BottomDest(DashboardRoute.ResearchCatalog.route, "Research", Lucide.FileText),
        BottomDest(DashboardRoute.BorrowingHistory.route, "Activity", Lucide.CalendarClock),
        BottomDest("__more__", "More", Lucide.Menu, opensDrawer = true)
    )

    val selectedBottom = when {
        currentRoute == DashboardRoute.Overview.route -> DashboardRoute.Overview.route
        currentRoute == DashboardRoute.BookCatalog.route ||
            currentRoute?.startsWith("book_detail") == true ||
            currentRoute == DashboardRoute.BookCart.route -> DashboardRoute.BookCatalog.route
        currentRoute == DashboardRoute.ResearchCatalog.route ||
            currentRoute?.startsWith("research_detail") == true -> DashboardRoute.ResearchCatalog.route
        currentRoute == DashboardRoute.BorrowingHistory.route ||
            currentRoute == DashboardRoute.ReserveBook.route ||
            currentRoute == DashboardRoute.BorrowProcess.route ||
            currentRoute == DashboardRoute.ReturnBook.route -> DashboardRoute.BorrowingHistory.route
        else -> null
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Sign out?") },
            text = { Text("You will need to sign in again to access your library account.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    }
                ) { Text("Sign out", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                Column(modifier = Modifier.fillMaxHeight()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderIconButton(
                            onClick = {
                                dashboardNavController.openStackedRoute(DashboardRoute.Search.route)
                                scope.launch { drawerState.close() }
                            },
                            contentDescription = "Search"
                        ) {
                            Icon(
                                Lucide.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        HeaderIconButton(
                            onClick = { scope.launch { drawerState.close() } },
                            contentDescription = "Close navigation"
                        ) {
                            Icon(
                                Lucide.X,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        items.forEach { item ->
                            if (item.section != null) {
                                Text(
                                    text = item.section.uppercase(),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.4.sp,
                                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
                                )
                            }
                            val selected = current?.route == item.route
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .sizeIn(minHeight = 48.dp)
                                    .clip(ShapeXl)
                                    .background(
                                        if (selected) ElmsCyan.copy(alpha = 0.15f) else Color.Transparent
                                    )
                                    .clickable {
                                        // Secondary screens (notifications, fines, …) stack; tabs restore.
                                        val stacksOverTabs = item.route in setOf(
                                            DashboardRoute.Notifications.route,
                                            DashboardRoute.Fines.route,
                                            DashboardRoute.Clearance.route,
                                            DashboardRoute.Profile.route,
                                            DashboardRoute.ReserveBook.route,
                                            DashboardRoute.BorrowProcess.route,
                                            DashboardRoute.ReturnBook.route,
                                            DashboardRoute.PrintingService.route,
                                            DashboardRoute.QrLogin.route,
                                            DashboardRoute.Search.route,
                                        )
                                        if (stacksOverTabs) {
                                            dashboardNavController.openStackedRoute(item.route)
                                        } else {
                                            dashboardNavController.navigateDashboardRoute(item.route)
                                        }
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) ElmsCyan else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    item.label,
                                    color = if (selected) ElmsCyan else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .sizeIn(minHeight = 48.dp)
                                .clip(ShapeXl)
                                .clickable {
                                    dashboardNavController.openStackedRoute(DashboardRoute.QrLogin.route)
                                    scope.launch { drawerState.close() }
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Lucide.QrCode,
                                contentDescription = "My QR",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("My QR attendance", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .sizeIn(minHeight = 48.dp)
                                .clip(ShapeXl)
                                .clickable {
                                    themeController.themeMode =
                                        if (darkTheme) ThemeMode.LIGHT else ThemeMode.DARK
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (darkTheme) Lucide.Sun else Lucide.Moon,
                                contentDescription = if (darkTheme) "Switch to light mode" else "Switch to dark mode",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                if (darkTheme) "Light mode" else "Dark mode",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .sizeIn(minHeight = 48.dp)
                                .clip(ShapeXl)
                                .clickable { showLogoutConfirm = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Lucide.LogOut,
                                contentDescription = "Sign out",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Log out", color = MaterialTheme.colorScheme.error, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .sizeIn(minHeight = 48.dp)
                                .clip(ShapeXl)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    dashboardNavController.openStackedRoute(DashboardRoute.Profile.route)
                                    scope.launch { drawerState.close() }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(StiYellow),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    userType.displayName.take(2).uppercase(),
                                    color = StiMarkBlue,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    repository.session.userName.ifBlank { "STI account" },
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    userType.displayName,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeaderIconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        contentDescription = "Open navigation"
                    ) {
                        Icon(
                            Lucide.Menu,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    StiMarkTile(
                        size = 36.dp,
                        contentDescription = stringResource(R.string.system_full_name),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    HeaderIconButton(
                        onClick = {
                            dashboardNavController.openStackedRoute(DashboardRoute.Notifications.route)
                        },
                        contentDescription = "Notifications"
                    ) {
                        Box {
                            Icon(
                                Lucide.Bell,
                                contentDescription = null,
                                tint = ElmsCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            ElmsCountBadge(
                                count = unreadCount,
                                modifier = Modifier.align(Alignment.TopEnd)
                            )
                        }
                    }
                    HeaderIconButton(
                        onClick = {
                            dashboardNavController.openStackedRoute(DashboardRoute.Profile.route)
                        },
                        contentDescription = "My profile"
                    ) {
                        val initials = repository.session.userName
                            .trim()
                            .split(Regex("\\s+"))
                            .filter { it.isNotEmpty() }
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
                            .joinToString("")
                            .ifBlank { userType.displayName.take(2).uppercase() }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(StiYellow)
                                .border(1.dp, ElmsCyan.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = StiMarkBlue,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                            )
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp
                ) {
                    bottomItems.forEach { dest ->
                        val selected = !dest.opensDrawer && selectedBottom == dest.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (dest.opensDrawer) {
                                    scope.launch { drawerState.open() }
                                } else {
                                    dashboardNavController.navigateDashboardRoute(dest.route)
                                }
                            },
                            icon = {
                                Icon(dest.icon, contentDescription = dest.label)
                            },
                            label = { Text(dest.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ElmsCyan,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = stringResource(R.string.system_identity_line),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.system_full_descriptor),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                NavHost(
                    navController = dashboardNavController,
                    startDestination = DashboardRoute.Overview.route,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    composable(DashboardRoute.Overview.route) {
                        DashboardOverviewScreen(
                            onNavigateToBookCatalog = {
                                dashboardNavController.navigate(DashboardRoute.BookCatalog.route)
                            },
                            onNavigateToResearchCatalog = {
                                dashboardNavController.navigate(DashboardRoute.ResearchCatalog.route)
                            },
                            onNavigateToBorrowingHistory = {
                                dashboardNavController.navigate(DashboardRoute.BorrowingHistory.route)
                            },
                            onNavigateToBorrowProcess = {
                                dashboardNavController.navigate(DashboardRoute.BorrowProcess.route)
                            },
                            onNavigateToReturnBook = {
                                dashboardNavController.navigate(DashboardRoute.ReturnBook.route)
                            },
                            onNavigateToReserveBook = {
                                dashboardNavController.navigate(DashboardRoute.ReserveBook.route)
                            },
                            onNavigateToPrintingService = {
                                dashboardNavController.navigate(DashboardRoute.PrintingService.route)
                            },
                            onNavigateToQrLogin = {
                                dashboardNavController.navigate(DashboardRoute.QrLogin.route)
                            },
                            onNavigateToNotifications = {
                                dashboardNavController.openStackedRoute(DashboardRoute.Notifications.route)
                            },
                            onLogout = { showLogoutConfirm = true }
                        )
                    }
                    composable(DashboardRoute.BookCatalog.route) {
                        BookCatalogScreen(
                            onBookClick = {
                                dashboardNavController.navigate(DashboardRoute.BookDetail.create(it))
                            },
                            onCartClick = {
                                dashboardNavController.navigate(DashboardRoute.BookCart.route)
                            }
                        )
                    }
                    composable(
                        route = DashboardRoute.BookDetail.route,
                        arguments = listOf(navArgument("bookId") { type = NavType.StringType })
                    ) { entry ->
                        BookDetailScreen(
                            bookId = entry.arguments?.getString("bookId").orEmpty(),
                            onBack = { dashboardNavController.popBackStack() },
                            onCartClick = {
                                dashboardNavController.navigate(DashboardRoute.BookCart.route)
                            }
                        )
                    }
                    composable(DashboardRoute.BookCart.route) {
                        BookCartScreen(onBack = { dashboardNavController.popBackStack() })
                    }
                    composable(DashboardRoute.BorrowProcess.route) {
                        BorrowProcessScreen(
                            onBack = { dashboardNavController.popBackStack() },
                            onBrowseCatalog = {
                                dashboardNavController.navigate(DashboardRoute.BookCatalog.route)
                            },
                            onOpenCart = {
                                dashboardNavController.navigate(DashboardRoute.BookCart.route)
                            }
                        )
                    }
                    composable(DashboardRoute.ReturnBook.route) {
                        ReturnBookScreen(onBack = { dashboardNavController.popBackStack() })
                    }
                    composable(DashboardRoute.ReserveBook.route) {
                        ReserveBookScreen(
                            onBack = { dashboardNavController.popBackStack() },
                            onBrowseCatalog = {
                                dashboardNavController.navigate(DashboardRoute.BookCatalog.route)
                            }
                        )
                    }
                    composable(DashboardRoute.Notifications.route) {
                        NotificationsScreen(onBack = { dashboardNavController.popBackStack() })
                    }
                    composable(DashboardRoute.Search.route) {
                        GlobalSearchScreen(
                            onBack = { dashboardNavController.popBackStack() },
                            onBookClick = { bookId ->
                                dashboardNavController.navigate(DashboardRoute.BookDetail.create(bookId))
                            },
                            onResearchClick = { researchId ->
                                dashboardNavController.navigate(DashboardRoute.ResearchDetail.create(researchId))
                            },
                            onBrowseCatalog = {
                                dashboardNavController.navigateDashboardRoute(DashboardRoute.BookCatalog.route)
                            },
                        )
                    }
                    composable(DashboardRoute.QrLogin.route) {
                        AttendanceScreen()
                    }
                    composable(DashboardRoute.ResearchCatalog.route) {
                        ResearchCatalogScreen(
                            onResearchClick = {
                                dashboardNavController.navigate(DashboardRoute.ResearchDetail.create(it))
                            }
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
                            onViewHistory = {
                                dashboardNavController.navigate(DashboardRoute.PrintingHistory.route)
                            }
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
                    composable(DashboardRoute.Profile.route) {
                        ProfileScreen()
                    }
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
    }
    items += DrawerItem(DashboardRoute.Profile.route, "My profile", Lucide.User, "My account")
    items += DrawerItem(DashboardRoute.Notifications.route, "Notifications", Lucide.Bell)
    items += DrawerItem(DashboardRoute.Fines.route, "Fines", Lucide.CircleDollarSign)
    items += DrawerItem(DashboardRoute.Clearance.route, "Clearance status", Lucide.BadgeCheck)
    return items
}

@Composable
private fun HeaderIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clip(ShapeXl)
            .clickable(onClick = onClick, onClickLabel = contentDescription)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
