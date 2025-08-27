package com.rs.ownvocabulary

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.rs.ownvocabulary.composeable.AddWordDialogShare
import com.rs.ownvocabulary.viewmodels.AppViewModel
import com.rs.ownvocabulary.ui.theme.OwnVocabularyTheme
import kotlin.let


class ShareActivity : ComponentActivity() {

    private var receivedText: String? = null

    val context = this
    private lateinit var appViewModel: AppViewModel

    private fun handleClose() {
        (context as Activity).finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val application = applicationContext as Application
        appViewModel = AppViewModel(application)
        println(intent.type)
        println(intent.action)
        println(intent.data)
        val text = handleIncomingIntent(intent)
        println("receivedLink: $text")

        setContent {
            OwnVocabularyTheme() {
                AddWordDialogShare(
                    showDialog = true,
                    incomingWord = text,
                    onDismiss = { handleClose() },
                    onAddWord = {
                        appViewModel.addWord(it) {
                            if (it != null) {
                                Toast.makeText(applicationContext, it, Toast.LENGTH_SHORT).show()
                            } else {
                                handleClose()
                            }
                        }
                    }
                )
            }
        }
    }

    private fun handleIncomingIntent(intent: Intent): String? {
        if (Intent.ACTION_SEND == intent.action && intent.type == "text/plain") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            text?.let {
                println("incoming text... $it")
                receivedText = it
            }
        }
        return receivedText
    }

    override fun onResume() {
        super.onResume()
        intent?.let { handleIncomingIntent(it) }
    }
}
