package com.example.stismartlib

import com.example.stismartlib.ui.auth.isValidLoginCode
import com.example.stismartlib.ui.auth.loginSchoolIdError
import com.example.stismartlib.ui.auth.newPasswordError
import com.example.stismartlib.ui.auth.normalizeSchoolId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginRulesTest {
    @Test
    fun acceptsServerSchoolIdFormats() {
        assertNull(loginSchoolIdError("02000123456"))
        assertNull(loginSchoolIdError("ADMIN-PORTAL-001"))
        assertNull(loginSchoolIdError("admin-portal-001"))
        assertNull(loginSchoolIdError("  fac.2024_01  "))
        assertNull(loginSchoolIdError("ABC"))
        assertNull(loginSchoolIdError("A" + "1".repeat(49)))
    }

    @Test
    fun rejectsInvalidSchoolIds() {
        assertEquals("Student ID or Employee ID is required.", loginSchoolIdError("   "))
        assertNotNull(loginSchoolIdError("AB"))
        assertNotNull(loginSchoolIdError("-ADMIN"))
        assertNotNull(loginSchoolIdError("ADMIN PORTAL"))
        assertNotNull(loginSchoolIdError("A" + "1".repeat(50)))
        assertNotNull(loginSchoolIdError("name@ormoc.sti.edu.ph"))
    }

    @Test
    fun normalizesLikeTheServer() {
        assertEquals("ADMIN-PORTAL-001", normalizeSchoolId("  admin-portal-001 "))
    }

    @Test
    fun loginCodeMustBeSixDigits() {
        assertTrue(isValidLoginCode("012345"))
        assertFalse(isValidLoginCode("12345"))
        assertFalse(isValidLoginCode("1234567"))
        assertFalse(isValidLoginCode("12a456"))
    }

    @Test
    fun newPasswordFollowsServerRule() {
        assertNull(newPasswordError("abcd1234", "abcd1234"))
        assertNull(newPasswordError("a".repeat(72), "a".repeat(72)))
        assertEquals("Use at least 8 characters.", newPasswordError("short", "short"))
        assertNotNull(newPasswordError("a".repeat(73), "a".repeat(73)))
        // 36 two-byte characters = 72 bytes is fine; 37 is 74 bytes.
        assertNull(newPasswordError("é".repeat(36), "é".repeat(36)))
        assertNotNull(newPasswordError("é".repeat(37), "é".repeat(37)))
        assertEquals("The passwords do not match.", newPasswordError("abcd1234", "abcd1235"))
    }
}
