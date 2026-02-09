package com.rs.myvocabulary.composeable.detail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rs.myvocabulary.utils.formatDate

@Composable
fun ArticleTitleCard(
        isReadOnly: Boolean,
        title: String,
        createdAt: Long,
        onTitleChange: (String) -> Unit
) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
        ) {
                Column(modifier = Modifier.padding(20.dp)) {
                        if (isReadOnly) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text = title,
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                                onClick = {
                                                        com.rs.myvocabulary.TTSManager.speak(title)
                                                }
                                        ) {
                                                Icon(
                                                        imageVector =
                                                                Icons.AutoMirrored.Filled.VolumeUp,
                                                        contentDescription = "Speak"
                                                )
                                        }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                        text = formatDate(createdAt),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        } else {
                                OutlinedTextField(
                                        value = title,
                                        onValueChange = onTitleChange,
                                        label = { Text("Title") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                )
                        }
                }
        }
}
