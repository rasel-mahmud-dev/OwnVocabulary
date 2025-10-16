package com.rs.ownvocabulary.screens.test


import android.widget.Space
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.rs.ownvocabulary.TTSManager
import com.rs.ownvocabulary.ai.AIIndex
import com.rs.ownvocabulary.ai.MeaningResult
import com.rs.ownvocabulary.composeable.AddWordDialogShare
import com.rs.ownvocabulary.composeable.TopBar
import com.rs.ownvocabulary.database.SyncStatus
import com.rs.ownvocabulary.database.Word
import com.rs.ownvocabulary.database.WordPartial
import com.rs.ownvocabulary.viewmodels.AppViewModel
import kotlinx.coroutines.launch
import com.rs.ownvocabulary.R
import com.rs.ownvocabulary.composeable.detail.WordHeaderCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordPractice2(navHostController: NavHostController, uid: String, appViewModel: AppViewModel) {
    var wordDetail by remember { mutableStateOf<Word?>(null) }
    var isFavorite by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val user by appViewModel.currentUser.collectAsStateWithLifecycle()
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
                WordDetailContent(
                    appViewModel = appViewModel,
                    word = word,
                    isFavorite = isFavorite,
                    onFavoriteToggle = {
                        isFavorite = !isFavorite
                        // appViewModel.updateWordFavorite(word.uid, isFavorite)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    refetchWordDetail = { loadDetail(uid) },
                    onSetEditItem = { showEditModal = true }
                )
            }
        }

        AddWordDialogShare(
            userId = user?.userId,
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
    onSetEditItem: () -> Unit,
    refetchWordDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var synonymsResult by remember { mutableStateOf<List<com.rs.ownvocabulary.ai.WordSuggestion>?>(null) }
    var antonymsResult by remember { mutableStateOf<List<com.rs.ownvocabulary.ai.WordSuggestion>?>(null) }
    var sentencesResult by remember { mutableStateOf<List<String>?>(null) }
    var translationResult by remember { mutableStateOf<String?>(null) }
    var shortMeaning by remember { mutableStateOf<String?>("sdl;kfsdflk;") }
    var detailFromAi by remember { mutableStateOf<String?>(null) }

    var isLoadingSynonyms by remember { mutableStateOf(false) }
    var isLoadingAntonyms by remember { mutableStateOf(false) }
    var isLoadingSentences by remember { mutableStateOf(false) }
    var isLoadingTranslation by remember { mutableStateOf(false) }
    var isLoadingDetail by remember { mutableStateOf(false) }
    var isLoadingShortMeaning by remember { mutableStateOf(false) }

    var showSynonyms by remember { mutableStateOf(false) }
    var showAntonyms by remember { mutableStateOf(false) }
    var showSentences by remember { mutableStateOf(false) }
    var showTranslation by remember { mutableStateOf(false) }

    fun handleGenerateDetail() {
        if (isLoadingDetail) return // Prevent multiple clicks

        scope.launch {
            isLoadingDetail = true
            AIIndex.getDetail(word.word).fold(
                onSuccess = { result ->
                    detailFromAi = result
                    println("detailFromAi $detailFromAi")
                    isLoadingDetail = false
                },
                onFailure = { error ->
                    println("Error: ${error.message}")
                    isLoadingDetail = false
                }
            )
        }
    }

    fun handleSaveAiGenerated() {
        detailFromAi?.let {
            appViewModel.updatePartial(
                WordPartial(
                    uid = word.uid,
                    details = it,
                    syncStatus = SyncStatus.PENDING,
                )
            ) {
                detailFromAi = null
                refetchWordDetail()
            }
        }
    }

    fun handleGenerateShortMeaning(){
        if(isLoadingShortMeaning) return

        scope.launch {
            isLoadingShortMeaning = true
            AIIndex.getTranslation(word.word).fold(
                onSuccess = { result ->
                    shortMeaning = result
                    println(result)
                    isLoadingShortMeaning = false
                },
                onFailure = {
                    isLoadingShortMeaning = false
                }
            )
        }
    }


    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {

        item {
            WordHeaderCard(
                viewModel = appViewModel,
                word = word,
                isFavorite = isFavorite,
                onFavoriteToggle = onFavoriteToggle,
                setEditItem = onSetEditItem
            )
        }

        // Details Section
        if (word.details.isNotEmpty()) {
            item {
                DefinitionCard(
                    title = "Details",
                    content = word.details,
                    icon = Icons.Outlined.Info,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    onGenerate = ::handleGenerateDetail,
                    aiResult = detailFromAi,
                    isLoading = isLoadingDetail,
                    onSave = ::handleSaveAiGenerated
                )
            }
        }

        // AI Features Section Header
        item {
            Text(
                text = "AI-Powered Learning",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        // Synonyms Feature
        item {
            AIFeatureCard(
                title = "Synonyms",
                description = "Similar words to expand your vocabulary",
                icon = Icons.Outlined.ContentCopy,
                isExpanded = showSynonyms,
                isLoading = isLoadingSynonyms,
                onToggle = {
                    showSynonyms = !showSynonyms
                    if (showSynonyms && synonymsResult == null) {
                        isLoadingSynonyms = true
                        scope.launch {
                            AIIndex.getSynonyms(word.word, word.proficiencyLevel.lowercase()).fold(
                                onSuccess = { result ->
                                    synonymsResult = result
                                    isLoadingSynonyms = false
                                },
                                onFailure = {
                                    isLoadingSynonyms = false
                                }
                            )
                        }
                    }
                }
            ) {
                synonymsResult?.let { words ->
                    WordSuggestionsList(words = words)
                }
            }
        }

        // Antonyms Feature
        item {
            AIFeatureCard(
                title = "Antonyms",
                description = "Opposite words to understand contrast",
                icon = Icons.Outlined.SwapHoriz,
                isExpanded = showAntonyms,
                isLoading = isLoadingAntonyms,
                onToggle = {
                    showAntonyms = !showAntonyms
                    if (showAntonyms && antonymsResult == null) {
                        isLoadingAntonyms = true
                        scope.launch {
                            AIIndex.getAntonyms(word.word).fold(
                                onSuccess = { result ->
                                    antonymsResult = result
                                    isLoadingAntonyms = false
                                },
                                onFailure = {
                                    isLoadingAntonyms = false
                                }
                            )
                        }
                    }
                }
            ) {
                antonymsResult?.let { words ->
                    WordSuggestionsList(words = words)
                }
            }
        }

        // Example Sentences Feature
        item {
            AIFeatureCard(
                title = "Example Sentences",
                description = "See how to use this word in difference context.",
                icon = Icons.Outlined.FormatQuote,
                isExpanded = showSentences,
                isLoading = isLoadingSentences,
                onToggle = {
                    showSentences = !showSentences
                    if (showSentences && sentencesResult == null) {
                        isLoadingSentences = true
                        scope.launch {
                            AIIndex.getSentences(word.word, 5).fold(
                                onSuccess = { result ->
                                    sentencesResult = result
                                    isLoadingSentences = false
                                },
                                onFailure = {
                                    isLoadingSentences = false
                                }
                            )
                        }
                    }
                }
            ) {
                sentencesResult?.let { sentences ->
                    SentenceExamplesList(sentences = sentences)
                }
            }
        }

        // Translation Feature
        item {
            AIFeatureCard(
                title = "Translation",
                description = "See this word in your native language",
                icon = Icons.Outlined.Translate,
                isExpanded = showTranslation,
                isLoading = isLoadingTranslation,
                onToggle = {
                    showTranslation = !showTranslation
                    if (showTranslation && translationResult == null) {
                        isLoadingTranslation = true
                        scope.launch {
                            AIIndex.getTranslation(word.word, "Bengali").fold(
                                onSuccess = { result ->
                                    translationResult = result
                                    isLoadingTranslation = false
                                },
                                onFailure = {
                                    isLoadingTranslation = false
                                }
                            )
                        }
                    }
                }
            ) {
                translationResult?.let { translation ->
                    TranslationDisplay(translation = translation)
                }
            }
        }

    }
}

@Composable
private fun AIFeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    isExpanded: Boolean,
    isLoading: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp
                                )
                                Text(
                                    text = "Generating...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
private fun WordSuggestionsList(words: List<com.rs.ownvocabulary.ai.WordSuggestion>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        words.forEach { suggestion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = suggestion.word,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        DifficultyChip(difficulty = suggestion.difficulty)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = suggestion.meaning,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { TTSManager.speak(suggestion.word) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayCircle,
                        contentDescription = "Pronounce",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SentenceExamplesList(sentences: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sentences.forEach {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {

                Text(
                    text = "\"${it}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TranslationDisplay(translation: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = translation ?: "",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { TTSManager.speak(translation?:"") },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayCircle,
                    contentDescription = "Pronounce",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DifficultyChip(difficulty: String) {
    val color = when (difficulty.lowercase()) {
        "easy" -> Color(0xFF4CAF50)
        "medium" -> Color(0xFFFF9800)
        "hard" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.secondary
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.2f),
        modifier = Modifier.height(24.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = difficulty.capitalize(),
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


// Composable
@Composable
private fun DefinitionCard(
    title: String,
    content: String,
    icon: ImageVector,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onGenerate: () -> Unit,
    onSave: () -> Unit,
    aiResult: String?,
    isLoading: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
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

                Button(
                    onClick = onGenerate,
                    enabled = !isLoading,
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        painter = painterResource( R.drawable.wand_stars),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gen AI", style = MaterialTheme.typography.labelSmall)
                }
            }

            if (!aiResult.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "AI Generated",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = aiResult,
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", style = MaterialTheme.typography.labelSmall)
                }

            }

            // Original Content Section
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }
    }
}