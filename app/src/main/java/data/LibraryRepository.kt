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
import com.example.stismartlib.data.models.FineRecord
import com.example.stismartlib.data.models.FineStatus
import com.example.stismartlib.data.models.LibrarySession
import com.example.stismartlib.data.models.NotificationCategory
import com.example.stismartlib.data.models.NotificationItem
import com.example.stismartlib.data.models.PrintRequest
import com.example.stismartlib.data.models.PrintStatus
import com.example.stismartlib.data.models.ProfileAvatar
import com.example.stismartlib.data.models.Research
import com.example.stismartlib.data.models.Reservation
import com.example.stismartlib.data.models.UserProfile
import com.example.stismartlib.network.ApiClient
import com.example.stismartlib.network.ApiException
import com.example.stismartlib.network.dto.BorrowSubmitRequest
import com.example.stismartlib.network.dto.LoginData
import com.example.stismartlib.network.dto.LoginRequest
import com.example.stismartlib.network.dto.LoginResendRequest
import com.example.stismartlib.network.dto.LoginVerifyRequest
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
import okhttp3.RequestBody.Companion.toRequestBody

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
    val notifications = mutableStateListOf<NotificationItem>()
    val printHistory = mutableStateListOf<PrintRequest>()
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

    fun setUserType(userType: UserType) {
        session = session.copy(userType = userType)
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
        notifications.clear()
        printHistory.clear()
        reservations.clear()
        attendancePass = null
        profile = null
        profileAvatar = null
        isProfileSaving = false
        isAvatarUploading = false
        profileActionError = null
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
