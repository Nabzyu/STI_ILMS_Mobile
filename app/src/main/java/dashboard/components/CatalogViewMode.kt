package com.example.stismartlib.ui.dashboard.components

enum class CatalogViewMode {
    LIST,
    GRID_2;

    fun toStorage(): String = when (this) {
        LIST -> "list"
        GRID_2 -> "grid_2"
    }

    companion object {
        fun fromStorage(raw: String?): CatalogViewMode = when (raw) {
            "grid_2" -> GRID_2
            else -> LIST
        }
    }
}
