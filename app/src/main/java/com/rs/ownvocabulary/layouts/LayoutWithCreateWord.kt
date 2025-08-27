package com.rs.ownvocabulary.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.rs.ownvocabulary.composeable.AddWordDialogShare
import com.rs.ownvocabulary.composeable.BottomNavigationBar
import com.rs.ownvocabulary.viewmodels.AppViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutWithCreateWord(
    appViewModel: AppViewModel,
    content: @Composable () -> Unit,
) {

    val openAddWordDialog by appViewModel.openAddWordDialog.collectAsStateWithLifecycle()

    fun handleClose(){
        appViewModel.setAddWordDialog(false)
    }

    Surface {
        AddWordDialogShare(
            incomingWord = "",
            showDialog = openAddWordDialog,
            onDismiss = {  handleClose() },
            onAddWord = { newWord ->
                appViewModel.addWord(newWord) {
                    handleClose()
                }
            }
        )
        content.invoke()
    }
}
