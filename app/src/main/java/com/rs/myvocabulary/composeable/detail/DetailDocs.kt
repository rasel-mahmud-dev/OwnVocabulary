package com.rs.myvocabulary.composeable.detail

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.material3.AssistChip
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rs.myvocabulary.composeable.AttachmentPreview
import com.rs.myvocabulary.composeable.AudioAttachmentCard
import com.rs.myvocabulary.composeable.CommentInputSection
import com.rs.myvocabulary.composeable.PostDetailComments
import com.rs.myvocabulary.composeable.VideoPlayerSection
import com.rs.myvocabulary.composeable.common.GenericFileCard
import com.rs.myvocabulary.composeable.common.PDFAttachmentCard
import com.rs.myvocabulary.composeable.detail.components.ArticleContentCard
import com.rs.myvocabulary.composeable.detail.components.ArticleTitleCard
import com.rs.myvocabulary.composeable.detail.components.DetailDocsMenu
import com.rs.myvocabulary.composeable.dialogs.AiExampleDialog
import com.rs.myvocabulary.composeable.dialogs.AiPostEnhancementDialog
import com.rs.myvocabulary.composeable.dialogs.AiSuggestionsDialog
import com.rs.myvocabulary.database.Comment
import com.rs.myvocabulary.database.CommentAttachment
import com.rs.myvocabulary.database.Word
import com.rs.myvocabulary.utils.FileSharingHelper
import com.rs.myvocabulary.viewmodels.AppViewModel
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailDocs(uid: String, appViewModel: AppViewModel, onBack: () -> Unit) {
    var noteDetail by remember { mutableStateOf<Word?>(null) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isReadOnly by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var replyingToCommentId by remember { mutableStateOf<String?>(null) }
    var isSubmittingComment by remember { mutableStateOf(false) }

    val attachments = remember {
        androidx.compose.runtime.mutableStateListOf<
                com.rs.myvocabulary.composeable.AttachmentPreview>()
    }

    // Audio stuff
    var showAudioDialog by remember { mutableStateOf<Boolean>(false) }

    // AI suggest stuff
    var showAiSuggestionsDialog by remember { mutableStateOf(false) }
    val isAiSuggesting by appViewModel.isAiSuggesting.collectAsState()
    val suggestedTags by appViewModel.suggestedTags.collectAsState()
    val suggestedCategories by appViewModel.suggestedCategories.collectAsState()
    val isGeneratingExample by appViewModel.isGeneratingExample.collectAsState()
    var showExampleDialog by remember { mutableStateOf(false) }
    val generatedExampleSentences by appViewModel.generatedExampleSentences.collectAsState()

    // AI enhancement stuff
    var showAiEnhancementDialog by remember { mutableStateOf(false) }
    val enhancedWord by appViewModel.enhancedWord.collectAsState()
    val enhancedShortMeaning by appViewModel.enhancedShortMeaning.collectAsState()
    val enhancedDetails by appViewModel.enhancedDetails.collectAsState()

    // Permission launcher
    val permissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                    isGranted ->
                if (isGranted) {
                    showAudioDialog = true
                }
            }

    val fileLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris
                ->
                uris.forEach { uri ->
                    val mimeType = context.contentResolver.getType(uri)
                    val type =
                            when {
                                mimeType?.startsWith("image/") == true -> "image"
                                mimeType?.startsWith("video/") == true -> "video"
                                mimeType?.startsWith("audio/") == true -> "audio"
                                else -> "file"
                            }
                    attachments.add(
                            com.rs.myvocabulary.composeable.AttachmentPreview(
                                    uri = uri,
                                    type = type
                            )
                    )
                }
            }

    val coroutineScope = rememberCoroutineScope()
    val state = rememberPullToRefreshState()

    suspend fun loadNoteDetail(uid: String) {
        if (uid.isNotEmpty()) {
            val note = appViewModel.getWord(uid)
            noteDetail = note
            note?.let {
                title = it.word
                content = it.details
            }
        }
    }

    LaunchedEffect(uid) { loadNoteDetail(uid) }

    BackHandler {
        if (!isReadOnly) {
            isReadOnly = true
        } else {
            onBack()
        }
    }

    fun handleRefresh() {
        isRefreshing = true
        coroutineScope.launch {
            loadNoteDetail(uid)
            delay(100)
            isRefreshing = false
        }
    }

    LaunchedEffect(noteDetail) {
        noteDetail?.let { note ->
            attachments.clear()
            note.attachments?.forEach { attachment ->
                attachments.add(
                        AttachmentPreview(uri = Uri.parse(attachment.url), type = attachment.type)
                )
            }
        }
    }

    fun handleSave() {
        noteDetail?.let { currentNote ->
            isSaving = true

            // convert current attachments to CommentAttachment list
            val currentAttachments =
                    attachments.map { CommentAttachment(url = it.uri.toString(), type = it.type) }

            val updatedNote =
                    currentNote.copy(
                            word = title,
                            details = content,
                            updatedAt = System.currentTimeMillis(),
                            attachments = currentAttachments
                    )
            appViewModel.addWord(updatedNote) { error ->
                isSaving = false
                if (error == null) {
                    isReadOnly = true
                }
            }
        }
    }

    println("innerPadding ${noteDetail}")

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Note Details") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Back")
                            }
                        },
                        actions = {
                            if (isReadOnly) {
                                IconButton(onClick = { isReadOnly = false }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                            } else {
                                IconButton(onClick = { handleSave() }, enabled = !isSaving) {
                                    if (isSaving) {
                                        CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(Icons.Default.Save, contentDescription = "Save")
                                    }
                                }
                            }

                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More")
                            }
                            DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                            ) {
                                DetailDocsMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false },
                                        onSharePost = {
                                            noteDetail?.let { p ->
                                                val mediaUrls =
                                                        p.attachments
                                                                ?.map { it.url }
                                                                ?.toMutableList()
                                                                ?: mutableListOf()
                                                p.cover?.takeIf { it.isNotEmpty() }?.let {
                                                    mediaUrls.add(0, it)
                                                }
                                                val shareText =
                                                        "${p.details}\n\nMedia URLs:\n${
                                                    mediaUrls.joinToString(
                                                        "\n"
                                                    )
                                                }"
                                                val intent =
                                                        android.content.Intent(
                                                                        android.content.Intent
                                                                                .ACTION_SEND
                                                                )
                                                                .apply {
                                                                    type = "text/plain"
                                                                    putExtra(
                                                                            android.content.Intent
                                                                                    .EXTRA_TEXT,
                                                                            shareText
                                                                    )
                                                                }
                                                context.startActivity(
                                                        android.content.Intent.createChooser(
                                                                intent,
                                                                "Share Post"
                                                        )
                                                )
                                            }
                                        },
                                        onShareMedia = {
                                            noteDetail?.let { p ->
                                                val mediaUrls = mutableListOf<String>()
                                                p.cover?.takeIf { it.isNotEmpty() }?.let {
                                                    mediaUrls.add(it)
                                                }
                                                p.attachments?.forEach { attachment ->
                                                    attachment.url.takeIf { it.isNotEmpty() }?.let {
                                                        mediaUrls.add(it)
                                                    }
                                                }

                                                val mediaType =
                                                        p.attachments?.firstOrNull()?.type
                                                                ?: "media"

                                                if (mediaUrls.isNotEmpty()) {
                                                    scope.launch {
                                                        FileSharingHelper.shareMedia(
                                                                context,
                                                                mediaUrls,
                                                                mediaType
                                                        )
                                                    }
                                                } else {
                                                    android.widget.Toast.makeText(
                                                                    context,
                                                                    "No media to share",
                                                                    android.widget.Toast
                                                                            .LENGTH_SHORT
                                                            )
                                                            .show()
                                                }
                                            }
                                        },
                                        onAiPostEnhance = {
                                            noteDetail?.let { word ->
                                                appViewModel.generateAiPostEnhancement(word) {
                                                    showAiEnhancementDialog = true
                                                }
                                            }
                                        },
                                        onAiSuggestions = {
                                            appViewModel.generateAiSuggestions(
                                                    noteDetail?.word ?: ""
                                            ) { showAiSuggestionsDialog = true }
                                        },
                                        isAiSuggesting = isAiSuggesting,
                                        onAiExample = {
                                            appViewModel.generateAiExampleSentences(
                                                    noteDetail?.word ?: ""
                                            ) { showExampleDialog = true }
                                        },
                                        isGeneratingExample = isGeneratingExample,
                                        onDelete = {
                                            noteDetail?.uid?.let { uidToDelete ->
                                                appViewModel.deleteWord(uidToDelete) { onBack() }
                                            }
                                        }
                                )
                            }
                        }
                )
            }
    ) { innerPadding ->
        PullToRefreshBox(
                state = state,
                isRefreshing = isRefreshing,
                onRefresh = { handleRefresh() },
                modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            Column(
                    modifier =
                            Modifier.fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (noteDetail != null) {
                    ArticleTitleCard(
                            isReadOnly = isReadOnly,
                            title = title,
                            createdAt = noteDetail?.createdAt ?: 0,
                            onTitleChange = { title = it }
                    )

                    if(content.isNotEmpty()){
                        ArticleContentCard(
                            isReadOnly = isReadOnly,
                            content = content,
                            onContentChange = { content = it }
                        )
                    }

                    // Media Content
                    noteDetail?.attachments?.let { attachments ->
                        attachments.forEach { attachment ->
                            Spacer(modifier = Modifier.height(8.dp))
                            when (attachment.type) {
                                "image" -> {
                                    AsyncImage(
                                            model = attachment.url,
                                            contentDescription = "Post Image",
                                            modifier =
                                                    Modifier.fillMaxWidth()
                                                            .heightIn(min = 200.dp, max = 500.dp)
                                                            .clip(RoundedCornerShape(0.dp))
                                                            .combinedClickable(
                                                                    onClick = {},
                                                                    onLongClick = {
                                                                        // onMediaLongClick not
                                                                        // defined
                                                                    }
                                                            ),
                                            contentScale = ContentScale.Crop
                                    )
                                }
                                "video" -> {
                                    VideoPlayerSection(videoUrl = attachment.url)
                                }
                                "audio" -> {
                                    AudioAttachmentCard(
                                            audioUrl = attachment.url,
                                            fileName = attachment.url.split("/").lastOrNull()
                                                            ?: "Audio",
                                            onLongClick = {
                                                // onMediaLongClick not defined
                                            }
                                    )
                                }
                                "pdf", "document" -> {
                                    PDFAttachmentCard(
                                            fileUrl = attachment.url,
                                            fileName = attachment.url.split("/").lastOrNull()
                                                            ?: "Document",
                                            onLongClick = {
                                                // onMediaLongClick not defined
                                            }
                                    )
                                }
                                else -> {
                                    // Audio or generic file
                                    GenericFileCard(
                                            fileUrl = attachment.url,
                                            fileName = attachment.url.split("/").lastOrNull()
                                                            ?: "File",
                                            fileType = attachment.type,
                                            onLongClick = {
                                                // onMediaLongClick not defined
                                            }
                                    )
                                }
                            }
                        }
                    }

                    // Legacy file URL support (if cover is used as image)
                    noteDetail?.cover?.takeIf { it.isNotEmpty() }?.let { coverUrl ->
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(
                                model = coverUrl,
                                contentDescription = "Post Image",
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .heightIn(min = 200.dp, max = 500.dp)
                                                .clip(RoundedCornerShape(0.dp))
                                                .combinedClickable(
                                                        onClick = {},
                                                        onLongClick = {
                                                            // onMediaLongClick not defined
                                                        }
                                                ),
                                contentScale = ContentScale.Crop
                        )
                    }

                    // Labels and Tags
                    if (noteDetail != null &&
                                    (!noteDetail!!.categories.isNullOrEmpty() ||
                                            !noteDetail!!.tags.isNullOrEmpty())
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (!noteDetail!!.categories.isNullOrEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                            text = "Categories",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        noteDetail!!.categories?.forEach { category ->
                                            val chipColor = MaterialTheme.colorScheme.primary

                                            AssistChip(
                                                    onClick = {},
                                                    label = { Text(category.name) },
                                                    leadingIcon = {
                                                        Icon(
                                                                Icons.Default.Label,
                                                                contentDescription = null,
                                                                tint = chipColor
                                                        )
                                                    }
                                            )
                                        }
                                    }
                                }
                            }

                            if (!noteDetail!!.tags.isNullOrEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                            text = "Tags",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        noteDetail!!.tags?.forEach { tag ->
                                            AssistChip(
                                                    onClick = {},
                                                    label = { Text("#${tag.name}") }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val rootComments =
                            remember(noteDetail?.comments) {
                                noteDetail?.comments?.filter { it.parentId == null } ?: emptyList()
                            }

                    // comments section here.
                    rootComments.forEach { comment ->
                        PostDetailComments(
                                comment = comment,
                                post = noteDetail,
                                setReplyingToCommentId = { replyingToCommentId = it }
                        )
                    }

                    // Add comment section at bottom
                    CommentInputSection(
                            commentText = commentText,
                            onCommentTextChange = { commentText = it },
                            replyingToUsername =
                                    if (replyingToCommentId != null)
                                            noteDetail?.comments
                                                    ?.find { it._id == replyingToCommentId }
                                                    ?.username
                                    else null,
                            onCancelReply = { replyingToCommentId = null },
                            onRecordAudio = {
                                showAudioDialog = true
                                permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            },
                            onAttachment = { fileLauncher.launch("*/*") },
                            attachments = attachments,
                            onRemoveAttachment = { attachments.remove(it) },
                            onSendComment = {
                                if (commentText.isNotBlank() || attachments.isNotEmpty()) {
                                    scope.launch {
                                        isSubmittingComment = true
                                        try {
                                            val commentAttachments =
                                                    attachments.map {
                                                        CommentAttachment(
                                                                url = it.uri.toString(),
                                                                type = it.type
                                                        )
                                                    }

                                            val newComment =
                                                    Comment(
                                                            _id = UUID.randomUUID().toString(),
                                                            username =
                                                                    appViewModel
                                                                            .currentUser
                                                                            .value
                                                                            ?.username
                                                                            ?: "User",
                                                            parentId = replyingToCommentId,
                                                            text = commentText,
                                                            audioUrl = null,
                                                            mediaUrl = null,
                                                            mediaType = null,
                                                            attachments = commentAttachments,
                                                            createdAt = System.currentTimeMillis()
                                                    )

                                            // Save to local database via AppViewModel
                                            withContext(Dispatchers.IO) {
                                                appViewModel.insertWordComment(uid, newComment)
                                                // Reload details to show new comment
                                                loadNoteDetail(uid)
                                            }

                                            commentText = ""
                                            attachments.clear()
                                            replyingToCommentId = null
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        } finally {
                                            isSubmittingComment = false
                                        }
                                    }
                                }
                            },
                            isSubmitting = isSubmittingComment
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    if (showAiSuggestionsDialog) {
        AiSuggestionsDialog(
                suggestedTags = suggestedTags,
                suggestedCategories = suggestedCategories,
                onDismiss = { showAiSuggestionsDialog = false },
                onProceed = { tags: List<String>, categories: List<String> ->
                    appViewModel.applyAiSuggestions(context, uid, tags, categories)
                    showAiSuggestionsDialog = false
                }
        )
    }

    if (showExampleDialog) {
        AiExampleDialog(
                sentences = generatedExampleSentences,
                onDismiss = { showExampleDialog = false },
                onProceed = {
                    appViewModel.insertMultipleComments(uid, generatedExampleSentences)
                    showExampleDialog = false
                }
        )
    }

    if (showAiEnhancementDialog) {
        AiPostEnhancementDialog(
                enhancedWord = enhancedWord,
                enhancedShortMeaning = enhancedShortMeaning,
                enhancedDetails = enhancedDetails,
                onDismiss = { showAiEnhancementDialog = false },
                onProceed = { word, meaning, details ->
                    appViewModel.applyAiPostEnhancement(uid, word, meaning, details)
                    showAiEnhancementDialog = false
                }
        )
    }
}
