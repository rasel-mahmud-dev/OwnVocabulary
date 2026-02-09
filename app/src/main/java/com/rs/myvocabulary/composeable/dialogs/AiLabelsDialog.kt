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
fun AiLabelsDialog(
        suggestedLabels: List<String>,
        onDismiss: () -> Unit,
        onProceed: (labels: List<String>) -> Unit
) {
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("AI Label Generate") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (suggestedLabels.isNotEmpty()) {
                        Column {
                            Text("Suggested Labels:", style = MaterialTheme.typography.labelLarge)
                            FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                suggestedLabels.forEach { label ->
                                    AssistChip(onClick = {}, label = { Text(label) })
                                }
                            }
                        }
                    }

                    if (suggestedLabels.isEmpty()) {
                        Text("No suggestions found.")
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = { onProceed(suggestedLabels) },
                        enabled = suggestedLabels.isNotEmpty()
                ) { Text("Proceed") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Dismiss") } }
    )
}
