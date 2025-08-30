package com.rs.ownvocabulary.composeable

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SpatialAudioOff
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SpatialAudioOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ModernNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    selectedItem: String,
    tonalElevation: Dp = 12.dp,
    setSelectedItem: (index: String) -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = tonalElevation,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        val navItems = listOf(
            ModernNavItem(
                "Home",
                Icons.Rounded.Home,
                Icons.Outlined.Home,
                "home"
            ),
            ModernNavItem(
                "Dictionary",
                Icons.Rounded.Book,
                Icons.Outlined.Book,
                "dictionary"
            ),
            ModernNavItem(
                "Analytics",
                Icons.Rounded.Analytics,
                Icons.Outlined.Analytics,
                "analytics"
            ),
            ModernNavItem(
                "Profile",
                Icons.Rounded.Person,
                Icons.Outlined.Person,
                "profile"
            )
        )

        navItems.forEach { item ->
            val isSelected = selectedItem == item.route
            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1f,
                animationSpec = tween(200),
                label = "iconScale"
            )

            val iconColor by animateColorAsState(
                targetValue = if (isSelected) primaryColor else onSurfaceVariant,
                animationSpec = tween(200),
                label = "iconColor"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) primaryColor else onSurfaceVariant,
                animationSpec = tween(200),
                label = "textColor"
            )

            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .scale(iconScale),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = textColor,
                        maxLines = 1
                    )
                },
                selected = isSelected,
                onClick = {
                    setSelectedItem(item.route)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Transparent,
                    selectedTextColor = Color.Transparent,
                    unselectedIconColor = Color.Transparent,
                    unselectedTextColor = Color.Transparent,
                    indicatorColor = Color.Transparent
                ),
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
            )
        }
    }
}
