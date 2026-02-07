package com.rs.myvocabulary.composeable

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rs.myvocabulary.database.Label

@Composable
fun Drawer(labels: List<Label>, drawerState: DrawerState, content: @Composable () -> Unit) {
    ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                    val scrollState = rememberScrollState()

                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                        Column(
                                modifier =
                                        Modifier.verticalScroll(scrollState)
                                                .padding(end = 8.dp) // space for scrollbar
                        ) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                    "Note Net",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider()

                            Text(
                                    "Labels",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.titleMedium
                            )

                            labels.forEach {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.Label,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                                it.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontSize = 14.sp
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Text(
                                    "Section 2",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.titleMedium
                            )

                            NavigationDrawerItem(
                                    label = { Text("Settings") },
                                    selected = false,
                                    icon = {
                                        Icon(Icons.Outlined.Settings, contentDescription = null)
                                    },
                                    badge = { Text("20") },
                                    onClick = {}
                            )

                            NavigationDrawerItem(
                                    label = { Text("Help and feedback") },
                                    selected = false,
                                    icon = {
                                        Icon(
                                                Icons.AutoMirrored.Outlined.Help,
                                                contentDescription = null
                                        )
                                    },
                                    onClick = {}
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        // --- Scrollbar overlay ---
                        VerticalScrollbar(
                                modifier =
                                        Modifier.align(Alignment.CenterEnd)
                                                .fillMaxHeight()
                                                .padding(vertical = 8.dp),
                                scrollState = scrollState
                        )
                    }
                }
            }
    ) { content() }
}

@Composable
fun VerticalScrollbar(modifier: Modifier = Modifier, scrollState: ScrollState) {
    val maxScroll = scrollState.maxValue.toFloat().coerceAtLeast(1f)
    val scrollFraction = scrollState.value / maxScroll

    BoxWithConstraints(modifier = modifier.width(8.dp)) {
        val containerHeight = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        val visibleFraction = (containerHeight / (containerHeight + maxScroll)).coerceIn(0.05f, 1f)
        val thumbHeightPx = containerHeight * visibleFraction

        val thumbOffsetPx = (containerHeight - thumbHeightPx) * scrollFraction

        Box(
                modifier =
                        Modifier.fillMaxHeight()
                                .width(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Transparent)
        ) {
            Box(
                    modifier =
                            Modifier.width(6.dp)
                                    .height(with(LocalDensity.current) { thumbHeightPx.toDp() })
                                    .offset(y = with(LocalDensity.current) { thumbOffsetPx.toDp() })
                                    .align(Alignment.TopEnd)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    )
            )
        }
    }
}
