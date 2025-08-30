package com.rs.ownvocabulary

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rs.ownvocabulary.layouts.LayoutWithCreateWord
import com.rs.ownvocabulary.layouts.MainLayout
import com.rs.ownvocabulary.layouts.QuickWordViewLayout
import com.rs.ownvocabulary.screens.AnalyticsScreen
import com.rs.ownvocabulary.screens.ProfileScreen
import com.rs.ownvocabulary.screens.QuickView
import com.rs.ownvocabulary.screens.TestPage
import com.rs.ownvocabulary.screens.Vocabulary
import com.rs.ownvocabulary.screens.WordPractice
import com.rs.ownvocabulary.viewmodels.AppViewModel


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun AppNavigation(initialIntent: Intent, activity: Context, appViewModel: AppViewModel) {

    val navController = rememberNavController()

//    val bottomSheetState = rememberBottomSheetScaffoldState(
//        bottomSheetState = rememberStandardBottomSheetState(
//            initialValue = SheetValue.Hidden,
//            skipHiddenState = false
//        )
//    )
//
//    val shouldShowSheet by viewModel.isBottomSheetVisible.collectAsStateWithLifecycle()
//    val bottomSheetType by viewModel.bottomSheetType.collectAsStateWithLifecycle()
//
//    LaunchedEffect(shouldShowSheet) {
//        if (shouldShowSheet) {
//            bottomSheetState.bottomSheetState.expand()
//        } else {
//            bottomSheetState.bottomSheetState.hide()
//        }
//    }
//
//    LaunchedEffect(bottomSheetState.bottomSheetState.currentValue) {
//        if (bottomSheetState.bottomSheetState.currentValue != SheetValue.Expanded) {
//            viewModel.setBottomSheetVisible(false, "theme")
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        checkAndRequestPermissions(activity)
//    }

    val startDestination = remember(initialIntent) {
        when {
            initialIntent?.hasExtra("route") == true &&
                    initialIntent.getStringExtra("route") == "new-diary" -> "new-diary"

            else -> "home"
        }
    }

    var selectedItem by rememberSaveable { mutableStateOf("home") }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "home"
//            startDestination = "word_detail/fe291334-a8b8-4b9a-9aee-863f7df32002"
//                startDestination = startDestination // "home",
        ) {

            composable("home") {
                QuickWordViewLayout(appViewModel) {
                    LayoutWithCreateWord(appViewModel) {
                        MainLayout(
                            navController = navController,
                            selectedItem = selectedItem,
                            setSelectedItem = { selectedItem = it }
                        ) {
                            QuickView(navController, appViewModel)
                        }
                    }
                }
            }

            composable("analytics") {
                MainLayout(
                    navController = navController,
                    selectedItem = selectedItem,
                    setSelectedItem = { selectedItem = it }
                ) {
                    AnalyticsScreen(navController, appViewModel)
                }
            }
            composable("profile") {
                MainLayout(
                    navController = navController,
                    selectedItem = selectedItem,
                    setSelectedItem = { selectedItem = it }
                ) {
                    ProfileScreen(navController, appViewModel)
                }
            }

            composable("dictionary") {
                LayoutWithCreateWord(appViewModel) {
                    MainLayout(
                        navController = navController,
                        selectedItem = selectedItem,
                        setSelectedItem = { selectedItem = it }
                    ) {
                        Vocabulary(navController, appViewModel)
                    }
                }
            }

            composable("word_detail/{uid}") {
                val uid = it.arguments?.getString("uid") ?: ""
                WordPractice(navController, uid, appViewModel)
            }

        }

    }


    LaunchedEffect(initialIntent) {
        if (initialIntent.hasExtra("route")) {
            initialIntent.removeExtra("route")  // Clear the intent
        }
    }
}


