package com.rs.myvocabulary.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.rs.myvocabulary.composeable.QuickWordView
import com.rs.myvocabulary.composeable.detail.DetailDocs
import com.rs.myvocabulary.composeable.dialogs.AddCategoryDialog
import com.rs.myvocabulary.composeable.docs.CreateDiary
import com.rs.myvocabulary.composeable.docs.DocsScreen
import com.rs.myvocabulary.composeable.drawer.AppDrawer
import com.rs.myvocabulary.composeable.vocabulary.Vocabulary
import com.rs.myvocabulary.viewmodels.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(appViewModel: AppViewModel, navController: NavHostController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Words", "Docs")
    var showAddDialog by remember { mutableStateOf(false) }

    // Overlay states
    var editingWordUid by remember { mutableStateOf<String?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var editingDocUid by remember { mutableStateOf<String?>(null) }
    var isCreatingDoc by remember { mutableStateOf(false) }

    // Top bar states
    var showSearchBar by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val filterState by appViewModel.filterState.collectAsState()
    val viewMode by appViewModel.viewMode.collectAsState()
    val docsList by appViewModel.docsList.collectAsState()
    val allCategories by appViewModel.allCategories.collectAsState()
    val readingLists by appViewModel.readingLists.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Tab titles
    val tabTitle =
            when (selectedTab) {
                0 -> "Words"
                1 -> "Docs"
                else -> ""
            }

    val tabSubtitle =
            when (selectedTab) {
                1 -> "${docsList.size} docs collected"
                else -> null
            }

    // Back handling for overlays
    BackHandler(
            enabled =
                    editingWordUid != null ||
                            editingDocUid != null ||
                            isCreatingDoc ||
                            showAddDialog ||
                            showSearchBar ||
                            showSettingsSheet ||
                            drawerState.isOpen
    ) {
        when {
            drawerState.isOpen -> scope.launch { drawerState.close() }
            showSettingsSheet -> showSettingsSheet = false
            editingWordUid != null -> editingWordUid = null
            editingDocUid != null -> editingDocUid = null
            isCreatingDoc -> isCreatingDoc = false
            showAddDialog -> showAddDialog = false
            showSearchBar -> {
                showSearchBar = false
                appViewModel.clearSearchOwn()
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
                onDismiss = { showAddCategoryDialog = false },
                onConfirm = { name ->
                    appViewModel.addCategory(name)
                    showAddCategoryDialog = false
                }
        )
    }

    AppDrawer(
            drawerState = drawerState,
            categories = allCategories,
            readingLists = readingLists,
            onCategoryClick = { category ->
                scope.launch { drawerState.close() }
                // Handle category click (filter?) - for now just close
            },
            onReadingListClick = { listName ->
                scope.launch { drawerState.close() }
                appViewModel.loadReadingListWords(listName)
            },
            onAddCategoryClick = {
                scope.launch { drawerState.close() }
                showAddCategoryDialog = true
            },
        onWordClick = {
            scope.launch { drawerState.close() }
            navController.navigate("main")
        }
    ) {
        Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    if (editingWordUid == null && editingDocUid == null && !isCreatingDoc) {
                        TopAppBar(
                                title = {
                                    Column {
                                        Text(
                                                text = tabTitle,
                                                style = MaterialTheme.typography.headlineSmall
                                        )
                                        tabSubtitle?.let {
                                            Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color =
                                                            MaterialTheme.colorScheme
                                                                    .onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                navigationIcon = {
                                    IconButton(
                                            onClick = {
                                                scope.launch {
                                                    if (drawerState.isClosed) drawerState.open()
                                                    else drawerState.close()
                                                }
                                            }
                                    ) { Icon(Icons.Default.Menu, contentDescription = "Menu") }
                                },
                                actions = {
                                    // Search icon (all tabs)
                                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                                        Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Search",
                                                tint =
                                                        if (showSearchBar ||
                                                                        filterState.searchQuery
                                                                                .isNotEmpty()
                                                        )
                                                                MaterialTheme.colorScheme.primary
                                                        else
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                        )
                                    }

                                    // Sort menu (Words tab only)
                                    if (selectedTab == 0) {
                                        Box {
                                            IconButton(onClick = { showSortMenu = true }) {
                                                Icon(
                                                        imageVector =
                                                                Icons.AutoMirrored.Filled.Sort,
                                                        contentDescription = "Sort",
                                                        tint =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                )
                                            }

                                            DropdownMenu(
                                                    expanded = showSortMenu,
                                                    onDismissRequest = { showSortMenu = false }
                                            ) {
                                                DropdownMenuItem(
                                                        text = { Text("Newest First") },
                                                        onClick = {
                                                            appViewModel.setFilter(sortOrder = 2)
                                                            showSortMenu = false
                                                        },
                                                        leadingIcon = {
                                                            Icon(Icons.Default.NewReleases, null)
                                                        }
                                                )
                                                DropdownMenuItem(
                                                        text = { Text("Oldest First") },
                                                        onClick = {
                                                            appViewModel.setFilter(sortOrder = 1)
                                                            showSortMenu = false
                                                        },
                                                        leadingIcon = {
                                                            Icon(Icons.Default.History, null)
                                                        }
                                                )
                                                DropdownMenuItem(
                                                        text = { Text("Alphabetical") },
                                                        onClick = {
                                                            appViewModel.setFilter(sortOrder = 3)
                                                            showSortMenu = false
                                                        },
                                                        leadingIcon = {
                                                            Icon(Icons.Default.SortByAlpha, null)
                                                        }
                                                )
                                            }
                                        }
                                    }

                                    // Settings icon
                                    IconButton(onClick = { showSettingsSheet = true }) {
                                        Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = "Settings",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                colors =
                                        TopAppBarDefaults.topAppBarColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme.background,
                                                titleContentColor =
                                                        MaterialTheme.colorScheme.onBackground
                                        )
                        )
                    }
                },
                floatingActionButton = {
                    if (editingWordUid == null && editingDocUid == null && !isCreatingDoc) {
                        FloatingActionButton(
                                onClick = { navController.navigate("create_post") },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                        ) { Icon(Icons.Default.Add, contentDescription = "Add") }
                    }
                },
                bottomBar = {
                    if (editingWordUid == null && editingDocUid == null && !isCreatingDoc) {
                        NavigationBar {
                            tabs.forEachIndexed { index, title ->
                                NavigationBarItem(
                                        selected = selectedTab == index,
                                        onClick = { selectedTab = index },
                                        label = { Text(title) },
                                        icon = {
                                            // You can add icons here if needed
                                        }
                                )
                            }
                        }
                    }
                }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    // Main Content
                    if (editingWordUid == null && editingDocUid == null && !isCreatingDoc) {
                        when (selectedTab) {
                            0 ->
                                    Vocabulary(
                                            appViewModel = appViewModel,
                                            onItemClick = { uid -> editingWordUid = uid }
                                    )
                            1 ->
                                    DocsScreen(
                                            appViewModel = appViewModel,
                                            onNavigateToDetail = { uid -> editingDocUid = uid },
                                            onNavigateToCreate = { isCreatingDoc = true }
                                    )
                        }
                    }
                }

                // Overlays
                AnimatedVisibility(
                        visible = editingWordUid != null,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                ) {
                    editingWordUid?.let { uid ->
                        DetailDocs(
                                uid = uid,
                                appViewModel = appViewModel,
                                onBack = { editingWordUid = null },
                                onOpenDrawer = {
                                    scope.launch { if (drawerState.isClosed) drawerState.open() }
                                }
                        )
                    }
                }

                AnimatedVisibility(
                        visible = editingDocUid != null || isCreatingDoc,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                ) {
                    if (editingDocUid != null) {
                        DetailDocs(
                                uid = editingDocUid!!,
                                appViewModel = appViewModel,
                                onBack = { editingDocUid = null },
                                onOpenDrawer = {
                                    scope.launch { if (drawerState.isClosed) drawerState.open() }
                                }
                        )
                    } else if (isCreatingDoc) {
                        CreateDiary(appViewModel = appViewModel, onBack = { isCreatingDoc = false })
                    }
                }

                // QuickWordView overlay for long press
                val longPressItem by appViewModel.longPressItem.collectAsStateWithLifecycle()
                QuickWordView(
                        appViewModel = appViewModel,
                        word = longPressItem,
                        open = longPressItem != null,
                        onClose = { appViewModel.setLongPressItem(null) }
                )

                // Settings Sheet
                if (showSettingsSheet) {
                    ModalBottomSheet(
                            onDismissRequest = { showSettingsSheet = false },
                            sheetState = rememberModalBottomSheetState(),
                    ) {
                        Column(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(horizontal = 16.dp)
                                                .padding(bottom = 32.dp)
                        ) {
                            Text(
                                    "Settings",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 16.dp)
                            )

                            ListItem(
                                    headlineContent = { Text("Backup & Restore") },
                                    leadingContent = {
                                        Icon(Icons.Default.Backup, contentDescription = null)
                                    },
                                    modifier =
                                            Modifier.clip(RoundedCornerShape(12.dp)).clickable {
                                                showSettingsSheet = false
                                                navController.navigate("backup")
                                            }
                            )
                        }
                    }
                }
            }
        }
    }
}
