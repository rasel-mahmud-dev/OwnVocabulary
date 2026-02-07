package com.rs.myvocabulary.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object LocalAssetManager {
    private const val DIR_NAME = "learn_media"

    fun saveAsset(context: Context, uri: Uri, isImage: Boolean): String? {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null

            val dir = File(Environment.getExternalStorageDirectory(), DIR_NAME)
            if (!dir.exists()) {
                dir.mkdirs()
            }

            // Generate a unique filename
            val extension = if (isImage) "jpg" else "mp4" // Simplified extension handling
            val fileName = "${UUID.randomUUID()}.$extension"
            val file = File(dir, fileName)

            FileOutputStream(file).use { output -> inputStream.copyTo(output) }

            return file.path // Return path to be stored in DB
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getAssetFile(path: String): File {
        return File(path)
    }
}
