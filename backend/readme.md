### A. Smart Sentence Generation

- Generate example sentences for a word (word.examples).
- Adjust tone or difficulty: e.g., Beginner → simpler, Advanced → complex.
- You already store proficiencyLevel, so AI can adapt examples based on that.

prompt: "Explain the meaning of '${word.word}' in simple English and give a short definition."

### AI Meaning & Explanation Generation
prompt: "Explain the meaning of '${word.word}' in simple English and give a short definition."


### Quiz Generator
prompt: "Generate a 4-option multiple-choice question for the word '${word.word}' with only one correct answer."


### AI Translation Assistant
prompt: "Translate '${word.word}' and its meaning into Bengali."

### Smart word recommendations
prompt: "Suggest 5 similar words to '${word.word}' that a ${word.proficiencyLevel} English learner might find useful."


```kt
suspend fun generateAIExamples(word: Word): String {
    val prompt = """
        Generate 2 short and unique example sentences using the word "${word.word}".
        Make them suitable for a ${word.proficiencyLevel} English learner.
    """.trimIndent()

    val response = aiApi.generateContent(prompt) // Gemini API call
    return response.text
}

suspend fun translateMeaning(word: String, meaning: String, targetLang: String): String {
        val prompt = "Translate the meaning of '$word': '$meaning' into $targetLang."
        val response = model.generateContent(prompt)
        return response.text ?: ""
    }
    
    suspend fun generateExamples(word: String, level: String): String {
        val prompt = """
            Generate 2 unique example sentences for '$word' 
            suitable for a $level English learner.
        """.trimIndent()
        val response = model.generateContent(prompt)
        return response.text ?: ""
    }
    
     private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun generateMeaning(word: String): String {
        val prompt = "Explain the meaning of '$word' in simple English with a short definition."
        val response = model.generateContent(prompt)
        return response.text ?: ""
    }

    
```