package com.example.stismartlib.ui.dashboard.account

import com.composables.icons.lucide.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.models.FineStatus
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsLoadingBlock
import com.example.stismartlib.ui.components.ElmsPullRefresh
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.StatCard
import com.example.stismartlib.ui.components.StatusBadge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.stismartlib.data.models.FineReceipt
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.feedback.userFacingMessage
import com.example.stismartlib.ui.files.sharePdf
import com.example.stismartlib.ui.format.formatDateMedium
import kotlinx.coroutines.launch

@Composable
fun FinesScreen() {
    val repository = rememberLibraryRepository()
    val fines = repository.fines
    val loading = repository.isLoading && fines.isEmpty()
    val loadError = repository.lastError.takeIf { fines.isEmpty() }
    var refreshError by remember { mutableStateOf<String?>(null) }
    var downloadingId by remember { mutableStateOf<String?>(null) }
    var receiptError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val assessed = fines.sumOf { it.amount }
    val outstanding = fines.filter { it.paymentStatus == FineStatus.UNPAID }.sumOf { it.amount }
    // A paid fine's amount is its remaining balance (0), so count what the receipts recorded.
    val collected = repository.fineReceipts.filterNot { it.isReversed }.sumOf { it.amount }

    ElmsPullRefresh(
        onRefresh = { repository.loadFines() },
        onRefreshError = { refreshError = it },
    ) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                PageHeader(eyebrow = "Account payments", title = "Library fines")
            }
            item { StatCard("Total assessed", peso(assessed), icon = Lucide.CircleDollarSign, note = null) }
            item { StatCard("Outstanding balance", peso(outstanding), icon = Lucide.TriangleAlert, highlight = outstanding > 0, note = null) }
            item { StatCard("Cash payments recorded", peso(collected), icon = Lucide.Receipt, note = null) }
            item {
                SectionCard {
                    Text("Cash payment at the library counter", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Online payment is not available. Bring your student ID and pay an authorized librarian. A receipt appears here after the cash payment is recorded.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
            item { Text("Fine history", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            if (refreshError != null) {
                item {
                    ElmsBanner(message = refreshError.orEmpty(), tone = ElmsBannerTone.Error)
                }
            }
            if (loadError != null) {
                item {
                    ElmsBanner(message = loadError, tone = ElmsBannerTone.Error)
                }
            }
            when {
                loading -> item { ElmsLoadingBlock() }
                fines.isEmpty() -> item {
                    ElmsEmptyState(
                        title = "No fines",
                        description = "You're all clear — no fine records on file."
                    )
                }
                else -> items(fines, key = { it.id }) { fine ->
                    SectionCard {
                        Text(fine.bookTitle, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text(fine.reason, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                        Text(fine.dateIssued, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(peso(fine.amount), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        StatusBadge(fine.paymentStatus.label, warning = fine.paymentStatus == FineStatus.UNPAID)
                    }
                }
            }
            item {
                FineReceiptsSection(
                    receipts = repository.fineReceipts,
                    loadError = repository.fineReceiptsError,
                    downloadingId = downloadingId,
                    actionError = receiptError,
                    onSharePdf = { receipt ->
                        if (downloadingId != null) return@FineReceiptsSection
                        downloadingId = receipt.id
                        receiptError = null
                        scope.launch {
                            try {
                                val bytes = repository.loadFineReceiptPdf(receipt.id)
                                sharePdf(context, bytes, "${receipt.number}.pdf", "Share receipt PDF")
                            } catch (error: Exception) {
                                receiptError = userFacingMessage(error, "Unable to download the receipt.")
                            } finally {
                                downloadingId = null
                            }
                        }
                    },
                )
            }
        }
    }
}

private fun peso(amount: Double) = "₱${"%.2f".format(amount)}"

@Composable
private fun FineReceiptsSection(
    receipts: List<FineReceipt>,
    loadError: String?,
    downloadingId: String?,
    actionError: String?,
    onSharePdf: (FineReceipt) -> Unit,
) {
    SectionCard {
        Text("Digital receipts", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        when {
            loadError != null && receipts.isEmpty() -> ElmsBanner(message = loadError, tone = ElmsBannerTone.Error)
            receipts.isEmpty() -> Text(
                "No cash receipts yet.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                fontSize = 14.sp
            )
            else -> receipts.forEachIndexed { index, receipt ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    Text(receipt.number, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Text(
                        "${formatDateMedium(receipt.receivedOn)} · ${receipt.method}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(peso(receipt.amount), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (receipt.status.isNotBlank()) {
                        StatusBadge(receipt.status, warning = receipt.isReversed)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    PortalButton(
                        text = if (downloadingId == receipt.id) "Preparing PDF…" else "Share PDF",
                        onClick = { onSharePdf(receipt) },
                        variant = PortalButtonVariant.Secondary,
                        enabled = downloadingId == null,
                        loading = downloadingId == receipt.id,
                        leadingIcon = Lucide.Download,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        if (actionError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            ElmsBanner(message = actionError, tone = ElmsBannerTone.Error)
        }
    }
}
