package com.rs.myvocabulary.composeable.labels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rs.myvocabulary.viewmodels.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelsManagementScreen(appViewModel: AppViewModel, onBack: () -> Unit) {
    val allCategories by appViewModel.allCategories.collectAsState()
    var newCategory by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { appViewModel.loadCategories() }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Manage Labels") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                )
                            }
                        }
                )
            }
    ) { innerPadding ->
        Column(
                modifier =
                        Modifier.padding(innerPadding)
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                    Text(
                            "Categories",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                                value = newCategory,
                                onValueChange = { newCategory = it },
                                label = { Text("New Category (comma separated for multiple)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                        )
                        IconButton(
                                onClick = {
                                    if (newCategory.isNotBlank()) {
                                        appViewModel.addCategory(newCategory)
                                        newCategory = ""
                                    }
                                }
                        ) { Icon(Icons.Default.Add, contentDescription = "Add Category") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(allCategories) { category ->
                            ListItem(
                                    headlineContent = { Text(category.name) },
                                    trailingContent = {
                                        IconButton(
                                                onClick = { appViewModel.removeCategory(category) }
                                        ) {
                                            Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete"
                                            )
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}
