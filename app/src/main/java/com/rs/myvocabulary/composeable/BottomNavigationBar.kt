package com.rs.myvocabulary.composeable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

@Composable
fun BottomNavigationBar(
    selectedItem: String,
    tonalElevation: Dp = 8.dp,
    setSelectedItem: (index: String) -> Unit,
) {
    NavigationBar(

        tonalElevation = tonalElevation,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .padding(horizontal = 0.dp, vertical = 0.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        val navItems = listOf(
            NavItem(
                "Vocabulary",
                Icons.Filled.Home,
                Icons.Outlined.Home,
                "home"
            ),
            NavItem(
                "Clause",
                Icons.Filled.Person,
                Icons.Outlined.Person,
                "clause"
            ),
            NavItem(
                "Docs",
                Icons.Filled.Book,
                Icons.Outlined.Book,
                "docs"
            )

        )

        navItems.forEach { item ->
            val isSelected = selectedItem == item.route

            NavigationBarItem(
                modifier = Modifier,
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        modifier = Modifier.size(20.dp)
                    )
                },

                label = {
                    Text(item.title)
                },
                selected = isSelected,
                onClick = {
                    setSelectedItem(item.route)
                }
            )
        }
    }
}