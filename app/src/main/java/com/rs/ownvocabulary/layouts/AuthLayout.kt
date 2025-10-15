package com.rs.ownvocabulary.layouts

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.rs.ownvocabulary.composeable.LoginRequired
import com.rs.ownvocabulary.viewmodels.AppViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthLayout(
    navController: NavHostController,
    appViewModel: AppViewModel,
    content: @Composable () -> Unit,
) {
    val currentUser by appViewModel.currentUser.collectAsStateWithLifecycle()
    val isLoggedIn = currentUser?.userId != null

    if(isLoggedIn) {
        content.invoke()
    } else {
        LoginRequired(
            navHostController = navController,
            appViewModel = appViewModel
        )
    }
}