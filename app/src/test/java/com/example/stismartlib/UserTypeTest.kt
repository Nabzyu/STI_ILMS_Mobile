package com.example.stismartlib

import com.example.stismartlib.ui.auth.UserType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun deskStaffAreRecognisedButNotSupported() {
        assertTrue(UserType.isDeskStaffRole("Staff"))
        assertTrue(UserType.isDeskStaffRole(" staff "))
        assertTrue(UserType.isDeskStaffRole("Library Staff"))
        assertFalse(UserType.isDeskStaffRole("Librarian"))
        assertFalse(UserType.isDeskStaffRole("Student"))
        assertFalse(UserType.isDeskStaffRole(null))
    }

    @Test
    fun fromServerRoleMapsUserRoles() {
        assertEquals(UserType.FACULTY, UserType.fromServerRole("Faculty"))
        assertEquals(UserType.STUDENT, UserType.fromServerRole("Student"))
        assertEquals(UserType.STUDENT, UserType.fromServerRole(null))
        assertFalse(UserType.FACULTY.isStaff)
        assertFalse(UserType.STUDENT.isStaff)
    }
}
