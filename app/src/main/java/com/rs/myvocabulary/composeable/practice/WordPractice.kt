package com.rs.myvocabulary.composeable.practice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rs.myvocabulary.viewmodels.AppViewModel

@Composable
fun WordPractice(appViewModel: AppViewModel) {
    val currentWord by appViewModel.practiceCurrentWord.collectAsState()
    val isAnswerRevealed by appViewModel.practiceIsAnswerRevealed.collectAsState()
    val isLoading by appViewModel.practiceIsLoading.collectAsState()
    val sessionStats by appViewModel.practiceSessionStats.collectAsState()

    LaunchedEffect(Unit) { appViewModel.startPracticeSession() }

    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stats Header
        Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatsItem(
                    label = "Correct",
                    value = sessionStats.correctCount.toString(),
                    color = MaterialTheme.colorScheme.primary
            )
            StatsItem(
                    label = "Streak",
                    value = sessionStats.currentStreak.toString(),
                    color = MaterialTheme.colorScheme.tertiary
            )
            StatsItem(
                    label = "Total",
                    value = sessionStats.totalCardsReviewed.toString(),
                    color = MaterialTheme.colorScheme.secondary
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            currentWord?.let { word ->
                // Flashcard
                Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor =
                                                MaterialTheme.colorScheme.surfaceContainerHighest
                                ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                                text = word.word,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Audio button if available
                        IconButton(
                                onClick = { /* Play audio */},
                                modifier =
                                        Modifier.size(48.dp)
                                                .background(
                                                        MaterialTheme.colorScheme.primary.copy(
                                                                alpha = 0.1f
                                                        ),
                                                        RoundedCornerShape(12.dp)
                                                )
                        ) {
                            Icon(
                                    Icons.Default.VolumeUp,
                                    contentDescription = "Play pronunciation",
                                    tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        AnimatedVisibility(
                                visible = isAnswerRevealed,
                                enter = slideInVertically { it / 2 } + fadeIn(),
                                exit = slideOutVertically { it / 2 } + fadeOut()
                        ) {
                            Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                        text = word.shortMeaning,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.primary
                                )

                                if (word.details.isNotEmpty()) {
                                    Text(
                                            text = word.details,
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Controls
                Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!isAnswerRevealed) {
                        Button(
                                onClick = { appViewModel.revealAnswer() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                    "Show Answer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                    onClick = { appViewModel.markResult(false) },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors =
                                            ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error
                                            ),
                                    border =
                                            ButtonDefaults.outlinedButtonBorder.copy(
                                                    brush =
                                                            androidx.compose.ui.graphics.SolidColor(
                                                                    MaterialTheme.colorScheme.error
                                                            )
                                            )
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Forgot")
                            }

                            Button(
                                    onClick = { appViewModel.markResult(true) },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors =
                                            ButtonDefaults.buttonColors(
                                                    containerColor =
                                                            MaterialTheme.colorScheme.primary
                                            )
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Got it")
                            }
                        }
                    }
                }
            }
                    ?: run {
                        // Empty state or end of session
                        Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                    "Practice session complete!",
                                    style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { appViewModel.startPracticeSession() }) {
                                Text("Start New Session")
                            }
                        }
                    }
        }
    }
}

@Composable
private fun StatsItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
        )
        Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
