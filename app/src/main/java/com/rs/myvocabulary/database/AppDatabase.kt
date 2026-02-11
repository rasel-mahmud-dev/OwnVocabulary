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
        val attachments: List<CommentAttachment>? = null,
        val comments: List<Comment>? = null
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
        val isDeleted: Boolean? = null,
        var categories: List<Label>? = null,
        val attachments: List<CommentAttachment>? = null,
        val comments: List<Comment>? = null
)

enum class SyncStatus {
        PENDING,
        IN_PROGRESS,
        SYNCED,
        FAILED,
        DELETED
}

class WordDatabase private constructor(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        companion object {
                @Volatile private var INSTANCE: WordDatabase? = null
                private const val DATABASE_NAME = "words.db"
                private const val DATABASE_VERSION = 6

                // Table and column names
                private const val TABLE_WORDS = "words"
                private const val TABLE_CATEGORIES = "categories" // New table

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
                private const val COLUMN_VIEW_COUNT = "view_count"
                private const val COLUMN_LAST_VIEWED = "last_viewed"
                private const val COLUMN_CREATED_AT = "created_at"
                private const val COLUMN_UPDATED_AT = "updated_at"
                private const val COLUMN_IS_DELETED = "is_deleted"
                private const val COLUMN_SYNC_STATUS = "sync_status"
                private const val COLUMN_RETRY_COUNT = "retry_count"
                private const val COLUMN_LAST_SYNC_ATTEMPT = "last_sync_attempt"

                // Categories Table Columns
                private const val COLUMN_NAME = "name"
                private const val COLUMN_COLOR = "color"

                const val COLUMN_ATTACHMENTS = "attachments"
                const val COLUMN_CATEGORIES_JSON = "categories_json"
                const val COLUMN_COMMENTS_JSON = "comments_json"

                fun getInstance(context: Context): WordDatabase {
                        return INSTANCE
                                ?: synchronized(this) {
                                        INSTANCE
                                                ?: WordDatabase(context.applicationContext).also {
                                                        INSTANCE = it
                                                }
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
                $COLUMN_CATEGORIES_JSON TEXT,
                $COLUMN_COMMENTS_JSON TEXT,
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
            CREATE TABLE IF NOT EXISTS $TABLE_CATEGORIES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_UID TEXT UNIQUE NOT NULL,
                $COLUMN_NAME TEXT NOT NULL UNIQUE COLLATE NOCASE,
                $COLUMN_COLOR TEXT DEFAULT '#FF0000',
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_UPDATED_AT INTEGER NOT NULL,
                $COLUMN_IS_DELETED INTEGER DEFAULT 0,
                $COLUMN_SYNC_STATUS TEXT DEFAULT 'PENDING'
            )
        """.trimIndent()
                )

                db.execSQL("""
            )
        """.trimIndent())

                db.execSQL("CREATE INDEX IF NOT EXISTS idx_word ON $TABLE_WORDS ($COLUMN_WORD)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_uid ON $TABLE_WORDS ($COLUMN_UID)")
                db.execSQL(
                        "CREATE INDEX IF NOT EXISTS idx_updated_at ON $TABLE_WORDS ($COLUMN_UPDATED_AT)"
                )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
                if (oldVersion < 3) {
                        onCreate(db)
                }

                if (oldVersion < 5) {
                        try {
                                // Add new JSON columns to words table
                                db.execSQL(
                                        "ALTER TABLE $TABLE_WORDS ADD COLUMN $COLUMN_CATEGORIES_JSON TEXT"
                                )
                                db.execSQL(
                                        "ALTER TABLE $TABLE_WORDS ADD COLUMN $COLUMN_COMMENTS_JSON TEXT"
                                )

                                val wordsCursor =
                                        db.query(
                                                TABLE_WORDS,
                                                arrayOf(COLUMN_UID),
                                                null,
                                                null,
                                                null,
                                                null,
                                                null
                                        )
                                while (wordsCursor.moveToNext()) {
                                        val wordUid = wordsCursor.getString(0)

                                        // Get categories for this word
                                        val categoriesCursor =
                                                db.rawQuery(
                                                        """
                SELECT c.* FROM category c
                INNER JOIN note_category nc ON c.uid = nc.category_uid
                WHERE nc.item_uid = ? AND c.is_deleted = 0
            """,
                                                        arrayOf(wordUid)
                                                )

                                        val categories = mutableListOf<Label>()
                                        while (categoriesCursor.moveToNext()) {
                                                categories.add(
                                                        Label(
                                                                id =
                                                                        categoriesCursor.getInt(
                                                                                categoriesCursor
                                                                                        .getColumnIndexOrThrow(
                                                                                                COLUMN_ID
                                                                                        )
                                                                        ),
                                                                uid =
                                                                        categoriesCursor.getString(
                                                                                categoriesCursor
                                                                                        .getColumnIndexOrThrow(
                                                                                                COLUMN_UID
                                                                                        )
                                                                        ),
                                                                name =
                                                                        categoriesCursor.getString(
                                                                                categoriesCursor
                                                                                        .getColumnIndexOrThrow(
                                                                                                "name"
                                                                                        )
                                                                        ),
                                                                color =
                                                                        categoriesCursor.getString(
                                                                                categoriesCursor
                                                                                        .getColumnIndexOrThrow(
                                                                                                "color"
                                                                                        )
                                                                        ),
                                                                createdAt =
                                                                        categoriesCursor.getLong(
                                                                                categoriesCursor
                                                                                        .getColumnIndexOrThrow(
                                                                                                COLUMN_CREATED_AT
                                                                                        )
                                                                        ),
                                                                updatedAt =
                                                                        categoriesCursor.getLong(
                                                                                categoriesCursor
                                                                                        .getColumnIndexOrThrow(
                                                                                                COLUMN_UPDATED_AT
                                                                                        )
                                                                        )
                                                        )
                                                )
                                        }
                                        categoriesCursor.close()

                                        // Get comments for this word
                                        val commentsCursor =
                                                db.rawQuery(
                                                        """
                SELECT * FROM comments
                WHERE post_id = ? AND is_deleted = 0
            """,
                                                        arrayOf(wordUid)
                                                )

                                        val comments = mutableListOf<Comment>()
                                        while (commentsCursor.moveToNext()) {
                                                comments.add(
                                                        Comment(
                                                                _id =
                                                                        commentsCursor.getString(
                                                                                commentsCursor
                                                                                        .getColumnIndexOrThrow(
                                                                                                "uid"
                                                                                        )
                                                                        ),
                                                                username =
                                                                        commentsCursor.getString(
                                                                                commentsCursor
                                                                                        .getColumnIndexOrThrow(
                                                                                                "username"
                                                                                        )
                                                                        ),
                                                                text =
                                                                        commentsCursor.getString(
                                                                                commentsCursor
                                                                                        .getColumnIndexOrThrow(
                                                                                                "text"
                                                                                        )
                                                                        ),
                                                                parentId =
                                                                        commentsCursor.getString(
                                                                                commentsCursor
                                                                                        .getColumnIndexOrThrow(
                                                                                                "parent_id"
                                                                                        )
                                                                        ),
                                                                createdAt =
                                                                        commentsCursor.getLong(
                                                                                commentsCursor
                                                                                        .getColumnIndexOrThrow(
                                                                                                "created_at"
                                                                                        )
                                                                        ),
                                                                updatedAt =
                                                                        commentsCursor.getLong(
                                                                                commentsCursor
                                                                                        .getColumnIndexOrThrow(
                                                                                                COLUMN_UPDATED_AT
                                                                                        )
                                                                        )
                                                        )
                                                )
                                        }
                                        commentsCursor.close()

                                        // Update word with JSON data
                                        val values =
                                                ContentValues().apply {
                                                        if (categories.isNotEmpty()) {
                                                                put(
                                                                        COLUMN_CATEGORIES_JSON,
                                                                        Gson().toJson(categories)
                                                                )
                                                        }
                                                        if (comments.isNotEmpty()) {
                                                                put(
                                                                        COLUMN_COMMENTS_JSON,
                                                                        Gson().toJson(comments)
                                                                )
                                                        }
                                                }
                                        if (values.size() > 0) {
                                                db.update(
                                                        TABLE_WORDS,
                                                        values,
                                                        "$COLUMN_UID = ?",
                                                        arrayOf(wordUid)
                                                )
                                        }
                                }
                                wordsCursor.close()

                                Log.d("Database", "Migration to version 5 completed successfully")
                        } catch (e: Exception) {
                                Log.e("Database", "Migration to version 5 failed", e)
                                e.printStackTrace()
                        }
                }

                if (oldVersion < 6) {
                        try {
                                db.execSQL(
                                        """
                    CREATE TABLE IF NOT EXISTS $TABLE_CATEGORIES (
                        $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                        $COLUMN_UID TEXT UNIQUE NOT NULL,
                        $COLUMN_NAME TEXT NOT NULL,
                        $COLUMN_COLOR TEXT DEFAULT '#FF0000',
                        $COLUMN_CREATED_AT INTEGER NOT NULL,
                        $COLUMN_UPDATED_AT INTEGER NOT NULL,
                        $COLUMN_IS_DELETED INTEGER DEFAULT 0,
                        $COLUMN_SYNC_STATUS TEXT DEFAULT 'PENDING'
                    )
                """.trimIndent()
                                )
                        } catch (e: Exception) {
                                Log.e("Database", "Migration to version 6 failed", e)
                                e.printStackTrace()
                        }
                }
        }

        // FOR BACKUP/RESTORE
        fun clearAllData() {
                val db = writableDatabase
                db.beginTransaction()
                try {
                        db.delete(TABLE_WORDS, null, null)
                        db.setTransactionSuccessful()
                } finally {
                        db.endTransaction()
                }
        }

        fun insertWords(words: List<Word>) {
                val db = writableDatabase
                db.beginTransaction()
                try {
                        words.forEach { insertWord(it) }
                        db.setTransactionSuccessful()
                } finally {
                        db.endTransaction()
                }
        }

        // CATEGORIES OPERATIONS

        fun insertCategory(category: Label): Long {
                val db = writableDatabase

                // Check if category with this name already exists
                val cursor =
                        db.query(
                                TABLE_CATEGORIES,
                                arrayOf(COLUMN_ID),
                                "$COLUMN_NAME = ? AND $COLUMN_IS_DELETED = 0",
                                arrayOf(category.name),
                                null,
                                null,
                                null
                        )

                if (cursor.moveToFirst()) {
                        val id = cursor.getLong(0)
                        cursor.close()
                        return id
                }
                cursor.close()

                val values =
                        ContentValues().apply {
                                put(COLUMN_UID, category.uid)
                                put(COLUMN_NAME, category.name)
                                put(COLUMN_COLOR, category.color)
                                put(COLUMN_CREATED_AT, category.createdAt)
                                put(COLUMN_UPDATED_AT, category.updatedAt)
                                put(COLUMN_IS_DELETED, if (category.isDeleted) 1 else 0)
                                put(COLUMN_SYNC_STATUS, category.syncStatus.name)
                        }
                return db.insertWithOnConflict(
                        TABLE_CATEGORIES,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_IGNORE // Skip if name exists (though checked above)
                )
        }

        fun deleteCategory(uid: String) {
                val db = writableDatabase
                val values =
                        ContentValues().apply {
                                put(COLUMN_IS_DELETED, 1)
                                put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                                put(COLUMN_SYNC_STATUS, SyncStatus.PENDING.name)
                        }
                db.update(TABLE_CATEGORIES, values, "$COLUMN_UID = ?", arrayOf(uid))
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
                        categories.add(
                                Label(
                                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                                        uid =
                                                cursor.getString(
                                                        cursor.getColumnIndexOrThrow(COLUMN_UID)
                                                ),
                                        name =
                                                cursor.getString(
                                                        cursor.getColumnIndexOrThrow(COLUMN_NAME)
                                                ),
                                        color =
                                                cursor.getString(
                                                        cursor.getColumnIndexOrThrow(COLUMN_COLOR)
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
                                        isDeleted =
                                                cursor.getInt(
                                                        cursor.getColumnIndexOrThrow(
                                                                COLUMN_IS_DELETED
                                                        )
                                                ) == 1,
                                        syncStatus =
                                                SyncStatus.valueOf(
                                                        cursor.getString(
                                                                cursor.getColumnIndexOrThrow(
                                                                        COLUMN_SYNC_STATUS
                                                                )
                                                        )
                                                )
                                )
                        )
                }
                cursor.close()
                return categories
        }

        // FOR BACKUP/RESTORE
        fun getAllWords(): List<Word> {
                val words = mutableListOf<Word>()
                val db = readableDatabase
                val cursor =
                        db.query(
                                TABLE_WORDS,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "${COLUMN_CREATED_AT} DESC"
                        )

                while (cursor.moveToNext()) {
                        words.add(getWordFromCursor(cursor))
                }
                cursor.close()
                return words
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
                                        currentStatus =
                                                cursor.getInt(
                                                        cursor.getColumnIndexOrThrow(
                                                                COLUMN_IS_FAVORITE
                                                        )
                                                )
                                }
                                cursor.close()

                                val newStatus = if (currentStatus == 0) 1 else 0
                                val values =
                                        ContentValues().apply {
                                                put(COLUMN_IS_FAVORITE, newStatus)
                                                put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                                                put(
                                                        COLUMN_SYNC_STATUS,
                                                        SyncStatus.PENDING.name
                                                ) // Mark as pending sync
                                        }

                                val rowsUpdated =
                                        db.update(
                                                TABLE_WORDS,
                                                values,
                                                "${COLUMN_UID} = ?",
                                                arrayOf(uid)
                                        )

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
                                if (selectionParts.isEmpty()) null
                                else selectionParts.joinToString(" AND ")
                        val selectionArgs =
                                if (selectionArgsList.isEmpty()) null
                                else selectionArgsList.toTypedArray()

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
                                db.rawQuery(
                                        query,
                                        arrayOf(SyncStatus.DELETED.name, thirtyDaysAgo.toString())
                                )

                        while (cursor.moveToNext()) {
                                words.add(getWordFromCursor(cursor))
                        }
                        cursor.close()
                        callback(words)
                }
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
                                                word.shortMeaning?.let {
                                                        put(COLUMN_SHORT_MEANING, it)
                                                }
                                                word.details?.let { put(COLUMN_DETAILS, it) }
                                                word.isFavorite?.let {
                                                        put(COLUMN_IS_FAVORITE, if (it) 1 else 0)
                                                }
                                                word.viewCount?.let { put(COLUMN_VIEW_COUNT, it) }
                                                word.lastVisited?.let {
                                                        put(COLUMN_LAST_VIEWED, it)
                                                }
                                                put(COLUMN_SYNC_STATUS, SyncStatus.SYNCED.name)
                                                word.retryCount?.let { put(COLUMN_RETRY_COUNT, it) }
                                                word.lastSyncAttempt?.let {
                                                        put(COLUMN_LAST_SYNC_ATTEMPT, it)
                                                }
                                                word.isDeleted?.let {
                                                        put(COLUMN_IS_DELETED, if (it) 1 else 0)
                                                }

                                                val now = System.currentTimeMillis()
                                                if (!exists) {
                                                        put(
                                                                COLUMN_CREATED_AT,
                                                                word.createdAt ?: now
                                                        )
                                                }
                                                put(COLUMN_UPDATED_AT, word.updatedAt ?: now)
                                                // Handle JSON fields
                                                word.attachments?.let {
                                                        put(COLUMN_ATTACHMENTS, Gson().toJson(it))
                                                }
                                                word.categories?.let {
                                                        put(
                                                                COLUMN_CATEGORIES_JSON,
                                                                Gson().toJson(it)
                                                        )
                                                }
                                                word.comments?.let {
                                                        put(COLUMN_COMMENTS_JSON, Gson().toJson(it))
                                                }
                                        }

                                if (exists) {
                                        db.update(
                                                TABLE_WORDS,
                                                values,
                                                "$COLUMN_UID = ?",
                                                arrayOf(word.uid)
                                        )
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

        suspend fun updatePartial(partialWord: WordPartial): Int =
                withContext(Dispatchers.IO) {
                        val db = writableDatabase
                        val values =
                                ContentValues().apply {
                                        partialWord.word?.let { put(COLUMN_WORD, it) }
                                        partialWord.type?.let { put(COLUMN_TYPE, it) }
                                        partialWord.userId?.let { put(COLUMN_USER_ID, it) }
                                        partialWord.shortMeaning?.let {
                                                put(COLUMN_SHORT_MEANING, it)
                                        }
                                        partialWord.details?.let { put(COLUMN_DETAILS, it) }
                                        partialWord.isFavorite?.let {
                                                put(COLUMN_IS_FAVORITE, if (it) 1 else 0)
                                        }
                                        partialWord.viewCount?.let { put(COLUMN_VIEW_COUNT, it) }
                                        partialWord.lastVisited?.let { put(COLUMN_LAST_VIEWED, it) }
                                        partialWord.syncStatus?.let {
                                                put(COLUMN_SYNC_STATUS, it.name)
                                        }
                                        partialWord.retryCount?.let { put(COLUMN_RETRY_COUNT, it) }
                                        partialWord.lastSyncAttempt?.let {
                                                put(COLUMN_LAST_SYNC_ATTEMPT, it)
                                        }

                                        partialWord.categories?.let { categories ->
                                                // Ensure categories are in master table
                                                categories.forEach { insertCategory(it) }
                                                put(
                                                        COLUMN_CATEGORIES_JSON,
                                                        Gson().toJson(categories)
                                                )
                                        }

                                        // Always update these fields
                                        put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                                }

                        println("uid ${partialWord.uid}")

                        // Only proceed if there are values to update (excluding the always-updated
                        // fields)
                        if (values.size() > 1) { // More than just updatedAt
                                db.update(
                                        TABLE_WORDS,
                                        values,
                                        "$COLUMN_UID = ?",
                                        arrayOf(partialWord.uid)
                                )
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

        fun getWordByUidSync(uid: String): Word? {
                val db = readableDatabase
                db.query(TABLE_WORDS, null, "$COLUMN_UID = ?", arrayOf(uid), null, null, null)
                        .use { cursor ->
                                if (cursor.moveToFirst()) {
                                        return getWordFromCursor(cursor)
                                }
                        }
                return null
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

                                println("word_________ $word")
                                callback(word)
                        }
                        .start()
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
                                // Handle categories if present
                                word.categories?.let {
                                        put(COLUMN_CATEGORIES_JSON, Gson().toJson(it))
                                }
                                word.comments?.let { put(COLUMN_COMMENTS_JSON, Gson().toJson(it)) }
                        }

                return db.insertWithOnConflict(
                        TABLE_WORDS,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                )
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

                        val rowsAffected =
                                db.update(TABLE_WORDS, values, "$COLUMN_UID = ?", arrayOf(uid))
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
                                        db.update(
                                                TABLE_WORDS,
                                                values,
                                                "$COLUMN_UID = ?",
                                                arrayOf(uid)
                                        )

                                return@withContext rowsAffected
                        } catch (e: Exception) {
                                throw e
                        }
                }

        fun insertComment(wordUid: String, comment: Comment) {
                val word = getWordByUidSync(wordUid) ?: return
                val updatedComments = (word.comments ?: emptyList()).toMutableList()
                updatedComments.add(comment)

                val db = writableDatabase
                val values =
                        ContentValues().apply {
                                put(COLUMN_COMMENTS_JSON, Gson().toJson(updatedComments))
                                put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                                put(COLUMN_SYNC_STATUS, SyncStatus.PENDING.name)
                        }
                db.update(TABLE_WORDS, values, "$COLUMN_UID = ?", arrayOf(wordUid))
        }
        fun insertMultipleComments(wordUid: String, newComments: List<Comment>) {
                val word = getWordByUidSync(wordUid) ?: return
                val updatedComments = (word.comments ?: emptyList()).toMutableList()
                updatedComments.addAll(newComments)

                val db = writableDatabase
                val values =
                        ContentValues().apply {
                                put(COLUMN_COMMENTS_JSON, Gson().toJson(updatedComments))
                                put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                                put(COLUMN_SYNC_STATUS, SyncStatus.PENDING.name)
                        }
                db.update(TABLE_WORDS, values, "$COLUMN_UID = ?", arrayOf(wordUid))
        }

        fun deletePostComment(commentId: String, wordUid: String) {
                val word = getWordByUidSync(wordUid) ?: return
                val updatedComments = (word.comments ?: emptyList()).filter { it._id != commentId }

                val db = writableDatabase
                val values =
                        ContentValues().apply {
                                put(COLUMN_COMMENTS_JSON, Gson().toJson(updatedComments))
                                put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                                put(COLUMN_SYNC_STATUS, SyncStatus.PENDING.name)
                        }
                db.update(TABLE_WORDS, values, "$COLUMN_UID = ?", arrayOf(wordUid))
        }

        fun updateWordCategories(wordUid: String, categories: List<Label>) {
                val db = writableDatabase

                // Ensure all categories are inserted/updated in the master table
                categories.forEach { insertCategory(it) }

                val values =
                        ContentValues().apply {
                                put(COLUMN_CATEGORIES_JSON, Gson().toJson(categories))
                                put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                                put(COLUMN_SYNC_STATUS, SyncStatus.PENDING.name)
                        }
                db.update(TABLE_WORDS, values, "$COLUMN_UID = ?", arrayOf(wordUid))
        }

        private fun getWordFromCursor(cursor: Cursor): Word {
                return Word(
                        id = CursorUtils.getLongSafe(cursor, COLUMN_ID) ?: 0L,
                        uid = CursorUtils.getStringSafe(cursor, COLUMN_UID) ?: "",
                        word = CursorUtils.getStringSafe(cursor, COLUMN_WORD) ?: "",
                        userId = CursorUtils.getStringSafe(cursor, COLUMN_USER_ID) ?: "",
                        type = CursorUtils.getStringSafe(cursor, COLUMN_TYPE) ?: "",
                        shortMeaning = CursorUtils.getStringSafe(cursor, COLUMN_SHORT_MEANING)
                                        ?: "",
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
                                                CursorUtils.getStringSafe(
                                                        cursor,
                                                        COLUMN_SYNC_STATUS
                                                )
                                                        ?: "PENDING"
                                        )
                                } catch (e: IllegalArgumentException) {
                                        SyncStatus.PENDING
                                },
                        retryCount = CursorUtils.getIntSafe(cursor, COLUMN_RETRY_COUNT) ?: 0,
                        lastSyncAttempt = CursorUtils.getLongSafe(cursor, COLUMN_LAST_SYNC_ATTEMPT),
                        attachments =
                                try {
                                        val jsonString =
                                                CursorUtils.getStringSafe(
                                                        cursor,
                                                        COLUMN_ATTACHMENTS
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
                                },
                        categories =
                                try {
                                        val jsonString =
                                                CursorUtils.getStringSafe(
                                                        cursor,
                                                        COLUMN_CATEGORIES_JSON
                                                )
                                        if (!jsonString.isNullOrEmpty()) {
                                                val type = object : TypeToken<List<Label>>() {}.type
                                                Gson().fromJson<List<Label>>(jsonString, type)
                                        } else {
                                                null
                                        }
                                } catch (e: Exception) {
                                        null
                                },
                        comments =
                                try {
                                        val jsonString =
                                                CursorUtils.getStringSafe(
                                                        cursor,
                                                        COLUMN_COMMENTS_JSON
                                                )
                                        if (!jsonString.isNullOrEmpty()) {
                                                val type =
                                                        object : TypeToken<List<Comment>>() {}.type
                                                Gson().fromJson<List<Comment>>(jsonString, type)
                                        } else {
                                                null
                                        }
                                } catch (e: Exception) {
                                        null
                                },
                )
        }
}
