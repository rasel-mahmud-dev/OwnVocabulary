package com.rs.myvocabulary.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Unified AI Helper that automatically falls back between providers Usage: Same as GeminiApiHelper
 * - just replace the class
 */
class UnifiedAiHelper {

    private val groqHelper = GroqApiHelper()
    private val geminiHelper = GeminiApiHelper()

    // Set priority order - Groq first (faster + more generous free tier)
    private val providers = listOf("groq", "gemini")

    suspend fun generateContent(parts: List<GeminiPart>): HttpResponse =
            withContext(Dispatchers.IO) {
                var lastError: HttpResponse? = null

                for (provider in providers) {
                    val response =
                            when (provider) {
                                "groq" -> {
                                    println("Trying Groq AI...")
                                    groqHelper.generateContent(parts)
                                }
                                "gemini" -> {
                                    println("Trying Gemini AI...")
                                    geminiHelper.generateContent(parts)
                                }
                                else -> continue
                            }

                    // Check if successful
                    if (response.statusCode == 200) {
                        println("Success with $provider")
                        return@withContext response.copy(
                                provider = provider.replaceFirstChar { it.uppercase() }
                        )
                    } else if (response.statusCode == 429) {
                        // Rate limit - try next provider
                        println("$provider rate limited, trying next provider...")
                        lastError =
                                response.copy(
                                        provider = provider.replaceFirstChar { it.uppercase() }
                                )
                        continue
                    } else {
                        // Other error - return immediately
                        println("$provider error: ${response.statusCode}")
                        return@withContext response.copy(
                                provider = provider.replaceFirstChar { it.uppercase() }
                        )
                    }
                }

                // All providers failed
                println("All AI providers exhausted")
                lastError ?: HttpResponse(500, "{\"error\": \"All AI providers failed\"}")
            }

    // Overload for backward compatibility
    suspend fun generateContent(prompt: String): HttpResponse {
        return generateContent(listOf(GeminiPart(text = prompt)))
    }

    suspend fun fetchBase64FromUrl(url: String): Pair<String, String>? {
        return groqHelper.fetchBase64FromUrl(url)
    }

    fun parseResponse(response: HttpResponse): String? {
        // Try parsing as Groq format first
        val groqResult = groqHelper.parseResponse(response)
        if (groqResult != null) return groqResult

        // Fallback to Gemini format
        return geminiHelper.parseResponse(response)
    }

    fun getErrorMessage(response: HttpResponse): String? {
        return groqHelper.getErrorMessage(response) ?: geminiHelper.getErrorMessage(response)
    }
}
