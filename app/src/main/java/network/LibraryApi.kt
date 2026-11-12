package com.example.stismartlib.network

import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.network.dto.AttendancePassDto
import com.example.stismartlib.network.dto.BookCatalogItemDto
import com.example.stismartlib.network.dto.BorrowRequestReceiptDto
import com.example.stismartlib.network.dto.BorrowSubmitRequest
import com.example.stismartlib.network.dto.BorrowingHistoryDataDto
import com.example.stismartlib.network.dto.FineListDto
import com.example.stismartlib.network.dto.LoginData
import com.example.stismartlib.network.dto.LoginRequest
import com.example.stismartlib.network.dto.LoginResendData
import com.example.stismartlib.network.dto.LoginResendRequest
import com.example.stismartlib.network.dto.LoginVerifyRequest
import com.example.stismartlib.network.dto.LogoutData
import com.example.stismartlib.network.dto.RefreshTokenRequest
import com.example.stismartlib.network.dto.MeUser
import com.example.stismartlib.network.dto.NotificationListDto
import com.example.stismartlib.network.dto.PasswordRecoveryChallengeData
import com.example.stismartlib.network.dto.PasswordRecoveryRequest
import com.example.stismartlib.network.dto.PasswordRecoveryTokenData
import com.example.stismartlib.network.dto.PasswordRecoveryVerifyRequest
import com.example.stismartlib.network.dto.PasswordResetData
import com.example.stismartlib.network.dto.PasswordResetRequest
import com.example.stismartlib.network.dto.PrintRequestDto
import com.example.stismartlib.network.dto.RegisterData
import com.example.stismartlib.network.dto.RegisterRequest
import com.example.stismartlib.network.dto.ResearchCatalogItemDto
import com.example.stismartlib.network.dto.ReservationItemDto
import com.example.stismartlib.network.dto.ReserveResultDto
import com.example.stismartlib.network.dto.SessionActivityData
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
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

    /** Ends this phone's session on the server (refresh key in the body; no bearer token needed). */
    @POST("api/v1/auth/logout")
    suspend fun logout(@Body body: RefreshTokenRequest): Response<ApiEnvelope<LogoutData>>

    /** Keeps the server session alive (10-minute idle limit) and returns a renewed token. */
    @POST("api/v1/auth/activity")
    suspend fun sessionActivity(@Body body: Map<String, String> = emptyMap()): Response<ApiEnvelope<SessionActivityData>>

    @POST("api/v1/auth/password-recovery/request")
    suspend fun passwordRecoveryRequest(@Body body: PasswordRecoveryRequest): Response<ApiEnvelope<PasswordRecoveryChallengeData>>

    @POST("api/v1/auth/password-recovery/verify")
    suspend fun passwordRecoveryVerify(@Body body: PasswordRecoveryVerifyRequest): Response<ApiEnvelope<PasswordRecoveryTokenData>>

    @POST("api/v1/auth/password-recovery/reset")
    suspend fun passwordReset(@Body body: PasswordResetRequest): Response<ApiEnvelope<PasswordResetData>>

    @POST("api/v1/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<ApiEnvelope<RegisterData>>

    @GET("api/v1/auth/me")
    suspend fun me(): Response<ApiEnvelope<MeUser>>

    @GET("api/v1/catalog/books")
    suspend fun books(
        @Query("q") query: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): Response<ApiEnvelope<List<BookCatalogItemDto>>>

    @GET("api/v1/catalog/books/{titleId}")
    suspend fun bookOverview(@Path("titleId") titleId: Int): Response<ApiEnvelope<BookCatalogItemDto>>

    @POST("api/v1/catalog/books/{titleId}/reservations")
    suspend fun reserveBook(@Path("titleId") titleId: Int): Response<ApiEnvelope<ReserveResultDto>>

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

    @GET("api/v1/notifications")
    suspend fun notifications(@Query("limit") limit: Int = 100): Response<ApiEnvelope<NotificationListDto>>

    @GET("api/v1/printing/requests")
    suspend fun printRequests(): Response<ApiEnvelope<List<PrintRequestDto>>>

    @GET("api/v1/attendance/pass")
    suspend fun attendancePass(): Response<ApiEnvelope<AttendancePassDto>>

    @GET("api/v1/attendance/pass.png")
    suspend fun attendancePassPng(): Response<ResponseBody>
}
