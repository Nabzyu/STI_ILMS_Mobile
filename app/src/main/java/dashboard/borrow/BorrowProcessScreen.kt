package com.example.stismartlib.ui.dashboard.borrow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsListRow
import com.example.stismartlib.ui.components.ElmsSectionCard
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.dashboard.components.BookCoverPlaceholder

@Composable
fun BorrowProcessScreen(
    onBack: () -> Unit,
    onBrowseCatalog: () -> Unit,
    onOpenCart: () -> Unit
) {
    val repository = rememberLibraryRepository()
    val cartItems = repository.cart

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Borrow process",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "How to borrow from the library",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
        item {
            ElmsSectionCard {
                Text("Steps", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Step("1. Browse & add up to 2 books to your cart")
                Step("2. Visit the library counter with your ID")
                Step("3. Librarian confirms the borrow")
                Step("4. Books issued — due next day at 8:59 AM")
            }
        }
        item {
            ElmsSectionCard(contentPadding = 8.dp) {
                Text(
                    "Your cart",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (cartItems.isEmpty()) {
                    ElmsEmptyState(
                        title = "Cart is empty",
                        description = "Browse the catalog to start borrowing."
                    )
                } else {
                    cartItems.forEach { item ->
                        ElmsListRow(
                            title = item.book.title,
                            subtitle = item.book.author,
                            leading = {
                                BookCoverPlaceholder(
                                    title = item.book.title,
                                    color = Color(item.book.coverColor),
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
        if (cartItems.isEmpty()) {
            item {
                PortalButton(
                    text = "Browse catalog",
                    onClick = onBrowseCatalog,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            item {
                PortalButton(
                    text = "Open book cart (${cartItems.size})",
                    onClick = onOpenCart,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                PortalButton(
                    text = "Browse catalog",
                    onClick = onBrowseCatalog,
                    variant = PortalButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            PortalButton(
                text = "Back",
                onClick = onBack,
                variant = PortalButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun Step(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(6.dp))
}
