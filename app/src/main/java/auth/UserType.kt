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

        /**
         * Library desk staff (`Staff`) are not supported in the app yet; they use the website.
         * Kept separate so they are never treated as students. Support is parked on the
         * `archive/desk-staff-support` branch.
         */
        fun isDeskStaffRole(role: String?): Boolean {
            val normalized = role?.trim().orEmpty()
            return normalized.equals("Staff", ignoreCase = true) ||
                normalized.equals("Library Staff", ignoreCase = true)
        }

        /**
         * Librarian, Admin and desk Staff accounts. None of them can use the app yet: the staff side
         * of the app is still placeholders, so they use the website. Librarian sign-in is parked on
         * the `archive/librarian-sign-in` branch.
         */
        fun isStaffAccountRole(role: String?): Boolean = fromServerRole(role).isStaff || isDeskStaffRole(role)
    }
}
