package com.example.stismartlib.ui.dashboard.catalog

import android.widget.Toast
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.LibraryRepository
import com.example.stismartlib.data.models.apaCitation
import com.example.stismartlib.data.models.availabilityLabel
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.AppDialog
import com.example.stismartlib.ui.components.AppDialogType
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.dashboard.components.BookCoverPlaceholder
import com.example.stismartlib.ui.theme.PrimaryBlue

@Composable
fun BookDetailScreen(
    bookId: String,
    onBack: () -> Unit,
    onCartClick: () -> Unit
) {
    val repository = rememberLibraryRepository()
    val context = LocalContext.current
    var showApaCitation by remember { mutableStateOf(false) }
    val book = repository.books.find { it.id == bookId }

    if (book == null) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text("Book not found.", color = MaterialTheme.colorScheme.onSurface)
            PortalButton(text = "Go back", onClick = onBack, variant = PortalButtonVariant.Secondary)
        }
        return
    }

    val isAvailable = book.availableCopies > 0
    val inCart = repository.isInCart(book.id)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            PageHeader(eyebrow = "Catalog", title = book.title, description = book.author)
            Spacer(modifier = Modifier.height(12.dp))
            Row {
                BookCoverPlaceholder(title = book.title, color = Color(book.coverColor), modifier = Modifier.size(100.dp))
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    StatusBadge(if (isAvailable) "Available" else "Reserved", warning = !isAvailable)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(book.availabilityLabel(), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 13.sp)
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionCard {
                InfoLine("ISBN", book.isbn)
                InfoLine("Category", book.category)
                InfoLine("Year", book.yearPublished.toString())
                InfoLine("Publisher", book.publisher)
                InfoLine("Shelf", book.shelfLocation)
                Spacer(modifier = Modifier.height(8.dp))
                Text(book.description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 14.sp)
            }
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
            PortalButton(text = "Generate APA reference", onClick = { showApaCitation = true }, variant = PortalButtonVariant.Secondary, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            PortalButton(
                text = if (inCart) "Already in cart" else "Add to cart",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    when (repository.addToCart(book)) {
                        LibraryRepository.AddToCartResult.Success ->
                            Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
                        LibraryRepository.AddToCartResult.AlreadyInCart ->
                            Toast.makeText(context, "Book is already in cart", Toast.LENGTH_SHORT).show()
                        LibraryRepository.AddToCartResult.LimitReached ->
                            Toast.makeText(context, "Student cart limit: 2 books maximum", Toast.LENGTH_LONG).show()
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PortalButton(
                text = "View book cart (${repository.cart.size})",
                onClick = onCartClick,
                variant = PortalButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    AppDialog(
        visible = showApaCitation,
        onDismiss = { showApaCitation = false },
        type = AppDialogType.INFO,
        title = "APA Citation",
        message = book.apaCitation(),
        confirmText = "Close"
    )
}

@Composable
private fun InfoLine(label: String, value: String) {
    Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(8.dp))
}
