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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.theme.PrimaryBlue

@Composable
fun ReserveBookScreen(
    onBack: () -> Unit,
    onBrowseCatalog: () -> Unit
) {
    val repository = rememberLibraryRepository()
    val reservations = repository.reservations

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
        if (reservations.isEmpty()) {
            item { Text("You have no reservations.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)) }
        }
        items(reservations, key = { it.id }) { reservation ->
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        Lucide.BookMarked,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryBlue.copy(alpha = 0.06f))
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
                        .background(PrimaryBlue.copy(alpha = 0.05f))
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
                        onClick = { repository.cancelReservation(reservation.id) },
                        variant = PortalButtonVariant.Secondary
                    )
                }
            }
        }
    }
}
