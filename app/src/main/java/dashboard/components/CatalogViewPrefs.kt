package com.example.stismartlib.ui.dashboard.components

import android.content.Context

class CatalogViewPrefs(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(catalogKey: String): CatalogViewMode =
        CatalogViewMode.fromStorage(prefs.getString(storageKey(catalogKey), null))

    fun set(catalogKey: String, mode: CatalogViewMode) {
        prefs.edit().putString(storageKey(catalogKey), mode.toStorage()).apply()
    }

    companion object {
        const val PREFS_NAME = "smartlib_catalog_view"
        const val KEY_BOOKS = "books"
        const val KEY_RESEARCH = "research"

        private fun storageKey(catalogKey: String) = "catalog_view_$catalogKey"
    }
}
