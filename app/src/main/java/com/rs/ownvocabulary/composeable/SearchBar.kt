package com.rs.ownvocabulary.composeable

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    placeholder: String = "Search words or meanings..."
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Animation states
    val animatedElevation by animateFloatAsState(
        targetValue = if (isFocused) 8f else 2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val containerColor by animateColorAsState(
        targetValue = if (isFocused)
            MaterialTheme.colorScheme.surfaceContainer
        else
            MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = tween(300)
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else
            Color.Transparent,
        animationSpec = tween(300)
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        shadowElevation = animatedElevation.dp,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = if (isFocused) {
                    Brush.linearGradient(
                        colors = listOf(
                            borderColor,
                            Color.Transparent
                        )
                    )
                } else {
                    SolidColor(Color.Transparent)
                },
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated Search Icon
            AnimatedContent(
                targetState = isFocused,
                transitionSpec = {
                    scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)).togetherWith(
                        scaleOut(spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    )
                }
            ) { focused ->
                Surface(
                    shape = CircleShape,
                    color = if (focused)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else
                        Color.Transparent,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = if (focused)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Custom Text Field
            Box(
                modifier = Modifier.weight(1f)
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { isFocused = it.isFocused },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                        }
                    )
                )

                // Animated Placeholder
                Column {
                    AnimatedVisibility(
                        visible = query.isEmpty(),
                        enter = fadeIn() + slideInHorizontally(),
                        exit = fadeOut() + slideOutHorizontally()
                    ) {
                        Text(
                            text = placeholder,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            // Animated Close Button
            AnimatedVisibility(
                visible = query.isNotEmpty() || isFocused,
                enter = slideInHorizontally { it } + fadeIn() + scaleIn(),
                exit = slideOutHorizontally { it } + fadeOut() + scaleOut()
            ) {
                Surface(
                    onClick = {
                        if (query.isNotEmpty()) {
                            onQueryChange("")
                        } else {
                            keyboardController?.hide()
                            onSearchToggle()
                        }
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
//                            indication = rememberRipple(bounded = false, radius = 18.dp)
                        ) {
                            if (query.isNotEmpty()) {
                                onQueryChange("")
                            } else {
                                keyboardController?.hide()
                                onSearchToggle()
                            }
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (query.isNotEmpty()) "Clear" else "Close Search",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

//// Alternative Modern Search Bar with Glassmorphism Effect
//@Composable
//fun GlassmorphismSearchBar(
//    query: String,
//    onQueryChange: (String) -> Unit,
//    onSearchToggle: () -> Unit,
//    placeholder: String = "Search your vocabulary..."
//) {
//    var isFocused by remember { mutableStateOf(false) }
//    val focusRequester = remember { FocusRequester() }
//    val keyboardController = LocalSoftwareKeyboardController.current
//
//    val scale by animateFloatAsState(
//        targetValue = if (isFocused) 1.02f else 1f,
//        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
//    )
//
//    LaunchedEffect(Unit) {
//        focusRequester.requestFocus()
//    }
//
//    Surface(
//        shape = RoundedCornerShape(24.dp),
//        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
//        shadowElevation = if (isFocused) 12.dp else 4.dp,
//        modifier = Modifier
//            .fillMaxWidth()
//            .scale(scale)
//            .background(
//                brush = Brush.linearGradient(
//                    colors = listOf(
//                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
//                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
//                        Color.Transparent
//                    )
//                ),
//                shape = RoundedCornerShape(24.dp)
//            )
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 24.dp, vertical = 18.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // Pulsing Search Icon
//            val infiniteTransition = rememberInfiniteTransition()
//            val pulseAlpha by infiniteTransition.animateFloat(
//                initialValue = if (isFocused) 0.3f else 1f,
//                targetValue = if (isFocused) 1f else 0.6f,
//                animationSpec = infiniteRepeatable(
//                    animation = tween(1500),
//                    repeatMode = RepeatMode.Reverse
//                )
//            )
//
//            Icon(
//                imageVector = Icons.Outlined.Search,
//                contentDescription = "Search",
//                tint = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
//                modifier = Modifier.size(22.dp)
//            )
//
//            // Enhanced Text Field
//            Box(modifier = Modifier.weight(1f)) {
//                BasicTextField(
//                    value = query,
//                    onValueChange = onQueryChange,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .focusRequester(focusRequester)
//                        .onFocusChanged { isFocused = it.isFocused },
//                    textStyle = TextStyle(
//                        color = MaterialTheme.colorScheme.onSurface,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        letterSpacing = 0.2.sp
//                    ),
//                    singleLine = true,
//                    cursorBrush = Brush.linearGradient(
//                        colors = listOf(
//                            MaterialTheme.colorScheme.primary,
//                            MaterialTheme.colorScheme.secondary
//                        )
//                    ),
//                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
//                    keyboardActions = KeyboardActions(
//                        onSearch = { keyboardController?.hide() }
//                    )
//                )
//
//                AnimatedVisibility(
//                    visible = query.isEmpty(),
//                    enter = fadeIn() + slideInVertically { -it/2 },
//                    exit = fadeOut() + slideOutVertically { -it/2 }
//                ) {
//                    Text(
//                        text = placeholder,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//
//            // Morphing Close Button
//            AnimatedVisibility(
//                visible = query.isNotEmpty() || isFocused,
//                enter = slideInHorizontally { it/2 } + fadeIn() + scaleIn(
//                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
//                ),
//                exit = slideOutHorizontally { it/2 } + fadeOut() + scaleOut()
//            ) {
//                Surface(
//                    onClick = {
//                        if (query.isNotEmpty()) {
//                            onQueryChange("")
//                        } else {
//                            keyboardController?.hide()
//                            onSearchToggle()
//                        }
//                    },
//                    shape = CircleShape,
//                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
//                    modifier = Modifier.size(36.dp)
//                ) {
//                    Box(
//                        contentAlignment = Alignment.Center,
//                        modifier = Modifier.fillMaxSize()
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Close,
//                            contentDescription = "Close",
//                            tint = MaterialTheme.colorScheme.error,
//                            modifier = Modifier.size(20.dp)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}