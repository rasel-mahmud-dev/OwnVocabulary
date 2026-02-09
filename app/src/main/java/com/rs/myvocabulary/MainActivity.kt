package com.rs.myvocabulary

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rs.myvocabulary.composeable.createPost.CreatePostScreen
import com.rs.myvocabulary.database.PreferencesManager
import com.rs.myvocabulary.database.SessionManager
import com.rs.myvocabulary.database.WordDatabase
import com.rs.myvocabulary.screens.BackupScreen
import com.rs.myvocabulary.screens.MainScreen
import com.rs.myvocabulary.sync.SyncManager
import com.rs.myvocabulary.ui.theme.OwnVocabularyTheme
import com.rs.myvocabulary.viewmodels.AppViewModel

class MainActivity : ComponentActivity() {

    companion object {
        var sharedText: String? = null
        var sharedUris: List<Uri>? = null

        fun clearSharedData() {
            sharedText = null
            sharedUris = null
        }
    }


    private fun handleShareIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("text/") == true) {
                    sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                } else {
                    intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { uri ->
                        sharedUris = listOf(uri)
                    }
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let { uris ->
                    sharedUris = uris
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PreferencesManager.init(application)
        TTSManager.initialize(this)
        SessionManager.init(this)
        SyncManager.initialize(application)


        handleShareIntent(intent)

        TTSManager.initialize(this)

        enableEdgeToEdge()
        setContent {
            val application = applicationContext as Application
            val viewModel = remember { AppViewModel(application) }
            val navController = rememberNavController()

            viewModel.startWordSync()
            viewModel.pullDataFromServer()

            // Navigate to create post if share intent was received
            val hasSharedContent = sharedText != null || sharedUris != null
            LaunchedEffect(hasSharedContent) {
                if (hasSharedContent) {
                    navController.navigate("create_post")
                }
            }

            OwnVocabularyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") {
                            MainScreen(appViewModel = viewModel, navController = navController)
                        }
                        composable("backup") {
                            BackupScreen(appViewModel = viewModel, navController = navController)
                        }
                        composable(
                            route = "create_post?postId={postId}",
                            arguments =
                                listOf(
                                    navArgument("postId") {
                                        type = NavType.StringType
                                        nullable = true
                                        defaultValue = null
                                    }
                                )
                        ) { backStackEntry ->
                            val postId = backStackEntry.arguments?.getString("postId")
                            val context = LocalContext.current
                            val db = remember {
                                WordDatabase.getInstance(context)
                            }
//                            val postToEdit =
//                                remember(postId) { postId?.let { db.getPostById(it) } }

                            CreatePostScreen(
                                onPostCreated = {
                                    clearSharedData()
                                    navController.popBackStack()
                                },
                                onDismiss = {
                                    clearSharedData()
                                    navController.popBackStack()
                                },
                                initialText = sharedText,
                                initialUris = sharedUris,
                                postToEdit = null
                            )
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
