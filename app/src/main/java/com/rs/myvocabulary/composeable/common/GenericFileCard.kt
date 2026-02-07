package com.rs.myvocabulary.composeable.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GenericFileCard(
        fileUrl: String,
        fileName: String,
        fileType: String,
        onLongClick: () -> Unit = {}
) {
    val (icon, color, label) =
            when (fileType.lowercase()) {
                "doc", "docx" -> Triple(Icons.Default.Description, Color(0xFF2196F3), "DOC")
                "xls", "xlsx" -> Triple(Icons.Default.TableChart, Color(0xFF4CAF50), "Excel")
                "ppt", "pptx" -> Triple(Icons.Default.Slideshow, Color(0xFFFF9800), "PPT")
                "txt" -> Triple(Icons.Default.TextSnippet, Color(0xFF9E9E9E), "TXT")
                "zip", "rar" -> Triple(Icons.Default.FolderZip, Color(0xFF9C27B0), "ZIP")
                "audio", "mp3", "wav" -> Triple(Icons.Default.AudioFile, Color(0xFFE91E63), "Audio")
                else ->
                        Triple(
                                Icons.Default.InsertDriveFile,
                                Color(0xFF607D8B),
                                fileType.uppercase().take(3)
                        )
            }

    Surface(
            modifier =
                    Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .combinedClickable(
                                    onClick = { /* Open file */},
                                    onLongClick = onLongClick
                            ),
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                    modifier =
                            Modifier.size(56.dp)
                                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        icon,
                        contentDescription = fileType,
                        tint = color,
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
                    Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.15f)) {
                        Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                            text = "Tap to download",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                    Icons.Default.Download,
                    contentDescription = "Download",
                    tint = color,
                    modifier = Modifier.size(24.dp)
            )
        }
    }
}
