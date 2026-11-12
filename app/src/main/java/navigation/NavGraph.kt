package com.example.stismartlib.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.auth.ForgotPasswordScreen
import com.example.stismartlib.ui.auth.LoginScreen
import com.example.stismartlib.ui.auth.RegisterScreen
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.dashboard.DashboardScreen
import com.example.stismartlib.ui.dashboard.staff.StaffHomeScreen

@Composable
fun AppNavGraph(startDestination: String = Screen.Login.route) {

    val navController = rememberNavController()
    val sessionRepository = rememberLibraryRepository()
    val isAuthenticated = sessionRepository.session.isAuthenticated

    // The session can end outside a screen's control (server code, 10-minute idle limit).
    // Send the user back to sign-in; the login screen shows the reason.
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) return@LaunchedEffect
        val route = navController.currentDestination?.route
        if (route != null && route != Screen.Login.route && route != Screen.Register.route &&
            route != Screen.ForgotPassword.route
        ) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Screen.Login.route) {
            LoginScreen(navController)
        }

        composable(Screen.StaffHome.route) {
            val repository = rememberLibraryRepository()
            StaffHomeScreen(
                onLogout = {
                    repository.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }

        composable(
            route = Screen.ForgotPassword.route,
            arguments = listOf(
                navArgument("staff") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            ForgotPasswordScreen(
                navController = navController,
                staff = backStackEntry.arguments?.getBoolean("staff") ?: false,
            )
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
            val repository = rememberLibraryRepository()
            DashboardScreen(
                userType = userType,
                onLogout = {
                    repository.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

    }

}
