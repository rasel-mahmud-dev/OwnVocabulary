package com.rs.ownvocabulary.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.rs.ownvocabulary.database.Word


val words = listOf(
    Word(
         id = 0,
        uid = "3we",
        word = "Tesst",
        shortMeaning = "sdf",
        details = "sdf",
        examples = "sdf",)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(navHostController: NavHostController) {
    var searchQuery by remember { mutableStateOf("") }


    val frequentWords = words
        .filter { it.viewCount >= 15 || it.lastViewedDaysAgo <= 3 }
        .sortedByDescending { it.viewCount }
        .take(50) // Last 50 frequently viewed

    val totalFrequentViews = frequentWords.sumOf { it.viewCount }

        Column(
            modifier = Modifier

        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Text(
                text = "My Vocabulary",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search words...",
                        color = Color.Gray.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF6B73FF)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6B73FF),
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SectionHeader(
                        title = "Frequently Reviewed",
                        subtitle = "Last ${frequentWords.size} most viewed words • ${totalFrequentViews} total views"
                    )
                }

                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        frequentWords.forEach { word ->
                            ModernWordCard(word = word, showStats = true)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    SectionHeader(
                        title = "Discover New Words",
                        subtitle = "Expand your vocabulary"
                    )
                }

                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        words.forEach { word ->
                            ModernWordCard(word = word, showStats = false)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E)
        )
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ModernWordCard(word: Word, showStats: Boolean = false) {
    var isFavorite by remember { mutableStateOf(word.isFavorite) }

    val cardColors = when (word.proficiencyLevel) {
        "Beginner" -> listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))
        "Intermediate" -> listOf(Color(0xFFFB8C00), Color(0xFFFFD54F))
        "Advanced" -> listOf(Color(0xFF667eea), Color(0xFF764ba2))
        else -> listOf(Color(0xFF6B73FF), Color(0xFF9B59B6))
    }

    Card(
        modifier = Modifier
            .widthIn(min = 160.dp, max = 200.dp)
            .clickable { /* Navigate to word details */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Gradient accent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(cardColors)
                    )
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = word.word,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = word.shortMeaning,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = { isFavorite = !isFavorite },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color(0xFFE91E63) else Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats and Proficiency Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Proficiency Level Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(cardColors)

                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = word.proficiencyLevel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = cardColors.first()
                        )
                    }

                    // View Stats (if enabled)
                    if (showStats) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF2196F3).copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${word.viewCount}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2196F3)
                                )
                            }

                            if (word.lastViewedDaysAgo == 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF4CAF50).copy(alpha = 0.1f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Today",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            } else if (word.lastViewedDaysAgo <= 3) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFF9800).copy(alpha = 0.1f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${word.lastViewedDaysAgo}d",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFFF9800)
                                    )
                                }
                            }
                        }
                    }
                }

                // Example preview (if available)
                if (word.examples.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"${word.examples}\"",
                        fontSize = 11.sp,
                        color = Color.Gray.copy(alpha = 0.8f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}