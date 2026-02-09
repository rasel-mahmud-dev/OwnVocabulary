package com.rs.myvocabulary.composeable.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiSuggestionsDialog(
        suggestedCategories: List<String>,
        onDismiss: () -> Unit,
        onProceed: (categories: List<String>) -> Unit
) {
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("AI Suggestions") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (suggestedCategories.isNotEmpty()) {
                        Column {
                            Text(
                                    "Suggested Categories:",
                                    style = MaterialTheme.typography.labelLarge
                            )
                            FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                suggestedCategories.forEach { category ->
                                    AssistChip(onClick = {}, label = { Text(category) })
                                }
                            }
                        }
                    }

                    if (suggestedCategories.isEmpty()) {
                        Text("No suggestions found.")
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = { onProceed(suggestedCategories) },
                        enabled = suggestedCategories.isNotEmpty()
                ) { Text("Proceed") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Dismiss") } }
    )
}
