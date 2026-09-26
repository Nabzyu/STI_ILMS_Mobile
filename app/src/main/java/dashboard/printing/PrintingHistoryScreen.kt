package com.example.stismartlib.ui.dashboard.printing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant

@Composable
fun PrintingHistoryScreen(onBack: () -> Unit) {
    val repository = rememberLibraryRepository()
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PageHeader(eyebrow = "Online service", title = "Print history", description = "Current and previous print requests.")
        }
        if (repository.printHistory.isEmpty()) {
            item {
                Text(
                    "No print requests yet.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }
        }
        items(repository.printHistory, key = { it.id }) { request ->
            PrintQueueCard(request)
        }
        item {
            PortalButton(text = "Back", onClick = onBack, variant = PortalButtonVariant.Secondary)
        }
    }
}
