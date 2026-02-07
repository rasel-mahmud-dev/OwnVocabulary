package com.rs.myvocabulary.composeable.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AiExampleDialog(sentences: List<String>, onDismiss: () -> Unit, onProceed: () -> Unit) {
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Review AI Examples") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("AI suggested the following example sentences:")
                    sentences.forEach { sentence ->
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(
                                    text = "• $sentence",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = onProceed) { Text("Proceed") } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Dismiss") } }
    )
}
