package com.example.stismartlib.ui.auth

enum class UserType(val displayName: String) {
    STUDENT("Student"),
    FACULTY("Faculty"),
    /** Server role is Admin; UI label is Librarian. */
    ADMIN("Librarian"),
    /** Server role Staff (library desk staff). Signs in through the staff portal. */
    STAFF("Staff");

    val isStaff: Boolean get() = this == ADMIN || this == STAFF

    companion object {
        fun fromServerRole(role: String?): UserType {
            val normalized = role?.trim().orEmpty()
            return when {
                normalized.equals("Admin", ignoreCase = true) -> ADMIN
                normalized.equals("Librarian", ignoreCase = true) -> ADMIN
                normalized.equals("System Administrator", ignoreCase = true) -> ADMIN
                normalized.equals("Staff", ignoreCase = true) -> STAFF
                normalized.equals("Library Staff", ignoreCase = true) -> STAFF
                normalized.equals("Faculty", ignoreCase = true) -> FACULTY
                else -> STUDENT
            }
        }
    }
}
