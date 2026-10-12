package com.example.stismartlib.ui.dashboard.catalog

import com.composables.icons.lucide.*

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.network.ApiException
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.components.AppDialog
import com.example.stismartlib.ui.components.AppDialogType
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.dashboard.components.BookCoverPlaceholder
import kotlinx.coroutines.launch

@Composable
fun BookCartScreen(onBack: () -> Unit) {
    val repository = rememberLibraryRepository()
    val scope = rememberCoroutineScope()
    var showBorrowDialog by remember { mutableStateOf(false) }
    var showReserveDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("Your cart is ready to claim at the library desk.") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val cartItems = repository.cart
    val allAvailable = repository.allCartItemsAvailable()
    val cartLimitText = if (repository.session.userType == UserType.STUDENT) {
        "Student cart limit: maximum of 2 books"
    } else {
        "Faculty can add multiple books"
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        PageHeader(eyebrow = "My library", title = "Book cart", description = cartLimitText)
        Spacer(modifier = Modifier.height(12.dp))
        if (errorMessage != null) {
            Text(errorMessage.orEmpty(), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (cartItems.isEmpty()) {
            Text("Your cart is empty. Browse the catalog to add books.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems, key = { it.book.id }) { item ->
                    SectionCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BookCoverPlaceholder(title = item.book.title, color = Color(item.book.coverColor), modifier = Modifier.size(56.dp))
                            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(item.book.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                Text(item.book.author, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
                                StatusBadge(if (item.isAvailable) "Available" else "Unavailable", warning = !item.isAvailable)
                            }
                            IconButton(onClick = { repository.removeFromCart(item.book.id) }) {
                                Icon(Lucide.Trash2, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
            if (allAvailable) {
                PortalButton(
                    text = if (isSubmitting) "Submitting..." else "Borrow books",
                    onClick = {
                        isSubmitting = true
                        errorMessage = null
                        scope.launch {
                            try {
                                dialogMessage = repository.submitBorrowCart()
                                showBorrowDialog = true
                            } catch (error: ApiException) {
                                errorMessage = error.message
                            } catch (_: Exception) {
                                errorMessage = "Unable to submit the borrow request."
                            } finally {
                                isSubmitting = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                PortalButton(text = "Reserve unavailable books", onClick = { showReserveDialog = true }, modifier = Modifier.fillMaxWidth())
            }
        }
    }

    AppDialog(
        visible = showBorrowDialog,
        onDismiss = { showBorrowDialog = false },
        type = AppDialogType.SUCCESS,
        title = "Borrow request",
        message = dialogMessage,
        confirmText = "Done",
        onConfirm = { showBorrowDialog = false }
    )
    AppDialog(
        visible = showReserveDialog,
        onDismiss = { showReserveDialog = false },
        type = AppDialogType.INFO,
        title = "Reservation",
        message = "Unavailable titles in your cart can be reserved from the book page.",
        confirmText = "Close"
    )
}
