package com.example.stismartlib

import com.example.stismartlib.ui.auth.UserType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserTypeTest {
    @Test
    fun fromServerRoleMapsAdminAliases() {
        assertEquals(UserType.ADMIN, UserType.fromServerRole("Admin"))
        assertEquals(UserType.ADMIN, UserType.fromServerRole("Librarian"))
        assertEquals(UserType.ADMIN, UserType.fromServerRole("System Administrator"))
        assertTrue(UserType.fromServerRole("Admin").isStaff)
    }

    @Test
    fun fromServerRoleMapsUserRoles() {
        assertEquals(UserType.FACULTY, UserType.fromServerRole("Faculty"))
        assertEquals(UserType.STUDENT, UserType.fromServerRole("Student"))
        assertEquals(UserType.STUDENT, UserType.fromServerRole(null))
    }
}
