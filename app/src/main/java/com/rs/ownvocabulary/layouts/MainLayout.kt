package com.rs.ownvocabulary.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rs.ownvocabulary.composeable.BottomNavigationBar
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    navController: NavHostController,
    selectedItem: String,
    setSelectedItem: (r: String) -> Unit,
    content: @Composable () -> Unit,
) {

    val scope = rememberCoroutineScope()


    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                tonalElevation = 8.dp,
                selectedItem = selectedItem,
                setSelectedItem = {
                    setSelectedItem(it)
                    scope.launch {
                        navController.navigate(it)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8FAFF),
                            Color(0xFFEDF2FF)
                        )
                    )
                )
        ) {
            content.invoke()
        }
    }
}
