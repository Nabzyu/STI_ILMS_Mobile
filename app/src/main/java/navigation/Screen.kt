package com.example.stismartlib.navigation

import com.example.stismartlib.ui.auth.UserType
import java.net.URLEncoder

sealed class Screen(val route: String) {

    object Login : Screen("login")

    object StaffHome : Screen("staff_home")

    object Register : Screen("student_register?verifySchoolId={verifySchoolId}") {
        fun create(verifySchoolId: String? = null): String =
            if (verifySchoolId == null) "student_register"
            else "student_register?verifySchoolId=${URLEncoder.encode(verifySchoolId, "UTF-8")}"
    }

    object ForgotPassword : Screen("forgot_password")

    object Dashboard : Screen("dashboard/{userType}") {
        fun create(userType: UserType): String = "dashboard/${userType.name}"
    }
}
