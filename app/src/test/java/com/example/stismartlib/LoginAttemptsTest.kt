package com.example.stismartlib

import com.example.stismartlib.data.loginAttempts
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginAttemptsTest {
    @Test
    fun everyRoleIsTriedWithItsPortal() {
        assertEquals(
            listOf("Student" to "user", "Faculty" to "user", "Admin" to "staff"),
            loginAttempts(null),
        )
    }

    @Test
    fun rememberedRoleGoesFirstWithoutRepeating() {
        assertEquals(
            listOf("Admin" to "staff", "Student" to "user", "Faculty" to "user"),
            loginAttempts("Admin"),
        )
    }
}
