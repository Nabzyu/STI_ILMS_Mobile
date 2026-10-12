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
import com.example.stismartlib.network.dto.MeUser
import com.example.stismartlib.network.dto.NotificationListDto
import com.example.stismartlib.network.dto.PrintRequestDto
import com.example.stismartlib.network.dto.RegisterData
import com.example.stismartlib.network.dto.RegisterRequest
import com.example.stismartlib.network.dto.ResearchCatalogItemDto
import com.example.stismartlib.network.dto.ReservationItemDto
import com.example.stismartlib.network.dto.ReserveResultDto
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
