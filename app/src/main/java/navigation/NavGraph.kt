package com.example.stismartlib.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stismartlib.ui.auth.LoginScreen
import com.example.stismartlib.ui.auth.RegisterScreen
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.dashboard.DashboardScreen
import com.example.stismartlib.ui.dashboard.qr.QrLoginScreen
import com.example.stismartlib.ui.splash.SplashScreen

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController)
        }

        composable(Screen.QrLogin.route) {
            QrLoginScreen(
                onBack = { navController.popBackStack() },
                isAuthFlow = true,
                onAuthSuccess = {
                    navController.navigate(Screen.Dashboard.create(UserType.STUDENT)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }

        composable(Screen.FacultyRegister.route) {
            RegisterScreen(navController, isFaculty = true)
        }

        composable(
            route = Screen.Dashboard.route,
            arguments = listOf(
                navArgument("userType") {
                    type = NavType.StringType
                    defaultValue = UserType.STUDENT.name
                }
            )
        ) { backStackEntry ->
            val userTypeName = backStackEntry.arguments?.getString("userType") ?: UserType.STUDENT.name
            val userType = UserType.entries.find { it.name == userTypeName } ?: UserType.STUDENT
            DashboardScreen(
                userType = userType,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

    }

}
