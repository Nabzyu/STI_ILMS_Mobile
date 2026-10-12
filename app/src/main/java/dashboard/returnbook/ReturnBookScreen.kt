package com.example.stismartlib.ui.dashboard.returnbook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.stismartlib.data.models.BorrowingRecord
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsListRow
import com.example.stismartlib.ui.components.ElmsSectionCard
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.dashboard.components.BookCoverPlaceholder

@Composable
fun ReturnBookScreen(
    onBack: () -> Unit
) {
    val repository = rememberLibraryRepository()
    var selectedRecord by remember { mutableStateOf<BorrowingRecord?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    val activeBorrowings = repository.borrowingHistory.filter {
        it.borrowStatus == BorrowStatus.BORROWED || it.borrowStatus == BorrowStatus.OVERDUE
    }

    if (selectedRecord != null) {
        AlertDialog(
            onDismissRequest = { selectedRecord = null },
            title = { Text("Confirm return request") },
            text = {
                Text(
                    "Return \"${selectedRecord!!.book.title}\"? Bring the book to the library counter. " +
                        "A librarian will scan the barcode to finish. Due: ${selectedRecord!!.dueDate}"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedRecord = null
                        showSuccess = true
                    }
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { selectedRecord = null }) { Text("Cancel") }
            }
        )
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { showSuccess = false },
            title = { Text("Return submitted") },
            text = {
                Text("Please proceed to the library counter with the book. The librarian will finalize the return after scanning.")
            },
            confirmButton = {
                TextButton(onClick = { showSuccess = false }) { Text("Got it") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Return books",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Select a loan, then bring the book to the counter.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
        item {
            ElmsSectionCard {
                Text("Return steps", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                StepLine("1. Select a borrowed book")
                StepLine("2. Bring it to the library counter")
                StepLine("3. Librarian scans the barcode")
                StepLine("4. Return complete (fines appear in history if any)")
            }
        }
        if (showSuccess) {
            item { ElmsBanner("Return request noted.", tone = ElmsBannerTone.Success) }
        }
        if (activeBorrowings.isEmpty()) {
            item {
                ElmsEmptyState(
                    title = "Nothing to return",
                    description = "You have no active borrowed books.",
                    actionLabel = "Go back",
                    onAction = onBack
                )
            }
        } else {
            items(activeBorrowings, key = { it.id }) { record ->
                ElmsSectionCard(contentPadding = 8.dp) {
                    ElmsListRow(
                        title = record.book.title,
                        subtitle = "${record.book.author} · Due ${record.dueDate}",
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
                    Spacer(modifier = Modifier.height(8.dp))
                    PortalButton(
                        text = "Initiate return",
                        onClick = { selectedRecord = record },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        item {
            PortalButton(
                text = "Back",
                onClick = onBack,
                variant = PortalButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StepLine(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
