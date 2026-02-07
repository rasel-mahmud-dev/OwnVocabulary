package com.rs.myvocabulary.composeable

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
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
import java.io.File

data class AttachmentPreview(
        val uri: Uri? = null,
        val file: File? = null,
        val type: String // "image", "video", "audio", "file"
)

@Composable
fun CommentInputSection(
        commentText: String,
        onCommentTextChange: (String) -> Unit,
        replyingToUsername: String?,
        onCancelReply: () -> Unit,
        onRecordAudio: () -> Unit,
        onAttachment: () -> Unit = {},
        onSendComment: () -> Unit,
        isSubmitting: Boolean,
        userAvatarUrl: String? = null,
        userName: String = "You",
        // Replaced specific audioAttachment with generic binding
        attachments: List<AttachmentPreview> = emptyList(),
        onRemoveAttachment: (AttachmentPreview) -> Unit = {}
) {
        Surface(
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
        ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        // Reply indicator with cancel
                        if (replyingToUsername != null) {
                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(bottom = 12.dp)
                                                        .background(
                                                                MaterialTheme.colorScheme
                                                                        .primaryContainer.copy(
                                                                        alpha = 0.3f
                                                                ),
                                                                RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(
                                                                horizontal = 12.dp,
                                                                vertical = 8.dp
                                                        ),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                        Icons.AutoMirrored.Filled.Reply,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                        "Replying to $replyingToUsername",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        fontWeight = FontWeight.Medium
                                                )
                                        }
                                        IconButton(
                                                onClick = onCancelReply,
                                                modifier = Modifier.size(24.dp),
                                                colors =
                                                        IconButtonDefaults.iconButtonColors(
                                                                contentColor =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurface
                                                        )
                                        ) {
                                                Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Cancel",
                                                        modifier = Modifier.size(16.dp)
                                                )
                                        }
                                }
                        }

                        // Main comment input row with avatar
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                                // User Avatar
                                Box(
                                        modifier =
                                                Modifier.size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                                MaterialTheme.colorScheme
                                                                        .primaryContainer
                                                        )
                                ) {
                                        if (userAvatarUrl != null) {
                                                AsyncImage(
                                                        model = userAvatarUrl,
                                                        contentDescription = "Your avatar",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                )
                                        } else {
                                                // Default avatar with initials
                                                Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                ) {
                                                        Text(
                                                                text =
                                                                        userName.firstOrNull()
                                                                                ?.uppercase()
                                                                                ?: "U",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .titleMedium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onPrimaryContainer,
                                                                fontWeight = FontWeight.Bold
                                                        )
                                                }
                                        }
                                }

                                // Comment input and actions
                                Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                        // Attachment Previews
                                        if (attachments.isNotEmpty()) {
                                                LazyRow(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(bottom = 8.dp),
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(8.dp)
                                                ) {
                                                        items(attachments) { attachment ->
                                                                Surface(
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .secondaryContainer,
                                                                        shape =
                                                                                RoundedCornerShape(
                                                                                        8.dp
                                                                                ),
                                                                        modifier =
                                                                                Modifier.widthIn(
                                                                                        max = 200.dp
                                                                                )
                                                                ) {
                                                                        Row(
                                                                                modifier =
                                                                                        Modifier.padding(
                                                                                                8.dp
                                                                                        ),
                                                                                verticalAlignment =
                                                                                        Alignment
                                                                                                .CenterVertically
                                                                        ) {
                                                                                if (attachment
                                                                                                .type ==
                                                                                                "image" &&
                                                                                                attachment
                                                                                                        .uri !=
                                                                                                        null
                                                                                ) {
                                                                                        AsyncImage(
                                                                                                model =
                                                                                                        attachment
                                                                                                                .uri,
                                                                                                contentDescription =
                                                                                                        "Attachment",
                                                                                                modifier =
                                                                                                        Modifier.size(
                                                                                                                        40.dp
                                                                                                                )
                                                                                                                .clip(
                                                                                                                        RoundedCornerShape(
                                                                                                                                4.dp
                                                                                                                        )
                                                                                                                ),
                                                                                                contentScale =
                                                                                                        ContentScale
                                                                                                                .Crop
                                                                                        )
                                                                                } else {
                                                                                        Icon(
                                                                                                when (attachment
                                                                                                                .type
                                                                                                ) {
                                                                                                        "audio" ->
                                                                                                                Icons.Default
                                                                                                                        .Audiotrack
                                                                                                        "video" ->
                                                                                                                Icons.Default
                                                                                                                        .Videocam
                                                                                                        "image" ->
                                                                                                                Icons.Default
                                                                                                                        .Image
                                                                                                        else ->
                                                                                                                Icons.Default
                                                                                                                        .AttachFile
                                                                                                },
                                                                                                contentDescription =
                                                                                                        null,
                                                                                                modifier =
                                                                                                        Modifier.size(
                                                                                                                20.dp
                                                                                                        ),
                                                                                                tint =
                                                                                                        MaterialTheme
                                                                                                                .colorScheme
                                                                                                                .onSecondaryContainer
                                                                                        )
                                                                                }
                                                                                Spacer(
                                                                                        modifier =
                                                                                                Modifier.width(
                                                                                                        8.dp
                                                                                                )
                                                                                )
                                                                                Text(
                                                                                        when (attachment
                                                                                                        .type
                                                                                        ) {
                                                                                                "audio" ->
                                                                                                        "Voice"
                                                                                                "image" ->
                                                                                                        "Image"
                                                                                                "video" ->
                                                                                                        "Video"
                                                                                                else ->
                                                                                                        "File"
                                                                                        },
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .bodySmall,
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSecondaryContainer
                                                                                )
                                                                                IconButton(
                                                                                        onClick = {
                                                                                                onRemoveAttachment(
                                                                                                        attachment
                                                                                                )
                                                                                        },
                                                                                        modifier =
                                                                                                Modifier.size(
                                                                                                        24.dp
                                                                                                )
                                                                                ) {
                                                                                        Icon(
                                                                                                Icons.Default
                                                                                                        .Close,
                                                                                                contentDescription =
                                                                                                        "Remove",
                                                                                                modifier =
                                                                                                        Modifier.size(
                                                                                                                16.dp
                                                                                                        ),
                                                                                                tint =
                                                                                                        MaterialTheme
                                                                                                                .colorScheme
                                                                                                                .onSecondaryContainer
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }

                                        // Text input field
                                        TextField(
                                                value = commentText,
                                                onValueChange = onCommentTextChange,
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .heightIn(min = 48.dp)
                                                                .border(
                                                                        width = 1.dp,
                                                                        color =
                                                                                if (commentText
                                                                                                .isNotBlank()
                                                                                )
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .primary
                                                                                else
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .outline
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.3f
                                                                                                ),
                                                                        shape =
                                                                                RoundedCornerShape(
                                                                                        24.dp
                                                                                )
                                                                ),
                                                placeholder = {
                                                        Text(
                                                                if (replyingToUsername == null)
                                                                        "Add a comment..."
                                                                else "Add a reply...",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyMedium
                                                        )
                                                },
                                                colors =
                                                        TextFieldDefaults.colors(
                                                                focusedContainerColor =
                                                                        Color.Transparent,
                                                                unfocusedContainerColor =
                                                                        Color.Transparent,
                                                                disabledContainerColor =
                                                                        Color.Transparent,
                                                                focusedIndicatorColor =
                                                                        Color.Transparent,
                                                                unfocusedIndicatorColor =
                                                                        Color.Transparent,
                                                                disabledIndicatorColor =
                                                                        Color.Transparent,
                                                        ),
                                                shape = RoundedCornerShape(24.dp),
                                                textStyle = MaterialTheme.typography.bodyMedium
                                        )

                                        // Action buttons row
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                // Left side: Mic and Attachment icons
                                                Row(
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(4.dp)
                                                ) {
                                                        IconButton(
                                                                onClick = onRecordAudio,
                                                                modifier = Modifier.size(40.dp),
                                                                colors =
                                                                        IconButtonDefaults
                                                                                .iconButtonColors(
                                                                                        contentColor =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSurface
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.6f
                                                                                                        )
                                                                                )
                                                        ) {
                                                                Icon(
                                                                        Icons.Default.Mic,
                                                                        contentDescription =
                                                                                "Record Audio",
                                                                        modifier =
                                                                                Modifier.size(22.dp)
                                                                )
                                                        }

                                                        IconButton(
                                                                onClick = onAttachment,
                                                                modifier = Modifier.size(40.dp),
                                                                colors =
                                                                        IconButtonDefaults
                                                                                .iconButtonColors(
                                                                                        contentColor =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSurface
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.6f
                                                                                                        )
                                                                                )
                                                        ) {
                                                                Icon(
                                                                        Icons.Default.AttachFile,
                                                                        contentDescription =
                                                                                "Attach File",
                                                                        modifier =
                                                                                Modifier.size(22.dp)
                                                                )
                                                        }
                                                }

                                                // Right side: Comment/Post button
                                                Button(
                                                        onClick = onSendComment,
                                                        enabled =
                                                                !isSubmitting &&
                                                                        (commentText.isNotBlank() ||
                                                                                attachments
                                                                                        .isNotEmpty()),
                                                        modifier = Modifier.height(36.dp),
                                                        shape = RoundedCornerShape(18.dp),
                                                        contentPadding =
                                                                PaddingValues(
                                                                        horizontal = 20.dp,
                                                                        vertical = 0.dp
                                                                ),
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        disabledContainerColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.3f
                                                                                        ),
                                                                        disabledContentColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onPrimary
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.5f
                                                                                        )
                                                                )
                                                ) {
                                                        if (isSubmitting) {
                                                                CircularProgressIndicator(
                                                                        modifier =
                                                                                Modifier.size(
                                                                                        18.dp
                                                                                ),
                                                                        strokeWidth = 2.dp,
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onPrimary
                                                                )
                                                        } else {
                                                                Text(
                                                                        if (replyingToUsername ==
                                                                                        null
                                                                        )
                                                                                "Comment"
                                                                        else "Reply",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .labelLarge,
                                                                        fontWeight =
                                                                                FontWeight.SemiBold
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
}

// Optional: Divider to separate from content above
@Composable
fun CommentInputDivider() {
        HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
}
