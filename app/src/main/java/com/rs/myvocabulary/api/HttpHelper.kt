package com.rs.myvocabulary.api

import com.rs.myvocabulary.configs.Keys
import com.rs.myvocabulary.database.SessionManager
import com.rs.myvocabulary.sync.SyncManager
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.also
import kotlin.io.use



data class CommentAttachment(val url: String, val type: String)

data class CommentResponse(
    val _id: String,
    val userId: String,
    val username: String,
    val text: String,
    // Kept for backward compatibility if needed, but new logic uses attachments
    val audioUrl: String? = null,
    val mediaUrl: String? = null,
    val mediaType: String? = null,
    val attachments: List<CommentAttachment>? = null,
    val parentId: String? = null,
    val createdAt: String
)

data class TagResponse(val _id: String, val name: String)

data class CategoryResponse(val _id: String, val name: String, val parentId: String? = null)

data class PostResponse(
    val _id: String,
    val textContent: String?,
    val fileUrl: String?,
    val fileId: String?,
    val fileType: String,
    val attachments: List<CommentAttachment>? = null,
    val tags: List<TagResponse>? = null,
    val categories: List<CategoryResponse>? = null,
    val createdAt: String,
    val comments: List<CommentResponse>? = null
)

data class ImageKitAuthResponse(val token: String, val expire: Long, val signature: String)


data class HttpResponse(val statusCode: Int, val body: String?, val provider: String? = null)

class HttpHelper private constructor() {

    private val client = OkHttpClient.Builder().addInterceptor { chain ->
        val token = SyncManager.getAuthToken()

        val originalRequest = chain.request()

        println("Requesting: ${originalRequest.url}")

        val modifiedRequest = originalRequest.newBuilder()
            .addHeader("x-secret-key", Keys.SECRET_KEY)
            .addHeader("x-api-key", Keys.API_KEY)
            .addHeader("Authorization", "Bearer $token")
            .build()
        chain.proceed(modifiedRequest)
    }.build()


    private fun getBaseUrl(): String {
        return SessionManager.getInstance().getServerUrl() ?: Keys.BASE_URL
    }


    private val baseUrl = Keys.BASE_URL
    companion object {
        @Volatile
        private var instance: HttpHelper? = null

        fun getInstance(): HttpHelper {
            return instance ?: synchronized(this) {
                instance ?: HttpHelper().also { instance = it }
            }
        }
    }

    suspend fun get(url: String): HttpResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(baseUrl + url).build()
        client.newCall(request).execute().use { response ->
            HttpResponse(response.code, response.body?.string())
        }
    }

    suspend fun delete(url: String): HttpResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl + url)
            .delete()
            .build()
        client.newCall(request).execute().use { response ->
            HttpResponse(response.code, response.body?.string())
        }
    }

    suspend fun patch(url: String, json: String): HttpResponse = withContext(Dispatchers.IO) {
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(baseUrl + url)
            .patch(requestBody)
            .build()
        client.newCall(request).execute().use { response ->
            HttpResponse(response.code, response.body?.string())
        }
    }

    suspend fun put(url: String, json: String): HttpResponse = withContext(Dispatchers.IO) {
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(baseUrl + url)
            .put(requestBody)
            .build()
        client.newCall(request).execute().use { response ->
            HttpResponse(response.code, response.body?.string())
        }
    }

    suspend fun post(url: String, json: String): HttpResponse = withContext(Dispatchers.IO) {
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(baseUrl + url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            HttpResponse(response.code, response.body?.string())
        }
    }

    suspend fun postMultipart(url: String, requestBody: RequestBody): HttpResponse =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(getBaseUrl() + url).post(requestBody).build()

            client.newCall(request).execute().let { response ->
                response.use { HttpResponse(response.code, response.body?.string()) }
            }
        }

    suspend fun patchMultipart(url: String, requestBody: RequestBody): HttpResponse =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(getBaseUrl() + url).patch(requestBody).build()

            client.newCall(request).execute().let { response ->
                response.use { HttpResponse(response.code, response.body?.string()) }
            }
        }

    suspend fun getImageKitAuth():  HttpResponse {
        return get("api/general/imagekit/auth")
    }

}