package com.rs.learnmedia.composeable

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.rs.myvocabulary.utils.AudioRecorder
import com.rs.myvocabulary.utils.MediaPlaybackManager
import java.io.File

@Composable
fun AudioRecordingDialog(onDismiss: () -> Unit, onSendAudio: (File) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    val recorder = remember { AudioRecorder(context) }
    val currentUrl by MediaPlaybackManager.currentUrl.collectAsState()
    val isPlayingGlobal by MediaPlaybackManager.isPlaying.collectAsState()

    val isPlayingPreview =
            audioFile != null && currentUrl == audioFile!!.toUri().toString() && isPlayingGlobal

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 8.dp,
                modifier = Modifier.padding(16.dp)
        ) {
            Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                        text =
                                when {
                                    isRecording -> "Recording..."
                                    audioFile != null -> "Preview Audio"
                                    else -> "Audio Comment"
                                },
                        style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isRecording) {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                } else {
                    Icon(
                            if (audioFile == null) Icons.Default.Mic else Icons.Default.Audiotrack,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Button states based on recording status
                when {
                    // State 1: Not started recording
                    !isRecording && audioFile == null -> {
                        Button(
                                onClick = {
                                    val file =
                                            File(
                                                    context.cacheDir,
                                                    "comment_audio_${System.currentTimeMillis()}.m4a"
                                            )
                                    audioFile = file
                                    recorder.start(file)
                                    isRecording = true
                                }
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Recording")
                        }
                    }

                    // State 2: Currently recording
                    isRecording -> {
                        Button(
                                onClick = {
                                    recorder.stop()
                                    isRecording = false
                                },
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                        )
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Stop Recording")
                        }
                    }

                    // State 3: Recording completed, show preview and submit
                    audioFile != null -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Preview button
                            OutlinedButton(
                                    onClick = {
                                        MediaPlaybackManager.play(
                                                context,
                                                audioFile!!.toUri(),
                                                isVideo = false
                                        )
                                    }
                            ) {
                                Icon(
                                        if (isPlayingPreview) Icons.Default.Stop
                                        else Icons.Default.PlayArrow,
                                        contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isPlayingPreview) "Stop" else "Preview")
                            }

                            // Submit button
                            Button(
                                    onClick = {
                                        MediaPlaybackManager.stop()
                                        onSendAudio(audioFile!!)
                                    }
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Submit")
                            }
                        }

                        // Retake button below
                        TextButton(
                                onClick = {
                                    MediaPlaybackManager.stop()
                                    audioFile?.delete()
                                    audioFile = null
                                },
                                modifier = Modifier.padding(top = 8.dp)
                        ) { Text("Retake") }
                    }
                }

                TextButton(
                        onClick = {
                            MediaPlaybackManager.stop()
                            recorder.stop()
                            onDismiss()
                        },
                        modifier = Modifier.padding(top = 8.dp)
                ) { Text("Cancel") }
            }
        }
    }
}
