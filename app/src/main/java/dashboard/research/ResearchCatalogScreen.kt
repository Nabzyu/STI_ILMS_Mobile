package com.example.stismartlib.ui.dashboard.research

import androidx.compose.runtime.saveable.rememberSaveable
import com.example.stismartlib.data.isLoadedFor
import com.example.stismartlib.data.shouldOfferLoadMore
import com.example.stismartlib.ui.components.LoadMoreFooter
import com.composables.icons.lucide.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.models.Research
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsListRow
import com.example.stismartlib.ui.components.ElmsLoadingBlock
import com.example.stismartlib.ui.components.ElmsPullRefresh
import com.example.stismartlib.ui.components.ElmsSectionCard
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.dashboard.components.CatalogViewMode
import com.example.stismartlib.ui.dashboard.components.CatalogViewPrefs
import com.example.stismartlib.ui.dashboard.components.CatalogViewToggle
import com.example.stismartlib.ui.dashboard.components.ResearchCatalogGridTile
import com.example.stismartlib.ui.feedback.userFacingMessage
import com.example.stismartlib.ui.theme.ElmsCyan
import kotlinx.coroutines.delay

@Composable
fun ResearchCatalogScreen(onResearchClick: (String) -> Unit) {
    val repository = rememberLibraryRepository()
    val context = LocalContext.current
    val viewPrefs = remember { CatalogViewPrefs(context) }
    var viewMode by remember { mutableStateOf(viewPrefs.get(CatalogViewPrefs.KEY_RESEARCH)) }
    fun setViewMode(mode: CatalogViewMode) {
        viewMode = mode
        viewPrefs.set(CatalogViewPrefs.KEY_RESEARCH, mode)
    }
    var query by rememberSaveable { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var refreshError by remember { mutableStateOf<String?>(null) }
    var reloadToken by remember { mutableStateOf(0) }
    LaunchedEffect(query, reloadToken) {
        if (reloadToken == 0 && repository.researchCursor.isLoadedFor(query.ifBlank { null })) {
            loading = false
            return@LaunchedEffect
        }
        loading = true
        loadError = null
        delay(300)
        runCatching { repository.loadResearch(query.ifBlank { null }) }
            .onFailure { error ->
                if (repository.research.isEmpty()) {
                    loadError = userFacingMessage(error, "Unable to load research titles.")
                }
            }
        loading = false
    }
    val rows = repository.research.filter {
        "${it.title} ${it.author} ${it.category}".contains(query, ignoreCase = true)
    }

    val listContentPadding = PaddingValues(16.dp)
    val listSpacing = 8.dp

    ElmsPullRefresh(
        onRefresh = {
            repository.loadResearch(query.ifBlank { null })
        },
        onRefreshError = { refreshError = it },
    ) {
        if (viewMode == CatalogViewMode.GRID_2) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = listContentPadding,
                horizontalArrangement = Arrangement.spacedBy(listSpacing),
                verticalArrangement = Arrangement.spacedBy(listSpacing)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ResearchCatalogHeader()
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    PortalField(
                        label = "",
                        value = query,
                        onValueChange = { query = it },
                        placeholder = "Search research...",
                        leadingIcon = Lucide.Search
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ResearchCatalogResultsBar(
                        count = rows.size,
                        viewMode = viewMode,
                        onViewModeChange = ::setViewMode
                    )
                }
                if (loadError != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ElmsBanner(message = loadError.orEmpty(), tone = ElmsBannerTone.Error)
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        PortalButton(
                            text = "Try again",
                            onClick = { reloadToken += 1 },
                            variant = PortalButtonVariant.Secondary
                        )
                    }
                }
                if (refreshError != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ElmsBanner(message = refreshError.orEmpty(), tone = ElmsBannerTone.Error)
                    }
                }
                when {
                    loading && repository.research.isEmpty() -> {
                        item(span = { GridItemSpan(maxLineSpan) }) { ElmsLoadingBlock() }
                    }
                    loadError != null && repository.research.isEmpty() -> { /* banner + retry above */ }
                    rows.isEmpty() -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ElmsEmptyState(
                                title = "No research found",
                                description = "Try a different search term.",
                                actionLabel = "Clear search",
                                onAction = { query = "" }
                            )
                        }
                    }
                    else -> items(rows, key = { it.id }) { item ->
                        ResearchCatalogGridTile(
                            item = item,
                            onClick = { onResearchClick(item.id) }
                        )
                    }
                }
                if (shouldOfferLoadMore(repository.research.size, repository.researchCursor)) {
                    item(span = { GridItemSpan(maxLineSpan) }) { LoadMoreFooter { repository.loadMoreResearch() } }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = listContentPadding,
                verticalArrangement = Arrangement.spacedBy(listSpacing)
            ) {
                item { ResearchCatalogHeader() }
                item {
                    PortalField(
                        label = "",
                        value = query,
                        onValueChange = { query = it },
                        placeholder = "Search research...",
                        leadingIcon = Lucide.Search
                    )
                }
                item {
                    ResearchCatalogResultsBar(
                        count = rows.size,
                        viewMode = viewMode,
                        onViewModeChange = ::setViewMode
                    )
                }
                if (loadError != null) {
                    item {
                        ElmsBanner(message = loadError.orEmpty(), tone = ElmsBannerTone.Error)
                    }
                    item {
                        PortalButton(
                            text = "Try again",
                            onClick = { reloadToken += 1 },
                            variant = PortalButtonVariant.Secondary
                        )
                    }
                }
                if (refreshError != null) {
                    item {
                        ElmsBanner(message = refreshError.orEmpty(), tone = ElmsBannerTone.Error)
                    }
                }
                when {
                    loading && repository.research.isEmpty() -> item { ElmsLoadingBlock() }
                    loadError != null && repository.research.isEmpty() -> { /* banner + retry above */ }
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
                if (shouldOfferLoadMore(repository.research.size, repository.researchCursor)) {
                    item { LoadMoreFooter { repository.loadMoreResearch() } }
                }
            }
        }
    }
}

@Composable
private fun ResearchCatalogHeader() {
    Column {
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
}

@Composable
private fun ResearchCatalogResultsBar(
    count: Int,
    viewMode: CatalogViewMode,
    onViewModeChange: (CatalogViewMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$count titles",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
        CatalogViewToggle(
            mode = viewMode,
            onModeChange = onViewModeChange
        )
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
