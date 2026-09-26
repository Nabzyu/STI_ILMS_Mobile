package com.example.stismartlib.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import android.content.Context

val LocalLibraryRepository = staticCompositionLocalOf<LibraryRepository> {
    error("LibraryRepository not provided. Call ProvideLibraryRepository at the root of your composition.")
}

@Composable
fun ProvideLibraryRepository(content: @Composable () -> Unit) {
    val repository = remember { LibraryRepository() }
    androidx.compose.runtime.CompositionLocalProvider(LocalLibraryRepository provides repository) {
        content()
    }
}

@Composable
fun rememberLibraryRepository(): LibraryRepository = LocalLibraryRepository.current