package com.rs.myvocabulary.composeable.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rs.myvocabulary.database.SyncStatus
import com.rs.myvocabulary.database.Word

@Composable
fun WordItemCard(
        word: Word,
        onClick: () -> Unit,
        onPinClick: () -> Unit,
        onAddToReadingList: () -> Unit,
        onRemoveFromReadingList: (String) -> Unit,
        modifier: Modifier = Modifier
) {
        val isPinned = word.isFavorite
        var showMenu by remember { mutableStateOf(false) }

        Card(
                modifier = modifier.fillMaxWidth(),
                colors =
                        CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                shape = RoundedCornerShape(12.dp),
                onClick = onClick,
                elevation =
                        CardDefaults.cardElevation(defaultElevation = if (isPinned) 2.dp else 0.dp)
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                                Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                        if (isPinned) {
                                                Surface(
                                                        color =
                                                                MaterialTheme.colorScheme.primary
                                                                        .copy(alpha = 0.15f),
                                                        shape = CircleShape,
                                                        modifier = Modifier.size(4.dp)
                                                ) {}
                                        }

                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                                Text(
                                                        text = word.word,
                                                        style =
                                                                MaterialTheme.typography
                                                                        .titleMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                )

                                                Box(
                                                        modifier =
                                                                Modifier.width(8.dp)
                                                                        .height(8.dp)
                                                                        .background(
                                                                                if (word.syncStatus ==
                                                                                                SyncStatus
                                                                                                        .SYNCED
                                                                                )
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .primary
                                                                                else
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .error,
                                                                                shape = CircleShape
                                                                        )
                                                ) {}
                                        }
                                }

                                if (word.shortMeaning.isNotEmpty()) {
                                        Text(
                                                text = word.shortMeaning,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                lineHeight = 18.sp
                                        )
                                }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onPinClick, modifier = Modifier.size(32.dp)) {
                                        Icon(
                                                imageVector =
                                                        if (isPinned) Icons.Filled.Favorite
                                                        else Icons.Outlined.FavoriteBorder,
                                                contentDescription =
                                                        if (isPinned) "Unfavorite" else "Favorite",
                                                modifier = Modifier.size(18.dp),
                                                tint =
                                                        if (isPinned) {
                                                                MaterialTheme.colorScheme.primary
                                                        } else {
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant.copy(
                                                                        alpha = 0.5f
                                                                )
                                                        }
                                        )
                                }

                                Box {
                                        IconButton(
                                                onClick = { showMenu = true },
                                                modifier = Modifier.size(32.dp)
                                        ) {
                                                Icon(
                                                        imageVector = Icons.Default.MoreVert,
                                                        contentDescription = "More options",
                                                        modifier = Modifier.size(18.dp),
                                                        tint =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant.copy(
                                                                        alpha = 0.5f
                                                                )
                                                )
                                        }

                                        DropdownMenu(
                                                expanded = showMenu,
                                                onDismissRequest = { showMenu = false }
                                        ) {
                                                DropdownMenuItem(
                                                        text = { Text("Add to reading list") },
                                                        onClick = {
                                                                showMenu = false
                                                                onAddToReadingList()
                                                        },
                                                        leadingIcon = {
                                                                Icon(
                                                                        Icons.Default.Add,
                                                                        contentDescription = null
                                                                )
                                                        }
                                                )

                                                word.assignedReadingLists.forEach { listName ->
                                                        DropdownMenuItem(
                                                                text = {
                                                                        Text(
                                                                                "Remove from $listName"
                                                                        )
                                                                },
                                                                onClick = {
                                                                        showMenu = false
                                                                        onRemoveFromReadingList(
                                                                                listName
                                                                        )
                                                                },
                                                                leadingIcon = {
                                                                        Icon(
                                                                                Icons.Default
                                                                                        .Delete,
                                                                                contentDescription =
                                                                                        null,
                                                                                tint =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .error
                                                                        )
                                                                }
                                                        )
                                                }
                                        }
                                }
                        }
                }
        }
}
