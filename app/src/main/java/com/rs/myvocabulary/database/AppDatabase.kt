package com.rs.myvocabulary.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.forEach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class CommentAttachment(val url: String, val type: String)

data class Comment(
        val _id: String,
        val username: String,
        val text: String,
        val audioUrl: String? = null,
        val mediaUrl: String? = null,
        val mediaType: String? = null,
        val attachments: List<CommentAttachment>? = null,
        val parentId: String? = null,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis(),
        val syncStatus: SyncStatus = SyncStatus.PENDING,
        val isDeleted: Boolean = false,
)

data class PostComment(
        val id: Int = 0,
        val uid: String,
        val postId: String,
        val userId: String?,
        val text: String,
        val audioUrl: String? = null,
        val mediaUrl: String? = null,
        val mediaType: String? = null,
        val attachments: List<CommentAttachment>? = null,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis(),
        val syncStatus: SyncStatus = SyncStatus.PENDING,
        val isDeleted: Boolean = false
)

data class Tag(
        val id: Int = 0,
        val uid: String,
        val name: String,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis(),
        val syncStatus: SyncStatus = SyncStatus.PENDING,
        val isDeleted: Boolean = false,
)

data class Word(
        val id: Long = 0,
        val uid: String = UUID.randomUUID().toString(),
        val updateUid: String = "",
        val word: String,
        val userId: String?,
        val type: String = "word",
        val shortMeaning: String = "",
        val details: String = "",
        val cover: String = "",
        val isFavorite: Boolean = false,
        val isDeleted: Boolean = false,
        val viewCount: Int = 0,
        val lastVisited: Long = 0L,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis(),
        val syncStatus: SyncStatus = SyncStatus.PENDING,
        val retryCount: Int = 0,
        val lastSyncAttempt: Long? = null,
        var categories: List<Label>? = null,
        val tags: List<Tag>? = null,
        val attachments: List<CommentAttachment>? = null,
        val comments: List<Comment>? = null
)

data class Label(
        val id: Int = 0,
        val uid: String = System.currentTimeMillis().toString(),
        val name: String,
        val color: String,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis(),
        val syncStatus: SyncStatus = SyncStatus.PENDING,
        val retryCount: Int = 0,
        val lastSyncAttempt: Long? = null,
        val children: List<Label> = emptyList(),
        var associatedNotes: List<Word> = emptyList(),
        val associatedNoteCount: Int? = 0,
        val parentId: String? = null,
        val isDeleted: Boolean = false
)

data class NoteCategory(
        val id: Int = 0,
        val uid: String = UUID.randomUUID().toString(),
        val categoryUid: String,
        val itemUid: String,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis(),
        val syncStatus: SyncStatus = SyncStatus.PENDING,
        val isDeleted: Boolean = false
)

data class PostTag(
        val uid: String,
        val postId: String,
        val tagId: String,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis(),
        val syncStatus: SyncStatus = SyncStatus.PENDING
)

data class WordPartial(
        val id: Long = 0,
        val uid: String,
        val userId: String? = null,
        val word: String? = null,
        val type: String? = null,
        val shortMeaning: String? = null,
        val details: String? = null,
        val isFavorite: Boolean? = null,
        val viewCount: Int? = null,
        val lastVisited: Long? = null,
        val syncStatus: SyncStatus? = null,
        val retryCount: Int? = null,
        val lastSyncAttempt: Long? = null,
        val createdAt: Long? = null,
        val updatedAt: Long? = null,
        val isDeleted: Boolean? = null
)

enum class SyncStatus {
    PENDING,
    IN_PROGRESS,
    SYNCED,
    FAILED,
    DELETED
}

enum class SortOrder {
    CreatedAtAsc,
    CreatedAtDesc,
    UpdatedAtAsc,
    UpdatedAtDesc,
    WordAsc,
    WordDesc
}

