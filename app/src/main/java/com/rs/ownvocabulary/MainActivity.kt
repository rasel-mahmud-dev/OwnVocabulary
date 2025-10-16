package com.rs.ownvocabulary

import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rs.ownvocabulary.api.GeminiApiHelper
import com.rs.ownvocabulary.screens.AITestScreen
import com.rs.ownvocabulary.sync.SyncManager
import com.rs.ownvocabulary.ui.theme.OwnVocabularyTheme
import com.rs.ownvocabulary.viewmodels.AppViewModel
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SyncManager.initialize(application)

        TTSManager.initialize(this)

        enableEdgeToEdge()
        setContent {
            val application = applicationContext as Application
            val viewModel = remember {
                AppViewModel(application)
            }
            val scope = rememberCoroutineScope()


            viewModel.startWordSync()
            viewModel.pullWordFromServer()

            OwnVocabularyTheme {
                Surface(modifier = Modifier) {
//
//                    Column(Modifier.padding(50.dp)) {
//                        AITestScreen()
//                    }

                    AppNavigation(
                        initialIntent = intent,
                        activity = this,
                        appViewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TTSManager.shutdown()
    }
}

