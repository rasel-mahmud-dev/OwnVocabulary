package com.rs.myvocabulary.composeable.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rs.myvocabulary.database.Label

@Composable
fun AppDrawer(
        drawerState: DrawerState,
        categories: List<Label>,
        onCategoryClick: (Label) -> Unit,
        content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet {
                    Column(
                            modifier =
                                    Modifier.padding(horizontal = 8.dp)
                                            .width(270.dp)
                                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                                "Categories",
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.titleLarge
                        )
                        HorizontalDivider()

                        if (categories.isEmpty()) {
                            Text(
                                    "No categories yet",
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            categories.forEach { category ->
                                NavigationDrawerItem(
                                        modifier = Modifier.padding(0.dp).height(30.dp),
                                        label = { Text(category.name, fontSize = 14.sp) },
                                        selected = false,
                                        icon = {
                                            Icon(
                                                    Icons.Default.Label,
                                                    contentDescription = null,
                                                    tint =
                                                            try {
                                                                androidx.compose.ui.graphics.Color(
                                                                        android.graphics.Color
                                                                                .parseColor(
                                                                                        category.color
                                                                                )
                                                                )
                                                            } catch (e: Exception) {
                                                                MaterialTheme.colorScheme.primary
                                                            }
                                            )
                                        },
                                        onClick = { onCategoryClick(category) }
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                    }
                }
            },
            drawerState = drawerState,
            content = content
    )
}
