package com.rs.ownvocabulary.database

import android.database.Cursor


object CursorUtils {
    fun getStringSafe(cursor: Cursor, columnName: String): String? {
        val index = cursor.getColumnIndex(columnName)
        return if (index != -1 && !cursor.isNull(index)) cursor.getString(index) else null
    }

    fun getLongSafe(cursor: Cursor, columnName: String): Long? {
        val index = cursor.getColumnIndex(columnName)
        return if (index != -1 && !cursor.isNull(index)) cursor.getLong(index) else null
    }

    fun getIntSafe(cursor: Cursor, columnName: String): Int? {
        val index = cursor.getColumnIndex(columnName)
        return if (index != -1 && !cursor.isNull(index)) cursor.getInt(index) else null
    }
}