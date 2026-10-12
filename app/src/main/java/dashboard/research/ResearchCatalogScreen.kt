package com.example.stismartlib.ui.dashboard.research

import com.composables.icons.lucide.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.models.Research
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsListRow
import com.example.stismartlib.ui.components.ElmsLoadingBlock
import com.example.stismartlib.ui.components.ElmsSectionCard
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.theme.ElmsCyan
import kotlinx.coroutines.delay

@Composable
fun ResearchCatalogScreen(onResearchClick: (String) -> Unit) {
    val repository = rememberLibraryRepository()
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(query) {
        loading = true
        delay(300)
        runCatching { repository.loadResearch(query.ifBlank { null }) }
        loading = false
    }
    val rows = repository.research.filter {
        "${it.title} ${it.author} ${it.category}".contains(query, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Research & thesis",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Browse by department, author, title, or year",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
        item {
            PortalField(
                label = "",
                value = query,
                onValueChange = { query = it },
                placeholder = "Search research...",
                leadingIcon = Lucide.Search
            )
        }
        when {
            loading && repository.research.isEmpty() -> item { ElmsLoadingBlock() }
            rows.isEmpty() -> item {
                ElmsEmptyState(
                    title = "No research found",
                    description = "Try a different search term.",
                    actionLabel = "Clear search",
                    onAction = { query = "" }
                )
            }
            else -> items(rows, key = { it.id }) { item ->
                ResearchListItem(item, onClick = { onResearchClick(item.id) })
            }
        }
    }
}

@Composable
fun ResearchListItem(item: Research, onClick: () -> Unit) {
    ElmsSectionCard(contentPadding = 8.dp) {
        ElmsListRow(
            title = item.title,
            subtitle = "${item.author} · ${item.yearPublished} · ${item.category}",
            onClick = onClick,
            leading = {
                Icon(
                    Lucide.FileText,
                    contentDescription = null,
                    tint = ElmsCyan,
                    modifier = Modifier.size(24.dp)
                )
            },
            trailing = { StatusBadge("Available") }
        )
    }
}
