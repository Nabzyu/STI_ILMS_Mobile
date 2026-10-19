package com.example.stismartlib.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.stismartlib.ui.auth.UserType

data class StoredSession(
    val token: String,
    val accountId: Int,
    val schoolId: String,
    val fullName: String,
    val role: String,
)

class SessionStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = createPrefs(appContext)
    private val loginHints: SharedPreferences =
        appContext.getSharedPreferences(PREFS_LOGIN_HINTS, Context.MODE_PRIVATE)

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)?.takeIf { it.isNotBlank() }

    /**
     * Last successful `login_as` role for a school ID (`Student`, `Faculty`, or `Admin`).
     * Migrates older `user` / `staff` portal hints.
     */
    fun preferredLoginAs(schoolId: String): String? {
        val key = schoolId.trim().uppercase()
        if (key.isBlank()) return null
        return normalizeLoginAs(loginHints.getString(hintKey(key), null))
    }

    fun rememberLoginAs(schoolId: String, loginAs: String) {
        val key = schoolId.trim().uppercase()
        val role = normalizeLoginAs(loginAs) ?: return
        if (key.isBlank()) return
        loginHints.edit().putString(hintKey(key), role).apply()
    }

    fun getSession(): StoredSession? {
        val token = getToken() ?: return null
        val accountId = prefs.getInt(KEY_ACCOUNT_ID, -1)
        val schoolId = prefs.getString(KEY_SCHOOL_ID, null).orEmpty()
        val fullName = prefs.getString(KEY_FULL_NAME, null).orEmpty()
        val role = prefs.getString(KEY_ROLE, null).orEmpty()
        if (accountId < 0 || schoolId.isBlank() || role.isBlank()) return null
        return StoredSession(token, accountId, schoolId, fullName.ifBlank { schoolId }, role)
    }

    fun save(
        token: String,
        accountId: Int,
        schoolId: String,
        fullName: String?,
        role: String,
    ) {
        // commit() so the token is readable before the next API call.
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_ACCOUNT_ID, accountId)
            .putString(KEY_SCHOOL_ID, schoolId)
            .putString(KEY_FULL_NAME, fullName?.takeIf { it.isNotBlank() } ?: schoolId)
            .putString(KEY_ROLE, role)
            .commit()
    }

    /** Swap in a renewed token; the rest of the stored session stays as it is. */
    fun updateToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).commit()
    }

    fun clear() {
        prefs.edit().clear().commit()
    }

    fun userType(): UserType = UserType.fromServerRole(getSession()?.role)

    private companion object {
        const val TAG = "SessionStore"
        const val KEY_TOKEN = "token"
        const val KEY_ACCOUNT_ID = "account_id"
        const val KEY_SCHOOL_ID = "school_id"
        const val KEY_FULL_NAME = "full_name"
        const val KEY_ROLE = "role"
        const val PREFS_ENCRYPTED = "smartlib_session"
        const val PREFS_FALLBACK = "smartlib_session_fallback"
        const val PREFS_LOGIN_HINTS = "smartlib_login_hints"
        const val LOGIN_AS_STUDENT = "Student"
        const val LOGIN_AS_FACULTY = "Faculty"
        const val LOGIN_AS_ADMIN = "Admin"

        fun hintKey(schoolId: String): String = "portal_$schoolId"

        fun normalizeLoginAs(raw: String?): String? {
            val value = raw?.trim().orEmpty()
            if (value.isEmpty()) return null
            return when {
                value.equals("Student", ignoreCase = true) -> LOGIN_AS_STUDENT
                value.equals("Faculty", ignoreCase = true) -> LOGIN_AS_FACULTY
                value.equals("Admin", ignoreCase = true) -> LOGIN_AS_ADMIN
                value.equals("Librarian", ignoreCase = true) -> LOGIN_AS_ADMIN
                value.equals("user", ignoreCase = true) -> LOGIN_AS_STUDENT
                value.equals("staff", ignoreCase = true) -> LOGIN_AS_ADMIN
                else -> null
            }
        }

        fun createPrefs(context: Context): SharedPreferences {
            return try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                EncryptedSharedPreferences.create(
                    context,
                    PREFS_ENCRYPTED,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                )
            } catch (error: Exception) {
                Log.w(TAG, "Encrypted session storage unavailable; using fallback prefs.", error)
                context.getSharedPreferences(PREFS_FALLBACK, Context.MODE_PRIVATE)
            }
        }
    }
}
