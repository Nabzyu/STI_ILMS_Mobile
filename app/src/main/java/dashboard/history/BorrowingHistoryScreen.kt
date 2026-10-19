package com.example.stismartlib.ui.dashboard.history

import com.composables.icons.lucide.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.models.BorrowStatus
import com.example.stismartlib.data.models.BorrowingRecord
import com.example.stismartlib.data.models.FineStatus
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsListRow
import com.example.stismartlib.ui.components.ElmsLoadingBlock
import com.example.stismartlib.ui.components.ElmsPillTab
import com.example.stismartlib.ui.components.ElmsPullRefresh
import com.example.stismartlib.ui.components.ElmsSectionCard
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.StatCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.dashboard.components.BookCoverPlaceholder
import com.example.stismartlib.ui.feedback.userFacingMessage
import com.example.stismartlib.ui.format.formatDateMedium
import com.example.stismartlib.ui.format.formatDateShort
import kotlinx.coroutines.launch

@Composable
fun BorrowingHistoryScreen() {
    val repository = rememberLibraryRepository()
    val records = repository.borrowingHistory
    val scope = rememberCoroutineScope()
    val loading = repository.isLoading && records.isEmpty()
    val loadError = repository.lastError.takeIf { records.isEmpty() }
    var filter by remember { mutableStateOf("All") }
    var refreshError by remember { mutableStateOf<String?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var busyId by remember { mutableStateOf<String?>(null) }
    var cancelTarget by remember { mutableStateOf<BorrowingRecord?>(null) }
    var cancelReason by remember { mutableStateOf("") }
    var lostTarget by remember { mutableStateOf<BorrowingRecord?>(null) }
    val borrowedCount = records.count {
        it.borrowStatus == BorrowStatus.BORROWED || it.borrowStatus == BorrowStatus.OVERDUE
    }
    val activeFilterCount = records.count {
        it.borrowStatus == BorrowStatus.BORROWED ||
            it.borrowStatus == BorrowStatus.OVERDUE ||
            it.borrowStatus == BorrowStatus.PENDING
    }
    val nextDueRaw = records.firstOrNull { it.returnDate == null }?.dueDate
    val nextDue = nextDueRaw?.let { formatDateShort(it) } ?: "None"
    val unpaid = repository.fines.filter { it.paymentStatus == FineStatus.UNPAID }.sumOf { it.amount }
    val visible = records.filter {
        when (filter) {
            "Active" -> it.borrowStatus == BorrowStatus.BORROWED ||
                it.borrowStatus == BorrowStatus.OVERDUE ||
                it.borrowStatus == BorrowStatus.PENDING
            "Returned" -> it.borrowStatus == BorrowStatus.RETURNED
            else -> true
        }
    }

    if (cancelTarget != null) {
        AlertDialog(
            onDismissRequest = {
                cancelTarget = null
                cancelReason = ""
            },
            title = { Text("Cancel borrow request?") },
            text = {
                Column {
                    Text("Cancel the pending request for \"${cancelTarget!!.book.title}\"?")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Reason (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = cancelTarget ?: return@TextButton
                        val reason = cancelReason
                        cancelTarget = null
                        cancelReason = ""
                        busyId = target.id
                        actionError = null
                        actionMessage = null
                        scope.launch {
                            try {
                                repository.cancelBorrowRequest(target.id, reason)
                                actionMessage = "Borrow request cancelled."
                            } catch (error: Exception) {
                                actionError = userFacingMessage(
                                    error,
                                    "Unable to cancel the borrow request.",
                                )
                            } finally {
                                busyId = null
                            }
                        }
                    },
                ) { Text("Cancel request") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        cancelTarget = null
                        cancelReason = ""
                    },
                ) { Text("Keep") }
            },
        )
    }

    if (lostTarget != null) {
        AlertDialog(
            onDismissRequest = { lostTarget = null },
            title = { Text("Report lost book?") },
            text = {
                Text(
                    "Report \"${lostTarget!!.book.title}\" as lost? Library staff will assess " +
                        "a replacement charge. This cannot be undone from the app.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = lostTarget ?: return@TextButton
                        lostTarget = null
                        busyId = target.id
                        actionError = null
                        actionMessage = null
                        scope.launch {
                            try {
                                repository.reportLostBook(target.id)
                                actionMessage = "Lost book report submitted."
                            } catch (error: Exception) {
                                actionError = userFacingMessage(
                                    error,
                                    "Unable to report the lost book.",
                                )
                            } finally {
                                busyId = null
                            }
                        }
                    },
                ) { Text("Report lost") }
            },
            dismissButton = {
                TextButton(onClick = { lostTarget = null }) { Text("Cancel") }
            },
        )
    }

    ElmsPullRefresh(
        onRefresh = { repository.loadBorrowingHistory() },
        onRefreshError = { refreshError = it },
    ) {
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
                    "Loans, returns, and due dates. Bring books to the counter to return them.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ElmsPillTab("All", filter == "All", { filter = "All" }, count = records.size)
                    ElmsPillTab("Active", filter == "Active", { filter = "Active" }, count = activeFilterCount)
                    ElmsPillTab("Returned", filter == "Returned", { filter = "Returned" })
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        "Borrowed",
                        "$borrowedCount / 2",
                        "Limit",
                        Lucide.BookOpen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        "Next due",
                        nextDue,
                        null,
                        Lucide.Clock3,
                        highlight = borrowedCount > 0,
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
            if (loadError != null) {
                item {
                    ElmsBanner(message = loadError, tone = ElmsBannerTone.Error)
                }
            }
            if (refreshError != null) {
                item {
                    ElmsBanner(message = refreshError.orEmpty(), tone = ElmsBannerTone.Error)
                }
            }
            actionError?.let { message ->
                item { ElmsBanner(message = message, tone = ElmsBannerTone.Error) }
            }
            actionMessage?.let { message ->
                item { ElmsBanner(message = message, tone = ElmsBannerTone.Info) }
            }
            when {
                loading -> item { ElmsLoadingBlock() }
                visible.isEmpty() -> item {
                    if (records.isEmpty()) {
                        ElmsEmptyState(
                            title = "No history yet",
                            description = "Borrowed books will appear here."
                        )
                    } else {
                        ElmsEmptyState(
                            title = "No matching loans",
                            description = "Try another filter.",
                            actionLabel = "Show all",
                            onAction = { filter = "All" }
                        )
                    }
                }
                else -> items(visible, key = { it.id }) { record ->
                    ElmsSectionCard(contentPadding = 8.dp) {
                        ElmsListRow(
                            title = record.book.title,
                            subtitle = "Borrowed ${formatDateShort(record.borrowDate)} · Due ${formatDateMedium(record.dueDate)}",
                            subtitleMaxLines = 2,
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
                        if (record.borrowStatus == BorrowStatus.PENDING) {
                            Spacer(modifier = Modifier.height(8.dp))
                            PortalButton(
                                text = if (busyId == record.id) "Cancelling…" else "Cancel request",
                                onClick = { cancelTarget = record },
                                variant = PortalButtonVariant.Secondary,
                                enabled = busyId == null,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        if (
                            record.borrowStatus == BorrowStatus.BORROWED ||
                            record.borrowStatus == BorrowStatus.OVERDUE
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            PortalButton(
                                text = if (busyId == record.id) "Reporting…" else "Report lost",
                                onClick = { lostTarget = record },
                                variant = PortalButtonVariant.Ghost,
                                enabled = busyId == null,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}
