package com.rs.ownvocabulary.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.concurrent.Executors

data class AIResponseItem(
    val id: Long = 0,
    val uid: String = System.currentTimeMillis().toString(),
    val wordId: String = "",
    val input: String = "",
    val output: String = "",
    var detail: String = "",
    var synonym: String = "",
    var antonym: String = "",
    var exampleSentences: String = "",
    var shortMeaning: String = "",
    var userId: String = "",
    val type: String = "word",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val retryCount: Int = 0,
    val lastSyncAttempt: Long? = null
)

class AIResponseDatabase private constructor(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    companion object {
        @Volatile
        private var INSTANCE: AIResponseDatabase? = null
        private const val DATABASE_NAME = "ai_response_db.db"
        private const val DATABASE_VERSION = 3

        private const val TABLE_AI_RESPONSES = "ai_responses"

        private const val COLUMN_ID = "id"
        private const val COLUMN_UID = "uid"
        private const val COLUMN_INPUT = "input"
        private const val COLUMN_WORD_ID = "word_id"
        private const val COLUMN_OUTPUT = "output"
        private const val COLUMN_DETAIL = "detail"
        private const val COLUMN_SYNONYM = "synonym"
        private const val COLUMN_ANTONYM = "antonym"
        private const val COLUMN_EXAMPLE_SENTENCES = "example_sentences"
        private const val COLUMN_SHORT_MEANING = "short_meaning"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_UPDATED_AT = "updated_at"
        private const val COLUMN_SYNC_STATUS = "sync_status"
        private const val COLUMN_RETRY_COUNT = "retry_count"
        private const val COLUMN_LAST_SYNC_ATTEMPT = "last_sync_attempt"

        fun getInstance(context: Context): AIResponseDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AIResponseDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val executor = Executors.newFixedThreadPool(4)

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_AI_RESPONSES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_UID TEXT UNIQUE NOT NULL,
                $COLUMN_WORD_ID TEXT UNIQUE NOT NULL,
                $COLUMN_INPUT TEXT NOT NULL,
                $COLUMN_OUTPUT TEXT NOT NULL,
                $COLUMN_DETAIL TEXT DEFAULT '',
                $COLUMN_SYNONYM TEXT DEFAULT '',
                $COLUMN_ANTONYM TEXT DEFAULT '',
                $COLUMN_EXAMPLE_SENTENCES TEXT DEFAULT '',
                $COLUMN_SHORT_MEANING TEXT DEFAULT '',
                $COLUMN_USER_ID TEXT NOT NULL,
                $COLUMN_TYPE TEXT DEFAULT 'word',
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_UPDATED_AT INTEGER NOT NULL,
                $COLUMN_SYNC_STATUS TEXT DEFAULT 'PENDING',
                $COLUMN_RETRY_COUNT INTEGER DEFAULT 0,
                $COLUMN_LAST_SYNC_ATTEMPT INTEGER
            )
        """.trimIndent()
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_uid ON $TABLE_AI_RESPONSES ($COLUMN_UID)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_user_id ON $TABLE_AI_RESPONSES ($COLUMN_USER_ID)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_updated_at ON $TABLE_AI_RESPONSES ($COLUMN_UPDATED_AT)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_sync_status ON $TABLE_AI_RESPONSES ($COLUMN_SYNC_STATUS)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            try {
                // Add new columns if they don't exist
                db.execSQL("ALTER TABLE $TABLE_AI_RESPONSES ADD COLUMN $COLUMN_DETAIL TEXT DEFAULT ''")
            } catch (e: Exception) {
                Log.d("AIResponseDatabase", "Column $COLUMN_DETAIL may already exist")
            }

            try {
                db.execSQL("ALTER TABLE $TABLE_AI_RESPONSES ADD COLUMN $COLUMN_SYNONYM TEXT DEFAULT ''")
            } catch (e: Exception) {
                Log.d("AIResponseDatabase", "Column $COLUMN_SYNONYM may already exist")
            }

            try {
                db.execSQL("ALTER TABLE $TABLE_AI_RESPONSES ADD COLUMN $COLUMN_ANTONYM TEXT DEFAULT ''")
            } catch (e: Exception) {
                Log.d("AIResponseDatabase", "Column $COLUMN_ANTONYM may already exist")
            }

            try {
                db.execSQL("ALTER TABLE $TABLE_AI_RESPONSES ADD COLUMN $COLUMN_EXAMPLE_SENTENCES TEXT DEFAULT ''")
            } catch (e: Exception) {
                Log.d("AIResponseDatabase", "Column $COLUMN_EXAMPLE_SENTENCES may already exist")
            }

            try {
                db.execSQL("ALTER TABLE $TABLE_AI_RESPONSES ADD COLUMN $COLUMN_SHORT_MEANING TEXT DEFAULT ''")
            } catch (e: Exception) {
                Log.d("AIResponseDatabase", "Column $COLUMN_SHORT_MEANING may already exist")
            }
        }
    }

    fun getAllByInput(
        input: String,
        callback: (List<AIResponseItem>) -> Unit
    ) {
        executor.execute {
            val items = mutableListOf<AIResponseItem>()
            val db = readableDatabase

            val selection = "$COLUMN_INPUT = ?"
            val selectionArgs = arrayOf(input)
            val orderBy = "$COLUMN_UPDATED_AT ASC"

            val cursor = db.query(
                TABLE_AI_RESPONSES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
            )

            while (cursor.moveToNext()) {
                items.add(getItemFromCursor(cursor))
            }
            println("item len ${items.size}")
            cursor.close()
            callback(items)
        }
    }

    fun getAllByUserId(
        userId: String,
        callback: (List<AIResponseItem>) -> Unit
    ) {
        executor.execute {
            val items = mutableListOf<AIResponseItem>()
            val db = readableDatabase

            val selection = "$COLUMN_USER_ID = ?"
            val selectionArgs = arrayOf(userId)
            val orderBy = "$COLUMN_UPDATED_AT DESC"

            val cursor = db.query(
                TABLE_AI_RESPONSES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
            )

            while (cursor.moveToNext()) {
                items.add(getItemFromCursor(cursor))
            }
            cursor.close()
            callback(items)
        }
    }

    fun findOneByWordId(
        wordId: String,
        userId: String,
        callback: (AIResponseItem?) -> Unit
    ) {
        executor.execute {
            val db = readableDatabase
            var item: AIResponseItem? = null

            println("find by user id $userId and word id $wordId")

            val selection = "$COLUMN_USER_ID = ? AND $COLUMN_WORD_ID = ?"
            val selectionArgs = arrayOf(userId, wordId)
            val orderBy = "$COLUMN_UPDATED_AT DESC"
            val limit = "1"

            val cursor = db.query(
                TABLE_AI_RESPONSES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                orderBy,
                limit
            )

            if (cursor.moveToFirst()) {
                item = getItemFromCursor(cursor)
            }
            cursor.close()
            callback(item)
        }
    }

    fun insert(item: AIResponseItem, callback: (Long) -> Unit = {}) {
        executor.execute {
            val db = writableDatabase
            var resultId = -1L
            try {
                val values = ContentValues().apply {
                    put(COLUMN_UID, item.uid)
                    put(COLUMN_INPUT, item.input)
                    put(COLUMN_OUTPUT, item.output)
                    put(COLUMN_DETAIL, item.detail)
                    put(COLUMN_SYNONYM, item.synonym)
                    put(COLUMN_ANTONYM, item.antonym)
                    put(COLUMN_EXAMPLE_SENTENCES, item.exampleSentences)
                    put(COLUMN_SHORT_MEANING, item.shortMeaning)
                    put(COLUMN_USER_ID, item.userId)
                    put(COLUMN_TYPE, item.type)
                    put(COLUMN_CREATED_AT, item.createdAt)
                    put(COLUMN_UPDATED_AT, item.updatedAt)
                    put(COLUMN_SYNC_STATUS, item.syncStatus.name)
                    put(COLUMN_RETRY_COUNT, item.retryCount)
                    item.lastSyncAttempt?.let { put(COLUMN_LAST_SYNC_ATTEMPT, it) }
                }
                resultId = db.insertWithOnConflict(
                    TABLE_AI_RESPONSES,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
                callback(resultId)

            } catch (ex: Exception) {
                Log.e("AIResponseDatabase", "Error inserting AI response: ${ex.message}")
                callback(-1L)
            }
        }
    }

    fun update(item: AIResponseItem, callback: (Int) -> Unit = {}) {
        executor.execute {
            val db = writableDatabase
            var rowsAffected = 0
            try {
                val values = ContentValues().apply {
                    put(COLUMN_INPUT, item.input)
                    put(COLUMN_OUTPUT, item.output)
                    put(COLUMN_DETAIL, item.detail)
                    put(COLUMN_SYNONYM, item.synonym)
                    put(COLUMN_ANTONYM, item.antonym)
                    put(COLUMN_EXAMPLE_SENTENCES, item.exampleSentences)
                    put(COLUMN_SHORT_MEANING, item.shortMeaning)
                    put(COLUMN_TYPE, item.type)
                    put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                    put(COLUMN_SYNC_STATUS, item.syncStatus.name)
                    put(COLUMN_RETRY_COUNT, item.retryCount)
                    item.lastSyncAttempt?.let { put(COLUMN_LAST_SYNC_ATTEMPT, it) }
                }

                val whereClause = "$COLUMN_UID = ?"
                val whereArgs = arrayOf(item.uid)

                rowsAffected = db.update(TABLE_AI_RESPONSES, values, whereClause, whereArgs)
                callback(rowsAffected)

            } catch (ex: Exception) {
                Log.e("AIResponseDatabase", "Error updating AI response: ${ex.message}")
                callback(0)
            }
        }
    }

    fun updatePartial(
        item: AIResponseItem,
        callback: (Int) -> Unit = {}
    ) {
        executor.execute {
            val db = writableDatabase
            var rowsAffected = 0
            try {
                println("update partial $item")

                // Check if record exists
                val selection = "$COLUMN_USER_ID = ? AND $COLUMN_WORD_ID = ?"
                val selectionArgs = arrayOf(item.userId, item.wordId)

                val cursor = db.query(
                    TABLE_AI_RESPONSES,
                    arrayOf(COLUMN_ID),
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null,
                    "1"
                )

                val exists = cursor.count > 0
                cursor.close()

                val values = ContentValues().apply {
                    if (item.detail.isNullOrBlank()) {
                        put(COLUMN_DETAIL, item.detail)
                    }
                    if (item.synonym.isNullOrBlank()) {
                        put(COLUMN_SYNONYM, item.synonym)
                    }
                    if (item.antonym.isNullOrBlank()) {
                        put(COLUMN_ANTONYM, item.antonym)
                    }
                    if (item.exampleSentences.isNullOrBlank()) {
                        put(COLUMN_EXAMPLE_SENTENCES, item.exampleSentences)
                    }
                    if (item.shortMeaning.isNullOrBlank()) {
                        put(COLUMN_SHORT_MEANING, item.shortMeaning)
                    }
                    put(COLUMN_SYNC_STATUS, item.syncStatus.name)
                    put(COLUMN_UPDATED_AT, System.currentTimeMillis())
                }

                if (values.size() == 2) {
                    // Only syncStatus and updatedAt
                    callback(0)
                    return@execute
                }

                if (exists) {
                    val whereClause = "$COLUMN_USER_ID = ? AND $COLUMN_WORD_ID = ?"
                    val whereArgs = arrayOf(item.userId, item.wordId)
                    rowsAffected = db.update(TABLE_AI_RESPONSES, values, whereClause, whereArgs)
                } else {
                    values.apply {
                        put(COLUMN_USER_ID, item.userId)
                        put(COLUMN_WORD_ID, item.wordId)
                        put(COLUMN_UID, item.uid)
                        put(COLUMN_INPUT, item.input)
                        put(COLUMN_OUTPUT, item.output)
                        put(COLUMN_TYPE, item.type)
                        put(COLUMN_CREATED_AT, item.createdAt)
                        put(COLUMN_RETRY_COUNT, item.retryCount)

                        // Add empty strings for fields not provided
                        if (!containsKey(COLUMN_DETAIL)) put(COLUMN_DETAIL, "")
                        if (!containsKey(COLUMN_SYNONYM)) put(COLUMN_SYNONYM, "")
                        if (!containsKey(COLUMN_ANTONYM)) put(COLUMN_ANTONYM, "")
                        if (!containsKey(COLUMN_EXAMPLE_SENTENCES)) put(COLUMN_EXAMPLE_SENTENCES, "")
                        if (!containsKey(COLUMN_SHORT_MEANING)) put(COLUMN_SHORT_MEANING, "")

                        item.lastSyncAttempt?.let { put(COLUMN_LAST_SYNC_ATTEMPT, it) }
                    }

                    val id = db.insert(TABLE_AI_RESPONSES, null, values)
                    rowsAffected = if (id != -1L) 1 else 0
                }

                callback(rowsAffected)

            } catch (ex: Exception) {
                Log.e("AIResponseDatabase", "Error updating AI response partially: ${ex.message}")
                callback(0)
            }
        }
    }

    fun delete(uid: String, callback: (Int) -> Unit = {}) {
        executor.execute {
            val db = writableDatabase
            var rowsAffected = 0
            try {
                val whereClause = "$COLUMN_UID = ?"
                val whereArgs = arrayOf(uid)

                rowsAffected = db.delete(TABLE_AI_RESPONSES, whereClause, whereArgs)
                callback(rowsAffected)

            } catch (ex: Exception) {
                Log.e("AIResponseDatabase", "Error deleting AI response: ${ex.message}")
                callback(0)
            }
        }
    }

    fun getPendingSyncItems(callback: (List<AIResponseItem>) -> Unit) {
        executor.execute {
            val items = mutableListOf<AIResponseItem>()
            val db = readableDatabase

            val selection = "$COLUMN_SYNC_STATUS = ?"
            val selectionArgs = arrayOf(SyncStatus.PENDING.name)
            val orderBy = "$COLUMN_CREATED_AT ASC"

            val cursor = db.query(
                TABLE_AI_RESPONSES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
            )

            while (cursor.moveToNext()) {
                items.add(getItemFromCursor(cursor))
            }
            cursor.close()
            callback(items)
        }
    }

    private fun getItemFromCursor(cursor: Cursor): AIResponseItem {
        return AIResponseItem(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            uid = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UID)),
            wordId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORD_ID)),
            input = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INPUT)),
            output = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OUTPUT)),
            detail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DETAIL)),
            synonym = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SYNONYM)),
            antonym = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANTONYM)),
            exampleSentences = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXAMPLE_SENTENCES)),
            shortMeaning = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SHORT_MEANING)),
            userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
            type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)),
            syncStatus = try {
                SyncStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SYNC_STATUS)))
            } catch (e: IllegalArgumentException) {
                SyncStatus.PENDING
            },
            retryCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RETRY_COUNT)),
            lastSyncAttempt = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_LAST_SYNC_ATTEMPT))) {
                null
            } else {
                cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_SYNC_ATTEMPT))
            }
        )
    }
}