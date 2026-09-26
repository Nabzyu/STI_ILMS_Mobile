package com.example.stismartlib.ui.dashboard.overview
import com.composables.icons.lucide.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.SampleData
import com.example.stismartlib.data.models.BorrowStatus
import com.example.stismartlib.data.models.FineStatus
import com.example.stismartlib.data.models.PrintStatus
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.StatCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.dashboard.components.BookCoverPlaceholder
import com.example.stismartlib.ui.theme.AccentYellow
import com.example.stismartlib.ui.theme.PrimaryBlue

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

    val activeLoans = SampleData.borrowingHistory.filter {
        it.borrowStatus == BorrowStatus.BORROWED || it.borrowStatus == BorrowStatus.OVERDUE
    }
    val unread = SampleData.notifications.count { !it.isRead }
    val outstanding = repository.fines.filter { it.paymentStatus == FineStatus.UNPAID }.sumOf { it.amount }
    val readyPrint = SampleData.printHistory.firstOrNull { it.status == PrintStatus.READY }
    val currentLoan = activeLoans.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PageHeader(
                eyebrow = "Student workspace",
                title = "Good morning, John!",
                description = "Here is what is happening with your library account today."
            )
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(PrimaryBlue)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.10f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Lucide.Sparkles, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Your library, made smarter", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "One place for books, research, printing, and every library visit.",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Search the collection before you arrive, track your transactions, and keep your clearance on schedule.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
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
                        leadingIcon = null
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.10f))
                                .padding(12.dp)
                        ) {
                            Text("Library occupancy", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text("42 / 80", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { 0.52f },
                                color = AccentYellow,
                                trackColor = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.10f))
                                .padding(12.dp)
                        ) {
                            Text("Clearance status", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Lucide.CircleCheck, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(if (outstanding == 0.0) "Cleared" else "Hold", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Text(
                                if (outstanding == 0.0) "No outstanding balance" else "Outstanding ₱${"%.2f".format(outstanding)}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
        item {
            StatCard("Active loans", activeLoans.size.toString(), "1 slot remaining", Lucide.BookOpen)
        }
        item {
            StatCard("Reservations", repository.reservations.size.toString(), "Queue in reservations", Lucide.BookMarked)
        }
        item {
            StatCard("Unread updates", unread.toString(), "Library alerts", Lucide.Bell, highlight = unread > 0)
        }
        item {
            StatCard("Outstanding fines", "₱${"%.2f".format(outstanding)}", if (outstanding == 0.0) "Account in good standing" else "Pay at the library counter", Lucide.CircleDollarSign, highlight = outstanding > 0)
        }
        item {
            SectionCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("CURRENT LOAN", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("Return reminder", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    if (currentLoan != null) StatusBadge(currentLoan.borrowStatus.label, warning = currentLoan.borrowStatus == BorrowStatus.OVERDUE)
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (currentLoan == null) {
                    Text("You have no active loans.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(PrimaryBlue.copy(alpha = 0.05f))
                            .padding(12.dp)
                    ) {
                        BookCoverPlaceholder(title = currentLoan.book.title, color = Color(currentLoan.book.coverColor), modifier = Modifier.size(width = 72.dp, height = 96.dp))
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text(currentLoan.book.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text(currentLoan.book.author, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Lucide.CalendarClock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Due ${currentLoan.dueDate}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Lucide.MapPin, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Shelf ${currentLoan.book.shelfLocation}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    PortalButton(text = "Return book", onClick = onNavigateToReturnBook, variant = PortalButtonVariant.Secondary, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        item {
            SectionCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("PRINT SERVICE", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(if (readyPrint != null) "Ready for pickup" else "No pickup waiting", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Icon(Lucide.Printer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }
                if (readyPrint != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(readyPrint.fileName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    Text("${readyPrint.copies} copies · ${readyPrint.printType}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
                    Text("₱${"%.2f".format(readyPrint.totalCost)}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                PortalButton(text = "View request", onClick = onNavigateToPrintingService, variant = PortalButtonVariant.Secondary, modifier = Modifier.fillMaxWidth())
            }
        }
        item {
            SectionCard {
                Text("QUICK ACTIONS", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("What would you like to do?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                QuickRow("Browse catalog", "Find books and check live availability", Lucide.Search, onNavigateToBookCatalog)
                QuickRow("Request a print", "Upload a document for library pickup", Lucide.Printer, onNavigateToPrintingService)
                QuickRow("Scan attendance", "Record your library visit with QR", Lucide.QrCode, onNavigateToQrLogin)
                QuickRow("Borrowing history", "Track due dates and returns", Lucide.CalendarClock, onNavigateToBorrowingHistory)
            }
        }
        item {
            SectionCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("RECOMMENDED FOR BSIT", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Continue discovering", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Text("View all", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onNavigateToBookCatalog))
                }
                Spacer(modifier = Modifier.height(12.dp))
                SampleData.books.take(3).forEach { book ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToBookCatalog() }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BookCoverPlaceholder(title = book.title, color = Color(book.coverColor), modifier = Modifier.size(width = 48.dp, height = 64.dp))
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text(book.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(book.author, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickRow(label: String, detail: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryBlue.copy(alpha = 0.06f))
                .padding(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(detail, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
        }
        Icon(Lucide.ArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun FacultyOverview() {
    Column(modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(PrimaryBlue)
                .padding(24.dp)
        ) {
            Column {
                Icon(Lucide.ShieldCheck, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("AUTHORIZED WORKSPACE", color = AccentYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.4.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Welcome to your faculty dashboard.", color = Color.White, fontWeight = FontWeight.Black, fontSize = 28.sp, lineHeight = 34.sp)
            }
        }
    }
}
