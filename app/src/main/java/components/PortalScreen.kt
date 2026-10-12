package com.example.stismartlib.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Applies status-bar and navigation-bar padding so content clears system chrome. */
fun Modifier.portalInsets(): Modifier =
    this
        .statusBarsPadding()
        .navigationBarsPadding()

/**
 * Thin screen chrome: brand background + system bar insets.
 * Content keeps its own layout (Column, LazyColumn, etc.).
 */
@Composable
fun PortalScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .portalInsets()
    ) {
        content()
    }
}
