package com.rs.myvocabulary.api

import android.util.Base64
import com.rs.myvocabulary.BuildConfig
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class GeminiPart(val text: String? = null, val inlineData: InlineData? = null)

data class InlineData(
    val mimeType: String,
    val data: String // Base64
)

class GeminiApiHelper() {

    private val client =
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"
    private val apiKeys = BuildConfig.GEMINI_API_KEYS.split(",").filter { it.isNotBlank() }

    // Track failed keys to avoid repeated failures
    private val failedKeys = mutableSetOf<String>()
    private var lastKeyRotation = System.currentTimeMillis()

    suspend fun generateContent(parts: List<GeminiPart>): HttpResponse =
        withContext(Dispatchers.IO) {
            if (apiKeys.isEmpty()) {
                println("Gemini AI error: No API keys found in BuildConfig")
                return@withContext HttpResponse(400, "{\"error\": \"No API keys configured\"}")
            }

            // Reset failed keys every 24 hours
            if (System.currentTimeMillis() - lastKeyRotation > 24 * 60 * 60 * 1000) {
                failedKeys.clear()
                lastKeyRotation = System.currentTimeMillis()
            }

            var lastError: HttpResponse? = null
            val availableKeys = apiKeys.filterNot { failedKeys.contains(it) }

            // If all keys failed, try them all again
            val keysToTry = if (availableKeys.isEmpty()) apiKeys else availableKeys

            // Try each available key
            for (apiKey in keysToTry.shuffled()) {
                val response = makeApiCall(apiKey, parts)

                when (response.statusCode) {
                    200 -> {
                        // Success - remove from failed list if it was there
                        failedKeys.remove(apiKey)
                        return@withContext response
                    }
                    429 -> {
                        // Rate limit - mark as failed and try next key
                        println("Gemini AI: Key quota exceeded, trying next key...")
                        failedKeys.add(apiKey)
                        lastError = response

                        // Extract retry delay from response if available
                        val retryDelay = extractRetryDelay(response.body)
                        if (retryDelay > 0 && retryDelay < 5000) {
                            delay(retryDelay)
                        }
                    }
                    else -> {
                        // Other error - return immediately
                        return@withContext response
                    }
                }
            }

            // All keys failed
            lastError ?: HttpResponse(429, "{\"error\": \"All API keys exhausted\"}")
        }

    private suspend fun makeApiCall(apiKey: String, parts: List<GeminiPart>): HttpResponse {
        val url = "$baseUrl/models/gemini-2.0-flash:generateContent?key=$apiKey"

        val requestJson =
            JSONObject().apply {
                put(
                    "contents",
                    JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put(
                                    "parts",
                                    JSONArray().apply {
                                        parts.forEach { part ->
                                            if (part.text != null) {
                                                put(
                                                    JSONObject().apply {
                                                        put(
                                                            "text",
                                                            part.text
                                                        )
                                                    }
                                                )
                                            } else if (part.inlineData != null) {
                                                put(
                                                    JSONObject().apply {
                                                        put(
                                                            "inlineData",
                                                            JSONObject()
                                                                .apply {
                                                                    put(
                                                                        "mimeType",
                                                                        part.inlineData
                                                                            .mimeType
                                                                    )
                                                                    put(
                                                                        "data",
                                                                        part.inlineData
                                                                            .data
                                                                    )
                                                                }
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        )
                    }
                )
            }

        val requestBody =
            requestJson.toString().toRequestBody("application/json".toMediaTypeOrNull())

        println("Gemini AI Request: ${url.replace(apiKey, "***")}")
        println("Gemini AI Prompt Request Body: ${requestJson.toString()}")

        val request = Request.Builder().url(url).post(requestBody).build()

        return client.newCall(request).execute().use { response ->
            val code = response.code
            val body = response.body?.string()
            println("Gemini AI Response Code: $code")
            if (code != 200) {
                println("Gemini AI Error Body: $body")
            }
            HttpResponse(code, body)
        }
    }

    private fun extractRetryDelay(responseBody: String?): Long {
        return try {
            val json = JSONObject(responseBody ?: return 0)
            val details = json.getJSONObject("error").getJSONArray("details")
            for (i in 0 until details.length()) {
                val detail = details.getJSONObject(i)
                if (detail.getString("@type") == "type.googleapis.com/google.rpc.RetryInfo") {
                    val retryDelay = detail.getString("retryDelay")
                    // Convert "23s" to milliseconds
                    retryDelay.replace("s", "").toLongOrNull()?.let { it * 1000 } ?: 0
                } else {
                    0
                }
            }
            0
        } catch (e: Exception) {
            0
        }
    }

    // Overload for backward compatibility
    suspend fun generateContent(prompt: String): HttpResponse {
        return generateContent(listOf(GeminiPart(text = prompt)))
    }

    suspend fun fetchBase64FromUrl(url: String): Pair<String, String>? =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    val bytes = response.body?.bytes() ?: return@withContext null
                    val base64Data =
                        Base64.encodeToString(
                            bytes,
                            Base64.NO_WRAP
                        )
                    val mimeType =
                        response.body?.contentType()?.toString()
                            ?: "application/octet-stream"
                    base64Data to mimeType
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
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

    // Helper to get error message from response
    fun getErrorMessage(response: HttpResponse): String? {
        return try {
            if (response.body == null) return null
            val json = JSONObject(response.body)
            json.getJSONObject("error").getString("message")
        } catch (e: Exception) {
            null
        }
    }
}