package com.example.stismartlib.ui.dashboard.printing

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.stismartlib.data.models.PrintReceipt
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsLoadingBlock
import com.example.stismartlib.ui.components.ElmsPullRefresh
import com.example.stismartlib.ui.components.ElmsSectionCard
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.feedback.userFacingMessage
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun PrintingHistoryScreen(onBack: () -> Unit) {
    val repository = rememberLibraryRepository()
    val history = repository.printHistory
    val receipts = repository.printReceipts
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cancellingId by remember { mutableStateOf<String?>(null) }
    var downloadingId by remember { mutableStateOf<String?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var selectedReceipt by remember { mutableStateOf<PrintReceipt?>(null) }
    var refreshError by remember { mutableStateOf<String?>(null) }
    val loading = repository.isLoading && history.isEmpty() && receipts.isEmpty()
    val loadError = repository.lastError.takeIf { history.isEmpty() && receipts.isEmpty() }

    LaunchedEffect(Unit) {
        runCatching { repository.loadPrintHistory() }
        runCatching { repository.loadPrintReceipts() }
    }

    selectedReceipt?.let { receipt ->
        AlertDialog(
            onDismissRequest = { selectedReceipt = null },
            title = { Text(receipt.receiptNumber.ifBlank { "Print receipt" }) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("File: ${receipt.fileName}")
                    Text("Amount: ₱${"%.2f".format(receipt.amountReceived)}")
                    Text("Type: ${receipt.printType.ifBlank { "—" }} · ${receipt.paperSize.ifBlank { "—" }}")
                    Text("Copies: ${receipt.copies} · Pages: ${receipt.pageCount}")
                    Text("Received: ${receipt.receivedAt}")
                    if (receipt.receivedBy.isNotBlank()) {
                        Text("Received by: ${receipt.receivedBy}")
                    }
                    if (receipt.verificationCode.isNotBlank()) {
                        Text("Code: ${receipt.verificationCode}")
                    }
                    if (receipt.status.isNotBlank()) {
                        Text("Status: ${receipt.status}")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = selectedReceipt ?: return@TextButton
                        downloadingId = target.id
                        actionError = null
                        scope.launch {
                            try {
                                val bytes = repository.loadPrintReceiptPdf(target.id)
                                val file = File(context.cacheDir, "print-receipt-${target.id}.pdf")
                                file.writeBytes(bytes)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file,
                                )
                                val share = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(share, "Share receipt PDF"))
                            } catch (error: Exception) {
                                actionError = userFacingMessage(error, "Unable to download the receipt.")
                            } finally {
                                downloadingId = null
                            }
                        }
                    },
                ) {
                    Text(if (downloadingId == receipt.id) "Preparing…" else "Share PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedReceipt = null }) { Text("Close") }
            },
        )
    }

    ElmsPullRefresh(
        onRefresh = {
            repository.loadPrintHistory()
            runCatching { repository.loadPrintReceipts() }
        },
        onRefreshError = { refreshError = it },
    ) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            PageHeader(
                eyebrow = "Online service",
                title = "Print history",
                description = "Requests and payment receipts. Cancel while Pending and Unpaid.",
            )
        }
        if (refreshError != null) {
            item { ElmsBanner(message = refreshError.orEmpty(), tone = ElmsBannerTone.Error) }
        }
        if (loadError != null) {
            item { ElmsBanner(message = loadError, tone = ElmsBannerTone.Error) }
        }
        actionError?.let { message ->
            item { ElmsBanner(message = message, tone = ElmsBannerTone.Error) }
        }
        repository.printActionError?.let { message ->
            item { ElmsBanner(message = message, tone = ElmsBannerTone.Error) }
        }
        item {
            Text(
                "Requests",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        when {
            loading -> item { ElmsLoadingBlock() }
            history.isEmpty() -> item {
                ElmsEmptyState(
                    title = "No print requests",
                    description = "Requests you submit will appear here.",
                )
            }
            else -> items(history, key = { it.id }) { request ->
                Column {
                    PrintQueueCard(request)
                    if (request.canCancel) {
                        Spacer(modifier = Modifier.height(8.dp))
                        PortalButton(
                            text = if (cancellingId == request.id) "Cancelling…" else "Cancel request",
                            variant = PortalButtonVariant.Secondary,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = cancellingId == null,
                            onClick = {
                                actionError = null
                                cancellingId = request.id
                                scope.launch {
                                    try {
                                        repository.cancelPrint(request.id)
                                    } catch (error: Exception) {
                                        actionError = userFacingMessage(
                                            error,
                                            "Unable to cancel the print request.",
                                        )
                                    } finally {
                                        cancellingId = null
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Receipts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Paid print jobs with verification details",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }
        when {
            receipts.isEmpty() -> item {
                ElmsEmptyState(
                    title = "No receipts yet",
                    description = "Receipts appear after a print payment is recorded.",
                )
            }
            else -> items(receipts, key = { it.id }) { receipt ->
                ElmsSectionCard(contentPadding = 12.dp) {
                    Text(
                        receipt.receiptNumber.ifBlank { "Receipt #${receipt.id}" },
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        receipt.fileName,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "₱${"%.2f".format(receipt.amountReceived)} · ${receipt.receivedAt}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PortalButton(
                        text = "View receipt",
                        onClick = { selectedReceipt = receipt },
                        variant = PortalButtonVariant.Secondary,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = downloadingId == null,
                    )
                }
            }
        }
        item {
            PortalButton(text = "Back", onClick = onBack, variant = PortalButtonVariant.Secondary)
        }
    }
    }
}
