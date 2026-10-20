package com.example.stismartlib.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stismartlib.ui.feedback.userFacingMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Load-more progress owned by the screen, not the footer row, so scrolling the footer
 * off-screen neither cancels the fetch nor forgets that it is running.
 */
@Stable
class LoadMoreState(private val scope: CoroutineScope) {
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    /** Runs [block] unless a load is already running; a failure becomes [error]. */
    fun load(block: suspend () -> Unit) {
        if (loading) return
        loading = true
        error = null
        scope.launch {
            try {
                block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                error = userFacingMessage(failure, "More results could not be loaded.")
            } finally {
                loading = false
            }
        }
    }
}

@Composable
fun rememberLoadMoreState(): LoadMoreState {
    val scope = rememberCoroutineScope()
    return remember(scope) { LoadMoreState(scope) }
}

/** Last row of a paged list: fetches the next page and shows its own error. */
@Composable
fun LoadMoreFooter(state: LoadMoreState, onLoadMore: suspend () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        state.error?.let {
            ElmsBanner(message = it)
            Spacer(modifier = Modifier.height(8.dp))
        }
        PortalButton(
            text = if (state.loading) "Loading…" else "Load more",
            onClick = { state.load(onLoadMore) },
            variant = PortalButtonVariant.Secondary,
            enabled = !state.loading,
            loading = state.loading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
