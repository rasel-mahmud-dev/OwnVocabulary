package com.rs.ownvocabulary.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rs.ownvocabulary.composeable.AddWordDialogShare
import com.rs.ownvocabulary.database.SyncStatus
import com.rs.ownvocabulary.database.Word
import com.rs.ownvocabulary.database.WordPartial
import com.rs.ownvocabulary.viewmodels.AppViewModel
import com.rs.ownvocabulary.composeable.SearchBar

@Composable
fun Vocabulary(navHostController: NavHostController, appViewModel: AppViewModel) {
    var isGridView by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    val openAddWordDialog by appViewModel.openAddWordDialog.collectAsStateWithLifecycle()



    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val totalWord by appViewModel.totalWord.collectAsStateWithLifecycle()
    val words by appViewModel.words.collectAsStateWithLifecycle()

    val filteredWords = remember(words, searchQuery) {
        if (searchQuery.isEmpty()) {
            words
        } else {
            words.filter {
                it.word.contains(searchQuery, ignoreCase = true) ||
                        it.shortMeaning.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Animation states
    val fabScale by animateFloatAsState(
        targetValue = if (openAddWordDialog) 0.8f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    // Scroll states for performance
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()


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
            Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Animated FAB
        FloatingActionButton(
            onClick = {  appViewModel.setAddWordDialog(true) },
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = showSearch,
                transitionSpec = {
                    (slideInVertically { -it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                }
            ) { isSearching ->
                if (isSearching) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearchToggle = { showSearch = false }
                    )
                } else {
                    HeaderSection(
                        totalWords = totalWord,
                        isGridView = isGridView,
                        onViewToggle = { isGridView = !isGridView },
                        onSearchToggle = { showSearch = true },
                        filteredCount = filteredWords.size
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (searchQuery.isNotEmpty()) {
                ResultsSummary(
                    query = searchQuery,
                    totalResults = filteredWords.size,
                    totalWords = words.size,
                    onClearSearch = {
                        searchQuery = ""
                        showSearch = false
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (filteredWords.isEmpty()) {
                if (searchQuery.isNotEmpty()) {
                    SearchEmptyState(
                        query = searchQuery,
                        onClearSearch = {
                            searchQuery = ""
                            showSearch = false
                        },
                        onAddWord = { appViewModel.setAddWordDialog(true) }
                    )
                } else {
                    MainEmptyState(
                        onAddWordClick = { appViewModel.setAddWordDialog(true) }
                    )
                }
            } else {
                WordsContent(
                    words = filteredWords,
                    isGridView = isGridView,
                    listState = listState,
                    gridState = gridState,
                    onToggleLove = ::toggleLove,
                    onWordClick = { word ->
                        navHostController.navigate("word_detail/${word.uid}")
                    }
                )
            }
        }
    }
}

@Composable
fun HeaderSection(
    totalWords: Int,
    isGridView: Boolean,
    onViewToggle: () -> Unit,
    onSearchToggle: () -> Unit,
    filteredCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Vocabulary",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "$totalWords words",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "• Your collection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search Button
            Surface(
                onClick = onSearchToggle,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shadowElevation = 1.dp
            ) {
                Box(
                    modifier = Modifier.padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // View Toggle Button
            Surface(
                onClick = onViewToggle,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shadowElevation = 1.dp,
                modifier = Modifier.animateContentSize()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.GridView else Icons.AutoMirrored.Filled.ViewList,
                        contentDescription = "Toggle View",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isGridView) "Grid" else "List",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}



@Composable
fun ResultsSummary(
    query: String,
    totalResults: Int,
    totalWords: Int,
    onClearSearch: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Search Results",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$totalResults of $totalWords words found for \"$query\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(
                onClick = onClearSearch,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "Clear",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WordsContent(
    words: List<Word>,
    isGridView: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    onToggleLove: (Word) -> Unit,
    onWordClick: (Word) -> Unit
) {
    AnimatedContent(
        targetState = isGridView,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)).togetherWith(fadeOut(animationSpec = tween(300)))
        }
    ) { useGrid ->
        if (useGrid) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                words.forEach { word ->
                    QuickWordCard(
                        word = word,
                        onToggleLove = { onToggleLove(word) },
                        onItemClick = { onWordClick(word) }
                    )
                }
            }

        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(words, key = { it.uid }) { word ->
                    QuickWordCard(
                        word = word,
                        onToggleLove = { onToggleLove(word) },
                        onItemClick = { onWordClick(word) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchEmptyState(
    query: String,
    onClearSearch: () -> Unit,
    onAddWord: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(24.dp)
                    .size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No results found",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No words found for \"$query\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onClearSearch,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Clear Search")
            }

            Button(
                onClick = onAddWord,
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
}

@Composable
fun MainEmptyState(
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
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(24.dp)
                    .size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Start Your Dictionary",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add your first word to begin building your personal vocabulary",
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
            Text("Add First Word")
        }
    }
}