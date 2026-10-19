package com.example.stismartlib.ui.dashboard.mybooks

import com.composables.icons.lucide.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.MyBook
import com.example.stismartlib.data.filterMyBooks
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsLoadingBlock
import com.example.stismartlib.ui.components.ElmsPullRefresh
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.format.formatDateMedium

/** Every title the user has borrowed, once each (web: My books). */
@Composable
fun MyBooksScreen() {
    val repository = rememberLibraryRepository()
    var loading by remember { mutableStateOf(repository.myBooks.isEmpty()) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(reloadKey) {
        if (repository.myBooks.isEmpty()) loading = true
        runCatching { repository.loadMyBooks() } // failure shows through myBooksError
        loading = false
    }

    ElmsPullRefresh(onRefresh = { repository.loadMyBooks() }, onRefreshError = {}) {
        MyBooksContent(
            books = repository.myBooks,
            loading = loading,
            error = repository.myBooksError,
            query = query,
            onQueryChange = { query = it },
            onRetry = { reloadKey++ },
        )
    }
}

@Composable
internal fun MyBooksContent(
    books: List<MyBook>,
    loading: Boolean,
    error: String?,
    query: String,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
) {
    val visible = filterMyBooks(books, query)
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            PageHeader(
                eyebrow = "My library",
                title = "My books",
                description = "Your personal collection of books you have borrowed. Each title appears once, with your most recent borrowing record.",
            )
        }
        item {
            PortalField(
                label = "Search my books",
                value = query,
                onValueChange = onQueryChange,
                placeholder = "Book title or author",
                leadingIcon = Lucide.Search,
            )
        }
        if (error != null && books.isNotEmpty()) {
            item { ElmsBanner(message = error) }
        }
        when {
            loading -> item { ElmsLoadingBlock() }
            error != null && books.isEmpty() -> item {
                ElmsEmptyState(
                    title = "My books unavailable",
                    description = "Your book collection is temporarily unavailable.",
                    actionLabel = "Try again",
                    onAction = onRetry,
                )
            }
            books.isEmpty() -> item {
                ElmsEmptyState(title = "No books yet", description = "You have not borrowed any books yet.")
            }
            visible.isEmpty() -> item {
                ElmsEmptyState(title = "No matches", description = "No books match your search.")
            }
            else -> items(visible, key = { it.key }) { book -> MyBookCard(book) }
        }
    }
}

@Composable
private fun MyBookCard(book: MyBook) {
    val active = book.status == "Borrowed" || book.status == "Overdue"
    SectionCard {
        Text(book.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text(book.author, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        StatusBadge(book.status, warning = book.status == "Overdue" || book.status == "Lost")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Last borrowed ${formatDateMedium(book.borrowedOn)}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
        )
        val due = book.dueOn
        if (active && due != null) {
            Text("Due ${formatDateMedium(due)}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}
