package com.rs.learnmedia.composeable.createPost

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ImagePreview(model: Any?, onRemove: () -> Unit) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))) {
                AsyncImage(
                        model = model,
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                )

                // Remove button
                IconButton(
                        onClick = onRemove,
                        modifier =
                                Modifier.align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        .size(32.dp)
                ) {
                        Icon(
                                Icons.Filled.Close,
                                contentDescription = "Remove",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                        )
                }
        }
}

@Composable
fun VideoPreview(fileName: String?, onRemove: () -> Unit) {
        Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().height(120.dp)
        ) {
                Box {
                        Row(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                                Box(
                                        modifier =
                                                Modifier.size(64.dp)
                                                        .background(
                                                                Color(0xFFE91E63)
                                                                        .copy(alpha = 0.2f),
                                                                RoundedCornerShape(8.dp)
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Icon(
                                                Icons.Filled.Videocam,
                                                contentDescription = null,
                                                tint = Color(0xFFE91E63),
                                                modifier = Modifier.size(32.dp)
                                        )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                fileName ?: "Video",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFE91E63).copy(alpha = 0.15f)
                                        ) {
                                                Text(
                                                        "VIDEO",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFE91E63),
                                                        modifier =
                                                                Modifier.padding(
                                                                        horizontal = 6.dp,
                                                                        vertical = 2.dp
                                                                )
                                                )
                                        }
                                }
                        }

                        IconButton(
                                onClick = onRemove,
                                modifier =
                                        Modifier.align(Alignment.TopEnd).padding(8.dp).size(32.dp)
                        ) {
                                Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                }
        }
}

@Composable
fun DocumentPreview(fileName: String?, label: String, color: Color, onRemove: () -> Unit) {
        Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().height(80.dp)
        ) {
                Box {
                        Row(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                                Box(
                                        modifier =
                                                Modifier.size(48.dp)
                                                        .background(
                                                                color.copy(alpha = 0.2f),
                                                                RoundedCornerShape(8.dp)
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Icon(
                                                if (label == "PDF") Icons.Filled.PictureAsPdf
                                                else if (label == "Audio") Icons.Filled.AudioFile
                                                else Icons.Filled.Description,
                                                contentDescription = null,
                                                tint = color,
                                                modifier = Modifier.size(28.dp)
                                        )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                fileName ?: "Document",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1
                                        )
                                        Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = color.copy(alpha = 0.15f)
                                        ) {
                                                Text(
                                                        label,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = color,
                                                        modifier =
                                                                Modifier.padding(
                                                                        horizontal = 6.dp,
                                                                        vertical = 2.dp
                                                                )
                                                )
                                        }
                                }
                        }

                        IconButton(
                                onClick = onRemove,
                                modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
                        ) {
                                Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(18.dp)
                                )
                        }
                }
        }
}

@Composable
fun GenericFilePreview(fileName: String?, onRemove: () -> Unit) {
        DocumentPreview(fileName, "FILE", Color(0xFF607D8B), onRemove)
}
