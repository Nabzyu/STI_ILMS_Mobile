package com.example.stismartlib.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.stismartlib.network.SessionTokens
import com.example.stismartlib.ui.auth.UserType

data class StoredSession(
    val token: String,
    val accountId: Int,
    val schoolId: String,
    val fullName: String,
    val role: String,
)

class SessionStore(context: Context) : SessionTokens {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = createPrefs(appContext)
    private val appLockChoices: SharedPreferences =
        appContext.getSharedPreferences(PREFS_APP_LOCK, Context.MODE_PRIVATE)

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)?.takeIf { it.isNotBlank() }

    /** 30-day key from the server (Student/Faculty only). Stored only in the encrypted prefs. */
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)?.takeIf { it.isNotBlank() }

    override fun accessToken(): String? = getToken()

    override fun refreshToken(): String? = getRefreshToken()

    /** Replaces both tokens after `/auth/refresh`; no-op when signed out meanwhile. */
    override fun saveTokens(accessToken: String, refreshToken: String) {
        if (getToken() == null) return
        prefs.edit()
            .putString(KEY_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .commit()
    }

    /** Fingerprint unlock answer for a school ID: true/false, or null if never asked. Survives logout. */
    fun appLockChoice(schoolId: String): Boolean? {
        val key = schoolId.trim().uppercase()
        if (key.isBlank() || !appLockChoices.contains(appLockKey(key))) return null
        return appLockChoices.getBoolean(appLockKey(key), false)
    }

    fun setAppLockChoice(schoolId: String, enabled: Boolean) {
        val key = schoolId.trim().uppercase()
        if (key.isBlank()) return
        appLockChoices.edit().putBoolean(appLockKey(key), enabled).apply()
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
        refreshToken: String? = getRefreshToken(),
    ) {
        // commit() so the token is readable before the next API call.
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putInt(KEY_ACCOUNT_ID, accountId)
            .putString(KEY_SCHOOL_ID, schoolId)
            .putString(KEY_FULL_NAME, fullName?.takeIf { it.isNotBlank() } ?: schoolId)
            .putString(KEY_ROLE, role)
            .commit()
    }

    /** Replaces only the token (renewed by `/auth/activity`); no-op when signed out. */
    fun updateToken(token: String) {
        if (getToken() == null) return
        prefs.edit().putString(KEY_TOKEN, token).commit()
    }

    fun clear() {
        prefs.edit().clear().commit()
    }

    fun userType(): UserType = UserType.fromServerRole(getSession()?.role)

    private companion object {
        const val TAG = "SessionStore"
        const val KEY_TOKEN = "token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_ACCOUNT_ID = "account_id"
        const val KEY_SCHOOL_ID = "school_id"
        const val KEY_FULL_NAME = "full_name"
        const val KEY_ROLE = "role"
        const val PREFS_ENCRYPTED = "smartlib_session"
        const val PREFS_FALLBACK = "smartlib_session_fallback"
        const val PREFS_APP_LOCK = "smartlib_app_lock"

        fun appLockKey(schoolId: String): String = "lock_$schoolId"

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
