package com.rs.myvocabulary.composeable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rs.myvocabulary.database.Word
import com.rs.myvocabulary.viewmodels.AppViewModel

@Composable
fun UnifiedAddDialog(showDialog: Boolean, onDismiss: () -> Unit, appViewModel: AppViewModel) {
        if (!showDialog) return

        var selectedType by remember { mutableStateOf("word") } // word, clause, docs
        var word by remember { mutableStateOf("") }
        var meaning by remember { mutableStateOf("") }
        var details by remember { mutableStateOf("") }
        var isSaving by remember { mutableStateOf(false) }

        Dialog(
                onDismissRequest = onDismiss,
                properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
                Surface(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                ) {
                        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                                // Header
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text = "Add New Item",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold
                                        )
                                        IconButton(onClick = onDismiss) {
                                                Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Close"
                                                )
                                        }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Type Selector
                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(
                                                                MaterialTheme.colorScheme
                                                                        .surfaceContainerHighest
                                                        )
                                                        .padding(4.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                        TypeButton(
                                                text = "Word",
                                                selected = selectedType == "word",
                                                onClick = { selectedType = "word" }
                                        )
                                        TypeButton(
                                                text = "Clause",
                                                selected = selectedType == "clause",
                                                onClick = { selectedType = "clause" }
                                        )
                                        TypeButton(
                                                text = "Note",
                                                selected = selectedType == "docs",
                                                onClick = { selectedType = "docs" }
                                        )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Input Fields
                                OutlinedTextField(
                                        value = word,
                                        onValueChange = { word = it },
                                        label = {
                                                Text(
                                                        if (selectedType == "docs") "Title"
                                                        else "Word/Phrase"
                                                )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                if (selectedType != "docs") {
                                        OutlinedTextField(
                                                value = meaning,
                                                onValueChange = { meaning = it },
                                                label = { Text("Short Meaning") },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                }

                                OutlinedTextField(
                                        value = details,
                                        onValueChange = { details = it },
                                        label = {
                                                Text(
                                                        if (selectedType == "docs") "Content"
                                                        else "Details/Examples"
                                                )
                                        },
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        minLines = 3
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Action Buttons
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        TextButton(onClick = onDismiss) { Text("Cancel") }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Button(
                                                onClick = {
                                                        if (word.isNotBlank()) {
                                                                isSaving = true
                                                                println(
                                                                        "UnifiedAddDialog: Creating new item - type=$selectedType, word=$word"
                                                                )
                                                                val newWord =
                                                                        Word(
                                                                                word = word,
                                                                                shortMeaning =
                                                                                        meaning,
                                                                                details = details,
                                                                                type = selectedType,
                                                                                userId =
                                                                                        "1" // Default user ID
                                                                        )
                                                                println(
                                                                        "UnifiedAddDialog: Word object created - uid=${newWord.uid}, type=${newWord.type}"
                                                                )
                                                                appViewModel.addWord(newWord) {
                                                                        errorMessage ->
                                                                        isSaving = false
                                                                        println(
                                                                                "UnifiedAddDialog: addWord callback - errorMessage=$errorMessage"
                                                                        )
                                                                        if (errorMessage == null) {
                                                                                println(
                                                                                        "UnifiedAddDialog: Success! Dismissing dialog"
                                                                                )
                                                                                onDismiss()
                                                                                // Reset fields
                                                                                word = ""
                                                                                meaning = ""
                                                                                details = ""
                                                                        } else {
                                                                                println(
                                                                                        "UnifiedAddDialog: Error - $errorMessage"
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                },
                                                enabled = word.isNotBlank() && !isSaving
                                        ) {
                                                if (isSaving) {
                                                        CircularProgressIndicator(
                                                                modifier = Modifier.size(24.dp),
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onPrimary
                                                        )
                                                } else {
                                                        Text("Save")
                                                }
                                        }
                                }
                        }
                }
        }
}

@Composable
fun TypeButton(text: String, selected: Boolean, onClick: () -> Unit) {
        Box(
                modifier =
                        Modifier.clip(RoundedCornerShape(8.dp))
                                .background(
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                )
                                .clickable(onClick = onClick)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
        ) {
                Text(
                        text = text,
                        color =
                                if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
        }
}
