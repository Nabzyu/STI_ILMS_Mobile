package com.example.stismartlib.data

import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.stismartlib.data.models.AttendancePass
import com.example.stismartlib.data.models.AvatarApprovalStatus
import com.example.stismartlib.data.models.Book
import com.example.stismartlib.data.models.BorrowStatus
import com.example.stismartlib.data.models.BorrowingRecord
import com.example.stismartlib.data.models.CartItem
import com.example.stismartlib.data.models.ClearanceFineItem
import com.example.stismartlib.data.models.ClearanceLoanItem
import com.example.stismartlib.data.models.ClearanceLostBookItem
import com.example.stismartlib.data.models.ClearanceStanding
import com.example.stismartlib.data.models.FineReceipt
import com.example.stismartlib.data.models.FineRecord
import com.example.stismartlib.data.models.FineStatus
import com.example.stismartlib.data.models.LibrarySession
import com.example.stismartlib.data.models.NotificationCategory
import com.example.stismartlib.data.models.NotificationItem
import com.example.stismartlib.data.models.PrintQuote
import com.example.stismartlib.data.models.PrintReceipt
import com.example.stismartlib.data.models.PrintRequest
import com.example.stismartlib.data.models.PrintStatus
import com.example.stismartlib.data.models.ProfileAvatar
import com.example.stismartlib.data.models.Research
import com.example.stismartlib.data.models.Reservation
import com.example.stismartlib.data.models.UserProfile
import com.example.stismartlib.network.ApiClient
import com.example.stismartlib.network.ApiException
import com.example.stismartlib.network.dto.BorrowSubmitRequest
import com.example.stismartlib.network.dto.CancelBorrowRequestDto
import com.example.stismartlib.network.dto.ClearanceRecordDto
import com.example.stismartlib.network.dto.FineReceiptDto
import com.example.stismartlib.network.dto.LoginData
import com.example.stismartlib.network.dto.LoginRequest
import com.example.stismartlib.network.dto.LoginResendRequest
import com.example.stismartlib.network.dto.LoginVerifyRequest
import com.example.stismartlib.network.dto.PrintReceiptDto
import com.example.stismartlib.network.dto.PrintRequestDto
import com.example.stismartlib.network.dto.ProfileAvatarDto
import com.example.stismartlib.network.dto.ProfileMeDto
import com.example.stismartlib.network.dto.ProfileUpdateRequest
import com.example.stismartlib.network.dto.RecoveryRequest
import com.example.stismartlib.network.dto.RecoveryResetRequest
import com.example.stismartlib.network.dto.RecoveryStartDto
import com.example.stismartlib.network.dto.RecoveryVerifyRequest
import com.example.stismartlib.network.dto.RegisterData
import com.example.stismartlib.network.dto.RegisterRequest
import com.example.stismartlib.network.dto.RegisterResendRequest
import com.example.stismartlib.network.dto.RegisterVerifyRequest
import com.example.stismartlib.network.dto.ReserveRequestDto
import com.example.stismartlib.session.renewSafely
import com.example.stismartlib.session.SessionStore
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.feedback.userFacingMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response

// shortcut: the hosted API still requires login_as, so each role is tried in turn and a wrong
// password can use up to 3 server attempts. Drop login_as and the role loop once the
// library-revision API is deployed (it reads portal only).
/** Sign-in tries as (login_as, portal) pairs, remembered role first. */
internal fun loginAttempts(preferredLoginAs: String?): List<Pair<String, String>> =
    (listOfNotNull(preferredLoginAs) + listOf("Student", "Faculty", "Admin"))
        .distinct()
        .map { role -> role to if (role == "Admin") "staff" else "user" }

