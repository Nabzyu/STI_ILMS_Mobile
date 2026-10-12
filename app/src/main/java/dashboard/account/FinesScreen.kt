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
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.StatCard
import com.example.stismartlib.ui.components.StatusBadge

@Composable
fun FinesScreen() {
    val repository = rememberLibraryRepository()
    val fines = repository.fines
    val loading = repository.isLoading && fines.isEmpty()
    val loadError = repository.lastError.takeIf { fines.isEmpty() }
    val assessed = fines.sumOf { it.amount }
    val outstanding = fines.filter { it.paymentStatus == FineStatus.UNPAID }.sumOf { it.amount }
    val collected = fines.filter { it.paymentStatus == FineStatus.PAID }.sumOf { it.amount }

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
            SectionCard {
                Text("Digital receipts", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                val paid = fines.filter { it.paymentStatus == FineStatus.PAID }
                if (paid.isEmpty()) {
                    Text("No cash receipts yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 14.sp)
                } else {
                    paid.forEach { fine ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(fine.id.uppercase(), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text("${fine.paymentDate ?: fine.dateIssued} · Cash", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
                            Text(peso(fine.amount), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun peso(amount: Double) = "₱${"%.2f".format(amount)}"
