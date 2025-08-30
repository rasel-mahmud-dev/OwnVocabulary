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

                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Overview") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Words") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Practice") }
                    )
                }

                when (selectedTab) {
                    0 -> OverviewContent(
                        analyticsData = analyticsData,
                        modifier = Modifier.weight(1f)
                    )

                    1 -> WordsAnalyticsContent(
                        analyticsData = analyticsData,
                        navHostController = navHostController,
                        modifier = Modifier.weight(1f)
                    )

                    2 -> PracticeAnalyticsContent(
                        analyticsData = analyticsData,
                        modifier = Modifier.weight(1f)
                    )
                }
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
private fun WordsAnalyticsContent(
    analyticsData: AnalyticsData,
    navHostController: NavHostController,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Most Visited Words Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Most Visited Words",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "${analyticsData.mostVisitedWords.size} words",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }

        // Most Visited Words List
        items(analyticsData.mostVisitedWords) { word ->
            MostVisitedWordCard(
                word = word,
                rank = analyticsData.mostVisitedWords.indexOf(word) + 1,
                onClick = {
                    navHostController.navigate("word_practice/${word.uid}")
                }
            )
        }
    }
}

@Composable
private fun PracticeAnalyticsContent(
    analyticsData: AnalyticsData,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Practice Stats
        item {
            PracticeStatsCard(analyticsData)
        }

        // Recent AI Responses Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent AI Practice",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "${analyticsData.recentAIResponses.size} sessions",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }

        // Recent AI Responses List
        items(analyticsData.recentAIResponses) { aiResponse ->
            AIResponseCard(aiResponse = aiResponse)
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

@Composable
private fun MostVisitedWordCard(
    word: Word,
    rank: Int,
    onClick: () -> Unit
) {
    val cardColors = when (word.proficiencyLevel) {
        "Beginner" -> listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))
        "Intermediate" -> listOf(Color(0xFFFB8C00), Color(0xFFFFD54F))
        "Advanced" -> listOf(Color(0xFF667eea), Color(0xFF764ba2))
        else -> listOf(Color(0xFF6B73FF), Color(0xFF9B59B6))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(
                        color = cardColors.first(),
                        end = androidx.compose.ui.geometry.Offset(0f, 0f),
                        start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                        strokeWidth = 6.dp.toPx()
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rank Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = cardColors.first().copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "#$rank",
                                style = MaterialTheme.typography.labelLarge,
                                color = cardColors.first(),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column {
                        Text(
                            text = word.word,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = word.shortMeaning.take(50) + if (word.shortMeaning.length > 50) "..." else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = word.proficiencyLevel,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                            if (word.isFavorite) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Favorite",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${word.viewCount}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "views",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = DateFormatter.formatToRelativeTime(word.lastVisited),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PracticeStatsCard(analyticsData: AnalyticsData) {
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
                    imageVector = Icons.Outlined.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Practice Statistics",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Outlined.Psychology,
                    label = "Total Sessions",
                    value = analyticsData.totalPractice.toString()
                )

                VerticalDivider(
                    modifier = Modifier.height(56.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                StatItem(
                    icon = Icons.Outlined.Today,
                    label = "Today",
                    value = analyticsData.todayPractice.toString()
                )

                VerticalDivider(
                    modifier = Modifier.height(56.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                StatItem(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "This Week",
                    value = analyticsData.weeklyPractice.toString()
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AIResponseCard(aiResponse: AIResponseItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Hub,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "AI Practice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                Text(
                    text = DateFormatter.formatToRelativeTime(aiResponse.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Input: ${aiResponse.input.take(100)}${if (aiResponse.input.length > 100) "..." else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Output: ${aiResponse.output.take(100)}${if (aiResponse.output.length > 100) "..." else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Extension functions for AppViewModel (add these to your AppViewModel class)

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
