package com.example.stismartlib.data

import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.stismartlib.data.models.AttendancePass
import com.example.stismartlib.data.models.Book
import com.example.stismartlib.data.models.BorrowStatus
import com.example.stismartlib.data.models.BorrowingRecord
import com.example.stismartlib.data.models.CartItem
import com.example.stismartlib.data.models.FineRecord
import com.example.stismartlib.data.models.FineStatus
import com.example.stismartlib.data.models.LibrarySession
import com.example.stismartlib.data.models.NotificationCategory
import com.example.stismartlib.data.models.NotificationItem
import com.example.stismartlib.data.models.PrintRequest
import com.example.stismartlib.data.models.PrintStatus
import com.example.stismartlib.data.models.Research
import com.example.stismartlib.data.models.Reservation
import com.example.stismartlib.network.ApiClient
import com.example.stismartlib.network.ApiException
import com.example.stismartlib.network.dto.BorrowSubmitRequest
import com.example.stismartlib.network.dto.LoginRequest
import com.example.stismartlib.network.dto.RegisterRequest
import com.example.stismartlib.session.SessionStore
import com.example.stismartlib.ui.auth.UserType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryRepository(
    private val sessionStore: SessionStore,
) {
    var session by mutableStateOf(LibrarySession())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var lastError by mutableStateOf<String?>(null)
        private set

    val cart = mutableStateListOf<CartItem>()
    val books = mutableStateListOf<Book>()
    val research = mutableStateListOf<Research>()
    val borrowingHistory = mutableStateListOf<BorrowingRecord>()
    val fines = mutableStateListOf<FineRecord>()
    val notifications = mutableStateListOf<NotificationItem>()
    val printHistory = mutableStateListOf<PrintRequest>()
    val reservations = mutableStateListOf<Reservation>()

    var attendancePass by mutableStateOf<AttendancePass?>(null)
        private set

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
        cart.add(CartItem(book = book, isAvailable = book.availableCopies > 0))
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

    /**
     * Single sign-in for Student, Faculty, and Librarian/Admin.
     * The API keeps separate user/staff portals, so we try the remembered portal first,
     * then the other only on INVALID_CREDENTIALS (Admin is rejected by the user portal).
     */
    suspend fun login(schoolId: String, password: String) = withContext(Dispatchers.IO) {
        lastError = null
        val normalizedSchoolId = schoolId.trim().uppercase()
        val preferred = sessionStore.preferredLoginPortal(normalizedSchoolId)
        val primary = preferred ?: PORTAL_USER
        val fallback = if (primary == PORTAL_STAFF) PORTAL_USER else PORTAL_STAFF
        try {
            authenticatePortal(normalizedSchoolId, password, primary)
        } catch (error: ApiException) {
            if (error.code != "INVALID_CREDENTIALS") throw error
            try {
                authenticatePortal(normalizedSchoolId, password, fallback)
            } catch (fallbackError: ApiException) {
                // Prefer rate-limit / deactivation messages from the second attempt when present.
                if (fallbackError.code == "TOO_MANY_LOGIN_ATTEMPTS" ||
                    fallbackError.code == "ACCOUNT_DEACTIVATED"
                ) {
                    throw fallbackError
                }
                throw error
            }
        }
    }

    private suspend fun authenticatePortal(schoolId: String, password: String, portal: String) {
        require(portal == PORTAL_USER || portal == PORTAL_STAFF) {
            "Unsupported login portal."
        }
        // Drop any previous token so login is not sent with a stale Authorization header.
        sessionStore.clear()
        clearLocalData()
        session = LibrarySession()

        val normalizedSchoolId = schoolId.trim().uppercase()
        val response = ApiClient.api.login(
            LoginRequest(
                schoolId = normalizedSchoolId,
                password = password,
                portal = portal,
            ),
        )
        val data = ApiClient.unwrap(response)
        val user = data.user ?: throw ApiException("The server returned an incomplete login response.")
        val token = data.token?.takeIf { it.isNotBlank() }
            ?: throw ApiException("The server returned an invalid access token.", "INVALID_TOKEN_RESPONSE")
        val accountId = user.accountId ?: user.id ?: throw ApiException("Missing account id.")
        val resolvedRole = user.role.orEmpty()
        val resolvedSchoolId = user.schoolId ?: normalizedSchoolId
        val resolvedName = user.fullName?.takeIf { it.isNotBlank() } ?: resolvedSchoolId
        val userType = UserType.fromServerRole(resolvedRole)

        if (portal == PORTAL_STAFF && !userType.isStaff) {
            sessionStore.clear()
            throw ApiException(
                "This sign-in is for Librarian / Admin accounts only.",
                "ADMIN_ROLE_REQUIRED",
            )
        }

        val roleToStore = if (userType.isStaff) "Admin" else resolvedRole.ifBlank { userType.displayName }
        sessionStore.save(
            token = token,
            accountId = accountId,
            schoolId = resolvedSchoolId,
            fullName = resolvedName,
            role = roleToStore,
        )
        session = LibrarySession(
            userType = userType,
            userName = resolvedName,
            schoolId = resolvedSchoolId,
            accountId = accountId,
            isAuthenticated = true,
        )
        sessionStore.rememberLoginPortal(resolvedSchoolId, portal)
    }

    private companion object {
        const val PORTAL_USER = "user"
        const val PORTAL_STAFF = "staff"
    }

    suspend fun registerStudent(
        schoolId: String,
        firstName: String,
        lastName: String,
        email: String,
        programStrand: String,
        yearGradeLevel: String,
        password: String,
        confirmPassword: String,
    ) = withContext(Dispatchers.IO) {
        lastError = null
        val response = ApiClient.api.register(
            RegisterRequest(
                schoolId = schoolId.trim(),
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                email = email.trim().lowercase(),
                programStrand = programStrand.trim(),
                yearGradeLevel = yearGradeLevel.trim(),
                password = password,
                confirmPassword = confirmPassword,
            ),
        )
        ApiClient.unwrap(response)
    }

    suspend fun restoreSession(): Boolean = withContext(Dispatchers.IO) {
        val stored = sessionStore.getSession() ?: return@withContext false
        try {
            val me = ApiClient.unwrap(ApiClient.api.me())
            val accountId = me.accountId ?: me.id ?: stored.accountId
            val schoolId = me.schoolId ?: stored.schoolId
            val role = me.role ?: stored.role
            val fullName = me.fullName?.takeIf { it.isNotBlank() } ?: stored.fullName
            sessionStore.save(
                token = stored.token,
                accountId = accountId,
                schoolId = schoolId,
                fullName = fullName,
                role = role,
            )
            session = LibrarySession(
                userType = UserType.fromServerRole(role),
                userName = fullName,
                schoolId = schoolId,
                accountId = accountId,
                isAuthenticated = true,
            )
            true
        } catch (error: ApiException) {
            sessionStore.clear()
            clearLocalData()
            session = LibrarySession()
            lastError = error.message
            false
        }
    }

    fun logout() {
        sessionStore.clear()
        clearLocalData()
        session = LibrarySession()
    }

    suspend fun refreshStudentData() = withContext(Dispatchers.IO) {
        if (!session.isAuthenticated) return@withContext
        isLoading = true
        lastError = null
        try {
            loadBooks()
            loadResearch()
            loadBorrowingHistory()
            loadReservations()
            loadFines()
            loadNotifications()
            loadPrintHistory()
            loadAttendancePass()
        } catch (error: ApiException) {
            lastError = error.message
            throw error
        } finally {
            isLoading = false
        }
    }

    suspend fun loadBooks(query: String? = null) = withContext(Dispatchers.IO) {
        val items = ApiClient.unwrap(ApiClient.api.books(query = query))
        books.clear()
        books.addAll(items.map { dto ->
            Book(
                id = (dto.titleId ?: 0).toString(),
                title = dto.title.orEmpty(),
                author = dto.author.orEmpty(),
                isbn = dto.isbn.orEmpty(),
                category = dto.categoryName.orEmpty().ifBlank { "General" },
                yearPublished = dto.publicationYear ?: 0,
                publisher = dto.publisher.orEmpty().ifBlank { "—" },
                shelfLocation = dto.shelfLocation.orEmpty().ifBlank { "—" },
                availableCopies = dto.availableCopiesCount ?: 0,
                totalCopies = dto.totalCopiesCount ?: 0,
                description = dto.currentAvailabilityStatus.orEmpty(),
            )
        })
    }

    suspend fun loadResearch(query: String? = null) = withContext(Dispatchers.IO) {
        val items = ApiClient.unwrap(ApiClient.api.research(query = query))
        research.clear()
        research.addAll(items.map { dto ->
            Research(
                id = (dto.titleId ?: 0).toString(),
                title = dto.title.orEmpty(),
                author = dto.authors.orEmpty(),
                category = dto.department.orEmpty().ifBlank { "Research" },
                yearPublished = dto.publicationYear ?: 0,
                abstract = dto.abstractText.orEmpty(),
                shelfLocation = dto.shelfLocation.orEmpty().ifBlank { "—" },
            )
        })
    }

    suspend fun loadBorrowingHistory() = withContext(Dispatchers.IO) {
        val data = ApiClient.unwrap(ApiClient.api.borrowingHistory())
        borrowingHistory.clear()
        borrowingHistory.addAll((data.items ?: emptyList()).map { item ->
            val status = when (item.status?.lowercase()) {
                "pending" -> BorrowStatus.PENDING
                "overdue" -> BorrowStatus.OVERDUE
                "returned" -> BorrowStatus.RETURNED
                "cancelled" -> BorrowStatus.CANCELLED
                else -> BorrowStatus.BORROWED
            }
            BorrowingRecord(
                id = (item.transactionId ?: 0).toString(),
                book = Book(
                    id = (item.titleId ?: 0).toString(),
                    title = item.title.orEmpty(),
                    author = item.author.orEmpty(),
                    isbn = "",
                    category = "",
                    yearPublished = 0,
                    publisher = "",
                    shelfLocation = "",
                    availableCopies = 0,
                    totalCopies = 0,
                    description = "",
                ),
                borrowDate = formatDate(item.borrowDate) ?: "—",
                dueDate = formatDate(item.dueDate) ?: "—",
                returnDate = formatDate(item.returnDate),
                borrowStatus = status,
                fineStatus = FineStatus.NONE,
            )
        })
    }

    suspend fun loadReservations() = withContext(Dispatchers.IO) {
        val items = ApiClient.unwrap(ApiClient.api.reservations())
        reservations.clear()
        reservations.addAll(items.map { item ->
            Reservation(
                id = (item.reservationId ?: 0).toString(),
                book = Book(
                    id = (item.titleId ?: 0).toString(),
                    title = item.title.orEmpty(),
                    author = item.author.orEmpty(),
                    isbn = "",
                    category = "",
                    yearPublished = 0,
                    publisher = "",
                    shelfLocation = "",
                    availableCopies = 0,
                    totalCopies = 0,
                    description = item.status.orEmpty(),
                ),
                queuePosition = item.queuePosition ?: 0,
                dateReserved = formatDate(item.reservedAt) ?: "—",
                status = item.status.orEmpty(),
            )
        })
    }

    suspend fun loadFines() = withContext(Dispatchers.IO) {
        val data = ApiClient.unwrap(ApiClient.api.fines())
        fines.clear()
        fines.addAll((data.items ?: emptyList()).map { item ->
            val unpaid = (item.balance ?: 0.0) > 0.0 ||
                item.status?.contains("unpaid", ignoreCase = true) == true ||
                item.status?.equals("Open", ignoreCase = true) == true
            FineRecord(
                id = item.id ?: (item.fineId ?: 0).toString(),
                bookTitle = item.title.orEmpty().ifBlank { item.type.orEmpty() },
                amount = item.balance ?: item.assessed ?: 0.0,
                reason = item.reason.orEmpty(),
                dateIssued = formatDate(item.occurredAt) ?: "—",
                paymentStatus = if (unpaid) FineStatus.UNPAID else FineStatus.PAID,
                paymentDate = null,
            )
        })
    }

    suspend fun loadNotifications() = withContext(Dispatchers.IO) {
        val data = ApiClient.unwrap(ApiClient.api.notifications())
        notifications.clear()
        notifications.addAll((data.items ?: emptyList()).map { item ->
            val category = when {
                item.type?.contains("print", ignoreCase = true) == true -> NotificationCategory.PRINTING
                item.type?.contains("book", ignoreCase = true) == true -> NotificationCategory.BOOKS
                item.type?.contains("remind", ignoreCase = true) == true -> NotificationCategory.REMINDERS
                else -> NotificationCategory.GENERAL
            }
            NotificationItem(
                id = (item.notificationId ?: 0).toString(),
                title = item.title.orEmpty(),
                message = item.body.orEmpty(),
                time = formatDate(item.createdAt) ?: "—",
                category = category,
                isRead = item.isRead == true,
            )
        })
    }

    suspend fun loadPrintHistory() = withContext(Dispatchers.IO) {
        val items = ApiClient.unwrap(ApiClient.api.printRequests())
        printHistory.clear()
        printHistory.addAll(items.map { item ->
            val status = when (item.jobStatus?.lowercase()) {
                "printing" -> PrintStatus.PRINTING
                "ready for pickup" -> PrintStatus.READY
                "completed" -> PrintStatus.COMPLETED
                "cancelled" -> PrintStatus.CANCELLED
                else -> PrintStatus.PENDING
            }
            val cost = item.calculatedCost ?: 0.0
            PrintRequest(
                id = (item.requestId ?: 0).toString(),
                fileName = item.fileName.orEmpty(),
                copies = item.numberOfCopies ?: 1,
                printType = item.printType.orEmpty(),
                isColored = item.printType?.contains("color", ignoreCase = true) == true,
                note = item.paperSize.orEmpty(),
                totalCost = cost,
                amountPaid = if (item.paymentStatus.equals("Paid", ignoreCase = true)) cost else 0.0,
                status = status,
                dateSubmitted = formatDate(item.createdAt) ?: "—",
            )
        })
    }

    suspend fun loadAttendancePass() = withContext(Dispatchers.IO) {
        val pass = ApiClient.unwrap(ApiClient.api.attendancePass())
        val png = try {
            ApiClient.api.attendancePassPng().body()?.bytes()?.let { bytes ->
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        } catch (_: Exception) {
            null
        }
        attendancePass = AttendancePass(
            schoolId = pass.profile?.schoolId ?: session.schoolId,
            fullName = pass.profile?.name ?: session.userName,
            role = pass.profile?.role ?: session.userType.displayName,
            visitsThisMonth = pass.summary?.visitsThisMonth ?: 0,
            todayCheckIn = pass.summary?.todayCheckIn,
            commonPurpose = pass.summary?.commonPurpose,
            qrBitmap = png,
        )
    }

    suspend fun submitBorrowCart(): String = withContext(Dispatchers.IO) {
        val titleIds = cart.mapNotNull { it.book.id.toIntOrNull() }
        if (titleIds.isEmpty()) throw ApiException("Your cart is empty.")
        val receipt = ApiClient.unwrap(ApiClient.api.submitBorrowRequest(BorrowSubmitRequest(titleIds)))
        clearCart()
        loadBorrowingHistory()
        receipt.instructions
            ?: "Your borrow request was submitted. Claim the books at the library desk."
    }

    suspend fun reserveBook(book: Book) = withContext(Dispatchers.IO) {
        val titleId = book.id.toIntOrNull() ?: throw ApiException("Invalid book id.")
        ApiClient.unwrap(ApiClient.api.reserveBook(titleId))
        loadReservations()
    }

    suspend fun cancelReservation(reservationId: String) = withContext(Dispatchers.IO) {
        val id = reservationId.toIntOrNull() ?: throw ApiException("Invalid reservation id.")
        ApiClient.unwrap(ApiClient.api.cancelReservation(id))
        loadReservations()
    }

    private fun clearLocalData() {
        cart.clear()
        books.clear()
        research.clear()
        borrowingHistory.clear()
        fines.clear()
        notifications.clear()
        printHistory.clear()
        reservations.clear()
        attendancePass = null
        lastError = null
    }

    private fun formatDate(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return value.take(10)
    }

    enum class AddToCartResult {
        Success, AlreadyInCart, LimitReached
    }
}
