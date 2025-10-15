package com.rs.ownvocabulary.screens

import android.widget.Toast
import androidx.compose.animation.animateBounds
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rs.ownvocabulary.composeable.QuickWordCard
import com.rs.ownvocabulary.composeable.SearchBar
import com.rs.ownvocabulary.composeable.TopBar
import com.rs.ownvocabulary.viewmodels.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Discover(navHostController: NavHostController, appViewModel: AppViewModel) {

    val words by appViewModel.discoverWords.collectAsStateWithLifecycle()
    val totalWordsCount by appViewModel.totalWordsCount.collectAsStateWithLifecycle()
    val isLoadingMore by appViewModel.isLoadingMore.collectAsStateWithLifecycle()
    val hasMoreData by appViewModel.hasMoreData.collectAsStateWithLifecycle()
    val searchQuery by appViewModel.searchQuery.collectAsStateWithLifecycle()
    val sortBy by appViewModel.sortBy.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var showSearchBar by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index != 0 &&
                    lastVisibleItem?.index == listState.layoutInfo.totalItemsCount - 1
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && hasMoreData && !isLoadingMore) {
            appViewModel.loadCommunityWords(loadMore = true)
        }
    }

    LaunchedEffect(Unit) {
        appViewModel.pullWordFromServer()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        topBar = {
            TopBar(
                title = "Discover",
                subTitle = "Explore community vocabulary",
                onBackClick = { navHostController.popBackStack() },
                disableBack = true
            ) {

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
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Newest First")

                                }
                            },
                            onClick = {
                                appViewModel.updateSortBy("newest")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Oldest First")

                                }
                            },
                            onClick = {
                                appViewModel.updateSortBy("oldest")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Alphabetical")
                                }
                            },
                            onClick = {
                                appViewModel.updateSortBy("alphabetical")
                                showSortMenu = false
                            }
                        )
                    }
                }

                IconButton(
                    onClick = {
                        appViewModel.resetPagination()
                        appViewModel.pullWordFromServer()
                        appViewModel.loadCommunityWords()
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
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {


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
                                   onQueryChange = { appViewModel.updateSearchQuery(it) },
                                   onSearchToggle = { showSearchBar = false }
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
                            text = "$totalWordsCount words available",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = when (sortBy) {
                                "newest" -> "Newest First"
                                "oldest" -> "Oldest First"
                                "alphabetical" -> "A-Z"
                                else -> ""
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                val chunkedWords = words.chunked(10)

                items(chunkedWords.size) { chunkIndex ->
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chunkedWords[chunkIndex].forEach { word ->
                            QuickWordCard(
                                word = word,
                                onToggleLove = {
                                    appViewModel.toggleFavorite(word.uid, word.isFavorite) {
                                        if (it != null) {
                                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                            return@toggleFavorite
                                        }
                                    }
                                },
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

                if (!hasMoreData && words.isNotEmpty()) {
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

                // Empty state
                if (words.isEmpty() && !isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "No words found" else "No words available",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (searchQuery.isNotEmpty()) {
                                    Text(
                                        text = "Try a different search term",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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