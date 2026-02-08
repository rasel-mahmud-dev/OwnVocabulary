package com.rs.myvocabulary.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.rs.myvocabulary.utils.BackupUtils
import com.rs.myvocabulary.viewmodels.AppViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.apply
import kotlin.collections.isNotEmpty
import kotlin.let
import kotlin.text.format
import kotlin.text.startsWith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(appViewModel: AppViewModel, navController: NavHostController) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val backupFiles by appViewModel.backupFiles.collectAsState()
        var isBackingUpDb by remember { mutableStateOf(false) }
        var isBackingUpAssets by remember { mutableStateOf(false) }
        var isRestoring by remember { mutableStateOf(false) }
        var expandedBackups by remember { mutableStateOf(true) }
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) { appViewModel.fetchBackupFiles() }

        val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                        uri?.let {
                                scope.launch {
                                        isRestoring = true
                                        appViewModel.restoreData(context, it) { success ->
                                                isRestoring = false
                                                scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                                if (success)
                                                                        "✓ Restore completed successfully"
                                                                else
                                                                        "✗ Restore failed. Please try again",
                                                                duration = SnackbarDuration.Short
                                                        )
                                                }
                                        }
                                }
                        }
                }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = {
                                        Text("Backup & Restore", fontWeight = FontWeight.SemiBold)
                                },
                                navigationIcon = {
                                        IconButton(onClick = { navController.popBackStack() }) {
                                                Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back"
                                                )
                                        }
                                },
                                colors =
                                        TopAppBarDefaults.topAppBarColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                        )
                        )
                },
                snackbarHost = {
                        SnackbarHost(snackbarHostState) { data ->
                                Snackbar(
                                        snackbarData = data,
                                        containerColor =
                                                if (data.visuals.message.startsWith("✓"))
                                                        MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.errorContainer,
                                        contentColor =
                                                if (data.visuals.message.startsWith("✓"))
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onErrorContainer,
                                        shape = RoundedCornerShape(12.dp)
                                )
                        }
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ) { padding ->
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                        HorizontalDivider(thickness = 1.dp)

                        // Full screen scrollable list for backups
                        LazyColumn(
                                modifier = Modifier.fillMaxSize().weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                                // Full screen scrollable list for backups

                                item {
                                        Column(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                                Text(
                                                        "Data Management",
                                                        style =
                                                                MaterialTheme.typography
                                                                        .headlineSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                        "Export, backup, and restore your application data",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                )
                                        }
                                }

                                // Header Section
                                item {
                                        Row(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Row(
                                                        verticalAlignment =
                                                                Alignment.CenterVertically,
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(8.dp)
                                                ) {
                                                        Text(
                                                                "Saved Backups",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .titleMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurface
                                                        )

                                                        if (backupFiles.isNotEmpty()) {
                                                                Surface(
                                                                        shape = CircleShape,
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primaryContainer
                                                                ) {
                                                                        Text(
                                                                                backupFiles.size
                                                                                        .toString(),
                                                                                modifier =
                                                                                        Modifier.padding(
                                                                                                horizontal =
                                                                                                        10.dp,
                                                                                                vertical =
                                                                                                        4.dp
                                                                                        ),
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .labelMedium,
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold,
                                                                                color =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .onPrimaryContainer
                                                                        )
                                                                }
                                                        }
                                                }

                                                Row(
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(8.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        // Quick backup button
                                                        FilledTonalButton(
                                                                onClick = {
                                                                        if (!isBackingUpDb) {
                                                                                scope.launch {
                                                                                        isBackingUpDb =
                                                                                                true
                                                                                        val zipFile =
                                                                                                withContext(
                                                                                                        Dispatchers
                                                                                                                .IO
                                                                                                ) {
                                                                                                        BackupUtils
                                                                                                                .createDatabaseBackup(
                                                                                                                        context
                                                                                                                )
                                                                                                }
                                                                                        isBackingUpDb =
                                                                                                false
                                                                                        if (zipFile !=
                                                                                                        null
                                                                                        ) {
                                                                                                appViewModel
                                                                                                        .fetchBackupFiles()
                                                                                                snackbarHostState
                                                                                                        .showSnackbar(
                                                                                                                "✓ Backup created successfully"
                                                                                                         )
                                                                                                 // Also save in phone storage (Public Downloads)
                                                                                                 withContext(Dispatchers.IO) {
                                                                                                     BackupUtils.copyFileToPublicDownloads(
                                                                                                         context,
                                                                                                         zipFile,
                                                                                                         zipFile.name
                                                                                                     )
                                                                                                 }
                                                                                                shareFile(
                                                                                                        context,
                                                                                                        zipFile,
                                                                                                        "Full Backup"
                                                                                                )
                                                                                        } else {
                                                                                                snackbarHostState
                                                                                                        .showSnackbar(
                                                                                                                "✗ Failed to create backup"
                                                                                                        )
                                                                                        }
                                                                                }
                                                                        }
                                                                },
                                                                enabled = !isBackingUpDb,
                                                                shape = RoundedCornerShape(10.dp),
                                                                contentPadding =
                                                                        PaddingValues(
                                                                                horizontal = 14.dp,
                                                                                vertical = 8.dp
                                                                        ),
                                                                modifier = Modifier.height(36.dp)
                                                        ) {
                                                                if (isBackingUpDb) {
                                                                        CircularProgressIndicator(
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                16.dp
                                                                                        ),
                                                                                strokeWidth = 2.dp
                                                                        )
                                                                } else {
                                                                        Icon(
                                                                                Icons.Outlined.Add,
                                                                                contentDescription =
                                                                                        null,
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                18.dp
                                                                                        )
                                                                        )
                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.width(
                                                                                                6.dp
                                                                                        )
                                                                        )
                                                                        Text(
                                                                                "New Backup",
                                                                                fontSize = 13.sp,
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Medium
                                                                        )
                                                                }
                                                        }

                                                        IconButton(
                                                                onClick = {
                                                                        restoreLauncher.launch(
                                                                                "application/zip"
                                                                        )
                                                                },
                                                                enabled = !isRestoring,
                                                                modifier = Modifier.size(36.dp)
                                                        ) {
                                                                Icon(
                                                                        Icons.Default.Restore,
                                                                        contentDescription =
                                                                                "Restore from file",
                                                                        tint =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onSurfaceVariant,
                                                                        modifier =
                                                                                Modifier.size(24.dp)
                                                                )
                                                        }
                                                }
                                        }
                                }

                                // Backup Files List
                                if (backupFiles.isEmpty()) {
                                        item { EmptyBackupsState() }
                                } else {
                                        items(
                                                items =
                                                        if (expandedBackups) backupFiles
                                                        else emptyList(),
                                                key = { it.absolutePath }
                                        ) { file ->
                                                AnimatedVisibility(
                                                        visible = expandedBackups,
                                                        enter = expandVertically() + fadeIn(),
                                                        exit = shrinkVertically() + fadeOut()
                                                ) {
                                                        BackupFileItem(
                                                                file = file,
                                                                onRestore = {
                                                                        scope.launch {
                                                                                isRestoring = true
                                                                                appViewModel
                                                                                        .restoreFromBackupFile(
                                                                                                context,
                                                                                                file
                                                                                        ) { success
                                                                                                ->
                                                                                                isRestoring =
                                                                                                        false
                                                                                                scope
                                                                                                        .launch {
                                                                                                                snackbarHostState
                                                                                                                        .showSnackbar(
                                                                                                                                if (success
                                                                                                                                )
                                                                                                                                        "✓ Restore completed successfully"
                                                                                                                                else
                                                                                                                                        "✗ Restore failed. Please try again"
                                                                                                                        )
                                                                                                        }
                                                                                        }
                                                                        }
                                                                },
                                                                onDelete = {
                                                                        appViewModel
                                                                                .deleteBackupFile(
                                                                                        file
                                                                                )
                                                                },
                                                                onShare = {
                                                                        shareFile(
                                                                                context,
                                                                                file,
                                                                                "Backup"
                                                                        )
                                                                },
                                                                isRestoring = isRestoring
                                                        )
                                                }
                                        }
                                }
                        }
                }
        }
}

