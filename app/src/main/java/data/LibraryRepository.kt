package com.example.stismartlib.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.stismartlib.data.models.Book
import com.example.stismartlib.data.models.CartItem
import com.example.stismartlib.data.models.FineRecord
import com.example.stismartlib.data.models.LibrarySession
import com.example.stismartlib.data.models.NotificationItem
import com.example.stismartlib.data.models.PrintRequest
import com.example.stismartlib.data.models.Research
import com.example.stismartlib.data.models.Reservation
import com.example.stismartlib.data.models.BorrowingRecord
import com.example.stismartlib.ui.auth.UserType

class LibraryRepository(
    initialSession: LibrarySession = LibrarySession()
) {
    var session by mutableStateOf(initialSession)
        private set

    val cart = mutableStateListOf<CartItem>()
    val reservations = mutableStateListOf<Reservation>()

    val books: List<Book> = emptyList()
    val research: List<Research> = emptyList()
    val borrowingHistory: List<BorrowingRecord> = emptyList()
    val fines: List<FineRecord> = emptyList()
    val notifications: List<NotificationItem> = emptyList()
    val printHistory: List<PrintRequest> = emptyList()

    fun setUserType(userType: UserType) {
        session = session.copy(userType = userType)
    }

    fun cartLimit(): Int? = if (session.userType == UserType.STUDENT) 2 else null

    fun canAddToCart(): Boolean {
        val limit = cartLimit() ?: return true
        return cart.size < limit
    }

    fun isInCart(bookId: String): Boolean = cart.any { it.book.id == bookId }

    fun addToCart(book: Book): AddToCartResult {
        if (isInCart(book.id)) return AddToCartResult.AlreadyInCart
        if (!canAddToCart()) return AddToCartResult.LimitReached
        cart.add(
            CartItem(
                book = book,
                isAvailable = book.availableCopies > 0
            )
        )
        return AddToCartResult.Success
    }

    fun removeFromCart(bookId: String) {
        cart.removeAll { it.book.id == bookId }
    }

    fun clearCart() {
        cart.clear()
    }

    fun allCartItemsAvailable(): Boolean =
        cart.isNotEmpty() && cart.all { it.isAvailable }

    fun addReservation(book: Book) {
        if (reservations.any { it.book.id == book.id }) return
        reservations.add(
            Reservation(
                id = "res_${book.id}_${System.currentTimeMillis()}",
                book = book,
                queuePosition = reservations.size + 1,
                dateReserved = "Today"
            )
        )
    }

    fun cancelReservation(reservationId: String) {
        reservations.removeAll { it.id == reservationId }
    }

    enum class AddToCartResult {
        Success, AlreadyInCart, LimitReached
    }
}
