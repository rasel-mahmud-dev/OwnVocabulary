package com.rs.ownvocabulary.ai

import com.rs.ownvocabulary.api.GeminiApiHelper
import org.json.JSONArray
import org.json.JSONObject

data class MeaningResult(
    val meanings: List<String>,
    val partOfSpeech: String?,
    val examples: List<String>
)

data class WordSuggestion(
    val word: String,
    val meaning: String,
    val difficulty: String
)

data class TranslationResult(
    val original: String,
    val translation: String,
)


object AIIndex {
    private val api = GeminiApiHelper("YOUR_API_KEY")

    /**
     * Get comprehensive meaning of a word in target language
     */
    suspend fun getMeaning(word: String, targetLang: String = "English"): Result<MeaningResult> {
        val prompt = """
            Provide the meaning of the word "$word" in $targetLang language.
            
            Requirements:
            - Include the part of speech (noun, verb, adjective, etc.)
            - Provide 2-3 clear definitions
            - Include 2 example sentences showing proper usage
            - Use simple, easy-to-understand language
            
            Return ONLY a JSON object in this exact format:
            {
              "partOfSpeech": "noun/verb/adjective/etc",
              "meanings": ["definition 1", "definition 2"],
              "examples": ["example sentence 1", "example sentence 2"]
            }
        """.trimIndent()

        return try {
            val response = api.generateContent(prompt)
            if (response.statusCode == 200) {
                val text = api.parseResponse(response) ?: return Result.failure(Exception("Empty response"))
                val json = JSONObject(extractJson(text))

                Result.success(MeaningResult(
                    meanings = json.getJSONArray("meanings").toStringList(),
                    partOfSpeech = json.optString("partOfSpeech").takeIf { it.isNotEmpty() },
                    examples = json.getJSONArray("examples").toStringList()
                ))
            } else {
                Result.failure(Exception("API Error: ${response.statusCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDetail(word: String, targetLang: String = "English"): Result<String> {
        val prompt = """
            Provide the meaning of the word "$word" in $targetLang language.
            
            Requirements:
            - Include the part of speech (noun, verb, adjective, etc.)
            - Provide 2-3 clear definitions
            - Include 2 example sentences showing proper usage
            - Use simple, easy-to-understand language
            
            Return ONLY a String:
            
        """.trimIndent()

        return try {
            val response = api.generateContent(prompt)
            if (response.statusCode == 200) {
                val text = api.parseResponse(response) ?: return Result.failure(Exception("Empty response"))
                Result.success(text)
            } else {
                Result.failure(Exception("API Error: ${response.statusCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * Get synonyms appropriate for English learners
     */
    suspend fun getSynonyms(word: String, level: String = "intermediate"): Result<List<WordSuggestion>> {
        val prompt = """
            Suggest 5 synonyms for the word "$word" suitable for $level level English learners.
            
            Requirements:
            - Words should be commonly used and practical
            - Include a brief meaning for each synonym
            - Indicate difficulty level (easy/medium/hard)
            - Order from easiest to hardest
            
            Return ONLY a JSON array in this exact format:
            [
              {
                "word": "synonym1",
                "meaning": "brief definition",
                "difficulty": "easy"
              }
            ]
        """.trimIndent()

        return try {
            val response = api.generateContent(prompt)
            if (response.statusCode == 200) {
                val text = api.parseResponse(response) ?: return Result.failure(Exception("Empty response"))
                val jsonArray = JSONArray(extractJson(text))

                val suggestions = (0 until jsonArray.length()).map { i ->
                    val item = jsonArray.getJSONObject(i)
                    WordSuggestion(
                        word = item.getString("word"),
                        meaning = item.getString("meaning"),
                        difficulty = item.getString("difficulty")
                    )
                }
                Result.success(suggestions)
            } else {
                Result.failure(Exception("API Error: ${response.statusCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get contextually similar words based on usage patterns
     */
    suspend fun getSmartRecommendedWords(word: String, context: String? = null): Result<List<WordSuggestion>> {
        val contextClause = context?.let { " in the context of '$it'" } ?: ""

        val prompt = """
            Recommend 5 words that are contextually similar to "$word"$contextClause.
            
            Requirements:
            - Consider usage context, not just dictionary synonyms
            - Include words that an English learner would benefit from knowing
            - Provide brief, clear meanings
            - Order by relevance and usefulness
            
            Return ONLY a JSON array in this exact format:
            [
              {
                "word": "related_word",
                "meaning": "how it relates to '$word'",
                "difficulty": "easy/medium/hard"
              }
            ]
        """.trimIndent()

        return try {
            val response = api.generateContent(prompt)
            if (response.statusCode == 200) {
                val text = api.parseResponse(response) ?: return Result.failure(Exception("Empty response"))
                val jsonArray = JSONArray(extractJson(text))

                val suggestions = (0 until jsonArray.length()).map { i ->
                    val item = jsonArray.getJSONObject(i)
                    WordSuggestion(
                        word = item.getString("word"),
                        meaning = item.getString("meaning"),
                        difficulty = item.getString("difficulty")
                    )
                }
                Result.success(suggestions)
            } else {
                Result.failure(Exception("API Error: ${response.statusCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get antonyms (opposite meaning words)
     */
    suspend fun getAntonyms(word: String): Result<List<WordSuggestion>> {
        val prompt = """
            Suggest 5 antonyms (opposite meaning words) for "$word" suitable for English learners.
            
            Requirements:
            - Provide clear, commonly used antonyms
            - Include brief explanations of the opposite relationship
            - Indicate difficulty level
            - Use practical, everyday vocabulary
            
            Return ONLY a JSON array in this exact format:
            [
              {
                "word": "antonym",
                "meaning": "opposite meaning explanation",
                "difficulty": "easy/medium/hard"
              }
            ]
        """.trimIndent()

        return try {
            val response = api.generateContent(prompt)
            if (response.statusCode == 200) {
                val text = api.parseResponse(response) ?: return Result.failure(Exception("Empty response"))
                val jsonArray = JSONArray(extractJson(text))

                val suggestions = (0 until jsonArray.length()).map { i ->
                    val item = jsonArray.getJSONObject(i)
                    WordSuggestion(
                        word = item.getString("word"),
                        meaning = item.getString("meaning"),
                        difficulty = item.getString("difficulty")
                    )
                }
                Result.success(suggestions)
            } else {
                Result.failure(Exception("API Error: ${response.statusCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get accurate translation with context
     */
    suspend fun getTranslation(word: String, targetLang: String = "Bengali"): Result<String?> {

        val prompt = """
            Translate "$word" to $targetLang
            Return ONLY a JSON Array in this exact format:
            [ "translate_value" ]
        """.trimIndent()

        return try {
            val response = api.generateContent(prompt)
            if (response.statusCode == 200) {
                val text = api.parseResponse(response) ?: return Result.failure(Exception("Empty response"))
                println(text)
                val jsonArray = JSONArray(extractJson(text))
                val item = (0 until jsonArray.length()).map { i ->
                    jsonArray.getString(i)
                }
                Result.success(item.firstOrNull())
            } else {
                Result.failure(Exception("API Error: ${response.statusCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get example sentences demonstrating word usage
     */

    suspend fun getSentences(word: String, count: Int = 5): Result<List<String>> {
        val prompt = """
        Create $count example sentences using the word "$word" to help English learners understand its usage.
        
        Requirements:
        - Use clear, natural English
        - Show different contexts and meanings if the word has multiple uses
        - Vary sentence difficulty (include both simple and complex examples)
        - Make sentences practical and relevant to everyday situations
        - Indicate the context/situation for each sentence
        
        Return ONLY a JSON array in this exact format:
        [
          "Example sentence with the word.",
          "Another example sentence."
        ]
    """.trimIndent()

        return try {
            val response = api.generateContent(prompt)
            if (response.statusCode == 200) {
                val text = api.parseResponse(response) ?: return Result.failure(Exception("Empty response"))
                val jsonArray = JSONArray(extractJson(text))

                val examples = (0 until jsonArray.length()).map { i ->
                    jsonArray.getString(i)
                }
                Result.success(examples)
            } else {
                Result.failure(Exception("API Error: ${response.statusCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * Extract JSON from response text that might contain markdown code blocks
     */
    private fun extractJson(text: String): String {
        // Remove markdown code blocks if present
        val cleaned = text.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        // Find the first { or [ to handle any preamble text
        val jsonStart = cleaned.indexOfFirst { it == '{' || it == '[' }
        val jsonEnd = cleaned.indexOfLast { it == '}' || it == ']' }

        return if (jsonStart >= 0 && jsonEnd >= jsonStart) {
            cleaned.substring(jsonStart, jsonEnd + 1)
        } else {
            cleaned
        }
    }

    /**
     * Helper extension to convert JSONArray to List<String>
     */
    private fun JSONArray.toStringList(): List<String> {
        return (0 until length()).map { getString(it) }
    }
}