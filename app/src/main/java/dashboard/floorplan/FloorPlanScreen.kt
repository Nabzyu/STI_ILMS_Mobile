package com.example.stismartlib.ui.dashboard.floorplan

import com.composables.icons.lucide.*

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.stismartlib.data.bookLocation
import com.example.stismartlib.data.detailLine
import com.example.stismartlib.data.floorPlanImage
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.network.dto.BookLocationDto
import com.example.stismartlib.network.dto.FloorPlanImageDto
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsLoadingBlock
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.feedback.userFacingMessage
import com.example.stismartlib.ui.format.formatDateMedium
import com.example.stismartlib.ui.components.Shape2Xl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

private const val MAX_ZOOM = 5f

/** Library floor plan image; with [titleId], also the shelf where that book is kept. */
@Composable
fun FloorPlanScreen(titleId: String? = null) {
    val repository = rememberLibraryRepository()
    var image by remember { mutableStateOf<FloorPlanImageDto?>(null) }
    var location by remember { mutableStateOf<BookLocationDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(titleId, reloadKey) {
        loading = true
        try {
            coroutineScope {
                val nextImage = async { repository.floorPlanImage() }
                val nextLocation = async { titleId?.let { repository.bookLocation(it) } }
                image = nextImage.await()
                location = nextLocation.await()
            }
            error = null
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            error = userFacingMessage(failure, "Unable to load the floor plan. Try again.")
        } finally {
            loading = false
        }
    }

    FloorPlanContent(
        showLocation = titleId != null,
        image = image,
        location = location,
        loading = loading,
        error = error,
        onRefresh = { if (!loading) reloadKey++ },
    )
}

@Composable
internal fun FloorPlanContent(
    showLocation: Boolean,
    image: FloorPlanImageDto?,
    location: BookLocationDto?,
    loading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        PageHeader(
            eyebrow = "Library guide",
            title = "Library floor plan",
            description = "View the library image and use the shelf label to find a book.",
            action = {
                PortalButton(
                    text = "Refresh",
                    onClick = onRefresh,
                    variant = PortalButtonVariant.Secondary,
                    enabled = !loading,
                    leadingIcon = Lucide.RefreshCw,
                )
            }
        )
        if (showLocation) {
            Spacer(modifier = Modifier.height(16.dp))
            BookLocationCard(location = location, loading = loading)
        }
        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            ElmsBanner(message = error)
        }
        Spacer(modifier = Modifier.height(16.dp))
        FloorPlanImageArea(image = image, loading = loading, failed = error != null, onRetry = onRefresh)
    }
}

@Composable
private fun BookLocationCard(location: BookLocationDto?, loading: Boolean) {
    SectionCard {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                Lucide.MapPin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = location?.title ?: "Book location",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                val shelf = when {
                    loading -> "Loading…"
                    else -> location?.shelfLabel?.takeIf { it.isNotBlank() } ?: "Location not recorded"
                }
                Text(
                    text = "Shelf: $shelf",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                )
                location?.detailLine()?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.FloorPlanImageArea(
    image: FloorPlanImageDto?,
    loading: Boolean,
    failed: Boolean,
    onRetry: () -> Unit,
) {
    val imageUrl = image?.imageUrl
    when {
        loading -> ElmsLoadingBlock()
        imageUrl != null -> {
            ZoomableFloorPlan(imageUrl = imageUrl, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = listOfNotNull(
                    "Pinch to zoom. Double-tap to reset.",
                    image.uploadedAt?.let { at ->
                        "Uploaded ${formatDateMedium(at.take(10))}" + image.uploadedBy?.let { " by $it" }.orEmpty()
                    },
                ).joinToString("\n"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        failed -> ElmsEmptyState(
            title = "Floor plan unavailable",
            description = "Unable to load the floor plan.",
            actionLabel = "Try again",
            onAction = onRetry,
        )
        else -> ElmsEmptyState(
            title = "No floor plan yet",
            description = "Floor plan image not available yet.",
        )
    }
}

@Composable
private fun ZoomableFloorPlan(imageUrl: String, modifier: Modifier = Modifier) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    // Keep the zoomed image covering the frame: pan at most half the extra width/height.
    fun clamp(value: Offset, atScale: Float): Offset {
        val maxX = size.width * (atScale - 1f) / 2f
        val maxY = size.height * (atScale - 1f) / 2f
        return Offset(value.x.coerceIn(-maxX, maxX), value.y.coerceIn(-maxY, maxY))
    }
    val transform = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, MAX_ZOOM)
        offset = clamp(offset + panChange, scale)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(Shape2Xl)
            .border(1.dp, MaterialTheme.colorScheme.outline, Shape2Xl)
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { scale = 1f; offset = Offset.Zero })
            }
            .transformable(transform)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Current library floor plan",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
    }
}
