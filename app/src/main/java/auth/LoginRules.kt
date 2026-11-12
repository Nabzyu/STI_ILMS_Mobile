package com.example.stismartlib.ui.auth

/** Same rule as the server (`account-auth.validation.ts`), applied after upper-casing. */
private val SchoolIdPattern = Regex("^[A-Z0-9][A-Z0-9._-]{2,49}$")
private val LoginCodePattern = Regex("^\\d{6}$")

fun normalizeSchoolId(raw: String): String = raw.trim().uppercase()

/** Returns a field error for the sign-in school ID, or null when it is acceptable. */
fun loginSchoolIdError(raw: String): String? {
    val normalized = normalizeSchoolId(raw)
    return when {
        normalized.isEmpty() -> "Student ID or Employee ID is required."
        !SchoolIdPattern.matches(normalized) -> "Enter a valid STI Student ID or Employee ID."
        else -> null
    }
}

fun isValidLoginCode(code: String): Boolean = LoginCodePattern.matches(code)

/** Server rule for a new password: 8+ characters, at most 72 UTF-8 bytes, and a matching confirmation. */
fun newPasswordError(password: String, confirmation: String): String? = when {
    password.length < 8 -> "Use at least 8 characters."
    password.toByteArray(Charsets.UTF_8).size > 72 -> "Use a shorter password (at most 72 characters)."
    password != confirmation -> "The passwords do not match."
    else -> null
}
