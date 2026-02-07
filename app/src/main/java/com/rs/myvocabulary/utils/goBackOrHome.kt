package com.rs.myvocabulary.utils

import androidx.navigation.NavHostController

fun goBackOrHome(navController: NavHostController, target: String = "home") {
    val success = navController.popBackStack()
    if (!success) {
        navController.navigate(target) {
            popUpTo(target) { inclusive = true }
            launchSingleTop = true
        }
    }
}
