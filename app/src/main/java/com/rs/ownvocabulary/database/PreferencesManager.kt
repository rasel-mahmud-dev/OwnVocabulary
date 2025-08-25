package com.rs.ownvocabulary.database

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit


const val THEME_KEY = "selected_theme_prefs"
object PreferencesManager {
    private const val PREFS_NAME = "markdown_editor_prefs"

    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private fun getPrefs(): SharedPreferences {
        return sharedPreferences ?: throw IllegalStateException("PreferencesManager not initialized. Call init() first.")
    }

    fun putString(key: String, value: String) {
        getPrefs().edit() { putString(key, value) }
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return getPrefs().getString(key, defaultValue) ?: defaultValue
    }

    fun putInt(key: String, value: Int) {
        getPrefs().edit() { putInt(key, value) }
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return getPrefs().getInt(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        getPrefs().edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return getPrefs().getBoolean(key, defaultValue)
    }

    fun putFloat(key: String, value: Float) {
        getPrefs().edit().putFloat(key, value).apply()
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return getPrefs().getFloat(key, defaultValue)
    }

    fun putLong(key: String, value: Long) {
        getPrefs().edit().putLong(key, value).apply()
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return getPrefs().getLong(key, defaultValue)
    }

    fun remove(key: String) {
        getPrefs().edit().remove(key).apply()
    }

    fun clear() {
        getPrefs().edit().clear().apply()
    }

    fun contains(key: String): Boolean {
        return getPrefs().contains(key)
    }
}

fun Context.initPreferences() {
    PreferencesManager.init(this)
}

