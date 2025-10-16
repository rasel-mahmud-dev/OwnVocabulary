package com.rs.ownvocabulary.composeable.detail

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rs.ownvocabulary.TTSManager
import com.rs.ownvocabulary.database.Word
import com.rs.ownvocabulary.R
import com.rs.ownvocabulary.ai.AIIndex
import com.rs.ownvocabulary.viewmodels.AppViewModel
import kotlinx.coroutines.launch

@Composable
fun WordHeaderCard(
    viewModel: AppViewModel,
    word: Word,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    setEditItem: () -> Unit,
) {

    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }

    val aiResponse by viewModel.aiResponse.collectAsStateWithLifecycle()
    val shortMeaning = aiResponse?.shortMeaning ?: ""

    fun handleSave() {
        viewModel.saveAiResponse(word.uid)
    }

    fun handleDiscard() {
        viewModel.setAiResponse(
            wordId = word.uid,
            shortMeaning = ""
        )
    }

    fun handleGenerateFromAi() {
        if (isLoading) return

        scope.launch {
            isLoading = true
            AIIndex.getTranslation(word.word).fold(
                onSuccess = { result ->
                    viewModel.setAiResponse(
                        wordId = word.uid,
                        shortMeaning = result
                    )
                    isLoading = false
                },
                onFailure = {
                    isLoading = false
                }
            )
        }
    }

    fun handlePlaySound(word: String) {
        TTSManager.speak(word)
    }

    val cardColors = when (word.proficiencyLevel) {
        "Beginner" -> listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))
        "Intermediate" -> listOf(Color(0xFFFB8C00), Color(0xFFFFD54F))
        "Advanced" -> listOf(Color(0xFF667eea), Color(0xFF764ba2))
        else -> listOf(Color(0xFF6B73FF), Color(0xFF9B59B6))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .drawBehind {
                drawLine(
                    color = cardColors.first(),
                    end = androidx.compose.ui.geometry.Offset(0f, 0f),
                    start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = 8.dp.toPx()
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(40.dp)
                ) {
                    val animatedScale by animateFloatAsState(
                        targetValue = if (isFavorite) 1.3f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "favorite_scale"
                    )

                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale)
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (word.type.isNotEmpty()) {
                        Text(
                            text = "/${word.type}/",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { handlePlaySound(word.word) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayCircle,
                            contentDescription = "Pronounce",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    if (shortMeaning.isEmpty()) {
                        Text(
                            text = word.shortMeaning ?: "No meaning",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = ::handleGenerateFromAi,
                            enabled = !isLoading,
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.dp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.wand_stars),
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Gen", style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        Text(
                            text = shortMeaning,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = ::handleDiscard,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Discard",
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        IconButton(
                            onClick = ::handleSave,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Save",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chips Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = word.type.ifEmpty { "Word" }.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Label,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = word.proficiencyLevel,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.OfflinePin,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                AssistChip(
                    onClick = { setEditItem() },
                    label = {
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}