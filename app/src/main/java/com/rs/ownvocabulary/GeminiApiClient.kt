package com.rs.ownvocabulary

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.apply

class GeminiApiClient {
    private val client = OkHttpClient()
    private val apiKeys = listOf(
        
    )
    private val apiKey = apiKeys.random()
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

    suspend fun handleGenerate(word: String, allGeneratedSentences: String, count: Int): List<String> {
        try {
            val prompt = """
                Generate example sentences for "$word" - this is request #${count + 1}.
                Create 2 COMPLETELY NEW sentences that are different from previous ones.
                
                Previous sentences (avoid these): $allGeneratedSentences
                
                Make the new sentences:
                - Different contexts
                - Different grammatical structures  
                - 5-12 words each
                - Natural modern English 
                - Easy English for Beginner Learner
                
                Return JSON: ["sentence1 (bangla translate)", "sentence2 (bangla translate)"]
            """.trimIndent()

            val result = getResponse(prompt)
            val newSentences = parseJsonResponse(result)
            return newSentences

        } catch (e: Exception) {
            println("Error: ${e.message}")
            return emptyList()
        }
    }

    fun parseJsonResponse(response: String): List<String> {
        return try {
            val cleaned = response
                .replace("```json", "")
                .replace("```", "")
            val regex = "\"(.*?)\"".toRegex()
            regex.findAll(cleaned)
                .map { it.groupValues[1] }
                .filter { it.isNotBlank() }
                .toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getResponse(prompt: String): String = withContext(Dispatchers.IO) {
        val requestBody = JSONObject().apply {
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

        val request = Request.Builder()
            .url("$baseUrl?key=$apiKey")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw IOException("API failed: ${response.code}")
        }

        val responseBody = response.body?.string() ?: throw IOException("Empty response")
        val jsonResponse = JSONObject(responseBody)

        return@withContext jsonResponse
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
    }
}