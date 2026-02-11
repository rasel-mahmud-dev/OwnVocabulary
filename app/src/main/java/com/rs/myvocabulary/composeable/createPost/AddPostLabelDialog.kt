package com.rs.myvocabulary.composeable.createPost

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rs.myvocabulary.database.Label
import com.rs.myvocabulary.viewmodels.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostLabelDialog(
        appViewModel: AppViewModel,
        currentLabels: List<Label>,
        onSelectedLabels: (List<Label>) -> Unit
) {
    var showModal by remember { mutableStateOf(false) }
    var newLabelName by remember { mutableStateOf("") }
    val allLabels by appViewModel.allCategories.collectAsState()

    // Maintain a local state of selected labels while the dialog is open
    var selectedLabels by remember(showModal) { mutableStateOf(currentLabels.toSet()) }

    AssistChip(
            onClick = { showModal = true },
            label = { Text("Tags") },
            leadingIcon = {
                Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Label",
                        modifier = Modifier.size(18.dp)
                )
            }
    )

    if (showModal) {
        AlertDialog(
                onDismissRequest = { showModal = false },
                title = { Text("Select Labels") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Add New Label Input
                        OutlinedTextField(
                                value = newLabelName,
                                onValueChange = { newLabelName = it },
                                label = { Text("New Label") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                trailingIcon = {
                                    if (newLabelName.isNotBlank()) {
                                        IconButton(
                                                onClick = {
                                                    appViewModel.addCategory(newLabelName)
                                                    newLabelName = ""
                                                }
                                        ) {
                                            Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = "Create Label"
                                            )
                                        }
                                    }
                                }
                        )

                        // Labels List
                        Text(
                                text = "Available Labels",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                        )

                        LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 100.dp),
                                modifier = Modifier.heightIn(max = 300.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(allLabels) { label ->
                                val isSelected =
                                        selectedLabels.any {
                                            it.name.lowercase() == label.name.lowercase()
                                        }
                                FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedLabels =
                                                    if (isSelected) {
                                                        selectedLabels
                                                                .filterNot {
                                                                    it.name.lowercase() ==
                                                                            label.name.lowercase()
                                                                }
                                                                .toSet()
                                                    } else {
                                                        selectedLabels + label
                                                    }
                                        },
                                        label = {
                                            Text(text = label.name, fontSize = 12.sp, maxLines = 1)
                                        },
                                        leadingIcon =
                                                if (isSelected) {
                                                    {
                                                        Icon(
                                                                Icons.Default.Check,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                } else {
                                                    {
                                                        Icon(
                                                                Icons.Default.Label,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                            onClick = {
                                onSelectedLabels(selectedLabels.toList())
                                showModal = false
                            }
                    ) { Text("Apply") }
                },
                dismissButton = { TextButton(onClick = { showModal = false }) { Text("Cancel") } }
        )
    }
}
