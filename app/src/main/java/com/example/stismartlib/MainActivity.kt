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
    private val repository get() = (application as STIOrmocLibraryApp).libraryRepository

    /** Touches and key presses keep the server session alive (10-minute idle limit). */
    override fun onUserInteraction() {
        super.onUserInteraction()
        repository.onUserInteraction()
    }

    override fun onStart() {
        super.onStart()
        // Returning after 10+ idle minutes: sign out locally instead of failing the next request.
        repository.checkIdleTimeout()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplashVisible }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
