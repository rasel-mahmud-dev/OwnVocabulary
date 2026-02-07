package com.rs.myvocabulary.composeable.detail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ArticleContentCard(isReadOnly: Boolean, content: String, onContentChange: (String) -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (isReadOnly) {
                SelectionContainer {
                    Text(
                            text = content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                    )
                }
            } else {
                OutlinedTextField(
                        value = content,
                        onValueChange = onContentChange,
                        label = { Text("Content") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 10,
                        shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}
