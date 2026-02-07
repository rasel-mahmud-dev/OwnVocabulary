package com.rs.myvocabulary.composeable.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EditorToolbar(
        modifier: Modifier = Modifier,
        showToolbar: Boolean,
        onClick: (key: String) -> Unit
) {
    Column(
            modifier = modifier.fillMaxWidth().imePadding() // keeps toolbar above keyboard
    ) {
        AnimatedVisibility(
                visible = showToolbar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                    shadowElevation = 8.dp
            ) {
                LazyRow(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Undo / Redo
                    item {
                        MarkdownButton(
                                onClick = { onClick("undo") },
                                label = "Undo",
                                icon = Icons.AutoMirrored.Filled.Undo
                        )
                    }
                    item {
                        MarkdownButton(
                                onClick = { onClick("redo") },
                                label = "Redo",
                                icon = Icons.AutoMirrored.Filled.Redo
                        )
                    }

                    item { Spacer(modifier = Modifier.width(4.dp)) }

                    // Headings
                    item {
                        MarkdownButton(
                                onClick = { onClick("h1") },
                                icon = "H1",
                                label = "Heading 1"
                        )
                    }
                    item {
                        MarkdownButton(
                                onClick = { onClick("h2") },
                                icon = "H2",
                                label = "Heading 2"
                        )
                    }
                    item {
                        MarkdownButton(
                                onClick = { onClick("h3") },
                                icon = "H3",
                                label = "Heading 3"
                        )
                    }

                    item { Spacer(modifier = Modifier.width(4.dp)) }

                    // Text formatting
                    item {
                        MarkdownButton(
                                onClick = { onClick("bold") },
                                icon = Icons.Filled.FormatBold,
                                label = "Bold"
                        )
                    }
                    item {
                        MarkdownButton(
                                onClick = { onClick("italic") },
                                icon = Icons.Filled.FormatItalic,
                                label = "Italic"
                        )
                    }
                    item {
                        MarkdownButton(
                                onClick = { onClick("strike") },
                                icon = Icons.Filled.FormatStrikethrough,
                                label = "Strikethrough"
                        )
                    }
                    item {
                        MarkdownButton(
                                onClick = { onClick("code") },
                                icon = Icons.Filled.Code,
                                label = "Code"
                        )
                    }

                    item { Spacer(modifier = Modifier.width(4.dp)) }

                    // Lists
                    item {
                        MarkdownButton(
                                onClick = { onClick("bullet") },
                                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                                label = "Bullet List"
                        )
                    }
                    item {
                        MarkdownButton(
                                onClick = { onClick("numbered") },
                                icon = Icons.Filled.FormatListNumbered,
                                label = "Numbered List"
                        )
                    }
                    item {
                        MarkdownButton(
                                onClick = { onClick("checklist") },
                                icon = Icons.Filled.CheckBox,
                                label = "Checklist"
                        )
                    }

                    item { Spacer(modifier = Modifier.width(4.dp)) }

                    // Extras
                    item {
                        MarkdownButton(
                                onClick = { onClick("quote") },
                                icon = Icons.Filled.FormatQuote,
                                label = "Quote"
                        )
                    }
                    item {
                        MarkdownButton(
                                onClick = { onClick("link") },
                                icon = Icons.Filled.Link,
                                label = "Link"
                        )
                    }
                    item {
                        MarkdownButton(
                                onClick = { onClick("image") },
                                icon = Icons.Filled.Image,
                                label = "Image"
                        )
                    }
                    item {
                        MarkdownButton(
                                onClick = { onClick("divider") },
                                icon = Icons.Filled.HorizontalRule,
                                label = "Divider"
                        )
                    }
                }
            }
        }
    }
}
