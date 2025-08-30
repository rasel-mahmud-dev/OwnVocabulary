package com.rs.ownvocabulary

import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.rs.ownvocabulary.sync.SyncManager
import com.rs.ownvocabulary.ui.theme.OwnVocabularyTheme
import com.rs.ownvocabulary.viewmodels.AppViewModel

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SyncManager.initialize(application)

        enableEdgeToEdge()
        setContent {
            val application = applicationContext as Application
            val viewModel = remember {
                AppViewModel(application)
            }

            viewModel.startWordSync()
            viewModel.pullWordFromServer()

            OwnVocabularyTheme {
                Surface(modifier = Modifier) {
                    AppNavigation(
                        initialIntent = intent,
                        activity = this,
                        appViewModel = viewModel
                    )
                }
            }
        }
    }
}

