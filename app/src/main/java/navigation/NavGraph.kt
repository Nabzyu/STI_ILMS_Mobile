package com.example.stismartlib.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.auth.LoginScreen
import com.example.stismartlib.ui.auth.RegisterScreen
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.dashboard.DashboardScreen
import com.example.stismartlib.ui.dashboard.staff.StaffHomeScreen

@Composable
fun AppNavGraph(startDestination: String = Screen.Login.route) {

    val navController = rememberNavController()

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
