package com.rs.myvocabulary.utils

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.rs.myvocabulary.api.HttpHelper
import com.rs.myvocabulary.api.ImageKitAuthResponse
import com.rs.myvocabulary.configs.Keys
import java.io.File
import kotlin.collections.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.BufferedSink
import okio.source

class ImageKitUploader(private val context: Context) {
    private val client = OkHttpClient()
    private val gson = Gson()

    suspend fun uploadFile(
            uri: Uri? = null,
            file: File? = null,
            remoteUrl: String? = null,
            fileName: String,
            folder: String = "/learnmedia"
    ): String? =
            withContext(Dispatchers.IO) {
                try {
                    // 1. Get Authentication Parameters from Backend
                    val authResponse = HttpHelper.getInstance().getImageKitAuth()
                    if (authResponse.statusCode != 200 || authResponse.body == null) {
                        return@withContext null
                    }

                    val auth = gson.fromJson(authResponse.body, ImageKitAuthResponse::class.java)

                    // 2. Prepare Multipart Request for ImageKit
                    val requestBodyBuilder =
                            MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("publicKey", Keys.IMAGEKIT_PUBLIC_KEY)
                                    .addFormDataPart("signature", auth.signature)
                                    .addFormDataPart("expire", auth.expire.toString())
                                    .addFormDataPart("token", auth.token)
                                    .addFormDataPart("fileName", fileName)
                                    .addFormDataPart("folder", folder)

                    if (file != null) {
                        val mediaType = "application/octet-stream".toMediaTypeOrNull()
                        requestBodyBuilder.addFormDataPart(
                                "file",
                                fileName,
                                file.asRequestBody(mediaType)
                        )
                    } else if (remoteUrl != null) {
                        requestBodyBuilder.addFormDataPart("file", remoteUrl)
                    } else if (uri != null) {
                        val mediaType =
                                context.contentResolver.getType(uri)?.toMediaTypeOrNull()
                                        ?: "application/octet-stream".toMediaTypeOrNull()

                        val streamingRequestBody =
                                object : RequestBody() {
                                    override fun contentType(): MediaType? = mediaType

                                    override fun contentLength(): Long {
                                        return try {
                                            context.contentResolver.openAssetFileDescriptor(
                                                            uri,
                                                            "r"
                                                    )
                                                    ?.use { it.length }
                                                    ?: -1L
                                        } catch (e: Exception) {
                                            -1L
                                        }
                                    }

                                    override fun writeTo(sink: BufferedSink) {
                                        context.contentResolver.openInputStream(uri)?.use {
                                                inputStream ->
                                            sink.writeAll(inputStream.source())
                                        }
                                    }
                                }

                        requestBodyBuilder.addFormDataPart("file", fileName, streamingRequestBody)
                    } else {
                        return@withContext null
                    }

                    val request =
                            Request.Builder()
                                    .url("https://upload.imagekit.io/api/v1/files/upload")
                                    .post(requestBodyBuilder.build())
                                    .build()

                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val result = gson.fromJson(responseBody, Map::class.java)
                        return@withContext result["url"] as? String
                    } else {
                        android.util.Log.e(
                                "ImageKitUploader",
                                "Upload Failed: ${response.code} ${response.message}"
                        )
                        return@withContext null
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ImageKitUploader", "Upload exception", e)
                    return@withContext null
                }
            }
}
