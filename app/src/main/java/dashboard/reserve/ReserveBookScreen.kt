package com.example.stismartlib.ui.dashboard.reserve
import com.composables.icons.lucide.*

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.models.Reservation
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsLoadingBlock
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.theme.ElmsCyan
import kotlinx.coroutines.launch

@Composable
fun ReserveBookScreen(
    onBack: () -> Unit,
    onBrowseCatalog: () -> Unit
) {
    val repository = rememberLibraryRepository()
    val scope = rememberCoroutineScope()
    val reservations = repository.reservations
    val loading = repository.isLoading && reservations.isEmpty()
    val loadError = repository.lastError.takeIf { reservations.isEmpty() }
    var pendingCancel by remember { mutableStateOf<Reservation?>(null) }

    if (pendingCancel != null) {
        AlertDialog(
            onDismissRequest = { pendingCancel = null },
            title = { Text("Cancel reservation?") },
            text = {
                Text(
                    "Cancel your reservation for \"${pendingCancel!!.book.title}\"? " +
                        "You will lose your place in the queue."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = pendingCancel!!.id
                        pendingCancel = null
                        scope.launch {
                            runCatching { repository.cancelReservation(id) }
                        }
                    }
                ) { Text("Cancel reservation", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingCancel = null }) { Text("Keep reservation") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PageHeader(
                eyebrow = "My library",
                title = "Book reservations",
                description = "Follow your place in the waiting list and collect available resources before the reservation expires.",
                action = {
                    PortalButton(text = "Find a book", onClick = onBrowseCatalog, leadingIcon = Lucide.Search)
                }
            )
        }
        if (loadError != null) {
            item {
                ElmsBanner(message = loadError, tone = ElmsBannerTone.Error)
            }
        }
        when {
            loading -> item { ElmsLoadingBlock() }
            reservations.isEmpty() -> item {
                ElmsEmptyState(
                    title = "No reservations",
                    description = "Reserve unavailable books from the catalog.",
                    actionLabel = "Find a book",
                    onAction = onBrowseCatalog
                )
            }
            else -> items(reservations, key = { it.id }) { reservation ->
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            Lucide.BookMarked,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElmsCyan.copy(alpha = 0.12f))
                                .padding(10.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        StatusBadge("Queued", warning = true)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(reservation.book.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Request ${reservation.id} · ${reservation.dateReserved}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElmsCyan.copy(alpha = 0.08f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("QUEUE POSITION", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("#${reservation.queuePosition}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        PortalButton(
                            text = "Cancel",
                            onClick = { pendingCancel = reservation },
                            variant = PortalButtonVariant.Secondary
                        )
                    }
                }
            }
        }
        item {
            PortalButton(
                text = "Back",
                onClick = onBack,
                variant = PortalButtonVariant.Ghost
            )
        }
    }
}
