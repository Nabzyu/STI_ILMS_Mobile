package com.example.stismartlib.ui.auth

/** Same rule as the server (`account-auth.validation.ts`), applied after upper-casing. */
private val SchoolIdPattern = Regex("^[A-Z0-9][A-Z0-9._-]{2,49}$")
private val LoginCodePattern = Regex("^\\d{6}$")

fun normalizeSchoolId(raw: String): String = raw.trim().uppercase()

/** Returns a field error for the sign-in school ID, or null when it is acceptable. */
fun loginSchoolIdError(raw: String): String? {
    val normalized = normalizeSchoolId(raw)
    return when {
        normalized.isEmpty() -> "School ID is required."
        !SchoolIdPattern.matches(normalized) -> "Enter a valid STI school ID."
        else -> null
    }
}

fun isValidLoginCode(code: String): Boolean = LoginCodePattern.matches(code)
