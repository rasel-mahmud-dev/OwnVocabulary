package com.rs.myvocabulary.composeable

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.rs.myvocabulary.database.Word
import com.rs.myvocabulary.viewmodels.AppViewModel

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickWordView(appViewModel: AppViewModel, word: Word?, open: Boolean, onClose: () -> Unit) {

        if (!open) return

        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp

        val scope = rememberCoroutineScope()

        var isLoading by remember { mutableStateOf(false) }

        AlertDialog(
                modifier = Modifier.heightIn(min = 300.dp, max = 550.dp).width(screenWidth * 0.9f),
                onDismissRequest = onClose,
                properties =
                        DialogProperties(
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true,
                                usePlatformDefaultWidth = false
                        ),
                title = {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                text = word?.word ?: "",
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                        )

                                        if (word?.shortMeaning != null &&
                                                        word.shortMeaning.isNotBlank()
                                        ) {
                                                Text(
                                                        text = word.shortMeaning,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                        modifier = Modifier.padding(top = 4.dp)
                                                )
                                        }
                                }

                                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                                        Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                }
                        }
                },
                text = {
                        Column(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .verticalScroll(rememberScrollState())
                        ) {
                                // Example Sentences Section
                                Text(
                                        text = "Example Sentences",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                )

                                if (word?.details?.isNotBlank() == true) {
                                        Text(
                                                text = word.details,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 20.sp
                                        )
                                } else if (isLoading) {
                                        Box(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(vertical = 32.dp),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                Column(
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally,
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(12.dp)
                                                ) {
                                                        CircularProgressIndicator(
                                                                modifier = Modifier.size(36.dp),
                                                                strokeWidth = 3.dp
                                                        )
                                                        Text(
                                                                text = "Generating examples...",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyMedium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant
                                                        )
                                                }
                                        }
                                } else {
                                        Text(
                                                text = "No examples available.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontStyle =
                                                        androidx.compose.ui.text.font.FontStyle
                                                                .Italic
                                        )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                        }
                },
                confirmButton = {},
                dismissButton = {}
        )
}

@Composable
private fun SentenceCard(sentence: String, index: Int, onSpeak: () -> Unit) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                        CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                ) {
                        Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                                // Index Badge
                                Box(
                                        modifier =
                                                Modifier.size(24.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(
                                                                MaterialTheme.colorScheme.primary
                                                                        .copy(alpha = 0.2f)
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Text(
                                                text = "$index",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                        )
                                }

                                // Sentence Text
                                Text(
                                        text = sentence,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                )
                        }

                        // Speaker Icon
                        IconButton(onClick = onSpeak, modifier = Modifier.size(32.dp)) {
                                Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Speak",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                )
                        }
                }
        }
}
