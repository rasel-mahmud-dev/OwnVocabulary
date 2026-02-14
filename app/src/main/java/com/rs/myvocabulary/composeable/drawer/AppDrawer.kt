package com.rs.myvocabulary.composeable.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rs.myvocabulary.database.Label

@Composable
fun AppDrawer(
        drawerState: DrawerState,
        categories: List<Label>,
        readingLists: List<String> = emptyList(),
        onCategoryClick: (Label) -> Unit,
        onWordClick: () -> Unit,
        onFavoritesClick: () -> Unit = {},
        onFrequentClick: () -> Unit = {},
        onReadingListClick: (String) -> Unit = {},
        onAddCategoryClick: () -> Unit = {},
        onAddReadingListClick: () -> Unit = {},
        content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                        drawerTonalElevation = 0.dp
                ) {
                    Column(
                            modifier =
                                    Modifier.padding(horizontal = 12.dp)
                                            .width(280.dp)
                                            .verticalScroll(rememberScrollState())
                    ) {
                        // Header Section
                        DrawerHeader()

                        Spacer(Modifier.height(8.dp))

                        // Main Navigation Items
                        ModernDrawerItem(
                                label = "Word List",
                                icon = Icons.Outlined.Label,
                                selectedIcon = Icons.Filled.Label,
                                selected = false,
                                onClick = onWordClick
                        )

                        ModernDrawerItem(
                                label = "Favorites",
                                icon = Icons.Outlined.Favorite,
                                selectedIcon = Icons.Filled.Favorite,
                                selected = false,
                                onClick = onFavoritesClick
                        )

                        ModernDrawerItem(
                                label = "Frequently Viewed",
                                icon = Icons.Outlined.History,
                                selectedIcon = Icons.Filled.History,
                                selected = false,
                                onClick = onFrequentClick
                        )

                        Spacer(Modifier.height(16.dp))

                        // Categories Section
                        SectionHeader(title = "Categories", onActionClick = onAddCategoryClick)

                        Spacer(Modifier.height(8.dp))

                        if (categories.isEmpty()) {
                            EmptyStateMessage("No categories yet")
                        } else {
                            categories.forEach { category ->
                                CategoryItem(
                                        category = category,
                                        onClick = { onCategoryClick(category) }
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Reading Lists Section
                        SectionHeader(
                                title = "Reading Lists",
                                onActionClick = onAddReadingListClick
                        )

                        Spacer(Modifier.height(8.dp))

                        if (readingLists.isEmpty()) {
                            EmptyStateMessage("No reading lists yet")
                        } else {
                            readingLists.forEach { listName ->
                                ModernDrawerItem(
                                        label = listName,
                                        icon = Icons.Default.AutoStories,
                                        selected = false,
                                        onClick = { onReadingListClick(listName) },
                                        compact = true
                                )
                            }

                            Spacer(Modifier.height(16.dp))
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            },
            drawerState = drawerState,
            content = content
    )
}

@Composable
private fun DrawerHeader() {
    Surface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(
                    text = "My Vocabulary",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                    text = "Build your word collection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ModernDrawerItem(
        label: String,
        icon: ImageVector,
        selectedIcon: ImageVector = icon,
        selected: Boolean,
        onClick: () -> Unit,
        compact: Boolean = false
) {
    NavigationDrawerItem(
            modifier = Modifier.padding(vertical = 2.dp).height(if (compact) 48.dp else 56.dp),
            label = {
                Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            },
            selected = selected,
            icon = {
                Icon(
                        imageVector = if (selected) selectedIcon else icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                )
            },
            onClick = onClick,
            colors =
                    NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedContainerColor = MaterialTheme.colorScheme.surface,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
            shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun CategoryItem(category: Label, onClick: () -> Unit) {
    NavigationDrawerItem(
            modifier = Modifier.padding(vertical = 2.dp).height(48.dp),
            label = { Text(text = category.name, style = MaterialTheme.typography.bodyLarge) },
            selected = false,
            icon = {
                Icon(
                        imageVector = Icons.Default.Label,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint =
                                try {
                                    androidx.compose.ui.graphics.Color(
                                            android.graphics.Color.parseColor(category.color)
                                    )
                                } catch (e: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }
                )
            },
            onClick = onClick,
            colors =
                    NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = MaterialTheme.colorScheme.surface,
                            unselectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
            shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun SectionHeader(title: String, onActionClick: (() -> Unit)? = null) {
    Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
        )

        onActionClick?.let {
            IconButton(onClick = it, modifier = Modifier.size(32.dp)) {
                Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add $title",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(message: String) {
    Surface(
            modifier =
                    Modifier.fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .clip(RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Text(
                text = message,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
