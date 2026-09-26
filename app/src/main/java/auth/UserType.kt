package com.example.stismartlib.ui.auth

enum class UserType(val displayName: String) {
    STUDENT("Student"),
    FACULTY("Faculty");

    companion object {
        val allDisplayNames: List<String> = entries.map { it.displayName }

        fun fromDisplayName(name: String): UserType =
            entries.find { it.displayName == name } ?: STUDENT
    }
}
