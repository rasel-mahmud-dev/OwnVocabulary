package com.rs.ownvocabulary.layouts

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rs.ownvocabulary.composeable.QuickWordView
import com.rs.ownvocabulary.viewmodels.AppViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickWordViewLayout(
    appViewModel: AppViewModel,
    content: @Composable () -> Unit,
) {

    val longPressItem by appViewModel.longPressItem.collectAsStateWithLifecycle()

    fun handleClose() {
        appViewModel.setLongPressItem(null)
    }

    Surface {
        QuickWordView(
            appViewModel = appViewModel,
            word = longPressItem,
            open = longPressItem != null,
            onClose = {handleClose()}
        )

        content.invoke()
    }
}
