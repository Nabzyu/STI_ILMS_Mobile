package com.example.stismartlib.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stismartlib.ui.feedback.userFacingMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Last row of a paged list: fetches the next page and shows its own error. */
@Composable
fun LoadMoreFooter(onLoadMore: suspend () -> Unit) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    Column(modifier = Modifier.fillMaxWidth()) {
        error?.let {
            ElmsBanner(message = it)
            Spacer(modifier = Modifier.height(8.dp))
        }
        PortalButton(
            text = if (loading) "Loading…" else "Load more",
            onClick = {
                if (loading) return@PortalButton
                loading = true
                error = null
                scope.launch {
                    try {
                        onLoadMore()
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (failure: Exception) {
                        error = userFacingMessage(failure, "More results could not be loaded.")
                    } finally {
                        loading = false
                    }
                }
            },
            variant = PortalButtonVariant.Secondary,
            enabled = !loading,
            loading = loading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
