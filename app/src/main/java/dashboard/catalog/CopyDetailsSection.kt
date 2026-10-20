package com.example.stismartlib.ui.dashboard.catalog

import com.composables.icons.lucide.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.stismartlib.data.copyDetails
import com.example.stismartlib.data.displayValue
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.data.svgBytesFromDataUri
import com.example.stismartlib.network.dto.CatalogCopyDto
import com.example.stismartlib.ui.components.ElmsLoadingBlock
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.ShapeXl
import kotlinx.coroutines.CancellationException

/** Physical copy facts and barcode for [barcode] (web: book detail drawer). */
@Composable
fun CopyDetailsSection(barcode: String) {
    val repository = rememberLibraryRepository()
    var copy by remember(barcode) { mutableStateOf<CatalogCopyDto?>(null) }
    var loading by remember(barcode) { mutableStateOf(true) }
    var failed by remember(barcode) { mutableStateOf(false) }
    LaunchedEffect(barcode) {
        try {
            copy = repository.copyDetails(barcode)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            failed = true // unknown barcode (404) or server down: the rest of book detail still works
        } finally {
            loading = false
        }
    }
    CopyDetailsContent(copy = copy, loading = loading, failed = failed)
}

@Composable
internal fun CopyDetailsContent(copy: CatalogCopyDto?, loading: Boolean, failed: Boolean) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Lucide.ScanBarcode, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Barcode", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(10.dp))
        when {
            loading -> ElmsLoadingBlock()
            failed || copy == null -> Text(
                "Copy details are unavailable right now.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            else -> CopyFacts(copy)
        }
    }
}

@Composable
private fun CopyFacts(copy: CatalogCopyDto) {
    Column {
        CopyFact("Book location", displayValue(copy.shelfLocation))
        CopyFact("Current condition", displayValue(copy.conditionStatus))
        CopyFact("Accession", displayValue(copy.accessionNumber))
        val svg = svgBytesFromDataUri(copy.barcodeImageData)
        if (svg != null) {
            Spacer(modifier = Modifier.height(10.dp))
            // Barcodes need a light background to scan, so this stays white in dark mode too.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeXl)
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(svg)
                        .decoderFactory(SvgDecoder.Factory())
                        .build(),
                    contentDescription = "Barcode ${copy.barcode.orEmpty()}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                )
            }
        }
        copy.barcode?.takeIf { it.isNotBlank() }?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                it,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CopyFact(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
    }
}
