package com.example.stismartlib.network

class ApiException(
    message: String,
    val code: String = "REQUEST_FAILED",
    val fieldErrors: Map<String, String> = emptyMap(),
    val httpStatus: Int = 0,
) : Exception(message)
