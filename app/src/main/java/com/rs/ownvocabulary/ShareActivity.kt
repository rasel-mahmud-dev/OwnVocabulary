package com.rs.ownvocabulary

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rs.ownvocabulary.viewmodels.AppViewModel
import com.rs.ownvocabulary.ui.theme.OwnVocabularyTheme

class ShareActivity : ComponentActivity() {

    private var receivedLink: String? = null
    private val context = this
    private lateinit var noteViewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make this activity appear as a floating dialog
        setupFloatingDialog()

        val application = applicationContext as Application
        noteViewModel = AppViewModel(application)

        handleIncomingIntent(intent)
        println("receivedLink: $receivedLink")

        setContent {
            var showDialog by remember { mutableStateOf(true) }

            OwnVocabularyTheme {
                if (showDialog) {
                    FloatingShareDialog(
                        sharedText = receivedLink ?: "",
                        onSave = {
                            handleSave(receivedLink)
                            showDialog = false
                            finish()
                        },
                        onDismiss = {
                            showDialog = false
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun setupFloatingDialog() {
        // Make the activity appear as a floating dialog
        setTheme(android.R.style.Theme_Translucent_NoTitleBar)

        // Set window flags to appear as overlay
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )

        window.setFlags(
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        )
    }

    private fun handleIncomingIntent(intent: Intent) {
        if (Intent.ACTION_SEND == intent.action && intent.type == "text/plain") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            text?.let {
                println("incoming text... $it")
                receivedLink = it
            }
        }
    }

    private fun handleSave(text: String?) {
        text?.let {
            // Save logic using your ViewModel
            // noteViewModel.saveSharedText(it)
            println("Saving: $it")
        }
    }

    override fun onResume() {
        super.onResume()
        intent?.let { handleIncomingIntent(it) }
    }
}

@Composable
fun FloatingShareDialog(
    sharedText: String,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Save to Vocabulary",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Shared content preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp, max = 120.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = sharedText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 16.sp
                        )
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Save",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}