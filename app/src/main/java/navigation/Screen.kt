package com.example.stismartlib.navigation

import com.example.stismartlib.ui.auth.UserType

sealed class Screen(val route: String) {

    object Login : Screen("login")

    object StaffHome : Screen("staff_home")

    object Register : Screen("student_register")

    object ForgotPassword : Screen("forgot_password?staff={staff}") {
        fun create(staff: Boolean): String = "forgot_password?staff=$staff"
    }

    object Dashboard : Screen("dashboard/{userType}") {
        fun create(userType: UserType): String = "dashboard/${userType.name}"
    }
}
