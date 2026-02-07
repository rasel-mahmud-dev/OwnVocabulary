package com.rs.myvocabulary.utils

import android.content.Context
import android.content.SharedPreferences

object PlaybackPreferenceManager {
    private const val PREF_NAME = "video_playback_prefs"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun savePosition(context: Context, url: String, position: Long) {
        getPrefs(context).edit().putLong(url, position).apply()
    }

    fun getPosition(context: Context, url: String): Long {
        return getPrefs(context).getLong(url, 0L)
    }
}
