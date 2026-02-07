package com.rs.myvocabulary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
        darkColorScheme(
                primary = Color(0xFFBB86FC),
                onPrimary = Color(0xFF000000),
                secondary = Color(0xFF03DAC6),
                onSecondary = Color(0xFF000000),
                background = Color(0xFF121212),
                onBackground = Color(0xFFE6E1E5),
                surface = Color(0xFF1C1B1F),
                onSurface = Color(0xFFE6E1E5),
                surfaceVariant = Color(0xFF49454F),
                onSurfaceVariant = Color(0xFFCAC4D0),
                outline = Color(0xFF938F99),
                outlineVariant = Color(0xFF49454F),
        )

private val LightColorScheme =
        lightColorScheme(
                primary = Color(0xFF6200EE),
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFF03DAC6),
                onSecondary = Color(0xFF000000),
                background = Color(0xFFFFFBFE),
                onBackground = Color(0xFF1C1B1F),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF1C1B1F),
                surfaceVariant = Color(0xFFE7E0EC),
                onSurfaceVariant = Color(0xFF49454F),
                outline = Color(0xFF79747E),
                outlineVariant = Color(0xFFCAC4D0),
        )

@Composable
fun OwnVocabularyTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
