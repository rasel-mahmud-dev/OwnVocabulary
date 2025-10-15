package com.rs.ownvocabulary.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rs.ownvocabulary.GoogleSignInScreen
import com.rs.ownvocabulary.composeable.LoginRequired
import com.rs.ownvocabulary.composeable.QuickWordCard
import com.rs.ownvocabulary.database.SyncStatus
import com.rs.ownvocabulary.database.Word
import com.rs.ownvocabulary.database.WordPartial
import com.rs.ownvocabulary.viewmodels.AppViewModel
import com.rs.ownvocabulary.composeable.SearchBar
import com.rs.ownvocabulary.composeable.TopBar

@Composable
fun Vocabulary(navHostController: NavHostController, appViewModel: AppViewModel) {
    val openAddWordDialog by appViewModel.openAddWordDialog.collectAsStateWithLifecycle()
    val myWords by appViewModel.myWords.collectAsStateWithLifecycle()
    val totalWordsCount by appViewModel.totalWordsCountOwn.collectAsStateWithLifecycle()
    val isLoadingMore by appViewModel.isLoadingMoreOwn.collectAsStateWithLifecycle()
    val hasMoreData by appViewModel.hasMoreDataOwn.collectAsStateWithLifecycle()
    val searchQuery by appViewModel.searchQueryOwn.collectAsStateWithLifecycle()
    val sortBy by appViewModel.sortByOwn.collectAsStateWithLifecycle()
    val currentUser by appViewModel.currentUser.collectAsStateWithLifecycle()

    // Add this to check if user is logged in
    val isLoggedIn = currentUser?.userId != null

    var showSearchBar by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val context = LocalContext.current

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index != 0 &&
                    lastVisibleItem?.index == listState.layoutInfo.totalItemsCount - 1
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && hasMoreData && !isLoadingMore && isLoggedIn) {
            appViewModel.loadCommunityWords(loadMore = true)
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            appViewModel.pullWordFromServer()
            appViewModel.loadOwnWords()
        }
    }

    val fabScale by animateFloatAsState(
        targetValue = if (openAddWordDialog) 0.8f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    fun toggleLove(word: Word) {
        appViewModel.toggleFavorite(word.uid, word.isFavorite) {
            if (it != null) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                return@toggleFavorite
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                title = "My Words",
                subTitle = "My personal vocabulary",
                onBackClick = { navHostController.popBackStack() },
                disableBack = true
            ) {
                if (isLoggedIn) {
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search words",
                            tint = if (showSearchBar || searchQuery.isNotEmpty())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort words",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Newest First") },
                                onClick = {
                                    appViewModel.updateSortByOwn("newest")
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.NewReleases, null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Oldest First") },
                                onClick = {
                                    appViewModel.updateSortByOwn("oldest")
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.History, null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Alphabetical") },
                                onClick = {
                                    appViewModel.updateSortByOwn("alphabetical")
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.SortByAlpha, null)
                                }
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            appViewModel.resetPaginationOwn()
                            appViewModel.pullWordFromServer()
                            appViewModel.loadOwnWords()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh words",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (!isLoggedIn) {


                LoginRequired(navHostController, appViewModel)


            } else {
                // Logged in state - Show vocabulary
                if (isLoggedIn) {
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
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    item {
                        if (showSearchBar) {
                            Column(Modifier.padding(top = 0.dp, bottom = 20.dp)) {
                                SearchBar(
                                    query = searchQuery,
                                    onQueryChange = { appViewModel.updateSearchQueryOwn(it) },
                                    onSearchToggle = {
                                        appViewModel.clearSearchOwn()
                                        showSearchBar = false
                                    }
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$totalWordsCount ${if (totalWordsCount == 1) "word" else "words"} available",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = when (sortBy) {
                                        "newest" -> "Newest First"
                                        "oldest" -> "Oldest First"
                                        "alphabetical" -> "A-Z"
                                        else -> ""
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    val chunkedWords = myWords.chunked(10)

                    items(chunkedWords.size) { chunkIndex ->
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chunkedWords[chunkIndex].forEach { word ->
                                QuickWordCard(
                                    word = word,
                                    onToggleLove = { toggleLove(word) },
                                    onItemLongPress = { appViewModel.setLongPressItem(word) },
                                    onItemClick = { navHostController.navigate("word_detail/${word.uid}") }
                                )
                            }
                        }

                        if (chunkIndex < chunkedWords.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp
                                )
                            }
                        }
                    }

                    if (!hasMoreData && myWords.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "You've reached the end",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (myWords.isEmpty() && !isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (searchQuery.isNotEmpty())
                                            Icons.Default.SearchOff
                                        else
                                            Icons.Default.BookmarkBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = if (searchQuery.isNotEmpty()) "No words found" else "No words yet",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (searchQuery.isNotEmpty())
                                            "Try a different search term"
                                        else
                                            "Start building your vocabulary by adding words",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}