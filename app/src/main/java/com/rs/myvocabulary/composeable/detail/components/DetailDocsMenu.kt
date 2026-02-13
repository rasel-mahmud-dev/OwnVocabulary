package com.rs.myvocabulary.composeable.detail.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DetailDocsMenu(
        expanded: Boolean,
        onDismissRequest: () -> Unit,
        onSharePost: () -> Unit,
        onShareMedia: () -> Unit,
        onAiLabelGenerate: () -> Unit,
        onAiStoryMake: () -> Unit,
        onAiPostEnhance: () -> Unit,
        isAiLabelGenerating: Boolean,
        onAiExample: () -> Unit,
        isGeneratingExample: Boolean,
        onAiMixBanglishForVocabullary: () -> Unit,
        onDelete: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
        DropdownMenuItem(
                text = { Text("Share Post") },
                onClick = {
                    onDismissRequest()
                    onSharePost()
                },
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
        )

        DropdownMenuItem(
                text = { Text("Share Post Media") },
                onClick = {
                    onDismissRequest()
                    onShareMedia()
                },
                leadingIcon = { Icon(Icons.Filled.PermMedia, contentDescription = null) }
        )

        DropdownMenuItem(
                text = {
                    if (isAiLabelGenerating) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating Labels...")
                        }
                    } else {
                        Text("Ai Label Generate")
                    }
                },
                onClick = {
                    onDismissRequest()
                    onAiLabelGenerate()
                },
                enabled = !isAiLabelGenerating,
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null) }
        )

        DropdownMenuItem(
                text = {
                    if (isGeneratingExample) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating...")
                        }
                    } else {
                        Text("Write Example Sentance by AI.")
                    }
                },
                onClick = {
                    onDismissRequest()
                    onAiExample()
                },
                enabled = !isGeneratingExample,
                leadingIcon = { Icon(Icons.Filled.AutoFixHigh, contentDescription = null) }
        )
        DropdownMenuItem(
                text = { Text("Make AI Story.") },
                onClick = {
                    onDismissRequest()
                    onAiStoryMake()
                },
                leadingIcon = { Icon(Icons.Filled.AutoFixNormal, contentDescription = null) }
        )

        DropdownMenuItem(
                text = { Text("Post Enhance (Detailed)") },
                onClick = {
                    onDismissRequest()
                    onAiPostEnhance()
                },
                leadingIcon = { Icon(Icons.Filled.AutoFixHigh, contentDescription = null) }
        )

        DropdownMenuItem(
                text = { Text("Ai Banglish Mix") },
                onClick = {
                    onDismissRequest()
                    onAiMixBanglishForVocabullary()
                },
                leadingIcon = { Icon(Icons.Filled.Translate, contentDescription = null) }
        )

        DropdownMenuItem(
                text = { Text("Delete Post") },
                onClick = {
                    onDismissRequest()
                    onDelete()
                },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
        )
    }
}
