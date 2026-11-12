package com.example.stismartlib.ui.dashboard.search

import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsListRow
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.feedback.userFacingMessage
import com.example.stismartlib.ui.theme.ElmsCyan
import kotlinx.coroutines.delay

private const val SEARCH_DEBOUNCE_MS = 300L

@Composable
fun GlobalSearchScreen(
    onBack: () -> Unit,
    onBookClick: (String) -> Unit,
    onResearchClick: (String) -> Unit,
    onBrowseCatalog: (() -> Unit)? = null,
) {
    val repository = rememberLibraryRepository()
    var query by rememberSaveable { mutableStateOf("") }
    var apiError by remember { mutableStateOf<String?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val booksSnapshot = repository.books.toList()
    val researchSnapshot = repository.research.toList()
    val predictions = remember(query, booksSnapshot, researchSnapshot) {
        buildSearchPredictions(query, booksSnapshot, researchSnapshot)
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(query) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            apiError = null
            refreshing = false
            return@LaunchedEffect
        }
        delay(SEARCH_DEBOUNCE_MS)
        refreshing = true
        apiError = null
        val bookResult = runCatching { repository.loadBooks(trimmed) }
        val researchResult = runCatching { repository.loadResearch(trimmed) }
        val err = bookResult.exceptionOrNull() ?: researchResult.exceptionOrNull()
        // Keep local suggestions; hide session noise when offline cache already has hits.
        apiError = err?.let {
            userFacingMessage(it, "Unable to refresh search results.")
        }?.takeUnless { message ->
            predictions.isNotEmpty() && message.contains("session expired", ignoreCase = true)
        }
        refreshing = false
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Search",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Find books and research by title or author",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }
        item {
            PortalField(
                label = "",
                value = query,
                onValueChange = { query = it },
                placeholder = "Search books and research...",
                leadingIcon = Lucide.Search,
                textFieldModifier = Modifier.focusRequester(focusRequester),
            )
        }
        if (apiError != null && predictions.isNotEmpty()) {
            item {
                ElmsBanner(message = apiError.orEmpty(), tone = ElmsBannerTone.Error)
            }
        }
        when {
            query.trim().isEmpty() -> item {
                Text(
                    "Start typing to see book and research suggestions.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                )
            }
            predictions.isEmpty() && !refreshing -> item {
                ElmsEmptyState(
                    title = "No matches",
                    description = "Try another title, author, or ISBN.",
                    actionLabel = if (onBrowseCatalog != null) "Browse catalog" else null,
                    onAction = onBrowseCatalog,
                )
            }
            predictions.isEmpty() && refreshing -> item {
                Text(
                    "Searching…",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                )
            }
            else -> items(predictions, key = { "${it.kind}-${it.id}" }) { prediction ->
                ElmsListRow(
                    title = prediction.title,
                    subtitle = prediction.subtitle,
                    onClick = {
                        when (prediction.kind) {
                            SearchResultKind.Book -> onBookClick(prediction.id)
                            SearchResultKind.Research -> onResearchClick(prediction.id)
                        }
                    },
                    leading = {
                        Icon(
                            imageVector = when (prediction.kind) {
                                SearchResultKind.Book -> Lucide.BookOpen
                                SearchResultKind.Research -> Lucide.FileText
                            },
                            contentDescription = null,
                            tint = ElmsCyan,
                        )
                    },
                    trailing = {
                        StatusBadge(
                            label = when (prediction.kind) {
                                SearchResultKind.Book -> "Book"
                                SearchResultKind.Research -> "Research"
                            },
                        )
                    },
                )
            }
        }
        item {
            PortalButton(
                text = "Back",
                onClick = onBack,
                variant = PortalButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
