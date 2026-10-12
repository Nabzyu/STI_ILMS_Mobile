package com.example.stismartlib.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.example.stismartlib.STIOrmocLibraryApp

val LocalLibraryRepository = staticCompositionLocalOf<LibraryRepository> {
    error("LibraryRepository not provided. Call ProvideLibraryRepository at the root of your composition.")
}

@Composable
fun ProvideLibraryRepository(content: @Composable () -> Unit) {
    val context = LocalContext.current.applicationContext
    val repository = remember {
        (context as STIOrmocLibraryApp).libraryRepository
    }
    CompositionLocalProvider(LocalLibraryRepository provides repository) {
        content()
    }
}

@Composable
fun rememberLibraryRepository(): LibraryRepository = LocalLibraryRepository.current
