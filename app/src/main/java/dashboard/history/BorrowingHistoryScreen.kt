package com.example.stismartlib.ui.dashboard.history

import com.composables.icons.lucide.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.models.BorrowStatus
import com.example.stismartlib.data.models.FineStatus
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsListRow
import com.example.stismartlib.ui.components.ElmsPillTab
import com.example.stismartlib.ui.components.ElmsSectionCard
import com.example.stismartlib.ui.components.StatCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.dashboard.components.BookCoverPlaceholder

@Composable
fun BorrowingHistoryScreen() {
    val repository = rememberLibraryRepository()
    val records = repository.borrowingHistory
    var filter by remember { mutableStateOf("All") }
    val active = records.count {
        it.borrowStatus == BorrowStatus.BORROWED || it.borrowStatus == BorrowStatus.OVERDUE
    }
    val nextDue = records.firstOrNull { it.returnDate == null }?.dueDate ?: "None"
    val unpaid = repository.fines.filter { it.paymentStatus == FineStatus.UNPAID }.sumOf { it.amount }
    val visible = records.filter {
        when (filter) {
            "Active" -> it.borrowStatus == BorrowStatus.BORROWED || it.borrowStatus == BorrowStatus.OVERDUE
            "Returned" -> it.borrowStatus == BorrowStatus.RETURNED
            else -> true
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Activity",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Loans, returns, and due dates",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ElmsPillTab("All", filter == "All", { filter = "All" }, count = records.size)
                ElmsPillTab("Active", filter == "Active", { filter = "Active" }, count = active)
                ElmsPillTab("Returned", filter == "Returned", { filter = "Returned" })
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    "Borrowed",
                    "$active / 2",
                    "Limit",
                    Lucide.BookOpen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    "Next due",
                    nextDue,
                    null,
                    Lucide.Clock3,
                    highlight = active > 0,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            StatCard(
                "Unpaid fines",
                "₱${"%.2f".format(unpaid)}",
                if (unpaid == 0.0) "No outstanding charges" else "Pay at the counter",
                Lucide.CircleDollarSign,
                highlight = unpaid > 0
            )
        }
        if (visible.isEmpty()) {
            item {
                ElmsEmptyState(
                    title = "No history yet",
                    description = "Borrowed books will appear here."
                )
            }
        } else {
            items(visible, key = { it.id }) { record ->
                ElmsSectionCard(contentPadding = 8.dp) {
                    ElmsListRow(
                        title = record.book.title,
                        subtitle = "Borrowed ${record.borrowDate} · Due ${record.dueDate}",
                        leading = {
                            BookCoverPlaceholder(
                                title = record.book.title,
                                color = Color(record.book.coverColor),
                                modifier = Modifier.size(52.dp)
                            )
                        },
                        trailing = {
                            StatusBadge(
                                record.borrowStatus.label,
                                warning = record.borrowStatus == BorrowStatus.OVERDUE
                            )
                        }
                    )
                }
            }
        }
    }
}
