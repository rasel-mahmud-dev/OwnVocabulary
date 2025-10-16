package com.rs.ownvocabulary.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.rs.ownvocabulary.ai.AIIndex
import kotlinx.coroutines.launch

@Composable
fun AITestScreen() {
    val scope = rememberCoroutineScope()

    // State variables for each function
    var meaningResult by remember { mutableStateOf("") }
    var synonymsResult by remember { mutableStateOf("") }
    var smartWordsResult by remember { mutableStateOf("") }
    var antonymsResult by remember { mutableStateOf("") }
    var translationResult by remember { mutableStateOf("") }
    var sentencesResult by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val word = "empathize"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "AI Vocabulary Helper Test",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Testing word: '$word'",
            style = MaterialTheme.typography.bodyLarge
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Loading indicator
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // Get Meaning Button
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    meaningResult = "Loading..."
                    AIIndex.getMeaning(word, "English").fold(
                        onSuccess = { result ->
                            meaningResult = buildString {
                                appendLine("Word: ${word}")
                                appendLine("Part of Speech: ${result.partOfSpeech ?: "N/A"}")
                                appendLine("\nMeanings:")
                                result.meanings.forEachIndexed { index, meaning ->
                                    appendLine("${index + 1}. $meaning")
                                }
                                appendLine("\nExamples:")
                                result.examples.forEachIndexed { index, example ->
                                    appendLine("${index + 1}. $example")
                                }
                            }
                        },
                        onFailure = { error ->
                            meaningResult = "Error: ${error.message}"
                        }
                    )
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Meaning")
        }

        if (meaningResult.isNotEmpty()) {
            ResultCard(title = "Meaning", content = meaningResult)
        }

        // Get Synonyms Button
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    synonymsResult = "Loading..."
                    AIIndex.getSynonyms(word, "intermediate").fold(
                        onSuccess = { words ->
                            synonymsResult = buildString {
                                appendLine("Synonyms for '$word':\n")
                                words.forEachIndexed { index, suggestion ->
                                    appendLine("${index + 1}. ${suggestion.word} (${suggestion.difficulty})")
                                    appendLine("   ${suggestion.meaning}\n")
                                }
                            }
                        },
                        onFailure = { error ->
                            synonymsResult = "Error: ${error.message}"
                        }
                    )
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Synonyms")
        }

        if (synonymsResult.isNotEmpty()) {
            ResultCard(title = "Synonyms", content = synonymsResult)
        }

        // Get Smart Recommended Words Button
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    smartWordsResult = "Loading..."
                    AIIndex.getSmartRecommendedWords(word, "emotional intelligence").fold(
                        onSuccess = { words ->
                            smartWordsResult = buildString {
                                appendLine("Smart Recommendations for '$word':\n")
                                words.forEachIndexed { index, suggestion ->
                                    appendLine("${index + 1}. ${suggestion.word} (${suggestion.difficulty})")
                                    appendLine("   ${suggestion.meaning}\n")
                                }
                            }
                        },
                        onFailure = { error ->
                            smartWordsResult = "Error: ${error.message}"
                        }
                    )
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Smart Recommendations")
        }

        if (smartWordsResult.isNotEmpty()) {
            ResultCard(title = "Smart Recommendations", content = smartWordsResult)
        }

        // Get Antonyms Button
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    antonymsResult = "Loading..."
                    AIIndex.getAntonyms(word).fold(
                        onSuccess = { words ->
                            antonymsResult = buildString {
                                appendLine("Antonyms for '$word':\n")
                                words.forEachIndexed { index, suggestion ->
                                    appendLine("${index + 1}. ${suggestion.word} (${suggestion.difficulty})")
                                    appendLine("   ${suggestion.meaning}\n")
                                }
                            }
                        },
                        onFailure = { error ->
                            antonymsResult = "Error: ${error.message}"
                        }
                    )
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Antonyms")
        }

        if (antonymsResult.isNotEmpty()) {
            ResultCard(title = "Antonyms", content = antonymsResult)
        }

        // Get Translation Button
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    translationResult = "Loading..."
                    AIIndex.getTranslation(word, "Bengali").fold(
                        onSuccess = { result ->
                            translationResult = buildString {
                                appendLine("Translation: ${result}")
                            }
                        },
                        onFailure = { error ->
                            translationResult = "Error: ${error.message}"
                        }
                    )
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Translation (Bengali)")
        }

        if (translationResult.isNotEmpty()) {
            ResultCard(title = "Translation", content = translationResult)
        }

        // Get Sentences Button
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    sentencesResult = "Loading..."
                    AIIndex.getSentences(word, 5).fold(
                        onSuccess = { sentences ->
                            sentencesResult = buildString {
                                appendLine("Example Sentences for '$word':\n")
                                sentences.forEachIndexed { index, example ->
                                    appendLine("${index + 1}. ${example}")
                                }
                            }
                        },
                        onFailure = { error ->
                            sentencesResult = "Error: ${error.message}"
                        }
                    )
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Example Sentences")
        }

        if (sentencesResult.isNotEmpty()) {
            ResultCard(title = "Example Sentences", content = sentencesResult)
        }

        // Test All Button
        Button(
            onClick = {
                scope.launch {
                    isLoading = true

                    // Test all functions sequentially
                    meaningResult = "Loading..."
                    AIIndex.getMeaning(word, "English").fold(
                        onSuccess = { result ->
                            meaningResult = "✓ Success: ${word} - ${result.meanings.firstOrNull()}"
                        },
                        onFailure = { meaningResult = "✗ Failed: ${it.message}" }
                    )

                    synonymsResult = "Loading..."
                    AIIndex.getSynonyms(word).fold(
                        onSuccess = { words ->
                            synonymsResult = "✓ Success: Found ${words.size} synonyms"
                        },
                        onFailure = { synonymsResult = "✗ Failed: ${it.message}" }
                    )

                    smartWordsResult = "Loading..."
                    AIIndex.getSmartRecommendedWords(word).fold(
                        onSuccess = { words ->
                            smartWordsResult = "✓ Success: Found ${words.size} recommendations"
                        },
                        onFailure = { smartWordsResult = "✗ Failed: ${it.message}" }
                    )

                    antonymsResult = "Loading..."
                    AIIndex.getAntonyms(word).fold(
                        onSuccess = { words ->
                            antonymsResult = "✓ Success: Found ${words.size} antonyms"
                        },
                        onFailure = { antonymsResult = "✗ Failed: ${it.message}" }
                    )

                    translationResult = "Loading..."
                    AIIndex.getTranslation(word, "Bengali").fold(
                        onSuccess = { result ->
                            translationResult = "✓ Success: ${result}"
                        },
                        onFailure = { translationResult = "✗ Failed: ${it.message}" }
                    )

                    sentencesResult = "Loading..."
                    AIIndex.getSentences(word, 3).fold(
                        onSuccess = { sentences ->
                            sentencesResult = "✓ Success: Found ${sentences.size} sentences"
                        },
                        onFailure = { sentencesResult = "✗ Failed: ${it.message}" }
                    )

                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Test All Functions")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ResultCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}
