package com.example.stismartlib.ui.dashboard.overview
import com.composables.icons.lucide.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.models.BorrowStatus
import com.example.stismartlib.data.models.FineStatus
import com.example.stismartlib.data.models.PrintStatus
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.Shape2Xl
import com.example.stismartlib.ui.components.Shape3Xl
import com.example.stismartlib.ui.components.ShapePill
import com.example.stismartlib.ui.components.ShapeXl
import com.example.stismartlib.ui.components.StatCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.dashboard.components.BookCoverPlaceholder
import com.example.stismartlib.ui.theme.AccentYellow
import com.example.stismartlib.ui.theme.EyebrowStyle
import com.example.stismartlib.ui.theme.PrimaryBlue
import com.example.stismartlib.ui.theme.PrimaryBlue5

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
    val currentLoan = activeLoans.firstOrNull()
    val slotsRemaining = (2 - activeLoans.size).coerceAtLeast(0)
    val recommendedBooks = repository.books.take(3)
    val firstName = repository.session.userName.trim().split(Regex("\\s+")).firstOrNull().orEmpty()
        .ifBlank { "Library user" }
    val latestNotification = repository.notifications.firstOrNull()
    val reservation = repository.reservations.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            PageHeader(
                eyebrow = "Student workspace",
                title = "Good day, $firstName!"
            )
        }

        // Hero — matches UserDashboardPage navy banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shape3Xl)
                    .background(PrimaryBlue)
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .clip(ShapePill)
                            .background(Color.White.copy(alpha = 0.10f))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), ShapePill)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Lucide.Sparkles, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Your library, made smarter", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Your books, requests, updates, and library visit in one place.",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        lineHeight = 30.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Check your account and explore available library resources.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PortalButton(
                        text = "Explore catalog",
                        onClick = onNavigateToBookCatalog,
                        variant = PortalButtonVariant.Yellow,
                        leadingIcon = Lucide.ArrowRight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PortalButton(
                        text = "Browse research",
                        onClick = onNavigateToResearchCatalog,
                        variant = PortalButtonVariant.Ghost,
                        modifier = Modifier
                            .clip(ShapeXl)
                            .background(Color.White.copy(alpha = 0.10f))
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(Shape2Xl)
                                .background(Color.White.copy(alpha = 0.10f))
                                .border(1.dp, Color.White.copy(alpha = 0.10f), Shape2Xl)
                                .padding(16.dp)
                        ) {
                            Text("Library occupancy", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text("—", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Unavailable", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(Shape2Xl)
                                .background(Color.White.copy(alpha = 0.10f))
                                .border(1.dp, Color.White.copy(alpha = 0.10f), Shape2Xl)
                                .padding(16.dp)
                        ) {
                            Text("Clearance status", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Lucide.CircleCheck, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(
                                    if (outstanding == 0.0) "Cleared" else "Hold",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                if (outstanding == 0.0) "No clearance block" else "Resolve account obligations",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Stats 2×2 — web grid-cols-2
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    "Active loans",
                    activeLoans.size.toString(),
                    if (slotsRemaining == 1) "1 slot remaining" else "$slotsRemaining slots remaining",
                    Lucide.BookOpen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    "Reservations",
                    repository.reservations.size.toString(),
                    null,
                    Lucide.BookMarked,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    "Unread updates",
                    unread.toString(),
                    null,
                    Lucide.Bell,
                    highlight = unread > 0,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    "Outstanding fines",
                    "₱${"%.2f".format(outstanding)}",
                    null,
                    Lucide.CircleDollarSign,
                    highlight = outstanding > 0,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recommendations (web order: before loan cards)
        item {
            SectionCard(contentPadding = 20.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "RECOMMENDED FOR YOU",
                            style = EyebrowStyle,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Available to borrow",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        "View all",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .sizeIn(minHeight = 48.dp)
                            .clickable(onClick = onNavigateToBookCatalog)
                            .padding(vertical = 12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (recommendedBooks.isEmpty()) {
                    EmptyLine("No available recommendations right now.")
                } else {
                    recommendedBooks.forEach { book ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .sizeIn(minHeight = 48.dp)
                                .clickable { onNavigateToBookCatalog() }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BookCoverPlaceholder(
                                title = book.title,
                                color = Color(book.coverColor),
                                modifier = Modifier
                                    .size(width = 48.dp, height = 64.dp)
                                    .clip(ShapeXl)
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Column {
                                Text(book.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(book.author, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), fontSize = 11.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PortalButton(
                        text = "Browse catalog",
                        onClick = onNavigateToBookCatalog,
                        leadingIcon = Lucide.Search
                    )
                    PortalButton(
                        text = "Borrow cart",
                        onClick = onNavigateToBorrowProcess,
                        variant = PortalButtonVariant.Secondary,
                        leadingIcon = Lucide.ShoppingBag
                    )
                }
            }
        }

        // Current loan
        item {
            SectionCard(contentPadding = 20.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "CURRENT LOAN",
                            style = EyebrowStyle,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Return reminder",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (currentLoan != null) {
                        StatusBadge(
                            currentLoan.borrowStatus.label,
                            warning = currentLoan.borrowStatus == BorrowStatus.OVERDUE
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (currentLoan == null) {
                    EmptyLine("You have no active borrowed books.")
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shape2Xl)
                            .background(PrimaryBlue5)
                            .padding(16.dp)
                    ) {
                        BookCoverPlaceholder(
                            title = currentLoan.book.title,
                            color = Color(currentLoan.book.coverColor),
                            modifier = Modifier
                                .size(width = 80.dp, height = 112.dp)
                                .clip(ShapeXl)
                                .border(1.dp, MaterialTheme.colorScheme.outline, ShapeXl)
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        Column {
                            Text(currentLoan.book.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text(currentLoan.book.author, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Lucide.CalendarClock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Due ${currentLoan.dueDate}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Lucide.MapPin, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(currentLoan.book.shelfLocation, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    PortalButton(
                        text = "Return book",
                        onClick = onNavigateToReturnBook,
                        variant = PortalButtonVariant.Secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Print service
        item {
            SectionCard(contentPadding = 20.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "PRINT SERVICE",
                            style = EyebrowStyle,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Latest request",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(Lucide.Printer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(21.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (readyPrint == null) {
                    EmptyLine("No active print request.")
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shape2Xl)
                            .border(1.dp, MaterialTheme.colorScheme.outline, Shape2Xl)
                            .background(PrimaryBlue5)
                            .padding(16.dp)
                    ) {
                        Text(readyPrint.fileName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${readyPrint.copies} copies · ${readyPrint.printType}",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "₱${"%.2f".format(readyPrint.totalCost)}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                PortalButton(
                    text = "View request",
                    onClick = onNavigateToPrintingService,
                    variant = PortalButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Reservation + notifications
        item {
            SectionCard(contentPadding = 20.dp) {
                Text(
                    "RESERVATION",
                    style = EyebrowStyle,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Queue and pickup",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (reservation == null) {
                    EmptyLine("No active reservation.")
                } else {
                    Text(reservation.book.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Queue position #${reservation.queuePosition}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PortalButton(
                        text = "View reservations",
                        onClick = onNavigateToReserveBook,
                        variant = PortalButtonVariant.Secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            SectionCard(contentPadding = 20.dp) {
                Text(
                    "LATEST UPDATE",
                    style = EyebrowStyle,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Notifications",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (latestNotification == null) {
                    EmptyLine("You are all caught up.")
                } else {
                    Text(latestNotification.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        latestNotification.message,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "View notifications",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .sizeIn(minHeight = 48.dp)
                        .clickable(onClick = onNavigateToNotifications)
                        .padding(vertical = 12.dp)
                )
            }
        }

        // Recent borrowing
        item {
            SectionCard(contentPadding = 20.dp) {
                Text(
                    "RECENT BORROWING",
                    style = EyebrowStyle,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "History preview",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (repository.borrowingHistory.isEmpty()) {
                    EmptyLine("No borrowing history yet.")
                } else {
                    repository.borrowingHistory.take(3).forEach { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(record.book.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(record.borrowDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                            }
                            StatusBadge(
                                record.borrowStatus.label,
                                warning = record.borrowStatus == BorrowStatus.OVERDUE
                            )
                        }
                    }
                }
                Text(
                    "View full history",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .sizeIn(minHeight = 48.dp)
                        .clickable(onClick = onNavigateToBorrowingHistory)
                        .padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyLine(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    )
}

@Composable
private fun FacultyOverview() {
    Column(modifier = Modifier.padding(16.dp)) {
        PageHeader(eyebrow = "Faculty workspace", title = "Good day!")
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Shape3Xl)
                .background(PrimaryBlue)
                .padding(24.dp)
        ) {
            Column {
                Icon(Lucide.ShieldCheck, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "AUTHORIZED WORKSPACE",
                    color = AccentYellow,
                    style = EyebrowStyle
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Welcome to your faculty dashboard.",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    lineHeight = 34.sp
                )
            }
        }
    }
}
