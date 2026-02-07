package com.rs.myvocabulary.composeable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.rs.myvocabulary.database.Comment
import com.rs.myvocabulary.database.Word
import com.rs.myvocabulary.utils.MediaPlaybackManager
import kotlin.apply
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.collections.isNotEmpty
import kotlin.collections.isNullOrEmpty
import kotlin.ranges.random
import kotlin.text.isNullOrEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailComments(
    comment: Comment,
    post: Word? = null,
    setReplyingToCommentId: (String?) -> Unit
) {
    CommentHierarchy(
        comment = comment,
        allComments = post?.comments ?: emptyList(),
        onReplyClick = { setReplyingToCommentId(it._id) }
    )
}

@Composable
fun CommentHierarchy(
    comment: Comment,
    allComments: List<Comment>,
    onReplyClick: (Comment) -> Unit,
    indentation: Int = 0
) {
    val replies =
        remember(comment, allComments) { allComments.filter { it.parentId == comment._id } }

    Column(
        modifier =
            Modifier.padding(
                start = if (indentation > 0) 40.dp else 0.dp,
                top = if (indentation > 0) 8.dp else 0.dp
            )
    ) {
        CommentItemDetail(comment, onReplyClick, indentation)

        if (replies.isNotEmpty()) {
            // Show replies with connecting line
            Column(modifier = Modifier.padding(start = 0.dp)) {
                replies.forEachIndexed { index, reply ->
                    CommentHierarchy(
                        comment = reply,
                        allComments = allComments,
                        onReplyClick = onReplyClick,
                        indentation = indentation + 1
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItemDetail(comment: Comment, onReplyClick: (Comment) -> Unit, indentation: Int = 0) {
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf((0..50).random()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Column(modifier = Modifier.weight(1f)) {
            Surface(
                shape =
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp
                    ),
                color =
                    if (indentation > 0)
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    else MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = comment.username,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if ((0..1).random() == 1) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint =
                                    MaterialTheme.colorScheme
                                        .primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = comment.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight =
                            MaterialTheme.typography
                                .bodyMedium
                                .lineHeight * 1.3
                    )

                    if (!comment.audioUrl.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AudioAttachmentItem(url = comment.audioUrl)
                    }

                    if (!comment.attachments.isNullOrEmpty()) {
                        comment.attachments.forEach { attachment ->
                            Spacer(modifier = Modifier.height(8.dp))
                            when (attachment.type) {
                                "image" -> {
                                    AsyncImage(
                                        model =
                                            attachment
                                                .url,
                                        contentDescription =
                                            "Attachment Image",
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(
                                                    200.dp
                                                )
                                                .clip(
                                                    RoundedCornerShape(
                                                        12.dp
                                                    )
                                                )
                                                .background(
                                                    MaterialTheme
                                                        .colorScheme
                                                        .surfaceVariant
                                                ),
                                        contentScale =
                                            ContentScale
                                                .Crop
                                    )
                                }

                                "audio" -> {
                                    AudioAttachmentItem(
                                        url = attachment.url
                                    )
                                }

                                "video" -> {
                                    val context =
                                        LocalContext
                                            .current
                                    val currentUrl by
                                    MediaPlaybackManager
                                        .currentUrl
                                        .collectAsState()
                                    val isAttached =
                                        currentUrl ==
                                                attachment
                                                    .url

                                    Surface(
                                        shape =
                                            RoundedCornerShape(
                                                12.dp
                                            ),
                                        color = Color.Black,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(
                                                    200.dp
                                                )
                                    ) {
                                        if (isAttached) {
                                            val exoPlayer =
                                                remember {
                                                    MediaPlaybackManager
                                                        .getPlayer(
                                                            context
                                                        )
                                                }
//                                            AndroidView(
//                                                factory = { ctx
//                                                    ->
//                                                    androidx.media3
//                                                        .ui
//                                                        .PlayerView(
//                                                            ctx
//                                                        )
//                                                        .apply {
//                                                            player =
//                                                                exoPlayer
//                                                            useController =
//                                                                true
//                                                        }
//                                                },
//                                                modifier =
//                                                    Modifier.fillMaxSize()
//                                            )
                                        } else {
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxSize()
                                                        .clickable {
                                                            MediaPlaybackManager
                                                                .play(
                                                                    context,
                                                                    attachment
                                                                        .url,
                                                                    isVideo =
                                                                        true
                                                                )
                                                        },
                                                contentAlignment =
                                                    Alignment
                                                        .Center
                                            ) {
                                                Icon(
                                                    Icons.Default
                                                        .PlayCircle,
                                                    contentDescription =
                                                        "Play Video",
                                                    tint =
                                                        Color.White,
                                                    modifier =
                                                        Modifier.size(
                                                            48.dp
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }

                                else -> {
                                    Surface(
                                        shape =
                                            RoundedCornerShape(
                                                12.dp
                                            ),
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .surfaceVariant,
                                        modifier =
                                            Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment =
                                                Alignment
                                                    .CenterVertically,
                                            modifier =
                                                Modifier.padding(
                                                    12.dp
                                                )
                                        ) {
                                            Icon(
                                                Icons.Default
                                                    .InsertDriveFile,
                                                contentDescription =
                                                    null,
                                                tint =
                                                    MaterialTheme
                                                        .colorScheme
                                                        .primary
                                            )
                                            Spacer(
                                                modifier =
                                                    Modifier.width(
                                                        8.dp
                                                    )
                                            )
                                            Text(
                                                "File Attachment",
                                                style =
                                                    MaterialTheme
                                                        .typography
                                                        .bodyMedium,
                                                fontWeight =
                                                    FontWeight
                                                        .Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Action buttons row
            Row(
                modifier =
                    Modifier.padding(start = 12.dp, top = 6.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timestamp
                Text(
                    text = comment.createdAt.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.7f
                        )
                )

                // Like button with count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier.clickable {
                            isLiked = !isLiked
                            likeCount =
                                if (isLiked) likeCount + 1
                                else kotlin.comparisons.maxOf(0, likeCount - 1)
                        }
                ) {
                    Icon(
                        if (isLiked) Icons.Filled.Favorite
                        else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint =
                            if (isLiked) Color(0xFFE91E63)
                            else
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    if (likeCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = likeCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight =
                                if (isLiked) FontWeight.Bold
                                else FontWeight.Normal,
                            color =
                                if (isLiked) Color(0xFFE91E63)
                                else
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                        )
                    }
                }

                // Reply button
                Text(
                    text = "Reply",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onReplyClick(comment) }
                )
            }
        }
    }
}

@Composable
fun AudioAttachmentItem(url: String) {
    val context = LocalContext.current
    val currentUrl by MediaPlaybackManager.currentUrl.collectAsState()
    val isPlayingGlobal by MediaPlaybackManager.isPlaying.collectAsState()

    val isPlaying = currentUrl == url && isPlayingGlobal

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            IconButton(
                onClick = {
                    MediaPlaybackManager.play(context, url, isVideo = false)
                },
                modifier =
                    Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause
                    else Icons.Default.PlayArrow,
                    contentDescription = "Play Audio",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Voice Message",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (isPlaying) "Playing..." else "Tap to play",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.GraphicEq,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
