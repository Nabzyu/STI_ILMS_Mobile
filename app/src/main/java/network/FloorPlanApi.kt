package com.example.stismartlib.network

import com.example.stismartlib.network.dto.ApiEnvelope
import com.example.stismartlib.network.dto.BookLocationDto
import com.example.stismartlib.network.dto.FloorPlanImageDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/** Library floor plan (Student, Faculty, Librarian). */
interface FloorPlanApi {
    @GET("api/v1/floor-plan/image")
    suspend fun image(): Response<ApiEnvelope<FloorPlanImageDto>>

    @GET("api/v1/floor-plan/location")
    suspend fun location(@Query("titleId") titleId: Long): Response<ApiEnvelope<BookLocationDto>>
}
