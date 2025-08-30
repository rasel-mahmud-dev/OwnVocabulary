package com.rs.ownvocabulary.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rs.ownvocabulary.composeable.TopBar
import com.rs.ownvocabulary.database.Word
import com.rs.ownvocabulary.database.AIResponseItem
import com.rs.ownvocabulary.utils.DateFormatter
import com.rs.ownvocabulary.viewmodels.AppViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(navHostController: NavHostController, appViewModel: AppViewModel) {
    var analyticsData by remember { mutableStateOf(AnalyticsData()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }


    LaunchedEffect(Unit) {
        appViewModel.db.totalWordCount {
            analyticsData = analyticsData.copy(
                totalWords = it
            )
        }
        appViewModel.db.totalFavWordCount {
            analyticsData = analyticsData.copy(
                favoriteWordsCount = it
            )
        }
        appViewModel.db.getVisitCounts { todayCount, weekCount ->
            analyticsData = analyticsData.copy(
                todayPractice = todayCount,
                weeklyPractice = weekCount
            )
        }
    }



    fun loadAnalytics() {
        appViewModel.getAllWords { words ->
            appViewModel.getAllAIResponses { aiResponses ->
                val sortedWords = words.sortedByDescending { it.viewCount }.take(10)
                val recentAI = aiResponses.sortedByDescending { it.createdAt }.take(5)

                val proficiencyMap = words.groupBy { it.proficiencyLevel }
                    .mapValues { it.value.size }

                analyticsData = AnalyticsData(
                    totalWords = words.size,
                    totalPractice = aiResponses.size,
                    todayPractice = aiResponses.count {
                        System.currentTimeMillis() - it.createdAt < 24 * 60 * 60 * 1000
                    },
                    weeklyPractice = aiResponses.count {
                        System.currentTimeMillis() - it.createdAt < 7 * 24 * 60 * 60 * 1000
                    },
                    mostVisitedWords = sortedWords,
                    recentAIResponses = recentAI,
                    proficiencyBreakdown = proficiencyMap,
                    favoriteWordsCount = words.count { it.isFavorite }
                )
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAnalytics()
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Analytics",
                subTitle = "Overview",
                disableBack=true,
                onBackClick = { navHostController.popBackStack() },
                right = {}
            )
        }
    ) { innerPadding ->

        if (isLoading) {
            LoadingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()

                    .background(MaterialTheme.colorScheme.background)
            ) {

                OverviewContent(
                    analyticsData = analyticsData,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Loading analytics...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OverviewContent(
    analyticsData: AnalyticsData,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Summary Stats Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OverviewStatCard(
                    title = "Total Words",
                    value = analyticsData.totalWords.toString(),
                    icon = Icons.Outlined.Book,
                    color = Color(0xFF4FACFE),
                    modifier = Modifier.weight(1f)
                )
                OverviewStatCard(
                    title = "Favorites",
                    value = analyticsData.favoriteWordsCount.toString(),
                    icon = Icons.Filled.Favorite,
                    color = Color(0xFFFF6B6B),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OverviewStatCard(
                    title = "Today's Practice",
                    value = analyticsData.todayPractice.toString(),
                    icon = Icons.Outlined.Today,
                    color = Color(0xFF51CF66),
                    modifier = Modifier.weight(1f)
                )
                OverviewStatCard(
                    title = "This Week",
                    value = analyticsData.weeklyPractice.toString(),
                    icon = Icons.Outlined.CalendarMonth,
                    color = Color(0xFFFFD93D),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Proficiency Breakdown
        item {
            ProficiencyBreakdownCard(analyticsData.proficiencyBreakdown)
        }

        // Top 3 Most Visited Words Preview
        item {
            TopWordsPreviewCard(
                words = analyticsData.mostVisitedWords.take(3),
                onSeeAll = { /* Navigate to Words tab */ }
            )
        }
    }
}


@Composable
private fun OverviewStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProficiencyBreakdownCard(proficiencyBreakdown: Map<String, Int>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Proficiency Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                proficiencyBreakdown.forEach { (level, count) ->
                    ProficiencyItem(
                        level = level,
                        count = count,
                        total = proficiencyBreakdown.values.sum()
                    )
                }
            }
        }
    }
}

@Composable
private fun ProficiencyItem(level: String, count: Int, total: Int) {
    val percentage = if (total > 0) (count.toFloat() / total * 100).toInt() else 0
    val color = when (level) {
        "Beginner" -> Color(0xFF4FACFE)
        "Intermediate" -> Color(0xFFFB8C00)
        "Advanced" -> Color(0xFF667eea)
        else -> Color(0xFF6B73FF)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, RoundedCornerShape(6.dp))
            )
            Text(
                text = level,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$count words",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun TopWordsPreviewCard(
    words: List<Word>,
    onSeeAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Top Words",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(onClick = onSeeAll) {
                    Text("See All")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                words.forEachIndexed { index, word ->
                    TopWordPreviewItem(
                        word = word,
                        rank = index + 1
                    )
                }
            }
        }
    }
}

@Composable
private fun TopWordPreviewItem(word: Word, rank: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(24.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = rank.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = word.shortMeaning.take(30) + if (word.shortMeaning.length > 30) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "${word.viewCount} views",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}
fun AppViewModel.getAllWords(callback: (List<Word>) -> Unit) {
    // Implementation to get all words from database
    // Example:
    // viewModelScope.launch {
    //     val words = wordRepository.getAllWords()
    //     callback(words)
    // }
}

fun AppViewModel.getAllAIResponses(callback: (List<AIResponseItem>) -> Unit) {
    // Implementation to get all AI responses from database
    // Example:
    // viewModelScope.launch {
    //     val aiResponses = aiResponseRepository.getAllResponses()
    //     callback(aiResponses)
    // }
}
