package com.rs.myvocabulary.database

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kotlin.apply
import kotlin.jvm.java

data class UserProfile(val id: String, val email: String, val username: String? = null)

class SessionManager private constructor(context: Context) {
    private val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "learn_media_session"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER = "user_profile"
        private const val KEY_LAST_EMAIL = "last_email"
        private const val KEY_SERVER_URL = "server_url"

        @Volatile
        private var instance: SessionManager? = null

        fun init(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = SessionManager(context)
                    }
                }
            }
        }

        fun getInstance(): SessionManager {
            return instance ?: throw kotlin.IllegalStateException("SessionManager not initialized")
        }
    }

    fun saveSession(token: String, user: UserProfile) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER, gson.toJson(user))
            putString(KEY_LAST_EMAIL, user.email)
            apply()
        }
    }

    fun getLastEmail(): String? {
        return prefs.getString(KEY_LAST_EMAIL, null)
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun getUserProfile(): UserProfile? {
        val userJson = prefs.getString(KEY_USER, null)
        if (userJson == null) return UserProfile("local_user", "user@example.com", "Local User")
        return try {
            gson.fromJson(userJson, UserProfile::class.java)
        } catch (e: Exception) {
            UserProfile("local_user", "user@example.com", "Local User")
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return true
    }

    fun getServerUrl(): String? {
        return prefs.getString(KEY_SERVER_URL, "http://localhost:3000") // Default
    }

    fun saveServerUrl(url: String) {
        prefs.edit().putString(KEY_SERVER_URL, url).apply()
    }
}
