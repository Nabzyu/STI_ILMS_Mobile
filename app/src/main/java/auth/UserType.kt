package com.example.stismartlib.ui.auth

enum class UserType(val displayName: String) {
    STUDENT("Student"),
    FACULTY("Faculty"),
    /** Server role is Admin; UI label is Librarian. */
    ADMIN("Librarian");

    val isStaff: Boolean get() = this == ADMIN

    companion object {
        fun fromServerRole(role: String?): UserType {
            val normalized = role?.trim().orEmpty()
            return when {
                normalized.equals("Admin", ignoreCase = true) -> ADMIN
                normalized.equals("Librarian", ignoreCase = true) -> ADMIN
                normalized.equals("System Administrator", ignoreCase = true) -> ADMIN
                normalized.equals("Faculty", ignoreCase = true) -> FACULTY
                else -> STUDENT
            }
        }
    }
}
