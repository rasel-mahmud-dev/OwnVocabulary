package com.rs.myvocabulary.composeable.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PDFAttachmentCard(fileUrl: String, fileName: String, onLongClick: () -> Unit = {}) {
    Surface(
            modifier =
                    Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .combinedClickable(
                                    onClick = { /* Open PDF */},
                                    onLongClick = onLongClick
                            ),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
    ) {
        Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                    modifier =
                            Modifier.size(56.dp)
                                    .background(
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp)
                                    ),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = "PDF",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    ) {
                        Text(
                                text = "PDF",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                            text = "Tap to view",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
            )
        }
    }
}
