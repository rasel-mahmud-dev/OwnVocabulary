package com.rs.ownvocabulary.api


import com.rs.ownvocabulary.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.TimeUnit

class GeminiApiHelper(private val apiKey2: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // time to establish connection
        .readTimeout(120, TimeUnit.SECONDS)    // time to wait for server response
        .writeTimeout(120, TimeUnit.SECONDS)   // time to send the request body
        .build()

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"
    private val apiKeys = BuildConfig.GEMINI_API_KEYS.split(",")

    suspend fun generateContent(prompt: String): HttpResponse = withContext(Dispatchers.IO) {
        val apiKey = apiKeys.random()
        println("apiKey ran $apiKey")
        val url = "$baseUrl/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        val requestBody = requestJson.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            HttpResponse(response.code, response.body?.string())
        }
    }

    fun parseResponse(response: HttpResponse): String? {
        if (response.statusCode != 200 || response.body == null) {
            return null
        }

        return try {
            val json = JSONObject(response.body)
            json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        } catch (e: Exception) {
            null
        }
    }
}