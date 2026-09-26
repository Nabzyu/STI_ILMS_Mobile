package com.example.stismartlib.ui.dashboard.history
import com.composables.icons.lucide.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.example.stismartlib.ui.theme.PrimaryBlue

@Composable
fun BorrowingHistoryScreen() {
    val repository = rememberLibraryRepository()
    val records = repository.borrowingHistory
    val active = records.count { it.borrowStatus == BorrowStatus.BORROWED || it.borrowStatus == BorrowStatus.OVERDUE }
    val nextDue = records.firstOrNull { it.returnDate == null }?.dueDate ?: "None"
    val unpaid = repository.fines.filter { it.paymentStatus == FineStatus.UNPAID }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PageHeader(
                eyebrow = "My library",
                title = "Borrowing and return history",
                description = "Track active items, due times, returned books, and penalties."
            )
        }
        item { StatCard("Currently borrowed", "$active / 2", "Student borrowing limit", Lucide.BookOpen) }
        item { StatCard("Next return", nextDue, "Due date", Lucide.Clock3, highlight = active > 0) }
        item { StatCard("Unpaid fines", "₱${"%.2f".format(unpaid)}", "No outstanding charges".takeIf { unpaid == 0.0 } ?: "Pay at the counter", Lucide.CircleDollarSign, highlight = unpaid > 0) }
        items(records, key = { it.id }) { record ->
            SectionCard {
                Text(record.book.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("Copy ${record.book.shelfLocation}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Borrowed ${record.borrowDate}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
                Text("Due ${record.dueDate}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                StatusBadge(record.borrowStatus.label, warning = record.borrowStatus == BorrowStatus.OVERDUE)
            }
        }
    }
}
