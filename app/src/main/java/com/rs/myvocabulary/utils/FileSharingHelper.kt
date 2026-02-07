package com.rs.myvocabulary.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.ArrayList

object FileSharingHelper {
    suspend fun shareMedia(context: Context, url: String, type: String) {
        shareMedia(context, listOf(url), type)
    }

    suspend fun shareMedia(context: Context, urls: List<String>, type: String) {
        if (urls.isEmpty()) return

        if (urls.size == 1) {
            val result = downloadFile(context, urls[0], type)
            if (result != null) {
                val (uri, mimeType) = result
                val shareIntent =
                        Intent(Intent.ACTION_SEND).apply {
                            this.type = mimeType
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_TEXT, "Shared from LearnMedia")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                context.startActivity(Intent.createChooser(shareIntent, "Share $type"))
            }
        } else {
            val uris = ArrayList<Uri>()
            var mimeType = "*/*"

            urls.forEach { url ->
                val result = downloadFile(context, url, type)
                if (result != null) {
                    uris.add(result.first)
                    mimeType = result.second // Just take the last one roughly
                }
            }

            if (uris.isNotEmpty()) {
                val shareIntent =
                        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                            this.type = mimeType
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                            putExtra(Intent.EXTRA_TEXT, "Shared from LearnMedia")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                context.startActivity(Intent.createChooser(shareIntent, "Share $type"))
            }
        }
    }

    private suspend fun downloadFile(
            context: Context,
            urlString: String,
            fallbackType: String
    ): Pair<Uri, String>? =
            withContext(Dispatchers.IO) {
                try {
                    val url = URL(urlString)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connect()

                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        return@withContext null
                    }

                    val contentType =
                            connection.contentType
                                    ?: when (fallbackType.lowercase()) {
                                        "image" -> "image/jpeg"
                                        "video" -> "video/mp4"
                                        "audio" -> "audio/mpeg"
                                        else -> "application/octet-stream"
                                    }

                    val extension =
                            MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType)
                                    ?: when (fallbackType.lowercase()) {
                                        "image" -> "jpg"
                                        "video" -> "mp4"
                                        "audio" -> "mp3"
                                        else -> "bin"
                                    }

                    var fileName = urlString.split("/").last().substringBefore("?")
                    if (!fileName.contains(".")) {
                        fileName = "$fileName.$extension"
                    } else if (MimeTypeMap.getFileExtensionFromUrl(fileName).isEmpty()) {
                        // If the end of the URL has a dot but no real extension
                        fileName = "$fileName.$extension"
                    }

                    val cacheDir = File(context.cacheDir, "shared_media")
                    if (!cacheDir.exists()) cacheDir.mkdirs()

                    val file = File(cacheDir, fileName)

                    connection.inputStream.use { input ->
                        FileOutputStream(file).use { output -> input.copyTo(output) }
                    }

                    val uri =
                            FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                            )
                    Pair(uri, contentType)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
}