/** Pending Student/Faculty sign-in waiting for the 6-digit code sent to the school email. */
data class LoginChallenge(
    val challengeId: String,
    val schoolId: String,
    val portal: String,
    val emailHint: String?,
    val resendAfterSeconds: Int,
)

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
    val fineReceipts = mutableStateListOf<FineReceipt>()
    var fineReceiptsError by mutableStateOf<String?>(null)
        private set
    var librarySchedule by mutableStateOf<LibrarySchedule?>(null)
        private set
    val notifications = mutableStateListOf<NotificationItem>()
    val printHistory = mutableStateListOf<PrintRequest>()
    val printReceipts = mutableStateListOf<PrintReceipt>()
    val reservations = mutableStateListOf<Reservation>()

    var attendancePass by mutableStateOf<AttendancePass?>(null)
        private set

    var profile by mutableStateOf<UserProfile?>(null)
        private set
    var profileAvatar by mutableStateOf<ProfileAvatar?>(null)
        private set
    var isProfileSaving by mutableStateOf(false)
        private set
    var isAvatarUploading by mutableStateOf(false)
        private set
    var profileActionError by mutableStateOf<String?>(null)
        private set

    var printServiceAccepting by mutableStateOf(true)
        private set
    var printUnavailableReason by mutableStateOf<String?>(null)
        private set
    var printDocxAutoCountAvailable by mutableStateOf(true)
        private set
    var isPrintSubmitting by mutableStateOf(false)
        private set
    var printActionError by mutableStateOf<String?>(null)
        private set

    var clearance by mutableStateOf<ClearanceStanding?>(null)
        private set
    var isClearanceLoading by mutableStateOf(false)
        private set
    var clearanceError by mutableStateOf<String?>(null)
        private set

    fun setUserType(userType: UserType) {
        session = session.copy(userType = userType)
    }

    /** Preview / demo seed; not used by production API flows. */
    fun applyPreviewSchedule(schedule: LibrarySchedule) {
        librarySchedule = schedule
    }

    /** Preview / demo seed; not used by production API flows. */
    fun applyPreviewProfile(userProfile: UserProfile, avatar: ProfileAvatar? = null) {
        profile = userProfile
        profileAvatar = avatar
        session = session.copy(
            userName = userProfile.fullName.ifBlank { session.userName },
            schoolId = userProfile.schoolId.ifBlank { session.schoolId },
            userType = UserType.fromServerRole(userProfile.role).takeIf {
                userProfile.role.isNotBlank()
            } ?: session.userType,
        )
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
     * Single sign-in for Student, Faculty, and Librarian/Admin. The server resolves the role
     * from the account; `portal` only limits which roles may sign in. Try the remembered role
     * first, then the others ([loginAttempts]) on INVALID_CREDENTIALS only.
     *
     * Returns a [LoginChallenge] when the server asks for an email code (Student/Faculty);
     * finish with [verifyLoginCode]. Returns null when the session is already signed in.
     */
    suspend fun login(schoolId: String, password: String): LoginChallenge? = withContext(Dispatchers.IO) {
        lastError = null
        val normalizedSchoolId = schoolId.trim().uppercase()
        var lastInvalid: ApiException? = null
        for ((loginAs, portal) in loginAttempts(sessionStore.preferredLoginAs(normalizedSchoolId))) {
            try {
                return@withContext authenticate(normalizedSchoolId, password, loginAs, portal)
            } catch (error: ApiException) {
                if (error.code != "INVALID_CREDENTIALS") throw error
                lastInvalid = error
            }
        }
        throw lastInvalid
            ?: ApiException("Invalid school ID or password.", "INVALID_CREDENTIALS")
    }

    private suspend fun authenticate(
        schoolId: String,
        password: String,
        loginAs: String,
        portal: String,
    ): LoginChallenge? {
        // Drop any previous token so login is not sent with a stale Authorization header.
        sessionStore.clear()
        clearLocalData()
        session = LibrarySession()

        val data = ApiClient.unwrap(
            ApiClient.api.login(
                LoginRequest(schoolId = schoolId, password = password, portal = portal, loginAs = loginAs),
            ),
        )
        if (data.otpRequired == true) {
            val challengeId = data.challengeId?.takeIf { it.isNotBlank() }
                ?: throw ApiException("Sign-in could not be completed. Please try again.")
            return LoginChallenge(
                challengeId = challengeId,
                schoolId = schoolId,
                portal = portal,
                emailHint = data.emailHint,
                resendAfterSeconds = data.resendAfter ?: DEFAULT_RESEND_SECONDS,
            )
        }
        completeLogin(data, schoolId, portal)
        return null
    }

    suspend fun verifyLoginCode(challenge: LoginChallenge, code: String) = withContext(Dispatchers.IO) {
        lastError = null
        val data = ApiClient.unwrap(
            ApiClient.api.loginVerify(LoginVerifyRequest(challengeId = challenge.challengeId, code = code.trim())),
        )
        completeLogin(data, challenge.schoolId, challenge.portal)
    }

    /** Returns seconds to wait before the next resend. */
    suspend fun resendLoginCode(challenge: LoginChallenge): Int = withContext(Dispatchers.IO) {
        lastError = null
        val data = ApiClient.unwrap(ApiClient.api.loginResend(LoginResendRequest(challenge.challengeId)))
        data.resendAfter ?: DEFAULT_RESEND_SECONDS
    }

    private fun completeLogin(data: LoginData, schoolId: String, portal: String) {
        val user = data.user
            ?: throw ApiException("Sign-in could not be completed. Please try again.")
        val token = data.token?.takeIf { it.isNotBlank() }
            ?: throw ApiException(
                "Sign-in could not be completed. Please try again.",
                "INVALID_TOKEN_RESPONSE",
            )
        val accountId = user.accountId ?: user.id
            ?: throw ApiException("Sign-in could not be completed. Please try again.")
        val resolvedRole = user.role.orEmpty()
        val resolvedSchoolId = user.schoolId ?: schoolId
        val composedName = listOf(user.firstName, user.lastName)
            .mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
            .joinToString(" ")
            .takeIf { it.isNotEmpty() }
        val resolvedName = user.fullName?.trim()?.takeIf { it.isNotEmpty() }
            ?: composedName
            ?: resolvedSchoolId
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
        sessionStore.rememberLoginAs(resolvedSchoolId, roleToStore)
    }

    private companion object {
        const val PORTAL_STAFF = "staff"
        const val DEFAULT_RESEND_SECONDS = 60
        const val MAX_PRINT_DOCUMENT_BYTES = 4 * 1024 * 1024
    }

    /** Creates a pending registration request and emails a 6-digit code to [schoolEmail]. */
    suspend fun registerStudent(
        schoolId: String,
        firstName: String,
        lastName: String,
        schoolEmail: String,
        programStrand: String,
        yearGradeLevel: String,
        password: String,
        confirmPassword: String,
    ): RegisterData = withContext(Dispatchers.IO) {
        lastError = null
        val response = ApiClient.api.register(
            RegisterRequest(
                schoolId = schoolId.trim(),
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                schoolEmail = schoolEmail.trim().lowercase(),
                programStrand = programStrand.trim(),
                yearGradeLevel = yearGradeLevel.trim(),
                password = password,
                confirmPassword = confirmPassword,
            ),
        )
        ApiClient.unwrap(response)
    }

    /** Verifies the registration email code; the request then waits for librarian approval. */
    suspend fun verifyRegistrationCode(schoolId: String, code: String): RegisterData = withContext(Dispatchers.IO) {
        lastError = null
        ApiClient.unwrap(ApiClient.api.registerVerify(RegisterVerifyRequest(schoolId.trim(), code.trim())))
    }

    suspend fun resendRegistrationCode(schoolId: String) = withContext(Dispatchers.IO) {
        lastError = null
        ApiClient.unwrap(ApiClient.api.registerResend(RegisterResendRequest(schoolId.trim())))
        Unit
    }

    /** Emails a 6-digit reset code. The reply is neutral whether or not the account exists. */
    suspend fun startPasswordRecovery(identifier: String): RecoveryStartDto = withContext(Dispatchers.IO) {
        lastError = null
        ApiClient.unwrap(ApiClient.api.recoveryRequest(RecoveryRequest(identifier.trim())))
    }

    /** Returns the one-time reset token for [resetPassword]. */
    suspend fun verifyRecoveryCode(challengeId: String, code: String): String = withContext(Dispatchers.IO) {
        lastError = null
        ApiClient.unwrap(ApiClient.api.recoveryVerify(RecoveryVerifyRequest(challengeId, code.trim())))
            .resetToken?.takeIf { it.isNotBlank() }
            ?: throw ApiException("This password reset has expired or already been used. Start again.")
    }

    /** Returns the server's confirmation message. */
    suspend fun resetPassword(
        challengeId: String,
        resetToken: String,
        password: String,
        confirm: String,
    ): String = withContext(Dispatchers.IO) {
        lastError = null
        ApiClient.unwrap(
            ApiClient.api.recoveryReset(RecoveryResetRequest(challengeId, resetToken, password, confirm)),
        ).message ?: "Password updated. Sign in with your new password."
    }

    /**
     * Extends the server session and stores the fresh token. False on any failure; a 401
     * goes through [ApiClient.unwrap], which ends the session for the nav graph.
     */
    suspend fun renewSession(): Boolean = withContext(Dispatchers.IO) {
        renewSafely {
            val token = ApiClient.unwrap(ApiClient.api.authActivity()).token
                ?.takeIf { it.isNotBlank() }
            if (token != null) sessionStore.updateToken(token)
            token != null
        }
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
            lastError = userFacingMessage(error, "Unable to restore your session.")
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
            runCatching { loadPrintReceipts() }
            runCatching { loadLibrarySchedule() }
            loadAttendancePass()
        } catch (error: ApiException) {
            lastError = userFacingMessage(error, "Unable to refresh your library data.")
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
                callNumber = dto.callNumber.orEmpty(),
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
                pickupDeadline = formatDate(item.pickupDeadline),
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
        loadFineReceipts()
    }

    /** Receipts are secondary to the fine list, so a failure here is shown in their own section. */
    private suspend fun loadFineReceipts() {
        try {
            val rows = ApiClient.unwrap(ApiClient.api.fineReceipts())
            fineReceipts.clear()
            fineReceipts.addAll(rows.mapNotNull { it.toFineReceipt() })
            fineReceiptsError = null
        } catch (error: ApiException) {
            fineReceiptsError = userFacingMessage(error, "Receipts could not be loaded.")
        } catch (error: java.io.IOException) {
            fineReceiptsError = userFacingMessage(error, "Receipts could not be loaded.")
        }
    }

    suspend fun loadFineReceiptPdf(receiptId: String): ByteArray = withContext(Dispatchers.IO) {
        val id = receiptId.toLongOrNull()
            ?: throw ApiException("This receipt could not be downloaded.")
        downloadPdf { ApiClient.api.fineReceiptPdf(id) }
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

    /** Opening hours and closures; screens hide the schedule while this is null. */
    suspend fun loadLibrarySchedule() = withContext(Dispatchers.IO) {
        librarySchedule = ApiClient.unwrap(ApiClient.api.librarySchedule()).toSchedule()
    }

    suspend fun markNotificationRead(notificationId: String) = withContext(Dispatchers.IO) {
        val id = notificationId.toIntOrNull()
            ?: throw ApiException("This notification could not be updated.")
        try {
            ApiClient.unwrap(ApiClient.api.markNotificationRead(id))
            val index = notifications.indexOfFirst { it.id == notificationId }
            if (index >= 0) {
                notifications[index] = notifications[index].copy(isRead = true)
            }
        } catch (error: ApiException) {
            lastError = userFacingMessage(error, "Unable to mark the notification as read.")
            throw error
        }
    }

    suspend fun markAllNotificationsRead() = withContext(Dispatchers.IO) {
        try {
            ApiClient.unwrap(ApiClient.api.markAllNotificationsRead())
            for (index in notifications.indices) {
                if (!notifications[index].isRead) {
                    notifications[index] = notifications[index].copy(isRead = true)
                }
            }
        } catch (error: ApiException) {
            lastError = userFacingMessage(error, "Unable to mark notifications as read.")
            throw error
        }
    }

    suspend fun deleteNotification(notificationId: String) = withContext(Dispatchers.IO) {
        val id = notificationId.toIntOrNull()
            ?: throw ApiException("This notification could not be deleted.")
        try {
            ApiClient.unwrap(ApiClient.api.deleteNotification(id))
            notifications.removeAll { it.id == notificationId }
        } catch (error: ApiException) {
            lastError = userFacingMessage(error, "Unable to delete the notification.")
            throw error
        }
    }

    suspend fun clearAllNotifications() = withContext(Dispatchers.IO) {
        try {
            ApiClient.unwrap(ApiClient.api.deleteAllNotifications())
            notifications.clear()
        } catch (error: ApiException) {
            lastError = userFacingMessage(error, "Unable to clear notifications.")
            throw error
        }
    }

    suspend fun cancelBorrowRequest(transactionId: String, reason: String? = null) =
        withContext(Dispatchers.IO) {
            val id = transactionId.toIntOrNull()
                ?: throw ApiException("This borrow request could not be cancelled.")
            try {
                ApiClient.unwrap(
                    ApiClient.api.cancelBorrowRequest(
                        id,
                        CancelBorrowRequestDto(reason = reason?.trim()?.takeIf { it.isNotEmpty() }),
                    ),
                )
                loadBorrowingHistory()
            } catch (error: ApiException) {
                lastError = userFacingMessage(error, "Unable to cancel the borrow request.")
                throw error
            }
        }

    suspend fun reportLostBook(transactionId: String) = withContext(Dispatchers.IO) {
        val id = transactionId.toIntOrNull()
            ?: throw ApiException("This loan could not be reported as lost.")
        try {
            ApiClient.unwrap(ApiClient.api.reportLostBook(id))
            loadBorrowingHistory()
            runCatching { loadClearance() }
        } catch (error: ApiException) {
            lastError = userFacingMessage(error, "Unable to report the lost book.")
            throw error
        }
    }

    suspend fun loadPrintServiceStatus() = withContext(Dispatchers.IO) {
        val status = ApiClient.unwrap(ApiClient.api.printServiceStatus())
        printServiceAccepting = status.isAccepting()
        printUnavailableReason = status.unavailableReason
        printDocxAutoCountAvailable = status.docxAutoCountAvailable != false
    }

    suspend fun loadPrintHistory() = withContext(Dispatchers.IO) {
        val items = ApiClient.unwrap(ApiClient.api.printRequests())
        printHistory.clear()
        printHistory.addAll(items.map { it.toPrintRequest() })
    }

    suspend fun loadPrintReceipts() = withContext(Dispatchers.IO) {
        val items = ApiClient.unwrap(ApiClient.api.printReceipts())
        printReceipts.clear()
        printReceipts.addAll(items.map { it.toPrintReceipt() })
    }

    suspend fun loadPrintReceiptPdf(receiptId: String): ByteArray = withContext(Dispatchers.IO) {
        val id = receiptId.toIntOrNull()
            ?: throw ApiException("This receipt could not be downloaded.")
        try {
            downloadPdf { ApiClient.api.printReceiptPdf(id) }
        } catch (error: ApiException) {
            printActionError = userFacingMessage(error, "Unable to download the receipt.")
            throw error
        }
    }

    suspend fun quotePrint(
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String,
        copies: Int,
        printType: String,
        paperSize: String,
    ): PrintQuote = withContext(Dispatchers.IO) {
        if (fileBytes.size > MAX_PRINT_DOCUMENT_BYTES) {
            throw ApiException("The document must not exceed 4 MB.")
        }
        if (copies !in 1..50) {
            throw ApiException("Enter 1–50 copies.")
        }
        val document = fileBytes.toDocumentPart(fileName, mimeType)
        val quote = ApiClient.unwrap(
            ApiClient.api.quotePrint(
                document = document,
                numberOfCopies = copies.toFormBody(),
                printType = printType.toFormBody(),
                paperSize = paperSize.toFormBody(),
            ),
        )
        val pageCount = quote.pageCount ?: throw ApiException("Unable to read the document page count.")
        val sha = quote.documentSha256?.takeIf { it.isNotBlank() }
            ?: throw ApiException("Unable to verify the document for quoting.")
        PrintQuote(
            pageCount = pageCount,
            totalSheets = quote.totalSheets ?: (pageCount * copies),
            calculatedCost = quote.calculatedCost ?: 0.0,
            documentSha256 = sha,
            printableFileName = quote.printableFileName,
        )
    }

    suspend fun submitPrint(
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String,
        copies: Int,
        printType: String,
        paperSize: String,
        quote: PrintQuote,
        notes: String,
    ) = withContext(Dispatchers.IO) {
        isPrintSubmitting = true
        printActionError = null
        try {
            if (!printServiceAccepting) {
                throw ApiException(
                    printUnavailableReason?.takeIf { it.isNotBlank() }
                        ?: "The printing service is not accepting requests right now.",
                )
            }
            if (fileBytes.size > MAX_PRINT_DOCUMENT_BYTES) {
                throw ApiException("The document must not exceed 4 MB.")
            }
            val document = fileBytes.toDocumentPart(fileName, mimeType)
            val notesBody = notes.trim().takeIf { it.isNotEmpty() }?.toFormBody()
            ApiClient.unwrap(
                ApiClient.api.submitPrintRequest(
                    document = document,
                    numberOfCopies = copies.toFormBody(),
                    pageCount = quote.pageCount.toFormBody(),
                    documentSha256 = quote.documentSha256.toFormBody(),
                    quotedCost = quote.calculatedCost.toString().toFormBody(),
                    printType = printType.toFormBody(),
                    paperSize = paperSize.toFormBody(),
                    optionalNotes = notesBody,
                ),
            )
            loadPrintHistory()
        } catch (error: ApiException) {
            printActionError = userFacingMessage(error, "Unable to submit the print request.")
            throw error
        } catch (error: Exception) {
            printActionError = userFacingMessage(error, "Unable to submit the print request.")
            throw error
        } finally {
            isPrintSubmitting = false
        }
    }

    suspend fun cancelPrint(requestId: String) = withContext(Dispatchers.IO) {
        printActionError = null
        val id = requestId.toIntOrNull()
            ?: throw ApiException("This print request could not be cancelled.")
        try {
            ApiClient.unwrap(ApiClient.api.cancelPrintRequest(id))
            loadPrintHistory()
        } catch (error: ApiException) {
            printActionError = userFacingMessage(error, "Unable to cancel the print request.")
            throw error
        }
    }

    suspend fun loadClearance() = withContext(Dispatchers.IO) {
        isClearanceLoading = true
        clearanceError = null
        try {
            val dto = ApiClient.unwrap(ApiClient.api.clearanceMe())
            clearance = dto.toClearanceStanding()
        } catch (error: ApiException) {
            clearanceError = userFacingMessage(error, "Unable to load your clearance status.")
            throw error
        } finally {
            isClearanceLoading = false
        }
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

    suspend fun reserveBook(book: Book) =
        withContext(Dispatchers.IO) {
            val titleId = book.id.toIntOrNull()
                ?: throw ApiException("This book could not be reserved.")
            ApiClient.unwrap(ApiClient.api.reserveBook(ReserveRequestDto(bookTitleId = titleId)))
            loadReservations()
        }

    suspend fun cancelReservation(reservationId: String) = withContext(Dispatchers.IO) {
        val id = reservationId.toIntOrNull()
            ?: throw ApiException("This reservation could not be updated.")
        ApiClient.unwrap(ApiClient.api.cancelReservation(id))
        loadReservations()
    }

    suspend fun loadProfile() = withContext(Dispatchers.IO) {
        val dto = ApiClient.unwrap(ApiClient.api.profileMe())
        profile = dto.toUserProfile()
    }

    suspend fun loadProfileAvatar() = withContext(Dispatchers.IO) {
        val dto = ApiClient.unwrap(ApiClient.api.profileAvatarMe())
        profileAvatar = dto.toProfileAvatar()
    }

    suspend fun refreshProfileScreen() = withContext(Dispatchers.IO) {
        isLoading = true
        lastError = null
        try {
            loadProfile()
            loadProfileAvatar()
        } catch (error: ApiException) {
            lastError = userFacingMessage(error, "Unable to load your profile.")
            throw error
        } finally {
            isLoading = false
        }
    }

    suspend fun saveProfile(
        firstName: String,
        lastName: String,
        programStrand: String?,
        yearGradeLevel: String?,
    ) = withContext(Dispatchers.IO) {
        isProfileSaving = true
        profileActionError = null
        try {
            ApiClient.unwrap(
                ApiClient.api.updateProfileMe(
                    ProfileUpdateRequest(
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        programStrand = programStrand?.trim()?.takeIf { it.isNotEmpty() },
                        yearGradeLevel = yearGradeLevel?.trim()?.takeIf { it.isNotEmpty() },
                    ),
                ),
            )
            // PATCH /profile/me returns { id, changed_fields }, not the full profile.
            loadProfile()
            val updated = profile ?: throw ApiException("Profile saved, but the updated details could not be loaded.")
            val stored = sessionStore.getSession()
            if (stored != null) {
                val displayName = updated.fullName.ifBlank {
                    listOf(updated.firstName, updated.lastName)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                }.ifBlank { stored.fullName }
                sessionStore.save(
                    token = stored.token,
                    accountId = stored.accountId,
                    schoolId = updated.schoolId.ifBlank { stored.schoolId },
                    fullName = displayName,
                    role = updated.role.ifBlank { stored.role },
                )
                session = session.copy(
                    userName = displayName,
                    schoolId = updated.schoolId.ifBlank { session.schoolId },
                )
            }
        } catch (error: ApiException) {
            profileActionError = userFacingMessage(error, "Unable to update your profile.")
            throw error
        } finally {
            isProfileSaving = false
        }
    }

    suspend fun uploadProfileAvatar(imageBytes: ByteArray, mimeType: String, fileName: String) =
        withContext(Dispatchers.IO) {
            isAvatarUploading = true
            profileActionError = null
            try {
                val mediaType = mimeType.toMediaTypeOrNull() ?: "image/jpeg".toMediaType()
                val body = imageBytes.toRequestBody(mediaType)
                val part = MultipartBody.Part.createFormData("image", fileName, body)
                ApiClient.unwrap(ApiClient.api.uploadProfileAvatarMe(part))
                // Upload returns { status: "Pending" }; reload for signed URL / rejection state.
                loadProfileAvatar()
            } catch (error: ApiException) {
                profileActionError = userFacingMessage(error, "Unable to upload your photo.")
                throw error
            } finally {
                isAvatarUploading = false
            }
        }

    private fun ProfileMeDto.toUserProfile(): UserProfile {
        val composed = listOf(firstName, lastName).filter { !it.isNullOrBlank() }.joinToString(" ")
        return UserProfile(
            accountId = accountId ?: id ?: 0,
            schoolId = schoolId.orEmpty(),
            firstName = firstName.orEmpty(),
            lastName = lastName.orEmpty(),
            fullName = fullName?.takeIf { it.isNotBlank() } ?: composed,
            email = email.orEmpty(),
            role = role.orEmpty(),
            programStrand = programStrand.orEmpty(),
            yearGradeLevel = yearGradeLevel.orEmpty(),
            status = (accountStatus ?: status).orEmpty(),
        )
    }

    private fun ProfileAvatarDto.toProfileAvatar(): ProfileAvatar {
        val raw = resolvedStatus().orEmpty()
        val mapped = when {
            raw.isBlank() && resolvedUrl().isNullOrBlank() -> AvatarApprovalStatus.NONE
            raw.contains("pend", ignoreCase = true) -> AvatarApprovalStatus.PENDING
            raw.contains("reject", ignoreCase = true) ||
                raw.contains("denied", ignoreCase = true) -> AvatarApprovalStatus.REJECTED
            raw.contains("approv", ignoreCase = true) ||
                raw.equals("active", ignoreCase = true) -> AvatarApprovalStatus.APPROVED
            !resolvedUrl().isNullOrBlank() -> AvatarApprovalStatus.APPROVED
            else -> AvatarApprovalStatus.UNKNOWN
        }
        return ProfileAvatar(
            status = mapped,
            imageUrl = resolvedUrl(),
            rejectionReason = resolvedRejectionReason(),
            note = message,
        )
    }

    private fun clearLocalData() {
        cart.clear()
        books.clear()
        research.clear()
        borrowingHistory.clear()
        fines.clear()
        fineReceipts.clear()
        fineReceiptsError = null
        librarySchedule = null
        notifications.clear()
        printHistory.clear()
        printReceipts.clear()
        reservations.clear()
        attendancePass = null
        profile = null
        profileAvatar = null
        isProfileSaving = false
        isAvatarUploading = false
        profileActionError = null
        printActionError = null
        printServiceAccepting = true
        printUnavailableReason = null
        clearance = null
        clearanceError = null
        lastError = null
    }

    private fun PrintReceiptDto.toPrintReceipt(): PrintReceipt {
        return PrintReceipt(
            id = (printReceiptId ?: 0).toString(),
            receiptNumber = receiptNumber.orEmpty(),
            verificationCode = verificationCode.orEmpty(),
            fileName = fileName.orEmpty().ifBlank { "Print receipt" },
            amountReceived = amountReceived ?: 0.0,
            printType = printType.orEmpty(),
            paperSize = paperSize.orEmpty(),
            copies = numberOfCopies ?: 1,
            pageCount = pageCount ?: 0,
            receivedAt = formatDate(receivedAt) ?: "—",
            receivedBy = receivedBy.orEmpty(),
            status = receiptStatus.orEmpty(),
        )
    }

    private fun PrintRequestDto.toPrintRequest(): PrintRequest {
        val status = when (jobStatus?.lowercase()) {
            "printing" -> PrintStatus.PRINTING
            "ready for pickup" -> PrintStatus.READY
            "completed" -> PrintStatus.COMPLETED
            "cancelled" -> PrintStatus.CANCELLED
            else -> PrintStatus.PENDING
        }
        val cost = calculatedCost ?: 0.0
        return PrintRequest(
            id = (requestId ?: 0).toString(),
            fileName = fileName.orEmpty(),
            copies = numberOfCopies ?: 1,
            printType = printType.orEmpty(),
            isColored = printType?.contains("color", ignoreCase = true) == true,
            note = paperSize.orEmpty(),
            totalCost = cost,
            amountPaid = if (paymentStatus.equals("Paid", ignoreCase = true)) cost else 0.0,
            status = status,
            dateSubmitted = formatDate(createdAt) ?: "—",
            paymentStatus = paymentStatus.orEmpty(),
            pageCount = pageCount ?: 0,
        )
    }

    private fun ClearanceRecordDto.toClearanceStanding(): ClearanceStanding {
        val statusText = (status ?: computedStatus).orEmpty()
        val cleared = statusText.equals("Cleared", ignoreCase = true)
        return ClearanceStanding(
            isCleared = cleared,
            statusLabel = if (cleared) "Cleared" else "Not Cleared",
            reason = reason.orEmpty(),
            checkedAt = checkedAt,
            activeLoans = summary?.activeLoans ?: loans.orEmpty().size,
            unpaidOverdueFines = summary?.unpaidOverdueFines ?: fines.orEmpty().size,
            unpaidReplacementCharges = summary?.unpaidReplacementCharges ?: 0.0,
            totalOutstanding = summary?.totalOutstanding
                ?: ((fines.orEmpty().sumOf { it.amount ?: 0.0 }) +
                    (lostBooks.orEmpty().sumOf { it.replacementCharge ?: 0.0 })),
            loans = loans.orEmpty().map { loan ->
                ClearanceLoanItem(
                    title = loan.title.orEmpty().ifBlank { "Untitled" },
                    status = loan.status.orEmpty(),
                    dueAt = formatDate(loan.dueAt),
                    currentFine = loan.currentFine ?: 0.0,
                )
            },
            fines = fines.orEmpty().map { fine ->
                ClearanceFineItem(
                    title = fine.title.orEmpty().ifBlank { "Fine" },
                    amount = fine.amount ?: 0.0,
                    basis = fine.basis,
                )
            },
            lostBooks = lostBooks.orEmpty().map { lost ->
                ClearanceLostBookItem(
                    title = lost.title.orEmpty().ifBlank { "Lost item" },
                    status = lost.status.orEmpty(),
                    replacementCharge = lost.replacementCharge ?: 0.0,
                    paymentStatus = lost.paymentStatus.orEmpty(),
                )
            },
            activeOverrideReason = activeOverride?.reason,
        )
    }

    private fun ByteArray.toDocumentPart(fileName: String, mimeType: String): MultipartBody.Part {
        val mediaType = mimeType.toMediaTypeOrNull() ?: "application/pdf".toMediaType()
        val body = toRequestBody(mediaType)
        return MultipartBody.Part.createFormData("document", fileName, body)
    }

    private fun String.toFormBody(): RequestBody =
        toRequestBody("text/plain".toMediaType())

    private fun Int.toFormBody(): RequestBody = toString().toFormBody()

    private suspend fun downloadPdf(call: suspend () -> Response<ResponseBody>): ByteArray {
        val response = call()
        if (!response.isSuccessful) {
            throw ApiException(
                message = when (response.code()) {
                    404 -> "This feature is not available right now."
                    401 -> "Sign-in is required."
                    403 -> "You do not have access to this action."
                    else -> "Unable to download the receipt."
                },
                httpStatus = response.code(),
            )
        }
        return response.body()?.bytes()
            ?: throw ApiException("The receipt PDF was empty.")
    }

    private fun formatDate(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return value.take(10)
    }

    enum class AddToCartResult {
        Success, AlreadyInCart, LimitReached
    }
}

/** Null for a row without an id; the PDF link needs one. */
internal fun FineReceiptDto.toFineReceipt(): FineReceipt? {
    val id = receiptId?.takeIf { it > 0 } ?: return null
    return FineReceipt(
        id = id.toString(),
        number = receiptNumber?.takeIf { it.isNotBlank() } ?: "Receipt $id",
        amount = amountReceived ?: 0.0,
        method = paymentMethod?.takeIf { it.isNotBlank() } ?: "Cash",
        receivedOn = receivedAt?.take(10).orEmpty(),
        status = status.orEmpty(),
    )
}
