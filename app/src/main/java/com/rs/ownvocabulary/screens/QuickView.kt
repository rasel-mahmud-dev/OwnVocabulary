package com.rs.ownvocabulary.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rs.ownvocabulary.ShareActivity
import com.rs.ownvocabulary.composeable.AddWordDialogShare
import com.rs.ownvocabulary.composeable.TopBar
import com.rs.ownvocabulary.database.SortOrder
import com.rs.ownvocabulary.database.SyncStatus
import com.rs.ownvocabulary.database.Word
import com.rs.ownvocabulary.database.WordPartial
import com.rs.ownvocabulary.viewmodels.AppViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickView(navHostController: NavHostController, appViewModel: AppViewModel) {
    var isGridView by remember { mutableStateOf(true) }
    val openAddWordDialog by appViewModel.openAddWordDialog.collectAsStateWithLifecycle()


    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val totalWord by appViewModel.totalWord.collectAsStateWithLifecycle()

    var favoriteWords by remember { mutableStateOf<List<Word>>(emptyList()) }
    var frequentViewWords by remember { mutableStateOf<List<Word>>(emptyList()) }

    // Animation states
    val fabScale by animateFloatAsState(
        targetValue = if (openAddWordDialog) 0.8f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    fun load() {
        appViewModel.db.getAllWords(SortOrder.CreatedAtDesc, true) {
            favoriteWords = it
        }
        appViewModel.db.getFrequentViewWords {
            frequentViewWords = it
        }
    }

    LaunchedEffect(Unit) {
        load()
    }


    fun toggleLove(word: Word) {
        appViewModel.updatePartial(
            WordPartial(
                uid = word.uid,
                syncStatus = SyncStatus.PENDING,
                isFavorite = !word.isFavorite
            )
        ) {
            if (it != null) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                return@updatePartial
            }
            appViewModel.loadWords()
            load() // Refresh the local data
            Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
        }
    }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                title = "Quick View",
                disableBack = true,
                onBackClick = { navHostController.popBackStack() },
                right = {},
                subTitle = "Fast browse mode",
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Animated FAB
            FloatingActionButton(
                onClick = { appViewModel.setAddWordDialog(true) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(56.dp)
                    .scale(fabScale)
                    .zIndex(1f),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Word",
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {


                // Quick Stats Section
//            item {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(bottom = 24.dp),
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    StatCard(
//                        title = "Favorites",
//                        count = favoriteWords.size,
//                        icon = Icons.Default.Favorite,
//                        color = Color(0xFFE91E63),
//                        modifier = Modifier.weight(1f)
//                    )
//
//                    StatCard(
//                        title = "Frequent",
//                        count = frequentViewWords.size,
//                        icon = Icons.Default.TrendingUp,
//                        color = Color(0xFF4CAF50),
//                        modifier = Modifier.weight(1f)
//                    )
//                }
//            }

                // Frequent Views Section
                if (frequentViewWords.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Frequently Viewed",
                            subtitle = "${frequentViewWords.size} words",
                            icon = Icons.Default.TrendingUp,
                            iconTint = Color(0xFF4CAF50)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            frequentViewWords.forEach { word ->
                                QuickWordCard(
                                    word = word,
                                    onToggleLove = { toggleLove(word) },
                                    onItemClick = {
                                        navHostController.navigate("word_detail/${word.uid}")
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // Favorites Section
                if (favoriteWords.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Favorites",
                            subtitle = "${favoriteWords.size} words",
                            icon = Icons.Default.Favorite,
                            iconTint = Color(0xFFE91E63)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            favoriteWords.forEach { word ->
                                QuickWordCard(
                                    word = word,
                                    onToggleLove = { toggleLove(word) },
                                    onItemClick = {
                                        navHostController.navigate("word_detail/${word.uid}")
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                if (favoriteWords.isEmpty() && frequentViewWords.isEmpty()) {
                    item {
                        EmptyState(
                            onAddWordClick = { appViewModel.setAddWordDialog(true) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = iconTint.copy(alpha = 0.1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .padding(8.dp)
                    .size(20.dp)
            )
        }

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyState(
    onAddWordClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(24.dp)
                    .size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No words yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start building your vocabulary by adding your first word",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddWordClick,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Word")
        }
    }
}

// Keeping your original QuickWordCard design exactly as is
@Composable
fun QuickWordCard(word: Word, onItemClick: () -> Unit, onToggleLove: () -> Unit) {
    var isFavorite by remember(word.isFavorite) { mutableStateOf(word.isFavorite) }

    val cardColors = when (word.proficiencyLevel) {
        "Beginner" -> listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))
        "Intermediate" -> listOf(Color(0xFFFB8C00), Color(0xFFFFD54F))
        "Advanced" -> listOf(Color(0xFF667eea), Color(0xFF764ba2))
        else -> listOf(Color(0xFF6B73FF), Color(0xFF9B59B6))
    }

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .drawBehind {
                drawLine(
                    color = cardColors.first(),
                    end = androidx.compose.ui.geometry.Offset(0f, 0f),
                    start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = 4.dp.toPx()
                )
            }
            .clickable {
                onItemClick()
            }
    ) {

        Row(
            modifier = Modifier
                .wrapContentWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            Text(
                text = word.word,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .clickable { onToggleLove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
