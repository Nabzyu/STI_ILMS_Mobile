package com.example.stismartlib

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.stismartlib.data.ProvideLibraryRepository
import com.example.stismartlib.navigation.AppNavGraph
import com.example.stismartlib.navigation.Screen
import com.example.stismartlib.ui.theme.LocalThemeController
import com.example.stismartlib.ui.theme.STIOrmocLibraryTheme
import com.example.stismartlib.ui.theme.isAppDarkTheme
import com.example.stismartlib.ui.theme.rememberThemeController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var keepSplashVisible by mutableStateOf(true)
    private var startDestination by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplashVisible }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as STIOrmocLibraryApp).libraryRepository
        lifecycleScope.launch {
            val restored = repository.restoreSession()
            startDestination = when {
                restored && repository.session.userType.isStaff -> Screen.StaffHome.route
                restored -> Screen.Dashboard.create(repository.session.userType)
                else -> Screen.Login.route
            }
            keepSplashVisible = false
        }

        setContent {
            val themeController = rememberThemeController()
            val destination = startDestination

            CompositionLocalProvider(LocalThemeController provides themeController) {
                STIOrmocLibraryTheme(darkTheme = isAppDarkTheme()) {
                    ProvideLibraryRepository {
                        if (destination != null) {
                            AppNavGraph(startDestination = destination)
                        }
                    }
                }
            }
        }
    }
}
