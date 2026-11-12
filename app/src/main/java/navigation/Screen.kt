package com.example.stismartlib.navigation

import com.example.stismartlib.ui.auth.UserType
import java.net.URLEncoder

sealed class Screen(val route: String) {

    object Login : Screen("login")

    object StaffHome : Screen("staff_home")

    /** Student account activation (the website's roster sign-up). */
    object ActivateAccount : Screen("activate_account")

    object ForgotPassword : Screen("forgot_password")

    object Dashboard : Screen("dashboard/{userType}") {
        fun create(userType: UserType): String = "dashboard/${userType.name}"
    }
}
