package com.example.stismartlib.ui.dashboard.account
import com.composables.icons.lucide.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import com.example.stismartlib.data.models.BorrowStatus
import com.example.stismartlib.data.models.FineStatus
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.StatCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.theme.AccentYellow
import com.example.stismartlib.ui.theme.PrimaryBlue

@Composable
fun ClearanceScreen() {
    val repository = rememberLibraryRepository()
    val activeLoans = repository.borrowingHistory.filter {
        it.borrowStatus == BorrowStatus.BORROWED || it.borrowStatus == BorrowStatus.OVERDUE
    }
    val unpaid = repository.fines.filter { it.paymentStatus == FineStatus.UNPAID }
    val outstanding = unpaid.sumOf { it.amount }
    val cleared = activeLoans.isEmpty() && outstanding == 0.0

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            PageHeader(
                eyebrow = "Account standing",
                title = "Library clearance",
                description = "Computed from current unreturned books and outstanding fines."
            )
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(PrimaryBlue)
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Lucide.BadgeCheck, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(16.dp))
                    Text(
                        if (cleared) " CLEARED" else " BLOCKED",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    if (cleared) "You have no library obligations." else "Your clearance is currently blocked.",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (cleared) "This status uses your current loans and fine records." else "Return unreturned books and settle unpaid fines at the library.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
        item { StatCard("Unreturned books", activeLoans.size.toString(), icon = Lucide.BookOpen, note = null) }
        item { StatCard("Outstanding fines", "₱${"%.2f".format(outstanding)}", icon = Lucide.CircleDollarSign, note = null) }
        item { StatCard("Lost-book charges", "₱0.00", icon = Lucide.ShieldAlert, note = null) }
        item {
            SectionCard {
                Text("Unreturned books", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (activeLoans.isEmpty()) {
                    Text("No unreturned books.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                } else {
                    activeLoans.forEach { loan ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(loan.book.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text("Due ${loan.dueDate}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
                            StatusBadge(loan.borrowStatus.label, warning = loan.borrowStatus == BorrowStatus.OVERDUE)
                        }
                    }
                }
            }
        }
        item {
            SectionCard {
                Text("Outstanding charges", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (unpaid.isEmpty()) {
                    Text("No unpaid charges.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                } else {
                    unpaid.forEach { fine ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(fine.bookTitle, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("₱${"%.2f".format(fine.amount)}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        item {
            SectionCard {
                Row {
                    Icon(Lucide.Clock3, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text("How clearance is determined", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text(
                            "Any unreturned book or unpaid fine blocks clearance. Authorized staff may record a reasoned override, and its history stays visible for audit.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
