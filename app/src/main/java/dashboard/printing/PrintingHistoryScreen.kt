package com.example.stismartlib.ui.dashboard.printing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsLoadingBlock
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant

@Composable
fun PrintingHistoryScreen(onBack: () -> Unit) {
    val repository = rememberLibraryRepository()
    val history = repository.printHistory
    val loading = repository.isLoading && history.isEmpty()
    val loadError = repository.lastError.takeIf { history.isEmpty() }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PageHeader(eyebrow = "Online service", title = "Print history", description = "Current and previous print requests.")
        }
        if (loadError != null) {
            item {
                ElmsBanner(message = loadError, tone = ElmsBannerTone.Error)
            }
        }
        when {
            loading -> item { ElmsLoadingBlock() }
            history.isEmpty() -> item {
                ElmsEmptyState(
                    title = "No print requests",
                    description = "Requests you submit will appear here."
                )
            }
            else -> items(history, key = { it.id }) { request ->
                PrintQueueCard(request)
            }
        }
        item {
            PortalButton(text = "Back", onClick = onBack, variant = PortalButtonVariant.Secondary)
        }
    }
}
