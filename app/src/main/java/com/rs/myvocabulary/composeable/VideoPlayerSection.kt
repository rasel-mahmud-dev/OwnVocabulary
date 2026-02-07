package com.rs.myvocabulary.composeable

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.rs.myvocabulary.utils.MediaPlaybackManager
import com.rs.myvocabulary.utils.PlaybackPreferenceManager
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerSection(videoUrl: String, onLongClick: () -> Unit = {}) {
    val context = LocalContext.current
    val exoPlayer = remember { MediaPlaybackManager.getPlayer(context) }

    val currentUrl by MediaPlaybackManager.currentUrl.collectAsState()
    val isAttached = currentUrl == videoUrl

    var isPlaying by remember { mutableStateOf(false) }
    var playbackPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var is2xSpeed by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }

    LaunchedEffect(isAttached, isPlaying) {
        if (isAttached) {
            while (true) {
                playbackPosition = exoPlayer.currentPosition
                duration = exoPlayer.duration.coerceAtLeast(0L)

                // Save position periodically if playing
                if (isPlaying && playbackPosition > 0) {
                    PlaybackPreferenceManager.savePosition(context, videoUrl, playbackPosition)
                }

                delay(500)
            }
        }
    }

    DisposableEffect(isAttached) {
        val listener =
                object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        if (isAttached) {
                            isPlaying = playing
                        }
                    }
                    override fun onPlaybackParametersChanged(
                            playbackParameters: PlaybackParameters
                    ) {
                        if (isAttached) {
                            is2xSpeed = playbackParameters.speed > 1.0f
                        }
                    }
                }
        if (isAttached) {
            exoPlayer.addListener(listener)
        }
        onDispose { exoPlayer.removeListener(listener) }
    }

    Box(
            modifier =
                    Modifier.fillMaxWidth().height(300.dp).background(Color.Black).pointerInput(
                                    videoUrl
                            ) {
                        detectTapGestures(
                                onTap = { showControls = !showControls },
                                onLongPress = { onLongClick() },
                                onPress = {
                                    awaitRelease()
                                    if (isAttached) {
                                        exoPlayer.setPlaybackSpeed(1.0f)
                                    }
                                }
                        )
                    },
            contentAlignment = Alignment.Center
    ) {
        if (isAttached) {
//            AndroidView(
//                    factory = { ctx ->
//                        androidx.media3.ui.PlayerView(ctx).apply {
//                            player = exoPlayer
//                            useController = false
//                            resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
//                        }
//                    },
//                    modifier = Modifier.fillMaxSize(),
//                    update = { view -> view.player = exoPlayer }
//            )
        } else {
            Box(
                    modifier = Modifier.fillMaxSize().background(Color.DarkGray),
                    contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { MediaPlaybackManager.play(context, videoUrl) }) {
                    Icon(
                            Icons.Filled.PlayArrow,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // 2x Speed Indicator
        if (is2xSpeed && isAttached) {
            Surface(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                        "2x Speed",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // Overlay Controls
        AnimatedVisibility(
                visible = (showControls || !isPlaying) && isAttached,
                enter = fadeIn(),
                exit = fadeOut()
        ) {
            Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
            ) {
                IconButton(
                        onClick = { if (isPlaying) exoPlayer.pause() else exoPlayer.play() },
                        modifier =
                                Modifier.size(72.dp)
                                        .background(
                                                MaterialTheme.colorScheme.surface.copy(
                                                        alpha = 0.95f
                                                ),
                                                CircleShape
                                        )
                                        .border(
                                                2.dp,
                                                MaterialTheme.colorScheme.primary.copy(
                                                        alpha = 0.5f
                                                ),
                                                CircleShape
                                        )
                ) {
                    Icon(
                            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Tap to pause if playing (alternative to button)
        if (isPlaying && isAttached && !showControls) {
            Box(modifier = Modifier.fillMaxSize().clickable { exoPlayer.pause() })
        }

        // Seek Bar
        if (isAttached) {
            Box(
                    modifier =
                            Modifier.fillMaxSize()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 40.dp),
                    contentAlignment = Alignment.BottomCenter
            ) {
                Slider(
                        value = playbackPosition.toFloat(),
                        onValueChange = { exoPlayer.seekTo(it.toLong()) },
                        valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                        colors =
                                SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                )
                )
            }
        }

        // Tag Overlay
        Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
        ) {
            Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                        Icons.Default.Videocam,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                )
                Text(
                        "Video",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
