package com.example.stismartlib.ui.dashboard.catalog

import androidx.compose.runtime.saveable.rememberSaveable
import com.example.stismartlib.data.isLoadedFor
import com.example.stismartlib.data.shouldOfferLoadMore
import com.example.stismartlib.ui.components.LoadMoreFooter
import com.composables.icons.lucide.*

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.models.Book
import com.example.stismartlib.data.models.availabilityLabel
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.ElmsCountBadge
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsListRow
import com.example.stismartlib.ui.components.ElmsLoadingBlock
import com.example.stismartlib.ui.components.ElmsPillTab
import com.example.stismartlib.ui.components.ElmsPullRefresh
import com.example.stismartlib.ui.components.ElmsSectionCard
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.dashboard.components.BookCatalogGridTile
import com.example.stismartlib.ui.dashboard.components.BookCoverPlaceholder
import com.example.stismartlib.ui.dashboard.components.CatalogViewMode
import com.example.stismartlib.ui.dashboard.components.CatalogViewPrefs
import com.example.stismartlib.ui.dashboard.components.CatalogViewToggle
import com.example.stismartlib.ui.feedback.userFacingMessage
import com.example.stismartlib.ui.theme.EyebrowStyle
import kotlinx.coroutines.delay

@Composable
fun BookCatalogScreen(
    onBookClick: (String) -> Unit,
    onCartClick: () -> Unit
) {
    val repository = rememberLibraryRepository()
    val context = LocalContext.current
    val viewPrefs = remember { CatalogViewPrefs(context) }
    var viewMode by remember { mutableStateOf(viewPrefs.get(CatalogViewPrefs.KEY_BOOKS)) }
    fun setViewMode(mode: CatalogViewMode) {
        viewMode = mode
        viewPrefs.set(CatalogViewPrefs.KEY_BOOKS, mode)
    }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var availabilityFilter by remember { mutableStateOf("All") }
    var category by remember { mutableStateOf("All") }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var refreshError by remember { mutableStateOf<String?>(null) }
    var reloadToken by remember { mutableStateOf(0) }

    LaunchedEffect(searchQuery, reloadToken) {
        // Coming back to the same search keeps the pages already loaded with Load more.
        if (reloadToken == 0 && repository.booksCursor.isLoadedFor(searchQuery.ifBlank { null })) {
            loading = false
            return@LaunchedEffect
        }
        loading = true
        loadError = null
        delay(300)
        runCatching { repository.loadBooks(searchQuery.ifBlank { null }) }
            .onFailure { error ->
                if (repository.books.isEmpty()) {
                    loadError = userFacingMessage(error, "Unable to load the catalog.")
                }
            }
        loading = false
    }

    val categories = listOf("All") + repository.books.map { it.category }.distinct()
    val visible = repository.books.filter { book ->
        val matchesCategory = category == "All" || book.category == category
        val matchesAvailability = when (availabilityFilter) {
            "Available" -> book.availableCopies > 0
            "Unavailable" -> book.availableCopies == 0
            else -> true
        }
        val matchesSearch =
            "${book.title} ${book.author} ${book.isbn}".contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesAvailability && matchesSearch
    }

    val listContentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp)
    val listSpacing = 8.dp

    Box(modifier = Modifier.fillMaxSize()) {
        ElmsPullRefresh(
            onRefresh = {
                repository.loadBooks(searchQuery.ifBlank { null })
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
                        BookCatalogHeader()
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        PortalField(
                            label = "",
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "Search title, author, or ISBN...",
                            leadingIcon = Lucide.Search
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        BookCatalogAvailabilityFilters(
                            availabilityFilter = availabilityFilter,
                            onAvailabilityChange = { availabilityFilter = it },
                            totalCount = repository.books.size
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        BookCatalogCategoryFilters(
                            categories = categories,
                            selected = category,
                            onCategoryChange = { category = it }
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        BookCatalogResultsBar(
                            count = visible.size,
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
                        loading && repository.books.isEmpty() -> {
                            item(span = { GridItemSpan(maxLineSpan) }) { ElmsLoadingBlock() }
                        }
                        loadError != null && repository.books.isEmpty() -> { /* banner + retry above */ }
                        visible.isEmpty() -> {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ElmsEmptyState(
                                    title = "No books found",
                                    description = "Try another search or category.",
                                    actionLabel = "Clear filters",
                                    onAction = {
                                        searchQuery = ""
                                        category = "All"
                                        availabilityFilter = "All"
                                    }
                                )
                            }
                        }
                        else -> items(visible, key = { it.id }) { book ->
                            BookCatalogGridTile(
                                book = book,
                                onClick = { onBookClick(book.id) }
                            )
                        }
                    }
                    if (shouldOfferLoadMore(repository.books.size, repository.booksCursor)) {
                        item(span = { GridItemSpan(maxLineSpan) }) { LoadMoreFooter { repository.loadMoreBooks() } }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = listContentPadding,
                    verticalArrangement = Arrangement.spacedBy(listSpacing)
                ) {
                    item { BookCatalogHeader() }
                    item {
                        PortalField(
                            label = "",
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "Search title, author, or ISBN...",
                            leadingIcon = Lucide.Search
                        )
                    }
                    item {
                        BookCatalogAvailabilityFilters(
                            availabilityFilter = availabilityFilter,
                            onAvailabilityChange = { availabilityFilter = it },
                            totalCount = repository.books.size
                        )
                    }
                    item {
                        BookCatalogCategoryFilters(
                            categories = categories,
                            selected = category,
                            onCategoryChange = { category = it }
                        )
                    }
                    item {
                        BookCatalogResultsBar(
                            count = visible.size,
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
                        loading && repository.books.isEmpty() -> item { ElmsLoadingBlock() }
                        loadError != null && repository.books.isEmpty() -> { /* banner + retry above */ }
                        visible.isEmpty() -> item {
                            ElmsEmptyState(
                                title = "No books found",
                                description = "Try another search or category.",
                                actionLabel = "Clear filters",
                                onAction = {
                                    searchQuery = ""
                                    category = "All"
                                    availabilityFilter = "All"
                                }
                            )
                        }
                        else -> items(visible, key = { it.id }) { book ->
                            ElmsSectionCard(contentPadding = 8.dp) {
                                ElmsListRow(
                                    title = book.title,
                                    subtitle = "${book.author} · ${book.availabilityLabel()}",
                                    onClick = { onBookClick(book.id) },
                                    leading = {
                                        BookCoverPlaceholder(
                                            title = book.title,
                                            color = Color(book.coverColor),
                                            modifier = Modifier.size(52.dp)
                                        )
                                    },
                                    trailing = {
                                        StatusBadge(
                                            label = if (book.availableCopies > 0) "Available" else "Reserved",
                                            warning = book.availableCopies == 0
                                        )
                                    }
                                )
                            }
                        }
                    }
                    if (shouldOfferLoadMore(repository.books.size, repository.booksCursor)) {
                        item { LoadMoreFooter { repository.loadMoreBooks() } }
                    }
                }
            }
        }

        val cartCount = repository.cart.size
        val fabDescription = if (cartCount > 0) "Book cart, $cartCount items" else "Book cart"
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = onCartClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.semantics { contentDescription = fabDescription }
            ) {
                Icon(Lucide.ShoppingCart, contentDescription = null)
            }
            ElmsCountBadge(
                count = cartCount,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
            )
        }
    }
}

