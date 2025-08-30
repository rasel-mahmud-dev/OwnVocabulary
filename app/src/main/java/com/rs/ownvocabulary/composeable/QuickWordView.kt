package com.rs.ownvocabulary.composeable

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.rs.ownvocabulary.TTSManager
import com.rs.ownvocabulary.database.AIResponseItem
import com.rs.ownvocabulary.database.Word
import com.rs.ownvocabulary.viewmodels.AppViewModel
import kotlinx.coroutines.launch


@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickWordView(
    appViewModel: AppViewModel,
    word: Word?,
    open: Boolean,
    onClose: () -> Unit
) {

    if (!open) return

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val scope = rememberCoroutineScope()

    var allGeneratedSentences by remember { mutableStateOf<List<AIResponseItem>>(emptyList()) }

    fun loadAiGeneratedResponse(input: String) {
        scope.launch {
            appViewModel.loadAiGeneratedResponse(input) {
                allGeneratedSentences = it
            }
        }
    }

    LaunchedEffect(Unit) {
        if (word != null) {
            loadAiGeneratedResponse(word.word)
        }
    }

    AlertDialog(
        modifier = Modifier
            .heightIn(min = 200.dp, max = 400.dp)
            .width(screenWidth * 0.9f),
        onDismissRequest = onClose,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        ),
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    word?.word ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (word?.shortMeaning != null) {
                    Text(
                        word.shortMeaning,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

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
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        TTSManager.speakOnlyEnglish(sentence.output)
                                    }
                            )
                        }
                    }
                }


            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(0.dp)
            ) {


            }
        },

        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
//                Button(
//                    onClick = {
//                        onClose()
//                    },
//                    modifier = Modifier.weight(1f),
//                    shape = RoundedCornerShape(16.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.primary
//                    )
//                ) {
//                    Text("Submit", fontWeight = FontWeight.SemiBold)
//                }
            }
        }
    )
}
