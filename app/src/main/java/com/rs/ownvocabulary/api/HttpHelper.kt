package com.rs.ownvocabulary.api

import com.rs.ownvocabulary.configs.Keys
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.also
import kotlin.io.use

data class HttpResponse(val statusCode: Int, val body: String?)

class HttpHelper private constructor() {

    private val client = OkHttpClient.Builder().addInterceptor { chain ->
        val originalRequest = chain.request()
        val modifiedRequest = originalRequest.newBuilder()
            .addHeader("x-secret-key", Keys.SECRET_KEY)
            .addHeader("x-api-key", Keys.API_KEY)
            .build()
        chain.proceed(modifiedRequest)
    }.build()

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

}