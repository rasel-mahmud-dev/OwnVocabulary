package com.rs.myvocabulary.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.rs.myvocabulary.viewmodels.CurrentUser

object SyncManager {
    private lateinit var sharedPreferences: SharedPreferences

    private const val PREF_CATEGORY_LAST_SYNC_TIME = "category_last_sync_time"
    private const val PREF_TAG_LAST_SYNC_TIME = "tag_last_sync_time"
    private const val PREF_NOTE_CATEGORY_LAST_SYNC_TIME = "note_category_last_sync_time"
    private const val PREF_POST_TAG_LAST_SYNC_TIME = "post_tag_last_sync_time"
    private const val PREF_COMMENT_LAST_SYNC_TIME = "comment_last_sync_time"
    private const val PREF_POST_COMMENT_LAST_SYNC_TIME = "post_comment_last_sync_time"
    private const val PREF_LAST_SYNC_TIME = "last_sync_time"
    private const val PREF_LAST_SYNC_PAGE = "last_sync_page"
    private const val PREF_CATEGORY_LAST_ID = "category_last_id"
    private const val PREF_TAG_LAST_ID = "tag_last_id"
    private const val PREF_LAST_ID = "last_id"
    private const val DEFAULT_PAGE = 0

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
    }

    fun setAuth(auth: CurrentUser?) {
        sharedPreferences.edit {
            if (auth == null) {
                remove("auth")
                return@edit
            }
            putString("auth", Gson().toJson(auth))
            apply()
        }
    }

    fun setAuthToken(token: String?) {
        sharedPreferences.edit {
            putString("auth_token", token)
            apply()
        }
    }

    fun getAuth(): CurrentUser? {
        val authStr = sharedPreferences.getString("auth", "") ?: ""
        val a = Gson().fromJson(authStr, CurrentUser::class.java)
        return a
    }

    fun getAuthToken(): String {
        val authStr = sharedPreferences.getString("auth_token", "") ?: ""
        return authStr
    }

    fun setLastSyncTime(lastTime: Long) {
        sharedPreferences.edit { putLong(PREF_LAST_SYNC_TIME, lastTime) }
    }

    fun setCategoryLastSyncTime(lastTime: Long) {
        sharedPreferences.edit { putLong(PREF_CATEGORY_LAST_SYNC_TIME, lastTime) }
    }

    fun getNoteCategoryLastSyncTime(): Long =
            sharedPreferences.getLong(PREF_NOTE_CATEGORY_LAST_SYNC_TIME, 0L)
    fun setNoteCategoryLastSyncTime(lastTime: Long) =
            sharedPreferences.edit {
                putLong(PREF_NOTE_CATEGORY_LAST_SYNC_TIME, lastTime)
                apply()
            }

    fun getPostTagLastSyncTime(): Long = sharedPreferences.getLong(PREF_POST_TAG_LAST_SYNC_TIME, 0L)
    fun setPostTagLastSyncTime(lastTime: Long) =
            sharedPreferences.edit {
                putLong(PREF_POST_TAG_LAST_SYNC_TIME, lastTime)
                apply()
            }

    fun getTagLastSyncTime(): Long = sharedPreferences.getLong(PREF_TAG_LAST_SYNC_TIME, 0L)
    fun setTagLastSyncTime(lastTime: Long) =
            sharedPreferences.edit {
                putLong(PREF_TAG_LAST_SYNC_TIME, lastTime)
                apply()
            }

    fun getCommentLastSyncTime(): Long = sharedPreferences.getLong(PREF_COMMENT_LAST_SYNC_TIME, 0L)
    fun setCommentLastSyncTime(lastTime: Long) =
            sharedPreferences.edit {
                putLong(PREF_COMMENT_LAST_SYNC_TIME, lastTime)
                apply()
            }

    fun getPostCommentLastSyncTime(): Long =
            sharedPreferences.getLong(PREF_POST_COMMENT_LAST_SYNC_TIME, 0L)
    fun setPostCommentLastSyncTime(lastTime: Long) =
            sharedPreferences.edit {
                putLong(PREF_POST_COMMENT_LAST_SYNC_TIME, lastTime)
                apply()
            }

    fun updateLastId(id: String) {
        sharedPreferences.edit {
            putString(PREF_LAST_ID, id)
            apply()
        }
    }

    fun updateCategoryLastId(id: String) {
        sharedPreferences.edit {
            putString(PREF_CATEGORY_LAST_ID, id)
            apply()
        }
    }

    fun updateTagLastId(id: String) {
        sharedPreferences.edit {
            putString(PREF_TAG_LAST_ID, id)
            apply()
        }
    }

    fun getLastSyncTime(): Long {
        return sharedPreferences.getLong(PREF_LAST_SYNC_TIME, 0L)
    }

    fun getCategoryLastSyncTime(): Long {
        return sharedPreferences.getLong(PREF_CATEGORY_LAST_SYNC_TIME, 0L)
    }

    fun getLastId(): String {
        return sharedPreferences.getString(PREF_LAST_ID, "") ?: ""
    }

    fun getCategoryLastId(): String {
        return sharedPreferences.getString(PREF_CATEGORY_LAST_ID, "") ?: ""
    }

    fun getTagLastId(): String {
        return sharedPreferences.getString(PREF_TAG_LAST_ID, "") ?: ""
    }

    fun getLastSyncTime(key: String): Long {
        return sharedPreferences.getLong(key, 0L)
    }

    fun setLastSyncTime(key: String, lastTime: Long) {
        sharedPreferences.edit { putLong(key, lastTime) }
    }

    fun resetAll() {
        sharedPreferences.edit {
            clear()
            apply()
        }
    }
}
