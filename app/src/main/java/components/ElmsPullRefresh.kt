package com.example.stismartlib.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.stismartlib.ui.feedback.userFacingMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElmsPullRefresh(
    onRefresh: suspend () -> Unit,
    onRefreshError: (String?) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            if (isRefreshing) return@PullToRefreshBox
            scope.launch {
                isRefreshing = true
                onRefreshError(null)
                runCatching { onRefresh() }
                    .onFailure { error ->
                        onRefreshError(
                            userFacingMessage(error, "Unable to refresh. Please try again.")
                        )
                    }
                isRefreshing = false
            }
        },
        modifier = modifier.fillMaxSize(),
        state = state,
        contentAlignment = Alignment.TopCenter,
    ) {
        content()
    }
}
