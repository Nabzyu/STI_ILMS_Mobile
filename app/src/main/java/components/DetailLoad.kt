package com.example.stismartlib.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stismartlib.ui.feedback.userFacingMessage

/** State of a detail screen's item: from the loaded list, or fetched by id when it isn't there. */
sealed interface DetailLoad<out T> {
    data object Loading : DetailLoad<Nothing>
    data object NotFound : DetailLoad<Nothing>
    data class Failed(val message: String) : DetailLoad<Nothing>
    data class Ready<T>(val item: T) : DetailLoad<T>
}

/**
 * Uses [cached] (the item from the loaded list) when present; otherwise calls [fetch] once per
 * [key], so a detail opened from search or a deep link works before its catalog page is loaded.
 */
@Composable
fun <T : Any> rememberDetailItem(key: String, cached: T?, fetch: suspend () -> T?): DetailLoad<T> {
    var fetched by remember(key) { mutableStateOf<DetailLoad<T>>(DetailLoad.Loading) }
    LaunchedEffect(key, cached == null) {
        if (cached != null) return@LaunchedEffect
        fetched = try {
            fetch()?.let { DetailLoad.Ready(it) } ?: DetailLoad.NotFound
        } catch (error: Exception) {
            DetailLoad.Failed(userFacingMessage(error, "Unable to load this record."))
        }
    }
    return if (cached != null) DetailLoad.Ready(cached) else fetched
}

/** Loading, not-found and error states for a detail screen whose item isn't ready. */
@Composable
fun DetailFallback(state: DetailLoad<*>, notFoundText: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (state) {
            DetailLoad.Loading -> ElmsLoadingBlock()
            is DetailLoad.Failed -> ElmsBanner(message = state.message, tone = ElmsBannerTone.Error)
            else -> Text(notFoundText, color = MaterialTheme.colorScheme.onSurface)
        }
        PortalButton(text = "Go back", onClick = onBack, variant = PortalButtonVariant.Secondary)
    }
}
