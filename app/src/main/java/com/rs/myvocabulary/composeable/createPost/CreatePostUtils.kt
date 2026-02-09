package com.rs.learnmedia.composeable.createPost

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun getFileFromUri(context: Context, uri: Uri): File {
    val tempFile = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}")
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(tempFile).use { output -> input.copyTo(output) }
    }
    return tempFile
}
