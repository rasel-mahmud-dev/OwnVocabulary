package com.rs.myvocabulary.composeable

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rs.myvocabulary.utils.MediaPlaybackManager

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AudioAttachmentCard(audioUrl: String, fileName: String, onLongClick: () -> Unit = {}) {
        val context = LocalContext.current
        val currentUrl by MediaPlaybackManager.currentUrl.collectAsState()
        val isPlayingGlobal by MediaPlaybackManager.isPlaying.collectAsState()

        val isCurrentAudio = currentUrl == audioUrl
        val isPlaying = isCurrentAudio && isPlayingGlobal

        Surface(
                modifier =
                        Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .combinedClickable(onClick = {}, onLongClick = onLongClick),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
                Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        // Play/Pause Button
                        IconButton(
                                onClick = {
                                        MediaPlaybackManager.play(
                                                context,
                                                audioUrl,
                                                isVideo = false
                                        )
                                },
                                modifier =
                                        Modifier.size(48.dp)
                                                .background(
                                                        MaterialTheme.colorScheme.primary,
                                                        CircleShape
                                                )
                        ) {
                                Icon(
                                        imageVector =
                                                if (isPlaying) Icons.Filled.Pause
                                                else Icons.Filled.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                )
                        }

                        // Audio Info
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = fileName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                        Icon(
                                                Icons.Default.VolumeUp,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                                text =
                                                        if (isPlaying) "Playing now..."
                                                        else "Audio Attachment",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                        )
                                }
                        }

                        // Animated Visualizer (Simple)
                        if (isPlaying) {
                                Row(
                                        modifier = Modifier.height(20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.Bottom
                                ) {
                                        repeat(4) { index ->
                                                val height by
                                                        rememberInfiniteTransition()
                                                                .animateFloat(
                                                                        initialValue = 0.2f,
                                                                        targetValue = 1f,
                                                                        animationSpec =
                                                                                infiniteRepeatable(
                                                                                        animation =
                                                                                                tween(
                                                                                                        300 +
                                                                                                                index *
                                                                                                                        100
                                                                                                ),
                                                                                        repeatMode =
                                                                                                RepeatMode
                                                                                                        .Reverse
                                                                                )
                                                                )
                                                Box(
                                                        modifier =
                                                                Modifier.width(3.dp)
                                                                        .fillMaxHeight(height)
                                                                        .background(
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary,
                                                                                RoundedCornerShape(
                                                                                        2.dp
                                                                                )
                                                                        )
                                                )
                                        }
                                }
                        }
                }
        }
}
