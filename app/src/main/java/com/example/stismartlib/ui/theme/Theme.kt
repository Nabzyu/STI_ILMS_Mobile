package com.example.stismartlib.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = StiBrandBlue,
    onPrimary = Color.White,
    primaryContainer = StiBrandBlueSoft,
    onPrimaryContainer = StiBrandBlue,
    secondary = StiYellow,
    onSecondary = StiBrandBlue,
    secondaryContainer = AccentYellow10,
    onSecondaryContainer = StiBrandBlue,
    tertiary = StiBrandBlue,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutline,
    error = ErrorRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = StiBrandBlue,
    onPrimary = Color.White,
    primaryContainer = DarkFocusFill,
    onPrimaryContainer = DarkOnSurface,
    secondary = StiYellow,
    onSecondary = StiBrandBlue,
    secondaryContainer = Color(0xFF3D3200),
    onSecondaryContainer = StiYellow,
    tertiary = StiBrandBlue,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceMuted,
    outline = DarkBorder,
    outlineVariant = Color(0x2EFFFFFF),
    error = Color(0xFFFF6B6B),
    onError = DarkBackground
)

@Composable
fun STIOrmocLibraryTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = (if (darkTheme) DarkBackground else LightBackground).toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
