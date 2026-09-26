package com.example.stismartlib.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    fun resolve(isSystemDark: Boolean): Boolean = when (this) {
        LIGHT -> false
        DARK -> true
        SYSTEM -> isSystemDark
    }

    fun label(): String = when (this) {
        LIGHT -> "Light"
        DARK -> "Dark"
        SYSTEM -> "System"
    }
}

class ThemeController(initialMode: ThemeMode = ThemeMode.SYSTEM) {
    var themeMode by mutableStateOf(initialMode)

    fun cycleThemeMode() {
        themeMode = when (themeMode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
        }
    }
}

val LocalThemeController = staticCompositionLocalOf<ThemeController> {
    error("ThemeController not provided")
}

@Composable
fun rememberThemeController(): ThemeController {
    return rememberSaveable(
        saver = Saver(
            save = { it.themeMode.name },
            restore = { ThemeController(ThemeMode.valueOf(it)) }
        )
    ) { ThemeController() }
}

@Composable
fun isAppDarkTheme(): Boolean {
    val controller = LocalThemeController.current
    return controller.themeMode.resolve(isSystemInDarkTheme())
}
