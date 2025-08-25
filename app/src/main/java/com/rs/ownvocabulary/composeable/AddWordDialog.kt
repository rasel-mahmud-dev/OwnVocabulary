package com.rs.ownvocabulary.composeable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rs.ownvocabulary.database.Word


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onAddWord: (Word) -> Unit
) {
    if (!showDialog) return

    var word by remember { mutableStateOf("") }
    var shortMeaning by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var examples by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }
    var proficiencyLevel by remember { mutableStateOf("Beginner") }
    var expanded by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    val proficiencyLevels = listOf("Beginner", "Intermediate", "Advanced")
    val proficiencyColors = mapOf(
        "Beginner" to Color(0xFF4CAF50),
        "Intermediate" to Color(0xFFFF9800),
        "Advanced" to Color(0xFFF44336)
    )

    // Animation for favorite button
    val favoriteScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.2f else 1f,
        animationSpec = tween(200),
        label = "favoriteScale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 16.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.BookmarkAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Add New Word",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Word Input
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it },
                    label = { Text("Word") },
                    placeholder = { Text("Enter the word...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = shortMeaning,
                    onValueChange = { shortMeaning = it },
                    label = { Text("Short Meaning") },
                    placeholder = { Text("Brief definition...") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Detailed Meaning") },
                    placeholder = { Text("Comprehensive explanation...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = examples,
                    onValueChange = { examples = it },
                    label = { Text("Examples") },
                    placeholder = { Text("Usage examples...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = proficiencyLevel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Proficiency Level") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = proficiencyColors[proficiencyLevel] ?: MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        proficiencyLevels.forEach { level ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    proficiencyColors[level] ?: Color.Gray,
                                                    RoundedCornerShape(6.dp)
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(level)
                                    }
                                },
                                onClick = {
                                    proficiencyLevel = level
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Favorite Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Add to Favorites",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = { isFavorite = !isFavorite },
                        modifier = Modifier.scale(favoriteScale)
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            if (word.isNotBlank()) {
                                val newWord = Word(
                                    word = word.trim(),
                                    shortMeaning = shortMeaning.trim(),
                                    details = details.trim(),
                                    examples = examples.trim(),
                                    isFavorite = isFavorite,
                                    proficiencyLevel = proficiencyLevel,
                                    viewCount = 0,
                                    lastViewedDaysAgo = 0
                                )
                                onAddWord(newWord)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = word.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Add Word", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // Auto-focus on word input when dialog opens
    LaunchedEffect(showDialog) {
        if (showDialog) {
            focusRequester.requestFocus()
        }
    }
}

// Usage Example Composable
@Composable
fun AddWordExample() {
    var showDialog by remember { mutableStateOf(false) }
    var wordsList by remember { mutableStateOf(emptyList<Word>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add New Word")
        }

        // Display added words count
        if (wordsList.isNotEmpty()) {
            Text(
                "Total Words: ${wordsList.size}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }

    AddWordDialog(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onAddWord = { newWord ->
            wordsList = wordsList + newWord
        }
    )
}