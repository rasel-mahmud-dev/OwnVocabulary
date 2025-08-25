package com.rs.ownvocabulary.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.*

data class Word(
    val id: Long = 0,
    val uid: String = UUID.randomUUID().toString(),
    val word: String,
    val type: String = "word",
    val shortMeaning: String = "",
    val details: String = "",
    val examples: String = "",
    val isFavorite: Boolean = false,
    val proficiencyLevel: String = "Beginner",
    val viewCount: Int = 0,
    val lastViewedDaysAgo: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val retryCount: Int = 0,
    val lastSyncAttempt: Long? = null
)

enum class SyncStatus {
    PENDING, IN_PROGRESS, SYNCED, FAILED, DELETED
}

enum class SortOrder {
    CreatedAtAsc, CreatedAtDesc, UpdatedAtAsc, UpdatedAtDesc, WordAsc, WordDesc
}

class WordDatabase private constructor(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    companion object {
        @Volatile
        private var INSTANCE: WordDatabase? = null
        private const val DATABASE_NAME = "words.db"
        private const val DATABASE_VERSION = 2

        // Table and column names
        private const val TABLE_WORDS = "words"
        private const val COLUMN_ID = "id"
        private const val COLUMN_UID = "uid"
        private const val COLUMN_WORD = "word"
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_SHORT_MEANING = "short_meaning"
        private const val COLUMN_DETAILS = "details"
        private const val COLUMN_EXAMPLES = "examples"
        private const val COLUMN_IS_FAVORITE = "is_favorite"
        private const val COLUMN_PROFICIENCY_LEVEL = "proficiency_level"
        private const val COLUMN_VIEW_COUNT = "view_count"
        private const val COLUMN_LAST_VIEWED_DAYS_AGO = "last_viewed_days_ago"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_UPDATED_AT = "updated_at"
        private const val COLUMN_SYNC_STATUS = "sync_status"
        private const val COLUMN_RETRY_COUNT = "retry_count"
        private const val COLUMN_LAST_SYNC_ATTEMPT = "last_sync_attempt"

        fun getInstance(context: Context): WordDatabase {
            return INSTANCE ?: synchronized(this) {
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
                $COLUMN_WORD TEXT NOT NULL,
                $COLUMN_TYPE TEXT DEFAULT 'word',
                $COLUMN_SHORT_MEANING TEXT DEFAULT '',
                $COLUMN_DETAILS TEXT DEFAULT '',
                $COLUMN_EXAMPLES TEXT DEFAULT '',
                $COLUMN_IS_FAVORITE INTEGER DEFAULT 0,
                $COLUMN_PROFICIENCY_LEVEL TEXT DEFAULT 'Beginner',
                $COLUMN_VIEW_COUNT INTEGER DEFAULT 0,
                $COLUMN_LAST_VIEWED_DAYS_AGO INTEGER DEFAULT 0,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_UPDATED_AT INTEGER NOT NULL,
                $COLUMN_SYNC_STATUS TEXT DEFAULT 'PENDING',
                $COLUMN_RETRY_COUNT INTEGER DEFAULT 0,
                $COLUMN_LAST_SYNC_ATTEMPT INTEGER
            )
        """.trimIndent()
        )

        // Create index for better performance
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_word ON $TABLE_WORDS ($COLUMN_WORD)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_uid ON $TABLE_WORDS ($COLUMN_UID)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_updated_at ON $TABLE_WORDS ($COLUMN_UPDATED_AT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades here
        if (oldVersion < newVersion) {
            // Add any schema changes here
        }
    }

    fun getAllWords(
        sortOrder: SortOrder = SortOrder.UpdatedAtDesc,
        favoriteOnly: Boolean = false,
        proficiencyFilter: String? = null,
        callback: (List<Word>) -> Unit
    ) {
        executor.execute {
            val words = mutableListOf<Word>()
            val db = readableDatabase

            var selection = "$COLUMN_SYNC_STATUS != ?"
            val selectionArgs = mutableListOf(SyncStatus.DELETED.name)

            if (favoriteOnly) {
                selection += " AND $COLUMN_IS_FAVORITE = ?"
                selectionArgs.add("1")
            }

            if (!proficiencyFilter.isNullOrEmpty()) {
                selection += " AND $COLUMN_PROFICIENCY_LEVEL = ?"
                selectionArgs.add(proficiencyFilter)
            }

            val orderBy = when (sortOrder) {
                SortOrder.CreatedAtAsc -> "$COLUMN_CREATED_AT ASC"
                SortOrder.CreatedAtDesc -> "$COLUMN_CREATED_AT DESC"
                SortOrder.UpdatedAtAsc -> "$COLUMN_UPDATED_AT ASC"
                SortOrder.UpdatedAtDesc -> "$COLUMN_UPDATED_AT DESC"
                SortOrder.WordAsc -> "$COLUMN_WORD ASC"
                SortOrder.WordDesc -> "$COLUMN_WORD DESC"
            }

            val cursor = db.query(
                TABLE_WORDS,
                null,
                selection,
                selectionArgs.toTypedArray(),
                null,
                null,
                orderBy
            )

            while (cursor.moveToNext()) {
                words.add(getWordFromCursor(cursor))
            }
            cursor.close()
            callback(words)
        }
    }

    fun insertWord(word: Word, callback: (Long) -> Unit = {}) {
        executor.execute {
            val db = writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_UID, word.uid)
                put(COLUMN_WORD, word.word)
                put(COLUMN_TYPE, word.type)
                put(COLUMN_SHORT_MEANING, word.shortMeaning)
                put(COLUMN_DETAILS, word.details)
                put(COLUMN_EXAMPLES, word.examples)
                put(COLUMN_IS_FAVORITE, if (word.isFavorite) 1 else 0)
                put(COLUMN_PROFICIENCY_LEVEL, word.proficiencyLevel)
                put(COLUMN_VIEW_COUNT, word.viewCount)
                put(COLUMN_LAST_VIEWED_DAYS_AGO, word.lastViewedDaysAgo)
                put(COLUMN_CREATED_AT, word.createdAt)
                put(COLUMN_UPDATED_AT, word.updatedAt)
                put(COLUMN_SYNC_STATUS, word.syncStatus.name)
                put(COLUMN_RETRY_COUNT, word.retryCount)
                word.lastSyncAttempt?.let { put(COLUMN_LAST_SYNC_ATTEMPT, it) }
            }
            val id = db.insert(TABLE_WORDS, null, values)
            callback(id)
        }
    }

    suspend fun updateWord(word: Word): Int = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_WORD, word.word)
            put(COLUMN_SHORT_MEANING, word.shortMeaning)
            put(COLUMN_DETAILS, word.details)
            put(COLUMN_EXAMPLES, word.examples)
            put(COLUMN_IS_FAVORITE, if (word.isFavorite) 1 else 0)
            put(COLUMN_PROFICIENCY_LEVEL, word.proficiencyLevel)
            put(COLUMN_VIEW_COUNT, word.viewCount)
            put(COLUMN_LAST_VIEWED_DAYS_AGO, word.lastViewedDaysAgo)
            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
            put(COLUMN_SYNC_STATUS, SyncStatus.PENDING.name)
        }

        db.update(
            TABLE_WORDS,
            values,
            "$COLUMN_UID = ?",
            arrayOf(word.uid)
        )
    }

    suspend fun incrementViewCount(uid: String): Int = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LAST_VIEWED_DAYS_AGO, 0) // Reset to 0 since just viewed
            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
        }

        // Increment view count
        db.execSQL(
            "UPDATE $TABLE_WORDS SET $COLUMN_VIEW_COUNT = $COLUMN_VIEW_COUNT + 1 WHERE $COLUMN_UID = ?",
            arrayOf(uid)
        )

        db.update(
            TABLE_WORDS,
            values,
            "$COLUMN_UID = ?",
            arrayOf(uid)
        )
    }

    suspend fun toggleFavorite(uid: String): Int = withContext(Dispatchers.IO) {
        val db = writableDatabase

        // First get current favorite status
        val cursor = db.query(
            TABLE_WORDS,
            arrayOf(COLUMN_IS_FAVORITE),
            "$COLUMN_UID = ?",
            arrayOf(uid),
            null, null, null
        )

        var newFavoriteStatus = 1
        if (cursor.moveToFirst()) {
            val currentStatus = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_FAVORITE))
            newFavoriteStatus = if (currentStatus == 1) 0 else 1
        }
        cursor.close()

        val values = ContentValues().apply {
            put(COLUMN_IS_FAVORITE, newFavoriteStatus)
            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
            put(COLUMN_SYNC_STATUS, SyncStatus.PENDING.name)
        }

        db.update(
            TABLE_WORDS,
            values,
            "$COLUMN_UID = ?",
            arrayOf(uid)
        )
    }

    fun getWordByUid(uid: String, callback: (Word?) -> Unit) {
        executor.execute {
            val db = readableDatabase
            val cursor = db.query(
                TABLE_WORDS,
                null,
                "$COLUMN_UID = ?",
                arrayOf(uid),
                null, null, null
            )

            val word = if (cursor.moveToFirst()) {
                getWordFromCursor(cursor)
            } else {
                null
            }
            cursor.close()
            callback(word)
        }
    }

    fun searchWords(query: String, callback: (List<Word>) -> Unit) {
        executor.execute {
            val words = mutableListOf<Word>()
            val db = readableDatabase

            val selection = "($COLUMN_WORD LIKE ? OR $COLUMN_SHORT_MEANING LIKE ? OR $COLUMN_DETAILS LIKE ?) AND $COLUMN_SYNC_STATUS != ?"
            val selectionArgs = arrayOf("%$query%", "%$query%", "%$query%", SyncStatus.DELETED.name)

            val cursor = db.query(
                TABLE_WORDS,
                null,
                selection,
                selectionArgs,
                null, null,
                "$COLUMN_UPDATED_AT DESC"
            )

            while (cursor.moveToNext()) {
                words.add(getWordFromCursor(cursor))
            }
            cursor.close()
            callback(words)
        }
    }

    suspend fun getUnsyncedWords(): List<Word> = withContext(Dispatchers.IO) {
        val words = mutableListOf<Word>()
        val db = readableDatabase

        db.query(
            TABLE_WORDS,
            null,
            "$COLUMN_SYNC_STATUS != ?",
            arrayOf(SyncStatus.SYNCED.name),
            null, null,
            "$COLUMN_UPDATED_AT DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                words.add(getWordFromCursor(cursor))
            }
        }

        return@withContext words
    }

    fun updateWordSyncStatus(
        uid: String,
        status: SyncStatus,
        retryCount: Int = 0,
        lastSyncAttempt: Long? = null,
        callback: (Int) -> Unit = {}
    ) {
        executor.execute {
            val db = writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_SYNC_STATUS, status.name)
                put(COLUMN_RETRY_COUNT, retryCount)
                if (lastSyncAttempt != null) {
                    put(COLUMN_LAST_SYNC_ATTEMPT, lastSyncAttempt)
                }
                if (status == SyncStatus.SYNCED) {
                    put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                }
            }

            val rowsAffected = db.update(
                TABLE_WORDS,
                values,
                "$COLUMN_UID = ?",
                arrayOf(uid)
            )
            callback(rowsAffected)
        }
    }

    suspend fun deleteWord(uid: String): Int = withContext(Dispatchers.IO) {
        val db = writableDatabase
        try {
            val values = ContentValues().apply {
                put(COLUMN_SYNC_STATUS, SyncStatus.DELETED.name)
                put(COLUMN_UPDATED_AT, System.currentTimeMillis())
            }

            val rowsAffected = db.update(
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

    fun deleteWordHard(uid: String) {
        executor.execute {
            val db = writableDatabase
            try {
                db.delete(
                    TABLE_WORDS,
                    "$COLUMN_UID = ?",
                    arrayOf(uid)
                )
            } catch (e: Exception) {
                println("Failed to delete word: ${e.message}")
            }
        }
    }

    // Analytics functions
    suspend fun getWordStats(): WordStats = withContext(Dispatchers.IO) {
        val db = readableDatabase

        // Total words
        val totalCursor = db.query(
            TABLE_WORDS,
            arrayOf("COUNT(*) as total"),
            "$COLUMN_SYNC_STATUS != ?",
            arrayOf(SyncStatus.DELETED.name),
            null, null, null
        )
        val totalWords = if (totalCursor.moveToFirst()) totalCursor.getInt(0) else 0
        totalCursor.close()

        // Favorite words
        val favoriteCursor = db.query(
            TABLE_WORDS,
            arrayOf("COUNT(*) as favorites"),
            "$COLUMN_IS_FAVORITE = ? AND $COLUMN_SYNC_STATUS != ?",
            arrayOf("1", SyncStatus.DELETED.name),
            null, null, null
        )
        val favoriteWords = if (favoriteCursor.moveToFirst()) favoriteCursor.getInt(0) else 0
        favoriteCursor.close()

        // Proficiency breakdown
        val proficiencyCursor = db.query(
            TABLE_WORDS,
            arrayOf("$COLUMN_PROFICIENCY_LEVEL", "COUNT(*) as count"),
            "$COLUMN_SYNC_STATUS != ?",
            arrayOf(SyncStatus.DELETED.name),
            COLUMN_PROFICIENCY_LEVEL,
            null,
            null
        )

        val proficiencyMap = mutableMapOf<String, Int>()
        while (proficiencyCursor.moveToNext()) {
            val level = proficiencyCursor.getString(0)
            val count = proficiencyCursor.getInt(1)
            proficiencyMap[level] = count
        }
        proficiencyCursor.close()

        WordStats(
            totalWords = totalWords,
            favoriteWords = favoriteWords,
            beginnerWords = proficiencyMap["Beginner"] ?: 0,
            intermediateWords = proficiencyMap["Intermediate"] ?: 0,
            advancedWords = proficiencyMap["Advanced"] ?: 0
        )
    }

    private fun getWordFromCursor(cursor: Cursor): Word {
        return Word(
            id = CursorUtils.getLongSafe(cursor, COLUMN_ID) ?: 0L,
            uid = CursorUtils.getStringSafe(cursor, COLUMN_UID) ?: "",
            word = CursorUtils.getStringSafe(cursor, COLUMN_WORD) ?: "",
            type = CursorUtils.getStringSafe(cursor, COLUMN_TYPE) ?: "",
            shortMeaning = CursorUtils.getStringSafe(cursor, COLUMN_SHORT_MEANING) ?: "",
            details = CursorUtils.getStringSafe(cursor, COLUMN_DETAILS) ?: "",
            examples = CursorUtils.getStringSafe(cursor, COLUMN_EXAMPLES) ?: "",
            isFavorite = (CursorUtils.getIntSafe(cursor, COLUMN_IS_FAVORITE) ?: 0) == 1,
            proficiencyLevel = CursorUtils.getStringSafe(cursor, COLUMN_PROFICIENCY_LEVEL) ?: "Beginner",
            viewCount = CursorUtils.getIntSafe(cursor, COLUMN_VIEW_COUNT) ?: 0,
            lastViewedDaysAgo = CursorUtils.getIntSafe(cursor, COLUMN_LAST_VIEWED_DAYS_AGO) ?: 0,
            createdAt = CursorUtils.getLongSafe(cursor, COLUMN_CREATED_AT) ?: System.currentTimeMillis(),
            updatedAt = CursorUtils.getLongSafe(cursor, COLUMN_UPDATED_AT) ?: System.currentTimeMillis(),
            syncStatus = try {
                SyncStatus.valueOf(CursorUtils.getStringSafe(cursor, COLUMN_SYNC_STATUS) ?: "PENDING")
            } catch (e: IllegalArgumentException) {
                SyncStatus.PENDING
            },
            retryCount = CursorUtils.getIntSafe(cursor, COLUMN_RETRY_COUNT) ?: 0,
            lastSyncAttempt = CursorUtils.getLongSafe(cursor, COLUMN_LAST_SYNC_ATTEMPT)
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