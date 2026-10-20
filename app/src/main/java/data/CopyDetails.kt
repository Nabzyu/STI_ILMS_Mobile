package com.example.stismartlib.data

import com.example.stismartlib.network.ApiClient
import com.example.stismartlib.network.dto.CatalogCopyDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Base64

private const val SvgDataUriPrefix = "data:image/svg+xml;base64,"

/** One physical copy (condition, accession, shelf, barcode image); errors reach the caller. */
suspend fun LibraryRepository.copyDetails(barcode: String): CatalogCopyDto = withContext(Dispatchers.IO) {
    ApiClient.unwrap(ApiClient.api.catalogCopy(barcode))
}

/** SVG bytes from the server's barcode data URI, or null for anything else or bad base64. */
fun svgBytesFromDataUri(uri: String?): ByteArray? {
    if (uri == null || !uri.startsWith(SvgDataUriPrefix)) return null
    return try {
        Base64.getDecoder().decode(uri.substring(SvgDataUriPrefix.length))
    } catch (_: IllegalArgumentException) {
        null
    }
}

fun displayValue(value: String?): String = value?.takeIf { it.isNotBlank() } ?: "Not recorded"
