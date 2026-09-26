package com.example.stismartlib.ui.dashboard.returnbook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.data.models.BorrowStatus
import com.example.stismartlib.data.models.BorrowingRecord
import com.example.stismartlib.ui.components.AppDialog
import com.example.stismartlib.ui.components.AppDialogType
import com.example.stismartlib.ui.components.PrimaryButton
import com.example.stismartlib.ui.dashboard.components.BookCoverPlaceholder
import com.example.stismartlib.ui.dashboard.components.EmptyStateMessage
import com.example.stismartlib.ui.dashboard.components.InfoRow
import com.example.stismartlib.ui.dashboard.components.ProcessStepCard
import com.example.stismartlib.ui.dashboard.components.SectionCard
import com.example.stismartlib.ui.dashboard.components.StatusChip
import com.example.stismartlib.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnBookScreen(
    onBack: () -> Unit
) {
    val repository = rememberLibraryRepository()
    var selectedRecord by remember { mutableStateOf<BorrowingRecord?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val allBorrowings = repository.borrowingHistory

    val activeBorrowings = allBorrowings.filter {
        it.borrowStatus == BorrowStatus.BORROWED || it.borrowStatus == BorrowStatus.OVERDUE
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Return Book Process") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionCard(title = "Return Steps") {
                    ProcessStepCard(
                        stepNumber = 1,
                        title = "Select Book to Return",
                        description = "Choose a currently borrowed book from the list below.",
                        isActive = true
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    ProcessStepCard(
                        stepNumber = 2,
                        title = "Bring Book to Counter",
                        description = "Return the physical book to the library borrowing desk."
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    ProcessStepCard(
                        stepNumber = 3,
                        title = "Barcode Scan",
                        description = "The librarian scans the book barcode to confirm the return."
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    ProcessStepCard(
                        stepNumber = 4,
                        title = "Return Complete",
                        description = "Any applicable fines will appear in your borrowing history."
                    )
                }
            }

            item {
                Text(
                    text = "Books to Return",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (activeBorrowings.isEmpty()) {
                item {
                    EmptyStateMessage("You have no books to return right now.")
                }
            } else {
                items(activeBorrowings, key = { it.id }) { record ->
                    ReturnBookCard(
                        record = record,
                        onReturnClick = { selectedRecord = record }
                    )
                }
            }
        }
    }

    AppDialog(
        visible = selectedRecord != null,
        onDismiss = { selectedRecord = null },
        type = AppDialogType.CONFIRM,
        title = "Confirm Return Request",
        message = selectedRecord?.let { record ->
            "Return \"${record.book.title}\"?\n\n" +
                "Please bring the book to the library counter. " +
                "A librarian will scan the barcode to complete the return.\n\n" +
                "Due date: ${record.dueDate}"
        }.orEmpty(),
        confirmText = "Confirm",
        dismissText = "Cancel",
        onConfirm = {
            selectedRecord = null
            showSuccessDialog = true
        }
    )

    AppDialog(
        visible = showSuccessDialog,
        onDismiss = { showSuccessDialog = false },
        type = AppDialogType.SUCCESS,
        title = "Return Submitted",
        message = "Your return request has been noted.\n\n" +
            "Please proceed to the library counter with the book. " +
            "The librarian will finalize the return after scanning.",
        confirmText = "Got It"
    )
}

@Composable
private fun ReturnBookCard(
    record: BorrowingRecord,
    onReturnClick: () -> Unit
) {
    val statusColor = when (record.borrowStatus) {
        BorrowStatus.OVERDUE -> Color(0xFFE03131)
        else -> PrimaryBlue
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD0EBFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row {
                BookCoverPlaceholder(
                    title = record.book.title,
                    color = Color(record.book.coverColor),
                    modifier = Modifier.size(64.dp)
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(record.book.title, fontWeight = FontWeight.Bold)
                    Text(record.book.author, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    InfoRow("Borrow Date", record.borrowDate)
                    InfoRow("Due Date", record.dueDate)
                    StatusChip(record.borrowStatus.label, statusColor)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            PrimaryButton(
                text = "Initiate Return",
                onClick = onReturnClick,
                useAccentStyle = true
            )
        }
    }
}
