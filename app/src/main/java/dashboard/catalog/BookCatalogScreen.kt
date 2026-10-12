package com.example.stismartlib.ui.dashboard.catalog

import com.composables.icons.lucide.*

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.models.Book
import com.example.stismartlib.data.models.availabilityLabel
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsListRow
import com.example.stismartlib.ui.components.ElmsLoadingBlock
import com.example.stismartlib.ui.components.ElmsPillTab
import com.example.stismartlib.ui.components.ElmsSectionCard
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.dashboard.components.BookCoverPlaceholder
import kotlinx.coroutines.delay

@Composable
fun BookCatalogScreen(
    onBookClick: (String) -> Unit,
    onCartClick: () -> Unit
) {
    val repository = rememberLibraryRepository()
    var searchQuery by remember { mutableStateOf("") }
    var availabilityFilter by remember { mutableStateOf("All") }
    var category by remember { mutableStateOf("All") }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(searchQuery) {
        loading = true
        delay(300)
        runCatching { repository.loadBooks(searchQuery.ifBlank { null }) }
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ElmsPillTab(
                    label = "All",
                    selected = availabilityFilter == "All",
                    onClick = { availabilityFilter = "All" },
                    count = repository.books.size
                )
                ElmsPillTab(
                    label = "Available",
                    selected = availabilityFilter == "Available",
                    onClick = { availabilityFilter = "Available" }
                )
                ElmsPillTab(
                    label = "Unavailable",
                    selected = availabilityFilter == "Unavailable",
                    onClick = { availabilityFilter = "Unavailable" }
                )
            }
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { item ->
                    PortalButton(
                        text = item,
                        onClick = { category = item },
                        variant = if (category == item) {
                            PortalButtonVariant.Primary
                        } else {
                            PortalButtonVariant.Secondary
                        }
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${visible.size} resources",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                PortalButton(
                    text = "Cart",
                    onClick = onCartClick,
                    variant = PortalButtonVariant.Ghost,
                    leadingIcon = Lucide.ShoppingCart
                )
            }
        }
        when {
            loading && repository.books.isEmpty() -> item { ElmsLoadingBlock() }
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
