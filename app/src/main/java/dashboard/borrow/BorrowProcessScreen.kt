package com.example.stismartlib.ui.dashboard.borrow

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.PrimaryButton
import com.example.stismartlib.ui.dashboard.components.BookCoverPlaceholder
import com.example.stismartlib.ui.dashboard.components.EmptyStateMessage
import com.example.stismartlib.ui.dashboard.components.ProcessStepCard
import com.example.stismartlib.ui.dashboard.components.SectionCard
import com.example.stismartlib.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowProcessScreen(
    onBack: () -> Unit,
    onBrowseCatalog: () -> Unit,
    onOpenCart: () -> Unit
) {
    val repository = rememberLibraryRepository()
    val cartItems = repository.cart

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Borrow Book Process") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionCard(title = "How Borrowing Works") {
                    ProcessStepCard(
                        stepNumber = 1,
                        title = "Browse & Add to Cart",
                        description = "Search the book catalog and add up to 2 books (students) to your cart.",
                        isActive = cartItems.isEmpty(),
                        isCompleted = cartItems.isNotEmpty()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    ProcessStepCard(
                        stepNumber = 2,
                        title = "Visit the Library Counter",
                        description = "Bring your student ID and go to the borrowing desk during library hours.",
                        isActive = cartItems.isNotEmpty()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    ProcessStepCard(
                        stepNumber = 3,
                        title = "Librarian Confirmation",
                        description = "A librarian will scan the book barcode or confirm your borrow manually."
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    ProcessStepCard(
                        stepNumber = 4,
                        title = "Books Issued",
                        description = "Due date is the next day at 8:59 AM. You will receive a notification reminder."
                    )
                }
            }

            item {
                SectionCard(title = "Your Cart Preview") {
                    if (cartItems.isEmpty()) {
                        EmptyStateMessage(
                            message = "No books in cart yet. Browse the catalog to start borrowing.",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        cartItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                BookCoverPlaceholder(
                                    title = item.book.title,
                                    color = Color(item.book.coverColor),
                                    modifier = Modifier.size(48.dp)
                                )
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    Text(item.book.title, fontWeight = FontWeight.SemiBold)
                                    Text(item.book.author, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            item {
                PrimaryButton(
                    text = "Browse Book Catalog",
                    onClick = onBrowseCatalog
                )
            }

            item {
                PrimaryButton(
                    text = "Open Book Cart (${cartItems.size})",
                    onClick = onOpenCart,
                    useAccentStyle = true
                )
            }
        }
    }
}
