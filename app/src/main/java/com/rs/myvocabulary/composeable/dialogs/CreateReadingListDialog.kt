package com.rs.myvocabulary.composeable.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

@Composable
fun CreateReadingListDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var listName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Create Reading List") },
            text = {
                Column {
                    OutlinedTextField(
                            value = listName,
                            onValueChange = { listName = it },
                            label = { Text("Reading List Name") },
                            placeholder = { Text("e.g. TOEFL Prep") },
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                            singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                            if (listName.isNotBlank()) {
                                onConfirm(listName.trim())
                            }
                        },
                        enabled = listName.isNotBlank()
                ) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
