package com.rs.myvocabulary.composeable.detail

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rs.myvocabulary.TTSManager

@Composable
fun WordHeaderCard(
    setWordStr: (String) -> Unit,
    setWordMeaning: (String) -> Unit,
    wordStr: String,
    shortMeaning: String,
    isNew: Boolean,
    isReadOnly: Boolean,
    onSave: () -> Unit,
    isFavorite: Boolean,
    syncStatus: String,
    onFavoriteToggle: () -> Unit,
    setEditItem: () -> Unit,
) {
    fun handlePlaySound(word: String) {
        TTSManager.speak(word)
    }

    val primary = colorScheme.primaryContainer
    val secondary = colorScheme.secondaryContainer

    // Animation for card entrance
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "card_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = colorScheme.primary.copy(alpha = 0.1f),
                spotColor = colorScheme.primary.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.surfaceContainer,
                        colorScheme.surfaceContainerLow
                    )
                )
            )
    ) {
        // Animated accent bar at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            primary,
                            secondary,
                            primary
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(top = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Word input with enhanced styling
                    BasicTextField(
                        interactionSource = remember { MutableInteractionSource() },
                        readOnly = isReadOnly,
                        value = wordStr,
                        onValueChange = setWordStr,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            lineHeight = 32.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        cursorBrush = SolidColor(colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box {
                                if (wordStr.isEmpty()) {
                                    Text(
                                        text = "Enter word...",
                                        style = TextStyle(
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurface.copy(alpha = 0.3f),
                                            lineHeight = 32.sp,
                                            letterSpacing = (-0.5).sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    // Meaning input with enhanced styling
                    BasicTextField(
                        interactionSource = remember { MutableInteractionSource() },
                        readOnly = isReadOnly,
                        value = shortMeaning,
                        onValueChange = setWordMeaning,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp,
                            letterSpacing = 0.1.sp
                        ),
                        cursorBrush = SolidColor(colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box {
                                if (shortMeaning.isEmpty()) {
                                    Text(
                                        text = "Add a brief definition...",
                                        style = TextStyle(
                                            fontSize = 15.sp,
                                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                            lineHeight = 22.sp,
                                            letterSpacing = 0.1.sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Enhanced favorite button with better animation
                Surface(
                    onClick = onFavoriteToggle,
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFavorite)
                        colorScheme.errorContainer.copy(alpha = 0.3f)
                    else
                        Color.Transparent,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        val animatedScale by animateFloatAsState(
                            targetValue = if (isFavorite) 1.2f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "favorite_scale"
                        )

                        val animatedRotation by animateFloatAsState(
                            targetValue = if (isFavorite) 0f else -10f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "favorite_rotation"
                        )

                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) colorScheme.error else colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer(
                                    scaleX = animatedScale,
                                    scaleY = animatedScale,
                                    rotationZ = animatedRotation
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Enhanced action chips with better spacing and design
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                // Speak chip with gradient background
                ElevatedAssistChip(
                    onClick = { handlePlaySound(wordStr) },
                    label = {
                        Text(
                            text = "Speak",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = AssistChipDefaults.elevatedAssistChipColors(
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.6f),
                        labelColor = colorScheme.onPrimaryContainer,
                        leadingIconContentColor = colorScheme.onPrimaryContainer
                    )
                )

                // Edit chip (conditionally shown)
                AnimatedVisibility(
                    visible = !isNew,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    ElevatedAssistChip(
                        onClick = setEditItem,
                        label = {
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = AssistChipDefaults.elevatedAssistChipColors(
                            containerColor = colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            labelColor = colorScheme.onSecondaryContainer,
                            leadingIconContentColor = colorScheme.onSecondaryContainer
                        )
                    )
                }

                // Sync status chip
                AssistChip(
                    onClick = { setEditItem() },
                    label = {
                        Text(
                            text = syncStatus,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },

                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color.Transparent,
                        labelColor = colorScheme.onSurface,
                        leadingIconContentColor = colorScheme.onSurfaceVariant
                    )
                )

                // Save chip (conditionally shown)
                AnimatedVisibility(
                    visible = !isReadOnly,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    ElevatedAssistChip(
                        onClick = onSave,
                        label = {
                            Text(
                                text = "Save",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = AssistChipDefaults.elevatedAssistChipColors(
                            containerColor = colorScheme.tertiaryContainer,
                            labelColor = colorScheme.onTertiaryContainer,
                            leadingIconContentColor = colorScheme.onTertiaryContainer
                        ),
                        elevation = AssistChipDefaults.elevatedAssistChipElevation(
                            elevation = 2.dp
                        )
                    )
                }
            }
        }
    }
}