@Composable
fun ModernActionCard(
        icon: ImageVector,
        title: String,
        description: String,
        iconTint: Color,
        iconBackground: Color,
        isLoading: Boolean,
        loadingText: String,
        buttonText: String,
        showWarning: Boolean = false,
        onClick: () -> Unit
) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors =
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                        Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                        ) {
                                Box(
                                        modifier =
                                                Modifier.size(48.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(iconBackground),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Icon(
                                                icon,
                                                contentDescription = null,
                                                tint = iconTint,
                                                modifier = Modifier.size(24.dp)
                                        )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                                description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 18.sp
                                        )
                                }
                        }

                        if (showWarning) {
                                Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color =
                                                MaterialTheme.colorScheme.errorContainer.copy(
                                                        alpha = 0.3f
                                                )
                                ) {
                                        Row(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Icon(
                                                        Icons.Outlined.Warning,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                        "This will overwrite current data",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onErrorContainer,
                                                        fontSize = 11.sp
                                                )
                                        }
                                }
                        }

                        Button(
                                onClick = onClick,
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                                if (isLoading) {
                                        CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(loadingText, fontSize = 14.sp)
                                } else {
                                        Text(
                                                buttonText,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                        )
                                }
                        }
                }
        }
}

@Composable
fun BackupFileItem(
        file: File,
        onRestore: () -> Unit,
        onDelete: () -> Unit,
        onShare: () -> Unit,
        isRestoring: Boolean
) {
        var showDeleteDialog by remember { mutableStateOf(false) }
        val fileDate = remember {
                SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                        .format(Date(file.lastModified()))
        }
        val fileSize = remember { String.format("%.2f MB", file.length() / (1024.0 * 1024.0)) }

        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
                Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                                Box(
                                        modifier =
                                                Modifier.size(40.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(
                                                                MaterialTheme.colorScheme
                                                                        .primaryContainer
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Icon(
                                                Icons.Outlined.FolderZip,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                        )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                file.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Text(
                                                        fileSize,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                        fontSize = 11.sp
                                                )
                                                Text(
                                                        "•",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                )
                                                Text(
                                                        fileDate,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                        fontSize = 11.sp
                                                )
                                        }
                                }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                FilledTonalButton(
                                        onClick = onRestore,
                                        enabled = !isRestoring,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                        Icon(
                                                Icons.Outlined.Restore,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Restore", fontSize = 13.sp)
                                }

                                OutlinedIconButton(
                                        onClick = onShare,
                                        modifier = Modifier.size(40.dp),
                                        shape = RoundedCornerShape(8.dp)
                                ) {
                                        Icon(
                                                Icons.Outlined.Share,
                                                contentDescription = "Share",
                                                modifier = Modifier.size(18.dp)
                                        )
                                }

                                OutlinedIconButton(
                                        onClick = { showDeleteDialog = true },
                                        modifier = Modifier.size(40.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors =
                                                IconButtonDefaults.outlinedIconButtonColors(
                                                        contentColor =
                                                                MaterialTheme.colorScheme.error
                                                )
                                ) {
                                        Icon(
                                                Icons.Outlined.Delete,
                                                contentDescription = "Delete",
                                                modifier = Modifier.size(18.dp)
                                        )
                                }
                        }
                }
        }

        if (showDeleteDialog) {
                AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        icon = {
                                Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                )
                        },
                        title = { Text("Delete Backup?") },
                        text = {
                                Text(
                                        "This action cannot be undone. Are you sure you want to delete this backup file?"
                                )
                        },
                        confirmButton = {
                                TextButton(
                                        onClick = {
                                                onDelete()
                                                showDeleteDialog = false
                                        }
                                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                        },
                        dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                        Text("Cancel")
                                }
                        },
                        shape = RoundedCornerShape(16.dp)
                )
        }
}

@Composable
fun EmptyBackupsState() {
        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
        ) {
                Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        Icon(
                                Icons.Outlined.CloudOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).alpha(0.5f),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                                "No backups yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                                "Create your first backup to get started",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                        )
                }
        }
}

fun shareFile(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val intent =
                Intent(Intent.ACTION_SEND).apply {
                        type = "application/zip"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

        context.startActivity(Intent.createChooser(intent, "Share $title"))
}
