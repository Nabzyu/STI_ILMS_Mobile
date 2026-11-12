package com.example.stismartlib.network

import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.network.dto.AttendancePassDto
import com.example.stismartlib.network.dto.BookCatalogItemDto
import com.example.stismartlib.network.dto.BookCategoryDto
import com.example.stismartlib.network.dto.InLibraryUseDto
import com.example.stismartlib.network.dto.BorrowRequestReceiptDto
import com.example.stismartlib.network.dto.BorrowSubmitRequest
import com.example.stismartlib.network.dto.BorrowingHistoryDataDto
import com.example.stismartlib.network.dto.CartStateDto
import com.example.stismartlib.network.dto.CatalogCopyDto
import com.example.stismartlib.network.dto.FineListDto
import com.example.stismartlib.network.dto.FineReceiptDto
import com.example.stismartlib.network.dto.LibraryScheduleDto
import com.example.stismartlib.network.dto.MyBooksDto
import com.example.stismartlib.network.dto.LoginData
import com.example.stismartlib.network.dto.LoginRequest
import com.example.stismartlib.network.dto.LoginResendData
import com.example.stismartlib.network.dto.LoginResendRequest
import com.example.stismartlib.network.dto.LoginVerifyRequest
import com.example.stismartlib.network.dto.MeUser
import com.example.stismartlib.network.dto.NotificationListDto
import com.example.stismartlib.network.dto.CancelBorrowRequestDto
import com.example.stismartlib.network.dto.ClearanceRecordDto
import com.example.stismartlib.network.dto.PasswordRecoveryChallengeData
import com.example.stismartlib.network.dto.PasswordRecoveryRequest
import com.example.stismartlib.network.dto.PasswordRecoveryTokenData
import com.example.stismartlib.network.dto.PasswordRecoveryVerifyRequest
import com.example.stismartlib.network.dto.PasswordResetData
import com.example.stismartlib.network.dto.PasswordResetRequest
import com.example.stismartlib.network.dto.PrintQuoteDto
import com.example.stismartlib.network.dto.PrintReceiptDto
import com.example.stismartlib.network.dto.PrintRequestDto
import com.example.stismartlib.network.dto.PrintServiceStatusDto
import com.example.stismartlib.network.dto.ProfileAvatarDto
import com.example.stismartlib.network.dto.ProfileMeDto
import com.example.stismartlib.network.dto.ProfileUpdateRequest
import com.example.stismartlib.network.dto.RosterBeginRequest
import com.example.stismartlib.network.dto.RosterChallengeData
import com.example.stismartlib.network.dto.RosterCompleteData
import com.example.stismartlib.network.dto.RosterCompleteRequest
import com.example.stismartlib.network.dto.RosterGrantData
import com.example.stismartlib.network.dto.RosterStatusData
import com.example.stismartlib.network.dto.RosterVerifyRequest
import com.example.stismartlib.network.dto.ResearchCatalogItemDto
import com.example.stismartlib.network.dto.UserDashboardDto
import com.example.stismartlib.network.dto.ReservationItemDto
import com.example.stismartlib.network.dto.ReserveRequestDto
import com.example.stismartlib.network.dto.ReserveResultDto
import com.example.stismartlib.network.dto.SessionActivityData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface LibraryApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<ApiEnvelope<LoginData>>

    @POST("api/v1/auth/login/verify")
    suspend fun loginVerify(@Body body: LoginVerifyRequest): Response<ApiEnvelope<LoginData>>

    @POST("api/v1/auth/login/resend")
    suspend fun loginResend(@Body body: LoginResendRequest): Response<ApiEnvelope<LoginResendData>>

    /** Keeps the server session alive (10-minute idle limit) and returns a renewed token. */
    @POST("api/v1/auth/activity")
    suspend fun sessionActivity(@Body body: Map<String, String> = emptyMap()): Response<ApiEnvelope<SessionActivityData>>

    @POST("api/v1/auth/password-recovery/request")
    suspend fun passwordRecoveryRequest(@Body body: PasswordRecoveryRequest): Response<ApiEnvelope<PasswordRecoveryChallengeData>>

    @POST("api/v1/auth/password-recovery/verify")
    suspend fun passwordRecoveryVerify(@Body body: PasswordRecoveryVerifyRequest): Response<ApiEnvelope<PasswordRecoveryTokenData>>

    @POST("api/v1/auth/password-recovery/reset")
    suspend fun passwordReset(@Body body: PasswordResetRequest): Response<ApiEnvelope<PasswordResetData>>

    /** Student account activation against the semester roster (replaces the retired `/auth/register`). */
    @GET("api/v1/auth/roster-registration")
    suspend fun rosterStatus(): Response<ApiEnvelope<RosterStatusData>>

    @POST("api/v1/auth/roster-registration/begin")
    suspend fun rosterBegin(@Body body: RosterBeginRequest): Response<ApiEnvelope<RosterChallengeData>>

    @POST("api/v1/auth/roster-registration/verify")
    suspend fun rosterVerify(@Body body: RosterVerifyRequest): Response<ApiEnvelope<RosterGrantData>>

    @POST("api/v1/auth/roster-registration/complete")
    suspend fun rosterComplete(@Body body: RosterCompleteRequest): Response<ApiEnvelope<RosterCompleteData>>

    @GET("api/v1/auth/me")
    suspend fun me(): Response<ApiEnvelope<MeUser>>

    @GET("api/v1/catalog/books")
    suspend fun books(
        @Query("q") query: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("category_id") categoryId: Int? = null,
    ): Response<ApiEnvelope<List<BookCatalogItemDto>>>

    @GET("api/v1/catalog/categories")
    suspend fun bookCategories(): Response<ApiEnvelope<List<BookCategoryDto>>>

    @GET("api/v1/catalog/books/{titleId}")
    suspend fun bookOverview(@Path("titleId") titleId: Int): Response<ApiEnvelope<BookCatalogItemDto>>

    @GET("api/v1/catalog/research/{id}")
    suspend fun researchOverview(@Path("id") id: Int): Response<ApiEnvelope<ResearchCatalogItemDto>>

    @GET("api/v1/borrowing/in-library")
    suspend fun inLibraryUses(): Response<ApiEnvelope<List<InLibraryUseDto>>>

    @POST("api/v1/reservations/request")
    suspend fun reserveBook(@Body body: ReserveRequestDto): Response<ApiEnvelope<ReserveResultDto>>

    @GET("api/v1/catalog/research")
    suspend fun research(
        @Query("q") query: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): Response<ApiEnvelope<List<ResearchCatalogItemDto>>>

    @GET("api/v1/borrowing/history")
    suspend fun borrowingHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): Response<ApiEnvelope<BorrowingHistoryDataDto>>

    @POST("api/v1/borrow/submit-request")
    suspend fun submitBorrowRequest(@Body body: BorrowSubmitRequest): Response<ApiEnvelope<BorrowRequestReceiptDto>>

    @PUT("api/v1/circulation/requests/{transactionId}/cancel")
    suspend fun cancelBorrowRequest(
        @Path("transactionId") transactionId: Int,
        @Body body: CancelBorrowRequestDto = CancelBorrowRequestDto(),
    ): Response<ApiEnvelope<Any>>

    @GET("api/v1/reservations")
    suspend fun reservations(): Response<ApiEnvelope<List<ReservationItemDto>>>

    @PUT("api/v1/reservations/{reservationId}/cancel")
    suspend fun cancelReservation(@Path("reservationId") reservationId: Int): Response<ApiEnvelope<Any>>

    @GET("api/v1/fines/me")
    suspend fun fines(
        @Query("status") status: String = "all",
        @Query("period") period: String = "all",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): Response<ApiEnvelope<FineListDto>>

    @GET("api/v1/profile/me")
    suspend fun profileMe(): Response<ApiEnvelope<ProfileMeDto>>

    @PATCH("api/v1/profile/me")
    suspend fun updateProfileMe(@Body body: ProfileUpdateRequest): Response<ApiEnvelope<ProfileMeDto>>

    @GET("api/v1/profile/avatar/me")
    suspend fun profileAvatarMe(): Response<ApiEnvelope<ProfileAvatarDto>>

    @Multipart
    @POST("api/v1/profile/avatar/me")
    suspend fun uploadProfileAvatarMe(
        @Part image: MultipartBody.Part,
    ): Response<ApiEnvelope<ProfileAvatarDto>>

    @GET("api/v1/notifications")
    suspend fun notifications(@Query("limit") limit: Int = 100): Response<ApiEnvelope<NotificationListDto>>

    @PATCH("api/v1/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: Int): Response<ApiEnvelope<Any>>

    @PATCH("api/v1/notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<ApiEnvelope<Any>>

    @DELETE("api/v1/notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: Int): Response<ApiEnvelope<Any>>

    @DELETE("api/v1/notifications")
    suspend fun deleteAllNotifications(): Response<ApiEnvelope<Any>>

    @GET("api/v1/printing/service-status")
    suspend fun printServiceStatus(): Response<ApiEnvelope<PrintServiceStatusDto>>

    @GET("api/v1/printing/requests")
    suspend fun printRequests(): Response<ApiEnvelope<List<PrintRequestDto>>>

    @GET("api/v1/printing/receipts")
    suspend fun printReceipts(): Response<ApiEnvelope<List<PrintReceiptDto>>>

    @GET("api/v1/printing/receipts/{receiptId}")
    suspend fun printReceipt(
        @Path("receiptId") receiptId: Int,
    ): Response<ApiEnvelope<PrintReceiptDto>>

    @GET("api/v1/catalog/copies/{barcode}")
    suspend fun catalogCopy(@Path("barcode") barcode: String): Response<ApiEnvelope<CatalogCopyDto>>

    @GET("api/v1/dashboard")
    suspend fun userDashboard(): Response<ApiEnvelope<UserDashboardDto>>

    @GET("api/v1/borrow/cart-state")
    suspend fun cartState(): Response<ApiEnvelope<CartStateDto>>

    @GET("api/v1/borrowing/my-books")
    suspend fun myBooks(): Response<ApiEnvelope<MyBooksDto>>

    @GET("api/v1/notifications/schedule")
    suspend fun librarySchedule(): Response<ApiEnvelope<LibraryScheduleDto>>

    @GET("api/v1/fines/receipts")
    suspend fun fineReceipts(): Response<ApiEnvelope<List<FineReceiptDto>>>

    @GET("api/v1/fines/receipts/{receiptId}.pdf")
    suspend fun fineReceiptPdf(@Path("receiptId") receiptId: Long): Response<ResponseBody>

    @GET("api/v1/printing/receipts/{receiptId}/pdf")
    suspend fun printReceiptPdf(
        @Path("receiptId") receiptId: Int,
    ): Response<ResponseBody>

    @GET("api/v1/printing/requests/{requestId}/invoice/pdf")
    suspend fun printInvoicePdf(
        @Path("requestId") requestId: Int,
    ): Response<ResponseBody>

    @Multipart
    @POST("api/v1/printing/quote")
    suspend fun quotePrint(
        @Part document: MultipartBody.Part,
        @Part("number_of_copies") numberOfCopies: RequestBody,
        @Part("print_type") printType: RequestBody,
        @Part("paper_size") paperSize: RequestBody,
    ): Response<ApiEnvelope<PrintQuoteDto>>

    @Multipart
    @POST("api/v1/printing/requests")
    suspend fun submitPrintRequest(
        @Part document: MultipartBody.Part,
        @Part("number_of_copies") numberOfCopies: RequestBody,
        @Part("page_count") pageCount: RequestBody,
        @Part("document_sha256") documentSha256: RequestBody,
        @Part("quoted_cost") quotedCost: RequestBody,
        @Part("print_type") printType: RequestBody,
        @Part("paper_size") paperSize: RequestBody,
        @Part("optional_notes") optionalNotes: RequestBody?,
    ): Response<ApiEnvelope<PrintRequestDto>>

    @PUT("api/v1/printing/requests/{requestId}/cancel")
    suspend fun cancelPrintRequest(
        @Path("requestId") requestId: Int,
    ): Response<ApiEnvelope<Any>>

    @GET("api/v1/clearance/me")
    suspend fun clearanceMe(): Response<ApiEnvelope<ClearanceRecordDto>>

    @POST("api/v1/clearance/lost-books/{transactionId}/report")
    suspend fun reportLostBook(
        @Path("transactionId") transactionId: Int,
    ): Response<ApiEnvelope<Any>>

    @GET("api/v1/attendance/pass")
    suspend fun attendancePass(): Response<ApiEnvelope<AttendancePassDto>>

    @GET("api/v1/attendance/pass.png")
    suspend fun attendancePassPng(): Response<ResponseBody>
}
