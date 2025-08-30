package com.rs.ownvocabulary.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rs.ownvocabulary.composeable.AddAnExample
import com.rs.ownvocabulary.composeable.AddWordDialogShare
import com.rs.ownvocabulary.composeable.ExcersizeSentance
import com.rs.ownvocabulary.composeable.TopBar
import com.rs.ownvocabulary.database.SyncStatus
import com.rs.ownvocabulary.database.Word
import com.rs.ownvocabulary.database.WordPartial
import com.rs.ownvocabulary.utils.DateFormatter
import com.rs.ownvocabulary.viewmodels.AppViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordPractice(navHostController: NavHostController, uid: String, appViewModel: AppViewModel) {
    var wordDetail by remember { mutableStateOf<Word?>(null) }
    var isFavorite by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    var showExamples by remember { mutableStateOf(true) }
    var showEditModal by remember { mutableStateOf(false) }

    fun loadDetail(uid: String) {
        appViewModel.getItemByUid(uid) { word ->
            wordDetail = word
            isFavorite = word?.isFavorite ?: false
            isLoading = false
            word?.let {
                appViewModel.incrementViewCount(it.uid)
            }
        }
    }

    LaunchedEffect(uid) {
        println("uid $uid")
        if (uid.isNotEmpty()) {
            loadDetail(uid)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                title = wordDetail?.word ?: "",
                subTitle = "Practice vocabulary",
                onBackClick = { navHostController.popBackStack() }
            ) {}
        }
    ) { innerPadding ->

        if (isLoading) {
            LoadingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            wordDetail?.let { word ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {

                    WordDetailContent(
                        appViewModel = appViewModel,
                        word = word,
                        isFavorite = isFavorite,
                        onFavoriteToggle = {
                            isFavorite = !isFavorite
//                                appViewModel.updateWordFavorite(word.uid, isFavorite)
                        },
                        showExamples = showExamples,
                        onToggleExamples = { showExamples = !showExamples },
                        modifier = Modifier.weight(1f),
                        refetchWordDetail = { loadDetail(uid) },
                        onSetEditItem = { showEditModal = true }
                    )

                }
            }
        }


        AddWordDialogShare(
            editItem = wordDetail,
            incomingWord = wordDetail?.word ?: "",
            showDialog = showEditModal,
            onDismiss = { showEditModal = false },
            onAddWord = { newWord ->
                appViewModel.updatePartial(
                    WordPartial(
                        uid = wordDetail!!.uid,
                        word = newWord.word,
                        shortMeaning = newWord.shortMeaning,
                        details = newWord.details,
                        examples = newWord.examples,
                        isFavorite = newWord.isFavorite,
                        proficiencyLevel = newWord.proficiencyLevel,
                        syncStatus = SyncStatus.PENDING,
                    )
                ) {
                    loadDetail(uid)
                    showEditModal = false
                }
            },
        )

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
                text = "Loading word details...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WordDetailContent(
    appViewModel: AppViewModel,
    word: Word,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    showExamples: Boolean,
    onToggleExamples: () -> Unit,
    onSetEditItem: () -> Unit,
    refetchWordDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Word Header Card
        item {
            WordHeaderCard(
                word = word,
                isFavorite = isFavorite,
                onFavoriteToggle = onFavoriteToggle,
                setEditItem = onSetEditItem
            )
        }

        // Word Statistics
        item {
//            WordStatsCard(word = word)
        }

        // Short Meaning Section
//        if (word.shortMeaning.isNotEmpty()) {
//            item {
//                DefinitionCard(
//                    title = "Definition",
//                    content = word.shortMeaning,
//                    icon = Icons.Outlined.Description
//                )
//            }
//        }

        if (word.details.isNotEmpty()) {
            item {
                DefinitionCard(
                    title = "Details",
                    content = word.details,
                    icon = Icons.Outlined.Info,
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        item {
            ExcersizeSentance(
                appViewModel = appViewModel,
                title = "Generate from AI",
                word = word,
                icon = Icons.Outlined.Hub,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }

        item {
            UsageTipsCard(word = word)
        }
    }
}


@Composable
private fun WordHeaderCard(
    word: Word,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    setEditItem: () -> Unit
) {

    val context = LocalContext.current

    val tts = remember {
        TextToSpeech(context) {}
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun handlePlaySound(word: String) {
        tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    val cardColors = when (word.proficiencyLevel) {
        "Beginner" -> listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))
        "Intermediate" -> listOf(Color(0xFFFB8C00), Color(0xFFFFD54F))
        "Advanced" -> listOf(Color(0xFF667eea), Color(0xFF764ba2))
        else -> listOf(Color(0xFF6B73FF), Color(0xFF9B59B6))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                MaterialTheme.colorScheme.surfaceContainer
            )
            .drawBehind {
                drawLine(
                    color = cardColors.first(),
                    end = androidx.compose.ui.geometry.Offset(0f, 0f),
                    start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = 8.dp.toPx()
                )
            }

    ) {
        Column(
            modifier = Modifier.padding(24.dp)

        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {


                Column {
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (word.type.isNotEmpty()) {
                            Text(
                                text = "/${word.type}/",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(onClick = {
                            handlePlaySound(word.word)
                        }, modifier = Modifier.size(30.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.PlayCircle,
                                contentDescription = "infod",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (word.shortMeaning.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = word.shortMeaning,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(40.dp)
                ) {
                    val animatedScale by animateFloatAsState(
                        targetValue = if (isFavorite) 1.3f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "favorite_scale"
                    )

                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale)
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Word info chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = word.type.ifEmpty { "Word" }.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Label,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = word.proficiencyLevel,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.OfflinePin,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                AssistChip(
                    onClick = {
                        setEditItem()
                    },
                    label = {
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

            }
        }
    }
}

@Composable
private fun WordStatsCard(word: Word) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Outlined.Visibility,
                label = "Views",
                value = word.viewCount.toString()
            )

            VerticalDivider(
                modifier = Modifier.height(56.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            StatItem(
                icon = Icons.Outlined.Schedule,
                label = "Last viewed",
                value = if (word.lastVisited == 0L) "Today" else DateFormatter.formatToRelativeTime(
                    word.createdAt
                )
            )
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DefinitionCard(
    title: String,
    content: String,
    icon: ImageVector,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }

}

@Composable
private fun ExpandableSection(
    refetchWordDetail: () -> Unit,
    uid: String,
    title: String,
    examples: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
    appViewModel: AppViewModel
) {

    var isAddExampleMode by remember { mutableStateOf(false) }

    if (isAddExampleMode) {
        AddAnExample(
            exampleText = null,
            showDialog = true,
            onDismiss = {
                isAddExampleMode = false
            },
            onAction = {
                appViewModel.updatePartial(
                    WordPartial(
                        uid = uid,
                        syncStatus = SyncStatus.PENDING,
                        examples = examples + "\n" + it
                    )
                ) {
                    refetchWordDetail()
                    isAddExampleMode = false
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }


                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(onClick = { isAddExampleMode = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "add example",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    val rotationAngle by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        animationSpec = tween(300),
                        label = "rotation"
                    )

                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer(rotationZ = rotationAngle)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                    animationSpec = tween(
                        300
                    )
                ),
                exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(
                    animationSpec = tween(
                        200
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 20.dp
                    )
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    content()
                }
            }
        }
    }
}

@Composable
private fun ExamplesContent(examples: String) {
    val exampleList = examples.split("\n").filter { it.isNotBlank() }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        exampleList.forEachIndexed { index, example ->
            ExampleItem(
                example = example.trim(),
                index = index + 1
            )
        }
    }
}

@Composable
private fun ExampleItem(example: String, index: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = "\"$example\"",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )
    }
}

@Composable
private fun UsageTipsCard(word: Word) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Learning Tips",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TipItem("Try using this word in 3 different sentences today")
                TipItem("Practice pronunciation by reading it aloud")
                TipItem("Look for this word in articles or books")
                if (word.proficiencyLevel == "Beginner") {
                    TipItem("Focus on memorizing the basic meaning first")
                }
            }
        }
    }
}

@Composable
private fun TipItem(tip: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
            modifier = Modifier
                .size(6.dp)
                .padding(top = 8.dp)
        )
        Text(
            text = tip,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(1f)
        )
    }
}
