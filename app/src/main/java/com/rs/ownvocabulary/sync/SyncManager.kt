package com.rs.ownvocabulary.sync

import android.content.SharedPreferences
import android.content.Context
import androidx.core.content.edit

object SyncManager {
    private lateinit var sharedPreferences: SharedPreferences

    private const val PREF_CATEGORY_LAST_SYNC_TIME = "category_last_sync_time"
    private const val PREF_NOTE_CATEGORY_LAST_SYNC_TIME = "note_category_last_sync_time"
    private const val PREF_LAST_SYNC_TIME = "last_sync_time"
    private const val PREF_LAST_SYNC_PAGE = "last_sync_page"
    private const val PREF_CATEGORY_LAST_ID = "category_last_id"
    private const val PREF_LAST_ID = "last_id"
    private const val DEFAULT_PAGE = 0

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(
            "sync_prefs",
            Context.MODE_PRIVATE
        )
    }

    fun updateLastSyncTime(lastTime: Long){
        sharedPreferences.edit {
            putLong(PREF_LAST_SYNC_TIME, lastTime)
            apply()
        }
    }

    fun updateCategoryLastSyncTime(lastTime: Long){
        sharedPreferences.edit {
            putLong(PREF_CATEGORY_LAST_SYNC_TIME, lastTime)
            apply()
        }
    }

    fun getNoteCategoryLastSyncTime(): Long{
        return sharedPreferences.getLong(PREF_NOTE_CATEGORY_LAST_SYNC_TIME, 0L)
    }

    fun setNoteCategoryLastSyncTime(lastTime: Long){
        sharedPreferences.edit {
            putLong(PREF_NOTE_CATEGORY_LAST_SYNC_TIME, lastTime)
            apply()
        }
    }

    fun updateLastId(id: String){
        sharedPreferences.edit {
            putString(PREF_LAST_ID, id)
            apply()
        }
    }

    fun updateCategoryLastId(id: String){
        sharedPreferences.edit {
            putString(PREF_CATEGORY_LAST_ID, id)
            apply()
        }
    }

    fun getLastSyncTime():  Long {
        return sharedPreferences.getLong(PREF_LAST_SYNC_TIME, 0L)
    }

    fun getCategoryLastSyncTime():  Long {
        return sharedPreferences.getLong(PREF_CATEGORY_LAST_SYNC_TIME, 0L)
    }

    fun getLastId():  String {
        return sharedPreferences.getString(PREF_LAST_ID, "") ?: ""
    }

    fun getCategoryLastId():  String {
        return sharedPreferences.getString(PREF_CATEGORY_LAST_ID, "") ?: ""
    }

    fun resetAll() {
        sharedPreferences.edit {
            clear()
            apply()
        }
    }
}