package com.rs.myvocabulary.composeable.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AiPostEnhancementDialog(
        enhancedWord: String,
        enhancedShortMeaning: String,
        enhancedDetails: String,
        enhancedCategories: List<String>,
        onDismiss: () -> Unit,
        onProceed:
                (word: String, meaning: String, details: String, categories: List<String>) -> Unit
) {
        var editableWord by remember { mutableStateOf(enhancedWord) }
        var editableMeaning by remember { mutableStateOf(enhancedShortMeaning) }
        var editableDetails by remember { mutableStateOf(enhancedDetails) }
        var editableCategories by remember { mutableStateOf(enhancedCategories.joinToString(", ")) }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("AI Post Enhancement") },
                text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text(
                                        "Enhanced Word:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                )
                                OutlinedTextField(
                                        value = editableWord,
                                        onValueChange = { editableWord = it },
                                        modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                        "Enhanced Short Meaning:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                )
                                OutlinedTextField(
                                        value = editableMeaning,
                                        onValueChange = { editableMeaning = it },
                                        modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                        "Enhanced Categories (comma separated):",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                )
                                OutlinedTextField(
                                        value = editableCategories,
                                        onValueChange = { editableCategories = it },
                                        modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                        "Enhanced Details:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                )
                                OutlinedTextField(
                                        value = editableDetails,
                                        onValueChange = { editableDetails = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3
                                )
                        }
                },
                confirmButton = {
                        Button(
                                onClick = {
                                        val categoriesList =
                                                editableCategories
                                                        .split(",")
                                                        .map { it.trim() }
                                                        .filter { it.isNotEmpty() }
                                        onProceed(
                                                editableWord,
                                                editableMeaning,
                                                editableDetails,
                                                categoriesList
                                        )
                                }
                        ) { Text("Apply Changes") }
                },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Discard") } }
        )
}