class WordDatabase private constructor(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        @Volatile private var INSTANCE: WordDatabase? = null
        private const val DATABASE_NAME = "words.db"
        private const val DATABASE_VERSION = 4

        // Table and column names
        private const val TABLE_NAME = "words"
        private const val TABLE_WORDS = "words"
        private const val COLUMN_ID = "id"

        private const val COLUMN_UID = "uid"
        private const val COLUMN_UPDATE_UID = "update_uid"
        private const val COLUMN_WORD = "word"
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_SHORT_MEANING = "short_meaning"
        private const val COLUMN_COVER = "cover"
        private const val COLUMN_IS_PINNED = "is_pinned"
        private const val COLUMN_DETAILS = "details"
        private const val COLUMN_IS_FAVORITE = "is_favorite"
        private const val COLUMN_PROFICIENCY_LEVEL = "proficiency_level"
        private const val COLUMN_VIEW_COUNT = "view_count"
        private const val COLUMN_LAST_VIEWED = "last_viewed"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_UPDATED_AT = "updated_at"
        private const val COLUMN_IS_DELETED = "is_deleted"

        private const val COLUMN_SYNC_STATUS = "sync_status"
        private const val COLUMN_RETRY_COUNT = "retry_count"
        private const val COLUMN_LAST_SYNC_ATTEMPT = "last_sync_attempt"

        const val TABLE_NOTE_CATEGORY = "note_category"
        const val TABLE_CATEGORIES = "category"
        const val COLUMN_NAME = "name"
        const val COLUMN_COLOR = "color"
        const val COLUMN_CATEGORY_UID = "category_uid"
        const val COLUMN_CATEGORY_PARENT_UID = "parent_uid"
        const val COLUMN_ITEM_UID = "item_uid"

        const val COLUMN_ATTACHMENTS = "attachments" // New attachments column for words/posts

        const val TABLE_POST_COMMENTS = "post_comments"
        const val COLUMN_PC_ID = "id"
        const val COLUMN_PC_UID = "uid"
        const val COLUMN_PC_POST_ID = "post_id"
        const val COLUMN_PC_USER_ID = "user_id"
        const val COLUMN_PC_TEXT = "text"
        const val COLUMN_PC_AUDIO_URL = "audio_url"
        const val COLUMN_PC_MEDIA_URL = "media_url"
        const val COLUMN_PC_MEDIA_TYPE = "media_type"
        const val COLUMN_PC_ATTACHMENTS = "attachments"
        const val COLUMN_PC_CREATED_AT = "created_at"
        const val COLUMN_PC_UPDATED_AT = "updated_at"
        const val COLUMN_PC_SYNC_STATUS = "sync_status"
        const val COLUMN_PC_IS_DELETED = "is_deleted"

        private const val TABLE_COMMENTS = "comments"
        private const val COLUMN_COMMENT_ID = "id"
        private const val COLUMN_COMMENT_REMOTE_ID = "remote_id"
        private const val COLUMN_COMMENT_USER_ID = "user_id"
        private const val COLUMN_COMMENT_USERNAME = "username"
        private const val COLUMN_COMMENT_POST_ID = "post_id"
        private const val COLUMN_COMMENT_PARENT_ID = "parent_id"
        private const val COLUMN_COMMENT_TEXT = "text"
        private const val COLUMN_COMMENT_AUDIO_URL = "audio_url"
        private const val COLUMN_COMMENT_MEDIA_URL = "media_url"
        private const val COLUMN_COMMENT_MEDIA_TYPE = "media_type"
        private const val COLUMN_COMMENT_ATTACHMENTS = "attachments"
        private const val COLUMN_COMMENT_CREATED_AT = "created_at"

        const val TABLE_TAGS = "tags"
        const val COLUMN_TAG_ID = "id"
        const val COLUMN_TAG_UID = "uid"
        const val COLUMN_TAG_NAME = "name"
        const val COLUMN_TAG_SYNC_STATUS = "sync_status"
        const val COLUMN_TAG_IS_DELETED = "is_deleted"
        const val COLUMN_TAG_SYNCED_AT = "synced_at"

        const val TABLE_POST_TAGS = "post_tags"
        const val COLUMN_PT_UID = "uid"
        const val COLUMN_PT_POST_ID = "post_id"
        const val COLUMN_PT_TAG_ID = "tag_id"
        const val COLUMN_PT_SYNC_STATUS = "sync_status"

        fun getInstance(context: Context): WordDatabase {
            return INSTANCE
                    ?: synchronized(this) {
                        INSTANCE ?: WordDatabase(context.applicationContext).also { INSTANCE = it }
                    }
        }
    }

    private val executor = Executors.newFixedThreadPool(4)

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS $TABLE_WORDS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_UID TEXT UNIQUE NOT NULL,
                $COLUMN_UPDATE_UID TEXT,
                $COLUMN_WORD TEXT NOT NULL UNIQUE COLLATE NOCASE,
                $COLUMN_USER_ID TEXT,
                $COLUMN_TYPE TEXT DEFAULT 'word',
                $COLUMN_SHORT_MEANING TEXT DEFAULT '',
                ${COLUMN_COVER} TEXT,
                ${COLUMN_IS_PINNED} INTEGER DEFAULT 0,
                $COLUMN_DETAILS TEXT DEFAULT '',
                $COLUMN_ATTACHMENTS TEXT,
                $COLUMN_IS_FAVORITE INTEGER DEFAULT 0,
                $COLUMN_VIEW_COUNT INTEGER DEFAULT 0,
                $COLUMN_IS_DELETED INTEGER DEFAULT 0,
                $COLUMN_LAST_VIEWED INTEGER DEFAULT 0,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_UPDATED_AT INTEGER NOT NULL,
                $COLUMN_SYNC_STATUS TEXT DEFAULT 'PENDING',
                $COLUMN_RETRY_COUNT INTEGER DEFAULT 0,
                $COLUMN_LAST_SYNC_ATTEMPT INTEGER
            )
        """.trimIndent()
        )

        db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS ${TABLE_COMMENTS} (
                ${COLUMN_COMMENT_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${COLUMN_COMMENT_REMOTE_ID} TEXT UNIQUE,
                ${COLUMN_COMMENT_USER_ID} TEXT,
                ${COLUMN_COMMENT_USERNAME} TEXT,
                ${COLUMN_COMMENT_POST_ID} TEXT,
                ${COLUMN_COMMENT_PARENT_ID} TEXT,
                ${COLUMN_COMMENT_TEXT} TEXT,
                ${COLUMN_COMMENT_AUDIO_URL} TEXT,
                ${COLUMN_COMMENT_MEDIA_URL} TEXT,
                ${COLUMN_COMMENT_MEDIA_TYPE} TEXT,
                ${COLUMN_COMMENT_ATTACHMENTS} TEXT,
                ${COLUMN_COMMENT_CREATED_AT} INTEGER,
                $COLUMN_UPDATED_AT INTEGER,
                $COLUMN_SYNC_STATUS TEXT DEFAULT 'PENDING',
                $COLUMN_IS_DELETED INTEGER DEFAULT 0,
                FOREIGN KEY(${COLUMN_COMMENT_POST_ID}) REFERENCES $TABLE_WORDS($COLUMN_UID)
            )
        """.trimIndent()
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_word ON $TABLE_WORDS ($COLUMN_WORD)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_uid ON $TABLE_WORDS ($COLUMN_UID)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_updated_at ON $TABLE_WORDS ($COLUMN_UPDATED_AT)")

        db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS  $TABLE_CATEGORIES (
                ${COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${COLUMN_UID} TEXT NOT NULL UNIQUE,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_CATEGORY_PARENT_UID TEXT DEFAULT NULL,
                $COLUMN_COLOR TEXT,
                $COLUMN_IS_DELETED INTEGER DEFAULT 0,
                ${COLUMN_CREATED_AT} INTEGER,
                ${COLUMN_UPDATED_AT} INTEGER,
                ${COLUMN_SYNC_STATUS} TEXT DEFAULT 'PENDING'
            )
            """.trimIndent()
        )

        db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS  $TABLE_NOTE_CATEGORY (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_UID TEXT NOT NULL UNIQUE,
                $COLUMN_CATEGORY_UID TEXT,
                $COLUMN_ITEM_UID TEXT,
                $COLUMN_CREATED_AT INTEGER,
                $COLUMN_UPDATED_AT INTEGER,
                $COLUMN_SYNC_STATUS TEXT DEFAULT 'PENDING',
                $COLUMN_IS_DELETED INTEGER DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
                """
                 CREATE TABLE IF NOT EXISTS $TABLE_TAGS (
                     $COLUMN_TAG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                     $COLUMN_TAG_UID TEXT UNIQUE,
                     $COLUMN_TAG_NAME TEXT,
                     $COLUMN_CREATED_AT INTEGER,
                     $COLUMN_UPDATED_AT INTEGER,
                     $COLUMN_TAG_SYNC_STATUS TEXT DEFAULT 'PENDING',
                     $COLUMN_TAG_IS_DELETED INTEGER DEFAULT 0
                 )
             """.trimIndent()
        )

        db.execSQL(
                """
                  CREATE TABLE IF NOT EXISTS $TABLE_POST_TAGS (
                      $COLUMN_PT_UID TEXT PRIMARY KEY,
                      $COLUMN_PT_POST_ID TEXT,
                      $COLUMN_PT_TAG_ID TEXT,
                      $COLUMN_CREATED_AT INTEGER,
                      $COLUMN_UPDATED_AT INTEGER,
                      $COLUMN_PT_SYNC_STATUS TEXT DEFAULT 'PENDING',
                      UNIQUE($COLUMN_PT_POST_ID, $COLUMN_PT_TAG_ID),
                      FOREIGN KEY($COLUMN_PT_POST_ID) REFERENCES $TABLE_WORDS($COLUMN_UID),
                      FOREIGN KEY($COLUMN_PT_TAG_ID) REFERENCES $TABLE_TAGS($COLUMN_TAG_UID)
                  )
             """.trimIndent()
        )

        db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS $TABLE_POST_COMMENTS (
                $COLUMN_PC_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PC_UID TEXT UNIQUE NOT NULL,
                $COLUMN_PC_POST_ID TEXT,
                $COLUMN_PC_USER_ID TEXT,
                $COLUMN_PC_TEXT TEXT,
                $COLUMN_PC_AUDIO_URL TEXT,
                $COLUMN_PC_MEDIA_URL TEXT,
                $COLUMN_PC_MEDIA_TYPE TEXT,
                $COLUMN_PC_ATTACHMENTS TEXT,
                $COLUMN_PC_CREATED_AT INTEGER,
                $COLUMN_PC_UPDATED_AT INTEGER,
                $COLUMN_PC_SYNC_STATUS TEXT DEFAULT 'PENDING',
                $COLUMN_PC_IS_DELETED INTEGER DEFAULT 0,
                FOREIGN KEY($COLUMN_PC_POST_ID) REFERENCES $TABLE_WORDS($COLUMN_UID)
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            onCreate(db)
        }
        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE $TABLE_POST_TAGS ADD COLUMN $COLUMN_PT_UID TEXT")
                db.execSQL(
                        "ALTER TABLE $TABLE_POST_TAGS ADD COLUMN $COLUMN_PT_SYNC_STATUS TEXT DEFAULT 'PENDING'"
                )
                // Populate existing rows with UIDs
                db.execSQL(
                        "UPDATE $TABLE_POST_TAGS SET $COLUMN_PT_UID = lower(hex(randomblob(16))) WHERE $COLUMN_PT_UID IS NULL"
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // FOR BACKUP/RESTORE
    // FOR BACKUP/RESTORE
    fun getAllWords(): List<Word> {
        val words = mutableListOf<Word>()
        val db = readableDatabase
        val cursor =
                db.query(TABLE_WORDS, null, null, null, null, null, "${COLUMN_CREATED_AT} DESC")

        while (cursor.moveToNext()) {
            words.add(getWordFromCursor(cursor))
        }
        cursor.close()
        return words
    }

    // FOR BACKUP/RESTORE
    fun getAllComments(): List<Comment> {
        val comments = mutableListOf<Comment>()
        val db = readableDatabase
        val cursor =
                db.query(
                        TABLE_COMMENTS,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "${COLUMN_COMMENT_CREATED_AT} ASC"
                )

        while (cursor.moveToNext()) {
            comments.add(getCommentFromCursor(cursor))
        }
        cursor.close()
        return comments
    }

    fun getAllTags(): List<Tag> {
        val tags = mutableListOf<Tag>()
        val db = readableDatabase
        val cursor =
                db.query(
                        TABLE_TAGS,
                        null,
                        "${COLUMN_TAG_IS_DELETED} = 0",
                        null,
                        null,
                        null,
                        "${COLUMN_TAG_NAME} ASC"
                )
        while (cursor.moveToNext()) {
            tags.add(getTagFromCursor(cursor))
        }
        cursor.close()
        return tags
    }

    fun getAllCategories(): List<Label> {
        val categories = mutableListOf<Label>()
        val db = readableDatabase
        val cursor =
                db.query(
                        TABLE_CATEGORIES,
                        null,
                        "$COLUMN_IS_DELETED = 0",
                        null,
                        null,
                        null,
                        "$COLUMN_NAME ASC"
                )
        while (cursor.moveToNext()) {
            categories.add(getLabelFromCursor(cursor))
        }
        cursor.close()
        return categories
    }

    fun getAllNoteCategories(): List<NoteCategory> {
        val list = mutableListOf<NoteCategory>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NOTE_CATEGORY, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            list.add(
                    NoteCategory(
                            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            uid = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UID)),
                            categoryUid =
                                    cursor.getString(
                                            cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_UID)
                                    ),
                            itemUid =
                                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_UID)),
                            createdAt =
                                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                            updatedAt =
                                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)),
                            syncStatus =
                                    try {
                                        SyncStatus.valueOf(
                                                cursor.getString(
                                                        cursor.getColumnIndexOrThrow(
                                                                COLUMN_SYNC_STATUS
                                                        )
                                                )
                                        )
                                    } catch (e: Exception) {
                                        SyncStatus.PENDING
                                    },
                            isDeleted =
                                    cursor.getInt(
                                            cursor.getColumnIndexOrThrow(COLUMN_IS_DELETED)
                                    ) == 1
                    )
            )
        }
        cursor.close()
        return list
    }

    fun getAllPostTags(): List<PostTag> {
        val list = mutableListOf<PostTag>()
        val db = readableDatabase
        val cursor = db.query(TABLE_POST_TAGS, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            list.add(
                    PostTag(
                            uid = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PT_UID)),
                            postId =
                                    cursor.getString(
                                            cursor.getColumnIndexOrThrow(COLUMN_PT_POST_ID)
                                    ),
                            tagId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PT_TAG_ID))
                    )
            )
        }
        cursor.close()
        return list
    }

    fun insertNoteCategories(list: List<NoteCategory>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            list.forEach { item ->
                val values =
                        ContentValues().apply {
                            put(COLUMN_UID, item.uid)
                            put(COLUMN_CATEGORY_UID, item.categoryUid)
                            put(COLUMN_ITEM_UID, item.itemUid)
                            put(COLUMN_CREATED_AT, item.createdAt)
                            put(COLUMN_UPDATED_AT, item.updatedAt)
                            put(COLUMN_SYNC_STATUS, item.syncStatus.name)
                            put(COLUMN_IS_DELETED, if (item.isDeleted) 1 else 0)
                        }
                db.insertWithOnConflict(
                        TABLE_NOTE_CATEGORY,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun insertPostTags(list: List<PostTag>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            list.forEach { item ->
                val values =
                        ContentValues().apply {
                            put(COLUMN_PT_POST_ID, item.postId)
                            put(COLUMN_PT_TAG_ID, item.tagId)
                        }
                db.insertWithOnConflict(
                        TABLE_POST_TAGS,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun toggleFavorite(uid: String, callback: (Int) -> Unit) {
        executor.execute {
            val db = writableDatabase

            try {
                // First, get current favorite status
                val cursor =
                        db.query(
                                TABLE_WORDS,
                                arrayOf(COLUMN_IS_FAVORITE),
                                "${COLUMN_UID} = ?",
                                arrayOf(uid),
                                null,
                                null,
                                null
                        )

                var currentStatus = 0
                if (cursor.moveToFirst()) {
                    currentStatus = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_FAVORITE))
                }
                cursor.close()

                val newStatus = if (currentStatus == 0) 1 else 0
                val values =
                        ContentValues().apply {
                            put(COLUMN_IS_FAVORITE, newStatus)
                            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                            put(COLUMN_SYNC_STATUS, SyncStatus.PENDING.name) // Mark as pending sync
                        }

                val rowsUpdated = db.update(TABLE_WORDS, values, "${COLUMN_UID} = ?", arrayOf(uid))

                if (rowsUpdated > 0) {
                    callback(newStatus)
                } else {
                    callback(-1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(-1) // Error
            }
        }
    }

    fun getTotalWordsCountExceptOwn(
            authId: String,
            searchQuery: String = "",
            callback: (Int) -> Unit
    ) {
        executor.execute {
            val db = readableDatabase

            val selectionList = mutableListOf<String>()
            val selectionArgsList = mutableListOf<String>()

            // Base conditions
            selectionList.add("$COLUMN_SYNC_STATUS != ?")
            selectionArgsList.add(SyncStatus.DELETED.name)

            selectionList.add("$COLUMN_USER_ID != ?")
            selectionArgsList.add(authId)

            // Search query
            if (searchQuery.isNotEmpty()) {
                selectionList.add(
                        "($COLUMN_WORD LIKE ? OR $COLUMN_SHORT_MEANING LIKE ? OR $COLUMN_DETAILS LIKE ?)"
                )
                val searchPattern = "%$searchQuery%"
                selectionArgsList.add(searchPattern)
                selectionArgsList.add(searchPattern)
                selectionArgsList.add(searchPattern)
            }

            val selection = selectionList.joinToString(" AND ")

            val cursor =
                    db.rawQuery(
                            "SELECT COUNT(*) FROM $TABLE_WORDS WHERE $selection",
                            selectionArgsList.toTypedArray()
                    )

            var count = 0
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }
            cursor.close()
            callback(count)
        }
    }

    fun getTotalWordsCountOwn(authId: String, searchQuery: String = "", callback: (Int) -> Unit) {
        executor.execute {
            val db = readableDatabase

            val selectionList = mutableListOf<String>()
            val selectionArgsList = mutableListOf<String>()

            // Base conditions
            selectionList.add("$COLUMN_SYNC_STATUS != ?")
            selectionArgsList.add(SyncStatus.DELETED.name)

            selectionList.add("$COLUMN_USER_ID = ?")
            selectionArgsList.add(authId)

            if (searchQuery.isNotEmpty()) {
                selectionList.add(
                        "($COLUMN_WORD LIKE ? OR $COLUMN_SHORT_MEANING LIKE ? OR $COLUMN_DETAILS LIKE ?)"
                )
                val searchPattern = "%$searchQuery%"
                selectionArgsList.add(searchPattern)
                selectionArgsList.add(searchPattern)
                selectionArgsList.add(searchPattern)
            }

            val selection = selectionList.joinToString(" AND ")

            val cursor =
                    db.rawQuery(
                            "SELECT COUNT(*) FROM $TABLE_WORDS WHERE $selection",
                            selectionArgsList.toTypedArray()
                    )

            var count = 0
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }
            cursor.close()
            callback(count)
        }
    }

    fun getAllWordsExceptOwn(
            authId: String,
            limit: Int = 20,
            offset: Int = 0,
            searchQuery: String = "",
            sortBy: String = "newest", // newest, oldest, alphabetical
            callback: (List<Word>) -> Unit
    ) {
        executor.execute {
            val words = mutableListOf<Word>()
            val db = readableDatabase

            val selectionList = mutableListOf<String>()
            val selectionArgsList = mutableListOf<String>()

            selectionList.add("$COLUMN_SYNC_STATUS != ?")
            selectionArgsList.add(SyncStatus.DELETED.name)

            selectionList.add("$COLUMN_USER_ID != ?")
            selectionArgsList.add(authId)

            if (searchQuery.isNotEmpty()) {
                selectionList.add(
                        "($COLUMN_WORD LIKE ? OR $COLUMN_SHORT_MEANING LIKE ? OR $COLUMN_DETAILS LIKE ?)"
                )
                val searchPattern = "%$searchQuery%"
                selectionArgsList.add(searchPattern)
                selectionArgsList.add(searchPattern)
                selectionArgsList.add(searchPattern)
            }

            val selection = selectionList.joinToString(" AND ")

            // Sorting
            val orderBy =
                    when (sortBy) {
                        "oldest" -> "$COLUMN_CREATED_AT ASC"
                        "alphabetical" -> "$COLUMN_WORD COLLATE NOCASE ASC"
                        else -> "$COLUMN_CREATED_AT DESC" // newest (default)
                    }

            val cursor =
                    db.query(
                            TABLE_WORDS,
                            null,
                            selection,
                            selectionArgsList.toTypedArray(),
                            null,
                            null,
                            orderBy,
                            "$offset, $limit"
                    )

            while (cursor.moveToNext()) {
                words.add(getWordFromCursor(cursor))
            }
            cursor.close()
            callback(words)
        }
    }

    fun getAllWordsAndClausesPaginated(
            sortOrder: Int,
            limit: Int,
            searchQuery: String? = null,
            offset: Int,
            isFav: Boolean = false,
            callback: (List<Word>) -> Unit
    ) {
        executor.execute {
            val notes = mutableListOf<Word>()

            val table = TABLE_WORDS

            val selectionParts = mutableListOf<String>()
            val selectionArgsList = mutableListOf<String>()

            // Filter by type = 'word' or 'clause'
            selectionParts.add("$TABLE_WORDS.$COLUMN_TYPE IN (?, ?)")
            selectionArgsList.add("word")
            selectionArgsList.add("clause")

            if (!searchQuery.isNullOrEmpty()) {
                val searchCondition =
                        """
                ($TABLE_WORDS.$COLUMN_WORD LIKE ? COLLATE NOCASE OR $TABLE_WORDS.$COLUMN_DETAILS LIKE ? COLLATE NOCASE)
            """.trimIndent()
                val arg = "%$searchQuery%"
                selectionParts.add(searchCondition)
                selectionArgsList.addAll(listOf(arg, arg))
            }

            // Handle favorite filter
            if (isFav) {
                selectionParts.add("$TABLE_WORDS.${COLUMN_IS_FAVORITE} = ?")
                selectionArgsList.add("1")
            }

            // Combine all conditions with AND
            val selection =
                    if (selectionParts.isEmpty()) null else selectionParts.joinToString(" AND ")
            val selectionArgs =
                    if (selectionArgsList.isEmpty()) null else selectionArgsList.toTypedArray()

            val groupBy = null

            var orderBy: String? = null
            if (sortOrder == 1) {
                orderBy = "$TABLE_WORDS.${COLUMN_UPDATED_AT} ASC "
            } else if (sortOrder == 2) {
                orderBy = "$TABLE_WORDS.${COLUMN_UPDATED_AT} DESC "
            } else if (sortOrder == 3) {
                orderBy = "$TABLE_WORDS.${COLUMN_WORD} COLLATE NOCASE ASC "
            }

            val columns =
                    arrayOf(
                            "$TABLE_WORDS.${COLUMN_ID}",
                            "$TABLE_WORDS.${COLUMN_UID}",
                            "$TABLE_WORDS.$COLUMN_WORD",
                            "$TABLE_WORDS.$COLUMN_TYPE",
                            "$TABLE_WORDS.$COLUMN_SHORT_MEANING",
                            "$TABLE_WORDS.$COLUMN_DETAILS",
                            "$TABLE_WORDS.${COLUMN_SYNC_STATUS}",
                            "$TABLE_WORDS.${COLUMN_IS_FAVORITE}",
                            "$TABLE_WORDS.${COLUMN_CREATED_AT}",
                            "$TABLE_WORDS.${COLUMN_UPDATED_AT}",
                            "$TABLE_WORDS.$COLUMN_IS_DELETED",
                            "$TABLE_WORDS.${COLUMN_LAST_SYNC_ATTEMPT}",
                            "$TABLE_WORDS.${COLUMN_RETRY_COUNT}"
                    )

            val db = readableDatabase
            val cursor =
                    db.query(
                            table,
                            columns,
                            selection,
                            selectionArgs,
                            groupBy,
                            null,
                            orderBy,
                            "$offset,$limit"
                    )

            while (cursor.moveToNext()) {
                notes.add(getWordFromCursor(cursor))
            }

            cursor.close()
            callback(notes)
        }
    }

    fun getAllWordsPaginated(
            sortOrder: Int,
            limit: Int,
            searchQuery: String? = null,
            offset: Int,
            isFav: Boolean = false,
            callback: (List<Word>) -> Unit
    ) {
        executor.execute {
            val notes = mutableListOf<Word>()

            val table = TABLE_WORDS

            val selectionParts = mutableListOf<String>()
            val selectionArgsList = mutableListOf<String>()

            // Filter by type = 'word' to exclude clauses and docs
            selectionParts.add("$TABLE_WORDS.$COLUMN_TYPE = ?")
            selectionArgsList.add("word")

            if (!searchQuery.isNullOrEmpty()) {
                val searchCondition =
                        """
                ($TABLE_WORDS.$COLUMN_WORD LIKE ? COLLATE NOCASE OR $TABLE_WORDS.$COLUMN_DETAILS LIKE ? COLLATE NOCASE)
            """.trimIndent()
                val arg = "%$searchQuery%"
                selectionParts.add(searchCondition)
                selectionArgsList.addAll(listOf(arg, arg))
            }

            // Handle favorite filter
            if (isFav) {
                selectionParts.add("$TABLE_WORDS.${COLUMN_IS_FAVORITE} = ?")
                selectionArgsList.add("1")
            }

            // Combine all conditions with AND
            val selection =
                    if (selectionParts.isEmpty()) null else selectionParts.joinToString(" AND ")
            val selectionArgs =
                    if (selectionArgsList.isEmpty()) null else selectionArgsList.toTypedArray()

            val groupBy = null

            var orderBy: String? = null
            if (sortOrder == 1) {
                orderBy = "$TABLE_WORDS.${COLUMN_UPDATED_AT} ASC "
            } else if (sortOrder == 2) {
                orderBy = "$TABLE_WORDS.${COLUMN_UPDATED_AT} DESC "
            } else if (sortOrder == 3) {
                orderBy = "$TABLE_WORDS.${COLUMN_WORD} COLLATE NOCASE ASC "
            }

            val columns =
                    arrayOf(
                            "$TABLE_WORDS.${COLUMN_ID}",
                            "$TABLE_WORDS.${COLUMN_UID}",
                            "$TABLE_WORDS.$COLUMN_WORD",
                            "$TABLE_WORDS.$COLUMN_TYPE",
                            "$TABLE_WORDS.$COLUMN_SHORT_MEANING",
                            "$TABLE_WORDS.$COLUMN_DETAILS",
                            "$TABLE_WORDS.${COLUMN_SYNC_STATUS}",
                            "$TABLE_WORDS.${COLUMN_IS_FAVORITE}",
                            "$TABLE_WORDS.${COLUMN_CREATED_AT}",
                            "$TABLE_WORDS.${COLUMN_UPDATED_AT}",
                            "$TABLE_WORDS.$COLUMN_IS_DELETED",
                            "$TABLE_WORDS.${COLUMN_LAST_SYNC_ATTEMPT}",
                            "$TABLE_WORDS.${COLUMN_RETRY_COUNT}"
                    )

            val db = readableDatabase
            val cursor =
                    db.query(
                            table,
                            columns,
                            selection,
                            selectionArgs,
                            groupBy,
                            null,
                            orderBy,
                            "$offset,$limit"
                    )

            while (cursor.moveToNext()) {
                notes.add(getWordFromCursor(cursor))
            }

            cursor.close()
            callback(notes)
        }
    }

    fun getClouseWordsList(sortOrder: Int = 2, callback: (List<Word>) -> Unit) {
        executor.execute {
            val notes = mutableListOf<Word>()

            val table = TABLE_WORDS

            val selectionParts = mutableListOf<String>()
            val selectionArgsList = mutableListOf<String>()

            selectionParts.add("$TABLE_WORDS.$COLUMN_TYPE = ?")
            selectionArgsList.add("clause")

            val selection =
                    if (selectionParts.isEmpty()) null else selectionParts.joinToString(" AND ")
            val selectionArgs =
                    if (selectionArgsList.isEmpty()) null else selectionArgsList.toTypedArray()

            val groupBy = null

            var orderBy: String? = null
            // Always sort by favorite first, then by the requested order
            val baseOrder = "$TABLE_WORDS.$COLUMN_IS_FAVORITE DESC"

            if (sortOrder == 1) {
                orderBy = "$baseOrder, $TABLE_WORDS.${COLUMN_UPDATED_AT} ASC "
            } else if (sortOrder == 2) {
                orderBy = "$baseOrder, $TABLE_WORDS.${COLUMN_UPDATED_AT} DESC "
            } else if (sortOrder == 3) {
                orderBy = "$baseOrder, $TABLE_WORDS.${COLUMN_WORD} COLLATE NOCASE ASC "
            }

            val columns =
                    arrayOf(
                            "$TABLE_WORDS.${COLUMN_ID}",
                            "$TABLE_WORDS.${COLUMN_UID}",
                            "$TABLE_WORDS.$COLUMN_WORD",
                            "$TABLE_WORDS.$COLUMN_TYPE",
                            "$TABLE_WORDS.$COLUMN_SHORT_MEANING",
                            "$TABLE_WORDS.$COLUMN_DETAILS",
                            "$TABLE_WORDS.${COLUMN_SYNC_STATUS}",
                            "$TABLE_WORDS.${COLUMN_IS_FAVORITE}",
                            "$TABLE_WORDS.${COLUMN_CREATED_AT}",
                            "$TABLE_WORDS.${COLUMN_UPDATED_AT}",
                            "$TABLE_WORDS.$COLUMN_IS_DELETED",
                            "$TABLE_WORDS.${COLUMN_LAST_SYNC_ATTEMPT}",
                            "$TABLE_WORDS.${COLUMN_RETRY_COUNT}"
                    )

            val db = readableDatabase
            val cursor =
                    db.query(table, columns, selection, selectionArgs, groupBy, null, orderBy, null)

            while (cursor.moveToNext()) {
                notes.add(getWordFromCursor(cursor))
            }

            cursor.close()
            callback(notes)
        }
    }

    fun getAllWordsOwn(
            authId: String,
            limit: Int = 20,
            offset: Int = 0,
            searchQuery: String = "",
            sortBy: String = "newest", // newest, oldest, alphabetical
            callback: (List<Word>) -> Unit
    ) {
        executor.execute {
            println("authId----------------- $authId")
            val words = mutableListOf<Word>()
            val db = readableDatabase

            val selectionList = mutableListOf<String>()
            val selectionArgsList = mutableListOf<String>()

            selectionList.add("$TABLE_WORDS.$COLUMN_SYNC_STATUS != ?")
            selectionArgsList.add(SyncStatus.DELETED.name)

            if (searchQuery.isNotEmpty()) {
                selectionList.add(
                        "($TABLE_WORDS.$COLUMN_WORD LIKE ? OR $TABLE_WORDS.$COLUMN_SHORT_MEANING LIKE ? OR $TABLE_WORDS.$COLUMN_DETAILS LIKE ?)"
                )
                val searchPattern = "%$searchQuery%"
                selectionArgsList.add(searchPattern)
                selectionArgsList.add(searchPattern)
                selectionArgsList.add(searchPattern)
            }

            val selection = selectionList.joinToString(" AND ")

            val orderBy =
                    when (sortBy) {
                        "oldest" -> "$TABLE_WORDS.$COLUMN_CREATED_AT ASC"
                        "alphabetical" -> "$TABLE_WORDS.$COLUMN_WORD COLLATE NOCASE ASC"
                        else -> "$TABLE_WORDS.$COLUMN_CREATED_AT DESC" // newest (default)
                    }

            val query =
                    """
                SELECT $TABLE_WORDS.* FROM $TABLE_WORDS 
                WHERE $selection 
                ORDER BY $orderBy LIMIT $limit OFFSET $offset
        """.trimIndent()

            val allArgs = selectionArgsList

            val cursor = db.rawQuery(query, allArgs.toTypedArray())

            while (cursor.moveToNext()) {
                words.add(getWordFromCursor(cursor))
            }
            cursor.close()
            callback(words)
        }
    }

    fun totalWordCount(callback: (Int) -> Unit) {
        executor.execute {
            val db = readableDatabase
            val query = "SELECT COUNT(*) FROM $TABLE_WORDS WHERE $COLUMN_SYNC_STATUS != ?"
            val cursor = db.rawQuery(query, arrayOf(SyncStatus.DELETED.name))
            var count = 0
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }
            cursor.close()
            callback(count)
        }
    }

    fun totalFavWordCount(callback: (Int) -> Unit) {
        executor.execute {
            val db = readableDatabase
            val query =
                    "SELECT COUNT(*) FROM $TABLE_WORDS WHERE $COLUMN_SYNC_STATUS != ? AND $COLUMN_IS_FAVORITE = 1"
            val cursor = db.rawQuery(query, arrayOf(SyncStatus.DELETED.name))
            var count = 0
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }
            cursor.close()
            callback(count)
        }
    }

    fun getVisitCounts(callback: (todayCount: Int, weekCount: Int) -> Unit) {
        executor.execute {
            val db = readableDatabase

            // Calculate time boundaries
            val calendar =
                    Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
            val todayStartTime = calendar.timeInMillis

            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val weekStartTime = calendar.timeInMillis

            // Single query using CASE statements
            val query =
                    """
            SELECT 
                SUM(CASE WHEN $COLUMN_LAST_VIEWED >= ? THEN 1 ELSE 0 END) as today_count,
                SUM(CASE WHEN $COLUMN_LAST_VIEWED >= ? THEN 1 ELSE 0 END) as week_count
            FROM $TABLE_WORDS 
            WHERE $COLUMN_SYNC_STATUS != ?
        """.trimIndent()

            var todayCount = 0
            var weekCount = 0

            val cursor =
                    db.rawQuery(
                            query,
                            arrayOf(
                                    todayStartTime.toString(),
                                    weekStartTime.toString(),
                                    SyncStatus.DELETED.name
                            )
                    )

            if (cursor.moveToFirst()) {
                todayCount = cursor.getInt(0)
                weekCount = cursor.getInt(1)
            }
            cursor.close()

            callback(todayCount, weekCount)
        }
    }

    fun getFavoriteWords(limit: Int = 20, offset: Int = 0, callback: (List<Word>) -> Unit) {
        executor.execute {
            val words = mutableListOf<Word>()
            val db = readableDatabase

            val query =
                    """
            SELECT $TABLE_WORDS.* FROM $TABLE_WORDS WHERE $TABLE_WORDS.$COLUMN_SYNC_STATUS != ? AND $TABLE_WORDS.$COLUMN_IS_FAVORITE = 1
            ORDER BY $TABLE_WORDS.$COLUMN_UPDATED_AT DESC
            LIMIT $limit OFFSET $offset
        """.trimIndent()
            val cursor = db.rawQuery(query, arrayOf(SyncStatus.DELETED.name))
            while (cursor.moveToNext()) {
                words.add(getWordFromCursor(cursor))
            }
            cursor.close()
            callback(words)
        }
    }

    fun getFrequentViewWords(limit: Int = 20, offset: Int = 0, callback: (List<Word>) -> Unit) {
        executor.execute {
            val words = mutableListOf<Word>()
            val db = readableDatabase

            val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)

            val query =
                    """
            SELECT $TABLE_WORDS.*
            FROM $TABLE_WORDS 
            WHERE $TABLE_WORDS.$COLUMN_SYNC_STATUS != ? 
                AND $TABLE_WORDS.$COLUMN_LAST_VIEWED >= ? 
                AND $TABLE_WORDS.$COLUMN_VIEW_COUNT > 0
            ORDER BY $TABLE_WORDS.$COLUMN_VIEW_COUNT DESC
            LIMIT $limit OFFSET $offset
        """.trimIndent()

            val cursor =
                    db.rawQuery(query, arrayOf(SyncStatus.DELETED.name, thirtyDaysAgo.toString()))

            while (cursor.moveToNext()) {
                words.add(getWordFromCursor(cursor))
            }
            cursor.close()
            callback(words)
        }
    }

    suspend fun updateWord(word: Word): Int =
            withContext(Dispatchers.IO) {
                val db = writableDatabase
                val values =
                        ContentValues().apply {
                            put(COLUMN_WORD, word.word)
                            put(COLUMN_USER_ID, word.userId)
                            put(COLUMN_SHORT_MEANING, word.shortMeaning)
                            put(COLUMN_DETAILS, word.details)
                            put(COLUMN_IS_FAVORITE, if (word.isFavorite) 1 else 0)
                            put(COLUMN_VIEW_COUNT, word.viewCount)
                            put(COLUMN_LAST_VIEWED, word.lastVisited)
                            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                            put(COLUMN_SYNC_STATUS, SyncStatus.PENDING.name)
                            if (word.attachments != null) {
                                val jsonArray = JSONArray()
                                word.attachments.forEach { attachment ->
                                    val jsonObject = JSONObject()
                                    jsonObject.put("url", attachment.url)
                                    jsonObject.put("type", attachment.type)
                                    jsonArray.put(jsonObject)
                                }
                                put(COLUMN_ATTACHMENTS, jsonArray.toString())
                            }
                        }

                db.update(TABLE_WORDS, values, "$COLUMN_UID = ?", arrayOf(word.uid))
            }

    fun upsertWord(words: List<WordPartial>) {
        if (words.isEmpty()) return

        val db = writableDatabase
        try {
            db.beginTransaction()
            words.forEach { word ->
                // Check if word already exists
                val cursor =
                        db.query(
                                TABLE_WORDS,
                                arrayOf(COLUMN_ID),
                                "$COLUMN_UID = ?",
                                arrayOf(word.uid),
                                null,
                                null,
                                null
                        )

                val exists = cursor.count > 0
                cursor.close()

                val values =
                        ContentValues().apply {
                            put(COLUMN_UID, word.uid)
                            word.word?.let { put(COLUMN_WORD, it) }
                            word.userId?.let { put(COLUMN_USER_ID, it) }
                            word.type?.let { put(COLUMN_TYPE, it) }
                            word.shortMeaning?.let { put(COLUMN_SHORT_MEANING, it) }
                            word.details?.let { put(COLUMN_DETAILS, it) }
                            word.isFavorite?.let { put(COLUMN_IS_FAVORITE, if (it) 1 else 0) }
                            word.viewCount?.let { put(COLUMN_VIEW_COUNT, it) }
                            word.lastVisited?.let { put(COLUMN_LAST_VIEWED, it) }
                            put(COLUMN_SYNC_STATUS, SyncStatus.SYNCED.name)
                            word.retryCount?.let { put(COLUMN_RETRY_COUNT, it) }
                            word.lastSyncAttempt?.let { put(COLUMN_LAST_SYNC_ATTEMPT, it) }
                            word.isDeleted?.let { put(COLUMN_IS_DELETED, if (it) 1 else 0) }

                            val now = System.currentTimeMillis()
                            if (!exists) {
                                put(COLUMN_CREATED_AT, word.createdAt ?: now)
                            }
                            put(COLUMN_UPDATED_AT, word.updatedAt ?: now)
                        }

                if (exists) {
                    db.update(TABLE_WORDS, values, "$COLUMN_UID = ?", arrayOf(word.uid))
                } else {
                    db.insert(TABLE_WORDS, null, values)
                }
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("Database", "Transaction failed: ${e.message}")
            throw e
        } finally {
            try {
                db.endTransaction()
            } catch (e: Exception) {
                Log.e("Database", "Error ending transaction: ${e.message}")
            }
        }
    }

    fun upsertTags(tags: List<Tag>) {
        if (tags.isEmpty()) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            tags.forEach { tag ->
                val values =
                        ContentValues().apply {
                            put(COLUMN_TAG_UID, tag.uid)
                            put(COLUMN_TAG_NAME, tag.name)
                            put(COLUMN_CREATED_AT, tag.createdAt)
                            put(COLUMN_UPDATED_AT, tag.updatedAt)
                            put(COLUMN_TAG_SYNC_STATUS, SyncStatus.SYNCED.name)
                            put(COLUMN_TAG_IS_DELETED, if (tag.isDeleted) 1 else 0)
                        }
                db.insertWithOnConflict(TABLE_TAGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun upsertCategories(categories: List<Label>) {
        if (categories.isEmpty()) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            categories.forEach { category ->
                val values =
                        ContentValues().apply {
                            put(COLUMN_UID, category.uid)
                            put(COLUMN_NAME, category.name)
                            put(COLUMN_COLOR, category.color)
                            put(COLUMN_CREATED_AT, category.createdAt)
                            put(COLUMN_UPDATED_AT, category.updatedAt)
                            put(COLUMN_SYNC_STATUS, SyncStatus.SYNCED.name)
                            put(COLUMN_IS_DELETED, if (category.isDeleted) 1 else 0)
                        }
                db.insertWithOnConflict(
                        TABLE_CATEGORIES,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun upsertComments(comments: List<Comment>) {
        if (comments.isEmpty()) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            comments.forEach { comment ->
                val values =
                        ContentValues().apply {
                            put(COLUMN_COMMENT_REMOTE_ID, comment._id)
                            put(COLUMN_COMMENT_USERNAME, comment.username)
                            put(COLUMN_COMMENT_TEXT, comment.text)
                            put(COLUMN_COMMENT_AUDIO_URL, comment.audioUrl)
                            put(COLUMN_COMMENT_MEDIA_URL, comment.mediaUrl)
                            put(COLUMN_COMMENT_MEDIA_TYPE, comment.mediaType)
                            put(COLUMN_COMMENT_PARENT_ID, comment.parentId)
                            put(COLUMN_CREATED_AT, comment.createdAt)
                            put(COLUMN_UPDATED_AT, comment.updatedAt)
                            put(COLUMN_SYNC_STATUS, SyncStatus.SYNCED.name)
                            put(COLUMN_IS_DELETED, if (comment.isDeleted) 1 else 0)
                            comment.attachments?.let {
                                put(COLUMN_COMMENT_ATTACHMENTS, Gson().toJson(it))
                            }
                        }
                db.insertWithOnConflict(
                        TABLE_COMMENTS,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun upsertPostComments(comments: List<PostComment>) {
        if (comments.isEmpty()) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            comments.forEach { comment ->
                val values =
                        ContentValues().apply {
                            put(COLUMN_PC_UID, comment.uid)
                            put(COLUMN_PC_POST_ID, comment.postId)
                            put(COLUMN_PC_USER_ID, comment.userId)
                            put(COLUMN_PC_TEXT, comment.text)
                            put(COLUMN_PC_AUDIO_URL, comment.audioUrl)
                            put(COLUMN_PC_MEDIA_URL, comment.mediaUrl)
                            put(COLUMN_PC_MEDIA_TYPE, comment.mediaType)
                            put(COLUMN_PC_CREATED_AT, comment.createdAt)
                            put(COLUMN_PC_UPDATED_AT, comment.updatedAt)
                            put(COLUMN_PC_SYNC_STATUS, SyncStatus.SYNCED.name)
                            put(COLUMN_PC_IS_DELETED, if (comment.isDeleted) 1 else 0)
                            comment.attachments?.let {
                                put(COLUMN_PC_ATTACHMENTS, Gson().toJson(it))
                            }
                        }
                db.insertWithOnConflict(
                        TABLE_POST_COMMENTS,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun upsertPostTags(list: List<PostTag>) {
        if (list.isEmpty()) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            list.forEach { item ->
                val values =
                        ContentValues().apply {
                            put(COLUMN_PT_UID, item.uid)
                            put(COLUMN_PT_POST_ID, item.postId)
                            put(COLUMN_PT_TAG_ID, item.tagId)
                            put(COLUMN_CREATED_AT, item.createdAt)
                            put(COLUMN_UPDATED_AT, item.updatedAt)
                            put(COLUMN_PT_SYNC_STATUS, SyncStatus.SYNCED.name)
                        }
                db.insertWithOnConflict(
                        TABLE_POST_TAGS,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun upsertNoteCategories(list: List<NoteCategory>) {
        if (list.isEmpty()) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            list.forEach { item ->
                val values =
                        ContentValues().apply {
                            put(COLUMN_UID, item.uid)
                            put(COLUMN_CATEGORY_UID, item.categoryUid)
                            put(COLUMN_ITEM_UID, item.itemUid)
                            put(COLUMN_CREATED_AT, item.createdAt)
                            put(COLUMN_UPDATED_AT, item.updatedAt)
                            put(COLUMN_SYNC_STATUS, SyncStatus.SYNCED.name)
                            put(COLUMN_IS_DELETED, if (item.isDeleted) 1 else 0)
                        }
                db.insertWithOnConflict(
                        TABLE_NOTE_CATEGORY,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    suspend fun updatePartial(partialWord: WordPartial): Int =
            withContext(Dispatchers.IO) {
                val db = writableDatabase
                val values =
                        ContentValues().apply {
                            partialWord.word?.let { put(COLUMN_WORD, it) }
                            partialWord.type?.let { put(COLUMN_TYPE, it) }
                            partialWord.userId?.let { put(COLUMN_USER_ID, it) }
                            partialWord.shortMeaning?.let { put(COLUMN_SHORT_MEANING, it) }
                            partialWord.details?.let { put(COLUMN_DETAILS, it) }
                            partialWord.isFavorite?.let {
                                put(COLUMN_IS_FAVORITE, if (it) 1 else 0)
                            }
                            partialWord.viewCount?.let { put(COLUMN_VIEW_COUNT, it) }
                            partialWord.lastVisited?.let { put(COLUMN_LAST_VIEWED, it) }
                            partialWord.syncStatus?.let { put(COLUMN_SYNC_STATUS, it.name) }
                            partialWord.retryCount?.let { put(COLUMN_RETRY_COUNT, it) }
                            partialWord.lastSyncAttempt?.let { put(COLUMN_LAST_SYNC_ATTEMPT, it) }

                            // Always update these fields
                            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                        }

                println("uid ${partialWord.uid}")

                // Only proceed if there are values to update (excluding the always-updated fields)
                if (values.size() > 1) { // More than just updatedAt
                    db.update(TABLE_WORDS, values, "$COLUMN_UID = ?", arrayOf(partialWord.uid))
                } else {
                    0
                }
            }

    suspend fun incrementViewCount(uid: String): Int =
            withContext(Dispatchers.IO) {
                val db = writableDatabase
                val values =
                        ContentValues().apply {
                            put(COLUMN_LAST_VIEWED, System.currentTimeMillis())
                        }

                db.execSQL(
                        "UPDATE $TABLE_WORDS SET $COLUMN_VIEW_COUNT = $COLUMN_VIEW_COUNT + 1 WHERE $COLUMN_UID = ?",
                        arrayOf(uid)
                )

                db.update(TABLE_WORDS, values, "$COLUMN_UID = ?", arrayOf(uid))
            }

    fun getWordByUid(uid: String, callback: (Word?) -> Unit) {
        Thread {
                    val db = readableDatabase
                    val cursor =
                            db.query(
                                    TABLE_WORDS,
                                    null,
                                    "$COLUMN_UID = ? AND $COLUMN_IS_DELETED = 0",
                                    arrayOf(uid),
                                    null,
                                    null,
                                    null
                            )

                    var word =
                            if (cursor.moveToFirst()) {
                                getWordFromCursor(cursor)
                            } else null

                    cursor.close()

                    if (word != null) {
                        val comments = getCommentsForPost(uid)
                        val tags = getTagsForPost(uid)
                        val categories = getCategoriesForPost(uid)
                        word = word.copy(comments = comments, tags = tags, categories = categories)
                    }

                    callback(word)
                }
                .start()
    }

    private fun getTagsForPost(postUid: String): List<Tag> {
        val tags = mutableListOf<Tag>()
        val db = readableDatabase
        // Query to join POST_TAGS and TAGS
        val query =
                """
            SELECT T.* FROM $TABLE_TAGS T
            INNER JOIN $TABLE_POST_TAGS PT ON T.$COLUMN_TAG_UID = PT.$COLUMN_PT_TAG_ID
            WHERE PT.$COLUMN_PT_POST_ID = ? AND T.$COLUMN_TAG_IS_DELETED = 0
        """
        val cursor = db.rawQuery(query, arrayOf(postUid))
        while (cursor.moveToNext()) {
            tags.add(getTagFromCursor(cursor))
        }
        cursor.close()
        return tags
    }

    private fun getCategoriesForPost(postUid: String): List<Label> {
        val categories = mutableListOf<Label>()
        val db = readableDatabase
        val query =
                """
             SELECT C.* FROM $TABLE_CATEGORIES C
             INNER JOIN $TABLE_NOTE_CATEGORY NC ON C.$COLUMN_UID = NC.$COLUMN_CATEGORY_UID
             WHERE NC.$COLUMN_ITEM_UID = ? AND C.$COLUMN_IS_DELETED = 0
         """
        val cursor = db.rawQuery(query, arrayOf(postUid))
        while (cursor.moveToNext()) {
            categories.add(getLabelFromCursor(cursor))
        }
        cursor.close()
        return categories
    }

    private fun getTagFromCursor(cursor: Cursor): Tag {
        return Tag(
                id = CursorUtils.getIntSafe(cursor, COLUMN_TAG_ID) ?: 0,
                uid = CursorUtils.getStringSafe(cursor, COLUMN_TAG_UID) ?: "",
                name = CursorUtils.getStringSafe(cursor, COLUMN_TAG_NAME) ?: "",
                createdAt = CursorUtils.getLongSafe(cursor, COLUMN_CREATED_AT) ?: 0L,
                updatedAt = CursorUtils.getLongSafe(cursor, COLUMN_UPDATED_AT) ?: 0L,
                syncStatus =
                        try {
                            SyncStatus.valueOf(
                                    CursorUtils.getStringSafe(cursor, COLUMN_TAG_SYNC_STATUS)
                                            ?: "PENDING"
                            )
                        } catch (e: Exception) {
                            SyncStatus.PENDING
                        },
                isDeleted = (CursorUtils.getIntSafe(cursor, COLUMN_TAG_IS_DELETED) ?: 0) == 1
        )
    }

    fun insertWord(word: Word): Long {
        val db = writableDatabase
        val values =
                ContentValues().apply {
                    put(COLUMN_UID, word.uid)
                    put(COLUMN_UPDATE_UID, word.updateUid)
                    put(COLUMN_WORD, word.word)
                    put(COLUMN_USER_ID, word.userId)
                    put(COLUMN_TYPE, word.type)
                    put(COLUMN_SHORT_MEANING, word.shortMeaning)
                    put(COLUMN_DETAILS, word.details)
                    put(COLUMN_COVER, word.cover)
                    put(COLUMN_IS_FAVORITE, if (word.isFavorite) 1 else 0)
                    put(COLUMN_IS_DELETED, if (word.isDeleted) 1 else 0)
                    put(COLUMN_VIEW_COUNT, word.viewCount)
                    put(COLUMN_LAST_VIEWED, word.lastVisited)
                    put(COLUMN_CREATED_AT, word.createdAt)
                    put(COLUMN_UPDATED_AT, word.updatedAt)
                    put(COLUMN_SYNC_STATUS, word.syncStatus.name)
                    put(COLUMN_RETRY_COUNT, word.retryCount)
                    put(COLUMN_LAST_SYNC_ATTEMPT, word.lastSyncAttempt)
                    // Handle attachments if present
                    word.attachments?.let { put(COLUMN_ATTACHMENTS, Gson().toJson(it)) }
                }

        return db.insertWithOnConflict(TABLE_WORDS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deletePostComment(commentId: String) {
        val db = writableDatabase
        db.delete(TABLE_POST_COMMENTS, "$COLUMN_PC_UID = ?", arrayOf(commentId))
    }

    suspend fun getUnsyncedWords(): List<Word> =
            withContext(Dispatchers.IO) {
                val words = mutableListOf<Word>()
                val db = readableDatabase

                db.query(
                                TABLE_WORDS,
                                null,
                                "$COLUMN_SYNC_STATUS != ?",
                                arrayOf(SyncStatus.SYNCED.name),
                                null,
                                null,
                                "$COLUMN_UPDATED_AT DESC"
                        )
                        .use { cursor ->
                            while (cursor.moveToNext()) {
                                words.add(getWordFromCursor(cursor))
                            }
                        }

                return@withContext words
            }

    fun updateWordSyncStatus(
            uid: String,
            syncStatus: SyncStatus = SyncStatus.SYNCED,
            retryCount: Int = 0,
            lastSyncAttempt: Long? = null,
            callback: (Int) -> Unit = {}
    ) {
        executor.execute {
            val db = writableDatabase
            val values =
                    ContentValues().apply {
                        put(COLUMN_SYNC_STATUS, syncStatus.name)
                        put(COLUMN_RETRY_COUNT, retryCount)
                        if (lastSyncAttempt != null) {
                            put(COLUMN_LAST_SYNC_ATTEMPT, lastSyncAttempt)
                        }
                        put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                    }

            val rowsAffected = db.update(TABLE_WORDS, values, "$COLUMN_UID = ?", arrayOf(uid))
            callback(rowsAffected)
        }
    }

    suspend fun getUnsyncedTags(): List<Tag> =
            withContext(Dispatchers.IO) {
                val tags = mutableListOf<Tag>()
                val db = readableDatabase
                db.query(
                                TABLE_TAGS,
                                null,
                                "$COLUMN_TAG_SYNC_STATUS != ?",
                                arrayOf(SyncStatus.SYNCED.name),
                                null,
                                null,
                                "$COLUMN_CREATED_AT DESC"
                        )
                        .use { cursor ->
                            while (cursor.moveToNext()) {
                                tags.add(
                                        Tag(
                                                id =
                                                        cursor.getInt(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_TAG_ID
                                                                )
                                                        ),
                                                uid =
                                                        cursor.getString(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_TAG_UID
                                                                )
                                                        ),
                                                name =
                                                        cursor.getString(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_TAG_NAME
                                                                )
                                                        ),
                                                createdAt =
                                                        cursor.getLong(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_CREATED_AT
                                                                )
                                                        ),
                                                updatedAt =
                                                        cursor.getLong(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_UPDATED_AT
                                                                )
                                                        ),
                                                syncStatus =
                                                        try {
                                                            SyncStatus.valueOf(
                                                                    cursor.getString(
                                                                            cursor.getColumnIndexOrThrow(
                                                                                    COLUMN_TAG_SYNC_STATUS
                                                                            )
                                                                    )
                                                            )
                                                        } catch (e: Exception) {
                                                            SyncStatus.PENDING
                                                        },
                                                isDeleted =
                                                        cursor.getInt(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_TAG_IS_DELETED
                                                                )
                                                        ) == 1
                                        )
                                )
                            }
                        }
                return@withContext tags
            }

    fun updateTagSyncStatus(
            uid: String,
            syncStatus: SyncStatus = SyncStatus.SYNCED,
            retryCount: Int = 0,
            callback: (Int) -> Unit = {}
    ) {
        executor.execute {
            val db = writableDatabase
            val values =
                    ContentValues().apply {
                        put(COLUMN_TAG_SYNC_STATUS, syncStatus.name)
                        put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                    }
            val rowsAffected = db.update(TABLE_TAGS, values, "$COLUMN_TAG_UID = ?", arrayOf(uid))
            callback(rowsAffected)
        }
    }

    suspend fun getUnsyncedCategories(): List<Label> =
            withContext(Dispatchers.IO) {
                val categories = mutableListOf<Label>()
                val db = readableDatabase
                db.query(
                                TABLE_CATEGORIES,
                                null,
                                "$COLUMN_SYNC_STATUS != ?",
                                arrayOf(SyncStatus.SYNCED.name),
                                null,
                                null,
                                "$COLUMN_CREATED_AT DESC"
                        )
                        .use { cursor ->
                            while (cursor.moveToNext()) {
                                categories.add(getLabelFromCursor(cursor))
                            }
                        }
                return@withContext categories
            }

    fun updateCategorySyncStatus(
            uid: String,
            syncStatus: SyncStatus = SyncStatus.SYNCED,
            callback: (Int) -> Unit = {}
    ) {
        executor.execute {
            val db = writableDatabase
            val values =
                    ContentValues().apply {
                        put(COLUMN_SYNC_STATUS, syncStatus.name)
                        put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                    }
            val rowsAffected = db.update(TABLE_CATEGORIES, values, "$COLUMN_UID = ?", arrayOf(uid))
            callback(rowsAffected)
        }
    }

    suspend fun getUnsyncedPostTags(): List<PostTag> =
            withContext(Dispatchers.IO) {
                val associations = mutableListOf<PostTag>()
                val db = readableDatabase
                db.query(
                                TABLE_POST_TAGS,
                                null,
                                "$COLUMN_PT_SYNC_STATUS != ?",
                                arrayOf(SyncStatus.SYNCED.name),
                                null,
                                null,
                                null
                        )
                        .use { cursor ->
                            while (cursor.moveToNext()) {
                                associations.add(
                                        PostTag(
                                                uid =
                                                        cursor.getString(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_PT_UID
                                                                )
                                                        ),
                                                postId =
                                                        cursor.getString(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_PT_POST_ID
                                                                )
                                                        ),
                                                tagId =
                                                        cursor.getString(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_PT_TAG_ID
                                                                )
                                                        ),
                                                syncStatus =
                                                        try {
                                                            SyncStatus.valueOf(
                                                                    cursor.getString(
                                                                            cursor.getColumnIndexOrThrow(
                                                                                    COLUMN_PT_SYNC_STATUS
                                                                            )
                                                                    )
                                                            )
                                                        } catch (e: Exception) {
                                                            SyncStatus.PENDING
                                                        }
                                        )
                                )
                            }
                        }
                return@withContext associations
            }

    fun updatePostTagSyncStatus(
            uid: String,
            syncStatus: SyncStatus = SyncStatus.SYNCED,
            callback: (Int) -> Unit = {}
    ) {
        executor.execute {
            val db = writableDatabase
            val values = ContentValues().apply { put(COLUMN_PT_SYNC_STATUS, syncStatus.name) }
            val rowsAffected =
                    db.update(TABLE_POST_TAGS, values, "$COLUMN_PT_UID = ?", arrayOf(uid))
            callback(rowsAffected)
        }
    }

    suspend fun getUnsyncedNoteCategories(): List<NoteCategory> =
            withContext(Dispatchers.IO) {
                val associations = mutableListOf<NoteCategory>()
                val db = readableDatabase
                db.query(
                                TABLE_NOTE_CATEGORY,
                                null,
                                "$COLUMN_SYNC_STATUS != ?",
                                arrayOf(SyncStatus.SYNCED.name),
                                null,
                                null,
                                "$COLUMN_CREATED_AT DESC"
                        )
                        .use { cursor ->
                            while (cursor.moveToNext()) {
                                associations.add(
                                        NoteCategory(
                                                id =
                                                        cursor.getInt(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_ID
                                                                )
                                                        ),
                                                uid =
                                                        cursor.getString(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_UID
                                                                )
                                                        ),
                                                categoryUid =
                                                        cursor.getString(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_CATEGORY_UID
                                                                )
                                                        ),
                                                itemUid =
                                                        cursor.getString(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_ITEM_UID
                                                                )
                                                        ),
                                                createdAt =
                                                        cursor.getLong(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_CREATED_AT
                                                                )
                                                        ),
                                                updatedAt =
                                                        cursor.getLong(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_UPDATED_AT
                                                                )
                                                        ),
                                                syncStatus =
                                                        try {
                                                            SyncStatus.valueOf(
                                                                    cursor.getString(
                                                                            cursor.getColumnIndexOrThrow(
                                                                                    COLUMN_SYNC_STATUS
                                                                            )
                                                                    )
                                                            )
                                                        } catch (e: Exception) {
                                                            SyncStatus.PENDING
                                                        },
                                                isDeleted =
                                                        cursor.getInt(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_IS_DELETED
                                                                )
                                                        ) == 1
                                        )
                                )
                            }
                        }
                return@withContext associations
            }

    fun updateNoteCategorySyncStatus(
            uid: String,
            syncStatus: SyncStatus = SyncStatus.SYNCED,
            callback: (Int) -> Unit = {}
    ) {
        executor.execute {
            val db = writableDatabase
            val values =
                    ContentValues().apply {
                        put(COLUMN_SYNC_STATUS, syncStatus.name)
                        put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                    }
            val rowsAffected =
                    db.update(TABLE_NOTE_CATEGORY, values, "$COLUMN_UID = ?", arrayOf(uid))
            callback(rowsAffected)
        }
    }

    suspend fun getUnsyncedComments(): List<Comment> =
            withContext(Dispatchers.IO) {
                val comments = mutableListOf<Comment>()
                val db = readableDatabase
                db.query(
                                TABLE_COMMENTS,
                                null,
                                "$COLUMN_SYNC_STATUS != ?",
                                arrayOf(SyncStatus.SYNCED.name),
                                null,
                                null,
                                "$COLUMN_COMMENT_CREATED_AT DESC"
                        )
                        .use { cursor ->
                            while (cursor.moveToNext()) {
                                comments.add(getCommentFromCursor(cursor))
                            }
                        }
                return@withContext comments
            }

    fun updateCommentSyncStatus(
            remoteId: String,
            syncStatus: SyncStatus = SyncStatus.SYNCED,
            callback: (Int) -> Unit = {}
    ) {
        executor.execute {
            val db = writableDatabase
            val values = ContentValues().apply { put(COLUMN_SYNC_STATUS, syncStatus.name) }
            val rowsAffected =
                    db.update(
                            TABLE_COMMENTS,
                            values,
                            "$COLUMN_COMMENT_REMOTE_ID = ?",
                            arrayOf(remoteId)
                    )
            callback(rowsAffected)
        }
    }

    suspend fun getUnsyncedPostComments(): List<PostComment> =
            withContext(Dispatchers.IO) {
                val comments = mutableListOf<PostComment>()
                val db = readableDatabase
                db.query(
                                TABLE_POST_COMMENTS,
                                null,
                                "$COLUMN_PC_SYNC_STATUS != ?",
                                arrayOf(SyncStatus.SYNCED.name),
                                null,
                                null,
                                "$COLUMN_PC_CREATED_AT DESC"
                        )
                        .use { cursor ->
                            while (cursor.moveToNext()) {
                                comments.add(
                                        PostComment(
                                                id =
                                                        cursor.getInt(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_PC_ID
                                                                )
                                                        ),
                                                uid =
                                                        cursor.getString(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_PC_UID
                                                                )
                                                        ),
                                                postId =
                                                        cursor.getString(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_PC_POST_ID
                                                                )
                                                        ),
                                                userId =
                                                        cursor.getString(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_PC_USER_ID
                                                                )
                                                        ),
                                                text =
                                                        cursor.getString(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_PC_TEXT
                                                                )
                                                        ),
                                                audioUrl =
                                                        cursor.getString(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_PC_AUDIO_URL
                                                                )
                                                        ),
                                                mediaUrl =
                                                        cursor.getString(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_PC_MEDIA_URL
                                                                )
                                                        ),
                                                mediaType =
                                                        cursor.getString(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_PC_MEDIA_TYPE
                                                                )
                                                        ),
                                                attachments =
                                                        try {
                                                            val jsonString =
                                                                    cursor.getString(
                                                                            cursor.getColumnIndexOrThrow(
                                                                                    COLUMN_PC_ATTACHMENTS
                                                                            )
                                                                    )
                                                            if (!jsonString.isNullOrEmpty()) {
                                                                val jsonArray =
                                                                        JSONArray(jsonString)
                                                                val list =
                                                                        mutableListOf<
                                                                                CommentAttachment>()
                                                                for (i in
                                                                        0 until
                                                                                jsonArray
                                                                                        .length()) {
                                                                    val obj =
                                                                            jsonArray.getJSONObject(
                                                                                    i
                                                                            )
                                                                    list.add(
                                                                            CommentAttachment(
                                                                                    url =
                                                                                            obj.getString(
                                                                                                    "url"
                                                                                            ),
                                                                                    type =
                                                                                            obj.getString(
                                                                                                    "type"
                                                                                            )
                                                                            )
                                                                    )
                                                                }
                                                                list
                                                            } else null
                                                        } catch (e: Exception) {
                                                            null
                                                        },
                                                createdAt =
                                                        cursor.getLong(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_PC_CREATED_AT
                                                                )
                                                        ),
                                                updatedAt =
                                                        cursor.getLong(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_PC_UPDATED_AT
                                                                )
                                                        ),
                                                syncStatus =
                                                        try {
                                                            SyncStatus.valueOf(
                                                                    cursor.getString(
                                                                            cursor.getColumnIndexOrThrow(
                                                                                    COLUMN_PC_SYNC_STATUS
                                                                            )
                                                                    )
                                                            )
                                                        } catch (e: Exception) {
                                                            SyncStatus.PENDING
                                                        },
                                                isDeleted =
                                                        cursor.getInt(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_PC_IS_DELETED
                                                                )
                                                        ) == 1
                                        )
                                )
                            }
                        }
                return@withContext comments
            }

    fun updatePostCommentSyncStatus(
            uid: String,
            syncStatus: SyncStatus = SyncStatus.SYNCED,
            callback: (Int) -> Unit = {}
    ) {
        executor.execute {
            val db = writableDatabase
            val values = ContentValues().apply { put(COLUMN_PC_SYNC_STATUS, syncStatus.name) }
            val rowsAffected =
                    db.update(TABLE_POST_COMMENTS, values, "$COLUMN_PC_UID = ?", arrayOf(uid))
            callback(rowsAffected)
        }
    }

    suspend fun deleteWord(uid: String): Int =
            withContext(Dispatchers.IO) {
                val db = writableDatabase
                try {
                    val values =
                            ContentValues().apply {
                                put(COLUMN_SYNC_STATUS, SyncStatus.DELETED.name)
                                put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                            }

                    val rowsAffected =
                            db.update(TABLE_WORDS, values, "$COLUMN_UID = ?", arrayOf(uid))

                    return@withContext rowsAffected
                } catch (e: Exception) {
                    throw e
                }
            }

    fun deleteWordHard(uid: String) {
        executor.execute {
            val db = writableDatabase
            try {
                db.delete(TABLE_WORDS, "$COLUMN_UID = ?", arrayOf(uid))
            } catch (e: Exception) {
                println("Failed to delete word: ${e.message}")
            }
        }
    }

    fun getTagByName(name: String): Tag? {
        val db = readableDatabase
        val cursor =
                db.query(
                        TABLE_TAGS,
                        null,
                        "$COLUMN_TAG_NAME = ? COLLATE NOCASE AND ($COLUMN_TAG_IS_DELETED IS NULL OR $COLUMN_TAG_IS_DELETED = 0)",
                        arrayOf(name),
                        null,
                        null,
                        null
                )
        val tag =
                if (cursor.moveToFirst()) {
                    Tag(
                            id = CursorUtils.getIntSafe(cursor, COLUMN_TAG_ID) ?: 0,
                            name = CursorUtils.getStringSafe(cursor, COLUMN_TAG_NAME) ?: "",
                            uid = CursorUtils.getStringSafe(cursor, COLUMN_TAG_UID) ?: "",
                            createdAt = CursorUtils.getLongSafe(cursor, COLUMN_CREATED_AT) ?: 0L,
                            updatedAt = CursorUtils.getLongSafe(cursor, COLUMN_UPDATED_AT) ?: 0L,
                            syncStatus =
                                    try {
                                        SyncStatus.valueOf(
                                                CursorUtils.getStringSafe(
                                                        cursor,
                                                        COLUMN_TAG_SYNC_STATUS
                                                )
                                                        ?: "PENDING"
                                        )
                                    } catch (e: IllegalArgumentException) {
                                        SyncStatus.PENDING
                                    },
                            isDeleted = (CursorUtils.getIntSafe(cursor, COLUMN_TAG_IS_DELETED)
                                            ?: 0) == 1
                    )
                } else null
        cursor.close()
        return tag
    }

    fun getCategoryByName(name: String): Label? {
        val db = readableDatabase
        val cursor =
                db.query(
                        TABLE_CATEGORIES,
                        null,
                        "$COLUMN_NAME = ? COLLATE NOCASE AND ($COLUMN_IS_DELETED IS NULL OR $COLUMN_IS_DELETED = 0)",
                        arrayOf(name),
                        null,
                        null,
                        null
                )
        val label =
                if (cursor.moveToFirst()) {
                    getLabelFromCursor(cursor)
                } else null
        cursor.close()
        return label
    }

    fun insertTag(tag: Tag) {
        val db = writableDatabase
        val values =
                ContentValues().apply {
                    put(COLUMN_TAG_UID, tag.uid)
                    put(COLUMN_TAG_NAME, tag.name)
                    put(COLUMN_CREATED_AT, tag.createdAt)
                    put(COLUMN_UPDATED_AT, tag.updatedAt)
                    put(COLUMN_TAG_SYNC_STATUS, tag.syncStatus.name)
                    put(COLUMN_TAG_IS_DELETED, if (tag.isDeleted) 1 else 0)
                }
        db.insertWithOnConflict(TABLE_TAGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun insertCategory(label: Label) {
        val db = writableDatabase
        val values =
                ContentValues().apply {
                    put(COLUMN_UID, label.uid)
                    put(COLUMN_NAME, label.name)
                    put(COLUMN_COLOR, label.color)
                    put(COLUMN_CREATED_AT, label.createdAt)
                    put(COLUMN_UPDATED_AT, label.updatedAt)
                    put(COLUMN_SYNC_STATUS, label.syncStatus.name)
                    put(COLUMN_IS_DELETED, 0)
                }
        db.insertWithOnConflict(TABLE_CATEGORIES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun insertComment(postId: String, comment: Comment) {
        val db = writableDatabase
        val values =
                ContentValues().apply {
                    put(COLUMN_COMMENT_REMOTE_ID, comment._id)
                    put(COLUMN_COMMENT_USERNAME, comment.username)
                    put(COLUMN_COMMENT_POST_ID, postId)
                    put(COLUMN_COMMENT_PARENT_ID, comment.parentId)
                    put(COLUMN_COMMENT_TEXT, comment.text)
                    put(COLUMN_COMMENT_CREATED_AT, comment.createdAt)
                    put(
                            COLUMN_SYNC_STATUS,
                            comment.syncStatus.name
                    ) // Use default or passed sync status
                    if (comment.attachments != null) {
                        val jsonArray = JSONArray()
                        comment.attachments.forEach { attachment ->
                            val jsonObject = JSONObject()
                            jsonObject.put("url", attachment.url)
                            jsonObject.put("type", attachment.type)
                            jsonArray.put(jsonObject)
                        }
                        put(COLUMN_COMMENT_ATTACHMENTS, jsonArray.toString())
                    }
                }
        db.insertWithOnConflict(TABLE_COMMENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getCommentsForPost(postId: String): List<Comment> {
        val comments = mutableListOf<Comment>()
        val db = readableDatabase
        val cursor =
                db.query(
                        TABLE_COMMENTS,
                        null,
                        "$COLUMN_COMMENT_POST_ID = ?",
                        arrayOf(postId),
                        null,
                        null,
                        "$COLUMN_COMMENT_CREATED_AT DESC"
                )
        while (cursor.moveToNext()) {
            comments.add(
                    Comment(
                            _id = CursorUtils.getStringSafe(cursor, COLUMN_COMMENT_REMOTE_ID) ?: "",
                            username = CursorUtils.getStringSafe(cursor, COLUMN_COMMENT_USERNAME)
                                            ?: "User",
                            text = CursorUtils.getStringSafe(cursor, COLUMN_COMMENT_TEXT) ?: "",
                            parentId = CursorUtils.getStringSafe(cursor, COLUMN_COMMENT_PARENT_ID),
                            createdAt = CursorUtils.getLongSafe(cursor, COLUMN_COMMENT_CREATED_AT)
                                            ?: 0L,
                            attachments =
                                    try {
                                        val jsonString =
                                                CursorUtils.getStringSafe(
                                                        cursor,
                                                        COLUMN_COMMENT_ATTACHMENTS
                                                )
                                        if (!jsonString.isNullOrEmpty()) {
                                            val jsonArray = JSONArray(jsonString)
                                            val list = mutableListOf<CommentAttachment>()
                                            for (i in 0 until jsonArray.length()) {
                                                val obj = jsonArray.getJSONObject(i)
                                                list.add(
                                                        CommentAttachment(
                                                                url = obj.getString("url"),
                                                                type = obj.getString("type")
                                                        )
                                                )
                                            }
                                            list
                                        } else {
                                            null
                                        }
                                    } catch (e: Exception) {
                                        null
                                    }
                    )
            )
        }
        cursor.close()
        return comments
    }

    fun updateWordTagsAndCategories(wordId: String, tags: List<Tag>, categories: List<Label>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Update Tags
            // First, remove existing tags for this word (if any - simplified approach)
            // Or better, we should have a table linking words and tags.
            // Looking at the schema: TABLE_POST_TAGS links post_id (which is word uid) and tag_id
            // (which is tag uid)

            // Clear existing tags association
            db.delete(TABLE_POST_TAGS, "$COLUMN_PT_POST_ID = ?", arrayOf(wordId))

            tags.forEach { tag ->
                // Ensure tag exists
                val validTag = getTagByName(tag.name)
                val tagUid = validTag?.uid ?: tag.uid
                if (validTag == null) {
                    insertTag(tag)
                }

                // Insert association
                val values =
                        ContentValues().apply {
                            put(COLUMN_PT_UID, UUID.randomUUID().toString())
                            put(COLUMN_PT_POST_ID, wordId)
                            put(COLUMN_PT_TAG_ID, tagUid)
                            put(COLUMN_PT_SYNC_STATUS, SyncStatus.PENDING.name)
                        }
                db.insertWithOnConflict(
                        TABLE_POST_TAGS,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                )
            }

            // Update Categories
            // Schema has TABLE_NOTE_CATEGORY linking category_uid and item_uid
            // Clear existing categories association
            db.delete(TABLE_NOTE_CATEGORY, "$COLUMN_ITEM_UID = ?", arrayOf(wordId))

            categories.forEach { category ->
                // Ensure category exists
                val validCategory = getCategoryByName(category.name)
                val catUid = validCategory?.uid ?: category.uid
                if (validCategory == null) {
                    insertCategory(category)
                }

                // Insert association
                val values =
                        ContentValues().apply {
                            put(COLUMN_UID, UUID.randomUUID().toString())
                            put(COLUMN_CATEGORY_UID, catUid)
                            put(COLUMN_ITEM_UID, wordId)
                            put(COLUMN_CREATED_AT, System.currentTimeMillis())
                            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                            put(COLUMN_SYNC_STATUS, SyncStatus.PENDING.name)
                            put(COLUMN_IS_DELETED, 0)
                        }
                db.insertWithOnConflict(
                        TABLE_NOTE_CATEGORY,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                )
            }

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    fun getAllLabel(callback: (List<Label>) -> Unit) {
        executor.execute {
            val labels = mutableListOf<Label>()
            val db = readableDatabase

            val sql =
                    """
            SELECT c.${COLUMN_ID},
                   c.${COLUMN_UID},
                   c.$COLUMN_NAME,
                   c.$COLUMN_COLOR,
                   c.$COLUMN_CATEGORY_PARENT_UID,
                   c.${COLUMN_SYNC_STATUS},
                   c.${COLUMN_CREATED_AT},
                   c.${COLUMN_UPDATED_AT},
                   c.${COLUMN_IS_DELETED},
                   (SELECT COUNT(*) FROM $TABLE_NOTE_CATEGORY nc WHERE nc.$COLUMN_CATEGORY_UID = c.${COLUMN_UID}) as note_count
            FROM $TABLE_CATEGORIES c 
            WHERE c.${COLUMN_IS_DELETED} IS NULL OR c.${COLUMN_IS_DELETED} = 0
        """.trimIndent()

            val cursor = db.rawQuery(sql, null)

            while (cursor.moveToNext()) {
                labels.add(getLabelFromCursor(cursor))
            }
            cursor.close()
            callback(labels)
        }
    }

    private fun getWordFromCursor(cursor: Cursor): Word {
        return Word(
                id = CursorUtils.getLongSafe(cursor, COLUMN_ID) ?: 0L,
                uid = CursorUtils.getStringSafe(cursor, COLUMN_UID) ?: "",
                word = CursorUtils.getStringSafe(cursor, COLUMN_WORD) ?: "",
                userId = CursorUtils.getStringSafe(cursor, COLUMN_USER_ID) ?: "",
                type = CursorUtils.getStringSafe(cursor, COLUMN_TYPE) ?: "",
                shortMeaning = CursorUtils.getStringSafe(cursor, COLUMN_SHORT_MEANING) ?: "",
                cover = CursorUtils.getStringSafe(cursor, COLUMN_COVER) ?: "",
                details = CursorUtils.getStringSafe(cursor, COLUMN_DETAILS) ?: "",
                isFavorite = (CursorUtils.getIntSafe(cursor, COLUMN_IS_FAVORITE) ?: 0) == 1,
                isDeleted = (CursorUtils.getIntSafe(cursor, COLUMN_IS_DELETED) ?: 0) == 1,
                viewCount = CursorUtils.getIntSafe(cursor, COLUMN_VIEW_COUNT) ?: 0,
                lastVisited = CursorUtils.getLongSafe(cursor, COLUMN_LAST_VIEWED) ?: 0,
                createdAt = CursorUtils.getLongSafe(cursor, COLUMN_CREATED_AT)
                                ?: System.currentTimeMillis(),
                updatedAt = CursorUtils.getLongSafe(cursor, COLUMN_UPDATED_AT)
                                ?: System.currentTimeMillis(),
                syncStatus =
                        try {
                            SyncStatus.valueOf(
                                    CursorUtils.getStringSafe(cursor, COLUMN_SYNC_STATUS)
                                            ?: "PENDING"
                            )
                        } catch (e: IllegalArgumentException) {
                            SyncStatus.PENDING
                        },
                retryCount = CursorUtils.getIntSafe(cursor, COLUMN_RETRY_COUNT) ?: 0,
                lastSyncAttempt = CursorUtils.getLongSafe(cursor, COLUMN_LAST_SYNC_ATTEMPT),
                attachments =
                        try {
                            val jsonString = CursorUtils.getStringSafe(cursor, COLUMN_ATTACHMENTS)
                            if (!jsonString.isNullOrEmpty()) {
                                val jsonArray = JSONArray(jsonString)
                                val list = mutableListOf<CommentAttachment>()
                                for (i in 0 until jsonArray.length()) {
                                    val obj = jsonArray.getJSONObject(i)
                                    list.add(
                                            CommentAttachment(
                                                    url = obj.getString("url"),
                                                    type = obj.getString("type")
                                            )
                                    )
                                }
                                list
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            null
                        }
        )
    }

    private fun getCommentFromCursor(cursor: Cursor): Comment {
        return Comment(
                _id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT_REMOTE_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT_USERNAME)),
                parentId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT_PARENT_ID)),
                text = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT_TEXT)),
                audioUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT_AUDIO_URL)),
                mediaUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT_MEDIA_URL)),
                mediaType =
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT_MEDIA_TYPE)),
                attachments =
                        try {
                            val json =
                                    cursor.getString(
                                            cursor.getColumnIndexOrThrow(COLUMN_COMMENT_ATTACHMENTS)
                                    )
                            if (json != null) {
                                val type = object : TypeToken<List<CommentAttachment>>() {}.type
                                Gson().fromJson<List<CommentAttachment>>(json, type)
                            } else null
                        } catch (e: Exception) {
                            null
                        },
                createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COMMENT_CREATED_AT)),
                updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT))
        )
    }

    private fun getLabelFromCursor(cursor: Cursor): Label {
        return Label(
                id = CursorUtils.getIntSafe(cursor, COLUMN_ID) ?: 0,
                uid = CursorUtils.getStringSafe(cursor, COLUMN_UID) ?: "",
                name = CursorUtils.getStringSafe(cursor, COLUMN_NAME) ?: "",
                color = CursorUtils.getStringSafe(cursor, COLUMN_COLOR) ?: "",
                parentId = CursorUtils.getStringSafe(cursor, COLUMN_CATEGORY_PARENT_UID),
                createdAt = CursorUtils.getLongSafe(cursor, COLUMN_CREATED_AT) ?: 0L,
                updatedAt = CursorUtils.getLongSafe(cursor, COLUMN_UPDATED_AT) ?: 0L,
                associatedNoteCount = CursorUtils.getIntSafe(cursor, "note_count") ?: 0,
                syncStatus =
                        try {
                            SyncStatus.valueOf(
                                    CursorUtils.getStringSafe(cursor, COLUMN_SYNC_STATUS)
                                            ?: "PENDING"
                            )
                        } catch (e: Exception) {
                            SyncStatus.PENDING
                        }
        )
    }
}

data class WordStats(
        val totalWords: Int,
        val favoriteWords: Int,
        val beginnerWords: Int,
        val intermediateWords: Int,
        val advancedWords: Int
)
