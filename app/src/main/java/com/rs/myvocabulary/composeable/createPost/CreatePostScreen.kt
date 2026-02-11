package com.rs.myvocabulary.composeable.createPost

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rs.learnmedia.composeable.AudioRecordingDialog
import com.rs.learnmedia.composeable.createPost.DocumentPreview
import com.rs.learnmedia.composeable.createPost.GenericFilePreview
import com.rs.learnmedia.composeable.createPost.ImagePreview
import com.rs.learnmedia.composeable.createPost.MediaOptionButton
import com.rs.learnmedia.composeable.createPost.PostAttachment
import com.rs.learnmedia.composeable.createPost.VideoPreview
import com.rs.myvocabulary.database.Word
import com.rs.myvocabulary.viewmodels.AppViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
        onPostCreated: () -> Unit,
        onDismiss: () -> Unit = {},
        initialText: String? = null,
        initialUris: List<Uri>? = null,
        postToEdit: Word? = null,
        viewModel: AppViewModel = viewModel()
) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        val isUploading by viewModel.isUploading.collectAsState()

        LaunchedEffect(Unit) { viewModel.loadData(context) }

        // Data States
        val selectedCategories = remember {
                mutableStateListOf<Map<String, String>>().apply {
                        postToEdit?.categories
                                ?.map {
                                        mapOf("name" to it.name, "parentId" to (it.parentId ?: ""))
                                }
                                ?.let { addAll(it) }
                }
        }

        // Post Type State
        var selectedType by remember { mutableStateOf(postToEdit?.type ?: "word") }
        val types = listOf("word", "docs")
        var wordText by remember { mutableStateOf(postToEdit?.word ?: "") }
        var shortMeaning by remember { mutableStateOf(postToEdit?.shortMeaning ?: "") }

        // Character counter
        val maxChars = 50000
        val textContentState = remember { mutableStateOf(initialText ?: "") }
        var textContent by textContentState
        val charsRemaining = maxChars - textContent.length

        val attachments = remember {
                mutableStateListOf<PostAttachment>().apply {
                        postToEdit?.attachments?.forEach { attachment ->
                                add(
                                        PostAttachment(
                                                type = attachment.type,
                                                name = attachment.url.split("/").lastOrNull(),
                                                remoteUrl = attachment.url
                                        )
                                )
                        }
                }
        }
        var showMediaOptions by remember { mutableStateOf(false) }
        var showAudioDialog by remember { mutableStateOf(false) }
        var expanded by remember { mutableStateOf(false) }

        // Handle shared URIs
        LaunchedEffect(initialUris) {
                initialUris?.forEach { uri ->
                        val mimeType = context.contentResolver.getType(uri)
                        val type =
                                when {
                                        mimeType?.startsWith("image/") == true -> "image"
                                        mimeType?.startsWith("video/") == true -> "video"
                                        mimeType?.startsWith("audio/") == true -> "audio"
                                        mimeType?.contains("pdf") == true -> "pdf"
                                        else -> "file"
                                }
                        val name = uri.lastPathSegment
                        attachments.add(PostAttachment(uri, type, name))
                }
        }

        val permissionLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                        isGranted ->
                        if (isGranted) {
                                showAudioDialog = true
                        }
                }

        val imagePicker =
                rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickMultipleVisualMedia()
                ) { uris ->
                        uris.forEach { uri -> attachments.add(PostAttachment(uri, "image", null)) }
                        if (uris.isNotEmpty()) showMediaOptions = false
                }

        val videoPicker =
                rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickMultipleVisualMedia()
                ) { uris ->
                        uris.forEach { uri -> attachments.add(PostAttachment(uri, "video", null)) }
                        if (uris.isNotEmpty()) showMediaOptions = false
                }

        val documentPicker =
                rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetMultipleContents()
                ) { uris ->
                        uris.forEach { uri ->
                                val mime = context.contentResolver.getType(uri)
                                val type =
                                        when {
                                                mime?.contains("pdf") == true -> "pdf"
                                                mime?.contains("document") == true -> "document"
                                                mime?.contains("audio") == true -> "audio"
                                                else -> "file"
                                        }
                                val name = uri.lastPathSegment
                                attachments.add(PostAttachment(uri, type, name))
                        }
                        if (uris.isNotEmpty()) showMediaOptions = false
                }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = { Text("Create Post", fontWeight = FontWeight.Bold) },
                                navigationIcon = {
                                        IconButton(onClick = onDismiss) {
                                                Icon(
                                                        Icons.Filled.Close,
                                                        contentDescription = "Close"
                                                )
                                        }
                                },
                                actions = {
                                        Button(
                                                onClick = {
                                                        viewModel.createPost(
                                                                context = context,
                                                                textContent = textContent,
                                                                attachments = attachments,
                                                                selectedCategories =
                                                                        selectedCategories.toList(),
                                                                type = selectedType,
                                                                word = wordText,
                                                                shortMeaning = shortMeaning,
                                                                onSuccess = {
                                                                        Toast.makeText(
                                                                                        context,
                                                                                        "Post created locally!",
                                                                                        Toast.LENGTH_SHORT
                                                                                )
                                                                                .show()
                                                                        onPostCreated()
                                                                },
                                                                onError = { error ->
                                                                        Toast.makeText(
                                                                                        context,
                                                                                        "Error: $error",
                                                                                        Toast.LENGTH_SHORT
                                                                                )
                                                                                .show()
                                                                }
                                                        )
                                                },
                                                enabled =
                                                        !isUploading &&
                                                                (textContent.isNotBlank() ||
                                                                        attachments.isNotEmpty()),
                                                shape = RoundedCornerShape(20.dp),
                                                modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                                if (isUploading) {
                                                        CircularProgressIndicator(
                                                                modifier = Modifier.size(20.dp),
                                                                strokeWidth = 2.dp,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onPrimary
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("Posting...")
                                                } else {
                                                        Text("Post", fontWeight = FontWeight.Bold)
                                                }
                                        }
                                },
                                colors =
                                        TopAppBarDefaults.topAppBarColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                        )
                        )
                },
                bottomBar = {
                        Column(modifier = Modifier.fillMaxWidth().imePadding()) {
                                HorizontalDivider()
                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(
                                                                horizontal = 8.dp,
                                                                vertical = 12.dp
                                                        ),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                "Add to your post",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                MediaOptionButton(
                                                        icon = Icons.Outlined.Image,
                                                        tint = Color(0xFF4CAF50),
                                                        onClick = {
                                                                imagePicker.launch(
                                                                        PickVisualMediaRequest(
                                                                                ActivityResultContracts
                                                                                        .PickVisualMedia
                                                                                        .ImageOnly
                                                                        )
                                                                )
                                                        }
                                                )

                                                MediaOptionButton(
                                                        icon = Icons.Outlined.Videocam,
                                                        tint = Color(0xFFE91E63),
                                                        onClick = {
                                                                videoPicker.launch(
                                                                        PickVisualMediaRequest(
                                                                                ActivityResultContracts
                                                                                        .PickVisualMedia
                                                                                        .VideoOnly
                                                                        )
                                                                )
                                                        }
                                                )

                                                MediaOptionButton(
                                                        icon = Icons.Outlined.AttachFile,
                                                        tint = Color(0xFF2196F3),
                                                        onClick = { documentPicker.launch("*/*") }
                                                )

                                                MediaOptionButton(
                                                        icon = Icons.Outlined.Mic,
                                                        tint = Color(0xFFFF9800),
                                                        onClick = {
                                                                permissionLauncher.launch(
                                                                        Manifest.permission
                                                                                .RECORD_AUDIO
                                                                )
                                                        }
                                                )

                                                MediaOptionButton(
                                                        icon = Icons.Outlined.MoreHoriz,
                                                        tint =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                        onClick = {
                                                                showMediaOptions = !showMediaOptions
                                                        }
                                                )
                                        }
                                }
                        }
                }
        ) { padding ->
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {

                        // Type Selection Dropdown
                        ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                                OutlinedTextField(
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        readOnly = true,
                                        value =
                                                selectedType.replaceFirstChar {
                                                        if (it.isLowerCase())
                                                                it.titlecase(
                                                                        java.util.Locale
                                                                                .getDefault()
                                                                )
                                                        else it.toString()
                                                },
                                        onValueChange = {},
                                        label = { Text("Post Type") },
                                        trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(
                                                        expanded = expanded
                                                )
                                        },
                                        colors =
                                                ExposedDropdownMenuDefaults
                                                        .outlinedTextFieldColors()
                                )
                                ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                ) {
                                        types.forEach { type ->
                                                DropdownMenuItem(
                                                        text = {
                                                                Text(
                                                                        type.replaceFirstChar {
                                                                                if (it.isLowerCase()
                                                                                )
                                                                                        it.titlecase(
                                                                                                java.util
                                                                                                        .Locale
                                                                                                        .getDefault()
                                                                                        )
                                                                                else it.toString()
                                                                        }
                                                                )
                                                        },
                                                        onClick = {
                                                                selectedType = type
                                                                expanded = false
                                                        },
                                                        contentPadding =
                                                                ExposedDropdownMenuDefaults
                                                                        .ItemContentPadding
                                                )
                                        }
                                }
                        }

                        // Short Meaning Input (for Word)
                        if (selectedType == "word") {
                                OutlinedTextField(
                                        value = wordText,
                                        onValueChange = { wordText = it },
                                        label = { Text("Word") },
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(
                                                                horizontal = 16.dp,
                                                                vertical = 8.dp
                                                        ),
                                        colors =
                                                TextFieldDefaults.colors(
                                                        focusedContainerColor = Color.Transparent,
                                                        unfocusedContainerColor = Color.Transparent,
                                                        disabledContainerColor = Color.Transparent,
                                                )
                                )
                                OutlinedTextField(
                                        value = shortMeaning,
                                        onValueChange = { shortMeaning = it },
                                        label = { Text("Short Meaning") },
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(
                                                                horizontal = 16.dp,
                                                                vertical = 8.dp
                                                        ),
                                        colors =
                                                TextFieldDefaults.colors(
                                                        focusedContainerColor = Color.Transparent,
                                                        unfocusedContainerColor = Color.Transparent,
                                                        disabledContainerColor = Color.Transparent,
                                                )
                                )
                        }

                        // Text Input Area
                        TextField(
                                value = textContent,
                                onValueChange = { if (it.length <= maxChars) textContent = it },
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .weight(1f)
                                                .padding(horizontal = 4.dp),
                                placeholder = {
                                        Text(
                                                "What's on your mind?",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color =
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                                .copy(alpha = 0.6f)
                                        )
                                },
                                colors =
                                        TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                disabledContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                        ),
                                textStyle = MaterialTheme.typography.bodyLarge
                        )

                        // Media Preview Section
                        AnimatedVisibility(
                                visible = attachments.isNotEmpty(),
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                        ) {
                                LazyRow(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                        items(attachments) { attachment ->
                                                Box(
                                                        modifier = Modifier.width(300.dp)
                                                ) { // Fixed width for carousel items
                                                        when (attachment.type) {
                                                                "image" ->
                                                                        ImagePreview(
                                                                                attachment.uri
                                                                                        ?: attachment
                                                                                                .remoteUrl
                                                                        ) {
                                                                                attachments.remove(
                                                                                        attachment
                                                                                )
                                                                        }
                                                                "video" ->
                                                                        VideoPreview(
                                                                                attachment.name
                                                                        ) {
                                                                                attachments.remove(
                                                                                        attachment
                                                                                )
                                                                        }
                                                                "pdf" ->
                                                                        DocumentPreview(
                                                                                attachment.name,
                                                                                "PDF",
                                                                                Color(0xFFE53935)
                                                                        ) {
                                                                                attachments.remove(
                                                                                        attachment
                                                                                )
                                                                        }
                                                                "document" ->
                                                                        DocumentPreview(
                                                                                attachment.name,
                                                                                "DOC",
                                                                                Color(0xFF2196F3)
                                                                        ) {
                                                                                attachments.remove(
                                                                                        attachment
                                                                                )
                                                                        }
                                                                "audio" ->
                                                                        DocumentPreview(
                                                                                attachment.name,
                                                                                "Audio",
                                                                                Color(0xFFE91E63)
                                                                        ) {
                                                                                attachments.remove(
                                                                                        attachment
                                                                                )
                                                                        }
                                                                else ->
                                                                        GenericFilePreview(
                                                                                attachment.name
                                                                        ) {
                                                                                attachments.remove(
                                                                                        attachment
                                                                                )
                                                                        }
                                                        }
                                                }
                                        }
                                }
                        }

                        // Character Counter
                        if (textContent.isNotEmpty()) {
                                Text(
                                        text = "$charsRemaining characters remaining",
                                        style = MaterialTheme.typography.labelSmall,
                                        color =
                                                if (charsRemaining < 50) Color(0xFFE53935)
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier =
                                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                        }

                        HorizontalDivider()
                }

                if (showAudioDialog) {
                        AudioRecordingDialog(
                                onDismiss = { showAudioDialog = false },
                                onSendAudio = { audioFile: File ->
                                        attachments.add(
                                                PostAttachment(
                                                        uri = Uri.fromFile(audioFile),
                                                        type = "audio",
                                                        name = audioFile.name,
                                                        file = audioFile
                                                )
                                        )
                                        showAudioDialog = false
                                }
                        )
                }
        }
}
