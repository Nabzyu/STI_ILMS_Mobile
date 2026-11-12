package com.example.stismartlib.data

import com.example.stismartlib.network.ApiClient
import com.example.stismartlib.network.dto.BookLocationDto
import com.example.stismartlib.network.dto.FloorPlanImageDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The published floor plan image, or null when the library has not uploaded one. */
suspend fun LibraryRepository.floorPlanImage(): FloorPlanImageDto? = withContext(Dispatchers.IO) {
    ApiClient.unwrapOrNull(ApiClient.floorPlanApi.image())
}

/** Shelf location of a title (an available copy first), or null when none is recorded. */
suspend fun LibraryRepository.bookLocation(titleId: String): BookLocationDto? = withContext(Dispatchers.IO) {
    val id = titleId.toLongOrNull()?.takeIf { it > 0 } ?: return@withContext null
    ApiClient.unwrapOrNull(ApiClient.floorPlanApi.location(id))
}

/** "Accession A · Call number C", or null when there is no accession number. */
fun BookLocationDto.detailLine(): String? {
    val accessionNumber = accession?.takeIf { it.isNotBlank() } ?: return null
    val call = callNumber?.takeIf { it.isNotBlank() }?.let { " · Call number $it" }.orEmpty()
    return "Accession $accessionNumber$call"
}
