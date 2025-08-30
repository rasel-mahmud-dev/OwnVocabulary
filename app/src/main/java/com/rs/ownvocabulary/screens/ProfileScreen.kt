package com.rs.ownvocabulary.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.rs.ownvocabulary.composeable.TopBar
import com.rs.ownvocabulary.database.Word
import com.rs.ownvocabulary.database.AIResponseItem
import com.rs.ownvocabulary.utils.DateFormatter
import com.rs.ownvocabulary.viewmodels.AppViewModel

data class AnalyticsData(
    var totalWords: Int = 0,
    val totalPractice: Int = 0,
    val todayPractice: Int = 0,
    val weeklyPractice: Int = 0,
    val mostVisitedWords: List<Word> = emptyList(),
    val recentAIResponses: List<AIResponseItem> = emptyList(),
    val proficiencyBreakdown: Map<String, Int> = emptyMap(),
    val favoriteWordsCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navHostController: NavHostController, appViewModel: AppViewModel) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "your information.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background)
        ) {

           Row(
               modifier = Modifier
                   .fillMaxWidth()
                   .fillMaxHeight(1f)
                   .padding(16.dp),
               horizontalArrangement = Arrangement.Center,
               verticalAlignment = Alignment.CenterVertically
           ) {
               Text("Profile Screen..")
           }
        }
    }
}
