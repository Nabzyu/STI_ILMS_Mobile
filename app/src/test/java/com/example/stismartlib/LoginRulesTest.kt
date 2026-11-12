package com.example.stismartlib

import com.example.stismartlib.ui.auth.isValidLoginCode
import com.example.stismartlib.ui.auth.loginSchoolIdError
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
        assertEquals("School ID is required.", loginSchoolIdError("   "))
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
}
