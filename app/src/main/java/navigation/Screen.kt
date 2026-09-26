package com.example.stismartlib.navigation

import com.example.stismartlib.ui.auth.UserType

sealed class Screen(val route: String) {

    object Splash : Screen("splash")

    object Login : Screen("login")

    object Register : Screen("student_register")

    object FacultyRegister : Screen("faculty_register")

    object QrLogin : Screen("qr_login")

    object Dashboard : Screen("dashboard/{userType}") {
        fun create(userType: UserType): String = "dashboard/${userType.name}"
    }
}
