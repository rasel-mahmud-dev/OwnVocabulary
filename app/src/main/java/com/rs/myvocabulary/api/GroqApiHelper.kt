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

// Reuse same data classes for compatibility
// data class GeminiPart(val text: String? = null, val inlineData: InlineData? = null)
// data class InlineData(val mimeType: String, val data: String)

class GroqApiHelper() {

    private val client =
            OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .build()

    private val baseUrl = "https://api.groq.com/openai/v1"
    private val apiKeys = BuildConfig.GROQ_API_KEYS.split(",").filter { it.isNotBlank() }

    // Track failed keys to avoid repeated failures
    private val failedKeys = mutableSetOf<String>()
    private var lastKeyRotation = System.currentTimeMillis()

    // Default model - very fast and free
    private val defaultModel = "llama-3.3-70b-versatile" // or "mixtral-8x7b-32768"

    suspend fun generateContent(parts: List<GeminiPart>): HttpResponse =
            withContext(Dispatchers.IO) {
                if (apiKeys.isEmpty()) {
                    println("Groq AI error: No API keys found in BuildConfig")
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
                            println("Groq AI: Key quota exceeded, trying next key...")
                            failedKeys.add(apiKey)
                            lastError = response
                            delay(1000) // Brief delay before trying next key
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
        val url = "$baseUrl/chat/completions"

        // Convert parts to OpenAI-compatible messages format (Text only for Groq)
        val messages = convertPartsToMessages(parts)

        val requestJson =
                JSONObject().apply {
                    put("model", defaultModel)
                    put("messages", messages)
                    put("temperature", 0.7)
                    put("max_tokens", 8000)
                }

        val requestBody =
                requestJson.toString().toRequestBody("application/json".toMediaTypeOrNull())

        println("Groq AI Request: ${url.replace(apiKey, "***")}")
        println("Groq AI Request Body: ${requestJson.toString()}")

        val request =
                Request.Builder()
                        .url(url)
                        .header("Authorization", "Bearer $apiKey")
                        .header("Content-Type", "application/json")
                        .post(requestBody)
                        .build()

        return client.newCall(request).execute().use { response ->
            val code = response.code
            val body = response.body?.string()
            println("Groq AI Response Code: $code")
            if (code != 200) {
                println("Groq AI Error Body: $body")
            }
            HttpResponse(code, body)
        }
    }

    private fun convertPartsToMessages(parts: List<GeminiPart>): JSONArray {
        val messages = JSONArray()
        val textContent = parts.filter { it.text != null }.joinToString("\n") { it.text!! }

        // Create user message with text content only
        messages.put(
                JSONObject().apply {
                    put("role", "user")
                    put("content", textContent)
                }
        )

        return messages
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
            // Groq uses OpenAI format
            json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
        } catch (e: Exception) {
            e.printStackTrace()
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
            response.body
        }
    }

    // Optional: Set custom model
    fun setModel(model: String): GroqApiHelper {
        // Available models:
        // - llama-3.3-70b-versatile (best general purpose)
        // - llama-3.1-70b-versatile
        // - mixtral-8x7b-32768
        // - llama-3.2-11b-vision-preview (for images)
        // - gemma2-9b-it
        return this
    }
}
