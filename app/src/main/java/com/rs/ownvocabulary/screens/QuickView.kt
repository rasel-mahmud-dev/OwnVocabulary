package com.rs.ownvocabulary.screens

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rs.ownvocabulary.ShareActivity
import com.rs.ownvocabulary.composeable.AddWordDialog
import com.rs.ownvocabulary.composeable.AddWordDialogShare
import com.rs.ownvocabulary.database.SyncStatus
import com.rs.ownvocabulary.database.Word
import com.rs.ownvocabulary.database.WordPartial
import com.rs.ownvocabulary.viewmodels.AppViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickView(navHostController: NavHostController, appViewModel: AppViewModel) {
    var isGridView by remember { mutableStateOf(true) }


    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val words by appViewModel.words.collectAsStateWithLifecycle()

    println("wordsListwordsListaa ${words}")

    AddWordDialogShare(
        incomingWord = "",
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onAddWord = { newWord ->
            appViewModel.addWord(newWord) {
                showDialog = false

            }
        }
    )

    fun toggleLove(word: Word){
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


    fun openShareModal() {
        val textToShare = "sdfsdfsdf"
        val intent = Intent(context, ShareActivity::class.java)
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, textToShare)
        context.startActivity(intent)
    }

    Box() {

        FloatingActionButton(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onClick = {
//                openShareModal()
                showDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(56.dp)
                .zIndex(1F)

        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Word",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Quick View",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold

                    )
                    Text(
                        text = "${words.size} words • Fast browse mode",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }



                Card(
                    modifier = Modifier
                        .clickable { isGridView = !isGridView },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isGridView)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.primaryContainer
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.GridView else Icons.AutoMirrored.Filled.ViewList,
                            contentDescription = "Toggle View",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isGridView) "Grid" else "List",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                item {
                    if (isGridView) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            words.forEach { word ->
                                QuickWordCard(word = word,
                                    onToggleLove={toggleLove(word)},
                                    onItemClick = {
                                    navHostController.navigate("word_detail/${word.uid}")
                                })
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            words.forEach { word ->
                                QuickWordListItem(word = word)
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun QuickWordCard(word: Word, onItemClick: () -> Unit, onToggleLove: ()-> Unit) {
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

@Composable
fun QuickWordListItem(word: Word) {
    var isFavorite by remember { mutableStateOf(word.isFavorite) }

    val cardColors = when (word.proficiencyLevel) {
        "Beginner" -> listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))
        "Intermediate" -> listOf(Color(0xFFFB8C00), Color(0xFFFFD54F))
        "Advanced" -> listOf(Color(0xFF667eea), Color(0xFF764ba2))
        else -> listOf(Color(0xFF6B73FF), Color(0xFF9B59B6))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { /* Navigate to word details */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Level indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(cardColors)
                        )
                )

                // Word
                Text(
                    text = word.word,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Favorite Icon
            IconButton(
                onClick = { isFavorite = !isFavorite },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFE91E63) else Color.Gray.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun getProficiencyColors(proficiencyLevel: String): List<Color>{
    val cardColors = when (proficiencyLevel) {
        "Beginner" -> listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))
        "Intermediate" -> listOf(Color(0xFFFB8C00), Color(0xFFFFD54F))
        "Advanced" -> listOf(Color(0xFF667eea), Color(0xFF764ba2))
        else -> listOf(Color(0xFF6B73FF), Color(0xFF9B59B6))
    }
    return cardColors
}