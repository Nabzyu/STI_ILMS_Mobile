package com.example.stismartlib.network.dto

/** `GET floor-plan/image`; the server sends `data: null` when no image is published. */
data class FloorPlanImageDto(
    val imageUrl: String? = null,
    val originalName: String? = null,
    val uploadedAt: String? = null,
    val uploadedBy: String? = null,
)

/** `GET floor-plan/location`; `data: null` when the title has no active copy. */
data class BookLocationDto(
    val title: String? = null,
    val shelfLabel: String? = null,
    val accession: String? = null,
    val callNumber: String? = null,
)
