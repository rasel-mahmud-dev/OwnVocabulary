package com.rs.myvocabulary

import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rs.myvocabulary.database.PreferencesManager
import com.rs.myvocabulary.database.SessionManager
import com.rs.myvocabulary.screens.BackupScreen
import com.rs.myvocabulary.screens.MainScreen
import com.rs.myvocabulary.sync.SyncManager
import com.rs.myvocabulary.ui.theme.OwnVocabularyTheme
import com.rs.myvocabulary.viewmodels.AppViewModel

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PreferencesManager.init(application)
        TTSManager.initialize(this)
        SessionManager.init(this)
        SyncManager.initialize(application)

        TTSManager.initialize(this)

        enableEdgeToEdge()
        setContent {
            val application = applicationContext as Application
            val viewModel = remember { AppViewModel(application) }
            val navController = rememberNavController()

            viewModel.startWordSync()
            viewModel.pullWordFromServer()

            OwnVocabularyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavHost(navController = navController, startDestination = "backup") {
                        composable("main") {
                            MainScreen(appViewModel = viewModel, navController = navController)
                        }
                        composable("backup") {
                            BackupScreen(appViewModel = viewModel, navController = navController)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TTSManager.shutdown()
    }
}
