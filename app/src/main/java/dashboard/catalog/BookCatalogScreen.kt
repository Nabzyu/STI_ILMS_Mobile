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
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.dashboard.components.BookCoverPlaceholder
import com.example.stismartlib.ui.theme.PrimaryBlue

@Composable
fun BookCatalogScreen(
    onBookClick: (String) -> Unit,
    onCartClick: () -> Unit
) {
    val repository = rememberLibraryRepository()
    var searchQuery by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }
    val categories = listOf("All") + repository.books.map { it.category }.distinct()
    val visible = repository.books.filter { book ->
        (category == "All" || book.category == category) &&
            "${book.title} ${book.author} ${book.isbn}".contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PageHeader(
                eyebrow = "Digital catalog",
                title = "Find your next resource",
                description = "Search books by title, author, ISBN, category, or shelf location."
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
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { item ->
                    PortalButton(
                        text = item,
                        onClick = { category = item },
                        variant = if (category == item) PortalButtonVariant.Primary else PortalButtonVariant.Secondary
                    )
                }
            }
        }
        item {
            Text("${visible.size} resources found", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        }
        items(visible, key = { it.id }) { book ->
            BookListItem(book, onClick = { onBookClick(book.id) }, onCart = onCartClick)
        }
    }
}

@Composable
fun BookListItem(
    book: Book,
    onClick: () -> Unit,
    onCart: () -> Unit = {}
) {
    SectionCard {
        Row {
            BookCoverPlaceholder(
                title = book.title,
                color = Color(book.coverColor),
                modifier = Modifier.size(width = 72.dp, height = 96.dp)
            )
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                StatusBadge(
                    label = if (book.availableCopies > 0) "Available" else "Reserved",
                    warning = book.availableCopies == 0
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(book.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(book.author, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
                Text("Shelf: ${book.shelfLocation}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
                Text("Copies: ${book.availabilityLabel()}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            PortalButton(text = "View details", onClick = onClick, variant = PortalButtonVariant.Secondary, modifier = Modifier.weight(1f))
            PortalButton(
                text = if (book.availableCopies > 0) "Borrow" else "Reserve",
                onClick = onClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
