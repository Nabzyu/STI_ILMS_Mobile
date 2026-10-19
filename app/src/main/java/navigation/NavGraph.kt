package com.example.stismartlib.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.network.ApiClient
import com.example.stismartlib.ui.auth.ForgotPasswordScreen
import com.example.stismartlib.ui.auth.LoginScreen
import com.example.stismartlib.ui.auth.RegisterScreen
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.dashboard.DashboardScreen
import com.example.stismartlib.ui.dashboard.staff.StaffHomeScreen

@Composable
fun AppNavGraph(startDestination: String = Screen.Login.route) {

    val navController = rememberNavController()
    val repository = rememberLibraryRepository()
    val sessionEnded by ApiClient.sessionEnded.collectAsState()

    LaunchedEffect(sessionEnded) {
        if (!sessionEnded) return@LaunchedEffect
        repository.logout()
        if (navController.currentDestination?.route != Screen.Login.route) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
        ApiClient.acknowledgeSessionEnded()
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

        composable(
            route = Screen.Register.route,
            arguments = listOf(
                navArgument("verifySchoolId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            RegisterScreen(
                navController,
                initialVerifySchoolId = backStackEntry.arguments?.getString("verifySchoolId"),
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController)
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
