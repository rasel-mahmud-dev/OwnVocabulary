package com.rs.myvocabulary.composeable.vocabulary

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rs.myvocabulary.composeable.common.WordItemCard
import com.rs.myvocabulary.viewmodels.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ClauseVocabulary(appViewModel: AppViewModel, onItemClick: (String) -> Unit) {
    val clouseWordList by appViewModel.clouseWordList.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Filter logic
    val filteredWords =
            remember(clouseWordList, searchQuery) {
                if (searchQuery.isBlank()) {
                    clouseWordList
                } else {
                    clouseWordList.filter { word ->
                        word.word.contains(searchQuery, ignoreCase = true) ||
                                word.shortMeaning.contains(searchQuery, ignoreCase = true) ||
                                word.details.contains(searchQuery, ignoreCase = true)
                    }
                }
            }

    LaunchedEffect(Unit) {
        if (clouseWordList.isEmpty()) {
            appViewModel.loadClouseWordList()
        }
    }

    fun handleRefresh() {
        isRefreshing = true
        coroutineScope.launch {
            appViewModel.loadClouseWordList()
            delay(500)
            isRefreshing = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { handleRefresh() },
                modifier = Modifier.fillMaxSize()
        ) {
            if (filteredWords.isEmpty() && searchQuery.isEmpty() && clouseWordList.isEmpty()) {
                EmptyState(showSearch = false)
            } else if (filteredWords.isEmpty() && searchQuery.isNotEmpty()) {
                EmptyState(showSearch = true)
            } else {
                LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredWords, key = { it.uid }) { word ->
                        WordItemCard(
                                word = word,
                                onClick = { onItemClick(word.uid) },
                                onPinClick = { appViewModel.toggleFavorite(word.uid) },
                                modifier = Modifier.animateItem()
                        )
                    }

                    item {
                        EndOfListIndicator(
                                itemCount = filteredWords.size,
                                searchQuery = searchQuery
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(showSearch: Boolean) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = CircleShape,
                    modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                            imageVector =
                                    if (showSearch) Icons.Default.SearchOff
                                    else Icons.Default.AutoStories,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                    text = if (showSearch) "No matches found" else "No clauses yet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
            )

            Text(
                    text =
                            if (showSearch) {
                                "Try different search terms"
                            } else {
                                "Add your first clause to get started"
                            },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EndOfListIndicator(itemCount: Int, searchQuery: String) {
    Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider(
                modifier = Modifier.fillMaxWidth(0.2f),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 1.dp
        )

        Text(
                text =
                        when {
                            searchQuery.isNotEmpty() -> "$itemCount results"
                            itemCount == 0 -> "No clauses"
                            else -> "$itemCount clauses"
                        },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
        )
    }
}