@Composable
private fun BookCatalogHeader() {
    Column {
        Text(
            "Catalog",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Search by title, author, or ISBN",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun BookCatalogAvailabilityFilters(
    availabilityFilter: String,
    onAvailabilityChange: (String) -> Unit,
    totalCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Availability",
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElmsPillTab(
                label = "All",
                selected = availabilityFilter == "All",
                onClick = { onAvailabilityChange("All") },
                count = totalCount
            )
            ElmsPillTab(
                label = "Available",
                selected = availabilityFilter == "Available",
                onClick = { onAvailabilityChange("Available") }
            )
            ElmsPillTab(
                label = "Unavailable",
                selected = availabilityFilter == "Unavailable",
                onClick = { onAvailabilityChange("Unavailable") }
            )
        }
    }
}

@Composable
private fun BookCatalogCategoryFilters(
    categories: List<String>,
    selected: String,
    onCategoryChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Category",
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { item ->
                ElmsPillTab(
                    label = item,
                    selected = selected == item,
                    onClick = { onCategoryChange(item) }
                )
            }
        }
    }
}

@Composable
private fun BookCatalogResultsBar(
    count: Int,
    viewMode: CatalogViewMode,
    onViewModeChange: (CatalogViewMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$count resources",
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
fun BookListItem(
    book: Book,
    onClick: () -> Unit,
    onCart: () -> Unit = {}
) {
    ElmsSectionCard {
        ElmsListRow(
            title = book.title,
            subtitle = book.author,
            onClick = onClick,
            leading = {
                BookCoverPlaceholder(
                    title = book.title,
                    color = Color(book.coverColor),
                    modifier = Modifier.size(52.dp)
                )
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            PortalButton(
                text = "View details",
                onClick = onClick,
                variant = PortalButtonVariant.Secondary,
                modifier = Modifier.weight(1f)
            )
            PortalButton(
                text = if (book.availableCopies > 0) "Borrow" else "Reserve",
                onClick = onClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
