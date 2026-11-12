package com.example.stismartlib.data

import android.graphics.BitmapFactory
import android.os.SystemClock
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
import com.example.stismartlib.network.SESSION_IDLE_CODE
import com.example.stismartlib.network.SessionEndingCodes
import com.example.stismartlib.network.dto.BorrowSubmitRequest
import com.example.stismartlib.network.dto.LoginRequest
import com.example.stismartlib.network.dto.LoginResendRequest
import com.example.stismartlib.network.dto.LoginVerifyRequest
import com.example.stismartlib.network.dto.RegisterRequest
import com.example.stismartlib.session.SessionActivityTracker
import com.example.stismartlib.session.SessionStore
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.auth.normalizeSchoolId
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class LibraryRepository(
    private val sessionStore: SessionStore,
    private val clock: () -> Long = { SystemClock.elapsedRealtime() },
) {
    var session by mutableStateOf(LibrarySession())
        private set

    /** Why the last session ended without a sign-out (server code or [SESSION_IDLE_CODE]). */
    var sessionEndReason by mutableStateOf<String?>(null)
        private set

    private val activityTracker = SessionActivityTracker()
    private val backgroundScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, _ -> })
    private var activityJob: Job? = null

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
     * Sign-in for one server portal: `user` (Student/Faculty) or `staff` (Admin/Librarian/Staff).
     * The server counts a portal/role mismatch as a wrong password, so the app never retries the
     * other portal on its own; the login screen lets the user pick the staff sign-in instead.
     * Student/Faculty get [LoginOutcome.CodeRequired] and finish with [verifyLoginCode].
     */
    suspend fun login(schoolId: String, password: String, staffPortal: Boolean): LoginOutcome =
        withContext(Dispatchers.IO) {
            lastError = null
            val normalizedSchoolId = normalizeSchoolId(schoolId)
            val portal = if (staffPortal) PORTAL_STAFF else PORTAL_USER
            // Drop any previous token so login is not sent with a stale Authorization header.
            endLocalSession()
            val data = ApiClient.unwrap(
                ApiClient.api.login(
                    LoginRequest(schoolId = normalizedSchoolId, password = password, portal = portal),
                ),
            )
            when (val step = interpretLoginData(data, normalizedSchoolId)) {
                is LoginStep.CodeRequired -> {
                    sessionStore.rememberLoginPortal(normalizedSchoolId, portal)
                    LoginOutcome.CodeRequired(step.challenge)
                }
                is LoginStep.Authenticated -> {
                    completeSignIn(step, portal)
                    LoginOutcome.SignedIn
                }
            }
        }

    /** Second step for Student/Faculty: exchange the emailed 6-digit code for a session. */
    suspend fun verifyLoginCode(schoolId: String, challengeId: String, code: String) = withContext(Dispatchers.IO) {
        lastError = null
        val data = ApiClient.unwrap(ApiClient.api.loginVerify(LoginVerifyRequest(challengeId, code.trim())))
        when (val step = interpretLoginData(data, normalizeSchoolId(schoolId))) {
            is LoginStep.Authenticated -> completeSignIn(step, PORTAL_USER)
            is LoginStep.CodeRequired ->
                throw ApiException("The server returned an incomplete login response.", "INVALID_LOGIN_RESPONSE")
        }
    }

    /** Sends a new code (the previous one stops working). Returns seconds until the next resend. */
    suspend fun resendLoginCode(challengeId: String): Int = withContext(Dispatchers.IO) {
        ApiClient.unwrap(ApiClient.api.loginResend(LoginResendRequest(challengeId))).resendAfter ?: 60
    }

    /** True/false when this school ID last signed in through the staff/user portal; null if unknown. */
    fun prefersStaffPortal(schoolId: String): Boolean? =
        sessionStore.preferredLoginPortal(normalizeSchoolId(schoolId))?.let { it == PORTAL_STAFF }

    private fun completeSignIn(step: LoginStep.Authenticated, portal: String) {
        val userType = UserType.fromServerRole(step.role)
        if (portal == PORTAL_STAFF && !userType.isStaff) {
            sessionStore.clear()
            throw ApiException(
                "This sign-in is for Librarian and library staff accounts only.",
                "ADMIN_ROLE_REQUIRED",
            )
        }
        sessionStore.save(
            token = step.token,
            accountId = step.accountId,
            schoolId = step.schoolId,
            fullName = step.fullName,
            role = step.role.ifBlank { userType.displayName },
        )
        session = LibrarySession(
            userType = userType,
            userName = step.fullName,
            schoolId = step.schoolId,
            accountId = step.accountId,
            isAuthenticated = true,
        )
        sessionStore.rememberLoginPortal(step.schoolId, portal)
        sessionEndReason = null
        activityTracker.start(clock())
    }

    /** Called by [ApiClient] after a session-ending error code (stored token already cleared). */
    fun onServerSessionEnded(code: String) {
        if (!session.isAuthenticated) return
        endLocalSession()
        sessionEndReason = code
    }

    /** Hands the reason the last session ended to the login screen, once. */
    fun consumeSessionEndReason(): String? = sessionEndReason.also { sessionEndReason = null }

    /**
     * Any touch or key press counts as activity (like the web app's pointer/key listeners).
     * Pings `/auth/activity` at most every 30 s, always following the latest interaction.
     */
    fun onUserInteraction() {
        if (!session.isAuthenticated || !activityTracker.isActive) return
        val now = clock()
        if (activityTracker.isIdleExpired(now)) {
            endIdleSession()
            return
        }
        activityTracker.recordInteraction(now)
        if (activityJob?.isActive == true) return
        val wait = activityTracker.delayUntilNextPing(now)
        activityJob = backgroundScope.launch {
            delay(wait)
            sendActivity()
        }
    }

    /** Call when the app returns to the foreground: ends a session idle for 10 minutes or more. */
    fun checkIdleTimeout() {
        if (session.isAuthenticated && activityTracker.isIdleExpired(clock())) endIdleSession()
    }

    private suspend fun sendActivity() {
        if (!session.isAuthenticated) return
        val accountId = session.accountId
        try {
            val data = ApiClient.unwrap(ApiClient.api.sessionActivity())
            if (session.isAuthenticated && session.accountId == accountId) {
                data.token?.takeIf { it.isNotBlank() }?.let(sessionStore::updateToken)
                activityTracker.markPinged(clock())
            }
        } catch (_: ApiException) {
            // Session-ending codes are handled through ApiClient.onSessionEnded; others retry on next touch.
        } catch (_: IOException) {
            // Offline: the next interaction retries.
        }
    }

    private fun endIdleSession() {
        endLocalSession()
        sessionEndReason = SESSION_IDLE_CODE
    }

    private fun endLocalSession() {
        activityJob?.cancel()
        activityJob = null
        activityTracker.stop()
        sessionStore.clear()
        clearLocalData()
        session = LibrarySession()
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
            activityTracker.start(clock())
            // `/auth/me` does not count as activity on the server; renew the idle window now.
            sendActivity()
            session.isAuthenticated
        } catch (error: ApiException) {
            endLocalSession()
            lastError = error.message
            if (error.code in SessionEndingCodes) sessionEndReason = error.code
            false
        }
    }

    fun logout() {
        endLocalSession()
        sessionEndReason = null
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
