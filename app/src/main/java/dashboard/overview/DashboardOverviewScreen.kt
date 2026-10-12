package com.example.stismartlib.ui.dashboard.overview

import com.composables.icons.lucide.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.models.BorrowStatus
import com.example.stismartlib.data.models.FineStatus
import com.example.stismartlib.data.models.PrintStatus
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.components.ElmsListRow
import com.example.stismartlib.ui.components.ElmsPillTab
import com.example.stismartlib.ui.components.ElmsSectionCard
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.Shape2Xl
import com.example.stismartlib.ui.components.ShapePill
import com.example.stismartlib.ui.components.ShapeXl
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.dashboard.components.BookCoverPlaceholder
import com.example.stismartlib.ui.theme.AccentYellow
import com.example.stismartlib.ui.theme.ElmsCyan
import com.example.stismartlib.ui.theme.ElmsCyanSoft

@Composable
fun DashboardOverviewScreen(
    onNavigateToBookCatalog: () -> Unit,
    onNavigateToResearchCatalog: () -> Unit,
    onNavigateToBorrowingHistory: () -> Unit,
    onNavigateToBorrowProcess: () -> Unit,
    onNavigateToReturnBook: () -> Unit,
    onNavigateToReserveBook: () -> Unit,
    onNavigateToPrintingService: () -> Unit,
    onNavigateToQrLogin: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    val repository = rememberLibraryRepository()
    if (repository.session.userType == UserType.FACULTY) {
        FacultyOverview()
        return
    }

    val activeLoans = repository.borrowingHistory.filter {
        it.borrowStatus == BorrowStatus.BORROWED || it.borrowStatus == BorrowStatus.OVERDUE
    }
    val unread = repository.notifications.count { !it.isRead }
    val outstanding = repository.fines.filter { it.paymentStatus == FineStatus.UNPAID }.sumOf { it.amount }
    val readyPrint = repository.printHistory.firstOrNull { it.status == PrintStatus.READY }
    val firstName = repository.session.userName.trim().split(Regex("\\s+")).firstOrNull().orEmpty()
        .ifBlank { "Library user" }
    val latestNotification = repository.notifications.firstOrNull()
    val reservation = repository.reservations.firstOrNull()
    val recommendedBooks = repository.books.take(5)

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ElmsPillTab(label = "Dashboard", selected = true, onClick = {})
                ElmsPillTab(
                    label = "Updates",
                    selected = false,
                    onClick = onNavigateToNotifications,
                    count = unread.takeIf { it > 0 }
                )
            }
        }

        item {
            ElmsSectionCard(contentPadding = 16.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Welcome",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Hi, $firstName",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Shape2Xl)
                        .background(ElmsCyan)
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            "Your library, made smarter",
                            color = AccentYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Books, requests, and visits in one place.",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PortalButton(
                            text = "Explore catalog",
                            onClick = onNavigateToBookCatalog,
                            variant = PortalButtonVariant.Yellow,
                            leadingIcon = Lucide.ArrowRight
                        )
                    }
                }
            }
        }

        item {
            WidgetHeader(
                title = "Active loans",
                badgeCount = activeLoans.size,
                onViewAll = onNavigateToBorrowingHistory
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (activeLoans.isEmpty()) {
                ElmsSectionCard {
                    Text(
                        "No active borrowed books.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PortalButton(
                        text = "Browse catalog",
                        onClick = onNavigateToBookCatalog,
                        variant = PortalButtonVariant.Secondary
                    )
                }
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    activeLoans.take(5).forEach { loan ->
                        ElmsSectionCard(
                            modifier = Modifier.width(260.dp),
                            contentPadding = 12.dp
                        ) {
                            BookCoverPlaceholder(
                                title = loan.book.title,
                                color = Color(loan.book.coverColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(ShapeXl)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                loan.book.title,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            StatusBadge(
                                loan.borrowStatus.label,
                                warning = loan.borrowStatus == BorrowStatus.OVERDUE
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Due ${loan.dueDate}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            PortalButton(
                                text = "Return",
                                onClick = onNavigateToReturnBook,
                                variant = PortalButtonVariant.Secondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        item {
            WidgetHeader(title = "Recommended", onViewAll = onNavigateToBookCatalog)
            Spacer(modifier = Modifier.height(8.dp))
            ElmsSectionCard(contentPadding = 8.dp) {
                if (recommendedBooks.isEmpty()) {
                    Text(
                        "No recommendations yet.",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    recommendedBooks.forEach { book ->
                        ElmsListRow(
                            title = book.title,
                            subtitle = book.author,
                            onClick = onNavigateToBookCatalog,
                            leading = {
                                BookCoverPlaceholder(
                                    title = book.title,
                                    color = Color(book.coverColor),
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickStat(
                    label = "Unread",
                    value = unread.toString(),
                    highlight = unread > 0,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToNotifications
                )
                QuickStat(
                    label = "Fines",
                    value = "₱${"%.0f".format(outstanding)}",
                    highlight = outstanding > 0,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToBorrowingHistory
                )
            }
        }

        item {
            ElmsSectionCard {
                WidgetHeader(title = "Printing", badgeCount = if (readyPrint != null) 1 else 0)
                Spacer(modifier = Modifier.height(8.dp))
                if (readyPrint == null) {
                    Text("No active print request.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(readyPrint.fileName, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "${readyPrint.copies} copies · ₱${"%.2f".format(readyPrint.totalCost)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                PortalButton(
                    text = "Printing service",
                    onClick = onNavigateToPrintingService,
                    variant = PortalButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            ElmsSectionCard {
                WidgetHeader(title = "Reservations", badgeCount = repository.reservations.size)
                Spacer(modifier = Modifier.height(8.dp))
                if (reservation == null) {
                    Text("No active reservation.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(reservation.book.title, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Queue #${reservation.queuePosition}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                PortalButton(
                    text = "View reservations",
                    onClick = onNavigateToReserveBook,
                    variant = PortalButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            ElmsSectionCard {
                WidgetHeader(title = "Latest update", onViewAll = onNavigateToNotifications)
                Spacer(modifier = Modifier.height(8.dp))
                if (latestNotification == null) {
                    Text("You are all caught up.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(latestNotification.title, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        latestNotification.message,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                PortalButton(
                    text = "Borrow cart",
                    onClick = onNavigateToBorrowProcess,
                    leadingIcon = Lucide.ShoppingBag,
                    modifier = Modifier.weight(1f)
                )
                PortalButton(
                    text = "My QR",
                    onClick = onNavigateToQrLogin,
                    variant = PortalButtonVariant.Secondary,
                    leadingIcon = Lucide.QrCode,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            PortalButton(
                text = "Browse research",
                onClick = onNavigateToResearchCatalog,
                variant = PortalButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WidgetHeader(
    title: String,
    badgeCount: Int = 0,
    onViewAll: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    badgeCount.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(ShapePill)
                        .background(ElmsCyan)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        if (onViewAll != null) {
            Text(
                "View all",
                color = ElmsCyan,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier
                    .sizeIn(minHeight = 48.dp)
                    .clickable(onClick = onViewAll)
                    .padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun QuickStat(
    label: String,
    value: String,
    highlight: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(Shape2Xl)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, Shape2Xl)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            value,
            color = if (highlight) ElmsCyan else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
    }
}

@Composable
private fun FacultyOverview() {
    Column(modifier = Modifier.padding(16.dp)) {
        ElmsSectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Lucide.ShieldCheck, contentDescription = null, tint = ElmsCyan, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Faculty workspace", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(
                        "Welcome to your dashboard",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shape2Xl)
                    .background(ElmsCyanSoft)
                    .padding(16.dp)
            ) {
                Text(
                    "Use the navigation bar to browse the catalog, research, and account tools.",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
