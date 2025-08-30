package com.rs.ownvocabulary.composeable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rs.ownvocabulary.GeminiApiClient
import com.rs.ownvocabulary.TTSManager
import com.rs.ownvocabulary.database.AIResponseItem
import com.rs.ownvocabulary.database.Word
import com.rs.ownvocabulary.viewmodels.AppViewModel
import kotlinx.coroutines.launch


@Composable
fun ExcersizeSentance(
    appViewModel: AppViewModel,
    title: String,
    word: Word,
    icon: ImageVector,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {

    val geminiApi = remember { GeminiApiClient() }
    val scope = rememberCoroutineScope()

    var generationCount by remember { mutableIntStateOf(0) }
    var allGeneratedSentences by remember { mutableStateOf<List<AIResponseItem>>(emptyList()) }

    fun loadAiGeneratedResponse(input: String) {
        scope.launch {
            appViewModel.loadAiGeneratedResponse(input) {
                allGeneratedSentences = it
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAiGeneratedResponse(word.word)
    }

    fun handleDelete(uid: String) {
        scope.launch {
            appViewModel.aiResponseDb.delete(uid) {
                allGeneratedSentences = allGeneratedSentences.filter { it.uid != uid }
            }
        }
    }

    fun handleGenerate() {
        scope.launch {

            if (appViewModel.currentUser.value == null) {
                return@launch
            }

            val newSentences = geminiApi.handleGenerate(
                word.word,
                allGeneratedSentences.map { it.output }.toString(),
                generationCount
            )

            newSentences.forEachIndexed { index, it ->
                appViewModel.addAiResponse(
                    AIResponseItem(
                        input = word.word,
                        output = it,
                        userId = appViewModel.currentUser.value!!.userId,
                        type = "word",
                    )
                ) {
                    if (index == newSentences.size - 1) {
                        loadAiGeneratedResponse(word.word)
                    }
                }
            }
        }
    }

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

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            allGeneratedSentences.forEach { sentence ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(

                        text = "• ${sentence.output}",
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                            .clickable {
                                TTSManager.speakOnlyEnglish(sentence.output)
                            }
                    )

                    IconButton(
                        modifier = Modifier.size(30.dp),
                        onClick = { handleDelete(sentence.uid) }
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = {
            handleGenerate()
        }) {
            Text("Generate")
        }
    }

}


