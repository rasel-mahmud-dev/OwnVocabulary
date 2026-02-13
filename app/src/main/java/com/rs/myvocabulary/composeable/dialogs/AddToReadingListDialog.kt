package com.rs.myvocabulary.composeable.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun AddToReadingListDialog(
        readingLists: List<String>,
        onDismiss: () -> Unit,
        onConfirm: (String) -> Unit
) {
    var listName by remember { mutableStateOf("") }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add to Reading List") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                            value = listName,
                            onValueChange = { listName = it },
                            label = { Text("Reading List Name") },
                            placeholder = { Text("e.g. Morning Routine") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                    )

                    if (readingLists.isNotEmpty()) {
                        Text(
                                "Existing Lists:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Box(modifier = Modifier.heightIn(max = 200.dp)) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(
                                        readingLists.filter {
                                            it.contains(listName, ignoreCase = true) ||
                                                    listName.isBlank()
                                        }
                                ) { name ->
                                    Surface(
                                            modifier =
                                                    Modifier.fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .clickable { onConfirm(name) },
                                            color =
                                                    if (listName == name)
                                                            MaterialTheme.colorScheme
                                                                    .primaryContainer
                                                    else
                                                            MaterialTheme.colorScheme.surfaceVariant
                                                                    .copy(alpha = 0.5f)
                                    ) {
                                        Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                    Icons.Default.AutoStories,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(name, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onConfirm(listName) }, enabled = listName.isNotBlank()) {
                    Text("Add")
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
