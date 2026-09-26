package com.example.stismartlib

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.example.stismartlib.data.ProvideLibraryRepository
import com.example.stismartlib.navigation.AppNavGraph
import com.example.stismartlib.ui.theme.LocalThemeController
import com.example.stismartlib.ui.theme.STISmartLibTheme
import com.example.stismartlib.ui.theme.isAppDarkTheme
import com.example.stismartlib.ui.theme.rememberThemeController

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeController = rememberThemeController()

            CompositionLocalProvider(LocalThemeController provides themeController) {
                STISmartLibTheme(darkTheme = isAppDarkTheme()) {
                    ProvideLibraryRepository {
                        AppNavGraph()
                    }
                }
            }
        }
    }
}